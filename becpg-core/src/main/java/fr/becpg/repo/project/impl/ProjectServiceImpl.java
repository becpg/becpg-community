package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.BeCPGListDao;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.AbstractProjectData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectTplData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskHistoryListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * Project service that manage project
 * 
 * @author quere
 * 
 */
public class ProjectServiceImpl implements ProjectService {

	private static final String DESCRIPTION_SEPARATOR = ", ";

	private static Log logger = LogFactory.getLog(ProjectServiceImpl.class);

	private BeCPGListDao<AbstractProjectData> projectDAO;
	private WorkflowService workflowService;
	private WUsedListService wUsedListService;
	private AssociationService associationService;
	private NodeService nodeService;

	public void setProjectDAO(BeCPGListDao<AbstractProjectData> projectDAO) {
		this.projectDAO = projectDAO;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void startNextTasks(NodeRef taskHistoryNodeRef) {

		NodeRef projectNodeRef = wUsedListService.getRoot(taskHistoryNodeRef);
		NodeRef taskNodeRef = associationService.getTargetAssoc(taskHistoryNodeRef, ProjectModel.ASSOC_THL_TASK);
		startNextTasks(projectNodeRef, taskNodeRef);
	}

	@Override
	public void start(NodeRef projectNodeRef) {
		startNextTasks(projectNodeRef, null);
	}

	private void startNextTasks(NodeRef projectNodeRef, NodeRef taskNodeRef) {

		logger.debug("submit Task");
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		dataLists.add(ProjectModel.TYPE_TASK_HISTORY_LIST);
		ProjectData projectData = (ProjectData)projectDAO.find(projectNodeRef, dataLists);
		ProjectTplData projectTplData = (ProjectTplData)projectDAO.find(projectData.getProjectTpl(), dataLists);
		
		// add next tasks
		List<TaskListDataItem> nextTasks = getNextTasks(projectData, taskNodeRef);
		logger.debug("nextTasks size: " + nextTasks.size());
		for (TaskListDataItem nextTask : nextTasks) {
			if (areTasksDone(projectData, nextTask.getPrevTasks())) {

				if (projectData.getTaskHistoryList() == null) {
					projectData.setTaskHistoryList(new ArrayList<TaskHistoryListDataItem>());
				}
				TaskHistoryListDataItem t = new TaskHistoryListDataItem(nextTask);
				t.setState(TaskState.InProgress);
				projectData.getTaskHistoryList().add(t);
				
				// deliverable list				
				List<DeliverableListDataItem> nextDeliverables = getDeliverables(projectTplData, nextTask.getTask());
				logger.debug("deliverables size to add: " + nextDeliverables.size());
				for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

					if (projectData.getDeliverableList() == null) {
						projectData.setDeliverableList(new ArrayList<DeliverableListDataItem>());
					}
					DeliverableListDataItem d = new DeliverableListDataItem(nextDeliverable);
					d.setState(DeliverableState.InProgress);
					projectData.getDeliverableList().add(d);
				}
			}
		}
		
		logger.debug("task history: " + projectData.getTaskHistoryList().size());
		projectDAO.update(projectNodeRef, projectData, dataLists);
		
		// start workflow, we need to update project to create taskHistoryListDataItem first before starting workflow
		projectData = (ProjectData)projectDAO.find(projectNodeRef, dataLists);
		for(TaskHistoryListDataItem taskHistoryListDataItem : projectData.getTaskHistoryList()){
			
			if(TaskState.InProgress.equals(taskHistoryListDataItem.getState())){
				
				logger.debug("Start workflow");
				
				String workflowDescription = "";
				for(DeliverableListDataItem deliverableListDataItem : projectData.getDeliverableList()){
					if(taskHistoryListDataItem.getTask().equals(deliverableListDataItem.getTask())){						
						if (!workflowDescription.isEmpty()) {
							workflowDescription += DESCRIPTION_SEPARATOR;
						}
						workflowDescription += deliverableListDataItem.getDescription();
					}
				}
				
				for(TaskListDataItem taskListDataItem : projectData.getTaskList()){
					if(taskHistoryListDataItem.getTask().equals(taskListDataItem.getTask())){
						
						logger.debug("assignees size: " + taskListDataItem.getAssignees());
						if (taskListDataItem.getAssignees() != null) {
							for (NodeRef assignee : taskListDataItem.getAssignees()) {
								// start workflow
								startWorkflow(taskListDataItem, workflowDescription, assignee, taskHistoryListDataItem);
							}
						}
						break;
					}
				}
			}
		}
		
		projectDAO.update(projectNodeRef, projectData, dataLists);
	}

