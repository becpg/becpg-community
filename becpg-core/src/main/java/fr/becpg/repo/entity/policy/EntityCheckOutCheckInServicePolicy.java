package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.event.CheckInEntityEvent;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.TranslateHelper;
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
												CheckOutCheckInServicePolicies.BeforeCancelCheckOut,
												NodeServicePolicies.OnAddAspectPolicy,
												NodeServicePolicies.OnRemoveAspectPolicy,
												NodeServicePolicies.OnDeleteNodePolicy,
												ApplicationContextAware{

	private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";
	private static final String MSG_INITIAL_VERSION = "create_version.initial_version";
    
	private static Log logger = LogFactory.getLog(EntityCheckOutCheckInServicePolicy.class);
	    
    private AuthenticationService authenticationService;
    private EntityListDAO entityListDAO;
    private PermissionService permissionService;
    private ApplicationContext applicationContext;
    private EntityVersionService entityVersionService;
    private FileFolderService fileFolderService;

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

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * Inits the.
	 */
	public void doInit(){
		logger.debug("Init EntityCheckOutCheckInServicePolicy...");
		
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME,
				BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(this, "onCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckIn.QNAME,
				BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(this, "beforeCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME,
				BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(this, "onCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCancelCheckOut.QNAME,
				BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(this, "beforeCancelCheckOut"));
		
		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
				ContentModel.ASPECT_VERSIONABLE, new JavaBehaviour(this, "onAddAspect",
				Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
		
		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"),
				ContentModel.ASPECT_VERSIONABLE, new JavaBehaviour(this, "onRemoveAspect",
						Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
				ContentModel.ASPECT_VERSIONABLE, new JavaBehaviour(this, "onDeleteNode",
						Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
	}



	@Override
	public void onCheckOut(final NodeRef workingCopy) {
		
		// Copy entity datalists (rights are checked by copyService during recursiveCopy)
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {

				NodeRef nodeRef = getCheckedOut(workingCopy);										
				entityListDAO.copyDataLists(nodeRef, workingCopy, true);					
				moveFiles(nodeRef, workingCopy);					
				return null;

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

			entityVersionService.createVersionAndCheckin(origNodeRef, workingCopyNodeRef, versionProperties);
			
			//frozeVersionSensitiveLists(origNodeRef, entityVersionRef);
	}
		
	@Override
	public void onCheckIn(NodeRef nodeRef) {		
		
		nodeService.setProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY, new Date());
		
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
            if (!assocs.isEmpty())
            {
            	if(logger.isWarnEnabled()){
	                if (assocs.size() > 1)
	                {
	                    logger.warn("Found multiple " + ContentModel.ASSOC_WORKING_COPY_LINK + " associations to node: " + nodeRef);
	                }
            	}
                original = assocs.get(0).getSourceRef();
            }
        }
        
        return original;
    }

	@Override
	public void beforeCancelCheckOut(final NodeRef workingCopyNodeRef) {
		
		final NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);		
		
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
			@Override
		public NodeRef doWork() throws Exception {
		    
			// move files
			moveFiles(workingCopyNodeRef, origNodeRef);
			return null;

		}
	 }, AuthenticationUtil.getSystemUserName());
		
	}
	
	private void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		
		if(targetNodeRef != null && sourceNodeRef != null){	

			for (FileInfo file : fileFolderService.list(sourceNodeRef)) {
				
				if(file.getName().equals(TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS))){
					
					// create Documents folder if needed
					String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
					NodeRef documentsFolderNodeRef = nodeService.getChildByName(targetNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);
					if(documentsFolderNodeRef == null){
						documentsFolderNodeRef = fileFolderService.create(targetNodeRef, documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
					}
					
					for (FileInfo file2 : fileFolderService.list(file.getNodeRef())){
						
						// move files that are not report
						if(!ReportModel.TYPE_REPORT.equals(file2.getType())){
							
							NodeRef documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, file2.getName());
							if (documentNodeRef != null) {
								nodeService.deleteNode(documentNodeRef);
							}				
							logger.debug("move file in Documents: " + file.getName() + " entityFolderNodeRef: " + targetNodeRef);
							nodeService.moveNode(file2.getNodeRef(), targetNodeRef, ContentModel.ASSOC_CONTAINS, nodeService.getPrimaryParent(file.getNodeRef()).getQName());
						}						
					}
				}	
				else{
					logger.debug("move file: " + file.getName() + " entityFolderNodeRef: " + targetNodeRef);				
					nodeService.moveNode(file.getNodeRef(), targetNodeRef, ContentModel.ASSOC_CONTAINS, nodeService.getPrimaryParent(file.getNodeRef()).getQName());
				}
			}
		}		
	}
	
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {

		logger.debug("OnDeleteNode cm:versionable " + childAssocRef.getChildRef() + " isNodeArchived: " + isNodeArchived);
		
		if (isNodeArchived == false) {
			// If we are perminantly deleting the node then we need to remove
			// the associated version history
			entityVersionService.deleteVersionHistory(childAssocRef.getChildRef());
		}
	}

	@Override
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {

		// When the versionable aspect is removed from a node, then delete the
		// associated version history
		entityVersionService.deleteVersionHistory(nodeRef);
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		
		if (nodeService.exists(nodeRef) == true &&
				nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS) &&
				!isBeCPGVersion(nodeRef)) {
						
			// Create the initial-version
            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1);
            
            // If a major version is requested, indicate it in the versionProperties map
            String versionType = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_TYPE);
            if (versionType == null  || !versionType.equals(VersionType.MINOR.toString()))
            {
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
            }
            
            versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_INITIAL_VERSION));
			entityVersionService.createVersion(nodeRef, versionProperties);
		}
		
	}

//    private void frozeVersionSensitiveLists(NodeRef origNodeRef, NodeRef entityVersionRef){
//    	
//    	logger.debug("frozeVersionSensitiveLists for origNodeRef: " + origNodeRef + " and entityVersionRef: " + entityVersionRef);
//    	
//    	//TODO : not generic
//    	QName [] listQNames = {BeCPGModel.TYPE_COMPOLIST, BeCPGModel.TYPE_PACKAGINGLIST, MPMModel.TYPE_PROCESSLIST};
//    	QName [] associationQNames = {BeCPGModel.ASSOC_COMPOLIST_PRODUCT, BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT, MPMModel.ASSOC_PL_PRODUCT};
//    	int i=0;
//    	for(QName listQName : listQNames){
//    		MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(origNodeRef, listQName, RepoConsts.MAX_DEPTH_LEVEL);
//    		frozeVersionSensitiveNodes(wUsedData, entityVersionRef, associationQNames[i]);
//    		i++;
//    	}    	
//    }
//    
//    private void frozeVersionSensitiveNodes(MultiLevelListData wUsedData, NodeRef entityVersionRef, QName associationQName){
//    	
//    	for(Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()){
//    		logger.debug("frozeVersionSensitiveNodes, entityListItem: " + kv.getKey() + " - associationQName: " + associationQName + " - entityVersionRef: " + entityVersionRef);
//    		associationService.update(kv.getKey(), associationQName, entityVersionRef);
//    	}
//    }
}
