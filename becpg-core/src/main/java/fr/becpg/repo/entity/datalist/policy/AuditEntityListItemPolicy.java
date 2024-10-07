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
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.behaviour.BehaviourRegistry;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>
 * AuditEntityListItemPolicy class.
 * </p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class AuditEntityListItemPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnDeleteNodePolicy, NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

	/** Constant <code>UPDATED_LISTS="AuditEntityListItemPolicy.UpdatedLists"</code> */
	public static final String UPDATED_LISTS = "AuditEntityListItemPolicy.UpdatedLists";
	private static final String CATALOG_ONLY = "AuditEntityListItemPolicy.CatalogOnly";

	private static final Log logger = LogFactory.getLog(AuditEntityListItemPolicy.class);
	
	private AuthenticationService authenticationService;

	private EntityCatalogService entityCatalogService;
	
	/**
	 * <p>
	 * Setter for the field <code>entityCatalogService</code>.
	 * </p>
	 *
	 * @param entityCatalogService
	 *            a {@link fr.becpg.repo.entity.catalog.EntityCatalogService}
	 *            object.
	 */
	public void setEntityCatalogService(EntityCatalogService entityCatalogService) {
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
	 * {@inheritDoc}
	 *
	 * <p>
	 * doInit.
	 * </p>
	 */
	@Override
	public void doInit() {
		logger.debug("Init AuditEntityListItemPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onUpdateProperties"));

		super.disableOnCopyBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		queueListNodeRef(nodeService.getPrimaryParent(destinationRef).getParentRef());
		super.onCopyComplete(classRef, sourceNodeRef, destinationRef, copyToNewNode, copyMap);
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		queueListNodeRef(childAssocRef.getParentRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		queueListNodeRef(childAssocRef.getParentRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		if (!ContentModel.ASSOC_ORIGINAL.equals(assocRef.getTypeQName())) {
			queueListNodeRef(nodeService.getPrimaryParent(assocRef.getSourceRef()).getParentRef());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		queueListNodeRef(nodeService.getPrimaryParent(assocRef.getSourceRef()).getParentRef());
	}

	private void queueListNodeRef(NodeRef listNodeRef) {
		if (policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ENTITYLIST_ITEM) && policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)) {
			queueNode(listNodeRef);
		} else {
			queueNode(CATALOG_ONLY, listNodeRef);
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
		for (NodeRef listNodeRef : pendingNodes) {
			if (nodeService.exists(listNodeRef)) {
				
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
			updateEntityAuditedFields(entityNodeRef, listNodeRefByContainer.get(listContainerNodeRef), CATALOG_ONLY.equals(key));
		}
		return true;
	}

	private void updateEntityAuditedFields(NodeRef entityNodeRef, Set<NodeRef> listNodeRefs, boolean catalogOnly) {
		if ((entityNodeRef != null) && !isVersionNode(entityNodeRef) && isNotLocked(entityNodeRef)) {
			try {
				if (policyBehaviourFilter.isEnabled(entityNodeRef, ContentModel.ASPECT_AUDITABLE) && !catalogOnly) {
					if (logger.isDebugEnabled()) {
						logger.debug("Update modified date of entity:" + entityNodeRef);
					}
					
					TransactionSupportUtil.bindResource(UPDATED_LISTS + entityNodeRef, listNodeRefs);
					policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
					nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIED, Calendar.getInstance().getTime());
					nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIER, authenticationService.getCurrentUserName());
				}
				entityCatalogService.updateAuditedField(entityNodeRef, null, listNodeRefs);
			} finally {
				policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
			}

		}
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
				
				boolean shouldIgnoreAudit = changedEntries.stream().anyMatch(BehaviourRegistry::shouldIgnoreAuditField);
				
				if (!shouldIgnoreAudit) {
					queueListNodeRef(nodeService.getPrimaryParent(nodeRef).getParentRef());
				}
			}
		}
	}

}
