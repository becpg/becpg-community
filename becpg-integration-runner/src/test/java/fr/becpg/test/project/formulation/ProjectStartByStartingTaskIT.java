/*
 *
 */
package fr.becpg.test.project.formulation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectCalculatePlanningDatesTest.
 *
 * @author quere
 */
public class ProjectStartByStartingTaskIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectStartByStartingTaskIT.class);

	/**
	 * Test the project is InProgress if we set a task InProgress
	 */
	@Test
	public void testStartProjectByStartingTask() {

		logger.debug("testStartProjectByStartingTask");

		final NodeRef projectNodeRef = createProject(ProjectState.Planned, null, null);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertNotNull(projectData);
			assertNotNull(projectData.getTaskList());
			assertEquals(6, projectData.getTaskList().size());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getTaskState());

			projectData.getTaskList().get(0).setTaskState(TaskState.InProgress);
			alfrescoRepository.save(projectData);

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertNotNull(projectData);
			assertNotNull(projectData.getTaskList());
			assertEquals(6, projectData.getTaskList().size());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getTaskState());
			assertEquals(ProjectState.InProgress, projectData.getProjectState());

			return null;
		}, false, true);
	}
}
