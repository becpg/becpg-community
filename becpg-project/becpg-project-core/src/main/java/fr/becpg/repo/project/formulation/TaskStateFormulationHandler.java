/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
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

public class TaskStateFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static final int COMPLETED = 100;

	private static Log logger = LogFactory.getLog(TaskStateFormulationHandler.class);

	private ProjectWorkflowService projectWorkflowService;

	private ProjectService projectService;

	public void setProjectWorkflowService(ProjectWorkflowService projectWorkflowService) {
		this.projectWorkflowService = projectWorkflowService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		
		// we don't want tasks of project template start
		if (!projectData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {

			// start project if startdate is before now and startdate != created
			// otherwise ProjectMgr will start it manually
			if (ProjectState.Planned.equals(projectData.getProjectState()) && projectData.getStartDate() != null
					&& projectData.getStartDate().before(new Date())) {
				projectData.setProjectState(ProjectState.InProgress);
			}

			// even if project is not in Progress, we visit it because a task
			// can start the project (manual task or task that has startdate <
			// NOW)
			 if(visitTask(projectData, null)){
				 
				 if(projectData.getReformulateCount()==null){
					 projectData.setReformulateCount(1);
				 } else {
					 if( projectData.getReformulateCount()<3){
						 projectData.setReformulateCount(projectData.getReformulateCount()+1);
					 }
				 }
				 
			 }

			boolean allTaskPlanned = true;
			for (TaskListDataItem task : projectData.getTaskList()) {
				if (!TaskState.Planned.equals(task.getTaskState())) {
					allTaskPlanned = false;
					break;
				}
			}
			if (!allTaskPlanned && ProjectState.Planned.equals(projectData.getProjectState())) {
				projectData.setProjectState(ProjectState.InProgress);
			} else if (allTaskPlanned && ProjectState.InProgress.equals(projectData.getProjectState())) {
				projectData.setProjectState(ProjectState.Planned);
			}

			// is project completed ?
			if (ProjectHelper.areTasksDone(projectData)) {
				projectData.setCompletionDate(ProjectHelper.getLastEndDate(projectData));
				projectData.setCompletionPercent(COMPLETED);
				projectData.setProjectState(ProjectState.Completed);
			}

			projectData.setCompletionPercent(ProjectHelper.geProjectCompletionPercent(projectData));

			calculateProjectLegends(projectData);
		}

		return true;
	}

	private boolean visitTask(ProjectData projectData, TaskListDataItem taskListDataItem) {

		
		logger.debug("Enter visit task : " +(taskListDataItem!=null ? taskListDataItem.getTaskName() : "Project root"));
		
		boolean reformulate = false;
		
		NodeRef taskListNodeRef = taskListDataItem != null ? taskListDataItem.getNodeRef() : null;

		// add next tasks
		List<TaskListDataItem> nextTasks = ProjectHelper.getNextTasks(projectData, taskListNodeRef);
		
		if (!nextTasks.isEmpty()) {
			for (TaskListDataItem nextTask : nextTasks) {

				// cancel active workflow if task is not anymore InProgress
				logger.debug("Visit task : " + nextTask.getTaskName()+" - state - "+nextTask.getState());
				
				if (!TaskState.InProgress.equals(nextTask.getTaskState()) && projectWorkflowService.isWorkflowActive(nextTask)) {
					projectWorkflowService.cancelWorkflow(nextTask);
				}

				if (TaskState.Planned.equals(nextTask.getTaskState())) {

					// no previous task
					if (nextTask.getPrevTasks().isEmpty()) {
						if (nextTask.getStart()!=null &&  nextTask.getStart().before(new Date())) {
							logger.debug("Start first task.");
							nextTask.setTaskState(TaskState.InProgress);

						}
					} else {
						// previous task are done
						if (ProjectHelper.areTasksDone(projectData, nextTask.getPrevTasks())) {
							if (nextTask.getManualDate() == null) {
								logger.debug("Start task since previous are done");
								nextTask.setTaskState(TaskState.InProgress);
							}
							// manual date -> we wait the date
							else if (nextTask.getStart()!=null && nextTask.getStart().before(new Date())) {
								logger.debug("Start task since we are after planned startDate. start planned: " + nextTask.getStart());
								nextTask.setTaskState(TaskState.InProgress);
							}
						}
					}

				} else if (TaskState.Completed.equals(nextTask.getTaskState())) {

					nextTask.setCompletionPercent(COMPLETED);
					
					List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData, nextTask.getNodeRef());

					for (DeliverableListDataItem nextDeliverable : nextDeliverables) {
						
						// set Planned dl InProgress
						if (DeliverableState.InProgress.equals(nextDeliverable.getState())) {
							if(DeliverableScriptOrder.Post.equals(nextDeliverable.getScriptOrder())){
								projectService.runScript(projectData, nextTask, nextDeliverable.getContent());
							} 
							nextDeliverable.setState(DeliverableState.Completed);
						}
						
					}
					
					// Status can change during script execution
					if (!TaskState.Completed.equals(nextTask.getTaskState())) {
						logger.debug("Task "+nextTask.getTaskName()+" reopen by script "+nextTask.getTaskState());
						reformulate = true;
					}
					
					
				} else if (TaskState.Refused.equals(nextTask.getTaskState()) && nextTask.getRefusedTask() != null) {
					boolean shouldRefused = true;
					logger.debug("Enter refused task");
					// Check if all brothers are closed
					for (TaskListDataItem brotherTask : ProjectHelper.getBrethrenTask(projectData, nextTask)) {
						if (!nextTask.getRefusedTask().equals(brotherTask) && TaskState.InProgress.equals(brotherTask.getTaskState())) {
							shouldRefused = false;
							logger.debug("Will not refused task as there is still some brother Open");
							break;
						}
					}

					if (shouldRefused) {
						logger.debug("Reopen path : "+nextTask.getRefusedTask().getTaskName());
						
						ProjectHelper.reOpenPath(projectData, nextTask, nextTask.getRefusedTask());
						
						//Revisit tasks 
						reformulate = true;
					}

				}

				if (TaskState.InProgress.equals(nextTask.getTaskState())) {

					Integer taskCompletionPercent = 0;
					List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData, nextTask.getNodeRef());

					for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

						// Completed or Closed
						if (DeliverableState.Completed.equals(nextDeliverable.getState())
								|| DeliverableState.Closed.equals(nextDeliverable.getState())) {
							taskCompletionPercent += nextDeliverable.getCompletionPercent();
						}

						// set Planned dl InProgress
						if (DeliverableState.Planned.equals(nextDeliverable.getState())) {
							nextDeliverable.setState(DeliverableState.InProgress);
							nextDeliverable.setUrl(projectService.getDeliverableUrl(projectData.getNodeRef(), nextDeliverable.getUrl()));
							if(DeliverableScriptOrder.Pre.equals(nextDeliverable.getScriptOrder())){
								projectService.runScript(projectData, nextTask, nextDeliverable.getContent());
								nextDeliverable.setState(DeliverableState.Completed);
							} 
						}
						
					}
					
					//Status can change during script execution
					if (TaskState.InProgress.equals(nextTask.getTaskState()) && !nextTask.getIsGroup()) {
						
						logger.debug("set completion percent to value " + taskCompletionPercent + " - noderef: " + nextTask.getNodeRef());
						nextTask.setCompletionPercent(taskCompletionPercent == 0 ? null : taskCompletionPercent);
						
	
						// check workflow instance (task may be reopened) and
						// workflow properties
						projectWorkflowService.checkWorkflowInstance(projectData, nextTask, nextDeliverables);
	
						if (nextTask.getResources() != null && !nextTask.getResources().isEmpty()) {
	
							nextTask.setResources(projectService.updateTaskResources(projectData.getNodeRef(), nextTask.getNodeRef(),
									nextTask.getResources(), true));
	
							// workflow (task may have been set as InProgress with
							// UI)
							if ((nextTask.getWorkflowInstance() == null || nextTask.getWorkflowInstance().isEmpty())
									&& nextTask.getWorkflowName() != null && !nextTask.getWorkflowName().isEmpty()) {
	
								// start workflow
								projectWorkflowService.startWorkflow(projectData, nextTask, nextDeliverables);
							}
						}
					} else {
						logger.debug("Task "+nextTask.getTaskName()+" reopen by script "+nextTask.getTaskState());
						reformulate = true;
					}
				}

				// we visit every task since user can have started a task in the
				// middle of the project even if previous are not started
				reformulate = reformulate || visitTask(projectData, nextTask);
				// children first
				// visitGroup(projectData, nextTask);
				// parent second
				visitGroup(projectData, nextTask.getParent());
			}
		}
		
		return reformulate;
	}

	private void visitGroup(ProjectData projectData, TaskListDataItem parent) {

		// close Group ?
		if (parent != null) {
			boolean hasTaskInProgress = false;
			boolean allTasksPlanned = true;
			int completionPerc = 0;
			List<TaskListDataItem> tasks = ProjectHelper.getChildrenTasks(projectData, parent);
			for (TaskListDataItem c : tasks) {
				completionPerc += (c.getCompletionPercent()!=null?c.getCompletionPercent():0);
				if (TaskState.InProgress.equals(c.getTaskState())) {
					hasTaskInProgress = true;
				} else if (TaskState.Completed.equals(c.getTaskState())) {
					allTasksPlanned = false;
				}
			}
			if (hasTaskInProgress) {
				parent.setTaskState(TaskState.InProgress);
			} else if (allTasksPlanned) {
				parent.setTaskState(TaskState.Planned);
			} else {
				parent.setTaskState(TaskState.Completed);
			}
			parent.setCompletionPercent(completionPerc/tasks.size());
			
		}
	}

	private void calculateProjectLegends(ProjectData projectData) {

		if (projectData.getLegends() == null) {
			logger.debug("projectData.setLegends(new ArrayList<NodeRef>());");
			projectData.setLegends(new ArrayList<NodeRef>());
		}

		for (TaskListDataItem tl : projectData.getTaskList()) {

			if (TaskState.InProgress.equals(tl.getTaskState())) {
				if (!projectData.getLegends().contains(tl.getTaskLegend())) {
					projectData.getLegends().add(tl.getTaskLegend());
				}
			} else if (TaskState.Completed.equals(tl.getTaskState())) {
				if (projectData.getLegends().contains(tl.getTaskLegend())) {
					projectData.getLegends().remove(tl.getTaskLegend());
				}
			}

		}
	}
}
