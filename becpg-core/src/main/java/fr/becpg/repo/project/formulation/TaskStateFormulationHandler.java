package fr.becpg.repo.project.formulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

public class TaskStateFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static final int COMPLETED = 100;	

	private static Log logger = LogFactory.getLog(TaskStateFormulationHandler.class);

	private ProjectWorkflowService projectWorkflowService;


	public void setProjectWorkflowService(ProjectWorkflowService projectWorkflowService) {
		this.projectWorkflowService = projectWorkflowService;
	}

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {
		
		logger.debug("Formulate project " + projectData.getNodeRef());
		
		// start project if startdate is before now and startdate != created otherwise ProjectMgr will start it manually
		if(ProjectState.Planned.equals(projectData.getProjectState()) && 
				projectData.getStartDate() != null && 
				!projectData.getStartDate().equals(projectData.getCreated()) &&
				projectData.getStartDate().before(new Date())){
			projectData.setProjectState(ProjectState.InProgress);
		}
		
		// even if project is not in Progress, we visit it because a task can start the project (manual task or task that has startdate < NOW)
		visitTask(projectData, null);
		
		// first tasks to manage Project state
		if(ProjectState.Planned.equals(projectData.getProjectState())){
			List<TaskListDataItem> firstTasks = ProjectHelper.getNextTasks(projectData, null);
			if (!firstTasks.isEmpty()) {
				for (TaskListDataItem nextTask : firstTasks) {
					if(TaskState.InProgress.equals(nextTask.getState()) || TaskState.Completed.equals(nextTask.getState())){
						projectData.setProjectState(ProjectState.InProgress);
						break;
					}				
				}			
			}
		}
				
		// is project completed ?
		if(ProjectHelper.areTasksDone(projectData)){
			projectData.setCompletionDate(new Date());
			projectData.setCompletionPercent(COMPLETED);
			projectData.setProjectState(ProjectState.Completed);
		}
		
		projectData.setCompletionPercent(ProjectHelper.geProjectCompletionPercent(projectData));
		
		calculateProjectLegends(projectData);		
		return true;
	}
	

	private void visitTask(ProjectData projectData, TaskListDataItem taskListDataItem) {		

		NodeRef taskListNodeRef = taskListDataItem != null ? taskListDataItem.getNodeRef() : null;
		logger.debug("visitTask taskListNodeRef: " + taskListNodeRef);
		
		// add next tasks		
		List<TaskListDataItem> nextTasks = ProjectHelper.getNextTasks(projectData, taskListNodeRef);
		logger.debug("nextTasks size: " + nextTasks.size());
		if (!nextTasks.isEmpty()) {
			for (TaskListDataItem nextTask : nextTasks) {
				
				// cancel active workflow if task is not anymore InProgress								
				if(!TaskState.InProgress.equals(nextTask.getState()) &&
						projectWorkflowService.isWorkflowActive(nextTask)){
					projectWorkflowService.cancelWorkflow(nextTask);
				}
					
				// start task
				// - we are on first task and Project is in progress
				// - previous task are done
				if (TaskState.Planned.equals(nextTask.getState()) && ProjectHelper.areTasksDone(projectData, nextTask.getPrevTasks())) {
					// manual date -> we wait the date 
					// task start automatically if startDate are after projectStartDate
					if(!nextTask.getStart().equals(ProjectHelper.removeTime(projectData.getStartDate())) &&
							(nextTask.getManualDate() == null || nextTask.getStart().before(new Date()))){
						logger.debug("Start task since we are after planned startDate.");
						ProjectHelper.setTaskStartDate(nextTask, new Date());
						nextTask.setState(TaskState.InProgress);							
					}
				} else if (TaskState.Completed.equals(nextTask.getState())) {

					List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData,
							nextTask.getNodeRef());

					for (DeliverableListDataItem nextDeliverable : nextDeliverables) {
						nextDeliverable.setState(DeliverableState.Completed);
					}

					nextTask.setCompletionPercent(COMPLETED);											
				}
				
				if (TaskState.InProgress.equals(nextTask.getState())) {

					Integer taskCompletionPercent = 0;
					List<DeliverableListDataItem> nextDeliverables = ProjectHelper.getDeliverables(projectData,
							nextTask.getNodeRef());

					for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

						// Completed or Closed
						if (DeliverableState.Completed.equals(nextDeliverable.getState())
								|| DeliverableState.Closed.equals(nextDeliverable.getState())) {
							taskCompletionPercent += nextDeliverable.getCompletionPercent();
						}
						
						// set Planned dl InProgress
						if (DeliverableState.Planned.equals(nextDeliverable.getState())) {
							nextDeliverable.setState(DeliverableState.InProgress);								
						}
					}

					logger.debug("set completion percent to value " + taskCompletionPercent + " - nodref: "
							+ nextTask.getNodeRef());
					nextTask.setCompletionPercent(taskCompletionPercent);
					
					// check workflow instance (task may be reopened) and workflow properties
					projectWorkflowService.checkWorkflowInstance(projectData, nextTask, nextDeliverables);
										
					// workflow (task may have been set as InProgress with UI)
					if ((nextTask.getWorkflowInstance() == null ||  nextTask.getWorkflowInstance().isEmpty()) &&
							nextTask.getWorkflowName() != null && !nextTask.getWorkflowName().isEmpty() &&
							nextTask.getResources() != null && !nextTask.getResources().isEmpty()) {					
						
						// start workflow
						projectWorkflowService.startWorkflow(projectData, nextTask, nextDeliverables);
					}
				}										
					
				// we visit every task since user can have started a task in the middle of the project even if previous are not started
				visitTask(projectData, nextTask);
			}
		}
	}
	
	

	private void calculateProjectLegends(ProjectData projectData){
		
		if(projectData.getLegends() == null){
			logger.debug("projectData.setLegends(new ArrayList<NodeRef>());");
			projectData.setLegends(new ArrayList<NodeRef>());
		}
		
		for(TaskListDataItem tl : projectData.getTaskList()){
			
			if(TaskState.InProgress.equals(tl.getState())){
				if(!projectData.getLegends().contains(tl.getTaskLegend())){
					projectData.getLegends().add(tl.getTaskLegend());
				}
			}
			else if(TaskState.Completed.equals(tl.getState())){
				if(projectData.getLegends().contains(tl.getTaskLegend())){
					projectData.getLegends().remove(tl.getTaskLegend());
				}
			}
					
		}		
	}
}
