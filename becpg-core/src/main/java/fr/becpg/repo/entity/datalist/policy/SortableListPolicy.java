/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class SortableListPolicy.
 * 
 * @author querephi
 */
@Service
public class SortableListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, 
										   NodeServicePolicies.OnAddAspectPolicy, 
										   NodeServicePolicies.OnDeleteNodePolicy
										   {

	private static Log logger = LogFactory.getLog(SortableListPolicy.class);


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
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL, 
				new JavaBehaviour(this, "onAddAspect"));		
		
		logger.debug("Init SortableListPolicy...");		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, 
				new JavaBehaviour(this, "onAddAspect"));		
		
		super.disableOnCopyBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
		super.disableOnCopyBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

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
		
		try{
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			dataListSortService.deleteChildrens(childRef.getParentRef(), childRef.getChildRef());
		}
		finally{
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
		}			
	}
	

}
