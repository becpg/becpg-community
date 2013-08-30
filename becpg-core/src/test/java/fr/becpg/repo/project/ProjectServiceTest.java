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
import org.alfresco.model.ForumModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.formulation.PlanningFormulationHandler;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.project.policy.ProjectPolicy;
import fr.becpg.test.BeCPGTestHelper;

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

		initTest();
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

		initTest();
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

		initTest();
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
	public void testSubmitTask() {

		initTest();
		createProject(ProjectState.InProgress, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

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

		
		final Date task2EndDate = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Date>() {
			@Override
			public Date execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check completion percent of task 2 is 30%
				assertEquals(30, projectData.getTaskList().get(1).getCompletionPercent().intValue());
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getState());
				Date date = projectData.getTaskList().get(1).getEnd();

				// submit by workflow
				String workflowInstanceId = projectData.getTaskList().get(1).getWorkflowInstance();
				assertNotNull(workflowInstanceId);

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(workflowInstanceId);
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, false);
				logger.debug("tasks in progress size: " + tasks.size());
				for (WorkflowTask task : tasks) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(WorkflowModel.PROP_COMMENT, "Comment submited by WF!");
					workflowService.updateTask(task.getId(), properties, null, null);
					workflowService.endTask(task.getId(), null);
				}

				tasks = workflowService.queryTasks(taskQuery, false);
				logger.debug("tasks 2 in progress: " + tasks.size());
				for (WorkflowTask task : tasks) {
					workflowService.endTask(task.getId(), null);
				}

				return date;
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
				// check real end date is before estimated end date
				assertTrue(projectData.getTaskList().get(1).getEnd().before(task2EndDate));				

				// check task 3 is InProgress
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

				// reopen deliverables
				nodeService.setProperty(projectData.getDeliverableList().get(1).getNodeRef(),
						ProjectModel.PROP_DL_STATE, DeliverableState.InProgress);
				
				// check comment has been added to project
				Integer commentCount = (Integer)nodeService.getProperty(projectNodeRef, ForumModel.PROP_COMMENT_COUNT);
				assertEquals(new Integer(1), commentCount);

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

				// submit deliverable 2
				projectData.getDeliverableList().get(1).setState(DeliverableState.Completed);
				projectData.getTaskList().get(1).setState(TaskState.Completed);
				alfrescoRepository.save(projectData);
				
				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);								

				// check task 2
				assertEquals(TaskState.Completed, projectData.getTaskList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.Completed, projectData.getDeliverableList().get(2).getState());

				// check task 3 is InProgress
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(2).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(3).getState());

				// reopen task 2
				projectData.getTaskList().get(1).setState(TaskState.InProgress);
				alfrescoRepository.save(projectData);
				
				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);								

				// check task 2
				assertEquals(TaskState.InProgress, projectData.getTaskList().get(1).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(1).getState());
				assertEquals(DeliverableState.InProgress, projectData.getDeliverableList().get(2).getState());

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
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("15/11/2012")).intValue());
		assertEquals(2,
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("16/11/2012")).intValue());
		assertEquals(3,
				ProjectHelper.calculateTaskDuration(dateFormat.parse("15/11/2012"), dateFormat.parse("19/11/2012")).intValue());
	}

	@Test
	public void testCalculatePlanningDates() throws ParseException {

		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		initTest();
		createProject(ProjectState.OnHold, dateFormat.parse("15/11/2012"), null);

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
	public void testInitDeliverables() throws InterruptedException {

		initTest();
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
		initTest();
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
						ProjectData projectData = new ProjectData(null, "Pjt", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, null,
										null, null, null, null, null, 0, null);
						
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
	 * Test the project is InProgress if we set a task InProgress
	 */
	@Test
	public void testStartProjectByStartingTask() {

		initTest();
		createProject(ProjectState.Planned, null, null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(0).getState());
				assertEquals(TaskState.Planned, projectData.getTaskList().get(1).getState());
				
				projectData.getTaskList().get(0).setState(TaskState.InProgress);
				alfrescoRepository.save(projectData);

				return null;
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

		initTest();
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
	 * Test the calculation of the project overdue
	 */
	@Test
	public void testProjectOverdue() {

		initTest();
		createProject(ProjectState.Planned, new Date(), null);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				assertNotNull(projectData);
				assertEquals(0, projectData.getOverdue().intValue());
				
				PlanningFormulationHandler planningFormulationHandler = new PlanningFormulationHandler();					
				
				projectData.getTaskList().get(0).setState(TaskState.InProgress);				
				planningFormulationHandler.process(projectData);				
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 1 day after start (task in progress)				
				Date startDate = ProjectHelper.calculateNextDate(new Date(), 1, false);
				projectData.setStartDate(startDate);
				planningFormulationHandler.process(projectData);				
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 3 days after start (task in progress)
				startDate = ProjectHelper.calculateNextDate(new Date(), 3, false);
				projectData.getTaskList().get(0).setStart(startDate);				
				planningFormulationHandler.process(projectData);	
				assertEquals(1, projectData.getOverdue().intValue());
				
				// set start date to simulate 3 days after start (task completed in time)
				startDate = ProjectHelper.calculateNextDate(new Date(), 3, false);			
				Date endDate = ProjectHelper.calculateEndDate(startDate, 2);
				projectData.getTaskList().get(0).setState(TaskState.Completed);
				projectData.getTaskList().get(0).setStart(startDate);	
				projectData.getTaskList().get(0).setEnd(endDate);
				projectData.getTaskList().get(1).setState(TaskState.InProgress);
				planningFormulationHandler.process(projectData);				
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 6 days after start (task4 and task5 in progress)
				startDate = ProjectHelper.calculateNextDate(new Date(), 6, false);			
				projectData.setStartDate(startDate);
				for(TaskListDataItem t : projectData.getTaskList()){
					t.setState(TaskState.Planned);
				}				
				planningFormulationHandler.process(projectData);	
				projectData.getTaskList().get(0).setState(TaskState.Completed);
				projectData.getTaskList().get(1).setState(TaskState.Completed);
				projectData.getTaskList().get(2).setState(TaskState.Completed);
				projectData.getTaskList().get(3).setState(TaskState.InProgress);
				projectData.getTaskList().get(4).setState(TaskState.InProgress);	
				planningFormulationHandler.process(projectData);
				assertEquals(0, projectData.getOverdue().intValue());
				
				// set start date to simulate 9 days after start (task4 and task5 in progress)
				startDate = ProjectHelper.calculateNextDate(new Date(), 9, false);			
				projectData.setStartDate(startDate);
				for(TaskListDataItem t : projectData.getTaskList()){
					t.setState(TaskState.Planned);
				}
				planningFormulationHandler.process(projectData);	
				projectData.getTaskList().get(0).setState(TaskState.Completed);
				projectData.getTaskList().get(1).setState(TaskState.Completed);
				projectData.getTaskList().get(2).setState(TaskState.Completed);
				projectData.getTaskList().get(3).setState(TaskState.InProgress);
				projectData.getTaskList().get(4).setState(TaskState.InProgress);
				planningFormulationHandler.process(projectData);
				assertEquals(1, projectData.getOverdue().intValue());

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

		initTest();
		
		projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						rawMaterialNodeRef = BeCPGTestHelper.createRawMaterial(testFolderNodeRef, "Raw material");
						ProjectData projectData = new ProjectData(null, "Pjt 1", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, new Date(),
								null, null, 2, ProjectState.InProgress, null, 0, rawMaterialNodeRef);

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

		initTest();
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

		initTest();
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

		initTest();
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
						"Pjt 1 - task1 : Deliveray descr 1", taskListDataItem.getEnd(), taskListDataItem.getResources());
				
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
						"Pjt 1 - task1 modified : Deliveray descr 1", taskListDataItemDB.getEnd(), taskListDataItemDB.getResources());				

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

		initTest();
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
						"Pjt 1 - task1 : Deliveray descr 1", taskListDataItem.getEnd(), taskListDataItem.getResources());
				
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
