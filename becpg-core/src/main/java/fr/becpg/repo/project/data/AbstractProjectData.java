package fr.becpg.repo.project.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.data.DataItem;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Abstract project data used to manipulate project and project template
 * 
 * @author quere
 * 
 */
public abstract class AbstractProjectData extends BeCPGDataObject implements DataItem {

	private NodeRef nodeRef;
	private String name;
	private List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();
	private List<DeliverableListDataItem> deliverableList = new LinkedList<DeliverableListDataItem>();	

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<TaskListDataItem> getTaskList() {
		return taskList;
	}

	public void setTaskList(List<TaskListDataItem> taskList) {
		this.taskList = taskList;
	}

	public List<DeliverableListDataItem> getDeliverableList() {
		return deliverableList;
	}

	public void setDeliverableList(List<DeliverableListDataItem> deliverableList) {
		this.deliverableList = deliverableList;
	}
	
	public AbstractProjectData(NodeRef nodeRef, String name) {
		super();
		this.nodeRef = nodeRef;
		this.name = name;
	}
	
	@Override
	public Map<QName, Serializable> getProperties() {
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
		properties.put(ContentModel.PROP_NAME, name);
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {
		return new HashMap<QName, NodeRef>();
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations() {
		return new HashMap<QName, List<NodeRef>>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deliverableList == null) ? 0 : deliverableList.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((taskList == null) ? 0 : taskList.hashCode());
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
		AbstractProjectData other = (AbstractProjectData) obj;
		if (deliverableList == null) {
			if (other.deliverableList != null)
				return false;
		} else if (!deliverableList.equals(other.deliverableList))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;		
		if (taskList == null) {
			if (other.taskList != null)
				return false;
		} else if (!taskList.equals(other.taskList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractProjectData [nodeRef=" + nodeRef + ", name=" + name + ", taskList=" + taskList
				+ ", deliverableList=" + deliverableList + "]";
	}
}
