/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;

/**
 * The Class ProductCheckOutCheckInServiceImpl.
 *
 * @author querephi
 */
//TODO philippe alfresco 4.0
public class EntityCheckOutCheckInServiceImpl extends CheckOutCheckInServiceImpl implements EntityCheckOutCheckInService {

	/** The Constant MSG_ERR_BAD_COPY. */
	private static final String MSG_ERR_BAD_COPY = "coci_service.err_bad_copy";
    
    /** The Constant MSG_ERR_NOT_OWNER. */
    private static final String MSG_ERR_NOT_OWNER = "coci_service.err_not_owner";
    
    /** The Constant MSG_ERR_NOT_AUTHENTICATED. */
    private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";
	
	/** The Constant MSG_ALREADY_CHECKEDOUT. */
	private static final String MSG_ALREADY_CHECKEDOUT = "coci_service.err_already_checkedout";
	
	/** The Constant MSG_ERR_ALREADY_WORKING_COPY. */
	private static final String MSG_ERR_ALREADY_WORKING_COPY = "coci_service.err_workingcopy_checkout";
	
	/** The Constant WORKING_COPY_MODE. */
	private static final String WORKING_COPY_MODE = "offlineEditing";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityCheckOutCheckInServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The lock service. */
	private LockService lockService;
	
	/** The copy service. */
	private CopyService copyService;
	
	/** The rule service. */
	private RuleService ruleService;	
	
	private EntityListDAO entityListDAO;
	
	/** The product version service. */
	private EntityVersionService entityVersionService;
	
	/** The authentication service. */
	private AuthenticationService authenticationService;
	
	/** The permission service. */
	private PermissionService permissionService;	
	
	private BehaviourFilter policyBehaviourFilter;
			
