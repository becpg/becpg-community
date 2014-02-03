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
package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.CSVPropertyFormats;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.security.SecurityService;

@Service("attributeExtractorService")
public class AttributeExtractorServiceImpl implements AttributeExtractorService,InitializingBean {

	private static Log logger = LogFactory.getLog(AttributeExtractorServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;
	
	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private DictionaryService dictionaryService;
	
	@Autowired
	private AssociationService associationService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private PersonService personService;

	@Autowired
	private TaggingService taggingService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private AttributeExtractorPlugin[] attributeExtractorPlugins;
	
	@Autowired
	private PersonAttributeExtractorPlugin personAttributeExtractorPlugin;
	
	private Map<QName, AttributeExtractorPlugin> pluginsCache = new HashMap<>();
	

	
	private AttributeExtractorPlugin getAttributeExtractorPlugin(QName type, NodeRef nodeRef) {
		
		return pluginsCache.get(type);
	}

	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		for(AttributeExtractorPlugin plugin  : attributeExtractorPlugins) {
			for(QName type : plugin.getMatchingTypes()) {
				pluginsCache.put(type, plugin);
			}
		} 
		
	}

	

	private PropertyFormats propertyFormats = new PropertyFormats(false);
	
	private PropertyFormats csvPropertyFormats = new CSVPropertyFormats(false);


	public class AttributeExtractorStructure {

		String fieldName;
		DataListCallBack callback;
		boolean isEntityField;
		ClassAttributeDefinition fieldDef;
		List<AttributeExtractorStructure> childrens;
		QName fieldQname;

		public AttributeExtractorStructure(String fieldName, QName fieldQname, List<String> dLFields, boolean isEntityField) {
			this.fieldName = fieldName;
			this.isEntityField = isEntityField;
			this.fieldQname = fieldQname;
			this.childrens = readExtractStructure(fieldQname, dLFields);
		}

		public AttributeExtractorStructure(String fieldName, ClassAttributeDefinition fieldDef) {
			this.fieldDef = fieldDef;
			this.fieldName = fieldName;
		}

		public AttributeExtractorStructure(String fieldName, QName fieldQname, ClassAttributeDefinition fieldDef, List<String> dLFields) {
			this.fieldName = fieldName;
			this.fieldDef = fieldDef;
			this.childrens = readExtractStructure(fieldQname, dLFields);
		}

		public String getFieldName() {
			return fieldName;
		}

		public DataListCallBack getCallback() {
			return callback;
		}

		public boolean isEntityField() {
			return isEntityField;
		}

		public boolean isDataListItems() {
			return isNested() && !isEntityField && fieldDef == null;
		}

		public boolean isNested() {
			return childrens != null && !childrens.isEmpty();
		}

		public ClassAttributeDefinition getFieldDef() {
			return fieldDef;
		}

		public List<AttributeExtractorStructure> getChildrens() {
			return childrens;
		}

		public QName getFieldQname() {
			return fieldQname;
		}

		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
			result = prime * result + ((fieldQname == null) ? 0 : fieldQname.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AttributeExtractorStructure other = (AttributeExtractorStructure) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			if (fieldQname == null) {
				if (other.fieldQname != null)
					return false;
			} else if (!fieldQname.equals(other.fieldQname))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "AttributeExtractorStructure [fieldName=" + fieldName + ", callback=" + callback + ", isEntityField=" + isEntityField + ", fieldDef=" + fieldDef
					+ ", childrens=" + childrens + ", fieldQname=" + fieldQname + "]";
		}

