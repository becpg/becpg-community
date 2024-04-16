package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.alfresco.repo.node.MLPropertyInterceptor;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisMode;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.helper.DecernisHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * <p>DecernisServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("decernisService")
public class DecernisServiceImpl implements DecernisService {

	private static final Log logger = LogFactory.getLog(DecernisServiceImpl.class);

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

	@Value("#{new Boolean('${beCPG.formulation.specification.addInfoReqCtrll}'.trim())}")
	private Boolean addInfoReqCtrl;

	private RestTemplate restTemplate;

	/**
	 * <p>Constructor for DecernisServiceImpl.</p>
	 */
	public DecernisServiceImpl() {
		super();
		restTemplate = new RestTemplate();
	}

	// 1, Food Additives
	// 2, Standards Of Identity
	// 3, Contaminants
	// 5, Food Contact
	// 11; Product Check

	private static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	private static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";
	private static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";
	private static final String MESSAGE_NO_RID_ING = "message.decernis.ingredient.noRid";
	private static final String MESSAGE_SEVERAL_RID_ING = "message.decernis.ingredient.severalRid";

	private static final String PARAM_COMPANY = "company";
	private static final String PARAM_MODULE = "module";
	private static final String PARAM_FORMULA = "formula";
	private static final String PARAM_USAGE = "usage";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_RESULTS = "results";
	private static final String PARAM_QUERY = "query";
	private static final String PARAM_ANALYSIS_RESULTS = "analysis_results";

	private static final String MISSING_VALUE = "NA";

