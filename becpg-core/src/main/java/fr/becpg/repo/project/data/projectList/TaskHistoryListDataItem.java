package fr.becpg.repo.project.data.projectList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.data.DataItem;
import fr.becpg.repo.product.data.BaseObject;

/**
 * History task list (done or to do)
 * 
 * @author quere
 * 
 */
public class TaskHistoryListDataItem extends BaseObject implements DataItem {

	private NodeRef nodeRef;
	private Date start = new Date();
	private Date end;
	private Integer duration;
	private String comment;
	private TaskState state = TaskState.InProgress;
	private NodeRef taskSet;
	private NodeRef task;
	private List<NodeRef> assignees = new ArrayList<NodeRef>();

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public TaskState getState() {
		return state;
	}

	public void setState(TaskState state) {
		this.state = state;
	}

	public NodeRef getTaskSet() {
		return taskSet;
	}

	public void setTaskSet(NodeRef taskSet) {
		this.taskSet = taskSet;
	}

	public NodeRef getTask() {
		return task;
	}

	public void setTask(NodeRef task) {
		this.task = task;
	}

	public List<NodeRef> getAssignees() {
		return assignees;
	}

	public void setAssignees(List<NodeRef> assignees) {
		this.assignees = assignees;
	}

	public TaskHistoryListDataItem(NodeRef nodeRef, Date start, Date end, Integer duration, String comment,
			TaskState state, NodeRef taskSet, NodeRef task, List<NodeRef> assignees) {
		super();
		this.nodeRef = nodeRef;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.comment = comment;
		this.state = state;
		this.taskSet = taskSet;
		this.task = task;
		this.assignees = assignees;
	}

	public TaskHistoryListDataItem(TaskListDataItem taskListDataItem) {
		this.taskSet = taskListDataItem.getTaskSet();
		this.task = taskListDataItem.getTask();
	}

	@Override
	public String toString() {
		return "TaskHistoryListDataItem [nodeRef=" + nodeRef + ", start=" + start + ", end=" + end + ", duration="
				+ duration + ", comment=" + comment + ", state=" + state + ", taskSet=" + taskSet + ", task=" + task
				+ ", assignees=" + assignees + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assignees == null) ? 0 : assignees.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((taskSet == null) ? 0 : taskSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskHistoryListDataItem other = (TaskHistoryListDataItem) obj;
		if (assignees == null) {
			if (other.assignees != null)
				return false;
		} else if (!assignees.equals(other.assignees))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (state != other.state)
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		if (taskSet == null) {
			if (other.taskSet != null)
				return false;
		} else if (!taskSet.equals(other.taskSet))
			return false;
		return true;
	}

	@Override
	public Map<QName, Serializable> getProperties() {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ProjectModel.PROP_THL_START, start);
		properties.put(ProjectModel.PROP_THL_END, end);
		properties.put(ProjectModel.PROP_THL_DURATION, duration);
		properties.put(ProjectModel.PROP_THL_COMMENT, comment);
		properties.put(ProjectModel.PROP_THL_STATE, state);
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {

		Map<QName, NodeRef> associations = new HashMap<QName, NodeRef>();
		associations.put(ProjectModel.ASSOC_THL_TASKSET, taskSet);
		associations.put(ProjectModel.ASSOC_THL_TASK, task);
		return associations;
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations() {

		Map<QName, List<NodeRef>> associations = new HashMap<QName, List<NodeRef>>();
		return associations;
	}
}
