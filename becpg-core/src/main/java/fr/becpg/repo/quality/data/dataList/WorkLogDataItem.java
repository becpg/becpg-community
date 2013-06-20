package fr.becpg.repo.quality.data.dataList;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;


@AlfType
@AlfQname(qname = "qa:workLog")
public class WorkLogDataItem extends BeCPGDataObject {

	private String state;
	private String comment;
	private String creator;
	private Date created;
	
	@AlfProp
	@AlfQname(qname = "qa:wlState")
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}	
	
	@AlfProp
	@AlfQname(qname = "qa:wlComment")
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}	
	
	@AlfProp
	@AlfQname(qname = "cm:creator")
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	@AlfProp
	@AlfQname(qname = "cm:created")
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	
	public WorkLogDataItem() {
		super();
	}
	public WorkLogDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}
	public WorkLogDataItem(NodeRef nodeRef, String state, String comment, String creator, Date created){
		this.nodeRef = nodeRef;
		this.state = state;
		this.comment = comment;
		this.creator = creator;
		this.created = created;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		WorkLogDataItem other = (WorkLogDataItem) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "WorkLogDataItem [state=" + state + ", comment=" + comment + ", creator=" + creator + ", created=" + created + "]";
	}
	
	
}
