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
	private DeliverableState state = DeliverableState.NotYetStarted;
	private String description;
	private Integer completionPercent = 0;
	private NodeRef content;

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

	public DeliverableState getState() {
		return state;
	}

	public void setState(DeliverableState state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCompletionPercent() {
		return completionPercent;
	}

	public void setCompletionPercent(Integer completionPercent) {
		this.completionPercent = completionPercent;
	}

	public NodeRef getContent() {
		return content;
	}

	public void setContent(NodeRef content) {
		this.content = content;
	}

	public DeliverableListDataItem(NodeRef nodeRef, NodeRef task, DeliverableState state, String description, Integer completionPercent, NodeRef content) {
		super();
		this.nodeRef = nodeRef;
		this.task = task;
		this.state = state;
		this.description = description;
		this.completionPercent = completionPercent;
		this.content = content;
	}

	public DeliverableListDataItem(DeliverableListDataItem d) {
		super();
		this.nodeRef = d.getNodeRef();
		this.task = d.getTask();
		this.state = d.getState();
		this.description = d.getDescription();
		this.completionPercent = d.getCompletionPercent();
		this.content = d.content;
	}

	@Override
	public Map<QName, Serializable> getProperties() {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ProjectModel.PROP_DL_STATE, state);
		properties.put(ProjectModel.PROP_DL_DESCRIPTION, description);
		properties.put(ProjectModel.PROP_COMPLETION_PERCENT, completionPercent);
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {

		Map<QName, NodeRef> associations = new HashMap<QName, NodeRef>();
		associations.put(ProjectModel.ASSOC_DL_TASK, task);
		associations.put(ProjectModel.ASSOC_DL_CONTENT, content);
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
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
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
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeliverableListDataItem [nodeRef=" + nodeRef + ", task=" + task + ", state=" + state
				+ ", description=" + description + ", content=" + content + "]";
	}
}
