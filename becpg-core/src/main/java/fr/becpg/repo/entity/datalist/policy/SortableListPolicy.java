/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class SortableListPolicy.
 *
 * @author querephi
 */
public class SortableListPolicy implements NodeServicePolicies.OnAddAspectPolicy{
			
	//private static final String QUERY_LIST_ITEMS_BY_SORT = "+PARENT:\"%s\" AND +@bcpg\\:sort:[%s TO %s]";
	private static final String QUERY_LIST_ITEMS = "+PARENT:\"%s\"";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(SortableListPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The search service. */
	private BeCPGSearchService beCPGSearchService;
	
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

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}
	
	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init SortableListPolicy...");
		//policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, new JavaBehaviour(this, "onAddAspect"));
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {
		
		if(aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST)){
			
			Integer sortIndex = (Integer)nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);
			
			if(sortIndex == null){
				
				NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
				String query = String.format(QUERY_LIST_ITEMS, parentNodeRef);
				
				Map<String, Boolean> sort = new HashMap<String, Boolean>();
				sort.put("@" + BeCPGModel.PROP_SORT, false);
				
				List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, sort, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
				
				if(listItems.isEmpty()){
					sortIndex = 1;
				}
				else if(listItems.size() == 1){
					
					NodeRef lastIndexNodeRef = listItems.get(0);
					sortIndex = (Integer)nodeService.getProperty(lastIndexNodeRef, BeCPGModel.PROP_SORT);

					if(sortIndex != null){
						sortIndex++;
					}
					else{
						fixSortableList(parentNodeRef);
					}
				}
				else{
					logger.error("Returned several results. Query: " + query);
				}
				
				if(sortIndex != null){
					logger.debug("set property sort: " + sortIndex + " - node: " + nodeRef);
					nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, sortIndex);
				}				
			}
		}		
	}	
	
	private void fixSortableList(NodeRef parentNodeRef){
	
		Integer sortIndex = 0;
		
		String query = String.format(QUERY_LIST_ITEMS, parentNodeRef);		
		Map<String, Boolean> sort = new HashMap<String, Boolean>();
		sort.put("@" + ContentModel.PROP_CREATED, true);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, sort, RepoConsts.MAX_RESULTS_NO_LIMIT);
		
		for(NodeRef listItem : listItems){
		
			sortIndex++;
			nodeService.setProperty(listItem, BeCPGModel.PROP_SORT, sortIndex);			
		}
		
		logger.info("FixSortableList. parentNodeRef: " + parentNodeRef + ", last sortIndex: " + sortIndex);		
	}
	
}
