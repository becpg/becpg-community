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
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EntityFolderPolicy.
 * 
 * @author querephi
 */
public class EntityTplRefAspectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnAddAspectPolicy {

	private static final Log logger = LogFactory.getLog(EntityTplRefAspectPolicy.class);

	private AssociationService associationService;

	private EntityTplService entityTplService;


	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

    @Override
	public void doInit() {
		logger.debug("Init EntityTplPolicy...");

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				BeCPGModel.ASSOC_ENTITY_TPL_REF, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_ENTITY_TPL_REF, new JavaBehaviour(this,
				"onAddAspect"));

		super.disableOnCopyBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

	}

	@Override
	public void onAddAspect(NodeRef entityNodeRef, QName aspectTypeQName) {

		if (aspectTypeQName != null && aspectTypeQName.equals(BeCPGModel.ASPECT_ENTITY_TPL_REF)) {

			queueNode(entityNodeRef);
		}
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		NodeRef entityNodeRef = assocRef.getSourceRef();

		if (assocRef.getTypeQName().equals(BeCPGModel.ASSOC_ENTITY_TPL_REF)) {

			NodeRef entityTplNodeRef = assocRef.getTargetRef();
			if (logger.isDebugEnabled()) {
				logger.debug("Call synchronizeEntity with template '"+ nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME)
									+ "' for entity "+nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
			}
			
			entityTplService.synchronizeEntity(entityNodeRef, entityTplNodeRef);

		}
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef entityNodeRef : pendingNodes) {
			if (nodeService.exists(entityNodeRef)) {
				NodeRef entityTplNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);

				if (entityTplNodeRef == null) {
					QName entityType = nodeService.getType(entityNodeRef);
					entityTplNodeRef = entityTplService.getEntityTpl(entityType);
					if (entityTplNodeRef != null && nodeService.exists(entityTplNodeRef)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found default entity template '" + nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME)
									+ "' to assoc to "+nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
						}
						associationService.update(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF, entityTplNodeRef);
					}
				}
			}
		}
	}

}
