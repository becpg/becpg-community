/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskManualDate;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.formulation.TaskWrapper;

/**
 * <p>ProjectHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProjectHelper {

	private static final int DURATION_DEFAULT = 1;
	private static final int DURATION_NEXT_DAY = 2;

	private static final Log logger = LogFactory.getLog(ProjectHelper.class);

	/**
	 * <p>isOnHold.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @return a boolean.
	 */
	public static boolean isOnHold(ProjectData projectData) {
		return projectData.getAspects().contains(ContentModel.ASPECT_CHECKED_OUT)
				|| projectData.getAspects().contains(ContentModel.ASPECT_WORKING_COPY)
				|| projectData.getAspects().contains(BeCPGModel.ASPECT_COMPOSITE_VERSION)
				|| ProjectState.Cancelled.equals(projectData.getProjectState()) || ProjectState.OnHold.equals(projectData.getProjectState());
	}

	/**
	 * <p>getTask.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param taskListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	@Deprecated
	public static TaskListDataItem getTask(ProjectData projectData, NodeRef taskListNodeRef) {

		if ((taskListNodeRef != null) && (projectData.getTaskList() != null)) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				if (taskListNodeRef.equals(p.getNodeRef())) {
					return p;
				}
			}
		}
		return null;
	}

	/*
	 * Return all tasks including subproject tasks exclude groups
	 */
	/**
	 * <p>getNextTasks.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param taskListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	@Deprecated
	public static List<TaskListDataItem> getNextTasks(ProjectData projectData, NodeRef taskListNodeRef) {

		List<TaskListDataItem> taskList = new ArrayList<>();
		if (projectData.getTaskList() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				// taskNodeRef is null when we start project
				if (p.getPrevTasks().contains(taskListNodeRef) || ((taskListNodeRef == null)
						&& ((p.getIsGroup() == null) || !p.getIsGroup() || (p.getSubProject() != null)) && p.getPrevTasks().isEmpty())) {
					taskList.add(p);
				}
			}
		}
		return taskList;
	}

	/**
	 * <p>getPrevTasks.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param taskListDataItem a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a {@link java.util.List} object.
	 */
	@Deprecated
	public static List<TaskListDataItem> getPrevTasks(ProjectData projectData, TaskListDataItem taskListDataItem) {

		List<TaskListDataItem> taskList = new ArrayList<>();
		if (taskListDataItem.getPrevTasks() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				if (taskListDataItem.getPrevTasks().contains(p.getNodeRef())) {
					taskList.add(p);
				}
			}
		}
		return taskList;
	}

	/**
	 * <p>getLastTasks.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @return a {@link java.util.List} object.
	 */
	@Deprecated
	public static List<TaskListDataItem> getLastTasks(ProjectData projectData) {

		List<TaskListDataItem> taskList = new ArrayList<>();
		if (projectData.getTaskList() != null) {
			for (TaskListDataItem t : projectData.getTaskList()) {
				if (getNextTasks(projectData, t.getNodeRef()).isEmpty() && ((t.getIsGroup() == null) || !t.getIsGroup())) {
					taskList.add(t);
				}
			}
		}
		return taskList;
	}

	/**
	 * <p>getChildrenTasks.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param taskListDataItem a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a {@link java.util.List} object.
	 */
	@Deprecated
	public static List<TaskListDataItem> getChildrenTasks(ProjectData projectData, TaskListDataItem taskListDataItem) {

		List<TaskListDataItem> taskList = new ArrayList<>();
		for (TaskListDataItem t : projectData.getTaskList()) {
			if ((t.getParent() != null) && t.getParent().equals(taskListDataItem)) {
				taskList.add(t);
			}
		}
		return taskList;
	}

	/**
	 * <p>getBrethrenTask.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param nextTask a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<TaskListDataItem> getBrethrenTask(ProjectData projectData, TaskListDataItem nextTask) {
		List<TaskListDataItem> taskList = new ArrayList<>();
		for (TaskListDataItem t : projectData.getTaskList()) {
			for (NodeRef taskListNodeRef : t.getPrevTasks()) {
				if (!taskList.contains(t) && nextTask.getPrevTasks().contains(taskListNodeRef)
						&& (((t.getParent() != null) && t.getParent().equals(nextTask.getParent()))
								|| ((t.getParent() == null) && (nextTask.getParent() == null)))) {
					taskList.add(t);
				}
			}
		}
		return taskList;
	}

	/**
	 * <p>reOpenRefusePath.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param nextTask a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param refusedTask a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param refusedTasksToReopen a {@link java.util.List} object.
	 * @param projectActivityService a {@link fr.becpg.repo.project.ProjectActivityService} object.
	 */
	public static void reOpenRefusePath(ProjectData projectData, TaskListDataItem nextTask, TaskListDataItem refusedTask,
			List<NodeRef> refusedTasksToReopen, ProjectActivityService projectActivityService) {

		if (nextTask.equals(refusedTask)) {
			setTaskState(nextTask, TaskState.InProgress, projectActivityService);
			reOpenDeliverables(projectData, nextTask);

		} else {
			reOpenRefusedTask(projectData, nextTask, refusedTasksToReopen, projectActivityService);
			// Reopen brethen
			for (TaskListDataItem brotherTask : getBrethrenTask(projectData, nextTask)) {
				reOpenRefusedTask(projectData, brotherTask, refusedTasksToReopen, projectActivityService);
			}

			for (TaskListDataItem prevTask : getPrevTasks(projectData, nextTask)) {
				reOpenRefusePath(projectData, prevTask, refusedTask, refusedTasksToReopen, projectActivityService);
			}

		}

	}

	private static void reOpenRefusedTask(ProjectData projectData, TaskListDataItem nextTask, List<NodeRef> refusedTasksToReopen,
			ProjectActivityService projectActivityService) {

		if ((refusedTasksToReopen == null) || refusedTasksToReopen.isEmpty() || refusedTasksToReopen.contains(nextTask.getNodeRef())) {
			if (!TaskState.Refused.equals(nextTask.getTaskState())) {
				setTaskState(nextTask, TaskState.Planned, projectActivityService);
			} else {
				nextTask.setIsRefused(true);
			}
			reOpenDeliverables(projectData, nextTask);
		} else if (TaskState.Refused.equals(nextTask.getTaskState())) {
			nextTask.setIsRefused(true);
			reOpenDeliverables(projectData, nextTask);
		}
	}

	private static void reOpenDeliverables(ProjectData projectData, TaskListDataItem nextTask) {
		List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData, nextTask.getNodeRef());
		for (DeliverableListDataItem dl : nextDeliverables) {
			if (dl.getTasks().size() == 1) {
				dl.setState(DeliverableState.Planned);
			}
		}

	}

	/**
	 * <p>getSourceTasks.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @return a {@link java.util.List} object.
	 */
	@Deprecated
	public static List<TaskListDataItem> getSourceTasks(ProjectData projectData) {

		List<TaskListDataItem> taskList = new ArrayList<>();
		for (TaskListDataItem t : projectData.getTaskList()) {
			// has parent with prevTask ?
			boolean hasPrevTaskInParent = false;
			TaskListDataItem parent = t.getParent();
			if (parent != null) {
				if (parent.getPrevTasks().isEmpty()) {
				} else {
					break;
				}
			}
			boolean hasChildren = !ProjectHelper.getChildrenTasks(projectData, t).isEmpty();
			if (!hasChildren && !hasPrevTaskInParent) {
				taskList.add(t);
			}
		}
		return taskList;
	}

	/**
	 * <p>getDeliverables.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param taskListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<DeliverableListDataItem> getDeliverables(ProjectData projectData, NodeRef taskListNodeRef) {

		List<DeliverableListDataItem> deliverableList = new ArrayList<>();
		if (projectData.getDeliverableList() != null) {
			for (DeliverableListDataItem d : projectData.getDeliverableList()) {
				if ((d.getTasks() != null) && d.getTasks().contains(taskListNodeRef)) {
					deliverableList.add(d);
				}
			}
		}
		return deliverableList;
	}

	/**
	 * <p>getLastEndDate.</p>
	 *
	 * @param tasks a {@link java.util.Set} object.
	 * @return a {@link java.util.Date} object.
	 */
	public static Date getLastEndDate(Set<TaskWrapper> tasks) {
		Date endDate = null;
		for (TaskWrapper task : tasks) {
			if(task.getTask()!=null && task.getTask().getEnd() != null) {
				if (!task.isCancelled() && !task.isParent()
						&& ((endDate == null) || task.getTask().getEnd().after(endDate))) {
					endDate = task.getTask().getEnd();
				}
			}
		}

		return endDate;
	}

	/**
	 * <p>getFirstStartDate.</p>
	 *
	 * @param tasks a {@link java.util.Set} object.
	 * @return a {@link java.util.Date} object.
	 */
	public static Date getFirstStartDate(Set<TaskWrapper> tasks) {

		return tasks.stream().filter(e -> e.isRoot() && ((e.getTask() != null) && (e.getTask().getStart() != null))).map(e -> e.getTask().getStart())
				.min(Date::compareTo).orElse(null);
	}

	/**
	 * <p>setTaskStartDate.</p>
	 *
	 * @param t a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param startDate a {@link java.util.Date} object.
	 */
	public static void setTaskStartDate(TaskListDataItem t, Date startDate) {
		if ((t.getIsGroup() || t.isPlanned()  || TaskState.OnHold.equals(t.getTaskState()) || TaskState.Cancelled.equals(t.getTaskState())
				|| (TaskState.InProgress.equals(t.getTaskState()) && (t.getStart() == null))) && !TaskManualDate.Start.equals(t.getManualDate())) {
			if (logger.isDebugEnabled()) {
				logger.debug("setTaskStartDate: " + t.getTaskName() + ", state: " + t.getTaskState() + ", start: " + startDate + ", is group:"
						+ t.getIsGroup());
			}
			t.setStart(removeTime(startDate));
		}
	}

	/**
	 * <p>setTaskEndDate.</p>
	 *
	 * @param t a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param endDate a {@link java.util.Date} object.
	 */
	public static void setTaskEndDate(TaskListDataItem t, Date endDate) {
		if ((t.getIsGroup() || t.isPlanned() || TaskState.OnHold.equals(t.getTaskState()) || TaskState.Cancelled.equals(t.getTaskState()) || TaskState.InProgress.equals(t.getTaskState()))
				&& !TaskManualDate.End.equals(t.getManualDate())) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"setTaskEndDate: " + t.getTaskName() + ", state: " + t.getTaskState() + ", end: " + endDate + ", is group:" + t.getIsGroup());
			}
			
			t.setEnd(removeTime(endDate));
		}
	}

	/**
	 * <p>removeTime.</p>
	 *
	 * @param date a {@link java.util.Date} object.
	 * @return a {@link java.util.Date} object.
	 */
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

	/**
	 * <p>calculateNextDate.</p>
	 *
	 * @param startDate a {@link java.util.Date} object.
	 * @param duration a {@link java.lang.Integer} object.
	 * @param isPlanned a boolean.
	 * @return a {@link java.util.Date} object.
	 */
	public static Date calculateNextDate(Date startDate, Integer duration, boolean isPlanned) {

		if (logger.isDebugEnabled()) {
			logger.debug("calculateNextDate - startDate: " + startDate);
			logger.debug("calculateNextDate - duration: " + duration);
		}

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

		if (logger.isDebugEnabled()) {
			logger.debug("calculateNextDate - next date: " + calendar.getTime());
		}
		return calendar.getTime();
	}

	/**
	 * <p>isWorkingDate.</p>
	 *
	 * @param calendar a {@link java.util.Calendar} object.
	 * @return a boolean.
	 */
	public static boolean isWorkingDate(Calendar calendar) {
		// saturday == 7 || sunday == 1
		return (calendar.get(Calendar.DAY_OF_WEEK) != 7) && (calendar.get(Calendar.DAY_OF_WEEK) != 1);
	}

	/**
	 * <p>calculateTaskDuration.</p>
	 *
	 * @param startDate a {@link java.util.Date} object.
	 * @param endDate a {@link java.util.Date} object.
	 * @return a {@link java.lang.Integer} object.
	 */
	public static Integer calculateTaskDuration(Date startDate, Date endDate) {

		if ((startDate == null) || (endDate == null)) {
			logger.debug("calculateTaskDuration - startDate or endDate is null. startDate: " + startDate + " - endDate: " + endDate);
			return null;
		}

		if (startDate.after(endDate)) {
			logger.debug("calculateTaskDuration - startDate is after endDate : " + startDate + " - " + endDate);
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

		if (logger.isDebugEnabled()) {
			logger.debug("calculateTaskDuration startDate: " + startDate + " - endDate: " + endDate + " - duration: " + duration);
		}
		return duration;
	}

	/**
	 * <p>calculateEndDate.</p>
	 *
	 * @param startDate a {@link java.util.Date} object.
	 * @param duration a {@link java.lang.Integer} object.
	 * @return a {@link java.util.Date} object.
	 */
	public static Date calculateEndDate(Date startDate, Integer duration) {
		return calculateNextDate(startDate, duration, true);
	}

	/**
	 * <p>calculateNextStartDate.</p>
	 *
	 * @param endDate a {@link java.util.Date} object.
	 * @return a {@link java.util.Date} object.
	 */
	public static Date calculateNextStartDate(Date endDate) {
		return calculateNextDate(endDate, DURATION_NEXT_DAY, true);
	}

	/**
	 * <p>calculateStartDate.</p>
	 *
	 * @param endDate a {@link java.util.Date} object.
	 * @param duration a {@link java.lang.Integer} object.
	 * @return a {@link java.util.Date} object.
	 */
	public static Date calculateStartDate(Date endDate, Integer duration) {
		return calculateNextDate(endDate, duration, false);
	}

	/**
	 * <p>calculatePrevEndDate.</p>
	 *
	 * @param startDate a {@link java.util.Date} object.
	 * @return a {@link java.util.Date} object.
	 */
	public static Date calculatePrevEndDate(Date startDate) {
		return calculateNextDate(startDate, DURATION_NEXT_DAY, false);
	}

	/**
	 * <p>calculateDuration.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a {@link java.lang.Integer} object.
	 */
	@Deprecated
	public static Integer calculateDuration(TaskListDataItem task) {
		return task.getDuration() != null ? task.getDuration() : (Boolean.TRUE.equals(task.getIsMilestone())) ? DURATION_DEFAULT : null;
	}

	/**
	 * <p>setTaskState.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param state a {@link fr.becpg.repo.project.data.projectList.TaskState} object.
	 * @param projectActivityService a {@link fr.becpg.repo.project.ProjectActivityService} object.
	 */
	public static void setTaskState(TaskListDataItem task, TaskState state, ProjectActivityService projectActivityService) {
		if (!state.equals(task.getTaskState())) {
			projectActivityService.postTaskStateChangeActivity(task.getNodeRef(), null, task.getTaskState().toString(), state.toString(), true);
			task.setTaskState(state);
		}
	}

	/**
	 * <p>resetProperties.</p>
	 *
	 * @param classQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param properties a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	public static Map<QName, Serializable> resetProperties(QName classQName, Map<QName, Serializable> properties) {

		if (ProjectModel.TYPE_TASK_LIST.equals(classQName)) {
			properties.remove(ProjectModel.PROP_TL_START);
			properties.remove(ProjectModel.PROP_TL_END);
			properties.remove(ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
			properties.remove(ProjectModel.PROP_TL_WORKFLOW_TASK_INSTANCE);
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

	/**
	 * <p>hasPlannedDuration.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a boolean.
	 */
	public static boolean hasPlannedDuration(TaskListDataItem task) {

		if ((task != null) && ((task.getDuration() != null) || (Boolean.TRUE.equals(task.getIsMilestone())))) {
			return true;
		} else {
			return false;
		}
	}
	
	

	public static  boolean isRoleAuhtority(String authorityName) {
		return (authorityName != null) && authorityName.startsWith(PermissionService.GROUP_PREFIX + ProjectRepoConsts.PROJECT_GROUP_PREFIX);
	}

	public static DeliverableListDataItem createDeliverable(String name, String description, DeliverableScriptOrder order, TaskListDataItem task, NodeRef content) {
		
		DeliverableListDataItem del = new DeliverableListDataItem();
		
		del.setName(name);
		del.setDescription(description);
		del.setState(DeliverableState.Planned);
		del.setScriptOrder(order);
		del.setContent(content);
		del.setTasks(new ArrayList<>());
		del.getTasks().add(task.getNodeRef());

		return del;
	}
}
