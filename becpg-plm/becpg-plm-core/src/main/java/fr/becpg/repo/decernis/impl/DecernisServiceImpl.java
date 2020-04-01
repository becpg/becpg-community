package fr.becpg.repo.decernis.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

@Service("decernisService")
public class DecernisServiceImpl implements DecernisService {

	private static final Log logger = LogFactory.getLog(DecernisServiceImpl.class);

	private Map<String, NodeRef> ings = new HashMap<>();

	@Autowired
	private NodeService nodeService;

	@Value("${beCPG.decernis.serverUrl}")
	private String serverUrl;

	@Value("${beCPG.decernis.companyName}")
	private String companyName;

	@Value("${beCPG.decernis.token}")
	private String token;

	@Value("${beCPG.decernis.module}")
	private String module;

	private RestTemplate restTemplate = new RestTemplate();

	// 1, Food Additives
	// 2, Standards Of Identity
	// 3, Contaminants	
	// 5, Food Contact
	// 11; Product Check

	private static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	private static final String MESSAGE_NO_RID_ING = "message.decernis.ingredient.noRid";
	private static final String MESSAGE_SEVERAL_RID_ING = "message.decernis.ingredient.severalRid";

	private static final String COMPANY = "company";
	private static final String MODULE = "module";
	private static final String FORMULA = "formula";
	private static final String USAGE = "usage";

