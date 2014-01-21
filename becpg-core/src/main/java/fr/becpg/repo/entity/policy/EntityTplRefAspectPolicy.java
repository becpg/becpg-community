/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EntityFolderPolicy.
 * 
 * @author querephi
 */
public class EntityTplRefAspectPolicy extends AbstractBeCPGPolicy  implements NodeServicePolicies.OnCreateAssociationPolicy,NodeServicePolicies.OnAddAspectPolicy {

	private static Log logger = LogFactory.getLog(EntityTplRefAspectPolicy.class);

	private EntityService entityService;

	private AssociationService associationService;
	
	private EntityTplService entityTplService;
	
	private EntityListDAO entityListDAO;
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
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
			
			queueNode(entityNodeRef);
		}
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		NodeRef entityNodeRef = assocRef.getSourceRef();
		
		if (assocRef.getTypeQName().equals(BeCPGModel.ASSOC_ENTITY_TPL_REF)) {
			
			NodeRef entityTplNodeRef = assocRef.getTargetRef();
			
			// copy files
			entityService.copyFiles(entityTplNodeRef, entityNodeRef);	
			
			// copy datalists
			entityListDAO.copyDataLists(entityTplNodeRef, entityNodeRef, false);
			
			// copy missing aspects
			Set<QName> aspects = nodeService.getAspects(entityTplNodeRef);			
			for(QName aspect : aspects){
				if(!nodeService.hasAspect(entityNodeRef, aspect) && !BeCPGModel.ASPECT_ENTITY_TPL.isMatch(aspect)){
					nodeService.addAspect(entityNodeRef, aspect, null);
				}
			}
		}
	}
	
	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		
		for (NodeRef entityNodeRef : pendingNodes) {
			if(nodeService.exists(entityNodeRef)){
				NodeRef entityTplNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);		
				
				if(entityTplNodeRef == null){
					QName entityType = nodeService.getType(entityNodeRef);
					entityTplNodeRef = entityTplService.getEntityTpl(entityType);
					if(entityTplNodeRef!=null && nodeService.exists(entityTplNodeRef)){
						if(logger.isDebugEnabled()){
							logger.debug("Found default entity template '" + nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME) + "' to assoc.");
						}				
						associationService.update(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF, entityTplNodeRef);
					}
				}
			}					
		}
	}
	
}
