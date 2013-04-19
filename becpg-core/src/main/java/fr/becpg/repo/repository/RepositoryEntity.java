package fr.becpg.repo.repository;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


//TODO merge with BaseObject and BeCPGDataObject
public interface RepositoryEntity {

	public NodeRef getNodeRef();
	public void setNodeRef(NodeRef nodeRef);
	
	public NodeRef getParentNodeRef();
	public void setParentNodeRef(NodeRef parentNodeRef);
	public String getName();
	
	public Set<QName> getAspects();
	public void setAspects(Set<QName> aspects);

}
