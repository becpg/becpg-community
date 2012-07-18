/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.util.Date;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * @author querephi
 */
@Service
public class AuditEntityListItemPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnDeleteNodePolicy {

	private static Log logger = LogFactory.getLog(AuditEntityListItemPolicy.class);

	private AuthenticationService authenticationService;


	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}


	public void doInit() {
		logger.debug("Init AuditEntityListItemPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onDeleteNode"));
	}


	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		NodeRef listNodeRef = childAssocRef.getParentRef();

		if (listNodeRef != null) {
			queueNode(listNodeRef);
		}
	}

	/**
	 * Store in the entity list folder that an item has been deleted.
	 * 
	 */
	 @Override
	protected void doBeforeCommit(Set<NodeRef> pendingNodes) {
		for (NodeRef listNodeRef : pendingNodes) {
			if (nodeService.exists(listNodeRef)) {
				try {
					policyBehaviourFilter.disableBehaviour(listNodeRef, ContentModel.ASPECT_AUDITABLE);
					nodeService.setProperty(listNodeRef, ContentModel.PROP_MODIFIED, new Date());
					nodeService.setProperty(listNodeRef, ContentModel.PROP_MODIFIER, authenticationService.getCurrentUserName());
				} finally {
					policyBehaviourFilter.enableBehaviour(listNodeRef, ContentModel.ASPECT_AUDITABLE);
				}
			}
		}

	}

}
