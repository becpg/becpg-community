/*
 * 
 */
package fr.becpg.test.project.formulation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectMultiLevelPlanningTest.
 * 
 * @author quere
 */
public class ProjectMultiLevelPlanningTest extends AbstractProjectTestCase {	

	private static Log logger = LogFactory.getLog(ProjectMultiLevelPlanningTest.class);
	private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	protected void createProject(final ProjectState projectState, final Date startDate, final Date endDate, final PlanningMode planningMode) {

		projectNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				ProjectData projectData = new ProjectData(null, "Pjt", PROJECT_HIERARCHY1_SEA_FOOD_REF, PROJECT_HIERARCHY2_CRUSTACEAN_REF, 
						startDate, endDate, null, planningMode, null, null,
						null, 0, null);
				projectData.setParentNodeRef(testFolderNodeRef);				
				
				//multi level tasks
				logger.info("multi level tasks");
				List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();
				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));				
				taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0), "activiti$projectAdhoc"));
				taskList.get(1).setParent(taskList.get(0));
				taskList.add(new TaskListDataItem(null, "task3", false, 2, null, assigneesOne, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.get(2).setParent(taskList.get(0));
				taskList.add(new TaskListDataItem(null, "task4", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(1), "activiti$projectAdhoc"));
				taskList.get(4).setParent(taskList.get(3));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesTwo, taskLegends.get(2), "activiti$projectAdhoc"));
				taskList.get(5).setParent(taskList.get(3));
				projectData.setTaskList(taskList);
					
				projectData = (ProjectData) alfrescoRepository.save(projectData);
				projectNodeRef = projectData.getNodeRef();
				
				// update a second time to manage prevTask
				// TODO : should avoid to save twice
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				List<NodeRef> prevTasks = new ArrayList<NodeRef>();

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectData.getTaskList().get(1).getNodeRef());
				projectData.getTaskList().get(2).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectData.getTaskList().get(2).getNodeRef());
				projectData.getTaskList().get(4).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectData.getTaskList().get(4).getNodeRef());
				projectData.getTaskList().get(5).setPrevTasks(prevTasks);

				alfrescoRepository.save(projectData);
				
				return projectNodeRef;
			}
		}, false, true);
	}
	
	@Test
	public void testCalculatePlanningDates() throws ParseException {		

		createProject(ProjectState.OnHold, dateFormat.parse("15/11/2012"), null, PlanningMode.Planning);		
		final Date today = ProjectHelper.removeTime(new Date());
		final Date nextStartDate = ProjectHelper.calculateNextStartDate(today);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				logger.info("formulate multi level tasks");
				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check initialization
				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(0).getStart());
				assertEquals(dateFormat.parse("20/11/2012"), projectData.getTaskList().get(0).getEnd());
				assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(1).getStart());
				assertEquals(dateFormat.parse("16/11/2012"), projectData.getTaskList().get(1).getEnd());
				assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(2).getStart());
				assertEquals(dateFormat.parse("20/11/2012"), projectData.getTaskList().get(2).getEnd());
				assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(3).getStart());
				assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(3).getEnd());
				assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(4).getStart());
				assertEquals(dateFormat.parse("22/11/2012"), projectData.getTaskList().get(4).getEnd());
				assertEquals(dateFormat.parse("23/11/2012"), projectData.getTaskList().get(5).getStart());
				assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(5).getEnd());

				// modify some tasks
				projectData.getTaskList().get(1).setStart(dateFormat.parse("19/11/2012"));
				projectData.getTaskList().get(1).setDuration(4);
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);

				// check
				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(0).getStart());
				assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(0).getEnd());
				assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(1).getStart());
				assertEquals(dateFormat.parse("22/11/2012"), projectData.getTaskList().get(1).getEnd());
				assertEquals(dateFormat.parse("23/11/2012"), projectData.getTaskList().get(2).getStart());
				assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(2).getEnd());
				assertEquals(dateFormat.parse("27/11/2012"), projectData.getTaskList().get(3).getStart());
				assertEquals(dateFormat.parse("30/11/2012"), projectData.getTaskList().get(3).getEnd());
				assertEquals(dateFormat.parse("27/11/2012"), projectData.getTaskList().get(4).getStart());
				assertEquals(dateFormat.parse("28/11/2012"), projectData.getTaskList().get(4).getEnd());
				assertEquals(dateFormat.parse("29/11/2012"), projectData.getTaskList().get(5).getStart());
				assertEquals(dateFormat.parse("30/11/2012"), projectData.getTaskList().get(5).getEnd());

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				// start project				
//				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
//				projectData.setProjectState(ProjectState.InProgress);
//				alfrescoRepository.save(projectData);
				nodeService.setProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress);

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

				assertEquals(today, projectData.getTaskList().get(0).getStart());
				assertEquals(today, projectData.getTaskList().get(1).getStart());

				// submit task
				projectService.submitTask(projectData.getTaskList().get(1).getNodeRef());

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check				
				assertEquals(today, projectData.getTaskList().get(1).getEnd());
				assertEquals(nextStartDate, projectData.getTaskList().get(2).getStart());

				// submit task
				logger.debug("submit task");
				projectService.submitTask(projectData.getTaskList().get(2).getNodeRef());

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check
				assertEquals(today, projectData.getTaskList().get(2).getStart());
				assertEquals(today, projectData.getTaskList().get(2).getEnd());
				assertEquals(today, projectData.getTaskList().get(0).getEnd());
				assertEquals(nextStartDate, projectData.getTaskList().get(3).getStart());
				assertEquals(nextStartDate, projectData.getTaskList().get(4).getStart());

				// submit task
				logger.debug("submit task");
				projectService.submitTask(projectData.getTaskList().get(4).getNodeRef());

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				// check
				assertEquals(today, projectData.getTaskList().get(4).getStart());
				assertEquals(today, projectData.getTaskList().get(4).getEnd());
				assertEquals(nextStartDate, projectData.getTaskList().get(5).getStart());

				// submit task
				logger.debug("submit task");
				projectService.submitTask(projectData.getTaskList().get(5).getNodeRef());

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
				assertEquals(today, projectData.getTaskList().get(5).getStart());
				assertEquals(today, projectData.getTaskList().get(5).getEnd());
				assertEquals(today, projectData.getTaskList().get(3).getStart());
				assertEquals(today, projectData.getTaskList().get(3).getEnd());

				return null;
			}
		}, false, true);
	}
	
	@Test
	public void testRetroCalculatePlanningDates() throws ParseException {

		createProject(ProjectState.OnHold, null, dateFormat.parse("15/11/2012"), PlanningMode.RetroPlanning);		

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				logger.info("formulate multi level tasks");
				projectService.formulate(projectNodeRef);

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				logger.info("Load : " + projectData.toString());

				// check initialization
				assertNotNull(projectData);
				assertNotNull(projectData.getTaskList());
				assertEquals(6, projectData.getTaskList().size());
				assertEquals(dateFormat.parse("6/11/2012"), projectData.getTaskList().get(0).getStart());
				assertEquals(dateFormat.parse("9/11/2012"), projectData.getTaskList().get(0).getEnd());
				assertEquals(dateFormat.parse("6/11/2012"), projectData.getTaskList().get(1).getStart());
				assertEquals(dateFormat.parse("7/11/2012"), projectData.getTaskList().get(1).getEnd());
				assertEquals(dateFormat.parse("8/11/2012"), projectData.getTaskList().get(2).getStart());
				assertEquals(dateFormat.parse("9/11/2012"), projectData.getTaskList().get(2).getEnd());
				assertEquals(dateFormat.parse("12/11/2012"), projectData.getTaskList().get(3).getStart());
				assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(3).getEnd());
				assertEquals(dateFormat.parse("12/11/2012"), projectData.getTaskList().get(4).getStart());
				assertEquals(dateFormat.parse("13/11/2012"), projectData.getTaskList().get(4).getEnd());
				assertEquals(dateFormat.parse("14/11/2012"), projectData.getTaskList().get(5).getStart());
				assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(5).getEnd());

				return null;
			}
		}, false, true);
	}
}
