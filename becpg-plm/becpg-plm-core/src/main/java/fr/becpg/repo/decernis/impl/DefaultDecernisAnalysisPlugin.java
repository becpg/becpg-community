package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.MLPropertyInterceptor;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
public class DefaultDecernisAnalysisPlugin implements DecernisAnalysisPlugin {

	private Map<Integer, Set<String>> availableCountries = new HashMap<>();
	
	// 1, Food Additives
	// 2, Standards Of Identity
	// 3, Contaminants
	// 5, Food Contact
	// 11; Product Check

	private static final String PARAM_COMPANY = "company";
	private static final String PARAM_MODULE = "module";
	private static final String PARAM_FORMULA = "formula";
	private static final String PARAM_USAGE = "usage";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_RESULTS = "results";
	private static final String PARAM_ANALYSIS_RESULTS = "analysis_results";
	private static final String INGREDIENT_DATA_PDF = "INGREDIENT_DATA_PDF";
	private static final String RESULT_INDICATOR = "resultIndicator";
	private static final String SEARCH_PARAMETERS = "search_parameters";
	private static final String TABULAR = "tabular";
	private static final String THRESHOLD = "threshold";
	
	private static final Log logger = LogFactory.getLog(DefaultDecernisAnalysisPlugin.class);
	
	protected final NodeService nodeService;
	
	private final SystemConfigurationService systemConfigurationService;
	
	private final RestTemplate restTemplate;
	
