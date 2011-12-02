package fr.becpg.repo.report.entity.impl;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.common.RepoConsts;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.PropertyService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

public class EntityReportServiceImpl implements EntityReportService{

	/** The Constant VALUE_NULL. */
	private static final String VALUE_NULL = "";
	private static final String VALUE_PERSON = "%s %s";
		
	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	
	private static final String PARAM_VALUE_HIDE_CHAPTER_SUFFIX = "HideChapter";
		
	private static final String QUERY_XPATH_FORM_SETS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/set";
	private static final String QUERY_XPATH_FORM_FIELDS_BY_SET = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/field[@set=\"%s\"]";
	private static final String QUERY_XPATH_FORM_FIELDS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/field-visibility/show";
	private static final String QUERY_ATTR_GET_ID = "@id";
	private static final String QUERY_ATTR_GET_LABEL = "@label";
	private static final String SET_DEFAULT = "";
	
	private static Log logger = LogFactory.getLog(EntityReportServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The content service. */
	private ContentService contentService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;	
	
	private EntityListDAO entityListDAO;
	
	/** The search service. */
	private SearchService searchService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;
	
	/** The report engine. */
	private IReportEngine reportEngine;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;
	
	private ServiceRegistry serviceRegistry;
	
	private PropertyService propertyService;
	
	private ReportTplService reportTplService;
			
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	/**
	 * Sets the search service.
	 *
	 * @param searchService the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
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
	 * Sets the report engine.
	 *
	 * @param reportEngine the new report engine
	 */
	public void setReportEngine(IReportEngine reportEngine){
		this.reportEngine = reportEngine;
	}
	
	/**
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService){
		this.mimetypeService = mimetypeService;
	}		
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	public void setPropertyService(PropertyService propertyService) {
		this.propertyService = propertyService;
	}
	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	/**
	 * Load node attributes.
	 *
	 * @param nodeRef the node ref
	 * @param elt the elt
	 * @return the element
	 */
	@Override
	public Map<ClassAttributeDefinition, String> loadNodeAttributes(NodeRef nodeRef) {

		PropertyFormats propertyFormats = new PropertyFormats(false);
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
			property.getKey().equals(ContentModel.PROP_CONTENT) ||
			property.getKey().equals(ContentModel.PROP_VERSION_LABEL))){
			
				PropertyDefinition propertyDef =  dictionaryService.getProperty(property.getKey());
				if(propertyDef == null){
					logger.error("This property doesn't exist. Name: " + property.getKey());
					continue;
				}
				
				String value = VALUE_NULL;				
				if (property.getValue() != null) {
					
					value = propertyService.getStringValue(propertyDef, property.getValue(), propertyFormats);
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
			logger.debug("###targetQName: " + targetQName);
			
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
			} else {
				tempValues.put(qName, name);
			}
		}		
		
		for(Map.Entry<QName, String> tempValue : tempValues.entrySet()){
			AssociationDefinition associationDef =  dictionaryService.getAssociation(tempValue.getKey());
			values.put(associationDef, tempValue.getValue());
		}
		
		return values;
	}	

	/**
	 * Load the image associated to the node.
	 *
	 * @param nodeRef the node ref
	 * @return the image
	 */
	@Override
	public byte[] getImage(NodeRef nodeRef) {

		byte[] imageBytes = null;
		
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		
		if(reader != null){
			InputStream is = reader.getContentInputStream();

			try {
				Image image = ImageIO.read(is);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write((RenderedImage) image, "jpg", out);
				imageBytes = out.toByteArray();
			} catch (IOException e) {
				logger.error("Failed to get the content", e);
			}
		}		
		
		return imageBytes;
	}
	
	
	/**
	 * Check if node has changed, so the report is out of date.
	 *
	 * @param nodeRef the node ref
	 * @return true, if is report up to date
	 */	
	@Override
	public boolean isReportUpToDate(NodeRef nodeRef) {
					
		Date reportModified = (Date)nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
		
		// report not generated
		if(reportModified == null){
			logger.debug("report not generated");
			return false;
		}
		
		// check modified date (modified is always bigger than reportModified so a delta is defined)
		Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);		
		logger.debug("modified: " + ISO8601DateFormat.format(modified) + " - reportModified: " + ISO8601DateFormat.format(reportModified));
		if(modified.after(reportModified)){			
			logger.debug("node has been modified");
			return false;
		}		
		
