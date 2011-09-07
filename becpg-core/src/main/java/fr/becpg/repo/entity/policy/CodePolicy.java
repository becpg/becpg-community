/*
 * 
 */
package fr.becpg.repo.entity.policy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.AutoNumService;

// TODO: Auto-generated Javadoc
/**
 * The Class CodePolicy.
 *
 * @author querephi
 */
public class CodePolicy implements NodeServicePolicies.OnAddAspectPolicy {
			
	/** The logger. */
	private static Log logger = LogFactory.getLog(CodePolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The auto num service. */
	private AutoNumService autoNumService;
	
	/** The search service. */
	private SearchService searchService;
			
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
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
	 * Sets the auto num service.
	 *
	 * @param autoNumService the new auto num service
	 */
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
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
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init CodePolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_CODE, new JavaBehaviour(this, "onAddAspect"));
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
	 */
	@Override
	public void onAddAspect(NodeRef nodeRef, QName type) {
								

		// check code is already taken. If yes : this object is a copy of an existing node
		Long code = (Long)nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE);
		boolean generateCode = true;
		QName typeQName = nodeService.getType(nodeRef);
		
		if(code != null){
									
			SearchParameters sp = new SearchParameters();
	        sp.addStore(RepoConsts.SPACES_STORE);
	        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
	        sp.setQuery(String.format(RepoConsts.PATH_QUERY_NODE_BY_CODE, typeQName, code));	                
	        
	        ResultSet resultSet =null;
	        
	        try{
		        resultSet = searchService.query(sp);
				
		        if (resultSet.length() == 0){
		        	 generateCode = false;
		        }	        
	        }
	        finally{
	        	if(resultSet != null)
	        		resultSet.close();
	        }
		}
		
		// generate a new code
		if(generateCode){
					
			code = autoNumService.getAutoNumValue(typeQName, BeCPGModel.PROP_CODE);
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_CODE, code);
		}
		else{
			// store autoNum in db
			autoNumService.createOrUpdateAutoNumValue(typeQName, BeCPGModel.PROP_CODE, code);
		}		
	}	
}
