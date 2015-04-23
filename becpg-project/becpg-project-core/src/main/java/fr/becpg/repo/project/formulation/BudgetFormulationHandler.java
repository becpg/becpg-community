package fr.becpg.repo.project.formulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.BudgetListDataItem;
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

	private AssociationService associationService;
	
	private NodeService nodeService;
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		logger.debug("BudgetFormulationHandler");
		clearData(projectData);

		
		for(BudgetListDataItem budgetListItem : projectData.getBudgetList()){
			List<NodeRef> assocs = associationService.getSourcesAssocs(budgetListItem.getNodeRef(), RegexQNamePattern.MATCH_ALL);
			for(NodeRef item : assocs){
				if(nodeService.exists(item) && nodeService.hasAspect(item, ProjectModel.ASPECT_BUDGET)){
					Double invoice  = (Double) nodeService.getProperty(item, ProjectModel.PROP_BUDGET_INVOICE);
					if(invoice!=null){
						budgetListItem.setActualInvoice(budgetListItem.getActualInvoice() + invoice);
					}
					Double expense  = (Double) nodeService.getProperty(item, ProjectModel.PROP_BUDGET_EXPENSE);
					if(expense!=null){
						budgetListItem.setActualExpense(budgetListItem.getActualExpense() + expense);
					}	
				}
			}
		}
		
		for(TaskListDataItem taskListDataItem : projectData.getTaskList()){
			List<NodeRef> assocs = associationService.getSourcesAssocs(taskListDataItem.getNodeRef(), RegexQNamePattern.MATCH_ALL);
			for(NodeRef item : assocs){
				if(nodeService.exists(item) && nodeService.hasAspect(item, ProjectModel.ASPECT_BUDGET)){
					Double invoice  = (Double) nodeService.getProperty(item, ProjectModel.PROP_BUDGET_INVOICE);
					if(invoice!=null){
						taskListDataItem.setActualInvoice(taskListDataItem.getActualInvoice() + invoice);
					}
					Double expense  = (Double) nodeService.getProperty(item, ProjectModel.PROP_BUDGET_EXPENSE);
					if(expense!=null){
						taskListDataItem.setActualExpense(taskListDataItem.getActualExpense() + expense);
					}	
				}
			}
		}
		
		// Hierarchie dans Task List
		Composite<TaskListDataItem> compositeTask = CompositeHelper.getHierarchicalCompoList(projectData.getTaskList());
		calculateTaskParentValueAndCosts(compositeTask,projectData);
		calculateLogTime(projectData);
		
		// Hierachie dans Budget List
		Composite<BudgetListDataItem> compositeBugdet = CompositeHelper.getHierarchicalCompoList(projectData.getBudgetList());
		calculateBudgetParentValue(compositeBugdet);

		return true;
	}

	

	private void calculateTaskParentValueAndCosts(Composite<TaskListDataItem> parent, ProjectData projectData) {
		Double actualExpense = 0d;
		Double actualInvoice = 0d;
		Double cost = 0d;
		if (!parent.isLeaf()) {
			for (Composite<TaskListDataItem> component : parent.getChildren()) {
				calculateTaskParentValueAndCosts(component, projectData);	
				
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
				if(taskListDataItem.getActualExpense()!=null){
					actualExpense += taskListDataItem.getActualExpense();
				}
				if(taskListDataItem.getActualInvoice()!=null){
					actualInvoice += taskListDataItem.getActualInvoice();
				}
			}
			if (!parent.isRoot()) {
				parent.getData().setActualExpense(actualExpense);
				parent.getData().setActualInvoice(actualInvoice);
				parent.getData().setBudgetedCost(cost == 0d ? null : cost);
			}
		}
		if (parent.isRoot()) {
			projectData.setBudgetedCost(cost == 0d ? null : cost);
		}
		
	}

	private void calculateBudgetParentValue(Composite<BudgetListDataItem> parent) {
		Double actualExpense = 0d;
		Double actualInvoice = 0d;
		Double budgetedExpense = 0d;
		Double budgetedInvoice = 0d;
		if (!parent.isLeaf()) {
			for (Composite<BudgetListDataItem> component : parent.getChildren()) {
				calculateBudgetParentValue(component);	
				if(component.getData().getActualExpense()!=null){
					actualExpense += component.getData().getActualExpense();
				}
				if(component.getData().getActualInvoice()!=null){
					actualInvoice += component.getData().getActualInvoice();
				}
				if(component.getData().getBudgetedExpense()!=null){
					budgetedExpense += component.getData().getBudgetedExpense();
				}
				if(component.getData().getBudgetedInvoice()!=null){
					budgetedInvoice += component.getData().getBudgetedInvoice();
				}
			}
			if (!parent.isRoot()) {
				parent.getData().setActualExpense(actualExpense);
				parent.getData().setActualInvoice(actualInvoice);
				parent.getData().setBudgetedExpense(budgetedExpense);
				parent.getData().setBudgetedInvoice(budgetedInvoice);
			}
		}
		if (!parent.isRoot()) {
			parent.getData().setProfit(parent.getData().getActualInvoice() - parent.getData().getActualExpense());
		}
		
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

	
	
}
