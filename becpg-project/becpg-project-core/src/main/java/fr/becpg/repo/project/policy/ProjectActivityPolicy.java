package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
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
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.projectList.ActivityEvent;

public class ProjectActivityPolicy extends ProjectPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnCreateNodePolicy,
		ContentServicePolicies.OnContentUpdatePolicy {

	private static final Log logger = LogFactory.getLog(ProjectListPolicy.class);

	private static Integer MAX_DEPTH_LEVEL = 6;

	protected static final String KEY_QUEUE_UPDATED = "ProjectActivity_updated";
	protected static final String KEY_QUEUE_DELETED = "ProjectActivity_deleted";
	protected static final String KEY_QUEUE_CREATED = "ProjectActivity_created";

	private static final Set<QName> IGNORE_PARENT_ASSOC_TYPES = new HashSet<QName>(7);
	static {
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_MEMBER);
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_IN_ZONE);
	}

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
		if (isInProject(actionedUponNodeRef, null)) {
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

	private boolean isInProject(NodeRef nodeRef, Set<NodeRef> visitedNodeRefs) {
		if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) && !nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {

			if (ProjectModel.TYPE_PROJECT.equals(nodeService.getType(nodeRef))) {
				return true;
			}

			// Create the visited nodes set if it has not already been created
			if (visitedNodeRefs == null) {
				visitedNodeRefs = new HashSet<NodeRef>();
			}

			// This check prevents stack over flow when we have a cyclic node
			// graph
			if (visitedNodeRefs.contains(nodeRef) == false && visitedNodeRefs.size() < MAX_DEPTH_LEVEL) {
				visitedNodeRefs.add(nodeRef);

				List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
				for (ChildAssociationRef parent : parents) {
					// We are not interested in following potentially massive
					// person group membership trees!
					if (IGNORE_PARENT_ASSOC_TYPES.contains(parent.getTypeQName())) {
						continue;
					}

					if (isInProject(parent.getParentRef(), visitedNodeRefs)) {
						return true;
					}

				}

			}
		}
		return false;
	}

}
