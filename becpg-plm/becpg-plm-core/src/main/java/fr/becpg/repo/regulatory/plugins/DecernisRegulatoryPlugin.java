package fr.becpg.repo.regulatory.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.CountryBatch;
import fr.becpg.repo.regulatory.RegulatoryBatch;
import fr.becpg.repo.regulatory.RegulatoryContext;
import fr.becpg.repo.regulatory.RegulatoryMode;
import fr.becpg.repo.regulatory.RegulatoryPlugin;
import fr.becpg.repo.regulatory.RegulatoryService;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.regulatory.UsageBatch;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>DecernisRegulatoryPlugin class.</p>
 *
 * @author Valentin
 */
@Service
public class DecernisRegulatoryPlugin implements RegulatoryPlugin {

	private static final String FUNCTION = "function";

	private static final String COUNTRY = "country";

	private static final String USAGE_ON_LIST = "usageOnList";

	private static final String COMMENTS = "comments";

	private static final String TABULAR_REPORT = "tabularReport";

	private static final String DID = "did";

	/** Constant <code>DECERNIS_CHAIN_ID="decernis"</code> */
	public static final String DECERNIS_CHAIN_ID = "decernis";

	/** Constant <code>MODULE_SUFFIX=" module"</code> */
	public static final String MODULE_SUFFIX = " module";

	private static final String MESSAGE_DECERNIS_ERROR = "message.decernis.error";

	private static final String EUROPEAN_UNION = "European Union";

	private static final Log logger = LogFactory.getLog(DecernisRegulatoryPlugin.class);

	/** Constant <code>NOT_APPLICABLE="NA"</code> */
	public static final String NOT_APPLICABLE = "NA";

	private static final String RESULT_INDICATOR = "resultIndicator";

	private static final String RECIPE_REPORT = "recipeReport";

	private static final String RECIPE_ANALAYSIS_REPORT = "recipeAnalaysisReport";

	private static final String PARAM_COUNTRY = COUNTRY;

	private static final String PARAM_NAME = "name";

	/** Constant <code>THRESHOLD="threshold"</code> */
	protected static final String THRESHOLD = "threshold";

	/** Constant <code>CITATION="citation"</code> */
	protected static final String CITATION = "citation";

	private static final Map<String, String> moduleToCodeMap = new HashMap<>();
	private static final Map<String, Integer> moduleToIDMap = new HashMap<>();

	private static final String FORMULATION_CHECK = "FORMULATION_CHECK";
	private static final String COSMETICS = "COSMETICS";
	private static final String STANDARDS_OF_IDENTITY_FOOD = "STANDARDS_OF_IDENTITY_FOOD";
	private static final String FOOD_ADDITIVES = "FOOD_ADDITIVES";

	/** Constant <code>MESSAGE_PROHIBITED_ING="message.decernis.ingredient.prohibited"</code> */
	public static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	/** Constant <code>MESSAGE_NOTLISTED_ING="message.decernis.ingredient.notListed"</code> */
	public static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";
	/** Constant <code>MESSAGE_PERMITTED_ING="message.decernis.ingredient.permitted"</code> */
	public static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";

	private static final String PARAM_COMPANY = "company";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_RESULTS = "results";
	private static final String PARAM_QUERY = "query";
	private static final String LIBIDENTS = "libidents";

	private static final Map<QName, String> ingNumbers = new HashMap<>();

	static {
		ingNumbers.put(PLMModel.PROP_CAS_NUMBER, "CAS");
		ingNumbers.put(PLMModel.PROP_EC_NUMBER, "EC No.");
		ingNumbers.put(PLMModel.PROP_CE_NUMBER, "EINECS");
		ingNumbers.put(PLMModel.PROP_FEMA_NUMBER, "FEMA No.");
		ingNumbers.put(PLMModel.PROP_FL_NUMBER, "FL No.");
		ingNumbers.put(PLMModel.PROP_FDA_NUMBER, "FDA Cat.");
		moduleToCodeMap.put(FOOD_ADDITIVES, "ADD");
		moduleToCodeMap.put(STANDARDS_OF_IDENTITY_FOOD, "SOI");
		moduleToCodeMap.put(COSMETICS, "COS");
		moduleToCodeMap.put(FORMULATION_CHECK, "PC");
		moduleToIDMap.put(FOOD_ADDITIVES, 1);
		moduleToIDMap.put(STANDARDS_OF_IDENTITY_FOOD, 2);
		moduleToIDMap.put(COSMETICS, 9);
		moduleToIDMap.put(FORMULATION_CHECK, 100);
	}

