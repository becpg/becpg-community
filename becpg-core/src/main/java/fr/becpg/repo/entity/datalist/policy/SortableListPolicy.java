/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;

/**
 * The Class SortableListPolicy.
 * 
 * @author querephi
 */
public class SortableListPolicy extends TransactionListenerAdapter implements NodeServicePolicies.OnUpdatePropertiesPolicy, 
										   NodeServicePolicies.OnAddAspectPolicy, 
										   NodeServicePolicies.OnDeleteNodePolicy{

	private static final String KEY_DIRTY_NODES = "dirtyNodes";
	
	private static Log logger = LogFactory.getLog(SortableListPolicy.class);

	private PolicyComponent policyComponent;

	private DataListSortService dataListSortService;
	
	private NodeService nodeService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private TransactionListener transactionListener;
	
	private TransactionService transactionService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * Inits the.
	 */
	public void init() {
		logger.debug("Init DepthLevelListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onDeleteNode", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL, 
				new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));		
		
		logger.debug("Init SortableListPolicy...");		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, 
				new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
		
		// transaction listeners
		//this.transactionListener = new SortableListPolicyTransactionListener();
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		logger.debug("SortableListPolicy.onUpdateProperties");
		//createNode
		if(before.size()== 0){
			// nothing to do, work is done in addAspect, otherwise it duplicates nodeRef in lucene index !!!
			return;
		}
		
		NodeRef beforeParentLevel = (NodeRef) before.get(BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef afterParentLevel = (NodeRef) after.get(BeCPGModel.PROP_PARENT_LEVEL);
		
		if(logger.isDebugEnabled()){
			logger.debug("call SortableListPolicy");
		}
		
		// has changed ?
		boolean hasChanged = false;
		if (afterParentLevel != null && !afterParentLevel.equals(beforeParentLevel)) {				
			hasChanged = true;
		}else if(beforeParentLevel != null && !beforeParentLevel.equals(afterParentLevel)){//parentLevel is null
			hasChanged = true;
		}
		else{
			hasChanged = false;
		}
		
		if(hasChanged){		
			
			dataListSortService.computeDepthAndSort(nodeRef);
			
//			// Bind the listener to the transaction
//			AlfrescoTransactionSupport.bindListener(transactionListener);
//			// Get the set of nodes read
//			@SuppressWarnings("unchecked")
//			Set<NodeRef> dirtyNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_DIRTY_NODES);
//			if (dirtyNodeRefs == null) {
//				dirtyNodeRefs = new LinkedHashSet<NodeRef>(5);
//				AlfrescoTransactionSupport.bindResource(KEY_DIRTY_NODES, dirtyNodeRefs);
//			}
//			dirtyNodeRefs.add(nodeRef);
		}		
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {

		logger.debug("SortableListPolicy.onAddAspect: " + aspect);		
		
		// try to avoid to do two times the work, otherwise it duplicates nodeRef in lucene index !!!
		if ((aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST) && 
				!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL) && 
				nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null) 
				|| (aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL) && nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null)) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("Add sortable aspect policy ");
			}
	
			dataListSortService.computeDepthAndSort(nodeRef);
			
//			// Bind the listener to the transaction
//			AlfrescoTransactionSupport.bindListener(transactionListener);
//			// Get the set of nodes read
//			@SuppressWarnings("unchecked")
//			Set<NodeRef> dirtyNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_DIRTY_NODES);
//			if (dirtyNodeRefs == null) {
//				dirtyNodeRefs = new LinkedHashSet<NodeRef>(5);
//				AlfrescoTransactionSupport.bindResource(KEY_DIRTY_NODES, dirtyNodeRefs);
//			}
//			dirtyNodeRefs.add(nodeRef);
		}
	}	
	
	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
				
		logger.debug("SortableListPolicy.onDeleteNode");
		
		try{
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			dataListSortService.deleteChildrens(childRef.getParentRef(), childRef.getChildRef());
		}
		finally{
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
		}			
	}
	
	/*
	 * Doesn't work since children are sorted twice !!! when sorting parent and when sorting child itself
	 */
//	private class SortableListPolicyTransactionListener extends TransactionListenerAdapter {
//		
//		@Override
//		public void afterCommit() {
//
//			@SuppressWarnings("unchecked")
//			final Set<NodeRef> nodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_DIRTY_NODES);
//			
//			if (nodeRefs != null) {
//
//                RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>(){
//                	
//                    @Override
//					public Object execute(){                                   
//
//                    	logger.debug("nodeRefs: " + nodeRefs);
//            			for (NodeRef nodeRef : nodeRefs) {					            					
//        					dataListSortService.computeDepthAndSort(nodeRef);					
//            			}
//                    	
//                        return null;
//                    }
//                };
//                transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);                
//			}
//		}
//	}
}
