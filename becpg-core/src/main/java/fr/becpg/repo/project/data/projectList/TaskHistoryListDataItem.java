package fr.becpg.repo.project.data.projectList;

import java.io.Serializable;
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
	private String comment;
	private TaskState state = TaskState.NotYetStarted;
	private Integer completionPercent = 0;
	private String workflowInstance;
	private NodeRef taskLegend;
	private NodeRef task;

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

	public Integer getCompletionPercent() {
		return completionPercent;
	}

	public void setCompletionPercent(Integer completionPercent) {
		this.completionPercent = completionPercent;
	}

	public String getWorkflowInstance() {
		return workflowInstance;
	}

	public void setWorkflowInstance(String workflowInstance) {
		this.workflowInstance = workflowInstance;
	}

	public NodeRef getTaskLegend() {
		return taskLegend;
	}

	public void setTaskLegend(NodeRef taskLegend) {
		this.taskLegend = taskLegend;
	}

	public NodeRef getTask() {
		return task;
	}

	public void setTask(NodeRef task) {
		this.task = task;
	}
	
	public TaskHistoryListDataItem(NodeRef nodeRef, Date start, Date end, String comment,
			TaskState state, Integer completionPercent, String workflowInstance, NodeRef taskLegend, NodeRef task, List<NodeRef> assignees) {
		super();
		this.nodeRef = nodeRef;
		this.start = start;
		this.end = end;
		this.comment = comment;
		this.state = state;
		this.completionPercent = completionPercent;
		this.workflowInstance = workflowInstance;
		this.taskLegend = taskLegend;
		this.task = task;
	}

	public TaskHistoryListDataItem(TaskListDataItem taskListDataItem) {
		this.taskLegend = taskListDataItem.getTaskLegend();
		this.task = taskListDataItem.getTask();
	}

	@Override
	public String toString() {
		return "TaskHistoryListDataItem [nodeRef=" + nodeRef + ", start=" + start + ", end=" + end + ", comment="
				+ comment + ", state=" + state + ", completionPercent=" + completionPercent + ", workflowInstance="
				+ workflowInstance + ", taskLegend=" + taskLegend + ", task=" + task + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((taskLegend == null) ? 0 : taskLegend.hashCode());
		result = prime * result + ((workflowInstance == null) ? 0 : workflowInstance.hashCode());
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
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (completionPercent == null) {
			if (other.completionPercent != null)
				return false;
		} else if (!completionPercent.equals(other.completionPercent))
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
		if (taskLegend == null) {
			if (other.taskLegend != null)
				return false;
		} else if (!taskLegend.equals(other.taskLegend))
			return false;
		if (workflowInstance == null) {
			if (other.workflowInstance != null)
				return false;
		} else if (!workflowInstance.equals(other.workflowInstance))
			return false;
		return true;
	}

	@Override
	public Map<QName, Serializable> getProperties() {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ProjectModel.PROP_THL_START, start);
		properties.put(ProjectModel.PROP_THL_END, end);
		properties.put(ProjectModel.PROP_THL_COMMENT, comment);
		properties.put(ProjectModel.PROP_THL_STATE, state);
		properties.put(ProjectModel.PROP_COMPLETION_PERCENT, completionPercent);
		properties.put(ProjectModel.PROP_THL_WORKFLOW_INSTANCE, workflowInstance);
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {

		Map<QName, NodeRef> associations = new HashMap<QName, NodeRef>();
		associations.put(ProjectModel.ASSOC_THL_TASKLEGEND, taskLegend);
		associations.put(ProjectModel.ASSOC_THL_TASK, task);
		return associations;
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations() {

		Map<QName, List<NodeRef>> associations = new HashMap<QName, List<NodeRef>>();
		return associations;
	}
}
