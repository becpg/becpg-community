/*
 * 
 */
package fr.becpg.repo.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.BeCPGListDao;
import fr.becpg.repo.helper.AssociationService;
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
	@Resource(name = "WorkflowService")
	protected WorkflowService workflowService;
	@Resource
	protected AssociationService associationService;

	@Resource
	protected ProjectService projectService;

	private NodeRef userOne;
	private NodeRef userTwo;
	private List<NodeRef> assigneesOne;
	private List<NodeRef> assigneesTwo;
	private NodeRef rawMaterialNodeRef;
	private NodeRef projectTplNodeRef;

	private void initTest() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				userOne = createUser(USER_ONE);
				userTwo = createUser(USER_TWO);

				assigneesOne = new ArrayList<NodeRef>();
				assigneesOne.add(userOne);
				assigneesTwo = new ArrayList<NodeRef>();
				assigneesTwo.add(userTwo);

				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);

				ProjectTplData projectTplData = new ProjectTplData(null, "Pjt Tpl");

				List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();
				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task3", false, 2, null, assigneesOne, taskLegends.get(0),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task4", false, 2, null, assigneesTwo, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(2),
						"activiti$projectAdhoc"));
				projectTplData.setTaskList(taskList);

				List<DeliverableListDataItem> deliverableList = new LinkedList<DeliverableListDataItem>();
				deliverableList.add(new DeliverableListDataItem(null, null, null, "Deliveray descr 1", 100, null));
				deliverableList.add(new DeliverableListDataItem(null, null, null, "Deliveray descr 2.1", 30, null));
				deliverableList.add(new DeliverableListDataItem(null, null, null, "Deliveray descr 2.2", 70, null));
				deliverableList.add(new DeliverableListDataItem(null, null, null, "Deliveray descr 3", 100, null));
				projectTplData.setDeliverableList(deliverableList);

				projectTplNodeRef = projectDAO.create(testFolderNodeRef, projectTplData, dataLists);

				// update a second time to manage prevTask
				// TODO : should avoid to save twice
				projectTplData = (ProjectTplData) projectDAO.find(projectTplNodeRef, dataLists);
				List<NodeRef> prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(0).getNodeRef());
				projectTplData.getTaskList().get(1).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(1).getNodeRef());
				projectTplData.getTaskList().get(2).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(3).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(4).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(3).getNodeRef());
				prevTasks.add(projectTplData.getTaskList().get(4).getNodeRef());
				projectTplData.getTaskList().get(5).setPrevTasks(prevTasks);

				projectTplData.getDeliverableList().get(0).setTask(projectTplData.getTaskList().get(0).getNodeRef());
				projectTplData.getDeliverableList().get(1).setTask(projectTplData.getTaskList().get(1).getNodeRef());
				projectTplData.getDeliverableList().get(2).setTask(projectTplData.getTaskList().get(1).getNodeRef());
				projectTplData.getDeliverableList().get(3).setTask(projectTplData.getTaskList().get(2).getNodeRef());

				projectDAO.update(projectTplNodeRef, projectTplData, dataLists);

				return null;
			}
		}, false, true);
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
	public void testProjectAspectOnEntity() {

		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						Collection<QName> dataLists = new ArrayList<QName>();

						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, new Date(),
								null, null, 2, projectTplNodeRef, 0, rawMaterialNodeRef);

						return projectDAO.create(testFolderNodeRef, projectData, dataLists);
					}
				}, false, true);

		assertTrue(nodeService.hasAspect(rawMaterialNodeRef, ProjectModel.ASPECT_PROJECT_ASPECT));
		assertEquals(projectNodeRef, associationService.getTargetAssoc(rawMaterialNodeRef, ProjectModel.ASSOC_PROJECT));
	}

	@Test
	public void testSubmitTask() {

		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						Collection<QName> dataLists = new ArrayList<QName>();

						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, new Date(),
								null, null, 2, projectTplNodeRef, 0, rawMaterialNodeRef);

						return projectDAO.create(testFolderNodeRef, projectData, dataLists);
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);

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
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(0).getState());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getState());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(2).getState());
				assertNotNull(projectData.getDeliverableList());
				assertEquals(4, projectData.getDeliverableList().size());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(0).getState());
				assertEquals(DeliverableState.Planned, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.Planned, projectData.getDeliverableList().get(2).getState());

				// submit task 1st task
				projectData.getDeliverableList().get(0).setState(DeliverableState.Completed);
				projectData.getTaskList().get(0).setState(TaskState.Completed);
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

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(TaskState.Completed, projectData.getTaskList().get(0).getState());
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getState());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(2).getState());
				assertNotNull(projectData.getDeliverableList());
				assertEquals(4, projectData.getDeliverableList().size());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(0).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(2).getState());
				assertEquals(DeliverableState.Planned, projectData.getDeliverableList().get(3).getState());

				// check completion percent of task 2
				assertEquals(0, projectData.getTaskList().get(1).getCompletionPercent().intValue());

				// submit deliverable 2
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

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				// check completion percent of task 2 is 30%
				assertEquals(30, projectData.getTaskList().get(1).getCompletionPercent().intValue());
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getState());

				// submit by workflow
				String workflowInstanceId = projectData.getTaskList().get(1).getWorkflowInstance();
				assertNotNull(workflowInstanceId);

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery);
				logger.debug("tasks in progress size: " + tasks.size());
				for (WorkflowTask task : tasks) {
					workflowService.endTask(task.getId(), null);
				}

				tasks = workflowService.queryTasks(taskQuery);
				logger.debug("tasks 2 in progress: " + tasks.size());
				for (WorkflowTask task : tasks) {
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

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				// check task 2 is Completed
				assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());

				// check task 3 is InProgress
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

				// reopen deliverables
				projectData.getDeliverableList().get(1).setState(DeliverableState.InProgress);
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

				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				// Create new task -> difficult. Right now, we reopen the task
				// InProgress
				// // check task 2
				// assertEquals(TaskState.Completed,
				// projectData.getTaskList().get(1).getState());
				// assertEquals(DeliverableState.InProgress,
				// projectData.getDeliverableList().get(1).getState());
				// assertEquals(DeliverableState.Completed,
				// projectData.getDeliverableList().get(2).getState());
				//
				// //check that reopen deliverable add a new task
				// assertEquals(7, projectData.getTaskList().size());
				// assertEquals(projectData.getTaskList().get(1).getTaskName(),
				// projectData.getTaskList().get(6).getTaskName());
				// assertEquals(TaskState.InProgress,
				// projectData.getTaskList().get(6).getState());
				// logger.debug("### task duration: " +
				// projectData.getTaskList().get(6).getDuration());
				//
				// // check task 3,4 is InProgress
				// assertEquals(TaskState.InProgress,
				// projectData.getTaskList().get(2).getState());
				// assertEquals(DeliverableState.InProgress,
				// projectData.getDeliverableList().get(3).getState());

				// check task 2
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());
				assertEquals(2 * 1, 3, projectData.getTaskList().get(1).getDuration());

				// check task 3,4 is InProgress
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

				return null;
			}
		}, false, true);
	}

	@Test
	public void testProjectModuleInfo() {

		NodeRef legendNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						ChildAssociationRef assocRef = nodeService.createNode(testFolderNodeRef,
								ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ProjectModel.TYPE_TASK_LEGEND);

						return assocRef.getChildRef();
					}
				}, false, true);

		Assert.assertNotNull(projectService.getProjectsContainer(null));
		Assert.assertTrue(projectService.getTaskLegendList().size() > 0);
		Assert.assertTrue(projectService.getTaskLegendList().contains(legendNodeRef));

	}
}
