package fr.becpg.repo.entity.datalist;

import org.alfresco.service.cmr.repository.NodeRef;

public interface DataListSortService {

	public void createSortIndex(NodeRef nodeRef);

	public void calculateNextSort(NodeRef listContainer, NodeRef nodeRef, NodeRef parentLevel);

	public NodeRef getLastSiblingNode(NodeRef nodeRef);

	public void insertAfter(NodeRef destNodeRef, NodeRef nodeRef);

	
}
