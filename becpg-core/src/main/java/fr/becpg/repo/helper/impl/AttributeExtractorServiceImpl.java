package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.security.SecurityService;

@Service
public class AttributeExtractorServiceImpl implements AttributeExtractorService {

	private static Log logger = LogFactory.getLog(AttributeExtractorServiceImpl.class);

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private BeCPGCacheService beCPGCacheService;

	private AssociationService associationService;

	private NamespaceService namespaceService;

	private PersonService personService;

	private TaggingService taggingService;

	private PermissionService permissionService;

	private SecurityService securityService;

	private PropertyFormats propertyFormats = new PropertyFormats(false);

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setTaggingService(TaggingService taggingService) {
		this.taggingService = taggingService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	
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

		public String getFieldName() {
			return fieldName;
		}

		public DataListCallBack getCallback() {
			return callback;
		}

		public boolean isEntityField() {
			return isEntityField;
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

			if (propertyDef.getName().equals(BeCPGModel.PROP_PRODUCT_STATE)) {

				value = TranslateHelper.getTranslatedSystemState(SystemState.getSystemState((String) v));
			}
			// translate constraints (not cm:name)
			else if (!propertyDef.getName().isMatch(ContentModel.PROP_NAME) && !propertyDef.getConstraints().isEmpty()) {

				value = TranslateHelper.getConstraint(propertyDef.getName(), v, propertyFormats.isUseDefaultLocale());
			} else if (propertyDef.isMultiValued()) {

				List<String> values = (List<String>) v;

				for (String tempValue : values) {

					if (value == null) {
						value = tempValue;
					} else {
						value += RepoConsts.LABEL_SEPARATOR + tempValue;
					}
				}
			} else {
				value = v.toString();
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
			if (v.equals(BeCPGModel.TYPE_COMPOLIST)) {
				value = I18NUtil.getMessage("bcpg_bcpgmodel.type.bcpg_compoList.title");
			} else if (v.equals(BeCPGModel.TYPE_PACKAGINGLIST)) {
				value = I18NUtil.getMessage("bcpg_bcpgmodel.type.bcpg_packaging.title");
			} else {
				value = v.toString();
			}
		}

		else {

			TypeConverter converter = new TypeConverter();
			value = converter.convert(propertyDef.getDataType(), v).toString();
		}

		return value;
	}


	@Override
	public List<AttributeExtractorStructure> readExtractStructure( QName itemType, List<String> metadataFields){
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

				if (isSubClass(fieldQname, BeCPGModel.TYPE_ENTITYLIST_ITEM) ) {
					ret.add(new AttributeExtractorStructure("dt_" + dlField.replaceFirst(":", "_"),fieldQname,dLFields,false));
					
				} else if (isSubClass(fieldQname, BeCPGModel.TYPE_ENTITY_V2) ) {
					ret.add(new AttributeExtractorStructure("dt_" + dlField.replaceFirst(":", "_"),fieldQname,dLFields,true));
			
				}

			} else {

				QName fieldQname = QName.createQName(field, namespaceService);

				if (hasReadAccess(itemType, field)) {

					ClassAttributeDefinition prodDef = getPropDef(fieldQname);
					String prefix = "prop_";
						if (isAssoc(prodDef)) {
							prefix = "assoc_";
						}
						ret.add(new AttributeExtractorStructure(prefix + field.replaceFirst(":", "_"),prodDef));
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
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, Map<QName, Serializable> properties, List<AttributeExtractorStructure> metadataFields, AttributeExtractorMode mode,
			AttributeExtractorService.DataListCallBack callback) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		Map<String, Object> ret = new HashMap<String, Object>(metadataFields.size());

		Integer order = 0;
		
	
		for (AttributeExtractorStructure field : metadataFields) {
			if(field.getChildrens()!=null){
				if(AttributeExtractorMode.CSV.equals(mode)) {
					if(field.isEntityField()){
						//put all directly on line
						ret.putAll(callback.extractEntityField(nodeRef, field.getFieldQname(), field.getChildrens()));
					}
					//ignore datalists for now
				} else {
					if(field.isEntityField()){
						ret.put(field.getFieldName(), callback.extractEntityField(nodeRef, field.getFieldQname(), field.getChildrens()));
					} else {
						ret.put(field.getFieldName(), callback.extractDataListField(nodeRef, field.getFieldQname(), field.getChildrens()));
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

	private ClassAttributeDefinition getPropDef(final QName fieldQname) {

		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), fieldQname.toString() + ".propDef",
				new BeCPGCacheDataProviderCallBack<ClassAttributeDefinition>() {
					public ClassAttributeDefinition getData() {
						ClassAttributeDefinition propDef = dictionaryService.getProperty(fieldQname);
						if (propDef == null) {
							propDef = dictionaryService.getAssociation(fieldQname);
						}

						return propDef;
					}
				});

	}

	private boolean isSubClass(final QName fieldQname, final QName typeEntitylistItem) {
		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), fieldQname.toString() + "_" + typeEntitylistItem.toString() + ".isSubClass",
				new BeCPGCacheDataProviderCallBack<Boolean>() {
					public Boolean getData() {
						return dictionaryService.isSubClass(fieldQname, typeEntitylistItem);
					}
				});
	}

	@Override
	public Collection<QName> getSubTypes(final QName typeQname) {
		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), typeQname.toString() + ".getSubTypes",
				new BeCPGCacheDataProviderCallBack<Collection<QName>>() {
					public Collection<QName> getData() {
						return dictionaryService.getSubTypes(typeQname, true);
					}
				});
	}

