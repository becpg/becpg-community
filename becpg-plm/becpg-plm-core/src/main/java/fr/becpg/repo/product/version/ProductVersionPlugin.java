package fr.becpg.repo.product.version;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PLMWorkflowModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.version.EntityVersionPlugin;

@Service
public class ProductVersionPlugin implements EntityVersionPlugin {

	@Autowired
	NodeService nodeService;
	
	@Autowired
	EntityDictionaryService entityDictionaryService;
	
	@Autowired
	LockService lockService;
	
	@Autowired
	private BehaviourFilter policyBehaviourFilter;
	
	
	
	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		
		if(entityDictionaryService.isSubClass(nodeService.getType(origNodeRef), PLMModel.TYPE_PRODUCT)){
	        nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
			if (nodeService.hasAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
				nodeService.removeAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
			}
		}
		
	}

	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		if(entityDictionaryService.isSubClass(nodeService.getType(origNodeRef), PLMModel.TYPE_PRODUCT)){
	        nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE, nodeService.getProperty(origNodeRef, PLMModel.PROP_PRODUCT_STATE));
	        nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_ERP_CODE,  nodeService.getProperty(origNodeRef, PLMModel.PROP_ERP_CODE));
	      
	        if(!nodeService.hasAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
	        	try {
	        		policyBehaviourFilter.disableBehaviour();
	        		  if (nodeService.hasAspect(origNodeRef, ContentModel.ASPECT_LOCKABLE))
	                    {
	                        // Release the lock on the original node
	                        lockService.unlock(origNodeRef, false, true);
	                    }
	                   	nodeService.removeAspect(origNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
	        	} finally {
	        		  policyBehaviourFilter.enableBehaviour();
	        		  lockService.lock(origNodeRef, LockType.READ_ONLY_LOCK);
	        	}
	        }
	        
		
		} 
		
	}

	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		
	}


}
