/*
 *
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * The Class SortableListPolicy.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class SortableListPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnAddAspectPolicy, NodeServicePolicies.OnDeleteNodePolicy,
		CopyServicePolicies.OnCopyNodePolicy, CopyServicePolicies.OnCopyCompletePolicy {

	private static final Log logger = LogFactory.getLog(SortableListPolicy.class);

	private DataListSortService dataListSortService;

	private EntityDictionaryService entityDictionaryService;

	private EntityListDAO entityListDAO;

	private RepoService repoService;

	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>dataListSortService</code>.</p>
	 *
	 * @param dataListSortService a {@link fr.becpg.repo.entity.datalist.DataListSortService} object.
	 */
	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init DepthLevelListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.EVERY_EVENT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onDeleteNode", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onCopyComplete"));

		logger.debug("Init SortableListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST,
				new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));

	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			// createNode
			if (before.isEmpty()) {
				// nothing to do, work is done in addAspect, otherwise it
				// duplicates
				// nodeRef in lucene index !!!
				return;
			}

			NodeRef beforeParentLevel = (NodeRef) before.get(BeCPGModel.PROP_PARENT_LEVEL);
			NodeRef afterParentLevel = (NodeRef) after.get(BeCPGModel.PROP_PARENT_LEVEL);

			if (logger.isDebugEnabled()) {
				logger.debug("call SortableListPolicy");

			}

			// has changed ?
			boolean hasChanged;
			if ((afterParentLevel != null) && !afterParentLevel.equals(beforeParentLevel) && nodeService.exists(nodeRef)
					&& nodeService.exists(afterParentLevel)) {

				if (entityDictionaryService.isSubClass(nodeService.getType(afterParentLevel), BeCPGModel.TYPE_ENTITY_V2)) {

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(afterParentLevel);
					if (listContainerNodeRef != null) {
						NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, nodeService.getType(nodeRef));
						if (listNodeRef != null) {
							repoService.moveNode(nodeRef, listNodeRef);
						}
					}
					nodeService.setProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL, null);
				} else if (entityDictionaryService.isSubClass(nodeService.getType(afterParentLevel), BeCPGModel.TYPE_ENTITYLIST_ITEM)
						&& entityDictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

					if (!nodeService.getPrimaryParent(afterParentLevel).getParentRef().equals(nodeService.getPrimaryParent(nodeRef).getParentRef())) {

						repoService.moveNode(nodeRef, nodeService.getPrimaryParent(afterParentLevel).getParentRef());

					}

				}

				hasChanged = true;
			} else if ((beforeParentLevel != null) && !beforeParentLevel.equals(afterParentLevel)) {// parentLevel																							// null
				hasChanged = true;
			} else {
				hasChanged = false;
			}

			if (hasChanged) {
				logger.debug("onUpdateProperties has changed");
				queueNode(nodeRef);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {

		if (policyBehaviourFilter.isEnabled(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			// try to avoid to do two times the work, otherwise it duplicates
			// nodeRef in lucene index !!!
			if (nodeService.exists(nodeRef)) {

				boolean addInQueue = false;

				if (aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL)) {
					NodeRef parentNodeRef = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
					if ((parentNodeRef != null)) {

						if (entityDictionaryService.isSubClass(nodeService.getType(parentNodeRef), BeCPGModel.TYPE_ENTITY_V2)) {
							NodeRef listContainerNodeRef = entityListDAO.getListContainer(parentNodeRef);
							if (listContainerNodeRef != null) {
								NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, nodeService.getType(nodeRef));
								if (listNodeRef != null) {
									repoService.moveNode(nodeRef, listNodeRef);
								}
							}
							nodeService.setProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL, null);
						} else if (entityDictionaryService.isSubClass(nodeService.getType(parentNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)
								&& entityDictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

							if (!nodeService.getPrimaryParent(parentNodeRef).getParentRef()
									.equals(nodeService.getPrimaryParent(nodeRef).getParentRef())) {

								repoService.moveNode(nodeRef, nodeService.getPrimaryParent(parentNodeRef).getParentRef());

							}
						}
					}

				}

				if ((nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null) && aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST)
						&& !nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
					addInQueue = true;
					if (logger.isDebugEnabled()) {
						logger.debug("Add sortable aspect policy "+nodeService.getType(nodeRef)+ " "+nodeService.getProperty( entityListDAO.getEntity(nodeRef), ContentModel.PROP_NAME));
						
					}
				}

				if (((nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) == null)
						|| (nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL) == null))
						&& aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL)) {
					addInQueue = true;
					// queue parent before
					NodeRef parentNodeRef = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
					if ((parentNodeRef != null) && ((nodeService.getProperty(parentNodeRef, BeCPGModel.PROP_SORT) == null)
							|| (nodeService.getProperty(parentNodeRef, BeCPGModel.PROP_DEPTH_LEVEL) == null))) {
						queueNode(parentNodeRef);
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Add depthLevel aspect policy on :"+nodeService.getType(nodeRef)+ " "+nodeService.getProperty( entityListDAO.getEntity(nodeRef), ContentModel.PROP_NAME) );
					}
				}

				if (addInQueue) {
					queueNode(nodeRef);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		L2CacheSupport.doInCacheContext(() -> {
			AuthenticationUtil.runAsSystem(() -> {
				dataListSortService.computeDepthAndSort(pendingNodes);
				return true;
			});

		}, false, true);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {

		// if folder is deleted, all children are
		
		if (nodeService.exists(childRef.getParentRef()) && isNodeArchived && ! isPendingDelete(childRef.getParentRef())) {
			
			logger.debug("SortableListPolicy.onDeleteNode");
			dataListSortService.deleteChildrens(childRef.getParentRef(), childRef.getChildRef());

		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.disableBehaviour( BeCPGModel.TYPE_ACTIVITY_LIST);

			logger.debug("onCopyComplete destinationRef " + destinationRef);

			NodeRef sourceParentLevelNodeRef = (NodeRef) nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_PARENT_LEVEL);

			// parent equals -> need to update the parent of copied node
			if (sourceParentLevelNodeRef != null) {

				NodeRef targetParentLevelNodeRef = (NodeRef) nodeService.getProperty(destinationRef, BeCPGModel.PROP_PARENT_LEVEL);
				if (sourceParentLevelNodeRef.equals(targetParentLevelNodeRef)) {

					NodeRef copiedParentNodeRef = null;
					if(copyMap.containsKey(sourceParentLevelNodeRef)) {
						logger.debug("Find new parent in copyMap");
						copiedParentNodeRef = copyMap.get(sourceParentLevelNodeRef);

					} else {
						// we assume sort is keeped during copy
						Integer sourceParentSort = (Integer) nodeService.getProperty(sourceParentLevelNodeRef, BeCPGModel.PROP_SORT);
	
						NodeRef targetParentNodeRef = nodeService.getPrimaryParent(destinationRef).getParentRef();
	
						 copiedParentNodeRef = BeCPGQueryBuilder.createQuery().parent(targetParentNodeRef)
								.ofType(nodeService.getType(sourceParentLevelNodeRef))
								.andPropEquals(BeCPGModel.PROP_SORT, sourceParentSort != null ? sourceParentSort.toString() : null).inDB().singleValue();
					}
					
					if (copiedParentNodeRef != null) {
						logger.debug("update the parent of copied node " + targetParentLevelNodeRef + " with value " + copiedParentNodeRef);
						nodeService.setProperty(destinationRef, BeCPGModel.PROP_PARENT_LEVEL, copiedParentNodeRef);
					} else {
						logger.warn("DepthLevelAspectCopyBehaviourCallback : parent not found.");
					}
				}
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.enableBehaviour( BeCPGModel.TYPE_ACTIVITY_LIST);
		}

	}

}
