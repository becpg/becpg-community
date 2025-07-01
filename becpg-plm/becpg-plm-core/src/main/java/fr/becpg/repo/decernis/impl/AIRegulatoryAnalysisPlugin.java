package fr.becpg.repo.decernis.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.client.HttpStatusCodeException;

import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.helper.DecernisHelper;
import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>
 * An implementation of {@link DecernisAnalysisPlugin} that integrates with a
 * modern, AI-driven regulatory analysis REST API.
 * </p>
 * <p>
 * This plugin sends the entity's Alfresco node ID to an external service.
 * It expects a consolidated analysis in a complex JSON format mirroring Alfresco's
 * data structure, which it then uses to populate both product-level compliance
 * requirements and ingredient-specific regulatory data.
 * </p>
 *
 * @author matthieu
 * @version 1.3
 */
//@Service
public class AIRegulatoryAnalysisPlugin extends DefaultDecernisAnalysisPlugin implements DecernisAnalysisPlugin {

	private static final Log logger = LogFactory.getLog(AIRegulatoryAnalysisPlugin.class);


	public AIRegulatoryAnalysisPlugin(@Qualifier("nodeService") NodeService nodeService,
			SystemConfigurationService systemConfigurationService) {
		super(nodeService, systemConfigurationService);
	}

	@Override
	public boolean isEnabled() {
		return analysisUrl() != null && !analysisUrl().isBlank() && !analysisUrl().equals(serverUrl());
	}

	@Override
	public boolean needsRecipeId() {
		return false;
	}

	@Override
	public void extractRequirements(RegulatoryContext productContext, RegulatoryContextItem contextItem) {
	
		try {
			JSONObject analysisResults = postAIProductAnalysis(productContext);
			if (analysisResults != null) {
				parseAIAnalysisForRequirements(productContext, analysisResults);
			}
		} catch (HttpStatusCodeException e) {
			logger.error("Error during AI regulatory analysis for requirements: " + DecernisHelper.cleanError(e.getMessage()), e);
			
					ReqCtrlListDataItem req = ReqCtrlListDataItem.forbidden()
							.withMessage(MLTextHelper.getI18NMessage("message.decernis.error",
									"Error during AI analysis: " + DecernisHelper.cleanError(e.getMessage())))
							.ofDataType(RequirementDataType.Formulation)
							.withFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
					productContext.getRequirements().add(req);
			
		} catch (Exception e) {
			logger.error(
					"An unexpected error occurred during AI regulatory analysis for requirements: " + DecernisHelper.cleanError(e.getMessage()), e);
		}
	}

	@Override
	public void ingredientAnalysis(RegulatoryContext productContext, RegulatoryContextItem contextItem) {
		

		try {
			JSONObject analysisResults = postAIProductAnalysis(productContext);
			if (analysisResults != null) {
				parseAIAnalysisForIngredients(productContext, contextItem, analysisResults);
			}
		} catch (HttpStatusCodeException e) {
			logger.error("Error during AI regulatory analysis for ingredients: " + DecernisHelper.cleanError(e.getMessage()), e);
		} catch (Exception e) {
			logger.error(
					"An unexpected error occurred during AI regulatory analysis for ingredients: " + DecernisHelper.cleanError(e.getMessage()), e);
		}
	}

	private JSONObject postAIProductAnalysis(RegulatoryContext context) throws JSONException {

		String entityId = context.getProduct().getNodeRef().getId();
		if (entityId == null || entityId.isBlank()) {
			logger.warn("Product node ID is missing, cannot perform AI analysis for node: " + context.getProduct().getNodeRef());
			return null;
		}

		JSONObject payload = new JSONObject();
		payload.put("entityId", entityId);

		// Use the new service endpoint
		String url = analysisUrl() + "/regulatory-analysis";

		HttpEntity<String> entity = createEntity(payload.toString());

		if (logger.isDebugEnabled()) {
			logger.debug("Posting to AI Regulatory Analysis API. URL: " + url + ", Payload: " + payload.toString(2));
		}

		String result = RestTemplateHelper.getRestTemplate().postForObject(url, entity, String.class, new HashMap<>());

		if (logger.isTraceEnabled()) {
			logger.trace("Received response from AI Regulatory Analysis API: " + result);
		}
		
		return (result != null && !result.isBlank()) ? new JSONObject(result) : null;
	}
	
