package fr.becpg.repo.regulatory.becpg.regulatory;

import com.google.common.collect.Streams;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.authentication.BeCPGTicketService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class BecpgRegulatoryService extends AbstractRegulatoryService {
    private static final Log logger = LogFactory.getLog(BecpgRegulatoryService.class);
    public static final String ERROR_PREFIX = "Error during becpg-regulatory analysis: ";
    private static final String MESSAGE_REGULATORY_ERROR = "message.regulatory.error";

    private ProductDataEntityJsonService productDataEntityJsonService;
    private RemoteEntityService remoteEntityService;
    private BeCPGTicketService beCPGTicketService;

    private volatile String cachedAccessToken;
    private volatile long tokenExpiryEpochMs;

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
                                     BeCPGTicketService beCPGTicketService) {
        super(nodeService, alfrescoRepository, formulationService, batchQueueService, policyBehaviourFilter,
                entityActivityService, mutexFactory, systemConfigurationService);
        this.productDataEntityJsonService = productDataEntityJsonService;
        this.remoteEntityService = remoteEntityService;
        this.beCPGTicketService = beCPGTicketService;
    }

    @Override
    protected String serverUrl() {
        return systemConfigurationService.confValue("beCPG.regulatory.serverUrl");
    }

    @Override
    protected String getToken() {
        if (isOAuth2Mode()) {
            return fetchOAuth2Token();
        }
        // ticket mode: no bearer token, authentication is carried by the BECPG_TICKET header
        return null;
    }

    @Override
    protected String generateError(Exception e) {
        return "Error while performing regulatory check: " + cleanError(e.getMessage());
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
        if (!isTicketMode()) {
            return;
        }
        try {
            String authToken = beCPGTicketService.getCurrentAuthToken();
            if (authToken != null && !authToken.isBlank()) {
                headers.set("BECPG_TICKET", authToken);
            }
        } catch (Exception e) {
            logger.warn("Unable to build BECPG_TICKET for regulatory request: " + e.getMessage());
        }
    }

    /**
     * Returns the configured authentication mode for calls to the regulatory
     * service. Either {@code ticket} (delegated Alfresco ticket, default) or
     * {@code oauth2} (Keycloak client_credentials bearer).
     *
     * @return the lower-cased authentication mode, never null
     */
    private String authMode() {
        String mode = systemConfigurationService.confValue("beCPG.regulatory.authMode");
        return mode != null && !mode.isBlank() ? mode.trim().toLowerCase() : "ticket";
    }

    private boolean isTicketMode() {
        return "ticket".equals(authMode());
    }

    private boolean isOAuth2Mode() {
        return "oauth2".equals(authMode());
    }

    /**
     * Obtains a bearer token from Keycloak using the OAuth2 client_credentials
     * flow, mirroring the regulatory batch. Tokens are cached and refreshed
     * shortly before expiry. The client secret is read from configuration and
     * is therefore never persisted in code.
     *
     * @return a valid access token, or null when one could not be obtained
     */
    private synchronized String fetchOAuth2Token() {
        long now = System.currentTimeMillis();
        if (cachedAccessToken != null && now < tokenExpiryEpochMs) {
            return cachedAccessToken;
        }

        String tokenUrl = systemConfigurationService.confValue("beCPG.regulatory.oauth2.tokenUrl");
        String clientId = systemConfigurationService.confValue("beCPG.regulatory.oauth2.clientId");
        String clientSecret = systemConfigurationService.confValue("beCPG.regulatory.oauth2.clientSecret");
        String scope = systemConfigurationService.confValue("beCPG.regulatory.oauth2.scope");

        if (tokenUrl == null || tokenUrl.isBlank() || clientId == null || clientId.isBlank()) {
            logger.warn("OAuth2 mode enabled but beCPG.regulatory.oauth2.tokenUrl/clientId are not configured");
            return null;
        }

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", clientId);
            if (clientSecret != null && !clientSecret.isBlank()) {
                form.add("client_secret", clientSecret);
            }
            if (scope != null && !scope.isBlank()) {
                form.add("scope", scope);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

            String response = RestTemplateHelper.getRestTemplateLongTimeout().postForObject(tokenUrl, request, String.class);
            JSONObject json = new JSONObject(response);
            String accessToken = json.getString("access_token");
            int expiresIn = json.optInt("expires_in", 300);

            cachedAccessToken = accessToken;
            tokenExpiryEpochMs = now + (Math.max(expiresIn - 30, 30) * 1000L);
            if (logger.isDebugEnabled()) {
                logger.debug("Obtained regulatory OAuth2 token, expires in " + expiresIn + "s");
            }
            return accessToken;
        } catch (Exception e) {
            // Do not call cleanError() here: it resolves getToken(), which would
            // re-enter this method in OAuth2 mode. The thrown message carries the
            // token endpoint response, not the request body, so no secret leaks.
            logger.error("Unable to obtain OAuth2 token for regulatory service: " + e.getMessage(), e);
            cachedAccessToken = null;
            tokenExpiryEpochMs = 0;
            return null;
        }
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
        try {
            checkRecipe(context);
        } catch (Exception e) {
            logger.error(ERROR_PREFIX + cleanError(e.getMessage()), e);
            RequirementListDataItem req = RequirementListDataItem.forbidden()
                    .withMessage(MLTextHelper.getI18NMessage(MESSAGE_REGULATORY_ERROR, generateError(e)))
                    .ofDataType(RequirementDataType.Formulation)
                    .withFormulationChainId(REGULATORY_KEY);
            context.getRequirements().add(req);
        }
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
                if (retries <= 0) {
                    throw e;
                }
                logger.error(ERROR_PREFIX + cleanError(e.getMessage()) + ", try restarting request...");
            }
        }
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