		return true;
	}	
	
	/**
	 * Get the node where the document will we stored.
	 *
	 * @param nodeRef the node ref
	 * @param tplNodeRef the tpl node ref
	 * @return the document content writer
	 */
	@Override
	public ContentWriter getDocumentContentWriter(NodeRef nodeRef, NodeRef tplNodeRef){
		
		ContentWriter contentWriter = null;
		
		if((Boolean)nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT)){
			contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		}
		else{
			// the doc will be stored in the documents folder of the node			
			NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			
			if(nodeService.getType(parentNodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
			
				String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
				NodeRef documentsFolderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);		
				if(documentsFolderNodeRef == null){
					
					documentsFolderNodeRef = fileFolderService.create(parentNodeRef, documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
				}
				
				String documentName = (String)nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME);
				NodeRef documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, documentName);
				if(documentNodeRef == null){
					
					documentNodeRef = fileFolderService.create(documentsFolderNodeRef, documentName, ContentModel.TYPE_CONTENT).getNodeRef();
				}
				
				contentWriter = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);
			}
			else{
				logger.debug("The node does not have a entityFolder so we cannot store doc in the folder 'Documents'.");
			}			
		}
		
		return contentWriter;
	}
	

	/**
	 * Method that generates reports.
	 *
	 * @param nodeRef the node ref
	 * @param tplsNodeRef the tpls node ref
	 * @param nodeElt the node elt
	 * @param images the images
	 */
	@Override
	public void generateReports(NodeRef nodeRef, List<NodeRef> tplsNodeRef, Element nodeElt, Map<String, byte[]> images) {
		
		if(nodeRef == null){
			throw new IllegalArgumentException("nodeRef is null");
		}
		
		if(tplsNodeRef.isEmpty()){
			throw new IllegalArgumentException("tplsNodeRef is empty");
		}
		
		if(nodeElt == null){
			throw new IllegalArgumentException("nodeElt is null");
		}		
		
		// calculate the visible datalists
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
		List<QName> existingLists = entityListDAO.getExistingListsQName(listContainerNodeRef);		
		
		// generate reports
		for(NodeRef tplNodeRef : tplsNodeRef){        			
				        					        						
			//prepare
			try{							
			    
				logger.debug("reportTplNodeRef " + tplNodeRef);
				ContentReader reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);
    			InputStream inputStream = reader.getContentInputStream();
				IReportRunnable design = reportEngine.openReportDesign(inputStream);							
				
				//Run report		
				logger.debug("Run report");
				ContentWriter writer = getDocumentContentWriter(nodeRef, tplNodeRef);
				
				if(writer != null){
				
					String mimetype = mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF);
					logger.debug("add file, mimetype: " + mimetype);
					writer.setMimetype(mimetype);
					
					//Create task to run and render the report,
					logger.debug("Create task to run and render the report");
					IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
					
					IRenderOption options = new RenderOption();
					OutputStream outputStream = writer.getContentOutputStream();
					options.setOutputStream(outputStream);							
					options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
					task.setRenderOption(options);
					
					// xml data
					ByteArrayInputStream bais = new ByteArrayInputStream( nodeElt.asXML().getBytes());
					task.getAppContext().put(KEY_XML_INPUTSTREAM, bais);
					
					// images
					logger.debug("add images");
					if(images != null){
					
						for(Map.Entry<String, byte[]> entry : images.entrySet()){
							task.getAppContext().put(entry.getKey(), entry.getValue());
						}
					}					
					
					IGetParameterDefinitionTask paramTask = reportEngine.createGetParameterDefinitionTask(design);											
					
					// hide all datalists and display visible ones
					for(Object key : paramTask.getDefaultValues().keySet()){
						
						if(((String)key).endsWith(PARAM_VALUE_HIDE_CHAPTER_SUFFIX)){
							task.setParameterValue((String)key, Boolean.TRUE);
						}
					}							
					
					for(QName existingList : existingLists){
						task.setParameterValue(existingList.getLocalName() + PARAM_VALUE_HIDE_CHAPTER_SUFFIX, Boolean.FALSE);
					}
					
					logger.debug("run task");
					task.run();
					task.close();
					outputStream.close();
						
					logger.debug("Report generated.");
				}    							
			}
			catch(Exception e){
				logger.error("Failed to execute report: ",  e);
			}						
		}
		
		// set reportNodeGenerated property to now
        nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, new Date());
	}
	
	
	@Override
	public Map<String, List<String>> getFieldsBySets(NodeRef nodeRef, String reportFormConfigPath){
				
		NamespaceService namespaceService = serviceRegistry.getNamespaceService();
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
	
	/**
	 * Get the report templates to generate.
	 *
	 * @param nodeRef the product node ref
	 * @return the report tpls to generate
	 */
	@Override
	public List<NodeRef> getReportTplsToGenerate(NodeRef nodeRef) {
		
		List<NodeRef> tplsToReturnNodeRef = new ArrayList<NodeRef>();
		
		// system reports
		QName nodeType = nodeService.getType(nodeRef);
		List<NodeRef> tplsNodeRef = reportTplService.getSystemReportTemplates(ReportType.Document, nodeType);										
		
		for(NodeRef tplNodeRef : tplsNodeRef){			
			
			tplsToReturnNodeRef.add(tplNodeRef);
		}
		
		// selected user reports
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, ReportModel.ASSOC_REPORT_TEMPLATES);
		
		for(AssociationRef assocRef : assocRefs){
			
			NodeRef tplNodeRef = assocRef.getTargetRef();			
			tplsToReturnNodeRef.add(tplNodeRef);
		}		
		
		return tplsToReturnNodeRef;		
	}
}
