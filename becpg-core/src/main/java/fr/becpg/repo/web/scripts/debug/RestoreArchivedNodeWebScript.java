/*
 * 
 */
package fr.becpg.repo.web.scripts.debug;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.RepoConsts;

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
		
		
		/** The node service. */
		private NodeService nodeService;
		
		/** The search service. */
		private SearchService searchService;
		
		
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
