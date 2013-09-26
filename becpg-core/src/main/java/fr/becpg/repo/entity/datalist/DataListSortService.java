package fr.becpg.repo.entity.datalist;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public interface DataListSortService {

	public NodeRef getLastChild(NodeRef nodeRef);

	public void insertAfter(NodeRef selectedNodeRef, NodeRef nodeRef);

	public void computeDepthAndSort(Set<NodeRef> nodeRefs);

	public void deleteChildrens(NodeRef parentRef, NodeRef childRef);

	public void move(NodeRef nodeRef, boolean moveUp);
	
}
