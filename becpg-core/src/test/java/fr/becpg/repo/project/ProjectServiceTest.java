/*
 * 
 */
package fr.becpg.repo.project;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.data.AbstractProjectData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.ProjectTplData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
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
	private AlfrescoRepository<AbstractProjectData> alfrescoRepository;
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
	@Resource
	protected EntityTplService entityTplService;

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

				// create documents in tpl folder
				NodeRef folderTplNodeRef = entityTplService.getFolderTpl(ProjectModel.TYPE_PROJECT);
				assertNotNull(folderTplNodeRef);
				NodeRef subFolder = nodeService.getChildByName(folderTplNodeRef, ContentModel.ASSOC_CONTAINS,
						"SubFolder");
				if(subFolder == null){
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "SubFolder");
					subFolder = nodeService.createNode(folderTplNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "SubFolder"),
							ContentModel.TYPE_FOLDER, properties).getChildRef();
				}
				
				NodeRef doc1NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS,
						"Doc1");
				if(doc1NodeRef == null){					
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Doc1");
					doc1NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc1"), ContentModel.TYPE_CONTENT, properties)
							.getChildRef();
				}

				NodeRef doc2NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS,
						"Doc2");
				if(doc2NodeRef == null){
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Doc2");
					doc2NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc2"), ContentModel.TYPE_CONTENT, properties)
							.getChildRef();
				}				

				ProjectTplData projectTplData = new ProjectTplData(null, "Pjt Tpl");

				List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();

				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task3", false, 2, null, assigneesOne, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task4", false, 2, null, assigneesTwo, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesTwo, taskLegends.get(2),
						"activiti$projectAdhoc"));
				projectTplData.setTaskList(taskList);

				projectTplData.setParentNodeRef(testFolderNodeRef);
				projectTplData = (ProjectTplData) alfrescoRepository.save(projectTplData);

				projectTplNodeRef = projectTplData.getNodeRef();

