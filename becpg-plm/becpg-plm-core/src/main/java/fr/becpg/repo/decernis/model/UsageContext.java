package fr.becpg.repo.decernis.model;

import org.alfresco.service.cmr.repository.NodeRef;

public class UsageContext {

	private String name;
	
	private NodeRef nodeRef;
	
	private Integer moduleId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public Integer getModuleId() {
		return moduleId;
	}

	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}
	
}
