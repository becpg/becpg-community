package fr.becpg.repo.project.formulation;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
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
	
	private ProjectActivityService projectActivityService;

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {
		for (TaskListDataItem task : projectData.getTaskList()) {
			if (task.getSubProject()!=null) {
	
				ProjectData subProject = alfrescoRepository.findOne(task.getSubProject());
	
				task.setManualDate(TaskManualDate.End);
				task.setStart(subProject.getStartDate());
				task.setEnd(subProject.getCompletionDate());
				task.setDuration(null);
				task.setCompletionPercent(subProject.getCompletionPercent());
				task.setTaskName(subProject.getName());	
				
				if(subProject.getLegends()!=null && !subProject.getLegends().isEmpty()) {
					task.setTaskLegend(subProject.getLegends().get(0));
				}

				ProjectState state = subProject.getProjectState();
				if(state == null) {
					state = ProjectState.Planned;
				}
				
				TaskState subProjectState = state.toTaskState();
				if (!subProjectState.equals(task.getTaskState())) {
					ProjectHelper.setTaskState(task, subProjectState, projectActivityService);
				}
	
			}
		}
		return true;
	}

}
