/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectNotificationEvent;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * Class used to manage workflow
 *
 * @author quere
 * @version $Id: $Id
 */

@Service("projectWorkflowService")
public class ProjectWorkflowServiceImpl implements ProjectWorkflowService {

	private static final String WORKFLOW_DESCRIPTION = "%s - %s - %s";
	private static final String DEFAULT_INITIATOR = "System";

	private static final Log logger = LogFactory.getLog(ProjectWorkflowServiceImpl.class);

	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AutoNumService autoNumService;
	
	@Autowired
    private WorkflowNotificationUtils workflowNotificationUtils;

	/** {@inheritDoc} */
	@Override
	public void cancelWorkflow(TaskListDataItem task) {

		logger.debug("Cancel workflow instance: " + task.getWorkflowInstance());
		WorkflowInstance instance = workflowService.cancelWorkflow(task.getWorkflowInstance());
		if(instance == null || !instance.isActive()) {
			task.setWorkflowInstance("");
			task.setWorkflowTaskInstance("");
		} else {
			logger.error("Cannot cancel worflow:"+ task.getWorkflowInstance());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void startWorkflow(final ProjectData projectData, final TaskListDataItem taskListDataItem,
			final List<DeliverableListDataItem> nextDeliverables) {

		final String workflowDescription = calculateWorkflowDescription(projectData, taskListDataItem);
		final Map<QName, Serializable> workflowProps = new HashMap<>();

		if (taskListDataItem.getDue() != null) {
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, taskListDataItem.getDue());
		}

		if (projectData.getPriority() != null) {
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, projectData.getPriority());
		}
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
		List<NodeRef> assignees = getAssignees(taskListDataItem.getResources(), false);
		List<NodeRef> groupAssignees = getAssignees(taskListDataItem.getResources(), true);
		if (!assignees.isEmpty()) {
			logger.debug("Add assignees to workflow : " + assignees.size());
			workflowProps.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable) assignees);
		}
		if (!groupAssignees.isEmpty()) {
			logger.debug("Add group assignees to workflow : " + groupAssignees.size());
			workflowProps.put(WorkflowModel.ASSOC_GROUP_ASSIGNEES, (Serializable) groupAssignees);
		}

		workflowProps.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, shouldNotify(projectData, taskListDataItem));
		workflowProps.put(ProjectModel.ASSOC_WORKFLOW_TASK, taskListDataItem.getNodeRef());
		workflowProps.put(BeCPGModel.ASSOC_WORKFLOW_ENTITY, projectData.getNodeRef());

		// set workflow Initiator as Project Manager

		String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
		String authenticatedUser = fullyAuthenticatedUser;

		if (projectData.getProjectManager() != null) {
			authenticatedUser = (String) nodeService.getProperty(projectData.getProjectManager(), ContentModel.PROP_USERNAME);
		} else if ((authenticatedUser == null) || authenticatedUser.isEmpty()) {
			authenticatedUser = DEFAULT_INITIATOR;
		}

		try {
			AuthenticationUtil.setFullyAuthenticatedUser(authenticatedUser);

			NodeRef wfPackage = workflowService.createPackage(null);
			nodeService.addChild(wfPackage, projectData.getNodeRef(), WorkflowModel.ASSOC_PACKAGE_CONTAINS, ContentModel.ASSOC_CHILDREN);
			if (!projectData.getEntities().isEmpty()) {
				for (NodeRef entity : projectData.getEntities()) {
					if (nodeService.exists(entity)) {
						nodeService.addChild(wfPackage, entity, WorkflowModel.ASSOC_PACKAGE_CONTAINS, ContentModel.ASSOC_CHILDREN);
					}
				}
			}
			workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

			String workflowDefId = getWorkflowDefId(taskListDataItem.getWorkflowName());
			if (logger.isDebugEnabled()) {
				logger.debug("workflowDefId: " + workflowDefId + " props " + workflowProps);
			}
			if (workflowDefId != null) {

				WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, workflowProps);
				logger.debug("New worflow started. Id: " + wfPath.getId() + " - workflowDescription: " + workflowDescription);
				String workflowId = wfPath.getInstance().getId();
				taskListDataItem.setWorkflowInstance(workflowId);

				// get the workflow tasks
				WorkflowTask startTask = workflowService.getStartTask(workflowId);

				// end task
				try {
					workflowService.endTask(startTask.getId(), null);

				} catch (WorkflowException err) {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed - caught error during project adhoc workflow transition: " + err.getMessage());
					}
					throw err;
				}

				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(taskListDataItem.getWorkflowInstance());
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

				if (!workflowTasks.isEmpty()) {
					Map<QName, Serializable> taskProps = workflowTasks.get(0).getProperties();
					taskProps.put(WorkflowModel.PROP_STATUS, WorkflowConstants.TASK_STATUS_IN_PROGRESS);
					workflowService.updateTask(workflowTasks.get(0).getId(), taskProps, null, null);

					taskListDataItem.setWorkflowTaskInstance(workflowTasks.get(0).getId());

				}
			}
		} finally {
			AuthenticationUtil.setFullyAuthenticatedUser(fullyAuthenticatedUser);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean shouldNotify(ProjectData projectData, TaskListDataItem task) {
		boolean notify = true;
		List<String> notificationEvents = (List<String>) nodeService.getProperty(task.getNodeRef(), ProjectModel.PROP_OBSERVERS_EVENTS);

		if ((notificationEvents != null) && !notificationEvents.isEmpty()) {
			for (String notificationEvent : notificationEvents) {
				ProjectNotificationEvent event = ProjectNotificationEvent.valueOf(notificationEvent);
				if (ProjectNotificationEvent.NotifyOnRefused.equals(event) && isReopenedAfterRefuse(projectData, task)) {
					notify = true;
					break;
				}
				if (ProjectNotificationEvent.NotifyDisabled.equals(event) || ProjectNotificationEvent.NotifyOnRefused.equals(event)) {
					notify = false;
				}
			}
		}

		return notify;
	}

	private boolean isReopenedAfterRefuse(ProjectData projectData, TaskListDataItem reopenedTask) {
		boolean isReopenedAfterRefuse = false;
		for (TaskListDataItem task : projectData.getTaskList()) {
			if (TaskState.Refused.equals(task.getTaskState()) && (reopenedTask.equals(task.getRefusedTask())
					|| ((task.getRefusedTasksToReopen() != null) && task.getRefusedTasksToReopen().contains(reopenedTask.getNodeRef())))) {
				isReopenedAfterRefuse = true;
				break;
			}
		}
		return isReopenedAfterRefuse;
	}

	private List<NodeRef> getAssignees(List<NodeRef> resources, boolean group) {
		List<NodeRef> ret = new ArrayList<>();

		for (NodeRef resource : resources) {
			QName type = nodeService.getType(resource);
			if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
				if (group) {
					ret.add(resource);
				}
			} else {
				if (!group) {
					ret.add(resource);
				}
			}
		}
		return ret;
	}

	private String calculateWorkflowDescription(ProjectData projectData, TaskListDataItem taskListDataItem) {

		return String.format(WORKFLOW_DESCRIPTION, getProjectCode(projectData), projectData.getName(), taskListDataItem.getTaskName());
	}

	private Object getProjectCode(ProjectData projectData) {
		return projectData.getCode() != null ? projectData.getCode() : autoNumService.getOrCreateBeCPGCode(projectData.getNodeRef());
	}

	private String getWorkflowDefId(String workflowName) {
		if ((workflowName != null) && !workflowName.isEmpty()) {
			WorkflowDefinition def = workflowService.getDefinitionByName(workflowName);
			if (def != null) {
				return def.getId();
			}
		}

		logger.error("Unknown workflow name: " + workflowName);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isWorkflowActive(TaskListDataItem task) {

		if ((task != null) && (task.getWorkflowInstance() != null) && !task.getWorkflowInstance().isEmpty()) {
			String workflowId = task.getWorkflowInstance();
			WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowId);
			if (workflowInstance != null) {
				if (workflowInstance.isActive()) {
					return true;
				} else {
					task.setWorkflowInstance("");
					task.setWorkflowTaskInstance("");
				}
			} else {
				logger.warn(
						"Workflow instance unknown. WorkflowId: " + workflowId + " task " + task.getNodeRef() + " Task name " + task.getTaskName());
				task.setWorkflowInstance("");
				task.setWorkflowTaskInstance("");
			}
		}

		return false;

	}

	/**
	 * {@inheritDoc}
	 *
	 * Check workflow instance and properties
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void checkWorkflowInstance(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> nextDeliverables) {

		if ((taskListDataItem.getWorkflowInstance() != null) && !taskListDataItem.getWorkflowInstance().isEmpty()) {

			// task may be reopened so
			if (!isWorkflowActive(taskListDataItem)) {
				taskListDataItem.setWorkflowInstance("");
				taskListDataItem.setWorkflowTaskInstance("");
			} else {

				if (taskListDataItem.getResources().isEmpty()) {
					workflowService.cancelWorkflow(taskListDataItem.getWorkflowInstance());
					return;
				}

				// check workflow properties
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setProcessId(taskListDataItem.getWorkflowInstance());
				taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

				List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

				if (!workflowTasks.isEmpty()) {

					String workflowDescription = calculateWorkflowDescription(projectData, taskListDataItem);

					for (WorkflowTask workflowTask : workflowTasks) {
						NodeRef taskNodeRef = (NodeRef) workflowTask.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK);
						if ((taskNodeRef != null) && taskNodeRef.equals(taskListDataItem.getNodeRef())) {

							logger.debug("check task" + taskListDataItem.getTaskName());
							Map<QName, Serializable> properties = new HashMap<>();

							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription,
									workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_DESCRIPTION, workflowDescription,
									workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_WORKFLOW_DUE_DATE, taskListDataItem.getDue(),
									workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_DUE_DATE, taskListDataItem.getDue(),
									workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_WORKFLOW_PRIORITY, projectData.getPriority(),
									workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_PRIORITY, projectData.getPriority(),
									workflowTask.getProperties(), properties);
							properties = getWorkflowTaskNewProperties(WorkflowModel.PROP_STATUS, WorkflowConstants.TASK_STATUS_IN_PROGRESS,
									workflowTask.getProperties(), properties);

							List<NodeRef> assignees = getAssignees(taskListDataItem.getResources(), false);
							
							List<NodeRef> oldPooledActors = (List<NodeRef>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
							
							String oldOwnerUsername = (String) workflowTask.getProperties().get(ContentModel.PROP_OWNER);
							
							if (taskListDataItem.getResources().size() == 1 && assignees.size() == 1) {
								String userName = (String) nodeService.getProperty(assignees.get(0), ContentModel.PROP_USERNAME);
								properties = getWorkflowTaskNewProperties(ContentModel.PROP_OWNER, userName, workflowTask.getProperties(), properties);
								properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, new ArrayList<>());
							} else {
								if (oldPooledActors == null || oldPooledActors.size() != taskListDataItem.getResources().size() || !oldPooledActors.containsAll(taskListDataItem.getResources())) {
									properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) taskListDataItem.getResources());
								}
							}
							
							// Send notifications for new actors
							if (properties.containsKey(WorkflowModel.ASSOC_POOLED_ACTORS)) {
								
								List<NodeRef> newActors = (List<NodeRef>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
								
								if (!newActors.isEmpty()) {
									properties.put(ContentModel.PROP_OWNER, null);
								}
								
								List<String> newAuthorityNames = new ArrayList<>();
								
								for (NodeRef newActor : newActors) {
									
									String authorityName = "";
									
									QName type = nodeService.getType(newActor);
									
									if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
										authorityName = (String) nodeService.getProperty(newActor, ContentModel.PROP_NAME);
									} else {
										authorityName = (String) nodeService.getProperty(newActor, ContentModel.PROP_USERNAME);
									}
									
									if (!oldPooledActors.contains(newActor) && !authorityName.equals(oldOwnerUsername)) {
										newAuthorityNames.add(authorityName);
									}
									
								}
								
								if (!newAuthorityNames.isEmpty()) {
									workflowNotificationUtils.sendWorkflowAssignedNotificationEMail(workflowTask.getId(), null, newAuthorityNames.toArray(new String[0]), false);
								}
							}
							
							if (!properties.isEmpty()) {
								
								if (logger.isDebugEnabled()) {
									logger.debug("update task " + taskListDataItem.getTaskName() + " props " + properties);
								}
								
								workflowService.updateTask(workflowTask.getId(), properties, null, null);
								
								taskListDataItem.setWorkflowTaskInstance(workflowTask.getId());
							}
						}
					}
				}
			}
		}
	}

	private Map<QName, Serializable> getWorkflowTaskNewProperties(QName propertyQName, Serializable value, Map<QName, Serializable> dbProperties,
			Map<QName, Serializable> newProperties) {

		Serializable dbValue = dbProperties.get(propertyQName);
		if (((dbValue == null) && (value != null)) || ((dbValue != null) && (value == null)) || ((value != null) && !value.equals(dbValue))) {
			newProperties.put(propertyQName, value);
		}
		return newProperties;
	}

	/** {@inheritDoc} */
	@Override
	public void deleteWorkflowTask(NodeRef taskListNodeRef) {

		String workflowInstanceId = (String) nodeService.getProperty(taskListNodeRef, ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
		if ((workflowInstanceId != null) && !workflowInstanceId.isEmpty()) {
			deleteWorkflowById(workflowInstanceId);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteWorkflowById(String workflowInstanceId) {
		WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);
		if (workflowInstance != null) {
			workflowService.deleteWorkflow(workflowInstanceId);
		}

	}

}
