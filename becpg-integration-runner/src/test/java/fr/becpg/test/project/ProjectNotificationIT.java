/*
 *
 */
package fr.becpg.test.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * The Class ProjectNotificationTest.
 *
 * @author quere
 */
public class ProjectNotificationIT extends AbstractProjectTestCase {

	/**
	 * Test observers get notifications
	 */
	@Test
	public void testNotification() {

		final NodeRef projectNodeRef = createProject(ProjectState.Planned, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			List<NodeRef> observerNodeRefs = new ArrayList<>();
			observerNodeRefs.add(userOne);
			observerNodeRefs.add(userTwo);
			projectData.getTaskList().get(0).setObservers(observerNodeRefs);
			projectData.getTaskList().get(0).setTaskState(TaskState.InProgress);

			alfrescoRepository.save(projectData);

			return null;
		}, false, true);
	}
}
