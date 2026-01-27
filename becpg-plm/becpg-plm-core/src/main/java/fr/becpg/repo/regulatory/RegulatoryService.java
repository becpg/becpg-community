package fr.becpg.repo.regulatory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.CheckSumHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.ComplianceResult.Status;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.repo.variant.filters.VariantFilters;
import fr.becpg.util.MutexFactory;

/**
 * <p>RegulatoryService class.</p>
 *
 * @author Valentin
 */
@Service
public class RegulatoryService {

	/** Constant <code>UNKNOWN="unknown"</code> */
	public static final String UNKNOWN = "unknown";
	
	/** Constant <code>REGULATORY_KEY="regulatory"</code> */
	public static final String REGULATORY_KEY = "regulatory";

	private static final Log logger = LogFactory.getLog(RegulatoryService.class);

	private static final String MESSAGE_NO_CODE_CHARACT = "message.regulatory.charact.noCode";

	private static final String ASYNC_ACTION_URL_PREFIX = "page/entity-data-lists?list=regulatoryList&nodeRef=%s";

	private NodeService nodeService;

	private List<RegulatoryPlugin> regulatoryPlugins;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private FormulationService<FormulatedEntity> formulationService;

	private BatchQueueService batchQueueService;

	private SystemConfigurationService systemConfigurationService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private EntityActivityService entityActivityService;
	
	private MutexFactory mutexFactory;
	
	/**
	 * <p>Constructor for RegulatoryService.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param regulatoryPlugins a {@link java.util.List} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param formulationService a {@link fr.becpg.repo.formulation.FormulationService} object
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 * @param entityActivityService a {@link fr.becpg.repo.activity.EntityActivityService} object
	 * @param mutexFactory a {@link fr.becpg.util.MutexFactory} object
	 */
	public RegulatoryService(@Qualifier("nodeService") NodeService nodeService, List<RegulatoryPlugin> regulatoryPlugins, AlfrescoRepository<RepositoryEntity> alfrescoRepository,
			FormulationService<FormulatedEntity> formulationService, BatchQueueService batchQueueService,
			SystemConfigurationService systemConfigurationService, @Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter, EntityActivityService entityActivityService,
			MutexFactory mutexFactory) {
		super();
		this.nodeService = nodeService;
		this.regulatoryPlugins = regulatoryPlugins;
		this.alfrescoRepository = alfrescoRepository;
		this.formulationService = formulationService;
		this.batchQueueService = batchQueueService;
		this.systemConfigurationService = systemConfigurationService;
		this.policyBehaviourFilter = policyBehaviourFilter;
		this.entityActivityService = entityActivityService;
		this.mutexFactory = mutexFactory;
	}

	private static final int DEFAULT_REGULATORY_BATCH_THREADS = 1;