	/* (non-Javadoc)
	 * @see org.alfresco.repo.coci.CheckOutCheckInServiceImpl#setNodeService(org.alfresco.service.cmr.repository.NodeService)
	 */
	@Override
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
		super.setNodeService(nodeService);
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.coci.CheckOutCheckInServiceImpl#setLockService(org.alfresco.service.cmr.lock.LockService)
	 */
	@Override
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
		super.setLockService(lockService);
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.coci.CheckOutCheckInServiceImpl#setCopyService(org.alfresco.service.cmr.repository.CopyService)
	 */
	@Override
	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
		super.setCopyService(copyService);
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.coci.CheckOutCheckInServiceImpl#setRuleService(org.alfresco.service.cmr.rule.RuleService)
	 */
	@Override
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
		super.setRuleService(ruleService);
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * Sets the entity version service.
	 *
	 * @param entityVersionService the new entity version service
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.coci.CheckOutCheckInServiceImpl#setAuthenticationService(org.alfresco.service.cmr.security.AuthenticationService)
	 */
	@Override
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}		
	
	/**
	 * Sets the permission service.
	 *
	 * @param permissionService the new permission service
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}	
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * Check out a entity.
	 *
	 * @param nodeRef the node ref
	 * @param destinationParentNodeRef the destination parent node ref
	 * @param destinationAssocTypeQName the destination assoc type q name
	 * @param destinationAssocQName the destination assoc q name
	 * @return the node ref
	 */
	@Override
	public NodeRef checkout(
            final NodeRef nodeRef, 
            final NodeRef destinationParentNodeRef,
            final QName destinationAssocTypeQName, 
            QName destinationAssocQName){
		
		
		LockType lockType = this.lockService.getLockType(nodeRef);
        if (LockType.READ_ONLY_LOCK.equals(lockType) == true || getWorkingCopy(nodeRef) != null)
        {
            throw new CheckOutCheckInServiceException(MSG_ALREADY_CHECKEDOUT);
        }
    
        // Make sure we are no checking out a working copy node
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == true)
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_ALREADY_WORKING_COPY);
        }
        
        // Apply the lock aspect if required
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == false)
        {
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        }        
        
        // Rename the working copy
        String copyName = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        String workingCopyLabel = getWorkingCopyLabel();
        copyName = createWorkingCopyName(copyName, workingCopyLabel);


        // Make the working copy
        final QName copyQName = QName.createQName(destinationAssocQName.getNamespaceURI(), QName.createValidLocalName(copyName));
        final NodeRef workingCopy = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            @Override
			public NodeRef doWork() throws Exception
            {
            	// disable policy to avoid the creation of a new code
        		policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_CODE);
        		policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY);
        		NodeRef workingCopy = null;
        		
        		try{
        			
        			workingCopy = copyService.copy(
                            nodeRef,
                            destinationParentNodeRef,
                            destinationAssocTypeQName,
                            copyQName);
                
	            }
				finally{
					policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_CODE);
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITY);
				}
                
                return workingCopy;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // Get the user 
        String userName = getUserName();
        
        ruleService.disableRules();
        try
        {
            // Update the working copy name        
            this.nodeService.setProperty(workingCopy, ContentModel.PROP_NAME, copyName);
            
            // Extra property to allow the full series of actions via the Explorer client
            nodeService.setProperty(workingCopy, ContentModel.PROP_WORKING_COPY_MODE, WORKING_COPY_MODE);
            
            // Apply the working copy aspect to the working copy
            Map<QName, Serializable> workingCopyProperties = new HashMap<QName, Serializable>(1);
            workingCopyProperties.put(ContentModel.PROP_WORKING_COPY_OWNER, userName);
            this.nodeService.addAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY, workingCopyProperties);
        }
        finally
        {
            ruleService.enableRules();
        }
        
        // Copy entity datalists
		// Rights are checked by copyService during recursiveCopy
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>(){
            @Override
			public Void doWork() throws Exception
            {      
            	entityListDAO.copyDataLists(nodeRef, workingCopy, true);
            	return null;
            	
            }
        }, AuthenticationUtil.getSystemUserName());	
        
        // Lock the original node
        this.lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
        
        // Set contributor permission for user to edit datalists        
        permissionService.setPermission(workingCopy, userName, PermissionService.CONTRIBUTOR, true);
        
        // Return the working copy
        return workingCopy;		
	}
	
	/**
	 * Check in a entity.
	 *
	 * @param workingCopyNodeRef the working copy node ref
	 * @param properties the properties
	 * @return the node ref
	 */
	@Override
	//TODO 4.0 migration
	public NodeRef checkin(final NodeRef workingCopyNodeRef, Map<String, Serializable> properties) {		
		
		NodeRef entityNodeRef = null;
		
		// Check that the working node still has the copy aspect applied
        if (nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_COPIEDFROM) == true){        	
        
        	// Try and get the original node reference
        	entityNodeRef = getCheckedOut(workingCopyNodeRef);      	
            if(entityNodeRef == null)
            {
                // Error since the original node can not be found
                throw new CheckOutCheckInServiceException(MSG_ERR_BAD_COPY);                            
            }
            
            try
            {
                // Release the lock
                lockService.unlock(entityNodeRef);
            }
            catch (UnableToReleaseLockException exception)
            {
                throw new CheckOutCheckInServiceException(MSG_ERR_NOT_OWNER, exception);
            }            
            
            //copy properties
    		copyService.copy(workingCopyNodeRef, entityNodeRef);
    		
    		//create new version
            entityVersionService.createVersion(entityNodeRef, properties);
    		
    		// Copy entity datalists
    		// Rights are checked by copyService during recursiveCopy
    		final NodeRef finalEntityNodeRef = entityNodeRef;
    		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>(){
                @Override
				public Void doWork() throws Exception
                {      
                	entityListDAO.copyDataLists(workingCopyNodeRef, finalEntityNodeRef, true);
                	return null;
                	
                }
            }, AuthenticationUtil.getSystemUserName());
    		    		
    		// Delete the working copy
            this.nodeService.deleteNode(workingCopyNodeRef);
                       
            // Remove the lock aspect (copied from working copy)
            this.nodeService.removeAspect(entityNodeRef, ContentModel.ASPECT_LOCKABLE);                        
        }
        else{
        	// Error since the copy aspect is missing
            throw new AspectMissingException(ContentModel.ASPECT_COPIEDFROM, workingCopyNodeRef);
        }		
        
		return entityNodeRef;
	}
	
	  @Override
	    public NodeRef getCheckedOut(NodeRef nodeRef)
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

	
	
	/**
	 * Calculate new version			
	 * @param versionLabel
	 * @param majorVersion
	 * @return
	 */
	@Override
	public VersionNumber getVersionNumber(String versionLabel, boolean majorVersion){
		
		VersionNumber versionNumber = new VersionNumber(versionLabel);
		if(majorVersion){
			int majorNb = versionNumber.getPart(0) + 1;
			versionNumber = new VersionNumber(majorNb + EntityVersionService.VERSION_DELIMITER + 0);			
		}
		else{
			int minorNb = versionNumber.getPart(1) + 1;
			versionNumber = new VersionNumber(versionNumber.getPart(0) + EntityVersionService.VERSION_DELIMITER + minorNb);
		}
		
		return versionNumber;
	}
	
	/**
	 * Gets the authenticated users node reference.
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
}
