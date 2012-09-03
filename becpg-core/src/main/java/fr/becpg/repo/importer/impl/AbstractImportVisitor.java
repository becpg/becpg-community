/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.config.mapping.MappingException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.extractor.RemoteHelper;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Abstract class used to import a node with its attributes and files.
 *
 * @author querephi
 */
public class AbstractImportVisitor  implements ImportVisitor, ApplicationContextAware  {

	// we don't know where is the node ? product may be in the Products folder or in the sites or somewhere else !
	protected static final String QUERY_NODE_BY_TYPE = " +TYPE:\"%s\"";	
	
	/** The Constant QUERY_XPATH_MAPPING. */
	protected static final String QUERY_XPATH_MAPPING = "mapping";
	
	/** The Constant QUERY_XPATH_DATE_FORMAT. */
	protected static final String QUERY_XPATH_DATE_FORMAT = "settings/setting[@id='dateFormat']/@value";
	
	protected static final String QUERY_XPATH_DATETIME_FORMAT = "settings/setting[@id='datetimeFormat']/@value";
	
	protected static final String QUERY_XPATH_DECIMAL_PATTERN = "settings/setting[@id='decimalPattern']/@value";
	
	/** The Constant QUERY_XPATH_NODE_COLUMN_KEY. */
	protected static final String QUERY_XPATH_NODE_COLUMN_KEY = "nodeColumnKeys/nodeColumnKey";
	
	/** The Constant QUERY_XPATH_DATALIST_COLUMN_KEY. */
	protected static final String QUERY_XPATH_DATALIST_COLUMN_KEY = "dataListColumnKeys/dataListColumnKey";	
	
	/** The Constant QUERY_XPATH_COLUMNS_ATTRIBUTE. */
	protected static final String QUERY_XPATH_COLUMNS_ATTRIBUTE = "columns/column[@type='Attribute']";
	
	/** The Constant QUERY_XPATH_COLUMNS_DATALIST. */
	protected static final String QUERY_XPATH_COLUMNS_DATALIST = "columns/column[@type='Characteristic']"; // productLists, projectLists and more...
	
	/** The Constant QUERY_XPATH_COLUMNS_FILE. */
	protected static final String QUERY_XPATH_COLUMNS_FILE = "columns/column[@type='File']";
	
	/** The Constant QUERY_ATTR_GET_ID. */
	protected static final String QUERY_ATTR_GET_ID = "@id";
	
	/** The Constant QUERY_ATTR_GET_ATTRIBUTE. */
	protected static final String QUERY_ATTR_GET_ATTRIBUTE = "@attribute";
	
	/** The Constant QUERY_ATTR_GET_NAME. */
	protected static final String QUERY_ATTR_GET_NAME = "@name";
	
	/** The Constant QUERY_ATTR_GET_DATALIST_QNAME. */
	protected static final String QUERY_ATTR_GET_DATALIST_QNAME = "@dataListQName";
	
	/** The Constant QUERY_ATTR_GET_PATH. */
	protected static final String QUERY_ATTR_GET_PATH = "@path";
	
	/** The Constant QUERY_ATTR_GET_CHARACT_QNAME. */
	protected static final String QUERY_ATTR_GET_CHARACT_QNAME = "@charactQName";
	
	/** The Constant QUERY_ATTR_GET_CHARACT_NODE_REF. */
	protected static final String QUERY_ATTR_GET_CHARACT_NODE_REF = "@charactNodeRef";
	
	/** The Constant QUERY_ATTR_GET_CHARACT_NAME. */
	protected static final String QUERY_ATTR_GET_CHARACT_NAME = "@charactName";
	
	protected static final String CACHE_KEY = "cKey%s-%s";
	
	protected static final String MSG_ERROR_LOAD_FILE = "import_service.error.err_load_file";
	protected static final String MSG_ERROR_FILE_NOT_FOUND = "import_service.error.err_file_not_found";
	protected static final String MSG_ERROR_MAPPING_ATTR_FAILED = "import_service.error.err_mapping_attr_failed";
	protected static final String MSG_ERROR_GET_OR_CREATE_NODEREF = "import_service.error.err_get_or_create_noderef";
	protected static final String MSG_ERROR_GET_NODEREF_CHARACT = "import_service.error.err_get_noderef_charact";
	protected static final String MSG_ERROR_UNDEFINED_CHARACT = "import_service.error.err_undefined_charact";
	protected static final String MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING = "import_service.error.err_columns_do_not_respect_mapping";
	protected static final String MSG_ERROR_TARGET_ASSOC_NOT_FOUND = "import_service.error.err_target_assoc_not_found";
	protected static final String MSG_ERROR_TARGET_ASSOC_SEVERAL = "import_service.error.err_target_assoc_several";
	protected static final String MSG_ERROR_GET_ASSOC_TARGET = "import_service.error.err_get_assoc_target";	
		
