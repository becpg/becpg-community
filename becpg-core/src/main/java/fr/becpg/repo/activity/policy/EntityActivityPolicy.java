package fr.becpg.repo.activity.policy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.L2CacheSupport;

public class EntityActivityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy,
		NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnCreateNodePolicy, ContentServicePolicies.OnContentUpdatePolicy {

	private static final Log logger = LogFactory.getLog(EntityActivityPolicy.class);

	protected static final String KEY_QUEUE_UPDATED = "EntityActivity_updated";
	protected static final String KEY_QUEUE_DELETED = "EntityActivity_deleted";
	protected static final String KEY_QUEUE_CREATED = "EntityActivity_created";
	protected static final String KEY_QUEUE_UPDATED_STATUS = "EntityActivity_UpdatedStatus";

	private static final Set<QName> isIgnoredTypes = new HashSet<>();

	private static final String DELIMITER = "###";
	private static final Pattern pattern = Pattern.compile(Pattern.quote(DELIMITER));

	static {
		isIgnoredTypes.add(ContentModel.PROP_MODIFIED);
		isIgnoredTypes.add(ContentModel.PROP_MODIFIER);
		isIgnoredTypes.add(ForumModel.PROP_COMMENT_COUNT);
	}

	private EntityActivityService entityActivityService;

	private EntityDictionaryService entityDictionaryService;

	public void setEntityActivityService(EntityActivityService entityActivityService) {
		this.entityActivityService = entityActivityService;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init EntityActivityPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "beforeDeleteNode"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "beforeDeleteNode"));

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

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return DoNothingCopyBehaviourCallback.getInstance();
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)) {

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
			QName type = nodeService.getType(nodeRef);
			if (accept(type)) {

				if ((before != null) && (after != null) && (before.size() == after.size())) {
					for (QName beforeType : before.keySet()) {
						if (!isIgnoredTypes.contains(beforeType)) {
							if (((before.get(beforeType) != null) && !before.get(beforeType).equals(after.get(beforeType)))
									|| ((before.get(beforeType) == null) && (after.get(beforeType) != null))) {

								if (entityActivityService.isMatchingStateProperty(beforeType)) {
									entityState = beforeType;
									beforeState = before.get(entityState).toString();
									afterState = after.get(entityState).toString();
								}

								isDifferent = true;

								break;
							}

						}
					}
				} else if ((before != null) && (after != null) && before.size() < after.size()) {
					isDifferent = true;
				}

				if (isDifferent) {

					if (entityState != null) {
						queueNode(KEY_QUEUE_UPDATED_STATUS + DELIMITER + beforeState + DELIMITER + afterState, nodeRef);
					} else {
						queueNode(KEY_QUEUE_UPDATED, nodeRef);
					}
				}
			}
		}

	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)) {
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

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)) {
			if (L2CacheSupport.isThreadLockEnable()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Entity [" + Thread.currentThread().getName() + "] is locked  :" + childAssocRef.getChildRef());
				}
				return;
			}

			QName type = nodeService.getType(childAssocRef.getChildRef());
			if (accept(type)) {
				queueNode(KEY_QUEUE_CREATED, childAssocRef.getChildRef());
			}
		}

	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE) && policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_SORTABLE_LIST)) {
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

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		Set<QName> types = new HashSet<>();

		for (NodeRef nodeRef : pendingNodes) {
			if (nodeService.exists(nodeRef)) {
				QName type = nodeService.getType(nodeRef);
				if (!(entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM) && types.contains(type))) {

					switch (key) {
					case KEY_QUEUE_UPDATED:
						if (!containsNodeInQueue(KEY_QUEUE_CREATED, nodeRef) && !containsNodeInQueue(KEY_QUEUE_DELETED, nodeRef)) {
							registerActivity(nodeRef, type, ActivityEvent.Update);
						}
						break;
					case KEY_QUEUE_CREATED:
						registerActivity(nodeRef, type, ActivityEvent.Create);
						break;
					default:
						if (key.contains(KEY_QUEUE_UPDATED_STATUS)) {
							String[] strState = pattern.split(key);
							entityActivityService.postStateChangeActivity(nodeRef, null, strState[1], strState[2]);
						}
						break;
					}

				}
				types.add(type);

			}
		}

	}

	private boolean accept(QName type) {
		return (ForumModel.TYPE_POST.equals(type) || ContentModel.TYPE_CONTENT.equals(type)
				|| entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)
				|| ((entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM)) && !BeCPGModel.TYPE_ACTIVITY_LIST.equals(type)));
	}

	private void registerActivity(NodeRef actionedUponNodeRef, QName type, ActivityEvent activityEvent) {

		NodeRef entityNodeRef = entityActivityService.getEntityNodeRefForActivity(actionedUponNodeRef, type);

		if (entityNodeRef != null) {
			try {
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				if (activityEvent != null) {
					if (ForumModel.TYPE_POST.equals(type)) {
						logger.debug("Action upon comment, post activity");
						entityActivityService.postCommentActivity(entityNodeRef, actionedUponNodeRef, activityEvent);
					} else if (ContentModel.TYPE_CONTENT.equals(type)) {
						logger.debug("Action upon content, post activity");
						entityActivityService.postContentActivity(entityNodeRef, actionedUponNodeRef, activityEvent);
					} else if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						logger.debug("Action upon datalist, post activity");
						entityActivityService.postDatalistActivity(entityNodeRef, actionedUponNodeRef, activityEvent);
					} else if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)) {
						logger.debug("Action upon entity, post activity");
						entityActivityService.postEntityActivity(actionedUponNodeRef, ActivityType.Entity, activityEvent);
					}
				}
			} finally {
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			}

		}

	}

}
