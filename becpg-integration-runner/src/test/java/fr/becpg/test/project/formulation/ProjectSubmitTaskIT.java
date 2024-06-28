/*
 *
 */
package fr.becpg.test.project.formulation;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectCalculatePlanningDatesTest.
 *
 * @author quere
 */
public class ProjectSubmitTaskIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectSubmitTaskIT.class);

	@Test
	public void testSubmitTask() {

		final NodeRef projectNodeRef = createProject(ProjectState.InProgress, new Date(), null);

		inWriteTx(() -> {

			ProjectData projectTplData = (ProjectData) alfrescoRepository.findOne(projectTplNodeRef);

			assertNotNull(projectTplData);
			assertNotNull(projectTplData.getTaskList());
			assertEquals(6, projectTplData.getTaskList().size());
			assertNotNull(projectTplData.getDeliverableList());
			assertEquals(4, projectTplData.getDeliverableList().size());

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertNotNull(projectData);
			assertNotNull(projectData.getLegends());
			assertEquals(1, projectData.getLegends().size());
			assertTrue(projectData.getLegends().contains(taskLegends.get(0)));
			assertNotNull(projectData.getTaskList());
			assertEquals(6, projectData.getTaskList().size());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(2).getTaskState());
			assertNotNull(projectData.getDeliverableList());
			assertEquals(4, projectData.getDeliverableList().size());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(0).getState());
			assertEquals(DeliverableState.Planned, projectData.getDeliverableList().get(1).getState());
			assertEquals(DeliverableState.Planned, projectData.getDeliverableList().get(2).getState());

			// submit task 1st task
			projectData.getDeliverableList().get(0).setState(DeliverableState.Completed);
			projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			// check
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertNotNull(projectData);
			assertNotNull(projectData.getLegends());
			assertEquals(1, projectData.getLegends().size());
			assertTrue(projectData.getLegends().contains(taskLegends.get(0)));
			assertNotNull(projectData.getTaskList());
			assertEquals(6, projectData.getTaskList().size());
			assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getTaskState());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getTaskState());
			assertEquals(TaskState.Planned, projectData.getTaskList().get(2).getTaskState());
			assertNotNull(projectData.getDeliverableList());
			assertEquals(4, projectData.getDeliverableList().size());
			assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(0).getState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(1).getState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(2).getState());
			assertEquals(DeliverableState.Planned, projectData.getDeliverableList().get(3).getState());

			// check completion percent of task 2
			assertEquals(0, (int) projectData.getTaskList().get(1).getCompletionPercent());

			// submit deliverable 2
			projectData.getDeliverableList().get(1).setState(DeliverableState.Completed);
			alfrescoRepository.save(projectData);

			return null;
		});

		final Date task2EndDate = inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check completion percent of task 2 is 30%
			assertEquals(30, projectData.getTaskList().get(1).getCompletionPercent().intValue());
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getTaskState());
			Date date = projectData.getTaskList().get(1).getEnd();

			// submit by workflow
			String workflowInstanceId = projectData.getTaskList().get(1).getWorkflowInstance();
			assertNotNull(workflowInstanceId);

			WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
			taskQuery.setProcessId(workflowInstanceId);
			taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

			List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, false);
			logger.debug("tasks in progress size: " + tasks.size());
			for (WorkflowTask task1 : tasks) {
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(WorkflowModel.PROP_COMMENT, "Comment submited by WF!");
				// properties.put(PROP_NPD_ACTION, "createNewProduct");
				workflowService.updateTask(task1.getId(), properties, null, null);
				workflowService.endTask(task1.getId(), null);
			}

			tasks = workflowService.queryTasks(taskQuery, false);
			logger.debug("tasks 2 in progress: " + tasks.size());
			for (WorkflowTask task2 : tasks) {
				workflowService.endTask(task2.getId(), null);
			}

			return date;
		});

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// legend
			assertNotNull(projectData.getLegends());
			assertEquals(1, projectData.getLegends().size());
			assertTrue(projectData.getLegends().contains(taskLegends.get(1)));

			// check task 2 is Completed
			assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getTaskState());
			assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(1).getState());
			assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());
			// check real end date is before estimated end date
			assertTrue(projectData.getTaskList().get(1).getEnd().before(task2EndDate));

			// check task 3 is InProgress
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

			// reopen deliverables
			nodeService.setProperty(projectData.getDeliverableList().get(1).getNodeRef(), ProjectModel.PROP_DL_STATE,
					DeliverableState.InProgress);

			return null;
		});

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// legend
			assertNotNull(projectData.getLegends());
			assertEquals(2, projectData.getLegends().size());
			assertTrue(projectData.getLegends().contains(taskLegends.get(0)));
			assertTrue(projectData.getLegends().contains(taskLegends.get(1)));

			// check task 2
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getTaskState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(1).getState());
			assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());

			// check task 3 is InProgress
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

			// submit deliverable 2
			projectData.getDeliverableList().get(1).setState(DeliverableState.Completed);
			projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check task 2
			assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getTaskState());
			assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(1).getState());
			assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());

			// check task 3 is InProgress
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

			// reopen task 2
			projectData.getTaskList().get(1).setTaskState(TaskState.InProgress);
			alfrescoRepository.save(projectData);

			return null;
		});

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check task 2
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getTaskState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(1).getState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(2).getState());

			// check task 3 is InProgress
			assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());
			assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

			return null;
		});
	}
}
