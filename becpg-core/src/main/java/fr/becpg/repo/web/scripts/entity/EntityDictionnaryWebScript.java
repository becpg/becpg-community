/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.EntityDictionaryService;


@Service
public class EntityDictionnaryWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(EntityDictionnaryWebScript.class);

	protected static final String PARAM_ITEMTYPE = "itemType";
	
	protected static final String PARAM_ASSOCNAME = "assocName";
	

	private EntityDictionaryService entityDictionaryService;
	
	private NamespaceService namespaceService;



	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}


	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
			logger.debug("EntityDictionnaryWebScript executeImpl()");
		}
		
		
		String itemType = req.getParameter(PARAM_ITEMTYPE);
		String assocName = req.getParameter(PARAM_ASSOCNAME);
		QName dataType = null;
		QName assocQname =null;
		if(itemType!=null){
			 dataType = QName.createQName(itemType, namespaceService);
		} else if(assocName!=null){
			assocQname = QName.createQName(assocName.replace("assoc_","").replace("_", ":"),namespaceService);
			dataType = entityDictionaryService.getTargetType( assocQname);
		}

		
		try {
		
		JSONObject ret = new JSONObject();
		JSONArray items = new JSONArray();
		for(AssociationDefinition assocDef : entityDictionaryService.getPivotAssocDefs(dataType)){
			JSONObject item = new JSONObject();
			item.put("label",assocDef.getTitle()+" - "+assocDef.getSourceClass().getTitle());
			item.put("assocType",assocDef.getName().toPrefixString(namespaceService));
			item.put("itemType",assocDef.getSourceClass().getName().toPrefixString(namespaceService));
			if(assocQname!=null && assocQname.equals(assocDef.getName())){
				item.put("selected",true);
			}
			items.put(item);
		}
		ret.put("type",dataType.toPrefixString());
		ret.put("items",items);

		
		res.setContentType("application/json");
		res.setContentEncoding("UTF-8");
		ret.write(res.getWriter());
		

	} catch (JSONException e) {
		throw new WebScriptException("Unable to serialize JSON", e);
	} finally {
		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("EntityDictionnaryWebScript execute in " + watch.getTotalTimeSeconds() + "s");
		}
	}

	}

}