	private Object extractNodeData(NodeRef nodeRef, Map<QName, Serializable> properties, ClassAttributeDefinition attribute,AttributeExtractorMode mode, int order) {

		Serializable value = null;
		String displayName = "";
		QName type = null;

		// property
		if (attribute instanceof PropertyDefinition) {
			
			value = properties.get(attribute.getName());
			displayName = getStringValue((PropertyDefinition) attribute, value, propertyFormats);
			
			if (AttributeExtractorMode.CSV.equals(mode)) {
				return displayName;
			} else {
				HashMap<String, Object> tmp = new HashMap<String, Object>(6);
		
				type = ((PropertyDefinition) attribute).getDataType().getName().getPrefixedQName(namespaceService);
	
				if (AttributeExtractorMode.SEARCH.equals(mode)) {
					tmp.put("order", order);
					tmp.put("type", type);
					tmp.put("label", attribute.getTitle());
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
				tmp.put("label", attribute.getTitle());
				tmp.put("type", "subtype");
				tmp.put("displayValue", displayName);
				tmp.put("value", nodeRefs);
				return tmp;

			} else if (AttributeExtractorMode.CSV.equals(mode)) {
				String ret = "";
				for (NodeRef assocNodeRef : assocRefs) {
					type = nodeService.getType(assocNodeRef);
					if(ret.length()>0){
						ret+="|";
					}
					ret+= extractPropName(type, assocNodeRef);
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

	private String extractPropName(QName type, NodeRef nodeRef) {
		String value = "";

		if (permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
			value = (String) nodeService.getProperty(nodeRef, getPropName(type));
		} else {
			value = I18NUtil.getMessage("message.becpg.access.denied");
		}

		return value;
	}

	private String extractPropName(NodeRef v) {
		QName type = nodeService.getType((NodeRef) v);
		return extractPropName(type, v);
	}

	private QName getPropName(QName type) {
		if (type.equals(ContentModel.TYPE_PERSON)) {
			return ContentModel.PROP_USERNAME;
		} else if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
			return ContentModel.PROP_AUTHORITY_DISPLAY_NAME;
		} else if (type.equals(BeCPGModel.TYPE_LINKED_VALUE)) {
			return BeCPGModel.PROP_LKV_VALUE;
		} else if (type.equals(ProjectModel.TYPE_TASK_LIST)) {
			return ProjectModel.PROP_TL_TASK_NAME;
		}

		return ContentModel.PROP_NAME;
	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		String metadata = "";
		if (type.equals(ContentModel.TYPE_PERSON)) {
			metadata = getPersonDisplayName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
		} else if (type.equals(ContentModel.TYPE_FOLDER)) {
			metadata = "container";
		} else {
			metadata = type.toPrefixString(namespaceService).split(":")[1];
			if (isSubClass(type, BeCPGModel.TYPE_PRODUCT)) {
				metadata += "-" + nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_STATE);
			}
		}
		return metadata;
	}

	@Override
	public Serializable getProperty(NodeRef nodeRef, QName propName) {
		Serializable value = nodeService.getProperty(nodeRef, propName);

		if (value instanceof Date) {
			return (Serializable) formatDate((Date) value);
		}
		return value;
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
	public String getPersonDisplayName(final String userId) {
		if (userId == null) {
			return "";
		}
		if (userId.equalsIgnoreCase(AuthenticationUtil.getSystemUserName())) {
			return userId;
		}
		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), userId + ".person", new BeCPGCacheDataProviderCallBack<String>() {
			public String getData() {
				String displayName = "";
				NodeRef personNodeRef = personService.getPerson(userId);
				if (personNodeRef != null) {
					displayName = nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME) + " " + nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
				}
				return displayName;
			}
		});

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

}
