/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.EntityDictionaryService;

public class EntityDictionnaryWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(EntityDictionnaryWebScript.class);

	protected static final String PARAM_ITEMTYPE = "itemType";

	protected static final String PARAM_ASSOCNAME = "assocName";

	private EntityDictionaryService entityDictionaryService;

	private NamespaceService namespaceService;

	private DictionaryService dictionaryService;

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

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
		QName assocQname = null;
		if (itemType != null) {
			dataType = QName.createQName(itemType, namespaceService);
		} else if (assocName != null) {
			assocQname = QName.createQName(assocName.replace("assoc_", "").replace("_", ":"), namespaceService);
			dataType = entityDictionaryService.getTargetType(assocQname);
		}

		try {

			JSONObject ret = new JSONObject();
			JSONArray items = new JSONArray();
			
			List<AssociationDefinition> assocDefs = entityDictionaryService.getPivotAssocDefs(dataType);
			if(assocDefs == null || assocDefs.isEmpty()){
				//Try assocs on parent
				assocDefs = entityDictionaryService.getPivotAssocDefs(dictionaryService.getClass(dataType).getParentName());
			}
			
			for (AssociationDefinition assocDef : assocDefs) {
				if (assocDef.getTitle(dictionaryService) != null && assocDef.getSourceClass().getTitle(dictionaryService) != null) {
					JSONObject item = new JSONObject();
					item.put("label", assocDef.getTitle(dictionaryService) + " - " + assocDef.getSourceClass().getTitle(dictionaryService));
					item.put("assocType", assocDef.getName().toPrefixString(namespaceService));
					item.put("itemType", assocDef.getSourceClass().getName().toPrefixString(namespaceService));
					if (assocQname != null && assocQname.equals(assocDef.getName())) {
						item.put("selected", true);
					}
					items.put(item);
				}
			}
			if(dataType!=null){
				ret.put("type", dataType.toPrefixString());
			}
			ret.put("items", items);

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
