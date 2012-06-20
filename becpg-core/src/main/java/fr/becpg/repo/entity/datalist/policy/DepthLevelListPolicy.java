/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;

/**
 * The Class DepthLevelListPolicy.
 * 
 * @author querephi
 */
public class DepthLevelListPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, 
NodeServicePolicies.OnAddAspectPolicy, 
NodeServicePolicies.OnDeleteNodePolicy {


	
	private static Log logger = LogFactory.getLog(DepthLevelListPolicy.class);

	private PolicyComponent policyComponent;

	private DataListSortService dataListSortService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	

	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}


	/**
	 * Inits the.
	 */
	public void init() {
		logger.debug("Init DepthLevelListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onDeleteNode"));
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		NodeRef beforeParentLevel = (NodeRef) before.get(BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef afterParentLevel = (NodeRef) after.get(BeCPGModel.PROP_PARENT_LEVEL);
		
		if(logger.isDebugEnabled()){
			logger.debug("call DepthLevelListPolicy");
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
		
		if(aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL)){			
			dataListSortService.computeDepthAndSort(nodeRef);
		}	
	}
	
	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
		
		dataListSortService.deleteChildrens(childRef.getParentRef(), childRef.getChildRef());
	
	}
	

}
