package fr.becpg.repo.project.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskHistoryListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Abstract project data used to manipulate project and project template
 * 
 * @author quere
 * 
 */
public abstract class AbstractProjectData extends BeCPGDataObject {

	private NodeRef nodeRef;
	private String name;
	private NodeRef projectTpl;
	private List<TaskListDataItem> taskList = new ArrayList<TaskListDataItem>();
	private List<DeliverableListDataItem> deliverableList = new ArrayList<DeliverableListDataItem>();
	private List<TaskHistoryListDataItem> taskHistoryList; // null on project Tpl

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

	public NodeRef getProjectTpl() {
		return projectTpl;
	}

	public void setProjectTpl(NodeRef projectTpl) {
		this.projectTpl = projectTpl;
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

	public List<TaskHistoryListDataItem> getTaskHistoryList() {
		return taskHistoryList;
	}

	public void setTaskHistoryList(List<TaskHistoryListDataItem> taskHistoryList) {
		this.taskHistoryList = taskHistoryList;
	}

	public AbstractProjectData(NodeRef nodeRef, String name) {
		super();
		this.nodeRef = nodeRef;
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deliverableList == null) ? 0 : deliverableList.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((projectTpl == null) ? 0 : projectTpl.hashCode());
		result = prime * result + ((taskHistoryList == null) ? 0 : taskHistoryList.hashCode());
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
		if (projectTpl == null) {
			if (other.projectTpl != null)
				return false;
		} else if (!projectTpl.equals(other.projectTpl))
			return false;
		if (taskHistoryList == null) {
			if (other.taskHistoryList != null)
				return false;
		} else if (!taskHistoryList.equals(other.taskHistoryList))
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
		return "AbstractProjectData [nodeRef=" + nodeRef + ", name=" + name + ", projectTpl=" + projectTpl
				+ ", taskList=" + taskList + ", deliverableList=" + deliverableList + ", taskHistoryList="
				+ taskHistoryList + "]";
	}
}
