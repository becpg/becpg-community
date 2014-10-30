package fr.becpg.repo.project.formulation;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.LogTimeListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Calculate budget
 * @author quere
 *
 */
public class BudgetFormulationHandler extends FormulationBaseHandler<ProjectData> {


	@Override
	public boolean process(ProjectData projectData) throws FormulateException {
		if (!projectData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			Composite<TaskListDataItem> composite = CompositeHelper.getHierarchicalCompoList(projectData.getTaskList());
			clearData(projectData);
			calculateCost(projectData, composite);
			calculateLogTime(projectData);
		}
		return true;
	}	

	private void clearData(ProjectData projectData){
		for(TaskListDataItem tl : projectData.getTaskList()){
			tl.setBudgetedCost(null);
			tl.setLoggedTime(null);
		}
	}
	
	private void calculateCost(ProjectData projectData, Composite<TaskListDataItem> composite){		
		
		Double cost = 0d;
		for(Composite<TaskListDataItem> component : composite.getChildren()){
			calculateCost(projectData, component);
			TaskListDataItem taskListDataItem = component.getData();
			Double taskCost = 0d;
			if(taskListDataItem.getBudgetedCost() != null){
				//cost are roll-up
				taskCost += taskListDataItem.getBudgetedCost();
			}
			else{
				if(taskListDataItem.getWork() != null && taskListDataItem.getResourceCost() != null && taskListDataItem.getResourceCost().getValue() != null){
					taskCost += taskListDataItem.getWork() * taskListDataItem.getResourceCost().getValue();								
				}			
				//fixed cost are not roll-up
				if(taskListDataItem.getFixedCost() != null){
					taskCost += taskListDataItem.getFixedCost();
				}
			}						
			cost += taskCost;			
			taskListDataItem.setBudgetedCost(taskCost == 0d ? null : taskCost);
		}
		if(composite.isRoot()){
			projectData.setBudgetedCost(cost == 0d ? null : cost);
		}
		else{
			composite.getData().setBudgetedCost(cost == 0d ? null : cost);
		}
	}

	private void calculateLogTime(ProjectData projectData){
		Map<NodeRef, Double> totalLogTimeMap = new HashMap<>();
		Double totalLogTime = 0d;
		
		for(LogTimeListDataItem logTime : projectData.getLogTimeList()){
			Double time = logTime.getTime();
			if(time != null){
				totalLogTime += time;
				Double totalTime = totalLogTimeMap.get(logTime.getTask());
				if(totalTime == null){
					totalLogTimeMap.put(logTime.getTask(), time);
				}
				else{
					totalLogTimeMap.put(logTime.getTask(), totalTime + time);
				}
			}			
		}
		
		for(TaskListDataItem tl : projectData.getTaskList()){
			tl.setLoggedTime(totalLogTimeMap.get(tl.getNodeRef()));
		}
		
		projectData.setLoggedTime(totalLogTime);
	}
}