	public DefaultDecernisAnalysisPlugin(NodeService nodeService, SystemConfigurationService systemConfigurationService, @Qualifier("logRestTemplate") RestTemplate restTemplate) {
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

	@Override
	public boolean isEnabled() {
		return (analysisUrl() == null || analysisUrl().isBlank() || analysisUrl().equals(serverUrl()));
	}
	
	@Override
	public boolean needsRecipeId() {
		return true;
	}
	
	/**
	 * {"search_parameters": { "usage": "Breakfast foods", "country": [ "United
	 * States", "European Union" ], "recipe_name": "Berry Fruit Filling" },
	 * PARAM_ANALYSIS_RESULTS: { "European Union": { u"result_indicator":
	 * "PERMITTED", u"xml": "RAW_XML_RESPONSE", u"matrix": { '1034': {'did':
	 * '1034', 'name': 'Color Yellow 5', 'result_indicator': 'REVIEW - NOT
	 * LISTED'}, }, u"tabular": { 'SCOPE DETAIL': [{ 'country': 'United States -
	 * United States', 'ingredient': 'Carrageenan', 'function': 'Gelling,
	 * Thickening Agents', 'usage': 'Breakfast foods - Foods',
	 * 'resultIndicator': 'PERMITTED', 'threshold': '-- No Threshold',
	 * 'ingredientPercent': '1.9', 'citation': '21 CFR 172.620 Carrageenan',
	 * 'comments': 'labeling restriction', 'expressedAs': '--', 'citationLink':
	 * 'doc=21cfr172.620.pdf&pg=1' }] }, }, }}
	 *
	 * @throws JSONException
	 * @throws RestClientException
	 */

	@Override
	public JSONObject postRecipeAnalysis(RegulatoryContext productContext, Set<String> countries, String usage, Integer moduleId) throws JSONException {

		StringBuilder countryParam = new StringBuilder("");
		for (String country : countries) {
			if (isAvailableCountry(country, moduleId)) {
				countryParam.append("&country=" + country);
			} else {
				logger.warn("No country found for :" + country);
			}
		}

		if (countryParam.toString().isEmpty()) {
			throw new FormulateException("No available country: " + countries.toString() + " over " + this.availableCountries.toString());
		}

		String url = serverUrl() + "/recipe_analysis?current_company={company}&formula={formula}" + countryParam.toString()
				+ "&usage={usage}&category=null&module_id={module}&limit=1";

		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName());
		params.put(PARAM_FORMULA, productContext.getRegulatoryRecipeId());
		params.put(PARAM_USAGE, usage.trim());
		params.put(PARAM_MODULE, moduleId);

		logger.debug("Get recipe analysis from decernis : " + productContext.getRegulatoryRecipeId() + ", usage : " + usage+", countries :"+ countryParam.toString());

		HttpEntity<String> entity = createEntity(null);
		JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url, entity, String.class, params));
		if (jsonObject.has(PARAM_ANALYSIS_RESULTS) && (jsonObject.getJSONObject(PARAM_ANALYSIS_RESULTS).length() > 0)) {
			return jsonObject;
		}

		return null;
	}
	
	private boolean isAvailableCountry(String country, Integer moduleId) {
		
		if (!availableCountries.containsKey(moduleId)) {
			
			Set<String> moduleCountries = new HashSet<>();
			
			try {
				JSONObject response = fetchAvailableCountries(moduleId);
				if (response != null) {
					JSONArray results = response.getJSONArray(PARAM_RESULTS);
					for (int row = 0; row < results.length(); row++) {
						moduleCountries.add(results.getJSONObject(row).getString("country"));
					}
				}
				availableCountries.put(moduleId, moduleCountries);
			} catch (RestClientException | JSONException e) {
				availableCountries.clear();
				logger.error(e);
			}
		}
		
		return availableCountries.get(moduleId).contains(country);
	}
	
	private JSONObject fetchAvailableCountries(Integer moduleId) throws JSONException {

		logger.debug("Look for decernis available country ");

		String url = serverUrl() + "/countries/for_module?current_company={company}&module_id={module}";
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName());
		params.put(PARAM_MODULE, moduleId);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params);

		if (HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {

			JSONObject jsonObject = new JSONObject(response.getBody());
			if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) > 0) && jsonObject.has(PARAM_RESULTS)) {
				return jsonObject;
			}

		} else {
			logger.warn("No response body for :" + url);
		}

		return null;
	}
	
	@Override
	public String extractAnalysisResult(JSONObject analysisResults) {
		if (analysisResults.has("overall_recipe_conclusion") ) {
			return analysisResults.getJSONObject("overall_recipe_conclusion").getString("description");
		}
		return null;
	}

	@Override
	public List<ReqCtrlListDataItem> extractRequirements(JSONObject analysisResults, List<IngListDataItem> ingList, String country, Integer moduleId) {
		
		if (!isAvailableCountry(country, moduleId)) {
			return Collections.emptyList();
		}
		
		if (!analysisResults.has(PARAM_ANALYSIS_RESULTS) || !analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).has(country)) {
			return Collections.emptyList();
		}
		
		JSONObject countryResults = analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).getJSONObject(country);
		
		if (!countryResults.has(TABULAR) || !countryResults.getJSONObject(TABULAR).has(INGREDIENT_DATA_PDF)) {
			return Collections.emptyList();
		}
		
		List<ReqCtrlListDataItem> reqCtrlList = new ArrayList<>();
		JSONArray tabularResults = countryResults.getJSONObject(TABULAR).getJSONArray(INGREDIENT_DATA_PDF);
		for (int row = 0; row < tabularResults.length(); row++) {
			JSONObject result = tabularResults.getJSONObject(row);
			if (result.has("did") && result.has(RESULT_INDICATOR)) {
				
				String decernisID = result.getString("did");
				String function = result.getString("function_name");
				String ingredientName = result.getString("ingredient");
				IngListDataItem ingItem = findIngredientItem(ingList, decernisID, function, ingredientName);
				
				String usage = (analysisResults.has(SEARCH_PARAMETERS) && analysisResults.getJSONObject(SEARCH_PARAMETERS).has(PARAM_USAGE)
						? analysisResults.getJSONObject(SEARCH_PARAMETERS).getString("usage")
						: "");
					
				String regulatoryCode = country + (!usage.isEmpty() ? " - " + usage : "");
				
				if (result.getString(RESULT_INDICATOR).toLowerCase().startsWith("prohibited")) {
					String threshold = (result.has(THRESHOLD) && !result.getString(THRESHOLD).equals("None")
							? "(" + result.getString(THRESHOLD) + ")"
									: "");
					
					MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, threshold);
					
					ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage, RequirementType.Forbidden);
					reqCtrlItem.setRegulatoryCode(regulatoryCode);
					
					reqCtrlList.add(reqCtrlItem);
					if (logger.isDebugEnabled()) {
						logger.debug("Adding prohibited ing :" + result.getString("did"));
					}
					
				} else if (result.getString(RESULT_INDICATOR).toLowerCase().startsWith("not listed")) {
					MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
					ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage, RequirementType.Tolerated);
					reqCtrlItem.setRegulatoryCode(regulatoryCode);
					reqCtrlList.add(reqCtrlItem);
					if (logger.isDebugEnabled()) {
						logger.debug("Adding not listed ing :" + result.getString("did"));
					}
				} else if (Boolean.TRUE.equals(Boolean.parseBoolean(addInfoReqCtrl()))) {
					
					String threshold = (result.has(THRESHOLD) && !result.getString(THRESHOLD).equals("None")
							? result.getString(THRESHOLD)
									: "");
					
					MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PERMITTED_ING, result.getString(RESULT_INDICATOR),
							threshold);
					ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage, RequirementType.Info);
					
					reqCtrlItem.setRegulatoryCode(regulatoryCode);
					reqCtrlList.add(reqCtrlItem);
					if (logger.isDebugEnabled()) {
						logger.debug("Adding " + reqMessage.getDefaultValue() + " ing :" + result.getString("did"));
					}
					
				}
			}
		}
		return reqCtrlList;
	}
	
	private IngListDataItem findIngredientItem(List<IngListDataItem> ingList, String decernisID, String function, String ingredientName) {
		for (IngListDataItem ing : ingList) {
			if (decernisID.equals(nodeService.getProperty(ing.getIng(), PLMModel.PROP_REGULATORY_CODE))) {
				NodeRef ingType = (NodeRef) nodeService.getProperty(ing.getIng(), PLMModel.PROP_ING_TYPE_V2);
				if (function.equalsIgnoreCase((String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_CODE))) {
					return ing;
				}
			}
		}
		
		if (ingredientName.split("\\|").length > 1) {
			ingredientName = ingredientName.split("\\|")[1];
		}
		
		boolean mlAware = MLPropertyInterceptor.setMLAware(true);
		try {
			for (IngListDataItem ing : ingList) {
				MLText charactName = (MLText) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_CHARACT_NAME);
				if (ingredientName.equalsIgnoreCase(charactName.getDefaultValue())) {
					return ing;
				}
				if (ingredientName.equalsIgnoreCase(charactName.getValue(Locale.ENGLISH))) {
					return ing;
				}
				MLText legalName = (MLText) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_LEGAL_NAME);
				if (ingredientName.equalsIgnoreCase(legalName.getDefaultValue())) {
					return ing;
				}
				if (ingredientName.equalsIgnoreCase(legalName.getValue(Locale.ENGLISH))) {
					return ing;
				}
			}
		} finally {
			MLPropertyInterceptor.setMLAware(mlAware);
		}
		return null;
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
