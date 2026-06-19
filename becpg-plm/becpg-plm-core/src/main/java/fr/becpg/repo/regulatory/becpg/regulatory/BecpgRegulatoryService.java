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
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.regulatory.AbstractRegulatoryService;
import fr.becpg.repo.regulatory.RequirementDataType;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private ProductDataEntityJsonService productDataEntityJsonService;
    private RemoteEntityService remoteEntityService;

    public BecpgRegulatoryService(@Qualifier("nodeService") NodeService nodeService,
                                     AlfrescoRepository<RepositoryEntity> alfrescoRepository,
                                     FormulationService<FormulatedEntity> formulationService,
                                     BatchQueueService batchQueueService,
                                     @Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter,
                                     EntityActivityService entityActivityService,
                                     MutexFactory mutexFactory,
                                     SystemConfigurationService systemConfigurationService,
                                     ProductDataEntityJsonService productDataEntityJsonService,
                                     RemoteEntityService remoteEntityService) {
        super(nodeService, alfrescoRepository, formulationService, batchQueueService, policyBehaviourFilter,
                entityActivityService, mutexFactory, systemConfigurationService);
        this.productDataEntityJsonService = productDataEntityJsonService;
        this.remoteEntityService = remoteEntityService;
    }

    @Override
    protected String serverUrl() {
        return systemConfigurationService.confValue("beCPG.regulatory.serverUrl");
    }

    @Override
    protected Optional<String> getToken() {
        String token = systemConfigurationService.confValue("beCPG.regulatory.token");
        if (token == null)
            return Optional.empty();
        token = token.trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    @Override
    protected String generateError(Exception e) {
        return "Error while performing regulatory check: " + cleanError(e.getMessage());
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
        if (logger.isDebugEnabled()) {
            logger.debug("Launch becpg regulatory in mode :" + context.getRegulatoryMode());
        }

        boolean analysisPassed = false;
        int retries = 2;
        while (!analysisPassed && retries >= 0) {
            try {
                retries--;
                analysisPassed = analyze(context);
            } catch (RestClientException e) {
                if (retries <= 0)
                    context.getRequirements().addAll(generateErrorsforAllRegulatoryPairs(context, e));
                logger.error(ERROR_PREFIX + cleanError(e.getMessage()) + ", try restarting request...");
            } catch (Exception e) {
                context.getRequirements().addAll(generateErrorsforAllRegulatoryPairs(context, e));
            }
        }
    }

    private List<RequirementListDataItem> generateErrorsforAllRegulatoryPairs(RegulatoryContext context, Exception e) {
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

        List<IngRegulatoryListDataItem> parsedIngRegulatoryElements = productDataEntityJsonService.deserializeDatalist(IngRegulatoryListDataItem.class, json).toList();
        context.getIngRegulatoryListDataItems().addAll(parsedIngRegulatoryElements);

        List<RequirementListDataItem> parsedRequirements = productDataEntityJsonService.deserializeDatalist(RequirementListDataItem.class, json).toList();
        Stream<RequirementListDataItem> alertsForNotCoveredCountryToUsagePairs = productDataEntityJsonService.createAlertsForNotCoveredCountryToUsagePairs(
                context.getProduct().getRegulatoryList(), parsedRequirements);
        Stream<RequirementListDataItem> alertsForNotCoveredIngredients = productDataEntityJsonService.createAlertsForNotCoveredIngredients(
                context.getProduct().getIngList(), parsedIngRegulatoryElements);
        List<RequirementListDataItem> allRequirementAlerts = Streams.concat(
                parsedRequirements.stream(), alertsForNotCoveredCountryToUsagePairs, alertsForNotCoveredIngredients
        ).toList();
        context.getRequirements().addAll(allRequirementAlerts);

        return true;
    }

    private RemoteParams buildRecipeParams() {
        RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
        params.setFilteredProperties(Set.of(
                ContentModel.PROP_SYS_NAME, PLMModel.PROP_INGLIST_QTY_PERC,
                PLMModel.ASSOC_INGLIST_ING, PLMModel.ASSOC_REGULATORY_USAGE_REF, PLMModel.ASSOC_REGULATORY_COUNTRIES
        ));
        params.setFilteredAssocProperties(Map.of(
                PLMModel.ASSOC_INGLIST_ING, Set.of(PLMModel.PROP_CAS_NUMBER, PLMModel.PROP_CE_NUMBER, PLMModel.PROP_REGULATORY_CODE),
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
