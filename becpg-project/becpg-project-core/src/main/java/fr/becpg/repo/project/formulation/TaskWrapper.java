package fr.becpg.repo.project.formulation;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

public class TaskWrapper implements Comparable<TaskWrapper> {

	private static final int DURATION_DEFAULT = 1;

	private TaskListDataItem task;

	// the cost of the task along the critical path
	private Integer maxDuration = 0;
	private Integer maxRealDuration = 0;

	private List<TaskWrapper> descendants = new LinkedList<>();
	private List<TaskWrapper> ancestors = new LinkedList<>();
	private List<TaskWrapper> childs = new LinkedList<>();

	public TaskListDataItem getTask() {
		return task;
	}

	public void setTask(TaskListDataItem task) {
		this.task = task;

	}

	public List<TaskWrapper> getDescendants() {
		return descendants;
	}

	public void setDescendants(List<TaskWrapper> descendants) {
		this.descendants = descendants;
	}

	public List<TaskWrapper> getAncestors() {
		return ancestors;
	}

	public void setAncestors(List<TaskWrapper> ancestors) {
		this.ancestors = ancestors;
	}

	public List<TaskWrapper> getChilds() {
		return childs;
	}

	public void setChilds(List<TaskWrapper> childs) {
		this.childs = childs;
	}

	public boolean isRoot() {
		return ((ancestors == null) || ancestors.isEmpty()) && !isGroup();
	}

	public boolean isLeaf() {
		return ((descendants == null) || descendants.isEmpty()) && !isGroup();
	}

	public boolean isSubProject() {
		return (task != null) && (task.getSubProject() != null);
	}

	public boolean isGroup() {
		return (childs != null) && !childs.isEmpty();
	}

	public Integer getDuration() {
		return ((task != null) && (task.getDuration() != null)) ? task.getDuration()
				: ((task != null) && Boolean.TRUE.equals(task.getIsMilestone())) ? DURATION_DEFAULT : null;
	}

	public Integer getRealDuration() {

		if (task != null) {

			if (TaskState.InProgress.equals(task.getTaskState()) || TaskState.Refused.equals(task.getTaskState())) {
				Date endDate = ProjectHelper.removeTime(new Date());

				// we wait the overdue of the task to take it in account
				if ((task.getEnd() != null) && endDate.before(task.getEnd())) {
					return getDuration();
				}
				return ProjectHelper.calculateTaskDuration(task.getStart(), endDate);

			} else if (TaskState.Completed.equals(task.getTaskState())) {
				return ProjectHelper.calculateTaskDuration(task.getStart(), task.getEnd());
			} else if(TaskState.Cancelled.equals(task.getTaskState())) {
				return 0;
			}

		}
		return getDuration();

	}

	public Integer getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(Integer maxDuration) {
		this.maxDuration = maxDuration;
	}

	public Integer getMaxRealDuration() {
		return maxRealDuration;
	}

	public void setMaxRealDuration(Integer maxRealDuration) {
		this.maxRealDuration = maxRealDuration;
	}

	public Long getStartDateTime() {
		return (task != null) && (task.getStart() != null) ? task.getStart().getTime() : null;
	}

	public boolean dependsOf(TaskWrapper t) {
		// is t a direct dependency?
		if (ancestors.contains(t)) {
			return true;
		}
		// is t an indirect dependency
		for (TaskWrapper dep : ancestors) {
			if (dep.dependsOf(t)) {
				return true;
			}
		}
		return false;
	}

	public boolean childOf(TaskWrapper t) {
		// is t a direct dependency?
		if (t.getChilds().contains(this)) {
			return true;
		}

		return false;
	}

