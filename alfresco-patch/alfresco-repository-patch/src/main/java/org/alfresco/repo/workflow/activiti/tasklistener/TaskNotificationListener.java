/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.workflow.activiti.tasklistener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.FormData;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLinkType;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Tasklistener that is notified when a task is created, will send email-notification
 * if this is required for this workflow.
 *
 * @author Frederik Heremans
 * @since 4.2
 */
public class TaskNotificationListener implements TaskListener
{
    private static final long serialVersionUID = 1L;
    
    private WorkflowNotificationUtils workflowNotificationUtils;
    private ActivitiPropertyConverter propertyConverter;
    
    /**
     * <p>setWorkflowNotification.</p>
     *
     * @param service  the service registry
     */
    public void setWorkflowNotification(WorkflowNotificationUtils service)
    {
        this.workflowNotificationUtils = service;
    }
    
    /**
     * <p>Setter for the field <code>propertyConverter</code>.</p>
     *
     * @param propertyConverter the property converter
     */
    public void setPropertyConverter(ActivitiPropertyConverter propertyConverter)
    {
        this.propertyConverter = propertyConverter;
    }
    
    /** {@inheritDoc} */
    @Override
    public void notify(DelegateTask task)
    {
        // Determine whether we need to send the workflow notification or not
        ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
        Boolean value = (Boolean)executionEntity.getVariable(WorkflowNotificationUtils.PROP_SEND_EMAIL_NOTIFICATIONS);
        if (Boolean.TRUE.equals(value) == true)
        {    
            NodeRef workflowPackage = null;
            ActivitiScriptNode scriptNode = (ActivitiScriptNode)executionEntity.getVariable(WorkflowNotificationUtils.PROP_PACKAGE);
            if (scriptNode != null)
            {
                workflowPackage = scriptNode.getNodeRef();
            }
            
            // Determine whether the task is pooled or not
            String[] authorities = null;
            boolean isPooled = false;
            if (task.getAssignee() == null)
            {
                // Task is pooled
                isPooled = true;
                
                // Get the pool of user/groups for this task
                List<IdentityLinkEntity> identities = ((TaskEntity)task).getIdentityLinks();
                List<String> temp = new ArrayList<String>(identities.size());
				for (IdentityLinkEntity item : identities) {
					//beCPG don't notify initiator if is not in pooled group
					if (!IdentityLinkType.STARTER.equals(item.getType())) {
                    String group = item.getGroupId();
                    if (group != null)
                    {
                        temp.add(group);
                    }
                    String user = item.getUserId();
                    if (user != null)
                    {
                        temp.add(user);
                    }
                }
				}
                authorities = temp.toArray(new String[temp.size()]);
            }
            else
            {
                // Get the assigned user or group
                authorities = new String[]{task.getAssignee()};
            }
            
            String title = null;
            String taskFormKey = getFormKey(task);
            
            // Fetch definition and extract name again. Possible that the default is used if the provided is missing
            TypeDefinition typeDefinition = propertyConverter.getWorkflowObjectFactory().getTaskTypeDefinition(taskFormKey, false);
            taskFormKey = typeDefinition.getName().toPrefixString();
            
            if (taskFormKey != null) 
            {
                String processDefinitionKey = ((ProcessDefinition) ((TaskEntity)task).getExecution().getProcessDefinition()).getKey();
                String defName = propertyConverter.getWorkflowObjectFactory().buildGlobalId(processDefinitionKey);
                title = propertyConverter.getWorkflowObjectFactory().getTaskTitle(typeDefinition, defName, task.getName(), taskFormKey.replace(":", "_"));
            }
            
            if (title == null)
            {
                if (task.getName() != null)
                {
                    title = task.getName();
                }
                else
                {
                    title = taskFormKey.replace(":", "_");
                }
            }
            
            // Make sure a description is present
            String description = task.getDescription();
            if (description == null || description.length() == 0)
            {
            	// use the task title as the description
            	description = title;
            }

			Map<QName, Serializable> props = new HashMap<>();
			
			//beCPG
			ActivitiScriptNode projectTask =  (ActivitiScriptNode)  executionEntity.getVariable("pjt_workflowTask");
			if(projectTask!=null) {
				props.put(WorkflowNotificationUtils.ASSOC_WORKFLOW_TASK, projectTask.getNodeRef());
			}
			ActivitiScriptNode entity = (ActivitiScriptNode) executionEntity.getVariable("bcpg_workflowEntity");
			if(entity!=null) {
				props.put(WorkflowNotificationUtils.ASSOC_WORKFLOW_ENTITY, entity.getNodeRef());
			}
			
			List<IdentityLinkEntity> identities = ((TaskEntity)task).getIdentityLinks();
            for (IdentityLinkEntity item : identities) {
                if (item.getType().equals(IdentityLinkType.STARTER)) {
                    props.put(WorkflowNotificationUtils.PROP_WORKFLOW_INITIATOR, item.getUserId());
                    break;
                }
            }

            // Send email notification
            workflowNotificationUtils.sendWorkflowAssignedNotificationEMail(
                    ActivitiConstants.ENGINE_ID + "$" + task.getId(),
                    title,
                    description,
                    task.getDueDate(),
                    Integer.valueOf(task.getPriority()),
                    workflowPackage,
                    authorities,
                    isPooled, props);
        }
    }

    private String getFormKey(DelegateTask task)
    {
        FormData formData = null;
        TaskEntity taskEntity = (TaskEntity) task;
        TaskFormHandler taskFormHandler = taskEntity.getTaskDefinition().getTaskFormHandler();
        if (taskFormHandler != null)
        {
            formData = taskFormHandler.createTaskForm(taskEntity);
            if (formData != null) { return formData.getFormKey(); }
        }
        return null;
    }
}
