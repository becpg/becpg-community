package fr.becpg.repo.project.formulation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DeliverableUrl;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * <p>TaskFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TaskFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static final int COMPLETED = 100;
	private static final int DEFAULT_WORK_HOURS_PER_DAY = 8;

	private static Log logger = LogFactory.getLog(TaskFormulationHandler.class);

	private ProjectWorkflowService projectWorkflowService;

	private ProjectService projectService;

	private ProjectActivityService projectActivityService;

	private NodeService nodeService;

	AlfrescoRepository<ProjectData> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>projectWorkflowService</code>.</p>
	 *
	 * @param projectWorkflowService a {@link fr.becpg.repo.project.ProjectWorkflowService} object.
	 */
	public void setProjectWorkflowService(ProjectWorkflowService projectWorkflowService) {
		this.projectWorkflowService = projectWorkflowService;
	}

	/**
	 * <p>Setter for the field <code>projectService</code>.</p>
	 *
	 * @param projectService a {@link fr.becpg.repo.project.ProjectService} object.
	 */
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * <p>Setter for the field <code>projectActivityService</code>.</p>
	 *
	 * @param projectActivityService a {@link fr.becpg.repo.project.ProjectActivityService} object.
	 */
	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
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
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProjectData projectData) {

		boolean isTpl = projectData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL);

		Set<TaskWrapper> tasks = TaskWrapper.extract(projectData);

		calculateStartDate(projectData, tasks, isTpl);

		boolean isOnHold = ProjectHelper.isOnHold(projectData);

		if (logger.isDebugEnabled()) {

			logger.debug("Formulate tasks for project: " + projectData.getName());
			logger.debug(" - mode :" + projectData.getPlanningMode());
			logger.debug(" - startDate :" + projectData.getStartDate());
			logger.debug(" - onHold :" + isOnHold);
			logger.debug("before formulate tasks:" + TaskWrapper.print(projectData));

		}

		if (isOnHold) {

			tasks.forEach(t -> {

				if (t.getTask() != null) {
					if (projectWorkflowService.isWorkflowActive(t.getTask())) {
						logger.debug("Cancel workflow of project " + projectData.getName() + " for task " + t.getTask().getTaskName());
						projectWorkflowService.cancelWorkflow(t.getTask());
					}
					t.getTask().setIsExcludeFromSearch(true);
				}

			});

		}
		// we don't want tasks of project template start
		// start project if startdate is before now and startdate != created
		// otherwise ProjectMgr will start it manually
		if (!isTpl && (ProjectState.Planned.equals(projectData.getProjectState())) && (projectData.getStartDate() != null)
				&& projectData.getStartDate().before(new Date())) {
			projectData.setProjectState(ProjectState.InProgress);
		}

		// even if project is not in Progress, we visit it because a task
		// can start the project (manual task or task that has startdate <
		// NOW)
		if (visit(projectData, tasks, !isOnHold && !isTpl)) {

			if (projectData.getReformulateCount() == null) {
				projectData.setReformulateCount(1);
			} else {
				if (projectData.getReformulateCount() < 3) {
					projectData.setReformulateCount(projectData.getReformulateCount() + 1);
				}
			}

		}

		visitParents(projectData, tasks, !isOnHold && !isTpl);

		visitProject(projectData, tasks, isTpl);
		
		// exclude project template tasks from search
		if (isTpl) {
			projectData.getTaskList().forEach(t -> t.setIsExcludeFromSearch(true));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("After formulate tasks:" + TaskWrapper.print(projectData));
		}

		return true;
	}

	private void calculateCapacity(TaskListDataItem tl) {
		if ((tl.getWork() != null) && (tl.getDuration() != null) && (tl.getDuration() != 0)) {
			double hoursPerDay = DEFAULT_WORK_HOURS_PER_DAY;
			if ((tl.getResourceCost() != null) && (tl.getResourceCost().getHoursPerDay() != null) && (tl.getResourceCost().getHoursPerDay() != 0d)) {
				hoursPerDay = tl.getResourceCost().getHoursPerDay();
			}
			tl.setCapacity((int) ((100 * tl.getWork()) / (tl.getDuration() * hoursPerDay)));
		}
	}

	private void visitParents(ProjectData projectData, Set<TaskWrapper> tasks, boolean calculateState) {

		// tasks whose critical cost has been calculated
		List<TaskWrapper> completed = new LinkedList<>();
		// tasks whose critical cost needs to be calculated
		Set<TaskWrapper> remaining = new HashSet<>(tasks);

		// while there are tasks whose critical cost isn't calculated.
		while (!remaining.isEmpty()) {
			boolean progress = false;

			// find a new task to calculate
			for (Iterator<TaskWrapper> it = remaining.iterator(); it.hasNext();) {
				TaskWrapper task = it.next();
				if (completed.containsAll(task.getChilds())) {
					if (task.getTask() != null) {

						if (task.isParent()) {

							Double work = 0d;
							boolean hasTaskInProgress = false;
							boolean allTasksPlanned = true;
							boolean allTasksCancelled = true;
							int completionPerc = 0;

							task.getTask().setStart(null);
							task.getTask().setEnd(null);
							task.getTask().setDue(null);
							task.getTask().setTargetStart(null);
							task.getTask().setTargetEnd(null);
							task.getTask().setManualDate(null);

							for (TaskWrapper child : task.getChilds()) {

								if ((child.getTask().getStart() != null)
										&& ((task.getTask().getStart() == null) || task.getTask().getStart().after(child.getTask().getStart()))) {
									ProjectHelper.setTaskStartDate(task.getTask(), child.getTask().getStart());
								}

								if ((child.getTask().getEnd() != null)
										&& ((task.getTask().getEnd() == null) || task.getTask().getEnd().before(child.getTask().getEnd()))) {
									ProjectHelper.setTaskEndDate(task.getTask(), child.getTask().getEnd());
								}
								
								if ((child.getTask().getDue() != null)
										&& ((task.getTask().getDue() == null) || task.getTask().getDue().before(child.getTask().getDue()))) {
									task.getTask().setDue(child.getTask().getDue());
								}

								if ((child.getTask().getTargetStart() != null) && ((task.getTask().getTargetStart() == null)
										|| task.getTask().getTargetStart().after(child.getTask().getTargetStart()))) {
									task.getTask().setTargetStart(child.getTask().getTargetStart());
								}

								if ((child.getTask().getTargetEnd() != null) && ((task.getTask().getTargetEnd() == null)
										|| task.getTask().getTargetEnd().before(child.getTask().getTargetEnd()))) {
									task.getTask().setTargetEnd(child.getTask().getTargetEnd());
								}

								if (child.getTask().getWork() != null) {
									work += child.getTask().getWork();
								}

								completionPerc += (child.getTask().getCompletionPercent() != null ? child.getTask().getCompletionPercent() : 0);
								if (TaskState.InProgress.equals(child.getTask().getTaskState())
										|| TaskState.OnHold.equals(child.getTask().getTaskState()) || child.getTask().isRefused()) {
									hasTaskInProgress = true;
									allTasksCancelled = false;
									allTasksPlanned = false;
								} else if (TaskState.Completed.equals(child.getTask().getTaskState())) {
									allTasksPlanned = false;
									allTasksCancelled = false;
								} else if (child.getTask().isPlanned()) {
									allTasksCancelled = false;
								}

							}

							Integer duration = ProjectHelper.calculateTaskDuration(task.getTask().getTargetStart(), task.getTask().getTargetEnd());
							Integer realDuration = ProjectHelper.calculateTaskDuration(task.getTask().getStart(), task.getTask().getEnd());


							if (duration == null) {
								logger.warn("Parent task duration is null:" + task.getTask().getTaskName());
							}
							
							task.getTask().setDuration(duration);
							task.getTask().setRealDuration(realDuration);
							task.getTask().setWork(work);

							if (calculateState) {

								if (hasTaskInProgress) {

									ProjectHelper.setTaskState(task.getTask(), TaskState.InProgress, projectActivityService);
								} else if (allTasksPlanned && !allTasksCancelled) {
									ProjectHelper.setTaskState(task.getTask(), TaskState.Planned, projectActivityService);
								} else if (allTasksCancelled) {
									ProjectHelper.setTaskState(task.getTask(), TaskState.Cancelled, projectActivityService);
								} else {
									ProjectHelper.setTaskState(task.getTask(), TaskState.Completed, projectActivityService);
								}
								task.getTask().setCompletionPercent(completionPerc / task.getChilds().size());

							}

						}

						calculateCapacity(task.getTask());

						// set task as calculated an remove
						completed.add(task);
					}

					it.remove();
					// note we are making progress
					progress = true;
				}
			}

			// If we haven't made any progress then a cycle must exist in
			// the graph and we wont be able to calculate the critical path
			if (!progress) {
				logger.warn("Cyclic dependency, algorithm stopped for project " + projectData.getName() + " (" + projectData.getNodeRef() + ")");
				return;
			}
		}

	}

	private void calculateStartDate(ProjectData projectData, Set<TaskWrapper> tasks, boolean isTpl) {

		if (PlanningMode.Planning.equals(projectData.getPlanningMode())) {
			Date startDate = null;
			Date targetStartDate = null;

			if (!isTpl) {
				startDate = ProjectHelper.getFirstStartDate(tasks);
				if (startDate == null) {
					if (projectData.getStartDate() == null) {
						startDate = ProjectHelper.calculateNextStartDate(projectData.getCreated());
					} else {
						startDate = projectData.getStartDate();
					}
				}
				if (projectData.getDueDate() == null) {
					targetStartDate = startDate;
				} else {
					targetStartDate = ProjectHelper.calculateStartDate(projectData.getDueDate(), TaskWrapper.calculateMaxDuration(tasks));
				}
			} else {
				startDate = ProjectHelper.calculateNextStartDate(new Date());
				targetStartDate = startDate;
			}
			

			projectData.setStartDate(startDate);
			projectData.setTargetStartDate(targetStartDate);
			projectData.setCompletionDate(startDate);

		} else {
			// retro-planning
			Date endDate = null;
			if (!isTpl) {
				endDate = ProjectHelper.getLastEndDate(tasks);
			} else {
				endDate = ProjectHelper.calculatePrevEndDate(new Date());
			}

			if (endDate == null) {
				endDate = projectData.getDueDate();
				if (endDate == null) {
					endDate = ProjectHelper.calculatePrevEndDate(projectData.getCreated());
				}
			} else {
				projectData.setDueDate(endDate);
			}
			if (!isTpl) {
				projectData.setStartDate(ProjectHelper.calculateStartDate(endDate, TaskWrapper.calculateMaxDuration(tasks)));
				projectData.setTargetStartDate(projectData.getStartDate());
			}

		}

	}

	private void visitProject(ProjectData projectData, Set<TaskWrapper> tasks, boolean isTpl) {

		double work = 0d;

		List<NodeRef> currLegends = new ArrayList<>();
		List<NodeRef> currTasks = new ArrayList<>();

		if (!tasks.isEmpty() && !isTpl) {

			boolean allTaskPlanned = true;
			boolean allTaskDone = true;
			int totalWork = 0;
			int workDone = 0;

			for (TaskWrapper task : tasks) {
				if (task.getTask() != null) {
					TaskState state = task.getTask().getTaskState();

					if (!TaskState.Cancelled.equals(state)) {
						if (!task.getTask().isPlanned()) {
							allTaskPlanned = false;
						}

						if (!(TaskState.Completed.equals(state))) {
							allTaskDone = false;
						}

						Integer duration = task.getDuration() != null ? task.getDuration()
								: ProjectHelper.calculateTaskDuration(task.getTask().getStart(), task.getTask().getEnd());
						if (duration != null) {
							totalWork += duration;
							if (TaskState.Completed.equals(state)) {
								workDone += duration;
							} else if ((task.isSubProject()) && (task.getTask().getCompletionPercent() != null)) {
								workDone += ((duration * task.getTask().getCompletionPercent()) / 100);
							}
						}

						if (TaskState.InProgress.equals(state)) {
							if (!currLegends.contains(task.getTask().getTaskLegend())) {
								currLegends.add(task.getTask().getTaskLegend());
							}
							currTasks.add(task.getTask().getNodeRef());
						}

						if (!task.isParent() && (task.getTask().getWork() != null)) {
							work += task.getTask().getWork();
						}

					}
				}
			}

			if (!allTaskPlanned && ProjectState.Planned.equals(projectData.getProjectState())) {
				projectData.setProjectState(ProjectState.InProgress);
			} else if (allTaskPlanned && ProjectState.InProgress.equals(projectData.getProjectState())) {
				projectData.setProjectState(ProjectState.Planned);
			}

			if (allTaskDone) {
				projectData.setCompletionDate(ProjectHelper.getLastEndDate(tasks));
				projectData.setCompletionPercent(COMPLETED);
				projectData.setProjectState(ProjectState.Completed);
			} else {
				projectData.setCompletionPercent(totalWork != 0 ? (100 * workDone) / totalWork : 0);
				// ici realduration est fausse

				projectData.setCompletionDate(ProjectHelper.calculateEndDate(projectData.getStartDate(), projectData.getRealDuration()));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Project completion date: " + projectData.getCompletionDate());
		}

		projectData.setCurrTasks(currTasks);
		projectData.setLegends(currLegends);
		projectData.setWork(work);

	}

	private boolean visit(ProjectData projectData, Set<TaskWrapper> allTasks, boolean calculateState) {
		// tasks whose critical cost has been calculated
		List<TaskWrapper> completed = new LinkedList<>();
		// tasks whose critical cost needs to be calculated
		Set<TaskWrapper> remaining = new HashSet<>(allTasks);
		int projectDuration = 0;
		int projectRealDuration = 0;
		boolean reformulate = false;

		// while there are tasks whose critical cost isn't calculated.
		while (!remaining.isEmpty()) {
			boolean progress = false;

			// find a new task to calculate
			for (Iterator<TaskWrapper> it = remaining.iterator(); it.hasNext();) {
				TaskWrapper task = it.next();
				if (completed.containsAll(task.getAncestors())) {

					if ((task.getTask() != null) && !task.isParent()) {

						calculatePlanning(projectData, task);

						if ((task.getMaxDuration() != null) && (projectDuration < task.getMaxDuration())) {
							projectDuration = task.getMaxDuration();
						}
						if ((task.getMaxRealDuration() != null) && (projectRealDuration < task.getMaxRealDuration())) {
							projectRealDuration = task.getMaxRealDuration();
							if(logger.isDebugEnabled()) {
								logger.debug(task.getTask().getTaskName() + " - maxRealDuration: " + task.getMaxRealDuration());
								for (TaskWrapper tmp : task.getAncestors()) {
									logger.debug("###-" + tmp.getTask().getTaskName());
								}
							}
						}

						if (calculateState) {
							reformulate = calculateState(projectData, task) || reformulate;
						}
					}
					// set task as calculated and remove
					completed.add(task);

					it.remove();
					// note we are making progress
					progress = true;
				}
			}
			// If we haven't made any progress then a cycle must exist in
			// the graph and we wont be able to calculate the critical path
			if (!progress) {
				logger.warn("Cyclic dependency, algorithm stopped for " + projectData.getName() + " (" + projectData.getNodeRef() + ")");
				return false;
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Project theoric duration: " + projectDuration);
			logger.debug("Project real duration: " + projectRealDuration);
			logger.debug("Project overdue: " + (projectRealDuration - projectDuration));
		}

		projectData.setDuration(projectDuration);
		projectData.setRealDuration(projectRealDuration);
		projectData.setOverdue(projectRealDuration - projectDuration);

		return reformulate;

	}

	private boolean calculateState(ProjectData projectData, TaskWrapper task) {

		boolean reformulate = false;

		task.getTask().setIsExcludeFromSearch(false);

		TaskState currentTaskState = task.getTask().getTaskState();
		// cancel active workflow if task is not anymore
		// InProgress
		if (!TaskState.InProgress.equals(task.getTask().getTaskState()) && projectWorkflowService.isWorkflowActive(task.getTask())) {
			projectWorkflowService.cancelWorkflow(task.getTask());
		}

		List<DeliverableListDataItem> deliverables = ProjectHelper.getDeliverables(projectData, task.getTask().getNodeRef());

		Date now = Calendar.getInstance().getTime();

		if (task.getTask().isPlanned()) {

			// no previous task
			if (task.isRoot()) {
				if ((task.getTask().getStart() != null) && task.getTask().getStart().before(now)) {
					logger.debug("Start first task.");
					task.getTask().setTaskState(TaskState.InProgress);
				}
			} else {

				// previous task are done
				if (previousDone(task) && ((task.getTask().getManualDate() == null)
						|| ((task.getTask().getStart() != null) && task.getTask().getStart().before(new Date())))) {
					task.getTask().setTaskState(TaskState.InProgress);
				}

			}

		} else if (task.getTask().isRefused() && (task.getTask().getRefusedTask() != null)) {
			boolean shouldRefused = true;
			logger.debug("Enter refused task");
			// Check if all brothers are closed
			for (TaskListDataItem brotherTask : ProjectHelper.getBrethrenTask(projectData, task.getTask())) {
				if (!task.getTask().getRefusedTask().equals(brotherTask) && TaskState.InProgress.equals(brotherTask.getTaskState())) {
					shouldRefused = false;
					logger.debug("Will not refused task as there is still some brother Open");
					break;
				}
			}

			if (shouldRefused) {
				logger.debug("Reopen path : " + task.getTask().getRefusedTask().getTaskName());

				ProjectHelper.reOpenRefusePath(projectData, task.getTask(), task.getTask().getRefusedTask(), task.getTask().getRefusedTasksToReopen(),
						projectActivityService);

				// Revisit tasks
				reformulate = true;
			}

		}

		// Order is important otherwise visitDeliverables is not called
		reformulate = visitDeliverables(projectData, task.getTask(), deliverables) || reformulate;

		if (TaskState.InProgress.equals(task.getTask().getTaskState())) {

			if (!TaskState.InProgress.equals(currentTaskState)) {
				projectActivityService.postTaskStateChangeActivity(task.getTask().getNodeRef(), null, currentTaskState.toString(),
						TaskState.InProgress.toString(), true);
			}

			assign(projectData, task.getTask(), deliverables);

		}

		if (logger.isDebugEnabled()) {
			logger.debug("Visit task : " + task.getTask().getTaskName() + " - state before: " + currentTaskState + ", after: "
					+ task.getTask().getTaskState() + ", reformulate: " + reformulate + " isRefused: " + task.getTask().getIsRefused());

		}

		return reformulate;
	}

	private boolean previousDone(TaskWrapper task) {

		for (TaskWrapper t : task.getAncestors()) {
			if ((!(TaskState.Completed.equals(t.getTask().getTaskState()) || TaskState.Cancelled.equals(t.getTask().getTaskState())))
					|| (TaskState.Cancelled.equals(t.getTask().getTaskState()) && !previousDone(t))) {
				return false;
			}
		}

		return true;
	}

	private void assign(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> deliverables) {
		if ((taskListDataItem.getResources() != null) && !taskListDataItem.getResources().isEmpty()) {

			// reassign task resources
			List<NodeRef> reassignedResources = extractReassignedPeople(projectData, taskListDataItem, taskListDataItem.getResources(), true);
			taskListDataItem.setResources(reassignedResources);
			
			//reassign notification authorities
			List<NodeRef> reassignedNotficationAuthorities = extractReassignedPeople(projectData, taskListDataItem, taskListDataItem.getNotificationAuthorities(), false);
			taskListDataItem.setNotificationAuthorities(reassignedNotficationAuthorities);
			
			taskListDataItem.setObservers(projectService.extractResources(projectData.getNodeRef(), taskListDataItem.getObservers()));

		}

		// check workflow instance (task may be reopened) and
		// workflow properties
		projectWorkflowService.checkWorkflowInstance(projectData, taskListDataItem, deliverables);

		// workflow (task may have been set as InProgress
		// with
		// UI)
		if (((taskListDataItem.getResources() != null) && !taskListDataItem.getResources().isEmpty())
				&& (((taskListDataItem.getWorkflowInstance() == null) || taskListDataItem.getWorkflowInstance().isEmpty())
						&& (taskListDataItem.getWorkflowName() != null) && !taskListDataItem.getWorkflowName().isEmpty())) {

			// start workflow
			projectWorkflowService.startWorkflow(projectData, taskListDataItem, deliverables);

		}

	}

	private List<NodeRef> extractReassignedPeople(ProjectData projectData, TaskListDataItem taskListDataItem, List<NodeRef> originalResources, boolean updatePermission) {
		
		List<NodeRef> resources = new ArrayList<>();
		
		for (NodeRef resource : projectService.extractResources(projectData.getNodeRef(), originalResources)) {
			NodeRef reassignResource = projectService.getReassignedResource(resource, new HashSet<>());
			
			NodeRef toAdd = resource;
			// check delegation
			if (reassignResource != null) {

				Date delegationStart = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_START);
				Date delegationEnd = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_END);

				if ((delegationStart != null)
						&& (taskListDataItem.getStart().after(delegationStart) || taskListDataItem.getStart().equals(delegationStart))) {

					if ((delegationEnd == null)
							|| (taskListDataItem.getStart().before(delegationEnd) || taskListDataItem.getStart().equals(delegationEnd))) {

						// reassign new tasks
						toAdd = reassignResource;
					}
				}

				else {
					if ((boolean) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_REASSIGN_TASK)) {
						// reassign current tasks
						toAdd = reassignResource;
					}
				}
			}

			if (updatePermission) {
				projectService.updateProjectPermission(projectData.getNodeRef(), taskListDataItem.getNodeRef(), toAdd, true);
			}

			resources.add(toAdd);
		}

		return resources;
	}

	private void calculatePlanning(ProjectData projectData, TaskWrapper task) {
		// all dependencies calculated, critical cost is max
		// dependency
		// critical cost, plus our cost
		int maxDuration = 0;
		int maxRealDuration = 0;

		Date startDate = null;
		Date targetStart = null;

		if (task.isRoot()) {

			startDate = projectData.getStartDate();
			targetStart = projectData.getTargetStartDate();

		} else {

			TaskWrapper criticalTask = null;
			for (TaskWrapper t : task.getAncestors()) {

				if ((t.getMaxDuration() != null) && (t.getMaxDuration() > maxDuration)) {
					maxDuration = t.getMaxDuration();
				}

				if ((t.getMaxDuration() != null) && (t.getMaxDuration() >= maxDuration)) {
					criticalTask = t;
				}

				if ((t.getMaxRealDuration() != null) && (t.getMaxRealDuration() > maxRealDuration)) {
					maxRealDuration = t.getMaxRealDuration();
				}

				Date endDate = t.getTask().getEnd() != null ? t.getTask().getEnd() : t.getTask().getStart();
				Date targetEnd = t.getTask().getTargetEnd() != null ? t.getTask().getTargetEnd() : t.getTask().getTargetStart();

				if (TaskState.Cancelled.equals(t.getTask().getTaskState())) {
					endDate = ProjectHelper.calculatePrevEndDate(t.getTask().getStart());
					targetEnd = ProjectHelper.calculatePrevEndDate(t.getTask().getTargetStart());
				}

				if ((startDate == null) || ((endDate != null) && startDate.before(endDate))) {
					startDate = endDate;
				}

				if ((targetStart == null) || ((targetEnd != null) && targetStart.before(targetEnd))) {
					targetStart = targetEnd;
				}
			}

			if (criticalTask != null) {
				criticalTask.getTask().setIsCritical(true);
			}

			if (startDate != null) {
				startDate = ProjectHelper.calculateNextStartDate(startDate);
			}

			if (targetStart != null) {
				targetStart = ProjectHelper.calculateNextStartDate(targetStart);
			}

		}
		if ((task.getTask().getSubProject() == null)) {

			if ((startDate != null)) {
				ProjectHelper.setTaskStartDate(task.getTask(), startDate);

				if (((task.getTask().getDuration() != null) || (Boolean.TRUE.equals(task.getTask().getIsMilestone())))) {
					Date dueDate = ProjectHelper.calculateEndDate(task.getTask().getStart(), task.getTask().getDuration());
					
					Date endDate = dueDate;
					Date now = Calendar.getInstance().getTime();

					if (TaskState.OnHold.equals(task.getTask().getTaskState()) || TaskState.InProgress.equals(task.getTask().getTaskState()) && (endDate != null) && endDate.before(now)) {
						endDate = now;
					}

					task.getTask().setDue(dueDate!=null ? ProjectHelper.removeTime(dueDate): null);
					ProjectHelper.setTaskEndDate(task.getTask(), endDate);
				}

			}

			if (targetStart != null) {
				task.getTask().setTargetStart(ProjectHelper.removeTime(targetStart));

				if (((task.getTask().getDuration() != null) || (Boolean.TRUE.equals(task.getTask().getIsMilestone())))) {
					Date targetEnd = ProjectHelper.calculateEndDate(task.getTask().getTargetStart(), task.getTask().getDuration());
					task.getTask().setTargetEnd(ProjectHelper.removeTime(targetEnd));
				}

			}
		}
		if (!TaskState.Cancelled.equals(task.getTask().getTaskState())) {
			if (task.getDuration() != null) {
				task.setMaxDuration(maxDuration + task.getDuration());
			}

		} else {
			task.setMaxDuration(maxDuration);
		}

		Integer realDuration = task.computeRealDuration();
		task.getTask().setRealDuration(realDuration);

		if (TaskState.Completed.equals(task.getTask().getTaskState())) {
			task.setMaxRealDuration(ProjectHelper.calculateTaskDuration(projectData.getStartDate(), task.getTask().getEnd()));
		} else {
			if (realDuration != null) {
				task.setMaxRealDuration(maxRealDuration + realDuration);
			}
		}

	}

	private boolean visitDeliverables(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> deliverables) {

		TaskState previousState = taskListDataItem.getTaskState();

		if (TaskState.Completed.equals(taskListDataItem.getTaskState()) || TaskState.InProgress.equals(taskListDataItem.getTaskState())) {

			Integer taskCompletionPercent = TaskState.InProgress.equals(taskListDataItem.getTaskState()) ? 0 : COMPLETED;

			for (DeliverableListDataItem deliverable : deliverables) {

				if (TaskState.InProgress.equals(taskListDataItem.getTaskState())) {

					// Completed or Closed
					if ((deliverable.getCompletionPercent() != null) && (DeliverableState.Completed.equals(deliverable.getState())
							|| DeliverableState.Closed.equals(deliverable.getState()))) {
						taskCompletionPercent += deliverable.getCompletionPercent();
					}

					// set Planned dl InProgress
					if (DeliverableState.Planned.equals(deliverable.getState())) {
						deliverable.setState(DeliverableState.InProgress);
						
						if ((deliverable.getUrl() != null) && deliverable.getUrl().startsWith(DeliverableUrl.CONTENT_URL_PREFIX)
								&& NodeRef.isNodeRef(deliverable.getUrl().substring(DeliverableUrl.CONTENT_URL_PREFIX.length()))) {
							deliverable.setContent(new NodeRef(deliverable.getUrl().substring(DeliverableUrl.CONTENT_URL_PREFIX.length())));
							deliverable.setUrl(null);
						}

					}

					if (DeliverableState.InProgress.equals(deliverable.getState())
							&& DeliverableScriptOrder.Pre.equals(deliverable.getScriptOrder())) {
						projectService.runScript(projectData, taskListDataItem, deliverable);
						deliverable.setState(DeliverableState.Completed);
					}
				}

				if (TaskState.Completed.equals(taskListDataItem.getTaskState()) && DeliverableState.InProgress.equals(deliverable.getState())) {
					if (DeliverableScriptOrder.Post.equals(deliverable.getScriptOrder())) {
						projectService.runScript(projectData, taskListDataItem, deliverable);
					}
					deliverable.setState(DeliverableState.Completed);

				}

			}

			taskListDataItem.setCompletionPercent(taskCompletionPercent);

			// Status can change during script execution
			if (!previousState.equals(taskListDataItem.getTaskState()) && TaskState.InProgress.equals(taskListDataItem.getTaskState())) {
				logger.debug("Task " + taskListDataItem.getTaskName() + " reopen by script " + taskListDataItem.getTaskState());

				return true;
			}

		} else if (TaskState.Planned.equals(taskListDataItem.getTaskState())) {
			taskListDataItem.setCompletionPercent(0);
		}

		return false;
	}

}
