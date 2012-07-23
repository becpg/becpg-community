/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EntityFolderPolicy.
 * 
 * @author querephi
 */
@Service
public class EntityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnDeleteNodePolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityPolicy.class);

	/** The policy component. */

	private EntityService entityService;

	
	
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}


	public void doInit() {
		logger.debug("Init EntityPolicy...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onDeleteNode"));


	}

	/**
	 * Create an entity folder if needed
	 */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		NodeRef entityNodeRef = childAssocRef.getChildRef();
	
		queueNode(entityNodeRef);
		

	}

	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		NodeRef entityNodeRef = childAssocRef.getChildRef();
		if(nodeService.exists(entityNodeRef)){
			entityService.deleteEntity(entityNodeRef);
		}

	}
	
	@Override
	protected void doBeforeCommit(Set<NodeRef> pendingNodes) {

		for (NodeRef entityNodeRef : pendingNodes) {
			if(isNotLocked(entityNodeRef)){
				entityService.initializeEntity(entityNodeRef);
				entityService.initializeEntityFolder(entityNodeRef);
			}
		}
	}

	
}
