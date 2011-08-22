/*
 * 
 */
package fr.becpg.repo.report.search;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;

import fr.becpg.common.RepoConsts;
import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.config.mapping.MappingException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.PropertyService;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.report.ProductReportServiceImpl;

/**
 * Class used to render the result of a search in a report
 *
 * @author querephi
 */
public class ExportSearchServiceImpl implements ExportSearchService{		
		
	/** The Constant QUERY_XPATH_DATE_FORMAT. */
	protected static final String QUERY_XPATH_DATE_FORMAT = "settings/setting[@id='dateFormat']/@value";
	protected static final String QUERY_XPATH_DATETIME_FORMAT = "settings/setting[@id='datetimeFormat']/@value";
	
	/** The Constant QUERY_XPATH_DECIMAL_PATTERN. */
	protected static final String QUERY_XPATH_DECIMAL_PATTERN = "settings/setting[@id='decimalPattern']/@value";
	
	/** The Constant FILE_RPTDESIGN. */
	private static final String FILE_RPTDESIGN = "ExportSearch.rptdesign";
	
	/** The Constant FILE_QUERY. */
	private static final String FILE_QUERY = "ExportSearchQuery.xml";
	
	/** The Constant QUERY_XPATH_COLUMNS_ATTRIBUTE. */
	private static final String QUERY_XPATH_COLUMNS_ATTRIBUTE = "/export/query/columns/column[@type='Attribute']";
	
	/** The Constant QUERY_XPATH_COLUMNS_DATALIST. */
	private static final String QUERY_XPATH_COLUMNS_DATALIST = "/export/query/columns/column[@type='Characteristic']";
	
	/** The Constant QUERY_XPATH_COLUMNS_FILE. */
	private static final String QUERY_XPATH_COLUMNS_FILE = "/export/query/columns/column[@type='File']";
	
	/** The Constant QUERY_ATTR_GET_ID. */
	private static final String QUERY_ATTR_GET_ID = "@id";
	
	/** The Constant QUERY_ATTR_GET_ATTRIBUTE. */
	private static final String QUERY_ATTR_GET_ATTRIBUTE = "@attribute";
	
	/** The Constant QUERY_ATTR_GET_DATALIST_QNAME. */
	private static final String QUERY_ATTR_GET_DATALIST_QNAME = "@dataListQName";
	
	/** The Constant QUERY_ATTR_GET_CHARACT_QNAME. */
	private static final String QUERY_ATTR_GET_CHARACT_QNAME = "@charactQName";
	
	/** The Constant QUERY_ATTR_GET_CHARACT_NODE_REF. */
	private static final String QUERY_ATTR_GET_CHARACT_NODE_REF = "@charactNodeRef";
	
	/** The Constant QUERY_ATTR_CHARACT_NODE_REF. */
	private static final String QUERY_ATTR_CHARACT_NODE_REF = "charactNodeRef";
	
	/** The Constant QUERY_ATTR_GET_CHARACT_NAME. */
	private static final String QUERY_ATTR_GET_CHARACT_NAME = "@charactName";
	
	/** The Constant QUERY_ATTR_GET_PATH. */
	private static final String QUERY_ATTR_GET_PATH = "@path";
	
	/** The Constant TAG_EXPORT. */
	private static final String TAG_EXPORT = "export";
	
	/** The Constant TAG_NODES. */
	private static final String TAG_NODES = "nodes";
	
	/** The Constant TAG_NODE. */
	private static final String TAG_NODE = "node";
	
	/** The Constant ATTR_ID. */
	private static final String ATTR_ID = "id";
	
	/** The Constant VALUE_NULL. */
	public static final String VALUE_NULL = "";
	
