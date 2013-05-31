package fr.becpg.repo.report.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractor;

public abstract class AbstractEntityReportExtractor implements EntityReportExtractor {


	private static Log logger = LogFactory.getLog(AbstractEntityReportExtractor.class);
	
	/** The Constant TAG_ENTITY. */
	protected static final String TAG_ENTITY = "entity";

	/** The Constant TAG_DATALISTS. */
	protected static final String TAG_DATALISTS = "dataLists";
	protected static final String TAG_ATTRIBUTES = "attributes";
	protected static final String TAG_ATTRIBUTE = "attribute";
	protected static final String TAG_VERSIONS = "versions";
	protected static final String TAG_VERSION = "version";
	protected static final String ATTR_SET = "set";
	protected static final String ATTR_NAME = "name";
	protected static final String ATTR_VALUE = "value";
	
	/** The Constant VALUE_NULL. */
	protected static final String VALUE_NULL = "";
	
	private static final String VALUE_PERSON = "%s %s";
	private static final String REGEX_REMOVE_CHAR = "[^\\p{L}\\p{N}]";
	
	private static final String QUERY_XPATH_FORM_SETS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/set";
	private static final String QUERY_XPATH_FORM_FIELDS_BY_SET = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/field[@set=\"%s\"]";
	private static final String QUERY_XPATH_FORM_FIELDS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/field-visibility/show";
	private static final String QUERY_ATTR_GET_ID = "@id";
	private static final String QUERY_ATTR_GET_LABEL = "@label";
	private static final String SET_DEFAULT = "";
	
	protected static final String REPORT_FORM_CONFIG_PATH = "beCPG/birt/document/becpg-report-form-config.xml";

	protected DictionaryService dictionaryService;
	
	protected NamespaceService namespaceService;
	
	protected AttributeExtractorService attributeExtractorService;

	protected NodeService nodeService;
	
	protected EntityService entityService;

	protected VersionService versionService;

	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	
	/**
	 * @param dictionaryService the dictionaryService to set
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}


	/**
	 * @param namespaceService the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * @param attributeExtractorService the propertyService to set
	 */

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}



	/**
	 * @param entityService the entityService to set
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}


	@Override
	public EntityReportData extract(NodeRef entityNodeRef) {

		EntityReportData ret = new EntityReportData();

		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_ENTITY);

		// add attributes at <product/> tag
		Map<ClassAttributeDefinition, String> attributes = loadNodeAttributes(entityNodeRef);

		for (Map.Entry<ClassAttributeDefinition, String> attrKV : attributes.entrySet()) {

			Element cDATAElt = entityElt.addElement(attrKV.getKey().getName().getLocalName());
			cDATAElt.addCDATA(attrKV.getValue());			
		}

		// render target assocs (plants...special cases)
		loadTargetAssocs(entityNodeRef, entityElt);				
		
		// render data lists
		Element dataListsElt = entityElt.addElement(TAG_DATALISTS);
		loadDataLists(entityNodeRef, dataListsElt);
		
		// render versions
		loadVersions(entityNodeRef, entityElt);

		ret.setXmlDataSource(entityElt);
		ret.setDataObjects(extractImages(entityNodeRef, entityElt));

		return ret;
	}
	
	protected Map<String, byte[]> extractImages(NodeRef entityNodeRef, Element entityElt) {
		return null;
	}
	
	protected void loadTargetAssocs(NodeRef entityNodeRef, Element entityElt) {
	}
	
	protected void loadMultiLinesAttributes(Map.Entry<ClassAttributeDefinition, String> attrKV, Element entityElt) {
	}
	
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt) {
	}
	
	/**
	 * Load node attributes.
	 *
	 * @param nodeRef the node ref
	 * @param elt the elt
	 * @return the element
	 */
	protected Map<ClassAttributeDefinition, String> loadNodeAttributes(NodeRef nodeRef) {

		PropertyFormats propertyFormats = new PropertyFormats(true);
		Map<ClassAttributeDefinition, String> values = new HashMap<ClassAttributeDefinition, String>();		
		
		// properties
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

			// do not display system properties
			if(!(property.getKey().equals(ContentModel.PROP_NODE_REF) || 
			property.getKey().equals(ContentModel.PROP_NODE_DBID) ||
			property.getKey().equals(ContentModel.PROP_NODE_UUID) ||
			property.getKey().equals(ContentModel.PROP_STORE_IDENTIFIER) ||
			property.getKey().equals(ContentModel.PROP_STORE_NAME) ||
			property.getKey().equals(ContentModel.PROP_STORE_PROTOCOL) ||
			property.getKey().equals(ContentModel.PROP_CONTENT))){
			
				PropertyDefinition propertyDef =  dictionaryService.getProperty(property.getKey());
				if(propertyDef == null){
					logger.error("This property doesn't exist. Name: " + property.getKey());
					continue;
				}
				
				String value = VALUE_NULL;				
				if (property.getValue() != null) {
					
					value = attributeExtractorService.getStringValue(propertyDef, property.getValue(), propertyFormats);
				}			
				
				values.put(propertyDef, value);
			}			
		}		
		
		
		// associations
		Map<QName, String> tempValues = new HashMap<QName, String>();
		List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

