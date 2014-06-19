/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * @author querephi
 */
public class AuditEntityListItemPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnDeleteNodePolicy, NodeServicePolicies.OnUpdateNodePolicy,
		NodeServicePolicies.OnCreateNodePolicy {

	private static String KEY_LIST_ITEM = "KeyListItem";
	private static String KEY_LIST = "KeyList";
	
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
		queueListNodeRef(KEY_LIST, childAssocRef.getParentRef());
	}

	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		queueListNodeRef(KEY_LIST, childAssocRef.getParentRef());
	}

	@Override
	public void onUpdateNode(NodeRef listItemNodeRef) {				
		queueListNodeRef(KEY_LIST_ITEM, listItemNodeRef);
	}
	
	private void queueListNodeRef(String key, NodeRef listNodeRef){		
		if(policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ENTITYLIST_ITEM)){
			queueNode(key, listNodeRef);
		}
	}

	/**
	 * Store in the entity list folder that an item has been deleted.
	 * 
	 */
	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		
		Set<NodeRef> listNodeRefs = new HashSet<>();
		Set<NodeRef> listContainerNodeRefs = new HashSet<>();
		
		for(NodeRef pendingNode : pendingNodes){			
			if(nodeService.exists(pendingNode)){
				
				NodeRef listNodeRef = null;			
				if(key.equals(KEY_LIST_ITEM)){
					listNodeRef = nodeService.getPrimaryParent(pendingNode).getParentRef();
				}
				else{
					listNodeRef = pendingNode;
				}
				
				if(!listNodeRefs.contains(listNodeRef)){
					listNodeRefs.add(listNodeRef);					
					NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef();
					
					if(listContainerNodeRef!=null && !listContainerNodeRefs.contains(listContainerNodeRef) && nodeService.exists(listContainerNodeRef)){
						listContainerNodeRefs.add(listContainerNodeRef);
						NodeRef entityNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
						if (entityNodeRef!=null && !isVersionNode(entityNodeRef)) {
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
		}
	}

}
