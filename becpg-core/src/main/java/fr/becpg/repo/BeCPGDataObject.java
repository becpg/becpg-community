package fr.becpg.repo;

import org.alfresco.service.cmr.repository.NodeRef;

public  abstract class  BeCPGDataObject {

	NodeRef nodeRef;	
	String name;
	
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
	
}
