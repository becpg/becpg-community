package fr.becpg.repo.regulatory.becpg.regulatory;

import com.google.common.collect.Lists;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.RegulatoryPlugin;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.regulatory.decernis.*;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final String MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND = "message.regulatory.usage-to-country.missing";

    private RemoteEntityService remoteEntityService;
    private SystemConfigurationService systemConfigurationService;
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
    private NodeService nodeService;

    private String serverUrl() {
        return systemConfigurationService.confValue("beCPG.regulatory.becpg-regulatory.serverUrl");
    }

    public BecpgRegulatoryPlugin(SystemConfigurationService systemConfigurationService,
                                 AlfrescoRepository<RepositoryEntity> alfrescoRepository,
                                 RemoteEntityService remoteEntityService,
                                 @Qualifier("nodeService") NodeService nodeService) {
        super();
        this.systemConfigurationService = systemConfigurationService;
        this.alfrescoRepository = alfrescoRepository;
        this.remoteEntityService = remoteEntityService;
        this.nodeService = nodeService;
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
                becpgRegulatoryUrl, createEntity(recipePayload.toString()), String.class, new HashMap<>()
        );
        if (analysisResult == null)
            return false;

        JSONObject receivedFromService = new JSONObject(analysisResult);
        updateContextualProductFromJSONObject(context, receivedFromService);
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

    private void updateContextualProductFromJSONObject(RegulatoryContext context, JSONObject json) {
        HashSet<IngListDataItem> allIngredients = new HashSet<>(context.getProduct().getIngList());

        // "COUNTRY - USAGE" : Pair(country.noderef, usage.noderef)
        Map<String, Pair<NodeRef, NodeRef>> pairCodeToRefs = context.getProduct().getRegulatoryList().stream()
                .flatMap(regListItem -> Lists.cartesianProduct(
                        regListItem.getRegulatoryCountriesRef(), regListItem.getRegulatoryUsagesRef()).stream()
                ).collect(Collectors.toMap(
                        countryUsage -> getCode(countryUsage.get(0)) + " - " + getCode(countryUsage.get(1)),
                        countryUsage -> Pair.of(countryUsage.get(0), countryUsage.get(1)),
                        (val1, val2) -> val1
                ));

        JSONObject datalists = json.getJSONObject("datalists");

        if (datalists.has(qnameToString(PLMModel.TYPE_ING_REGULATORY_LIST))) {
            JSONArray ingRegList = datalists.getJSONArray(qnameToString(PLMModel.TYPE_ING_REGULATORY_LIST));
            for (int i = 0; i < ingRegList.length(); i++) {
                JSONObject itemAttributes = ingRegList.getJSONObject(i).optJSONObject("attributes");
                IngRegulatoryListDataItem dataItem = parseIngRegulatoryItem(context, itemAttributes);
                if (dataItem != null) {
                    context.getIngRegulatoryListDataItems().add(dataItem);
                    // mark ing as handled by regulatory service
                    allIngredients.removeIf(ingListDataItem -> ingListDataItem.getIng().getId().equals(dataItem.getIng().getId()));
                }
            }
        }

        if (datalists.has(qnameToString(PLMModel.TYPE_REQCTRLLIST))) {
            JSONArray reqCtrlList = datalists.getJSONArray(qnameToString(PLMModel.TYPE_REQCTRLLIST));
            for (int i = 0; i < reqCtrlList.length(); i++) {
                JSONObject itemAttributes = reqCtrlList.getJSONObject(i).optJSONObject("attributes");
                RequirementListDataItem dataItem = parseReqCtrlItem(context, itemAttributes);
                if (dataItem != null) {
                    context.getRequirements().add(dataItem);
                    // mark country-usage pair as handled by regulatory service
                    pairCodeToRefs.remove(dataItem.getRegulatoryCode());
                }
            }
        }

        createReqCtrlsForMissingElements(context, allIngredients, pairCodeToRefs);
    }

    /**
     * Manual parsing of the JSON representing reqCtrlList element
     */
    private RequirementListDataItem parseReqCtrlItem(RegulatoryContext context, JSONObject attrs) {
        if (attrs == null)
            return null;

        RequirementListDataItem dataItem = new RequirementListDataItem();

        if (attrs.has(qnameToString(PLMModel.PROP_RCL_REQ_TYPE)))
            dataItem.setReqType(RequirementType.fromString(attrs.getString(qnameToString(PLMModel.PROP_RCL_REQ_TYPE))));
        if (attrs.has(qnameToString(PLMModel.PROP_RCL_REQ_DATA_TYPE)))
            dataItem.setReqDataType(RequirementDataType.fromString(attrs.getString(qnameToString(PLMModel.PROP_RCL_REQ_DATA_TYPE))));
        if (attrs.has(qnameToString(PLMModel.PROP_RCL_REQ_MESSAGE)))
            dataItem.setReqMlMessage(new MLText(attrs.getString(qnameToString(PLMModel.PROP_RCL_REQ_MESSAGE))));
        if (attrs.has(qnameToString(PLMModel.PROP_RCL_REQ_MAX_QTY)))
            dataItem.setReqMaxQty(attrs.getDouble(qnameToString(PLMModel.PROP_RCL_REQ_MAX_QTY)));
        if (attrs.has(qnameToString(PLMModel.PROP_REGULATORY_CODE)))
            dataItem.setRegulatoryCode(attrs.getString(qnameToString(PLMModel.PROP_REGULATORY_CODE)));
        if (attrs.has(qnameToString(PLMModel.PROP_RCL_FORMULATION_CHAIN_ID)))
            dataItem.setFormulationChainId(attrs.getString(qnameToString(PLMModel.PROP_RCL_FORMULATION_CHAIN_ID)));
        if (attrs.has(qnameToString(PLMModel.ASSOC_RCL_CHARACT)))
            getIngListElemNodeRef(attrs.getJSONObject(qnameToString(PLMModel.ASSOC_RCL_CHARACT)).getString("id"), context.getProduct().getIngList())
                    .ifPresent(dataItem::setCharact);
        if (attrs.has(qnameToString(PLMModel.PROP_RCL_ERROR_LOG)))
            dataItem.setErrorLog(attrs.getString(qnameToString(PLMModel.PROP_RCL_ERROR_LOG)));
        if (attrs.has(qnameToString(PLMModel.PROP_RCL_SOURCES_V2))) {
            JSONArray sources = attrs.getJSONArray(qnameToString(PLMModel.PROP_RCL_SOURCES_V2));
            for (int i = 0; i < sources.length(); i++) {
                getIngListElemNodeRef(sources.getJSONObject(i).getString("id"), context.getProduct().getIngList())
                        .ifPresent(dataItem::addSource);
            }
        }
        return dataItem;
    }

    /**
     * Manual parsing of the JSON representing ingRegulatoryList element
     */
    private IngRegulatoryListDataItem parseIngRegulatoryItem(RegulatoryContext context, JSONObject attrs) {
        if (attrs == null)
            return null;

        IngRegulatoryListDataItem dataItem = new IngRegulatoryListDataItem();

        if (attrs.has(qnameToString(PLMModel.ASSOC_IRL_ING)))
            getIngNodeRef(attrs.getJSONObject(qnameToString(PLMModel.ASSOC_IRL_ING)).getString("id"), context.getProduct().getIngList())
                    .ifPresent(dataItem::setIng);
        if (attrs.has(qnameToString(PLMModel.PROP_IRL_CITATION)))
            dataItem.setCitation(new MLText(attrs.getString(qnameToString(PLMModel.PROP_IRL_CITATION))));
        if (attrs.has(qnameToString(PLMModel.PROP_IRL_RESTRICTION_LEVELS)))
            dataItem.setRestrictionLevels(new MLText(attrs.getString(qnameToString(PLMModel.PROP_IRL_RESTRICTION_LEVELS))));
        if (attrs.has(qnameToString(PLMModel.PROP_IRL_PRECAUTIONS)))
            dataItem.setPrecautions(new MLText(attrs.getString(qnameToString(PLMModel.PROP_IRL_PRECAUTIONS))));
        if (attrs.has(qnameToString(PLMModel.PROP_IRL_RESULT_INDICATOR)))
            dataItem.setResultIndicator(new MLText(attrs.getString(qnameToString(PLMModel.PROP_IRL_RESULT_INDICATOR))));
        if (attrs.has(qnameToString(PLMModel.PROP_REGULATORY_COMMENT)))
            dataItem.setComment(new MLText(attrs.getString(qnameToString(PLMModel.PROP_REGULATORY_COMMENT))));
        if (attrs.has(qnameToString(PLMModel.PROP_IRL_USAGES)))
            dataItem.setUsages(new MLText(attrs.getString(qnameToString(PLMModel.PROP_IRL_USAGES))));
        if (attrs.has(qnameToString(PLMModel.ASSOC_REGULATORY_COUNTRIES))) {
            JSONArray countries = attrs.getJSONArray(qnameToString(PLMModel.ASSOC_REGULATORY_COUNTRIES));
            List<NodeRef> countryRefs = new ArrayList<>();
            for (int i = 0; i < countries.length(); i++) {
                getRegulatoryCountryNodeRef(countries.getJSONObject(i).getString("id"), context.getProduct().getRegulatoryList())
                        .ifPresent(countryRefs::add);
            }
            dataItem.setRegulatoryCountries(countryRefs);
        }
        if (attrs.has(qnameToString(PLMModel.ASSOC_REGULATORY_USAGE_REF))) {
            JSONArray usages = attrs.getJSONArray(qnameToString(PLMModel.ASSOC_REGULATORY_USAGE_REF));
            List<NodeRef> usageRefs = new ArrayList<>();
            for (int i = 0; i < usages.length(); i++) {
                getRegulatoryUsageNodeRef(usages.getJSONObject(i).getString("id"), context.getProduct().getRegulatoryList())
                        .ifPresent(usageRefs::add);
            }
            dataItem.setRegulatoryUsages(usageRefs);
        }
        return dataItem;
    }

    /**
     * Fill additional reqCtrl items:
     * - for each ingredient that was not handled
     * - for each COUNTRY - USAGE pair that was not handled
     */
    private void createReqCtrlsForMissingElements(RegulatoryContext context, HashSet<IngListDataItem> allIngredients,
                                                  Map<String, Pair<NodeRef, NodeRef>> pairCodeToRefs) {
        Stream<RequirementListDataItem> notListedIngredients = allIngredients.stream().map(ingItem ->
                createReqCtrl(ingItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING), RequirementType.Tolerated));

        Stream<RequirementListDataItem> notListedPairs = pairCodeToRefs.entrySet().stream().map(entry -> {
            RequirementListDataItem item = new RequirementListDataItem();
            item.setReqType(RequirementType.Tolerated);
            item.setRegulatoryCode(entry.getKey()); // "COUNTRY - USAGE"
            item.addSource(entry.getValue().getLeft()); // country nodeRef
            item.addSource(entry.getValue().getRight()); // usage nodeRef
            item.setReqDataType(RequirementDataType.Specification);
            item.setReqMlMessage(MLTextHelper.getI18NMessage(MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND));
            item.setFormulationChainId(DecernisRegulatoryService.REGULATORY_KEY);
            return item;
        });

        Stream.concat(notListedPairs, notListedIngredients).forEach(context.getRequirements()::add);
    }

    private String getCode(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, PLMModel.PROP_REGULATORY_CODE);
    }

    private static String qnameToString(QName qname) {
        return "bcpg:" + qname.getLocalName();
    }

    private Optional<NodeRef> getIngNodeRef(String id, List<IngListDataItem> ingList) {
        return ingList.stream().filter(elem -> elem.getIng().getId().equals(id)).findFirst().map(IngListDataItem::getIng);
    }

    private Optional<NodeRef> getIngListElemNodeRef(String id, List<IngListDataItem> ingList) {
        return ingList.stream().filter(elem -> elem.getNodeRef().getId().equals(id)).findFirst().map(IngListDataItem::getNodeRef);
    }

    private Optional<NodeRef> getRegulatoryCountryNodeRef(String id, List<RegulatoryListDataItem> regulatoryList) {
        return regulatoryList.stream().flatMap(elem -> elem.getRegulatoryCountriesRef().stream())
                .filter(country -> country.getId().equals(id)).findFirst();
    }

    private Optional<NodeRef> getRegulatoryUsageNodeRef(String id, List<RegulatoryListDataItem> regulatoryList) {
        return regulatoryList.stream().flatMap(elem -> elem.getRegulatoryUsagesRef().stream())
                .filter(usage -> usage.getId().equals(id)).findFirst();
    }

    /** @deprecated findOne returns cached product without recent updates, NodeRefs on the reqCtrl list elements are causing conflicts during formulation */
    @Deprecated
    private void updateContextualProductUsingEntityImporter(RegulatoryContext context, JSONObject receivedFromService) {
        JSONObject wrappedResult = new JSONObject().put("entity", receivedFromService);
        InputStream inputStream = new ByteArrayInputStream(wrappedResult.toString().getBytes(StandardCharsets.UTF_8));
        NodeRef updatedEntity = remoteEntityService.createOrUpdateEntity(
                null, inputStream, new RemoteParams(RemoteEntityFormat.json));
        context.setProduct((ProductData) alfrescoRepository.findOne(updatedEntity));
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
