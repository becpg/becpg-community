package fr.becpg.repo.activity.extractor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormatService;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityExtractorService;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.behaviour.BehaviourRegistry;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.security.SecurityService;

@Service("entityActivityExtractorService")
public class EntityActivityExtractorServiceImpl implements EntityActivityExtractorService {

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private PropertyFormatService propertyFormatService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityActivityService entityActivityService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;
	
	private static final String ACTIVITYEVENT_UPDATE = "Update";
	
	private static final Log logger = LogFactory.getLog(EntityActivityExtractorServiceImpl.class);

	@Override
	public Object extractAuditActivityData(JSONObject auditActivityData, List<AttributeExtractorStructure> metadataFields) {

		Map<String, Object> ret = new HashMap<>(metadataFields.size());

		for (AttributeExtractorStructure metadataField : metadataFields) {
			ClassAttributeDefinition attributeDef = getFieldDef(BeCPGModel.TYPE_ACTIVITY_LIST, metadataField);

			if (auditActivityData.has(metadataField.getFieldName())) {
				Object value = auditActivityData.get(metadataField.getFieldName());
				
				HashMap<String, Object> tmp = new HashMap<>();
				
				if (attributeDef instanceof PropertyDefinition) {
					
					if (DataTypeDefinition.DATETIME.equals(((PropertyDefinition) attributeDef).getDataType().getName())) {
						
						Date date = ISO8601DateFormat.parse(value.toString());
						String displayName = attributeExtractorService.getStringValue((PropertyDefinition) attributeDef, date, propertyFormatService.getPropertyFormats(FormatMode.JSON, false));
						tmp.put("displayValue", displayName);
						value = date;
						
					} else {
						String displayName = attributeExtractorService.getStringValue((PropertyDefinition) attributeDef, value.toString(), propertyFormatService.getPropertyFormats(FormatMode.JSON, false));
						tmp.put("displayValue", displayName);
					}
					
					QName type = ((PropertyDefinition) attributeDef).getDataType().getName().getPrefixedQName(namespaceService);
					
					String metadata = entityDictionaryService.toPrefixString(type).split(":")[1];
					
					tmp.put("metadata", metadata);
					tmp.put("value", JsonHelper.formatValue(value));
					
					ret.put(metadataField.getFieldName(), tmp);
					
				}
			}
		}
		
		ret.put(ActivityAuditPlugin.PROP_BCPG_AL_USER_ID, extractPerson((String) auditActivityData.get(ActivityAuditPlugin.PROP_BCPG_AL_USER_ID)));

		JSONObject postLookup = entityActivityService.postActivityLookUp(ActivityType.valueOf((String) auditActivityData.get("prop_bcpg_alType")), (String) auditActivityData.get("prop_bcpg_alData"));
		
		if (postLookup != null) {
			try {
				formatPostLookup(postLookup);
			} catch (JSONException e) {
				logger.error(e, e);
			}
			ret.put("prop_bcpg_alData", postLookup);
		}

		return ret;
	}
	
	@Override
	public void formatPostLookup(JSONObject postLookup) {
		NodeRef entityNodeRef = null;
		NodeRef charactNodeRef = null;
		QName entityType = null;

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
						&& !BehaviourRegistry.shouldIgnoreActivityField(propertyName)) {
					// Property Title
					PropertyDefinition propertyDef = dictionaryService.getProperty(propertyName);
					ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(propertyName);
					if ((propDef != null) && (propDef.getTitle(dictionaryService) != null)
							&& (propDef.getTitle(dictionaryService).length() > 0)) {
						postProperty.put(AbstractDataListExtractor.PROP_TITLE, propDef.getTitle(dictionaryService));
					} else {
						postProperty.put(AbstractDataListExtractor.PROP_TITLE, propertyName.toPrefixString());
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
	}

	private Map<String, String> extractPerson(String person) {
		Map<String, String> ret = new HashMap<>(2);
		ret.put("value", person);
		ret.put("displayValue", attributeExtractorService.getPersonDisplayName(person));
		return ret;
	}

	private ClassAttributeDefinition getFieldDef(QName itemType, AttributeExtractorStructure field) {

		if (!field.getItemType().equals(itemType)) {
			return entityDictionaryService.findMatchingPropDef(field.getItemType(), itemType, field.getFieldQname());
		}
		return field.getFieldDef();
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
	 */
	private JSONArray checkProperty(JSONArray propertyArray, PropertyDefinition propertyDef) {
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
}
