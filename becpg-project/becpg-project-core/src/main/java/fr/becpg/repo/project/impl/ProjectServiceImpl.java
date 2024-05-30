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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulationPlugin;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.policy.ProjectListPolicy;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.data.PermissionModel;
import fr.becpg.repo.security.plugins.SecurityServicePlugin;

/**
 * Project service that manage project
 *
 * @author quere
 * @version $Id: $Id
 */

@Service("projectService")
public class ProjectServiceImpl implements ProjectService, FormulationPlugin, SecurityServicePlugin {

	private static final Log logger = LogFactory.getLog(ProjectServiceImpl.class);

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	@Autowired
	private AssociationService associationService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private SiteService siteService;
	@Autowired
	private FormulationService<ProjectData> formulationService;
	@Autowired
	private AuthorityDAO authorityDAO;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private ScriptService scriptService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private BehaviourFilter policyBehaviourFilter;
	@Autowired
	private ProjectActivityService projectActivityService;
	@Autowired
	private ProjectListPolicy projectListPolicy;
	@Autowired
	private EntityDictionaryService entityDictionaryService;
	@Autowired
	private PersonService personService;
	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	SysAdminParams sysAdminParams;

	/** {@inheritDoc} */
	@Override
	public void openDeliverable(NodeRef deliverableNodeRef) {

		logger.debug("open Deliverable " + deliverableNodeRef);
		NodeRef taskNodeRef = associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);
		if (taskNodeRef != null) {
			nodeService.setProperty(taskNodeRef, ProjectModel.PROP_TL_STATE, TaskState.InProgress.toString());
		} else {
			logger.warn("Task is not defined for the deliverable. nodeRef: " + deliverableNodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void reopenTask(NodeRef taskNodeRef) {

		logger.debug("open Task " + taskNodeRef);
		List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(taskNodeRef, ProjectModel.ASSOC_DL_TASK);
		for (AssociationRef sourceAssoc : sourceAssocs) {
			String dlState = (String) nodeService.getProperty(sourceAssoc.getSourceRef(), ProjectModel.PROP_DL_STATE);
			if (DeliverableState.Completed.toString().equals(dlState)) {
				nodeService.setProperty(sourceAssoc.getSourceRef(), ProjectModel.PROP_DL_STATE, DeliverableState.InProgress.toString());
			}
		}

		nodeService.setProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_COMMENT, null);

	}

	@Override
	public Set<NodeRef> updateProjectState(NodeRef projectNodeRef, String beforeState, String afterState) {
		Set<NodeRef> toReformulates = new HashSet<>();

		try {
			// Disable notifications
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
			policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
			policyBehaviourFilter.disableBehaviour(ProjectModel.ASPECT_BUDGET);
			if (ProjectState.InProgress.toString().equals(afterState)) {
				if (beforeState == null || beforeState.isEmpty() || ProjectState.Planned.toString().equals(beforeState)) {

					Date startDate = ProjectHelper.removeTime(new Date());
					nodeService.setProperty(projectNodeRef, ProjectModel.PROP_PROJECT_START_DATE, startDate);
					ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
					for (TaskListDataItem taskListDataItem : ProjectHelper.getNextTasks(projectData, null)) {
						if (taskListDataItem.getSubProject() == null) {
							nodeService.setProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_START, startDate);
						}
					}
				} else {
					ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
					for (TaskListDataItem taskListDataItem : projectData.getTaskList()) {
						String previousState = (String) nodeService.getProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_PREVIOUS_STATE);
						if (previousState != null && !previousState.isEmpty()) {
							nodeService.setProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_PREVIOUS_STATE, null);
							if (taskListDataItem.getSubProject() != null) {
								nodeService.setProperty(taskListDataItem.getSubProject(), ProjectModel.PROP_PROJECT_STATE, previousState);
								updateProjectState(taskListDataItem.getSubProject(), taskListDataItem.getState(), previousState);
								toReformulates.add(taskListDataItem.getSubProject());

							}
							nodeService.setProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_STATE, previousState);

						}

					}

				}
				toReformulates.add(projectNodeRef);
			} else if (ProjectState.Cancelled.toString().equals(afterState) || ProjectState.OnHold.toString().equals(afterState)
					|| ProjectState.Completed.toString().equals(beforeState)) {

				ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
				for (TaskListDataItem taskListDataItem : projectData.getTaskList()) {
					if (TaskState.InProgress.equals(taskListDataItem.getTaskState())) {
						nodeService.setProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_PREVIOUS_STATE, taskListDataItem.getState());
						if (taskListDataItem.getSubProject() != null) {
							String previousState = (String) nodeService.getProperty(taskListDataItem.getSubProject(),
									ProjectModel.PROP_TL_PREVIOUS_STATE);
							if (!afterState.equals(previousState)) {
								nodeService.setProperty(taskListDataItem.getSubProject(), ProjectModel.PROP_PROJECT_STATE, afterState);
								updateProjectState(taskListDataItem.getSubProject(), previousState, afterState);
								toReformulates.add(taskListDataItem.getSubProject());
							}
						}
						nodeService.setProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_STATE, afterState);

					}

				}

				toReformulates.add(projectNodeRef);

			}

		} finally {
			policyBehaviourFilter.enableBehaviour(ProjectModel.ASPECT_BUDGET);
			policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
		}

		return toReformulates;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getTaskLegendList() {
		return BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_TASK_LEGEND).addSort(BeCPGModel.PROP_SORT, true).inDB().list();
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getProjectsContainer(String siteId) {
		if ((siteId != null) && (siteId.length() > 0)) {
			return siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void formulate(NodeRef projectNodeRef) {
		formulate(projectNodeRef, new HashSet<>());
	}

	private void formulate(NodeRef projectNodeRef, Set<NodeRef> visited) {
		if (nodeService.getType(projectNodeRef).equals(ProjectModel.TYPE_PROJECT)) {
			try {

				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_LOG_TIME_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_SCORE_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_BUDGET_LIST);
				policyBehaviourFilter.disableBehaviour(ProjectModel.ASPECT_BUDGET);
				policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_SYSTEM_ENTITY);

				L2CacheSupport.doInCacheContext(() -> {
					AuthenticationUtil.runAsSystem(() -> {

						formulationService.formulate(projectNodeRef);

						visited.add(projectNodeRef);

						NodeRef parentProjectNodeRef = associationService.getTargetAssoc(projectNodeRef, ProjectModel.ASSOC_PARENT_PROJECT);

						// Check for parent project
						if (parentProjectNodeRef != null) {
							if (!visited.contains(parentProjectNodeRef)) {
								formulate(parentProjectNodeRef, visited);
							} else {
								throw new StackOverflowError("Loop in parent project formulation :" + parentProjectNodeRef);
							}
						}

						return true;
					});

				}, false, true);

			} finally {
				policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_LOG_TIME_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_SCORE_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_BUDGET_LIST);
				policyBehaviourFilter.enableBehaviour(ProjectModel.ASPECT_BUDGET);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_SYSTEM_ENTITY);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteTask(NodeRef taskListNodeRef) {

		// update prevTasks assoc of next tasks
		List<NodeRef> deleteTaskPrevTaskNodeRefs = associationService.getTargetAssocs(taskListNodeRef, ProjectModel.ASSOC_TL_PREV_TASKS);
		List<NodeRef> nextTaskAssociationRefs = associationService.getSourcesAssocs(taskListNodeRef, ProjectModel.ASSOC_TL_PREV_TASKS);

		for (NodeRef nextTaskAssociationRef : nextTaskAssociationRefs) {

			if (!nodeService.hasAspect(nextTaskAssociationRef, ContentModel.ASPECT_PENDING_DELETE)) {

				List<NodeRef> nextTaskPrevTaskNodeRefs = associationService.getTargetAssocs(nextTaskAssociationRef, ProjectModel.ASSOC_TL_PREV_TASKS);

				if (nextTaskPrevTaskNodeRefs.contains(taskListNodeRef)) {
					nextTaskPrevTaskNodeRefs.remove(taskListNodeRef);
				}

				for (NodeRef deleteTaskPrevTaskNodeRef : deleteTaskPrevTaskNodeRefs) {
					nextTaskPrevTaskNodeRefs.add(deleteTaskPrevTaskNodeRef);
				}
				associationService.update(nextTaskAssociationRef, ProjectModel.ASSOC_TL_PREV_TASKS, nextTaskPrevTaskNodeRefs);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public void submitTask(NodeRef nodeRef, String taskComment) {
		try {
			// Disable notifications
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
			policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
			policyBehaviourFilter.disableBehaviour(ProjectModel.ASPECT_BUDGET);

			logger.debug("Call submitTask");

			NodeRef commentNodeRef = null;

			if ((taskComment != null) && !taskComment.isEmpty()) {
				commentNodeRef = commentService.createComment(nodeRef, "", taskComment, false);
				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_TASK_COMMENT, taskComment);
			} else {
				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_TASK_COMMENT, null);
			}

			Date startDate = (Date) nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_START);
			Date endDate = ProjectHelper.removeTime(new Date());
			// we want to keep the planned duration to calculate overdue
			nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_END, endDate);
			// milestone duration is maximum 1 day or startDate is after endDate
			Boolean isMileStone = (Boolean) nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_IS_MILESTONE);
			if (((isMileStone != null) && isMileStone) || ((startDate == null) || startDate.after(endDate))) {
				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_START, endDate);
			}

			nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_STATE, TaskState.Completed.toString());

			projectActivityService.postTaskStateChangeActivity(nodeRef, commentNodeRef, TaskState.InProgress.toString(),
					TaskState.Completed.toString(), false);

			projectListPolicy.queueListItem(nodeRef);

		} finally {
			policyBehaviourFilter.enableBehaviour(ProjectModel.ASPECT_BUDGET);
			policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
		}

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef refusedTask(NodeRef nodeRef, String taskComment) {

		try {
			// Disable notifications
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
			policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
			policyBehaviourFilter.disableBehaviour(ProjectModel.ASPECT_BUDGET);
			return AuthenticationUtil.runAsSystem(() -> {

				logger.debug("Call refusedTask");

				NodeRef taskNodeRef = associationService.getTargetAssoc(nodeRef, ProjectModel.ASSOC_TL_REFUSED_TASK_REF);

				NodeRef commentNodeRef = null;
				if ((taskNodeRef != null) && (taskComment != null) && !taskComment.isEmpty()) {
					commentService.createComment(nodeRef, "", taskComment, false);
					commentNodeRef = commentService.createComment(taskNodeRef, "", taskComment, false);
					nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_TASK_COMMENT, taskComment);
				} else {
					nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_TASK_COMMENT, null);
				}

				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_STATE, TaskState.Refused.toString());
				nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_IS_REFUSED, false);

				projectActivityService.postTaskStateChangeActivity(nodeRef, commentNodeRef, TaskState.InProgress.toString(),
						TaskState.Refused.toString(), false);

				projectListPolicy.queueListItem(nodeRef);

				return taskNodeRef;
			});
		} finally {
			policyBehaviourFilter.enableBehaviour(ProjectModel.ASPECT_BUDGET);
			policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
		}

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef refusedTask(NodeRef nodeRef) {
		nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_IS_REFUSED, false);
		return nodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef reassignTask(NodeRef taskNodeRef, String user) {

		try {
			// Disable notifications
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
			policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);

			logger.debug("Call reassignTask to: " + user);
			NodeRef userNodeRef = authorityDAO.getAuthorityNodeRefOrNull(user);
			if (userNodeRef != null) {
				List<NodeRef> resources = associationService.getTargetAssocs(taskNodeRef, ProjectModel.ASSOC_TL_RESOURCES);
				if ((resources.size() == 1) && !resources.contains(userNodeRef)
						&& !resources.contains(getReassignedResource(userNodeRef, new HashSet<>()))) {
					QName type = nodeService.getType(resources.get(0));
					if (!type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
						associationService.update(taskNodeRef, ProjectModel.ASSOC_TL_RESOURCES, Arrays.asList(userNodeRef));
					}
				}
			} else {
				logger.error("Cannot find user for assignee: " + user);
			}

			return taskNodeRef;
		} finally {
			policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> extractResources(NodeRef projectNodeRef, List<NodeRef> resources) {
		List<NodeRef> ret = new ArrayList<>();

		for (NodeRef resourceNodeRef : resources) {
			String authorityName = authorityDAO.getAuthorityName(resourceNodeRef);
			if (ProjectHelper.isRoleAuhtority(authorityName)) {
				logger.debug("Found project role : " + authorityName);
				QName propName = extractRolePropName(authorityName);
				if (propName != null) {
					Object user = nodeService.getProperty(projectNodeRef, propName);
					logger.debug("Try getting : " + propName + " : " + user);

					if (user != null) {
						if (user instanceof String) {
							NodeRef userNodeRef = authorityDAO.getAuthorityNodeRefOrNull((String) user);
							if (userNodeRef != null) {
								if (logger.isDebugEnabled()) {
									logger.debug("Adding user :" + authorityDAO.getAuthorityName(userNodeRef));
								}
								ret.add(userNodeRef);
							}
						} else if (user instanceof NodeRef) {
							if (logger.isDebugEnabled()) {
								logger.debug("Adding user :" + authorityDAO.getAuthorityName((NodeRef) user));
							}
							ret.add((NodeRef) user);
						}
					} else {
						logger.debug("Try getting assoc: " + propName);
						List<AssociationRef> assocs = nodeService.getTargetAssocs(projectNodeRef, propName);
						if (assocs != null) {
							for (AssociationRef assoc : assocs) {
								if (logger.isDebugEnabled()) {
									logger.debug("Adding user :" + authorityDAO.getAuthorityName(assoc.getTargetRef()));
								}
								ret.add(assoc.getTargetRef());
							}
						}
					}
				}
			} else {
				ret.add(resourceNodeRef);

			}
		}

		return ret;

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getReassignedResource(NodeRef resource, Set<NodeRef> reassignedCandidates) {

		if ((resource != null) && (nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_STATE) != null)
				&& (Boolean.TRUE.equals(nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_STATE)))) {

			Date delegationStart = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_START);
			Date delegationEnd = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_END);

			if ((delegationStart == null) || ((delegationStart.before(new Date()) || delegationStart.equals(new Date()))
					&& ((delegationEnd == null) || delegationEnd.after(new Date()) || delegationEnd.equals(new Date())))) {

				NodeRef reassignedResource = associationService.getTargetAssoc(resource, ProjectModel.PROP_QNAME_REASSIGN_RESOURCE);

				if (reassignedCandidates.contains(reassignedResource)) {
					return resource;
				}

				reassignedCandidates.add(reassignedResource);

				NodeRef nextReassignResource = getReassignedResource(reassignedResource, reassignedCandidates);

				if (nextReassignResource != null) {
					return nextReassignResource;
				}

				return reassignedResource;
			}
		}
		return null;
	}

	private QName extractRolePropName(String authorityName) {
		String propName = authorityName
				.substring((PermissionService.GROUP_PREFIX + ProjectRepoConsts.PROJECT_GROUP_PREFIX).length(), authorityName.length())
				.replace("_", ":");

		return QName.createQName(propName, namespaceService);
	}

	/** {@inheritDoc} */
	@Override
	public void updateProjectPermission(NodeRef projectNodeRef, NodeRef taskListNodeRef, NodeRef resourceNodeRef, boolean allow) {
		if (ProjectModel.TYPE_PROJECT.equals(nodeService.getType(projectNodeRef))) {

			List<NodeRef> nodeRefs = new ArrayList<>(1);
			nodeRefs.add(taskListNodeRef);

			if ((resourceNodeRef != null) && nodeService.exists(resourceNodeRef)) {
				String authorityName = authorityDAO.getAuthorityName(resourceNodeRef);

				if ((authorityName != null) && !ProjectHelper.isRoleAuhtority(authorityName)) {
					logger.debug("Set permission for authority: " + authorityName + " allow :" + allow);
					ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
					List<DeliverableListDataItem> deliverableList = ProjectHelper.getDeliverables(projectData, taskListNodeRef);
					for (DeliverableListDataItem dl : deliverableList) {
						nodeRefs.add(dl.getNodeRef());
					}

					for (NodeRef n : nodeRefs) {
						if (allow) {
							boolean updatePerm = true;
							for (AccessPermission perm : permissionService.getAllSetPermissions(n)) {
								if (authorityName.equals(perm.getAuthority()) && PermissionService.COORDINATOR.equals(perm.getPermission())) {
									updatePerm = false;
									break;
								}
							}
							if (updatePerm) {
								permissionService.setPermission(n, authorityName, PermissionService.COORDINATOR, true);
							}
						} else {
							permissionService.clearPermission(n, authorityName);
						}

					}
				}
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public void runScript(ProjectData project, TaskListDataItem task, DeliverableListDataItem deliverable) {

		NodeRef scriptNode = deliverable.getContent();

		if ((scriptNode != null) && nodeService.exists(scriptNode)
				&& nodeService.getPath(scriptNode).toPrefixString(namespaceService).startsWith(RepoConsts.SCRIPTS_FULL_PATH)) {

			String userName = AuthenticationUtil.getFullyAuthenticatedUser();

			Map<String, Object> model = new HashMap<>();

			logger.debug("Run task script ");

			model.put("currentUser", userName);
			model.put("task", task);
			model.put("project", project);
			model.put("deliverable", deliverable);
			model.put("shareUrl", sysAdminParams.getShareProtocol() + "://" + sysAdminParams.getShareHost() + ":" + sysAdminParams.getSharePort()
					+ "/" + sysAdminParams.getShareContext());
			try {
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_PROJECT);

				scriptService.executeScript(scriptNode, ContentModel.PROP_CONTENT, model);
			} finally {
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_PROJECT);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Long getNbProjectsByLegend(NodeRef legendNodeRef, String siteId) {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT)
				.andPropEquals(ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress.toString());
		if (legendNodeRef == null) {
			queryBuilder.isNull(ProjectModel.PROP_PROJECT_LEGENDS);
		} else {
			queryBuilder.andPropEquals(ProjectModel.PROP_PROJECT_LEGENDS, legendNodeRef.toString());
		}
		if (siteId != null) {
			queryBuilder.inSite(siteId, null);
		}
		return queryBuilder.count();
	}

	/** {@inheritDoc} */
	@Override
	public FormulationPluginPriority getMatchPriority(QName type) {
		return entityDictionaryService.isSubClass(type, ProjectModel.TYPE_PROJECT) ? FormulationPluginPriority.NORMAL
				: FormulationPluginPriority.NONE;

	}

	/** {@inheritDoc} */
	@Override
	public void runFormulation(NodeRef entityNodeRef, String chainId) {
		formulate(entityNodeRef);
	}

	@Override
	public boolean checkIsInSecurityGroup(NodeRef nodeRef, PermissionModel permissionModel) {
		if (nodeRef != null) {
			for (NodeRef groupNodeRef : permissionModel.getGroups()) {
				String authorityName = authorityDAO.getAuthorityName(groupNodeRef);
				if (ProjectHelper.isRoleAuhtority(authorityName)) {
					List<NodeRef> resources = extractResources(nodeRef, Arrays.asList(groupNodeRef));
					if (resources.contains(personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser()))) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean accept(QName nodeType) {
		return ProjectModel.TYPE_PROJECT.equals(nodeType);
	}

	@Override
	public TaskListDataItem createNewTask(ProjectData project) {
		NodeRef listContainer = entityListDAO.getListContainer(project.getNodeRef());

		NodeRef taskList = entityListDAO.getList(listContainer, ProjectModel.TYPE_TASK_LIST);

		NodeRef newTaskNodeRef = entityListDAO.createListItem(taskList, ProjectModel.TYPE_TASK_LIST, null, new HashMap<>());

		TaskListDataItem newTask = (TaskListDataItem) alfrescoRepository.findOne(newTaskNodeRef);

		project.getTaskList().add(newTask);

		return newTask;

	}

}
