/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;

/**
 * The Class SortableListPolicy.
 * 
 * @author querephi
 */
public class SortableListPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, 
										   NodeServicePolicies.OnAddAspectPolicy, 
										   NodeServicePolicies.OnDeleteNodePolicy,
										   NodeServicePolicies.BeforeDeleteNodePolicy{

	private static Log logger = LogFactory.getLog(SortableListPolicy.class);

	private PolicyComponent policyComponent;

	private DataListSortService dataListSortService;
	
	private NodeService nodeService;
	
	private BehaviourFilter policyBehaviourFilter;

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

	/**
	 * Inits the.
	 */
	public void init() {
		logger.debug("Init DepthLevelListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_ENTITYLISTS,
				new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL, new JavaBehaviour(this, "onAddAspect"));
		
		logger.debug("Init SortableListPolicy...");		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, new JavaBehaviour(this, "onAddAspect"));		
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
		}		
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {

		logger.debug("SortableListPolicy.onAddAspect: " + aspect);		
		
		// try to avoid to do two times the work, otherwise it duplicates nodeRef in lucene index !!!
		if ((aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST) && 
				!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL) && 
				nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null) 
				|| aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("Add sortable aspect policy ");
			}
	
			dataListSortService.computeDepthAndSort(nodeRef);
		}
	}	
	
	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
		
		logger.debug("SortableListPolicy.onDeleteNode");
		
//		if(nodeService.hasAspect(childRef.getChildRef(), BeCPGModel.ASPECT_ENTITYLISTS)){
//			//activate again policy
//			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
//		}
//		else{
			dataListSortService.deleteChildrens(childRef.getParentRef(), childRef.getChildRef());
//		}				
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		
//		if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS)){
//			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
//		}		
	}
}
