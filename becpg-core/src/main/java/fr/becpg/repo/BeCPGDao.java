package fr.becpg.repo;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.model.BeCPGDataObject;

@Deprecated
public interface BeCPGDao<T extends BeCPGDataObject> {

	public NodeRef create(NodeRef parentNodeRef, T cpData);	
	public void update(NodeRef cpNodeRef, T cpData);		
	public T find(NodeRef cpNodeRef);
	public void delete(NodeRef cpNodeRef);
	
	
}
