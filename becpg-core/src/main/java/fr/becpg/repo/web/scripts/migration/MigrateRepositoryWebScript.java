/*
 * 
 */
package fr.becpg.repo.web.scripts.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

	/** The search service. */
	private SearchService searchService;
	
	
	private BehaviourFilter policyBehaviourFilter;
	
	private NodeService mlNodeService;
		   					
	
	
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
	
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("start migration");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	

    	String pagination = templateArgs.get(PARAM_PAGINATION);
    	Integer iPagination = (pagination != null && !pagination.isEmpty()) ? Integer.parseInt(pagination) : null;
		
    	// migration ingMLName
		QName ingMLNameQName = QName.createQName(BeCPGModel.BECPG_URI, "ingMLName");
		migrateProperty(iPagination, " +TYPE:\"bcpg:ing\" ", ingMLNameQName, BeCPGModel.PROP_LEGAL_NAME, mlNodeService);
    }
	
	private void migrateProperty(Integer iPagination, String query, QName oldProperty, QName newProperty, NodeService nodeService){

		logger.info("migrateProperty");
		
		List<NodeRef> items = new ArrayList<NodeRef>();
    	ResultSet resultSet = null;
    	
    	SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);	                         
        
    	try{
    		resultSet = searchService.query(sp);
    		if(resultSet.length() > 0){
    			items = resultSet.getNodeRefs();        			
    		}
    	}	       
    	catch(Exception e){
    		logger.error("Failed to get items", e);
    	}
    	finally{
    		if(resultSet != null)
    			resultSet.close();
    	}        
    	
    	logger.info("items to migrate: " + items.size());    	
    	
    	policyBehaviourFilter.disableAllBehaviours();
    	        	
    	try{
    		int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
    		for(int cnt=0 ; cnt < maxCnt ; cnt++){
        		
        		final NodeRef nodeRef = items.get(cnt);        		
        		Serializable value = nodeService.getProperty(nodeRef, oldProperty);
        		
        		if(value != null){
        			logger.info("node: " + nodeRef + " - change property: " + oldProperty + " - value: " + value);
        			nodeService.setProperty(nodeRef, newProperty, value);
        			nodeService.removeProperty(nodeRef, oldProperty);
        		}        		   	
        	}
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}    		      
	}	
	
}
