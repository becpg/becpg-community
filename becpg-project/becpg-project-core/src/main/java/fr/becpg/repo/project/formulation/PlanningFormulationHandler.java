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

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskManualDate;
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

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		calculateGroup(projectData);
		clearDates(projectData);		
		
		if(projectData.getPlanningMode() == null || projectData.getPlanningMode().equals(PlanningMode.Planning)){
			// planning
			Date startDate = ProjectHelper.getFirstStartDate(projectData);
			if(startDate == null){
				if (projectData.getStartDate() == null) {
					startDate = ProjectHelper.calculateNextStartDate(projectData.getCreated());
					projectData.setStartDate(startDate);
				}
				else{
					startDate = projectData.getStartDate();
				}				
			}
			else{
				projectData.setStartDate(startDate);
			}
			
			projectData.setCompletionDate(startDate);
			calculatePlanningOfNextTasks(projectData, (NodeRef)null, startDate);
		}
		else{
			//retro-planning
			Date endDate = ProjectHelper.getLastEndDate(projectData);
			if(endDate == null){
				endDate = projectData.getDueDate();
			}
			else{
				projectData.setDueDate(endDate);
			}
			
			projectData.setStartDate(endDate);
			calculateRetroPlanningOfPrevTasks(projectData, null, projectData.getStartDate());
		}
		
		Composite<TaskListDataItem> composite = CompositeHelper.getHierarchicalCompoList(projectData.getTaskList());		
		calculateDurationAndWork(composite);
		
		Integer projectOverdue = calculateOverdue(projectData, null);
		projectData.setOverdue(projectOverdue);
		
		if(logger.isDebugEnabled()){
			logger.debug(composite);
		}
		
		return true;
	}
	
	private void clearDates(ProjectData projectData){
		
		// Task dates manage project dates
		if(projectData.getDueDate() != null){
			Date endDate = ProjectHelper.getLastEndDate(projectData);
			if(endDate != null){
				projectData.setDueDate(endDate);
			}			
		}
		else{
			Date startDate = ProjectHelper.getFirstStartDate(projectData);
			if(startDate != null){
				projectData.setStartDate(startDate);
			}				
		}
		
		for(TaskListDataItem tl : projectData.getTaskList()){
			if(hasPlannedDuration(tl)){
				if((TaskState.Planned.equals(tl.getState()) || (tl.getIsGroup() && TaskState.InProgress.equals(tl.getState()))) &&
						!TaskManualDate.Start.equals(tl.getManualDate())){
					tl.setStart(null);
				}
				if((TaskState.Planned.equals(tl.getState()) || (TaskState.InProgress.equals(tl.getState()))) &&
						!TaskManualDate.End.equals(tl.getManualDate())){
					tl.setEnd(null);
				}				
			}				
		}
	}
	
	private void calculateGroup(ProjectData projectData){
		
		for(TaskListDataItem tl : projectData.getTaskList()){
			if(ProjectHelper.getChildrenTasks(projectData, tl).isEmpty()){
				tl.setIsGroup(false);
			}
			else{
				tl.setIsGroup(true);
			}
		}
	}
	
	private void calculateCapacity(TaskListDataItem tl){
		if(tl.getWork() != null && tl.getDuration() != null && tl.getDuration() != 0){				
			tl.setCapacity((int)(100 * tl.getWork() / tl.getDuration()));
		}
	}
	private void calculateDurationAndWork(Composite<TaskListDataItem> composite){
		
		Integer duration = 0;
		Double work = 0d;
		for(Composite<TaskListDataItem> component : composite.getChildren()){
			calculateDurationAndWork(component);
			TaskListDataItem taskListDataItem = component.getData();
			if(taskListDataItem.getWork() != null && taskListDataItem.getDuration() != null){
				work += taskListDataItem.getWork();
				duration += taskListDataItem.getDuration();					
			}						
		}
		if(!composite.isRoot()){
			if(!composite.isLeaf()){
				composite.getData().setDuration(ProjectHelper.calculateTaskDuration(composite.getData().getStart(), composite.getData().getEnd()));
				composite.getData().setWork(work);
			}			
			calculateCapacity(composite.getData());
		}			
	}
	
	private void calculateDatesOfParent(TaskListDataItem composite, TaskListDataItem component){
		
		if(composite != null && component != null){
			if(component.getStart() != null && (composite.getStart() == null || composite.getStart().after(component.getStart()))){
				ProjectHelper.setTaskStartDate(composite, component.getStart());
				calculateDatesOfParent(composite.getParent(), composite);
			}
			
			if(component.getEnd() != null && (composite.getEnd() == null || composite.getEnd().before(component.getEnd()))){
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
		
		// check new startDate is equals or after, otherwise we stop since a parallel branch is after			
		if(startDate != null && (nextTask.getStart()==null || nextTask.getStart().equals(startDate) || nextTask.getStart().before(startDate))){
			ProjectHelper.setTaskStartDate(nextTask, startDate);
		}

		if(nextTask.getIsGroup()){
			calculatePlanningOfChildren(projectData, nextTask);
		}
		else if(hasPlannedDuration(nextTask)){			
			Date endDate = ProjectHelper.calculateEndDate(nextTask.getStart(), nextTask.getDuration());
			ProjectHelper.setTaskEndDate(nextTask, endDate);
		}
				
		calculateDatesOfParent(nextTask.getParent(), nextTask);
		
		if (nextTask.getEnd() != null && (projectData.getCompletionDate() == null || projectData.getCompletionDate().before(nextTask.getEnd()))) {
			projectData.setCompletionDate(nextTask.getEnd());
		}

		calculatePlanningOfNextTasks(projectData, nextTask.getNodeRef(), ProjectHelper.calculateNextStartDate(nextTask.getEnd()));
	}
	
	private void calculatePlanningOfChildren(ProjectData projectData, TaskListDataItem taskListDataItem) throws FormulateException{
		
		List<TaskListDataItem> children = ProjectHelper.getChildrenTasks(projectData, taskListDataItem);
		
		for(TaskListDataItem c : children){
			if(c.getPrevTasks().isEmpty()){
				calculatePlanningOfTask(projectData, c, taskListDataItem.getStart());
			}
		}
		
		Date endDate = taskListDataItem.getStart();
		for(TaskListDataItem c : children){
			if(c.getEnd() != null && c.getEnd().after(endDate)){
				endDate = c.getEnd();
			}
		}
		ProjectHelper.setTaskEndDate(taskListDataItem, endDate);		
	}
	
	private void calculateRetroPlanningOfPrevTasks(ProjectData projectData, TaskListDataItem task, Date endDate) throws FormulateException {
		List<TaskListDataItem> prevTasks = null;
		if(task == null){
			prevTasks = ProjectHelper.getLastTasks(projectData);
		}
		else{
			prevTasks = ProjectHelper.getPrevTasks(projectData, task);
		}
		
		for (TaskListDataItem prevTask : prevTasks) {

			// avoid cycle
			checkCycle(prevTask.getNodeRef(), task);
			
			calculateRetroPlanningOfTask(projectData, prevTask, endDate);
		}
	}
	
	private void calculateRetroPlanningOfTask(ProjectData projectData, TaskListDataItem prevTask, Date endDate) throws FormulateException {
		
		if(TaskState.InProgress.equals(prevTask.getState())){
			
			// InProgress => calculate endDate from the startDate and we stop retro-planning
			if(prevTask.getIsGroup()){
				calculatePlanningOfChildren(projectData, prevTask);
			}
			else if(hasPlannedDuration(prevTask)){
				Date d = ProjectHelper.calculateEndDate(prevTask.getStart(), prevTask.getDuration());
				ProjectHelper.setTaskEndDate(prevTask, d);
			}								
		}
		else{
			// check new endDate is equals or before, otherwise we stop since a parallel branch is before
			if(prevTask.getEnd() == null || prevTask.getEnd().equals(endDate) || prevTask.getEnd().after(endDate)){					
				ProjectHelper.setTaskEndDate(prevTask, endDate);
			}
			if(prevTask.getIsGroup()){
				calculateRetroPlanningOfChildren(projectData, prevTask);
			}
			else if(hasPlannedDuration(prevTask)){
				Date startDate = ProjectHelper.calculateStartDate(prevTask.getEnd(), prevTask.getDuration());
				ProjectHelper.setTaskStartDate(prevTask, startDate);
			}				
			if (prevTask.getStart()==null || projectData.getStartDate().after(prevTask.getStart())) {
				projectData.setStartDate(prevTask.getStart());
			}

			calculateRetroPlanningOfPrevTasks(projectData, prevTask, ProjectHelper.calculatePrevEndDate(prevTask.getStart()));			
		}		
		
		calculateDatesOfParent(prevTask.getParent(), prevTask);
	}
	
	private void calculateRetroPlanningOfChildren(ProjectData projectData, TaskListDataItem taskListDataItem) throws FormulateException{
		
		List<TaskListDataItem> children = ProjectHelper.getChildrenTasks(projectData, taskListDataItem);
		
		for(TaskListDataItem c : children){
			if(ProjectHelper.getNextTasks(projectData, c.getNodeRef()).isEmpty()){
				calculateRetroPlanningOfTask(projectData, c, taskListDataItem.getEnd());
			}
		}
		
		Date startDate = taskListDataItem.getEnd();
		for(TaskListDataItem c : children){
			if(c.getStart() != null && c.getStart().before(startDate)){
				startDate = c.getStart();
			}
		}
		ProjectHelper.setTaskStartDate(taskListDataItem, startDate);
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
			if(o != null && (initOverdue || o.compareTo(taskOverdue) < 0)){
				taskOverdue += o;
				initOverdue = false;
			}			

			o = calculateOverdue(projectData, nextTask.getNodeRef());
			if(o != null && (initNextOverdue || o.compareTo(nextTaskOverdue) < 0)){
				nextTaskOverdue +=o;
				initNextOverdue = false;
			}					
		}
				
		return taskOverdue + nextTaskOverdue;
	}
	
	private void checkCycle(NodeRef taskNodeRef, TaskListDataItem nextTask) throws FormulateException{
		
		if (taskNodeRef != null && nextTask != null && taskNodeRef.equals(nextTask.getNodeRef())) {
			String error = "cycle detected. taskNodeRef: " + taskNodeRef + " nextTask: " + nextTask.getNodeRef() + " nextTask name: " + nextTask.getTaskName();
			logger.error(error);
			throw new FormulateException(error);
		}
	}

	private boolean hasPlannedDuration(TaskListDataItem task){
		
		if(task.getDuration() != null || task.getIsMilestone()){
			return true;		
		}
		else{
			return false;
		}
	}
}

