package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.helper.DecernisHelper;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;
import fr.becpg.repo.decernis.model.UsageContext;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>DefaultDecernisAnalysisPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DefaultDecernisAnalysisPlugin implements DecernisAnalysisPlugin {

	private Map<Integer, Set<String>> availableCountries = new HashMap<>();

	// 1, Food Additives
	// 2, Standards Of Identity
	// 3, Contaminants
	// 5, Food Contact
	// 11; Product Check

	private static final int DECERNIS_MAX_COUNTRIES = 20;

	private static final String PARAM_COMPANY = "company";
	private static final String PARAM_MODULE = "module";
	private static final String PARAM_FORMULA = "formula";
	private static final String PARAM_USAGE = "usage";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_RESULTS = "results";
	private static final String PARAM_ANALYSIS_RESULTS = "analysis_results";
	private static final String INGREDIENT_DATA_PDF = "INGREDIENT_DATA_PDF";
	/** Constant <code>RESULT_INDICATOR="resultIndicator"</code> */
	protected static final String RESULT_INDICATOR = "resultIndicator";
	/** Constant <code>CITATION="citation"</code> */
	protected static final String CITATION = "citation";
	/** Constant <code>USAGE_NAME="usage_name"</code> */
	protected static final String USAGE_NAME = "usage_name";
	private static final String SEARCH_PARAMETERS = "search_parameters";
	private static final String TABULAR = "tabular";
	/** Constant <code>THRESHOLD="threshold"</code> */
	protected static final String THRESHOLD = "threshold";

	private static final Log logger = LogFactory.getLog(DefaultDecernisAnalysisPlugin.class);

	protected final NodeService nodeService;

	protected final SystemConfigurationService systemConfigurationService;

	/**
	 * <p>Constructor for DefaultDecernisAnalysisPlugin.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public DefaultDecernisAnalysisPlugin(@Qualifier("nodeService") NodeService nodeService, SystemConfigurationService systemConfigurationService) {
		super();
		this.nodeService = nodeService;
		this.systemConfigurationService = systemConfigurationService;
	}

	/**
	 * <p>serverUrl.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String serverUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.serverUrl");
	}

	/**
	 * <p>analysisUrl.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String analysisUrl() {
		return systemConfigurationService.confValue("beCPG.decernis.analysisUrl");
	}

	/**
	 * <p>companyName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String companyName() {
		return systemConfigurationService.confValue("beCPG.decernis.companyName");
	}

	/**
	 * <p>addInfoReqCtrl.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean addInfoReqCtrl() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.specification.addInfoReqCtrl"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEnabled() {
		return (analysisUrl() == null || analysisUrl().isBlank() || analysisUrl().equals(serverUrl()));
	}

	/** {@inheritDoc} */
	@Override
	public boolean needsRecipeId() {
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public void ingredientAnalysis(RegulatoryContext productContext, RegulatoryContextItem contextItem) {
		// implemented in extractRequirements()
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

	private JSONObject postRecipeAnalysis(RegulatoryContext productContext, List<String> countries, String usage, Integer moduleId)
			throws JSONException {

		if (productContext.getRegulatoryRecipeId() == null) {
			logger.error("Recipe ID is null");
			return null;
		}

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

		logger.debug("Get recipe analysis from decernis : " + productContext.getRegulatoryRecipeId() + ", usage : " + usage + ", countries :"
				+ countryParam.toString());

		HttpEntity<String> entity = createEntity(null);
		if (logger.isTraceEnabled()) {
			logger.trace("POST url: " + url + " params: " + params);
		}
		JSONObject jsonObject = new JSONObject(RestTemplateHelper.getRestTemplate().postForObject(url, entity, String.class, params));
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

		if (logger.isTraceEnabled()) {
			logger.trace("GET url: " + url + " params: " + params);
		}
		ResponseEntity<String> response = RestTemplateHelper.getRestTemplate().exchange(url, HttpMethod.GET, createEntity(null), String.class, params);

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

	/** {@inheritDoc} */
	@Override
	public void extractRequirements(RegulatoryContext productContext, RegulatoryContextItem contextItem) {

		for (UsageContext usageContext : contextItem.getUsages()) {

			List<List<String>> countriesBatch = Lists.partition(new ArrayList<>(contextItem.getCountries().keySet()), DECERNIS_MAX_COUNTRIES);

			for (List<String> countries : countriesBatch) {

				JSONObject analysisResults = null;

				try {
					analysisResults = postRecipeAnalysis(productContext, countries, usageContext.getName(), usageContext.getModuleId());
				} catch (HttpStatusCodeException e) {
					logger.error("Error during Decernis analysis: " + DecernisHelper.cleanError(e.getMessage()), e);
					for (String country : countries) {
						ReqCtrlListDataItem req = ReqCtrlListDataItem.forbidden().withMessage(
								MLTextHelper.getI18NMessage("message.decernis.error", "Error while creating Decernis recipe: " + DecernisHelper.cleanError(e.getMessage())))
								.ofDataType(RequirementDataType.Formulation);

						req.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
						req.setRegulatoryCode(country + (!usageContext.getName().isEmpty() ? " - " + usageContext.getName() : ""));
						productContext.getRequirements().add(req);
					}
				}
				if (analysisResults != null) {
					for (String country : countries) {

						if (isAvailableCountry(country, usageContext.getModuleId()) && analysisResults.has(PARAM_ANALYSIS_RESULTS)
								&& analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).has(country)

						) {

							JSONObject countryResults = analysisResults.getJSONObject(PARAM_ANALYSIS_RESULTS).getJSONObject(country);

							if (logger.isTraceEnabled()) {
								logger.trace(countryResults.toString(3));
							}

							if (countryResults.has(TABULAR) && countryResults.getJSONObject(TABULAR).has(INGREDIENT_DATA_PDF)) {

								JSONArray tabularResults = countryResults.getJSONObject(TABULAR).getJSONArray(INGREDIENT_DATA_PDF);
								for (int row = 0; row < tabularResults.length(); row++) {
									JSONObject result = tabularResults.getJSONObject(row);
									if (result.has("did") && result.has(RESULT_INDICATOR)) {

										String decernisID = result.get("did").toString();
										String function = result.getString("function_name");
										String ingredientName = result.getString("ingredient");
										IngListDataItem ingItem = findIngredientItem(productContext.getProduct().getIngList(), decernisID, function,
												ingredientName);

										String usage = (analysisResults.has(SEARCH_PARAMETERS)
												&& analysisResults.getJSONObject(SEARCH_PARAMETERS).has(PARAM_USAGE)
														? analysisResults.getJSONObject(SEARCH_PARAMETERS).getString(PARAM_USAGE)
														: "");

										String regulatoryCode = country + (!usage.isEmpty() ? " - " + usage : "");

										if (contextItem.getCountries().get(country) != null && usageContext.getNodeRef() != null) {
											IngRegulatoryListDataItem ingRegulatoryListDataItem = createIngRegulatoryListDataItem(ingItem.getIng(),
													contextItem.getCountries().get(country));
											
											ingRegulatoryListDataItem.setCitation(new MLText(result.getString(CITATION)));
											ingRegulatoryListDataItem.setUsages(new MLText(result.getString(USAGE_NAME)));
											ingRegulatoryListDataItem.setRestrictionLevels(new MLText(result.getString(THRESHOLD)));
											ingRegulatoryListDataItem.setResultIndicator(new MLText(result.getString(RESULT_INDICATOR)));
											
											productContext.getIngRegulatoryList().add(ingRegulatoryListDataItem);
										}

										if (result.getString(RESULT_INDICATOR).toLowerCase().startsWith("prohibited")) {
											String threshold = (result.has(THRESHOLD) && !result.getString(THRESHOLD).equals("None")
													? "(" + result.getString(THRESHOLD) + ")"
													: "");

											MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, threshold);

											ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage,
													RequirementType.Forbidden);
											reqCtrlItem.setRegulatoryCode(regulatoryCode);
											reqCtrlItem.setReqMaxQty(0d);
											if (!threshold.isBlank() && ingItem != null && ingItem.getQtyPerc() != null && ingItem.getQtyPerc() != 0d) {
												Double thresholdValue = DecernisHelper.extractThresholdValue(threshold);
												if (thresholdValue != null) {
													reqCtrlItem.setReqMaxQty((thresholdValue / ingItem.getQtyPerc()) * 100d);
												}
											}
											productContext.getRequirements().add(reqCtrlItem);
											if (logger.isDebugEnabled()) {
												logger.debug("Adding prohibited ing :" + result.get("did").toString());
											}

										} else if (result.getString(RESULT_INDICATOR).toLowerCase().startsWith("not listed")) {
											MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
											ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage,
													RequirementType.Tolerated);
											reqCtrlItem.setRegulatoryCode(regulatoryCode);
											productContext.getRequirements().add(reqCtrlItem);
											if (logger.isDebugEnabled()) {
												logger.debug("Adding not listed ing :" + result.get("did").toString());
											}
										} else if (Boolean.TRUE.equals(addInfoReqCtrl())) {

											String threshold = (result.has(THRESHOLD) && !result.getString(THRESHOLD).equals("None")
													? result.getString(THRESHOLD)
													: "");

											MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PERMITTED_ING, result.getString(RESULT_INDICATOR),
													threshold);
											ReqCtrlListDataItem reqCtrlItem = createReqCtrl(ingItem == null ? null : ingItem.getIng(), reqMessage,
													RequirementType.Info);

											reqCtrlItem.setRegulatoryCode(regulatoryCode);
											productContext.getRequirements().add(reqCtrlItem);
											if (logger.isDebugEnabled()) {
												logger.debug("Adding " + reqMessage.getDefaultValue() + " ing :" + result.get("did").toString());
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

	}

	private IngListDataItem findIngredientItem(List<IngListDataItem> ingList, String decernisID, String function, String ingredientName) {
		for (IngListDataItem ing : ingList) {
			if (decernisID.equals(nodeService.getProperty(ing.getIng(), PLMModel.PROP_REGULATORY_CODE))) {
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
				if (charactName != null) {
					if (ingredientName.equalsIgnoreCase(charactName.getDefaultValue())) {
						return ing;
					}
					if (ingredientName.equalsIgnoreCase(charactName.getValue(Locale.ENGLISH))) {
						return ing;
					}
				}
				MLText legalName = (MLText) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_LEGAL_NAME);
				if (legalName != null) {
					if (ingredientName.equalsIgnoreCase(legalName.getDefaultValue())) {
						return ing;
					}
					if (ingredientName.equalsIgnoreCase(legalName.getValue(Locale.ENGLISH))) {
						return ing;
					}
				}
			}
		} finally {
			MLPropertyInterceptor.setMLAware(mlAware);
		}
		return null;
	}

	/**
	 * <p>createReqCtrl.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param reqCtrlMessage a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @param reqType a {@link fr.becpg.repo.product.data.constraints.RequirementType} object
	 * @return a {@link fr.becpg.repo.product.data.productList.ReqCtrlListDataItem} object
	 */
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

}