	private static final String MISSING_VALUE = "NA";
	HttpEntity<String> createEntity(String param) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(param, headers);

	}

	private JSONObject getAvaillableCountries() throws RestClientException, JSONException {
		String url = serverUrl + "countries/for_module?current_company={company}&module_id={module}";
		Map<String, String> params = new HashMap<>();
		params.put(COMPANY, companyName);
		params.put(MODULE, module);
		JSONObject jsonObject = new JSONObject(restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params).getBody());
		if (jsonObject.has("count") && (jsonObject.getInt("count") > 0) && jsonObject.has("results")) {
			return jsonObject;
		}

		return null;
	}

	private boolean isAvaillableCountry(String country) throws RestClientException, JSONException {
		JSONObject availableCountries = getAvaillableCountries();
		if (availableCountries != null) {
			JSONArray results = availableCountries.getJSONArray("results");
			for (int row = 0; row < results.length(); row++) {
				if (country.equals(results.getJSONObject(row).getString("country"))) {
					return true;
				}
			}
		}
		return false;
	}

	private JSONObject getIngredients(ProductData product) throws InvalidNodeRefException, JSONException {

		Map<QName,String> ingNumbers = new HashMap<>();
		ingNumbers.put(PLMModel.PROP_CAS_NUMBER, "CAS");
		ingNumbers.put(PLMModel.PROP_EC_NUMBER, "EC No.");
		ingNumbers.put(PLMModel.PROP_CE_NUMBER, "EINECS");
		ingNumbers.put(PLMModel.PROP_FEMA_NUMBER, "FEMA No.");
		ingNumbers.put(PLMModel.PROP_FL_NUMBER, "FL No.");
		ingNumbers.put(PLMModel.PROP_FDA_NUMBER, "FDA Cat.");

		JSONObject ret = new JSONObject();
		ret.put("spec", nodeService.getProperty(product.getNodeRef(), BeCPGModel.PROP_CODE));
		ret.put("name", product.getName());
		ret.put(COMPANY, companyName);

		JSONArray ingredients = new JSONArray();

		for (IngListDataItem ingListDataItem : product.getIngList()) {
			boolean cond = true;
			if (ingListDataItem.getIng() != null) {
				Serializable ingName = (nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME) != null && !nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME).equals("") ? 
						nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_LEGAL_NAME) : nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
				Serializable rid = nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE);
				Serializable ingType = nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_TYPE_V2);
				Serializable ingQtyPerc = nodeService.getProperty(ingListDataItem.getNodeRef(), PLMModel.PROP_INGLIST_QTY_PERC);
				Serializable function = null;
				if (ingType != null) {
					function = nodeService.getProperty((NodeRef) ingType, PLMModel.PROP_REGULATORY_CODE);
				}

				// Get ingredient regulatory code
				if ((rid == null) || rid.equals("")) {
					String url = serverUrl + "ingredients?current_company={company}&q={query}&identifier_type={type}&module_id={module}&limit=1";
					Map<String, String> params = new HashMap<>();
					params.put(COMPANY, companyName);
					params.put(MODULE, module);

					Iterator<Map.Entry<QName, String>> iterator = ingNumbers.entrySet().iterator();
					while (iterator.hasNext() && cond) {
						Map.Entry<QName, String> ingNumber = iterator.next();
						Serializable number = nodeService.getProperty(ingListDataItem.getIng(), ingNumber.getKey());
						if (number != null && !number.equals("") && !number.equals(MISSING_VALUE)) {
							cond = false;
							params.put("query", number.toString());
							params.put("type", ingNumber.getValue());
						}
					}
					if (cond && (ingName != null) && !ingName.equals("")) {
						params.put("query", ingName.toString());
						params.put("type", "Name");
					}

					if (params.containsKey("query")) {
						JSONObject jsonObject = new JSONObject(
								restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params).getBody());
						if (jsonObject.has("count") && (jsonObject.getInt("count") == 1) && jsonObject.has("results")) {
							JSONArray results = jsonObject.getJSONArray("results");
							rid = results.getJSONObject(0).getString("did");
							nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE, rid);
						} else if (jsonObject.has("count") && (jsonObject.getInt("count") == 0)) {
							createReqCtrl(product, ingListDataItem.getIng(), MESSAGE_NO_RID_ING);
						} else if (jsonObject.has("count") && (jsonObject.getInt("count") > 1)) {
							createReqCtrl(product, ingListDataItem.getIng(), MESSAGE_SEVERAL_RID_ING);
						}
					}
				}

				if ((rid != null) && !rid.equals("") && !rid.equals(MISSING_VALUE) && ((function != null) && !function.equals("")) && ((ingName != null) && !ingName.equals(""))
						&& ((ingQtyPerc != null) && !ingQtyPerc.equals(""))) {
					ings.put(rid.toString(), ingListDataItem.getIng());
					JSONObject ingredient = new JSONObject();
					ingredient.put("name", ingName);
					ingredient.put("percentage", ingQtyPerc);
					ingredient.put("ingredient_did", rid);
					ingredient.put("function", function);
					ingredient.put("spec_parameters", JSONObject.NULL);
					ingredient.put("upper_limit", JSONObject.NULL);
					ingredients.put(ingredient);
				}
			}
		}
		if (ingredients.length() > 0) {
			ret.put("ingredients", ingredients);
			return ret;
		}
		return null;
	}

	private String sendRecipe(JSONObject data) throws JSONException {
		String url = serverUrl + "formulas";
		if (data != null) {
			try {
				HttpEntity<String> request = createEntity(data.toString());
				JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url, request, String.class));
				if (jsonObject.has("id")) {
					return jsonObject.getString("id");
				}
			} catch (HttpClientErrorException e) {
				logger.error("Error sending recipe: " + e.getResponseBodyAsString());
			}
		}
		return null;
	}

	private void deleteRecipe(String recipeId, String usage) {
		Map<String, String> params = new HashMap<>();
		params.put(COMPANY, companyName);
		params.put(FORMULA, recipeId);
		params.put(USAGE, usage);
		params.put(MODULE, module);

		restTemplate.exchange(serverUrl + "formulas/" + recipeId + "?current_company={company}", HttpMethod.DELETE, createEntity(null), String.class,
				params);

	}

	/**
	 * {"search_parameters": { "usage": "Breakfast foods", "country": [ "United
	 * States", "European Union" ], "recipe_name": "Berry Fruit Filling" },
	 * "analysis_results": { "European Union": { u"result_indicator":
	 * "PERMITTED", u"xml": "RAW_XML_RESPONSE", u"matrix": { '1034': {'did':
	 * '1034', 'name': 'Color Yellow 5', 'result_indicator': 'REVIEW - NOT
	 * LISTED'}, }, u"tabular": { 'SCOPE DETAIL': [{ 'country': 'United States -
	 * United States', 'ingredient': 'Carrageenan', 'function': 'Gelling,
	 * Thickening Agents', 'usage': 'Breakfast foods - Foods',
	 * 'resultIndicator': 'PERMITTED', 'threshold': '-- No Threshold',
	 * 'ingredientPercent': '1.9', 'citation': '21 CFR 172.620 Carrageenan',
	 * 'comments': 'labeling restriction', 'expressedAs': '--', 'citationLink':
	 * 'doc=21cfr172.620.pdf&pg=1' }] }, }, }}
	 * @throws JSONException 
	 * @throws RestClientException 
	 */

	private JSONObject recipeAnalysis(String recipeId, List<String> countries, String usage) throws RestClientException, JSONException {

		StringBuilder bld = new StringBuilder("");
		for (String country : countries) {
			bld.append("&country=" + country);
		}
		String countryParam = bld.toString();

		String url = serverUrl + "recipe_analysis?current_company={company}&formula={formula}" + countryParam
				+ "&usage={usage}&category=null&module_id={module}&limit=1";

		Map<String, String> params = new HashMap<>();
		params.put(COMPANY, companyName);
		params.put(FORMULA, recipeId);
		params.put(USAGE, usage);
		params.put(MODULE, module);

		try {
			HttpEntity<String> entity = createEntity(null);
			JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url, entity, String.class, params));
			if (jsonObject.has("analysis_results") && (jsonObject.getJSONObject("analysis_results").length() > 0)) {
				return jsonObject;
			}
		} catch (HttpClientErrorException e) {
			logger.error("Error analysing recipe: " + e.getResponseBodyAsString());
		}
		return null;
	}

	private String createReqCtrl(ProductData product, List<String> countries, JSONObject analysisResults) throws JSONException {
		List<ReqCtrlListDataItem> reqCtrlList = new ArrayList<>();
		StringBuilder bld = new StringBuilder("");
		for (String country : countries) {
			int count = 0;

			if (analysisResults.getJSONObject("analysis_results").has(country)) {
				JSONObject countryResults = analysisResults.getJSONObject("analysis_results").getJSONObject(country);
				if (countryResults.has("tabular") && countryResults.getJSONObject("tabular").has("INGREDIENT_DATA_PDF")) {
					JSONArray tabularResults = countryResults.getJSONObject("tabular").getJSONArray("INGREDIENT_DATA_PDF");
					for (int row = 0; row < tabularResults.length(); row++) {
						JSONObject result = tabularResults.getJSONObject(row);
						if (result.has("did") && result.has("resultIndicator") && result.getString("resultIndicator").toLowerCase().startsWith("prohibited")) {
							if (ings.containsKey(result.getString("did")) && (ings.get(result.getString("did")) != null)) {
								ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
								reqCtrlItem.setReqType(RequirementType.Forbidden);
								reqCtrlItem.setReqDataType(RequirementDataType.Specification);
								reqCtrlItem.getSources().add(ings.get(result.getString("did")));
								String threshold = (result.has("threshold") && !result.getString("threshold").equals("None")
										? "(" + result.getString("threshold") + ")"
												: "");
								String usage = (analysisResults.has("search_parameters")
										&& analysisResults.getJSONObject("search_parameters").has("usage")
										? analysisResults.getJSONObject("search_parameters").getString("usage") + " - "
												: "");
								MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, country, usage, threshold);
								reqCtrlItem.setReqMlMessage(reqMessage);
								reqCtrlList.add(reqCtrlItem);
								count++;
							}
						}
					}
				}
			}
			bld.append(country + ":" + count + "\n");
		}
		if (product.getReqCtrlList() == null) {
			product.setReqCtrlList(new LinkedList<>());
		}
		if (!reqCtrlList.isEmpty()) {
			product.getReqCtrlList().addAll(reqCtrlList);
		}

		return bld.toString();
	}

	private void createReqCtrl(ProductData product, NodeRef ing, String reqCtrlMessage) {
		MLText reqMessage = MLTextHelper.getI18NMessage(reqCtrlMessage);
		ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
		reqCtrlItem.setReqType(RequirementType.Info);
		reqCtrlItem.getSources().add(ing);
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqMessage);
		if (product.getReqCtrlList() == null) {
			product.setReqCtrlList(new LinkedList<>());
		}
		product.getReqCtrlList().add(reqCtrlItem);
	}

	@Override
	public String launchDecernisAnalysis(ProductData product, List<String> countries, List<String> usages) throws RestClientException, JSONException {
		if (!usages.isEmpty()) {
			while (countries.removeAll(Arrays.asList("", null)));
			for (String country : countries) {
				if (!isAvaillableCountry(country)) {
					countries.remove(country);
				}
			}
			JSONObject data = getIngredients(product);
			if ((data != null) && (!countries.isEmpty())) {
				for (String usage : usages) {
					String recipeId = sendRecipe(data);
					if (recipeId != null) {
						try {
							JSONObject analysisResults = recipeAnalysis(recipeId, countries, usage.trim());
							if (analysisResults != null) {
								return createReqCtrl(product, countries, analysisResults);
							} 
							return "Error analysing recipe";
						} finally {
							deleteRecipe(recipeId, usage.trim());
						}
					} 
					return "Error sending recipe";
				}
			} else if (countries.isEmpty()) {
				return "No available country";
			} 
			return "No ingredient found";
		} 
		return "No usage found";
	}

	//	public String startQueryJob() {
	//		String.format(serverUrl + "landscape?current_company=%s", companyName);
	//
	//		// --data-binary '{"module_id":1,"country":["China","European
	//		// Union","United States","Saudi
	//		// Arabia"],"usage":[1113],"did":[103329,3608,6112]}’
	//
	//		return null;
	//
	//	}
	//
	//	public String getMatrixData(String id) {
	//		String.format(serverUrl + "landscape/%s/data_matrix?current_company=%s", id, companyName);
	//
	//		return null;
	//
	//	}
	//
	//	public String getTabularData(String id) {
	//		String.format(serverUrl + "landscape/%s/data_tabular?current_company=%s", id, companyName);
	//
	//		return null;
	//
	//	}

}
