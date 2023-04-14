package fr.becpg.repo.product.version;

import java.io.Serializable;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
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
import fr.becpg.repo.helper.AssociationService;

/**
 * <p>ProductVersionPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProductVersionPlugin implements EntityVersionPlugin {

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private EntityDictionaryService entityDictionaryService;
	
	@Autowired
	private NamespaceService namespaceService;
	
	@Autowired
	private LockService lockService;

	@Autowired
	private AssociationService associationService;
	
	@Value("${beCPG.copyOrBranch.propertiesToReset}")
	private String propertiesToKeep;
	
	
	/** {@inheritDoc} */
	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		
		if(entityDictionaryService.isSubClass(nodeService.getType(origNodeRef), PLMModel.TYPE_PRODUCT)){
	        nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
			if (nodeService.hasAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
				nodeService.removeAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
			}
		}
		
	}

	/** {@inheritDoc} */
	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		if(entityDictionaryService.isSubClass(nodeService.getType(origNodeRef), PLMModel.TYPE_PRODUCT)){
	        nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE, nodeService.getProperty(origNodeRef, PLMModel.PROP_PRODUCT_STATE));
     
			if (propertiesToKeep != null) {
				for (String propertyToKeep : propertiesToKeep.split(",")) {
					QName propertyQname = QName.createQName(propertyToKeep, namespaceService);

					if (entityDictionaryService.getProperty(propertyQname) != null) {
						Serializable value = nodeService.getProperty(origNodeRef, propertyQname);
						if (value != null) {
							nodeService.setProperty(workingCopyNodeRef, propertyQname, value);
						} else {
							nodeService.removeProperty(workingCopyNodeRef, propertyQname);
						}
					} else if (entityDictionaryService.getAssociation(propertyQname) != null) {
						List<NodeRef> originalAssocs = associationService.getTargetAssocs(origNodeRef, propertyQname);
						associationService.update(workingCopyNodeRef, propertyQname, originalAssocs);
					}

				}
	        }
	        
			if (!nodeService.hasAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
				if (nodeService.hasAspect(origNodeRef, ContentModel.ASPECT_LOCKABLE)) {
					// Release the lock on the original node
					lockService.unlock(origNodeRef, false, true);
				}
				nodeService.removeAspect(origNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
	        }
	        
		} else if (nodeService.getProperty(origNodeRef, PLMModel.PROP_SUPPLIER_STATE) != null) {
	        nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_SUPPLIER_STATE, nodeService.getProperty(origNodeRef, PLMModel.PROP_SUPPLIER_STATE));
		}
		
	}

	
	
	
	/** {@inheritDoc} */
	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		//Do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description) {
		//Do nothing
	}


}
