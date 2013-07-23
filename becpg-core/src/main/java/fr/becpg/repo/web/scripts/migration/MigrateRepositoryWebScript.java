/*
 * 
 */
package fr.becpg.repo.web.scripts.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class MigrateRepositoryWebScript.
 *
 * @author querephi
 */
public class MigrateRepositoryWebScript extends AbstractWebScript
{	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(MigrateRepositoryWebScript.class);
				
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_PAGINATION = "pagination";
	
	private static final String ACTION_MIGRATE_PROPERTY = "property";
	
	private static final String PARAM_NODEREF = "nodeRef";
	private static final String ACTION_DELETE_MODEL = "deleteModel";
	
	private static final String PARAM_CONVERT_COMMAND = "convertCommand";
	private static final String ACTION_CREATE_RULE_COMPRESSOR = "createRuleCompressor";
	private static final String ACTION_CLEAN_TRASH = "cleanTrash";	
	
	/** The node service. */
	private NodeService nodeService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private NodeService mlNodeService;
	
	private BeCPGSearchService beCPGSearchService;
	
	private ContentService contentService;
	
	private EntityReportService entityReportService;
	
	private ContentTransformer imageMagickContentTransformer;
	
	private LockService lockService;
	
	private SearchService searchService;
	
	private NodeArchiveService nodeArchiveService;
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setImageMagickContentTransformer(
			ContentTransformer imageMagickContentTransformer) {
		this.imageMagickContentTransformer = imageMagickContentTransformer;
	}

	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNodeArchiveService(NodeArchiveService nodeArchiveService) {
		this.nodeArchiveService = nodeArchiveService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
		logger.debug("start migration");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	

    	String pagination = templateArgs.get(PARAM_PAGINATION);
    	String action = templateArgs.get(PARAM_ACTION);
    	Integer iPagination = (pagination != null && !pagination.isEmpty()) ? Integer.parseInt(pagination) : null;
		
    	if(ACTION_MIGRATE_PROPERTY.equals(action)){
    		// migration ingMLName
    		QName ingMLNameQName = QName.createQName(BeCPGModel.BECPG_URI, "ingMLName");
    		migrateProperty(iPagination, " +TYPE:\"bcpg:ing\" ", ingMLNameQName, BeCPGModel.PROP_LEGAL_NAME, mlNodeService);
    	}
    	else if(ACTION_DELETE_MODEL.equals(action)){
    		NodeRef modelNodeRef = new NodeRef( req.getParameter(PARAM_NODEREF));
    		deleteModel(modelNodeRef);
    	}
    	else if(ACTION_CREATE_RULE_COMPRESSOR.equals(action)){
    		String convertCommand = req.getParameter(PARAM_CONVERT_COMMAND);
    		try{
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				createRuleCompressor(convertCommand);
    		}
			finally{
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			}    		
    	}
    	else if(ACTION_CLEAN_TRASH.equals(action)){
    		cleanTrash();
    	}
    	else{
    		logger.error("Unknown action" + action);
    	}
    }

	private void migrateProperty(Integer iPagination, String query, QName oldProperty, QName newProperty, NodeService nodeService){

		logger.info("migrateProperty");
		
		List<NodeRef> items = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_NO_LIMIT);    	
    	logger.info("items to migrate: " + items.size());    	
    	
    	int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
		for(int cnt=0 ; cnt < maxCnt ; cnt++){
    		
			NodeRef nodeRef = items.get(cnt);
    		
	    	policyBehaviourFilter.disableAllBehaviours();
	    	        	
	    	try{
	    		        		
        		Serializable value = nodeService.getProperty(nodeRef, oldProperty);
        		
        		if(value != null){
        			logger.info("node: " + nodeRef + " - change property: " + oldProperty + " - value: " + value);
        			nodeService.setProperty(nodeRef, newProperty, value);
        			nodeService.removeProperty(nodeRef, oldProperty);
        		}        		   	
	    	}
	    	finally{
	    		policyBehaviourFilter.enableAllBehaviours();
	    	}
		}
	}	
	
	private void deleteModel(NodeRef modelNodeRef){

		logger.info("deleteModel");		
    	policyBehaviourFilter.disableAllBehaviours();
    	        	
    	try{
    		        		
    		mlNodeService.deleteNode(modelNodeRef);      		   	
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}
	}
	
	private void createRuleCompressor(String convertCommand){

		List<NodeRef> products = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:finishedProduct\"", RepoConsts.MAX_RESULTS_NO_LIMIT);
		logger.info("Create rule compressor for '" + products.size() + "' products");
		
		for(NodeRef product : products){
			
			if(nodeService.exists(product)){
				
				NodeRef parentNodeRef = nodeService.getPrimaryParent(product).getParentRef();
				
				NodeRef imagesFolderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));		
				if(imagesFolderNodeRef == null){
					logger.warn("Folder 'Images' doesn't exist.");
					continue;
				}
				
				nodeService.removeAspect(imagesFolderNodeRef, RuleModel.ASPECT_RULES);

			    // compress image
				String imageQuery = String.format("+PARENT:\"%s\" AND +@cm\\:content.mimetype:\"image/jpeg\"", imagesFolderNodeRef);			
				List<NodeRef> images = beCPGSearchService.luceneSearch(imageQuery, RepoConsts.MAX_RESULTS_NO_LIMIT);
				
				for(NodeRef image : images){
											 		    	
	 		        ContentReader contentReader = contentService.getReader(image, ContentModel.PROP_CONTENT);
	 		        ContentWriter contentWriter = contentService.getWriter(image, ContentModel.PROP_CONTENT, true);
	 		    	
	 		        ImageTransformationOptions imageOptions = new ImageTransformationOptions();
	 		        imageOptions.setCommandOptions(convertCommand);
	 		    	imageMagickContentTransformer.transform(contentReader, contentWriter, imageOptions); 	
	 		    	
	 		    	logger.debug("image transformed. initialSize: " + contentReader.getSize() + " - afterSize: " + contentWriter.getSize());					
				}
				
				// force refresh report
				if(!images.isEmpty() && lockService.getLockStatus(product) == LockStatus.NO_LOCK){					
					entityReportService.generateReport(product);									
				}		
			}					
		}
	}
	
	private void cleanTrash(){
		List<NodeRef> nodes = new ArrayList<NodeRef>();
		
		SearchParameters sp = new SearchParameters();
		sp.addStore(nodeArchiveService.getStoreArchiveNode(RepoConsts.SPACES_STORE).getStoreRef());
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.setQuery("@cm\\:name:\"doclib\" OR @cm\\:name:\"webpreview\"");
		sp.setLimit(RepoConsts.MAX_RESULTS_NO_LIMIT);
		sp.setLimitBy(LimitBy.FINAL_SIZE);
		sp.addLocale(Locale.getDefault());
		sp.setPermissionEvaluation(PermissionEvaluationMode.EAGER);
		sp.excludeDataInTheCurrentTransaction(false);
		sp.addSort(SearchParameters.SORT_IN_DOCUMENT_ORDER_DESCENDING);
		ResultSet result = null;
		try {
			result = searchService.query(sp);
			if (result != null) {
				nodes =   new ArrayList<NodeRef>(result.getNodeRefs());
			}
		} finally {
			if (result != null) {
				result.close();
			}			
		}
		
		logger.info("Clean Trash, size: " + nodes.size());		
		nodeArchiveService.purgeArchivedNodes(nodes);
	}

	
}
