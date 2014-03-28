/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.Set;

import org.alfresco.email.server.EmailServerModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EmailAliasablePolicy.
 * 
 * @author querephi
 */
public class EmailAliasablePolicy extends AbstractBeCPGPolicy implements 
			NodeServicePolicies.OnAddAspectPolicy,
			CheckOutCheckInServicePolicies.BeforeCheckOut, 
			CheckOutCheckInServicePolicies.OnCheckOut,
			CheckOutCheckInServicePolicies.OnCheckIn,
			CheckOutCheckInServicePolicies.BeforeCancelCheckOut{

	private static Log logger = LogFactory.getLog(EmailAliasablePolicy.class);

	public void doInit() {
		logger.debug("Init EmailAlisablePolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, EmailServerModel.ASPECT_ALIASABLE, new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckOut.QNAME, EmailServerModel.ASPECT_ALIASABLE, new JavaBehaviour(this, "beforeCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, EmailServerModel.ASPECT_ALIASABLE, new JavaBehaviour(this, "onCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, EmailServerModel.ASPECT_ALIASABLE, new JavaBehaviour(this, "onCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCancelCheckOut.QNAME, EmailServerModel.ASPECT_ALIASABLE, new JavaBehaviour(this, "beforeCancelCheckOut"));
	}
	
	@Override
	public void onAddAspect(NodeRef nodeRef, QName type) {
		queueNode(nodeRef);
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {		
		for (NodeRef nodeRef : pendingNodes) {
			if (nodeService.exists(nodeRef)) {
				setAliasOnNode(nodeRef);
			}
		}		
	}

	@Override
	public void beforeCheckOut(NodeRef nodeRef, NodeRef destinationParentNodeRef, QName destinationAssocTypeQName,
			QName destinationAssocQName) {
		if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_CODE)){
			nodeService.removeProperty(nodeRef, EmailServerModel.PROP_ALIAS);			
		}		
	}
	
	@Override
	public void onCheckOut(NodeRef workingCopyNodeRef) {
		setAliasOnNode(workingCopyNodeRef);
	}

	@Override
	public void onCheckIn(NodeRef nodeRef) {
		setAliasOnNode(nodeRef);
	}	
	
	@Override
	public void beforeCancelCheckOut(final NodeRef workingCopyNodeRef){	
		setAliasOnNode(workingCopyNodeRef);
	}
	
	private void setAliasOnNode(NodeRef nodeRef){
		if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_CODE)){
			nodeService.setProperty(nodeRef, EmailServerModel.PROP_ALIAS, nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE));			
		}
	}
}
