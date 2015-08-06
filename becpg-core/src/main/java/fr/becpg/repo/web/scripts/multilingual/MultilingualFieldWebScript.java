/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.web.scripts.multilingual;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

/**
 * Return or save MLText field
 * 
 * @author matthieu
 * 
 */
public class MultilingualFieldWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(MultilingualFieldWebScript.class);

	private static final String PARAM_NODEREF = "nodeRef";

	private static final String PARAM_FIELD = "field";

	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter(PARAM_NODEREF);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		NodeRef formNodeRef = null;
		QName fieldQname = null;

		if (nodeRef != null && !nodeRef.isEmpty()) {
			formNodeRef = new NodeRef(nodeRef);
		}

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String fieldName = templateArgs.get(PARAM_FIELD);
			if (fieldName != null) {
				fieldName = fieldName.replace("_", ":");
				fieldQname = QName.createQName(fieldName, serviceRegistry.getNamespaceService());
			}
		}

		if (formNodeRef == null || fieldQname == null) {
			throw new WebScriptException("Invalid params");
		}

		try {

			PropertyDefinition propertyDef = serviceRegistry.getDictionaryService().getProperty(fieldQname);
			QName dataType = propertyDef.getDataType().getName();

			// MLText
			if (dataType.isMatch(DataTypeDefinition.MLTEXT)) {
				// Save
				MLText mlText = null;

				JSONObject json = (JSONObject) req.parseContent();
				if (json != null) {
					mlText = new MLText();
					for (Iterator<String> iterator = json.keys(); iterator.hasNext();) {
						String key = iterator.next();
						if (!"-".equals(key)) {
							Locale loc = new Locale(new Locale(key).getLanguage());
							if (json.getString(key) != null) {
								if(json.getString(key).length() > 0){
									mlText.addValue(loc, json.getString(key));
								} else {
									mlText.removeValue(loc);
								}
							}
						}
					}
					serviceRegistry.getNodeService().setProperty(formNodeRef, fieldQname, mlText);

				}

				boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
				try {
					if (mlText == null) {
						Serializable value = serviceRegistry.getNodeService().getProperty(formNodeRef, fieldQname);
						if (value instanceof MLText) {
							mlText = (MLText) value;
						} else if (value != null && value instanceof String) {
							mlText = new MLText();
							mlText.addValue(I18NUtil.getContentLocaleLang(), (String) value);
						} else {
							mlText = new MLText();
							mlText.addValue(I18NUtil.getContentLocaleLang(), "");
						}
					}
				} finally {
					MLPropertyInterceptor.setMLAware(wasMLAware);
				}

				JSONObject ret = new JSONObject();

				if (mlText != null) {
					JSONArray items = new JSONArray();
					for (Map.Entry<Locale, String> mlEntry : mlText.entrySet()) {
						JSONObject item = new JSONObject();
						item.put("label", propertyDef.getTitle(serviceRegistry.getDictionaryService()));
						item.put("description", propertyDef.getDescription(serviceRegistry.getDictionaryService()));
						item.put("name", fieldQname.toPrefixString(serviceRegistry.getNamespaceService()));
						item.put("value", mlEntry.getValue());
						item.put("locale", mlEntry.getKey().getLanguage());
						items.put(item);
					}

					ret.put("items", items);
				}

				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				res.getWriter().write(ret.toString(3));

			} else {
				throw new WebScriptException("Field is not an MLText");
			}

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("MultilingualFieldWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

}
