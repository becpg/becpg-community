/*
 *
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>
 * AuditEntityListItemPolicy class.
 * </p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class AuditEntityListItemPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnDeleteNodePolicy, NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static final String KEY_LIST_ITEM = "AuditEntityListItemPolicy.KeyListItem";
	private static final String KEY_LIST = "AuditEntityListItemPolicy.KeyList";

	private static final Log logger = LogFactory.getLog(AuditEntityListItemPolicy.class);

	private AuthenticationService authenticationService;

	private EntityCatalogService<RepositoryEntity> entityCatalogService;

	/**
	 * <p>
	 * Setter for the field <code>entityCatalogService</code>.
	 * </p>
	 *
	 * @param entityCatalogService
	 *            a {@link fr.becpg.repo.entity.catalog.EntityCatalogService}
	 *            object.
	 */
	public void setEntityCatalogService(EntityCatalogService<RepositoryEntity> entityCatalogService) {
		this.entityCatalogService = entityCatalogService;
	}

	/**
	 * <p>
	 * Setter for the field <code>authenticationService</code>.
	 * </p>
	 *
	 * @param authenticationService
	 *            a
	 *            {@link org.alfresco.service.cmr.security.AuthenticationService}
	 *            object.
	 */
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * <p>
	 * doInit.
	 * </p>
	 */
	@Override
	public void doInit() {
		logger.debug("Init AuditEntityListItemPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onUpdateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateNode"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onDeleteEntityAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onCreateEntityAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onUpdateProperties"));
		
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		queueListNodeRef(KEY_LIST, childAssocRef.getParentRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		queueListNodeRef(KEY_LIST, childAssocRef.getParentRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateNode(NodeRef listItemNodeRef) {
		queueListNodeRef(KEY_LIST_ITEM, listItemNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		if (!ContentModel.ASSOC_ORIGINAL.equals(assocRef.getTypeQName())) {
			queueListNodeRef(KEY_LIST_ITEM, assocRef.getSourceRef());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		queueListNodeRef(KEY_LIST_ITEM, assocRef.getSourceRef());
	}

	private void queueListNodeRef(String key, NodeRef listNodeRef) {
		if (policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ENTITYLIST_ITEM) && policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)) {
			queueNode(key, listNodeRef);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Store in the entity list folder that an item has been deleted.
	 */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		Set<NodeRef> listNodeRefs = new HashSet<>();
		Set<NodeRef> listContainerNodeRefs = new HashSet<>();
		Map<NodeRef, Set<NodeRef>> listNodeRefByContainer = new HashMap<>();
		for (NodeRef pendingNode : pendingNodes) {
			if (nodeService.exists(pendingNode)) {
				NodeRef listNodeRef;
				if (key.equals(KEY_LIST_ITEM)) {
					listNodeRef = nodeService.getPrimaryParent(pendingNode).getParentRef();
				} else {
					listNodeRef = pendingNode;
				}

				if (!listNodeRefs.contains(listNodeRef)) {
					listNodeRefs.add(listNodeRef);
					NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef();

					if (listNodeRefByContainer.get(listContainerNodeRef) != null) {
						listNodeRefByContainer.get(listContainerNodeRef).add(listNodeRef);
					} else {
						listNodeRefByContainer.put(listContainerNodeRef, new HashSet<>(Arrays.asList(listNodeRef)));
					}

					if ((listContainerNodeRef != null) && !listContainerNodeRefs.contains(listContainerNodeRef)
							&& nodeService.exists(listContainerNodeRef)) {
						listContainerNodeRefs.add(listContainerNodeRef);

					}
				}
			}
		}

		for (NodeRef listContainerNodeRef : listContainerNodeRefs) {
			NodeRef entityNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
			if ((entityNodeRef != null) && !isVersionNode(entityNodeRef) && isNotLocked(entityNodeRef)
					&& policyBehaviourFilter.isEnabled(entityNodeRef, ContentModel.ASPECT_AUDITABLE)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Update modified date of entity:" + entityNodeRef);
				}

				try {
					policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
					nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIED, Calendar.getInstance().getTime());
					nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIER, authenticationService.getCurrentUserName());
					entityCatalogService.updateAuditedField(entityNodeRef, null, listNodeRefByContainer.get(listContainerNodeRef));
				} finally {
					policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
				}
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (!isVersionNode(nodeRef) && isNotLocked(nodeRef) && before!=null && after!=null) {

			MapDifference<QName, Serializable> diff = Maps.difference(before, after);
			if(!diff.areEqual()) {
				entityCatalogService.updateAuditedField(nodeRef, diff.entriesDiffering().keySet(), null);
				entityCatalogService.updateAuditedField(nodeRef, diff.entriesOnlyOnLeft().keySet(), null);
				entityCatalogService.updateAuditedField(nodeRef, diff.entriesOnlyOnRight().keySet(), null);
			}
		}
	}

	public void onCreateEntityAssociation(AssociationRef assocRef) {
		if (!isVersionNode(assocRef.getSourceRef()) && isNotLocked(assocRef.getSourceRef())) {
			entityCatalogService.updateAuditedField(assocRef.getSourceRef(),  Set.of(assocRef.getTypeQName()), null);
		}
	}

	public void onDeleteEntityAssociation(AssociationRef assocRef) {
		if (!isVersionNode(assocRef.getSourceRef()) && isNotLocked(assocRef.getSourceRef())) {
			entityCatalogService.updateAuditedField(assocRef.getSourceRef(), Set.of(assocRef.getTypeQName()), null);
		}
	}

}
