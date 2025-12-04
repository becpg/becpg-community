/*******************************************************************************
 * Copyright (C) 2010-2025 beCPG.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectNotificationEvent;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * Service for managing project workflows
 *
 * @author quere
 * @version $Id: $Id
 */
@Service("projectWorkflowService")
public class ProjectWorkflowServiceImpl implements ProjectWorkflowService {

	// Constants
	private static final String WORKFLOW_DESCRIPTION_FORMAT = "%s - %s - %s";
	private static final String DEFAULT_INITIATOR = "System";
	private static final int DEFAULT_PRIORITY_NORMAL = 2;

	private static final Log logger = LogFactory.getLog(ProjectWorkflowServiceImpl.class);

	// Services
	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AutoNumService autoNumService;

	@Autowired
	private WorkflowNotificationUtils workflowNotificationUtils;

	/**
	 * Internal class to hold separated assignees (users vs groups)
	 */
	private static class AssigneesSplit {
		private final List<NodeRef> users;
		private final List<NodeRef> groups;

		public AssigneesSplit(List<NodeRef> users, List<NodeRef> groups) {
			this.users = users != null ? users : Collections.emptyList();
			this.groups = groups != null ? groups : Collections.emptyList();
		}

		public List<NodeRef> getUsers() {
			return users;
		}

		public List<NodeRef> getGroups() {
			return groups;
		}

		public boolean hasSingleUser() {
			return (users.size() == 1) && groups.isEmpty();
		}

		public NodeRef getSingleUser() {
			return hasSingleUser() ? users.get(0) : null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void cancelWorkflow(TaskListDataItem task) {
		if ((task == null) || StringUtils.isBlank(task.getWorkflowInstance())) {
			logger.warn("Cannot cancel workflow: task or workflow instance is null/empty");
			return;
		}

		String workflowId = task.getWorkflowInstance();
		logger.debug("Cancelling workflow instance: " + workflowId);

		try {
			WorkflowInstance instance = workflowService.cancelWorkflow(workflowId);
			if ((instance == null) || !instance.isActive()) {
				clearWorkflowReferences(task);
				logger.debug("Workflow cancelled successfully: " + workflowId);
			} else {
				logger.error("Failed to cancel workflow: " + workflowId + " - instance is still active");
			}
		} catch (Exception e) {
			logger.error("Error cancelling workflow: " + workflowId, e);
			clearWorkflowReferences(task);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void startWorkflow(final ProjectData projectData, final TaskListDataItem taskListDataItem,
			final List<DeliverableListDataItem> nextDeliverables) {

		if ((projectData == null) || (taskListDataItem == null)) {
			throw new IllegalArgumentException("ProjectData and TaskListDataItem cannot be null");
		}

		final String authenticatedUser = determineWorkflowInitiator(projectData);
		final String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();

		try {
			AuthenticationUtil.setFullyAuthenticatedUser(authenticatedUser);
			executeWorkflowStart(projectData, taskListDataItem);
		} catch (Exception e) {
			logger.error("Failed to start workflow for task: " + taskListDataItem.getTaskName(), e);
			throw new WorkflowException("Failed to start workflow", e);
		} finally {
			AuthenticationUtil.setFullyAuthenticatedUser(fullyAuthenticatedUser);
		}
	}

	/**
	 * Execute the workflow start process
	 */
	private void executeWorkflowStart(ProjectData projectData, TaskListDataItem taskListDataItem) {
		String workflowDescription = calculateWorkflowDescription(projectData, taskListDataItem);
		Map<QName, Serializable> workflowProps = buildWorkflowProperties(projectData, taskListDataItem, workflowDescription);

		NodeRef wfPackage = createWorkflowPackage(projectData);
		workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

		String workflowDefId = getWorkflowDefId(taskListDataItem.getWorkflowName());
		if (workflowDefId == null) {
			throw new WorkflowException("Workflow definition not found: " + taskListDataItem.getWorkflowName());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Starting workflow: " + workflowDefId + " with properties: " + workflowProps);
		}

		WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, workflowProps);
		String workflowId = wfPath.getInstance().getId();
		taskListDataItem.setWorkflowInstance(workflowId);

		logger.debug("Workflow started successfully. ID: " + workflowId + " - Description: " + workflowDescription);

		completeStartTaskAndSetProgress(workflowId, taskListDataItem);
	}

	/**
	 * Build workflow properties map
	 */
	private Map<QName, Serializable> buildWorkflowProperties(ProjectData projectData, TaskListDataItem taskListDataItem, String workflowDescription) {

		Map<QName, Serializable> workflowProps = new HashMap<>();

		if (taskListDataItem.getDue() != null) {
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, taskListDataItem.getDue());
		}

		Integer priority = projectData.getPriority() != null ? projectData.getPriority() : DEFAULT_PRIORITY_NORMAL;
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, priority);
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);

