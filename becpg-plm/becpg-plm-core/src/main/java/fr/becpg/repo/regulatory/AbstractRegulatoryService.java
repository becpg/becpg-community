package fr.becpg.repo.regulatory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.CheckSumHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.decernis.DecernisHelper;
import fr.becpg.repo.regulatory.decernis.RegulatoryBatch;
import fr.becpg.repo.regulatory.decernis.RegulatoryContext;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.repo.variant.filters.VariantFilters;
import fr.becpg.util.MutexFactory;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class AbstractRegulatoryService {
    private static final Log logger = LogFactory.getLog(AbstractRegulatoryService.class);

    /** Constant <code>UNKNOWN="unknown"</code> */
    public static final String UNKNOWN = "unknown";

    /** Constant <code>REGULATORY_KEY="regulatory"</code> */
    public static final String REGULATORY_KEY = "regulatory";

    protected static final String POST_URL = "POST url: ";

    protected static final String ASYNC_ACTION_URL_PREFIX = "page/entity-data-lists?list=regulatoryList&nodeRef=%s";

    protected static final int DEFAULT_REGULATORY_BATCH_THREADS = 1;

    protected NodeService nodeService;

    protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

    protected FormulationService<FormulatedEntity> formulationService;

    protected BatchQueueService batchQueueService;

    protected BehaviourFilter policyBehaviourFilter;

    protected EntityActivityService entityActivityService;

    protected MutexFactory mutexFactory;

    protected SystemConfigurationService systemConfigurationService;

    /**
     * <p>Constructor for RegulatoryService.</p>
     *
     * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
     * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
     * @param formulationService a {@link fr.becpg.repo.formulation.FormulationService} object
     * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
     * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
     * @param entityActivityService a {@link fr.becpg.repo.activity.EntityActivityService} object
     * @param mutexFactory a {@link fr.becpg.util.MutexFactory} object
     */
    protected AbstractRegulatoryService(NodeService nodeService,
                                        AlfrescoRepository<RepositoryEntity> alfrescoRepository,
                                        FormulationService<FormulatedEntity> formulationService,
                                        BatchQueueService batchQueueService,
                                        BehaviourFilter policyBehaviourFilter,
                                        EntityActivityService entityActivityService,
                                        MutexFactory mutexFactory,
                                        SystemConfigurationService systemConfigurationService) {
        super();
        this.nodeService = nodeService;
        this.alfrescoRepository = alfrescoRepository;
        this.formulationService = formulationService;
        this.batchQueueService = batchQueueService;
        this.policyBehaviourFilter = policyBehaviourFilter;
        this.entityActivityService = entityActivityService;
        this.mutexFactory = mutexFactory;
        this.systemConfigurationService = systemConfigurationService;
    }

    protected Integer getBatchThreads() {
        String confValue = systemConfigurationService.confValue("beCPG.regulatory.batchThreads");
        if (confValue != null && !confValue.isBlank()) {
            return Integer.parseInt(confValue);
        }
        return null;
    }

    protected abstract String serverUrl();

    /**
     * <p>addInfoReqCtrl.</p>
     *
     * @return a boolean
     */
    protected boolean addInfoReqCtrl() {
        return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.specification.addInfoReqCtrl"));
    }

    public Boolean doCheck(boolean async, ComplianceResult result, ProductData productData) {
        updateProductFromRegulatoryList(productData);
        updateProductFromLinkedSearches(productData);
        RegulatoryContext context = createContext(productData);
        if (isUpToDate(context)) {
            result.setStatus(ComplianceResult.Status.UP_TO_DATE);
            logger.debug("Product compliance is up to date");
            return false;
        }
        result.setContext(context);
        if (async) {
            checkComplianceAsync(context, result);
        } else {
            boolean finished = checkComplianceSync(context);
            result.setStatus(finished ? ComplianceResult.Status.FINISHED : ComplianceResult.Status.PENDING);
        }
        alfrescoRepository.save(productData);
        if (result.getStatus() == ComplianceResult.Status.STARTED || result.getStatus() == ComplianceResult.Status.FINISHED) {
            entityActivityService.postComplianceCheckActivity(productData.getNodeRef());
        }
        return true;
    }

    protected RegulatoryContext createContext(ProductData product) {
        RegulatoryContext context = new RegulatoryContext();
        if (product.getIngList() != null) {
            context.getIngList().addAll(product.getIngList().stream().filter(this::isIngItemValid).toList());
        }
        context.setProduct(product);
        return context;
    }

    protected boolean isIngItemValid(IngListDataItem ingListDataItem) {
        return !DeclarationType.Omit.equals(ingListDataItem.getDeclType());
    }

    protected String extractCode(NodeRef node) {
        return (String) nodeService.getProperty(node, PLMModel.PROP_REGULATORY_CODE);
    }

    protected boolean checkComplianceSync(RegulatoryContext context) {
        ReentrantLock mutex = mutexFactory.getMutex("complianceCheck-" + context.getProduct().getNodeRef().getId());
        if (mutex.tryLock()) {
            try {
                delegateSyncComplianceCheck(context);
            } finally {
                mutex.unlock();
            }
            return true;
        }
        return false;
    }

    protected abstract void delegateSyncComplianceCheck(RegulatoryContext context);

    protected abstract void checkComplianceAsync(RegulatoryContext context, ComplianceResult status);

    protected abstract boolean isEnabled();

    protected void finalizeRecipeCheck(RegulatoryContext context, ProductData productData) {
        if (productData.getReqCtrlList() == null) {
            productData.setReqCtrlList(new ArrayList<>());
        }
        productData.getReqCtrlList().addAll(context.getRequirements());
        formulationService.formulate(productData, REGULATORY_KEY);
        if (!hasError(context.getRequirements())) {
            updateChecksums(context, productData);
            productData.setRegulatoryFormulatedDate(new Date());
        } else {
            productData.setRequirementChecksum(null);
        }
        String regulatoryRecipeId = context.getRegulatoryRecipeId();
        if (regulatoryRecipeId != null && !regulatoryRecipeId.isBlank()) {
            productData.setRegulatoryRecipeId(regulatoryRecipeId);
            for (RegulatoryListDataItem regulatoryListItem : productData.getRegulatoryList()) {
                regulatoryListItem.setRegulatoryRecipeId(regulatoryRecipeId);
            }
        }
    }

    protected boolean hasError(List<RequirementListDataItem> reqList) {
        for (RequirementListDataItem req : reqList) {
            if (RequirementType.Forbidden.equals(req.getReqType()) && RequirementDataType.Formulation.equals(req.getReqDataType())) {
                return true;
            }
        }
        return false;
    }

    protected void updateChecksums(RegulatoryContext context, ProductData productData) {
        String checkSum = createContextChecksum(context);
        productData.setRequirementChecksum(CheckSumHelper.updateChecksum(REGULATORY_KEY, productData.getRequirementChecksum(), checkSum));
        for (RegulatoryListDataItem regulatoryListDataItem : productData.getRegulatoryList()) {
            Set<String> itemCountries = regulatoryListDataItem.getRegulatoryCountriesRef().stream().map(this::extractCode)
                    .collect(Collectors.toSet());
            Set<String> itemUsages = regulatoryListDataItem.getRegulatoryUsagesRef().stream().map(this::extractCode).collect(Collectors.toSet());
            String itemCheckSum = createRequirementChecksum(itemCountries, itemUsages);
            regulatoryListDataItem.setRequirementChecksum(
                    CheckSumHelper.updateChecksum(REGULATORY_KEY, regulatoryListDataItem.getRequirementChecksum(), itemCheckSum));
        }
    }

    /**
     * <p>processRegulatoryList.</p>
     *
     * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
     * @param ingRegulatoryListDataItems a {@link java.util.List} object
     */
    protected void processRegulatoryList(ProductData productData, List<IngRegulatoryListDataItem> ingRegulatoryListDataItems) {
        Map<NodeRef, Map<NodeRef, List<IngRegulatoryListDataItem>>> groupedByIngAndCountry = ingRegulatoryListDataItems.stream().collect(
                Collectors.groupingBy(IngRegulatoryListDataItem::getIng, Collectors.groupingBy(item -> item.getRegulatoryCountries().get(0))));

        for (Map<NodeRef, List<IngRegulatoryListDataItem>> countryGroup : groupedByIngAndCountry.values()) {
            for (List<IngRegulatoryListDataItem> items : countryGroup.values()) {
                mergeItems(productData, items);
            }
        }

        List<IngRegulatoryListDataItem> filteredList = productData
                .getIngRegulatoryList().stream().filter(
                        item -> ingRegulatoryListDataItems.stream()
                                .anyMatch(ingRegulatoryListDataItem -> Objects.equals(item.getIng(), ingRegulatoryListDataItem.getIng())
                                        && Objects.equals(item.getRegulatoryCountries(), ingRegulatoryListDataItem.getRegulatoryCountries())))
                .toList();

        productData.getIngRegulatoryList().retainAll(filteredList);

    }

    protected IngRegulatoryListDataItem mergeItems(ProductData productData, List<IngRegulatoryListDataItem> items) {

        // Assuming all items have the same ing and country
        IngRegulatoryListDataItem sampleItem = items.get(0);

        IngRegulatoryListDataItem mergedItem = productData.getIngRegulatoryList().stream()
                .filter(item -> Objects.equals(item.getIng(), sampleItem.getIng())
                        && Objects.equals(item.getRegulatoryCountries(), sampleItem.getRegulatoryCountries()))
                .findFirst().orElseGet(() -> {
                    IngRegulatoryListDataItem newItem = new IngRegulatoryListDataItem();
                    newItem.setIng(sampleItem.getIng());
                    newItem.setRegulatoryCountries(sampleItem.getRegulatoryCountries());
                    productData.getIngRegulatoryList().add(newItem);
                    return newItem;
                });

        String citation = items.stream().filter(item -> item.getCitation() != null)
                .map(item -> item.getCitation().getDefaultValue())
                .distinct().sorted().collect(Collectors.joining(";;"));

        String usages = items.stream().filter(item -> item.getUsages() != null)
                .map(item -> item.getUsages().getDefaultValue())
                .distinct().sorted().collect(Collectors.joining(";;"));

        String restrictionLevels = items.stream().filter(item -> item.getRestrictionLevels() != null)
                .map(item -> item.getRestrictionLevels().getDefaultValue())
                .filter(r -> r != null && !r.isBlank() && !r.equals("-"))
                .distinct().sorted().collect(Collectors.joining(";;"));

        String resultIndicators = items.stream().filter(item -> item.getResultIndicator() != null)
                .map(item -> item.getResultIndicator().getDefaultValue())
                .distinct().sorted().collect(Collectors.joining(";;"));

        String precautions = items.stream().filter(item -> item.getPrecautions() != null)
                .map(item -> item.getPrecautions().getDefaultValue())
                .distinct().sorted().collect(Collectors.joining(";;"));

        mergedItem.setResultIndicator(new MLText(resultIndicators));
        mergedItem.setCitation(new MLText(citation));
        mergedItem.setUsages(new MLText(usages));
        mergedItem.setRestrictionLevels(new MLText(restrictionLevels));
        mergedItem.setPrecautions(new MLText(precautions));
        boolean mlAware = MLPropertyInterceptor.setMLAware(true);
        try {
            MLText comment = (MLText) nodeService.getProperty(mergedItem.getIng(), PLMModel.PROP_REGULATORY_COMMENT);
            mergedItem.setComment(comment);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        mergedItem.setSources(extractSources(mergedItem.getIng(), productData));

        return mergedItem;
    }

    protected List<NodeRef> extractSources(NodeRef ing, ProductData formulatedProduct) {

        Set<NodeRef> sources = new HashSet<>();

        List<CompoListDataItem> compoList = formulatedProduct
                .getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()));

        if (compoList != null) {
            for (CompoListDataItem compoItem : compoList) {

                if ((compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
                    ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());
                    if (componentProductData.getIngList() != null) {

                        for (IngListDataItem ingListDataItem : componentProductData.getIngList()) {
                            if (ingListDataItem.getIng().equals(ing)) {
                                sources.add(compoItem.getProduct());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(sources);
    }

    protected BatchProcessWorkProvider<RegulatoryBatch> regulatoryWorkProvider(List<RegulatoryBatch> regulatoryBatches) {
        Iterator<RegulatoryBatch> it = regulatoryBatches.iterator();
        return new BatchProcessWorkProvider<>() {
            @Override
            public long getTotalEstimatedWorkSizeLong() {
                return getTotalEstimatedWorkSize();
            }

            @Override
            public int getTotalEstimatedWorkSize() {
                return regulatoryBatches.size();
            }

            @Override
            public Collection<RegulatoryBatch> getNextWork() {
                if (it.hasNext()) {
                    return List.of(it.next());
                }
                return List.of();
            }
        };
    }

    protected void updateProductFromRegulatoryList(ProductData product) {
        Set<NodeRef> countries = new HashSet<>();
        Set<NodeRef> usages = new HashSet<>();
        for (RegulatoryListDataItem item : product.getRegulatoryList()) {
            if (SystemState.Valid.equals(item.getRegulatoryState())) {
                countries.addAll(item.getRegulatoryCountriesRef());
                usages.addAll(item.getRegulatoryUsagesRef());
            }
        }
        if (!countries.isEmpty() || !usages.isEmpty()) {
            product.getRegulatoryCountriesRef().clear();
            product.getRegulatoryCountriesRef().addAll(countries);
            product.getRegulatoryUsagesRef().clear();
            product.getRegulatoryUsagesRef().addAll(usages);
        }
    }

    /**
     * <p>updateProductFromLinkedSearches.</p>
     *
     * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
     */
    protected void updateProductFromLinkedSearches(ProductData formulatedProduct) {
        updateRegulatoryEntityFromLinkedSearches(formulatedProduct);
        for (RegulatoryListDataItem regList : formulatedProduct.getRegulatoryList()) {
            updateRegulatoryEntityFromLinkedSearches(regList);
        }
    }

    /**
     * <p>updateRegulatoryEntityFromLinkedSearches.</p>
     *
     * @param regulatoryEntity a {@link fr.becpg.repo.regulatory.RegulatoryEntity} object
     */
    protected void updateRegulatoryEntityFromLinkedSearches(RegulatoryEntity regulatoryEntity) {
        List<NodeRef> linkedSearches = extractLinkedSearches(regulatoryEntity.getRegulatoryCountriesRef());
        regulatoryEntity.getRegulatoryCountriesRef().clear();
        for (NodeRef linkedSearch : linkedSearches) {
            if (!regulatoryEntity.getRegulatoryCountriesRef().contains(linkedSearch)) {
                regulatoryEntity.getRegulatoryCountriesRef().add(linkedSearch);
            }
        }
    }

    /**
     * <p>extractLinkedSearches.</p>
     *
     * @param regulatoryCountriesRef a {@link java.util.List} object
     * @return a {@link java.util.List} object
     */
    protected List<NodeRef> extractLinkedSearches(List<NodeRef> regulatoryCountriesRef) {
        List<NodeRef> linkedSearches = new ArrayList<>();
        for (NodeRef regulatoryCountry : regulatoryCountriesRef) {
            linkedSearches.addAll(extractLinkedSearches(regulatoryCountry));
        }
        return linkedSearches;
    }

    /**
     * <p>extractLinkedSearches.</p>
     *
     * @param regulatoryCountry a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @return a {@link java.util.List} object
     */
    protected List<NodeRef> extractLinkedSearches(NodeRef regulatoryCountry) {
        List<NodeRef> linkedSearches = new ArrayList<>();
        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(regulatoryCountry, BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION);
        if (!targetAssocs.isEmpty()) {
            for (AssociationRef targetAssoc : targetAssocs) {
                linkedSearches.addAll(extractLinkedSearches(targetAssoc.getTargetRef()));
            }
        } else {
            linkedSearches.add(regulatoryCountry);
        }
        return linkedSearches;
    }

    /**
     * <p>isUpToDate.</p>
     *
     * @param context a {@link RegulatoryContext} object
     * @return a boolean
     */
    protected boolean isUpToDate(RegulatoryContext context) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        if (context.getProduct().getRegulatoryFormulatedDate() == null || context.getProduct().getRegulatoryFormulatedDate().before(cal.getTime())) {
            return false;
        }
        if (!CheckSumHelper.isSameChecksum(REGULATORY_KEY, context.getProduct().getRequirementChecksum(), createContextChecksum(context))) {
            return false;
        }
        for (RegulatoryListDataItem regulatoryListDataItem : context.getProduct().getRegulatoryList()) {
            Set<String> countries = regulatoryListDataItem.getRegulatoryCountriesRef().stream().map(this::extractCode).collect(Collectors.toSet());
            Set<String> usages = regulatoryListDataItem.getRegulatoryUsagesRef().stream().map(this::extractCode).collect(Collectors.toSet());
            if (!CheckSumHelper.isSameChecksum(REGULATORY_KEY, regulatoryListDataItem.getRequirementChecksum(),
                    createRequirementChecksum(countries, usages))) {
                return false;
            }
        }
        return true;
    }

    protected String createContextChecksum(RegulatoryContext context) {
        Set<String> countries = context.getProduct().getRegulatoryCountriesRef().stream().map(this::extractCode).collect(Collectors.toSet());
        Set<String> usages = context.getProduct().getRegulatoryUsagesRef().stream().map(this::extractCode).collect(Collectors.toSet());
        if (!context.getProduct().getRegulatoryUsages().isEmpty() && !context.getProduct().getRegulatoryCountries().isEmpty()) {
            countries = context.getProduct().getRegulatoryCountries().stream().collect(Collectors.toSet());
            usages = context.getProduct().getRegulatoryUsages().stream().collect(Collectors.toSet());
        }
        StringBuilder checksumBuilder = new StringBuilder();
        checksumBuilder.append(createRequirementChecksum(countries, usages));
        for (RegulatoryListDataItem regulatoryListDataItem : context.getProduct().getRegulatoryList()) {
            Set<String> itemCountries = regulatoryListDataItem.getRegulatoryCountriesRef().stream().map(this::extractCode)
                    .collect(Collectors.toSet());
            Set<String> itemUsages = regulatoryListDataItem.getRegulatoryUsagesRef().stream().map(this::extractCode).collect(Collectors.toSet());
            checksumBuilder.append(createRequirementChecksum(itemCountries, itemUsages));
        }

        if (context.getIngList() != null) {
            context.getIngList().stream().map(ing -> ing.getNodeRef().getId() + ing.getIng() + ing.getValue()).sorted()
                    .forEach(checksumBuilder::append);
        }

        return CheckSumHelper.hashChecksum(checksumBuilder.toString());
    }

    private String createRequirementChecksum(Set<String> countries, Set<String> usages) {
        StringBuilder key = new StringBuilder();
        if (countries != null) {
            countries.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
        }
        if (usages != null) {
            usages.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
        }
        return CheckSumHelper.hashChecksum(key.toString());
    }

    protected void tracePostRequest(JSONObject recipePayload, String url) {
        if (logger.isTraceEnabled()) {
            logger.trace(POST_URL + url + " body: " + recipePayload);
        }
    }

    protected HttpEntity<String> createEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(DecernisHelper.getToken().trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(body, headers);
    }
}