	private Boolean ingredientAnalysisEnabled() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.decernis.ingredient.analysis.enabled"));
	}

	/**
	 * <p>checkCompliance.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param async a boolean
	 * @return a {@link fr.becpg.repo.regulatory.ComplianceResult} object
	 */
	public ComplianceResult checkCompliance(NodeRef nodeRef, boolean async) {
		policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
		policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
		policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

		ComplianceResult result = new ComplianceResult();

		L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
			ProductData productData = (ProductData) alfrescoRepository.findOne(nodeRef);
			updateProductFromRegulatoryList(productData);
			updateProductFromLinkedSearches(productData);
			RegulatoryContext context = createContext(productData);
			if (!isContextCompatible(context)) {
				result.setStatus(Status.NOT_APPLICABLE);
				logger.debug("Product is not compatible for compliance check");
				return false;
			}
			if (isUpToDate(context)) {
				result.setStatus(Status.UP_TO_DATE);
				logger.debug("Product compliance is up to date");
				return false;
			}
			result.setContext(context);
			if (async) {
				checkComplianceAsync(context, result);
			} else {
				boolean finished = checkComplianceSync(context);
				result.setStatus(finished ? Status.FINISHED : Status.PENDING);
			}
			alfrescoRepository.save(productData);
			if (result.getStatus() == Status.STARTED || result.getStatus() == Status.FINISHED) {
				entityActivityService.postComplianceCheckActivity(productData.getNodeRef());
			}
			return true;
		}), false, true);

		return result;
	}

	private RegulatoryContext createContext(ProductData product) {
		RegulatoryContext context = new RegulatoryContext();
		if (product.getIngList() != null) {
			context.getIngList().addAll(product.getIngList().stream().filter(this::isIngItemValid).toList());
		}
		context.setProduct(product);
		List<RegulatoryBatch> regulatoryBatches = new ArrayList<>();
		regulatoryBatches.addAll(createRegulatoryBatches(context, product));
		for (RegulatoryListDataItem regulatoryListItem : product.getRegulatoryList()) {
			regulatoryBatches.addAll(createRegulatoryBatches(context, regulatoryListItem));
		}
		context.setRegulatoryBatches(regulatoryBatches);
		return context;
	}

	private boolean isIngItemValid(IngListDataItem ingListDataItem) {
		return !DeclarationType.Omit.equals(ingListDataItem.getDeclType());
	}

	private List<RegulatoryBatch> createRegulatoryBatches(RegulatoryContext context, RegulatoryEntity regulatoryEntity) {
		List<RegulatoryBatch> regulatoryBatches = new ArrayList<>();
		List<CountryBatch> countryBatches = createCountryBatches(context, regulatoryEntity);
		List<UsageBatch> usageBatches = createUsageBatches(context, regulatoryEntity);
		for (CountryBatch countryBatch : countryBatches) {
			for (UsageBatch usageBatch : usageBatches) {
				regulatoryBatches.add(new RegulatoryBatch(countryBatch, usageBatch));
			}
		}
		return regulatoryBatches;
	}

	private List<CountryBatch> createCountryBatches(RegulatoryContext context, RegulatoryEntity regulatoryEntity) {
		List<String> countries = new ArrayList<>();
		for (NodeRef countryRef : regulatoryEntity.getRegulatoryCountriesRef()) {
			addCountry(context, countries, countryRef);
		}
		return getPlugin().splitCountries(context, countries);
	}

	private void addCountry(RegulatoryContext context, List<String> countries, NodeRef countryRef) {
		String code = extractCode(countryRef);
		if (code != null && !code.isBlank()) {
			countries.add(code);
			context.addCountry(code, countryRef);
		} else {
			RequirementListDataItem noCodeRequirement = createReqCtrl(null,
					MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT), RequirementType.Tolerated);
			context.getRequirements().add(noCodeRequirement);
		}
	}

	private List<UsageBatch> createUsageBatches(RegulatoryContext context, RegulatoryEntity regulatoryEntity) {
		List<String> usages = new ArrayList<>();
		for (NodeRef usageRef : regulatoryEntity.getRegulatoryUsagesRef()) {
			addUsage(context, usages, usageRef);
		}
		return getPlugin().splitUsages(context, usages);
	}

	private void addUsage(RegulatoryContext context, List<String> usages, NodeRef usageRef) {
		String code = extractCode(usageRef);
		if (code != null && !code.isBlank()) {
			usages.add(code);
			String moduleName = (String) nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_MODULE);
			context.addUsage(code, moduleName);
		} else {
			RequirementListDataItem noCodeRequirement = createReqCtrl(null,
					MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT), RequirementType.Tolerated);
			context.getRequirements().add(noCodeRequirement);
		}
	}

	private String extractCode(NodeRef node) {
		return (String) nodeService.getProperty(node, PLMModel.PROP_REGULATORY_CODE);
	}

	private boolean checkComplianceSync(RegulatoryContext context) {
		ReentrantLock mutex = mutexFactory.getMutex("complianceCheck-" + context.getProduct().getNodeRef());
		if (mutex.tryLock()) {
			try {
				fetchIngredients(context);
				for (RegulatoryBatch regulatoryCheckContext : context.getRegulatoryBatches()) {
					getPlugin().checkRecipe(context, regulatoryCheckContext);
				}
				finalizeRecipeCheck(context, context.getProduct());
				if (isIngRegulatoryListEnabled(context.getProduct())) {
					List<IngRegulatoryListDataItem> ingRegulatoryListDataItems = new ArrayList<>();
					for (RegulatoryBatch regulatoryCheckContext : context.getRegulatoryBatches()) {
						getPlugin().checkIngredients(context, regulatoryCheckContext);
					}
					processRegulatoryList(context.getProduct(), ingRegulatoryListDataItems);
				}
			} finally {
				mutex.unlock();
			}
			return true;
		}
		return false;
	}

	private boolean isIngRegulatoryListEnabled(ProductData productData) {
		return alfrescoRepository.hasDataList(productData, PLMModel.TYPE_ING_REGULATORY_LIST) && productData.getIngRegulatoryList() != null
				&& ingredientAnalysisEnabled();
	}

	private void checkComplianceAsync(RegulatoryContext context, ComplianceResult status) {
		boolean batchStarted = false;
		NodeRef entityNodeRef = context.getProduct().getNodeRef();
		String entityDescription = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE) + " - " + context.getProduct().getName();
		BatchInfo regulatoryBatchInfo = new BatchInfo(String.format("regulatory-%s", entityNodeRef.getId()), "becpg.batch.regulatory",
				entityDescription);
		List<BatchStep<RegulatoryBatch>> steps = new ArrayList<>();
		regulatoryBatchInfo.setBatchUser(AuthenticationUtil.getFullyAuthenticatedUser());
		Integer batchThreads = getPlugin().getBatchThreads();
		regulatoryBatchInfo.setWorkerThreads(batchThreads != null ? batchThreads : DEFAULT_REGULATORY_BATCH_THREADS);
		String batchId = regulatoryBatchInfo.getBatchId();
		regulatoryBatchInfo.setPriority(BatchPriority.VERY_HIGH);
		regulatoryBatchInfo.enableNotifyByMail(REGULATORY_KEY, String.format(ASYNC_ACTION_URL_PREFIX, entityNodeRef.toString()));
		
		BatchStep<RegulatoryBatch> fetchIngredientsStep = new BatchStep<>();
		fetchIngredientsStep.setStepDescId("becpg.batch.regulatory.fetchIng");
		fetchIngredientsStep.setWorkProvider(regulatoryWorkProvider(List.of(new RegulatoryBatch(null, null))));
		fetchIngredientsStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			public void process(RegulatoryBatch regulatoryCheckContext) throws Throwable {
				fetchIngredients(context);
			}
		});
		steps.add(fetchIngredientsStep);
		BatchStep<RegulatoryBatch> recipeStep = new BatchStep<>();
		recipeStep.setStepDescId("becpg.batch.regulatory.recipe");
		recipeStep.setWorkProvider(regulatoryWorkProvider(context.getRegulatoryBatches()));
		recipeStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			public void process(RegulatoryBatch regulatoryCheckContext) throws Throwable {
				getPlugin().checkRecipe(context, regulatoryCheckContext);
			}
		});
		recipeStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void afterStep() {
				policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				ProductData finalProductData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
				finalizeRecipeCheck(context, finalProductData);
				alfrescoRepository.save(finalProductData);
			}
		});
		steps.add(recipeStep);
		if (isIngRegulatoryListEnabled(context.getProduct())) {
			BatchStep<RegulatoryBatch> ingredientsStep = new BatchStep<>();
			ingredientsStep.setWorkProvider(regulatoryWorkProvider(context.getRegulatoryBatches()));
			ingredientsStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
				public void process(RegulatoryBatch regulatoryCheckContext) throws Throwable {
					getPlugin().checkIngredients(context, regulatoryCheckContext);
				}
			});
			ingredientsStep.setBatchStepListener(new BatchStepAdapter() {
				@Override
				public void afterStep() {
					policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					ProductData finalProductData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
					processRegulatoryList(finalProductData, context.getIngRegulatoryListDataItems());
					alfrescoRepository.save(finalProductData);
				}

			});
			ingredientsStep.setStepDescId("becpg.batch.regulatory.ingredients");
			steps.add(ingredientsStep);
		}
		batchStarted = batchQueueService.queueBatch(regulatoryBatchInfo, steps);
		status.setStatus(batchStarted ? Status.STARTED : Status.PENDING);
		status.setBatchId(batchId);
	}

	private void finalizeRecipeCheck(RegulatoryContext context, ProductData productData) {
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
	
	private boolean hasError(List<RequirementListDataItem> reqList) {
		for (RequirementListDataItem req : reqList) {
			if (RequirementType.Forbidden.equals(req.getReqType()) && RequirementDataType.Formulation.equals(req.getReqDataType())) {
				return true;
			}
		}
		return false;
	}

	private void updateChecksums(RegulatoryContext context, ProductData productData) {
		String checkSum = createContextCheckum(context);
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

	private void processRegulatoryList(ProductData productData, List<IngRegulatoryListDataItem> ingRegulatoryListDataItems) {
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

	private IngRegulatoryListDataItem mergeItems(ProductData productData, List<IngRegulatoryListDataItem> items) {

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

		String citation = items.stream().map(item -> item.getCitation().getDefaultValue()).distinct().sorted().collect(Collectors.joining(";;"));
		String usages = items.stream().map(item -> item.getUsages().getDefaultValue()).distinct().sorted().collect(Collectors.joining(";;"));
		String restrictionLevels = items.stream().map(item -> item.getRestrictionLevels().getDefaultValue())
				.filter(r -> r != null && !r.isBlank() && !r.equals("-")).distinct().sorted().collect(Collectors.joining(";;"));
		String resultIndicators = items.stream().map(item -> item.getResultIndicator().getDefaultValue()).distinct().sorted()
				.collect(Collectors.joining(";;"));
		String precautions = items.stream().filter(i -> i.getPrecautions() != null).map(item -> item.getPrecautions().getDefaultValue()).distinct()
				.sorted().collect(Collectors.joining(";;"));

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

	private List<NodeRef> extractSources(NodeRef ing, ProductData formulatedProduct) {

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

	private BatchProcessWorkProvider<RegulatoryBatch> regulatoryWorkProvider(List<RegulatoryBatch> regulatoryBatches) {
		Iterator<RegulatoryBatch> it = regulatoryBatches.iterator();
		return new BatchProcessWorkProvider<RegulatoryBatch>() {
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

	private RegulatoryPlugin getPlugin() {
		return regulatoryPlugins.get(0);
	}

	private void fetchIngredients(RegulatoryContext context) {
		for (IngListDataItem ingListDataItem : context.getIngList()) {
			if (ingListDataItem.getIng() != null) {
				IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
				String rid = ingItem.getRegulatoryCode();
				if (rid == null || rid.isEmpty()) {
					rid = getPlugin().fetchIngredientId(ingListDataItem);
					if (logger.isDebugEnabled()) {
						logger.debug("Try to fetch ingredient ID: " + ingItem.getCharactName());
					}
					if (rid != null) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found ingredient ID: " + ingItem.getCharactName() + ", ID: " + rid);
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Could not find ingredient ID: " + ingItem.getCharactName());
						}
						rid = UNKNOWN;
					}
					ingItem.setRegulatoryCode(rid);
					alfrescoRepository.save(ingItem);
				}
				if (UNKNOWN.equals(rid)) {
					RequirementListDataItem noCodeRequirement = createReqCtrl(ingListDataItem,
							MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT), RequirementType.Tolerated);
					context.getRequirements().add(noCodeRequirement);
				}
			}
		}
	}

	private void updateProductFromRegulatoryList(ProductData product) {
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

	private void updateProductFromLinkedSearches(ProductData formulatedProduct) {
		updateRegulatoryEntityFromLinkedSearches(formulatedProduct);
		for (RegulatoryListDataItem regList : formulatedProduct.getRegulatoryList()) {
			updateRegulatoryEntityFromLinkedSearches(regList);
		}
	}

	private void updateRegulatoryEntityFromLinkedSearches(RegulatoryEntity regulatoryEntity) {
		List<NodeRef> linkedSearches = extractLinkedSearches(regulatoryEntity.getRegulatoryCountriesRef());
		regulatoryEntity.getRegulatoryCountriesRef().clear();
		for (NodeRef linkedSearch : linkedSearches) {
			if (!regulatoryEntity.getRegulatoryCountriesRef().contains(linkedSearch)) {
				regulatoryEntity.getRegulatoryCountriesRef().add(linkedSearch);
			}
		}
	}

	private List<NodeRef> extractLinkedSearches(List<NodeRef> regulatoryCountriesRef) {
		List<NodeRef> linkedSearches = new ArrayList<>();
		for (NodeRef regulatoryCountry : regulatoryCountriesRef) {
			linkedSearches.addAll(extractLinkedSearches(regulatoryCountry));
		}
		return linkedSearches;
	}

	private List<NodeRef> extractLinkedSearches(NodeRef regulatoryCountry) {
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

	private boolean isUpToDate(RegulatoryContext context) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		if (context.getProduct().getRegulatoryFormulatedDate() == null || context.getProduct().getRegulatoryFormulatedDate().before(cal.getTime())) {
			return false;
		}
		if (!CheckSumHelper.isSameChecksum(REGULATORY_KEY, context.getProduct().getRequirementChecksum(), createContextCheckum(context))) {
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

	private String createContextCheckum(RegulatoryContext context) {
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
			context.getIngList().stream().map(ing -> ing.getNodeRef().toString() + ing.getIng() + ing.getValue()).sorted()
					.forEach(checksumBuilder::append);
		}

		return checksumBuilder.toString();
	}

	private String createRequirementChecksum(Set<String> countries, Set<String> usages) {
		StringBuilder key = new StringBuilder();
		if (countries != null) {
			countries.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
		}
		if (usages != null) {
			usages.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
		}
		return key.toString();
	}

	private boolean isContextCompatible(RegulatoryContext context) {
		if (context.getProduct().getRegulatoryMode() == null || RegulatoryMode.DISABLED.equals(context.getProduct().getRegulatoryMode())) {
			return false;
		}
		return true;
	}

	private RequirementListDataItem createReqCtrl(IngListDataItem ingListDataItem, MLText reqCtrlMessage, RequirementType reqType) {
		RequirementListDataItem reqCtrlItem = new RequirementListDataItem();
		reqCtrlItem.setReqType(reqType);
		if (ingListDataItem != null) {
			reqCtrlItem.setCharact(ingListDataItem.getNodeRef() != null ? ingListDataItem.getNodeRef() : ingListDataItem.getIng());
			reqCtrlItem.addSource(ingListDataItem.getIng());
		}
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqCtrlMessage);
		reqCtrlItem.setFormulationChainId(REGULATORY_KEY);
		return reqCtrlItem;
	}

}
