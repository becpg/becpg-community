/*
 * 
 */
package fr.becpg.repo.entity.policy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;

/**
 * The Class EntityFolderPolicy.
 *
 * @author querephi
 */
public class EntityPolicy implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnDeleteNodePolicy {	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;		
	
	private EntityService entityService;
			
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * Inits the.
	 */
	public void init(){
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
		
		entityService.initializeEntity(entityNodeRef);
		entityService.initializeEntityFolder(entityNodeRef);
	
	}

	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		NodeRef entityNodeRef = childAssocRef.getChildRef();
		
		entityService.deleteEntity(entityNodeRef);
		
	}
	
}
