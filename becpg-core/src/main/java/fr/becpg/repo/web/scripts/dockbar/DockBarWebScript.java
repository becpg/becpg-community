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
package fr.becpg.repo.web.scripts.dockbar;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.extractors.ContentDataExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.web.scripts.BrowserCacheHelper;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * return product history
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DockBarWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(DockBarWebScript.class);

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";
	
	private static final String PARAM_ENTITY_LIST = "list";
	
	private static final String DELIMITER = "#";

	private static final String PREF_DOCKBAR_HISTORY = "fr.becpg.DockBarHistory";

	private static final  int DOCKBAR_SIZE = 6;

	private AttributeExtractorService attributeExtractorService;

	private PreferenceService preferenceService;

	private ServiceRegistry serviceRegistry;
	

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/**
	 * <p>Setter for the field <code>preferenceService</code>.</p>
	 *
	 * @param preferenceService a {@link org.alfresco.service.cmr.preference.PreferenceService} object.
	 */
	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		String entityList = req.getParameter(PARAM_ENTITY_LIST);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		NodeRef productNodeRef = null;
		if (entityNodeRef != null && !entityNodeRef.isEmpty() && NodeRef.isNodeRef(entityNodeRef)) {
			productNodeRef = new NodeRef(entityNodeRef);
		}
		

		try {

			if (req.getParameter("clear") != null) {
				preferenceService.clearPreferences(AuthenticationUtil.getFullyAuthenticatedUser());
			}

			String username = AuthenticationUtil.getFullyAuthenticatedUser();

			Map<String, Serializable> histories = preferenceService.getPreferences(username);
			
			String nodeRefs = (String) histories.get(PREF_DOCKBAR_HISTORY);
			if (logger.isDebugEnabled()) {
				logger.debug("Getting :" + nodeRefs + " from history for " + username);
			}

			LinkedList<NodeRef> elements = new LinkedList<>();
			Map<NodeRef, String> items = new HashMap<>();
			
			if (nodeRefs != null && !nodeRefs.isEmpty()) {
				String[] splitted = nodeRefs.split(",");
				
				boolean isFirst = true;
				
				for (String field : splitted) {
					
					NodeRef el = new NodeRef(field.split(DELIMITER)[0]);
					if (!el.equals(productNodeRef)) {
						elements.add(el);
						items.put(el, field.contains(DELIMITER) ? field.split(DELIMITER)[1] : null  );
						
					} else if(isFirst && BrowserCacheHelper.isBrowserHasInCache(req)){
						res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
						if (logger.isDebugEnabled()) {
							logger.debug("Send Not_MODIFIED status");
						}
						return;
					}
					isFirst = false;
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Entity NodeRef empty : " + productNodeRef != null);

				logger.debug("Element.size(): " + elements.size());
			}

			if (productNodeRef != null &&  serviceRegistry.getNodeService().exists(productNodeRef)) {

				QName type = serviceRegistry.getNodeService().getType(productNodeRef);

				if (serviceRegistry.getDictionaryService().isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)
						&& !serviceRegistry.getDictionaryService().isSubClass(type, BeCPGModel.TYPE_SYSTEM_ENTITY)) {
					if (elements.size() > DOCKBAR_SIZE - 1) {
						NodeRef removedItem = elements.remove(DOCKBAR_SIZE - 1);
						items.remove(removedItem);
					}
					elements.add(0, productNodeRef);
					items.put(productNodeRef, entityList);

					nodeRefs = "";
					for (NodeRef nodeRef : elements) {
						if (!nodeRefs.isEmpty()) {
							nodeRefs += ",";
						}
						nodeRefs += nodeRef.toString() + DELIMITER + items.get(nodeRef) ;
					}

					if (logger.isDebugEnabled()) {
						logger.debug("Setting :" + nodeRefs + " to history");
					}

					histories.put(PREF_DOCKBAR_HISTORY, nodeRefs);
					preferenceService.setPreferences(username, histories);
				}
			} 

			Cache cache = new Cache(getDescription().getRequiredCache());
			cache.setIsPublic(false);
			cache.setMustRevalidate(true);
			cache.setNeverCache(false);
			cache.setMaxAge(0L);
			cache.setLastModified(new Date());
			res.setCache(cache);
			
			JSONObject ret = processResults(elements, WebscriptHelper.extractMetadataFields(req), items);
			ret.put("page", 1);
			ret.put("pageSize", DOCKBAR_SIZE);
			ret.put("fullListSize", elements.size());

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString(3));

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				assert watch != null;
				watch.stop();
				logger.debug("DockBarWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

	private JSONObject processResults(List<NodeRef> results, List< AttributeExtractorField> metadataFields, Map<NodeRef, String> elements) throws InvalidNodeRefException, JSONException {

		JSONArray items = new JSONArray();

		for (NodeRef nodeRef : results) {
			if (serviceRegistry.getNodeService().exists(nodeRef)
					&& serviceRegistry.getPermissionService().hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				Map<String, Object> item = new ContentDataExtractor(metadataFields, serviceRegistry, attributeExtractorService).extract(nodeRef);
				item.put(PARAM_ENTITY_LIST, elements.get(nodeRef));
				items.put(item);
			}
		}

		JSONObject obj = new JSONObject();
		obj.put("items", items);
		return obj;

	}
	
	 

}
