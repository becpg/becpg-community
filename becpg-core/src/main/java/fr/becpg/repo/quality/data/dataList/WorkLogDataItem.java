package fr.becpg.repo.quality.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

public class WorkLogDataItem {

	private NodeRef nodeRef;
	private String state;
	private String comment;
	
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
	
	public WorkLogDataItem(NodeRef nodeRef, String state, String comment){
		this.nodeRef = nodeRef;
		this.state = state;
		this.comment = comment;
	}
}
