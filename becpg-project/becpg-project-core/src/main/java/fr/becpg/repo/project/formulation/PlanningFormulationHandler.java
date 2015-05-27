/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

/**
 * Project visitor to calculate planningDates
 * 
 * @author quere
 * 
 */
public class PlanningFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static Log logger = LogFactory.getLog(PlanningFormulationHandler.class);
	private static int DEFAULT_WORK_HOURS_PER_DAY = 8;

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		boolean isPlanning = projectData.getPlanningMode() == null || projectData.getPlanningMode().equals(PlanningMode.Planning);
		calculateGroup(projectData);
		clearDates(projectData, isPlanning);

		Composite<TaskListDataItem> composite = CompositeHelper.getHierarchicalCompoList(projectData.getTaskList());

		if (logger.isDebugEnabled()) {
			logger.debug("after clear " + composite);
		}

		if (isPlanning) {
			// planning
			Date startDate = ProjectHelper.getFirstStartDate(projectData);
			if (startDate == null) {
				if (projectData.getStartDate() == null) {
					startDate = ProjectHelper.calculateNextStartDate(projectData.getCreated());
					projectData.setStartDate(startDate);
				} else {
					startDate = projectData.getStartDate();
				}
			} else {
				projectData.setStartDate(startDate);
			}
			projectData.setCompletionDate(startDate);
			calculatePlanningOfNextTasks(projectData, null, startDate);
		} else {
			// retro-planning
			Date endDate = ProjectHelper.getLastEndDate(projectData);
			if (endDate == null) {
				endDate = projectData.getDueDate();
				if (endDate == null) {
					endDate = ProjectHelper.calculatePrevEndDate(projectData.getCreated());
				}
			} else {
				projectData.setDueDate(endDate);
			}
			projectData.setStartDate(endDate);
			calculateRetroPlanningOfPrevTasks(projectData, null, projectData.getStartDate());
		}

		calculateDurationAndWork(projectData, composite);

		Integer projectOverdue = calculateOverdue(projectData, null);
		projectData.setOverdue(projectOverdue);

		if (logger.isDebugEnabled()) {
			logger.debug("End of formulation process " + composite);
		}

		return true;
	}

	private void clearDates(ProjectData projectData, boolean isPlanning) {

		for (TaskListDataItem tl : projectData.getTaskList()) {
			if (projectData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
				tl.setStart(null);
				tl.setEnd(null);
			} else {

				if (hasPlannedDuration(tl)) {
					//in retro-planning or task has prev tasks
					if (tl.getIsGroup() || !isPlanning || ProjectHelper.getPrevTasks(projectData, tl).isEmpty() == false) {
						ProjectHelper.setTaskStartDate(tl, null);
					}
					//in planning or task has next tasks
					if (tl.getIsGroup() || isPlanning || ProjectHelper.getNextTasks(projectData, tl.getNodeRef()).isEmpty() == false) {
						ProjectHelper.setTaskEndDate(tl, null);
					}
				}
				else if(tl.getStart() != null && tl.getEnd() != null){
					tl.setDuration(ProjectHelper.calculateTaskDuration(tl.getStart(), tl.getEnd()));
				}
			}
		}
	}

	private void calculateGroup(ProjectData projectData) {

		for (TaskListDataItem tl : projectData.getTaskList()) {
			if (ProjectHelper.getChildrenTasks(projectData, tl).isEmpty()) {
				tl.setIsGroup(false);
			} else {
				tl.setIsGroup(true);
			}
		}
	}

	private void calculateCapacity(TaskListDataItem tl) {
		if (tl.getWork() != null && tl.getDuration() != null && tl.getDuration() != 0) {
			double hoursPerDay = DEFAULT_WORK_HOURS_PER_DAY;
			if (tl.getResourceCost() != null && tl.getResourceCost().getHoursPerDay() != null && tl.getResourceCost().getHoursPerDay() != 0d) {
				hoursPerDay = tl.getResourceCost().getHoursPerDay();
			}
			tl.setCapacity((int) (100 * tl.getWork() / (tl.getDuration() * hoursPerDay)));
		}
	}

	private void calculateDurationAndWork(ProjectData projectData, Composite<TaskListDataItem> composite) {

		Integer duration = 0;
		Double work = 0d;
		for (Composite<TaskListDataItem> component : composite.getChildren()) {
			calculateDurationAndWork(projectData, component);
			TaskListDataItem taskListDataItem = component.getData();
			if (taskListDataItem.getWork() != null) {
				work += taskListDataItem.getWork();
			}
			if (taskListDataItem.getDuration() != null) {
				duration += taskListDataItem.getDuration();
			}
		}
		if (composite.isRoot()) {
			projectData.setWork(work);
		} else {
			if (!composite.isLeaf()) {
				composite.getData().setDuration(ProjectHelper.calculateTaskDuration(composite.getData().getStart(), composite.getData().getEnd()));
				composite.getData().setWork(work);
			}
			calculateCapacity(composite.getData());
		}
	}

	private void calculateDatesOfParent(TaskListDataItem composite, TaskListDataItem component) {

		if (composite != null && component != null) {
			if (component.getStart() != null && (composite.getStart() == null || composite.getStart().after(component.getStart()))) {
				ProjectHelper.setTaskStartDate(composite, component.getStart());
				calculateDatesOfParent(composite.getParent(), composite);
			}

			if (component.getEnd() != null && (composite.getEnd() == null || composite.getEnd().before(component.getEnd()))) {
				ProjectHelper.setTaskEndDate(composite, component.getEnd());
				calculateDatesOfParent(composite.getParent(), composite);
			}
		}
	}

	private void calculatePlanningOfNextTasks(ProjectData projectData, NodeRef taskNodeRef, Date startDate) throws FormulateException {

		for (TaskListDataItem nextTask : ProjectHelper.getNextTasks(projectData, taskNodeRef)) {
			// avoid cycle
			checkCycle(taskNodeRef, nextTask);

			calculatePlanningOfTask(projectData, nextTask, startDate);
		}
	}

	private void calculatePlanningOfTask(ProjectData projectData, TaskListDataItem nextTask, Date startDate) throws FormulateException {

		logger.debug("nextTask " + nextTask.getTaskName() + " - startDate " + startDate);

		// check new startDate is after, otherwise we stop since a
		// parallel branch is after
		if (startDate != null && (nextTask.getStart() == null || nextTask.getStart().before(startDate))) {
			ProjectHelper.setTaskStartDate(nextTask, startDate);
		}

		if (nextTask.getIsGroup()) {
			calculatePlanningOfChildren(projectData, nextTask);
		} else if (hasPlannedDuration(nextTask)) {
			Date endDate = ProjectHelper.calculateEndDate(nextTask.getStart(), nextTask.getDuration());
			ProjectHelper.setTaskEndDate(nextTask, endDate);
		}

		calculateDatesOfParent(nextTask.getParent(), nextTask);

		if (nextTask.getEnd() != null && (projectData.getCompletionDate() == null || projectData.getCompletionDate().before(nextTask.getEnd()))) {
			projectData.setCompletionDate(nextTask.getEnd());
		}

		// end date is null if task is cancelled		
		Date d = TaskState.Cancelled.equals(nextTask.getTaskState()) ? startDate : ProjectHelper.calculateNextStartDate(nextTask.getEnd());
		calculatePlanningOfNextTasks(projectData, nextTask.getNodeRef(), d);
	}

	private void calculatePlanningOfChildren(ProjectData projectData, TaskListDataItem taskListDataItem) throws FormulateException {

		
		
		List<TaskListDataItem> children = ProjectHelper.getChildrenTasks(projectData, taskListDataItem);

//		for (TaskListDataItem c : children) {
//			if (c.getPrevTasks().isEmpty()) {
//				calculatePlanningOfTask(projectData, c, taskListDataItem.getStart());
//			}
//		}

		Date endDate = taskListDataItem.getStart();
		for (TaskListDataItem c : children) {
			if (c.getEnd() != null && c.getEnd().after(endDate)) {
				endDate = c.getEnd();
			}
		}
		ProjectHelper.setTaskEndDate(taskListDataItem, endDate);
	}

	private void calculateRetroPlanningOfPrevTasks(ProjectData projectData, TaskListDataItem task, Date endDate) throws FormulateException {
		List<TaskListDataItem> prevTasks = null;
		if (task == null) {
			prevTasks = ProjectHelper.getLastTasks(projectData);
		} else {
			prevTasks = ProjectHelper.getPrevTasks(projectData, task);
		}

		for (TaskListDataItem prevTask : prevTasks) {

			// avoid cycle
			checkCycle(prevTask.getNodeRef(), task);
			
			if (TaskState.InProgress.equals(prevTask.getTaskState())) {

				// InProgress => calculate endDate from the startDate and we stop
				// retro-planning
				 if (hasPlannedDuration(prevTask)) {
					Date d = ProjectHelper.calculateEndDate(prevTask.getStart(), prevTask.getDuration());
					ProjectHelper.setTaskEndDate(prevTask, d);
				}
			} else {
				
					// check new endDate is before, otherwise we stop
					// since a parallel branch is before
					if (prevTask.getEnd() == null || prevTask.getEnd().after(endDate)) {
						ProjectHelper.setTaskEndDate(prevTask, endDate);
					}

					if (hasPlannedDuration(prevTask)) {
						Date startDate = ProjectHelper.calculateStartDate(prevTask.getEnd(), prevTask.getDuration());
						ProjectHelper.setTaskStartDate(prevTask, startDate);
					}
			

				if (prevTask.getStart() != null && projectData.getStartDate().after(prevTask.getStart())) {
					projectData.setStartDate(prevTask.getStart());
				}

			}
			
			// start date is null if task is cancelled
			Date d = TaskState.Cancelled.equals(prevTask.getTaskState()) ? endDate : ProjectHelper.calculatePrevEndDate(prevTask.getStart());
			calculateRetroPlanningOfPrevTasks(projectData, prevTask, d);

			calculateDatesOfParent(prevTask.getParent(), prevTask);
		}
	}
	


	private Integer calculateOverdue(ProjectData projectData, NodeRef taskNodeRef) throws FormulateException {

		Integer taskOverdue = 0;
		Integer nextTaskOverdue = 0;
		boolean initOverdue = true;
		boolean initNextOverdue = true;

		for (TaskListDataItem nextTask : ProjectHelper.getNextTasks(projectData, taskNodeRef)) {

			// avoid cycle
			checkCycle(taskNodeRef, nextTask);

			Integer o = ProjectHelper.calculateOverdue(nextTask);
			if (o != null && (initOverdue || o.compareTo(taskOverdue) < 0)) {
				taskOverdue += o;
				initOverdue = false;
			}

			o = calculateOverdue(projectData, nextTask.getNodeRef());
			if (o != null && (initNextOverdue || o.compareTo(nextTaskOverdue) < 0)) {
				nextTaskOverdue += o;
				initNextOverdue = false;
			}
		}

		return taskOverdue + nextTaskOverdue;
	}

	private void checkCycle(NodeRef taskNodeRef, TaskListDataItem nextTask) throws FormulateException {

		if (taskNodeRef != null && nextTask != null && taskNodeRef.equals(nextTask.getNodeRef())) {
			String error = "cycle detected. taskNodeRef: " + taskNodeRef + " nextTask: " + nextTask.getNodeRef() + " nextTask name: "
					+ nextTask.getTaskName();
			logger.error(error);
			throw new FormulateException(error);
		}
	}

	private boolean hasPlannedDuration(TaskListDataItem task) {

		if (task.getDuration() != null || task.getIsMilestone()) {
			return true;
		} else {
			return false;
		}
	}
}
