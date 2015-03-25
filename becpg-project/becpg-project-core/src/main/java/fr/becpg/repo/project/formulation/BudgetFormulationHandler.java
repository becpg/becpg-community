package fr.becpg.repo.project.formulation;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.BudgetListDataItem;
import fr.becpg.repo.project.data.projectList.ExpenseListDataItem;
import fr.becpg.repo.project.data.projectList.InvoiceListDataItem;
import fr.becpg.repo.project.data.projectList.LogTimeListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Calculate budget
 * 
 * @author quere
 * 
 */
public class BudgetFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private final static Log logger = LogFactory.getLog(BudgetFormulationHandler.class);

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		logger.debug("BudgetFormulationHandler");
		clearData(projectData);

		// Les champs Actual Invoice dans Task et Budget
		for (InvoiceListDataItem invoiceList : projectData.getInvoiceList()) {
			BudgetListDataItem budgetListItem = invoiceList.getBudget();
			TaskListDataItem taskListItem = invoiceList.getTask();
			Double addInvoice = invoiceList.getInvoiceAmount() != null ? invoiceList.getInvoiceAmount() : 0d;
			if(budgetListItem!=null){
			budgetListItem.setActualInvoice(budgetListItem.getActualInvoice() + addInvoice);
			}
			if(taskListItem!=null){
			taskListItem.setActualInvoice(taskListItem.getActualInvoice() + addInvoice);
			}
		}
		// Les champs Actual Expense dans Task et Budget
		for (ExpenseListDataItem expenseList : projectData.getExpenseList()) {
			BudgetListDataItem budgetListItem = expenseList.getBudget();
		    TaskListDataItem taskListItem = expenseList.getTask();
			Double addExpense = expenseList.getExpenseAmount() != null ? expenseList.getExpenseAmount() : 0d;
			if(budgetListItem!=null) {
			budgetListItem.setActualExpense(budgetListItem.getActualExpense() + addExpense);
			}
			if(taskListItem!=null){
			taskListItem.setActualExpense(taskListItem.getActualExpense() + addExpense);
			}
		}

		// Hierarchie dans Task List
		Composite<TaskListDataItem> compositeTask = CompositeHelper.getHierarchicalCompoList(projectData.getTaskList());
		calculateCost(projectData, compositeTask);
		calculateLogTime(projectData);
		calculateTaskListActualExpenseParentValue(compositeTask);
		calculateTaskListActualInvoiceParentValue(compositeTask);
		// Hierachie dans Budget List
		Composite<BudgetListDataItem> compositeBugdet = CompositeHelper.getHierarchicalCompoList(projectData.getBudgetList());
		calculateBudgetListActualInvoiceParentValue(compositeBugdet);
		calculateBudgetListActualExpenseParentValue(compositeBugdet);

		// Champs Profit dans Budget
		for (BudgetListDataItem myBudget : projectData.getBudgetList()) {
			myBudget.setProfit(myBudget.getActualInvoice() - myBudget.getActualExpense());
		}

		return true;
	}

	public Double calculateBudgetListActualExpenseParentValue(Composite<BudgetListDataItem> compositeBugdet) {
		Double value = 0d;
		for (Composite<BudgetListDataItem> component : compositeBugdet.getChildren()) {
			value += calculateBudgetListActualExpenseParentValue(component);
		}

		if (!compositeBugdet.isRoot()) {
			if (compositeBugdet.isLeaf()) {
				return compositeBugdet.getData().getActualExpense();
			} else {
				compositeBugdet.getData().setActualExpense(value + compositeBugdet.getData().getActualExpense());
			}
		}
		return value;
	}

	public Double calculateBudgetListActualInvoiceParentValue(Composite<BudgetListDataItem> compositeBugdet) {
		Double value = 0d;
		for (Composite<BudgetListDataItem> component : compositeBugdet.getChildren()) {
			value += calculateBudgetListActualInvoiceParentValue(component);
		}

		if (!compositeBugdet.isRoot()) {
			if (compositeBugdet.isLeaf()) {
				return compositeBugdet.getData().getActualInvoice();
			} else {
				compositeBugdet.getData().setActualInvoice(value + compositeBugdet.getData().getActualInvoice());
			}
		}
		return value;
	}

	private void clearData(ProjectData projectData) {
		for (TaskListDataItem tl : projectData.getTaskList()) {
			tl.setBudgetedCost(null);
			tl.setLoggedTime(null);
			tl.setActualExpense(0d);
			tl.setActualInvoice(0d);
		}
		for (BudgetListDataItem bl : projectData.getBudgetList()) {
			bl.setActualExpense(0d);
			bl.setActualInvoice(0d);
		}

	}

	private void calculateCost(ProjectData projectData, Composite<TaskListDataItem> composite) {
		Double cost = 0d;
		for (Composite<TaskListDataItem> component : composite.getChildren()) {
			calculateCost(projectData, component);
			TaskListDataItem taskListDataItem = component.getData();
			Double taskCost = 0d;

			if (taskListDataItem.getBudgetedCost() != null) {
				// cost are roll-up
				taskCost += taskListDataItem.getBudgetedCost();
			} else {
				if (taskListDataItem.getWork() != null && taskListDataItem.getResourceCost() != null && taskListDataItem.getResourceCost().getValue() != null) {
					taskCost += taskListDataItem.getWork() * taskListDataItem.getResourceCost().getValue();
				}
				// fixed cost are not roll-up
				if (taskListDataItem.getFixedCost() != null) {
					taskCost += taskListDataItem.getFixedCost();
				}
			}
			cost += taskCost;
			taskListDataItem.setBudgetedCost(taskCost == 0d ? null : taskCost);
		}
		if (composite.isRoot()) {
			projectData.setBudgetedCost(cost == 0d ? null : cost);
		} else
			composite.getData().setBudgetedCost(cost == 0d ? null : cost);
	}

	private void calculateLogTime(ProjectData projectData) {
		Map<NodeRef, Double> totalLogTimeMap = new HashMap<>();
		Double totalLogTime = 0d;

		for (LogTimeListDataItem logTime : projectData.getLogTimeList()) {
			Double time = logTime.getTime();
			if (time != null) {
				totalLogTime += time;
				Double totalTime = totalLogTimeMap.get(logTime.getTask());
				if (totalTime == null) {
					totalLogTimeMap.put(logTime.getTask(), time);
				} else {
					totalLogTimeMap.put(logTime.getTask(), totalTime + time);
				}
			}
		}
		for (TaskListDataItem tl : projectData.getTaskList()) {
			tl.setLoggedTime(totalLogTimeMap.get(tl.getNodeRef()));
		}
		projectData.setLoggedTime(totalLogTime);
	}

	public Double calculateTaskListActualExpenseParentValue(Composite<TaskListDataItem> compositeTask) {
		Double value = 0d;
		for (Composite<TaskListDataItem> component : compositeTask.getChildren()) {
			value += calculateTaskListActualExpenseParentValue(component);
		}

		if (!compositeTask.isRoot()) {
			if (compositeTask.isLeaf()) {
				return compositeTask.getData().getActualExpense();
			} else {
				compositeTask.getData().setActualExpense(value + compositeTask.getData().getActualExpense());
			}
		}
		return value;
	}

	public Double calculateTaskListActualInvoiceParentValue(Composite<TaskListDataItem> compositeTask) {
		Double value = 0d;
		for (Composite<TaskListDataItem> component : compositeTask.getChildren()) {
			value += calculateTaskListActualInvoiceParentValue(component);
		}

		if (!compositeTask.isRoot()) {
			if (compositeTask.isLeaf()) {
				return compositeTask.getData().getActualInvoice();
			} else {
				compositeTask.getData().setActualInvoice(value + compositeTask.getData().getActualInvoice());
			}
		}
		return value;
	}

}
