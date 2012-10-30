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
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
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

	@Override
	public void submitTask(NodeRef taskHistoryNodeRef) {

		NodeRef projectNodeRef = wUsedListService.getRoot(taskHistoryNodeRef);
		NodeRef taskNodeRef = associationService.getTargetAssoc(taskHistoryNodeRef, ProjectModel.ASSOC_THL_TASK);
		submitTask(projectNodeRef, taskNodeRef);
	}

	@Override
	public void start(NodeRef projectNodeRef) {
		submitTask(projectNodeRef, null);
	}

	private void submitTask(NodeRef projectNodeRef, NodeRef taskNodeRef) {

		logger.debug("submit Task");
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		dataLists.add(ProjectModel.TYPE_TASK_HISTORY_LIST);
		AbstractProjectData projectData = projectDAO.find(projectNodeRef, dataLists);
		AbstractProjectData projectTplData = projectDAO.find(projectData.getProjectTpl(), dataLists);

		List<TaskListDataItem> nextTasks = getNextTasks(projectData, taskNodeRef);
		logger.debug("nextTasks size: " + nextTasks.size());
		for (TaskListDataItem t : nextTasks) {
			if (areTasksDone(projectData, t.getPrevTasks())) {

				if (projectData.getTaskHistoryList() == null) {
					projectData.setTaskHistoryList(new ArrayList<TaskHistoryListDataItem>());
				}
				projectData.getTaskHistoryList().add(new TaskHistoryListDataItem(t));

				String workflowDescription = "";
				List<DeliverableListDataItem> deliverables = getDeliverables(projectTplData, t.getTask());
				logger.debug("deliverables size to add: " + deliverables.size());
				for (DeliverableListDataItem d : deliverables) {

					if (projectData.getDeliverableList() == null) {
						projectData.setDeliverableList(new ArrayList<DeliverableListDataItem>());
					}
					projectData.getDeliverableList().add(new DeliverableListDataItem(d));

					if (!workflowDescription.isEmpty()) {
						workflowDescription += DESCRIPTION_SEPARATOR;
					}
					workflowDescription += d.getDescription();
				}

				// TODO : manage group, single, multiple assignee ?
				//TODO : call script ?
				logger.debug("assignees size: " + t.getAssignees());
				if (t.getAssignees() != null) {
					for (NodeRef assignee : t.getAssignees()) {
						// start workflow
						startWorkflow(t.getWorkflowName(), workflowDescription, t.getDuration(), assignee);
					}
				}
			}
		}

		logger.debug("task history: " + projectData.getTaskHistoryList().size());
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

	private boolean areTasksDone(AbstractProjectData projectData, List<NodeRef> taskNodeRefs) {

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
			for (WorkflowDefinition def : workflowService.getAllDefinitions()) {
				logger.debug(def.getId() + " " + def.getName());
				if (workflowName.equals(def.getName())) {
					return def.getId();
				}
			}
		}

		logger.error("Unknown workflow name: " + workflowName);
		return null;
	}

	private void startWorkflow(String workflowName, String workflowDescription, Integer duration, NodeRef assignee) {
		Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
		Calendar cal = Calendar.getInstance();

		if (duration != null) {
			cal.add(Calendar.DAY_OF_YEAR, duration);
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, cal.getTime());
		}
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
		workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);

		NodeRef wfPackage = workflowService.createPackage(null);
		workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

		logger.debug("wf props: " + workflowProps);

		String workflowDefId = getWorkflowDefId(workflowName);
		if (workflowDefId != null) {

			WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, workflowProps);

			logger.debug("New worflow started. Id: " + wfPath.getId() + " - instance: " + wfPath.getInstance());

			// get the workflow tasks
			String workflowId = wfPath.getInstance().getId();
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
}
