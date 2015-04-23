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

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.BudgetListDataItem;
import fr.becpg.repo.project.data.projectList.ExpenseListDataItem;
import fr.becpg.repo.project.data.projectList.InvoiceListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectMultiLevelPlanningTest.
 * 
 * @author rafa
 */
public class ProjectBudgetListTest extends AbstractProjectTestCase {

	private static Log logger = LogFactory.getLog(ProjectBudgetListTest.class);
	private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Test
	public void testCalculateBudgetedExpense() throws ParseException {

		final NodeRef projectNodeRef  =  createMultiLevelProject(ProjectState.OnHold, dateFormat.parse("15/11/2012"), null, PlanningMode.Planning);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				projectData.getBudgetList().add(new BudgetListDataItem(null, 3d, 1d, 1d, 1d, 1d));// 0
				projectData.getBudgetList().add(new BudgetListDataItem(null, 2d, 2d, 2d, 2d, 1d));// 1
				projectData.getBudgetList().add(new BudgetListDataItem(null, 2d, 3d, 3d, 3d, 1d));// 2
				projectData.getBudgetList().add(new BudgetListDataItem(null, 3d, 3d, 3d, 3d, 1d));// 3
				projectData.getBudgetList().get(2).setParent(projectData.getBudgetList().get(1));
				projectData.getBudgetList().get(3).setParent(projectData.getBudgetList().get(1));

				projectData.getInvoiceList().add(new InvoiceListDataItem(projectData.getBudgetList().get(0), 1000d, projectData.getTaskList().get(0)));
				projectData.getInvoiceList().add(new InvoiceListDataItem(projectData.getBudgetList().get(1), 2000d, projectData.getTaskList().get(1)));
				projectData.getInvoiceList().add(new InvoiceListDataItem(projectData.getBudgetList().get(2), 3000d, projectData.getTaskList().get(2)));
				projectData.getInvoiceList().add(new InvoiceListDataItem(projectData.getBudgetList().get(3), 4000d, projectData.getTaskList().get(3)));

				projectData.getExpenseList().add(new ExpenseListDataItem(projectData.getBudgetList().get(0), 100d, projectData.getTaskList().get(0)));
				projectData.getExpenseList().add(new ExpenseListDataItem(projectData.getBudgetList().get(1), 200d, projectData.getTaskList().get(1)));
				projectData.getExpenseList().add(new ExpenseListDataItem(projectData.getBudgetList().get(2), 300d, projectData.getTaskList().get(2)));
				projectData.getExpenseList().add(new ExpenseListDataItem(projectData.getBudgetList().get(3), 400d, projectData.getTaskList().get(3)));

				alfrescoRepository.save(projectData);
				projectService.formulate(projectNodeRef);

				projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

				Composite<BudgetListDataItem> composite = CompositeHelper.getHierarchicalCompoList(projectData.getBudgetList());
				Composite<TaskListDataItem> taskComposite = CompositeHelper.getHierarchicalCompoList(projectData.getTaskList());
				logger.info(composite.toString());
				logger.info(taskComposite.toString());

			
				assertEquals(1000.0, projectData.getBudgetList().get(0).getActualInvoice());
				assertEquals(100.0, projectData.getBudgetList().get(0).getActualExpense());
				assertEquals(7000.0, projectData.getBudgetList().get(1).getActualInvoice());
				assertEquals(700.0, projectData.getBudgetList().get(1).getActualExpense());
				assertEquals(900.0, projectData.getBudgetList().get(0).getProfit());
				
				assertEquals(5000.0, projectData.getTaskList().get(0).getActualInvoice());
				assertEquals(500.0, projectData.getTaskList().get(0).getActualExpense());
				assertEquals(2000.0, projectData.getTaskList().get(1).getActualInvoice());
				assertEquals(200.0, projectData.getTaskList().get(1).getActualExpense());

				return null;
			}
		}, false, true);
	}

}
