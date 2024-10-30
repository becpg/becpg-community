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
package fr.becpg.repo.activity.extractor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>ActivityListExtractor class.</p>
 *
 * @author matthieu Extract activity Fields
 * @version $Id: $Id
 */
public class ActivityListExtractor extends SimpleExtractor {

	private static Log logger = LogFactory.getLog(ActivityListExtractor.class);

	private EntityActivityService entityActivityService;

	private DictionaryService dictionaryService;

	private SecurityService securityService;

	private static final String ACTIVITYEVENT_UPDATE = "Update";
	private static final String PROP_BECPG_ALDATA = "prop_bcpg_alData";
	

	private static final Set<QName> isIgnoredTypes = new HashSet<>();

	/**
	 * <p>Setter for the field <code>entityActivityService</code>.</p>
	 *
	 * @param entityActivityService a {@link fr.becpg.repo.activity.EntityActivityService} object.
	 */
	public void setEntityActivityService(EntityActivityService entityActivityService) {
		this.entityActivityService = entityActivityService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public static void registerIgnoredType(QName type) {
		isIgnoredTypes.add(type);
	}

	/** {@inheritDoc} */
	@Override
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		Map<String, Boolean> sortMap = new LinkedHashMap<>();

		sortMap.put("@cm:created", false);

		dataListFilter.setSortMap(sortMap);

		return super.getListNodeRef(dataListFilter, pagination);
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields, FormatMode mode,
			Map<QName, Serializable> properties, Map<String, Object> props, Map<NodeRef, Map<String, Object>> cache) {
		Map<String, Object> ret = super.doExtract(nodeRef, itemType, metadataFields, mode, properties, props, cache);
		if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(itemType)) {
			postLookupActivity(nodeRef, ret, properties, mode);
		}
		return ret;

	}

	/**
	 * <p>postLookupActivity.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param ret a {@link java.util.Map} object.
	 * @param properties a {@link java.util.Map} object.
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 */
	protected void postLookupActivity(NodeRef nodeRef, Map<String, Object> ret, Map<QName, Serializable> properties, FormatMode mode) {

		String activityType = (String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_TYPE);
		if (activityType != null) {

			ret.put("prop_bcpg_alUserId", extractPerson((String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_USERID)));
			JSONObject postLookup = entityActivityService.postActivityLookUp(
					ActivityType.valueOf((String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_TYPE)),
					(String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_DATA));
			if (postLookup != null) {
				if (FormatMode.JSON.equals(mode) || FormatMode.XLSX.equals(mode)) {
					NodeRef entityNodeRef = null;
					NodeRef charactNodeRef = null;
					QName entityType = null;
					try {
						if (postLookup.has(EntityActivityService.PROP_ENTITY_NODEREF)
								&& nodeService.exists(new NodeRef(postLookup.getString(EntityActivityService.PROP_ENTITY_NODEREF)))) {
							entityNodeRef = new NodeRef(postLookup.getString(EntityActivityService.PROP_ENTITY_NODEREF));
						}

						if (postLookup.has(EntityActivityService.PROP_CHARACT_NODEREF)
								&& nodeService.exists(new NodeRef(postLookup.getString(EntityActivityService.PROP_CHARACT_NODEREF)))) {
							charactNodeRef = new NodeRef(postLookup.getString(EntityActivityService.PROP_CHARACT_NODEREF));
						}

						if (postLookup.has(EntityActivityService.PROP_ENTITY_TYPE)) {
							entityType = QName.createQName(postLookup.getString(EntityActivityService.PROP_ENTITY_TYPE));
						} else if (entityNodeRef != null) {
							entityType = nodeService.getType(entityNodeRef);
						}

						if (((entityType != null)
								&& (postLookup.has(EntityActivityService.PROP_DATALIST_TYPE) && (securityService.computeAccessMode(entityNodeRef,
										entityType, postLookup.getString(EntityActivityService.PROP_DATALIST_TYPE)) == SecurityService.NONE_ACCESS)))
								|| ((charactNodeRef != null) && (permissionService.hasPermission(charactNodeRef, "Read") != AccessStatus.ALLOWED))) {

							// Entity Title
							if (postLookup.has(EntityActivityService.PROP_TITLE)) {
								postLookup.put(EntityActivityService.PROP_TITLE, I18NUtil.getMessage("message.becpg.access.denied"));
							}
							if (postLookup.has(EntityActivityService.PROP_PROPERTIES)) {
								postLookup.remove(EntityActivityService.PROP_PROPERTIES);
							}

						} else if (postLookup.has("activityEvent") && postLookup.get("activityEvent").equals(ACTIVITYEVENT_UPDATE)
								&& postLookup.has(EntityActivityService.PROP_PROPERTIES)) {
							JSONArray activityProperties = postLookup.getJSONArray(EntityActivityService.PROP_PROPERTIES);
							JSONArray postActivityProperties = new JSONArray();
							for (int i = 0; i < activityProperties.length(); i++) {
								JSONObject activityProperty = activityProperties.getJSONObject(i);
								JSONObject postProperty = activityProperty;
								QName propertyName = QName.createQName(activityProperty.getString(EntityActivityService.PROP_TITLE));

								if ((entityType != null)
										&& (securityService.computeAccessMode(entityNodeRef, entityType, propertyName) != SecurityService.NONE_ACCESS)
										&& !isIgnoredTypes.contains(propertyName)) {
									// Property Title
									PropertyDefinition propertyDef = dictionaryService.getProperty(propertyName);
									ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(propertyName);
									if ((propDef != null) && (propDef.getTitle(dictionaryService) != null)
											&& (propDef.getTitle(dictionaryService).length() > 0)) {
										postProperty.put(PROP_TITLE, propDef.getTitle(dictionaryService));
									} else {
										postProperty.put(PROP_TITLE, propertyName.toPrefixString());
									}
									// Before Property
									if (activityProperty.has(EntityActivityService.BEFORE)) {
										Object beforeProperty = activityProperty.get(EntityActivityService.BEFORE);
										if ((beforeProperty instanceof JSONArray) && (((JSONArray) beforeProperty).length() > 0)) {

											if (activityProperty.has(EntityActivityService.AFTER)) {
												Object afterProperty = activityProperty.get(EntityActivityService.AFTER);

												if ((afterProperty instanceof JSONArray) && (((JSONArray) afterProperty).length() > 0)) {
													adaptProperty((JSONArray) beforeProperty, (JSONArray) afterProperty);
												}
											}

											postProperty.put(EntityActivityService.BEFORE, checkProperty((JSONArray) beforeProperty, propertyDef));
										} else {
											postProperty.put(EntityActivityService.BEFORE, toDisplayValue(beforeProperty, propertyDef));
										}
									}

									// AfterProperty
									if (activityProperty.has(EntityActivityService.AFTER)) {
										Object afterProperty = activityProperty.get(EntityActivityService.AFTER);
										if ((afterProperty instanceof JSONArray) && (((JSONArray) afterProperty).length() > 0)) {

											if (activityProperty.has(EntityActivityService.BEFORE)) {
												Object beforeProperty = activityProperty.get(EntityActivityService.BEFORE);

												if ((beforeProperty instanceof JSONArray) && (((JSONArray) beforeProperty).length() > 0)) {
													adaptProperty((JSONArray) afterProperty, (JSONArray) beforeProperty);
												}
											}

											postProperty.put(EntityActivityService.AFTER, checkProperty((JSONArray) afterProperty, propertyDef));
										} else {
											postProperty.put(EntityActivityService.AFTER, toDisplayValue(afterProperty, propertyDef));
										}
									}

									if (!postProperty.has(EntityActivityService.BEFORE) || !postProperty.has(EntityActivityService.AFTER)
											|| areStringsDifferent(postProperty.get(EntityActivityService.BEFORE),
													postProperty.get(EntityActivityService.AFTER))) {
										postActivityProperties.put(postProperty);
									}

								}
							}

							postLookup.put(EntityActivityService.PROP_PROPERTIES, postActivityProperties);

						}
					} catch (JSONException e) {
						logger.error(e, e);
					}
					ret.put(PROP_BECPG_ALDATA, postLookup);
				} else {
					try {
						if (postLookup.has("content")) {
							ret.put(PROP_BECPG_ALDATA, postLookup.get("content"));
						} else {
							ret.put(PROP_BECPG_ALDATA, "");
						}
					} catch (JSONException e) {
						logger.error(e, e);
					}
				}
			} else {
				logger.warn("No activity type for node :" + nodeRef);
			}
		}

	}

	private boolean areStringsDifferent(Object object, Object object2) {

		if ((object == null) && (object2 == null)) {
			return false;
		}

		if ((object == null) || (object2 == null)) {
			return true;
		}

		return !object.toString().equals(object2.toString());
	}

	private void adaptProperty(JSONArray propToAdapt, JSONArray propRef) throws JSONException {

		if ((propToAdapt.get(0) == JSONObject.NULL) && (propRef.get(0) instanceof JSONArray)) {

			JSONArray newArray = new JSONArray();

			for (int k = 0; k < ((JSONArray) propRef.get(0)).length(); k++) {
				newArray.put("");
			}

			propToAdapt.put(0, newArray);

		} else if ((propToAdapt.get(0) instanceof JSONArray) && (propRef.get(0) instanceof JSONArray)
				&& (((JSONArray) propToAdapt.get(0)).length() < ((JSONArray) propRef.get(0)).length())) {
			for (int k = 0; k < (((JSONArray) propRef.get(0)).length() - ((JSONArray) propToAdapt.get(0)).length()); k++) {
				((JSONArray) propToAdapt.get(0)).put("");
			}
		}
	}

	/**
	 * <p>checkProperty.</p>
	 *
	 * @param property a {@link java.lang.Object} object.
	 * @param propertyDef a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object.
	 * @return a {@link org.json.JSONArray} object.
	 * @throws JSONException 
	 */
	public JSONArray checkProperty(JSONArray propertyArray, PropertyDefinition propertyDef) throws JSONException {
		JSONArray postproperty = new JSONArray();
		for (int i = 0; i < propertyArray.length(); i++) {
			postproperty.put(toDisplayValue(propertyArray.get(i), propertyDef));
		}
		return postproperty;
	}
	
	private Object toDisplayValue(Object prop, PropertyDefinition propertyDef) {
		try {
			String stringVal = prop.toString();
			if (((propertyDef == null) && stringVal.contains("workspace"))
					|| ((propertyDef != null) && DataTypeDefinition.NODE_REF.equals(propertyDef.getDataType().getName()) && (stringVal != null)
							&& !stringVal.isBlank() && !"null".equals(stringVal)  && !"[\"\"]".equals(stringVal))) {
				NodeRef nodeRef = null;
				String name = null;
				if (Pattern.matches("\\(.*,.*\\)", stringVal)) {
					String nodeRefString = stringVal.substring(stringVal.indexOf("(") + 1, stringVal.indexOf(","));
					nodeRef = new NodeRef(nodeRefString);
					name = stringVal.substring(stringVal.indexOf(",") + 1, stringVal.indexOf(")"));
					
				} else {
					
					int lastForwardSlash = stringVal.lastIndexOf('/');
					
					// case of malformed activities
					if (lastForwardSlash == -1) {
						JSONObject jsonNodeRef = new JSONObject(stringVal);
						nodeRef = new NodeRef(jsonNodeRef.getJSONObject("storeRef").getString("protocol") + "://"
								+ jsonNodeRef.getJSONObject("storeRef").getString("identifier") + "/" + jsonNodeRef.getString("id"));
					} else {
						nodeRef = new NodeRef(stringVal);
					}
				}
				if (nodeService.exists(nodeRef)) {
					if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED) {
						if (propertyDef != null) {
							return attributeExtractorService.getStringValue(propertyDef, nodeRef, attributeExtractorService.getPropertyFormats(FormatMode.JSON, true));
						} else {
							return attributeExtractorService.extractPropName(nodeRef);
						}
					} else {
						return I18NUtil.getMessage("message.becpg.access.denied");
					}
				} else {
					if (name != null) {
						return name;
					}
				}
			} else {
				if ((prop instanceof String) && (propertyDef != null) && (DataTypeDefinition.DATE.equals(propertyDef.getDataType().getName())
						|| DataTypeDefinition.DATETIME.equals(propertyDef.getDataType().getName()))) {
					return extractDate((String) prop);
				}
			}
		} catch (JSONException | MalformedNodeRefException e) {
			logger.error(e, e);
		}
		return prop;
	}

	private Object extractDate(String prop) {
		try {
			return ISO8601DateFormat.parse(prop);
		} catch (AlfrescoRuntimeException e) {
			return prop;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataType() != null) && dataListFilter.getDataType().equals(BeCPGModel.TYPE_ACTIVITY_LIST);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
