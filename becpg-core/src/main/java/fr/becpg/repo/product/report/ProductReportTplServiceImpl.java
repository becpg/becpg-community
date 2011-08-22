package fr.becpg.repo.product.report;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.product.data.ProductData;


/**
 * Class used to manage product report templates
 * @author querephi
 *
 */
public class ProductReportTplServiceImpl implements ProductReportTplService {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductReportTplServiceImpl.class);
	
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
	public List<NodeRef> getSystemReportTemplates(SystemProductType systemProductType) {    	  
    	
		List<NodeRef> tplsNodeRef = new ArrayList<NodeRef>();    	
		
		if(systemProductType == SystemProductType.Unknown){
			return tplsNodeRef;
		}
		
		StringBuilder queryPath = new StringBuilder(128);
		queryPath.append(String.format(RepoConsts.PATH_QUERY_PRODUCT_REPORTTEMPLATES, true));
					
		//productType, get constraint value
		// +@cm\\:localName:%s											
		queryPath.append(LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PRODUCT_TYPE, systemProductType.toString(), LuceneHelper.Operator.AND));
		
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
	 * @param systemProductType the system product type
	 * @param tplName the tpl name
	 * @return the user report templates
	 * @param:productType
	 * @param:tplName the name of the template or starting by
	 */
	@Override
	public List<NodeRef> getUserReportTemplates(SystemProductType systemProductType,
			String tplName) {
		
		List<NodeRef> tplsNodeRef = new ArrayList<NodeRef>();
		
		if(systemProductType == SystemProductType.Unknown){
			return null;
		}		
    	
    	StringBuilder queryPath = new StringBuilder(128);
		queryPath.append(String.format(RepoConsts.PATH_QUERY_PRODUCT_REPORTTEMPLATES, false));
					
		//productType, get constraint value
		// +@cm\\:localName:%s											
		queryPath.append(LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PRODUCT_TYPE, systemProductType.toString(), LuceneHelper.Operator.AND));
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
	 * Get the report templates to generate.
	 *
	 * @param productNodeRef the product node ref
	 * @return the report tpls to generate
	 */
	@Override
	public List<NodeRef> getReportTplsToGenerate(NodeRef productNodeRef) {
		
		List<NodeRef> tplsToReturnNodeRef = new ArrayList<NodeRef>();
		
		// system reports
		QName typeQName = nodeService.getType(productNodeRef);
		SystemProductType systemProductType = SystemProductType.valueOf(typeQName);
		List<NodeRef> tplsNodeRef = getSystemReportTemplates(systemProductType);										
		
		for(NodeRef tplNodeRef : tplsNodeRef){			
			
			tplsToReturnNodeRef.add(tplNodeRef);
		}
		
		// selected user reports
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(productNodeRef, BeCPGModel.ASSOC_PRODUCT_REPORT_TEMPLATES);
		
		for(AssociationRef assocRef : assocRefs){
			
			NodeRef tplNodeRef = assocRef.getTargetRef();			
			tplsToReturnNodeRef.add(tplNodeRef);
		}		
		
		return tplsToReturnNodeRef;		
	}
	
	/**
	 * Create a product report tpl
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 * @param systemProductType
	 * @param isSystemTpl
	 * @param isDefaultTpl
	 * @return
	 * @throws IOException
	 */
	@Override
	public NodeRef createTpl(NodeRef parentNodeRef, String tplName, String tplFilePath, SystemProductType systemProductType, boolean isSystemTpl, boolean isDefaultTpl) throws IOException{						
		
		NodeRef productReportTplNodeRef = null;
		ClassPathResource resource = new ClassPathResource(tplFilePath);
		
		if(resource.exists()){
			
			//create report template folder
		   	logger.debug("create report template folder");
	   		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, tplName);
			properties.put(BeCPGModel.PROP_PRODUCT_TYPE, systemProductType);
			properties.put(BeCPGModel.PROP_PRODUCT_REPORTTEMPLATE_IS_SYSTEM, isSystemTpl);
			properties.put(BeCPGModel.PROP_PRODUCT_REPORTTEMPLATE_IS_DEFAULT, isDefaultTpl);
			
			productReportTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,  (String)properties.get(ContentModel.PROP_NAME));    	
	    	if(productReportTplNodeRef == null){
	    		productReportTplNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_PRODUCT_REPORTTEMPLATE, properties).getChildRef();
	    		
	    		properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, resource.getFilename());
	        	NodeRef fileNodeRef = nodeService.createNode(productReportTplNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
	        	
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
			
			boolean isDefault = (Boolean)nodeService.getProperty(tplNodeRef, BeCPGModel.PROP_PRODUCT_REPORTTEMPLATE_IS_DEFAULT);
			boolean isSystem = (Boolean)nodeService.getProperty(tplNodeRef, BeCPGModel.PROP_PRODUCT_REPORTTEMPLATE_IS_SYSTEM);
			
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
}