	private List<TaskListDataItem> getNextTasks(AbstractProjectData projectData, NodeRef taskNodeRef) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		for (TaskListDataItem t : projectData.getTaskList()) {
			// taskNodeRef is null when we start project
			if (t.getPrevTasks().contains(taskNodeRef) || (taskNodeRef == null && t.getPrevTasks().isEmpty())) {
				taskList.add(t);
			}
		}
		return taskList;
	}

	private List<DeliverableListDataItem> getDeliverables(AbstractProjectData projectData, NodeRef taskNodeRef) {

		List<DeliverableListDataItem> deliverableList = new ArrayList<DeliverableListDataItem>();
		for (DeliverableListDataItem d : projectData.getDeliverableList()) {
			if (d.getTask() != null && d.getTask().equals(taskNodeRef)) {
				deliverableList.add(d);
			}
		}
		return deliverableList;
	}

	private boolean areTasksDone(ProjectData projectData, List<NodeRef> taskNodeRefs) {

		List<NodeRef> inProgressTasks = new ArrayList<NodeRef>();
		inProgressTasks.addAll(taskNodeRefs);

		if (projectData.getTaskHistoryList() != null && projectData.getTaskHistoryList().size() > 0) {
			for (int i = projectData.getTaskHistoryList().size() - 1; i >= 0; i--) {
				TaskHistoryListDataItem t = projectData.getTaskHistoryList().get(i);

				if (taskNodeRefs.contains(t.getTask())) {
					if (!TaskState.InProgress.equals(t.getState())) {
						inProgressTasks.remove(t.getTask());
					}
				}
			}
		}

		return inProgressTasks.isEmpty();
	}

	private String getWorkflowDefId(String workflowName) {
		if (workflowName != null) {
			WorkflowDefinition def = workflowService.getDefinitionByName(workflowName);
			if(def != null){
				return def.getId();
			}			
		}

		logger.error("Unknown workflow name: " + workflowName);
		return null;
	}

	private void startWorkflow(TaskListDataItem taskListDataItem, String workflowDescription, NodeRef assignee, TaskHistoryListDataItem taskHistoryListDataItem) {
		Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
		Calendar cal = Calendar.getInstance();

		if (taskListDataItem.getDuration() != null) {
			cal.add(Calendar.DAY_OF_YEAR, taskListDataItem.getDuration());
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, cal.getTime());
		}
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
		workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
		workflowProps.put(ProjectModel.ASSOC_WORKFLOW_TASK, taskHistoryListDataItem.getNodeRef());

		NodeRef wfPackage = workflowService.createPackage(null);
		workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

		String workflowDefId = getWorkflowDefId(taskListDataItem.getWorkflowName());
		if (workflowDefId != null) {

			WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, workflowProps);
			logger.debug("New worflow started. Id: " + wfPath.getId() + " - instance: " + wfPath.getInstance());
			String workflowId = wfPath.getInstance().getId();
			taskHistoryListDataItem.setWorkflowInstance(workflowId);

			// get the workflow tasks			
			WorkflowTask startTask = workflowService.getStartTask(workflowId);

			// end task
			try {
				workflowService.endTask(startTask.getId(), null);
			} catch (RuntimeException err) {
				if (logger.isDebugEnabled())
					logger.debug("Failed - caught error during project adhoc workflow transition: " + err.getMessage());
				throw err;
			}
		}
	}

	@Override
	public void submitDeliverable(NodeRef deliverableNodeRef) {
				
		Integer completionPercent = (Integer)nodeService.getProperty(deliverableNodeRef, ProjectModel.PROP_COMPLETION_PERCENT);		
		logger.debug("submit Deliverable. completionPercent: " + completionPercent);
		
		if(completionPercent != null){
			NodeRef taskNodeRef = associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);
			
			if(taskNodeRef != null){
				NodeRef projectNodeRef = wUsedListService.getRoot(deliverableNodeRef);
				
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_TASK_HISTORY_LIST);
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				ProjectData projectData = (ProjectData)projectDAO.find(projectNodeRef, dataLists);
				
				for(TaskHistoryListDataItem t : projectData.getTaskHistoryList()){
					if(taskNodeRef.equals(t.getTask())){
						Integer taskCompletionPercent = t.getCompletionPercent();
						if(taskCompletionPercent==null){
							taskCompletionPercent = 0;						
						}
						taskCompletionPercent+=completionPercent;
						t.setCompletionPercent(taskCompletionPercent);
						logger.debug("set completion percent to value " + taskCompletionPercent);
						
						if(taskCompletionPercent > 100){
							logger.debug("submit task");
							t.setState(TaskState.Completed);
						}
					}
				}
				projectDAO.update(projectNodeRef, projectData, dataLists);
			}
			else{
				logger.error("Task is not defined for the deliverable. nodeRef: " + deliverableNodeRef);
			}
		}		
	}
}
