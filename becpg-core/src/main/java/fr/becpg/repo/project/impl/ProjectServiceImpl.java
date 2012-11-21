package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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

	private static final int COMPLETED = 100;
	private static final String QUERY_TASK_LEGEND = "+TYPE:\"pjt:taskLegend\"";
	private static final int DURATION_NEXT_DAY = 2;
	private static final int DURATION_DEFAULT = 1;
	private static final String WORKFLOW_DESCRIPTION = "%s : ";
	
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
	public void submitTask(NodeRef taskListNodeRef) {

		NodeRef projectNodeRef = wUsedListService.getRoot(taskListNodeRef);
		submitTask(projectNodeRef, taskListNodeRef);
	}

	@Override
	public void start(NodeRef projectNodeRef) {
		submitTask(projectNodeRef, null);
	}

	private void submitTask(NodeRef projectNodeRef, NodeRef taskListNodeRef) {

		logger.debug("submit Task taskListNodeRef: " + taskListNodeRef);
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		AbstractProjectData abstractProjectData = projectDAO.find(projectNodeRef, dataLists);

		if (abstractProjectData instanceof ProjectData) {

			ProjectData projectData = (ProjectData) abstractProjectData;

			// end task
			TaskListDataItem currentTaskDataItem = getTask(projectData, taskListNodeRef);
			if (currentTaskDataItem != null) {
				// we force
				setTaskEndDate(currentTaskDataItem, new Date(), true);
				currentTaskDataItem.setDuration(calculateTaskDuration(currentTaskDataItem.getStart(),
						currentTaskDataItem.getEnd()));
				currentTaskDataItem.setCompletionPercent(COMPLETED);

				// set deliverables as completed
				List<DeliverableListDataItem> deliverables = getDeliverables(projectData, taskListNodeRef);
				logger.debug("deliverables size: " + deliverables.size());
				for (DeliverableListDataItem deliverable : deliverables) {
					deliverable.setState(DeliverableState.Completed);
				}
			}

			// add next tasks
			List<TaskListDataItem> nextTasks = getNextTasks(projectData, taskListNodeRef);
			logger.debug("nextTasks size: " + nextTasks.size());
			if (nextTasks.size() > 0) {
				for (TaskListDataItem nextTask : nextTasks) {
					if (areTasksDone(projectData, nextTask.getPrevTasks())) {

						// start task
						setTaskStartDate(nextTask, new Date(), false);
						nextTask.setState(TaskState.InProgress);

						// deliverable list
						String workflowDescription = String.format(WORKFLOW_DESCRIPTION, projectData.getName());
						boolean isFirst = true;
						List<DeliverableListDataItem> nextDeliverables = getDeliverables(projectData,
								nextTask.getNodeRef());
						logger.debug("next deliverables size: " + nextDeliverables.size());
						for (DeliverableListDataItem nextDeliverable : nextDeliverables) {

							// set Planned dl InProgress
							if(DeliverableState.Planned.equals(nextDeliverable.getState())){
								nextDeliverable.setState(DeliverableState.InProgress);
							}							

							if(DeliverableState.InProgress.equals(nextDeliverable.getState())){
								if (isFirst) {
									isFirst = false;
								}
								else{
									workflowDescription += DESCRIPTION_SEPARATOR;
								}
								workflowDescription += nextDeliverable.getDescription();
							}							
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
				projectData.setCompletionPercent(geProjectCompletionPercent(projectData));
			} else {
				projectData.setCompletionDate(new Date());
				projectData.setCompletionPercent(COMPLETED);
			}

			projectDAO.update(projectNodeRef, projectData, dataLists);
		}
	}

	private TaskListDataItem getTask(AbstractProjectData projectData, NodeRef taskListNodeRef) {

		if (taskListNodeRef != null && projectData.getTaskList() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				if (taskListNodeRef.equals(p.getNodeRef())) {
					return p;
				}
			}
		}
		return null;
	}

	private List<TaskListDataItem> getNextTasks(AbstractProjectData projectData, NodeRef taskListNodeRef) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		if (projectData.getTaskList() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				// taskNodeRef is null when we start project
				if (p.getPrevTasks().contains(taskListNodeRef)
						|| (taskListNodeRef == null && p.getPrevTasks().isEmpty())) {
					taskList.add(p);
				}
			}
		}
		return taskList;
	}

	private List<TaskListDataItem> getPrevTasks(AbstractProjectData projectData, TaskListDataItem taskListDataItem) {

		List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
		if (taskListDataItem.getPrevTasks() != null) {
			for (TaskListDataItem p : projectData.getTaskList()) {
				if (taskListDataItem.getPrevTasks().contains(p.getNodeRef())) {
					taskList.add(p);
				}
			}
		}
		return taskList;
	}

	private List<DeliverableListDataItem> getDeliverables(AbstractProjectData projectData, NodeRef taskListNodeRef) {

		List<DeliverableListDataItem> deliverableList = new ArrayList<DeliverableListDataItem>();
		if (projectData.getDeliverableList() != null) {
			for (DeliverableListDataItem d : projectData.getDeliverableList()) {
				if (d.getTask() != null && d.getTask().equals(taskListNodeRef)) {
					deliverableList.add(d);
				}
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

	/**
	 * completedPercent is calculated on duration property
	 * 
	 * @param projectData
	 * @return
	 */
	private int geProjectCompletionPercent(AbstractProjectData projectData) {

		int totalWork = 0;
		int workDone = 0;
		for (TaskListDataItem p : projectData.getTaskList()) {
			if (p.getDuration() != null) {
				totalWork += p.getDuration();
				if (TaskState.Completed.equals(p.getState()) || TaskState.Cancelled.equals(p.getState())) {
					workDone += p.getDuration();
				}
			}
		}

		return totalWork != 0 ? 100 * workDone / totalWork : 0;
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
				ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

				for (TaskListDataItem t : projectData.getTaskList()) {
					if (taskNodeRef.equals(t.getNodeRef())) {
						Integer taskCompletionPercent = t.getCompletionPercent();
						if (taskCompletionPercent == null) {
							taskCompletionPercent = 0;
						}

						taskCompletionPercent += completionPercent;

						if (taskCompletionPercent < COMPLETED) {
							logger.debug("set completion percent to value " + taskCompletionPercent);
							t.setCompletionPercent(taskCompletionPercent);
						} else {
							logger.debug("set completion percent to 100%");
							t.setCompletionPercent(COMPLETED);
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
	public void openDeliverable(NodeRef deliverableNodeRef) {

		Integer completionPercent = (Integer) nodeService.getProperty(deliverableNodeRef,
				ProjectModel.PROP_COMPLETION_PERCENT);
		logger.debug("open Deliverable. completionPercent: " + completionPercent);

		NodeRef taskNodeRef = associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);

		if (taskNodeRef != null) {
			NodeRef projectNodeRef = wUsedListService.getRoot(deliverableNodeRef);

			Collection<QName> dataLists = new ArrayList<QName>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			ProjectData projectData = (ProjectData) projectDAO.find(projectNodeRef, dataLists);

			// reopen the task InProgress
			for (TaskListDataItem t : projectData.getTaskList()) {
				if (taskNodeRef.equals(t.getNodeRef())) {

					// TODO : use sort to place task
					if (t.getDuration() != null && completionPercent != null) {
						int newDuration = t.getDuration() * completionPercent;
						t.setDuration(t.getDuration() + newDuration);
					}
					t.setState(TaskState.InProgress);
					break;
				}
			}

			projectDAO.update(projectNodeRef, projectData, dataLists);
		} else {
			logger.error("Task is not defined for the deliverable. nodeRef: " + deliverableNodeRef);
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
	public void calculateTaskDates(NodeRef taskListNodeRef) {

		NodeRef projectNodeRef = wUsedListService.getRoot(taskListNodeRef);
		calculateProjectDates(projectNodeRef, taskListNodeRef);
	}

	@Override
	public void initializeProjectDates(NodeRef projectNodeRef) {

		calculateProjectDates(projectNodeRef, null);
	}

	private void calculateProjectDates(NodeRef projectNodeRef, NodeRef taskListNodeRef) {

		logger.debug("calculateProjectDates " + projectNodeRef);

		if (projectNodeRef != null) {

			Collection<QName> dataLists = new ArrayList<QName>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			AbstractProjectData abstractProjectData = (AbstractProjectData) projectDAO.find(projectNodeRef, dataLists);

			if (abstractProjectData instanceof ProjectData) {

				ProjectData projectData = (ProjectData) abstractProjectData;
				if (projectData.getStartDate() == null) {
					projectData.setStartDate(new Date());
				}
				Date completionDate = projectData.getStartDate();

				for (TaskListDataItem t : projectData.getTaskList()) {

					if (taskListNodeRef == null || taskListNodeRef.equals(t.getNodeRef())) {
						// 1st task
						if (t.getPrevTasks() == null || t.getPrevTasks().size() == 0) {
							//init if startDate is null or startDate is before projectStart
							if(t.getStart() == null || t.getStart().before(projectData.getStartDate())){
								setTaskStartDate(t, projectData.getStartDate(), false);
							}							
						} else {
							Date endDate = getLastEndDate(getPrevTasks(projectData, t));
							setTaskStartDate(t, calculateNextStartDate(endDate), false);
						}
						calculateTaskDates(projectData, t);					
					}
					
					logger.debug("completionDate: " + completionDate);
					logger.debug("t.getEnd(): " + t.getEnd());
					if (completionDate.before(t.getEnd())) {
						completionDate = t.getEnd();
					}
				}

				projectData.setCompletionDate(completionDate);
				projectDAO.update(projectNodeRef, projectData, dataLists);
			}
		} else {
			logger.error("Task is not defined. taskListNodeRef: " + taskListNodeRef);
		}
	}

	private void calculateTaskDates(AbstractProjectData projectData, TaskListDataItem t) {

		logger.debug("###calculateTaskDates " + t.getTaskName());

		// current task
		setTaskEndDate(t, calculateEndDate(t.getStart(), t.getDuration()), false);
		logger.debug("t.getEnd(): " + t.getEnd());

		// current next tasks
		for (TaskListDataItem nextTask : getNextTasks(projectData, t.getNodeRef())) {
			
			// avoid cycle
			if(t.getNodeRef() != null && nextTask.getNodeRef() != null && t.getNodeRef().equals(nextTask.getNodeRef())){
				logger.error("cycle detected on task " + t.getTaskName());
				return;
			}

			Date endDate = getLastEndDate(getPrevTasks(projectData, nextTask));
			setTaskStartDate(nextTask, calculateNextStartDate(endDate), false);
			calculateTaskDates(projectData, nextTask);
		}
	}

	private Date getLastEndDate(List<TaskListDataItem> tasks) {
		Date endDate = new Date(Long.MIN_VALUE);
		for (TaskListDataItem task : tasks) {
			if (task.getEnd() != null && task.getEnd().after(endDate)) {
				endDate = task.getEnd();
			}
		}
		return endDate;
	}

	private void setTaskStartDate(TaskListDataItem t, Date startDate, boolean force) {
		logger.debug("task: " + t.getTaskName() + " state: " + t.getState() + " start: " + startDate);
		if (TaskState.Planned.equals(t.getState()) || force) {
			t.setStart(removeTime(startDate));
		}
	}

	private void setTaskEndDate(TaskListDataItem t, Date endDate, boolean force) {
		logger.debug("task: " + t.getTaskName() + " state: " + t.getState() + " end: " + endDate);
		if (TaskState.Planned.equals(t.getState()) || TaskState.InProgress.equals(t.getState()) || force) {
			t.setEnd(removeTime(endDate));
		}
	}

	private Date removeTime(Date date) {
		if (date == null) {
			return null;
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		}
	}

	@Override
	public Date calculateEndDate(Date startDate, Integer duration) {
		return calculateNextDate(startDate, duration);
	}

	@Override
	public Date calculateNextStartDate(Date endDate) {
		return calculateNextDate(endDate, DURATION_NEXT_DAY);
	}

	private Date calculateNextDate(Date startDate, Integer duration) {

		if (startDate == null) {
			return null;
		}

		if (duration == null) {
			duration = DURATION_DEFAULT;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		int i = 1;
		while (i < duration) {
			calendar.add(Calendar.DATE, 1);
			if (isWorkingDate(calendar)) {
				i++;
			}
		}

		return calendar.getTime();
	}

	private boolean isWorkingDate(Calendar calendar) {
		// saturday == 7 || sunday == 1
		return calendar.get(Calendar.DAY_OF_WEEK) != 7 && calendar.get(Calendar.DAY_OF_WEEK) != 1;
	}

	@Override
	public int calculateTaskDuration(Date startDate, Date endDate) {

		if (startDate == null || endDate == null) {
			logger.error("startDate or endDate is null. startDate: " + startDate + " - endDate: " + endDate);
			return -1;
		}

		if (startDate.after(endDate)) {
			logger.error("startDate is after endDate");
			return -1;
		}

		int duration = 1;
		Calendar startDateCal = Calendar.getInstance();
		startDateCal.setTime(startDate);
		Calendar endDateCal = Calendar.getInstance();
		endDateCal.setTime(endDate);
		while (startDateCal.before(endDateCal)) {
			if (isWorkingDate(startDateCal)) {
				duration++;
			}
			startDateCal.add(Calendar.DAY_OF_MONTH, 1);
		}

		logger.debug("calculateTaskDuration startDate: " + startDate + " - endDate: " + endDate + " - duration: "
				+ duration);
		return duration;
	}

	@Override
	public void cancel(NodeRef projectNodeRef) {

		logger.debug("cancel project: " + projectNodeRef);
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		AbstractProjectData abstractProjectData = projectDAO.find(projectNodeRef, dataLists);

		for (TaskListDataItem taskListDataItem : abstractProjectData.getTaskList()) {
			if (taskListDataItem.getWorkflowInstance() != null
					&& workflowService.getWorkflowById(taskListDataItem.getWorkflowInstance()).isActive()) {
				logger.debug("Cancel workflow instance: " + taskListDataItem.getWorkflowInstance());
				workflowService.cancelWorkflow(taskListDataItem.getWorkflowInstance());
			}
		}
	}
}