	private Map<String, List<String>> functionsMap = new ConcurrentHashMap<>();

	/**
	 * <p>analysisUrl.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String analysisUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.analysisUrl");
	}

	private String serverUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.serverUrl");
	}

	private String companyName() {
		return systemConfigurationService.confValue("beCPG.decernis.companyName");
	}
	
	private Integer maxCountriesPerRequest() {
		String confValue = systemConfigurationService.confValue("beCPG.decernis.maxCountriesPerRequest");
		if (confValue != null && !confValue.isBlank()) {
			return Integer.parseInt(confValue);
		}
		return null;
	}
	
	private Integer maxUsagesPerRequest() {
		String confValue = systemConfigurationService.confValue("beCPG.decernis.maxUsagesPerRequest");
		if (confValue != null && !confValue.isBlank()) {
			return Integer.parseInt(confValue);
		}
		return null;
	}

	private boolean addInfoReqCtrl() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.specification.addInfoReqCtrl"));
	}

	/**
	 * <p>Constructor for DecernisRegulatoryPlugin.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public DecernisRegulatoryPlugin(SystemConfigurationService systemConfigurationService, @Qualifier("nodeService") NodeService nodeService,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		super();
		this.systemConfigurationService = systemConfigurationService;
		this.nodeService = nodeService;
		this.alfrescoRepository = alfrescoRepository;
	}

	private SystemConfigurationService systemConfigurationService;

	private NodeService nodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/** {@inheritDoc} */
	@Override
	public void checkRecipe(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {

		if (logger.isDebugEnabled()) {
			logger.debug("Launch decernis in mode :" + context.getRegulatoryMode());
		}

		if (RegulatoryMode.BOTH.equals(context.getRegulatoryMode()) || RegulatoryMode.DECERNIS_ONLY.equals(context.getRegulatoryMode())) {
			createRecipe(context);
		}

		if (RegulatoryMode.BOTH.equals(context.getRegulatoryMode()) || RegulatoryMode.BECPG_ONLY.equals(context.getRegulatoryMode())) {
			JSONObject recipeAnalysisResults = null;
			try {
				recipeAnalysisResults = recipeAnalysis(context, regulatoryBatch);
			} catch (RestClientException e) {
				logger.error("Error during Decernis recipe analysis: " + DecernisHelper.cleanError(e.getMessage()), e);
				for (String country : regulatoryBatch.countryBatches().countries()) {
					for (String usage : regulatoryBatch.usageBatches().usages()) {
						RequirementListDataItem req = RequirementListDataItem.forbidden()
								.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DECERNIS_ERROR, generateError(e)))
								.ofDataType(RequirementDataType.Formulation).withFormulationChainId(RegulatoryService.REGULATORY_KEY)
								.withRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));

						context.getRequirements().add(req);
					}
				}
			}
			if (recipeAnalysisResults != null) {
				List<RequirementListDataItem> parseRecipeAnalysisResults = parseRecipeAnalysisResults(context, regulatoryBatch,
						recipeAnalysisResults);
				context.getRequirements().addAll(parseRecipeAnalysisResults);
			}
			checkUsagesID(context);
		}
	}

	private JSONObject recipeAnalysis(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
		JSONObject recipeAnalysisResults = null;
		int retries = 2;
		while (recipeAnalysisResults == null && retries >= 0) {
			try {
				retries--;
				recipeAnalysisResults = postV5RecipeAnalysis(context, regulatoryBatch);
			} catch (RestClientException e) {
				if (retries <= 0) {
					throw e;
				}
				logger.error("Error during Decernis recipe analysis: " + DecernisHelper.cleanError(e.getMessage()) 
				+ ", try restarting request...");
				recipeAnalysisResults = null;
			}
		}
		return recipeAnalysisResults;
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer getBatchThreads() {
		String confValue = systemConfigurationService.confValue("beCPG.decernis.batchThreads");
		if (confValue != null && !confValue.isBlank()) {
			return Integer.parseInt(confValue);
		}
		return null;
	}

	private void checkUsagesID(RegulatoryContext context) {
		for (NodeRef usageRef : context.getProduct().getRegulatoryUsagesRef()) {
			String usageCode = (String) nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_CODE);
			Integer moduleId = moduleToIDMap.get(context.getUsageModule(usageCode));
			updateUsageID(usageRef, usageCode, moduleId);
		}
		for (RegulatoryListDataItem regulatoryListItem : context.getProduct().getRegulatoryList()) {
			for (NodeRef usageRef : regulatoryListItem.getRegulatoryUsagesRef()) {
				String usageCode = (String) nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_CODE);
				Integer moduleId = moduleToIDMap.get(context.getUsageModule(usageCode));
				updateUsageID(usageRef, usageCode, moduleId);
			}
		}
	}

	private void updateUsageID(NodeRef usageRef, String usageCode, Integer moduleId) {
		if (nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_ID) instanceof String) {
			return;
		}
		String url = serverUrl() + "/usages/structurized" + "?module_id=" + moduleId + "&phrase=" + usageCode;
		traceGetRequest(url);
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout().exchange(url, HttpMethod.GET, createEntity(null),
				String.class, new HashMap<>());
		if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
			JSONObject responseBody = new JSONObject(response.getBody());
			if (responseBody.has(PARAM_RESULTS)) {
				JSONObject results = responseBody.getJSONObject(PARAM_RESULTS);
				for (String key : results.keySet()) {
					JSONArray resultArray = results.getJSONArray(key);
					for (int i = 0; i < resultArray.length(); i++) {
						JSONObject result = resultArray.getJSONObject(i);

						if (result.has("phrase") && result.getString("phrase").equals(usageCode)) {
							String usageId = Integer.toString(result.getInt("scope_id"));
							nodeService.setProperty(usageRef, PLMModel.PROP_REGULATORY_ID, usageId);
							return;
						}
					}
				}
			}
		}
	}

	private void traceGetRequest(String url) {
		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url);
		}
	}

	private void createRecipe(RegulatoryContext context) {
		try {
			JSONObject recipePayload = createRecipePayload(context);
			if (recipePayload != null) {
				String recipeId = context.getProduct().getRegulatoryRecipeId();
				HttpEntity<String> request = createEntity(recipePayload.toString());
				String url = serverUrl() + "/formulas";
				if (recipeId != null && !recipeId.isBlank()) {
					url += "/" + recipeId;
					if (logger.isTraceEnabled()) {
						logger.trace("PUT url: " + url + " body: " + recipePayload);
					}
					logger.debug("Update decernis recipe : " + recipeId);
					ResponseEntity<String> responseEntity = RestTemplateHelper.getRestTemplateLongTimeout().exchange(url, HttpMethod.PUT, request,
							String.class);

					if (!responseEntity.getStatusCode().is2xxSuccessful()) {
						logger.debug("Error while updating recipe : " + recipeId + ", response is: " + responseEntity);
						recipeId = postRecipe(recipePayload, request, url);
					}
				} else {
					recipeId = postRecipe(recipePayload, request, url);
				}
				updateRecipeId(context, recipeId);
			}
		} catch (RestClientException e) {
			logger.error(generateError(e), e);
			RequirementListDataItem req = RequirementListDataItem.forbidden()
					.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DECERNIS_ERROR, generateError(e))).ofDataType(RequirementDataType.Specification)
					.withFormulationChainId(DECERNIS_CHAIN_ID);
			context.getRequirements().add(req);
		}
	}

	private void updateRecipeId(RegulatoryContext context, String recipeId) {
		context.getProduct().setRegulatoryRecipeId(recipeId);
		for (RegulatoryListDataItem regulatoryListItem : context.getProduct().getRegulatoryList()) {
			regulatoryListItem.setRegulatoryRecipeId(recipeId);
		}
	}

	private String postRecipe(JSONObject recipePayload, HttpEntity<String> request, String url) {
		tracePostRequest(recipePayload, url);
		JSONObject jsonObject = new JSONObject(RestTemplateHelper.getRestTemplateLongTimeout().postForObject(url, request, String.class));
		String recipeId = null;
		if (jsonObject.has("id")) {
			recipeId = jsonObject.get("id").toString();
		}
		logger.debug("Create decernis recipe : " + recipeId);
		return recipeId;
	}

	private void tracePostRequest(JSONObject recipePayload, String url) {
		if (logger.isTraceEnabled()) {
			logger.trace("POST url: " + url + " body: " + recipePayload);
		}
	}

	private JSONObject createRecipePayload(RegulatoryContext context) throws JSONException {

		JSONObject ret = new JSONObject();

		String code = (String) nodeService.getProperty(context.getProduct().getNodeRef(), BeCPGModel.PROP_CODE);
		code += Calendar.getInstance().getTimeInMillis();

		ret.put("spec", code);
		ret.put("name", code + " " + context.getProduct().getName());
		ret.put(PARAM_COMPANY, cleanToken(companyName()));

		JSONArray ingredients = new JSONArray();
		ret.put("ingredients", ingredients);

		for (IngListDataItem ingListDataItem : context.getIngList()) {

			if (ingListDataItem.getIng() != null) {

				IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
				String ingName = extractIngName(ingItem);
				String rid = ingItem.getRegulatoryCode();

				Double ingQtyPerc = DecernisHelper.truncateDoubleValue(ingListDataItem.getQtyPerc());

				IngTypeItem ingType = ingItem.getIngType();
				String function = null;
				if (ingType != null) {
					function = ingType.getRegulatoryCode();
				}
				try {
					if (isRIDValid(rid) && ingName != null && !ingName.isEmpty()) {
						JSONObject ingredient = new JSONObject();
						ingredient.put("name", ingName);
						ingredient.put("percentage", ingQtyPerc == null ? 0d : ingQtyPerc);
						ingredient.put("ingredient_did", rid);
						if (function != null) {
							ingredient.put(FUNCTION, function);
						}
						ingredient.put("spec_parameters", JSONObject.NULL);
						ingredient.put("upper_limit", JSONObject.NULL);
						ingredients.put(ingredient);
					}

				} catch (RestClientException e) {
					logger.warn("Cannot retrieve ingredient " + ingName + " error:" + e.getMessage());
				} catch (Exception e) {
					logger.error(e, e);
					throw new FormulateException("Unexpected decernis error: " + DecernisHelper.cleanError(e.getMessage()), e);
				}
			}
		}

		if (ingredients.length() > 0) {
			return ret;
		}
		return null;
	}

	private boolean isRIDValid(String rid) {
		return rid != null && !rid.isEmpty() && !rid.equals(NOT_APPLICABLE) && !rid.equals(RegulatoryService.UNKNOWN);
	}

	private String extractIngName(IngItem ingItem) {
		MLText mlTextLegalName = ingItem.getLegalName();
		String legalName = mlTextLegalName != null ? mlTextLegalName.getClosestValue(I18NUtil.getContentLocale()) : null;
		return legalName != null && !legalName.isBlank() ? legalName : ingItem.getCharactName();
	}

	private String cleanToken(String token) {
		return token != null ? token.replace("Bearer ", "").strip() : "";
	}

	/** {@inheritDoc} */
	@Override
	public void checkIngredients(RegulatoryContext context, RegulatoryBatch checkContext) {
		JSONObject ingredientAnalysisResults = null;
		try {
			ingredientAnalysisResults = ingredientAnalysis(context, checkContext);
		} catch (RestClientException e) {
			logger.error("Error during Decernis ingredients analysis: " + DecernisHelper.cleanError(e.getMessage()), e);
			RequirementListDataItem req = RequirementListDataItem.forbidden()
					.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DECERNIS_ERROR, generateError(e))).ofDataType(RequirementDataType.Formulation)
					.withFormulationChainId(RegulatoryService.REGULATORY_KEY);
			context.getRequirements().add(req);
		}
		if (ingredientAnalysisResults != null) {
			List<IngRegulatoryListDataItem> parseIngredientAnalysisResults = parseIngredientAnalysisResults(context, checkContext,
					ingredientAnalysisResults);
			context.getIngRegulatoryListDataItems().addAll(parseIngredientAnalysisResults);
		}
	}
	
	private JSONObject ingredientAnalysis(RegulatoryContext context, RegulatoryBatch regulatoryBatch) {
		JSONObject ingredientAnalysisResults = null;
		int retries = 2;
		while (ingredientAnalysisResults == null && retries >= 0) {
			try {
				retries--;
				ingredientAnalysisResults = postV5IngredientAnalysis(context, regulatoryBatch);
			} catch (RestClientException e) {
				if (retries <= 0) {
					throw e;
				}
				logger.error("Error during Decernis ingredient analysis: " + DecernisHelper.cleanError(e.getMessage()) 
				+ ", try restarting request...");
				ingredientAnalysisResults = null;
			}
		}
		return ingredientAnalysisResults;
	}

	private String generateError(RestClientException e) {
		return "Error while creating Decernis recipe: " + DecernisHelper.cleanError(e.getMessage());
	}

	/** {@inheritDoc} */
	@Override
	public String fetchIngredientId(IngListDataItem ingListDataItem) {

		IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
		String ingName = extractIngName(ingItem);

		String ingredientId = null;

		String url = serverUrl() + "/ingredients?current_company={company}&q={query}&identifier_type={type}&limit=25";

		Map<String, String> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName());

		if (!buildQuery(ingListDataItem, params)) {
			params.put(PARAM_QUERY, ingName);
			params.put("type", "Name");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Look for ingredients in decernis by " + params.get("type") + ": " + params.get(PARAM_QUERY));
		}

		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url + " params: " + params);
		}
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout().exchange(url, HttpMethod.GET, createEntity(null),
				String.class, params);

		if ((response != null) && HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {

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
		}
		return ingredientId;
	}

	/** {@inheritDoc} */
	@Override
	public List<CountryBatch> splitCountries(RegulatoryContext context, List<String> countries) {
		List<CountryBatch> countryBatches = new ArrayList<>();
		if (countries.contains(EUROPEAN_UNION)) {
			countries.remove(EUROPEAN_UNION);
			countryBatches.add(new CountryBatch(List.of(EUROPEAN_UNION)));
		}
		int ingListSize = context.getIngList().size();
		int maxCountries = 20;
		if (ingListSize >= 130) {
			maxCountries = 3;
		} else if (ingListSize >= 100) {
			maxCountries = 5;
		} else if (ingListSize >= 50) {
			maxCountries = 9;
		} else if (ingListSize >= 25) {
			maxCountries = 11;
		}
		Integer maxCountriesPerRequest = maxCountriesPerRequest();
		if (maxCountriesPerRequest != null) {
			maxCountries = Math.min(maxCountries, maxCountriesPerRequest);
		}
		List<List<String>> countriesBatches = Lists.partition(countries, maxCountries);
		for (List<String> countriesBatch : countriesBatches) {
			countryBatches.add(new CountryBatch(countriesBatch));
		}
		return countryBatches;
	}

	/** {@inheritDoc} */
	@Override
	public List<UsageBatch> splitUsages(RegulatoryContext context, List<String> usages) {
		List<UsageBatch> usageBatches = new ArrayList<>();
		Map<String, List<String>> moduleToUsages = new HashMap<>();
		for (String usage : usages) {
			String usageModule = context.getUsageModule(usage);
			List<String> moduleUsages = moduleToUsages.computeIfAbsent(usageModule, k -> new ArrayList<>());
			moduleUsages.add(usage);
		}
		int maxUsages = 20;
		Integer maxUsagesPerRequest = maxUsagesPerRequest();
		if (maxUsagesPerRequest != null) {
			maxUsages = Math.min(maxUsages, maxUsagesPerRequest);
		}
		for (Map.Entry<String, List<String>> entry : moduleToUsages.entrySet()) {
			List<String> allUsages = entry.getValue();
			List<List<String>> usagesBatches = Lists.partition(new ArrayList<>(allUsages), maxUsages);
			for (List<String> usagesBatch : usagesBatches) {
				usageBatches.add(new UsageBatch(entry.getKey(), usagesBatch));
			}
		}
		return usageBatches;
	}

	private JSONObject postV5RecipeAnalysis(RegulatoryContext context, RegulatoryBatch checkContext) throws JSONException {

		String recipeAnalysisResult = "";

		JSONObject payload = new JSONObject();

		JSONObject transaction = new JSONObject();
		payload.put("transaction", transaction);

		JSONObject recipe = new JSONObject();
		transaction.put("recipe", recipe);

		String code = (String) nodeService.getProperty(context.getProduct().getNodeRef(), BeCPGModel.PROP_CODE);
		code += Calendar.getInstance().getTimeInMillis();

		recipe.put("spec", code);
		String name = code + " " + context.getProduct().getName();

		recipe.put(PARAM_NAME, name);

		JSONArray ingredients = new JSONArray();
		recipe.put("ingredients", ingredients);

		String moduleCode = moduleToCodeMap.get(checkContext.usageBatches().module());

		for (IngListDataItem ingListDataItem : context.getIngList()) {

			IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());

			String function = null;
			IngTypeItem ingType = ingItem.getIngType();
			if (ingType != null) {
				String functionValue = ingType.getRegulatoryCode();
				if (functionValue != null) {
					function = findFunction(moduleCode, functionValue);
				}
				if (function == null) {
					functionValue = ingType.getLvCode();
					if (functionValue != null) {
						function = findFunction(moduleCode, functionValue);
					}
				}
				if (function == null) {
					functionValue = ingType.getLvValue();
					if (functionValue != null) {
						function = findFunction(moduleCode, functionValue);
					}
				}
			}
			String rid = ingItem.getRegulatoryCode();
			if (isRIDValid(rid)) {
				String ingName = extractIngName(ingItem);
				Double ingQtyPerc = DecernisHelper.truncateDoubleValue(ingListDataItem.getQtyPerc());
				JSONObject ingredient = new JSONObject();
				ingredient.put(PARAM_NAME, ingName);
				ingredient.put("spec", ingName);
				ingredient.put("idType", "Decernis ID");
				ingredient.put("idValue", rid);
				ingredient.put("percentage", ingQtyPerc == null ? 0d : ingQtyPerc);
				if (function != null) {
					ingredient.put(FUNCTION, function);
				}
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

			String url = analysisUrl() + "/recipe-analysis/transaction?report=tabular";

			HttpEntity<String> entity = createEntity(payload.toString());

			tracePostRequest(payload, url);
			recipeAnalysisResult = RestTemplateHelper.getRestTemplateLongTimeout().postForObject(url, entity, String.class, new HashMap<>());

			return new JSONObject(recipeAnalysisResult);
		}

		return null;
	}

	private JSONObject postV5IngredientAnalysis(RegulatoryContext context, RegulatoryBatch checkContext) throws JSONException {

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
				String ingName = extractIngName(ingItem);
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

			String url = analysisUrl() + "/ingredient-analysis/transaction?report=tabular";

			HttpEntity<String> entity = createEntity(payload.toString());

			tracePostRequest(payload, url);
			ingredientAnalysisResult = RestTemplateHelper.getRestTemplateLongTimeout().postForObject(url, entity, String.class, new HashMap<>());

			return new JSONObject(ingredientAnalysisResult);
		}

		return null;
	}

	private List<IngRegulatoryListDataItem> parseIngredientAnalysisResults(RegulatoryContext productContext, RegulatoryBatch checkContext,
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

					for (Entry<String, List<JSONObject>> entry : countryReports.entrySet()) {
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
											.filter(j -> j.getJSONObject(COMMENTS).get(COMMENTS) != null
													&& !j.getJSONObject(COMMENTS).get(COMMENTS).toString().isBlank()
													&& !j.getJSONObject(COMMENTS).get(COMMENTS).toString().equals("null"))
											.map(j -> j.getJSONObject(COMMENTS).getString(COMMENTS)).distinct().toList());
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

	/**
	 * <p>createIngRegulatoryListDataItem.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param country a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem} object
	 */
	protected IngRegulatoryListDataItem createIngRegulatoryListDataItem(NodeRef ing, NodeRef country) {

		IngRegulatoryListDataItem ingRegulatoryListDataItem = new IngRegulatoryListDataItem();
		ingRegulatoryListDataItem.setIng(ing);
		ingRegulatoryListDataItem.setRegulatoryCountries(Arrays.asList(country));

		return ingRegulatoryListDataItem;
	}

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

	private String findFunction(String moduleCode, String ingTypeValue) {
		if (!functionsMap.containsKey(moduleCode)) {
			List<String> functions = fetchFunctions(moduleCode);
			functionsMap.put(moduleCode, functions);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("functionsMap=" + functionsMap);
		}
		for (String function : functionsMap.get(moduleCode)) {
			if (function.trim().equalsIgnoreCase(ingTypeValue.trim())) {
				return function;
			}
		}
		logger.warn("Ingredient function is not recognized by Decernis v5 API: " + ingTypeValue + ", available functions are: "
				+ functionsMap.get(moduleCode));
		return null;
	}

	private List<String> fetchFunctions(String moduleCode) {

		List<String> functions = new ArrayList<>();

		String url = analysisUrl() + "/scope/function?topic=" + moduleCode;

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(DecernisHelper.getToken().trim());

		traceGetRequest(url);
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplateLongTimeout().exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
				String.class, new HashMap<>());

		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {
			JSONObject responseBody = new JSONObject(response.getBody());

			if (responseBody.has("functions")) {
				JSONArray functionArray = responseBody.getJSONArray("functions");
				for (int i = 0; i < functionArray.length(); i++) {
					functions.add(functionArray.getString(i));
				}
			}
		}
		return functions;
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

	private List<RequirementListDataItem> parseRecipeAnalysisResults(RegulatoryContext context, RegulatoryBatch checkContext,
			JSONObject analysisResults) {
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

											MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, threshold);
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
										} else if (Boolean.TRUE.equals(addInfoReqCtrl())) {

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

	private IngListDataItem findIngredientItemV5(List<IngListDataItem> ingList, String decernisID, String function, String ingredientName) {
		for (IngListDataItem ing : ingList) {
			IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());
			if (decernisID.equals(ingItem.getRegulatoryCode())) {
				IngTypeItem ingType = ingItem.getIngType();
				if (ingType != null && function != null && (function.equalsIgnoreCase(ingType.getLvValue())
						|| function.equalsIgnoreCase(ingType.getLvCode()) || function.equalsIgnoreCase(ingType.getRegulatoryCode()))) {
					return ing;
				}
			}
		}

		for (IngListDataItem ing : ingList) {
			IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());
			String ingName = extractIngName(ingItem);
			if (ingredientName.equals(ingName)) {
				return ing;
			}
		}
		return null;
	}

	/**
	 * <p>createReqCtrl.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param reqCtrlMessage a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @param reqType a {@link fr.becpg.repo.regulatory.RequirementType} object
	 * @return a {@link fr.becpg.repo.regulatory.RequirementListDataItem} object
	 */
	protected RequirementListDataItem createReqCtrl(NodeRef ing, MLText reqCtrlMessage, RequirementType reqType) {
		RequirementListDataItem reqCtrlItem = new RequirementListDataItem();
		reqCtrlItem.setReqType(reqType);
		reqCtrlItem.setCharact(ing);
		reqCtrlItem.addSource(ing);
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqCtrlMessage);
		reqCtrlItem.setFormulationChainId(RegulatoryService.REGULATORY_KEY);
		return reqCtrlItem;
	}

	private boolean buildQuery(IngListDataItem ingListDataItem, Map<String, String> params) {
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

	private JSONObject findIngByNumber(NodeRef ing, JSONArray results, String type) {
		for (Entry<QName, String> entry : ingNumbers.entrySet()) {
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

}
