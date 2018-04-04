package fr.becpg.repo.product.version;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	NamespaceService namespaceService;
	
	@Value("${beCPG.copyOrBranch.propertiesToReset}")
	String propertiesNotToMerge;
	
	
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
     
	        if(propertiesNotToMerge!=null) {
		        for(String propertyToKeep : propertiesNotToMerge.split(",")) {
		        	QName propertyQname = QName.createQName(propertyToKeep,namespaceService );
		        	Serializable value = nodeService.getProperty(origNodeRef, propertyQname);
		        	if(value!=null) {
		        		nodeService.setProperty(workingCopyNodeRef, propertyQname,value  );
		        	} else {
		        		nodeService.removeProperty(workingCopyNodeRef, propertyQname);
		        	}
		        }
	        }
	        
		}
		
	}

	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		
	}

	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description) {
			
	}


}
