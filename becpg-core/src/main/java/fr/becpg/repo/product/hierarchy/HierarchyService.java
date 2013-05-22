package fr.becpg.repo.product.hierarchy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.SystemProductType;

public interface HierarchyService {

	public NodeRef getRootHierarchy(QName type, String value);
	public NodeRef getHierarchy(QName type, NodeRef hierarchyParent, String value);
	public NodeRef createRootHierarchy(NodeRef dataListNodeRef, String value);
	public NodeRef createHierarchy(NodeRef dataListNodeRef, NodeRef hierarchyParent, String value);
	public String  getHierarchyPath(NodeRef hierarchy, SystemProductType systemProductType);
}
