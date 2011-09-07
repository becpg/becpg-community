package fr.becpg.repo.report.template.impl;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
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
import org.springframework.core.io.ClassPathResource;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.report.template.ReportFormat;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

public class ReportTplServiceImpl implements ReportTplService{


	/** The logger. */
	private static Log logger = LogFactory.getLog(ReportTplServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The content service. */
	private ContentService contentService;
	
	/** The search service. */
	private SearchService searchService;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;
			
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
	 * Sets the search service.
	 *
	 * @param searchService the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}	
	
	
	/**
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService){
		this.mimetypeService = mimetypeService;
	}			

	/**
	 * Get the report templates of the product.
	 *
	 * @param productNodeRef the product node ref
	 * @return the system report templates
	 * @param:productNodeRef
	 * @param:tplName the name of the template or starting by
	 */
	@Override
	public List<NodeRef> getSystemReportTemplates(ReportType reportType, QName nodeType) {    	  
    	
		List<NodeRef> tplsNodeRef = new ArrayList<NodeRef>();    	
		
		if(nodeType == null){
			return tplsNodeRef;
		}
		
		StringBuilder queryPath = new StringBuilder(128);
		queryPath.append(String.format(RepoConsts.PATH_QUERY_REPORTTEMPLATES, reportType, nodeType, true));			
		
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
	        if(resultSet.length() > 0)
	        	tplsNodeRef = resultSet.getNodeRefs();
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
        
        return tplsNodeRef;
	}
	
	/**
	 * Get the report templates of the product type that user can choose from UI.
	 *
	 * @param nodeType the node type
	 * @param tplName the tpl name
	 * @return the user report templates
	 * @param:productType
	 * @param:tplName the name of the template or starting by
	 */
	@Override
	public List<NodeRef> suggestUserReportTemplates(ReportType reportType, QName nodeType, String tplName) {
		
		List<NodeRef> tplsNodeRef = new ArrayList<NodeRef>();
		
		if(nodeType == null){
			return null;
		}		
    	
    	StringBuilder queryPath = new StringBuilder(128);
		queryPath.append(String.format(RepoConsts.PATH_QUERY_REPORTTEMPLATES, reportType, nodeType, false));
					
		// +@cm\\:localName:%s											
		queryPath.append(LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, tplName, LuceneHelper.Operator.AND));
		
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
	        if(resultSet.length() > 0)
	        	tplsNodeRef = resultSet.getNodeRefs();
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
        
        return tplsNodeRef;
	}
	
	
	
	/**
	 * Create a product report tpl
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 * @param nodeType
	 * @param isSystemTpl
	 * @param isDefaultTpl
	 * @return
	 * @throws IOException
	 */
	@Override
	public NodeRef createTpl(NodeRef parentNodeRef, String tplName, String tplFilePath, ReportType reportType, QName nodeType, boolean isSystemTpl, boolean isDefaultTpl) throws IOException{						
		
		NodeRef productReportTplNodeRef = null;
		ClassPathResource resource = new ClassPathResource(tplFilePath);
		
		if(resource.exists()){
			
			//create report template folder
		   	logger.debug("create report template folder");
	   		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, tplName);
			
			productReportTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,  (String)properties.get(ContentModel.PROP_NAME));    	
	    	if(productReportTplNodeRef == null){
	    		productReportTplNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_FOLDER, properties).getChildRef();
	    		
	    		properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, tplName);	    		
	    		properties.put(ReportModel.PROP_REPORT_TPL_TYPE, reportType);
	    		properties.put(ReportModel.PROP_REPORT_TPL_CLASS_NAME, nodeType);
				properties.put(ReportModel.PROP_REPORT_TPL_IS_SYSTEM, isSystemTpl);
				properties.put(ReportModel.PROP_REPORT_TPL_IS_DEFAULT, isDefaultTpl);
	        	NodeRef fileNodeRef = nodeService.createNode(productReportTplNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ReportModel.TYPE_REPORT_TPL, properties).getChildRef();
	        	
	        	ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
	        	
	        	String mimetype = mimetypeService.guessMimetype(tplFilePath);
	    		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
	            Charset charset = charsetFinder.getCharset(resource.getInputStream(), mimetype);
	            String encoding = charset.name();

	        	writer.setMimetype(mimetype);
	        	writer.setEncoding(encoding);
	        	writer.putContent(resource.getInputStream());
	    	}    		 
		}
		else{
			logger.error("Resource not found. Path: " + tplFilePath);
		}
		
		return productReportTplNodeRef;
	}


	@Override
	public List<NodeRef> cleanDefaultTpls(List<NodeRef> tplsNodeRef) {
		
		List<NodeRef> defaultTplsNodeRef = new ArrayList<NodeRef>();
		NodeRef userDefaultTplNodeRef = null;
		
		for(NodeRef tplNodeRef : tplsNodeRef){
			
			boolean isDefault = (Boolean)nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT);
			boolean isSystem = (Boolean)nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM);
			
			if(isDefault){
				
				if(!isSystem && userDefaultTplNodeRef == null){
					userDefaultTplNodeRef = tplNodeRef;
				}
				else{
					defaultTplsNodeRef.add(tplNodeRef);
				}					
			}
		}
		
		// no user default tpl, take the first system default
		if(userDefaultTplNodeRef == null && defaultTplsNodeRef.size() > 0){
			userDefaultTplNodeRef = defaultTplsNodeRef.get(0);
			defaultTplsNodeRef.remove(0);
		}
		
		// remove the other system default
		for(NodeRef tplNodeRef : defaultTplsNodeRef){
			
			tplsNodeRef.remove(tplNodeRef);
		}
		
		return tplsNodeRef;
	}

	@Override
	public ReportFormat getReportFormat(NodeRef tplNodeRef) {
		
		ReportType reportType = ReportType.parse((String)nodeService.getProperty(tplNodeRef,  ReportModel.PROP_REPORT_TPL_TYPE));
		String format = (String)nodeService.getProperty(tplNodeRef,  ReportModel.PROP_REPORT_TPL_FORMAT);
		ReportFormat reportFormat;
		
		String dbReportFormat = (String)nodeService.getProperty(tplNodeRef,  ReportModel.PROP_REPORT_TPL_FORMAT);
		if(dbReportFormat == null){
			if(ReportType.Search.equals(reportType)){
				reportFormat = ReportFormat.XLS;
			}
			else{
				reportFormat = ReportFormat.PDF;
			}
		}
		else{
			reportFormat = ReportFormat.valueOf(format);
		}
				
		return reportFormat;
	}	
}
