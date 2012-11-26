package fr.becpg.repo.project.impl;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.project.ProjectException;
import fr.becpg.repo.project.ProjectVisitor;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Project visitor to calculate planningDates
 * 
 * @author quere
 * 
 */
public class PlanningVisitor implements ProjectVisitor {

	private static Log logger = LogFactory.getLog(PlanningVisitor.class);

	@Override
	public ProjectData visit(ProjectData projectData) throws ProjectException {

		if (projectData.getStartDate() == null) {
			projectData.setStartDate(new Date());
		}
		projectData.setCompletionDate(projectData.getStartDate());
		calculateTaskDates(projectData, null, projectData.getStartDate());

		return projectData;
	}

	private void calculateTaskDates(ProjectData projectData, NodeRef taskNodeRef, Date startDate) {

		for (TaskListDataItem nextTask : ProjectHelper.getNextTasks(projectData, taskNodeRef)) {

			// avoid cycle
			if (taskNodeRef != null && nextTask.getNodeRef() != null && taskNodeRef.equals(nextTask.getNodeRef())) {
				logger.error("cycle detected on task " + nextTask.getTaskName());
				return;
			}

			// init if startDate is null or startDate of task is before
			// startDate
			if (nextTask.getStart() == null || nextTask.getStart().before(startDate)) {
				ProjectHelper.setTaskStartDate(nextTask, startDate, false);
			}

			Date endDate = ProjectHelper.calculateEndDate(nextTask.getStart(), nextTask.getDuration());
			ProjectHelper.setTaskEndDate(nextTask, endDate, false);
			if (projectData.getCompletionDate().before(endDate)) {
				projectData.setCompletionDate(endDate);
			}

			calculateTaskDates(projectData, nextTask.getNodeRef(), ProjectHelper.calculateNextStartDate(endDate));
		}
	}
}
