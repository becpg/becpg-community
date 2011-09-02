package fr.becpg.repo;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BeCPGDataItem {

	NodeRef nodeRef;
		
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}


}
