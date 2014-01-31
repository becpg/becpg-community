/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

@Service("projectWorkflowService")
public class ProjectWorkflowServiceImpl implements ProjectWorkflowService{

	private static final String WORKFLOW_DESCRIPTION = "%s - %s";
	private static final String DESCRIPTION__TASK_DL_SEPARATOR = " : ";
	private static final String DESCRIPTION_DL_SEPARATOR = ", ";
	private static final String DEFAULT_INITIATOR = "System";
	
	private static Log logger = LogFactory.getLog(ProjectWorkflowServiceImpl.class);
	
	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;
	
	@Autowired
	private NodeService nodeService;
	

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

		if(projectData.getPriority() != null){
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, projectData.getPriority());
		}		
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
		workflowProps.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable)taskListDataItem.getResources());
		workflowProps.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, true);
		workflowProps.put(ProjectModel.ASSOC_WORKFLOW_TASK, taskListDataItem.getNodeRef());		
		
		// set workflow Initiator as Project Manager
		String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
		if(projectData.getProjectManager() != null){
			authenticatedUser = (String)nodeService.getProperty(projectData.getProjectManager(), ContentModel.PROP_USERNAME);			
		}
		else if (authenticatedUser == null || authenticatedUser.isEmpty()){
			authenticatedUser = DEFAULT_INITIATOR;
		}
		AuthenticationUtil.setFullyAuthenticatedUser(authenticatedUser);
		
		
		NodeRef wfPackage = workflowService.createPackage(null);
		nodeService.addChild(wfPackage, projectData.getNodeRef(), WorkflowModel.ASSOC_PACKAGE_CONTAINS,
				ContentModel.ASSOC_CHILDREN);
		if (!projectData.getEntities().isEmpty()) {
			for(NodeRef entity : projectData.getEntities()){
				nodeService.addChild(wfPackage, entity, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
						ContentModel.ASSOC_CHILDREN);
			}			
		}
		workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

		String workflowDefId = getWorkflowDefId(taskListDataItem.getWorkflowName());
		if(logger.isDebugEnabled()){
			logger.debug("workflowDefId: " + workflowDefId + " props " + workflowProps);
		}		
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
							
							logger.debug("check task" + taskListDataItem.getTaskName());							
							Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
							
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription, workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_DESCRIPTION, workflowDescription, workflowTask.getProperties(), properties);							
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_WORKFLOW_DUE_DATE, taskListDataItem.getEnd(), workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_DUE_DATE, taskListDataItem.getEnd(), workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_WORKFLOW_PRIORITY, projectData.getPriority(), workflowTask.getProperties(), properties);
							//properties = getWorkflowTaskNewProperties(WorkflowModel.ASSOC_ASSIGNEES, (Serializable)taskListDataItem.getResources(), workflowTask.getProperties(), properties);							
														
							if(taskListDataItem.getResources().size() == 0){
								workflowService.cancelWorkflow(workflowTask.getId());
								return;
							}
							if(taskListDataItem.getResources().size() == 1){
								String userName = (String)nodeService.getProperty(taskListDataItem.getResources().get(0), ContentModel.PROP_USERNAME);
								properties = getWorkflowTaskNewProperties(ContentModel.PROP_OWNER, userName, workflowTask.getProperties(), properties);
							}
							else{
								//assocs.put(WorkflowModel.ASSOC_ASSIGNEES, taskListDataItem.getResources());
								properties = getWorkflowTaskNewProperties(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable)taskListDataItem.getResources(), workflowTask.getProperties(), properties);
								if(properties.containsKey(WorkflowModel.ASSOC_POOLED_ACTORS)){									
									properties.put(ContentModel.PROP_OWNER, null);
								}
							}
							
							if(!properties.isEmpty()){
								if(logger.isDebugEnabled()){
									logger.debug("update task " + taskListDataItem.getTaskName() + " props " + properties);
								}								
								workflowService.updateTask(workflowTask.getId(), properties, null, null);
							}													
						}
					}
				}
			}			
		}		
	}
	
	private Map<QName, Serializable> getWorkflowTaskNewProperties(QName propertyQName, 
			Serializable value, Map<QName, Serializable> dbProperties, 
			Map<QName, Serializable> newProperties){
		
		Serializable dbValue = dbProperties.get(propertyQName);
		if((dbValue == null && value != null) || (dbValue != null && value == null) || (value!=null && !value.equals(dbValue))){
			newProperties.put(propertyQName, value);
		}
		return newProperties;
	}

	@Override
	public void deleteWorkflowTask(NodeRef taskListNodeRef) {
		
		String workflowInstanceId = (String) nodeService.getProperty(taskListNodeRef, ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
		if (workflowInstanceId != null && !workflowInstanceId.isEmpty()) {
			WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);
			if (workflowInstance != null) {
				workflowService.deleteWorkflow(workflowInstanceId);
			}
		}		
	}
}
