package fr.becpg.repo.regulatory.becpg.regulatory;

import com.google.common.collect.Streams;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.regulatory.AbstractRegulatoryService;
import fr.becpg.repo.regulatory.IngredientRegulatoryCodes;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.decernis.RegulatoryBatch;
import fr.becpg.repo.regulatory.decernis.RegulatoryContext;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.util.MutexFactory;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

@Service
public class BecpgRegulatoryService extends AbstractRegulatoryService {
    private static final Log logger = LogFactory.getLog(BecpgRegulatoryService.class);
    public static final String ERROR_PREFIX = "Error during becpg-regulatory analysis: ";
    private static final String MESSAGE_REGULATORY_ERROR = "message.regulatory.error";

    private final RemoteEntityService remoteEntityService;
    private final ProductDataEntityJsonService productDataEntityJsonService;
    private final BecpgRegulatoryAuthenticationService authenticationService;

    public BecpgRegulatoryService(@Qualifier("nodeService") NodeService nodeService,
                                  AlfrescoRepository<RepositoryEntity> alfrescoRepository,
                                  FormulationService<FormulatedEntity> formulationService,
                                  BatchQueueService batchQueueService,
                                  @Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter,
                                  EntityActivityService entityActivityService,
                                  MutexFactory mutexFactory,
                                  SystemConfigurationService systemConfigurationService,
                                  ProductDataEntityJsonService productDataEntityJsonService,
                                  RemoteEntityService remoteEntityService,
                                  BecpgRegulatoryAuthenticationService authenticationService) {
        super(nodeService, alfrescoRepository, formulationService, batchQueueService, policyBehaviourFilter,
                entityActivityService, mutexFactory, systemConfigurationService);
        this.productDataEntityJsonService = productDataEntityJsonService;
        this.remoteEntityService = remoteEntityService;
        this.authenticationService = authenticationService;
    }

    @Override
    protected String serverUrl() {
        return systemConfigurationService.confValue("beCPG.regulatory.serverUrl");
    }

    /**
     * In OAuth2 mode, delegates token acquisition to {@link BecpgRegulatoryAuthenticationService}.
     * In ticket mode, no bearer token is needed (the ticket is set as a header).
     */
    @Override
    protected Optional<String> getToken() {
        return authenticationService.getOauth2Token();
    }

    @Override
    protected String generateError(Exception e) {
        return "Error while performing regulatory check: " + cleanError(e.getMessage());
    }

    @Override
    protected Log logger() {
        return logger;
    }

    /**
     * Adds the delegated Alfresco authentication ticket so the regulatory service
     * can authenticate the caller against this repository. The header name matches
     * the one expected by the regulatory core ({@code BECPG_TICKET}). Only applied
     * in {@code ticket} authentication mode (see {@code beCPG.regulatory.authMode}).
     *
     * @param headers the headers being built for the outgoing request
     */
    @Override
    protected void customizeHeaders(HttpHeaders headers) {
        authenticationService.getBecpgTicket().ifPresent(ticket -> headers.set("BECPG_TICKET", ticket));
    }

