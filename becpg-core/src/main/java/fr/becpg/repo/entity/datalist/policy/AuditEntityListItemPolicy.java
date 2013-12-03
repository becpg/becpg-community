/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.util.Calendar;
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
public class AuditEntityListItemPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnDeleteNodePolicy, NodeServicePolicies.OnUpdateNodePolicy,
		NodeServicePolicies.OnCreateNodePolicy {

	private static Log logger = LogFactory.getLog(AuditEntityListItemPolicy.class);

	private AuthenticationService authenticationService;


	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void doInit() {
		logger.debug("Init AuditEntityListItemPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onUpdateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onCreateNode"));
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		NodeRef listContainerNodeRef = nodeService.getPrimaryParent(childAssocRef.getParentRef()).getParentRef();

		if (listContainerNodeRef != null) {
			NodeRef rootNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
			queueNode(rootNodeRef);
		}

	}

	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {

		NodeRef listContainerNodeRef = nodeService.getPrimaryParent(childAssocRef.getParentRef()).getParentRef();

		if (listContainerNodeRef != null) {
			NodeRef rootNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
			queueNode(rootNodeRef);
		}

	}

	@Override
	public void onUpdateNode(NodeRef listItemNodeRef) {
		NodeRef listNodeRef = nodeService.getPrimaryParent(listItemNodeRef).getParentRef();

		if (listNodeRef != null) {
			NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef();

			if (listContainerNodeRef != null) {
				NodeRef rootNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
				queueNode(rootNodeRef);
			}
		}

	}

	/**
	 * Store in the entity list folder that an item has been deleted.
	 * 
	 */
	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef entityNodeRef : pendingNodes) {
			if (nodeService.exists(entityNodeRef) && !isVersionNode(entityNodeRef)) {
				try {
					policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
					nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIED, Calendar.getInstance().getTime());
					nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIER, authenticationService.getCurrentUserName());
				} finally {
					policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
				}
			}

		}
	}

}
