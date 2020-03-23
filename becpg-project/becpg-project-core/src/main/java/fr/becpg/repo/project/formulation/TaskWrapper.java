package fr.becpg.repo.project.formulation;

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
import fr.becpg.repo.project.impl.ProjectHelper;

//TODO Merge with TaskListItem when is over
public class TaskWrapper {

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
		return (descendants == null) || descendants.isEmpty();
	}

	public boolean isSubProject() {
		return (task != null) && (task.getSubProject() != null);
	}

	public boolean isGroup() {
		return (childs != null) && !childs.isEmpty();
	}

	public Integer getDuration() {
		return ((task != null) && (task.getDuration() != null)) ? task.getDuration()
				: (Boolean.TRUE.equals(task.getIsMilestone())) ? DURATION_DEFAULT : null;
	}

	public Integer getRealDuration() {

		if (task != null) {
			Integer tempDuration = ProjectHelper.calculateRealDuration(task);
			if (tempDuration != null) {
				return tempDuration;
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

		return completed.stream().map(t -> t.getMaxDuration()).max(Integer::compareTo).get();
	}

}
