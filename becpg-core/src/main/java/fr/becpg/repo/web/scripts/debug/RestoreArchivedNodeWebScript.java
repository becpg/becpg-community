/*
 * 
 */
package fr.becpg.repo.web.scripts.debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class RestoreArchivedNodeWebScript.
 *
 * @author querephi
 */
public class RestoreArchivedNodeWebScript extends AbstractWebScript
{	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(RestoreArchivedNodeWebScript.class);
		
		//request parameter names
		/** The Constant PARAM_STORE_TYPE. */
		private static final String PARAM_STORE_TYPE = "store_type";
		
		/** The Constant PARAM_STORE_ID. */
		private static final String PARAM_STORE_ID = "store_id";
		
		/** The Constant PARAM_ID. */
		private static final String PARAM_ID = "id";
		
		/** The node service. */
		private NodeService nodeService;
		
		/** The search service. */
		private SearchService searchService;
		
		/** The file folder service. */
		private FileFolderService fileFolderService;
		
		/** The transaction service. */
		private TransactionService transactionService;
		
		private Repository repositoryHelper;
		
		private DictionaryDAO dictionaryDAO;
		
		private RepoService repoService;
		
		private BehaviourFilter policyBehaviourFilter;
			   					
		/**
		 * Sets the node service.
		 *
		 * @param nodeService the new node service
		 */
		public void setNodeService(NodeService nodeService) {
			this.nodeService = nodeService;
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
		 * Sets the file folder service.
		 *
		 * @param fileFolderService the new file folder service
		 */
		public void setFileFolderService(FileFolderService fileFolderService) {
			this.fileFolderService = fileFolderService;
		}
		
		/**
		 * Sets the transaction service.
		 *
		 * @param transactionService the new transaction service
		 */
		public void setTransactionService(TransactionService transactionService) {
			this.transactionService = transactionService;
		}
				
		public void setRepositoryHelper(Repository repositoryHelper) {
			this.repositoryHelper = repositoryHelper;
		}
		
		public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
			this.dictionaryDAO = dictionaryDAO;
		}
		
		public void setRepoService(RepoService repoService) {
			this.repoService = repoService;
		}

		public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
			this.policyBehaviourFilter = policyBehaviourFilter;
		}

		/* (non-Javadoc)
		 * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
		 */
		@Override
		public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
	    {
	    	logger.debug("start restore archived node webscript");
	    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
	    	String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
	    	
			NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
			
			fillMissingCreator();
			
	    }
		
		private void fillMissingCreator(){

			List<NodeRef> items = new ArrayList<NodeRef>();
        	ResultSet resultSet = null;
        	
        	SearchParameters sp = new SearchParameters();
            sp.addStore(RepoConsts.SPACES_STORE);
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(" +TYPE:\"bcpg:product\" +ISNULL:\"cm:creator\"");	                         
            
        	try{
        		resultSet = searchService.query(sp);
        		if(resultSet.length() > 0){
        			items = resultSet.getNodeRefs();        			
        		}
        	}	       
        	catch(Exception e){
        		logger.error("Failed to get productListItems", e);
        	}
        	finally{
        		if(resultSet != null)
        			resultSet.close();
        	}        
        	        	
        	logger.debug("to migrate: " + items.size());
        	
        	for(int cnt=0 ; cnt < items.size() ; cnt++){
        		
        		NodeRef nodeRef = items.get(cnt);
        		
        		try{	
    	            policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
    	            
    	            logger.debug("Add createor to nodeRef: " + nodeRef);
    	            nodeService.setProperty(nodeRef, ContentModel.PROP_CREATOR, "admin");				            
    	        }
    	        finally{
    	        	policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);			        	
    	        }	 
        	}	          	
		}
		
		
}
