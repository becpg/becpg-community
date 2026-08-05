package fr.becpg.repo.regulatory.decernis;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import java.text.NumberFormat;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.regulatory.AbstractRegulatoryService;
import fr.becpg.repo.regulatory.RegulatoryHelper;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductDataDecernisJsonService {
    private static final Logger logger = LoggerFactory.getLogger(ProductDataDecernisJsonService.class);

    /**
     * Constant <code>FUNCTION="function"</code>
     */
    private static final String FUNCTION = "function";

    /**
     * Constant <code>COUNTRY="country"</code>
     */
    private static final String COUNTRY = "country";

    /**
     * Constant <code>USAGE_ON_LIST="usageOnList"</code>
     */
    private static final String USAGE_ON_LIST = "usageOnList";

    /**
     * Constant <code>COMMENTS="comments"</code>
     */
    private static final String COMMENTS = "comments";

    /**
     * Constant <code>TABULAR_REPORT="tabularReport"</code>
     */
    private static final String TABULAR_REPORT = "tabularReport";

    /**
     * Constant <code>DID="did"</code>
     */
    private static final String DID = "did";

    /**
     * Constant <code>MODULE_SUFFIX=" module"</code>
     */
    public static final String MODULE_SUFFIX = " module";

    /**
     * Constant <code>NOT_APPLICABLE="NA"</code>
     */
    public static final String NOT_APPLICABLE = "NA";

    /**
     * Constant <code>RESULT_INDICATOR="resultIndicator"</code>
     */
    private static final String RESULT_INDICATOR = "resultIndicator";

    /**
     * Constant <code>RECIPE_REPORT="recipeReport"</code>
     */
    private static final String RECIPE_REPORT = "recipeReport";

    /**
     * Constant <code>RECIPE_ANALAYSIS_REPORT="recipeAnalaysisReport"</code>
     */
    private static final String RECIPE_ANALAYSIS_REPORT = "recipeAnalaysisReport";

    /**
     * Constant <code>PARAM_COUNTRY="COUNTRY"</code>
     */
    private static final String PARAM_COUNTRY = COUNTRY;

    /**
     * Constant <code>PARAM_NAME="name"</code>
     */
    private static final String PARAM_NAME = "name";

    /**
     * Constant <code>PARAM_PHRASE="phrase"</code>
     */
    private static final String PARAM_PHRASE = "phrase";

    /**
     * Constant <code>PARAM_SCOPE_ID="scope_id"</code>
     */
    private static final String PARAM_SCOPE_ID = "scope_id";

    /**
     * Constant <code>GET_URL="GET url: "</code>
     */
    private static final String GET_URL = "GET url: ";

    /**
     * Constant <code>POST_URL="POST url: "</code>
     */
    private static final String POST_URL = "POST url: ";

    /**
     * Constant <code>PARAM_RESULTS="results"</code>
     */
    private static final String PARAM_RESULTS = "results";

    /**
     * Constant <code>PARAM_COUNT="count"</code>
     */
    private static final String PARAM_COUNT = "count";

    /**
     * Constant <code>PARAM_QUERY="query"</code>
     */
    private static final String PARAM_QUERY = "query";

    /**
     * Constant <code>PARAM_COMPANY="company"</code>
     */
    private static final String PARAM_COMPANY = "company";

    /**
     * Constant <code>LIBIDENTS="libidents"</code>
     */
    private static final String LIBIDENTS = "libidents";

    /**
     * Constant <code>THRESHOLD="threshold"</code>
     */
    private static final String THRESHOLD = "threshold";

    /**
     * Constant <code>CITATION="citation"</code>
     */
    private static final String CITATION = "citation";

    /**
     * Constant <code>moduleToCodeMap</code>
     */
    private static final Map<String, String> moduleToCodeMap = new HashMap<>();

    /**
     * Constant <code>MESSAGE_PROHIBITED_ING="message.decernis.ingredient.prohibited"</code>
     */
    public static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";

    /**
     * Constant <code>MESSAGE_NOTLISTED_ING="message.decernis.ingredient.notListed"</code>
     */
    public static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";

    /**
     * Constant <code>MESSAGE_PERMITTED_ING="message.decernis.ingredient.permitted"</code>
     */
    public static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";

    private static final Map<QName, String> ingNumbers = new HashMap<>();

	/** Constant <code>FORMULATION_CHECK="FORMULATION_CHECK"</code> */
	private static final String FORMULATION_CHECK = "FORMULATION_CHECK";
	/** Constant <code>COSMETICS="COSMETICS"</code> */
	private static final String COSMETICS = "COSMETICS";
	/** Constant <code>STANDARDS_OF_IDENTITY_FOOD="STANDARDS_OF_IDENTITY_FOOD"</code> */
	private static final String STANDARDS_OF_IDENTITY_FOOD = "STANDARDS_OF_IDENTITY_FOOD";
	/** Constant <code>FOOD_ADDITIVES="FOOD_ADDITIVES"</code> */
	private static final String FOOD_ADDITIVES = "FOOD_ADDITIVES";
	
    static {
    	moduleToCodeMap.put(FOOD_ADDITIVES, "ADD");
		moduleToCodeMap.put(STANDARDS_OF_IDENTITY_FOOD, "SOI");
		moduleToCodeMap.put(COSMETICS, "COS");
		moduleToCodeMap.put(FORMULATION_CHECK, "PC");
        ingNumbers.put(PLMModel.PROP_CAS_NUMBER, "CAS");
        ingNumbers.put(PLMModel.PROP_EC_NUMBER, "EC No.");
        ingNumbers.put(PLMModel.PROP_CE_NUMBER, "EINECS");
        ingNumbers.put(PLMModel.PROP_FEMA_NUMBER, "FEMA No.");
        ingNumbers.put(PLMModel.PROP_FL_NUMBER, "FL No.");
        ingNumbers.put(PLMModel.PROP_FDA_NUMBER, "FDA Cat.");
    }

    private NodeService nodeService;
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

    public ProductDataDecernisJsonService(@Qualifier("nodeService") NodeService nodeService,
                                          AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
        this.nodeService = nodeService;
        this.alfrescoRepository = alfrescoRepository;
    }

    /**
     * <p>isRIDValid.</p>
     *
     * @param rid a {@link java.lang.String} object
     * @return a boolean
     */
    private boolean isRIDValid(String rid) {
        return rid != null && !rid.isEmpty() && !rid.equals(NOT_APPLICABLE) && !rid.equals(AbstractRegulatoryService.UNKNOWN);
    }

    /**
     * <p>cleanToken.</p>
     *
     * @param token a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    private String cleanToken(String token) {
        return token != null ? token.replace("Bearer ", "").strip() : "";
    }


    public List<IngRegulatoryListDataItem> ingredientAnalysisParseResults(RegulatoryContext productContext, RegulatoryBatch checkContext,
                                                                          JSONObject analysisResults) {

        List<IngRegulatoryListDataItem> ingRegulatoryListDataItems = new ArrayList<>();

        for (String country : checkContext.countryBatches().countries()) {

            if (analysisResults.has("ingredientAnalysisReport")) {

                JSONObject ingredientAnalaysisReport = analysisResults.getJSONObject("ingredientAnalysisReport");

                if (logger.isTraceEnabled()) {
                    logger.trace(ingredientAnalaysisReport.toString(3));
                }

                if (ingredientAnalaysisReport.has(TABULAR_REPORT)) {

                    JSONArray tabularReports = ingredientAnalaysisReport.getJSONArray(TABULAR_REPORT);
                    Map<String, List<JSONObject>> countryReports = findReportsForCountry(tabularReports, country);

                    for (Map.Entry<String, List<JSONObject>> entry : countryReports.entrySet()) {
                        String decernisID = entry.getKey();
                        List<JSONObject> countryDidReports = entry.getValue();
                        IngListDataItem ingItem = findIngredientItemV5(productContext.getIngList(), decernisID, null,
                                countryDidReports.get(0).getString("customerName"));
                        if (ingItem != null) {
                            IngRegulatoryListDataItem ingRegulatoryListDataItem = createIngRegulatoryListDataItem(ingItem.getIng(),
                                    productContext.getCountryNodeRef(country));

                            String usage = String.join(";;",
                                    countryDidReports.stream()
                                            .filter(j -> j.getJSONObject(COMMENTS).get(USAGE_ON_LIST) != null
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().isBlank()
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().equals("null"))
                                            .map(j -> j.getJSONObject(COMMENTS).getString(USAGE_ON_LIST)).distinct().toList());
                            ingRegulatoryListDataItem.setUsages(new MLText(usage));

                            String citation = String.join(";;",
                                    countryDidReports.stream()
                                            .filter(j -> j.getJSONObject(COMMENTS).get(USAGE_ON_LIST) != null
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().isBlank()
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().equals("null"))
                                            .filter(j -> j.get(CITATION) != null && !j.get(CITATION).toString().isBlank()
                                                    && !j.get(CITATION).toString().equals("null"))
                                            .map(j -> j.getJSONObject(COMMENTS).getString(USAGE_ON_LIST) + " :: " + j.getString(CITATION)).distinct()
                                            .toList());
                            ingRegulatoryListDataItem.setCitation(new MLText(citation));

                            String restrictionLevel = String.join(";;",
                                    countryDidReports.stream()
                                            .filter(j -> j.getJSONObject(COMMENTS).get(USAGE_ON_LIST) != null
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().isBlank()
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().equals("null"))
                                            .filter(j -> j.get(THRESHOLD) != null && !j.get(THRESHOLD).toString().isBlank()
                                                    && !j.get(THRESHOLD).toString().equals("null"))
                                            .map(j -> j.getJSONObject(COMMENTS).getString(USAGE_ON_LIST) + " :: " + j.getString(THRESHOLD)).distinct()
                                            .toList());
                            ingRegulatoryListDataItem.setRestrictionLevels(new MLText(restrictionLevel));

                            String precautions = String.join(";;",
                                    countryDidReports.stream()
                                            .filter(j -> j.getJSONObject(COMMENTS).get(USAGE_ON_LIST) != null
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().isBlank()
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().equals("null"))
                                            .filter(j -> j.getJSONObject(COMMENTS).get(COMMENTS) != null
                                                    && !j.getJSONObject(COMMENTS).get(COMMENTS).toString().isBlank()
                                                    && !j.getJSONObject(COMMENTS).get(COMMENTS).toString().equals("null"))
                                            .map(j -> j.getJSONObject(COMMENTS).getString(USAGE_ON_LIST) + " :: " + j.getJSONObject(COMMENTS).getString(COMMENTS))
                                            .distinct().toList());
                            ingRegulatoryListDataItem.setPrecautions(new MLText(precautions));

                            String resultIndicator = String.join(";;",
                                    countryDidReports.stream()
                                            .filter(j -> j.getJSONObject(COMMENTS).get(USAGE_ON_LIST) != null
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().isBlank()
                                                    && !j.getJSONObject(COMMENTS).get(USAGE_ON_LIST).toString().equals("null"))
                                            .filter(j -> j.get(RESULT_INDICATOR) != null && !j.get(RESULT_INDICATOR).toString().isBlank()
                                                    && !j.get(RESULT_INDICATOR).toString().equals("null"))
                                            .map(j -> j.getJSONObject(COMMENTS).getString(USAGE_ON_LIST) + " :: " + j.getString(RESULT_INDICATOR))
                                            .distinct().toList());
                            ingRegulatoryListDataItem.setResultIndicator(new MLText(resultIndicator));

                            ingRegulatoryListDataItems.add(ingRegulatoryListDataItem);
                        }
                    }
                }
            }
        }
        return ingRegulatoryListDataItems;
    }

    public JSONObject ingredientAnalysisPreparePayload(RegulatoryContext context, RegulatoryBatch checkContext) throws JSONException {

        String ingredientAnalysisResult = "";

        JSONObject payload = new JSONObject();

        JSONObject transaction = new JSONObject();
        payload.put("transaction", transaction);

        JSONObject ingredientList = new JSONObject();
        transaction.put("ingredientList", ingredientList);

        String code = (String) nodeService.getProperty(context.getProduct().getNodeRef(), BeCPGModel.PROP_CODE);
        code += Calendar.getInstance().getTimeInMillis();

        String name = code + " " + context.getProduct().getName();

        ingredientList.put(PARAM_NAME, name);

        JSONArray ingredients = new JSONArray();
        ingredientList.put("list", ingredients);

        for (IngListDataItem ingListDataItem : context.getIngList()) {
            IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
            String rid = ingItem.getRegulatoryCode();
            if (isRIDValid(rid)) {
                String ingName = RegulatoryHelper.extractIngName(ingItem);
                JSONObject ingredient = new JSONObject();
                ingredient.put("customerId", ingName);
                ingredient.put("customerName", ingName);
                ingredient.put("idType", "Decernis ID");
                ingredient.put("idValue", rid);
                ingredients.put(ingredient);
            }
        }

        if (!ingredients.isEmpty()) {
            JSONObject scope = new JSONObject();
            transaction.put("scope", scope);

            scope.put(PARAM_NAME, name);

            JSONArray country = new JSONArray();
            scope.put(PARAM_COUNTRY, country);
            checkContext.countryBatches().countries().forEach(country::put);

            JSONArray topics = new JSONArray();
            scope.put("topic", topics);

            JSONObject topic = new JSONObject();
            topics.put(topic);

            topic.put(PARAM_NAME, moduleToCodeMap.get(checkContext.usageBatches().module()));
            JSONObject scopeDetail = new JSONObject();
            topic.put("scopeDetail", scopeDetail);

            JSONArray usages = new JSONArray();
            for (String usage : checkContext.usageBatches().usages()) {
                if (!usage.endsWith(MODULE_SUFFIX)) {
                    usages.put(usage);
                }
            }

            scopeDetail.put("usage", usages);
            return payload;
        }
        return null;
    }

    public List<RequirementListDataItem> recipeAnalysisParseResults(RegulatoryContext context, RegulatoryBatch checkContext,
                                                                    JSONObject analysisResults, boolean addInfoReqCtrl) {
        List<RequirementListDataItem> requirements = new ArrayList<>();
        for (String country : checkContext.countryBatches().countries()) {

            if (analysisResults.has(RECIPE_ANALAYSIS_REPORT)) {

                JSONObject recipeAnalaysisReport = analysisResults.getJSONObject(RECIPE_ANALAYSIS_REPORT);

                if (logger.isTraceEnabled()) {
                    logger.trace(recipeAnalaysisReport.toString(3));
                }

                if (recipeAnalaysisReport.has(RECIPE_REPORT)) {

                    JSONArray recipeReport = recipeAnalaysisReport.getJSONArray(RECIPE_REPORT);

                    for (int i = 0; i < recipeReport.length(); i++) {
                        JSONObject report = recipeReport.getJSONObject(i);
                        if (report.has(COUNTRY) && report.getString(COUNTRY).equals(country)) {

                            JSONArray tabularReports = report.getJSONArray(TABULAR_REPORT);

                            for (int j = 0; j < tabularReports.length(); j++) {
                                JSONObject tabularReport = tabularReports.getJSONObject(j);

                                for (String usage : checkContext.usageBatches().usages()) {

                                    String decernisID = tabularReport.getString(DID);
                                    String function = tabularReport.getString(FUNCTION);
                                    String ingredientName = tabularReport.getString(PARAM_NAME);

                                    IngListDataItem ingItem = findIngredientItemV5(context.getIngList(), decernisID, function,
                                            ingredientName);

                                    if (ingItem != null) {

                                        if (tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("prohibited")
                                                || tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("over limit")) {
                                            String threshold = (tabularReport.has(THRESHOLD) && !tabularReport.getString(THRESHOLD).equals("None")
                                                    ? "(" + tabularReport.getString(THRESHOLD) + ")"
                                                    : "");

                                            String thresholdVal = (tabularReport.has(THRESHOLD) && !tabularReport.getString(THRESHOLD).equals("None")
                                                    ? tabularReport.getString(THRESHOLD)
                                                    : "");

                                            MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING,
                                                    getFormattedValue(ingItem.getQtyPerc()),
                                                    thresholdVal);
                                            RequirementListDataItem reqCtrlItem = createReqCtrl(ingItem.getNodeRef(), reqMessage,
                                                    RequirementType.Forbidden);
                                            reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
                                            reqCtrlItem.setReqMaxQty(0d);
                                            if (!threshold.isBlank() && ingItem != null && ingItem.getQtyPerc() != null
                                                    && ingItem.getQtyPerc() != 0d) {
                                                Double thresholdValue = DecernisHelper.extractThresholdValue(threshold);
                                                if (thresholdValue != null) {
                                                    reqCtrlItem.setReqMaxQty((thresholdValue / ingItem.getQtyPerc()) * 100d);
                                                }
                                            }

                                            requirements.add(reqCtrlItem);
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Adding prohibited ing :" + tabularReport.getString(DID));
                                            }

                                        } else if (tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("not listed")) {
                                            MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
                                            RequirementListDataItem reqCtrlItem = createReqCtrl(ingItem.getNodeRef(), reqMessage,
                                                    RequirementType.Tolerated);
                                            reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
                                            requirements.add(reqCtrlItem);
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Adding not listed ing :" + tabularReport.getString(DID));
                                            }
                                        } else if (Boolean.TRUE.equals(addInfoReqCtrl)) {

                                            String threshold = (tabularReport.has(THRESHOLD) && !tabularReport.getString(THRESHOLD).equals("None")
                                                    ? tabularReport.getString(THRESHOLD)
                                                    : "");

                                            MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PERMITTED_ING,
                                                    tabularReport.getString(RESULT_INDICATOR), threshold);
                                            RequirementListDataItem reqCtrlItem = createReqCtrl(ingItem.getNodeRef(), reqMessage, RequirementType.Info);

                                            reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
                                            requirements.add(reqCtrlItem);
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Adding " + reqMessage.getDefaultValue() + " ing :" + tabularReport.getString(DID));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return requirements;
    }

    public Optional<String> getUsageId(String usageCode, ResponseEntity<String> response) {
        JSONObject responseBody = new JSONObject(response.getBody());
        if (responseBody.has(PARAM_RESULTS)) {
            JSONObject results = responseBody.getJSONObject(PARAM_RESULTS);
            for (String key : results.keySet()) {
                JSONArray resultArray = results.getJSONArray(key);
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject result = resultArray.getJSONObject(i);
                    if (result.has(PARAM_PHRASE) && result.getString(PARAM_PHRASE).equals(usageCode)) {
                        return Optional.of(Integer.toString(result.getInt(PARAM_SCOPE_ID)));
                    }
                }
            }
        }
        return Optional.empty();
    }

    public JSONObject recipeAnalysisPreparePayload(RegulatoryContext context, RegulatoryBatch checkContext, String code,
                                                   JSONArray ingredients, String moduleCode) throws JSONException {
        JSONObject payload = new JSONObject();

        JSONObject transaction = new JSONObject();
        payload.put("transaction", transaction);

        JSONObject recipe = new JSONObject();
        transaction.put("recipe", recipe);

        code += Calendar.getInstance().getTimeInMillis();

        recipe.put("spec", code);
        String name = code + " " + context.getProduct().getName();

        recipe.put(PARAM_NAME, name);

        recipe.put("ingredients", ingredients);

        if (!ingredients.isEmpty()) {
            JSONObject scope = new JSONObject();
            transaction.put("scope", scope);

            scope.put(PARAM_NAME, name);

            JSONArray country = new JSONArray();
            scope.put(PARAM_COUNTRY, country);
            checkContext.countryBatches().countries().forEach(country::put);

            JSONArray topics = new JSONArray();
            scope.put("topic", topics);

            JSONObject topic = new JSONObject();
            topics.put(topic);

            topic.put(PARAM_NAME, moduleCode);
            JSONObject scopeDetail = new JSONObject();
            topic.put("scopeDetail", scopeDetail);

            JSONArray usagesArray = new JSONArray();
            for (String usage : checkContext.usageBatches().usages()) {
                if (!usage.endsWith(MODULE_SUFFIX)) {
                    usagesArray.put(usage);
                }
            }

            scopeDetail.put("usage", usagesArray);
            return payload;
        }

        return null;
    }

    public JSONObject wrapIngredients(RegulatoryContext context, String companyName, JSONArray ingredients) throws JSONException {
        JSONObject ret = new JSONObject();

        String code = (String) nodeService.getProperty(context.getProduct().getNodeRef(), BeCPGModel.PROP_CODE);
        code += Calendar.getInstance().getTimeInMillis();

        ret.put("spec", code);
        ret.put("name", code + " " + context.getProduct().getName());
        ret.put(PARAM_COMPANY, cleanToken(companyName));

        ret.put("ingredients", ingredients);
        return ret;
    }

    public Optional<JSONObject> buildIngredientJsonByDid(String rid, String ingName, Double ingQtyPerc, String function) {
        if (isRIDValid(rid) && ingName != null && !ingName.isEmpty()) {
            JSONObject ingredient = new JSONObject();
            ingredient.put(PARAM_NAME, ingName);
            ingredient.put("percentage", ingQtyPerc == null ? 0d : ingQtyPerc);
            ingredient.put("ingredient_did", rid);
            if (function != null) {
                ingredient.put(FUNCTION, function);
            }
            ingredient.put("spec_parameters", JSONObject.NULL);
            ingredient.put("upper_limit", JSONObject.NULL);
            return Optional.of(ingredient);
        }
        return Optional.empty();
    }

    public Optional<JSONObject> buildIngredientJsonById(IngListDataItem ingListDataItem, IngItem ingItem, String function) {
        String rid = ingItem.getRegulatoryCode();
        if (isRIDValid(rid)) {
            String ingName = RegulatoryHelper.extractIngName(ingItem);
            Double ingQtyPerc = DecernisHelper.truncateDoubleValue(ingListDataItem.getQtyPerc());
            JSONObject ingredient = new JSONObject();
            ingredient.put(PARAM_NAME, ingName);
            ingredient.put("percentage", ingQtyPerc == null ? 0d : ingQtyPerc);
            ingredient.put("spec", ingName);
            ingredient.put("idType", "Decernis ID");
            ingredient.put("idValue", rid);
            if (function != null) {
                ingredient.put(FUNCTION, function);
            }
            return Optional.of(ingredient);
        }
        return Optional.empty();
    }

    public String parseIngredients(IngListDataItem ingListDataItem, ResponseEntity<String> response, String ingName, Map<String, String> params, String ingredientId) {
        JSONObject jsonObject = new JSONObject(response.getBody());

        if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) >= 1) && jsonObject.has(PARAM_RESULTS)) {
            JSONObject result = findIngredient(ingListDataItem.getIng(), ingName, jsonObject, params);
            if (result != null) {
                ingredientId = result.get(DID).toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("RID of ingredient " + params.get(PARAM_QUERY) + ": " + ingredientId);
                }
                nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE, ingredientId);
                // Get ingredient numbers (CAS, FEMA, CE)
                if (result.has(LIBIDENTS)) {
                    JSONObject libidents = result.getJSONObject(LIBIDENTS);
                    for (Map.Entry<QName, String> entry : ingNumbers.entrySet()) {
                        QName numberPropName = entry.getKey();
                        String numberPropValue = entry.getValue();
                        String ingNumberToFill = (String) nodeService.getProperty(ingListDataItem.getIng(), numberPropName);

                        if ((ingNumberToFill == null || ingNumberToFill.isEmpty()) && libidents.has(numberPropValue)) {
                            JSONArray numbers = libidents.getJSONArray(numberPropValue);
                            String number = null;
                            if (numbers.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < numbers.length(); i++) {
                                    sb.append(numbers.getString(i)).append(",");
                                }
                                number = sb.deleteCharAt(sb.length() - 1).toString();
                            }
                            if ((number != null) && !number.isEmpty()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Set ingredient RID: " + params.get(PARAM_QUERY) + " " + ingListDataItem.getIng() + " "
                                            + numberPropName + " " + number);
                                }

                                nodeService.setProperty(ingListDataItem.getIng(), numberPropName, number);
                            }
                        }
                    }
                }

            }
        }
        return ingredientId;
    }

    /**
     * <p>createIngRegulatoryListDataItem.</p>
     *
     * @param ing     a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param country a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @return a {@link fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem} object
     */
    protected IngRegulatoryListDataItem createIngRegulatoryListDataItem(NodeRef ing, NodeRef country) {

        IngRegulatoryListDataItem ingRegulatoryListDataItem = new IngRegulatoryListDataItem();
        ingRegulatoryListDataItem.setIng(ing);
        ingRegulatoryListDataItem.setRegulatoryCountries(Arrays.asList(country));

        return ingRegulatoryListDataItem;
    }

    /**
     * <p>findReportsForCountry.</p>
     *
     * @param tabularReports a {@link org.json.JSONArray} object
     * @param country        a {@link java.lang.String} object
     * @return a {@link java.util.Map} object
     */
    private Map<String, List<JSONObject>> findReportsForCountry(JSONArray tabularReports, String country) {
        Map<String, List<JSONObject>> map = new HashMap<>();
        for (int i = 0; i < tabularReports.length(); i++) {
            JSONObject tabularReport = tabularReports.getJSONObject(i);
            if (tabularReport.has(COUNTRY) && tabularReport.getString(COUNTRY).equals(country)) {
                List<JSONObject> list = map.computeIfAbsent(tabularReport.get("decernisId").toString(), k -> new ArrayList<>());
                list.add(tabularReport);
            }
        }
        return map;
    }

    /**
     * <p>findIngredientItemV5.</p>
     *
     * @param ingList        a {@link java.util.List} object
     * @param decernisID     a {@link java.lang.String} object
     * @param function       a {@link java.lang.String} object
     * @param ingredientName a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
     */
    private IngListDataItem findIngredientItemV5(List<IngListDataItem> ingList, String decernisID, String function, String ingredientName) {
        for (IngListDataItem ing : ingList) {
            IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());
            if (decernisID.equals(ingItem.getRegulatoryCode())) {
                for (IngTypeItem ingType : RegulatoryHelper.extractIngTypes(ing, alfrescoRepository)) {
                    if (ingType != null && function != null && (function.equalsIgnoreCase(ingType.getLvValue())
                            || function.equalsIgnoreCase(ingType.getLvCode()) || function.equalsIgnoreCase(ingType.getRegulatoryCode()))) {
                        return ing;
                    }
                }
            }
        }

        for (IngListDataItem ing : ingList) {
            IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());
            String ingName = RegulatoryHelper.extractIngName(ingItem);
            if (ingredientName.equals(ingName)) {
                return ing;
            }
        }
        return null;
    }

    private Object getFormattedValue(Double qtyPerc) {
        if (qtyPerc == null) {
            return "";
        }
        return MLTextHelper.createMLTextI18N(l -> NumberFormat.getInstance(l).format(qtyPerc) + "%");
    }

    /**
     * <p>createReqCtrl.</p>
     *
     * @param ing            a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param reqCtrlMessage a {@link org.alfresco.service.cmr.repository.MLText} object
     * @param reqType        a {@link fr.becpg.repo.regulatory.RequirementType} object
     * @return a {@link fr.becpg.repo.regulatory.RequirementListDataItem} object
     */
    protected RequirementListDataItem createReqCtrl(NodeRef ing, MLText reqCtrlMessage, RequirementType reqType) {
        RequirementListDataItem reqCtrlItem = new RequirementListDataItem();
        reqCtrlItem.setReqType(reqType);
        reqCtrlItem.setCharact(ing);
        reqCtrlItem.addSource(ing);
        reqCtrlItem.setReqDataType(RequirementDataType.Specification);
        reqCtrlItem.setReqMlMessage(reqCtrlMessage);
        reqCtrlItem.setFormulationChainId(AbstractRegulatoryService.REGULATORY_KEY);
        return reqCtrlItem;
    }


    /**
     * <p>findIngredient.</p>
     *
     * @param ing        a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param ingName    a {@link java.lang.String} object
     * @param jsonObject a {@link org.json.JSONObject} object
     * @param params     a {@link java.util.Map} object
     * @return a {@link org.json.JSONObject} object
     */
    private JSONObject findIngredient(NodeRef ing, String ingName, JSONObject jsonObject, Map<String, String> params) {
        JSONArray results = jsonObject.getJSONArray(PARAM_RESULTS);
        JSONObject result = null;
        if (jsonObject.getInt(PARAM_COUNT) == 1) {
            result = results.getJSONObject(0);
        }
        if (result == null) {
            result = findIngByNumber(ing, results, params.get("type"));
        }
        if (result == null) {
            result = getRidByIngName(results, ingName);
        }
        if (result == null && results.toList().stream().map(o -> ((Map<?, ?>) o).get(DID)).distinct().count() == 1) {
            result = results.getJSONObject(0);
        }
        return result;
    }

    /**
     * <p>findIngByNumber.</p>
     *
     * @param ing     a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param results a {@link org.json.JSONArray} object
     * @param type    a {@link java.lang.String} object
     * @return a {@link org.json.JSONObject} object
     */
    private JSONObject findIngByNumber(NodeRef ing, JSONArray results, String type) {
        for (Map.Entry<QName, String> entry : ingNumbers.entrySet()) {
            QName numberProp = entry.getKey();
            String numberKey = entry.getValue();
            if (!type.equals(numberKey)) {
                String propValue = (String) nodeService.getProperty(ing, numberProp);
                if (propValue != null && !propValue.isBlank()) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        if (result.has(LIBIDENTS)) {
                            JSONObject libidents = result.getJSONObject(LIBIDENTS);
                            if (libidents.has(numberKey)) {
                                JSONArray keyLibidents = libidents.getJSONArray(numberKey);
                                for (int j = 0; j < keyLibidents.length(); j++) {
                                    String keyLibident = keyLibidents.getString(j);
                                    for (String propValueSplit : propValue.split("/")) {
                                        if (propValueSplit.trim().equals(keyLibident)) {
                                            return result;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * <p>getRidByIngName.</p>
     *
     * @param results a {@link org.json.JSONArray} object
     * @param ingName a {@link java.lang.String} object
     * @return a {@link org.json.JSONObject} object
     * @throws org.json.JSONException if any.
     */
    private JSONObject getRidByIngName(JSONArray results, String ingName) throws JSONException {
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            if (result.has("synonyms")) {
                JSONArray synonyms = result.getJSONArray("synonyms");
                int j = 0;
                while (j < synonyms.length()) {
                    String[] split = synonyms.getString(j).split(",");
                    for (String syn : split) {
                        if (syn.toLowerCase().trim().equals(ingName.toLowerCase().replace(",", "").trim())) {
                            return result;
                        }
                    }
                    j++;
                }
            }
        }
        return null;
    }

    /**
     * <p>buildQuery.</p>
     *
     * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
     * @param params a {@link java.util.Map} object
     * @return a boolean
     */
    public boolean buildQuery(IngListDataItem ingListDataItem, Map<String, String> params) {
        Iterator<Map.Entry<QName, String>> iterator = ingNumbers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<QName, String> ingNumber = iterator.next();
            String number = (String) nodeService.getProperty(ingListDataItem.getIng(), ingNumber.getKey());
            if (isRIDValid(number) && !number.contains(",")) {
                if (number.contains("/")) {
                    number = number.split("/")[0].trim();
                }
                params.put(PARAM_QUERY, number);
                params.put("type", ingNumber.getValue());
                return true;
            }
        }

        return false;
    }
}
