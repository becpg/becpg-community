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

		if(projectData.getDueDate() == null){
			// planning
			if (projectData.getStartDate() == null) {
				projectData.setStartDate(new Date());
			}
			projectData.setCompletionDate(projectData.getStartDate());
			calculateTaskEndDates(projectData, null, projectData.getStartDate());
		}
		else{
			//retro-planning
			projectData.setStartDate(projectData.getDueDate());
			calculateTaskStartDates(projectData, null, projectData.getStartDate());
		}
		

		return true;
	}
	
	private void calculateTaskEndDates(ProjectData projectData, NodeRef taskNodeRef, Date startDate) {

		for (TaskListDataItem nextTask : ProjectHelper.getNextTasks(projectData, taskNodeRef)) {

			// avoid cycle
			if (taskNodeRef != null && nextTask.getNodeRef() != null && taskNodeRef.equals(nextTask.getNodeRef())) {
				logger.error("cycle detected on task " + nextTask.getTaskName());
				return;
			}

			ProjectHelper.setTaskStartDate(nextTask, startDate);

			Date endDate = ProjectHelper.calculateEndDate(nextTask.getStart(), nextTask.getDuration());
			ProjectHelper.setTaskEndDate(nextTask, endDate);
			if (projectData.getCompletionDate().before(endDate)) {
				projectData.setCompletionDate(endDate);
			}

			calculateTaskEndDates(projectData, nextTask.getNodeRef(), ProjectHelper.calculateNextStartDate(endDate));
		}
	}
	
	private void calculateTaskStartDates(ProjectData projectData, TaskListDataItem task, Date endDate) {

		List<TaskListDataItem> prevTasks = null;
		if(task == null){
			prevTasks = ProjectHelper.getLastTasks(projectData);
		}
		else{
			prevTasks = ProjectHelper.getPrevTasks(projectData, task);
		}
		
		for (TaskListDataItem prevTask : prevTasks) {

			// avoid cycle
			if (task != null && prevTask.getNodeRef() != null && prevTask.getNodeRef().equals(task.getNodeRef())) {
				logger.error("cycle detected on task " + prevTask.getTaskName());
				return;
			}

			ProjectHelper.setTaskEndDate(prevTask, endDate);

			Date startDate = ProjectHelper.calculateStartDate(prevTask.getEnd(), prevTask.getDuration());
			ProjectHelper.setTaskStartDate(prevTask, startDate);
			if (projectData.getStartDate().after(endDate)) {
				projectData.setStartDate(startDate);
			}

			calculateTaskStartDates(projectData, prevTask, ProjectHelper.calculatePrevEndDate(startDate));
		}
	}

	
}
