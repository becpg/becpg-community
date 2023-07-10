package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

@Service
public class LegacyAPIDecernisPlugin implements DecernisAnalysisPlugin {

	private Map<Integer, Set<String>> availableCountries = new HashMap<>();
	
	// 1, Food Additives
	// 2, Standards Of Identity
	// 3, Contaminants
	// 5, Food Contact
	// 11; Product Check
	
	private static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	private static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";
	private static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";

	private static final String PARAM_COMPANY = "company";
	private static final String PARAM_MODULE = "module";
	private static final String PARAM_FORMULA = "formula";
	private static final String PARAM_USAGE = "usage";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_RESULTS = "results";
	private static final String PARAM_ANALYSIS_RESULTS = "analysis_results";
	
	@Value("${beCPG.decernis.serverUrl}")
	private String serverUrl;

	@Value("${beCPG.decernis.companyName}")
	private String companyName;

	@Value("${beCPG.decernis.token}")
	private String token;

	@Value("#{new Boolean('${beCPG.formulation.specification.addInfoReqCtrll}'.trim())}")
	private Boolean addInfoReqCtrl;

	@Autowired
	private NodeService nodeService;

	private RestTemplate restTemplate;
	
	private static final Log logger = LogFactory.getLog(LegacyAPIDecernisPlugin.class);
	
	public LegacyAPIDecernisPlugin() {
		if (logger.isTraceEnabled()) {
			restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

			List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
			if (CollectionUtils.isEmpty(interceptors)) {
				interceptors = new ArrayList<>();
			}
			interceptors.add(new DecernisRequestInterceptor());
			restTemplate.setInterceptors(interceptors);
		} else {
			restTemplate = new RestTemplate();
		}
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean needsRecipeId() {
		return true;
	}

	@Override
	public void analyzeRecipe(RegulatoryContext productContext) {
		
		if (productContext.getRegulatoryRecipeId() != null) {
			for (RegulatoryContextItem contextItem : productContext.getContextItems()) {
				if (contextItem.isTreatable()) {
					for (String usage : contextItem.getUsages()) {
						JSONObject analysisResults = recipeAnalysis(productContext.getRegulatoryRecipeId(), contextItem.getCountries(), usage, contextItem.getModuleId());
						if (analysisResults != null) {
							productContext.getRequirements().addAll(extractItemRequirements(contextItem, analysisResults, productContext.getIngRegulatoryMapping()));
							
							if (analysisResults.has("overall_recipe_conclusion") ) {
								String regulatoryResult = analysisResults.getJSONObject("overall_recipe_conclusion").getString("description");
								
								if (contextItem.getItem() instanceof ProductData) {
									((ProductData) contextItem.getItem()).setRegulatoryResult(regulatoryResult);
								} else if (contextItem.getItem() instanceof RegulatoryListDataItem) {
									((RegulatoryListDataItem) contextItem.getItem()).setRegulatoryResult(regulatoryResult);
								}
							}
							
						} else {
							throw new FormulateException("Error analyzing recipe");
						}
					}
				}
			}
		}
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

	private JSONObject recipeAnalysis(String recipeId, Set<String> countries, String usage, Integer moduleId) throws JSONException {

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

		String url = serverUrl + "recipe_analysis?current_company={company}&formula={formula}" + countryParam.toString()
				+ "&usage={usage}&category=null&module_id={module}&limit=1";

		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName);
		params.put(PARAM_FORMULA, recipeId);
		params.put(PARAM_USAGE, usage.trim());
		params.put(PARAM_MODULE, moduleId);

		logger.debug("Get recipe analysis from decernis : " + recipeId + ", usage : " + usage+", countries :"+ countryParam.toString());

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

		String url = serverUrl + "countries/for_module?current_company={company}&module_id={module}";
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName);
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
	
	private HttpEntity<String> createEntity(String param) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(param, headers);
	}
	
	private List<ReqCtrlListDataItem> extractItemRequirements(RegulatoryContextItem itemContext, JSONObject analysisResults, Map<String, List<IngListDataItem>> ings)
			throws JSONException {
		List<ReqCtrlListDataItem> reqCtrlList = new ArrayList<>();

		for (String country : itemContext.getCountries()) {
			if (isAvailableCountry(country, itemContext.getModuleId()) && analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).has(country)) {
				JSONObject countryResults = analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).getJSONObject(country);
				if (countryResults.has("tabular") && countryResults.getJSONObject("tabular").has("INGREDIENT_DATA_PDF")) {
					JSONArray tabularResults = countryResults.getJSONObject("tabular").getJSONArray("INGREDIENT_DATA_PDF");
					for (int row = 0; row < tabularResults.length(); row++) {
						JSONObject result = tabularResults.getJSONObject(row);
						if (result.has("did") && result.has("resultIndicator")) {
							if (ings.containsKey(result.getString("did")) && (ings.get(result.getString("did")) != null)) {
								String usage = (analysisResults.has("search_parameters")
										&& analysisResults.getJSONObject("search_parameters").has(PARAM_USAGE)
												? analysisResults.getJSONObject("search_parameters").getString("usage")
												: "");

								List<IngListDataItem> ingList = ings.get(result.getString("did"));

								IngListDataItem ingItem = null;

								for (IngListDataItem ing : ingList) {
									String ingName = (String) nodeService.getProperty(ing.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME);
									if (result.getString("ingredient").toLowerCase().contains(ingName.toLowerCase())) {
										ingItem = ing;
										break;
									}
								}
								if (ingItem != null) {
									if (result.getString("resultIndicator").toLowerCase().startsWith("prohibited")) {
										String threshold = (result.has("threshold") && !result.getString("threshold").equals("None")
												? "(" + result.getString("threshold") + ")"
												: "");

										MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, threshold);
										ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage, RequirementType.Forbidden);
										reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));

										reqCtrlList.add(reqCtrlItem);
										if (logger.isDebugEnabled()) {
											logger.debug("Adding prohibited ing :" + result.getString("did"));
										}

									} else if (result.getString("resultIndicator").toLowerCase().startsWith("not listed")) {
										MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
										ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage, RequirementType.Tolerated);
										reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
										reqCtrlList.add(reqCtrlItem);
										if (logger.isDebugEnabled()) {
											logger.debug("Adding not listed ing :" + result.getString("did"));
										}
									} else if (Boolean.TRUE.equals(addInfoReqCtrl)) {

										String threshold = (result.has("threshold") && !result.getString("threshold").equals("None")
												? result.getString("threshold")
												: "");

										MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PERMITTED_ING, result.getString("resultIndicator"),
												threshold);
										ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem.getIng(), reqMessage, RequirementType.Info);

										reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
										reqCtrlList.add(reqCtrlItem);
										if (logger.isDebugEnabled()) {
											logger.debug("Adding " + reqMessage.getDefaultValue() + " ing :" + result.getString("did"));
										}

									}
								}
							}
						}
					}
				}
			}

		}
		return reqCtrlList;
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
	
}
