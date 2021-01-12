package fr.becpg.repo.project.formulation;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

/**
 * <p>TaskWrapper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TaskWrapper implements Comparable<TaskWrapper> {

	private static final int DURATION_DEFAULT = 1;
	
	private static Log logger = LogFactory.getLog(TaskWrapper.class);

	private TaskListDataItem task;

	// the cost of the task along the critical path
	private Integer maxDuration = 0;
	private Integer maxRealDuration = 0;

	private List<TaskWrapper> descendants = new LinkedList<>();
	private List<TaskWrapper> ancestors = new LinkedList<>();
	private List<TaskWrapper> childs = new LinkedList<>();
	
	private TaskWrapper parent = null;
	
	

	public TaskWrapper getParent() {
		return parent;
	}

	public void setParent(TaskWrapper parent) {
		this.parent = parent;
	}

	/**
	 * <p>Getter for the field <code>task</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	public TaskListDataItem getTask() {
		return task;
	}

	/**
	 * <p>Setter for the field <code>task</code>.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	public void setTask(TaskListDataItem task) {
		this.task = task;

	}

	/**
	 * <p>Getter for the field <code>descendants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<TaskWrapper> getDescendants() {
		return descendants;
	}

	/**
	 * <p>Setter for the field <code>descendants</code>.</p>
	 *
	 * @param descendants a {@link java.util.List} object.
	 */
	public void setDescendants(List<TaskWrapper> descendants) {
		this.descendants = descendants;
	}

	/**
	 * <p>Getter for the field <code>ancestors</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<TaskWrapper> getAncestors() {
		return ancestors;
	}

	/**
	 * <p>Setter for the field <code>ancestors</code>.</p>
	 *
	 * @param ancestors a {@link java.util.List} object.
	 */
	public void setAncestors(List<TaskWrapper> ancestors) {
		this.ancestors = ancestors;
	}

	/**
	 * <p>Getter for the field <code>childs</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<TaskWrapper> getChilds() {
		return childs;
	}

	/**
	 * <p>Setter for the field <code>childs</code>.</p>
	 *
	 * @param childs a {@link java.util.List} object.
	 */
	public void setChilds(List<TaskWrapper> childs) {
		this.childs = childs;
	}

	/**
	 * <p>isRoot.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRoot() {
		return ((ancestors == null) || ancestors.isEmpty()) && !isGroup();
	}

	/**
	 * <p>isLeaf.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLeaf() {
		return ((descendants == null) || descendants.isEmpty()) && !isGroup();
	}

	/**
	 * <p>isSubProject.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSubProject() {
		return (task != null) && (task.getSubProject() != null);
	}

	public boolean isCancelled() {
    	return TaskState.Cancelled.equals(task.getTaskState());
	}
	
	
	/**
	 * <p>isGroup.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isGroup() {
		return (childs != null) && !childs.isEmpty();
	}

	/**
	 * <p>getDuration.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getDuration() {
		return ((task != null) && (task.getDuration() != null)) ? task.getDuration()
				: ((task != null) && Boolean.TRUE.equals(task.getIsMilestone())) ? DURATION_DEFAULT : null;
	}

	/**
	 * <p>getRealDuration.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getRealDuration() {

		if (task != null) {

			if (TaskState.Planned.equals(task.getTaskState()) || TaskState.InProgress.equals(task.getTaskState())
					|| TaskState.Refused.equals(task.getTaskState())) {
				Date endDate = ProjectHelper.removeTime(new Date());

				// we wait the overdue of the task to take it in account
				if ((task.getEnd() != null) && endDate.before(task.getEnd())) {
					return getDuration();
				}
				return ProjectHelper.calculateTaskDuration(task.getStart(), endDate);

			} else if (TaskState.Completed.equals(task.getTaskState())) {
				return ProjectHelper.calculateTaskDuration(task.getStart(), task.getEnd());
			} else if (TaskState.Cancelled.equals(task.getTaskState())) {
				return 0;
			}

		}
		return getDuration();

	}

	/**
	 * <p>Getter for the field <code>maxDuration</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getMaxDuration() {
		return maxDuration;
	}

	/**
	 * <p>Setter for the field <code>maxDuration</code>.</p>
	 *
	 * @param maxDuration a {@link java.lang.Integer} object.
	 */
	public void setMaxDuration(Integer maxDuration) {
		this.maxDuration = maxDuration;
	}

	/**
	 * <p>Getter for the field <code>maxRealDuration</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getMaxRealDuration() {
		return maxRealDuration;
	}

	/**
	 * <p>Setter for the field <code>maxRealDuration</code>.</p>
	 *
	 * @param maxRealDuration a {@link java.lang.Integer} object.
	 */
	public void setMaxRealDuration(Integer maxRealDuration) {
		this.maxRealDuration = maxRealDuration;
	}

	/**
	 * <p>getStartDateTime.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getStartDateTime() {
		return (task != null) && (task.getStart() != null) ? task.getStart().getTime() : null;
	}

	/**
	 * <p>dependsOf.</p>
	 *
	 * @param t a {@link fr.becpg.repo.project.formulation.TaskWrapper} object.
	 * @return a boolean.
	 */
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

	/**
	 * <p>childOf.</p>
	 *
	 * @param t a {@link fr.becpg.repo.project.formulation.TaskWrapper} object.
	 * @return a boolean.
	 */
	public boolean childOf(TaskWrapper t) {
		// is t a direct dependency?
		return t.getChilds().contains(this);
	}

	/**
	 * <p>extract.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @return a {@link java.util.Set} object.
	 */
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
				parentGanttData.setTask(task.getParent());
				parentGanttData.getChilds().add(wrapper);
				wrapper.setParent(parentGanttData);
			}
		});

		cache.entrySet().forEach(t -> {
			if(t.getValue().getTask()!=null) {

				if (t.getValue().isGroup() || t.getValue().isSubProject()) {
					t.getValue().getTask().setIsGroup(true);
				} else if(t.getValue().getAncestors().isEmpty() && t.getValue().getParent()!=null) {
					//Append parent ancestors if needed
					appendParentAncestors(t.getValue().getParent() , t.getValue());
				}
			
			} else {
				logger.warn("Null task for:"+ t.getKey());
			}
		});

		return cache.values().stream().collect(Collectors.toSet());
	}

	
	private static void appendParentAncestors(TaskWrapper parent, TaskWrapper t) {

		if(parent.getAncestors().isEmpty() && parent.getParent()!=null) {
			appendParentAncestors(parent.getParent() , t);
		} else {
			appendAncestors(parent.getAncestors(),t);
		}
		
	}
	
	private static void appendAncestors(List<TaskWrapper> ancestors, TaskWrapper t) {

		ancestors.forEach( parentAncestor -> {
			// ancestor is a parent -> Take last child
			if(!parentAncestor.getChilds().isEmpty()) {	
				if(t.childOf(parentAncestor)) {
					// ancestor is a parent and i'm child of -> Append ancestor of him
					if(parentAncestor.getParent()!=null ) {
						appendParentAncestors(parentAncestor.getParent(), t);
					}
				} else {
					appendAncestors(parentAncestor.getLastChilds(), t);
				}
			} else  {
				//ancestor is a task -> Append task
				parentAncestor.getDescendants().add(t);
				t.getAncestors().add(parentAncestor);
			}
			
		});
		
	}

	private List<TaskWrapper> getLastChilds() {
		List<TaskWrapper> ret = new ArrayList<>();
		getChilds().forEach( child -> {
			
			boolean add = true;
			for(TaskWrapper descendant: child.getDescendants()) {
				if(getChilds().contains(descendant)) {
					ret.addAll(descendant.getLastChilds());
					add = false;
				}	
			}
			
			if(add) {
			   ret.add(child);
			} 
			
		});
		
		return ret;
	}

	private static TaskWrapper getOrCreateTaskWrapper(NodeRef nodeRef, Map<NodeRef, TaskWrapper> cache) {
		return cache.computeIfAbsent(nodeRef, n -> new TaskWrapper());

	}

	/**
	 * <p>calculateMaxDuration.</p>
	 *
	 * @param tasks a {@link java.util.Set} object.
	 * @return a {@link java.lang.Integer} object.
	 */
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

					if (!task.isCancelled()) {
						if (task.getDuration() != null) {
							task.setMaxDuration(critical + task.getDuration());
						}

					} else {
						task.setMaxDuration(critical);
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
				throw new FormulateException("Cyclic dependency, algorithm stopped!");
			}
		}

		return completed.stream().map(TaskWrapper::getMaxDuration).max(Integer::compareTo).orElse(DURATION_DEFAULT);

	}

	/**
	 * <p>print.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String print(ProjectData projectData) {

		StringBuilder ret = new StringBuilder();
		projectData.getTaskList().stream().forEach(t -> {
			if ((t != null) && (t.getDuration() != null)) {
				ret.append("\n"
						+ " ".repeat(Math.abs(Math.toIntExact(((t.getStart() != null) && (projectData.getStartDate() != null))
								? ChronoUnit.DAYS.between(t.getStart().toInstant(), projectData.getStartDate().toInstant())
								: 0)))
						+ (Boolean.TRUE.equals(t.getIsGroup()) ? "#" : "_").repeat(Math.abs(t.getDuration())) + " " + t.getTaskName() + "[ "
						+ t.getStart() + " / " + t.getEnd() + "] " + " (" + t.getTaskState() + "/" + t.getDuration() + "/" + t.getRealDuration()
						+ ") ");
			}
		});

		return ret.toString();
	}

	@Override
	public String toString() {
		return "TaskWrapper [task=" + task + "]";
	}

	
	
	
	/** {@inheritDoc} */
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

	@Override
	public int hashCode() {
		return Objects.hash(task);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskWrapper other = (TaskWrapper) obj;
		return Objects.equals(task, other.task);
	}
	
	

}
