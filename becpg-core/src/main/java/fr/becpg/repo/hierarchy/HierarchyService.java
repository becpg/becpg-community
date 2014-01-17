package fr.becpg.repo.hierarchy;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface HierarchyService {

	NodeRef getRootHierarchy(QName type, String value);
	NodeRef getHierarchy(QName type, NodeRef hierarchyParent, String value);
	NodeRef getHierarchyByPath(String path, NodeRef parentNodeRef, String value);
	List<NodeRef> getRootHierarchies(QName type, String value);
	List<NodeRef> getHierarchies(QName type, NodeRef hierarchyParent, String value);	
	List<NodeRef> getHierarchiesByPath(String path, NodeRef parentNodeRef, String value);
	List<NodeRef> getAllHierarchiesByPath(String path, String query);
	NodeRef createRootHierarchy(NodeRef dataListNodeRef, String value);
	NodeRef createHierarchy(NodeRef dataListNodeRef, NodeRef hierarchyParent, String value);
	String  getHierarchyPath(NodeRef hierarchy);
	void classifyByHierarchy(NodeRef containerNodeRef, NodeRef entityNodeRef);
	
}
