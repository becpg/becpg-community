package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
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
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.AbstractProjectData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableState;
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

	private static final String DESCRIPTION_SEPARATOR = ", ";
	
	private static final String QUERY_TASK_LEGEND = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"pjt:taskLegend\"";
	
	private static final String PATH_PROJECT_CONTAINER = "./"+RepoConsts.PATH_SYSTEM+"/"+RepoConsts.PATH_PROJECTS;

	private static Log logger = LogFactory.getLog(ProjectServiceImpl.class);

	private BeCPGListDao<AbstractProjectData> projectDAO;
	private WorkflowService workflowService;
	private WUsedListService wUsedListService;
	private AssociationService associationService;
	private NodeService nodeService;
	private BeCPGSearchService beCPGSearchService;
	private RepoService repoService;

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
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}
	
	

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	@Override
	public void startNextTasks(NodeRef taskListNodeRef) {

		NodeRef projectNodeRef = wUsedListService.getRoot(taskListNodeRef);
		startNextTasks(projectNodeRef, taskListNodeRef);
	}

	@Override
	public void start(NodeRef projectNodeRef) {
		startNextTasks(projectNodeRef, null);
	}

	private void startNextTasks(NodeRef projectNodeRef, NodeRef taskListNodeRef) {

		logger.debug("submit Task taskListNodeRef: " + taskListNodeRef);
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		AbstractProjectData abstractProjectData = projectDAO.find(projectNodeRef, dataLists);

		if (abstractProjectData instanceof ProjectData) {

			ProjectData projectData = (ProjectData) abstractProjectData;

			// add next tasks
			List<TaskListDataItem> nextTasks = getNextTasks(projectData, taskListNodeRef);
			logger.debug("nextTasks size: " + nextTasks.size());
			for (TaskListDataItem nextTask : nextTasks) {
				if (areTasksDone(projectData, nextTask.getPrevTasks())) {

					nextTask.setState(TaskState.InProgress);

					// deliverable list
					String workflowDescription = "";
					List<DeliverableListDataItem> nextDeliverables = getDeliverables(projectData, nextTask.getNodeRef());
					logger.debug("next deliverables size: " + nextDeliverables.size());
					for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

						nextDeliverable.setState(DeliverableState.InProgress);

						if (!workflowDescription.isEmpty()) {
							workflowDescription += DESCRIPTION_SEPARATOR;
						}
						workflowDescription += nextDeliverable.getDescription();
					}

					logger.debug("assignees size: " + nextTask.getResources());
					if (nextTask.getResources() != null) {
						for (NodeRef resource : nextTask.getResources()) {
							// start workflow
							startWorkflow(projectData, nextTask, workflowDescription, resource);
						}
					}
				}
			}

			projectDAO.update(projectNodeRef, projectData, dataLists);
		}
	}

	private List<TaskListDataItem> getNextTasks(AbstractProjectData projectData, NodeRef taskListNodeRef) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		for (TaskListDataItem p : projectData.getTaskList()) {
			// taskNodeRef is null when we start project
			if (p.getPrevTasks().contains(taskListNodeRef) || (taskListNodeRef == null && p.getPrevTasks().isEmpty())) {
				taskList.add(p);
			}
		}
		return taskList;
	}

	private List<DeliverableListDataItem> getDeliverables(AbstractProjectData projectData, NodeRef taskListNodeRef) {

		List<DeliverableListDataItem> deliverableList = new ArrayList<DeliverableListDataItem>();
		for (DeliverableListDataItem d : projectData.getDeliverableList()) {
			if (d.getTask() != null && d.getTask().equals(taskListNodeRef)) {
				deliverableList.add(d);
			}
		}
		return deliverableList;
	}

	private boolean areTasksDone(ProjectData projectData, List<NodeRef> taskNodeRefs) {

		List<NodeRef> inProgressTasks = new ArrayList<NodeRef>();
		inProgressTasks.addAll(taskNodeRefs);

		if (projectData.getTaskList() != null && projectData.getTaskList().size() > 0) {
			for (int i = projectData.getTaskList().size() - 1; i >= 0; i--) {
				TaskListDataItem t = projectData.getTaskList().get(i);

				if (taskNodeRefs.contains(t.getNodeRef()) && TaskState.Completed.equals(t.getState())) {
					inProgressTasks.remove(t.getNodeRef());
				}
			}
		}

		return inProgressTasks.isEmpty();
	}

	private String getWorkflowDefId(String workflowName) {
		if (workflowName != null && !workflowName.isEmpty()) {
			WorkflowDefinition def = workflowService.getDefinitionByName(workflowName);
			if (def != null) {
				return def.getId();
			}
		}

		logger.error("Unknown workflow name: " + workflowName);
		return null;
	}

	private void startWorkflow(ProjectData projectData, TaskListDataItem taskListDataItem, String workflowDescription,
			NodeRef assignee) {
		Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
		Calendar cal = Calendar.getInstance();

		if (taskListDataItem.getDuration() != null) {
			cal.add(Calendar.DAY_OF_YEAR, taskListDataItem.getDuration());
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, cal.getTime());
		}
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
		workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
		workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
		workflowProps.put(ProjectModel.ASSOC_WORKFLOW_TASK, taskListDataItem.getNodeRef());

		NodeRef wfPackage = workflowService.createPackage(null);
		nodeService.addChild(wfPackage, projectData.getNodeRef(), WorkflowModel.ASSOC_PACKAGE_CONTAINS,
				ContentModel.ASSOC_CHILDREN);
		if (projectData.getEntity() != null) {
			nodeService.addChild(wfPackage, projectData.getEntity(), WorkflowModel.ASSOC_PACKAGE_CONTAINS,
					ContentModel.ASSOC_CHILDREN);
		}
		workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

		String workflowDefId = getWorkflowDefId(taskListDataItem.getWorkflowName());
		logger.debug("workflowDefId: " + workflowDefId);
		if (workflowDefId != null) {

			WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, workflowProps);
			logger.debug("New worflow started. Id: " + wfPath.getId() + " - instance: " + wfPath.getInstance());
			String workflowId = wfPath.getInstance().getId();
			taskListDataItem.setWorkflowInstance(workflowId);

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

		Integer completionPercent = (Integer) nodeService.getProperty(deliverableNodeRef,
				ProjectModel.PROP_COMPLETION_PERCENT);
		logger.debug("submit Deliverable. completionPercent: " + completionPercent);

		if (completionPercent != null) {
			NodeRef taskNodeRef = associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);

			if (taskNodeRef != null) {
				NodeRef projectNodeRef = wUsedListService.getRoot(deliverableNodeRef);

				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(ProjectModel.TYPE_TASK_LIST);
				dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				for (TaskListDataItem t : projectData.getTaskList()) {
					if (taskNodeRef.equals(t.getNodeRef())) {
						Integer taskCompletionPercent = t.getCompletionPercent();
						if (taskCompletionPercent == null) {
							taskCompletionPercent = 0;
						}
						taskCompletionPercent += completionPercent;
						t.setCompletionPercent(taskCompletionPercent);
						logger.debug("set completion percent to value " + taskCompletionPercent);

						if (taskCompletionPercent > 100) {
							logger.debug("submit task");
							t.setState(TaskState.Completed);
						}
					}
				}
				projectDAO.update(projectNodeRef, projectData, dataLists);
			} else {
				logger.error("Task is not defined for the deliverable. nodeRef: " + deliverableNodeRef);
			}
		}
	}

	@Override
	public List<NodeRef> getTaskLegendList() {
		return beCPGSearchService.luceneSearch(QUERY_TASK_LEGEND);
	}

	@Override
	public NodeRef getProjectsContainer(String siteId) {
		return repoService.getFolderByPath(PATH_PROJECT_CONTAINER);
	}
}
