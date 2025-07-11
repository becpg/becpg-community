/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>EntityDictinnaryWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityDictionaryWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(EntityDictionaryWebScript.class);

	/** Constant <code>PARAM_ITEMTYPE="itemType"</code> */
	protected static final String PARAM_ITEMTYPE = "itemType";

	/** Constant <code>PARAM_ASSOCNAME="assocName"</code> */
	protected static final String PARAM_ASSOCNAME = "assocName";

	private EntityDictionaryService entityDictionaryService;

	private NamespaceService namespaceService;

	private DictionaryService dictionaryService;

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
			logger.debug("EntityDictionaryWebScript executeImpl()");
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
			if(assocDefs == null ){
				assocDefs = new java.util.LinkedList<>();
			}
			
			QName parentQname = entityDictionaryService.getClass(dataType).getParentName();
			
			if(BeCPGModel.TYPE_CHARACT.equals(parentQname) || assocDefs.isEmpty() || (!BeCPGModel.TYPE_ENTITY_V2.equals(parentQname) 
					&& entityDictionaryService.isSubClass(parentQname, BeCPGModel.TYPE_ENTITY_V2))) {
				//Try assocs on parent
				assocDefs.addAll(entityDictionaryService.getPivotAssocDefs(parentQname,true));
			}
			
			
			
			for (AssociationDefinition assocDef : assocDefs) {
				if (assocDef.getTitle(dictionaryService) != null && assocDef.getSourceClass().getTitle(dictionaryService) != null) {
					JSONObject item = new JSONObject();
					item.put("label", assocDef.getTitle(dictionaryService) + " - " + assocDef.getSourceClass().getTitle(dictionaryService));
					item.put("assocType", assocDef.getName().toPrefixString(namespaceService));
					item.put(PARAM_ITEMTYPE, assocDef.getSourceClass().getName().toPrefixString(namespaceService));
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
				logger.debug("EntityDictionaryWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

}
