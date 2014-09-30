package fr.becpg.repo.project.data.projectList;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Log time list
 * @author quere
 *
 */
@AlfType
@AlfQname(qname = "pjt:logTimeList")
public class LogTimeListDataItem extends BeCPGDataObject {

	private Date date;
	private Double time;
	private NodeRef task;
	
	
	@AlfProp
	@AlfQname(qname = "pjt:ltlDate")
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@AlfProp
	@AlfQname(qname = "pjt:ltlTime")
	public Double getTime() {
		return time;
	}

	public void setTime(Double time) {
		this.time = time;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "pjt:ltlTask")
	public NodeRef getTask() {
		return task;
	}

	public void setTask(NodeRef task) {
		this.task = task;
	}
	
	public LogTimeListDataItem(){
		super();
	}
	
	public LogTimeListDataItem(Date date, Double time, NodeRef task){
		this.date = date;
		this.time = time;
		this.task = task;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogTimeListDataItem other = (LogTimeListDataItem) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LogTimeListDataItem [date=" + date + ", time=" + time + ", task=" + task + ", nodeRef=" + nodeRef
				+ ", parentNodeRef=" + parentNodeRef + ", name=" + name + ", aspects=" + aspects + ", extraProperties="
				+ extraProperties + ", isTransient=" + isTransient + "]";
	}

}
