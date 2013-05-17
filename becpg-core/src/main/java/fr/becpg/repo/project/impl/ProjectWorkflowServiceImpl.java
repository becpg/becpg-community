package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Class used to manage workflow
 * @author quere
 *
 */
public class ProjectWorkflowServiceImpl implements ProjectWorkflowService{

	private static final String WORKFLOW_DESCRIPTION = "%s - %s";
	private static final String DESCRIPTION__TASK_DL_SEPARATOR = " : ";
	private static final String DESCRIPTION_DL_SEPARATOR = ", ";
	
	private static Log logger = LogFactory.getLog(ProjectWorkflowServiceImpl.class);
	
	private WorkflowService workflowService;
	private NodeService nodeService;
	
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void cancelWorkflow(TaskListDataItem task) {
		
		logger.debug("Cancel workflow instance: " + task.getWorkflowInstance());
		workflowService.cancelWorkflow(task.getWorkflowInstance());
		task.setWorkflowInstance("");	
	}

	@Override
	public void startWorkflow(ProjectData projectData, TaskListDataItem taskListDataItem,
			List<DeliverableListDataItem> nextDeliverables) {
		
		String workflowDescription = calculateWorkflowDescription(projectData, taskListDataItem, nextDeliverables);
		Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();

		if (taskListDataItem.getEnd() != null) {			
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, taskListDataItem.getEnd());
		}

		workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, projectData.getPriority());
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
		workflowProps.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable)taskListDataItem.getResources());
		workflowProps.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, true);
		workflowProps.put(ProjectModel.ASSOC_WORKFLOW_TASK, taskListDataItem.getNodeRef());
		
		NodeRef wfPackage = workflowService.createPackage(null);
		nodeService.addChild(wfPackage, projectData.getNodeRef(), WorkflowModel.ASSOC_PACKAGE_CONTAINS,
				ContentModel.ASSOC_CHILDREN);
		if (projectData.getEntity() != null) {
			nodeService.addChild(wfPackage, projectData.getEntity(), WorkflowModel.ASSOC_PACKAGE_CONTAINS,
					ContentModel.ASSOC_CHILDREN);
		}
		workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

		String workflowDefId = getWorkflowDefId(taskListDataItem.getWorkflowName());
		logger.debug("workflowDefId: " + workflowDefId);
		if (workflowDefId != null) {

			WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, workflowProps);
			logger.debug("New worflow started. Id: " + wfPath.getId() + " - workflowDescription: "
					+ workflowDescription);
			String workflowId = wfPath.getInstance().getId();
			taskListDataItem.setWorkflowInstance(workflowId);

			// get the workflow tasks
			WorkflowTask startTask = workflowService.getStartTask(workflowId);

			// end task
			try {
				workflowService.endTask(startTask.getId(), null);
			} catch (RuntimeException err) {
				if (logger.isDebugEnabled())
					logger.debug("Failed - caught error during project adhoc workflow transition: " + err.getMessage());
				throw err;
			}
		}
	}
	
	private String calculateWorkflowDescription(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> nextDeliverables){
		
		// deliverable list
		String workflowDescription = String.format(WORKFLOW_DESCRIPTION, projectData.getName(), taskListDataItem.getTaskName());
		boolean isFirst = true;

		for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

			if (DeliverableState.InProgress.equals(nextDeliverable.getState())) {
				if (isFirst) {
					isFirst = false;
					workflowDescription += DESCRIPTION__TASK_DL_SEPARATOR;
				} else {
					workflowDescription += DESCRIPTION_DL_SEPARATOR;
				}
				workflowDescription += nextDeliverable.getDescription();
			}
		}
		
		return workflowDescription;
	}

	private String getWorkflowDefId(String workflowName) {
		if (workflowName != null && !workflowName.isEmpty()) {
			WorkflowDefinition def = workflowService.getDefinitionByName(workflowName);
			if (def != null) {
				return def.getId();
			}
		}

		logger.error("Unknown workflow name: " + workflowName);
		return null;
	}

	@Override
	public boolean isWorkflowActive(TaskListDataItem task) {
	
		if(task.getWorkflowInstance() != null && !task.getWorkflowInstance().isEmpty()){
			String workflowId = task.getWorkflowInstance();
			WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowId);
			if(workflowInstance != null){
				if(workflowInstance.isActive()){
					return true;
				}
			}
			else{
				logger.warn("Workflow instance unknown. WorkflowId: " + workflowId);
			}
		}
		
		return false;
		
	}

	/**
	 * Check workflow instance and properties
	 */
	@Override
	public void checkWorkflowInstance(ProjectData projectData, TaskListDataItem taskListDataItem,
			List<DeliverableListDataItem> nextDeliverables) {
		 
		if(taskListDataItem.getWorkflowInstance() != null && 
			!taskListDataItem.getWorkflowInstance().isEmpty()){
			
			// task may be reopened so
			if(!isWorkflowActive(taskListDataItem)){				
				taskListDataItem.setWorkflowInstance("");
			}
			else{
				//check workflow properties
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(taskListDataItem.getWorkflowInstance());
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);
				
				if(!workflowTasks.isEmpty()){
					
					String workflowDescription = calculateWorkflowDescription(projectData, taskListDataItem, nextDeliverables);
					
					for (WorkflowTask workflowTask : workflowTasks) {
						NodeRef taskNodeRef  = (NodeRef)workflowTask.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK);			
						if (taskNodeRef != null && taskNodeRef.equals(taskListDataItem.getNodeRef())) {
							
							logger.debug("check task" + workflowTask.getName());
							
							boolean updateWorkflowTask = false;
							Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
							java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
							
							if(!workflowDescription.equals(workflowTask.getName())){
								updateWorkflowTask = true;
								properties.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
							}
							
							Date workflowDueDate = (Date)properties.get(WorkflowModel.PROP_WORKFLOW_DUE_DATE);
							if(workflowDueDate == null || (taskListDataItem.getEnd() != null && !taskListDataItem.getEnd().equals(workflowDueDate))){
								updateWorkflowTask = true;
								properties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, taskListDataItem.getEnd());
							}
							
							Integer workflowPriority = (Integer)properties.get(WorkflowModel.PROP_WORKFLOW_PRIORITY);
							if(workflowPriority == null || (projectData.getPriority() != null && !projectData.getPriority().equals(workflowPriority))){
								updateWorkflowTask = true;
								properties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, projectData.getPriority());
							}
							
							if(!taskListDataItem.getResources().equals(properties.get(WorkflowModel.ASSOC_ASSIGNEES))){
								updateWorkflowTask = true;
								assocs.put(WorkflowModel.ASSOC_ASSIGNEES, taskListDataItem.getResources());
							}
							
							if(updateWorkflowTask){
								logger.debug("update task" + workflowTask.getName());				
								workflowService.updateTask(workflowTask.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
							}													
						}
					}
				}
			}			
		}		
	}
	
}
