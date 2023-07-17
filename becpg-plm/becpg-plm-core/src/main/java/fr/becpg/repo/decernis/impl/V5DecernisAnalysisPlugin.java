package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;

@Service
public class V5DecernisAnalysisPlugin implements DecernisAnalysisPlugin {

	private static final String RESULT_INDICATOR = "resultIndicator";

	private static final String RECIPE_REPORT = "recipeReport";

	private static final String RECIPE_ANALAYSIS_REPORT = "recipeAnalaysisReport";

	private static final Log logger = LogFactory.getLog(V5DecernisAnalysisPlugin.class);
	
	private final NodeService nodeService;
	
	private final SystemConfigurationService systemConfigurationService;
	
	private final RestTemplate restTemplate;
	
	public V5DecernisAnalysisPlugin(NodeService nodeService, SystemConfigurationService systemConfigurationService, @Qualifier("logRestTemplate") RestTemplate restTemplate) {
		super();
		this.nodeService = nodeService;
		this.systemConfigurationService = systemConfigurationService;
		this.restTemplate = restTemplate;
	}
	
	public String serverUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.serverUrl");
	}
	public String analysisUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.analysisUrl");
	}
	public String companyName() {
		return systemConfigurationService.confValue("beCPG.decernis.companyName");
	}
	public String token() {
		return systemConfigurationService.confValue("beCPG.decernis.token");
	}
	public String addInfoReqCtrl() {
		return systemConfigurationService.confValue("beCPG.formulation.specification.addInfoReqCtrll");
	}

	private static final Map<Integer, String> moduleIdMap = new HashMap<>();

	static {
		moduleIdMap.put(1, "ADD");
		moduleIdMap.put(2, "SOI");
		moduleIdMap.put(9, "COS");
		moduleIdMap.put(100, "PC");
	}
	
	private List<String> availableCountries;
	
	private Map<Integer, List<String>> functionsMap = new HashMap<>();
	
	@Override
	public boolean isEnabled() {
		return analysisUrl() != null && !analysisUrl().isBlank() && !analysisUrl().equals(serverUrl());
	}
	
	@Override
	public boolean needsRecipeId() {
		return false;
	}

	@Override
	public String extractAnalysisResult(JSONObject analysisResults) {
		
		boolean notListed = false;
		
		if (analysisResults.has(RECIPE_ANALAYSIS_REPORT)) {
			JSONObject recipeAnalaysisReport = analysisResults.getJSONObject(RECIPE_ANALAYSIS_REPORT);
			if (recipeAnalaysisReport.has(RECIPE_REPORT)) {
				JSONArray recipeReport = recipeAnalaysisReport.getJSONArray(RECIPE_REPORT);
				
				for (int i = 0; i < recipeReport.length(); i++) {
					JSONObject report = recipeReport.getJSONObject(i);
						
					JSONArray matrixReports = report.getJSONArray("matrixReport");
					
					for (int j = 0; j < matrixReports.length(); j++) {
						JSONObject matrixReport = matrixReports.getJSONObject(j);
						String resultIndicator = matrixReport.getString(RESULT_INDICATOR);
						if (resultIndicator.toLowerCase().startsWith("prohibited") || resultIndicator.toLowerCase().startsWith("over limit")) {
							return "prohibited";
						}
						if (resultIndicator.toLowerCase().startsWith("not listed")) {
							notListed = true;
						}
					}
				}
			}
		}
		
		if (notListed) {
			return "not listed";
		}
		
		return "permitted";
	}

	@Override
	public JSONObject postRecipeAnalysis(RegulatoryContext context, Set<String> countries, String usage, Integer moduleId) throws JSONException {
		
		JSONObject payload = new JSONObject();
		
		JSONObject transaction = new JSONObject();
		payload.put("transaction", transaction);
		
		JSONObject recipe = new JSONObject();
		transaction.put("recipe", recipe);
		
		String code = (String) nodeService.getProperty(context.getProduct().getNodeRef(), BeCPGModel.PROP_CODE);
		code += Calendar.getInstance().getTimeInMillis();

		recipe.put("spec", code);
		String name = code + " " + context.getProduct().getName();
		
		recipe.put("name", name);
		
		JSONArray ingredients = new JSONArray();
		recipe.put("ingredients", ingredients);
		
		for (IngListDataItem ingListDataItem : context.getProduct().getIngList()) {
			String rid = (String) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);
			if (rid != null && !rid.isBlank()) {
				String legalName = (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME);
				String ingName = (legalName != null) && !legalName.isEmpty() ? legalName
						: (String) nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);
				Double ingQtyPerc = ingListDataItem.getQtyPerc();
				JSONObject ingredient = new JSONObject();
				NodeRef ingType = (NodeRef) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_TYPE_V2);
				ingredient.put("name", ingName);
				ingredient.put("spec", ingName);
				ingredient.put("idType", "Decernis ID");
				ingredient.put("idValue", rid);
				ingredient.put("percentage", ingQtyPerc);
				ingredient.put("function", findFunction(moduleId, (String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_CODE)));
				ingredients.put(ingredient);
			}
		}
		
		JSONObject scope = new JSONObject();
		transaction.put("scope", scope);
		
		scope.put("name", name);
		
		JSONArray country = new JSONArray();
		scope.put("country", country);
		countries.forEach(country::put);
		
		JSONArray topics = new JSONArray();
		scope.put("topic", topics);
		
		JSONObject topic = new JSONObject();
		topics.put(topic);
		
		topic.put("name", moduleIdMap.get(moduleId));
		JSONObject scopeDetail = new JSONObject();
		topic.put("scopeDetail", scopeDetail);
		
		JSONArray usages = new JSONArray();
		usages.put(usage);
		
		scopeDetail.put("usage", usages);
		
		String url = analysisUrl() + "/recipe-analysis/transaction";
		
		HttpEntity<String> entity = createEntity(payload.toString());
		
		return new JSONObject(restTemplate.postForObject(url, entity, String.class, new HashMap<>()));
	}
	
	private String findFunction(Integer moduelId, String ingTypeValue) {
		if (!functionsMap.containsKey(moduelId)) {
			List<String> functions = fetchFunctions(moduelId);
			functionsMap.put(moduelId, functions);
		}
		for (String function : functionsMap.get(moduelId)) {
			if (function.trim().equalsIgnoreCase(ingTypeValue.trim())) {
				return function;
			}
		}
		throw new FormulateException("Ingredient function is not recognized by Decernis v5 API: " + ingTypeValue);
	}

	private List<String> fetchFunctions(Integer moduelId) {
		
		List<String> functions = new ArrayList<>();
		
		String url = analysisUrl() + "/scope/function?topic=" + moduleIdMap.get(moduelId);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token());

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, new HashMap<>());
		
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

	@Override
	public List<ReqCtrlListDataItem> extractRequirements(JSONObject analysisResults, List<IngListDataItem> ingList, String country, Integer moduleId) {
		
		if (!isAvailableCountry(country)) {
			return Collections.emptyList();
		}
		
		if (!analysisResults.has(RECIPE_ANALAYSIS_REPORT)) {
			return Collections.emptyList();
		}
		
		JSONObject recipeAnalaysisReport = analysisResults.getJSONObject(RECIPE_ANALAYSIS_REPORT);
		
		if (!recipeAnalaysisReport.has(RECIPE_REPORT)) {
			return Collections.emptyList();
		}
		
		JSONArray recipeReport = recipeAnalaysisReport.getJSONArray(RECIPE_REPORT);
		
		List<ReqCtrlListDataItem> requirements = new ArrayList<>();
		
		for (int i = 0; i < recipeReport.length(); i++) {
			JSONObject report = recipeReport.getJSONObject(i);
			if (report.has("country") && report.getString("country").equals(country)) {
				
				JSONArray tabularReports = report.getJSONArray("tabularReport");
				
				for (int j = 0; j < tabularReports.length(); j++) {
					JSONObject tabularReport = tabularReports.getJSONObject(j);
					
					String usage = tabularReport.getString("usage");
					String decernisID = tabularReport.getString("did");
					String function = tabularReport.getString("function");
					
					IngListDataItem ingItem = findIngredientItem(ingList, decernisID, function);
					
					if (ingItem != null) {
						if (tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("prohibited") || tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("over limit")) {
							String threshold = (tabularReport.has("threshold") && !tabularReport.getString("threshold").equals("None")
									? "(" + tabularReport.getString("threshold") + ")"
											: "");
							
							MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, threshold);
							ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage, RequirementType.Forbidden);
							reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
							
							requirements.add(reqCtrlItem);
							if (logger.isDebugEnabled()) {
								logger.debug("Adding prohibited ing :" + tabularReport.getString("did"));
							}
							
						} else if (tabularReport.getString(RESULT_INDICATOR).toLowerCase().startsWith("not listed")) {
							MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
							ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage, RequirementType.Tolerated);
							reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
							requirements.add(reqCtrlItem);
							if (logger.isDebugEnabled()) {
								logger.debug("Adding not listed ing :" + tabularReport.getString("did"));
							}
						} else if (Boolean.TRUE.equals(addInfoReqCtrl())) {
							
							String threshold = (tabularReport.has("threshold") && !tabularReport.getString("threshold").equals("None")
									? tabularReport.getString("threshold")
											: "");
							
							MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PERMITTED_ING, tabularReport.getString(RESULT_INDICATOR),
									threshold);
							ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage, RequirementType.Info);
							
							reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
							requirements.add(reqCtrlItem);
							if (logger.isDebugEnabled()) {
								logger.debug("Adding " + reqMessage.getDefaultValue() + " ing :" + tabularReport.getString("did"));
							}
							
						}
					}
				}
				
			}
		}
		
		return requirements;
	}

	private IngListDataItem findIngredientItem(List<IngListDataItem> ingList, String decernisID, String function) {
		for (IngListDataItem ing : ingList) {
			if (decernisID.equals(nodeService.getProperty(ing.getIng(), PLMModel.PROP_REGULATORY_CODE))) {
				NodeRef ingType = (NodeRef) nodeService.getProperty(ing.getIng(), PLMModel.PROP_ING_TYPE_V2);
				if (function.equalsIgnoreCase((String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_CODE))) {
					return ing;
				}
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
		headers.setBearerAuth(token());

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class, new HashMap<>());
		
		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {
			JSONObject responseBody = new JSONObject(response.getBody());
			
			if (responseBody.has("countries")) {
				JSONArray countryArray = responseBody.getJSONArray("countries");
				for (int i = 0; i < countryArray.length(); i++) {
					countries.add(countryArray.getJSONObject(i).getString("country"));
				}
			}
		}
		
		return countries;
	}

	protected ReqCtrlListDataItem createReqCtrl(NodeRef ing, MLText reqCtrlMessage, RequirementType reqType) {
		ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
		reqCtrlItem.setReqType(reqType);
		reqCtrlItem.setCharact(ing);
		reqCtrlItem.addSource(ing);
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqCtrlMessage);
		reqCtrlItem.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
		return reqCtrlItem;
	}
	
	protected HttpEntity<String> createEntity(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBearerAuth(token());
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(body, headers);
	}

}
