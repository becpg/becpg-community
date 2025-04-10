package fr.becpg.repo.project.formulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Calculate budget
 *
 * @author quere
 * @version $Id: $Id
 */
public class BudgetFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private final static Log logger = LogFactory.getLog(BudgetFormulationHandler.class);

	private AssociationService associationService;
	
	private NodeService nodeService;
	
	protected AlfrescoRepository<ProjectData> alfrescoRepository;
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		logger.debug("BudgetFormulationHandler");
		clearData(projectData);

		calculateLogTime(projectData);
		calculateTaskExpenses(projectData);
		
		for(BudgetListDataItem budgetListItem : projectData.getBudgetList()){
			calculateExpensesAndInvoices(projectData, budgetListItem);
		}
				
		Composite<TaskListDataItem> compositeTask = CompositeHelper.getHierarchicalCompoList(projectData.getTaskList());
		calculateTaskParentValue(compositeTask,projectData);		
		
		Composite<BudgetListDataItem> compositeBugdet = CompositeHelper.getHierarchicalCompoList(projectData.getBudgetList());
		calculateBudgetParentValue(compositeBugdet,projectData);
		removeZeroValues(projectData);
		
		return true;
	}
	
	private void calculateExpensesAndInvoices(ProjectData projectData, BudgetListDataItem budgetListDataItem){
		List<NodeRef> assocs = associationService.getSourcesAssocs(budgetListDataItem.getNodeRef());
		for(NodeRef item : assocs){
			if(nodeService.exists(item) && nodeService.hasAspect(item, ProjectModel.ASPECT_BUDGET)){
				Double expense  = null;
				Double invoice  = null;
				if(ProjectModel.TYPE_TASK_LIST.equals(nodeService.getType(item))){
					for(TaskListDataItem taskList : projectData.getTaskList()){
						if(item.equals(taskList.getNodeRef())){
							expense = taskList.getExpense();
							invoice = taskList.getInvoice();
						}
					}
				}
				else{
					expense  = (Double) nodeService.getProperty(item, ProjectModel.PROP_BUDGET_EXPENSE);
					invoice  = (Double) nodeService.getProperty(item, ProjectModel.PROP_BUDGET_INVOICE);
				}				
				if(expense!=null){
					budgetListDataItem.setExpense(budgetListDataItem.getExpense() + expense);
				}
				if(invoice!=null){
					budgetListDataItem.setInvoice(budgetListDataItem.getInvoice() + invoice);
				}
			}
		}
	}

	private void calculateTaskExpenses(ProjectData projectData) {		

		for (TaskListDataItem taskListDataItem : projectData.getTaskList()) {			
			Double taskExpense = 0d;			
			logger.debug("taskListDataItem " + taskListDataItem.getTaskName() + " work " + taskListDataItem.getWork() + " loggedTime " + taskListDataItem.getLoggedTime());
			
			if(taskListDataItem.getResourceCost() != null && 
					taskListDataItem.getResourceCost().getValue() != null && 
					taskListDataItem.getTaskState() != null){				
				if(taskListDataItem.getLoggedTime() != null && taskListDataItem.getLoggedTime() != 0d && 
						!taskListDataItem.isPlanned() && 
						!TaskState.InProgress.equals(taskListDataItem.getTaskState())){
					taskExpense = taskListDataItem.getLoggedTime() * taskListDataItem.getResourceCost().getValue();
				}
				else if (taskListDataItem.getWork() != null) {
					taskExpense = taskListDataItem.getWork() * taskListDataItem.getResourceCost().getValue();
				}
			}												
			taskListDataItem.setExpense(taskExpense);
		}
	}
	
	private void calculateTaskParentValue(Composite<TaskListDataItem> parent, ProjectData projectData) {
		Double expense = 0d;
		if (!parent.isLeaf()) {
			for (Composite<TaskListDataItem> component : parent.getChildren()) {
				calculateTaskParentValue(component, projectData);					
				TaskListDataItem taskListDataItem = component.getData();
				expense += taskListDataItem.getExpense();
			}
			if (!parent.isRoot()) {
				parent.getData().setExpense(expense);
			}
		}
		if (parent.isRoot()) {
			projectData.setBudgetedCost(expense == 0d ? null : expense);
		}		
	}
	
	private void calculateBudgetParentValue(Composite<BudgetListDataItem> parent, ProjectData projectData) {		
		Double expense = 0d;
		Double invoice = 0d;
		Double budgetedExpense = 0d;
		Double budgetedInvoice = 0d;
		if (!parent.isLeaf()) {
			for (Composite<BudgetListDataItem> component : parent.getChildren()) {
				calculateBudgetParentValue(component, projectData);
				expense += component.getData().getExpense();
				invoice += component.getData().getInvoice();
				budgetedExpense += component.getData().getBudgetedExpense() != null ? component.getData().getBudgetedExpense() : 0d;
				budgetedInvoice += component.getData().getBudgetedInvoice() != null ? component.getData().getBudgetedInvoice() : 0d;			
			}
			if (!parent.isRoot()) {
				parent.getData().setExpense(expense);
				parent.getData().setInvoice(invoice);
				parent.getData().setBudgetedExpense(budgetedExpense);
				parent.getData().setBudgetedInvoice(budgetedInvoice);
			}
		}
		if (!parent.isRoot()) {
			parent.getData().setProfit(parent.getData().getInvoice() - parent.getData().getExpense());
		}
		
		// has budgetList
		if(parent.isRoot() && alfrescoRepository.hasDataList(projectData, ProjectModel.TYPE_BUDGET_LIST)){
			projectData.setBudgetedCost(budgetedExpense == 0d ? null : budgetedExpense);
		}
		
	}
	
	private void clearData(ProjectData projectData) {
		for (TaskListDataItem tl : projectData.getTaskList()) {
			tl.setLoggedTime(0d);
			tl.setExpense(0d);
		}
		for (BudgetListDataItem bl : projectData.getBudgetList()) {
			bl.setExpense(0d);
			bl.setInvoice(0d);
		}
	}
	
	private void removeZeroValues(ProjectData projectData) {
		for (TaskListDataItem tl : projectData.getTaskList()) {
			tl.setLoggedTime(tl.getLoggedTime() != 0d ? tl.getLoggedTime() : null);
			tl.setExpense(tl.getExpense() != 0d ? tl.getExpense() : null);
		}
		for (BudgetListDataItem bl : projectData.getBudgetList()) {
			bl.setExpense(bl.getExpense() != 0d ? bl.getExpense() : null);
			bl.setInvoice(bl.getInvoice() != 0d ? bl.getInvoice() : null);
		}
	}
	
	private void calculateLogTime(ProjectData projectData) {
		Double totalLogTime = 0d;
		for (LogTimeListDataItem logTime : projectData.getLogTimeList()) {							
			if (logTime.getTime() != null && logTime.getTask() != null) {
				logger.debug("logTime " + logTime.getTime() + "task" + logTime.getTask().getTaskName());
				totalLogTime += logTime.getTime();
				logTime.getTask().setLoggedTime(logTime.getTask().getLoggedTime()+logTime.getTime());				
				if(logTime.getTask().getResourceCost() != null && logTime.getTask().getResourceCost().getBillRate() != null){
					logTime.setInvoice(logTime.getTime() * logTime.getTask().getResourceCost().getBillRate());
				}
			}
		}
		projectData.setLoggedTime(totalLogTime);
	}	
}
