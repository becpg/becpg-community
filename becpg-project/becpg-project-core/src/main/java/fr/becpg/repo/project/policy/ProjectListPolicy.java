/*
 *
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.activity.policy.EntityActivityPolicy;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskManualDate;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * The Class SubmitTaskPolicy.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class ProjectListPolicy extends ProjectPolicy
		implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

	private static final Log logger = LogFactory.getLog(ProjectListPolicy.class);

	private AssociationService associationService;

	private ProjectActivityService projectActivityService;

	/**
	 * <p>Setter for the field <code>projectActivityService</code>.</p>
	 *
	 * @param projectActivityService a {@link fr.becpg.repo.project.ProjectActivityService} object.
	 */
	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init SubmitTaskPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_DELIVERABLE_LIST,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_SCORE_LIST,
				new JavaBehaviour(this, "onUpdateProperties"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.ASPECT_BUDGET,
				new JavaBehaviour(this, "onUpdateProperties"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_LOG_TIME_LIST,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_TL_RESOURCES, new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_TL_PREV_TASKS, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_TL_PREV_TASKS, new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_TL_RESOURCE_COST, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.ASPECT_BUDGET,
				new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.ASPECT_BUDGET,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_TL_RESOURCE_COST, new JavaBehaviour(this, "onDeleteAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_SUB_PROJECT, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				ProjectModel.ASSOC_SUB_PROJECT, new JavaBehaviour(this, "onDeleteAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_DELIVERABLE_LIST,
				ProjectModel.ASSOC_DL_TASK, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ProjectModel.TYPE_LOG_TIME_LIST,
				ProjectModel.ASSOC_LTL_TASK, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ProjectModel.TYPE_LOG_TIME_LIST,
				ProjectModel.ASSOC_LTL_TASK, new JavaBehaviour(this, "onDeleteAssociation"));

		// action duplicate use createNode API
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ProjectModel.TYPE_DELIVERABLE_LIST,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.TYPE_TASK_LIST,
				new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.TYPE_BUDGET_LIST,
				new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.TYPE_EXPENSE_LIST,
				new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ProjectModel.ASPECT_BUDGET,
				new JavaBehaviour(this, "beforeDeleteNode"));
	}

	/** {@inheritDoc} */
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

		if ((beforeState != null) && (afterState != null) && !beforeState.equals(afterState)) {

			
			projectActivityService.postTaskStateChangeActivity(nodeRef, null, beforeState, afterState, false);

			queueNode(EntityActivityPolicy.KEY_QUEUE_UPDATED_STATUS, nodeRef);

			formulateProject = true;

			if(afterState.equals(TaskState.Refused.toString())) {
				projectService.refusedTask(nodeRef);
			}
			
			
			if ((beforeState.equals(TaskState.Completed.toString()) || beforeState.equals(TaskState.Refused.toString()))
					&& afterState.equals(TaskState.InProgress.toString())) {
				// re-open task
				logger.debug("re-open task: " + nodeRef);
				projectService.reopenTask(nodeRef);

			}

		}

		if (isPropChanged(before, after, ProjectModel.PROP_TL_DURATION) || isPropChanged(before, after, ProjectModel.PROP_TL_START)
				|| isPropChanged(before, after, ProjectModel.PROP_TL_WORK) || isPropChanged(before, after, ProjectModel.PROP_TL_FIXED_COST)
				|| isPropChanged(before, after, BeCPGModel.PROP_PARENT_LEVEL) || isPropChanged(before, after, ProjectModel.PROP_TL_MANUAL_DATE)) {

			logger.debug("update task list start, duration or end: " + nodeRef);
			formulateProject = true;
		} else {
			String manualDate = (String) after.get(ProjectModel.PROP_TL_MANUAL_DATE);
			if ((TaskManualDate.End.toString().equals(manualDate) && isPropChanged(before, after, ProjectModel.PROP_TL_END))) {
				formulateProject = true;
			}

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


			projectActivityService.postDeliverableStateChangeActivity(nodeRef, beforeState, afterState, false);

			if ((beforeState != null) && (afterState != null) && beforeState.equals(DeliverableState.Completed.toString())
					&& afterState.equals(DeliverableState.InProgress.toString())) {

				// re-open deliverable and disable policy to avoid every dl
				// are
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

		if (isPropChanged(before, after, ProjectModel.PROP_BUDGET_EXPENSE) || isPropChanged(before, after, ProjectModel.PROP_BUDGET_INVOICE)
				|| isPropChanged(before, after, ProjectModel.PROP_BL_BUDGEDTED_EXPENSE)
				|| isPropChanged(before, after, ProjectModel.PROP_BL_BUDGEDTED_INVOICE)
				|| isPropChanged(before, after, BeCPGModel.PROP_PARENT_LEVEL)) {

			if (logger.isDebugEnabled()) {
				logger.debug("UPDATE Budget Aspect (Invoice or Expense)  : " + nodeRef);
			}
			queueListItem(nodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		if (assocRef.getTypeQName().equals(ProjectModel.ASSOC_SUB_PROJECT)) {
			NodeRef projectNodeRef = entityListDAO.getEntity(assocRef.getSourceRef());
			if ((projectNodeRef != null) && !isNotLocked(projectNodeRef)
					&& !nodeService.hasAspect(projectNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
				nodeService.removeAssociation(assocRef.getTargetRef(), projectNodeRef, ProjectModel.ASSOC_PARENT_PROJECT);
			}
		}

		if (assocRef.getTypeQName().equals(ProjectModel.ASSOC_TL_RESOURCES)) {
			setPermission(assocRef, false);
		}
		queueListItem(assocRef.getSourceRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		if (assocRef.getTypeQName().equals(ProjectModel.ASSOC_SUB_PROJECT)) {
			NodeRef projectNodeRef = entityListDAO.getEntity(assocRef.getSourceRef());
			associationService.update(assocRef.getTargetRef(), ProjectModel.ASSOC_PARENT_PROJECT, projectNodeRef);
		}

		if (assocRef.getTypeQName().equals(ProjectModel.ASSOC_TL_RESOURCES)) {
			setPermission(assocRef, true);
		}
		queueListItem(assocRef.getSourceRef());
	}

	private void setPermission(AssociationRef assocRef, boolean allow) {
		NodeRef taskListNodeRef = assocRef.getSourceRef();
		NodeRef resourceNodeRef = assocRef.getTargetRef();
		NodeRef projectNodeRef = entityListDAO.getEntity(taskListNodeRef);
		if (logger.isDebugEnabled()) {
			logger.debug("Update project permissions after bcpg:tlResources update");
		}
		projectService.updateProjectPermission(projectNodeRef, taskListNodeRef, resourceNodeRef, allow);

	}

	/** {@inheritDoc} */
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

				String workflowInstanceId = (String) nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
				if ((workflowInstanceId != null) && !workflowInstanceId.isEmpty()) {
					queueNode(KEY_DELETED_TASK_LIST_ITEM, new NodeRef("becpg", "wf", workflowInstanceId));
				}

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

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childRef) {

		// action duplicate use createNode API
		nodeService.removeProperty(childRef.getChildRef(), ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
		nodeService.removeProperty(childRef.getChildRef(), ProjectModel.PROP_TL_WORKFLOW_TASK_INSTANCE);
	}
}
