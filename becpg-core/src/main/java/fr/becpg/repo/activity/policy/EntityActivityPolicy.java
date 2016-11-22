package fr.becpg.repo.activity.policy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.L2CacheSupport;

public class EntityActivityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy,
		NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnCreateNodePolicy, ContentServicePolicies.OnContentUpdatePolicy {

	private static final Log logger = LogFactory.getLog(EntityActivityPolicy.class);

	protected static final String KEY_QUEUE_UPDATED = "EntityActivity_updated";
	protected static final String KEY_QUEUE_DELETED = "EntityActivity_deleted";
	protected static final String KEY_QUEUE_CREATED = "EntityActivity_created";

	private static final Set<QName> isIgnoredTypes = new HashSet<>();

	static {
		isIgnoredTypes.add(ContentModel.PROP_MODIFIED);
		isIgnoredTypes.add(ContentModel.PROP_MODIFIER);
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

	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

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

		if ((before != null) && (after != null) && (before.size() == after.size())) {
			for (QName beforeType : before.keySet()) {
				if (!isIgnoredTypes.contains(beforeType)) {
					if (((before.get(beforeType) != null) && !before.get(beforeType).equals(after.get(beforeType)))
							|| ((before.get(beforeType) == null) && (after.get(beforeType) != null))) {
						isDifferent = true;
						break;
					}

				}
			}
		}

		if (isDifferent) {
			QName type = nodeService.getType(nodeRef);
			if (accept(type)) {

				queueNode(KEY_QUEUE_UPDATED, nodeRef);
			}
		}
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {

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

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

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

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {

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
						break;
					}

				}
				types.add(type);

			}
		}

	}

	private boolean accept(QName type) {
		return ForumModel.TYPE_POST.equals(type) || ContentModel.TYPE_CONTENT.equals(type)
				|| entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2)
				|| entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM);
	}

	private void registerActivity(NodeRef actionedUponNodeRef, QName type, ActivityEvent activityEvent) {

		NodeRef entityNodeRef = entityActivityService.getEntityNodeRef(actionedUponNodeRef, type);

		if (entityNodeRef != null) {
			try {
				policyBehaviourFilter.disableBehaviour();

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
						entityActivityService.postEntityActivity(actionedUponNodeRef, activityEvent);
					}
				}
			} finally {
				policyBehaviourFilter.enableBehaviour();
			}

		}
	}

}
