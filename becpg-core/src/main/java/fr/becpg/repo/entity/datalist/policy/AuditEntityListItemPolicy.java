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
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
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
import fr.becpg.repo.entity.EntityDictionaryService;
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
	/** Constant <code>IGNORED_LISTS="AuditEntityListItemPolicy.IgnoredLists"</code> */
	private static final String IGNORED_LISTS = "AuditEntityListItemPolicy.IgnoredLists";
	/** Constant <code>CATALOG_ONLY="AuditEntityListItemPolicy.CatalogOnly"</code> */
	private static final String CATALOG_ONLY = "AuditEntityListItemPolicy.CatalogOnly";
	/** Constant <code>VARIANT_ENTITIES="AuditEntityListItemPolicy.VariantEntities"</code> */
	private static final String VARIANT_ENTITIES = "AuditEntityListItemPolicy.VariantEntities";

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(AuditEntityListItemPolicy.class);
	
	private AuthenticationService authenticationService;

	private EntityCatalogService entityCatalogService;
	
	private CommentService commentService;
	
	private ContentService contentService;
	
	private EntityDictionaryService entityDictionaryService;
	
	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}
	
	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * <p>Setter for the field <code>commentService</code>.</p>
	 *
	 * @param commentService a {@link org.alfresco.repo.forum.CommentService} object
	 */
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	
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

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_VARIANT,
				new JavaBehaviour(this, "onCreateVariant"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_VARIANT,
				new JavaBehaviour(this, "onDeleteVariant"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_VARIANT,
				new JavaBehaviour(this, "onUpdateVariantProperties"));

		super.disableOnCopyBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		queueListNodeRef(nodeService.getPrimaryParent(destinationRef).getParentRef());
		super.onCopyComplete(classRef, sourceNodeRef, destinationRef, copyToNewNode, copyMap);
		if (entityDictionaryService.isSubClass(classRef, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
			copyComments(sourceNodeRef, destinationRef);
		}
	}

	/**
	 * <p>copyComments.</p>
	 *
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param destinationRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private void copyComments(NodeRef sourceNodeRef, NodeRef destinationRef) {
		PagingResults<NodeRef> comments = commentService.listComments(sourceNodeRef, new PagingRequest(5000, null));
		if (comments != null) {
			for (NodeRef commentNodeRef : comments.getPage()) {
				NodeRef newComment = null;
				boolean mlAware = MLPropertyInterceptor.setMLAware(false);
				try {
					MLPropertyInterceptor.setMLAware(false);
					ContentReader reader = contentService.getReader(commentNodeRef, ContentModel.PROP_CONTENT);
					if (reader != null) {
						String comment = reader.getContentString();
						newComment = commentService.createComment(destinationRef,
								(String) nodeService.getProperty(commentNodeRef, ContentModel.PROP_TITLE), comment, false);
						nodeService.setProperty(newComment, ContentModel.PROP_CREATED,
								nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATED));
						nodeService.setProperty(newComment, ContentModel.PROP_CREATOR,
								nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR));
						nodeService.setProperty(newComment, ContentModel.PROP_MODIFIED,
								nodeService.getProperty(commentNodeRef, ContentModel.PROP_MODIFIED));
					}
				} finally {
					MLPropertyInterceptor.setMLAware(mlAware);
				}
			}
		}
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

	/**
	 * <p>queueListNodeRef.</p>
	 *
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private void queueListNodeRef(NodeRef listNodeRef) {
		if (policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ENTITYLIST_ITEM) && policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)) {
			queueNode(listNodeRef);
		} else {
			queueNode(CATALOG_ONLY, listNodeRef);
		}
	}

	/**
	 * <p>Propagates the creation of a variant to its entity.</p>
	 *
	 * @param childAssocRef a {@link org.alfresco.service.cmr.repository.ChildAssociationRef} object
	 */
	public void onCreateVariant(ChildAssociationRef childAssocRef) {
		queueVariantEntity(childAssocRef.getParentRef());
	}

	/**
	 * <p>Propagates the deletion of a variant to its entity.</p>
	 *
	 * @param childAssocRef a {@link org.alfresco.service.cmr.repository.ChildAssociationRef} object
	 * @param isNodeArchived a boolean
	 */
	public void onDeleteVariant(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		queueVariantEntity(childAssocRef.getParentRef());
	}

	/**
	 * <p>Propagates the update of a variant to its entity.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param before a {@link java.util.Map} object
	 * @param after a {@link java.util.Map} object
	 */
	public void onUpdateVariantProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (!isVersionNode(nodeRef) && isNotLocked(nodeRef) && (before != null) && (after != null)) {
			Set<QName> changedEntries = extractChangedEntries(before, after);
			if (!changedEntries.isEmpty() && changedEntries.stream().noneMatch(BehaviourRegistry::shouldIgnoreAuditField)) {
				queueVariantEntity(nodeService.getPrimaryParent(nodeRef).getParentRef());
			}
		}
	}

	/**
	 * <p>Queues the entity a variant belongs to. Unlike a datalist item, a variant is a direct child of
	 * the entity, so there is no list folder to walk up from.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private void queueVariantEntity(NodeRef entityNodeRef) {
		if (policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ENTITYLIST_ITEM) && policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)) {
			queueNode(VARIANT_ENTITIES, entityNodeRef);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Store in the entity list folder that an item has been deleted.
	 */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if (VARIANT_ENTITIES.equals(key)) {
			for (NodeRef entityNodeRef : pendingNodes) {
				updateEntityAuditedFields(entityNodeRef, null, false);
			}
			return true;
		}

		Set<NodeRef> listNodeRefs = new HashSet<>();
		Set<NodeRef> listContainerNodeRefs = new HashSet<>();
		Map<NodeRef, Set<NodeRef>> listNodeRefByContainer = new HashMap<>();
		for (NodeRef listNodeRef : pendingNodes) {
			
			Set<NodeRef> ignoredLists = TransactionSupportUtil.getResource(IGNORED_LISTS);
			if ((ignoredLists == null || !ignoredLists.contains(listNodeRef)) && !listNodeRefs.contains(listNodeRef)
					&& nodeService.exists(listNodeRef)) {
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
		
		for (NodeRef listContainerNodeRef : listContainerNodeRefs) {
			NodeRef entityNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
			updateEntityAuditedFields(entityNodeRef, listNodeRefByContainer.get(listContainerNodeRef), CATALOG_ONLY.equals(key));
		}
		return true;
	}

	/**
	 * <p>updateEntityAuditedFields.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param listNodeRefs a {@link java.util.Set} object
	 * @param catalogOnly a boolean
	 */
	private void updateEntityAuditedFields(NodeRef entityNodeRef, Set<NodeRef> listNodeRefs, boolean catalogOnly) {
		if ((entityNodeRef != null) && !isVersionNode(entityNodeRef) && isNotLocked(entityNodeRef)) {
			
				if (policyBehaviourFilter.isEnabled(entityNodeRef, ContentModel.ASPECT_AUDITABLE) && !catalogOnly) {
					if (logger.isDebugEnabled()) {
						logger.debug("Update modified date of entity:" + entityNodeRef);
					}
					
					TransactionSupportUtil.bindResource(UPDATED_LISTS + entityNodeRef, listNodeRefs);
					try {
						policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
						nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIED, Calendar.getInstance().getTime());
						if(!AuthenticationUtil.getSystemUserName().equals(authenticationService.getCurrentUserName())) {
							nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIER, authenticationService.getCurrentUserName());
						}
					} finally {
						policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
					}
				}
				entityCatalogService.updateAuditedField(entityNodeRef, null, listNodeRefs);
		}
	}

	/**
	 * <p>Lists the properties that differ between the two states of a node.</p>
	 *
	 * @param before a {@link java.util.Map} object
	 * @param after a {@link java.util.Map} object
	 * @return a {@link java.util.Set} object
	 */
	private Set<QName> extractChangedEntries(Map<QName, Serializable> before, Map<QName, Serializable> after) {
		MapDifference<QName, Serializable> diff = Maps.difference(before, after);
		Set<QName> changedEntries = new HashSet<>();
		if (!diff.areEqual()) {
			changedEntries.addAll(diff.entriesDiffering().keySet());
			changedEntries.addAll(diff.entriesOnlyOnLeft().keySet());
			changedEntries.addAll(diff.entriesOnlyOnRight().keySet());
		}
		return changedEntries;
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (!isVersionNode(nodeRef) && isNotLocked(nodeRef) && before != null && after != null) {

			Set<QName> changedEntries = extractChangedEntries(before, after);

			if (!changedEntries.isEmpty()) {

				boolean shouldIgnoreAudit = changedEntries.stream().anyMatch(BehaviourRegistry::shouldIgnoreAuditField);
				
				if (!shouldIgnoreAudit) {
					queueListNodeRef(nodeService.getPrimaryParent(nodeRef).getParentRef());
				} else {
					Set<NodeRef> ignoredLists = TransactionSupportUtil.getResource(IGNORED_LISTS);
					if (ignoredLists == null) {
						ignoredLists = new HashSet<>();
					}
					ignoredLists.add(nodeService.getPrimaryParent(nodeRef).getParentRef());
					TransactionSupportUtil.bindResource(IGNORED_LISTS, ignoredLists);
				}
			}
		}
	}

}
