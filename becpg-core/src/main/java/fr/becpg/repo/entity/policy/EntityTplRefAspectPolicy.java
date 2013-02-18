/*
 * 
 */
package fr.becpg.repo.entity.policy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EntityFolderPolicy.
 * 
 * @author querephi
 */
@Service
public class EntityTplRefAspectPolicy extends AbstractBeCPGPolicy  implements NodeServicePolicies.OnCreateAssociationPolicy,NodeServicePolicies.OnAddAspectPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityTplRefAspectPolicy.class);


	/** The policy component. */

	private EntityService entityService;

	private AssociationService associationService;
	
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void doInit() {
		logger.debug("Init EntityTplPolicy...");
		
		

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.ASPECT_ENTITY_TPL_REF, BeCPGModel.ASSOC_ENTITY_TPL_REF, new JavaBehaviour(this,
						"onCreateAssociation"));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME,BeCPGModel.ASPECT_ENTITY_TPL_REF, new JavaBehaviour(this,"onAddAspect"));
		
		super.disableOnCopyBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

	}

	@Override
	public void onAddAspect(NodeRef entityNodeRef, QName aspectTypeQName) {
		if(aspectTypeQName!=null 
				&& aspectTypeQName.equals(BeCPGModel.ASPECT_ENTITY_TPL_REF)){
			
			NodeRef entityTplNodeRef = entityService.getEntityTplNodeRef(entityNodeRef);
			if(entityTplNodeRef!=null){
				logger.debug("Found default entity template to assoc ");
				associationService.update(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF, entityTplNodeRef);
			}
		}
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		NodeRef entityNodeRef = assocRef.getSourceRef();
		
		if (assocRef.getTypeQName().equals(BeCPGModel.ASSOC_ENTITY_TPL_REF)) {
			
			NodeRef entityTplNodeRef = assocRef.getTargetRef();
			
			// copy files
			entityService.copyFiles(entityTplNodeRef, entityNodeRef);			
		}
	}
	
}
