package fr.becpg.repo.project.formulation;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
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

		clearDates(projectData);
		
		if(projectData.getDueDate() == null){
			// planning
			if (projectData.getStartDate() == null) {
				projectData.setStartDate(new Date());
			}
			projectData.setCompletionDate(projectData.getStartDate());
			calculatePlanning(projectData, null, projectData.getStartDate());
		}
		else{
			//retro-planning
			projectData.setStartDate(projectData.getDueDate());
			calculateRetroPlanning(projectData, null, projectData.getStartDate());
		}
		
		Integer projectOverdue = calculateOverdue(projectData, null);
		logger.debug("projectOverdue: " + projectOverdue);
		projectData.setOverdue(projectOverdue);
		
		return true;
	}
	
	private void clearDates(ProjectData projectData){
		
		for(TaskListDataItem tl : projectData.getTaskList()){
			if(TaskState.Planned.equals(tl.getState()) &&
					!TaskManualDate.Start.equals(tl.getManualDate())){
				tl.setStart(null);
			}
			if((TaskState.Planned.equals(tl.getState()) || (TaskState.InProgress.equals(tl.getState()))) &&
					!TaskManualDate.End.equals(tl.getManualDate())){
				tl.setEnd(null);
			}
		}
	}
	
	private void calculatePlanning(ProjectData projectData, NodeRef taskNodeRef, Date startDate) throws FormulateException {

		for (TaskListDataItem nextTask : ProjectHelper.getNextTasks(projectData, taskNodeRef)) {

			// avoid cycle
			checkCycle(taskNodeRef, nextTask);
						
			logger.debug("nextTask " + nextTask.getTaskName() + " - startDate " + startDate);

			// check new startDate is equals or after, otherwise we stop since a parallel branch is after			
			if(nextTask.getStart()==null || nextTask.getStart().equals(startDate) || nextTask.getStart().before(startDate)){
				ProjectHelper.setTaskStartDate(nextTask, startDate);
			}

			Date endDate = ProjectHelper.calculateEndDate(nextTask.getStart(), nextTask.getDuration());
			ProjectHelper.setTaskEndDate(nextTask, endDate);
			if (projectData.getCompletionDate().before(nextTask.getEnd())) {
				projectData.setCompletionDate(nextTask.getEnd());
			}

			calculatePlanning(projectData, nextTask.getNodeRef(), ProjectHelper.calculateNextStartDate(nextTask.getEnd()));			
		}
	}
	
	private void calculateRetroPlanning(ProjectData projectData, TaskListDataItem task, Date endDate) throws FormulateException {

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
			
			if(TaskState.InProgress.equals(prevTask.getState())){
				
				// InProgress => calculate endDate from the startDate and we stop retro-planning
				Date d = ProjectHelper.calculateEndDate(prevTask.getStart(), prevTask.getDuration());
				ProjectHelper.setTaskEndDate(prevTask, d);
			}
			else{
				// check new endDate is equals or before, otherwise we stop since a parallel branch is before
				if(prevTask.getEnd() == null || prevTask.getEnd().equals(endDate) || prevTask.getEnd().after(endDate)){					
					ProjectHelper.setTaskEndDate(prevTask, endDate);
				}
				
				Date startDate = ProjectHelper.calculateStartDate(prevTask.getEnd(), prevTask.getDuration());
				ProjectHelper.setTaskStartDate(prevTask, startDate);
				if (projectData.getStartDate().after(prevTask.getStart())) {
					projectData.setStartDate(prevTask.getStart());
				}

				calculateRetroPlanning(projectData, prevTask, ProjectHelper.calculatePrevEndDate(prevTask.getStart()));			
			}			
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

	
}

