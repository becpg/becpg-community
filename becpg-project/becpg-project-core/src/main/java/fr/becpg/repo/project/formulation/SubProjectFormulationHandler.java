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
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>SubProjectFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SubProjectFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private AlfrescoRepository<ProjectData> alfrescoRepository;

	private ProjectActivityService projectActivityService;

	private EntityDictionaryService entityDictionaryService;

	private String propsToCopyFromParent() {
		return systemConfigurationService.confValue("project.subProject.propsToCopyFromParent");
	}
	
	private String propsToCopyToParent() {
		return systemConfigurationService.confValue("project.subProject.propsToCopyToParent");
	}

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private AssociationService associationService;
	
	private SystemConfigurationService systemConfigurationService;
	
	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
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
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
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
	 * <p>Setter for the field <code>projectActivityService</code>.</p>
	 *
	 * @param projectActivityService a {@link fr.becpg.repo.project.ProjectActivityService} object.
	 */
	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProjectData projectData) {

		Map<QName, String> propsToCopyToParentTmp = new HashMap<>();
		Map<QName, List<NodeRef>> assocsToCopyToParentTmp = new HashMap<>();

		for (TaskListDataItem task : projectData.getTaskList()) {
			if (task.getSubProject() != null) {

				ProjectData subProject = alfrescoRepository.findOne(task.getSubProject());

				task.setStart(subProject.getStartDate());
				task.setEnd(subProject.getCompletionDate());
				task.setDue(subProject.getDueDate());
				task.setTargetStart(subProject.getTargetStartDate());
				task.setTargetEnd(ProjectHelper.calculateEndDate(subProject.getTargetStartDate(),subProject.getRealDuration()));
				task.setDuration(ProjectHelper.calculateTaskDuration(subProject.getStartDate(), subProject.getCompletionDate()));
				task.setCompletionPercent(subProject.getCompletionPercent());
				task.setTaskName(subProject.getName());

				if ((subProject.getLegends() != null) && !subProject.getLegends().isEmpty()) {
					task.setTaskLegend(subProject.getLegends().get(0));
				}

				ProjectState state = subProject.getProjectState();
				if (state == null) {
					state = ProjectState.Planned;
				}

				TaskState subProjectState = state.toTaskState();
				if (!subProjectState.equals(task.getTaskState())) {
					ProjectHelper.setTaskState(task, subProjectState, projectActivityService);
				}

				if ((propsToCopyFromParent() != null) && !propsToCopyFromParent().isEmpty()) {
					for (String propertyToCopy : propsToCopyFromParent().split(",")) {
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

				if ((propsToCopyToParent() != null) && !propsToCopyToParent().isEmpty()) {
					for (String propertyToCopy : propsToCopyToParent().split(",")) {
						QName propertyQname = QName.createQName(propertyToCopy, namespaceService);

						ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(propertyQname);
						if (propDef instanceof PropertyDefinition) {

							Serializable value = nodeService.getProperty(task.getSubProject(), propertyQname);

							if ((value instanceof String)) {

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

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

}
