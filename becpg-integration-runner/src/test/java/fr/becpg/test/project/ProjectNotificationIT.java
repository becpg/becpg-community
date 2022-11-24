/*
 *
 */
package fr.becpg.test.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.subethamail.wiser.WiserMessage;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * The Class ProjectNotificationTest.
 *
 * @author quere
 */
public class ProjectNotificationIT extends AbstractProjectTestCase {

	private static Log logger = LogFactory.getLog(ProjectNotificationIT.class);
	
	@Autowired
	CommentService commentService;
	
	/**
	 * Test observers get notifications
	 * 
	 * @throws MessagingException
	 * @throws InterruptedException
	 */
	@Test
	public void testNotification() throws MessagingException, InterruptedException {

		int nbMail = wiser.getMessages().size();

		final NodeRef projectNodeRef = createProject(ProjectState.Planned, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			List<NodeRef> observerNodeRefs = new ArrayList<>();
			observerNodeRefs.add(observerOne);

			projectData.getTaskList().get(0).setObservers(observerNodeRefs);
			projectData.getTaskList().get(1).setObservers(observerNodeRefs);
			projectData.getTaskList().get(2).setObservers(observerNodeRefs);
			projectData.getTaskList().get(3).setObservers(observerNodeRefs);
			alfrescoRepository.save(projectData);

			return null;
		}, false, true);

		waitForMail(1 + nbMail);

		checkActivity(projectNodeRef, 5);

		logger.info("Nb mails before notification:" + nbMail);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			projectData.getTaskList().get(0).setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);

			return null;
		}, false, true);

		// 2 mails 1 activity

		waitForMail(4 + nbMail);

		checkActivity(projectNodeRef, 6);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			commentService.createComment(projectData.getTaskList().get(0).getNodeRef(), "", "Test comment", false);

			return null;
		}, false, true);

		waitForMail(5 + nbMail);
		checkActivity(projectNodeRef, 7);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			try {
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getTaskState());

				// submit by workflow
				String workflowInstanceId = projectData.getTaskList().get(1).getWorkflowInstance();
				assertNotNull(workflowInstanceId);

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, false);
				logger.debug("tasks in progress size: " + tasks.size());
				for (WorkflowTask task : tasks) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(WorkflowModel.PROP_COMMENT, "Comment submited by WF!");
					workflowService.updateTask(task.getId(), properties, null, null);
					workflowService.endTask(task.getId(), null);
				}

				return null;
			} catch (Exception e) {
				logger.error(e, e);
				throw e;
			}

		}, false, true);

		waitForMail(8 + nbMail);
		checkActivity(projectNodeRef,9);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			try {
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getTaskState());

				// submit by workflow
				String workflowInstanceId = projectData.getTaskList().get(2).getWorkflowInstance();
				assertNotNull(workflowInstanceId);

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, false);
				logger.debug("tasks in progress size: " + tasks.size());
				for (WorkflowTask task : tasks) {
					Map<QName, Serializable> properties = new HashMap<>();
					java.util.Map<QName, List<NodeRef>> assocs = new HashMap<>();
					properties.put(ProjectModel.WORKFLOW_TRANSITION, "refused");
					properties.put(WorkflowModel.PROP_COMMENT, "Refused comment submited by WF!");

					workflowService.updateTask(task.getId(), properties, assocs, null);
					workflowService.endTask(task.getId(), null);
				}

				return null;
			} catch (Exception e) {
				logger.error(e, e);
				throw e;
			}

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			List<NodeRef> observerNodeRefs = new ArrayList<>();
			observerNodeRefs.add(observerOne);

			assertEquals(TaskState.Refused.toString(), projectData.getTaskList().get(2).getState()); 
			assertEquals(TaskState.InProgress.toString(), projectData.getTaskList().get(1).getState());
			alfrescoRepository.save(projectData);

			return null;
		}, false, true);

		waitForMail(11 + nbMail);
		checkActivity(projectNodeRef,11);

		// Resubmit task
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			try {
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getTaskState());

				// submit by workflow
				String workflowInstanceId = projectData.getTaskList().get(1).getWorkflowInstance();
				assertNotNull(workflowInstanceId);

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, false);
				logger.debug("tasks in progress size: " + tasks.size());
				for (WorkflowTask task : tasks) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(WorkflowModel.PROP_COMMENT, "Comment submited by WF!");
					workflowService.updateTask(task.getId(), properties, null, null);
					workflowService.endTask(task.getId(), null);
				}

				return null;
			} catch (Exception e) {
				logger.error(e, e);
				throw e;
			}

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			List<NodeRef> observerNodeRefs = new ArrayList<>();
			observerNodeRefs.add(observerOne);

			assertEquals(TaskState.InProgress.toString(), projectData.getTaskList().get(2).getState());
			assertEquals(TaskState.Completed.toString(), projectData.getTaskList().get(1).getState());
			alfrescoRepository.save(projectData);

			return null;
		}, false, true);

		waitForMail(14 + nbMail);
		
		checkActivity(projectNodeRef, 13);

	}

	private void waitForMail(int nbMail) throws InterruptedException, MessagingException {
		int j = 0;
		while ((wiser.getMessages().size() < nbMail) && (j < 10)) {
			Thread.sleep(2000);
			j++;
		}
		for (WiserMessage message : wiser.getMessages()) {
			logger.info(message.getMimeMessage().getSubject());
		}

		assertEquals(nbMail, wiser.getMessages().size());

	}

	protected void checkActivity(NodeRef entityNodeRef, int size) {

		assertEquals(size, transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> ret = new ArrayList<>();
			NodeRef activityListNodeRef = null;

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
			if (listContainerNodeRef != null) {
				activityListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
			}
			if (activityListNodeRef != null) {
				ret = entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

				ret.forEach(tmp -> {
					logger.info("Data: " + nodeService.getProperty(tmp, BeCPGModel.PROP_ACTIVITYLIST_DATA) + " user: "
							+ nodeService.getProperty(tmp, BeCPGModel.PROP_ACTIVITYLIST_USERID));
				});

			} else {
				logger.error("No activity list");
			}

			return ret;
		}, false, true).size());
	}

}
