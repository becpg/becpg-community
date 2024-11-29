package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.helper.DecernisHelper;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;
import fr.becpg.repo.decernis.model.UsageContext;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>V5DecernisAnalysisPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class V5DecernisAnalysisPlugin extends DefaultDecernisAnalysisPlugin implements DecernisAnalysisPlugin {

	private static final String RESULT_INDICATOR = "resultIndicator";

	private static final String RECIPE_REPORT = "recipeReport";

	private static final String RECIPE_ANALAYSIS_REPORT = "recipeAnalaysisReport";

	private static final int DECERNIS_MAX_COUNTRIES = 20;

	private static final Log logger = LogFactory.getLog(V5DecernisAnalysisPlugin.class);

	/**
	 * <p>Constructor for V5DecernisAnalysisPlugin.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public V5DecernisAnalysisPlugin(@Qualifier("nodeService") NodeService nodeService, SystemConfigurationService systemConfigurationService) {
		super(nodeService, systemConfigurationService);
	}
	
	private static final Map<Integer, String> moduleIdMap = new HashMap<>();

	private static final String PARAM_COUNTRY = "country";

	private static final String PARAM_NAME = "name";

	static {
		moduleIdMap.put(1, "ADD");
		moduleIdMap.put(2, "SOI");
		moduleIdMap.put(9, "COS");
		moduleIdMap.put(100, "PC");
	}

	private List<String> availableCountries;

	private Map<Integer, List<String>> functionsMap = new ConcurrentHashMap<>();

	/** {@inheritDoc} */
	@Override
	public boolean isEnabled() {
		return analysisUrl() != null && !analysisUrl().isBlank() && !analysisUrl().equals(serverUrl());
	}

	/** {@inheritDoc} */
	@Override
	public boolean needsRecipeId() {
		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public void extractRequirements(RegulatoryContext productContext, RegulatoryContextItem contextItem) {
	
		for (UsageContext usageContext : contextItem.getUsages()) {
			
			List<List<String>> countriesBatch = Lists.partition(new ArrayList<>(contextItem.getCountries().keySet()), DECERNIS_MAX_COUNTRIES);
			for (List<String> countries : countriesBatch) {
				if (!usageContext.getName().endsWith(DecernisService.MODULE_SUFFIX)) {
					JSONObject recipeAnalysisResults = null;
					try {
						recipeAnalysisResults = postV5RecipeAnalysis(productContext, countries, usageContext.getName(), usageContext.getModuleId());
					} catch (HttpStatusCodeException e) {
						logger.error("Error during Decernis recipe analysis: " + e.getMessage(), e);
						for (String country : countries) {
							ReqCtrlListDataItem req = ReqCtrlListDataItem.forbidden()
									.withMessage(MLTextHelper.getI18NMessage("message.decernis.error",
											"Error while creating Decernis recipe: " + e.getMessage()))
									.ofDataType(RequirementDataType.Formulation).withFormulationChainId(DecernisService.DECERNIS_CHAIN_ID)
									.withRegulatoryCode(country + (!usageContext.getName().isEmpty() ? " - " + usageContext.getName() : ""));
							
							productContext.getRequirements().add(req);
						}
					}
					if (recipeAnalysisResults != null) {
						parseRecipeAnalysisResults(productContext, contextItem, usageContext, countries, recipeAnalysisResults);
					}
				}
			}
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void ingredientAnalysis(RegulatoryContext productContext, RegulatoryContextItem contextItem) {
		List<List<String>> countriesBatch = Lists.partition(new ArrayList<>(contextItem.getCountries().keySet()), DECERNIS_MAX_COUNTRIES);
		for (List<String> countries : countriesBatch) {
			JSONObject ingredientAnalysisResults = null;
			try {
				ingredientAnalysisResults = postV5IngredientAnalysis(productContext, contextItem, countries);
			} catch (HttpStatusCodeException e) {
				logger.error("Error during Decernis ingredients analysis: " + e.getMessage(), e);
				ReqCtrlListDataItem req = ReqCtrlListDataItem.forbidden()
						.withMessage(MLTextHelper.getI18NMessage("message.decernis.error",
								"Error while creating Decernis recipe: " + e.getMessage()))
						.ofDataType(RequirementDataType.Formulation).withFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
				productContext.getRequirements().add(req);
			}
			if (ingredientAnalysisResults != null) {
				parseIngredientAnalysisResults(productContext, contextItem, countries, ingredientAnalysisResults);
			}
		}
	}

	private JSONObject postV5RecipeAnalysis(RegulatoryContext context, List<String> countries, String usage, Integer moduleId) throws JSONException {

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

		for (IngListDataItem ingListDataItem : context.getProduct().getIngList()) {
			String function = null;
			NodeRef ingType = (NodeRef) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_TYPE_V2);
			if (ingType != null) {
				String functionValue = (String) nodeService.getProperty(ingType, PLMModel.PROP_REGULATORY_CODE);
				if (functionValue != null) {
					function = findFunction(moduleId, functionValue);
				}
				if (function == null) {
					functionValue = (String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_CODE);
					if (functionValue != null) {
						function = findFunction(moduleId, functionValue);
					}
				}
				if (function == null) {
					functionValue = (String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_VALUE);
					if (functionValue != null) {
						function = findFunction(moduleId, functionValue);
					}
				}
			}
			String rid = (String) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);
			if (rid != null && !rid.isBlank() && !DecernisService.NOT_APPLICABLE.equals(rid)) {
				String legalName = (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME);
				String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
						: (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);
				Double ingQtyPerc = DecernisHelper.truncateDoubleValue(ingListDataItem.getQtyPerc());
				JSONObject ingredient = new JSONObject();
				ingredient.put(PARAM_NAME, ingName);
				ingredient.put("spec", ingName);
				ingredient.put("idType", "Decernis ID");
				ingredient.put("idValue", rid);
				ingredient.put("percentage", ingQtyPerc == null ? 0d : ingQtyPerc);
				if (function != null) {
					ingredient.put("function", function);
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
			countries.forEach(country::put);

			JSONArray topics = new JSONArray();
			scope.put("topic", topics);

			JSONObject topic = new JSONObject();
			topics.put(topic);

			topic.put(PARAM_NAME, moduleIdMap.get(moduleId));
			JSONObject scopeDetail = new JSONObject();
			topic.put("scopeDetail", scopeDetail);

			JSONArray usages = new JSONArray();
			usages.put(usage);

			scopeDetail.put("usage", usages);

			String url = analysisUrl() + "/recipe-analysis/transaction";

			HttpEntity<String> entity = createEntity(payload.toString());
			
			if (logger.isTraceEnabled()) {
				logger.trace("POST url: " + url + " body: " + payload);
			}
			recipeAnalysisResult = RestTemplateHelper.getRestTemplate().postForObject(url, entity, String.class, new HashMap<>());

			return new JSONObject(recipeAnalysisResult);
		}

		return null;
	}
	
	private JSONObject postV5IngredientAnalysis(RegulatoryContext context, RegulatoryContextItem contextItem, List<String> countries) throws JSONException {
		
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
		
		for (IngListDataItem ingListDataItem : context.getProduct().getIngList()) {
			String rid = (String) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);
			if (rid != null && !rid.isBlank() && !DecernisService.NOT_APPLICABLE.equals(rid)) {
				String legalName = (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME);
				String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
						: (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);
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
			countries.forEach(country::put);
			
			JSONArray topics = new JSONArray();
			scope.put("topic", topics);
			
			JSONObject topic = new JSONObject();
			topics.put(topic);
			
			topic.put(PARAM_NAME, moduleIdMap.get(contextItem.getUsages().get(0).getModuleId()));
			JSONObject scopeDetail = new JSONObject();
			topic.put("scopeDetail", scopeDetail);
			
			JSONArray usages = new JSONArray();
			for (UsageContext usage : contextItem.getUsages()) {
				if (!usage.getName().endsWith(DecernisService.MODULE_SUFFIX)) {
					usages.put(usage.getName());
				}
			}
			
			scopeDetail.put("usage", usages);
			
			String url = analysisUrl() + "/ingredient-analysis/transaction";
			
			HttpEntity<String> entity = createEntity(payload.toString());
			
			if (logger.isTraceEnabled()) {
				logger.trace("POST url: " + url + " body: " + payload);
			}
			ingredientAnalysisResult = RestTemplateHelper.getRestTemplate().postForObject(url, entity, String.class, new HashMap<>());
			
			return new JSONObject(ingredientAnalysisResult);
		}
		
		return null;
	}

	private String findFunction(Integer moduelId, String ingTypeValue) {
		if (!functionsMap.containsKey(moduelId)) {
			List<String> functions = fetchFunctions(moduelId);
			functionsMap.put(moduelId, functions);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("functionsMap=" + functionsMap);
		}
		for (String function : functionsMap.get(moduelId)) {
			if (function.trim().equalsIgnoreCase(ingTypeValue.trim())) {
				return function;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Ingredient function is not recognized by Decernis v5 API: " + ingTypeValue + ", available functions are: "
					+ functionsMap.get(moduelId));
		}
		return null;
	}

	private List<String> fetchFunctions(Integer moduelId) {

		List<String> functions = new ArrayList<>();

		String url = analysisUrl() + "/scope/function?topic=" + moduleIdMap.get(moduelId);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token().trim());
		
		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url);
		}
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, new HashMap<>());

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

	private void parseRecipeAnalysisResults(RegulatoryContext productContext, RegulatoryContextItem contextItem, UsageContext usageContext,
			List<String> countries, JSONObject analysisResults) {
		for (String country : countries) {

			if (isAvailableCountry(country) && analysisResults.has(RECIPE_ANALAYSIS_REPORT)) {

				JSONObject recipeAnalaysisReport = analysisResults.getJSONObject(RECIPE_ANALAYSIS_REPORT);

				if (logger.isTraceEnabled()) {
					logger.trace(recipeAnalaysisReport.toString(3));
				}

				if (recipeAnalaysisReport.has(RECIPE_REPORT)) {

					JSONArray recipeReport = recipeAnalaysisReport.getJSONArray(RECIPE_REPORT);

					for (int i = 0; i < recipeReport.length(); i++) {
						JSONObject report = recipeReport.getJSONObject(i);
						if (report.has("country") && report.getString("country").equals(country)) {

							JSONArray tabularReports = report.getJSONArray("tabularReport");

							for (int j = 0; j < tabularReports.length(); j++) {
								JSONObject tabularReport = tabularReports.getJSONObject(j);

								String usage = usageContext.getName();

								String decernisID = tabularReport.getString("did");
								String function = tabularReport.getString("function");
								String ingredientName = tabularReport.getString(PARAM_NAME);

								IngListDataItem ingItem = findIngredientItemV5(productContext.getProduct().getIngList(), decernisID,
										function, ingredientName);

								if (ingItem != null) {

									if (tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("prohibited")
											|| tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("over limit")) {
										String threshold = (tabularReport.has(THRESHOLD)
												&& !tabularReport.getString(THRESHOLD).equals("None")
														? "(" + tabularReport.getString(THRESHOLD) + ")"
														: "");

										MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, threshold);
										ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage,
												RequirementType.Forbidden);
										reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
										reqCtrlItem.setReqMaxQty(0d);
										if (!threshold.isBlank() && ingItem != null && ingItem.getQtyPerc() != null && ingItem.getQtyPerc() != 0d) {
											Double thresholdValue = DecernisHelper.extractThresholdValue(threshold);
											if (thresholdValue != null) {
												reqCtrlItem.setReqMaxQty((thresholdValue / ingItem.getQtyPerc()) * 100d);
											}
										}

										productContext.getRequirements().add(reqCtrlItem);
										if (logger.isDebugEnabled()) {
											logger.debug("Adding prohibited ing :" + tabularReport.getString("did"));
										}

									} else if (tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("not listed")) {
										MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
										ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage,
												RequirementType.Tolerated);
										reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
										productContext.getRequirements().add(reqCtrlItem);
										if (logger.isDebugEnabled()) {
											logger.debug("Adding not listed ing :" + tabularReport.getString("did"));
										}
									} else if (Boolean.TRUE.equals(addInfoReqCtrl())) {

										String threshold = (tabularReport.has(THRESHOLD)
												&& !tabularReport.getString(THRESHOLD).equals("None") ? tabularReport.getString(THRESHOLD)
														: "");

										MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PERMITTED_ING,
												tabularReport.getString(RESULT_INDICATOR), threshold);
										ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage,
												RequirementType.Info);

										reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
										productContext.getRequirements().add(reqCtrlItem);
										if (logger.isDebugEnabled()) {
											logger.debug(
													"Adding " + reqMessage.getDefaultValue() + " ing :" + tabularReport.getString("did"));
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
	
	private void parseIngredientAnalysisResults(RegulatoryContext productContext, RegulatoryContextItem contextItem,
			List<String> countries, JSONObject analysisResults) {
		for (String country : countries) {
			
			if (isAvailableCountry(country) && analysisResults.has("ingredientAnalysisReport")) {
				
				JSONObject ingredientAnalaysisReport = analysisResults.getJSONObject("ingredientAnalysisReport");
				
				if (logger.isTraceEnabled()) {
					logger.trace(ingredientAnalaysisReport.toString(3));
				}
				
				if (ingredientAnalaysisReport.has("tabularReport")) {
					
					JSONArray tabularReports = ingredientAnalaysisReport.getJSONArray("tabularReport");
					Map<String, List<JSONObject>> countryReports = findReportsForCountry(tabularReports, country);
					
					for (Entry<String, List<JSONObject>> entry : countryReports.entrySet()) {
						String decernisID = entry.getKey();
						List<JSONObject> countryDidReports = entry.getValue();
						IngListDataItem ingItem = findIngredientItemV5(productContext.getProduct().getIngList(), decernisID, null,
								countryDidReports.get(0).getString("customerName"));
						IngRegulatoryListDataItem ingRegulatoryListDataItem = createIngRegulatoryListDataItem(ingItem.getIng(),
								contextItem.getCountries().get(country));

						String usage = String.join(";;",
								countryDidReports.stream()
										.filter(j -> j.getJSONObject("comments").get("usageOnList") != null
												&& !j.getJSONObject("comments").get("usageOnList").toString().isBlank()
												&& !j.getJSONObject("comments").get("usageOnList").toString().equals("null"))
										.map(j -> j.getJSONObject("comments").getString("usageOnList"))
										.distinct()
										.toList());
						ingRegulatoryListDataItem.setUsages(new MLText(usage));

						String citation = String.join(";;",
								countryDidReports.stream()
										.filter(j -> j.getJSONObject("comments").get("usageOnList") != null
												&& !j.getJSONObject("comments").get("usageOnList").toString().isBlank()
												&& !j.getJSONObject("comments").get("usageOnList").toString().equals("null"))
										.filter(j -> j.get(CITATION) != null && !j.get(CITATION).toString().isBlank()
												&& !j.get(CITATION).toString().equals("null"))
										.map(j -> j.getJSONObject("comments").getString("usageOnList") + " :: " + j.getString(CITATION))
										.distinct()
										.toList());
						ingRegulatoryListDataItem.setCitation(new MLText(citation));

						String restrictionLevel = String.join(";;",
								countryDidReports.stream()
										.filter(j -> j.getJSONObject("comments").get("usageOnList") != null
												&& !j.getJSONObject("comments").get("usageOnList").toString().isBlank()
												&& !j.getJSONObject("comments").get("usageOnList").toString().equals("null"))
										.filter(j -> j.get(THRESHOLD) != null && !j.get(THRESHOLD).toString().isBlank()
												&& !j.get(THRESHOLD).toString().equals("null"))
										.map(j -> j.getJSONObject("comments").getString("usageOnList") + " :: " + j.getString(THRESHOLD))
										.distinct()
										.toList());
						ingRegulatoryListDataItem.setRestrictionLevels(new MLText(restrictionLevel));
						
						String precautions = String.join(";;",
								countryDidReports.stream()
								.filter(j -> j.getJSONObject("comments").get("comments") != null
								&& !j.getJSONObject("comments").get("comments").toString().isBlank()
								&& !j.getJSONObject("comments").get("comments").toString().equals("null"))
								.map(j -> j.getJSONObject("comments").getString("comments"))
								.distinct()
								.toList());
						ingRegulatoryListDataItem.setPrecautions(new MLText(precautions));

						String resultIndicator = String.join(";;",
								countryDidReports.stream()
										.filter(j -> j.getJSONObject("comments").get("usageOnList") != null
												&& !j.getJSONObject("comments").get("usageOnList").toString().isBlank()
												&& !j.getJSONObject("comments").get("usageOnList").toString().equals("null"))
										.filter(j -> j.get(RESULT_INDICATOR) != null && !j.get(RESULT_INDICATOR).toString().isBlank()
												&& !j.get(RESULT_INDICATOR).toString().equals("null"))
										.map(j -> j.getJSONObject("comments").getString("usageOnList") + " :: " + j.getString(RESULT_INDICATOR))
										.distinct()
										.toList());
						ingRegulatoryListDataItem.setResultIndicator(new MLText(resultIndicator));

						productContext.getIngRegulatoryList().add(ingRegulatoryListDataItem);
					}
				}
			}
		}
	}

	private Map<String, List<JSONObject>> findReportsForCountry(JSONArray tabularReports, String country) {
		Map<String, List<JSONObject>> map = new HashMap<>();
		for (int i = 0; i < tabularReports.length(); i++) {
			JSONObject tabularReport = tabularReports.getJSONObject(i);
			if (tabularReport.has("country") && tabularReport.getString("country").equals(country)) {
				List<JSONObject> list = map.computeIfAbsent(tabularReport.get("decernisId").toString(), k -> new ArrayList<>());
				list.add(tabularReport);
			}
		}
		return map;
	}

	private IngListDataItem findIngredientItemV5(List<IngListDataItem> ingList, String decernisID, String function, String ingredientName) {
		for (IngListDataItem ing : ingList) {
			if (decernisID.equals(nodeService.getProperty(ing.getIng(), PLMModel.PROP_REGULATORY_CODE))) {
				NodeRef ingType = (NodeRef) nodeService.getProperty(ing.getIng(), PLMModel.PROP_ING_TYPE_V2);
				if (ingType != null && function != null
						&& (function.equalsIgnoreCase((String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_VALUE))
								|| function.equalsIgnoreCase((String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_CODE))
								|| function.equalsIgnoreCase((String) nodeService.getProperty(ingType, PLMModel.PROP_REGULATORY_CODE)))) {
					return ing;
				}
			}
		}

		for (IngListDataItem ing : ingList) {
			String legalName = (String) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_LEGAL_NAME);

			String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
					: (String) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_CHARACT_NAME);

			if (ingredientName.equals(ingName)) {
				return ing;
			}
		}
		return null;
	}

	private boolean isAvailableCountry(String country) {
		if (availableCountries == null) {
			availableCountries = fetchAvailableCountries();
		}
		return availableCountries.contains(country);
	}

	private List<String> fetchAvailableCountries() {

		List<String> countries = new ArrayList<>();

		String url = analysisUrl() + "/scope/country";

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token().trim());

		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url);
		}
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, new HashMap<>());

		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {
			JSONObject responseBody = new JSONObject(response.getBody());

			if (responseBody.has("countries")) {
				JSONArray countryArray = responseBody.getJSONArray("countries");
				for (int i = 0; i < countryArray.length(); i++) {
					countries.add(countryArray.getJSONObject(i).getString(PARAM_COUNTRY));
				}
			}
		}

		return countries;
	}

}
