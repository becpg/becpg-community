/*
 * 
 */
package fr.becpg.test.project.formulation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.LogTimeListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectMultiLevelPlanningTest.
 * 
 * @author quere
 */
public class ProjectBudgetTasksTest extends AbstractProjectTestCase {	

	private static Log logger = LogFactory.getLog(ProjectBudgetTasksTest.class);
	private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");	
	
	@Test
	public void testCalculatePlanningDates() throws ParseException {		

		final NodeRef projectNodeRef  =  createMultiLevelProject(ProjectState.OnHold, dateFormat.parse("15/11/2012"), null, PlanningMode.Planning);		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				
				for(TaskListDataItem tl : projectData.getTaskList()){
					if(tl.getTaskName().equals("task2")){
						tl.setWork(8d);
						tl.setResourceCost(resourceCost);
						tl.setFixedCost(3000d);
						
						projectData.getLogTimeList().add(new LogTimeListDataItem(dateFormat.parse("15/11/2012"), 8d, tl.getNodeRef()));
						projectData.getLogTimeList().add(new LogTimeListDataItem(dateFormat.parse("16/11/2012"), 4d, tl.getNodeRef()));
					}
					if(tl.getTaskName().equals("task3")){
						tl.setWork(16d);
						tl.setResourceCost(resourceCost);
						
						projectData.getLogTimeList().add(new LogTimeListDataItem(dateFormat.parse("17/11/2012"), 3d, tl.getNodeRef()));
					}
					if(tl.getTaskName().equals("task5")){
						tl.setWork(24d);
						tl.setResourceCost(resourceCost);
						tl.setFixedCost(1000d);
					}
					if(tl.getTaskName().equals("task6")){
						tl.setWork(null);
						tl.setResourceCost(resourceCost);
						tl.setFixedCost(4000d);
					}
				}											
				
				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);

				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				int checks = 0;
				for (TaskListDataItem tl : projectData.getTaskList()) {	
					logger.info("Task " + tl.getTaskName() + " planned cost : " + tl.getBudgetedCost() + " work " + tl.getWork());
					
					if (tl.getTaskName().equals("task2")) {
						assertEquals(8d*RESOURCE_COST_VALUE+3000d, tl.getBudgetedCost());
						assertEquals(12d, tl.getLoggedTime());
						checks++;
					}
					if (tl.getTaskName().equals("task3")) {
						assertEquals(16d*RESOURCE_COST_VALUE, tl.getBudgetedCost());
						assertEquals(3d, tl.getLoggedTime());
					}
					if (tl.getTaskName().equals("task5")) {
						assertEquals(24d*RESOURCE_COST_VALUE+1000d, tl.getBudgetedCost());
					}
					if (tl.getTaskName().equals("task6")) {
						assertEquals(4000d, tl.getBudgetedCost());
					}
					if (tl.getTaskName().equals("task1")) {
						assertEquals(8d*RESOURCE_COST_VALUE+3000d+16d*RESOURCE_COST_VALUE, tl.getBudgetedCost());
					}
					if (tl.getTaskName().equals("task4")) {
						assertEquals(24d*RESOURCE_COST_VALUE+1000d+4000d, tl.getBudgetedCost());
					}
				}
				
				assertEquals(6, checks);
				assertEquals(12800d, projectData.getBudgetedCost());			
				assertEquals(48d, projectData.getWork());
				assertEquals(15d, projectData.getLoggedTime());

				return null;
			}
		}, false, true);
	}
}
