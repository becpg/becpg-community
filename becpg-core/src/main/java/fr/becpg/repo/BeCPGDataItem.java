package fr.becpg.repo;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BeCPGDataItem {

	protected NodeRef nodeRef;
	
		
	public BeCPGDataItem() {
		super();
	}

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public BeCPGDataItem(NodeRef nodeRef) {
		super();
		this.nodeRef = nodeRef;
	}
	
}
