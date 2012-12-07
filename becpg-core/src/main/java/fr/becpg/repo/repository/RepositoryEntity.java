package fr.becpg.repo.repository;

import org.alfresco.service.cmr.repository.NodeRef;


//TODO merge with BaseObject and BeCPGDataObject
public interface RepositoryEntity {

	public NodeRef getNodeRef();
	public void setNodeRef(NodeRef nodeRef);
	
	public NodeRef getParentNodeRef();
	public void setParentNodeRef(NodeRef parentNodeRef);
	public String getName();

}
