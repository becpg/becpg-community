/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * The Class SubmitTaskPolicy.
 * 
 * @author querephi
 */
public class ProjectListPolicy extends ProjectPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy, CopyServicePolicies.OnCopyNodePolicy, NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.BeforeDeleteNodePolicy,
		NodeServicePolicies.OnDeleteNodePolicy {

	private static final Log logger = LogFactory.getLog(ProjectListPolicy.class);


	private NodeArchiveService nodeArchiveService;

	public void setNodeArchiveService(NodeArchiveService nodeArchiveService) {
		this.nodeArchiveService = nodeArchiveService;
	}

	
	/**
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init SubmitTaskPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_DELIVERABLE_LIST, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_SCORE_LIST, new JavaBehaviour(this, "onUpdateProperties"));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.ASPECT_BUDGET, new JavaBehaviour(this, "onUpdateProperties"));		
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_LOG_TIME_LIST, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this,
				"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this,
				"onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_PREV_TASKS, new JavaBehaviour(this,
				"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_PREV_TASKS, new JavaBehaviour(this,
				"onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCE_COST, new JavaBehaviour(this,
				"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.ASPECT_BUDGET, new JavaBehaviour(this,
				"onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.ASPECT_BUDGET, new JavaBehaviour(this,
				"onCreateAssociation"));		
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST, ProjectModel.ASSOC_TL_RESOURCE_COST, new JavaBehaviour(this,
				"onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_DELIVERABLE_LIST, ProjectModel.ASSOC_DL_TASK, new JavaBehaviour(this,
				"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_LOG_TIME_LIST, ProjectModel.ASSOC_LTL_TASK, new JavaBehaviour(this,
				"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_LOG_TIME_LIST, ProjectModel.ASSOC_LTL_TASK, new JavaBehaviour(this,
				"onDeleteAssociation"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ProjectModel.TYPE_DELIVERABLE_LIST, new JavaBehaviour(this, "getCopyCallback"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "getCopyCallback"));
		
		// action duplicate use createNode API
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ProjectModel.TYPE_DELIVERABLE_LIST, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.TYPE_BUDGET_LIST, new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ProjectModel.TYPE_BUDGET_LIST, new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.TYPE_EXPENSE_LIST, new JavaBehaviour(this, "beforeDeleteNode"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.ASPECT_BUDGET, new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ProjectModel.ASPECT_BUDGET, new JavaBehaviour(this, "onDeleteNode"));
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (nodeService.getType(nodeRef).equals(ProjectModel.TYPE_TASK_LIST)) {
			onUpdatePropertiesTaskList(nodeRef, before, after);
		} else if (nodeService.getType(nodeRef).equals(ProjectModel.TYPE_DELIVERABLE_LIST)) {
			onUpdatePropertiesDeliverableList(nodeRef, before, after);
		} else if (nodeService.getType(nodeRef).equals(ProjectModel.TYPE_SCORE_LIST)) {
			onUpdatePropertiesScoreList(nodeRef, before, after);
		} else if (nodeService.getType(nodeRef).equals(ProjectModel.TYPE_LOG_TIME_LIST)) {
			onUpdatePropertiesLogTimeList(nodeRef, before, after);
		} else if (nodeService.hasAspect(nodeRef, ProjectModel.ASPECT_BUDGET)) {
			onUpdatePropertiesBudgetAspect(nodeRef, before, after);
		}
		
	}

	private void onUpdatePropertiesTaskList(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		boolean formulateProject = false;
		String beforeState = (String) before.get(ProjectModel.PROP_TL_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_TL_STATE);

		if (beforeState != null && afterState != null && !beforeState.equals(afterState)) {

			projectActivityService.postTaskStateChangeActivity(nodeRef, beforeState, afterState);
			formulateProject = true;

			if (beforeState.equals(TaskState.Completed.toString()) && afterState.equals(TaskState.InProgress.toString())) {
				// re-open task
				logger.debug("re-open task: " + nodeRef);
				projectService.reopenTask(nodeRef);

			}
		}

		if (isPropChanged(before, after, ProjectModel.PROP_TL_DURATION) || isPropChanged(before, after, ProjectModel.PROP_TL_START) || isPropChanged(before, after, ProjectModel.PROP_TL_WORK)
				|| isPropChanged(before, after, ProjectModel.PROP_TL_FIXED_COST)) {

			logger.debug("update task list start, duration or end: " + nodeRef);
			formulateProject = true;
		}

		if (formulateProject) {
			queueListItem(nodeRef);
		}
	}

	private void onUpdatePropertiesDeliverableList(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		boolean formulateProject = false;

		if (isPropChanged(before, after, ProjectModel.PROP_DL_STATE)) {

			String beforeState = (String) before.get(ProjectModel.PROP_DL_STATE);
			String afterState = (String) after.get(ProjectModel.PROP_DL_STATE);
			projectActivityService.postDeliverableStateChangeActivity(nodeRef, beforeState, afterState);

			if (beforeState != null && afterState != null && beforeState.equals(DeliverableState.Completed.toString()) && afterState.equals(DeliverableState.InProgress.toString())) {

				// re-open deliverable and disable policy to avoid every dl are
				// re-opened
				logger.debug("re-open deliverable: " + nodeRef);
				try {
					policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
					policyBehaviourFilter.disableBehaviour(ProjectModel.ASPECT_BUDGET);
					projectService.openDeliverable(nodeRef);
				} finally {
					policyBehaviourFilter.enableBehaviour(ProjectModel.ASPECT_BUDGET);
					policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
				}
			}

			formulateProject = true;
		}

		if (isPropChanged(before, after, ProjectModel.PROP_DL_DESCRIPTION)) {
			formulateProject = true;
		}

		if (formulateProject) {
			queueListItem(nodeRef);
		}
	}

	private void onUpdatePropertiesScoreList(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (isPropChanged(before, after, ProjectModel.PROP_SL_SCORE) || isPropChanged(before, after, ProjectModel.PROP_SL_WEIGHT)) {

			logger.debug("update score list : " + nodeRef);
			queueListItem(nodeRef);
		}
	}

	private void onUpdatePropertiesLogTimeList(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (isPropChanged(before, after, ProjectModel.PROP_LTL_TIME)) {

			logger.debug("update log time list : " + nodeRef);
			queueListItem(nodeRef);
		}
	}



	private void onUpdatePropertiesBudgetAspect(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (isPropChanged(before, after, ProjectModel.PROP_BUDGET_EXPENSE) || isPropChanged(before, after, ProjectModel.PROP_BUDGET_INVOICE) ) {

			if (logger.isDebugEnabled()) {
				logger.debug("UPDATE Budget Aspect (Invoice or Expense)  : " + nodeRef);
			}
			queueListItem(nodeRef);
		}
	}
	

	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		if (assocRef.getTypeQName().equals(ProjectModel.ASSOC_TL_RESOURCES)) {

			setPermission(assocRef, false);
			
		}
		queueListItem(assocRef.getSourceRef());
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		if (assocRef.getTypeQName().equals(ProjectModel.ASSOC_TL_RESOURCES)) {
			setPermission(assocRef, true);
		}
		queueListItem(assocRef.getSourceRef());
	}

	private void setPermission(AssociationRef assocRef, boolean allow) {
		NodeRef taskListNodeRef = assocRef.getSourceRef();
		NodeRef resourceNodeRef = assocRef.getTargetRef();
		NodeRef projectNodeRef = entityListDAO.getEntity(taskListNodeRef);
		projectService.updateProjectPermission(projectNodeRef, taskListNodeRef, resourceNodeRef, allow);

	}

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new ProjectListCopyBehaviourCallback();
	}

	private class ProjectListCopyBehaviourCallback extends DefaultCopyBehaviourCallback {

		private ProjectListCopyBehaviourCallback() {
		}

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			return true;
		}

		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties) {

			return resetProperties(classQName, properties);
		}
	}

	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isArchived) {
		if (isArchived) {
			NodeRef nodeRef = nodeArchiveService.getArchivedNode(childRef.getChildRef());
			QName projectListType = nodeService.getType(nodeRef);
			logger.debug("ProjectList policy delete type: " + projectListType + " nodeRef: " + nodeRef);

			// we need to do it at the end
			if (ProjectModel.TYPE_TASK_LIST.equals(projectListType)) {
				queueNode(KEY_DELETED_TASK_LIST_ITEM, nodeRef);
			}
		}
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		// we need to queue item before delete in order to have WUsed

		QName projectListType = nodeService.getType(nodeRef);
		if (ProjectModel.TYPE_TASK_LIST.equals(projectListType)) {

		   try {
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_LOG_TIME_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_PROJECT);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_SCORE_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_BUDGET_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.ASPECT_BUDGET);

				projectService.deleteTask(nodeRef);
			} finally {
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_LOG_TIME_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_PROJECT);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_SCORE_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_BUDGET_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.ASPECT_BUDGET);
			}

			queueListItem(nodeRef);
		}

	}

	@Override
	public void onCreateNode(ChildAssociationRef childRef) {

		// action duplicate use createNode API
		nodeService.setProperties(childRef.getChildRef(), resetProperties(nodeService.getType(childRef.getChildRef()), nodeService.getProperties(childRef.getChildRef())));
	}

	private Map<QName, Serializable> resetProperties(QName classQName, Map<QName, Serializable> properties) {

		if (ProjectModel.TYPE_TASK_LIST.equals(classQName)) {
			properties.remove(ProjectModel.PROP_TL_START);
			properties.remove(ProjectModel.PROP_TL_END);
			properties.remove(ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
			properties.remove(ProjectModel.PROP_COMPLETION_PERCENT);
			if (properties.containsKey(ProjectModel.PROP_TL_STATE) && !TaskState.OnHold.toString().equals(properties.get(ProjectModel.PROP_TL_STATE))
					&& !TaskState.Cancelled.toString().equals(properties.get(ProjectModel.PROP_TL_STATE))) {
				properties.put(ProjectModel.PROP_TL_STATE, TaskState.Planned);
			}
		} else if (ProjectModel.TYPE_DELIVERABLE_LIST.equals(classQName)) {
			if (properties.containsKey(ProjectModel.PROP_DL_STATE)) {
				properties.put(ProjectModel.PROP_DL_STATE, DeliverableState.Planned);
			}
		}

		return properties;
	}
}