	public static Set<TaskWrapper> extract(ProjectData projectData) {

		Map<NodeRef, TaskWrapper> cache = new HashMap<>();

		projectData.getTaskList().forEach(task -> {
			TaskWrapper wrapper = getOrCreateTaskWrapper(task.getNodeRef(), cache);
			wrapper.setTask(task);
			task.setIsGroup(false);

			task.getPrevTasks().forEach(prevTaskNodeRef -> {
				TaskWrapper prevGanttData = getOrCreateTaskWrapper(prevTaskNodeRef, cache);
				prevGanttData.getDescendants().add(prevGanttData);
				wrapper.getAncestors().add(prevGanttData);
			});

			if (task.getParent() != null) {
				TaskWrapper parentGanttData = getOrCreateTaskWrapper(task.getParent().getNodeRef(), cache);
				parentGanttData.getChilds().add(wrapper);

			}
		});

		cache.values().forEach(t -> {
			if (t.isGroup() || t.isSubProject()) {
				t.getTask().setIsGroup(true);
			}
		});

		return cache.values().stream().collect(Collectors.toSet());
	}

	private static TaskWrapper getOrCreateTaskWrapper(NodeRef nodeRef, Map<NodeRef, TaskWrapper> cache) {
		TaskWrapper ret = cache.get(nodeRef);
		if (ret == null) {
			ret = new TaskWrapper();
			cache.put(nodeRef, ret);
		}
		return ret;
	}

	public static Integer calculateMaxDuration(Set<TaskWrapper> tasks) {
		// tasks whose critical cost has been calculated
		HashSet<TaskWrapper> completed = new HashSet<>();
		// tasks whose critical cost needs to be calculated
		HashSet<TaskWrapper> remaining = new HashSet<>(tasks);

		// Backflow algorithm
		// while there are tasks whose critical cost isn't calculated.
		while (!remaining.isEmpty()) {
			boolean progress = false;

			// find a new task to calculate
			for (Iterator<TaskWrapper> it = remaining.iterator(); it.hasNext();) {
				TaskWrapper task = it.next();
				if (completed.containsAll(task.getAncestors())) {
					// all dependencies calculated, critical cost is max
					// dependency
					// critical cost, plus our cost
					int critical = 0;
					for (TaskWrapper t : task.getAncestors()) {
						if (t.getMaxDuration() > critical) {
							critical = t.getMaxDuration();
						}
					}
					if (task.getDuration() != null) {
						task.setMaxDuration(critical + task.getDuration());
					}
					// set task as calculated an remove
					completed.add(task);
					it.remove();
					// note we are making progress
					progress = true;
				}
			}
			// If we haven't made any progress then a cycle must exist in
			// the graph and we wont be able to calculate the critical path
			if (!progress) {
				throw new RuntimeException("Cyclic dependency, algorithm stopped!");
			}
		}

		return completed.stream().map(t -> t.getMaxDuration()).max(Integer::compareTo).orElse(DURATION_DEFAULT);
	}

	public static String print(ProjectData projectData) {

		StringBuilder ret = new StringBuilder();
		projectData.getTaskList().stream().forEach(t -> {
			if ((t != null) && (t.getDuration() != null)) {
				ret.append("\n"
						+ " ".repeat(Math.abs(Math.toIntExact(((t.getStart() != null) && (projectData.getStartDate() != null))
								? ChronoUnit.DAYS.between(t.getStart().toInstant(), projectData.getStartDate().toInstant())
								: 0)))
						+ (t.getIsGroup() ? "#" : "_").repeat(Math.abs(t.getDuration())) + " " + t.getTaskName() + "[ " + t.getStart() + " / "
						+ t.getEnd() + "] " + " (" + t.getTaskState() + "/" + t.getDuration() + "/" + t.getRealDuration() + ")");
			}
		});

		return ret.toString();
	}

	@Override
	// https://stackoverflow.com/questions/2985317/critical-path-method-algorithm
	public int compareTo(TaskWrapper o2) {
		// sort by cost

		if (o2.childOf(this)) {
			return -1;
		} else if (childOf(o2)) {
			return 1;
		}

		// using dependency as a tie breaker
		// note if a is dependent on b then
		// critical cost a must be >= critical cost of b
		if (o2.dependsOf(this)) {
			return 1;
		} else if (dependsOf(o2)) {
			return -1;
		}

		return this.getMaxDuration() - o2.getMaxDuration();

	}

}
