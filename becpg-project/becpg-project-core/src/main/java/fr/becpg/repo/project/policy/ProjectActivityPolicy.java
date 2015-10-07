package fr.becpg.repo.project.policy;

import java.io.Serializable;
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

import fr.becpg.repo.project.data.projectList.ActivityEvent;

public class ProjectActivityPolicy extends ProjectPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnCreateNodePolicy,
		ContentServicePolicies.OnContentUpdatePolicy {

	private static final Log logger = LogFactory.getLog(ProjectListPolicy.class);

	protected static final String KEY_QUEUE_UPDATED = "ProjectActivity_updated";
	protected static final String KEY_QUEUE_DELETED = "ProjectActivity_deleted";
	protected static final String KEY_QUEUE_CREATED = "ProjectActivity_created";

	

	/**
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init ProjectActivityPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ForumModel.TYPE_POST, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ForumModel.TYPE_POST, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ForumModel.TYPE_POST, new JavaBehaviour(this, "beforeDeleteNode"));

		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onContentUpdate"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "beforeDeleteNode"));

	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if ((before != null && before.equals(after)) || before == after) {
			return;
		}
		QName type = nodeService.getType(nodeRef);
		if (ForumModel.TYPE_POST.equals(type) || ContentModel.TYPE_CONTENT.equals(type)) {
			queueNode(KEY_QUEUE_UPDATED, nodeRef);
		}
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		QName type = nodeService.getType(nodeRef);
		if (ForumModel.TYPE_POST.equals(type) || ContentModel.TYPE_CONTENT.equals(type)) {
			queueNode(KEY_QUEUE_UPDATED, nodeRef);
		}

	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		QName type = nodeService.getType(childAssocRef.getChildRef());
		if (ForumModel.TYPE_POST.equals(type) || ContentModel.TYPE_CONTENT.equals(type)) {
			queueNode(KEY_QUEUE_CREATED, childAssocRef.getChildRef());
		}
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		if (ForumModel.TYPE_POST.equals(type) || ContentModel.TYPE_CONTENT.equals(type)) {
			registerActivity(nodeRef, ActivityEvent.Delete);
			queueNode(KEY_QUEUE_DELETED, nodeRef);
		}
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			switch (key) {
			case KEY_QUEUE_UPDATED:
				if (!containsNodeInQueue(KEY_QUEUE_CREATED, nodeRef) && !containsNodeInQueue(KEY_QUEUE_DELETED, nodeRef)) {
					registerActivity(nodeRef, ActivityEvent.Update);
				}
				break;
			case KEY_QUEUE_CREATED:
				registerActivity(nodeRef, ActivityEvent.Create);
				break;
			default:
				break;
			}
		}

	}

	private void registerActivity(NodeRef actionedUponNodeRef, ActivityEvent activityEvent) {
		if (projectActivityService.isInProject(actionedUponNodeRef)) {
			try {
			policyBehaviourFilter.disableBehaviour(ContentModel.TYPE_CONTENT);
				QName type = nodeService.getType(actionedUponNodeRef);
				if (activityEvent != null) {
					if (ForumModel.TYPE_POST.equals(type)) {
						logger.debug("Action upon comment, post activity");
						projectActivityService.postCommentActivity(actionedUponNodeRef, activityEvent);
					} else if (ContentModel.TYPE_CONTENT.equals(type)) {
						logger.debug("Action upon content, post activity");
						projectActivityService.postContentActivity(actionedUponNodeRef, activityEvent);
					}
				}
			} finally {
				policyBehaviourFilter.enableBehaviour(ContentModel.TYPE_CONTENT);
			}

		}
	}

	

}