		private AttributeExtractorServiceImpl getOuterType() {
			return AttributeExtractorServiceImpl.this;
		}

	}
	
	
	@Override
	public PropertyFormats getPropertyFormats() {
		return propertyFormats;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getStringValue(PropertyDefinition propertyDef, Serializable v, PropertyFormats propertyFormats) {

		String value = null;
		
		if (v == null || propertyDef == null) {
			return value;
		}

		String dataType = propertyDef.getDataType().toString();

		if (dataType.equals(DataTypeDefinition.ASSOC_REF.toString())) {
			value = extractPropName((NodeRef) v);
		} else if (dataType.equals(DataTypeDefinition.CATEGORY.toString())) {

			List<NodeRef> categories = (ArrayList<NodeRef>) v;

			for (NodeRef categoryNodeRef : categories) {
				if (value == null) {
					value = extractPropName(categoryNodeRef);
				} else {
					value += RepoConsts.LABEL_SEPARATOR + extractPropName(categoryNodeRef);
				}
			}
		} else if (dataType.equals(DataTypeDefinition.BOOLEAN.toString()) || (dataType.equals(DataTypeDefinition.ANY.toString()) && (v instanceof Boolean))) {

			Boolean b = (Boolean) v;

			value = TranslateHelper.getTranslatedBoolean(b, propertyFormats.isUseDefaultLocale());

		} else if (dataType.equals(DataTypeDefinition.TEXT.toString())) {

			String constraintName = null;
			if (!propertyDef.getConstraints().isEmpty()) {

				for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
					if ("LIST".equals(constraint.getConstraint().getType())) {
						constraintName = constraint.getRef().toPrefixString(namespaceService).replace(":", "_");
						break;
					}
				}

			}

			if (propertyDef.isMultiValued()) {

				List<String> values = (List<String>) v;

				for (String tempValue : values) {
					if(tempValue!=null){
						if (value != null) {
							value += RepoConsts.LABEL_SEPARATOR;
						} else {
							value="";
						}
	
						value += constraintName != null ? TranslateHelper.getConstraint(constraintName, tempValue, propertyFormats.isUseDefaultLocale()) : tempValue;
					}

				}
			} else {
				value = constraintName != null ? TranslateHelper.getConstraint(constraintName, v.toString(), propertyFormats.isUseDefaultLocale()) : v.toString();
			}

		} else if (dataType.equals(DataTypeDefinition.DATE.toString())) {
			value = propertyFormats.getDateFormat().format(v);
		} else if (dataType.equals(DataTypeDefinition.DATETIME.toString())) {
			value = propertyFormats.getDatetimeFormat().format(v);
		} else if (dataType.equals(DataTypeDefinition.NODE_REF.toString())) {
			if (!propertyDef.isMultiValued()) {
				value = extractPropName((NodeRef) v);
			} else {
				List<NodeRef> values = (List<NodeRef>) v;

				for (NodeRef tempValue : values) {
					if (value != null) {
						value += RepoConsts.LABEL_SEPARATOR;
					} else {
						value = "";
					}

					value += extractPropName(tempValue);
				}
			}

		} else if (dataType.equals(DataTypeDefinition.MLTEXT.toString())) {

			value = v.toString();
		} else if (dataType.equals(DataTypeDefinition.DOUBLE.toString()) || dataType.equals(DataTypeDefinition.FLOAT.toString())
				|| (dataType.equals(DataTypeDefinition.ANY.toString()) && (v instanceof Double || v instanceof Float))) {

			if (propertyFormats.getDecimalFormat() != null) {
				value = propertyFormats.getDecimalFormat().format(v);
			} else {
				value = v.toString();
			}
		} else if (dataType.equals(DataTypeDefinition.QNAME.toString())) {
			
			if (v!=null) {
				value = I18NUtil.getMessage("bcpg_bcpgmodel.type."+((QName)v).toPrefixString(namespaceService).replace(":", "_")+".title");
				if(value==null) {
					value = v.toString();
				}
				
			} 
		}

		else {

			TypeConverter converter = new TypeConverter();
			value = converter.convert(propertyDef.getDataType(), v).toString();
		}

		return value;
	}

	@Override
	public List<AttributeExtractorStructure> readExtractStructure(QName itemType, List<String> metadataFields) {
		List<AttributeExtractorStructure> ret = new LinkedList<>();

		for (String field : metadataFields) {

			if (field.contains("|")) {
				StringTokenizer tokeniser = new StringTokenizer(field, "|");
				String dlField = tokeniser.nextToken();

				QName fieldQname = QName.createQName(dlField, namespaceService);

				List<String> dLFields = new ArrayList<String>();
				while (tokeniser.hasMoreTokens()) {
					dLFields.add(tokeniser.nextToken());
				}

				if (entityDictionaryService.isSubClass(fieldQname, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
					ret.add(new AttributeExtractorStructure("dt_" + dlField.replaceFirst(":", "_"), fieldQname, dLFields, false));

				} else if (entityDictionaryService.isSubClass(fieldQname, BeCPGModel.TYPE_ENTITY_V2)) {
					ret.add(new AttributeExtractorStructure("dt_" + dlField.replaceFirst(":", "_"), fieldQname, dLFields, true));
				} else {
					// nested assoc
					ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(fieldQname);
					if (hasReadAccess(itemType, dlField)) {
						if (isAssoc(propDef)) {
							ret.add(new AttributeExtractorStructure("dt_" + dlField.replaceFirst(":", "_"), ((AssociationDefinition) propDef).getTargetClass().getName(), propDef,
									dLFields));
						}
					}

				}

			} else {

				QName fieldQname = QName.createQName(field, namespaceService);

				if (hasReadAccess(itemType, field)) {

					ClassAttributeDefinition prodDef = entityDictionaryService.getPropDef(fieldQname);
					String prefix = "prop_";
					if (isAssoc(prodDef)) {
						prefix = "assoc_";
					}
					ret.add(new AttributeExtractorStructure(prefix + field.replaceFirst(":", "_"), prodDef));
				}
			}
		}
		return ret;
	}

	@Override
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<String> metadataFields, AttributeExtractorMode mode) {
		return extractNodeData(nodeRef, itemType, nodeService.getProperties(nodeRef), readExtractStructure(itemType, metadataFields), mode, null);
	}

