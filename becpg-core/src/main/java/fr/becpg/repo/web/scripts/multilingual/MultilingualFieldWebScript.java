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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RestTemplateHelper;

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

	private static final String PARAM_DIFF_FIELD = "diffField";

	private static final String PARAM_SUGGEST = "suggest";

	private static final String PARAM_TARGET = "target";

	private static final String PARAM_COPY = "copy";

	private static final String PARAM_DEST_FIELD = "destField";

	private static final String DEEPL_API_URL = "https://api.deepl.com/v2/translate";
	private static final String GOOGLE_TRANSLATE_API_URL = "https://www.googleapis.com/language/translate/v2";
	private static final String DEEPL_AUTH_KEY = "auth_key";
	private static final String DEEPL_TEXT = "text";
	private static final String DEEPL_SOURCE_LANG = "source_lang";
	private static final String DEEPL_TARGET_LANG = "target_lang";
	private static final String GOOGLE_KEY = "key";
	private static final String GOOGLE_SOURCE = "source";
	private static final String GOOGLE_QUERY = "q";
	private static final String JSON_DATA = "data";
	private static final String JSON_TRANSLATIONS = "translations";
	private static final String JSON_TEXT = "text";
	private static final String JSON_TRANSLATED_TEXT = "translatedText";
	private static final String NO_SUGGESTION_MSG = "No suggestion available";
	private static final String NO_API_KEY_MSG = "No google translate API key provided";

	private String googleApiKey;

	private String deepLAPIKey;

	private ServiceRegistry serviceRegistry;
	
	private EntityFormatService entityFormatService;

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	/**
	 * <p>Setter for the field <code>entityFormatService</code>.</p>
	 *
	 * @param entityFormatService a {@link fr.becpg.repo.entity.EntityFormatService} object.
	 */
	public void setEntityFormatService(EntityFormatService entityFormatService) {
		this.entityFormatService = entityFormatService;
	}

	/**
	 * <p>Setter for the field <code>googleApiKey</code>.</p>
	 *
	 * @param googleApiKey a {@link java.lang.String} object.
	 */
	public void setGoogleApiKey(String googleApiKey) {
		this.googleApiKey = googleApiKey;
	}

	/**
	 * <p>Setter for the field <code>deepLAPIKey</code>.</p>
	 *
	 * @param deepLAPIKey a {@link java.lang.String} object
	 */
	public void setDeepLAPIKey(String deepLAPIKey) {
		this.deepLAPIKey = deepLAPIKey;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter(PARAM_NODEREF);

		boolean copy = "true".equals(req.getParameter(PARAM_COPY));

		String destFieldName = req.getParameter(PARAM_DEST_FIELD);
		String diffFieldName = req.getParameter(PARAM_DIFF_FIELD);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		NodeRef formNodeRef = null;
		QName fieldQname = null;
		QName destFieldQname = null;
		QName diffFieldQName = null;
		String itemType = null;
		String itemId = null;

		if (destFieldName != null) {
			destFieldName = destFieldName.replace("_", ":");
			destFieldQname = QName.createQName(destFieldName, serviceRegistry.getNamespaceService());
		}

		if (diffFieldName != null) {
			diffFieldName = diffFieldName.replace("_", ":");
			diffFieldQName = QName.createQName(diffFieldName, serviceRegistry.getNamespaceService());
		}

		if ((nodeRef != null) && !nodeRef.isEmpty()) {
			// If nodeRef contains '|', it's a versioned format: nodeRef|type|id
			if (nodeRef.contains("|")) {
				String[] parts = nodeRef.split("\\|");
				if (parts.length >= 3) {
					formNodeRef = new NodeRef(parts[0]);
					itemType = parts[1];
					itemId = parts[2];
				} else {
					throw new WebScriptException("Invalid versioned nodeRef format");
				}
			} else {
				formNodeRef = new NodeRef(nodeRef);
			}
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
				MLText diffMlText = null;

				Locale toSaveUnderLocale = MLTextHelper.getSupportedLocale(I18NUtil.getContentLocale());

				boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
				try {

					// If it's a version node, extract data from JSON
					if ((itemType != null) && (itemId != null)) {
						mlText = extractMLTextFromVersion(formNodeRef, itemType, itemId, fieldQname, toSaveUnderLocale);
						if (diffFieldQName != null) {
							diffMlText = extractMLTextFromVersion(formNodeRef, itemType, itemId, diffFieldQName, toSaveUnderLocale);
						}
					} else {
						Serializable value = serviceRegistry.getNodeService().getProperty(formNodeRef, fieldQname);
						if (value instanceof MLText mltext) {
							mlText = mltext;
						} else if (value instanceof String stVal) {
							mlText = new MLText();
							mlText.addValue(toSaveUnderLocale,stVal);
						} else {
							mlText = new MLText();
							mlText.addValue(toSaveUnderLocale, "");
						}

						if (diffFieldQName != null) {
							diffMlText = (MLText) serviceRegistry.getNodeService().getProperty(formNodeRef, diffFieldQName);
						}
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

					ret.put("translatedText", getTranslatedText(mlText, target));

				} else {

					JSONObject json = (JSONObject) req.parseContent();
					if (json != null) {
						// Only update if not a version node (version nodes are read-only)
						if ((itemType == null) && (itemId == null)) {
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

					}

					if (mlText != null) {
						if (copy && (itemType == null) && (itemId == null)) {
							serviceRegistry.getNodeService().setProperty(formNodeRef, destFieldQname, mlText);
						}

						JSONArray items = new JSONArray();
						for (Map.Entry<Locale, String> mlEntry : mlText.entrySet()) {
							JSONObject item = new JSONObject();
							item.put("label", propertyDef.getTitle(serviceRegistry.getDictionaryService()));
							item.put("description", propertyDef.getDescription(serviceRegistry.getDictionaryService()));
							item.put("name", fieldQname.toPrefixString(serviceRegistry.getNamespaceService()));

							if ((diffMlText != null) && diffMlText.containsKey(mlEntry.getKey())) {
								item.put("value",LargeTextHelper.htmlDiff(diffMlText.get(mlEntry.getKey()), mlEntry.getValue()));
							} else {
								item.put("value", mlEntry.getValue());
							}
							String lang = mlEntry.getKey().getLanguage();
							String code = lang;
							String country = lang.toUpperCase();
							if ((mlEntry.getKey().getCountry() != null) && !mlEntry.getKey().getCountry().isEmpty()) {
								country = mlEntry.getKey().getCountry();
								code += "_" + country;
							}
							item.put("locale", code);
							item.put("lang", lang);
							item.put("country", country);
							items.put(item);
						}

						ret.put("items", items);
						ret.put("currentLocale", MLTextHelper.localeKey(toSaveUnderLocale));

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
			if (logger.isDebugEnabled() && watch!=null) {
				watch.stop();
				logger.debug("MultilingualFieldWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

	private String getTranslatedText(MLText mlText, String target) {
		String language = I18NUtil.getContentLocale().getLanguage();

		String defaultValue = MLTextHelper.getClosestValue(mlText, MLTextHelper.getSupportedLocale(I18NUtil.getContentLocale()));

		if (target.split("_")[0].equals(language)) {
			return defaultValue;
		}

		if ((deepLAPIKey != null) && !deepLAPIKey.isEmpty()) {
			logger.debug("Try to translate : " + defaultValue + " in " + target + " using deepL");

			try {
				String url = DEEPL_API_URL;
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
				map.add(DEEPL_AUTH_KEY, deepLAPIKey);
				map.add(DEEPL_TEXT, defaultValue);
				map.add(DEEPL_SOURCE_LANG, language);
				map.add(DEEPL_TARGET_LANG, target.split("_")[0]);

				HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
				ResponseEntity<String> response = RestTemplateHelper.getRestTemplate().postForEntity(url, request, String.class);

				JSONObject jsonObject = new JSONObject(response.getBody());
				JSONArray translations = jsonObject.getJSONArray(JSON_TRANSLATIONS);

				if (translations.length() > 0) {
					return StringEscapeUtils.unescapeHtml4(translations.getJSONObject(0).getString(JSON_TEXT));
				}
			} catch (JSONException e) {
				logger.error("Fail to parse JSON", e);
			}

		} else if ((googleApiKey != null) && !googleApiKey.isEmpty()) {

			logger.debug("Try to translate : " + defaultValue + " in " + target + " using google");

			Map<String, String> vars = new HashMap<>();
			vars.put(GOOGLE_KEY, googleApiKey);
			vars.put(PARAM_TARGET, target.split("_")[0]);
			vars.put(GOOGLE_SOURCE, language);
			vars.put(GOOGLE_QUERY, defaultValue);

			String url = GOOGLE_TRANSLATE_API_URL + "?key={" + GOOGLE_KEY + "}&source={" + GOOGLE_SOURCE + "}&target={" + PARAM_TARGET + "}&q={" + GOOGLE_QUERY + "}";


			String ret = RestTemplateHelper.getRestTemplate().getForObject(url, String.class, vars);

			try {
				JSONObject jsonObject = new JSONObject(ret);
				if (jsonObject.has(JSON_DATA) && jsonObject.getJSONObject(JSON_DATA).has(JSON_TRANSLATIONS)) {
					JSONArray translations = jsonObject.getJSONObject(JSON_DATA).getJSONArray(JSON_TRANSLATIONS);
					if (translations.length() > 0) {
						return StringEscapeUtils.unescapeHtml4(translations.getJSONObject(0).getString(JSON_TRANSLATED_TEXT));
					}
				}

			} catch (JSONException e) {
				logger.error("Fail to parse JSON", e);

			}

		} else {
			logger.warn(NO_API_KEY_MSG);
		}
		return NO_SUGGESTION_MSG;
	}
	
	/**
	 * Extract MLText property from version JSON data.
	 *
	 * @param versionNodeRef the version node reference
	 * @param itemType the item type from versioned nodeRef
	 * @param itemId the item ID from versioned nodeRef
	 * @param propertyQName the property QName to extract
	 * @param defaultLocale the default locale to use if MLText is not found
	 * @return the extracted MLText or empty MLText if not found
	 */
	private MLText extractMLTextFromVersion(NodeRef versionNodeRef, String itemType, String itemId, QName propertyQName, Locale defaultLocale) {
		MLText mlText = new MLText();
		
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Extracting MLText from version node: " + versionNodeRef + " for item: " + itemId + " property: " + propertyQName);
			}

			String entityData = entityFormatService.getEntityData(versionNodeRef);

			if (entityData == null) {
				logger.warn("No entity data found for version node: " + versionNodeRef);
				return mlText;
			}

			JSONObject entityJson = new JSONObject(entityData);
			JSONObject entity = entityJson.getJSONObject("entity");
			JSONObject datalists = entity.getJSONObject("datalists");

			NamespaceService namespaceService = serviceRegistry.getNamespaceService();
			String propertyName = propertyQName.toPrefixString(namespaceService);

			// Find the datalist containing this item type
			for (String dataListKey : datalists.keySet()) {
				if (dataListKey.equals(itemType)) {
					JSONArray dataListArray = datalists.getJSONArray(dataListKey);

					// Find the item with matching ID
					for (int i = 0; i < dataListArray.length(); i++) {
						JSONObject item = dataListArray.getJSONObject(i);

						if (item.has("id") && itemId.equals(item.getString("id"))) {
							if (logger.isDebugEnabled()) {
								logger.debug("Found matching item in datalist");
							}

							JSONObject attributes = item.has("attributes") ? item.getJSONObject("attributes") : item;

							if (attributes.has(propertyName)) {
								Object value = attributes.get(propertyName);
								mlText.addValue(defaultLocale, value.toString());
							}

							for (String key : attributes.keySet()) {
								if (key.startsWith(propertyName + "_")) {
									String localePart = key.substring((propertyName + "_").length());
									mlText.addValue(MLTextHelper.parseLocale(localePart), attributes.getString(key));
								}
							}
							break;
						}
					}
				}
			}

		} catch (JSONException e) {
			logger.error("Failed to extract MLText from version JSON", e);
		}
		
		if (mlText.isEmpty()) {
			mlText.addValue(defaultLocale, "");
		}
		
		return mlText;
	}

}
