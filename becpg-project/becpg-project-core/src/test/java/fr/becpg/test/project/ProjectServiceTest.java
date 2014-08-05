/*
 * 
 */
package fr.becpg.test.project;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.project.policy.ProjectPolicy;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.data.EntityTestData;

/**
 * The Class ProjectServiceTest.
 * 
 * @author quere
 */
public class ProjectServiceTest extends AbstractProjectTestCase {	

	private static Log logger = LogFactory.getLog(ProjectServiceTest.class);
	
	@Resource
	private ProjectPolicy projectPolicy;
	
	@Resource
	private ProjectWorkflowService projectWorkflowService;
	
	@Resource
	private CopyService copyService;
	
	@Resource private PersonService personService;

	/**
	 * Test a project create InProgress start automatically
	 */
	@Test
	public void testCreateProjectInProgress() {

		createProject(ProjectState.InProgress, new Date(), null);

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

		createProject(ProjectState.InProgress, new Date(), null);

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
	
	/**
	 * Test a project can be deleted (and workflow)
	 */
	@Test
	public void testDeleteProject() {

		createProject(ProjectState.InProgress, new Date(), null);

		final String workflowInstanceId = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>() {
			@Override
			public String execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check workflow instance is active
				assertEquals(true,
						workflowService.getWorkflowById(projectData.getTaskList().get(0).getWorkflowInstance())
								.isActive());

				// cancel project and check wf are not active
				logger.debug("projectData.getTaskList().get(0).getWorkflowInstance(): "
						+ projectData.getTaskList().get(0).getWorkflowInstance());
				nodeService.deleteNode(projectNodeRef);

				return projectData.getTaskList().get(0).getWorkflowInstance();
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				assertEquals(null, workflowService.getWorkflowById(workflowInstanceId));

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
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("15/11/2012")).intValue());
		assertEquals(2,
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("16/11/2012")).intValue());
		assertEquals(3,
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("19/11/2012")).intValue());
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

		Assert.assertNull(projectService.getProjectsContainer(null));
		Assert.assertTrue(projectService.getTaskLegendList().size() > 0);
		Assert.assertTrue(projectService.getTaskLegendList().contains(legendNodeRef));
	}
	
	@Test
	public void testInitDeliverables() throws InterruptedException {

		createProject(ProjectState.Planned, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				projectPolicy.initializeNodeRefsAfterCopy(projectNodeRef);

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
	public void testCalculateRetroPlanningDates() throws ParseException {

		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		createProject(ProjectState.OnHold, null, dateFormat.parse("15/11/2012"));

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
	
	@Test
	public void testProjectOneTask() {

		
		final NodeRef projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						
						// create project Tpl
						ProjectData projectData = new ProjectData(null, "Pjt", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, 
								null, null, null, PlanningMode.Planning, null, null, null, 0, null);
						
						// create datalists
						List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();
						taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0),
								"activiti$projectAdhoc"));
						projectData.setTaskList(taskList);
						
						projectData.setParentNodeRef(testFolderNodeRef);
						projectData = (ProjectData) alfrescoRepository.save(projectData);
						
						// start
						projectData.setProjectState(ProjectState.InProgress);
						projectData = (ProjectData) alfrescoRepository.save(projectData);
						
						return projectData.getNodeRef();
					}
				}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						
						ProjectData projectData = (ProjectData)alfrescoRepository.findOne(projectNodeRef);
						assertEquals(ProjectState.InProgress, projectData.getProjectState());
						return null;
					}
				}, false, true);

	}
		
	/**
	 * Test the calculation of scoring
	 */
	@Test
	public void testCalculateScoring() {

		createProject(ProjectState.Planned, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertNotNull(projectData.getScoreList());
				assertEquals(5, projectData.getScoreList().size());
				for(int i=0;i<5;i++){
					assertEquals("Criterion" + i, projectData.getScoreList().get(i).getCriterion());
					assertEquals(i*10, projectData.getScoreList().get(i).getWeight().intValue());
				}
				
//		coef		10		2	
//				0	0		0		0
//				1	10		2		20
//				2	20		4		80
//				3	30		6		180
//				4	40		8		320
//		somme		100				6
				
				
				for(int i=0;i<5;i++){
					projectData.getScoreList().get(i).setScore(i*2);
				}
				
				alfrescoRepository.save(projectData);

				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);				
				assertEquals(6, projectData.getScore().intValue());							

				return null;
			}
		}, false, true);
	}	
	
	/**
	 * Test the project state calculation
	 * This project has 2 task in //, both must be completed in order project is completed
	 */
	@Test
	public void testProjectState(){

		
		projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {						


						EntityTestData entityTestData = new EntityTestData();
						entityTestData.setParentNodeRef(testFolderNodeRef);
						entityTestData.setName("Entity 1");
						
						alfrescoRepository.save(entityTestData);
						List<NodeRef> productNodeRefs = new ArrayList<>(1);
						productNodeRefs.add(entityTestData.getNodeRef());
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, new Date(),
								null, null, PlanningMode.Planning, 2, ProjectState.InProgress, null, 0, productNodeRefs);

						projectData.setParentNodeRef(testFolderNodeRef);
						
						// create datalists
						List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();

						taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0),
								"activiti$projectAdhoc"));
						taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0),
								"activiti$projectAdhoc"));
						
						projectData.setTaskList(taskList);
						projectData = (ProjectData) alfrescoRepository.save(projectData);
						return projectData.getNodeRef();
					}
				}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertEquals(ProjectState.InProgress, projectData.getProjectState());					
				
				projectData.getTaskList().get(0).setState(TaskState.Completed);
				alfrescoRepository.save(projectData); 
				projectService.formulate(projectNodeRef);	
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertEquals(ProjectState.InProgress, projectData.getProjectState());
				
				projectData.getTaskList().get(1).setState(TaskState.Completed);
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertEquals(ProjectState.Completed, projectData.getProjectState());

				return null;
			}
		}, false, true);		
	}
	
	@Test
	public void testTaskWorkflow() {

		createProject(ProjectState.Planned, null, null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {

			@Override
			public NodeRef execute() throws Throwable {

				List<WorkflowTask> assignedWorkflowTasks1 = workflowService.getAssignedTasks(BeCPGTestHelper.USER_ONE, WorkflowTaskState.IN_PROGRESS);
				
				// Inprogress
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				projectData.setProjectState(ProjectState.InProgress);
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);
				
				// check assigned tasks
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertTrue(projectWorkflowService.isWorkflowActive(projectData.getTaskList().get(0)));				
				List<WorkflowTask> assignedWorkflowTasks2 = workflowService.getAssignedTasks(BeCPGTestHelper.USER_ONE, WorkflowTaskState.IN_PROGRESS);													
				assertEquals(1, (assignedWorkflowTasks2.size() - assignedWorkflowTasks1.size()));
				
				// replan 1st task
				projectData.getTaskList().get(0).setState(TaskState.OnHold);
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);
				
				// check pooled tasks
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertEquals("", projectData.getTaskList().get(0).getWorkflowInstance());				
				List<WorkflowTask> pooledWorkflowTasks1 = workflowService.getPooledTasks(BeCPGTestHelper.USER_TWO);

				// start 1st task
				projectData.getTaskList().get(0).setState(TaskState.InProgress);
				// test multiple assignments
				projectData.getTaskList().get(0).getResources().add(personService.getPerson(BeCPGTestHelper.USER_TWO));
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);
				
				// check
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertTrue(projectWorkflowService.isWorkflowActive(projectData.getTaskList().get(0)));				
				List<WorkflowTask> pooledWorkflowTasks2 = workflowService.getPooledTasks(BeCPGTestHelper.USER_TWO);													
				assertEquals(1, (pooledWorkflowTasks2.size() - pooledWorkflowTasks1.size()));
				
				return null;
			}
		}, false, true);
		
	}
	
	@Test
	public void testCopyProject() {

		createProject(ProjectState.InProgress, null, null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>() {
			@Override
			public String execute() throws Throwable {

				// check
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertEquals(ProjectState.InProgress, projectData.getProjectState());
				assertNotSame("", projectData.getTaskList().get(0).getWorkflowInstance());
				assertTrue(projectWorkflowService.isWorkflowActive(projectData.getTaskList().get(0)));
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(0).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(0).getState());

				return null;
			}

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {

			@Override
			public NodeRef execute() throws Throwable {

				NodeRef copiedProjectNodeRef = copyService.copy(projectNodeRef, testFolderNodeRef,
						ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);				
				
				// check
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(copiedProjectNodeRef);
				assertEquals(ProjectState.Planned, projectData.getProjectState());				
				assertEquals(TaskState.Planned, projectData.getTaskList().get(0).getState());
				assertNull(projectData.getTaskList().get(0).getWorkflowInstance());
				assertEquals(DeliverableState.Planned, projectData.getDeliverableList().get(0).getState());
				
				return null;
				
			}
		}, false, true);		
	}
	
	@Test
	public void testWorkflowProperitesSynchronization() {

		createProject(ProjectState.InProgress, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check workflow instance is active and workflow props
				TaskListDataItem taskListDataItem = projectData.getTaskList().get(0);
				String workflowInstanceId = projectData.getTaskList().get(0).getWorkflowInstance();
				assertEquals(true, workflowService.getWorkflowById(workflowInstanceId).isActive());				
				checkWorkflowProperties(workflowInstanceId, taskListDataItem.getNodeRef(),
						"Pjt 1 - task1", taskListDataItem.getEnd(), taskListDataItem.getResources());
				
				// modify WF props (duration and add a resource)
				logger.info("modify WF props");
				taskListDataItem.setTaskName("task1 modified");
				taskListDataItem.setDuration(3);
				taskListDataItem.getResources().add(userTwo);
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);
				
				// check workflow props
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				TaskListDataItem taskListDataItemDB = projectData.getTaskList().get(0);
				assertEquals(taskListDataItem.getTaskName(), taskListDataItemDB.getTaskName());
				assertEquals(taskListDataItem.getDuration(), taskListDataItemDB.getDuration());
				assertEquals(taskListDataItem.getResources(), taskListDataItemDB.getResources());
				assertEquals(2, taskListDataItemDB.getResources().size());
				checkWorkflowProperties(workflowInstanceId, taskListDataItemDB.getNodeRef(),
						"Pjt 1 - task1 modified", taskListDataItemDB.getEnd(), taskListDataItemDB.getResources());				

				return null;
			}
		}, false, true);
	}
	
	private void checkWorkflowProperties(String workflowInstance, NodeRef taskListDataItemNodeRef,
			String workflowDescription, Date dueDate, List<NodeRef> assignees){
		
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstance);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);
		
		for (WorkflowTask workflowTask : workflowTasks) {
			NodeRef taskNodeRef  = (NodeRef)workflowTask.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK);			
			if (taskNodeRef != null && taskNodeRef.equals(taskListDataItemNodeRef)) {
				assertEquals(workflowDescription, workflowTask.getProperties().get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
				assertEquals(dueDate, workflowTask.getProperties().get(WorkflowModel.PROP_DUE_DATE));
				if(assignees.size() == 1){					
					assertEquals(nodeService.getProperty(assignees.get(0), ContentModel.PROP_USERNAME), workflowTask.getProperties().get(ContentModel.PROP_OWNER));
				}else{
					assertEquals(assignees, workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS));
				}				
			}
		}
	}
	
	@Test
	public void testTaskListOrDeliverableListDeleted() {

		createProject(ProjectState.InProgress, new Date(), null);

		final String finalWorkflowInstanceId = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>() {
			@Override
			public String execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check workflow instance is active and workflow props
				TaskListDataItem taskListDataItem = projectData.getTaskList().get(0);
				String workflowInstanceId = projectData.getTaskList().get(0).getWorkflowInstance();
				assertEquals(true, workflowService.getWorkflowById(workflowInstanceId).isActive());				
				checkWorkflowProperties(workflowInstanceId, taskListDataItem.getNodeRef(),
						"Pjt 1 - task1", taskListDataItem.getEnd(), taskListDataItem.getResources());
				
				// delete deliverable
				logger.info("Delete DL");
				nodeService.deleteNode(projectData.getDeliverableList().get(0).getNodeRef());
				
				return workflowInstanceId;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check workflow instance is active and workflow props
				TaskListDataItem taskListDataItem = projectData.getTaskList().get(0);
				String workflowInstanceId = projectData.getTaskList().get(0).getWorkflowInstance();
				assertEquals(true, workflowService.getWorkflowById(workflowInstanceId).isActive());				
				checkWorkflowProperties(workflowInstanceId, taskListDataItem.getNodeRef(),
						"Pjt 1 - task1", taskListDataItem.getEnd(), taskListDataItem.getResources());
				
				// delete task
				logger.info("Delete task");
				nodeService.deleteNode(projectData.getTaskList().get(0).getNodeRef());
				
				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				assertNull(workflowService.getWorkflowById(finalWorkflowInstanceId));				
				
				return null;
			}
		}, false, true);
	}
}
