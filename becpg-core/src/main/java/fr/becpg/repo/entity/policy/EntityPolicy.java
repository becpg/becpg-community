/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
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
public class EntityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityPolicy.class);

	private static final String COPY_OF_LABEL = "copy_service.copy_of_label";

	/** The policy component. */

	private EntityService entityService;

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void doInit() {
		logger.debug("Init EntityPolicy...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onUpdateProperties"));

	}

	/**
	 * Create an entity folder if needed
	 */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		NodeRef entityNodeRef = childAssocRef.getChildRef();

		if (!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Call EntityPolicy for :" + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + " (" + nodeService.getType(entityNodeRef) + ")");
			}
			entityService.initializeEntityFolder(entityNodeRef);
		}

	}

	@Override
	public void onUpdateProperties(NodeRef entityNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (after.containsKey(ContentModel.PROP_NAME)) {

			String beforeName = (String) before.get(ContentModel.PROP_NAME);
			String afterName = (String) after.get(ContentModel.PROP_NAME);						

			if (afterName != null && !afterName.isEmpty() && !afterName.equals(beforeName)) {
				queueNode(entityNodeRef);
			}
		}
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef entityNodeRef : pendingNodes) {
			// don't rename entity folder when doing checkout (working copy)
			if (nodeService.exists(entityNodeRef) && !isWorkingCopyOrVersion(entityNodeRef)) {
				NodeRef entityFolderNodeRef = entityService.getEntityFolder(entityNodeRef);
				if (entityFolderNodeRef != null) {
					String newName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
					if (!newName.equals(nodeService.getProperty(entityFolderNodeRef, ContentModel.PROP_NAME))) {
						NodeRef destinationParent = nodeService.getPrimaryParent(entityFolderNodeRef).getParentRef();

						NodeRef nodeRef = null;
						while ((nodeRef = this.nodeService.getChildByName(destinationParent, ContentModel.ASSOC_CONTAINS, newName)) != null && nodeRef != null
								&& !nodeRef.equals(entityFolderNodeRef)) {
							newName = I18NUtil.getMessage(COPY_OF_LABEL, newName);
						}
						if (nodeRef==null || !nodeRef.equals(entityFolderNodeRef)) {														
							nodeService.setProperty(entityFolderNodeRef, ContentModel.PROP_NAME, newName);
						}
					}
				}
			}
		}

	}

}