	private void parseAIAnalysisForRequirements(RegulatoryContext productContext, JSONObject analysisResults) {
	    JSONObject entity = analysisResults.optJSONObject("entity");
	    if (entity == null) return;

	    JSONObject datalists = entity.optJSONObject("datalists");
	    if (datalists == null) return;

	    JSONArray regulatoryList = datalists.optJSONArray("bcpg:regulatoryList");
	    if (regulatoryList == null) return;

	    for (int i = 0; i < regulatoryList.length(); i++) {
	        JSONObject regItem = regulatoryList.optJSONObject(i);
	        if (regItem == null) continue;

	        JSONObject attributes = regItem.optJSONObject("attributes");
	        if (attributes == null) continue;

	        String status = attributes.optString("bcpg:regulatoryResult");
	        RequirementType reqType = getRequirementTypeFromStatus(status);

	        if (reqType == RequirementType.Info && !Boolean.TRUE.equals(addInfoReqCtrl())) {
	            continue;
	        }

	        MLText reqMessage = new MLText("Product compliance status is: " + status);
	        
	        JSONArray countriesArray = attributes.optJSONArray("bcpg:regulatoryCountries");
	        JSONArray usagesArray = attributes.optJSONArray("bcpg:regulatoryUsageRef");

	        if (countriesArray == null || usagesArray == null) continue;

	        // Create a requirement for each combination of country and usage in this result block
	        for (int c = 0; c < countriesArray.length(); c++) {
	            JSONObject countryObj = countriesArray.optJSONObject(c);
	            if (countryObj == null || countryObj.optJSONObject("attributes") == null) continue;
	            String countryCode = countryObj.optJSONObject("attributes").optString("bcpg:geoOriginISOCode");

	            for (int u = 0; u < usagesArray.length(); u++) {
	                JSONObject usageObj = usagesArray.optJSONObject(u);
	                if (usageObj == null) continue;
	                String usageName = usageObj.optString("bcpg:charactName");
	                
	                if (!countryCode.isBlank() && !usageName.isBlank()) {
	                    ReqCtrlListDataItem reqCtrlItem = createReqCtrl(null, reqMessage, reqType);
	                    reqCtrlItem.setRegulatoryCode(countryCode + " - " + usageName);
	                    productContext.getRequirements().add(reqCtrlItem);
	                }
	            }
	        }
	    }
	}


	private void parseAIAnalysisForIngredients(RegulatoryContext productContext, RegulatoryContextItem contextItem, JSONObject analysisResults) {
	    JSONObject entity = analysisResults.optJSONObject("entity");
	    if (entity == null) return;

	    JSONObject datalists = entity.optJSONObject("datalists");
	    if (datalists == null) return;

	    JSONArray ingRegulatoryList = datalists.optJSONArray("bcpg:ingRegulatoryList");
	    if (ingRegulatoryList == null) return;

	    for (int i = 0; i < ingRegulatoryList.length(); i++) {
	        JSONObject ingRegItemJson = ingRegulatoryList.optJSONObject(i);
	        if (ingRegItemJson == null) continue;

	        JSONObject attributes = ingRegItemJson.optJSONObject("attributes");
	        if (attributes == null) continue;

	        JSONObject ingRef = attributes.optJSONObject("bcpg:irlIng");
	        if (ingRef == null) continue;
	        String ingId = ingRef.optString("id");

	        IngListDataItem ingItem = findIngredientByIng(productContext.getProduct().getIngList(), ingId);
	        if (ingItem == null) continue;
	        
	        JSONArray countriesArray = attributes.optJSONArray("bcpg:regulatoryCountries");
	        if (countriesArray == null || countriesArray.length() == 0) continue;
	        
	        // Assuming one country per entry as per the example
	        JSONObject countryRef = countriesArray.optJSONObject(0);
	        if(countryRef == null || countryRef.optJSONObject("attributes") == null) continue;
	        String countryCode = countryRef.optJSONObject("attributes").optString("bcpg:geoOriginISOCode");
	        
	        NodeRef countryNodeRef = contextItem.getCountries().get(countryCode);
	        if (countryNodeRef == null) continue;

	        IngRegulatoryListDataItem ingRegItem = createIngRegulatoryListDataItem(ingItem.getIng(), countryNodeRef);

	        ingRegItem.setRestrictionLevels(new MLText(attributes.optString("bcpg:irlRestrictionLevels")));
	        ingRegItem.setResultIndicator(new MLText(attributes.optString("bcpg:irlResultIndicator")));
	        ingRegItem.setCitation(new MLText(attributes.optString("bcpg:irlCitation")));
	        ingRegItem.setUsages(new MLText(attributes.optString("bcpg:irlUsages")));
	        ingRegItem.setPrecautions(new MLText(attributes.optString("bcpg:irlPrecautions")));

	        productContext.getIngRegulatoryList().add(ingRegItem);
	    }
	}

	private IngListDataItem findIngredientByIng(List<IngListDataItem> ingList, String id) {
		if (id == null || id.isBlank()) {
			return null;
		}
		for (IngListDataItem item : ingList) {
			if (id.equals(item.getIng().getId())) {
				return item;
			}
		}
		return null;
	}
	
	private RequirementType getRequirementTypeFromStatus(String status) {
	    if (status == null) {
	        return RequirementType.Info;
	    }
		switch (status.toLowerCase()) {
			case "forbidden", "prohibited","overlimit":
				return RequirementType.Forbidden;
			case "notlisted","tolerated":
				return RequirementType.Tolerated;
			case "permitted","allowed", "info":
			default:
				return RequirementType.Info;
		}
	}
}