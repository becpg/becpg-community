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
 * Deliverable list of project
 * 
 * @author quere
 * 
 */
public class DeliverableListDataItem extends BaseObject implements DataItem {

	private NodeRef nodeRef;
	private NodeRef task;
	private TaskState taskState;
	private String description;

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public NodeRef getTask() {
		return task;
	}

	public void setTask(NodeRef task) {
		this.task = task;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DeliverableListDataItem(NodeRef nodeRef, NodeRef task, TaskState taskState, String description) {
		super();
		this.nodeRef = nodeRef;
		this.task = task;
		this.taskState = taskState;
		this.description = description;
	}

	public DeliverableListDataItem(DeliverableListDataItem d) {
		super();
		this.nodeRef = d.getNodeRef();
		this.task = d.getTask();
		this.taskState = d.getTaskState();
		this.description = d.getDescription();
	}

	@Override
	public Map<QName, Serializable> getProperties() {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ProjectModel.PROP_DL_STATE, taskState);
		properties.put(ProjectModel.PROP_DL_DESCRIPTION, description);
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {

		Map<QName, NodeRef> associations = new HashMap<QName, NodeRef>();
		associations.put(ProjectModel.ASSOC_DL_TASK, task);
		return associations;
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations() {

		Map<QName, List<NodeRef>> associations = new HashMap<QName, List<NodeRef>>();
		return associations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((taskState == null) ? 0 : taskState.hashCode());
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
		DeliverableListDataItem other = (DeliverableListDataItem) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		if (taskState != other.taskState)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeliverableListDataItem [nodeRef=" + nodeRef + ", task=" + task + ", taskState=" + taskState
				+ ", description=" + description + "]";
	}
}
