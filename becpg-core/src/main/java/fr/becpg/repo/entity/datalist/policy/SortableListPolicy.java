/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class SortableListPolicy.
 * 
 * @author querephi
 */
@Service
public class SortableListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, 
										   NodeServicePolicies.OnAddAspectPolicy, 
										   NodeServicePolicies.OnDeleteNodePolicy,
										   CopyServicePolicies.OnCopyNodePolicy,
										   CopyServicePolicies.OnCopyCompletePolicy{

	private static Log logger = LogFactory.getLog(SortableListPolicy.class);


	private DataListSortService dataListSortService;
	private BeCPGSearchService beCPGSearchService;
	
	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
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
		}		
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {

		logger.debug("SortableListPolicy.onAddAspect: " + aspect);		
		
		// try to avoid to do two times the work, otherwise it duplicates nodeRef in lucene index !!!
		if (nodeService.exists(nodeRef) && (aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST) && 
				!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL) && 
				nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null) 
				|| (aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL) && nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null)) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("Add sortable aspect policy ");
			}
	
			dataListSortService.computeDepthAndSort(nodeRef);
		}
	}	
	
	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
				
		logger.debug("SortableListPolicy.onDeleteNode");
		
		if(nodeService.exists(childRef.getParentRef()) && nodeService.exists(childRef.getChildRef())){
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

		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails,
				Map<QName, Serializable> properties) {		
			
			logger.debug("DepthLevelAspectCopyBehaviourCallback.getCopyProperties()");			
			NodeRef sourceParentNodeRef = (NodeRef)copyDetails.getSourceNodeProperties().get(BeCPGModel.PROP_PARENT_LEVEL);
			NodeRef targetParentNodeRef = (NodeRef)properties.get(BeCPGModel.PROP_PARENT_LEVEL);
			
			// parent equals -> need to update the parent of copied node
			if(sourceParentNodeRef != null && sourceParentNodeRef.equals(targetParentNodeRef)){
				
				// we assume sort is keeped during copy
				Integer sourceParentSort = (Integer)nodeService.getProperty(sourceParentNodeRef, BeCPGModel.PROP_SORT);
				
				String query = LuceneHelper.getCondParent(copyDetails.getTargetParentNodeRef(), null);
				query += LuceneHelper.getCondType(nodeService.getType(copyDetails.getSourceNodeRef()));
				query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_SORT, sourceParentSort != null ? sourceParentSort.toString() : "", LuceneHelper.Operator.AND);
				List<NodeRef> result = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
				
				if(!result.isEmpty()){
					NodeRef copiedParentNodeRef = result.get(0);
					logger.debug("update the parent of copied node " + copyDetails.getTargetNodeRef() + " with value " + copiedParentNodeRef);
					properties.put(BeCPGModel.PROP_PARENT_LEVEL, copiedParentNodeRef);
				}
			}				
			return properties;
		}
	}
}
