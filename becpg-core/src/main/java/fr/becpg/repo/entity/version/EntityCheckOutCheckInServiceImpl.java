/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.VersionNumber;
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
 * The Class ProductCheckOutCheckInServiceImpl.
 *
 * @author querephi
 */
public class EntityCheckOutCheckInServiceImpl extends CheckOutCheckInServiceImpl 
												implements EntityCheckOutCheckInService,
												ApplicationContextAware{

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
	
	private ApplicationContext applicationContext;
			
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
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
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
        copyName = createWorkingCopyName(copyName);

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
	public NodeRef checkin(final NodeRef workingCopyNodeRef, Map<String, Serializable> properties) {		
		
		NodeRef entityNodeRef = null;
		
		// Check that the working node still has the copy aspect applied
        if (nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_COPIEDFROM) == true){        	
        
        	// Try and get the original node reference
        	entityNodeRef = (NodeRef)nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_COPY_REFERENCE);        	
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
    		copyResidualProperties(workingCopyNodeRef, entityNodeRef);
    		
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
    		
    		// reset state to ToValid and publish event
    		// reset state to ToValidate
    		nodeService.setProperty(finalEntityNodeRef, BeCPGModel.PROP_PRODUCT_STATE, SystemState.ToValidate);    
    		applicationContext.publishEvent(new CheckInEntityEvent(this, finalEntityNodeRef));
    		    		
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
    
    private void copyResidualProperties(NodeRef sourceCopy, NodeRef targetCopy){
    	
		/*
		 * Extending DefaultCopyBehaviourCallback doesn't work since we must implement it for every aspect
		 */
		
		List<AssociationRef> sourceAssocRefs = nodeService.getTargetAssocs(sourceCopy, RegexQNamePattern.MATCH_ALL);
		List<AssociationRef> targetAssocRefs = nodeService.getTargetAssocs(targetCopy, RegexQNamePattern.MATCH_ALL);
		
		List<ChildAssociationRef> sourceChildAssocRefs = nodeService.getChildAssocs(sourceCopy);
		List<ChildAssociationRef> targetChildAssocRefs = nodeService.getChildAssocs(targetCopy);
		
		// don't copy/remove theses assocs
		List<QName> assocs = new ArrayList<QName>(0);
		//assocs.add(ReportModel.ASSOC_REPORTS);
		
		List<QName> childAssocs = new ArrayList<QName>(2);
		childAssocs.add(ContentModel.ASSOC_CHILDREN);
		childAssocs.add(BeCPGModel.ASSOC_ENTITYLISTS);
		childAssocs.add(RenditionModel.ASSOC_RENDITION);
		childAssocs.add(RuleModel.ASSOC_RULE_FOLDER);
		childAssocs.add(RuleModel.ASSOC_ACTION);
		
		
		/*
		 * Copy
		 */
		
		for(AssociationRef sourceAssocRef : sourceAssocRefs){
			
			logger.debug("sourceAssocRef.getTypeQName() : " + sourceAssocRef.getTypeQName());
					
			if(!assocs.contains(sourceAssocRef.getTypeQName())){
				boolean addAssoc = true;
				if(sourceAssocRef.getTargetRef() != null){
					for(AssociationRef targetAssocRef : targetAssocRefs){
						if(sourceAssocRef.getTargetRef().equals(targetAssocRef.getTargetRef()) &&
								sourceAssocRef.getTypeQName().equals(targetAssocRef.getTypeQName())){
							addAssoc = false;
							break;
						}
					}
				}
				
				if(addAssoc){
					logger.debug("Add association sourceRef : " + targetCopy + " sourceRef: " + sourceAssocRef.getTargetRef() + " assocType: " + sourceAssocRef.getTypeQName());
					nodeService.createAssociation(targetCopy, sourceAssocRef.getTargetRef(), sourceAssocRef.getTypeQName());
				}
			}						
		}
		
		for(ChildAssociationRef sourceChildAssocRef : sourceChildAssocRefs){
			
			logger.debug("sourceChildAssocRef.getTypeQName() : " + sourceChildAssocRef.getTypeQName());
			
			if(!childAssocs.contains(sourceChildAssocRef.getTypeQName())){
				boolean addAssoc = true;
				if(sourceChildAssocRef.getChildRef()!= null){
					for(ChildAssociationRef targetChildAssocRef : targetChildAssocRefs){
						if(sourceChildAssocRef.getChildRef().equals(targetChildAssocRef.getChildRef()) &&
							sourceChildAssocRef.getTypeQName().equals(targetChildAssocRef.getTypeQName())){
							addAssoc = false;
							break;
						}
					}
				}
				
				if(addAssoc){
					logger.debug("Add association sourceRef : " + sourceCopy + " sourceRef: " + sourceChildAssocRef.getChildRef() + " assocType: " + sourceChildAssocRef.getTypeQName());
					nodeService.addChild(targetCopy, sourceChildAssocRef.getChildRef(), sourceChildAssocRef.getTypeQName(), sourceChildAssocRef.getQName());
				}	
			}				
		}
		
		/*
		 * Remove removed assoc
		 */
		
		for(AssociationRef targetAssocRef : targetAssocRefs){
			
			if(!assocs.contains(targetAssocRef.getTypeQName())){
				boolean removeAssoc = true;
				if(targetAssocRef.getTargetRef() != null){
					for(AssociationRef sourceAssocRef : sourceAssocRefs){
						if(targetAssocRef.getTargetRef().equals(sourceAssocRef.getTargetRef()) &&
								targetAssocRef.getTypeQName().equals(sourceAssocRef.getTypeQName())){
							removeAssoc = false;
							break;
						}
					}
				}
				
				if(removeAssoc){
					logger.debug("Remove association sourceRef : " + targetCopy + " targetRef: " + targetAssocRef.getTargetRef() + " assocType: " + targetAssocRef.getTypeQName());
					nodeService.removeAssociation(targetCopy, targetAssocRef.getTargetRef(), targetAssocRef.getTypeQName());
				}
			}						
		}
		
		for(ChildAssociationRef targetChildAssocRef : targetChildAssocRefs){
			
			if(!childAssocs.contains(targetChildAssocRef.getTypeQName())){
				boolean removeAssoc = true;
				if(targetChildAssocRef.getChildRef()!= null){
					for(ChildAssociationRef sourceChildAssocRef : sourceChildAssocRefs){
						if(targetChildAssocRef.getChildRef().equals(sourceChildAssocRef.getChildRef()) &&
								targetChildAssocRef.getTypeQName().equals(sourceChildAssocRef.getTypeQName())){
							removeAssoc = false;
							break;
						}
					}
				}
				
				if(removeAssoc){
					logger.debug("Remove association sourceRef : " + targetCopy + " targetRef: " + targetChildAssocRef.getChildRef() + " assocType: " + targetChildAssocRef.getTypeQName());
					nodeService.removeChildAssociation(targetChildAssocRef);
				}	
			}				
		}
		
		/*
		 * Remove Aspects that have been removed on working copy
		 */
		
		Set<QName> aspects = nodeService.getAspects(targetCopy);
		for(QName aspect : aspects){
			
			if(!aspect.isMatch(ContentModel.ASPECT_OWNABLE) && !nodeService.hasAspect(sourceCopy, aspect)){
				logger.debug("Remove aspect : " + aspect + " on node " + targetCopy);
				nodeService.removeAspect(targetCopy, aspect);
			}
		}	
		
		/*
		 * Remove props that have been removed on working copy
		 */
		Map<QName, Serializable> props = nodeService.getProperties(targetCopy);
		for(Map.Entry<QName, Serializable> prop : props.entrySet()){
			if(prop.getValue() != null && nodeService.getProperty(sourceCopy, prop.getKey()) == null){
				logger.debug("Remove property : " + prop.getKey() + " on node " + targetCopy);
				nodeService.removeProperty(targetCopy, prop.getKey());
			}
		}
	}
}