//				Project:
//					Task1	-> Task2	-> Task3	->	Task5	-> Task6
//													-> 	Task4	
						
				// update a second time to manage prevTask
				// TODO : should avoid to save twice
				projectTplData = (ProjectTplData) alfrescoRepository.findOne(projectTplData.getNodeRef());
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

				List<DeliverableListDataItem> deliverableList = new LinkedList<DeliverableListDataItem>();
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(0).getNodeRef(),
						null, "Deliveray descr 1", 100, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(1).getNodeRef(),
						null, "Deliveray descr 2.1", 30, doc2NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(1).getNodeRef(),
						null, "Deliveray descr 2.2", 70, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(2).getNodeRef(),
						null, "Deliveray descr 3", 100, null));
				projectTplData.setDeliverableList(deliverableList);

				alfrescoRepository.save(projectTplData);

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

						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, new Date(),
								null, null, 2, ProjectState.Planned, projectTplNodeRef, 0, rawMaterialNodeRef);

						projectData.setParentNodeRef(testFolderNodeRef);

						projectData = (ProjectData) alfrescoRepository.save(projectData);
						return projectData.getNodeRef();
					}
				}, false, true);

		assertTrue(nodeService.hasAspect(rawMaterialNodeRef, ProjectModel.ASPECT_PROJECT_ASPECT));
		assertEquals(projectNodeRef, associationService.getTargetAssoc(rawMaterialNodeRef, ProjectModel.ASSOC_PROJECT));
	}

	/**
	 * Test a project create InProgress start automatically
	 */
	@Test
	public void testCreateProjectInProgress() {

		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, new Date(),
								null, null, 2, ProjectState.InProgress, projectTplNodeRef, 0, rawMaterialNodeRef);

						projectData.setParentNodeRef(testFolderNodeRef);

						projectData = (ProjectData) alfrescoRepository.save(projectData);
						return projectData.getNodeRef();
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

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

				return null;
			}
		}, false, true);
	}

	/**
	 * Test a project can be cancelled (and workflow)
	 */
	@Test
	public void testCancelProject() {

		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, new Date(),
								null, null, 2, ProjectState.InProgress, projectTplNodeRef, 0, rawMaterialNodeRef);

						projectData.setParentNodeRef(testFolderNodeRef);

						projectData = (ProjectData) alfrescoRepository.save(projectData);
						return projectData.getNodeRef();
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check workflow instance is active
				assertEquals(true,
						workflowService.getWorkflowById(projectData.getTaskList().get(0).getWorkflowInstance())
								.isActive());

				// cancel project and check wf are not active
				logger.debug("projectData.getTaskList().get(0).getWorkflowInstance(): "
						+ projectData.getTaskList().get(0).getWorkflowInstance());
				projectService.cancel(projectNodeRef);
				assertEquals(null,
						workflowService.getWorkflowById(projectData.getTaskList().get(0).getWorkflowInstance()));

				return null;
			}
		}, false, true);
	}

	@Test
	public void testSubmitTask() {

		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, new Date(),
								null, null, 2, ProjectState.InProgress, projectTplNodeRef, 0, rawMaterialNodeRef);

						projectData.setParentNodeRef(testFolderNodeRef);

						projectData = (ProjectData) alfrescoRepository.save(projectData);

						projectService.formulate(projectData.getNodeRef());
						return projectData.getNodeRef();
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectTplData projectTplData = (ProjectTplData) alfrescoRepository.findOne(projectTplNodeRef);

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
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// check
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertNotNull(projectData.getLegends());
				assertEquals(1, projectData.getLegends().size());
				assertTrue(projectData.getLegends().contains(taskLegends.get(0)));
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
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check completion percent of task 2 is 30%
				assertEquals(30, projectData.getTaskList().get(1).getCompletionPercent().intValue());
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getState());

				// submit by workflow
				String workflowInstanceId = projectData.getTaskList().get(1).getWorkflowInstance();
				assertNotNull(workflowInstanceId);

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, false);
				logger.debug("tasks in progress size: " + tasks.size());
				for (WorkflowTask task : tasks) {
					workflowService.endTask(task.getId(), null);
				}

				tasks = workflowService.queryTasks(taskQuery, false);
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

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				
				// legend
				assertNotNull(projectData.getLegends());
				assertEquals(1, projectData.getLegends().size());
				assertTrue(projectData.getLegends().contains(taskLegends.get(1)));

				// check task 2 is Completed
				assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());

				// check task 3 is InProgress
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

				// reopen deliverables
				nodeService.setProperty(projectData.getDeliverableList().get(1).getNodeRef(),
						ProjectModel.PROP_DL_STATE, DeliverableState.InProgress);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				
				// legend
				assertNotNull(projectData.getLegends());
				assertEquals(2, projectData.getLegends().size());
				assertTrue(projectData.getLegends().contains(taskLegends.get(0)));
				assertTrue(projectData.getLegends().contains(taskLegends.get(1)));

				// check task 2
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());

				// check task 3 is InProgress
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

				return null;
			}
		}, false, true);
	}

	@Test
	public void testCalculateNextDate() throws ParseException {

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("15/11/2012");

		assertEquals(dateFormat.parse("15/11/2012"), ProjectHelper.calculateEndDate(date, 1));
		assertEquals(dateFormat.parse("16/11/2012"), ProjectHelper.calculateEndDate(date, 2));
		assertEquals(dateFormat.parse("19/11/2012"), ProjectHelper.calculateEndDate(date, 3));
		assertEquals(dateFormat.parse("20/11/2012"), ProjectHelper.calculateEndDate(date, 4));

		assertEquals(dateFormat.parse("16/11/2012"), ProjectHelper.calculateNextStartDate(date));
		assertEquals(dateFormat.parse("19/11/2012"),
				ProjectHelper.calculateNextStartDate(dateFormat.parse("16/11/2012")));
	}
	
	@Test
	public void testCalculateTaskDuration() throws ParseException {

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		assertEquals(1,
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("15/11/2012")));
		assertEquals(2,
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("16/11/2012")));
		assertEquals(3,
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("19/11/2012")));
	}

	@Test
	public void testCalculatePlanningDates() {

		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						Date startDate = dateFormat.parse("15/11/2012");
						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, startDate,
								null, null, 2, ProjectState.Planned, projectTplNodeRef, 0, rawMaterialNodeRef);
						projectData.setParentNodeRef(testFolderNodeRef);

						projectData = (ProjectData) alfrescoRepository.save(projectData);
						logger.info("Create : " + projectData.toString());

						return projectData.getNodeRef();
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				logger.info("Load : " + projectData.toString());

				// check initialization
				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(0).getStart());
				assertEquals(dateFormat.parse("16/11/2012"), projectData.getTaskList().get(0).getEnd());
				assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(1).getStart());
				assertEquals(dateFormat.parse("20/11/2012"), projectData.getTaskList().get(1).getEnd());
				assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(2).getStart());
				assertEquals(dateFormat.parse("22/11/2012"), projectData.getTaskList().get(2).getEnd());
				assertEquals(dateFormat.parse("23/11/2012"), projectData.getTaskList().get(3).getStart());
				assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(3).getEnd());
				assertEquals(dateFormat.parse("23/11/2012"), projectData.getTaskList().get(4).getStart());
				assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(4).getEnd());
				assertEquals(dateFormat.parse("27/11/2012"), projectData.getTaskList().get(5).getStart());
				assertEquals(dateFormat.parse("28/11/2012"), projectData.getTaskList().get(5).getEnd());

				// modify some tasks
				projectData.setStartDate(dateFormat.parse("19/11/2012"));
				projectData.getTaskList().get(1).setDuration(4);
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);

				// check
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(0).getStart());
				assertEquals(dateFormat.parse("20/11/2012"), projectData.getTaskList().get(0).getEnd());
				assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(1).getStart());
				assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(1).getEnd());
				assertEquals(dateFormat.parse("27/11/2012"), projectData.getTaskList().get(2).getStart());
				assertEquals(dateFormat.parse("28/11/2012"), projectData.getTaskList().get(2).getEnd());

				// start project
				logger.debug("###start project");
				projectData.setProjectState(ProjectState.InProgress);
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(0).getStart());

				// submit 1st task
				logger.debug("###submit 1st task. current state: " + projectData.getTaskList().get(0).getState());
				projectData.getTaskList().get(0).setState(TaskState.Completed);
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(0).getEnd());
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(1).getStart());

				// submit 2nd task
				logger.debug("submit 2nd task");
				projectData.getTaskList().get(1).setState(TaskState.Completed);
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(1).getEnd());
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(2).getStart());

				// submit 3rd task
				logger.debug("submit 3rd task");
				projectData.getTaskList().get(2).setState(TaskState.Completed);
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(2).getEnd());
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(3).getStart());

				// submit 4th task
				logger.debug("submit 4th task");
				projectData.getTaskList().get(3).setState(TaskState.Completed);
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				dataLists.add(ProjectModel.TYPE_TASK_LIST);

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(3).getEnd());
				assertEquals(ProjectHelper.removeTime(new Date()), projectData.getTaskList().get(4).getStart());

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
	
	@Test
	public void testInitDeliverables() {

		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, new Date(), null,
						null, 2, ProjectState.Planned, projectTplNodeRef, 0, null);

				projectData.setParentNodeRef(testFolderNodeRef);
				projectData = (ProjectData) alfrescoRepository.save(projectData);

				return projectData.getNodeRef();
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef subFolder = nodeService.getChildByName(projectNodeRef, ContentModel.ASSOC_CONTAINS,
						"SubFolder");
				assertNotNull(subFolder);

				NodeRef doc1NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS, "Doc1");
				assertNotNull(doc1NodeRef);
				NodeRef doc2NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS, "Doc2");
				assertNotNull(doc2NodeRef);

				int checks = 0;
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				for (DeliverableListDataItem dl : projectData.getDeliverableList()) {
					if (dl.getDescription().equals("Deliveray descr 1")) {
						assertNotNull(dl.getContent());
						assertEquals(doc1NodeRef, dl.getContent());
						checks++;
					} else if (dl.getDescription().equals("Deliveray descr 2.1")) {
						assertNotNull(dl.getContent());
						assertEquals(doc2NodeRef, dl.getContent());
						checks++;
					}
					if (dl.getDescription().equals("Deliveray descr 2.2")) {
						assertNotNull(dl.getContent());
						assertEquals(doc1NodeRef, dl.getContent());
						checks++;
					}
					if (dl.getDescription().equals("Deliveray descr 3")) {
						assertNull(dl.getContent());
						checks++;
					}
				}
				assertEquals(4, checks);

				return null;
			}
		}, false, true);

	}
	
	@Test
	public void testCalculatePrevDate() throws ParseException {

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("15/11/2012");

		assertEquals(dateFormat.parse("15/11/2012"), ProjectHelper.calculateStartDate(date, 1));
		assertEquals(dateFormat.parse("14/11/2012"), ProjectHelper.calculateStartDate(date, 2));
		assertEquals(dateFormat.parse("13/11/2012"), ProjectHelper.calculateStartDate(date, 3));
		assertEquals(dateFormat.parse("12/11/2012"), ProjectHelper.calculateStartDate(date, 4));
		assertEquals(dateFormat.parse("9/11/2012"), ProjectHelper.calculateStartDate(date, 5));

		assertEquals(dateFormat.parse("14/11/2012"), ProjectHelper.calculatePrevEndDate(date));
		assertEquals(dateFormat.parse("9/11/2012"),
				ProjectHelper.calculatePrevEndDate(dateFormat.parse("12/11/2012")));
	}
	
	@Test
	public void testCalculateRetroPlanningDates() {

		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		initTest();

		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						Date startDate = dateFormat.parse("15/11/2012");
						rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_PAIN, null,
								startDate, null, 2, ProjectState.Planned, projectTplNodeRef, 0, rawMaterialNodeRef);
						projectData.setParentNodeRef(testFolderNodeRef);

						projectData = (ProjectData) alfrescoRepository.save(projectData);
						logger.info("Create : " + projectData.toString());

						return projectData.getNodeRef();
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				logger.info("Load : " + projectData.toString());
				
