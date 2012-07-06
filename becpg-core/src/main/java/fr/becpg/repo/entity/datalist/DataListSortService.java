package fr.becpg.repo.entity.datalist;

import org.alfresco.service.cmr.repository.NodeRef;

public interface DataListSortService {

	public NodeRef getLastSiblingNode(NodeRef nodeRef);

	public void insertAfter(NodeRef destNodeRef, NodeRef nodeRef);

	public void computeDepthAndSort(NodeRef nodeRef);

	public void deleteChildrens(NodeRef parentRef, NodeRef childRef);

	public void swap(NodeRef nodeRef, NodeRef destNodeRef, boolean moveUp);
	
}
