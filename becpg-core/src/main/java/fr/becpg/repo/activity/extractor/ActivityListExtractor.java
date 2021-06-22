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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
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

	static final String ACTIVITYEVENT_UPDATE = "Update";

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
			if(postLookup!=null) {
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
								&& (postLookup.has(EntityActivityService.PROP_DATALIST_TYPE) && (securityService.computeAccessMode(entityNodeRef, entityType,
										postLookup.getString(EntityActivityService.PROP_DATALIST_TYPE)) == SecurityService.NONE_ACCESS)))
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
										&& (securityService.computeAccessMode(entityNodeRef, entityType, propertyName) != SecurityService.NONE_ACCESS)) {
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
											
											Object afterProperty = activityProperty.get(EntityActivityService.AFTER);
											
											if ((afterProperty instanceof JSONArray) && (((JSONArray) afterProperty).length() > 0)) {
												adaptProperty((JSONArray) beforeProperty, (JSONArray) afterProperty);
											}
											
											postProperty.put(EntityActivityService.BEFORE, checkProperty((JSONArray) beforeProperty, propertyDef));
										} else {
											postProperty.put(EntityActivityService.BEFORE, beforeProperty);
										}
									}
									
									// AfterProperty
									if (activityProperty.has(EntityActivityService.AFTER)) {
										Object afterProperty = activityProperty.get(EntityActivityService.AFTER);
										if ((afterProperty instanceof JSONArray) && (((JSONArray) afterProperty).length() > 0)) {
											
											Object beforeProperty = activityProperty.get(EntityActivityService.BEFORE);
											
											if ((beforeProperty instanceof JSONArray) && (((JSONArray) beforeProperty).length() > 0)) {
												adaptProperty((JSONArray) afterProperty, (JSONArray) beforeProperty);
											}
											
											postProperty.put(EntityActivityService.AFTER, checkProperty((JSONArray) afterProperty, propertyDef));
										} else {
											postProperty.put(EntityActivityService.AFTER, afterProperty);
										}
									}
									postActivityProperties.put(postProperty);
								}
							}
							postLookup.put(EntityActivityService.PROP_PROPERTIES, postActivityProperties);
						}
					} catch (JSONException e) {
						logger.error(e, e);
					}
					ret.put("prop_bcpg_alData", postLookup);
				} else {
					try {
						if (postLookup.has("content")) {
							ret.put("prop_bcpg_alData", postLookup.get("content"));
						} else {
							ret.put("prop_bcpg_alData", "");
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

	private void adaptProperty(JSONArray propToAdapt, JSONArray propRef) throws JSONException {

		if (propToAdapt.get(0) == JSONObject.NULL && propRef.get(0) instanceof JSONArray) {

			JSONArray newArray = new JSONArray();

			for (int k = 0; k < ((JSONArray) propRef.get(0)).length(); k++) {
				newArray.put("");
			}

			propToAdapt.put(0, newArray);

		} else if (propToAdapt.get(0) instanceof JSONArray && propRef.get(0) instanceof JSONArray
				&& ((JSONArray) propToAdapt.get(0)).length() < ((JSONArray) propRef.get(0)).length()) {
			for (int k = 0; k < ((JSONArray) propRef.get(0)).length() - ((JSONArray) propToAdapt.get(0)).length(); k++) {
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
	 */
	public JSONArray checkProperty(JSONArray propertyArray, PropertyDefinition propertyDef) {
		boolean updateProperty = true;
		JSONArray postproperty = new JSONArray();
		for (int i = 0; i < propertyArray.length(); i++) {
			try {
				if (propertyArray.getString(i).contains("workspace")) {
					NodeRef nodeRef = null;
				 	String name = null;
					if (Pattern.matches("\\(.*,.*\\)", propertyArray.getString(i))) {
						String nodeRefString = propertyArray.getString(i).substring(propertyArray.getString(i).indexOf("(") + 1,
								propertyArray.getString(i).indexOf(","));
						nodeRef = new NodeRef(nodeRefString);
						name = propertyArray.getString(i).substring(propertyArray.getString(i).indexOf(",") + 1,
								propertyArray.getString(i).indexOf(")"));

					} else {
						nodeRef = new NodeRef(propertyArray.getString(i));
					}
					if (nodeService.exists(nodeRef)) {
						if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED) {
							if (propertyDef != null) {
								postproperty.put(attributeExtractorService.getStringValue(propertyDef, nodeRef,
										attributeExtractorService.getPropertyFormats(FormatMode.JSON, true)));
							} else {
								postproperty.put(attributeExtractorService.extractPropName(nodeRef));
							}
						} else {
							postproperty.put(I18NUtil.getMessage("message.becpg.access.denied"));
						}
					} else {
						if (name != null) {
							postproperty.put(name);
						}
					}
				} else {
					updateProperty = false;
					break;
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
		}
		if (updateProperty) {
			return postproperty;
		}
		return propertyArray;
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
