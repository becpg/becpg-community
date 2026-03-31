package fr.becpg.repo.project.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DeliverableUrl;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.formulation.FormulatedEntityHelper;
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
import fr.becpg.repo.project.impl.CalendarWorkingDayProvider;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.project.impl.WorkingDayProvider;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.system.SystemConfigurationService;

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

	private PersonService personService;

	private SystemConfigurationService systemConfigurationService;

	private EntityDictionaryService entityDictionaryService;

	private NamespaceService namespaceService;

	private fr.becpg.repo.project.CalendarService calendarService;

	public void setCalendarService(fr.becpg.repo.project.CalendarService calendarService) {
		this.calendarService = calendarService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	AlfrescoRepository<ProjectData> alfrescoRepository;

	private String myProjectAttributes() {
		return systemConfigurationService.confValue("project.extractor.myProjectAttributes");
	}

	public void setProjectWorkflowService(ProjectWorkflowService projectWorkflowService) {
		this.projectWorkflowService = projectWorkflowService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

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

		if (!isTpl && (ProjectState.Planned.equals(projectData.getProjectState())) && (projectData.getStartDate() != null)
				&& projectData.getStartDate().before(new Date())) {
			projectData.setProjectState(ProjectState.InProgress);
		}

		if (visit(projectData, tasks, !isOnHold && !isTpl)) {
			FormulatedEntityHelper.incrementReformulateCount(projectData);
		}

		if (projectData.isDirtyTaskTree()) {
			projectData.setDirtyTaskTree(false);
		} else {
			visitParents(projectData, tasks, !isOnHold && !isTpl);
			visitProject(projectData, tasks, isTpl);
			if (isTpl) {
				projectData.getTaskList().forEach(t -> t.setIsExcludeFromSearch(true));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("After formulate tasks:" + TaskWrapper.print(projectData));
			}
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

		List<TaskWrapper> completed = new ArrayList<>();
		Set<TaskWrapper> remaining = new HashSet<>(tasks);

		while (!remaining.isEmpty()) {
			boolean progress = false;

			for (Iterator<TaskWrapper> it = remaining.iterator(); it.hasNext();) {
				TaskWrapper task = it.next();
				if (completed.containsAll(task.getChilds())) {
					if (task.getTask() != null) {

						if (task.isParent()) {

							Double work = 0d;
							boolean hasTaskInProgress = false;
							boolean allTasksPlanned = true;
							boolean allTasksCancelled = true;

							int weightedCompletionSum = 0;
							int totalDurationWeight = 0;

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

								int childDuration = (child.getDuration() != null) ? child.getDuration() : 1;
								int childCompletion = (child.getTask().getCompletionPercent() != null) ? child.getTask().getCompletionPercent() : 0;
								weightedCompletionSum += childCompletion * childDuration;
								totalDurationWeight += childDuration;

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

							NodeRef calendarRef = calendarService.getCalendar(task.getTask().getNodeRef());
							WorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

							Integer duration = ProjectHelper.calculateTaskDuration(task.getTask().getTargetStart(), task.getTask().getTargetEnd(),
									provider);
							Integer realDuration = ProjectHelper.calculateTaskDuration(task.getTask().getStart(), task.getTask().getEnd(), provider);

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

								task.getTask().setCompletionPercent(totalDurationWeight > 0 ? weightedCompletionSum / totalDurationWeight : 0);
							}
						}

						calculateCapacity(task.getTask());

						completed.add(task);
					}

					it.remove();
					progress = true;
				}
			}

			if (!progress) {
				logger.warn("Cyclic dependency, algorithm stopped for project " + projectData.getName() + " (" + projectData.getNodeRef() + ")");
				return;
			}
		}
	}

	private void calculateStartDate(ProjectData projectData, Set<TaskWrapper> tasks, boolean isTpl) {

		NodeRef calendarRef = calendarService.getCalendar(projectData.getNodeRef());
		WorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

		if (PlanningMode.Planning.equals(projectData.getPlanningMode())) {
			Date startDate = null;
			Date targetStartDate = null;

			if (!isTpl) {
				startDate = ProjectHelper.getFirstStartDate(tasks);
				if (startDate == null) {
					if (projectData.getStartDate() == null) {
						startDate = ProjectHelper.calculateNextStartDate(projectData.getCreated(), provider);
					} else {
						startDate = projectData.getStartDate();
					}
				}
				if (projectData.getDueDate() == null) {
					targetStartDate = startDate;
				} else {
					targetStartDate = ProjectHelper.calculateStartDate(projectData.getDueDate(), TaskWrapper.calculateMaxDuration(tasks), provider);
				}
			} else {
				startDate = ProjectHelper.calculateNextStartDate(new Date(), provider);
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
				endDate = ProjectHelper.calculatePrevEndDate(new Date(), provider);
			}

			if (endDate == null) {
				endDate = projectData.getDueDate();
				if (endDate == null) {
					endDate = ProjectHelper.calculatePrevEndDate(projectData.getCreated(), provider);
				}
			} else {
				projectData.setDueDate(endDate);
			}
			if (!isTpl) {
				projectData.setStartDate(ProjectHelper.calculateStartDate(endDate, TaskWrapper.calculateMaxDuration(tasks), provider));
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

						if (!TaskState.Completed.equals(state)) {
							allTaskDone = false;
						}

						Integer duration = task.getDuration() != null ? task.getDuration()
								: ProjectHelper.calculateTaskDuration(task.getTask().getStart(), task.getTask().getEnd(),
										new CalendarWorkingDayProvider(calendarService, calendarService.getCalendar(task.getTask().getNodeRef())));
						if (duration != null) {
							totalWork += duration;

							int completionPct = (task.getTask().getCompletionPercent() != null) ? task.getTask().getCompletionPercent() : 0;
							if (TaskState.Completed.equals(state)) {
								workDone += duration;
							} else if (task.isSubProject()) {
								workDone += (duration * completionPct) / 100;
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


		    Date lastTaskEnd = ProjectHelper.getLastEndDate(tasks);
			if (allTaskDone) {
				projectData.setCompletionDate(lastTaskEnd);
				projectData.setCompletionPercent(COMPLETED);
				projectData.setProjectState(ProjectState.Completed);
			} else {
				projectData.setCompletionPercent(totalWork != 0 ? (100 * workDone) / totalWork : 0);
				
			    Date base = lastTaskEnd != null ? lastTaskEnd : projectData.getStartDate();

			    NodeRef calendarRef = calendarService.getCalendar(projectData.getNodeRef());
			    WorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);
			    projectData.setCompletionDate(
			        base != null ? ProjectHelper.calculateEndDate(base, projectData.getOverdue(), provider) : null
			    );
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Project completion date: " + projectData.getCompletionDate());
		}

		projectData.setCurrTasks(currTasks);
		projectData.setLegends(currLegends);
		projectData.setWork(work);

		if (!isTpl) {
			List<NodeRef> projectOwners = new ArrayList<>();
			for (String prop : myProjectAttributes().split(",")) {
				QName propQname = QName.createQName(prop, namespaceService);
				if (entityDictionaryService.isAssoc(propQname)) {
					List<NodeRef> nodes = nodeService.getTargetAssocs(projectData.getNodeRef(), propQname).stream().map(AssociationRef::getTargetRef)
							.toList();
					projectOwners.addAll(nodes);
				} else {
					Serializable person = nodeService.getProperty(projectData.getNodeRef(), propQname);
					if (person instanceof NodeRef personNodeRef) {
						projectOwners.add(personNodeRef);
					} else if (person instanceof String userName && personService.personExists(userName)) {
						projectOwners.add(personService.getPerson(userName));
					}
				}
			}
			projectData.setProjectOwners(projectOwners);
		}
	}

	private boolean visit(ProjectData projectData, Set<TaskWrapper> allTasks, boolean calculateState) {

		List<TaskWrapper> completed = new ArrayList<>();
		Set<TaskWrapper> remaining = new HashSet<>(allTasks);
		int projectDuration = 0;
		int projectRealDuration = 0;
		boolean reformulate = false;

		Date now = Calendar.getInstance().getTime();

		while (!remaining.isEmpty()) {
			boolean progress = false;

			for (Iterator<TaskWrapper> it = remaining.iterator(); it.hasNext();) {
				TaskWrapper task = it.next();
				if (completed.containsAll(task.getAncestors())) {

					if ((task.getTask() != null) && !task.isParent()) {

						calculatePlanning(projectData, task, now);

						if ((task.getMaxDuration() != null) && (projectDuration < task.getMaxDuration())) {
							projectDuration = task.getMaxDuration();
						}
						if ((task.getMaxRealDuration() != null) && (projectRealDuration < task.getMaxRealDuration())) {
							projectRealDuration = task.getMaxRealDuration();
							if (logger.isDebugEnabled()) {
								logger.debug(task.getTask().getTaskName() + " - maxRealDuration: " + task.getMaxRealDuration());
								for (TaskWrapper tmp : task.getAncestors()) {
									logger.debug("###-" + tmp.getTask().getTaskName());
								}
							}
						}

						if (calculateState) {
							reformulate = calculateState(projectData, task, now) || reformulate;
						}

						//calculateCapacity(task.getTask());
					}

					completed.add(task);
					it.remove();
					progress = true;
				}

				if (projectData.isDirtyTaskTree()) {
					return true;
				}
			}

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

	private boolean calculateState(ProjectData projectData, TaskWrapper task, Date now) {

		boolean reformulate = false;

		task.getTask().setIsExcludeFromSearch(false);

		TaskState currentTaskState = task.getTask().getTaskState();

		if (!TaskState.InProgress.equals(task.getTask().getTaskState()) && projectWorkflowService.isWorkflowActive(task.getTask())) {
			projectWorkflowService.cancelWorkflow(task.getTask());
		}

		List<DeliverableListDataItem> deliverables = ProjectHelper.getDeliverables(projectData, task.getTask().getNodeRef());

		if (task.getTask().isPlanned()) {

			if (task.isRoot()) {
				if ((task.getTask().getStart() != null) && task.getTask().getStart().before(now)) {
					logger.debug("Start first task.");
					task.getTask().setTaskState(TaskState.InProgress);
				}
			} else if (previousDone(task, new HashSet<>())
					&& ((task.getTask().getManualDate() == null) || ((task.getTask().getStart() != null) && task.getTask().getStart().before(now)))) {
				task.getTask().setTaskState(TaskState.InProgress);
			}

		} else if (task.getTask().isRefused() && (task.getTask().getRefusedTask() != null)) {
			boolean shouldRefused = true;
			logger.debug("Enter refused task");

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
				reformulate = true;
			}
		}

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

	private boolean previousDone(TaskWrapper task, Set<TaskWrapper> visited) {
		for (TaskWrapper t : task.getAncestors()) {
			if (visited.contains(t)) {
				logger.warn("Cycle detected in previousDone for task: " + task.getTask().getTaskName());
				return false;
			}
			visited.add(t);

			boolean taskDone = TaskState.Completed.equals(t.getTask().getTaskState()) || TaskState.Cancelled.equals(t.getTask().getTaskState());

			if (!taskDone || (TaskState.Cancelled.equals(t.getTask().getTaskState()) && !previousDone(t, visited))) {
				return false;
			}
		}
		return true;
	}

	private void assign(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> deliverables) {
		if ((taskListDataItem.getResources() != null) && !taskListDataItem.getResources().isEmpty()) {

			List<NodeRef> reassignedResources = extractReassignedPeople(projectData, taskListDataItem, taskListDataItem.getResources(), true);
			taskListDataItem.setResources(reassignedResources);

			List<NodeRef> reassignedNotficationAuthorities = extractReassignedPeople(projectData, taskListDataItem,
					taskListDataItem.getNotificationAuthorities(), false);
			taskListDataItem.setNotificationAuthorities(reassignedNotficationAuthorities);

			taskListDataItem.setObservers(projectService.extractResources(projectData.getNodeRef(), taskListDataItem.getObservers()));
		}

		projectWorkflowService.checkWorkflowInstance(projectData, taskListDataItem, deliverables);

		if (((taskListDataItem.getResources() != null) && !taskListDataItem.getResources().isEmpty())
				&& (((taskListDataItem.getWorkflowInstance() == null) || taskListDataItem.getWorkflowInstance().isEmpty())
						&& (taskListDataItem.getWorkflowName() != null) && !taskListDataItem.getWorkflowName().isEmpty())) {
			projectWorkflowService.startWorkflow(projectData, taskListDataItem, deliverables);
		}
	}

	private List<NodeRef> extractReassignedPeople(ProjectData projectData, TaskListDataItem taskListDataItem, List<NodeRef> originalResources,
			boolean updatePermission) {

		List<NodeRef> resources = new ArrayList<>();

		for (NodeRef resource : projectService.extractResources(projectData.getNodeRef(), originalResources)) {
			NodeRef reassignResource = projectService.getReassignedResource(resource, new HashSet<>());

			NodeRef toAdd = resource;

			if (reassignResource != null) {

				Date delegationStart = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_START);
				Date delegationEnd = (Date) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_DELEGATION_END);

				Date taskStart = taskListDataItem.getStart();

				if ((taskStart != null) && (delegationStart != null) && (taskStart.after(delegationStart) || taskStart.equals(delegationStart))) {

					if ((delegationEnd == null) || (taskStart.before(delegationEnd) || taskStart.equals(delegationEnd))) {
						toAdd = reassignResource;
					}
				} else if ((boolean) nodeService.getProperty(resource, ProjectModel.PROP_QNAME_REASSIGN_TASK)) {
					toAdd = reassignResource;
				}
			}

			if (updatePermission) {
				if (logger.isDebugEnabled()) {
					logger.debug("Update project permissions after reassignment");
				}
				projectService.updateProjectPermission(projectData.getNodeRef(), taskListDataItem.getNodeRef(), toAdd, true);
			}

			resources.add(toAdd);
		}

		return resources;
	}

	private void calculatePlanning(ProjectData projectData, TaskWrapper task, Date now) {
		int maxDuration = 0;
		int maxRealDuration = 0;

		Date startDate = null;
		Date targetStart = null;

		if (logger.isDebugEnabled()) {
			logger.debug("Calculate planning of: " + task.getTask().getTaskName());
		}

		NodeRef calendarRef = calendarService.getCalendar(task.getTask().getNodeRef());
		WorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

		if (task.isRoot()) {

			startDate = projectData.getStartDate();
			targetStart = projectData.getTargetStartDate();

		} else {

			TaskWrapper criticalTask = null;
			TaskWrapper startDateSource = null;
			TaskWrapper targetStartSource = null;
			for (TaskWrapper t : task.getAncestors()) {

				if ((t.getMaxDuration() != null) && (t.getMaxDuration() >= maxDuration)) {
					maxDuration = t.getMaxDuration();
					criticalTask = t; 
				}

				if ((t.getMaxRealDuration() != null) && (t.getMaxRealDuration() > maxRealDuration)) {
					maxRealDuration = t.getMaxRealDuration();
				}

				Date endDate = t.getTask().getEnd() != null ? t.getTask().getEnd() : t.getTask().getStart();
				Date targetEnd = t.getTask().getTargetEnd() != null ? t.getTask().getTargetEnd() : t.getTask().getTargetStart();

				if (TaskState.Cancelled.equals(t.getTask().getTaskState()) && !TaskState.InProgress.equals(t.getTask().getPreviousTaskState())) {
					WorkingDayProvider previousProvider = getTaskWorkingDayProvider(t);
					endDate = ProjectHelper.calculatePrevEndDate(t.getTask().getStart(), previousProvider);
					targetEnd = ProjectHelper.calculatePrevEndDate(t.getTask().getTargetStart(), previousProvider);
				}

				if ((endDate != null) && ((startDate == null) || startDate.before(endDate))) {
					startDate = endDate;
					startDateSource = t;
				}

				if ((targetEnd != null) && ((targetStart == null) || targetStart.before(targetEnd))) {
					targetStart = targetEnd;
					targetStartSource = t;
				}
			}

			if (criticalTask != null) {
				criticalTask.getTask().setIsCritical(true);
			}
			if (startDate != null) {
			    WorkingDayProvider startProvider = startDateSource != null
			            ? getTaskWorkingDayProvider(startDateSource)
			            : provider;
			    startDate = ProjectHelper.calculateNextStartDate(startDate, startProvider);
			}

			if (targetStart != null) {
			    WorkingDayProvider targetProvider = targetStartSource != null
			            ? getTaskWorkingDayProvider(targetStartSource)
			            : provider;
			    targetStart = ProjectHelper.calculateNextStartDate(targetStart, targetProvider);
			}
		}

		if (task.getTask().getSubProject() == null) {
			boolean hasSchedule = (task.getTask().getDuration() != null) || Boolean.TRUE.equals(task.getTask().getIsMilestone());

			if (startDate != null) {
				ProjectHelper.setTaskStartDate(task.getTask(), startDate);

				if (hasSchedule) {
					Date dueDate = ProjectHelper.calculateEndDate(task.getTask().getStart(), task.getTask().getDuration(), provider);
					Date endDate = dueDate;

					if (TaskState.OnHold.equals(task.getTask().getTaskState())
							|| (TaskState.InProgress.equals(task.getTask().getTaskState()) && (endDate != null) && endDate.before(now))
							|| TaskState.InProgress.equals(task.getTask().getPreviousTaskState())) {
						endDate = provider.isWorkingDay(now) ? now : provider.getNextWorkingDay(now);
					}

					task.getTask().setDue(dueDate != null ? ProjectHelper.removeTime(dueDate) : null);
					ProjectHelper.setTaskEndDate(task.getTask(), endDate);
				}
			}

			if (targetStart != null) {
				task.getTask().setTargetStart(ProjectHelper.removeTime(targetStart));

				if (hasSchedule) {
					Date targetEnd = ProjectHelper.calculateEndDate(task.getTask().getTargetStart(), task.getTask().getDuration(), provider);
					task.getTask().setTargetEnd(targetEnd != null ? ProjectHelper.removeTime(targetEnd) : null);
				}
			}
		}

		if (!TaskState.Cancelled.equals(task.getTask().getTaskState()) && !TaskState.InProgress.equals(task.getTask().getPreviousTaskState())) {
			if (task.getDuration() != null) {
				task.setMaxDuration(maxDuration + task.getDuration());
			}
		} else {
			task.setMaxDuration(maxDuration);
		}

		Integer realDuration = task.computeRealDuration(provider);
		task.getTask().setRealDuration(realDuration);

		if (TaskState.Completed.equals(task.getTask().getTaskState())) {
			task.setMaxRealDuration(ProjectHelper.calculateTaskDuration(projectData.getStartDate(), task.getTask().getEnd(), provider));
		} else if (realDuration != null) {
			task.setMaxRealDuration(maxRealDuration + realDuration);
		}
	}

	private WorkingDayProvider getTaskWorkingDayProvider(TaskWrapper task) {
		NodeRef calendarRef = calendarService.getCalendar(task.getTask().getNodeRef());
		return new CalendarWorkingDayProvider(calendarService, calendarRef);
	}

	private boolean visitDeliverables(ProjectData projectData, TaskListDataItem taskListDataItem, List<DeliverableListDataItem> deliverables) {

		TaskState previousState = taskListDataItem.getTaskState();

		if (TaskState.Completed.equals(taskListDataItem.getTaskState()) || TaskState.InProgress.equals(taskListDataItem.getTaskState())) {

			if (taskListDataItem.getSubProject() == null) {
				Integer taskCompletionPercent = TaskState.InProgress.equals(taskListDataItem.getTaskState()) ? 0 : COMPLETED;

				for (DeliverableListDataItem deliverable : deliverables) {

					if (TaskState.InProgress.equals(taskListDataItem.getTaskState())) {

						if ((deliverable.getCompletionPercent() != null) && (DeliverableState.Completed.equals(deliverable.getState())
								|| DeliverableState.Closed.equals(deliverable.getState()))) {
							taskCompletionPercent += deliverable.getCompletionPercent();
						}

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

				if (!previousState.equals(taskListDataItem.getTaskState()) && TaskState.InProgress.equals(taskListDataItem.getTaskState())) {
					logger.debug("Task " + taskListDataItem.getTaskName() + " reopen by script " + taskListDataItem.getTaskState());
					return true;
				}
			}

		} else if (TaskState.Planned.equals(taskListDataItem.getTaskState())) {
			taskListDataItem.setCompletionPercent(0);
		}

		return false;
	}
}