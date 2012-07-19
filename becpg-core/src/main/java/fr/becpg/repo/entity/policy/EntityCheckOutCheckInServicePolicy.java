package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.event.CheckInEntityEvent;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * 
 * @author quere
 *
 */
@Service
public class EntityCheckOutCheckInServicePolicy extends AbstractBeCPGPolicy implements 
												CheckOutCheckInServicePolicies.OnCheckOut,
												CheckOutCheckInServicePolicies.BeforeCheckIn,
												CheckOutCheckInServicePolicies.OnCheckIn,
												ApplicationContextAware{

	private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";
	
	private static Log logger = LogFactory.getLog(EntityCheckOutCheckInServicePolicy.class);
	    
    private AuthenticationService authenticationService;
    private EntityListDAO entityListDAO;
    private PermissionService permissionService;
    private ApplicationContext applicationContext;

    private EntityVersionService entityVersionService;
    

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

	
	
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * Inits the.
	 */
	public void doInit(){
		logger.debug("Init EntityCheckOutCheckInServicePolicy...");
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckIn.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "beforeCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCheckIn"));
	}



	@Override
	public void onCheckOut(final NodeRef workingCopy) {
		
		// Copy entity datalists (rights are checked by copyService during recursiveCopy)
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				try {
					//disable datalist policies
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_COSTLIST);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_NUTLIST);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					policyBehaviourFilter.disableBehaviour(DataListModel.TYPE_DATALIST);
					
					NodeRef nodeRef = getCheckedOut(workingCopy);
					entityListDAO.copyDataLists(nodeRef, workingCopy, true);
					return null;
					
				} finally {
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_COSTLIST);
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_NUTLIST);
					policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
					policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					policyBehaviourFilter.enableBehaviour(DataListModel.TYPE_DATALIST);
				}

			}
		}, AuthenticationUtil.getSystemUserName());	
	     
	     // Set contributor permission for user to edit datalists
        String userName = getUserName();
	    permissionService.setPermission(workingCopy, userName, PermissionService.CONTRIBUTOR, true);
		     
	}
	
	@Override
	public void beforeCheckIn(NodeRef workingCopyNodeRef,
            Map<String,Serializable> versionProperties,
            String contentUrl,
            boolean keepCheckedOut) {
		
			NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);
			
			QName type = nodeService.getType(origNodeRef);

			// disable policy to avoid code, folder initialization and report
			// generation
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_CODE);
			policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
			//disable classify
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_PRODUCT);		
			// doesn't work, need to disable current class, subclass of entity, better than disableBehaviour()
			//policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY);
			policyBehaviourFilter.disableBehaviour(type);

			try {
			
				entityVersionService.createVersionAndCheckin(origNodeRef, workingCopyNodeRef);
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_CODE);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_FINISHEDPRODUCT);
				policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_PRODUCT);
				// doesn't work, need to disable current class, subclass of entity, better than disableBehaviour()
				//policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY);
				policyBehaviourFilter.enableBehaviour(type);
			}
			
	}
		
	@Override
	public void onCheckIn(NodeRef nodeRef) {
		
		// reset state to ToValidate
		nodeService.setProperty(nodeRef, BeCPGModel.PROP_PRODUCT_STATE, SystemState.ToValidate);
	
		// publish checkin entity event
		applicationContext.publishEvent(new CheckInEntityEvent(this, nodeRef));
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


}
