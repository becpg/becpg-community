package fr.becpg.repo.project.data.projectList;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.data.DataItem;
import fr.becpg.repo.product.data.BaseObject;

/**
 * Task list of project (planning)
 * 
 * @author quere
 * 
 */
public class TaskListDataItem extends BaseObject implements DataItem {

	private NodeRef nodeRef;
	private Boolean isMilestone;
	private Integer duration;
	private String workflowName;
	private NodeRef taskSet;
	private NodeRef task;
	private List<NodeRef> prevTasks;
	private List<NodeRef> assignees;

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public Boolean getIsMilestone() {
		return isMilestone;
	}

	public void setIsMilestone(Boolean isMilestone) {
		this.isMilestone = isMilestone;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
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

	public List<NodeRef> getPrevTasks() {
		return prevTasks;
	}

	public void setPrevTasks(List<NodeRef> prevTasks) {
		this.prevTasks = prevTasks;
	}

	public List<NodeRef> getAssignees() {
		return assignees;
	}

	public void setAssignees(List<NodeRef> assignees) {
		this.assignees = assignees;
	}

	public TaskListDataItem(NodeRef nodeRef, Boolean isMilestone, Integer duration, String workflowName,
			NodeRef taskSet, NodeRef task, List<NodeRef> prevTasks, List<NodeRef> assignees) {
		super();
		this.nodeRef = nodeRef;
		this.isMilestone = isMilestone;
		this.duration = duration;
		this.workflowName = workflowName;
		this.taskSet = taskSet;
		this.task = task;
		this.prevTasks = prevTasks;
		this.assignees = assignees;
	}

	@Override
	public String toString() {
		return "TaskListDataItem [nodeRef=" + nodeRef + ", isMilestone=" + isMilestone + ", duration=" + duration
				+ ", workflowName=" + workflowName + ", taskSet=" + taskSet + ", task=" + task + ", prevTasks="
				+ prevTasks + ", assignees=" + assignees + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assignees == null) ? 0 : assignees.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((isMilestone == null) ? 0 : isMilestone.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((prevTasks == null) ? 0 : prevTasks.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((taskSet == null) ? 0 : taskSet.hashCode());
		result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
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
		TaskListDataItem other = (TaskListDataItem) obj;
		if (assignees == null) {
			if (other.assignees != null)
				return false;
		} else if (!assignees.equals(other.assignees))
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (isMilestone == null) {
			if (other.isMilestone != null)
				return false;
		} else if (!isMilestone.equals(other.isMilestone))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (prevTasks == null) {
			if (other.prevTasks != null)
				return false;
		} else if (!prevTasks.equals(other.prevTasks))
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
		if (workflowName == null) {
			if (other.workflowName != null)
				return false;
		} else if (!workflowName.equals(other.workflowName))
			return false;
		return true;
	}

	@Override
	public Map<QName, Serializable> getProperties() {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ProjectModel.PROP_TL_IS_MILESTONE, isMilestone);
		properties.put(ProjectModel.PROP_TL_DURATION, duration);
		properties.put(ProjectModel.PROP_TL_WORKFLOW_NAME, workflowName);
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {

		Map<QName, NodeRef> associations = new HashMap<QName, NodeRef>();
		associations.put(ProjectModel.ASSOC_TL_TASKSET, taskSet);
		associations.put(ProjectModel.ASSOC_TL_TASK, task);
		return associations;
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations() {

		Map<QName, List<NodeRef>> associations = new HashMap<QName, List<NodeRef>>();
		associations.put(ProjectModel.ASSOC_TL_PREV_TASKS, prevTasks);
		associations.put(ProjectModel.ASSOC_TL_ASSIGNEES, assignees);
		return associations;
	}
}
