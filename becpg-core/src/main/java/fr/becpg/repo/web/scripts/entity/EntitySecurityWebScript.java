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
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowPackageComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.filter.SecurityContextHelper;
import fr.becpg.repo.web.scripts.remote.AbstractEntityWebScript;

/**
 * WebScript to check security access for a given entity (tasks + datalists validation)
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntitySecurityWebScript extends AbstractEntityWebScript {

	private static final Log logger = LogFactory.getLog(EntitySecurityWebScript.class);

	private WorkflowService workflowService;

	private WorkflowPackageComponent workflowPackageComponent;

	private EntityListDAO entityListDAO;

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	private NodeRef resolveEntityNodeRef(WebScriptRequest req) {
		Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
		if (templateVars != null) {
			String storeType = templateVars.get("store_type");
			String storeId = templateVars.get("store_id");
			String id = templateVars.get("id");
			if (storeType != null && storeId != null && id != null) {
				NodeRef nodeRef = new NodeRef(storeType, storeId, id);
				if (nodeService.exists(nodeRef)) {
					if (AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(nodeRef))) {
						return nodeRef;
					}
					throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
				}
				throw new WebScriptException(Status.STATUS_NOT_FOUND, "Node " + nodeRef + " doesn't exist in repository");
			}
		}
		return findEntity(req);
	}

	/**
	 * <p>Setter for the field <code>workflowService</code>.</p>
	 *
	 * @param workflowService a {@link org.alfresco.service.cmr.workflow.WorkflowService} object.
	 */
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * <p>Setter for the field <code>workflowPackageComponent</code>.</p>
	 *
	 * @param workflowPackageComponent a {@link org.alfresco.repo.workflow.WorkflowPackageComponent} object.
	 */
	public void setWorkflowPackageComponent(WorkflowPackageComponent workflowPackageComponent) {
		this.workflowPackageComponent = workflowPackageComponent;
	}

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		NodeRef entityNodeRef = resolveEntityNodeRef(req);

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Checking tasks for entity: " + entityNodeRef);
			}

			// Set security context if skipSecurityRules parameter is present
			String skipSecurityRules = req.getParameter("skipSecurityRules");
			if (skipSecurityRules != null && Boolean.parseBoolean(skipSecurityRules)) {
				SecurityContextHelper.setSkipSecurityRules(true);
			}

			boolean hasAssignedTask = false;

			// Check if task assignment should be verified (wizard configuration)
			String checkTaskParam = req.getParameter("checkTaskAssignment");
			boolean checkTaskAssignment = checkTaskParam != null && Boolean.parseBoolean(checkTaskParam);

			if (checkTaskAssignment) {
				hasAssignedTask = checkUserHasAssignedTask(entityNodeRef);
				if (logger.isDebugEnabled()) {
					logger.debug("Computed and cached task assignment result: " + hasAssignedTask);
				}
			}

			// Get datalists validation status
			JSONArray datalists = getEntityDataLists(entityNodeRef);

			int accessMode = securityService.computeAccessMode(entityNodeRef, nodeService.getType(entityNodeRef), (String) null);

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");

			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("hasAssignedTask", hasAssignedTask);
			jsonResponse.put("accessMode", accessMode);
			jsonResponse.put("datalists", datalists);
			resp.getWriter().write(jsonResponse.toString());

		} catch (Exception e) {
			logger.error("Cannot check tasks for entity " + entityNodeRef + " for user " + AuthenticationUtil.getFullyAuthenticatedUser(), e);
			throw new WebScriptException("Failed to check tasks", e);
		} finally {
			// Always clear the thread local
			SecurityContextHelper.clear();
		}
	}

	/**
	 * Check if current user has assigned tasks for the given entity
	 *
	 * @param entityNodeRef the entity node reference
	 * @return true if user has assigned tasks, false otherwise
	 */
	private boolean checkUserHasAssignedTask(NodeRef entityNodeRef) {
		if (entityNodeRef == null) {
			return false;
		}

		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

		List<String> contentWorkflowIds = workflowPackageComponent.getWorkflowIdsForContent(entityNodeRef);

		if (contentWorkflowIds.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("No workflows found for entity: " + entityNodeRef);
			}
			return false;
		}

		// Check assigned tasks
		List<WorkflowTask> assignedTasks = workflowService.getAssignedTasks(currentUser, WorkflowTaskState.IN_PROGRESS);

		boolean hasMatchingTask = assignedTasks.stream().anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId()));

		if (hasMatchingTask) {
			if (logger.isDebugEnabled()) {
				logger.debug("User " + currentUser + " has assigned tasks for entity: " + entityNodeRef);
			}
			return true;
		}

		// Check pooled tasks
		List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(currentUser);
		hasMatchingTask = pooledTasks.stream().anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId()));

		if (hasMatchingTask) {
			if (logger.isDebugEnabled()) {
				logger.debug("User " + currentUser + " has pooled tasks for entity: " + entityNodeRef);
			}
		}

		return hasMatchingTask;
	}

	/**
	 * Get entity datalists with their validation states
	 *
	 * @param entityNodeRef the entity node reference
	 * @return JSONArray of datalists with their states
	 */
	private JSONArray getEntityDataLists(NodeRef entityNodeRef) {
		JSONArray datalists = new JSONArray();

		if (entityNodeRef != null && entityListDAO != null) {
			try {
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
				if (listContainerNodeRef != null) {
					List<NodeRef> listsNodeRef = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);

					for (NodeRef listNodeRef : listsNodeRef) {
						JSONObject listObj = new JSONObject();

						String listName = (String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);
						
						listObj.put(EntityListsWebScript.KEY_NAME_NAME, listName);

						listObj.put(EntityListsWebScript.KEY_NAME_NODE_REF, listNodeRef.toString());

						listObj.put(EntityListsWebScript.KEY_NAME_ITEM_TYPE, defaultValue(nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),""));

						listObj.put(EntityListsWebScript.KEY_NAME_TITLE, defaultValue(nodeService.getProperty(listNodeRef, ContentModel.PROP_TITLE), listName));

						listObj.put(EntityListsWebScript.KEY_NAME_DESCRIPTION, defaultValue(nodeService.getProperty(listNodeRef, ContentModel.PROP_DESCRIPTION), ""));


						String state = "ToValidate";
						String stateValue = (String) nodeService.getProperty(listNodeRef, BeCPGModel.PROP_ENTITYLIST_STATE);
						if (stateValue != null) {
							state = stateValue;
						}
						listObj.put(EntityListsWebScript.KEY_NAME_STATE, state);

						datalists.put(listObj);
					}
				}
			} catch (Exception e) {
				logger.error("Error getting datalists for entity: " + entityNodeRef, e);
			}
		}

		return datalists;
	}
	
	private Serializable defaultValue(Serializable val, Serializable def) {
		if (val != null) {
			return val;
		} else {
			return def;
		}
	}
}
