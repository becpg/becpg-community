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
package fr.becpg.repo.web.scripts.multilingual;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
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
import org.apache.commons.text.StringEscapeUtils;
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
import org.springframework.web.client.RestTemplate;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * Return or save MLText field
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MultilingualFieldWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(MultilingualFieldWebScript.class);

	private static final String PARAM_NODEREF = "nodeRef";

	private static final String PARAM_FIELD = "field";

	private static final String PARAM_SUGGEST = "suggest";

	private static final String PARAM_TARGET = "target";
	
	private static final String PARAM_COPY = "copy";
	
	private static final String PARAM_DEST_FIELD = "destField";

	private String googleApiKey;

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
	 * <p>Setter for the field <code>googleApiKey</code>.</p>
	 *
	 * @param googleApiKey a {@link java.lang.String} object.
	 */
	public void setGoogleApiKey(String googleApiKey) {
		this.googleApiKey = googleApiKey;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter(PARAM_NODEREF);
		
		boolean copy = "true".equals(req.getParameter(PARAM_COPY));
		String destFieldName = req.getParameter(PARAM_DEST_FIELD);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		NodeRef formNodeRef = null;
		QName fieldQname = null;
		QName destFieldQname = null;
		
		if (destFieldName != null) {
			destFieldName = destFieldName.replace("_", ":");
			destFieldQname = QName.createQName(destFieldName, serviceRegistry.getNamespaceService());
		}

		if ((nodeRef != null) && !nodeRef.isEmpty()) {
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

		if ((formNodeRef == null) || (fieldQname == null)) {
			throw new WebScriptException("Invalid params");
		}

		try {

			PropertyDefinition propertyDef = serviceRegistry.getDictionaryService().getProperty(fieldQname);
			QName dataType = propertyDef.getDataType().getName();

			// MLText
			if (dataType.isMatch(DataTypeDefinition.MLTEXT)) {
				// Save
				MLText mlText = null;
				
				Locale contentLocale = I18NUtil.getContentLocale();
				String language = contentLocale.getLanguage();
		    	Locale  toSaveUnderLocale= new Locale(language);
		    	if(MLTextHelper.isSupportedLocale(contentLocale)){
		    		toSaveUnderLocale = contentLocale;
		    	}

				boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
				try {
					

					Serializable value = serviceRegistry.getNodeService().getProperty(formNodeRef, fieldQname);
					if (value instanceof MLText) {
						mlText = (MLText) value;
					} else if (value instanceof String) {
						mlText = new MLText();
						mlText.addValue(toSaveUnderLocale, (String) value);
					} else {
						mlText = new MLText();
						mlText.addValue(toSaveUnderLocale, "");
					}

				} finally {
					MLPropertyInterceptor.setMLAware(wasMLAware);
				}

				JSONObject ret = new JSONObject();

				String suggest = req.getParameter(PARAM_SUGGEST);

				if ((suggest != null) && "true".equals(suggest)) {
					String target = req.getParameter(PARAM_TARGET);
					if (target == null) {
						throw new WebScriptException("Invalid params");
					}

					ret.put("translatedText", getTranslatedText(mlText.getDefaultValue(), target));

				} else {

					JSONObject json = (JSONObject) req.parseContent();
					if (json != null) {
						for (Iterator<String> iterator = json.keys(); iterator.hasNext();) {
							String key = iterator.next();
							if (!"-".equals(key)) {
								Locale loc = MLTextHelper.parseLocale(key);
								if (json.getString(key) != null) {
									if (!json.getString(key).isBlank()) {
										mlText.addValue(loc, json.getString(key).trim());
									} else {
										mlText.removeValue(loc);
									}
								}
							}
						}
						if (mlText.isEmpty()) {
							serviceRegistry.getNodeService().removeProperty(formNodeRef, fieldQname);
						} else {
							serviceRegistry.getNodeService().setProperty(formNodeRef, fieldQname, mlText);
						}

					}

					if (mlText != null) {
						if(copy){
							serviceRegistry.getNodeService().setProperty(formNodeRef, destFieldQname, mlText);
						}
						JSONArray items = new JSONArray();
						for (Map.Entry<Locale, String> mlEntry : mlText.entrySet()) {
							JSONObject item = new JSONObject();
							item.put("label", propertyDef.getTitle(serviceRegistry.getDictionaryService()));
							item.put("description", propertyDef.getDescription(serviceRegistry.getDictionaryService()));
							item.put("name", fieldQname.toPrefixString(serviceRegistry.getNamespaceService()));
							item.put("value", mlEntry.getValue());
							
							String lang = mlEntry.getKey().getLanguage();
							String code = lang;
							String country = lang.toUpperCase();
							if(mlEntry.getKey().getCountry()!=null && !mlEntry.getKey().getCountry().isEmpty()){
								country =mlEntry.getKey().getCountry();
								code+="_"+country;
							}
							item.put("locale", code);
							item.put("lang", lang);
							item.put("country", country);
							items.put(item);
						}

						ret.put("items", items);
						ret.put("currentLocale",MLTextHelper.localeKey(toSaveUnderLocale));
						
					}

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


	private String getTranslatedText(String defaultValue, String target) {
		if ((googleApiKey != null) && !googleApiKey.isEmpty()) {
			logger.debug("Try to translate : " + defaultValue + " in " + target);

			if(target.split("_")[0].equals(I18NUtil.getContentLocale().getLanguage())) {
				return defaultValue;
			}
			
			Map<String, String> vars = new HashMap<>();
			vars.put("key", googleApiKey);
			vars.put("target", target.split("_")[0]);
			vars.put("source", I18NUtil.getContentLocale().getLanguage());
			vars.put("q", defaultValue);
			
			String url = "https://www.googleapis.com/language/translate/" + "v2?key={key}&source={source}&target={target}&q={q}";

			RestTemplate restTemplate = new RestTemplate();

			String ret = restTemplate.getForObject(url, String.class, vars);

			try {
				JSONObject jsonObject = new JSONObject(ret);
				if (jsonObject.has("data") && jsonObject.getJSONObject("data").has("translations")) {
					JSONArray translations = jsonObject.getJSONObject("data").getJSONArray("translations");
					if (translations.length() > 0) {
						return StringEscapeUtils.unescapeHtml4(translations.getJSONObject(0).getString("translatedText"));
					}
				}

			} catch (JSONException e) {
				logger.error("Fail to parse JSON", e);

			}

		} else {
			logger.warn("No google translate API key provided");
		}
		return "No suggestion available";
	}

}
