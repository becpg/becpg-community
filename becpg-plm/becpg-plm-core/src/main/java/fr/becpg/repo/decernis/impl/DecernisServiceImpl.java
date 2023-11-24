package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisMode;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.helper.DecernisHelper;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationChainPlugin;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RegulatoryResult;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>DecernisServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("decernisService")
public class DecernisServiceImpl implements DecernisService, FormulationChainPlugin {

	private static final Log logger = LogFactory.getLog(DecernisServiceImpl.class);

	private static final String MESSAGE_NO_RID_ING = "message.decernis.ingredient.noRid";
	private static final String MESSAGE_NO_FUNCTION_ING = "message.decernis.ingredient.noFunction";
	private static final String MESSAGE_NO_CODE_CHARACT = "message.decernis.charact.noCode";
	private static final String MESSAGE_SEVERAL_RID_ING = "message.decernis.ingredient.severalRid";

	private static final String PARAM_COMPANY = "company";
	private static final String PARAM_FORMULA = "formula";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_RESULTS = "results";
	private static final String PARAM_QUERY = "query";

	private static final String MISSING_VALUE = "NA";

	private final RestTemplate restTemplate;

	private final NodeService nodeService;

	private final DecernisAnalysisPlugin[] decernisPlugins;

	private final SystemConfigurationService systemConfigurationService;

	private static final int DECERNIS_MAX_COUNTRIES = 20;
	
	private static final Map<String, Integer> moduleIdMap = new HashMap<>();

	private static final Map<QName, String> ingNumbers = new HashMap<>();

	static {
		ingNumbers.put(PLMModel.PROP_CAS_NUMBER, "CAS");
		ingNumbers.put(PLMModel.PROP_EC_NUMBER, "EC No.");
		ingNumbers.put(PLMModel.PROP_CE_NUMBER, "EINECS");
		ingNumbers.put(PLMModel.PROP_FEMA_NUMBER, "FEMA No.");
		ingNumbers.put(PLMModel.PROP_FL_NUMBER, "FL No.");
		ingNumbers.put(PLMModel.PROP_FDA_NUMBER, "FDA Cat.");

		moduleIdMap.put("FOOD_ADDITIVES", 1);
		moduleIdMap.put("STANDARDS_OF_IDENTITY_FOOD", 2);
		moduleIdMap.put("COSMETICS", 9);
		moduleIdMap.put("FORMULATION_CHECK", 100);
	}

	public DecernisServiceImpl(@Qualifier("logRestTemplate") RestTemplate restTemplate, NodeService nodeService,
			DecernisAnalysisPlugin[] decernisPlugins, SystemConfigurationService systemConfigurationService) {
		super();
		this.restTemplate = restTemplate;
		this.nodeService = nodeService;
		this.decernisPlugins = decernisPlugins;
		this.systemConfigurationService = systemConfigurationService;
	}

