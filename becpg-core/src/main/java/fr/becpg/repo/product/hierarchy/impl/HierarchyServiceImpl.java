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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
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
	public NodeRef getHierarchy1(QName type, String value) {
		
		String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ROOT, 
				LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(type,namespaceService)),
				value);
		
		return getHierarchyByQuery(queryPath, value);
	}

	@Override
	public NodeRef getHierarchy2(QName type, NodeRef hierachy1NodeRef, String value) {
		
		String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, 
				LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(type,namespaceService)), hierachy1NodeRef.toString(),
				value);
		
		return getHierarchyByQuery(queryPath, value);
	}
	
	@Override
	public NodeRef createHierarchy1(NodeRef dataListNodeRef, String hierachy1) {
		
		return createHierarchy2(dataListNodeRef, null, hierachy1);
	}

	@Override
	public NodeRef createHierarchy2(NodeRef dataListNodeRef, NodeRef hierachy1NodeRef, String hierachy2) {
		
		logger.debug("createHierarchy, hierarchy1 : " + hierachy1NodeRef + " - hierarchy2: " + hierachy2);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(BeCPGModel.PROP_LKV_VALUE, hierachy2);
		if (hierachy1NodeRef != null) {
			properties.put(BeCPGModel.PROP_PARENT_LEVEL, hierachy1NodeRef);
		}

		NodeRef entityNodeRef = nodeService.getChildByName(dataListNodeRef, ContentModel.ASSOC_CONTAINS, hierachy2);

		if (entityNodeRef == null) {
			entityNodeRef = nodeService.createNode(dataListNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(hierachy2)), BeCPGModel.TYPE_LINKED_VALUE, properties).getChildRef();
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
				if (value.equals(nodeService.getProperty(n, ContentModel.PROP_NAME))) {
					return n;
				}
			}
		}	
		
		return null;
	}	
}
