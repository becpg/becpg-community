package fr.becpg.repo.report.template.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.search.impl.lucene.LuceneFunction;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
import org.jaxen.expr.AdditiveExpr;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.report.template.ReportFormat;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.search.BeCPGSearchService;

public class ReportTplServiceImpl implements ReportTplService{

	private static final String QUERY_REPORTTEMPLATE = " +TYPE:\"rep:reportTpl\" +@rep\\:reportTplType:%s +@rep\\:reportTplIsSystem:%s";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ReportTplServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The content service. */
	private ContentService contentService;	
	
	private BeCPGSearchService beCPGSearchService;
	
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
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService){
		this.mimetypeService = mimetypeService;
	}				
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
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
    	
		if(nodeType == null){
			return new ArrayList<NodeRef>();
		}		
		
		String query = getQueryReportTpl(reportType, nodeType, true);							
		
		return beCPGSearchService.unProtLuceneSearch(query, null, RepoConsts.MAX_RESULTS_NO_LIMIT);
	}
	
	/**
	 * Get the system report template
	 */
	@Override
	public NodeRef getSystemReportTemplate(ReportType reportType, QName nodeType, String tplName) {    	  
    	
		String query = getQueryReportTpl(reportType, nodeType, true);		
		query += LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, tplName, LuceneHelper.Operator.AND);
		
        List<NodeRef> tplsNodeRef = beCPGSearchService.unProtLuceneSearch(query, null, RepoConsts.MAX_RESULTS_SINGLE_VALUE);       
        return tplsNodeRef.size() > 0 ? tplsNodeRef.get(0) : null;        
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
		
		if(nodeType == null){
			logger.warn("suggestUserReportTemplates: nodeType is null, exit.");
			return new ArrayList<NodeRef>();
		}		
    	
		String query = getQueryReportTpl(reportType, nodeType, false);					
		query += LuceneHelper.getCondContainsValue(ContentModel.PROP_NAME, tplName, LuceneHelper.Operator.AND);
		
		return beCPGSearchService.unProtLuceneSearch(query, null, RepoConsts.MAX_RESULTS_NO_LIMIT);
	}
	
	/**
	 * Get the report template of the product type by name
	 *
	 * @param nodeType the node type
	 * @param tplName the tpl name
	 * @return the user report templates
	 * @param:productType
	 * @param:tplName the name of the template or starting by
	 */
	@Override
	public NodeRef getUserReportTemplate(ReportType reportType, QName nodeType, String tplName) {
		
		if(nodeType == null){
			return null;
		}		
    	
		String query = getQueryReportTpl(reportType, nodeType, false);
		query += LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, tplName, LuceneHelper.Operator.AND);
		
		List<NodeRef> tplsNodeRef = beCPGSearchService.unProtLuceneSearch(query, null, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return tplsNodeRef.size() > 0 ? tplsNodeRef.get(0) : null;		
	}
	
	/**
	 * Create the rptdesign node for the report
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 * @param reportType
	 * @param nodeType
	 * @param isSystemTpl
	 * @param isDefaultTpl
	 * @param overrideTpl
	 * @return
	 * @throws IOException
	 */
	@Override
	public NodeRef createTplRptDesign(NodeRef parentNodeRef, 
										String tplName, 
										String tplFilePath, 
										ReportType reportType, 
										ReportFormat reportFormat,
										QName nodeType, 
										boolean isSystemTpl, 
										boolean isDefaultTpl, 
										boolean overrideTpl) throws IOException{
		
		NodeRef reportTplNodeRef = null;
		ClassPathResource resource = new ClassPathResource(tplFilePath);
		
		if(resource.exists()){
			
			reportTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,  tplName);
			
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, tplName);	    		
			properties.put(ReportModel.PROP_REPORT_TPL_TYPE, reportType);
			properties.put(ReportModel.PROP_REPORT_TPL_FORMAT, reportFormat);
			properties.put(ReportModel.PROP_REPORT_TPL_CLASS_NAME, nodeType);
			properties.put(ReportModel.PROP_REPORT_TPL_IS_SYSTEM, isSystemTpl);
			properties.put(ReportModel.PROP_REPORT_TPL_IS_DEFAULT, isDefaultTpl);				
			
			if(reportTplNodeRef != null){
				
				if(overrideTpl){
					logger.debug("override report Tpl, name: " + tplName);
					
					nodeService.setProperties(reportTplNodeRef, properties);
				}
				else{
					return reportTplNodeRef;
				}
			}
			else{
				logger.debug("Create report Tpl, name: " + tplName);
				
				reportTplNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
						(String)properties.get(ContentModel.PROP_NAME)), 
						ReportModel.TYPE_REPORT_TPL, properties).getChildRef();
			}
			
	    	ContentWriter writer = contentService.getWriter(reportTplNodeRef, ContentModel.PROP_CONTENT, true);
	    	
	    	String mimetype = mimetypeService.guessMimetype(tplFilePath);
			ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
	        Charset charset = charsetFinder.getCharset(resource.getInputStream(), mimetype);
	        String encoding = charset.name();

	    	writer.setMimetype(mimetype);
	    	writer.setEncoding(encoding);
	    	writer.putContent(resource.getInputStream());	
		}		
		
		return reportTplNodeRef;
	}

	/**
	 * Create a ressource for the report
	 * @param parentNodeRef
	 * @param xmlFilePath
	 * @param overrideRessource
	 * @throws IOException
	 */
	@Override
	public void createTplRessource(NodeRef parentNodeRef, String xmlFilePath, boolean overrideRessource) throws IOException{
		
		ClassPathResource resource = new ClassPathResource(xmlFilePath);
    	if(resource.exists()){
    	
    		NodeRef xmlReportTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,  resource.getFilename());
    		
    		if(xmlReportTplNodeRef == null || overrideRessource){
    		
    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        		properties.put(ContentModel.PROP_NAME, resource.getFilename());
        		NodeRef fileNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
            	
        		ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
            	
        		String mimetype = mimetypeService.guessMimetype(xmlFilePath);
        		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
        		BufferedInputStream bis = new BufferedInputStream(resource.getInputStream());
        		Charset charset = charsetFinder.getCharset(bis, mimetype);
        		String encoding = charset.name();

            	writer.setMimetype(mimetype);
            	writer.setEncoding(encoding);
            	writer.putContent(resource.getInputStream());
    		}	        		
    	}
    	else{
    		logger.error("Resource not found. Path: " + xmlFilePath);
    	}
	}

	@Override
	public List<NodeRef> cleanDefaultTpls(List<NodeRef> tplsNodeRef) {
		
		List<NodeRef> defaultTplsNodeRef = new ArrayList<NodeRef>();
		NodeRef userDefaultTplNodeRef = null;
		
		for(NodeRef tplNodeRef : tplsNodeRef){
			
			Boolean isDefault = (Boolean)nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT);
			Boolean isSystem = (Boolean)nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM);
			
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
			if(ReportType.ExportSearch.equals(reportType)){
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
	
	private String getQueryReportTpl(ReportType reportType, QName nodeType, boolean isSystem){
		
		String query = String.format(QUERY_REPORTTEMPLATE, reportType, isSystem);				
		
		// nodeType
		if(nodeType == null){
			query += LuceneHelper.getCondIsNullValue(ReportModel.PROP_REPORT_TPL_CLASS_NAME, LuceneHelper.Operator.AND);
		}
		else{
			query += LuceneHelper.getCondEqualValue(ReportModel.PROP_REPORT_TPL_CLASS_NAME, nodeType.toString(), LuceneHelper.Operator.AND);
		}
		
		return query;
	}	
}
