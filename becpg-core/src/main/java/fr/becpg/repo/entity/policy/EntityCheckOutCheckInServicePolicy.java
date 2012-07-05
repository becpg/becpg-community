package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.event.CheckInEntityEvent;

/**
 * 
 * @author quere
 *
 */
public class EntityCheckOutCheckInServicePolicy implements CheckOutCheckInServicePolicies.BeforeCheckOut,
												CheckOutCheckInServicePolicies.OnCheckOut,
												CheckOutCheckInServicePolicies.BeforeCheckIn,
												CheckOutCheckInServicePolicies.OnCheckIn,
												ApplicationContextAware{

	private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";
	
	private static Log logger = LogFactory.getLog(EntityCheckOutCheckInServicePolicy.class);
	
	private PolicyComponent policyComponent;
	private BehaviourFilter policyBehaviourFilter;
	private NodeService nodeService;    
    private AuthenticationService authenticationService;
    private EntityListDAO entityListDAO;
    private PermissionService permissionService;
    private ApplicationContext applicationContext;
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
		this.applicationContext = applicationContext;		
	}

	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init EntityCheckOutCheckInServicePolicy...");
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckOut.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "beforeCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckIn.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "beforeCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCheckIn"));
	}

	@Override
	public void beforeCheckOut(NodeRef nodeRef,
            NodeRef destinationParentNodeRef,           
            QName destinationAssocTypeQName, 
            QName destinationAssocQName) {
		
		// disable policy to avoid the creation of a new code
        policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_CODE);
        policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY);        
	}

	@Override
	public void onCheckOut(final NodeRef workingCopy) {
				
		// Copy entity datalists (rights are checked by copyService during recursiveCopy)
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				try {
					//disable policy to avoid duplicated noderefs
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);

					NodeRef nodeRef = getCheckedOut(workingCopy);
					entityListDAO.copyDataLists(nodeRef, workingCopy, true);
					return null;
				} finally {
					policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
				}
			}
		}, AuthenticationUtil.getSystemUserName());	
	     
	     // Set contributor permission for user to edit datalists
        String userName = getUserName();
	    permissionService.setPermission(workingCopy, userName, PermissionService.CONTRIBUTOR, true);
		
		//enable policies
		policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_CODE);
        policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITY);        
	}
	
	@Override
	public void beforeCheckIn(NodeRef workingCopyNodeRef,
            Map<String,Serializable> versionProperties,
            String contentUrl,
            boolean keepCheckedOut) {
		
		// CopyService failed with DuplicateChildNodeNameException: Duplicate child name not allowed: DataLists
		// Delete the datalists of the target node
		NodeRef nodeRef = getCheckedOut(workingCopyNodeRef);
		NodeRef containerListNodeRef = entityListDAO.getListContainer(nodeRef);
		if(containerListNodeRef != null){
			nodeService.deleteNode(containerListNodeRef);
		}		
	}
	
	/**
     * Gets the authenticated users node reference
     * 
     * @return  the users node reference
     */
    private String getUserName()
    {
        String un =  this.authenticationService.getCurrentUserName();
        if (un != null)
        {
           return un;
        }
        else
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_NOT_AUTHENTICATED);
        }
    }
    
    private NodeRef getCheckedOut(NodeRef nodeRef)
    {
        NodeRef original = null;
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            List<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
            // It is a 1:1 relationship
            if (assocs.size() > 0)
            {
                if (assocs.size() > 1)
                {
                    logger.warn("Found multiple " + ContentModel.ASSOC_WORKING_COPY_LINK + " associations to node: " + nodeRef);
                }
                original = assocs.get(0).getSourceRef();
            }
        }
        
        return original;
    }

	@Override
	public void onCheckIn(NodeRef nodeRef) {
		
		// reset state to ToValidate
		nodeService.setProperty(nodeRef, BeCPGModel.PROP_PRODUCT_STATE, SystemState.ToValidate);
	
		// publish checkin entity event
		applicationContext.publishEvent(new CheckInEntityEvent(this, nodeRef));
	}

}
