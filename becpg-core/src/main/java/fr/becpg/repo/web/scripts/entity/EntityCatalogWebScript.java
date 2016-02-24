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
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
		String storeType = templateArgs.get("store_type");
		String storeId = templateArgs.get("store_id");
		String nodeId = templateArgs.get("id");

		NodeRef productNodeRef = new NodeRef(storeType, storeId, nodeId);

		if(!nodeService.exists(productNodeRef)){
			return;
		}

		QName qname = QName.createQName(BeCPGModel.BECPG_URI, "productScores");
		Serializable property = nodeService.getProperty(productNodeRef, qname);

		if(property == null){
			if(logger.isDebugEnabled()){
				logger.debug("productScores property was null");
			}

			return;
		}

		JSONObject jsonObject=null;
		try {
			jsonObject = new JSONObject(property.toString());
		} catch (JSONException e1) {
			logger.error("unable to create json object from string: "+property+".");
			logger.error("check on an online json parser if string is correct");
			return;
		}

		if(logger.isDebugEnabled()){
			logger.debug("jsonObject: "+jsonObject);
		}

		JSONObject scores=null;
		JSONObject missingFields=null;

		try {
			scores = jsonObject.getJSONObject("details").getJSONObject("mandatoryFieldsDetails");
			missingFields = jsonObject.getJSONObject("missingFields");
		} catch (JSONException e) {
			logger.error("unable to read scores/missingFields object from json.");
			logger.error("json was: "+jsonObject);
			return;
		}

		JSONObject ret = new JSONObject();

		/*
		 * fills ret object with object
		 * 
		 * {
		 * 		scores: 0.33
		 * 		fields: ["bcpg:productHierarchy1","bcpg:productHierarchy2"]
		 * }
		 */
		Iterator it = missingFields.keys();
		while(it.hasNext()){

			String currentKey = (String) it.next();
			JSONObject currentMandatoryScore = new JSONObject();
			JSONArray fields=null;

			try {
				fields = missingFields.getJSONArray(currentKey);
			} catch (JSONException e1) {
				logger.error("unable to read missing fields array");
				return;
			}

			if(logger.isDebugEnabled()){
				logger.debug("fields: "+fields);
			}
			
			JSONArray localizedFields = new JSONArray();

			for(int i=0; i<fields.length(); i++){
				QName fieldQname;
				String field="";
				try {
					field = (String) fields.get(i);
					if(logger.isDebugEnabled()){
						logger.debug("current field: "+field+ "("+i+")");
					}
					
					fieldQname = QName.createQName(field, namespaceService);
					AssociationDefinition assocDesc = dictionaryService.getAssociation(fieldQname);
					PropertyDefinition propertyDef = dictionaryService.getProperty(fieldQname);
					
					if(logger.isDebugEnabled()){
						logger.debug("assocDesc: "+assocDesc+", propertyDef: "+propertyDef);
					}
					
					if (assocDesc != null) {
						localizedFields.put(assocDesc.getTitle(dictionaryService));
					} else if(propertyDef != null){
						localizedFields.put(propertyDef.getTitle(dictionaryService));
					}
					
				} catch (InvalidQNameException e) {
					logger.error("qname "+field+" is invalid");
				} catch (JSONException e) {
					logger.error("unable to get field at index "+i);
				}
			}

			if(logger.isDebugEnabled()){
				logger.debug("localizedFields: "+localizedFields);
			}

			try {
				currentMandatoryScore.put("fields", localizedFields);
				currentMandatoryScore.put("score", scores.getDouble(currentKey));
			} catch (JSONException e) {
				if(logger.isDebugEnabled()){
					logger.debug("unable to fetch param from missingFields object/scores object with key "+currentKey);
					logger.debug("missingFields: "+missingFields);
					logger.debug("scores: "+scores);
				}
				continue;
			}

			try {
				ret.put(currentKey, currentMandatoryScore);
			} catch (JSONException e) {
				if(logger.isDebugEnabled()){
					logger.debug("unable to put loop score in ret jsonObject");
					logger.debug("score: "+currentMandatoryScore);
					logger.debug("ret: "+ret);
				}
			}
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
		 * 			scores: 0.33,
		 * 			fields: ["bcpg:productHierarchy1","bcpg:productHierarchy2"]
		 * 	},
		 * 	foo: {
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
