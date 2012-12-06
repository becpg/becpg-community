package fr.becpg.repo.project.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.BeCPGListDao;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.project.ProjectException;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.AbstractProjectData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Project service that manage project
 * 
 * @author quere
 * 
 */
public class ProjectServiceImpl implements ProjectService {

	private static final String QUERY_TASK_LEGEND = "+TYPE:\"pjt:taskLegend\"";

	private static Log logger = LogFactory.getLog(ProjectServiceImpl.class);

	private BeCPGListDao<AbstractProjectData> projectDAO;
	private WorkflowService workflowService;
	private AssociationService associationService;
	private NodeService nodeService;
	private BeCPGSearchService beCPGSearchService;
	private RepoService repoService;
	private PlanningVisitor planningVisitor;
	private TaskStateVisitor taskStateVisitor;

	public void setProjectDAO(BeCPGListDao<AbstractProjectData> projectDAO) {
		this.projectDAO = projectDAO;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setTaskStateVisitor(TaskStateVisitor taskStateVisitor) {
		this.taskStateVisitor = taskStateVisitor;
	}

	public void setPlanningVisitor(PlanningVisitor planningVisitor) {
		this.planningVisitor = planningVisitor;
	}

	@Override
	public void openDeliverable(NodeRef deliverableNodeRef) {

		Integer dlCompletionPercent = (Integer) nodeService.getProperty(deliverableNodeRef,
				ProjectModel.PROP_COMPLETION_PERCENT);
		logger.debug("open Deliverable. completionPercent: " + dlCompletionPercent);

		NodeRef taskNodeRef = associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);

		if (taskNodeRef != null) {

			Integer taskDuration = (Integer) nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_DURATION);

			if (taskDuration != null && dlCompletionPercent != null) {
				int newDuration = taskDuration * dlCompletionPercent;
				nodeService.setProperty(taskNodeRef, ProjectModel.PROP_TL_DURATION, taskDuration + newDuration);
			}
			logger.debug("set taskList InProgress: " + taskNodeRef);
			nodeService.setProperty(taskNodeRef, ProjectModel.PROP_TL_STATE, TaskState.InProgress.toString());
		} else {
			logger.debug("Task is not defined for the deliverable. nodeRef: " + deliverableNodeRef);
		}
	}

	@Override
	public List<NodeRef> getTaskLegendList() {
		return beCPGSearchService.luceneSearch(QUERY_TASK_LEGEND);
	}

	@Override
	public NodeRef getProjectsContainer(String siteId) {
		return repoService.getFolderByPath(RepoConsts.PATH_PROJECTS);
	}

	@Override
	public void cancel(NodeRef projectNodeRef) {

		logger.debug("cancel project: " + projectNodeRef);
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		AbstractProjectData abstractProjectData = projectDAO.find(projectNodeRef, dataLists);

		for (TaskListDataItem taskListDataItem : abstractProjectData.getTaskList()) {
			if (taskListDataItem.getWorkflowInstance() != null && !taskListDataItem.getWorkflowInstance().isEmpty()){
				
				WorkflowInstance workflowInstance = workflowService.getWorkflowById(taskListDataItem.getWorkflowInstance());
				if(workflowInstance != null){
					if(workflowInstance.isActive()){
						logger.debug("Cancel workflow instance: " + taskListDataItem.getWorkflowInstance());
						workflowService.cancelWorkflow(taskListDataItem.getWorkflowInstance());
					}
				}
				else{
					logger.warn("Workflow instance unknown. WorkflowId: " + taskListDataItem.getWorkflowInstance());
				}
			}					
		}
	}

	@Override
	public void formulate(NodeRef projectNodeRef) throws ProjectException {

		if (nodeService.getType(projectNodeRef).equals(ProjectModel.TYPE_PROJECT)) {
			logger.debug("formulate projectNodeRef: " + projectNodeRef);
			Collection<QName> dataLists = new ArrayList<QName>();
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

			try {
				planningVisitor.visit(projectData);
				taskStateVisitor.visit(projectData);
			} catch (ProjectException e) {
				if (e instanceof ProjectException) {
					throw (ProjectException) e;
				}
				throw new ProjectException("message.formulate.failure", e);
			}

			projectDAO.update(projectNodeRef, projectData, dataLists);
		}

	}
}
