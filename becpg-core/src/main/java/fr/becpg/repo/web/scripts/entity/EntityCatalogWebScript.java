package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;

/**
 * Gathers product's missing fields info : which ones are missing, and what
 * is the score related to it.
 * 
 * Requires PLM Module
 * 
 * @author steven
 *
 */
public class EntityCatalogWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(EntityCatalogWebScript.class);

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private NamespaceService namespaceService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService){
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException{
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
		String storeType = templateArgs.get("store_type");
		String storeId = templateArgs.get("store_id");
		String nodeId = templateArgs.get("id");

		NodeRef productNodeRef = new NodeRef(storeType, storeId, nodeId);


		if(!nodeService.exists(productNodeRef)){
			logger.error("node "+productNodeRef+" does not exist");
			return;
		}

		QName qname = QName.createQName(BeCPGModel.BECPG_URI, "productScores");
		Serializable property = nodeService.getProperty(productNodeRef, qname);

		if(property == null){		
			return;
		}

		JSONArray catalogs=null;
		JSONObject jsonObject=null;
		try {
			jsonObject = new JSONObject(property.toString());
			catalogs = jsonObject.getJSONArray("catalogs");
		} catch (JSONException e1) {
			logger.error("unable to create json object from string: "+property+".");
			logger.error("check on an online json parser if string is correct");
			return;
		}

		JSONArray ret = new JSONArray();

		/*
		 * fills ret object with object
		 * 
		 * {
		 * 		locales: [en, fr]
		 * 		scores: 0.33
		 * 		fields: ["bcpg:productHierarchy1","bcpg:productHierarchy2"]
		 * }
		 */
		for(int it=0; it<catalogs.length(); it++){
			JSONObject currentCatalog = new JSONObject();
			JSONArray fields=null;
			JSONObject currentTransformedCatalog = new JSONObject();

			try {
				currentCatalog = (JSONObject) catalogs.get(it);
				fields = currentCatalog.getJSONArray("missingFields");
			} catch (JSONException e1) {
				logger.error("unable to read missing fields array");
				return;
			}

			JSONArray missingFieldsArray = new JSONArray();

			for(int i=0; i<fields.length(); i++){
				QName fieldQname;
				String field="";
				String locale=null;
				JSONObject currentMissingField = new JSONObject();


				try {
					field = (String) fields.get(i);
				} catch (JSONException e) {
					logger.error("unable to get field with index "+i, e);
				}	

				if(field.contains("_")){
					locale = field.split("_")[1];
					field= field.split("_")[0];
				}					

				//try to fetch property/assoc for translation
				AssociationDefinition assocDesc = null;
				PropertyDefinition propertyDef = null;
				try {
					fieldQname = QName.createQName(field, namespaceService);
					assocDesc = dictionaryService.getAssociation(fieldQname);
					propertyDef = dictionaryService.getProperty(fieldQname);
				} catch (InvalidQNameException e){
					logger.error("qname "+field+" was invalid",e);
				}

				try {
					if (assocDesc != null) {
						currentMissingField.put("localized",assocDesc.getTitle(dictionaryService)+ (locale != null ? "_"+locale : ""));
					} else if(propertyDef != null){
						currentMissingField.put("localized",propertyDef.getTitle(dictionaryService)+ (locale != null ? "_"+locale : ""));
					} else {
						currentMissingField.put("localized",field + (locale != null ? "_"+locale : ""));
					}	

					currentMissingField.put("code",field);
				} catch(JSONException e){
					logger.error("unable to put localized field or field code in missing field object", e);
				}

				locale = null;
				missingFieldsArray.put(currentMissingField);
			}

			try {
				currentTransformedCatalog.put("missingFields", missingFieldsArray);
				currentTransformedCatalog.put("score", currentCatalog.getDouble("score"));
				currentTransformedCatalog.put("label", currentCatalog.getString("label"));
				currentTransformedCatalog.put("id", currentCatalog.getString("id"));
				if(currentCatalog.has("locales")){
					currentTransformedCatalog.put("locales", currentCatalog.getJSONArray("locales"));
				}
			} catch (JSONException e) {
				if(logger.isDebugEnabled()){
					logger.debug("unable to fetch param from missingFields object/scores object with key "+it);
					logger.debug("missingFields: "+catalogs);
				}
				continue;
			}

			ret.put(currentTransformedCatalog);
		}

		if(logger.isDebugEnabled()){
			logger.debug("done fetching scores, ret: \n"+ret);
		}

		res.setContentType("application/json");
		res.setContentEncoding("UTF-8");

		/*
		 * Writing ret in response
		 * 
		 * Looks like :
		 * 
		 * {
		 * 	std: {
		 * 			locales: [en, fr]
		 * 			scores: 0.33,
		 * 			fields: ["bcpg:productHierarchy1","bcpg:productHierarchy2"]
		 * 	},
		 * 	foo: {
		 * 			----locales: []---- 
		 * 			scores: 0.5,
		 * 			fields: ["bcpg:legalName", "bar", "baz"]
		 * 	}
		 * }
		 */
		try {
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}


	}

}
