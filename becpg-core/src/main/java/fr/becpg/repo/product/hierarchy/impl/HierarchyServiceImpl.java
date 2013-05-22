package fr.becpg.repo.product.hierarchy.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.product.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.hierarchy.HierarchyService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Service that manages hierarchies
 * @author quere
 *
 */
public class HierarchyServiceImpl implements HierarchyService{	
	
	private static Log logger = LogFactory.getLog(HierarchyServiceImpl.class);
	
	private NamespaceService namespaceService;
	private BeCPGSearchService beCPGSearchService;
	private NodeService nodeService;
		
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public NodeRef getRootHierarchy(QName type, String value) {
		
		String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ROOT, 
				LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(type,namespaceService)),
				value);
		
		return getHierarchyByQuery(queryPath, value);
	}

	@Override
	public NodeRef getHierarchy(QName type, NodeRef parentNodeRef, String value) {
		
		String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, 
				LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(type,namespaceService)), parentNodeRef.toString(),
				value);
		
		return getHierarchyByQuery(queryPath, value);
	}
	
	@Override
	public NodeRef createRootHierarchy(NodeRef dataListNodeRef, String hierachy1) {
		
		return createHierarchy(dataListNodeRef, null, hierachy1);
	}

	@Override
	public NodeRef createHierarchy(NodeRef dataListNodeRef, NodeRef parentHierachy, String hierachy) {
		
		logger.debug("createHierarchy, parent hierarchy : " + parentHierachy + " - hierarchy: " + hierachy);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(BeCPGModel.PROP_LKV_VALUE, hierachy);
		if (parentHierachy != null) {
			properties.put(BeCPGModel.PROP_PARENT_LEVEL, parentHierachy);
		}

		NodeRef entityNodeRef = nodeService.getChildByName(dataListNodeRef, ContentModel.ASSOC_CONTAINS, hierachy);

		if (entityNodeRef == null) {
			entityNodeRef = nodeService.createNode(dataListNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(hierachy)), BeCPGModel.TYPE_LINKED_VALUE, properties).getChildRef();
		}

		return entityNodeRef;
	}

	private NodeRef getHierarchyByQuery(String queryPath, String value){
		
		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_SINGLE_VALUE);

		logger.debug("resultSet.length() : " + ret.size()+" for "+queryPath);
		if (ret.size() == 1) {
			return	ret.get(0);
		} else if(ret.size()>1){
			for (NodeRef n : ret) {
				if (value.equals(nodeService.getProperty(n, BeCPGModel.PROP_LKV_VALUE))) {
					return n;
				}
			}
		}	
		
		return null;
	}
	

	@Override
	public String getHierarchyPath(NodeRef hierarchyNodeRef, SystemProductType systemProductType) {
		
		StringBuilder  path = new StringBuilder();
		
		path.append("./cm:"+RepoConsts.PATH_PRODUCTS);
		 
		appendNamePath(path, hierarchyNodeRef);
		
		
		return path.toString();
	}

	private void appendNamePath(StringBuilder path, NodeRef hierarchyNodeRef) {
		NodeRef parent = HierarchyHelper.getParentHierachy(hierarchyNodeRef, nodeService);
		if(parent!=null){
			appendNamePath(path,parent);
		}
		path.append("/cm:");
		path.append(ISO9075.encode(HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService)));
		
	}	
}
