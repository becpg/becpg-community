package fr.becpg.repo.project.formulation;

import java.util.Date;

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

		if (projectData.getStartDate() == null) {
			projectData.setStartDate(new Date());
		}
		projectData.setCompletionDate(projectData.getStartDate());
		calculateTaskDates(projectData, null, projectData.getStartDate());

		return true;
	}
	
	private void calculateTaskDates(ProjectData projectData, NodeRef taskNodeRef, Date startDate) {

		for (TaskListDataItem nextTask : ProjectHelper.getNextTasks(projectData, taskNodeRef)) {

			// avoid cycle
			if (taskNodeRef != null && nextTask.getNodeRef() != null && taskNodeRef.equals(nextTask.getNodeRef())) {
				logger.error("cycle detected on task " + nextTask.getTaskName());
				return;
			}

			ProjectHelper.setTaskStartDate(nextTask, startDate, false);

			Date endDate = ProjectHelper.calculateEndDate(nextTask.getStart(), nextTask.getDuration());
			ProjectHelper.setTaskEndDate(nextTask, endDate, false);
			if (projectData.getCompletionDate().before(endDate)) {
				projectData.setCompletionDate(endDate);
			}

			calculateTaskDates(projectData, nextTask.getNodeRef(), ProjectHelper.calculateNextStartDate(endDate));
		}
	}

	
}
