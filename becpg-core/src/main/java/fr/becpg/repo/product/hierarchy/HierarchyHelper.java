package fr.becpg.repo.product.hierarchy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;

/**
 * 
 * @author matthieu
 *
 */
public class HierarchyHelper {

	public static String HIERARCHY_SUFFIX = "_Hierarchy";
	
	public static String getHierarchyPath(QName type, NamespaceService namespaceService){
		return RepoConsts.PATH_SYSTEM+"/"+RepoConsts.PATH_PRODUCT_HIERARCHY+"/"+BeCPGModel.ASSOC_ENTITYLISTS.toPrefixString(namespaceService)+"/"+getHierarchyPathName(type);
	}

	public static String getHierarchyPathName(QName type) {
		return type.getLocalName()+HIERARCHY_SUFFIX;
	}

	public static String getHierachyName(NodeRef hierarchyNodeRef, NodeService nodeService) {
		if(hierarchyNodeRef!=null){
			return (String)nodeService.getProperty(hierarchyNodeRef, BeCPGModel.PROP_LKV_VALUE);
		}
		return null;
	}

	public static NodeRef getParentHierachy(NodeRef hierarchyNodeRef, NodeService nodeService) {
		return (NodeRef) nodeService.getProperty(hierarchyNodeRef, BeCPGModel.PROP_PARENT_LEVEL);
	}
}
