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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;

import fr.becpg.model.DeliverableUrl;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.BudgetListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Utility script methods for budget
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class ProjectScriptHelper extends BaseScopableProcessorExtension {

	private AlfrescoRepository<ProjectData> alfrescoRepository;

	private EntityListDAO entityListDAO;

	private NodeService nodeService;

	private NamespaceService namespaceService;
	
	private ProjectService projectService;
	
	private ServiceRegistry serviceRegistry;
	
	private EntityService entityService;
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}
	
	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>calculateBudgetParentValues.</p>
	 *
	 * @param listItemNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param qNames an array of {@link java.lang.String} objects.
	 */
	public void calculateBudgetParentValues(ScriptNode listItemNode, String[] qNames) {

		NodeRef projectNodeRef = entityListDAO.getEntity(listItemNode.getNodeRef());
		ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);

		List<QName> props = new ArrayList<>();

		for (String qName : qNames) {
			props.add(QName.createQName(qName, namespaceService));
		}

		Composite<BudgetListDataItem> compositeBugdet = CompositeHelper.getHierarchicalCompoList(projectData.getBudgetList());
		calculateBudgetParentValues(compositeBugdet, props);
		
		alfrescoRepository.save(projectData);

	}

	private void calculateBudgetParentValues(Composite<BudgetListDataItem> parent, List<QName> props) {
		Map<QName, Double> values = new HashMap<>();

		if (!parent.isLeaf()) {
			for (Composite<BudgetListDataItem> component : parent.getChildren()) {
				calculateBudgetParentValues(component, props);

				for (QName prop : props) {

					Double value = values.get(prop);
					if (value == null) {
						value = 0d;
					}

					if (nodeService.getProperty(component.getData().getNodeRef(), prop) != null) {
						value += (Double) nodeService.getProperty(component.getData().getNodeRef(), prop);
					}

					values.put(prop, value);

				}
			}
			if (!parent.isRoot()) {

				for (QName prop : props) {
					Double value = values.get(prop);
					if (value == null) {
						value = 0d;
					}
					nodeService.setProperty(parent.getData().getNodeRef(), prop, value);
				}
			}
		}
	}
	
	// {nodeRef} --> replace with project nodeRef
	// {nodeRef|propName} --> replace with project property
	// {nodeRef|xpath:./path} --> replace with nodeRef found in relative project path
	// {assocName} --> replace with association nodeRef
	// {assocName|propName} --> replace with association property
	// {assocName|@type} --> replace with association property
	// {assocName|xpath:./path} --> replace with nodeRef found in relative assoc path

	public String getDeliverableUrl(ScriptNode deliverable) {

		return AuthenticationUtil.runAsSystem(() -> {
			NodeRef deliverableNodeRef = deliverable.getNodeRef();

			String url = (String) nodeService.getProperty(deliverableNodeRef, ProjectModel.PROP_DL_URL);

			List<AssociationRef> taskAssocs = nodeService.getTargetAssocs(deliverableNodeRef,
					ProjectModel.ASSOC_DL_TASK);

			if (taskAssocs != null && !taskAssocs.isEmpty()) {
				NodeRef taskNodeRef = taskAssocs.get(0).getTargetRef();

				NodeRef projectNodeRef = null;

				List<AssociationRef> projectAssocs = nodeService.getSourceAssocs(taskNodeRef,
						ProjectModel.ASSOC_PROJECT_CUR_TASKS);
				
				if (projectAssocs != null && !projectAssocs.isEmpty()) {
					projectNodeRef = projectAssocs.get(0).getSourceRef();
				} else {
					projectNodeRef = entityService.getEntityNodeRef(taskNodeRef, nodeService.getType(taskNodeRef));
				}
				
				if (projectNodeRef != null && (url != null) && url.contains("{")) {
					Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(url);
					StringBuffer sb = new StringBuffer();
					while (patternMatcher.find()) {

						String assocQname = patternMatcher.group(1);
						StringBuilder replacement = new StringBuilder();
						if ((assocQname != null) && assocQname.startsWith(DeliverableUrl.NODEREF_URL_PARAM)) {
							String[] splitted = assocQname.split("\\|");
							replacement.append(extractDeliverableProp(projectNodeRef, splitted));

						} else if ((assocQname != null) && assocQname.startsWith(DeliverableUrl.TASK_URL_PARAM)) {
							String[] splitted = assocQname.split("\\|");
							replacement.append(extractDeliverableProp(taskNodeRef, splitted));

						} else if (assocQname != null) {
							String[] splitted = assocQname.split("\\|");
							List<AssociationRef> assocs = nodeService.getTargetAssocs(projectNodeRef,
									QName.createQName(splitted[0], namespaceService));
							if (assocs != null) {
								for (AssociationRef assoc : assocs) {
									if (replacement.length() > 0) {
										replacement.append(",");
									}
									replacement.append(extractDeliverableProp(assoc.getTargetRef(), splitted));
								}
							}
						}

						patternMatcher.appendReplacement(sb, replacement != null ? replacement.toString() : "");

					}
					patternMatcher.appendTail(sb);

					return sb.toString();
				}
			}

			return url;
		});

	}

	@SuppressWarnings("unchecked")
	private String extractDeliverableProp(NodeRef nodeRef, String[] splitted) {
		NodeRef ret = null;
		if (splitted.length > 1) {
			if (splitted[1].startsWith(DeliverableUrl.XPATH_URL_PREFIX)) {
				ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeRef,
						splitted[1].substring(DeliverableUrl.XPATH_URL_PREFIX.length()));
			} else if(splitted[1].startsWith("@type")) {
				QName type = nodeService.getType(nodeRef);
				return type != null ? type.getLocalName() : "";
			} else {
				Serializable tmp = nodeService.getProperty(nodeRef, QName.createQName(splitted[1], namespaceService));
				StringBuilder strRet = new StringBuilder();
				
				if(tmp instanceof List) {
					for (Serializable subEl : (List<Serializable>) tmp) {
						if (subEl.toString().length() > 0) {
							strRet.append(",");
						}
						strRet.append(subEl.toString());
					}
					
				} else if(tmp!=null) {
					strRet.append(tmp.toString());
				}
				
				
				return strRet.toString().replace("$", "\\$");
			}
		} else {
			ret = nodeRef;
		}
		return ret != null ? ret.toString() : "";
	}
	
	public void updateTaskState(TaskListDataItem task, String taskState) {
		task.setTaskState(TaskState.valueOf(taskState));
	}
	
	public ScriptNode[] extractResources(ScriptNode project, ScriptNode[] resources) {
		
		List<NodeRef> resourceList = new ArrayList<>();
		
		for (ScriptNode resource : resources) {
			resourceList.add(resource.getNodeRef());
		}
		
		return projectService.extractResources(project.getNodeRef(), resourceList).stream().map(e -> new ScriptNode(e, serviceRegistry, getScope())).collect(Collectors.toList()).toArray(new ScriptNode[0]);
	}

	public int calculateTaskDuration(String startDate, String endDate) {
		return ProjectHelper.calculateTaskDuration(parseDate(startDate), parseDate(endDate));
	}

	private Date parseDate(String dateString) {
		if ("NOW".equals(dateString)) {
			return new Date();
		}
		return ISO8601DateFormat.parse(dateString);
	}

}
