package fr.becpg.repo.activity.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.behaviour.BehaviourRegistry;
import fr.becpg.repo.behaviour.BehaviourRegistry.ActivityBehaviour;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.jscript.BeCPGStateHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>EntityActivityPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityActivityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnAddAspectPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy, ContentServicePolicies.OnContentUpdatePolicy {

	private static final Log logger = LogFactory.getLog(EntityActivityPolicy.class);

	/** Constant <code>KEY_QUEUE_UPDATED="EntityActivity_updated"</code> */
	public static final String KEY_QUEUE_UPDATED = "EntityActivity_updated";
	/** Constant <code>KEY_QUEUE_DELETED="EntityActivity_deleted"</code> */
	public static final String KEY_QUEUE_DELETED = "EntityActivity_deleted";
	/** Constant <code>KEY_QUEUE_CREATED="EntityActivity_created"</code> */
	public static final String KEY_QUEUE_CREATED = "EntityActivity_created";
	/** Constant <code>KEY_QUEUE_UPDATED_STATUS="EntityActivity_UpdatedStatus"</code> */
	public static final String KEY_QUEUE_UPDATED_STATUS = "EntityActivity_UpdatedStatus";
	/** Constant <code>KEY_QUEUE_ADDED_TPL_ASPECT="EntityActivity_AddedTplAspect"</code> */
	public static final String KEY_QUEUE_ADDED_TPL_ASPECT = "EntityActivity_AddedTplAspect";

	private static final String DELIMITER = "###";
	private static final Pattern pattern = Pattern.compile(Pattern.quote(DELIMITER));

	static {
		BehaviourRegistry.registerActivityBehaviour(new ActivityBehaviour(ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER, ForumModel.PROP_COMMENT_COUNT,
				BeCPGModel.PROP_SORT, BeCPGModel.PROP_ENTITY_SCORE, BeCPGModel.PROP_STATE_ACTIVITY_PREVIOUSSTATE,
				BeCPGModel.PROP_STATE_ACTIVITY_MODIFIED, BeCPGModel.PROP_STATE_ACTIVITY_MODIFIER));
	}

	private EntityActivityService entityActivityService;

	private EntityDictionaryService entityDictionaryService;

	/**
	 * <p>Setter for the field <code>entityActivityService</code>.</p>
	 *
	 * @param entityActivityService a {@link fr.becpg.repo.activity.EntityActivityService} object.
	 */
	public void setEntityActivityService(EntityActivityService entityActivityService) {
		this.entityActivityService = entityActivityService;
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
	 * {@inheritDoc}
	 *
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init EntityActivityPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_ENTITY_TPL,
				new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onDeleteAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onDeleteAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ForumModel.TYPE_POST,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ForumModel.TYPE_POST,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ForumModel.TYPE_POST,
				new JavaBehaviour(this, "beforeDeleteNode"));

		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, ContentModel.TYPE_CONTENT,
				new JavaBehaviour(this, "onContentUpdate"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ContentModel.TYPE_CONTENT,
				new JavaBehaviour(this, "beforeDeleteNode"));

		policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), BeCPGModel.TYPE_ACTIVITY_LIST,
				new JavaBehaviour(this, "getCopyCallback"));

	}

	/** {@inheritDoc} */
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return DoNothingCopyBehaviourCallback.getInstance();
	}

	/**
	 * <p>ignoreType.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param before a {@link java.util.Map} object.
	 * @param after a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	public boolean ignoreType(QName type, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		return (type.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
				|| ((!before.containsKey(type) || (before.get(type) == null)) && ((after.get(type) == null) || after.get(type).equals("")))
				|| ((!after.containsKey(type) || (after.get(type) == null)) && ((before.get(type) == null) || before.get(type).equals(""))));
	}

	/** {@inheritDoc} */
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ACTIVITY_LIST)) {
			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked  :" + nodeRef);
				}
				return;
			}
			QName type = nodeService.getType(nodeRef);
			if (accept(type)) {
				queueNode(KEY_QUEUE_ADDED_TPL_ASPECT, nodeRef);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)
				&& policyBehaviourFilter.isEnabled(nodeRef, ContentModel.ASPECT_AUDITABLE)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ACTIVITY_LIST)) {

			QName entityState = null;
			String beforeState = null;
			String afterState = null;

			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked  :" + nodeRef);
				}
				return;
			}

			if (((before != null) && before.equals(after)) || (before == after)) {
				return;
			}

			boolean isDifferent = false;
			boolean isIgnoreState = false;
			QName type = nodeService.getType(nodeRef);
			Map<QName, Pair<Serializable, Serializable>> updatedProperties = new HashMap<>();
			if (accept(type)) {

				if ((before != null) && (after != null) && (before.size() < after.size())) {
					MapDifference<QName, Serializable> diff = Maps.difference(before, after);
					for (QName afterType : diff.entriesOnlyOnRight().keySet()) {
						if (!BehaviourRegistry.shouldIgnoreActivityField(afterType) && (after.get(afterType) != null) && (after.get(afterType) != "") && !ignoreType(afterType, before, after)) {
							isDifferent = true;

							Pair<Serializable, Serializable> beforeAfterProperties = new Pair<>(null, after.get(afterType));
							updatedProperties.put(afterType, beforeAfterProperties);
						}
					}

				}

				if ((before != null) && (after != null)) {
					for (QName beforeType : before.keySet()) {
						if (!BehaviourRegistry.shouldIgnoreActivityField(beforeType) && !ignoreType(beforeType, before, after)) {

							if (((before.get(beforeType) == null) && (after.get(beforeType) == null)) || ((before.get(beforeType) != null)
									&& (after.get(beforeType) != null) && before.get(beforeType).equals(after.get(beforeType)))) {
								continue;
							}

							if (((before.get(beforeType) != null) && (after.get(beforeType) != null)
									&& !before.get(beforeType).equals(after.get(beforeType)))
									|| ((before.get(beforeType) == null) || (after.get(beforeType) == null))) {

								if ((before.get(beforeType) != null) && (after.get(beforeType) != null)
										&& before.get(beforeType).getClass().equals(MLText.class)) {
									MLText beforeMlText = (MLText) before.get(beforeType);
									MLText afterMlText = (MLText) after.get(beforeType);
									for (Entry<Locale, String> afterEntry : afterMlText.entrySet()) {

										if (beforeMlText.containsKey(afterEntry.getKey())) {
											String afterValue = afterEntry.getValue();
											String beforeValue = beforeMlText.getValue(afterEntry.getKey());

											if (!(afterValue != null ? afterValue : "").equals(beforeValue != null ? beforeValue : "")) {

												isDifferent = true;
												if (!entityActivityService.isMatchingStateProperty(beforeType)) {
													Pair<Serializable, Serializable> beforeAfterProperties = new Pair<>(before.get(beforeType), after.get(beforeType));
													updatedProperties.put(beforeType, beforeAfterProperties);
												}
											}
										} else {
											String afterValue = afterEntry.getValue();
											String beforeValue = "";

											if (!(afterValue != null ? afterValue : "").equals(beforeValue)) {

												isDifferent = true;
												if (!entityActivityService.isMatchingStateProperty(beforeType)) {
													Pair<Serializable, Serializable> beforeAfterProperties = new Pair<>(before.get(beforeType), after.get(beforeType));
													updatedProperties.put(beforeType, beforeAfterProperties);
												}
											}
										}
									}
								} else {
									isDifferent = true;
									if (!entityActivityService.isMatchingStateProperty(beforeType)) {
										Pair<Serializable, Serializable> beforeAfterProperties = new Pair<>(before.get(beforeType), after.get(beforeType));
										updatedProperties.put(beforeType, beforeAfterProperties);
									}
								}
							}

							if (entityActivityService.isMatchingStateProperty(beforeType)) {
								if (entityActivityService.isIgnoreStateProperty(beforeType)) {
									isIgnoreState = true;
								}
								entityState = beforeType;
								beforeState = before.get(entityState) != null ? before.get(entityState).toString() : "";
								afterState = after.get(entityState) != null ? after.get(entityState).toString() : "";
							}
						}
					}
				}
			}

			if (isDifferent) {
				if ((entityState != null) && !isIgnoreState) {
					queueNode(KEY_QUEUE_UPDATED_STATUS + DELIMITER + beforeState + DELIMITER + afterState, nodeRef);
					queueNode(KEY_QUEUE_UPDATED_STATUS, nodeRef);
				} else {
					queueNode(KEY_QUEUE_UPDATED, nodeRef);
				}
				
				if (updatedProperties != null && !updatedProperties.isEmpty()) {
					if ((TransactionSupportUtil.getResource(KEY_QUEUE_UPDATED_STATUS + nodeRef.toString()) == null)) {
						TransactionSupportUtil.bindResource(KEY_QUEUE_UPDATED_STATUS + nodeRef.toString(), updatedProperties);
					} else {
						Map<QName, Pair<Serializable, Serializable>> beforeUpdatedProperties = TransactionSupportUtil.getResource(KEY_QUEUE_UPDATED_STATUS + nodeRef.toString());
						
						boolean changed = false;
						
						for (Entry<QName, Pair<Serializable, Serializable>> entry : beforeUpdatedProperties.entrySet()) {
							if (!updatedProperties.containsKey(entry.getKey())) {
								updatedProperties.put(entry.getKey(), entry.getValue());
								changed = true;
							}
						}
						
						if (changed) {
							TransactionSupportUtil.bindResource(KEY_QUEUE_UPDATED_STATUS + nodeRef.toString(), updatedProperties);
						}

					}
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ACTIVITY_LIST)) {
			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked  :" + assocRef);
				}
				return;
			}

			QName type = assocRef.getTypeQName();

			if (assocRef.getTargetRef() != null) {
				Map<QName, Pair<List<Pair<NodeRef, Serializable>>, List<Pair<NodeRef, Serializable>>>> resources = TransactionSupportUtil.getResource(KEY_QUEUE_UPDATED_STATUS + assocRef.getSourceRef());
				List<Pair<NodeRef, Serializable>> afterAssocs = new ArrayList<>();
				List<Pair<NodeRef, Serializable>> beforeAssocs = new ArrayList<>();
				
				List<AssociationRef> currentAssocs = nodeService.getTargetAssocs(assocRef.getSourceRef(), type);
				
				if (resources != null && resources.get(type) != null && resources.get(type).getFirst() != null) {
					beforeAssocs = resources.get(type).getFirst();
				} else {
					
					for (AssociationRef currentAssoc : currentAssocs) {
						if (!currentAssoc.equals(assocRef)) {
							beforeAssocs.add(new Pair<>(currentAssoc.getTargetRef(), nodeService.getProperty(currentAssoc.getTargetRef(), ContentModel.PROP_NAME)));
						}
					}
				}
				
				for (AssociationRef currentAssoc : currentAssocs) {
					afterAssocs.add(new Pair<>(currentAssoc.getTargetRef(), nodeService.getProperty(currentAssoc.getTargetRef(), ContentModel.PROP_NAME)));
				}
				
				Pair<List<Pair<NodeRef, Serializable>>, List<Pair<NodeRef, Serializable>>> beforeAfterAssocs = new Pair<>(beforeAssocs, afterAssocs);
				
				if (resources == null) {
					resources = new HashMap<>();
				}
				
				resources.put(type, beforeAfterAssocs);
				
				
				if (!resources.isEmpty()) {
					TransactionSupportUtil.bindResource(KEY_QUEUE_UPDATED_STATUS + assocRef.getSourceRef(), resources);
				}
				queueNode(KEY_QUEUE_UPDATED, assocRef.getSourceRef());
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ACTIVITY_LIST)) {
			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked  :" + assocRef);
				}
				return;
			}

			QName type = assocRef.getTypeQName();

			if (assocRef.getTargetRef() != null) {
				Map<QName, Pair<List<Pair<NodeRef, Serializable>>, List<Pair<NodeRef, Serializable>>>> resources = TransactionSupportUtil.getResource(KEY_QUEUE_UPDATED_STATUS + assocRef.getSourceRef());
				List<Pair<NodeRef, Serializable>> afterAssocs = new ArrayList<>();
				List<Pair<NodeRef, Serializable>> beforeAssocs = new ArrayList<>();
				
				List<AssociationRef> currentAssocs = nodeService.getTargetAssocs(assocRef.getSourceRef(), type);
				
				if (resources != null && resources.get(type) != null && resources.get(type).getFirst() != null) {
					beforeAssocs = resources.get(type).getFirst();
				} else {
					
					for (AssociationRef currentAssoc : currentAssocs) {
						beforeAssocs.add(new Pair<>(currentAssoc.getTargetRef(), nodeService.getProperty(currentAssoc.getTargetRef(), ContentModel.PROP_NAME)));
					}
					beforeAssocs.add(new Pair<>(assocRef.getTargetRef(), nodeService.getProperty(assocRef.getTargetRef(), ContentModel.PROP_NAME)));
				}
				
				for (AssociationRef currentAssoc : currentAssocs) {
					afterAssocs.add(new Pair<>(currentAssoc.getTargetRef(), nodeService.getProperty(currentAssoc.getTargetRef(), ContentModel.PROP_NAME)));
				}
				
				Pair<List<Pair<NodeRef, Serializable>>, List<Pair<NodeRef, Serializable>>> beforeAfterAssocs = new Pair<>(beforeAssocs, afterAssocs);
				
				if (resources == null) {
					resources = new HashMap<>();
				}
				
				resources.put(type, beforeAfterAssocs);
				
				if (!resources.isEmpty()) {
					TransactionSupportUtil.bindResource(KEY_QUEUE_UPDATED_STATUS + assocRef.getSourceRef(), resources);
				}
				queueNode(KEY_QUEUE_UPDATED, assocRef.getSourceRef());
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ACTIVITY_LIST)) {
			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked :" + nodeRef);
				}
				return;
			}

			QName type = nodeService.getType(nodeRef);
			if (ContentModel.TYPE_CONTENT.equals(type)) {

				queueNode(KEY_QUEUE_UPDATED, nodeRef);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ACTIVITY_LIST)) {
			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked  :" + childAssocRef.getChildRef());
				}
				return;
			}

			QName type = nodeService.getType(childAssocRef.getChildRef());
			if (accept(type)) {
				queueNode(KEY_QUEUE_CREATED, childAssocRef.getChildRef());
				if(entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)) {
					BeCPGStateHelper.onCreateEntity(childAssocRef.getChildRef());
				}
			}
			
			
		}
	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ACTIVITY_LIST)) {
			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked  :" + nodeRef);
				}
				return;
			}

			QName type = nodeService.getType(nodeRef);
			if (accept(type)) {
				registerActivity(nodeRef, type, ActivityEvent.Delete);
				queueNode(KEY_QUEUE_DELETED, nodeRef);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			if (nodeService.exists(nodeRef)) {
				QName type = nodeService.getType(nodeRef);
				switch (key) {
				case KEY_QUEUE_UPDATED:
					if (!containsNodeInQueue(KEY_QUEUE_CREATED, nodeRef) && !containsNodeInQueue(KEY_QUEUE_DELETED, nodeRef)
							&& !containsNodeInQueue(KEY_QUEUE_UPDATED_STATUS, nodeRef)) {
						registerActivity(nodeRef, type, ActivityEvent.Update);
					}
					break;
				case KEY_QUEUE_CREATED:
					registerActivity(nodeRef, type, ActivityEvent.Create);
					break;
				case KEY_QUEUE_ADDED_TPL_ASPECT:
					registerActivity(nodeRef, type, null);
					break;
				default:
					if (key.contains(KEY_QUEUE_UPDATED_STATUS + DELIMITER)) {
						String[] strState = pattern.split(key);
						logger.debug("Action change state, post activity");
						if ((strState != null) && (strState.length > 2)) {
							entityActivityService.postStateChangeActivity(nodeRef, null, strState[1], strState[2]);
						}
						if (TransactionSupportUtil.getResource(KEY_QUEUE_UPDATED_STATUS + nodeRef.toString()) != null) {
							registerActivity(nodeRef, type, ActivityEvent.Update);
						}
					}
					break;
				}
			}
		}
		return false;
	}

	private boolean accept(QName type) {
		return (ForumModel.TYPE_POST.equals(type) || ContentModel.TYPE_CONTENT.equals(type)
				|| entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)
				|| ((entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM)) 
						&& !BeCPGModel.TYPE_ACTIVITY_LIST.equals(type)
						&& !BeCPGModel.TYPE_NOTIFICATIONRULELIST.equals(type)
						));
	}

	private void registerActivity(NodeRef actionedUponNodeRef, QName type, ActivityEvent activityEvent) {
		NodeRef entityNodeRef = entityActivityService.getEntityNodeRefForActivity(actionedUponNodeRef, type);

		if ((entityNodeRef != null) && nodeService.exists(entityNodeRef)) {
			boolean isEnabledBehaviour = policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE);
			try {
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				if (activityEvent != null) {
					if (ForumModel.TYPE_POST.equals(type)) {
						logger.debug("Action upon comment, post activity");
						entityActivityService.postCommentActivity(entityNodeRef, actionedUponNodeRef, activityEvent);
					} else if (ContentModel.TYPE_CONTENT.equals(type)) {
						if(logger.isDebugEnabled()) {
							logger.debug("Action upon content, post activity for: "+nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
						}
						entityActivityService.postContentActivity(entityNodeRef, actionedUponNodeRef, activityEvent);
					} else {
						Map<QName, Pair<Serializable, Serializable>> updatedFields = TransactionSupportUtil.getResource(KEY_QUEUE_UPDATED_STATUS + actionedUponNodeRef.toString());
						if (!BehaviourRegistry.shouldIgnoreActivity(actionedUponNodeRef, type, updatedFields)) {
							if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
								if(logger.isDebugEnabled()) {
									logger.debug("Action upon datalist, post activity for: "+nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME)+ " ("+nodeService.getType(actionedUponNodeRef)+")");
								}
								entityActivityService.postDatalistActivity(entityNodeRef, actionedUponNodeRef, activityEvent, updatedFields);
							} else if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)) {
								if(logger.isDebugEnabled()) {
									logger.debug("Action upon entity, post activity for: "+nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
								}
								entityActivityService.postEntityActivity(actionedUponNodeRef, ActivityType.Entity, activityEvent, updatedFields);
							}
						}
					}
				} else if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)) {
					entityActivityService.clearAllActivities(entityNodeRef);
				}
			} finally {
				if (isEnabledBehaviour) {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}
			}
		}

	}
}
