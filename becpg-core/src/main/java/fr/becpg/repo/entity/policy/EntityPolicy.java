/*
 * 
 */
package fr.becpg.repo.entity.policy;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EntityFolderPolicy.
 * 
 * @author querephi
 */
@Service
public class EntityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateNodePolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityPolicy.class);

	/** The policy component. */

	private EntityService entityService;

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void doInit() {
		logger.debug("Init EntityPolicy...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCreateNode"));

	}

	/**
	 * Create an entity folder if needed
	 */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		NodeRef entityNodeRef = childAssocRef.getChildRef();

		if (!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Call EntityPolicy for :" + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + " (" + nodeService.getType(entityNodeRef)
						+ ")");
			}

			entityService.initializeEntity(entityNodeRef);
			entityService.initializeEntityFolder(entityNodeRef);
		}

	}
}
