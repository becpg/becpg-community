package fr.becpg.repo;

import java.util.Collection;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface BeCPGListDao<T extends BeCPGDataObject> {

	public NodeRef create(NodeRef parentNodeRef, T cpData, Collection<QName> dataLists);	
	public void update(NodeRef cpNodeRef, T cpData, Collection<QName> dataLists);		
	public T find(NodeRef cpNodeRef, Collection<QName> dataLists);
	public void delete(NodeRef cpNodeRef);
	
}
