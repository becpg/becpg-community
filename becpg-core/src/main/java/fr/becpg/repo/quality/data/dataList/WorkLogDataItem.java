package fr.becpg.repo.quality.data.dataList;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

public class WorkLogDataItem {

	private NodeRef nodeRef;
	private String state;
	private String comment;
	private String creator;
	private Date created;
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}	
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public WorkLogDataItem(NodeRef nodeRef, String state, String comment, String creator, Date created){
		this.nodeRef = nodeRef;
		this.state = state;
		this.comment = comment;
		this.creator = creator;
		this.created = created;
	}
}