	private String serverUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.serverUrl");
	}

	private String companyName() {
		return systemConfigurationService.confValue("beCPG.decernis.companyName");
	}

	private String token() {
		return systemConfigurationService.confValue("beCPG.decernis.token");
	}
	
	@Override
	public String getChainId() {
		return DECERNIS_CHAIN_ID;
	}

	@Override
	public boolean isChainActiveOnEntity(NodeRef entityNodeRef) {
		return isEnabled();
	}

	@Override
	public boolean isEnabled() {
		return serverUrl() != null && !serverUrl().isBlank() && token() != null && !token().isBlank();
	}

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> extractRequirements(ProductData product) {

		try {
			resetRegulatoryResults(product);
			
			RegulatoryContext context = createContext(product);

			if (context.isTreatable()) {
				
				checkIngredients(context);
				
				boolean recipeCreated = false;

				if (DecernisMode.BOTH.equals(context.getRegulatoryMode()) || DecernisMode.DECERNIS_ONLY.equals(context.getRegulatoryMode())) {
					createRecipe(context);
					updateContextItemsRecipeId(context);
					recipeCreated = true;
				}

				if (DecernisMode.BOTH.equals(context.getRegulatoryMode()) || DecernisMode.BECPG_ONLY.equals(context.getRegulatoryMode())) {
					if (getAnalysisPlugin().needsRecipeId() && !recipeCreated) {
						createRecipe(context);
					}
					analyzeRecipe(context);
					checkUsagesID(context);
				}

				if (DecernisMode.BECPG_ONLY.equals(context.getRegulatoryMode()) && context.getRegulatoryRecipeId() != null) {
					deleteRecipe(context.getRegulatoryRecipeId());
					context.getProduct().setRegulatoryRecipeId(null);
				}
			}

			return context.getRequirements();

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			logger.error("Decernis HTTP ERROR STATUS: " + e.getStatusText());
			logger.error("- error body: " + e.getResponseBodyAsString());
			throw new FormulateException("Error calling decernis service: " + e.getStatusText(), e);
		} catch (Exception e) {
			throw new FormulateException("Unexpected decernis error: " + e.getMessage(), e);
		}
	}

	private void resetRegulatoryResults(ProductData product) {
		product.setRegulatoryResult(null);
		for (RegulatoryListDataItem regulatoryItem : product.getRegulatoryList()) {
			regulatoryItem.setRegulatoryResult(null);
		}
	}

	private DecernisAnalysisPlugin getAnalysisPlugin() {
		for (DecernisAnalysisPlugin plugin : decernisPlugins) {
			if (plugin.isEnabled()) {
				return plugin;
			}
		}
		throw new IllegalStateException("At least one plugin should be provided");
	}

	private void analyzeRecipe(RegulatoryContext productContext) {
		for (RegulatoryContextItem contextItem : productContext.getContextItems()) {
			if (!contextItem.isEmpty()) {
				List<JSONObject> analysisList = analyzeContext(productContext, contextItem.getUsages(), contextItem.getCountries(),
						contextItem.getModuleId());
				contextItem.getItem().setRegulatoryResult(extractResult(analysisList));
			}
		}
		if (!productContext.isEmpty()) {
			List<JSONObject> analysisList = analyzeContext(productContext, productContext.getUsages(), productContext.getCountries(),
					productContext.getModuleId());
			productContext.getProduct().setRegulatoryResult(extractResult(analysisList));
		}
	}

	private RegulatoryResult extractResult(List<JSONObject> analysisList) {

		if (analysisList.isEmpty()) {
			return null;
		}
		
		boolean notListed = false;

		for (JSONObject analysis : analysisList) {
			String result = getAnalysisPlugin().extractAnalysisResult(analysis);
			if (result.startsWith("prohibited")) {
				return RegulatoryResult.PROHIBITED;
			}
			if (result.startsWith("not listed")) {
				notListed = true;
			}
		}

		if (notListed) {
			return RegulatoryResult.NOT_LISTED;
		}

		return RegulatoryResult.PERMITTED;
	}

	private List<JSONObject> analyzeContext(RegulatoryContext productContext, Set<String> usages, Set<String> countries, Integer moduleId) {
		List<JSONObject> analysisList = new ArrayList<>();
		for (String usage : usages) {
			
			Set<String> countriesBatch = new HashSet<>();

			for (String country : countries) {
			    countriesBatch.add(country);
			    if (countriesBatch.size() == DECERNIS_MAX_COUNTRIES) {
			    	analyzeSubContext(productContext, moduleId, analysisList, usage, countriesBatch);
			        countriesBatch.clear();
			    }
			}

			if (!countriesBatch.isEmpty()) {
				analyzeSubContext(productContext, moduleId, analysisList, usage, countriesBatch);
			}
		}
		return analysisList;
	}

	private void analyzeSubContext(RegulatoryContext productContext, Integer moduleId, List<JSONObject> analysisList, String usage, Set<String> countries) {
		JSONObject analysis = getAnalysisPlugin().postRecipeAnalysis(productContext, countries, usage, moduleId);
		if (analysis != null) {
			for (String countryBatch : countries) {
				productContext.getRequirements()
						.addAll(getAnalysisPlugin().extractRequirements(analysis, productContext.getProduct().getIngList(), countryBatch, moduleId));
			}
			analysisList.add(analysis);
		}
	}

	private void checkUsagesID(RegulatoryContext context) {
		for (NodeRef usageRef : context.getProduct().getRegulatoryUsages()) {
			updateUsageID(usageRef, context.getModuleId());
		}
		for (RegulatoryContextItem contextItem : context.getContextItems()) {
			for (NodeRef usageRef : contextItem.getItem().getRegulatoryUsages()) {
				updateUsageID(usageRef, contextItem.getModuleId());
			}
		}
	}

	private void updateContextItemsRecipeId(RegulatoryContext context) {
		for (RegulatoryContextItem contextItem : context.getContextItems()) {
			contextItem.getItem().setRegulatoryRecipeId(context.getRegulatoryRecipeId());
		}
	}

	private void createRecipe(RegulatoryContext context) {

		JSONObject recipePayload = createRecipePayload(context);

		String recipeId = null;

		if (recipePayload != null) {
			String url = serverUrl() + "/formulas";
			HttpEntity<String> request = createEntity(recipePayload.toString());
			JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url, request, String.class));
			if (jsonObject.has("id")) {
				recipeId = jsonObject.get("id").toString();
			}
		}

		context.getProduct().setRegulatoryRecipeId(recipeId);

		for (RegulatoryContextItem contextItem : context.getContextItems()) {
			contextItem.getItem().setRegulatoryRecipeId(recipeId);
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

		for (IngListDataItem ingListDataItem : context.getProduct().getIngList()) {

			if (ingListDataItem.getIng() != null) {

				String legalName = (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME);

				String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
						: (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);
				String rid = (String) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);

				Double ingQtyPerc = DecernisHelper.truncateDoubleValue(ingListDataItem.getQtyPerc());

				NodeRef ingType = (NodeRef) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_TYPE_V2);
				String function = null;
				if (ingType != null) {
					function = (String) nodeService.getProperty(ingType, PLMModel.PROP_REGULATORY_CODE);
				}
				try {
					if ((rid != null) && !rid.isEmpty() && !rid.equals(MISSING_VALUE) && ((function != null) && !function.isEmpty())
							&& ((ingName != null) && !ingName.isEmpty()) && (ingQtyPerc != null)) {
						JSONObject ingredient = new JSONObject();
						ingredient.put("name", ingName);
						ingredient.put("percentage", ingQtyPerc);
						ingredient.put("ingredient_did", rid);
						ingredient.put("function", function);
						ingredient.put("spec_parameters", JSONObject.NULL);
						ingredient.put("upper_limit", JSONObject.NULL);
						ingredients.put(ingredient);
					} else if (function == null || function.isBlank()) {
						context.getRequirements().add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_FUNCTION_ING),
								RequirementType.Tolerated));
					}

				} catch (HttpClientErrorException | HttpServerErrorException e) {
					logger.warn("Cannot retrieve ingredient " + ingName + " error:" + e.getStatusText());
				} catch (Exception e) {
					logger.error(e, e);
					throw new FormulateException("Unexpected decernis error", e);
				}
			}
		}

		if (ingredients.length() > 0) {
			return ret;
		}
		return null;
	}

	private void checkIngredients(RegulatoryContext context) {
		for (IngListDataItem ingListDataItem : context.getProduct().getIngList()) {
			if (ingListDataItem.getIng() != null) {
				String rid = (String) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);
				if (rid == null || rid.isEmpty()) {
					rid = fetchIngredientId(ingListDataItem, context.getRequirements());
					nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE, rid);
				}
				NodeRef ingType = (NodeRef) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_TYPE_V2);
				if (ingType == null) {
					context.getRequirements().add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_FUNCTION_ING),
							RequirementType.Tolerated));
				}
			}
		}
	}

	private HttpEntity<String> createEntity(String param) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBearerAuth(cleanToken(token()));
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(param, headers);
	}

	private String cleanToken(String token) {
		return token!=null ? token.replace("Bearer ", "").strip() : "";
	}

	private boolean buildQuery(IngListDataItem ingListDataItem, Map<String, String> params) {
		Iterator<Map.Entry<QName, String>> iterator = ingNumbers.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<QName, String> ingNumber = iterator.next();
			String number = (String) nodeService.getProperty(ingListDataItem.getIng(), ingNumber.getKey());
			if ((number != null) && !number.isEmpty() && !number.equals(MISSING_VALUE) && !number.contains(",")) {
				params.put(PARAM_QUERY, number);
				params.put("type", ingNumber.getValue());
				return true;
			}
		}

		return false;
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

	private String fetchIngredientId(IngListDataItem ingListDataItem, List<ReqCtrlListDataItem> requirements) {

		String legalName = (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME);

		String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
				: (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);

		String ingredientId = "";

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

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params);

		if ((response != null) && HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {

			JSONObject jsonObject = new JSONObject(response.getBody());

			if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) >= 1) && jsonObject.has(PARAM_RESULTS)) {
				JSONArray results = jsonObject.getJSONArray(PARAM_RESULTS);
				JSONObject result = null;
				if (jsonObject.getInt(PARAM_COUNT) == 1) {
					result = results.getJSONObject(0);
				} else {
					result = getRidByIngName(results, ingName);
				}
				if (result == null) {
					if (results.toList().stream().map(o -> ((Map<?, ?>) o).get("did")).distinct().count() == 1) {
						result = results.getJSONObject(0);
					}
				}
				if (result != null) {
					ingredientId = result.get("did").toString();
					if (logger.isDebugEnabled()) {
						logger.debug("RID of ingredient " + params.get(PARAM_QUERY) + ": " + ingredientId);
					}
					nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE, ingredientId);
					// Get ingredient numbers (CAS, FEMA, CE)
					if (result.has("libidents")) {
						JSONObject libidents = result.getJSONObject("libidents");
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

				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Several RIDs for ingredient " + params.get(PARAM_QUERY));
					}
					requirements.add(
							createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_SEVERAL_RID_ING), RequirementType.Tolerated));
				}
			} else if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) == 0)) {
				requirements.add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_RID_ING), RequirementType.Tolerated));
			}
		} else {
			requirements.add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_RID_ING), RequirementType.Forbidden));
		}
		return ingredientId;
	}

	private ReqCtrlListDataItem createReqCtrl(NodeRef ing, MLText reqCtrlMessage, RequirementType reqType) {
		ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
		reqCtrlItem.setReqType(reqType);
		reqCtrlItem.setCharact(ing);
		reqCtrlItem.addSource(ing);
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqCtrlMessage);
		reqCtrlItem.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
		return reqCtrlItem;
	}

	private void deleteRecipe(String recipeId) {
		try {
			Map<String, String> params = new HashMap<>();
			params.put(PARAM_COMPANY, companyName());
			params.put(PARAM_FORMULA, recipeId);

			restTemplate.exchange(serverUrl() + "/formulas/" + recipeId + "?current_company={company}", HttpMethod.DELETE, createEntity(null),
					String.class, params);
		} catch (Exception e) {
			logger.error("failed to delete recipe: " + recipeId, e);
		}
	}

	private RegulatoryContext createContext(ProductData product) {
		RegulatoryContext context = new RegulatoryContext();
		context.setProduct(product);

		Set<String> countries = new HashSet<>();
		Set<String> usages = new HashSet<>();

		for (NodeRef nodeRef : product.getRegulatoryCountries()) {
			extractCodes(context, countries, nodeRef);
		}
		for (NodeRef nodeRef : product.getRegulatoryUsages()) {
			extractCodes(context, usages, nodeRef);
		}

		context.setCountries(countries);
		context.setUsages(usages);
		context.setModuleId(extractModuleId(product.getRegulatoryUsages()));

		for (RegulatoryListDataItem item : product.getRegulatoryList()) {
			context.getContextItems().add(createContextItem(item, context));
		}

		return context;
	}

	private RegulatoryContextItem createContextItem(RegulatoryListDataItem item, RegulatoryContext context) {

		RegulatoryContextItem contextItem = new RegulatoryContextItem();
		contextItem.setItem(item);

		Set<String> countries = new HashSet<>();
		Set<String> usages = new HashSet<>();
		Integer moduleId = null;

		for (NodeRef nodeRef : item.getRegulatoryCountries()) {
			extractCodes(context, countries, nodeRef);
		}
		for (NodeRef nodeRef : item.getRegulatoryUsages()) {
			extractCodes(context, usages, nodeRef);
		}
		moduleId = extractModuleId(item.getRegulatoryUsages());

		contextItem.setCountries(countries);
		contextItem.setUsages(usages);
		contextItem.setModuleId(moduleId);

		return contextItem;
	}

	private void extractCodes(RegulatoryContext context, Set<String> countries, NodeRef nodeRef) {
		String code = extractCode(nodeRef);
		if (code == null || code.isBlank()) {
			String name = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CHARACT_NAME);
			logger.warn("charact " + name + " has no regulatoryCode");
			context.getRequirements().add(createReqCtrl(null, MLTextHelper.getI18NMessage(MESSAGE_NO_CODE_CHARACT, name), RequirementType.Tolerated));
		} else {
			countries.add(code);
		}
	}

	private Integer extractModuleId(List<NodeRef> regulatoryUsages) {
		return regulatoryUsages.stream().map(usage -> nodeService.getProperty(usage, PLMModel.PROP_REGULATORY_MODULE)).map(moduleIdMap::get)
				.findFirst().orElse(null);
	}

	private String extractCode(NodeRef node) {
		return (String) nodeService.getProperty(node, PLMModel.PROP_REGULATORY_CODE);
	}

	private void updateUsageID(NodeRef usageRef, Integer moduleId) {

		if (nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_ID) instanceof String) {
			return;
		}

		String usageCode = extractCode(usageRef);

		ResponseEntity<String> response = restTemplate.exchange(
				serverUrl() + "/usages/structurized" + "?module_id=" + moduleId + "&phrase=" + usageCode, HttpMethod.GET, createEntity(null),
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

}