	private static Log logger = LogFactory.getLog(AbstractImportVisitor.class);
	
	protected NodeService nodeService;
	
	protected BeCPGSearchService beCPGSearchService;
	
	protected DictionaryService dictionaryService;
	
	protected RepoService repoService;
	
	protected ContentService contentService;
	
	protected MimetypeService mimetypeService;
	
	protected NamespaceService namespaceService;
	
	protected ApplicationContext applicationContext;
	
	protected EntityListDAO entityListDAO;	
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}


	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	

	/**
	 * @param beCPGSearchService
	 */
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}



	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	
	/**
	 * Sets the repo service.
	 *
	 * @param repoService the new repo service
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	
	/**
	 * Sets the content service.
	 *
	 * @param contentService the new content service
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}
	
	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}	
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.importer.ImportVisitor#importNode(fr.becpg.repo.importer.ImportContext, java.util.List)
	 */
	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException {
		
		logger.debug("ImportNode. type: " + importContext.getType());
				  						 
		// import properties		
		Map<QName, Serializable> properties = getNodePropertiesToImport(importContext, values); 				 		
		 
		NodeRef nodeRef = findNode(importContext, importContext.getType(), properties);		 
		 
		if(nodeRef == null){
			String name = (String)properties.get(ContentModel.PROP_NAME);
			if(logger.isDebugEnabled()){
				logger.debug("create node. Type: " + importContext.getType() + " - Properties: " + properties);				
			}
			QName assocName = ContentModel.ASSOC_CHILDREN;
			if(name != null && name.length()>0){
				assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));
			}
			nodeRef = nodeService.createNode(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS,
					assocName, importContext.getType(), properties).getChildRef();			 			
		}
		else if(importContext.isDoUpdate()){
			
			if(logger.isDebugEnabled()){
				logger.debug("update node. Properties: " + properties);
			}				
			nodeService.setType(nodeRef, importContext.getType());
			 
			for(Map.Entry<QName, Serializable> entry : properties.entrySet()){
				nodeService.setProperty(nodeRef, entry.getKey(), entry.getValue()); 
			}			
		}
		else{
			logger.info("Update mode is not enabled so no update is done.");
		}
		 
		// import associations	
		logger.debug("Import Assocs");
	 	importAssociations(importContext, values, nodeRef);
	 	
	 	// import files
	 	logger.debug("Import Files");
	 	importFiles(importContext, values, nodeRef);
	 	
	 	logger.debug("Node Imported");
	 	
	 	return nodeRef;
	}
	
	/**
	 * Calculate the properties of the node import
	 * @param importContext
	 * @param values
	 * @return
	 * @throws ParseException
	 * @throws ImporterException 
	 */
	protected Map<QName, Serializable> getNodePropertiesToImport(ImportContext importContext, List<String> values) throws ParseException, ImporterException{
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
		
		for(int z_idx=0; z_idx<values.size() && z_idx < importContext.getColumns().size(); z_idx++){
			 
			 AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);
			 
			 if(attributeMapping instanceof AttributeMapping){
				 ClassAttributeDefinition column = attributeMapping.getAttribute();
				 				 
				 if(column instanceof PropertyDefinition){	
					 PropertyDefinition propDef = (PropertyDefinition)column;
					 Serializable value = null;
					 QName dataType = propDef.getDataType().getName();
					 if(dataType.isMatch(DataTypeDefinition.NODE_REF)){
						 value = findPropertyTargetNodeByValue(importContext, propDef, values.get(z_idx), properties);
					 } else {
						 value = ImportHelper.loadPropertyValue(importContext, values, z_idx);			
					 }
					 
					 if(value != null){
						 properties.put(column.getName(), value);
					 }						
				 } 
			 }			 			
		 }
		
		return properties;
	}



	/**
	 * Import the associations of the node
	 * @param importContext
	 * @param values
	 * @param nodeRef
	 * @throws InvalidTargetNodeException 
	 * @throws ImporterException 
	 */
	protected void importAssociations(ImportContext importContext, List<String> values, NodeRef nodeRef) throws ImporterException{
		
		for(int z_idx=0; z_idx<values.size() && z_idx < importContext.getColumns().size(); z_idx++){
 	 		
			 AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);			 			 
			 
			 if(attributeMapping instanceof AbstractAttributeMapping){
				 ClassAttributeDefinition column = attributeMapping.getAttribute();
				 
				 if(column instanceof AssociationDefinition){					 
					 AssociationDefinition assocDef = (AssociationDefinition)column; 					
					 String value = values.get(z_idx);					
					 
					 List<NodeRef> targetRefs = findTargetNodesByValue(importContext, assocDef, value);		
					 
					 // mandatory target not found
					 if(assocDef.isTargetMandatory() && targetRefs.isEmpty()){						 
						 throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_ASSOC_TARGET, assocDef.getName(), value));
					 }
					 
					 // remove associations if needed
					 List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
					 for(AssociationRef assocRef : assocRefs){
						 NodeRef targetRef = assocRef.getTargetRef();
						 if(targetRefs.contains(targetRef)){
							 targetRefs.remove(targetRef);								 
						 }
						 else{								 
							 nodeService.removeAssociation(nodeRef, targetRef, assocDef.getName());
						 }
					 }
					 
					 // add new associations, the rest
					 for(NodeRef targetRef : targetRefs){
						 nodeService.createAssociation(nodeRef, targetRef, assocDef.getName());
					 }						 
				 }					 
			 } 
		 }	
	}
	
	/**
	 * Import the files of the node
	 * @param importContext
	 * @param values
	 * @param nodeRef
	 * @throws ImporterException 
	 */
	protected void importFiles(ImportContext importContext, List<String> values, NodeRef nodeRef) throws ParseException, ImporterException{
		
		/*
	 	 * import files
	 	 * 
	 	 * 		- get the parent of the node (folder, productFolder, supplierFolder), folder may have been initialized by a policy
	 	 * 		- get the targetFolder where files will be stored
	 	 */
	 	NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
	 	NodeRef targetFolderNodeRef = parentNodeRef; 
	 	String fileName = "";
	 	List<String> path = new ArrayList<String>();
		for(int z_idx=0; z_idx<values.size() && z_idx < importContext.getColumns().size(); z_idx++){
			 
			 AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);
			 
			 if(attributeMapping instanceof FileMapping){
				 				 	
				 // look for parent
				 FileMapping fileMapping = (FileMapping)attributeMapping;			 
				 QName contentQName = (fileMapping.getAttribute() instanceof PropertyDefinition) ? fileMapping.getAttribute().getName():ContentModel.PROP_CONTENT;
				 
				 // do not reload the same folder several times
				 if(!path.equals(fileMapping.getPath())){		
					 
					 path = fileMapping.getPath();
					 
					 if(fileMapping.getPath().size() > 1){
						 						 
						 fileName = fileMapping.getPath().get(fileMapping.getPath().size()-1);
						 
						 // remove the last path since it is the fileName
						 List<String> pathFolders = new ArrayList<String>();
						 for(int cntPath=0 ; cntPath<fileMapping.getPath().size()-1 ; cntPath++){
							 pathFolders.add(fileMapping.getPath().get(cntPath));
						 }
						 
						 logger.debug("creates folders" + pathFolders);
						 targetFolderNodeRef = repoService.createFolderByPaths(parentNodeRef, pathFolders);
					 }
					 else{
						 targetFolderNodeRef = nodeRef;
					 }
				 }
				 
				 String value = values.get(z_idx);
				 if(!value.isEmpty()){					 
					 
					 // create file if it doesn't exist
					 NodeRef fileNodeRef = nodeService.getChildByName(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
					 if(fileNodeRef == null){
						 Map<QName, Serializable> fileProperties = new HashMap<QName, Serializable>();
						 fileProperties.put(ContentModel.PROP_NAME, fileName);
						 fileNodeRef = nodeService.createNode(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileMapping.getId()), ContentModel.TYPE_CONTENT, fileProperties).getChildRef();
					 }
				
					 // add file content
					 if(fileMapping.getAttribute().getName().equals(ContentModel.PROP_CONTENT)){						 	
						InputStream in = null;
						try {
						 if(value!=null && 
								 (value.startsWith("classpath:")
								 || value.startsWith("file:")
								 || value.startsWith("http:")
							     || value.startsWith("ftp:"))){
				
							try{
								Resource resource = applicationContext.getResource(value);
								in = resource.getInputStream();
							}
							catch (IOException e) {
								throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_LOAD_FILE, value));
							}		
						
						 }else if(new File(value).exists()){			
							try{
								in = new FileInputStream(value);	    		
							}
							catch(FileNotFoundException e){
								throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_LOAD_FILE, value));
							}							
						 }
						 else{							 
							 throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_FILE_NOT_FOUND, value));
						 }
						 
						 if(in !=null){
							    String mimetype = mimetypeService.guessMimetype(value);
								ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
						        Charset charset = charsetFinder.getCharset(in, mimetype);
						        String encoding = charset.name();
		
						        ContentWriter writer = contentService.getWriter(fileNodeRef, contentQName, true);
						        writer.setMimetype(mimetype);
						    	writer.setEncoding(encoding);
						    	writer.putContent(in);
						 }
						 
						} finally {
							IOUtils.closeQuietly(in);
						}
					 }
					 // manage only properties
					 else if(fileMapping.getAttribute() instanceof PropertyDefinition){
						 
						PropertyDefinition propertyDefinition = (PropertyDefinition)fileMapping.getAttribute();
	                 	
						nodeService.setProperty(fileNodeRef, propertyDefinition.getName(),
												ImportHelper.loadPropertyValue(importContext, values, z_idx));
					 }
				 }				 		 				
			 }			 			
		 }	
	}
	
	/**
	 * Load the mapping of each class.
	 *
	 * @param mappingsElt the mappings elt
	 * @param importContext the import context
	 * @return the import context
	 * @throws ImporterException the be cpg exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ImportContext loadClassMapping(Element mappingsElt, ImportContext importContext) throws MappingException {						
				
		List<Node> mappingNodes = mappingsElt.selectNodes(QUERY_XPATH_MAPPING);
		
		Node dateFormat = mappingsElt.selectSingleNode(QUERY_XPATH_DATE_FORMAT);
		if(dateFormat != null){
			importContext.getPropertyFormats().setDateFormat(new SimpleDateFormat(dateFormat.getStringValue()));
		}
		
		Node datetimeFormat = mappingsElt.selectSingleNode(QUERY_XPATH_DATETIME_FORMAT);
		if(datetimeFormat != null){
			importContext.getPropertyFormats().setDateFormat(new SimpleDateFormat(datetimeFormat.getStringValue()));
		}
		
		Node decimalFormatPattern = mappingsElt.selectSingleNode(QUERY_XPATH_DECIMAL_PATTERN);
		if(decimalFormatPattern != null){
			importContext.getPropertyFormats().getDecimalFormat().applyPattern(decimalFormatPattern.getStringValue());			
		}
						
		for(Node mappingNode : mappingNodes){
			
			QName typeQName = QName.createQName(mappingNode.valueOf(QUERY_ATTR_GET_NAME), namespaceService);
			ClassMapping classMapping = new ClassMapping();
			classMapping.setType(typeQName);
			importContext.getClassMappings().put(typeQName, classMapping);
			
			// node keys
			List<Node> nodeColumnKeyNodes = mappingNode.selectNodes(QUERY_XPATH_NODE_COLUMN_KEY);
			for(Node columnNode : nodeColumnKeyNodes){
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
				
				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if(attributeDef == null){
				
					attributeDef = dictionaryService.getAssociation(attribute);
					if(attributeDef == null){						
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}
				
				//classMapping.getNodeColumnKeys().add(new KeyAttributeMapping(id, attributeDef, classQName));
				classMapping.getNodeColumnKeys().add(attribute);
			}
			

			// productlist keys
			List<Node>  dataListColumnKeyNodes = mappingNode.selectNodes(QUERY_XPATH_DATALIST_COLUMN_KEY);
			for(Node columnNode : dataListColumnKeyNodes){
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
				
				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if(attributeDef == null){
				
					attributeDef = dictionaryService.getAssociation(attribute);
					if(attributeDef == null){				
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}
				
				//classMapping.getDataListColumnKeys().add(new KeyAttributeMapping(id, attributeDef, classQName));
				classMapping.getDataListColumnKeys().add(attribute);
			}
			
			// attributes
			List<Node> columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_ATTRIBUTE);
			for(Node columnNode : columnNodes){
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
				
				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if(attributeDef == null){
				
					attributeDef = dictionaryService.getAssociation(attribute);					
					if(attributeDef == null){						
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}
								
				AbstractAttributeMapping attributeMapping = new AttributeMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef);
				classMapping.getColumns().add(attributeMapping);
			}									
			
			// characteristics
			columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_DATALIST);
			for(Node columnNode : columnNodes){
				QName qName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
				QName dataListQName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_DATALIST_QNAME), namespaceService);
				NodeRef charactNodeRef = null;
				String charactNodeRefString = columnNode.valueOf(QUERY_ATTR_GET_CHARACT_NODE_REF);
				String charactName = columnNode.valueOf(QUERY_ATTR_GET_CHARACT_NAME);
				QName charactQName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_CHARACT_QNAME), namespaceService);
				
				// get characteristic nodeRef
				if(charactNodeRefString != null && !charactNodeRefString.isEmpty() && NodeRef.isNodeRef(charactNodeRefString)){
	    			charactNodeRef = new NodeRef(charactNodeRefString);
	    		}				
				else if(!charactName.isEmpty()){					
					AssociationDefinition assocDef = dictionaryService.getAssociation(charactQName);
    				charactNodeRef = getItemByTypeAndName(assocDef.getTargetClass().getName(), charactName);


    				if(charactNodeRef == null){
    					throw new MappingException(I18NUtil.getMessage(MSG_ERROR_GET_NODEREF_CHARACT, assocDef.getTargetClass().getName(), charactName));
    				}
				}
				else{
					throw new MappingException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_CHARACT, columnNode.asXML()));
				}
				
				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(qName);
				if(attributeDef == null){
					attributeDef = dictionaryService.getAssociation(qName);
				}							
								
				CharacteristicMapping attributeMapping = new CharacteristicMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef, dataListQName, charactQName, charactNodeRef);
				classMapping.getColumns().add(attributeMapping);
			}
			
			// file import
			columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_FILE);
			for(Node columnNode : columnNodes){
				QName qName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
				
				String path  = columnNode.valueOf(QUERY_ATTR_GET_PATH);
				List<String> paths = new ArrayList<String>();
				String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
				for(String p : arrPath)
					paths.add(p);							
								
				PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);
				FileMapping attributeMapping = new FileMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), propertyDefinition, paths);
				classMapping.getColumns().add(attributeMapping);
			}
			
		}
				 			
		return importContext;
	}
	
	/**
	 * Load the columns of the type and check the import file respects the mapping file.
	 *
	 * @param mappingElt the mapping elt
	 * @param columns the columns
	 * @param importContext the import context
	 * @return the import context
	 * @throws ImporterException the be cpg exception
	 * @throws MappingException 
	 */
	@Override
	public ImportContext loadMappingColumns(Element mappingElt,
			List<String> columns, ImportContext importContext) throws MappingException {
		
		ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());			
			
		// check COLUMNS respects the mapping and the class attributes
		List<AbstractAttributeMapping> columnsAttributeMapping = new ArrayList<AbstractAttributeMapping>();
		List<String> unknownColumns = new ArrayList<String>();
		boolean isMLPropertyDef = false;
		for(int z_idx=0 ; z_idx<columns.size() ; z_idx++){
			
			boolean isAttributeMapped = false;
			String columnId = columns.get(z_idx);
			
			if(classMapping != null){
				// columns
				for(AbstractAttributeMapping attrMapping : classMapping.getColumns()){
					if(attrMapping.getId().equals(columnId)){
						columnsAttributeMapping.add(attrMapping);
						isAttributeMapped = true;
						break;
					}
				}	
			}
			
			// columnId not mapped, is it a property or an association ?
			if(!isAttributeMapped){
							
				QName qName = QName.createQName(columnId, namespaceService);					
				PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);
				
				if(propertyDefinition != null){
					logger.debug("loadMappingColumn property, id: " + columnId + " - name: " + propertyDefinition.getName());
					AbstractAttributeMapping attributeMapping = new AttributeMapping(columnId, propertyDefinition);
					columnsAttributeMapping.add(attributeMapping);
					
					// MLText : we store that we got an MLText Property, so the next COLUMNS may be propertyName_Locale (bcpg:ingMLName_en)
					// so they won't be defined in the dictionary
					if(propertyDefinition.getDataType().toString().equals(DataTypeDefinition.MLTEXT.toString())){
						isMLPropertyDef = true;
					}
					else{
						isMLPropertyDef = false;
					}
				}
				else{
					AssociationDefinition assocDefinition = dictionaryService.getAssociation(qName);
					if(assocDefinition != null){
						logger.debug("loadMappingColumn assoc, id: " + columnId + " - name: " + assocDefinition.getName());
						AbstractAttributeMapping attributeMapping = new AttributeMapping(columnId, assocDefinition);
						columnsAttributeMapping.add(attributeMapping);
					}
					else if(isMLPropertyDef){
						// not defined in dictionary but, it is an mltext
						logger.debug("MLText translation, id: " + columnId);
						columnsAttributeMapping.add(new AttributeMapping(columnId, null));
					}
					else{
						unknownColumns.add(columnId);
					}
				}						
			}
		}
		
		if(!unknownColumns.isEmpty()){
			
			// calculate mappedColumns
			List<String> mappedColumns = new ArrayList<String>();
			if(classMapping != null)
				for(AbstractAttributeMapping attrMapping : classMapping.getColumns())
					mappedColumns.add(attrMapping.getId());				

			throw new MappingException(I18NUtil.getMessage(MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING, 
								importContext.getType(), (classMapping != null), unknownColumns, mappedColumns));
		}
		
		importContext.setColumns(columnsAttributeMapping);
		
		return importContext;
	}

	/**
	 * Check if the node exists, according to :
	 * - keys or code
	 * - Path and name.
	 *
	 * @param importContext the import context
	 * @param type the type
	 * @param properties the properties
	 * @return the node ref
	 */
	protected NodeRef findNode(ImportContext importContext, QName type, Map<QName, Serializable> properties) throws ImporterException{				
		
		NodeRef nodeRef = findNodeByKeyOrCode(importContext, type, BeCPGModel.PROP_CODE, properties);		
		
		if(nodeRef == null){			
			
			String name = (String)properties.get(ContentModel.PROP_NAME);
			if(name != null && name != ""){
			
				// look in import folder
				nodeRef = nodeService.getChildByName(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, name);
				
				// entityFolder => look for node
				if(nodeRef != null && nodeService.getType(nodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
					nodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, name);
				}
			} else if(!type.equals(BeCPGModel.TYPE_LINKED_VALUE)) {
			
				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_OR_CREATE_NODEREF));
			}			
		}
				
		return nodeRef;	
	}
	
	/**
	 * find the node by key properties, according to :
	 * - nodeColumnKey
	 * - code.
	 *
	 * @param importContext the import context
	 * @param type the type
	 * @param codeQName the code q name
	 * @param properties the properties
	 * @return the node ref
	 */
	protected NodeRef findNodeByKeyOrCode(ImportContext importContext, QName type, QName codeQName, Map<QName, Serializable> properties){
				
		NodeRef nodeRef = null;		
				
		ClassMapping classMapping = importContext.getClassMappings().get(type);	
		String queryPath = String.format(QUERY_NODE_BY_TYPE, type);
		boolean doQuery = false;					
		
		// nodeColumnKeys
		if(classMapping != null && classMapping.getNodeColumnKeys().size() > 0){					
			
			for(QName attribute : classMapping.getNodeColumnKeys()){
				
				if(ContentModel.ASSOC_CONTAINS.isMatch(attribute)){
					//query by path
					queryPath += LuceneHelper.getCondPath(importContext.getPath(), Operator.AND);
					doQuery = true;
				}
				else if(properties.get(attribute) != null){
					// +@cm\\:localName:%s					
					queryPath += LuceneHelper.getCondEqualValue(attribute, properties.get(attribute) != null ? properties.get(attribute).toString() : null, LuceneHelper.Operator.AND);
					doQuery = true;
				}				
			}	
		}
		// code
		else if(properties.get(codeQName) != null){
			// +@cm\\:localName:%s
			queryPath += LuceneHelper.getCondEqualValue(codeQName, (String)properties.get(codeQName), LuceneHelper.Operator.AND);
			doQuery = true;
		}			
			
		if(doQuery){
			logger.debug("findNodeByKeyOrCode: " + queryPath);

			List<NodeRef> resultSet = beCPGSearchService.luceneSearch(queryPath.toString(), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
				
		        logger.debug("resultSet.length() : " + resultSet.size());
		        if (resultSet.size() != 0){
		        	nodeRef = resultSet.get(0);
		        	
		        	// check node exist
		        	if(!nodeService.exists(nodeRef)){
		        		nodeRef = null;
		        		logger.warn("Node found by lucene query but it doesn't exist. NodeRef: " + nodeRef);
		        	}
		        }		        

		}		
				
		return nodeRef;	
	}
	
	/**
	 * find the nodes by value (multi-value or single value)
	 * @param importContext
	 * @param assocDef
	 * @param value
	 * @return
	 * @throws InvalidTargetNodeException 
	 * @throws ImporterException 
	 */
	protected List<NodeRef> findTargetNodesByValue(ImportContext importContext, AssociationDefinition assocDef, String value) throws ImporterException{
		
		List<NodeRef> targetRefs = new ArrayList<NodeRef>();		 
		 
		 if(!value.isEmpty()){						 						 						
			 
			 if(assocDef.isTargetMany()){
				 String[] arrValue = value.split(RepoConsts.MULTI_VALUES_SEPARATOR);
				 
				 for(String v : arrValue){
					 if(!v.isEmpty()){
						 NodeRef targetNodeRef = findTargetNodeByValue(importContext, assocDef.getTargetClass().getName(), v);
						 if(targetNodeRef != null){
							targetRefs.add(targetNodeRef);
						 }										 										 									 
					 }								 
				 }
			 }
			 else{
				 NodeRef targetNodeRef = findTargetNodeByValue(importContext, assocDef.getTargetClass().getName(), value);
				 if(targetNodeRef != null){
					 targetRefs.add(targetNodeRef); 
				 }								 
			 }						 						 												 
		 }
		 
		 if(logger.isDebugEnabled())
			 logger.debug("assoc, name: " + assocDef.getName() + " - value: " + value + "- targetRefs: " + targetRefs);
		 return targetRefs;
	}
	
	

	/**
	 * Find the target node according to the property (no target type associated to a property of type nodeRef
	 * @param importContext
	 * @param propDef
	 * @param value
	 * @param properties
	 * @return
	 * @throws ImporterException
	 */
	protected NodeRef findPropertyTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef, String value, Map<QName, Serializable> properties) throws ImporterException {
		QName propName = propDef.getName();
		
		if (propName.equals(BeCPGModel.PROP_PARENT_LEVEL)) {

			String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ROOT, LuceneHelper.encodePath(importContext.getPath()), value);

			logger.debug("findPropertyTargetNodeByValue: " + queryPath);
			
			List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_256);

			logger.debug("resultSet.length() : " + ret.size() + " for " + queryPath);

			if (ret.size() == 1) {
				return	ret.get(0);
			} else if(ret.size()>1){
				for (NodeRef n : ret) {
					if (value.equals(nodeService.getProperty(n, RemoteHelper.getPropName(BeCPGModel.TYPE_LINKED_VALUE)))) {
						return n;
					}
				}
			}

			logger.error(" linked value parent : " + queryPath + " not found ");
			throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_ASSOC_TARGET, propDef.getName(), value));
		}

		return findTargetNodeByValue(importContext, propDef.getDataType().getName(), value);
	}

	
	
	/**
	 * find the node by value, according to :
	 * - nodeColumnKey, take the first
	 * - code
	 * - name if it is a listValue.
	 *
	 * @param importContext the import context
	 * @param type the type
	 * @param value the value
	 * @return the node ref
	 * @throws InvalidTargetNodeException 
	 * @throws ImporterException 
	 */
	protected NodeRef findTargetNodeByValue(ImportContext importContext, QName type, String value) throws ImporterException{
		
		NodeRef nodeRef = null;
		StringBuilder queryPath = new StringBuilder(128);
		queryPath.append(String.format(QUERY_NODE_BY_TYPE, type));
		ClassMapping classMapping = importContext.getClassMappings().get(type);
		boolean doQuery = false;
		boolean searchByName = false;
		
		// look in the cache
		String key = String.format(CACHE_KEY, type, value);		
		
		if(importContext.getCacheNodes().containsKey(key)){
			nodeRef = importContext.getCacheNodes().get(key);
		}
		else{
		
			// nodeColumnKeys, take the first
			if(classMapping != null && classMapping.getNodeColumnKeys() != null && classMapping.getNodeColumnKeys().size() > 0){
				
				for(QName attribute : classMapping.getNodeColumnKeys()){				
					
					// +@cm\\:localName:%s
					queryPath.append(LuceneHelper.getCondEqualValue(attribute, value, LuceneHelper.Operator.AND));
					doQuery = true;
					break;
				}
			}
			// productCode or code
			else{
				
				// is it a product
				if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCT)){
					// +@cm\\:localName:%s
					queryPath.append(LuceneHelper.getCondEqualValue(BeCPGModel.PROP_CODE, value, LuceneHelper.Operator.AND));
					doQuery = true;
				}
				// code
				else{
					
					// look for codeAspect
					if(dictionaryService.getType(type).getDefaultAspects()!=null){
						for(AspectDefinition aspectDef : dictionaryService.getType(type).getDefaultAspects()){
							if(aspectDef.getName().equals(BeCPGModel.ASPECT_CODE)){
								// +@cm\\:localName:%s
								queryPath.append(LuceneHelper.getCondEqualValue(BeCPGModel.PROP_CODE, value, LuceneHelper.Operator.AND));
								doQuery = true;
								break;
							}
						}
					}
					
					// we try with the name, if several results, we iterate and test the name
					if(doQuery == false){
						// +@cm\\:localName:%s
						queryPath.append(LuceneHelper.getCondEqualValue(RemoteHelper.getPropName(type), value, LuceneHelper.Operator.AND));
						doQuery = true;
						searchByName = true;
					}
				}				
			}		
				
			if(doQuery){
				
				logger.debug("findTargetNodeByValue: " + queryPath);
				
				List<NodeRef> resultSet = beCPGSearchService.luceneSearch(queryPath.toString(), RepoConsts.MAX_RESULTS_SINGLE_VALUE);

					
			        logger.debug("resultSet.length() : " + resultSet.size());
			        if (resultSet.size() == 0){
			        	
			        	String typeTitle = type.toString();
			        	TypeDefinition typeDef = dictionaryService.getType(type);
			        	if(typeDef != null && typeDef.getTitle() != null && !typeDef.getTitle().isEmpty()){
			        		typeTitle = typeDef.getTitle();
			        	}
			        	
			        	throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_TARGET_ASSOC_NOT_FOUND, typeTitle, value));			        	
			        }		        
			        else if(resultSet.size() == 1){
			        	nodeRef = resultSet.get(0); 
			        }
			        else{			        				        	
			        	if(searchByName){
			        		boolean found = false;
			        		
			        		for(NodeRef n : resultSet){
			        			if(value.equals(nodeService.getProperty(n, RemoteHelper.getPropName(type)))){
			        				
			        				// we found, but we continue to iterate to check how many match
			        				if(!found){
			        					found = true;
			        					nodeRef = n;
			        				}
			        				else{			       
			        					
			        					String typeTitle = type.toString();
			    			        	TypeDefinition typeDef = dictionaryService.getType(type);
			    			        	if(typeDef != null && typeDef.getTitle() != null && !typeDef.getTitle().isEmpty()){
			    			        		typeTitle = typeDef.getTitle();
			    			        	}
			        					
			        					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_TARGET_ASSOC_SEVERAL, typeTitle, value));			        					
			        				}
			        			}
			        		}
			        		
			        		// not found
				        	if(!found){
				        		
				        		String typeTitle = type.toString();
	    			        	TypeDefinition typeDef = dictionaryService.getType(type);
	    			        	if(typeDef != null && typeDef.getTitle() != null && !typeDef.getTitle().isEmpty()){
	    			        		typeTitle = typeDef.getTitle();
	    			        	}
				        		
				        		throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_TARGET_ASSOC_NOT_FOUND, typeTitle, value));
				        	}
			        	}			        				        	
			        }
		        
			}
			// list value => by name
			else if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_LIST_VALUE)){
				nodeRef = getItemByTypeAndName(type, value);
			}		
			
			// add in the cache
			importContext.getCacheNodes().put(key, nodeRef);
		}
		
		return nodeRef;	
	}


	private NodeRef getItemByTypeAndName(QName type, String name) {

		String queryPath = String.format(RepoConsts.QUERY_CHARACT_BY_TYPE_AND_NAME, type, name);

		List<NodeRef> nodes = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_SINGLE_VALUE);

		if (nodes.size() > 0) {
			return nodes.get(0);
		}

		return null;

	}


}
