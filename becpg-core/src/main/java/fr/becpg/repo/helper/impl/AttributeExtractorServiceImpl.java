package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.security.SecurityService;

public class AttributeExtractorServiceImpl implements AttributeExtractorService {

	private static Log logger = LogFactory.getLog(AttributeExtractorServiceImpl.class);

	private static final String PERSON_DISPLAY_CACHE = "fr.becpg.cache.personDisplayCache";

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private BeCPGCacheService beCPGCacheService;

	private NamespaceService namespaceService;

	private PersonService personService;

	private TaggingService taggingService;

	private PermissionService permissionService;

	private SecurityService securityService;

	private PropertyFormats propertyFormats = new PropertyFormats(false);

	/**
	 * @param securityService
	 *            the securityService to set
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param dictionaryService
	 *            the dictionaryService to set
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * @param beCPGCacheService
	 *            the beCPGCacheService to set
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	/**
	 * @param namespaceService
	 *            the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * @param personService
	 *            the personService to set
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * @param taggingService
	 *            the taggingService to set
	 */
	public void setTaggingService(TaggingService taggingService) {
		this.taggingService = taggingService;
	}

	/**
	 * @param permissionService
	 *            the permissionService to set
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
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
			value = (String) nodeService.getProperty((NodeRef) v, ContentModel.PROP_NAME);
		} else if (dataType.equals(DataTypeDefinition.CATEGORY.toString())) {

			List<NodeRef> categories = (ArrayList<NodeRef>) v;

			for (NodeRef categoryNodeRef : categories) {
				if (value == null) {
					value = (String) nodeService.getProperty(categoryNodeRef, ContentModel.PROP_NAME);
				} else {
					value += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(categoryNodeRef, ContentModel.PROP_NAME);
				}
			}
		} else if (dataType.equals(DataTypeDefinition.BOOLEAN.toString())
				|| (dataType.equals(DataTypeDefinition.ANY.toString())
				&& (v instanceof Boolean))) {

			Boolean b = (Boolean) v;

			value = TranslateHelper.getTranslatedBoolean(b, propertyFormats.isUseDefaultLocale());

		} else if (dataType.equals(DataTypeDefinition.TEXT.toString())) {
			
			if (propertyDef.getName().equals(BeCPGModel.PROP_PRODUCT_STATE)) {

				value = TranslateHelper.getTranslatedProductState(ProductData.getSystemState((String) v));
			}
			//translate constraints (not cm:name)
			else if (!propertyDef.getName().isMatch(ContentModel.PROP_NAME) && propertyDef.getConstraints().size()>0) {
				
				value = TranslateHelper.getConstraint(propertyDef.getName(),(String) v, propertyFormats.isUseDefaultLocale());
			}
			else if (propertyDef.isMultiValued()) {

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
			if(nodeService.getType((NodeRef) v).equals(BeCPGModel.TYPE_LINKED_VALUE)){
				value = (String) nodeService.getProperty((NodeRef) v,BeCPGModel.PROP_LKV_VALUE);
			} else {
				value = (String) nodeService.getProperty((NodeRef) v,ContentModel.PROP_NAME);
			}
		} else if (dataType.equals(DataTypeDefinition.MLTEXT.toString())) {

			value = v.toString();
		} else if (dataType.equals(DataTypeDefinition.DOUBLE.toString()) || dataType.equals(DataTypeDefinition.FLOAT.toString())
				|| (dataType.equals(DataTypeDefinition.ANY.toString())
						&& (v instanceof Double || v instanceof Float))) {

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
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<String> metadataFields, boolean isSearch) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		Map<String, Object> ret = new LinkedHashMap<String, Object>();

		TypeDefinition typeDef = dictionaryService.getType(itemType);
		Integer order = 0;
		for (String field : metadataFields) {
			QName fieldQname = QName.createQName(field, namespaceService);

			if (hasReadAccess(itemType, field)) {

				ClassAttributeDefinition propDef = getAttributeDef(fieldQname, typeDef);

				if (propDef == null) {
					for (QName aspectName : nodeService.getAspects(nodeRef)) {
						AspectDefinition aspectDefinition = dictionaryService.getAspect(aspectName);
						propDef = getAttributeDef(fieldQname, aspectDefinition);
						if (propDef != null) {
							break;
						}
					}
				}

				Object tmp = extractNodeData(nodeRef, propDef, isSearch, order++);

				if (tmp != null) {
					logger.debug("Extract field : " + field);
					if (isSearch) {
						ret.put(field, tmp);
					} else {
						String prefix = "prop_";
						if (isAssoc(propDef)) {
							prefix = "assoc_";
						}
						ret.put(prefix + field.replaceFirst(":", "_"), tmp);
					}
				}
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

	private ClassAttributeDefinition getAttributeDef(QName fieldQname, ClassDefinition typeDef) {
		if (typeDef != null) {
			PropertyDefinition propDef = typeDef.getProperties().get(fieldQname);
			if (propDef != null) {

				return propDef;

			} else {
				AssociationDefinition assocDef = typeDef.getAssociations().get(fieldQname);
				if (assocDef != null) {
					return assocDef;
				}
			}
		}
		return null;

	}

	
	
	
	private Object extractNodeData(NodeRef nodeRef, ClassAttributeDefinition attribute, boolean isSearch, int order) {
		Map<String, Object> tmp = new HashMap<String, Object>();
		if (isSearch) {
			tmp.put("order", order);
		}
		Serializable value = null;
		String displayName = "";
		QName type = null;

		// property
		if (attribute instanceof PropertyDefinition) {

			value = nodeService.getProperty(nodeRef, attribute.getName());
			displayName = getStringValue((PropertyDefinition) attribute, value, propertyFormats);
			type = ((PropertyDefinition) attribute).getDataType().getName().getPrefixedQName(namespaceService);

			if (isSearch) {
				tmp.put("type", type);
			} else if (type != null) {
				tmp.put("metadata", extractMetadata(type, nodeRef));
			}
			tmp.put("label", attribute.getTitle());
			tmp.put("displayValue", displayName);
			tmp.put("value", value);
			return tmp;

		} else if (attribute instanceof AssociationDefinition) {// associations

			List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, attribute.getName());
			if (isSearch) {
				
				for (AssociationRef assocRef : assocRefs) {

					if (!displayName.isEmpty()) {
						displayName += RepoConsts.LABEL_SEPARATOR;
					}

					displayName += (String) nodeService.getProperty(assocRef.getTargetRef(), ContentModel.PROP_NAME);
				}
				tmp.put("label", attribute.getTitle());
				tmp.put("type", "subtype");
				tmp.put("displayValue", displayName);
				tmp.put("value", displayName);
				return tmp;

			} else {
				List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
				for (AssociationRef assocRef : assocRefs) {
					tmp = new HashMap<String, Object>();

					type = nodeService.getType(assocRef.getTargetRef());
					if (type != null) {
						tmp.put("metadata", extractMetadata(type, assocRef.getTargetRef()));
					}
					tmp.put("displayValue", (String) nodeService.getProperty(assocRef.getTargetRef(), ContentModel.PROP_NAME));
					tmp.put("value", assocRef.getTargetRef().toString());
					
					String path = nodeService.getPath(assocRef.getTargetRef()).toPrefixString(namespaceService);
					if(SiteHelper.isSitePath(path)){
						String siteId = SiteHelper.extractSiteId(path, getDisplayPath(nodeRef));
						
						if(siteId!=null){
							tmp.put("siteId", siteId);
						}
					}
					
					ret.add(tmp);
				}
				return ret;
			}

		}

		return null;
	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		String metadata = "";
		if (type.equals(ContentModel.TYPE_PERSON)) {
			metadata = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
		} else if (type.equals(ContentModel.TYPE_FOLDER)) {
			metadata = "container";
		} else {
			metadata = type.toPrefixString(namespaceService).split(":")[1];
			if (dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCT)) {
				metadata += "-" + nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_STATE);
			}
		}		
		return metadata;
	}

	@Override
	public Serializable getProperty(NodeRef nodeRef, QName propName) {
		Serializable value = this.nodeService.getProperty(nodeRef, propName);
		if (value instanceof Date) {
			value = formatDate((Date) value);

		}
		return value;
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
		return beCPGCacheService.getFromUserCache(PERSON_DISPLAY_CACHE, userId, new BeCPGCacheDataProviderCallBack<String>() {
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
		if (tags.isEmpty() == true) {
			result = new String[0];
		} else {
			result = (String[]) tags.toArray(new String[tags.size()]);
		}
		return result;
	}

	private boolean hasReadAccess(QName nodeType, String propName) {

		return securityService.computeAccessMode(nodeType, propName) != SecurityService.NONE_ACCESS;

	}

}
