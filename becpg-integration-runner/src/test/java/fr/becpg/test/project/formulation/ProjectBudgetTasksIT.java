/*
 *
 */
package fr.becpg.test.project.formulation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.ExpenseListDataItem;
import fr.becpg.repo.project.data.projectList.LogTimeListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectMultiLevelPlanningTest.
 *
 * @author quere
 */
public class ProjectBudgetTasksIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectBudgetTasksIT.class);
	private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Test
	public void testCalculatePlanningDates() throws ParseException {

		final NodeRef projectNodeRef = createMultiLevelProject(ProjectState.OnHold, dateFormat.parse("15/11/2012"),
				null, PlanningMode.Planning);

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			projectData.setExpenseList(new ArrayList<ExpenseListDataItem>());

			for (TaskListDataItem tl1 : projectData.getTaskList()) {
				if (tl1.getTaskName().equals("task2")) {
					tl1.setWork(8d);
					tl1.setResourceCost(resourceCost);
					projectData.getExpenseList().add(new ExpenseListDataItem(null, 3000d, tl1));

					projectData.getLogTimeList().add(new LogTimeListDataItem(dateFormat.parse("15/11/2012"), 8d, tl1));
					projectData.getLogTimeList().add(new LogTimeListDataItem(dateFormat.parse("16/11/2012"), 4d, tl1));
				}
				if (tl1.getTaskName().equals("task3")) {
					tl1.setWork(16d);
					tl1.setResourceCost(resourceCost);

					projectData.getLogTimeList().add(new LogTimeListDataItem(dateFormat.parse("17/11/2012"), 3d, tl1));
				}
				if (tl1.getTaskName().equals("task5")) {
					tl1.setWork(24d);
					tl1.setResourceCost(resourceCost);
					projectData.getExpenseList().add(new ExpenseListDataItem(null, 1000d, tl1));
				}
				if (tl1.getTaskName().equals("task6")) {
					tl1.setWork(null);
					tl1.setResourceCost(resourceCost);
					projectData.getExpenseList().add(new ExpenseListDataItem(null, 4000d, tl1));
				}
			}

			alfrescoRepository.save(projectData);
			projectService.formulate(projectNodeRef);

			projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			int checks = 0;
			for (TaskListDataItem tl2 : projectData.getTaskList()) {
				logger.info("Task " + tl2.getTaskName() + " expense : " + tl2.getExpense() + " work " + tl2.getWork());

				if (tl2.getTaskName().equals("task2")) {
					assertEquals(8d * RESOURCE_COST_VALUE, tl2.getExpense());
					assertEquals(12d, tl2.getLoggedTime());
					checks++;
				}
				if (tl2.getTaskName().equals("task3")) {
					assertEquals(16d * RESOURCE_COST_VALUE, tl2.getExpense());
					assertEquals(3d, tl2.getLoggedTime());
					checks++;
				}
				if (tl2.getTaskName().equals("task5")) {
					assertEquals(24d * RESOURCE_COST_VALUE, tl2.getExpense());
					checks++;
				}
				if (tl2.getTaskName().equals("task6")) {
					assertEquals(null, tl2.getExpense());
					checks++;
				}
				if (tl2.getTaskName().equals("task1")) {
					assertEquals((8d * RESOURCE_COST_VALUE) + (16d * RESOURCE_COST_VALUE), tl2.getExpense());
					checks++;
				}
				if (tl2.getTaskName().equals("task4")) {
					assertEquals(24d * RESOURCE_COST_VALUE, tl2.getExpense());
					checks++;
				}
			}

			assertEquals(6, checks);
			assertEquals(4800d, projectData.getBudgetedCost());
			assertEquals(48d, projectData.getWork());
			assertEquals(15d, projectData.getLoggedTime());
			assertEquals(8d * RESOURCE_COST_BILL_RATE, projectData.getLogTimeList().get(0).getInvoice());
			assertEquals(4d * RESOURCE_COST_BILL_RATE, projectData.getLogTimeList().get(1).getInvoice());
			assertEquals(3d * RESOURCE_COST_BILL_RATE, projectData.getLogTimeList().get(2).getInvoice());

			// completet task 2
			projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			projectData.getTaskList().get(1).setTaskState(TaskState.Completed);
			alfrescoRepository.save(projectData);
			projectService.formulate(projectNodeRef);
			projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			checks = 0;
			for (TaskListDataItem tl3 : projectData.getTaskList()) {
				logger.info("Task " + tl3.getTaskName() + " expense : " + tl3.getExpense() + " work " + tl3.getWork());

				if (tl3.getTaskName().equals("task2")) {
					assertEquals(12d * RESOURCE_COST_VALUE, tl3.getExpense());
					assertEquals(12d, tl3.getLoggedTime());
					checks++;
				}
				if (tl3.getTaskName().equals("task3")) {
					assertEquals(16d * RESOURCE_COST_VALUE, tl3.getExpense());
					assertEquals(3d, tl3.getLoggedTime());
					checks++;
				}
				if (tl3.getTaskName().equals("task5")) {
					assertEquals(24d * RESOURCE_COST_VALUE, tl3.getExpense());
					checks++;
				}
				if (tl3.getTaskName().equals("task6")) {
					assertEquals(null, tl3.getExpense());
					checks++;
				}
				if (tl3.getTaskName().equals("task1")) {
					assertEquals((12d * RESOURCE_COST_VALUE) + (16d * RESOURCE_COST_VALUE), tl3.getExpense());
					checks++;
				}
				if (tl3.getTaskName().equals("task4")) {
					assertEquals(24d * RESOURCE_COST_VALUE, tl3.getExpense());
					checks++;
				}
			}

			assertEquals(6, checks);
			assertEquals(5200d, projectData.getBudgetedCost());
			assertEquals(48d, projectData.getWork());
			assertEquals(15d, projectData.getLoggedTime());

			return null;
		});
	}
}
