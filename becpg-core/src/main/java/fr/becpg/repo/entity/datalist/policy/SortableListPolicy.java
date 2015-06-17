/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * The Class SortableListPolicy.
 * 
 * @author querephi
 */
public class SortableListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, 
										   NodeServicePolicies.OnAddAspectPolicy, 
										   NodeServicePolicies.OnDeleteNodePolicy,
										   CopyServicePolicies.OnCopyNodePolicy,
										   CopyServicePolicies.OnCopyCompletePolicy{

	
	private static final Log logger = LogFactory.getLog(SortableListPolicy.class);

	private DataListSortService dataListSortService;
	
	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init DepthLevelListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onDeleteNode", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL, 
				new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));		
		
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL, 
				new JavaBehaviour(this, "getCopyCallback"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL, 
				new JavaBehaviour(this, "onCopyComplete"));
		
		logger.debug("Init SortableListPolicy...");		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, 
				new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));	
		
		//super.disableOnCopyBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		logger.debug("SortableListPolicy.onUpdateProperties");
		//createNode
		if(before.isEmpty()){
			// nothing to do, work is done in addAspect, otherwise it duplicates nodeRef in lucene index !!!
			return;
		}
		
		NodeRef beforeParentLevel = (NodeRef) before.get(BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef afterParentLevel = (NodeRef) after.get(BeCPGModel.PROP_PARENT_LEVEL);
		
		if(logger.isDebugEnabled()){
			logger.debug("call SortableListPolicy");
		}
		
		// has changed ?
		boolean hasChanged;
		if (afterParentLevel != null && !afterParentLevel.equals(beforeParentLevel)) {				
			hasChanged = true;
		}else if(beforeParentLevel != null && !beforeParentLevel.equals(afterParentLevel)){//parentLevel is null
			hasChanged = true;
		}
		else{
			hasChanged = false;
		}
		
		if(hasChanged){		
			logger.debug("onUpdateProperties has changed");
			queueNode(nodeRef);
		}		
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {

		// try to avoid to do two times the work, otherwise it duplicates nodeRef in lucene index !!!
		if (nodeService.exists(nodeRef)) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("onAddAspect");
			}
			
			boolean addInQueue = false;
			if(nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null && aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST) && 
					!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)){
				addInQueue = true;
				if (logger.isDebugEnabled()) {
					logger.debug("Add sortable aspect policy ");
				}
			}
			
			if((nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null || nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL) == null) && 
					aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL)){
				addInQueue = true;
				// queue parent before
				NodeRef parentNodeRef = (NodeRef)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
				if(parentNodeRef != null && 
						(nodeService.getProperty(parentNodeRef, BeCPGModel.PROP_SORT) == null || nodeService.getProperty(parentNodeRef, BeCPGModel.PROP_DEPTH_LEVEL) == null)){
					queueNode(parentNodeRef);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Add depthLevel aspect policy ");
				}
			}
	
			if(addInQueue){
				queueNode(nodeRef);
			}					
		}
	}	
	
	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		
		Set<NodeRef> deletedNodes = new HashSet<>();		
		for (NodeRef nodeRef : pendingNodes) {
			if(!nodeService.exists(nodeRef)){
				deletedNodes.add(nodeRef);				
			}
		}
		pendingNodes.removeAll(deletedNodes);
		
		dataListSortService.computeDepthAndSort(pendingNodes);
	}
	
	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
				
		logger.debug("SortableListPolicy.onDeleteNode");
		
		// if folder is deleted, all children are
		if(nodeService.exists(childRef.getParentRef())){
			try{
				policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
				dataListSortService.deleteChildrens(childRef.getParentRef(), childRef.getChildRef());
			}
			finally{
				policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			}
		}				
	}

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new DepthLevelAspectCopyBehaviourCallback(policyBehaviourFilter);
	}
	
	@Override
	public void onCopyComplete(QName classRef,
            NodeRef sourceNodeRef,
            NodeRef destinationRef,
            boolean copyToNewNode,
            Map<NodeRef, NodeRef> copyMap){
		
		logger.debug("onCopyComplete destinationRef " + destinationRef);
		
		NodeRef sourceParentLevelNodeRef = (NodeRef)nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_PARENT_LEVEL);		
		
		// parent equals -> need to update the parent of copied node
		if(sourceParentLevelNodeRef != null){
			
			NodeRef targetParentLevelNodeRef = (NodeRef)nodeService.getProperty(destinationRef, BeCPGModel.PROP_PARENT_LEVEL);
			if(sourceParentLevelNodeRef.equals(targetParentLevelNodeRef)){
			
				// we assume sort is keeped during copy
				Integer sourceParentSort = (Integer)nodeService.getProperty(sourceParentLevelNodeRef, BeCPGModel.PROP_SORT);
				
				NodeRef targetParentNodeRef = nodeService.getPrimaryParent(destinationRef).getParentRef();
				
				NodeRef copiedParentNodeRef = BeCPGQueryBuilder.createQuery()
							.parent(targetParentNodeRef)
							.ofType(nodeService.getType(sourceParentLevelNodeRef))
							.andPropEquals(BeCPGModel.PROP_SORT, sourceParentSort != null ? sourceParentSort.toString() : null)
							.inDB()
							.singleValue();
				
				if(copiedParentNodeRef!=null){
					logger.debug("update the parent of copied node " + targetParentLevelNodeRef + " with value " + copiedParentNodeRef);
					nodeService.setProperty(destinationRef, BeCPGModel.PROP_PARENT_LEVEL, copiedParentNodeRef);
				}
				else{
					logger.warn("DepthLevelAspectCopyBehaviourCallback : parent not found.");
				}
			}			
		}
		
		policyBehaviourFilter.enableBehaviour(destinationRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
    }
	
	private class DepthLevelAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback {
		
		private final BehaviourFilter policyBehaviourFilter;
        
        private DepthLevelAspectCopyBehaviourCallback(BehaviourFilter policyBehaviourFilter)
        {
            this.policyBehaviourFilter = policyBehaviourFilter;
        }
        
		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {				
			policyBehaviourFilter.disableBehaviour(copyDetails.getTargetNodeRef(), BeCPGModel.ASPECT_DEPTH_LEVEL);
			return true;
		}
	}
}
