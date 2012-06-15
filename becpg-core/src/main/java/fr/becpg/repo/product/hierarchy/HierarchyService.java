package fr.becpg.repo.product.hierarchy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface HierarchyService {

	public NodeRef getHierarchy1(QName type, String value);
	public NodeRef getHierarchy2(QName type, NodeRef hierarchy1, String value);
	public NodeRef createHierarchy1(NodeRef dataListNodeRef, String value);
	public NodeRef createHierarchy2(NodeRef dataListNodeRef, NodeRef hierarchy1, String value);
}
