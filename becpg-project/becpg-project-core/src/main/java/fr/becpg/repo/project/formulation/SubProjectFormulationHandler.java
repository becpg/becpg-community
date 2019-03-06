package fr.becpg.repo.project.formulation;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
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
public class SubProjectFormulationHandler extends FormulationBaseHandler<ProjectData>{


	private AlfrescoRepository<ProjectData> alfrescoRepository;
	
	private ProjectActivityService projectActivityService;
	
	private String propsToCopyFromParent = null;
	
	private String propsToCopyToParent = null;
	
	private NodeService nodeService;
	
	private NamespaceService namespaceService;
	

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
		
		if(propsToCopyToParent!=null && !propsToCopyToParent.isEmpty()) {
	        for(String propertyToCopy : propsToCopyToParent.split(",")) {
	        	QName propertyQname = QName.createQName(propertyToCopy,namespaceService );
	        	nodeService.removeProperty(projectData.getNodeRef(), propertyQname);
	        }
		}
		
		for (TaskListDataItem task : projectData.getTaskList()) {
			if (task.getSubProject()!=null) {
	
				ProjectData subProject = alfrescoRepository.findOne(task.getSubProject());
	
				task.setStart(subProject.getStartDate());
				task.setEnd(subProject.getCompletionDate());
				task.setDuration(ProjectHelper.calculateTaskDuration(subProject.getStartDate(),subProject.getCompletionDate()));
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
				
				
				if(propsToCopyFromParent!=null && !propsToCopyFromParent.isEmpty()) {
			        for(String propertyToCopy : propsToCopyFromParent.split(",")) {
			        	QName propertyQname = QName.createQName(propertyToCopy,namespaceService );
			        	
			        	Serializable value = nodeService.getProperty(projectData.getNodeRef(), propertyQname);
			        	if(value == null) {
			        		nodeService.removeProperty(task.getSubProject(), propertyQname);
			        	} else {
			        		nodeService.setProperty(task.getSubProject(), propertyQname, value);
			        	}
			        }
		        }
				
				
				if(propsToCopyToParent!=null && !propsToCopyToParent.isEmpty()) {
			        for(String propertyToCopy : propsToCopyToParent.split(",")) {
			        	QName propertyQname = QName.createQName(propertyToCopy,namespaceService );
			        	
			        	Serializable value = nodeService.getProperty(task.getSubProject(), propertyQname);
			        	Serializable origValue = nodeService.getProperty(projectData.getNodeRef(), propertyQname);
			        	
			        	
			        	if(value instanceof String && value != null) {
			        		
			        		if(origValue!=null) {
			        			value = origValue + "\n" + value;
				        	}
			        		
			        		nodeService.setProperty(projectData.getNodeRef(), propertyQname, value);
			        	}
			        }
		        }
				
	
			}
		}
		return true;
	}

}