//				Project:
//				Task1	-> Task2	-> Task3	->	Task5	-> Task6
//												-> 	Task4

				// check initialization
				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(dateFormat.parse("2/11/2012"), projectData.getTaskList().get(0).getStart());
				assertEquals(dateFormat.parse("5/11/2012"), projectData.getTaskList().get(0).getEnd());
				assertEquals(dateFormat.parse("6/11/2012"), projectData.getTaskList().get(1).getStart());
				assertEquals(dateFormat.parse("7/11/2012"), projectData.getTaskList().get(1).getEnd());
				assertEquals(dateFormat.parse("8/11/2012"), projectData.getTaskList().get(2).getStart());
				assertEquals(dateFormat.parse("9/11/2012"), projectData.getTaskList().get(2).getEnd());
				assertEquals(dateFormat.parse("12/11/2012"), projectData.getTaskList().get(3).getStart());
				assertEquals(dateFormat.parse("13/11/2012"), projectData.getTaskList().get(3).getEnd());
				assertEquals(dateFormat.parse("12/11/2012"), projectData.getTaskList().get(4).getStart());
				assertEquals(dateFormat.parse("13/11/2012"), projectData.getTaskList().get(4).getEnd());
				assertEquals(dateFormat.parse("14/11/2012"), projectData.getTaskList().get(5).getStart());
				assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(5).getEnd());
				

				return null;
			}
		}, false, true);
	}
}
