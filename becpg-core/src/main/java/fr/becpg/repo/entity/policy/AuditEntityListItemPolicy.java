/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

// TODO: Auto-generated Javadoc
/**
 * Policies of product list items.
 *
 * @author querephi
 */
public class AuditEntityListItemPolicy implements NodeServicePolicies.OnDeleteNodePolicy {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AuditEntityListItemPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;		
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The policy behaviour filter. */
	private BehaviourFilter policyBehaviourFilter;
	
	/** The authentication service. */
	private AuthenticationService authenticationService;
				
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}	
	
	/**
	 * Sets the policy behaviour filter.
	 *
	 * @param policyBehaviourFilter the new policy behaviour filter
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	/**
	 * Sets the authentication service.
	 *
	 * @param authenticationService the new authentication service
	 */
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init AuditEntityListItemPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onDeleteNode"));
	}
	
	/**
	 * Store in the entity list folder that an item has been deleted.
	 *
	 * @param childAssocRef the child assoc ref
	 * @param isNodeArchived the is node archived
	 */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		
		NodeRef listNodeRef = childAssocRef.getParentRef();
		
		if(listNodeRef != null){
			
			try
	        {
	            policyBehaviourFilter.disableBehaviour(listNodeRef, ContentModel.ASPECT_AUDITABLE);
	            nodeService.setProperty(listNodeRef, ContentModel.PROP_MODIFIED, new Date());
	            nodeService.setProperty(listNodeRef, ContentModel.PROP_MODIFIER, authenticationService.getCurrentUserName());	            
	        }
	        finally
	        {
	        	policyBehaviourFilter.enableBehaviour(listNodeRef, ContentModel.ASPECT_AUDITABLE);
	        }														
		}
					
	}	

}
