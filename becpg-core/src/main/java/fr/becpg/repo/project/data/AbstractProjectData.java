package fr.becpg.repo.project.data;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Abstract project data used to manipulate project and project template
 * 
 * @author quere
 * 
 */
public abstract class AbstractProjectData extends BeCPGDataObject{


	protected List<TaskListDataItem> taskList;
	protected List<DeliverableListDataItem> deliverableList;	

	
	public AbstractProjectData() {
		super();
	}

	public AbstractProjectData(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	@DataList
	@AlfQname(qname="pjt:taskList")
	public List<TaskListDataItem> getTaskList() {
		return taskList;
	}
	
	@DataList
	@AlfQname(qname="pjt:deliverableList")
	public List<DeliverableListDataItem> getDeliverableList() {
		return deliverableList;
	}


	public void setTaskList(List<TaskListDataItem> taskList) {
		this.taskList = taskList;
	}
	
	
	public void setDeliverableList(List<DeliverableListDataItem> deliverableList) {
		this.deliverableList = deliverableList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deliverableList == null) ? 0 : deliverableList.hashCode());
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
		if (taskList == null) {
			if (other.taskList != null)
				return false;
		} else if (!taskList.equals(other.taskList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractProjectData [taskList=" + taskList + ", deliverableList=" + deliverableList + "]";
	}
	

}
