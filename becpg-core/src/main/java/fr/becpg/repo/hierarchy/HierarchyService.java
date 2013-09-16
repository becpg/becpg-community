package fr.becpg.repo.hierarchy;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.SystemProductType;

public interface HierarchyService {

	public NodeRef getRootHierarchy(QName type, String value);
	public NodeRef getHierarchy(QName type, NodeRef hierarchyParent, String value);
	public NodeRef getHierarchyByPath(String path, NodeRef parentNodeRef, String value);
	public List<NodeRef> getRootHierarchies(QName type, String value);
	public List<NodeRef> getHierarchies(QName type, NodeRef hierarchyParent, String value);	
	public List<NodeRef> getHierarchiesByPath(String path, NodeRef parentNodeRef, String value);
	public List<NodeRef> getAllHierarchiesByPath(String path, String query);
	public NodeRef createRootHierarchy(NodeRef dataListNodeRef, String value);
	public NodeRef createHierarchy(NodeRef dataListNodeRef, NodeRef hierarchyParent, String value);
	public String  getHierarchyPath(NodeRef hierarchy, SystemProductType systemProductType);
	
}
