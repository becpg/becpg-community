package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.ProjectException;
import fr.becpg.repo.project.ProjectVisitor;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;

public class TaskStateVisitor implements ProjectVisitor {

	private static final int COMPLETED = 100;
	private static final String WORKFLOW_DESCRIPTION = "%s : ";
	private static final String DESCRIPTION_SEPARATOR = ", ";

	private static Log logger = LogFactory.getLog(TaskStateVisitor.class);

	private WorkflowService workflowService;
	private NodeService nodeService;

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public ProjectData visit(ProjectData projectData) throws ProjectException {

		visitTask(projectData, null);
		return projectData;
	}

	private void visitTask(ProjectData projectData, NodeRef taskListNodeRef) {

		logger.debug("visitTask taskListNodeRef: " + taskListNodeRef);

		// add next tasks
		List<TaskListDataItem> nextTasks = ProjectHelper.getNextTasks(projectData, taskListNodeRef);
		logger.debug("nextTasks size: " + nextTasks.size());
		if (nextTasks.size() > 0) {
			for (TaskListDataItem nextTask : nextTasks) {

				// should we continue ?
				// - we are on first task and Project is in progress
				// - previous task are done
				if ((ProjectState.InProgress.equals(projectData.getProjectState()) && nextTask.getPrevTasks().isEmpty())
						|| ProjectHelper.areTasksDone(projectData, nextTask.getPrevTasks())) {

					// start task
					if (TaskState.Planned.equals(nextTask.getState())) {
						ProjectHelper.setTaskStartDate(nextTask, new Date(), false);
						nextTask.setState(TaskState.InProgress);

						// deliverable list
						String workflowDescription = String.format(WORKFLOW_DESCRIPTION, projectData.getName());
						boolean isFirst = true;
						List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData,
								nextTask.getNodeRef());

						for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

							// set Planned dl InProgress
							if (DeliverableState.Planned.equals(nextDeliverable.getState())) {
								nextDeliverable.setState(DeliverableState.InProgress);
							}

							if (DeliverableState.InProgress.equals(nextDeliverable.getState())) {
								if (isFirst) {
									isFirst = false;
								} else {
									workflowDescription += DESCRIPTION_SEPARATOR;
								}
								workflowDescription += nextDeliverable.getDescription();
							}
						}

						if (nextTask.getResources() != null) {
							for (NodeRef resource : nextTask.getResources()) {
								// start workflow
								startWorkflow(projectData, nextTask, workflowDescription, resource);
							}
						}
					} else if (TaskState.InProgress.equals(nextTask.getState())) {

						Integer taskCompletionPercent = 0;
						int finishedDL = 0;
						List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData,
								nextTask.getNodeRef());

						for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

							// Completed or Closed
							if (DeliverableState.Completed.equals(nextDeliverable.getState())
									|| DeliverableState.Closed.equals(nextDeliverable.getState())) {
								taskCompletionPercent += nextDeliverable.getCompletionPercent();
								finishedDL++;
							}
						}

						if (nextDeliverables.size() == finishedDL) {
							logger.debug("set completion percent to 100%");
							nextTask.setCompletionPercent(COMPLETED);
							nextTask.setState(TaskState.Completed);							
						} else {							
							logger.debug("set completion percent to value " + taskCompletionPercent + " - nodref: "
									+ nextTask.getNodeRef());
							nextTask.setCompletionPercent(taskCompletionPercent);							
						}
					} else if (TaskState.Completed.equals(nextTask.getState())) {

						List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData,
								nextTask.getNodeRef());

						for (DeliverableListDataItem nextDeliverable : nextDeliverables) {
							nextDeliverable.setState(DeliverableState.Completed);
						}

						nextTask.setCompletionPercent(COMPLETED);
					}

					visitTask(projectData, nextTask.getNodeRef());
				}
				projectData.setCompletionPercent(ProjectHelper.geProjectCompletionPercent(projectData));
			}
		} else {
			projectData.setCompletionDate(new Date());
			projectData.setCompletionPercent(COMPLETED);
		}

	}

	private void startWorkflow(ProjectData projectData, TaskListDataItem taskListDataItem, String workflowDescription,
			NodeRef assignee) {
		Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
		Calendar cal = Calendar.getInstance();

		if (taskListDataItem.getDuration() != null) {
			cal.add(Calendar.DAY_OF_YEAR, taskListDataItem.getDuration());
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, cal.getTime());
		}
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
		workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
		workflowProps.put(ProjectModel.ASSOC_WORKFLOW_TASK, taskListDataItem.getNodeRef());

		NodeRef wfPackage = workflowService.createPackage(null);
		nodeService.addChild(wfPackage, projectData.getNodeRef(), WorkflowModel.ASSOC_PACKAGE_CONTAINS,
				ContentModel.ASSOC_CHILDREN);
		if (projectData.getEntity() != null) {
			nodeService.addChild(wfPackage, projectData.getEntity(), WorkflowModel.ASSOC_PACKAGE_CONTAINS,
					ContentModel.ASSOC_CHILDREN);
		}
		workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

		String workflowDefId = getWorkflowDefId(taskListDataItem.getWorkflowName());
		logger.debug("workflowDefId: " + workflowDefId);
		if (workflowDefId != null) {

			WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, workflowProps);
			logger.debug("New worflow started. Id: " + wfPath.getId() + " - workflowDescription: "
					+ workflowDescription);
			String workflowId = wfPath.getInstance().getId();
			taskListDataItem.setWorkflowInstance(workflowId);

			// get the workflow tasks
			WorkflowTask startTask = workflowService.getStartTask(workflowId);

			// end task
			try {
				workflowService.endTask(startTask.getId(), null);
			} catch (RuntimeException err) {
				if (logger.isDebugEnabled())
					logger.debug("Failed - caught error during project adhoc workflow transition: " + err.getMessage());
				throw err;
			}
		}
	}

	private String getWorkflowDefId(String workflowName) {
		if (workflowName != null && !workflowName.isEmpty()) {
			WorkflowDefinition def = workflowService.getDefinitionByName(workflowName);
			if (def != null) {
				return def.getId();
			}
		}

		logger.error("Unknown workflow name: " + workflowName);
		return null;
	}
}
