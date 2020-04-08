package fr.becpg.repo.project.formulation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 *
 * @author matthieu
 *
 */
public class SubProjectFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private AlfrescoRepository<ProjectData> alfrescoRepository;

	private ProjectActivityService projectActivityService;

	private EntityDictionaryService entityDictionaryService;

	private String propsToCopyFromParent = null;

	private String propsToCopyToParent = null;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private AssociationService associationService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	public void setPropsToCopyToParent(String propsToCopyToParent) {
		this.propsToCopyToParent = propsToCopyToParent;
	}

	public void setPropsToCopyFromParent(String propsToCopyFromParent) {
		this.propsToCopyFromParent = propsToCopyFromParent;
	}

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		Map<QName, String> propsToCopyToParentTmp = new HashMap<>();
		Map<QName, List<NodeRef>> assocsToCopyToParentTmp = new HashMap<>();

		for (TaskListDataItem task : projectData.getTaskList()) {
			if (task.getSubProject() != null) {

				ProjectData subProject = alfrescoRepository.findOne(task.getSubProject());

				task.setStart(subProject.getStartDate());
				task.setEnd(subProject.getCompletionDate());
				task.setDuration(ProjectHelper.calculateTaskDuration(subProject.getStartDate(), subProject.getCompletionDate()));
				task.setCompletionPercent(subProject.getCompletionPercent());
				task.setTaskName(subProject.getName());

				if ((subProject.getLegends() != null) && !subProject.getLegends().isEmpty()) {
					task.setTaskLegend(subProject.getLegends().get(0));
				}
				boolean updateTaskState = true;
				
				if(ProjectHelper.isOnHold(projectData)) {
					updateTaskState = true;
					if(ProjectState.Cancelled.equals(projectData.getProjectState())) {
						subProject.setProjectState(ProjectState.Cancelled);
					} else {
						subProject.setProjectState(ProjectState.OnHold);
					}
				} 
				
				ProjectState state = subProject.getProjectState();
				if (state == null) {
					state = ProjectState.Planned;
				}

				TaskState subProjectState = state.toTaskState();
				if (updateTaskState && !subProjectState.equals(task.getTaskState())) {
					ProjectHelper.setTaskState(task, subProjectState, projectActivityService);
				}

				if ((propsToCopyFromParent != null) && !propsToCopyFromParent.isEmpty()) {
					for (String propertyToCopy : propsToCopyFromParent.split(",")) {
						QName propertyQname = QName.createQName(propertyToCopy, namespaceService);

						ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(propertyQname);
						if (propDef instanceof PropertyDefinition) {

							Serializable value = nodeService.getProperty(projectData.getNodeRef(), propertyQname);
							if (value == null) {
								nodeService.removeProperty(task.getSubProject(), propertyQname);
							} else {
								nodeService.setProperty(task.getSubProject(), propertyQname, value);
							}
						} else if (propDef instanceof AssociationDefinition) {
							if (propDef instanceof ChildAssociationDefinition) {
								// Not supported

							} else {
								List<NodeRef> nodeRefs = associationService.getTargetAssocs(projectData.getNodeRef(), propertyQname);
								associationService.update(task.getSubProject(), propertyQname, nodeRefs);
							}
						}
					}
				}

				if ((propsToCopyToParent != null) && !propsToCopyToParent.isEmpty()) {
					for (String propertyToCopy : propsToCopyToParent.split(",")) {
						QName propertyQname = QName.createQName(propertyToCopy, namespaceService);

						ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(propertyQname);
						if (propDef instanceof PropertyDefinition) {

							Serializable value = nodeService.getProperty(task.getSubProject(), propertyQname);

							if ((value instanceof String) && (value != null)) {

								if (propsToCopyToParentTmp.get(propertyQname) != null) {
									value = propsToCopyToParentTmp.get(propertyQname) + "\n" + value;
								}
								propsToCopyToParentTmp.put(propertyQname, (String) value);
							} else if (propsToCopyToParentTmp.get(propertyQname) == null) {
								propsToCopyToParentTmp.put(propertyQname, null);
							}

						} else if (propDef instanceof AssociationDefinition) {
							if (propDef instanceof ChildAssociationDefinition) {
								// Not supported

							} else {
								List<NodeRef> nodeRefs = associationService.getTargetAssocs(task.getSubProject(), propertyQname);

								if (assocsToCopyToParentTmp.get(propertyQname) != null) {
									nodeRefs.addAll(assocsToCopyToParentTmp.get(propertyQname));
								}
								assocsToCopyToParentTmp.put(propertyQname, nodeRefs);

							}
						}
					}
				}

			}
		}

		for (Map.Entry<QName, String> entry : propsToCopyToParentTmp.entrySet()) {
			nodeService.setProperty(projectData.getNodeRef(), entry.getKey(), entry.getValue());
		}

		for (Map.Entry<QName, List<NodeRef>> entry : assocsToCopyToParentTmp.entrySet()) {
			associationService.update(projectData.getNodeRef(), entry.getKey(), entry.getValue());
		}

		return true;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

}