	/** The Constant KEY_IMAGE_NODE_IMG. */
	public static final String KEY_IMAGE_NODE_IMG = "node%s-%s";
	
	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ExportSearchServiceImpl.class);	
	
	private SearchService searchService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The content service. */
	private ContentService contentService;
	
	/** The report engine. */
	private IReportEngine reportEngine;
	
	/** The namespace service. */
	private NamespaceService namespaceService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The list value service. */
	private ListValueService listValueService;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The product report service. */
	private ProductReportServiceImpl productReportService; // devrait être un autre namespace et être renommé car non spécifique au produit				
	
	private PropertyService propertyService;
	
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
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
	 * Sets the report engine.
	 *
	 * @param reportEngine the new report engine
	 */
	public void setReportEngine(IReportEngine reportEngine) {
		this.reportEngine = reportEngine;
	}
	
	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
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
	 * Sets the product dao.
	 *
	 * @param productDAO the new product dao
	 */
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
	
	/**
	 * Sets the list value service.
	 *
	 * @param listValueService the new list value service
	 */
	public void setListValueService(ListValueService listValueService) {
		this.listValueService = listValueService;
	}
	
	/**
	 * Sets the product dictionary service.
	 *
	 * @param productDictionaryService the new product dictionary service
	 */
	public void setProductDictionaryService(
			ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}
	
	/**
	 * Sets the product report service.
	 *
	 * @param productReportService the new product report service
	 */
	public void setProductReportService(ProductReportServiceImpl productReportService) {
		this.productReportService = productReportService;
	}		
	
	public void setPropertyService(PropertyService propertyService) {
		this.propertyService = propertyService;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.report.ExportSearchService#getReportTpls()
	 */
	@Override
	public List<NodeRef> getReportTpls() {      	    				
		
		SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(RepoConsts.PATH_QUERY_REPORTS_EXPORT_SEARCH);	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(RepoConsts.MAX_RESULTS_NO_LIMIT);
        
        ResultSet resultSet =null;
        
        try{
	        resultSet = searchService.query(sp);
			
	        logger.debug("getReportTpls - resultSet.length() : " + resultSet.length());	        
	        
			return resultSet.getNodeRefs();
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.report.ExportSearchService#getReport(java.lang.String, java.lang.String, java.io.OutputStream)
	 */
	@Override
	public void getReport(String reportName, List<NodeRef> searchResults, OutputStream outputStream) {
		
		NodeRef templateNodeRef = getReportTemplate(reportName);
		
		if(templateNodeRef != null){
						
			ExportSearchContext exportSearchCtx = null;
			
			try{			
				exportSearchCtx = getQuery(templateNodeRef);
				
			}catch(MappingException e){
				logger.error("Failed to get the export search context. FileName: " + FILE_QUERY, e);
			}
			
			if(exportSearchCtx != null){
				
				NodeRef reportNodeRef = nodeService.getChildByName(templateNodeRef, ContentModel.ASSOC_CONTAINS, FILE_RPTDESIGN);
				
				if(reportNodeRef != null){													
					
					renderReport(reportNodeRef, exportSearchCtx, searchResults, outputStream);				
				}
				else{
					logger.error("Failed to get reportNodeRef, file name: " + FILE_RPTDESIGN);
				}		
			}								
		}
	}
	
	/**
	 * Render the report.
	 *
	 * @param templateNodeRef the template node ref
	 * @param queryElt the query elt
	 * @param searchQuery the search query
	 * @param outputStream the output stream
	 */
	private void renderReport(NodeRef templateNodeRef, ExportSearchContext exportSearchCtx, List<NodeRef> searchResults, OutputStream outputStream){		
		
		try{
			
			ContentReader reader = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT);
			InputStream inputStream = reader.getContentInputStream();
			IReportRunnable design = reportEngine.openReportDesign(inputStream);																					
			
			//Create task to run and render the report,
			logger.debug("Create task to run and render the report");
			IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
			
			EXCELRenderOption options = new EXCELRenderOption(); 
			options.setOutputFormat("xls");
			options.setOutputStream(outputStream);				  
			task.setRenderOption(options);								
			
			// Prepare data source
			logger.debug("Prepare data source");								
			Document document = DocumentHelper.createDocument();
			Element exportElt = document.addElement(TAG_EXPORT);
			task = loadReportData(exportSearchCtx, exportElt, task, searchResults);
			
			// xml data
			logger.debug("add Xml data");
			ByteArrayInputStream bais = new ByteArrayInputStream( exportElt.asXML().getBytes());
			task.getAppContext().put(KEY_XML_INPUTSTREAM, bais);
			
			task.run();
			task.close();
			outputStream.close();
				
				
//			//DEBUG code
//			FileOutputStream fosXML = new FileOutputStream(new File("/tmp/exportSearch_written.xml"));
//			OutputFormat format = OutputFormat.createPrettyPrint();
//			XMLWriter writer = new XMLWriter(fosXML, format);
//			writer.write(exportElt.getDocument().asXML());
//			writer.flush();
//			
//			FileOutputStream fos = new FileOutputStream(new File("/tmp/exportSearch_written.xls"));
//			IRunAndRenderTask task2 = reportEngine.createRunAndRenderTask(design);
//			EXCELRenderOption options2 = new EXCELRenderOption();
//			options2.setOutputStream(fos);							
//			options2.setOutputFormat("xls");
//			task2.setRenderOption(options2);
//			
//			// Prepare data source
//			logger.debug("Prepare data source");												
//			Document document2 = DocumentHelper.createDocument();
//			Element exportElt2 = document2.addElement(TAG_EXPORT);
//			task = loadReportData(exportSearchCtx, exportElt2, task2, searchResults);
//			
//			
//			ByteArrayInputStream bais2 = new ByteArrayInputStream( exportElt.asXML().getBytes());
//			task.getAppContext().put(KEY_XML_INPUTSTREAM, bais2);
//			
//			task2.run();
//			task2.close();
//			fos.close();
//			// End DEBUG code
				
		}
		catch(Exception e){
			logger.error("Failed to run report: ",  e);
		}				
		
	}
	
	/**
	 * Generate Xml export data.
	 *
	 * @param queryElt the query elt
	 * @param exportElt the export elt
	 * @param task the task
	 * @param nodeRefList the node ref list
	 * @return the i run and render task
	 */
	private IRunAndRenderTask loadReportData(ExportSearchContext exportSearchCtx, Element exportElt, IRunAndRenderTask task, List<NodeRef> nodeRefList){
		
		logger.debug("start loadReportData");		
		Element nodesElt = exportElt.addElement(TAG_NODES);
		Integer z_idx = 1;
		
		for(NodeRef nodeRef : nodeRefList){
			
			Element nodeElt = nodesElt.addElement(TAG_NODE);
			nodeElt.addAttribute(ATTR_ID, z_idx.toString());
			
			if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_PRODUCT)){
				task = exportProduct(exportSearchCtx, nodeElt, task, nodeRef);
			}
			else{
				task = exportNode(exportSearchCtx, nodeElt, task, nodeRef);
			}
			
			z_idx++;
		}
		
		logger.debug("End loadReportData");
			
		return task;
    }
 
	/**
	 * Export properties and associations of a node.
	 *
	 * @param queryElt the query elt
	 * @param nodeElt the node elt
	 * @param task the task
	 * @param nodeRef the node ref
	 * @return the i run and render task
	 */
	private IRunAndRenderTask exportNode(ExportSearchContext exportSearchCtx, Element nodeElt, IRunAndRenderTask task, NodeRef nodeRef){				
		
		
		// export class attributes
		for(AttributeMapping attributeMapping : exportSearchCtx.getAttributeColumns()){
			
			String value = getColumnValue(exportSearchCtx, nodeRef, attributeMapping.getAttribute());
			nodeElt.addAttribute(attributeMapping.getId(), value);
		}
		
    	
    	// export file
		for(FileMapping fileMapping : exportSearchCtx.getFileColumns()){
						   
    		NodeRef tempNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			for(String p : fileMapping.getPath()){
				
				if(tempNodeRef != null)
					tempNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, p);
			}
			
			logger.debug("tempNodeRef: " + tempNodeRef);
			
			if(tempNodeRef != null){
							
				// file content
				if(fileMapping.getAttribute().equals(ContentModel.PROP_CONTENT)){
					
					byte[] imageBytes = productReportService.getImage(tempNodeRef);
					if (imageBytes != null){
											
						task.getAppContext().put(String.format(KEY_IMAGE_NODE_IMG, nodeElt.valueOf(QUERY_ATTR_GET_ID), fileMapping.getId()), imageBytes);				
					}
				}
				// class attribute
				else{
					String value = getColumnValue(exportSearchCtx, nodeRef, fileMapping.getAttribute());
		    		nodeElt.addAttribute(fileMapping.getId(), value);
				}
			}
    	}
    	
    	return task;
	}
		
	/**
	 * Gets the column value.
	 *
	 * @param nodeRef the node ref
	 * @param qName the q name
	 * @return the column value
	 */
	private String getColumnValue(ExportSearchContext exportSearchCtx, NodeRef nodeRef, ClassAttributeDefinition attribute){
		
		String value = VALUE_NULL;
		
		// property
		if(attribute instanceof PropertyDefinition){
			
			Serializable serializable = nodeService.getProperty(nodeRef, attribute.getName());								
			value = propertyService.getStringValue((PropertyDefinition)attribute, serializable, exportSearchCtx.getPropertyFormats());
    		
		}
		else if(attribute instanceof AssociationDefinition){// associations
			    			
			List<AssociationRef> assocRefs =  nodeService.getTargetAssocs(nodeRef, attribute.getName());
			
			for(AssociationRef assocRef : assocRefs){
				
				if(!value.isEmpty())
					value += RepoConsts.LABEL_SEPARATOR;
				
				value += (String)nodeService.getProperty(assocRef.getTargetRef(), ContentModel.PROP_NAME);
			}
		}
		
		return value;
	}
	
	/**
	 * Export product.
	 *
	 * @param queryElt the query elt
	 * @param nodeElt the node elt
	 * @param task the task
	 * @param productNodeRef the product node ref
	 * @return the i run and render task
	 */
	private IRunAndRenderTask exportProduct(ExportSearchContext exportSearchCtx, Element nodeElt, IRunAndRenderTask task, NodeRef productNodeRef){
		
		// export node
		exportNode(exportSearchCtx, nodeElt, task, productNodeRef);
		
		// export charact		
		NodeRef listContainerNodeRef = productDAO.getListContainer(productNodeRef);
		
		for(CharacteristicMapping characteristicMapping : exportSearchCtx.getCharacteristicsColumns()){
    		
    		NodeRef listNodeRef = productDAO.getList(listContainerNodeRef, characteristicMapping.getDataListQName());
    		NodeRef linkNodeRef = productDAO.getLink(listNodeRef, characteristicMapping.getCharactQName(), characteristicMapping.getCharactNodeRef());
    		
    		if(linkNodeRef != null){
    			
    			String value = getColumnValue(exportSearchCtx, linkNodeRef, characteristicMapping.getAttribute());
    			nodeElt.addAttribute(characteristicMapping.getId(), value);
    		}
		}	    				    
		
    	return task;
	}
	
	
	
	/**
	 * Get the report template by name.
	 *
	 * @param reportName the report name
	 * @return the report template
	 */
	private NodeRef getReportTemplate(String reportName) {
    	
    	NodeRef templateNodeRef = null;  
    	    	
    	String queryPath = String.format(RepoConsts.PATH_QUERY_REPORT_EXPORT_SEARCH, ISO9075.encode(reportName));
					
		logger.debug(queryPath);
		
		SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath.toString());	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(RepoConsts.MAX_RESULTS_SINGLE_VALUE);
        
        ResultSet resultSet =null;
        
        try{
	        resultSet = searchService.query(sp);
			
	        logger.debug("resultSet.length() : " + resultSet.length());
	        if (resultSet.length() != 0){
	        	templateNodeRef = resultSet.getNodeRef(0); 
	        }
	        else{
	        	logger.error("Failed to load export search template, no template found. Path: " + queryPath);
	        }
	        
			return templateNodeRef;
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
	}
	
	/**
	 * Load the query file.
	 *
	 * @param templateNodeRef the template node ref
	 * @param queryFileName the query file name
	 * @return the query
	 * @throws MappingException 
	 * @throws BeCPGException 
	 */
	private ExportSearchContext getQuery(NodeRef templateNodeRef) throws MappingException{		
		
		Element queryElt = null;
		ExportSearchContext exportSearchCtx = new ExportSearchContext();
		
		NodeRef queryNodeRef = nodeService.getChildByName(templateNodeRef, ContentModel.ASSOC_CONTAINS, FILE_QUERY);		
		if(queryNodeRef == null){
			logger.error(String.format("The query file '%s' is not found", FILE_QUERY));
			return exportSearchCtx;
		}
		
		ContentReader reader = contentService.getReader(queryNodeRef, ContentModel.PROP_CONTENT);
		InputStream is = reader.getContentInputStream();
		SAXReader saxReader = new SAXReader();
		
		try{
			Document doc = saxReader.read(is);
			queryElt = doc.getRootElement();
		}
		catch(DocumentException e){
			logger.error(String.format("Failed to read the query file", e));
		}
		
		// date format
		Node dateFormat = queryElt.selectSingleNode(QUERY_XPATH_DATE_FORMAT);
		if(dateFormat != null){
			exportSearchCtx.getPropertyFormats().setDateFormat(new SimpleDateFormat(dateFormat.getStringValue()));
		}
		
		// datetime format
		Node datetimeFormat = queryElt.selectSingleNode(QUERY_XPATH_DATETIME_FORMAT);
		if(datetimeFormat != null){			
			exportSearchCtx.getPropertyFormats().setDatetimeFormat(new SimpleDateFormat(datetimeFormat.getStringValue()));
		}
		
		// decimal format
		Node decimalFormatPattern = queryElt.selectSingleNode(QUERY_XPATH_DECIMAL_PATTERN);
		if(decimalFormatPattern != null){
			exportSearchCtx.getPropertyFormats().getDecimalFormat().applyPattern(decimalFormatPattern.getStringValue());			
		}
		
		// attributes
		@SuppressWarnings("unchecked")
		List<Node> columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_ATTRIBUTE);
		for(Node columnNode : columnNodes){
			QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
			
			ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
			if(attributeDef == null){
			
				attributeDef = dictionaryService.getAssociation(attribute);					
				if(attributeDef == null){						
					throw new MappingException("Failed to map the following attribute. TemplateNodeRef: " + templateNodeRef + " - Attribute: " + attribute);
				}
			}
							
			AttributeMapping attributeMapping = new AttributeMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef);
			exportSearchCtx.getAttributeColumns().add(attributeMapping);
		}									
		
		// characteristics
		columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_DATALIST);
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
				charactNodeRef = listValueService.getItemByTypeAndName(assocDef.getTargetClass().getName(), charactName);
				
				if(charactNodeRef == null){
					throw new MappingException(String.format("ERROR : Failed to get the nodeRef of the characteristic. Type:%s - Name:%s",  assocDef.getTargetClass().getName(), charactName));
				}
			}
			else{
				throw new MappingException("ERROR : Missing Characteristic nodeRef or name. trace: " + columnNode.asXML());
			}
			
			ClassAttributeDefinition attributeDef = dictionaryService.getProperty(qName);
			if(attributeDef == null){
				attributeDef = dictionaryService.getAssociation(qName);
			}							
							
			CharacteristicMapping attributeMapping = new CharacteristicMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef, dataListQName, charactQName, charactNodeRef);
			exportSearchCtx.getCharacteristicsColumns().add(attributeMapping);
		}
		
		// file import
		columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_FILE);
		for(Node columnNode : columnNodes){
			QName qName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
			
			String path  = columnNode.valueOf(QUERY_ATTR_GET_PATH);
			List<String> paths = new ArrayList<String>();
			String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
			for(String p : arrPath)
				paths.add(p);							
							
			PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);
			FileMapping attributeMapping = new FileMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), propertyDefinition, paths);
			exportSearchCtx.getFileColumns().add(attributeMapping);
		}
		
		return exportSearchCtx;
	}

	
		

}