	@Override
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, Map<QName, Serializable> properties, List<AttributeExtractorStructure> metadataFields,
			AttributeExtractorMode mode, AttributeExtractorService.DataListCallBack callback) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		Map<String, Object> ret = new HashMap<String, Object>(metadataFields.size());

		Integer order = 0;

		for (AttributeExtractorStructure field : metadataFields) {
			if (field.isNested()) {
				List<Map<String, Object>> extracted = callback.extractNestedField(nodeRef, field);

				if (AttributeExtractorMode.CSV.equals(mode) && !extracted.isEmpty()) {
					for (Map.Entry<String, Object> entry : extracted.get(0).entrySet()) {
						// Prefix with field name for CSV
						ret.put(field.getFieldName() + "_" + entry.getKey(), entry.getValue());
					}

				} else {
					if (field.isEntityField() && !extracted.isEmpty()) {
						ret.put(field.getFieldName(), callback.extractNestedField(nodeRef, field).get(0));
					} else {
						ret.put(field.getFieldName(), callback.extractNestedField(nodeRef, field));
					}
				}
			} else {
				ret.put(field.getFieldName(), extractNodeData(nodeRef, properties, field.getFieldDef(), mode, order++));
			}

		}
		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug(getClass().getSimpleName() + " extract node data in  " + watch.getTotalTimeSeconds());
		}
		return ret;
	}

	private boolean isAssoc(ClassAttributeDefinition propDef) {
		return propDef instanceof AssociationDefinition;
	}

	

	private Object extractNodeData(NodeRef nodeRef, Map<QName, Serializable> properties, ClassAttributeDefinition attribute, AttributeExtractorMode mode, int order) {

		Serializable value = null;
		String displayName = "";
		QName type = null;

		// property
		if (attribute instanceof PropertyDefinition) {

			value = properties.get(attribute.getName());
			displayName = getStringValue((PropertyDefinition) attribute, value,AttributeExtractorMode.CSV.equals(mode)? csvPropertyFormats :  propertyFormats);

			if (AttributeExtractorMode.CSV.equals(mode)) {
				return displayName;
			} else {
				HashMap<String, Object> tmp = new HashMap<String, Object>(6);

				type = ((PropertyDefinition) attribute).getDataType().getName().getPrefixedQName(namespaceService);

				if (AttributeExtractorMode.SEARCH.equals(mode)) {
					tmp.put("order", order);
					tmp.put("type", type);
					tmp.put("label", attribute.getTitle(dictionaryService));
				} else if (type != null) {
					tmp.put("metadata", extractMetadata(type, nodeRef));
				}
				if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
					tmp.put("version", properties.get(ContentModel.PROP_VERSION_LABEL));
				}
				tmp.put("displayValue", displayName);
				tmp.put("value", formatValue(value));

				return tmp;
			}

		}

		if (attribute instanceof AssociationDefinition) {// associations

			List<NodeRef> assocRefs = null;
			if (((AssociationDefinition) attribute).isChild()) {
				assocRefs = associationService.getChildAssocs(nodeRef, attribute.getName());
			} else {
				assocRefs = associationService.getTargetAssocs(nodeRef, attribute.getName());
			}

			if (AttributeExtractorMode.SEARCH.equals(mode)) {
				HashMap<String, Object> tmp = new HashMap<String, Object>(5);

				String nodeRefs = "";
				for (NodeRef assocNodeRef : assocRefs) {

					if (!displayName.isEmpty()) {
						displayName += RepoConsts.LABEL_SEPARATOR;
						nodeRefs += RepoConsts.LABEL_SEPARATOR;
					}

					type = nodeService.getType(assocNodeRef);
					displayName += extractPropName(type, assocNodeRef);
					nodeRefs += assocNodeRef.toString();
				}
				tmp.put("order", order);
				tmp.put("label", attribute.getTitle(dictionaryService));
				tmp.put("type", "subtype");
				tmp.put("displayValue", displayName);
				tmp.put("value", nodeRefs);
				return tmp;

			} else if (AttributeExtractorMode.CSV.equals(mode)) {
				String ret = "";
				for (NodeRef assocNodeRef : assocRefs) {
					type = nodeService.getType(assocNodeRef);
					if (ret.length() > 0) {
						ret += "|";
					}
					ret += extractPropName(type, assocNodeRef);
				}
				return ret;

			} else {
				List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>(assocRefs.size());
				for (NodeRef assocNodeRef : assocRefs) {
					Map<String, Object> tmp = new HashMap<String, Object>(5);

					type = nodeService.getType(assocNodeRef);

					tmp.put("metadata", extractMetadata(type, assocNodeRef));
					if (nodeService.hasAspect(assocNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
						tmp.put("version", nodeService.getProperty(assocNodeRef, ContentModel.PROP_VERSION_LABEL));
					}

					tmp.put("displayValue", extractPropName(type, assocNodeRef));
					tmp.put("value", assocNodeRef.toString());
					String siteId = extractSiteId(assocNodeRef);
					if (siteId != null) {
						tmp.put("siteId", siteId);
					}

					ret.add(tmp);
				}
				return ret;
			}
		}
		return null;
	}

	private Object formatValue(Serializable value) {
		if (value != null) {
			if (value instanceof Date) {
				return ISO8601DateFormat.format((Date) value);
			} else if (value instanceof Double) {
				Double d = (Double) value;
				if (d.isInfinite()) {
					return 0 == d.compareTo(Double.POSITIVE_INFINITY) ? "23456789012E777" : "-23456789012E777";
				}
			} else if (value instanceof Float) {
				Float f = (Float) value;
				if (f.isInfinite()) {
					return 0 == f.compareTo(Float.POSITIVE_INFINITY) ? "23456789012E777" : "-23456789012E777";
				}
			}
			return value;
		}
		return null;
	}

	private String extractPropName(NodeRef v) {
		QName type = nodeService.getType((NodeRef) v);
		return extractPropName(type, v);
	}

	
	private String extractPropName(QName type, NodeRef nodeRef) {
		String value = "";

		if (permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
			AttributeExtractorPlugin plugin = getAttributeExtractorPlugin(type, nodeRef);
			if(plugin != null) {
				value = plugin.extractPropName( type, nodeRef);
			} else {
				value = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			}
		} else {
			value = I18NUtil.getMessage("message.becpg.access.denied");
		}

		return value;
	}

	@Override
	public  String extractMetadata(QName type, NodeRef nodeRef) {
		
		String metadata = "";
		
		AttributeExtractorPlugin plugin = getAttributeExtractorPlugin(type, nodeRef);
		if(plugin != null) {
			metadata = plugin.extractMetadata( type, nodeRef);
		} else if (type.equals(ContentModel.TYPE_FOLDER)) {
			metadata = "container";
    	} else {
			metadata = type.toPrefixString(namespaceService).split(":")[1];
		}
		
		
		return metadata;
	}
	
	


	

	@Override
	public String convertDateValue(Serializable value) {
		if (value instanceof Date) {
			return formatDate((Date) value);
		}
		return null;
	}

	@Override
	public String formatDate(Date date) {
		return propertyFormats.getDateFormat().format(date);
	}

	

	@Override
	public String getDisplayPath(NodeRef nodeRef) {
		return this.nodeService.getPath(nodeRef).toDisplayPath(nodeService, permissionService);

	}

	@Override
	public String[] getTags(NodeRef nodeRef) {
		String[] result = null;
		List<String> tags = taggingService.getTags(nodeRef);
		if (tags == null || tags.isEmpty()) {
			result = new String[0];
		} else {
			result = (String[]) tags.toArray(new String[tags.size()]);
		}
		return result;
	}

	private boolean hasReadAccess(QName nodeType, String propName) {

		return securityService.computeAccessMode(nodeType, propName) != SecurityService.NONE_ACCESS;

	}

	@Override
	public String extractSiteId(NodeRef nodeRef) {
		String path = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
		if (SiteHelper.isSitePath(path)) {
			return SiteHelper.extractSiteId(path, getDisplayPath(nodeRef));
		}
		return null;
	}

	@Override
	public String extractPropertyForReport(PropertyDefinition propertyDef, Serializable value,
			PropertyFormats propertyFormats) {
		
		if (value != null) {
			if (value instanceof NodeRef || value instanceof String || value instanceof List) {
				return getStringValue(propertyDef, value, propertyFormats);
			}
			else if (value instanceof Date) {
				return ISO8601DateFormat.format((Date) value);
			} else {
				return value.toString();
			}								
		}
		else{
			return "";
		}		
	}

	@Override
	public String extractAssociationForReport(AssociationRef assocRef) {
		NodeRef targetNodeRef = assocRef.getTargetRef();
		QName targetQName = nodeService.getType(targetNodeRef);

		if (targetQName.equals(ContentModel.TYPE_PERSON)) {
			return String.format("%s %s",
					(String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_FIRSTNAME),
					(String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_LASTNAME));
		} else {
			return (String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_NAME);
		}
	}



	@Override
	public String getPersonDisplayName(String userId) {
		return personAttributeExtractorPlugin.getPersonDisplayName(userId);
	}	
}