		for (AssociationRef assocRef : associations) {

			QName qName = assocRef.getTypeQName();
			NodeRef targetNodeRef = assocRef.getTargetRef();
			QName targetQName = nodeService.getType(targetNodeRef);
			String name = "";			
			
			if(targetQName.equals(ContentModel.TYPE_PERSON)){
				name = String.format(VALUE_PERSON, (String)nodeService.getProperty(targetNodeRef, ContentModel.PROP_FIRSTNAME),
								(String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_LASTNAME));
			}
			else{
				name = (String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_NAME);
			}									

			if (tempValues.containsKey(qName)) {
				String names = tempValues.get(qName);
				names += RepoConsts.LABEL_SEPARATOR;
				names += name;
				tempValues.put(qName, names);
			} else {
				tempValues.put(qName, name);
			}
		}		
		
		for(Map.Entry<QName, String> tempValue : tempValues.entrySet()){
			AssociationDefinition associationDef =  dictionaryService.getAssociation(tempValue.getKey());
			if(associationDef == null){
				logger.error("This association doesn't exist. Name: " + tempValue.getKey());
				continue;
			}
			values.put(associationDef, tempValue.getValue());
		}
		
		return values;
	}	

	@SuppressWarnings("unchecked")
	protected Map<String, List<String>> getFieldsBySets(NodeRef nodeRef, String reportFormConfigPath){
				
		Map<String, List<String>> fieldsBySets = new LinkedHashMap<String, List<String>>();
		Document doc = null;
		try{
			ClassPathResource classPathResource = new ClassPathResource(reportFormConfigPath);
			
			SAXReader reader = new SAXReader();
			doc = reader.read(classPathResource.getInputStream());
		}
		catch(Exception e){
			logger.error("Failed to load file " + reportFormConfigPath, e);
			return fieldsBySets;
		}				
		
		// fields to show
		List<String> fields = new ArrayList<String>();
		QName nodeType = nodeService.getType(nodeRef);		
		String nodeTypeWithPrefix = nodeType.toPrefixString(namespaceService);
		
		List<Element> fieldElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_FIELDS, nodeTypeWithPrefix));		
		for(Element fieldElt : fieldElts){
			fields.add(fieldElt.valueOf(QUERY_ATTR_GET_ID));
		}				
		
		// sets to show
		List<Element> setElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_SETS, nodeTypeWithPrefix));		
		for(Element setElt : setElts){
						
			String setId = setElt.valueOf(QUERY_ATTR_GET_ID);
			String setLabel = setElt.valueOf(QUERY_ATTR_GET_LABEL);
			
			List<String> fieldsForSet = new ArrayList<String>(); 
			List<Element> fieldsForSetElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_FIELDS_BY_SET, nodeTypeWithPrefix, setId));			
			for(Element fieldElt : fieldsForSetElts){
				
				String fieldId = fieldElt.valueOf(QUERY_ATTR_GET_ID);						
				fieldsForSet.add(fieldId);
				fields.remove(fieldId);
			}

			fieldsBySets.put(setLabel, fieldsForSet);
		}
		
		// fields not associated to set
		fieldsBySets.put(SET_DEFAULT, fields);	
		
		return fieldsBySets;
	}

	protected String generateKeyAttribute(String attributeName){
		
		return attributeName.replaceAll(REGEX_REMOVE_CHAR, "").toLowerCase();
	}
	
	protected void loadVersions(NodeRef entityNodeRef, Element entityElt) {
		
		VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);		
		Element versionsElt = entityElt.addElement(TAG_VERSIONS);
		
		if(versionHistory!=null && versionHistory.getAllVersions() != null){
			
			for(Version version : versionHistory.getAllVersions()){
				Element versionElt = versionsElt.addElement(TAG_VERSION);
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_LABEL.getLocalName(), version.getVersionLabel());			
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_DESCRIPTION.getLocalName(), version.getDescription());
				versionElt.addAttribute(ContentModel.PROP_CREATOR.getLocalName(), attributeExtractorService.getPersonDisplayName(version.getFrozenModifier()));
				
				versionElt.addAttribute(ContentModel.PROP_CREATED.getLocalName(), 
						attributeExtractorService.getPropertyFormats().getDateFormat().format(version.getFrozenModifiedDate()));
			}
		}
		
	}	
	
	protected String extractNames(List<NodeRef> nodeRefs){
		String value = VALUE_NULL;
		for(NodeRef nodeRef : nodeRefs){
			if (!value.isEmpty()) {
				value += RepoConsts.LABEL_SEPARATOR;
			}
			value += (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		}		
		return value;
	}
	
	protected String extractTargetNames(List<AssociationRef> assocRefs){
		List<NodeRef> nodeRefs = new ArrayList<NodeRef>(assocRefs.size());
		for(AssociationRef assocRef : assocRefs){
			nodeRefs.add(assocRef.getTargetRef());
		}
		return extractNames(nodeRefs);
	}
	
	
}