	private HttpEntity<String> createEntity(String param) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(param, headers);

	}

	private Set<String> countries = new HashSet<>();

	private JSONObject getAvaillableCountries() throws JSONException {

		logger.debug("Look for decernis available country ");

		String url = serverUrl + "countries/for_module?current_company={company}&module_id={module}";
		Map<String, String> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName);
		params.put(PARAM_MODULE, module);

		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url + " params: " + params);
		}
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

	private boolean isAvaillableCountry(String country) {
		if (countries.isEmpty()) {

			try {
				JSONObject availableCountries = getAvaillableCountries();
				if (availableCountries != null) {
					JSONArray results = availableCountries.getJSONArray(PARAM_RESULTS);
					for (int row = 0; row < results.length(); row++) {
						countries.add(results.getJSONObject(row).getString("country"));

					}
				}

			} catch (RestClientException | JSONException e) {
				countries.clear();
				logger.error(e);
			}

		}

		return countries.contains(country);
	}

	private JSONObject getIngredients(ProductData product, List<ReqCtrlListDataItem> errors) throws JSONException {

		Map<QName, String> ingNumbers = new HashMap<>();
		ingNumbers.put(PLMModel.PROP_CAS_NUMBER, "CAS");
		ingNumbers.put(PLMModel.PROP_EC_NUMBER, "EC No.");
		ingNumbers.put(PLMModel.PROP_CE_NUMBER, "EINECS");
		ingNumbers.put(PLMModel.PROP_FEMA_NUMBER, "FEMA No.");
		ingNumbers.put(PLMModel.PROP_FL_NUMBER, "FL No.");
		ingNumbers.put(PLMModel.PROP_FDA_NUMBER, "FDA Cat.");

		JSONObject ret = new JSONObject();

		String code = (String) nodeService.getProperty(product.getNodeRef(), BeCPGModel.PROP_CODE);
		code += Calendar.getInstance().getTimeInMillis();

		ret.put("spec", code);
		ret.put("name", code + " " + product.getName());
		ret.put(PARAM_COMPANY, companyName);

		JSONArray ingredients = new JSONArray();

		boolean isEmpty = true;

		for (IngListDataItem ingListDataItem : product.getIngList()) {

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
					// Get ingredient regulatory code
					if ((rid == null) || rid.isEmpty()) {
						String url = serverUrl + "ingredients?current_company={company}&q={query}&identifier_type={type}&module_id={module}&limit=25";

						Map<String, String> params = new HashMap<>();
						params.put(PARAM_COMPANY, companyName);
						params.put(PARAM_MODULE, module);
						boolean cond = true;
						Iterator<Map.Entry<QName, String>> iterator = ingNumbers.entrySet().iterator();
						while (iterator.hasNext() && cond) {
							Map.Entry<QName, String> ingNumber = iterator.next();
							String number = (String) nodeService.getProperty(ingListDataItem.getIng(), ingNumber.getKey());
							if ((number != null) && !number.isEmpty() && !number.equals(MISSING_VALUE) && !number.contains(",")) {
								cond = false;
								params.put(PARAM_QUERY, number);
								params.put("type", ingNumber.getValue());
								break;
							}
						}
						if (cond) {
							params.put(PARAM_QUERY, ingName);
							params.put("type", "Name");
						}

						if (params.containsKey(PARAM_QUERY)) {
							if (logger.isDebugEnabled()) {
								logger.debug("Look for ingredients in decernis by " + params.get("type") + ": " + params.get(PARAM_QUERY));
							}

							if (logger.isTraceEnabled()) {
								logger.trace("GET url: " + url + " params: " + params);
							}
							ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params);

							if ((response != null) && HttpStatus.OK.equals(response.getStatusCode()) && (response.getBody() != null)) {

								JSONObject jsonObject = new JSONObject(response.getBody());

								if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) >= 1) && jsonObject.has(PARAM_RESULTS)) {
									JSONArray results = jsonObject.getJSONArray(PARAM_RESULTS);
									JSONObject result = results.getJSONObject(0);
									if (jsonObject.getInt(PARAM_COUNT) > 1) {
										result = getRidByIngName(results, ingName);
									}
									if (result != null) {
										rid = result.get("did").toString();
										if (logger.isDebugEnabled()) {
											logger.debug("RID of ingredient " + params.get(PARAM_QUERY) + ": " + rid);
										}
										nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_REGULATORY_CODE, rid);
										// Get ingredient numbers (CAS, FEMA,
										// CE)
										if (result.has("libidents")) {
											JSONObject libidents = result.getJSONObject("libidents");
											for (Map.Entry<QName, String> ingNumber : ingNumbers.entrySet()) {
												if ((ingNumber.getKey() == PLMModel.PROP_CAS_NUMBER)
														|| (ingNumber.getKey() == PLMModel.PROP_CE_NUMBER)
														|| (ingNumber.getKey() == PLMModel.PROP_FEMA_NUMBER)) {
													String ingNumberToFill = (String) nodeService.getProperty(ingListDataItem.getIng(),
															ingNumber.getKey());
													if (((ingNumberToFill == null) || ingNumberToFill.isEmpty())
															&& libidents.has(ingNumber.getValue())) {
														JSONArray numbers = libidents.getJSONArray(ingNumber.getValue());
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
																logger.debug("Set ingredient RID: " + params.get(PARAM_QUERY) + " "
																		+ ingListDataItem.getIng() + " " + ingNumber.getKey() + " " + number);
															}

															nodeService.setProperty(ingListDataItem.getIng(), ingNumber.getKey(), number);
														}
													}
												}
											}
										}

									} else {
										if (logger.isDebugEnabled()) {
											logger.debug("Several RIDs for ingredient " + params.get(PARAM_QUERY));
										}
										errors.add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_SEVERAL_RID_ING),
												RequirementType.Tolerated));
									}
								} else if (jsonObject.has(PARAM_COUNT) && (jsonObject.getInt(PARAM_COUNT) == 0)) {
									errors.add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_RID_ING),
											RequirementType.Tolerated));
								}
							} else {
								errors.add(createReqCtrl(ingListDataItem.getIng(), MLTextHelper.getI18NMessage(MESSAGE_NO_RID_ING),
										RequirementType.Forbidden));
							}
						}

					} else if (logger.isDebugEnabled()) {
						logger.debug("Existing rid for ingredient (" + ingName + "): " + rid);
					}

					if ((rid != null) && !rid.isEmpty() && !rid.equals(MISSING_VALUE) && ((function != null) && !function.isEmpty())
							&& ((ingName != null) && !ingName.isEmpty()) && (ingQtyPerc != null)) {
						JSONObject ingredient = new JSONObject();
						ingredient.put("name", ingName);
						ingredient.put("percentage", ingQtyPerc);
						ingredient.put("ingredient_did", rid);
						ingredient.put("function", function);
						ingredient.put("spec_parameters", JSONObject.NULL);
						ingredient.put("upper_limit", JSONObject.NULL);
						isEmpty = false;
						ingredients.put(ingredient);
					}

				} catch (HttpClientErrorException | HttpServerErrorException e) {
					logger.warn("Cannot retrieve ingredient " + ingName + " error:" + e.getStatusText());
				} catch (Exception e) {
					logger.error(e, e);
					throw new FormulateException("Unexpected decernis error", e);
				}
			}
		}

		if (!isEmpty) {
			ret.put("ingredients", ingredients);
		}

		return ret;

	}

	private JSONObject getRidByIngName(JSONArray results, String ingName) throws JSONException {
		for (int i = 0; i < results.length(); i++) {
			JSONObject result = results.getJSONObject(i);
			if (result.has("synonyms")) {
				JSONArray synonyms = result.getJSONArray("synonyms");
				int j = 0;
				while (j < synonyms.length()) {
					if (ingName != null && synonyms.getString(j).toLowerCase().replace(",", "").equals(ingName.toLowerCase().replace(",", ""))) {
						return result;
					}
					j++;
				}
			}
		}
		return null;
	}

	private String sendRecipe(JSONObject data) throws JSONException {

		String url = serverUrl + "formulas";
		if (data != null) {
			HttpEntity<String> request = createEntity(data.toString());
			if (logger.isTraceEnabled()) {
				logger.trace("POST url: " + url + " body: " + data);
			}
			JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url, request, String.class));
			if (jsonObject.has("id")) {
				return jsonObject.get("id").toString();
			}

		}
		return null;
	}

	private void deleteRecipe(String recipeId) {
		Map<String, String> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName);
		params.put(PARAM_FORMULA, recipeId);
		params.put(PARAM_MODULE, module);

		String url = serverUrl + "formulas/" + recipeId + "?current_company={company}";
		if (logger.isTraceEnabled()) {
			logger.trace("DELETE url: " + url);
		}
		restTemplate.exchange(url, HttpMethod.DELETE, createEntity(null), String.class,
				params);

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

	private JSONObject recipeAnalysis(String recipeId, Set<String> countries, String usage) throws JSONException {

		StringBuilder countryParam = new StringBuilder("");
		for (String country : countries) {
			if (isAvaillableCountry(country)) {
				countryParam.append("&country=" + country);
			} else {
				logger.warn("No country found for :" + country);
			}
		}

		if (countryParam.toString().isEmpty()) {
			throw new FormulateException("No available country: " + countries.toString() + " over " + this.countries.toString());
		}

		String url = serverUrl + "recipe_analysis?current_company={company}&formula={formula}" + countryParam.toString()
				+ "&usage={usage}&category=null&module_id={module}&limit=1";

		Map<String, String> params = new HashMap<>();
		params.put(PARAM_COMPANY, companyName);
		params.put(PARAM_FORMULA, recipeId);
		params.put(PARAM_USAGE, usage);
		params.put(PARAM_MODULE, module);

		logger.debug("Get recipe analysis from decernis : " + recipeId + ", usage : " + usage);

		if (logger.isTraceEnabled()) {
			logger.trace("POST url: " + url + " params: " + params);
		}
		HttpEntity<String> entity = createEntity(null);
		JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url, entity, String.class, params));
		if (jsonObject.has(PARAM_ANALYSIS_RESULTS) && (jsonObject.getJSONObject(PARAM_ANALYSIS_RESULTS).length() > 0)) {
			return jsonObject;
		}

		return null;
	}

	private List<ReqCtrlListDataItem> createReqCtrl(Set<String> countries, JSONObject analysisResults, List<IngListDataItem> ingList)
			throws JSONException {
		List<ReqCtrlListDataItem> reqCtrlList = new ArrayList<>();

		for (String country : countries) {
			if (isAvaillableCountry(country) && analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).has(country)) {
				JSONObject countryResults = analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).getJSONObject(country);
				if (countryResults.has("tabular") && countryResults.getJSONObject("tabular").has("INGREDIENT_DATA_PDF")) {
					JSONArray tabularResults = countryResults.getJSONObject("tabular").getJSONArray("INGREDIENT_DATA_PDF");
					for (int row = 0; row < tabularResults.length(); row++) {
						JSONObject result = tabularResults.getJSONObject(row);
						if (result.has("did") && result.has("resultIndicator")) {
								String usage = (analysisResults.has("search_parameters")
										&& analysisResults.getJSONObject("search_parameters").has(PARAM_USAGE)
												? analysisResults.getJSONObject("search_parameters").getString("usage")
												: "");

							String decernisID = result.get("did").toString();
							String function = result.getString("function_name");
							String ingredientName = result.getString("ingredient");
							IngListDataItem ingItem = findIngredientItem(ingList, decernisID, function, ingredientName);
							
							if (result.getString("resultIndicator").toLowerCase().startsWith("prohibited")) {
								String threshold = (result.has("threshold") && !result.get("threshold").toString().equals("None")
										? "(" + result.get("threshold").toString() + ")"
												: "");
								
								MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, threshold);
								ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage, RequirementType.Forbidden);
								reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
								
								reqCtrlList.add(reqCtrlItem);
								if (logger.isDebugEnabled()) {
									logger.debug("Adding prohibited ing :" + result.get("did").toString());
								}
								
							} else if (result.getString("resultIndicator").toLowerCase().startsWith("not listed")) {
								MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
								ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage, RequirementType.Tolerated);
								reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
								reqCtrlList.add(reqCtrlItem);
								if (logger.isDebugEnabled()) {
									logger.debug("Adding not listed ing :" + result.get("did").toString());
								}
							} else if (Boolean.TRUE.equals(addInfoReqCtrl)) {
								
								String threshold = (result.has("threshold") && !result.get("threshold").toString().equals("None")
										? result.get("threshold").toString()
												: "");
								
								MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PERMITTED_ING, result.getString("resultIndicator"),
										threshold);
								ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage, RequirementType.Info);
								
								reqCtrlItem.setRegulatoryCode(country + (!usage.isEmpty() ? " - " + usage : ""));
								reqCtrlList.add(reqCtrlItem);
								if (logger.isDebugEnabled()) {
									logger.debug("Adding " + reqMessage.getDefaultValue() + " ing :" + result.get("did").toString());
								}
								
							}
							
						}
					}
				}
			}

		}
		return reqCtrlList;
	}
	
	private IngListDataItem findIngredientItem(List<IngListDataItem> ingList, String decernisID, String function, String ingredientName) {
		for (IngListDataItem ing : ingList) {
			if (ing.getIng() != null && decernisID.equals(nodeService.getProperty(ing.getIng(), PLMModel.PROP_REGULATORY_CODE))) {
				NodeRef ingType = (NodeRef) nodeService.getProperty(ing.getIng(), PLMModel.PROP_ING_TYPE_V2);
				if (ingType != null && function.equalsIgnoreCase((String) nodeService.getProperty(ingType, BeCPGModel.PROP_LV_CODE))) {
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
				if (charactName != null && ingredientName.equalsIgnoreCase(charactName.getDefaultValue())) {
					return ing;
				}
				if (charactName != null && ingredientName.equalsIgnoreCase(charactName.getValue(Locale.ENGLISH))) {
					return ing;
				}
				MLText legalName = (MLText) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_LEGAL_NAME);
				if (legalName != null && ingredientName.equalsIgnoreCase(legalName.getDefaultValue())) {
					return ing;
				}
				if (legalName != null && ingredientName.equalsIgnoreCase(legalName.getValue(Locale.ENGLISH))) {
					return ing;
				}
			}
		} finally {
			MLPropertyInterceptor.setMLAware(mlAware);
		}
		return null;
	}

	private ReqCtrlListDataItem createReqCtrl(NodeRef ing, MLText reqCtrlMessage, RequirementType reqType) {
		ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
		reqCtrlItem.setReqType(reqType);
		reqCtrlItem.setCharact(ing);
		reqCtrlItem.getSources().add(ing);
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqCtrlMessage);
		reqCtrlItem.setFormulationChainId(DECERNIS_CHAIN_ID);
		return reqCtrlItem;

	}

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> extractDecernisRequirements(ProductData product, @Nonnull Set<String> countries, @Nonnull Set<String> usages) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();
		try {

			if (!usages.isEmpty() && !countries.isEmpty()) {
				JSONObject data = getIngredients(product, ret);
				if ((data != null) && data.has("ingredients")) {
					String recipeId = sendRecipe(data);
					if (recipeId != null) {
						product.setRegulatoryRecipeId(recipeId);
						if (!DecernisMode.DECERNIS_ONLY.equals(product.getRegulatoryMode())) {
							try {
								for (String usage : usages) {
									JSONObject analysisResults = recipeAnalysis(recipeId, countries, usage.trim());
									if (analysisResults != null) {
										ret.addAll(createReqCtrl(countries, analysisResults, product.getIngList()));
									} else {
										throw new FormulateException("Error analysing recipe");
									}
								}
							} finally {
								if (!DecernisMode.BOTH.equals(product.getRegulatoryMode())) {
									logger.debug("Deleting: " + recipeId);
									deleteRecipe(recipeId);
								}
							}
						}
					} else {
						throw new FormulateException("Error sending recipe");
					}

				} else {
					logger.debug("No ingredients found in recipe");
				}

			} else {
				throw new IllegalStateException("countries or usage cannot be null");
			}
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			logger.error("Decernis HTTP ERROR STATUS:" + e.getStatusText());
			logger.error("- error body:" + e.getResponseBodyAsString());
			StringBuilder message = new StringBuilder();
			if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
				try {
					JSONObject eObject = new JSONObject(e.getResponseBodyAsString());
					if (eObject.has("country") && (eObject.getJSONArray("country") != null)) {
						message.append("\n Country: " + eObject.getJSONArray("country").getString(0));
					}
					if (eObject.has("usage") && (eObject.getJSONArray("usage") != null)) {
						message.append("\n Usage: " + eObject.getJSONArray("usage").getString(0));
					}
				} catch (JSONException e1) {
					logger.error(e1, e1);
				}
			}
			throw new FormulateException("Error calling decernis service: " + e.getLocalizedMessage() + message, e);
		} catch (Exception e) {
			throw new FormulateException("Unexpected decernis error", e);
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public String createDecernisChecksum(Set<String> countries, Set<String> usages) {
		StringBuilder key = new StringBuilder();

		if (countries != null) {
			countries.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
		}

		if (usages != null) {
			usages.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
		}

		return key.toString();
	}
}
