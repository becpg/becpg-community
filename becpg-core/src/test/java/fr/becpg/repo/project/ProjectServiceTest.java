/*
 * 
 */
package fr.becpg.repo.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.BeCPGListDao;
import fr.becpg.repo.project.data.AbstractProjectData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectTplData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ProjectServiceTest.
 * 
 * @author quere
 */
public class ProjectServiceTest extends RepoBaseTestCase {

	protected static final String USER_ONE = "matthieuWF";
	protected static final String USER_TWO = "philippeWF";

	private static Log logger = LogFactory.getLog(ProjectServiceTest.class);

	@Resource
	private BeCPGListDao<AbstractProjectData> projectDAO;
	@Resource
	protected MutableAuthenticationService authenticationService;
	@Resource
	protected PersonService personService;
	@Resource(name="WorkflowService")
	protected WorkflowService workflowService;

	private NodeRef userOne;
	private NodeRef userTwo;

	private void initUsers() {

		userOne = createUser(USER_ONE);
		userTwo = createUser(USER_TWO);
	}

	private NodeRef createUser(String userName) {
		if (this.authenticationService.authenticationExists(userName) == false) {
			this.authenticationService.createAuthentication(userName, "PWD".toCharArray());

			PropertyMap ppOne = new PropertyMap(4);
			ppOne.put(ContentModel.PROP_USERNAME, userName);
			ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
			ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
			ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
			ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

			return this.personService.createPerson(ppOne);
		} else {
			return personService.getPerson(userName);
		}
	}

