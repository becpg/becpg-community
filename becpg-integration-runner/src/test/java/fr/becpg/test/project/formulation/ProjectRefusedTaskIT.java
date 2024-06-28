/*
 *
 */
package fr.becpg.test.project.formulation;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 *
 * @author matthieu
 */
public class ProjectRefusedTaskIT extends AbstractProjectTestCase {
	@Test
	public void testRefusedTask() {

		final NodeRef projectNodeRef = createProject(ProjectState.InProgress, new Date(), null);

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(TaskState.InProgress, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(2).getTaskState());

			projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			// check
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());

			// submit deliverable 2
			projectData.getTaskList().get(2).setTaskState(TaskState.Refused);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			// check
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.Refused, projectData.getTaskList().get(2).getTaskState());

			projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());

			projectData.getTaskList().get(2).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(3).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(4).setTaskState(TaskState.Completed);
			projectData.getTaskList().get(5).setTaskState(TaskState.Refused);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			// check
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(4).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(3).getTaskState());
			assertEquals(TaskState.Refused, projectData.getTaskList().get(5).getTaskState());
			projectData.getTaskList().get(2).setTaskState(TaskState.Completed);

			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			// check
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(2).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(3).getTaskState());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(4).getTaskState());
			assertEquals(TaskState.Refused, projectData.getTaskList().get(5).getTaskState());

			projectData.getTaskList().get(4).setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			// check
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(2).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(4).getTaskState());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(3).getTaskState());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(5).getTaskState());

			return null;
		});

	}
}
