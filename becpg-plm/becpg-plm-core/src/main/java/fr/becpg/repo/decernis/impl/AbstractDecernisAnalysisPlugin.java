package fr.becpg.repo.decernis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public abstract class AbstractDecernisAnalysisPlugin implements DecernisAnalysisPlugin {

	protected static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	protected static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";
	protected static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";

	protected static final String PROHIBITED = "message.decernis.ingredient.result.prohibited";
	protected static final String PERMITTED = "message.decernis.ingredient.result.permitted";
	protected static final String NOT_LISTED = "message.decernis.ingredient.result.notListed";

	@Value("${beCPG.decernis.serverUrl}")
	protected String serverUrl;
	
	@Value("${beCPG.decernis.analysisUrl}")
	protected String analysisUrl;

	@Value("${beCPG.decernis.companyName}")
	protected String companyName;

	@Value("${beCPG.decernis.token}")
	protected String token;

	@Value("#{new Boolean('${beCPG.formulation.specification.addInfoReqCtrll}'.trim())}")
	protected Boolean addInfoReqCtrl;

	@Autowired
	protected NodeService nodeService;

	protected RestTemplate restTemplate;
	
	protected AbstractDecernisAnalysisPlugin() {
		restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		if (CollectionUtils.isEmpty(interceptors)) {
			interceptors = new ArrayList<>();
		}
		interceptors.add(new DecernisRequestInterceptor());
		restTemplate.setInterceptors(interceptors);
	}

	@Override
	public void analyzeRecipe(RegulatoryContext productContext) {
		for (RegulatoryContextItem contextItem : productContext.getContextItems()) {
			if (!contextItem.isEmpty()) {
				List<JSONObject> analysisList = analyzeContext(productContext, contextItem.getUsages(), contextItem.getCountries(), contextItem.getModuleId());
				contextItem.getItem().setRegulatoryResult(extractResult(analysisList));
			}
		}
		List<JSONObject> analysisList = analyzeContext(productContext, productContext.getUsages(), productContext.getCountries(), productContext.getModuleId());
		productContext.getProduct().setRegulatoryResult(extractResult(analysisList));
	}

	private String extractResult(List<JSONObject> analysisList) {
		
		boolean notListed = false;
		
		for (JSONObject analysis : analysisList) {
			String result = extractResult(analysis);
			if (result.startsWith("prohibited")) {
				return I18NUtil.getMessage(PROHIBITED);
			}
			if (result.startsWith("not listed")) {
				notListed = true;
			}
		}
		
		if (notListed) {
			return I18NUtil.getMessage(NOT_LISTED);
		}
		
		return I18NUtil.getMessage(PERMITTED);
	}

	private List<JSONObject> analyzeContext(RegulatoryContext productContext, Set<String> usages, Set<String> countries, Integer moduleId) {
		List<JSONObject> analysisList = new ArrayList<>();
		for (String usage : usages) {
			JSONObject analysis = postRecipeAnalysis(productContext, countries, usage, moduleId);
			if (analysis != null) {
				productContext.getRequirements().addAll(extractItemRequirements(countries, moduleId, analysis, productContext.getIngRegulatoryMapping()));
				analysisList.add(analysis);
			}
		}
		return analysisList;
	}

	protected abstract String extractResult(JSONObject analysisResults);
	
	protected abstract JSONObject postRecipeAnalysis(RegulatoryContext productContext, Set<String> countries, String usage, Integer moduleId);
	
	private List<ReqCtrlListDataItem> extractItemRequirements(Set<String> countries, Integer moduleId, JSONObject analysisResults, Map<String, List<IngListDataItem>> ings)
			throws JSONException {
		List<ReqCtrlListDataItem> reqCtrlList = new ArrayList<>();

		for (String country : countries) {
			if (isAvailableCountry(country, moduleId)) {
				reqCtrlList.addAll(extractRequirements(analysisResults, ings, country));
			}
		}
		return reqCtrlList;
	}
	
	protected abstract List<ReqCtrlListDataItem> extractRequirements(JSONObject analysisResults, Map<String, List<IngListDataItem>> ings, String country);

	protected abstract boolean isAvailableCountry(String country, Integer moduleId);

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
		headers.setBearerAuth(token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(body, headers);
	}
}
