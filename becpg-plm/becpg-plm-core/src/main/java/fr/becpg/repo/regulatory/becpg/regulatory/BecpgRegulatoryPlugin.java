package fr.becpg.repo.regulatory.becpg.regulatory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.regulatory.RegulatoryPlugin;
import fr.becpg.repo.regulatory.decernis.*;
import fr.becpg.repo.system.SystemConfigurationService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Plugin serializes {@link NodeRef} and sends it to the {@code becpg-regulatory} /api/v1/regulatory/check endpoint
 * <p>
 * As response, json-serialized entity is expected. When received - is used to fill {@code reqCtrlList} and {@code ingRegulatoryList} in the regulatory context.
 * Lists are then merged in the product during formulation.
 */
@Service
public class BecpgRegulatoryPlugin implements RegulatoryPlugin {
    private static final Log logger = LogFactory.getLog(BecpgRegulatoryPlugin.class);
	private static final String POST_URL = "POST url: ";

    private ProductDataJSONService productDataJSONService;
    private RemoteEntityService remoteEntityService;
    private SystemConfigurationService systemConfigurationService;

    private String serverUrl() {
        return systemConfigurationService.confValue("beCPG.regulatory.becpg-regulatory.serverUrl");
    }

    public BecpgRegulatoryPlugin(SystemConfigurationService systemConfigurationService,
                                 RemoteEntityService remoteEntityService,
                                 ProductDataJSONService productDataJSONService) {
        super();
        this.systemConfigurationService = systemConfigurationService;
        this.productDataJSONService = productDataJSONService;
        this.remoteEntityService = remoteEntityService;
    }

    /** {@inheritDoc} */
    @Override
    public void checkRecipe(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Launch becpg regulatory in mode :" + context.getRegulatoryMode());
        }

        if (RegulatoryMode.BOTH.equals(context.getRegulatoryMode()) ||
                RegulatoryMode.BECPG_ONLY.equals(context.getRegulatoryMode())) {
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
                    logger.error("Error during becpg-regulatory analysis: " + DecernisHelper.cleanError(e.getMessage())
                            + ", try restarting request...");
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Integer getBatchThreads() {
        String confValue = systemConfigurationService.confValue("beCPG.regulatory.batchThreads");
        if (confValue != null && !confValue.isBlank()) {
            return Integer.parseInt(confValue);
        }
        return null;
    }

    private void tracePostRequest(JSONObject recipePayload, String url) {
        if (logger.isTraceEnabled()) {
            logger.trace(POST_URL + url + " body: " + recipePayload);
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

        ProductData deserialized = new ProductData();
        deserialized.setReqCtrlList(context.getRequirements());
        deserialized.setIngRegulatoryList(context.getIngRegulatoryListDataItems());

        productDataJSONService.fillProductDataFromJson(deserialized, context.getProduct(), new JSONObject(analysisResult));
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

    /**
     * <p>createEntity.</p>
     *
     * @param body a {@link java.lang.String} object
     * @return a {@link org.springframework.http.HttpEntity} object
     */
    protected HttpEntity<String> createEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(DecernisHelper.getToken().trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(body, headers);
    }

    /** {@inheritDoc} */
    @Override
    public void checkIngredients(RegulatoryContext context, RegulatoryBatch checkContext) {
        throw new RuntimeException("Only relevant for Decernis interactions");
    }

    /** {@inheritDoc} */
    @Override
    public String fetchIngredientId(IngListDataItem ingListDataItem) {
        throw new RuntimeException("Only relevant for Decernis interactions");
    }

    /** {@inheritDoc} */
    @Override
    public List<CountryBatch> splitCountries(RegulatoryContext context, List<String> countries) {
        throw new RuntimeException("Only relevant for Decernis interactions");
    }

    /** {@inheritDoc} */
    @Override
    public List<UsageBatch> splitUsages(RegulatoryContext context, List<String> usages) {
        throw new RuntimeException("Only relevant for Decernis interactions");
    }
}
