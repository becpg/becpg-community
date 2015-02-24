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
package fr.becpg.repo.project.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskManualDate;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.formulation.PlanningFormulationHandler;

public class ProjectHelper {

	private static final int DURATION_DEFAULT = 1;
	private static final int DURATION_NEXT_DAY = 2;

	private static Log logger = LogFactory.getLog(PlanningFormulationHandler.class);

	public static TaskListDataItem getTask(ProjectData projectData, NodeRef taskListNodeRef) {

		if (taskListNodeRef != null && projectData.getTaskList() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				if (taskListNodeRef.equals(p.getNodeRef())) {
					return p;
				}
			}
		}
		return null;
	}

	public static List<TaskListDataItem> getNextTasks(ProjectData projectData, NodeRef taskListNodeRef) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		if (projectData.getTaskList() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				// taskNodeRef is null when we start project
				if (p.getPrevTasks().contains(taskListNodeRef) || (taskListNodeRef == null && (p.getIsGroup()==null || !p.getIsGroup()) && p.getPrevTasks().isEmpty())) {
					taskList.add(p);
				}
			}
		}
		return taskList;
	}

	public static List<TaskListDataItem> getPrevTasks(ProjectData projectData, TaskListDataItem taskListDataItem) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		if (taskListDataItem.getPrevTasks() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				if (taskListDataItem.getPrevTasks().contains(p.getNodeRef())) {
					taskList.add(p);
				}
			}
		}
		return taskList;
	}

	public static List<TaskListDataItem> getLastTasks(ProjectData projectData) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		if (projectData.getTaskList() != null) {
			for (TaskListDataItem t : projectData.getTaskList()) {
				if (getNextTasks(projectData, t.getNodeRef()).isEmpty() && (t.getIsGroup()==null || !t.getIsGroup())) {
					taskList.add(t);
				}
			}
		}
		return taskList;
	}

	public static List<TaskListDataItem> getChildrenTasks(ProjectData projectData, TaskListDataItem taskListDataItem) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		for (TaskListDataItem t : projectData.getTaskList()) {
			if (t.getParent() != null && t.getParent().equals(taskListDataItem)) {
				taskList.add(t);
			}
		}
		return taskList;
	}

	public static List<TaskListDataItem> getBrethrenTask(ProjectData projectData, TaskListDataItem nextTask) {
		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		for (TaskListDataItem t : projectData.getTaskList()) {
			for (NodeRef taskListNodeRef : t.getPrevTasks()) {
				if (!taskList.contains(t) && 
						nextTask.getPrevTasks().contains(taskListNodeRef)
						&& ((t.getParent() != null && t.getParent().equals(nextTask.getParent())) || (t.getParent() == null && nextTask.getParent() == null))) {
					taskList.add(t);
				}
			}
		}
		return  taskList;
	}

	public static void reOpenPath(ProjectData projectData, TaskListDataItem nextTask, TaskListDataItem refusedTask) {
		
		if(nextTask.equals(refusedTask)){
			nextTask.setTaskState(TaskState.InProgress);
			
			reOpenDeliverables(projectData,nextTask);
			
		} else {
			nextTask.setTaskState(TaskState.Planned);
			reOpenDeliverables(projectData,nextTask);
			//Reopen brethen
			for(TaskListDataItem brotherTask : getBrethrenTask(projectData,nextTask )){
				brotherTask.setTaskState(TaskState.Planned);
				reOpenDeliverables(projectData,nextTask);
			}
			
			for(TaskListDataItem prevTask : getPrevTasks(projectData,nextTask )){
				reOpenPath(projectData,prevTask, refusedTask);
			}
			
		}

	}

	private static void reOpenDeliverables(ProjectData projectData, TaskListDataItem nextTask) {
		List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData, nextTask.getNodeRef());
		for(DeliverableListDataItem dl : nextDeliverables){
			if(dl.getTasks().size() == 1){
				dl.setState(DeliverableState.Planned);
			}
		}
		
	}

	public static List<TaskListDataItem> getSourceTasks(ProjectData projectData) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		for (TaskListDataItem t : projectData.getTaskList()) {
			// has parent with prevTask ?
			boolean hasPrevTaskInParent = false;
			TaskListDataItem parent = t.getParent();
			if (parent != null) {
				if (parent.getPrevTasks().isEmpty()) {
					parent = parent.getParent();
				} else {
					hasPrevTaskInParent = true;
					break;
				}
			}
			boolean hasChildren = !ProjectHelper.getChildrenTasks(projectData, t).isEmpty();
			if (hasChildren == false && hasPrevTaskInParent == false) {
				taskList.add(t);
			}
		}
		return taskList;
	}

	public static List<DeliverableListDataItem> getDeliverables(ProjectData projectData, NodeRef taskListNodeRef) {

		List<DeliverableListDataItem> deliverableList = new ArrayList<DeliverableListDataItem>();
		if (projectData.getDeliverableList() != null) {
			for (DeliverableListDataItem d : projectData.getDeliverableList()) {
				if (d.getTasks() != null && d.getTasks().contains(taskListNodeRef)) {
					deliverableList.add(d);
				}
			}
		}
		return deliverableList;
	}

	public static boolean areTasksDone(ProjectData projectData) {

		if (projectData.getTaskList() != null && !projectData.getTaskList().isEmpty()) {
			for (TaskListDataItem t : projectData.getTaskList()) {
				if (!TaskState.Completed.equals(t.getTaskState())) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public static boolean areTasksDone(ProjectData projectData, List<NodeRef> taskNodeRefs) {

		// no task : they are done
		if (taskNodeRefs.isEmpty()) {
			return true;
		}

		List<NodeRef> inProgressTasks = new ArrayList<NodeRef>();
		inProgressTasks.addAll(taskNodeRefs);

		if (projectData.getTaskList() != null && projectData.getTaskList().isEmpty() == false) {
			for (int i = projectData.getTaskList().size() - 1; i >= 0; i--) {
				TaskListDataItem t = projectData.getTaskList().get(i);

				if (taskNodeRefs.contains(t.getNodeRef())){
					
					if(TaskState.Completed.equals(t.getTaskState())) {
						inProgressTasks.remove(t.getNodeRef());
					}
					else if(TaskState.Cancelled.equals(t.getTaskState())){
						if(ProjectHelper.areTasksDone(projectData, t.getPrevTasks())){
							inProgressTasks.remove(t.getNodeRef());
						}
					}					
				}
			}
		}

		return inProgressTasks.isEmpty();
	}

	/**
	 * completedPercent is calculated on duration property
	 * 
	 * @param projectData
	 * @return
	 */
	public static int geProjectCompletionPercent(ProjectData projectData) {

		int totalWork = 0;
		int workDone = 0;
		for (TaskListDataItem p : projectData.getTaskList()) {
			Integer duration = p.getDuration() != null ? p.getDuration() : calculateTaskDuration(p.getStart(), p.getEnd());
			if (duration != null) {
				totalWork += duration;
				if (TaskState.Completed.equals(p.getTaskState()) || TaskState.Cancelled.equals(p.getTaskState())) {
					workDone += duration;
				}
			}
		}

		return totalWork != 0 ? 100 * workDone / totalWork : 0;
	}

	public static Date getLastEndDate(ProjectData projectData) {
		Date endDate = null;
		for (TaskListDataItem task : projectData.getTaskList()) {
			if (!task.getIsGroup() && (endDate == null || (task.getEnd() != null && task.getEnd().after(endDate)))) {
				endDate = task.getEnd();
			}
		}
		return endDate;
	}

	public static Date getFirstStartDate(ProjectData projectData) {
		List<TaskListDataItem> tasks = getNextTasks(projectData, null);
		Date startDate = null;
		for (TaskListDataItem task : tasks) {
			if (!task.getIsGroup() && (startDate == null || (task.getStart() != null && task.getStart().before(startDate)))) {
				startDate = task.getStart();
			}
		}
		return startDate;
	}

	public static void setTaskStartDate(TaskListDataItem t, Date startDate) {
		logger.debug("task: " + t.getTaskName() + " state: " + t.getTaskState() + " start: " + startDate);
		if ((t.getIsGroup() || TaskState.Planned.equals(t.getTaskState()) || TaskState.Cancelled.equals(t.getTaskState()) || (TaskState.InProgress.equals(t.getTaskState()) && t.getStart() == null)) && !TaskManualDate.Start.equals(t.getManualDate())) {
			t.setStart(removeTime(startDate));
		}
	}

	public static void setTaskEndDate(TaskListDataItem t, Date endDate) {
		logger.debug("task: " + t.getTaskName() + " state: " + t.getTaskState() + " end: " + endDate);
		if ((t.getIsGroup() || TaskState.Planned.equals(t.getTaskState()) || TaskState.Cancelled.equals(t.getTaskState()) || TaskState.InProgress.equals(t.getTaskState())) && !TaskManualDate.End.equals(t.getManualDate())) {
			t.setEnd(removeTime(endDate));
		}
	}

	public static Date removeTime(Date date) {
		if (date == null) {
			return null;
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		}
	}

	public static Date calculateNextDate(Date startDate, Integer duration, boolean isPlanned) {

		logger.debug("startDate: " + startDate);
		logger.debug("duration: " + duration);

		if (startDate == null) {
			return null;
		}

		if (duration == null) {
			duration = DURATION_DEFAULT;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		int i = 1;
		while (i < duration) {
			if (isPlanned) {
				calendar.add(Calendar.DATE, 1);
			} else {
				calendar.add(Calendar.DATE, -1);
			}
			if (isWorkingDate(calendar)) {
				i++;
			}
		}

		logger.debug("calendar.getTime(): " + calendar.getTime());
		return calendar.getTime();
	}

	public static boolean isWorkingDate(Calendar calendar) {
		// saturday == 7 || sunday == 1
		return calendar.get(Calendar.DAY_OF_WEEK) != 7 && calendar.get(Calendar.DAY_OF_WEEK) != 1;
	}

	public static Integer calculateTaskDuration(Date startDate, Date endDate) {

		if (startDate == null || endDate == null) {
			logger.debug("startDate or endDate is null. startDate: " + startDate + " - endDate: " + endDate);
			return null;
		}

		if (startDate.after(endDate)) {
			logger.warn("startDate is after endDate");
			return null;
		}

		int duration = 1;
		Calendar startDateCal = Calendar.getInstance();
		startDateCal.setTime(startDate);
		Calendar endDateCal = Calendar.getInstance();
		endDateCal.setTime(endDate);
		while (startDateCal.before(endDateCal)) {
			if (isWorkingDate(startDateCal)) {
				duration++;
			}
			startDateCal.add(Calendar.DAY_OF_MONTH, 1);
		}

		logger.debug("calculateTaskDuration startDate: " + startDate + " - endDate: " + endDate + " - duration: " + duration);
		return duration;
	}

	public static Date calculateEndDate(Date startDate, Integer duration) {
		return calculateNextDate(startDate, duration, true);
	}

	public static Date calculateNextStartDate(Date endDate) {
		return calculateNextDate(endDate, DURATION_NEXT_DAY, true);
	}

	public static Date calculateStartDate(Date endDate, Integer duration) {
		return calculateNextDate(endDate, duration, false);
	}

	public static Date calculatePrevEndDate(Date startDate) {
		return calculateNextDate(startDate, DURATION_NEXT_DAY, false);
	}

	public static Integer calculateOverdue(TaskListDataItem task) {

		Date endDate;

		if (TaskState.InProgress.equals(task.getTaskState())) {
			endDate = ProjectHelper.removeTime(new Date());

			// we wait the overdue of the task to take it in account
			if (task.getEnd() != null && endDate.before(task.getEnd())) {
				return null;
			}
		} else if (TaskState.Completed.equals(task.getTaskState())) {
			endDate = task.getEnd();
		} else {
			return null;
		}
		Integer realDuration = calculateTaskDuration(task.getStart(), endDate);
		Integer plannedDuration = task.getDuration() != null ? task.getDuration() : task.getIsMilestone() ? DURATION_DEFAULT : null;
		if (realDuration != null && plannedDuration != null) {
			return realDuration - plannedDuration;
		}

		return null;
	}

}
