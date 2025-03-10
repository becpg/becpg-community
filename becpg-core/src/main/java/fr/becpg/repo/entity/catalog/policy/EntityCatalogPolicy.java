/*
 *
 */
package fr.becpg.repo.entity.catalog.policy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

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
	}


	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		if (!isVersionNode(assocRef.getSourceRef()) && isNotLocked(assocRef.getSourceRef())) {
			
			String diffKey = CHANGED_CATALOG_ENTRIES + assocRef.getSourceRef();
			Set<QName> pendingDiff = TransactionSupportUtil.getResource(diffKey);
			
			if (pendingDiff == null) {
				pendingDiff = new HashSet<>();
			}
			
			pendingDiff.add(assocRef.getTypeQName());
			TransactionSupportUtil.bindResource(diffKey, pendingDiff);
			
			queueNode(assocRef.getSourceRef());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		if (!isVersionNode(assocRef.getSourceRef()) && isNotLocked(assocRef.getSourceRef())) {
			
			String diffKey = CHANGED_CATALOG_ENTRIES + assocRef.getSourceRef();
			Set<QName> pendingDiff = TransactionSupportUtil.getResource(diffKey);
			
			if (pendingDiff == null) {
				pendingDiff = new HashSet<>();
			}
			
			pendingDiff.add(assocRef.getTypeQName());
			TransactionSupportUtil.bindResource(diffKey, pendingDiff);
			
			queueNode(assocRef.getSourceRef());
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
	}

}