	@Test
	public void testSubmitTask() {

		final NodeRef projectTplNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						initUsers();

						Collection<QName> dataLists = new ArrayList<QName>();
						dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
						dataLists.add(ProjectModel.TYPE_TASK_LIST);

						List<NodeRef> assigneesOne = new ArrayList<NodeRef>();
						assigneesOne.add(userOne);
						List<NodeRef> assigneesTwo = new ArrayList<NodeRef>();
						assigneesTwo.add(userTwo);

						ProjectTplData projectTplData = new ProjectTplData(null, "Pjt Tpl");

						List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
						taskList.add(new TaskListDataItem(null, false, 2, "activiti$projectAdhoc", taskLegends.get(0), tasks
								.get(0), null, assigneesOne));
						List<NodeRef> prevTasks = new ArrayList<NodeRef>();
						prevTasks.add(tasks.get(0));
						taskList.add(new TaskListDataItem(null, false, 2, "activiti$projectAdhoc", taskLegends.get(0), tasks
								.get(1), prevTasks, assigneesOne));
						prevTasks = new ArrayList<NodeRef>();
						prevTasks.add(tasks.get(1));
						taskList.add(new TaskListDataItem(null, false, 2, "activiti$projectAdhoc", taskLegends.get(0), tasks
								.get(2), prevTasks, assigneesOne));
						prevTasks = new ArrayList<NodeRef>();
						prevTasks.add(tasks.get(2));
						taskList.add(new TaskListDataItem(null, false, 3, "activiti$projectAdhoc", taskLegends.get(1), tasks
								.get(3), prevTasks, assigneesTwo));
						prevTasks = new ArrayList<NodeRef>();
						prevTasks.add(tasks.get(2));
						taskList.add(new TaskListDataItem(null, false, 2, "activiti$projectAdhoc", taskLegends.get(1), tasks
								.get(4), prevTasks, assigneesTwo));
						prevTasks = new ArrayList<NodeRef>();
						prevTasks.add(tasks.get(3));
						prevTasks.add(tasks.get(4));
						taskList.add(new TaskListDataItem(null, true, null, null, taskLegends.get(2), tasks.get(5),
								prevTasks, assigneesTwo));
						projectTplData.setTaskList(taskList);

						List<DeliverableListDataItem> deliverableList = new ArrayList<DeliverableListDataItem>();
						deliverableList.add(new DeliverableListDataItem(null, tasks.get(0), null, "Deliveray descr 1", 100, null));
						deliverableList.add(new DeliverableListDataItem(null, tasks.get(1), null, "Deliveray descr 2.1", 30, null));
						deliverableList.add(new DeliverableListDataItem(null, tasks.get(1), null, "Deliveray descr 2.2", 70, null));
						deliverableList.add(new DeliverableListDataItem(null, tasks.get(2), null, "Deliveray descr 3", 100, null));
						projectTplData.setDeliverableList(deliverableList);

						return projectDAO.create(testFolderNodeRef, projectTplData, dataLists);
					}
				}, false, true);

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						Collection<QName> dataLists = new ArrayList<QName>();

						ProjectData projectData = new ProjectData(null, "Pjt 1", projectTplNodeRef);
						projectData.setHierarchy1(PROJECT_HIERARCHY1_PAIN);

						return projectDAO.create(testFolderNodeRef, projectData, dataLists);
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_HISTORY_LIST);

				ProjectTplData projectTplData = (ProjectTplData) projectDAO.find(projectTplNodeRef, dataLists);

				assertNotNull(projectTplData);
				assertNotNull(projectTplData.getTaskList());
				assertEquals(6, projectTplData.getTaskList().size());
				assertNotNull(projectTplData.getDeliverableList());
				assertEquals(4, projectTplData.getDeliverableList().size());

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertNotNull(projectData.getDeliverableList());
				assertEquals(1, projectData.getDeliverableList().size());
				assertNotNull(projectData.getTaskHistoryList());
				assertEquals(1, projectData.getTaskHistoryList().size());

				// submit task 1st task
				projectData.getTaskHistoryList().get(0).setState(TaskState.Completed);
				projectDAO.update(projectNodeRef, projectData, dataLists);

				return null;
			}
		}, false, true);

		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// check
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_HISTORY_LIST);

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertNotNull(projectData.getDeliverableList());
				assertEquals(3, projectData.getDeliverableList().size());
				assertNotNull(projectData.getTaskHistoryList());
				assertEquals(2, projectData.getTaskHistoryList().size());
				
				// check completion percent of task 2
				logger.debug("projectData.getTaskHistoryList().get(1).getCompletionPercent(): " + projectData.getTaskHistoryList().get(1).getCompletionPercent());
				assertEquals(0, projectData.getTaskHistoryList().get(1).getCompletionPercent().intValue());
				
				// submit deliverable 2
				logger.debug("submit task with completionPercent: " + projectData.getDeliverableList().get(1).getCompletionPercent());
				projectData.getDeliverableList().get(1).setState(DeliverableState.Completed);
				projectDAO.update(projectNodeRef, projectData, dataLists);
				
				return null;
			}
		}, false, true);
				
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_HISTORY_LIST);

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				// check completion percent of task 2 is 30%
				assertEquals(30, projectData.getTaskHistoryList().get(1).getCompletionPercent().intValue());
				assertEquals(TaskState.InProgress, projectData.getTaskHistoryList().get(1).getState());
				
				//submit by workflow
				String workflowInstanceId = projectData.getTaskHistoryList().get(1).getWorkflowInstance();
				assertNotNull(workflowInstanceId);
				
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
				
				List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery);
				logger.debug("tasks in progress size: " + tasks.size());
				for(WorkflowTask task : tasks){
					workflowService.endTask(task.getId(), null);
				}
				
				tasks = workflowService.queryTasks(taskQuery);
				logger.debug("tasks 2 in progress: " + tasks.size());
				for(WorkflowTask task : tasks){
					workflowService.endTask(task.getId(), null);
				}

				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_HISTORY_LIST);

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				// check task 2 is Completed
				assertEquals(TaskState.Completed, projectData.getTaskHistoryList().get(1).getState());
				
				// check task 3 is InProgress
				assertEquals(TaskState.InProgress, projectData.getTaskHistoryList().get(2).getState());

				return null;
			}
		}, false, true);
	}
}
