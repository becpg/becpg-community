/*
 *
 */
package fr.becpg.test.project.formulation;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectCalculatePlanningDatesTest.
 *
 * @author quere
 */
public class ProjectOverdueIT extends AbstractProjectTestCase {

	@Autowired
	FormulationService<ProjectData> formulationService;

	/**
	 * Test the calculation of the project overdue
	 */
	@Test
	public void testProjectOverdue() {

		final NodeRef projectNodeRef = createProject(ProjectState.Planned, new Date(), null);

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertNotNull(projectData);
			assertEquals(0, projectData.getOverdue().intValue());

			projectData.getTaskList().get(0).setTaskState(TaskState.InProgress);
			formulationService.formulate(projectData);
			assertEquals(0, projectData.getOverdue().intValue());

			// set start date to simulate 1 day after start (task in progress)
			Date startDate = ProjectHelper.calculateNextDate(new Date(), 1, false);
			projectData.setStartDate(startDate);
			formulationService.formulate(projectData);
			assertEquals(0, projectData.getOverdue().intValue());

			// set start date to simulate 3 days after start (task in progress)
			startDate = ProjectHelper.calculateNextDate(new Date(), 3, false);
			projectData.getTaskList().get(0).setStart(startDate);
			formulationService.formulate(projectData);
			assertEquals(1, projectData.getOverdue().intValue());

			// set start date to simulate 3 days after start (task completed in
			// time)
			startDate = ProjectHelper.calculateNextDate(new Date(), 3, false);
			Date endDate = ProjectHelper.calculateEndDate(startDate, 2);
			projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(0).setStart(startDate);
			projectData.getTaskList().get(0).setEnd(endDate);
			projectData.getTaskList().get(1).setTaskState(TaskState.InProgress);
			formulationService.formulate(projectData);
			assertEquals(0, projectData.getOverdue().intValue());

			// set start date to simulate 6 days after start (task4 and task5 in
			// progress)
			startDate = ProjectHelper.calculateNextDate(new Date(), 6, false);
			projectData.setStartDate(startDate);
			for (TaskListDataItem t1 : projectData.getTaskList()) {
				t1.setTaskState(TaskState.Planned);
			}
			formulationService.formulate(projectData);
			projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(2).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(3).setTaskState(TaskState.InProgress);
			projectData.getTaskList().get(4).setTaskState(TaskState.InProgress);
			formulationService.formulate(projectData);
			assertEquals(0, projectData.getOverdue().intValue());

			// set start date to simulate 9 days after start (task4 and task5 in
			// progress)
			startDate = ProjectHelper.calculateNextDate(new Date(), 9, false);
			projectData.getTaskList().get(0).setStart(startDate);
			for (TaskListDataItem t2 : projectData.getTaskList()) {
				t2.setTaskState(TaskState.Planned);
			}
			formulationService.formulate(projectData);
			projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(2).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(3).setTaskState(TaskState.InProgress);
			projectData.getTaskList().get(4).setTaskState(TaskState.InProgress);
			formulationService.formulate(projectData);

			assertEquals(1, projectData.getOverdue().intValue());

			// add a parallel task
			TaskListDataItem task = new TaskListDataItem(null, "Task in parallel", false, 2, null, null, null, null);
			task.setStart(new Date());
			task.setEnd(ProjectHelper.calculateNextDate(new Date(), 5, true));
			task.setTaskState(TaskState.Completed);
			projectData.getTaskList().add(task);
			alfrescoRepository.save(projectData);
			projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			formulationService.formulate(projectData);
			assertEquals(4, projectData.getOverdue().intValue());

			return null;
		});

	}
}
