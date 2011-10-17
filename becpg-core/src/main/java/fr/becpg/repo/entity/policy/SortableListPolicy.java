/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class SortableListPolicy.
 *
 * @author querephi
 */
public class SortableListPolicy implements NodeServicePolicies.OnAddAspectPolicy{
			
	//private static final String QUERY_LIST_ITEMS_BY_SORT = "PARENT:\"%s\" AND +@bcpg\\:sort:[%s TO %s]";
	private static final String QUERY_LIST_ITEMS = "PARENT:\"%s\"";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(SortableListPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The search service. */
	private BeCPGSearchService beCPGSearchService;
	
	private BehaviourFilter policyBehaviourFilter;
	
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

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init SortableListPolicy...");
		//policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, new JavaBehaviour(this, "onAddAspect"));
	}

// SHOULD BE DONE BY UI
//	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
//			Map<QName, Serializable> after) {
//		
//		logger.debug("onUpdateProperties");
//		
//		NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
//		Integer beforeSort = (Integer)before.get(BeCPGModel.PROP_SORT);
//		Integer afterSort = (Integer)after.get(BeCPGModel.PROP_SORT);
//		
//		logger.debug("onUpdateProperties, sort beforeSort: " + beforeSort + "afterSort: " + afterSort);
//		
//		if(beforeSort != null && afterSort != null && !beforeSort.equals(afterSort)){						
//						
//			int min;
//			int max;
//			int sortIndex;						
//			
//			if(afterSort < beforeSort){
//				min = afterSort;
//				max = beforeSort;
//				sortIndex = min+1;
//			}
//			else{
//				min = beforeSort;
//				max = afterSort;
//				sortIndex = min;
//			}
//						
//			String query = String.format(QUERY_LIST_ITEMS_BY_SORT, parentNodeRef, min, max);
//			Map<String, Boolean> sort = new HashMap<String, Boolean>();
//			sort.put("@" + BeCPGModel.PROP_SORT, true);
//			int size = max - min;
//			List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, sort, size);			
//			
//			try
//	        {
//	            policyBehaviourFilter.disableBehaviour(BeCPGModel.PROP_SORT);
//	            
//	            for(int z_idx=0 ; z_idx<listItems.size() ; z_idx++){					
//					
//					if(!nodeRef.equals(listItems.get(z_idx))){
//					
//						nodeService.setProperty(listItems.get(z_idx), BeCPGModel.PROP_SORT, sortIndex);
//						sortIndex++;
//					}				
//				}
//	        }
//	        finally
//	        {
//	        	policyBehaviourFilter.enableBehaviour(BeCPGModel.PROP_SORT);
//	        }
//		}
//		
//	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {
		
		logger.debug("###onAddAspect");
		
		if(aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST)){
			
			Integer sortIndex = (Integer)nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);
			
			logger.debug("###onAddAspect, sortIndex: " + sortIndex);
			
			if(sortIndex == null){
				
				logger.debug("###onAddAspect, sortIndex is null");
				
				NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
				String query = String.format(QUERY_LIST_ITEMS, parentNodeRef);
				
				logger.debug("###onAddAspect 1");
				
				Map<String, Boolean> sort = new HashMap<String, Boolean>();
				sort.put("@" + BeCPGModel.PROP_SORT, false);
				
				logger.debug("###onAddAspect 2");
				
				List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, sort, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
				
				logger.debug("###onAddAspect 3");
				
				if(listItems.isEmpty()){
					sortIndex = 1;
				}
				else if(listItems.size() == 1){
					
					NodeRef lastIndexNodeRef = listItems.get(0);
					sortIndex = (Integer)nodeService.getProperty(lastIndexNodeRef, BeCPGModel.PROP_SORT) + 1;
				}
				else{
					logger.error("Returned several results. Query: " + query);
				}
				
				logger.debug("###Set sort property. sortIndex: " + sortIndex);
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, sortIndex);
			}
		}		
	}
	
	
}