    @Override
    protected List<BatchStep<RegulatoryBatch>> delegatePrepareAsyncSteps(RegulatoryContext context, NodeRef entityNodeRef) {
        BatchStep<RegulatoryBatch> postToCheckStep = new BatchStep<>();
        postToCheckStep.setStepDescId("becpg.batch.regulatory.post");
        postToCheckStep.setWorkProvider(regulatoryWorkProvider(List.of(new RegulatoryBatch(null, null))));
        postToCheckStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
            public void process(RegulatoryBatch regulatoryCheckContext) {
                checkRecipe(context);
            }
        });
        postToCheckStep.setBatchStepListener(new BatchStepAdapter() {
            @Override
            public void afterStep() {
                policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
                policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
                policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
                ProductData finalProductData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
                finalizeRecipeCheck(context, finalProductData);
                processRegulatoryList(finalProductData, context.getIngRegulatoryListDataItems());
                alfrescoRepository.save(finalProductData);
            }
        });
        return List.of(postToCheckStep);
    }

    @Override
    protected boolean isEnabled() {
        return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.regulatory.enabled")) &&
                serverUrl() != null && !serverUrl().isBlank();
    }

    @Override
    protected void delegateSyncComplianceCheck(RegulatoryContext context) {
        checkRecipe(context);
        finalizeRecipeCheck(context, context.getProduct());
        processRegulatoryList(context.getProduct(), context.getIngRegulatoryListDataItems());
    }

    private void checkRecipe(RegulatoryContext context) {
        boolean analysisPassed = false;
        int retries = 2;
        while (!analysisPassed && retries >= 0) {
            try {
                retries--;
                analysisPassed = analyze(context);
            } catch (RestClientException e) {
                if (retries <= 0)
                    context.getRequirements().addAll(generateErrorsForAllRegulatoryPairs(context, e));
                logger.error(ERROR_PREFIX + cleanError(e.getMessage()) + ", try restarting request...");
            } catch (Exception e) {
                context.getRequirements().addAll(generateErrorsForAllRegulatoryPairs(context, e));
            }
        }
        if (analysisPassed)
            logger.info("Regulatory check for " + context.getProduct().getName() + " finished");
    }

    private List<RequirementListDataItem> generateErrorsForAllRegulatoryPairs(RegulatoryContext context, Exception e) {
        logger.error("Error during beCPG regulatory analysis: " + cleanError(e.getMessage()), e);
        Map<NodeRef, String> nodeRefRegCodeMap = productDataEntityJsonService.fillNodeRefDictionary(context.getProduct().getRegulatoryList());

        return context.getProduct().getRegulatoryList().stream().flatMap(regElement -> generateReqCtrlErrors(
                regElement.getRegulatoryCountriesRef().stream().map(nodeRefRegCodeMap::get).filter(Objects::nonNull).toList(),
                regElement.getRegulatoryUsagesRef().stream().map(nodeRefRegCodeMap::get).filter(Objects::nonNull).toList(),
                MLTextHelper.getI18NMessage(MESSAGE_REGULATORY_ERROR, generateError(e))
        )).toList();
    }

    private boolean analyze(RegulatoryContext context) throws JSONException {
        JSONObject recipePayload = fetchEntityAsJson(context.getProduct().getNodeRef(), buildRecipeParams());
        String becpgRegulatoryUrl = serverUrl() + "/v1/regulatory/check";

        tracePostRequest(recipePayload, becpgRegulatoryUrl);
        String analysisResult = RestTemplateHelper.getRestTemplateLongTimeout().postForObject(
                becpgRegulatoryUrl, createEntity(recipePayload.toString()), String.class, new HashMap<>());
        if (analysisResult == null)
            return false;
        JSONObject json = new JSONObject(analysisResult);

        // The beCPG regulatory service resolves the ingredients in the same call as the analysis, where Decernis
        // has a dedicated endpoint (see DecernisRegulatoryService#fetchIngredients).
        productDataEntityJsonService.extractIngIdToRegulatoryCodes(json).forEach(this::storeBecpgCodes);

        List<IngRegulatoryListDataItem> parsedIngRegulatoryElements = productDataEntityJsonService.deserializeDatalist(IngRegulatoryListDataItem.class, json).toList();

        List<RequirementListDataItem> parsedRequirements = productDataEntityJsonService.deserializeDatalist(RequirementListDataItem.class, json).toList();
        Stream<RequirementListDataItem> alertsForNotCoveredCountryToUsagePairs = productDataEntityJsonService.createAlertsForNotCoveredCountryToUsagePairs(
                context.getProduct().getRegulatoryList(), parsedIngRegulatoryElements);
        Stream<RequirementListDataItem> alertsForNotCoveredIngredients = productDataEntityJsonService.createAlertsForNotCoveredIngredients(
                context.getProduct().getIngList(), parsedIngRegulatoryElements);

        List<IngRegulatoryListDataItem> filteredIngRegulatoryElements = parsedIngRegulatoryElements.stream()
                .filter(item -> !isEmptyRegulatoryItem(item))
                .toList();
        context.getIngRegulatoryListDataItems().addAll(filteredIngRegulatoryElements);

        List<RequirementListDataItem> allRequirementAlerts = Streams.concat(
                parsedRequirements.stream(), alertsForNotCoveredCountryToUsagePairs, alertsForNotCoveredIngredients
        ).toList();
        context.getRequirements().addAll(allRequirementAlerts);

        return true;
    }

    /**
     * Stores the codes returned by the beCPG regulatory service on the ingredient, next to the codes of the
     * other regulatory services, and saves only when something changed.
     *
     * @param ingredientId the ingredient node id
     * @param becpgCodes the comma separated codes returned for this ingredient
     */
    private void storeBecpgCodes(String ingredientId, String becpgCodes) {
        IngItem ingItem = (IngItem) alfrescoRepository.findOne(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, ingredientId));
        IngredientRegulatoryCodes present = IngredientRegulatoryCodes.parse(ingItem.getRegulatoryCode());
        IngredientRegulatoryCodes updated = present.withBecpgCodes(IngredientRegulatoryCodes.parse(becpgCodes).tokens());
        if (!updated.equals(present)) {
            ingItem.setRegulatoryCode(updated.format());
            alfrescoRepository.save(ingItem);
        }
    }

    /**
     * Elements without requirements are indicating that ingredient was resolved and providing regulatoryCode.
     */
    private boolean isEmptyRegulatoryItem(IngRegulatoryListDataItem item) {
        return (item.getCitation() == null || item.getCitation().isEmpty()) &&
                (item.getRestrictionLevels() == null || item.getRestrictionLevels().isEmpty()) &&
                (item.getResultIndicator() == null || item.getResultIndicator().isEmpty()) &&
                (item.getPrecautions() == null || item.getPrecautions().isEmpty()) &&
                (item.getComment() == null || item.getComment().isEmpty());
    }

    private RemoteParams buildRecipeParams() {
        RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
        params.setFilteredProperties(Set.of(
                ContentModel.PROP_SYS_NAME, PLMModel.PROP_INGLIST_QTY_PERC,
                PLMModel.ASSOC_INGLIST_ING,
                PLMModel.ASSOC_REGULATORY_USAGE_REF, PLMModel.ASSOC_REGULATORY_COUNTRIES
        ));
        params.setFilteredAssocProperties(Map.of(
                PLMModel.ASSOC_INGLIST_ING, Set.of(
                        PLMModel.PROP_CAS_NUMBER,
                        PLMModel.PROP_CE_NUMBER,
                        PLMModel.PROP_EC_NUMBER,
                        PLMModel.PROP_FDA_NUMBER,
                        PLMModel.PROP_FEMA_NUMBER,
                        PLMModel.PROP_FL_NUMBER,
                        PLMModel.PROP_ING_TYPE_V2,
                        PLMModel.TYPE_ING_TYPE_ITEM,
                        PLMModel.PROP_ING_TYPE_DEC_THRESHOLD,
                        PLMModel.PROP_PLURAL_LEGAL_NAME
                ),
                PLMModel.ASSOC_REGULATORY_USAGE_REF, Set.of(PLMModel.PROP_REGULATORY_CODE),
                PLMModel.ASSOC_REGULATORY_COUNTRIES, Set.of(PLMModel.PROP_REGULATORY_CODE, PLMModel.PROP_GEO_ORIGIN_ISOCODE)
        ));
        return params;
    }

    private JSONObject fetchEntityAsJson(NodeRef nodeRef, RemoteParams params) throws JSONException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        remoteEntityService.getEntity(nodeRef, out, params);
        return new JSONObject(out.toString(StandardCharsets.UTF_8));
    }
}