		AssigneesSplit assignees = splitAssignees(taskListDataItem.getResources());
		if (!assignees.getUsers().isEmpty()) {
			logger.debug("Adding " + assignees.getUsers().size() + " user assignees to workflow");
			workflowProps.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable) assignees.getUsers());
		}
		if (!assignees.getGroups().isEmpty()) {
			logger.debug("Adding " + assignees.getGroups().size() + " group assignees to workflow");
			workflowProps.put(WorkflowModel.ASSOC_GROUP_ASSIGNEES, (Serializable) assignees.getGroups());
		}

		workflowProps.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, shouldNotify(projectData, taskListDataItem));
		workflowProps.put(ProjectModel.ASSOC_WORKFLOW_TASK, taskListDataItem.getNodeRef());
		workflowProps.put(BeCPGModel.ASSOC_WORKFLOW_ENTITY, projectData.getNodeRef());

		return workflowProps;
	}

	/**
	 * Create workflow package with project and entities
	 */
	private NodeRef createWorkflowPackage(ProjectData projectData) {
		NodeRef wfPackage = workflowService.createPackage(null);
		nodeService.addChild(wfPackage, projectData.getNodeRef(), WorkflowModel.ASSOC_PACKAGE_CONTAINS, ContentModel.ASSOC_CHILDREN);

		if ((projectData.getEntities() != null) && !projectData.getEntities().isEmpty()) {
			for (NodeRef entity : projectData.getEntities()) {
				if (nodeService.exists(entity)) {
					nodeService.addChild(wfPackage, entity, WorkflowModel.ASSOC_PACKAGE_CONTAINS, ContentModel.ASSOC_CHILDREN);
				}
			}
		}

		return wfPackage;
	}

	/**
	 * Complete start task and set workflow to in progress
	 */
	private void completeStartTaskAndSetProgress(String workflowId, TaskListDataItem taskListDataItem) {
		WorkflowTask startTask = workflowService.getStartTask(workflowId);

		try {
			workflowService.endTask(startTask.getId(), null);
		} catch (WorkflowException err) {
			logger.error("Failed to end start task for workflow: " + workflowId, err);
			throw err;
		}

		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

		if (!workflowTasks.isEmpty()) {
			WorkflowTask workflowTask = workflowTasks.get(0);
			Map<QName, Serializable> taskProps = workflowTask.getProperties();
			taskProps.put(WorkflowModel.PROP_STATUS, WorkflowConstants.TASK_STATUS_IN_PROGRESS);
			workflowService.updateTask(workflowTask.getId(), taskProps, null, null);
			taskListDataItem.setWorkflowTaskInstance(workflowTask.getId());
		}
	}

	/**
	 * Determine who should initiate the workflow
	 */
	private String determineWorkflowInitiator(ProjectData projectData) {
		if (projectData.getProjectManager() != null) {
			return (String) nodeService.getProperty(projectData.getProjectManager(), ContentModel.PROP_USERNAME);
		}

		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
		return StringUtils.isNotBlank(currentUser) ? currentUser : DEFAULT_INITIATOR;
	}

	/**
	 * Check if notifications should be sent for this task
	 */
	@SuppressWarnings("unchecked")
	private boolean shouldNotify(ProjectData projectData, TaskListDataItem task) {
		List<String> notificationEvents = (List<String>) nodeService.getProperty(task.getNodeRef(), ProjectModel.PROP_OBSERVERS_EVENTS);

		if ((notificationEvents == null) || notificationEvents.isEmpty()) {
			return true;
		}

		boolean notify = true;
		for (String notificationEvent : notificationEvents) {
			try {
				ProjectNotificationEvent event = ProjectNotificationEvent.valueOf(notificationEvent);

				if (ProjectNotificationEvent.NotifyOnRefused.equals(event)) {
					if (isReopenedAfterRefuse(projectData, task)) {
						return true;
					}
					notify = false;
				} else if (ProjectNotificationEvent.NotifyDisabled.equals(event)) {
					notify = false;
				}
			} catch (IllegalArgumentException e) {
				logger.warn("Unknown notification event: " + notificationEvent);
			}
		}

		return notify;
	}

	/**
	 * Check if task was reopened after being refused
	 */
	private boolean isReopenedAfterRefuse(ProjectData projectData, TaskListDataItem reopenedTask) {
		if (projectData.getTaskList() == null) {
			return false;
		}

		for (TaskListDataItem task : projectData.getTaskList()) {
			if (TaskState.Refused.equals(task.getTaskState())) {
				if (reopenedTask.equals(task.getRefusedTask()) || ((task.getRefusedTasksToReopen() != null) && task.getRefusedTasksToReopen().contains(reopenedTask.getNodeRef()))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Split resources into users and groups
	 */
	private AssigneesSplit splitAssignees(List<NodeRef> resources) {
		if ((resources == null) || resources.isEmpty()) {
			return new AssigneesSplit(Collections.emptyList(), Collections.emptyList());
		}

		List<NodeRef> users = new ArrayList<>();
		List<NodeRef> groups = new ArrayList<>();

		for (NodeRef resource : resources) {
			QName type = nodeService.getType(resource);
			if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)) {
				groups.add(resource);
			} else {
				users.add(resource);
			}
		}

		return new AssigneesSplit(users, groups);
	}

	/**
	 * Calculate localized workflow description
	 */
	private String calculateWorkflowDescription(ProjectData projectData, TaskListDataItem taskListDataItem) {
		String taskName = taskListDataItem.getTaskName();
		List<NodeRef> resources = taskListDataItem.getResources();

		if ((resources != null) && !resources.isEmpty()) {
			taskName = getLocalizedTaskName(taskListDataItem, resources);
		}

		return String.format(WORKFLOW_DESCRIPTION_FORMAT, getProjectCode(projectData), projectData.getName(), taskName);
	}

	/**
	 * Get localized task name based on assignee locale
	 */
	private String getLocalizedTaskName(TaskListDataItem taskListDataItem, List<NodeRef> resources) {
		AssigneesSplit assignees = splitAssignees(resources);
		Locale previousContentLocale = I18NUtil.getContentLocale();

		try {
			Locale localeToUse = determineLocale(assignees);
			if (localeToUse != null) {
				I18NUtil.setContentLocale(localeToUse);
				Serializable localizedName = nodeService.getProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_TASK_NAME);
				if (localizedName instanceof String ret) {
					return ret;
				}
			}
		} finally {
			I18NUtil.setContentLocale(previousContentLocale);
		}

		return taskListDataItem.getTaskName();
	}

	/**
	 * Determine locale for task description
	 */
	private Locale determineLocale(AssigneesSplit assignees) {
		if (assignees.hasSingleUser()) {
			return MLTextHelper.getUserContentLocale(nodeService, assignees.getSingleUser());
		}
		return Locale.getDefault();
	}

	/**
	 * Get project code or generate one
	 */
	private String getProjectCode(ProjectData projectData) {
		return projectData.getCode() != null ? projectData.getCode() : autoNumService.getOrCreateBeCPGCode(projectData.getNodeRef());
	}

	/**
	 * Get workflow definition ID by name
	 */
	private String getWorkflowDefId(String workflowName) {
		if (StringUtils.isBlank(workflowName)) {
			logger.error("Workflow name is blank");
			return null;
		}

		WorkflowDefinition def = workflowService.getDefinitionByName(workflowName);
		if (def == null) {
			logger.error("Unknown workflow name: " + workflowName);
			return null;
		}

		return def.getId();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isWorkflowActive(TaskListDataItem task) {
		if (task == null) {
			return false;
		}

		String workflowId = task.getWorkflowInstance();
		if (StringUtils.isBlank(workflowId)) {
			return false;
		}

		try {
			WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowId);

			if (workflowInstance == null) {
				logger.warn(String.format("Workflow instance not found - WorkflowId: %s, Task: %s (%s)", workflowId, task.getNodeRef(),
						task.getTaskName()));
				clearWorkflowReferences(task);
				return false;
			}

			if (workflowInstance.isActive()) {
				return true;
			}

			clearWorkflowReferences(task);
			return false;

		} catch (Exception e) {
			logger.error(String.format("Error retrieving workflow instance: %s for task %s (%s)", workflowId, task.getNodeRef(), task.getTaskName()),
					e);
			handleCorruptedWorkflow(workflowId);
			clearWorkflowReferences(task);
			return false;
		}
	}

	/**
	 * Clear workflow references from task
	 */
	private void clearWorkflowReferences(TaskListDataItem task) {
		task.setWorkflowInstance("");
		task.setWorkflowTaskInstance("");
	}

	/**
	 * Handle corrupted workflow by attempting deletion
	 */
	private void handleCorruptedWorkflow(String workflowId) {
		logger.info("Attempting to delete corrupted workflow instance: " + workflowId);
		try {
			workflowService.deleteWorkflow(workflowId);
			logger.info("Successfully deleted corrupted workflow instance: " + workflowId);
		} catch (Exception deleteException) {
			logger.error("Failed to delete corrupted workflow instance: " + workflowId, deleteException);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void checkWorkflowInstance(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> nextDeliverables) {

		if (StringUtils.isBlank(taskListDataItem.getWorkflowInstance())) {
			return;
		}

		if (!isWorkflowActive(taskListDataItem)) {
			clearWorkflowReferences(taskListDataItem);
			return;
		}

		if ((taskListDataItem.getResources() == null) || taskListDataItem.getResources().isEmpty()) {
			workflowService.cancelWorkflow(taskListDataItem.getWorkflowInstance());
			clearWorkflowReferences(taskListDataItem);
			return;
		}

		updateWorkflowTasks(projectData, taskListDataItem);
	}

	/**
	 * Update workflow tasks properties and assignees
	 */
	private void updateWorkflowTasks(ProjectData projectData, TaskListDataItem taskListDataItem) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(taskListDataItem.getWorkflowInstance());
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery, false);

		if (workflowTasks.isEmpty()) {
			logger.warn("No in-progress workflow tasks found for: " + taskListDataItem.getWorkflowInstance());
			return;
		}

		String workflowDescription = calculateWorkflowDescription(projectData, taskListDataItem);

		for (WorkflowTask workflowTask : workflowTasks) {
			NodeRef taskNodeRef = (NodeRef) workflowTask.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK);
			if ((taskNodeRef != null) && taskNodeRef.equals(taskListDataItem.getNodeRef())) {
				logger.debug("Checking task: " + taskListDataItem.getTaskName());
				updateSingleWorkflowTask(projectData, taskListDataItem, workflowTask, workflowDescription);
			}
		}
	}

	/**
	 * Update a single workflow task
	 */
	@SuppressWarnings("unchecked")
	private void updateSingleWorkflowTask(ProjectData projectData, TaskListDataItem taskListDataItem, WorkflowTask workflowTask,
			String workflowDescription) {

		Map<QName, Serializable> properties = new HashMap<>();
		Integer priority = projectData.getPriority() != null ? projectData.getPriority() : DEFAULT_PRIORITY_NORMAL;

		// Update basic properties
		addPropertyIfChanged(properties, WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription, workflowTask.getProperties());
		addPropertyIfChanged(properties, WorkflowModel.PROP_DESCRIPTION, workflowDescription, workflowTask.getProperties());
		addPropertyIfChanged(properties, WorkflowModel.PROP_WORKFLOW_DUE_DATE, taskListDataItem.getDue(), workflowTask.getProperties());
		addPropertyIfChanged(properties, WorkflowModel.PROP_DUE_DATE, taskListDataItem.getDue(), workflowTask.getProperties());
		addPropertyIfChanged(properties, WorkflowModel.PROP_WORKFLOW_PRIORITY, priority, workflowTask.getProperties());
		addPropertyIfChanged(properties, WorkflowModel.PROP_PRIORITY, priority, workflowTask.getProperties());
		addPropertyIfChanged(properties, WorkflowModel.PROP_STATUS, WorkflowConstants.TASK_STATUS_IN_PROGRESS, workflowTask.getProperties());

		// Update assignees (original logic preserved)
		AssigneesSplit assignees = splitAssignees(taskListDataItem.getResources());
		List<NodeRef> oldPooledActors = (List<NodeRef>) workflowTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
		String oldOwnerUsername = (String) workflowTask.getProperties().get(ContentModel.PROP_OWNER);

		if ((taskListDataItem.getResources().size() == 1) && (assignees.getUsers().size() == 1)) {
			String userName = (String) nodeService.getProperty(assignees.getUsers().get(0), ContentModel.PROP_USERNAME);
			addPropertyIfChanged(properties, ContentModel.PROP_OWNER, userName, workflowTask.getProperties());
			properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, new ArrayList<>());
		} else if ((oldPooledActors == null) || (oldPooledActors.size() != taskListDataItem.getResources().size())
				|| !oldPooledActors.containsAll(taskListDataItem.getResources())) {
			properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) taskListDataItem.getResources());
		}

		// Send notifications for new actors (original logic preserved)
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

				if (((oldPooledActors == null) || !oldPooledActors.contains(newActor)) && !authorityName.equals(oldOwnerUsername)) {
					newAuthorityNames.add(authorityName);
				}

			}

			if (!newAuthorityNames.isEmpty()) {
				workflowNotificationUtils.sendWorkflowAssignedNotificationEMail(workflowTask.getId(), null, newAuthorityNames.toArray(new String[0]),
						false);
			}
		}

		// Apply updates if any
		if (!properties.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Updating task " + taskListDataItem.getTaskName() + " with properties: " + properties);
			}
			workflowService.updateTask(workflowTask.getId(), properties, null, null);
			taskListDataItem.setWorkflowTaskInstance(workflowTask.getId());
		}
	}

	/**
	 * Add property to map only if value has changed
	 */
	private void addPropertyIfChanged(Map<QName, Serializable> properties, QName propertyQName, Serializable newValue,
			Map<QName, Serializable> currentProperties) {

		Serializable currentValue = currentProperties.get(propertyQName);

		if (!Objects.equals(currentValue, newValue)) {
			properties.put(propertyQName, newValue);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteWorkflowTask(NodeRef taskListNodeRef) {
		if (taskListNodeRef == null) {
			logger.warn("Cannot delete workflow task: taskListNodeRef is null");
			return;
		}

		String workflowInstanceId = (String) nodeService.getProperty(taskListNodeRef, ProjectModel.PROP_TL_WORKFLOW_INSTANCE);
		if (StringUtils.isNotBlank(workflowInstanceId)) {
			deleteWorkflowById(workflowInstanceId);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteWorkflowById(String workflowInstanceId) {
		if (StringUtils.isBlank(workflowInstanceId)) {
			logger.warn("Cannot delete workflow: workflowInstanceId is blank");
			return;
		}

		try {
			WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);
			if (workflowInstance != null) {
				workflowService.deleteWorkflow(workflowInstanceId);
				logger.debug("Workflow deleted successfully: " + workflowInstanceId);
			} else {
				logger.debug("Workflow instance not found, nothing to delete: " + workflowInstanceId);
			}
		} catch (Exception e) {
			logger.error("Error deleting workflow: " + workflowInstanceId, e);
		}
	}
}