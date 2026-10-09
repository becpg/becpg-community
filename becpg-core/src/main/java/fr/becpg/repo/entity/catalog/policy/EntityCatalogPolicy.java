/*
 *
 */
package fr.becpg.repo.entity.catalog.policy;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionSupportUtil;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.entity.datalist.policy.AuditEntityListItemPolicy;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>EntityCatalogPolicy class.</p>
 *
 * @author matthieu
 */
public class EntityCatalogPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy,
		NodeServicePolicies.OnSetNodeTypePolicy {

	/** Constant <code>CHANGED_CATALOG_ENTRIES="EntityCatalogPolicy.ChangedCatalogEntri"{trunked}</code> */
	private static final String CHANGED_CATALOG_ENTRIES = "EntityCatalogPolicy.ChangedCatalogEntries";

	private EntityCatalogService entityCatalogService;
	
	/**
	 * <p>Setter for the field <code>entityCatalogService</code>.</p>
	 *
	 * @param entityCatalogService a {@link fr.becpg.repo.entity.catalog.EntityCatalogService} object
	 */
	public void setEntityCatalogService(EntityCatalogService entityCatalogService) {
		this.entityCatalogService = entityCatalogService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * doInit.
	 * </p>
	 */
	@Override
	public void doInit() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnSetNodeTypePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onSetNodeType"));
	}


	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		if (!isVersionNode(assocRef.getSourceRef()) && isNotLocked(assocRef.getSourceRef())) {
			queueAssociationChange(assocRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		if (!isVersionNode(assocRef.getSourceRef()) && isNotLocked(assocRef.getSourceRef())) {
			queueAssociationChange(assocRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			String diffKey = CHANGED_CATALOG_ENTRIES + nodeRef;
			Set<QName> pendingDiff = TransactionSupportUtil.getResource(diffKey);
			String updatedListsKey = AuditEntityListItemPolicy.UPDATED_LISTS + nodeRef;
			Set<NodeRef> updatedLists = TransactionSupportUtil.getResource(updatedListsKey);
			entityCatalogService.updateAuditedField(nodeRef, pendingDiff, updatedLists);
			TransactionSupportUtil.bindResource(updatedListsKey, null);
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (!isVersionNode(nodeRef) && isNotLocked(nodeRef) && before != null && after != null) {

			MapDifference<QName, Serializable> diff = Maps.difference(before, after);
			
			if (!diff.areEqual()) {
				Set<QName> changedEntries = new HashSet<>();
				changedEntries.addAll(diff.entriesDiffering().keySet());
				changedEntries.addAll(diff.entriesOnlyOnLeft().keySet());
				changedEntries.addAll(diff.entriesOnlyOnRight().keySet());

				queueChangedEntries(nodeRef, changedEntries);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * A type change updates cm:modified without firing onUpdateProperties, so the modified date is
	 * queued explicitly to notify the catalog observers.
	 * </p>
	 */
	@Override
	public void onSetNodeType(NodeRef nodeRef, QName oldType, QName newType) {
		if (!isVersionNode(nodeRef) && isNotLocked(nodeRef) && isAuditableUpdated(nodeRef)) {
			queueChangedEntries(nodeRef, Set.of(ContentModel.PROP_MODIFIED));
		}
	}

	/**
	 * Queues the association type of a created or deleted association on its source entity.
	 * <p>
	 * An association change updates cm:modified without firing onUpdateProperties, so the modified
	 * date is queued too when the auditable behaviour has actually updated it.
	 * </p>
	 *
	 * @param assocRef the created or deleted association
	 */
	private void queueAssociationChange(AssociationRef assocRef) {
		NodeRef sourceNodeRef = assocRef.getSourceRef();
		Set<QName> changedEntries = new HashSet<>();
		changedEntries.add(assocRef.getTypeQName());
		if (isAuditableUpdated(sourceNodeRef)) {
			changedEntries.add(ContentModel.PROP_MODIFIED);
		}
		queueChangedEntries(sourceNodeRef, changedEntries);
	}

	/**
	 * Tells whether the node DAO updates cm:modified when the node is touched, which mirrors the
	 * condition applied by the node DAO itself.
	 *
	 * @param nodeRef the touched node
	 * @return <code>true</code> if the node is auditable and its auditable behaviour is enabled
	 */
	private boolean isAuditableUpdated(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE)
				&& policyBehaviourFilter.isEnabled(nodeRef, ContentModel.ASPECT_AUDITABLE);
	}

	/**
	 * Adds the changed entries to the pending catalog diff of the node and queues it for the
	 * before-commit processing.
	 *
	 * @param nodeRef the changed entity
	 * @param changedEntries the changed properties or association types
	 */
	private void queueChangedEntries(NodeRef nodeRef, Collection<QName> changedEntries) {
		String diffKey = CHANGED_CATALOG_ENTRIES + nodeRef;
		Set<QName> pendingDiff = TransactionSupportUtil.getResource(diffKey);

		if (pendingDiff == null) {
			pendingDiff = new HashSet<>();
		}

		pendingDiff.addAll(changedEntries);
		TransactionSupportUtil.bindResource(diffKey, pendingDiff);
		queueNode(nodeRef);
	}

}
