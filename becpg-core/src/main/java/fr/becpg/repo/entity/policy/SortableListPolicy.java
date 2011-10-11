/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class CodePolicy.
 *
 * @author querephi
 */
public class SortableListPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {
			
	private static final String QUERY_LIST_ITEMS = "PARENT:\"%s\" AND +@bcpg\\:sort:[%s TO %s]";
	
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
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, new JavaBehaviour(this, "onUpdateProperties"));
	}

	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {
		
		NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
		
		if(after.containsKey(BeCPGModel.PROP_SORT)){
			
			int beforeSort = (Integer)before.get(BeCPGModel.PROP_SORT);
			int afterSort = (Integer)after.get(BeCPGModel.PROP_SORT);
			int min;
			int max;
			int sortIndex;
			
			if(afterSort < beforeSort){
				min = afterSort;
				max = beforeSort;
				sortIndex = min+1;
			}
			else{
				min = beforeSort;
				max = afterSort;
				sortIndex = min;
			}
						
			String query = String.format(QUERY_LIST_ITEMS, parentNodeRef, min, max);
			String [] sort = {BeCPGModel.PROP_SORT.toString()};
			int size = max - min;
			List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, sort, size);			
			
			for(int z_idx=0 ; z_idx<=size ; z_idx++){					
									
				if(!nodeRef.equals(listItems.get(z_idx))){
				
					nodeService.setProperty(listItems.get(z_idx), BeCPGModel.PROP_SORT, sortIndex);
					sortIndex++;
				}				
			}			
		}
		
	}
	
	
}
