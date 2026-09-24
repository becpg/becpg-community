package fr.becpg.repo.project.formulation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.CalendarWorkingDayProvider;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.project.impl.WorkingDayProvider;
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

	/**
	 * <p>propsToCopyFromParent.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	private String propsToCopyFromParent() {
		return systemConfigurationService.confValue("project.subProject.propsToCopyFromParent");
	}
	
	/**
	 * <p>propsToCopyToParent.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	private String propsToCopyToParent() {
		return systemConfigurationService.confValue("project.subProject.propsToCopyToParent");
	}

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private AssociationService associationService;
	
	private SystemConfigurationService systemConfigurationService;
	
	private fr.becpg.repo.project.CalendarService calendarService;
	
	/**
	 * <p>Setter for the field <code>calendarService</code>.</p>
	 *
	 * @param calendarService a {@link fr.becpg.repo.project.CalendarService} object
	 */
	public void setCalendarService(fr.becpg.repo.project.CalendarService calendarService) {
		this.calendarService = calendarService;
	}
	
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
				
				WorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarService.getCalendar(task.getNodeRef()));
				task.setTargetEnd(ProjectHelper.calculateEndDate(subProject.getTargetStartDate(), subProject.getRealDuration(), provider));
				task.setDuration(ProjectHelper.calculateTaskDuration(subProject.getStartDate(), subProject.getCompletionDate(), provider));
				task.setCompletionPercent(subProject.getCompletionPercent());
				copySubProjectName(task, subProject);

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

							if ((value instanceof String stringVal)) {

								if (propsToCopyToParentTmp.get(propertyQname) != null) {
									stringVal = propsToCopyToParentTmp.get(propertyQname) + "\n" + stringVal;
								}
								propsToCopyToParentTmp.put(propertyQname, stringVal);
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
	 * Copies the sub-project name into the task that carries it, replacing every translation.
	 * <p>
	 * A project name is plain text, so the task name must hold that name alone: merging it into the
	 * content locale would leave the placeholder or a former name in the other locales.
	 *
	 * @param task the task that carries the sub-project
	 * @param subProject the sub-project whose name is copied
	 */
	private void copySubProjectName(TaskListDataItem task, ProjectData subProject) {
		String subProjectName = subProject.getName();
		task.setTaskName(subProjectName);
		if ((task.getNodeRef() != null) && holdsOtherNames(task.getNodeRef(), subProjectName)) {
			task.getExtraProperties().put(ProjectModel.PROP_TL_TASK_NAME, new MLText(I18NUtil.getContentLocale(), subProjectName));
		}
	}

	/**
	 * Tells whether a stored task name holds another value than the given name in any locale.
	 * <p>
	 * Replacing the name only in that case keeps an unchanged task clean, since the repository cannot
	 * compare an {@link MLText} with the stored value and would otherwise save the task on every formulation.
	 *
	 * @param taskNodeRef the task node
	 * @param name the only name the task should hold
	 * @return true if a locale holds another value, or if the name is not multilingual yet
	 */
	private boolean holdsOtherNames(NodeRef taskNodeRef, String name) {
		boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
		try {
			Serializable storedName = nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME);
			return !(storedName instanceof MLText mlText) || !Set.of(name).equals(new HashSet<>(mlText.values()));
		} finally {
			MLPropertyInterceptor.setMLAware(wasMLAware);
		}
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
