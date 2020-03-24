/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.formulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DeliverableUrl;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

@Deprecated
public class TaskStateFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static final int COMPLETED = 100;

	private static final Log logger = LogFactory.getLog(TaskStateFormulationHandler.class);

	private ProjectWorkflowService projectWorkflowService;

	private ProjectService projectService;

	private ProjectActivityService projectActivityService;

	private NodeService nodeService;

	AlfrescoRepository<ProjectData> alfrescoRepository;

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setProjectWorkflowService(ProjectWorkflowService projectWorkflowService) {
		this.projectWorkflowService = projectWorkflowService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		if (isOnHold(projectData)) {
			for (TaskListDataItem task : projectData.getTaskList()) {
				if (projectWorkflowService.isWorkflowActive(task)) {
					logger.debug("Cancel workflow of project " + projectData.getName() + " for task " + task.getTaskName());
					projectWorkflowService.cancelWorkflow(task);
				}
				task.setIsExcludeFromSearch(true);
			}
		}
		// we don't want tasks of project template start
		else if (!projectData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {

			// start project if startdate is before now and startdate != created
			// otherwise ProjectMgr will start it manually
			if ((ProjectState.Planned.equals(projectData.getProjectState())) && (projectData.getStartDate() != null)
					&& projectData.getStartDate().before(new Date())) {
				projectData.setProjectState(ProjectState.InProgress);
			}

			// even if project is not in Progress, we visit it because a task
			// can start the project (manual task or task that has startdate <
			// NOW)
			if (visitTask(projectData)) {

				if (projectData.getReformulateCount() == null) {
					projectData.setReformulateCount(1);
				} else {
					if (projectData.getReformulateCount() < 3) {
						projectData.setReformulateCount(projectData.getReformulateCount() + 1);
					}
				}

			}

			visitProject(projectData);

		}

		return true;
	}

	private void visitProject(ProjectData projectData) {
		boolean allTaskPlanned = true;
		boolean allTaskDone = true;

		int totalWork = 0;
		int workDone = 0;

		List<NodeRef> currLegends = new ArrayList<>();
		List<NodeRef> currTasks = new ArrayList<>();

		for (TaskListDataItem task : projectData.getTaskList()) {
			TaskState state = task.getTaskState();

			if (!TaskState.Cancelled.equals(state)) {
				if (!task.isPlanned()) {
					allTaskPlanned = false;
				}

				if (!(TaskState.Completed.equals(state))) {
					allTaskDone = false;
				}

				Integer duration = task.getDuration() != null ? task.getDuration()
						: ProjectHelper.calculateTaskDuration(task.getStart(), task.getEnd());
				if (duration != null) {
					totalWork += duration;
					if (TaskState.Completed.equals(state)) {
						workDone += duration;
					} else if ((task.getSubProject() != null) && (task.getCompletionPercent() != null)) {
						workDone += ((duration * task.getCompletionPercent()) / 100);
					}
				}

				if (TaskState.InProgress.equals(state)) {
					if (!currLegends.contains(task.getTaskLegend())) {
						currLegends.add(task.getTaskLegend());
					}
					currTasks.add(task.getNodeRef());
				}

			}

		}

		if (!allTaskPlanned && ProjectState.Planned.equals(projectData.getProjectState())) {
			projectData.setProjectState(ProjectState.InProgress);
		} else if (allTaskPlanned && ProjectState.InProgress.equals(projectData.getProjectState())) {
			projectData.setProjectState(ProjectState.Planned);
		}

		if (allTaskDone) {
			projectData.setCompletionDate(ProjectHelper.getLastEndDate(projectData));
			projectData.setCompletionPercent(COMPLETED);
			projectData.setProjectState(ProjectState.Completed);
		} else {
			projectData.setCompletionPercent(totalWork != 0 ? (100 * workDone) / totalWork : 0);
		}

		projectData.setCurrTasks(currTasks);
		projectData.setLegends(currLegends);

	}

	private boolean isOnHold(ProjectData projectData) {
		return projectData.getAspects().contains(ContentModel.ASPECT_CHECKED_OUT)
				|| projectData.getAspects().contains(ContentModel.ASPECT_WORKING_COPY)
				|| projectData.getAspects().contains(BeCPGModel.ASPECT_COMPOSITE_VERSION)
				|| ProjectState.Cancelled.equals(projectData.getProjectState()) || ProjectState.OnHold.equals(projectData.getProjectState());
	}

	private boolean visitTask(ProjectData projectData) {
		Set<NodeRef> visited = new HashSet<>();

		boolean reformulate = false;
		for (TaskListDataItem taskListDataItem : projectData.getTaskList()) {
			reformulate = visitTask(projectData, taskListDataItem, visited) || reformulate;
		}

		return reformulate;
	}

	private boolean visitTask(ProjectData projectData, TaskListDataItem taskListDataItem, Set<NodeRef> visited) {

		NodeRef taskListNodeRef = taskListDataItem != null ? taskListDataItem.getNodeRef() : null;

		if (visited.contains(taskListNodeRef)) {
			return false;
		} else {
			visited.add(taskListNodeRef);
		}

		if (taskListDataItem != null) {
			taskListDataItem.setIsExcludeFromSearch(false);
		}

		TaskState currentTaskState = taskListDataItem.getTaskState();

		// cancel active workflow if task is not anymore InProgress
		logger.debug("Visit task : " + taskListDataItem.getTaskName() + " - state - " + taskListDataItem.getTaskState());

		if (!TaskState.InProgress.equals(taskListDataItem.getTaskState()) && projectWorkflowService.isWorkflowActive(taskListDataItem)) {
			projectWorkflowService.cancelWorkflow(taskListDataItem);
		}

		List<DeliverableListDataItem> deliverables = ProjectHelper.getDeliverables(projectData, taskListDataItem.getNodeRef());

		boolean reformulate = false;

		if (taskListDataItem.isPlanned()) {

			List<TaskListDataItem> prevTasks = ProjectHelper.getPrevTasks(projectData, taskListDataItem);

			// no previous task
			if (prevTasks.isEmpty()) {
				if ((taskListDataItem.getStart() != null) && taskListDataItem.getStart().before(new Date())) {
					List<TaskListDataItem> tasks = ProjectHelper.getChildrenTasks(projectData, taskListDataItem);
					if (!tasks.isEmpty()) {

						boolean hasTaskInProgress = false;
						boolean allTasksPlanned = true;
						boolean allTasksCancelled = true;

						int completionPerc = 0;

						for (TaskListDataItem c : tasks) {
							completionPerc += (c.getCompletionPercent() != null ? c.getCompletionPercent() : 0);
							if (TaskState.InProgress.equals(c.getTaskState()) || TaskState.OnHold.equals(c.getTaskState()) || c.isRefused()) {
								hasTaskInProgress = true;
								allTasksCancelled = false;
								allTasksPlanned = false;
							} else if (TaskState.Completed.equals(c.getTaskState())) {
								allTasksPlanned = false;
								allTasksCancelled = false;
							} else if (c.isPlanned()) {
								allTasksCancelled = false;
							}
						}
						if (hasTaskInProgress) {
							ProjectHelper.setTaskState(taskListDataItem, TaskState.InProgress, projectActivityService);
						} else if (allTasksPlanned && !allTasksCancelled) {
							ProjectHelper.setTaskState(taskListDataItem, TaskState.Planned, projectActivityService);
						} else if (allTasksCancelled) {
							ProjectHelper.setTaskState(taskListDataItem, TaskState.Cancelled, projectActivityService);
						} else {
							ProjectHelper.setTaskState(taskListDataItem, TaskState.Completed, projectActivityService);
						}
						taskListDataItem.setCompletionPercent(completionPerc / tasks.size());

					} else {

						logger.debug("Start first task.");

						taskListDataItem.setTaskState(TaskState.InProgress);
					}

				}
			} else {

				// VisitTask first
				for (TaskListDataItem prevTask : prevTasks) {
					logger.debug(" visit prev task :" + prevTask.getTaskName());

					reformulate = visitTask(projectData, prevTask, visited) || reformulate;
				}

				// previous task are done
				if (ProjectHelper.areTasksDone(projectData, taskListDataItem.getPrevTasks())) {
					if (taskListDataItem.getManualDate() == null) {
						logger.debug("Start task since previous are done");
						taskListDataItem.setTaskState(TaskState.InProgress);
					}
					// manual date -> we wait the date
					else if ((taskListDataItem.getStart() != null) && taskListDataItem.getStart().before(new Date())) {
						logger.debug("Start task since we are after planned startDate. start planned: " + taskListDataItem.getStart());
						taskListDataItem.setTaskState(TaskState.InProgress);
					}
				}
			}

		} else if (taskListDataItem.isRefused() && (taskListDataItem.getRefusedTask() != null)) {
			boolean shouldRefused = true;
			logger.debug("Enter refused task");
			// Check if all brothers are closed
			for (TaskListDataItem brotherTask : ProjectHelper.getBrethrenTask(projectData, taskListDataItem)) {
				if (!taskListDataItem.getRefusedTask().equals(brotherTask) && TaskState.InProgress.equals(brotherTask.getTaskState())) {
					shouldRefused = false;
					logger.debug("Will not refused task as there is still some brother Open");
					break;
				}
			}

			if (shouldRefused) {
				logger.debug("Reopen path : " + taskListDataItem.getRefusedTask().getTaskName());

				ProjectHelper.reOpenRefusePath(projectData, taskListDataItem, taskListDataItem.getRefusedTask(),
						taskListDataItem.getRefusedTasksToReopen(), projectActivityService);

				// Revisit tasks
				reformulate = true;
			}

		}

		reformulate = visitDeliverables(projectData, taskListDataItem, deliverables) || reformulate;

		if (TaskState.InProgress.equals(taskListDataItem.getTaskState())) {

			if (!TaskState.InProgress.equals(currentTaskState)) {
				projectActivityService.postTaskStateChangeActivity(taskListDataItem.getNodeRef(), null, currentTaskState.toString(),
						TaskState.InProgress.toString(), true);
			}

			if ((taskListDataItem.getResources() != null) && !taskListDataItem.getResources().isEmpty()) {

				List<NodeRef> resources = new ArrayList<>();

				for (NodeRef resource : projectService.extractResources(projectData.getNodeRef(), taskListDataItem.getResources())) {
					NodeRef reassignResource = projectService.getReassignedResource(resource);
					NodeRef toAdd = resource;
					// check delegation
					if (reassignResource != null) {

						Date delegationStart = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_START);
						Date delegationEnd = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_END);

						if ((delegationStart != null)
								&& (taskListDataItem.getStart().after(delegationStart) || taskListDataItem.getStart().equals(delegationStart))) {

							if ((delegationEnd == null)
									|| (taskListDataItem.getStart().before(delegationEnd) || taskListDataItem.getStart().equals(delegationEnd))) {

								// reassign new tasks
								toAdd = reassignResource;
							}
						}

						else {
							if ((boolean) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_REASSIGN_TASK)) {
								// reassign current tasks
								toAdd = reassignResource;
							}
						}
					}

					projectService.updateProjectPermission(projectData.getNodeRef(), taskListDataItem.getNodeRef(), toAdd, true);

					resources.add(toAdd);
				}

				taskListDataItem.setResources(resources);

			}

			// check workflow instance (task may be reopened) and
			// workflow properties
			projectWorkflowService.checkWorkflowInstance(projectData, taskListDataItem, deliverables);

			if ((taskListDataItem.getResources() != null) && !taskListDataItem.getResources().isEmpty()) {

				// workflow (task may have been set as InProgress
				// with
				// UI)
				if (((taskListDataItem.getWorkflowInstance() == null) || taskListDataItem.getWorkflowInstance().isEmpty())
						&& (taskListDataItem.getWorkflowName() != null) && !taskListDataItem.getWorkflowName().isEmpty()) {

					// start workflow
					projectWorkflowService.startWorkflow(projectData, taskListDataItem, deliverables);
				}
			}

		}

		logger.debug("State after - " + taskListDataItem.getTaskState() + " reformulate : " + reformulate);

		return reformulate;

	}

	private boolean visitDeliverables(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> deliverables) {

		TaskState previousState = taskListDataItem.getTaskState();

		if (TaskState.Completed.equals(taskListDataItem.getTaskState()) || TaskState.InProgress.equals(taskListDataItem.getTaskState())) {

			Integer taskCompletionPercent = TaskState.InProgress.equals(taskListDataItem.getTaskState()) ? 0 : COMPLETED;

			for (DeliverableListDataItem deliverable : deliverables) {

				if (TaskState.InProgress.equals(taskListDataItem.getTaskState())) {

					// Completed or Closed
					if ((deliverable.getCompletionPercent() != null) && (DeliverableState.Completed.equals(deliverable.getState())
							|| DeliverableState.Closed.equals(deliverable.getState()))) {
						taskCompletionPercent += deliverable.getCompletionPercent();
					}

					// set Planned dl InProgress
					if (DeliverableState.Planned.equals(deliverable.getState())) {
						deliverable.setState(DeliverableState.InProgress);
						deliverable.setUrl(projectService.getDeliverableUrl(projectData.getNodeRef(), deliverable.getUrl()));
						if ((deliverable.getUrl() != null) && deliverable.getUrl().startsWith(DeliverableUrl.CONTENT_URL_PREFIX)
								&& NodeRef.isNodeRef(deliverable.getUrl().substring(DeliverableUrl.CONTENT_URL_PREFIX.length()))) {
							deliverable.setContent(new NodeRef(deliverable.getUrl().substring(DeliverableUrl.CONTENT_URL_PREFIX.length())));
							deliverable.setUrl(null);
						}

					}

					if (DeliverableState.InProgress.equals(deliverable.getState())
							&& DeliverableScriptOrder.Pre.equals(deliverable.getScriptOrder())) {
						projectService.runScript(projectData, taskListDataItem, deliverable.getContent());
						deliverable.setState(DeliverableState.Completed);
					}
				}

				if (TaskState.Completed.equals(taskListDataItem.getTaskState())) {
					if (DeliverableState.InProgress.equals(deliverable.getState())) {
						if (DeliverableScriptOrder.Post.equals(deliverable.getScriptOrder())) {
							projectService.runScript(projectData, taskListDataItem, deliverable.getContent());
						}
						deliverable.setState(DeliverableState.Completed);
					}
				}

			}

			taskListDataItem.setCompletionPercent(taskCompletionPercent);

			// Status can change during script execution
			if (!previousState.equals(taskListDataItem.getTaskState()) && TaskState.InProgress.equals(taskListDataItem.getTaskState())) {
				logger.debug("Task " + taskListDataItem.getTaskName() + " reopen by script " + taskListDataItem.getTaskState());

				return true;
			}

		}

		return false;
	}

}
