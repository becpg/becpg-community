package fr.becpg.repo.project.formulation;

import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskManualDate;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * 
 * @author matthieu
 *
 */
public class SubProjectFormulationHandler extends FormulationBaseHandler<ProjectData>{


	private AlfrescoRepository<ProjectData> alfrescoRepository;
	
	private FormulationService<ProjectData> formulationService;
	
	private ProjectActivityService projectActivityService;

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setFormulationService(FormulationService<ProjectData> formulationService) {
		this.formulationService = formulationService;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {
		for (TaskListDataItem task : projectData.getTaskList()) {
			if (task.getSubProject()!=null) {
	
				ProjectData subProject = alfrescoRepository.findOne(task.getSubProject());
				formulationService.formulate(subProject);
				alfrescoRepository.save(subProject);
				
				task.setManualDate(TaskManualDate.End);
				task.setStart(subProject.getStartDate());
				task.setEnd(subProject.getCompletionDate());
				task.setDuration(null);
				task.setCompletionPercent(subProject.getCompletionPercent());
				task.setTaskName(subProject.getName());				
				TaskState subProjectState = subProject.getProjectState().toTaskState();
				if (!subProjectState.equals(task.getTaskState())) {
					ProjectHelper.setTaskState(task, subProjectState, projectActivityService);
				}
	
			}
		}
		return true;
	}

}
