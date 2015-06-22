/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DeliverableUrl;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Project service that manage project
 * 
 * @author quere
 * 
 */

@Service("projectService")
public class ProjectServiceImpl implements ProjectService {

	private static final Log logger = LogFactory.getLog(ProjectServiceImpl.class);

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;
	@Autowired
	private AssociationService associationService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private RepoService repoService;
	@Autowired
	private SiteService siteService;
	@Autowired
	private FormulationService<ProjectData> formulationService;
	@Autowired
	private ProjectWorkflowService projectWorkflowService;
	@Autowired
	private AuthorityDAO authorityDAO;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private ScriptService scriptService;

	@Autowired
	SysAdminParams sysAdminParams;

	@Autowired
	private EntityListDAO entityListDAO;

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

	}


	@Override
	public List<NodeRef> getTaskLegendList() {
		return BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_TASK_LEGEND).addSort(BeCPGModel.PROP_SORT, true).inDB().list();
	}

	@Override
	public NodeRef getProjectsContainer(String siteId) {
		if (siteId != null && siteId.length() > 0) {
			return siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
		}
		return null;
	}

	@Override
	public void cancel(NodeRef projectNodeRef) {

		logger.debug("cancel project: " + projectNodeRef + " exists ? " + nodeService.exists(projectNodeRef));
		if (nodeService.exists(projectNodeRef)) {
			ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);

			for (TaskListDataItem taskListDataItem : projectData.getTaskList()) {
				if (projectWorkflowService.isWorkflowActive(taskListDataItem)) {
					projectWorkflowService.cancelWorkflow(taskListDataItem);
				}
			}

			alfrescoRepository.save(projectData);
		}
	}

	@Override
	public void formulate(NodeRef projectNodeRef) throws FormulateException {
		if (nodeService.getType(projectNodeRef).equals(ProjectModel.TYPE_PROJECT)) {
			logger.debug("Formulate project : "+projectNodeRef);
			formulationService.formulate(projectNodeRef);
		}
	}

	@Override
	public void deleteTask(NodeRef taskListNodeRef) {

		// update prevTasks assoc of next tasks
		List<NodeRef> deleteTaskPrevTaskNodeRefs = associationService.getTargetAssocs(taskListNodeRef, ProjectModel.ASSOC_TL_PREV_TASKS);
		List<AssociationRef> nextTaskAssociationRefs = nodeService.getSourceAssocs(taskListNodeRef, ProjectModel.ASSOC_TL_PREV_TASKS);
		
		for (AssociationRef nextTaskAssociationRef : nextTaskAssociationRefs) {
			
			if(!nodeService.hasAspect(nextTaskAssociationRef.getSourceRef(), ContentModel.ASPECT_PENDING_DELETE)){
				
				List<NodeRef> nextTaskPrevTaskNodeRefs = associationService.getTargetAssocs(nextTaskAssociationRef.getSourceRef(),
						ProjectModel.ASSOC_TL_PREV_TASKS);
				
				if (nextTaskAssociationRefs.contains(taskListNodeRef)) {
					nextTaskPrevTaskNodeRefs.remove(taskListNodeRef);
				}

				for (NodeRef deleteTaskPrevTaskNodeRef : deleteTaskPrevTaskNodeRefs) {
					nextTaskPrevTaskNodeRefs.add(deleteTaskPrevTaskNodeRef);
				}
				associationService.update(nextTaskAssociationRef.getSourceRef(), ProjectModel.ASSOC_TL_PREV_TASKS, nextTaskPrevTaskNodeRefs);
			}
		}
		
	}
	

	@Override
	public void submitTask(NodeRef nodeRef) {

		Date startDate = (Date) nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_START);
		Date endDate = ProjectHelper.removeTime(new Date());

		nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_STATE, TaskState.Completed.toString());
		// we want to keep the planned duration to calculate overdue
		nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_END, endDate);
		// milestone duration is maximum 1 day or startDate is after endDate
		Boolean isMileStone = (Boolean) nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_IS_MILESTONE);
		if ((isMileStone != null && isMileStone) || (startDate == null || startDate.after(endDate))) {
			nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_START, endDate);
		}
	}

	@Override
	public List<NodeRef> updateTaskResources(NodeRef projectNodeRef, NodeRef taskRef, List<NodeRef> resources, boolean updatePermissions) {
		List<NodeRef> toRemove = new ArrayList<>();
		List<NodeRef> toAdd = new ArrayList<>();
		for (NodeRef resourceNodeRef : resources) {
			String authorityName = authorityDAO.getAuthorityName(resourceNodeRef);
			if (isRoleAuhtority(authorityName)) {
				logger.debug("Found project role : " + authorityName);
				toRemove.add(resourceNodeRef);
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
								toAdd.add(userNodeRef);
							}
						} else if (user instanceof NodeRef) {
							if (logger.isDebugEnabled()) {
								logger.debug("Adding user :" + authorityDAO.getAuthorityName((NodeRef) user));
							}
							toAdd.add((NodeRef) user);
						}
					} else {
						logger.debug("Try getting assoc: " + propName);
						List<AssociationRef> assocs = nodeService.getTargetAssocs(projectNodeRef, propName);
						if (assocs != null) {
							for (AssociationRef assoc : assocs) {
								if (logger.isDebugEnabled()) {
									logger.debug("Adding user :" + authorityDAO.getAuthorityName(assoc.getTargetRef()));
								}
								toAdd.add(assoc.getTargetRef());
							}
						}
					}
				}

			}
		}

		for (NodeRef resourceNodeRef : toAdd) {
			resources.add(resourceNodeRef);
			if (updatePermissions) {
				updateProjectPermission(projectNodeRef, taskRef, resourceNodeRef, true);
			}
		}

		resources.removeAll(toRemove);

		return resources;

	}

	@Override
	public String getDeliverableUrl(NodeRef projectNodeRef, String url) {
		if (url != null && url.contains("{")) {
			Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(url);
			StringBuffer sb = new StringBuffer();
			while (patternMatcher.find()) {

				String assocQname = patternMatcher.group(1);
				String replacement = "";
				if (DeliverableUrl.NODEREF_URL_PARAM.equals(assocQname)) {
					replacement += projectNodeRef;

				} else {
					String[] splitted = assocQname.split("\\|");
					List<AssociationRef> assocs = nodeService.getTargetAssocs(projectNodeRef, QName.createQName(splitted[0], namespaceService));
					if (assocs != null) {
						for (AssociationRef assoc : assocs) {
							if (replacement.length() > 0) {
								replacement += ",";
							}
							if(splitted.length>1){
								if(splitted[1].startsWith(DeliverableUrl.XPATH_URL_PREFIX)){
									replacement += BeCPGQueryBuilder.createQuery().selectNodeByPath(assoc.getTargetRef(), splitted[1].substring(DeliverableUrl.XPATH_URL_PREFIX.length()));	
								} else {
									replacement += nodeService.getProperty(assoc.getTargetRef(), QName.createQName(splitted[1], namespaceService));
								}
							} else {
								replacement += assoc.getTargetRef();
							}
						}
					}

				}

				patternMatcher.appendReplacement(sb, replacement != null ? replacement : "");

			}
			patternMatcher.appendTail(sb);
			return sb.toString();

		}
		return url;
	}

	private QName extractRolePropName(String authorityName) {
		String propName = authorityName.substring((PermissionService.GROUP_PREFIX + ProjectRepoConsts.PROJECT_GROUP_PREFIX).length(),
				authorityName.length()).replace("_", ":");

		return QName.createQName(propName, namespaceService);
	}

	private boolean isRoleAuhtority(String authorityName) {
		return authorityName != null && authorityName.startsWith(PermissionService.GROUP_PREFIX + ProjectRepoConsts.PROJECT_GROUP_PREFIX);
	}

	@Override
	public void updateProjectPermission(NodeRef projectNodeRef, NodeRef taskListNodeRef, NodeRef resourceNodeRef, boolean allow) {
		if (ProjectModel.TYPE_PROJECT.equals(nodeService.getType(projectNodeRef))
				&& permissionService.hasReadPermission(projectNodeRef) == AccessStatus.ALLOWED) {

			List<NodeRef> nodeRefs = new ArrayList<>(1);
			nodeRefs.add(taskListNodeRef);

			if (resourceNodeRef != null && nodeService.exists(resourceNodeRef)) {
				String authorityName = authorityDAO.getAuthorityName(resourceNodeRef);

				if (authorityName != null && !isRoleAuhtority(authorityName)) {
					logger.debug("Set permission for authority: " + authorityName + " allow :" + allow);
					ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);
					List<DeliverableListDataItem> deliverableList = ProjectHelper.getDeliverables(projectData, taskListNodeRef);
					for (DeliverableListDataItem dl : deliverableList) {
						nodeRefs.add(dl.getNodeRef());
					}

					for (NodeRef n : nodeRefs) {
						if (allow) {
							permissionService.setPermission(n, authorityName, PermissionService.EDITOR, true);
						} else {
							// permissionService.deletePermission(n, userName,
							// PermissionService.EDITOR);
							permissionService.clearPermission(n, authorityName);
						}

					}
				}
			}
		}

	}

	@Override
	public void runScript(ProjectData project, TaskListDataItem task, NodeRef scriptNode) {

		if (scriptNode != null && nodeService.exists(scriptNode)) {
			Map<String, Object> model = new HashMap<>();

			logger.debug("Run task script " );

			model.put("task", task);
			model.put("project", project);
			model.put("shareUrl", sysAdminParams.getShareProtocol() + "://" + sysAdminParams.getShareHost() + ":" + sysAdminParams.getSharePort()
					+ "/" + sysAdminParams.getShareContext());

			scriptService.executeScript(scriptNode, ContentModel.PROP_CONTENT, model);
		}

	}

	@Override
	public NodeRef refusedTask(NodeRef nodeRef) {
		nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_STATE, TaskState.Refused.toString());

		return associationService.getTargetAssoc(nodeRef, ProjectModel.ASSOC_TL_REFUSED_TASK_REF);

	}

	@Override
	public Long getNbProjectsByLegend(NodeRef legendNodeRef) {
		return BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT).andPropEquals(ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress.toString())
				.andPropEquals(ProjectModel.PROP_PROJECT_LEGENDS, legendNodeRef.toString()).inDB().count();
	}

}
