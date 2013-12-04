package fr.becpg.repo.hierarchy.impl;

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
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Service that manages hierarchies
 * @author quere
 *
 */
public class HierarchyServiceImpl implements HierarchyService{	
	
	private static Log logger = LogFactory.getLog(HierarchyServiceImpl.class);
	
	private static final String SUFFIX_ALL = "*";
	
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
		return getHierarchyByPath(HierarchyHelper.getHierarchyPath(type,namespaceService), null, value);
	}

	@Override
	public NodeRef getHierarchy(QName type, NodeRef parentNodeRef, String value) {		
		return getHierarchyByPath(HierarchyHelper.getHierarchyPath(type,namespaceService), parentNodeRef, value);
	}
	
	@Override
	public List<NodeRef> getRootHierarchies(QName type, String value) {				
		return getHierarchiesByPath(HierarchyHelper.getHierarchyPath(type,namespaceService), null, value);
	}

	@Override
	public List<NodeRef> getHierarchies(QName type, NodeRef parentNodeRef, String value) {		
		return getHierarchiesByPath(HierarchyHelper.getHierarchyPath(type,namespaceService), parentNodeRef, value);
	}
	
	
	
	@Override
	public NodeRef getHierarchyByPath(String path, NodeRef parentNodeRef, String value){
		
		NodeRef hierarchyNodeRef = getHierarchyByQuery(getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_CODE, value, false), value);
		
		if(hierarchyNodeRef == null){
			hierarchyNodeRef = getHierarchyByQuery(getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_LKV_VALUE, value, false), value);
		}
		
		return hierarchyNodeRef;
	}
	
	@Override
	public List<NodeRef> getHierarchiesByPath(String path, NodeRef parentNodeRef, String value){		
		return beCPGSearchService.luceneSearch(getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_LKV_VALUE, value,false), RepoConsts.MAX_SUGGESTIONS);
	}	
	
	@Override
	public List<NodeRef> getAllHierarchiesByPath(String path, String value) {
		return beCPGSearchService.luceneSearch(getLuceneQuery(path, null, BeCPGModel.PROP_LKV_VALUE, value, true), RepoConsts.MAX_SUGGESTIONS);
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
	
	private String getLuceneQuery(String path, NodeRef parentNodeRef, QName property, String value, boolean all){
		
		String query = LuceneHelper.getCondPath(LuceneHelper.encodePath(path), null) +
				LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_LINKED_VALUE));
		
		if(parentNodeRef != null){
			query += LuceneHelper.mandatory(LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PARENT_LEVEL, parentNodeRef.toString()));
		}
		else if(!all){
			//query += LuceneHelper.mandatory(LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_PARENT_LEVEL));
			query += LuceneHelper.mandatory(LuceneHelper.getCondEqualValue(BeCPGModel.PROP_DEPTH_LEVEL, "1"));
		}
		
		// value == * -> return all
		if(!isAllQuery(value)){
			query += LuceneHelper.mandatory(LuceneHelper.getCondEqualValue(property, value));
		}
		
		return query;
	}
	

	@Override
	public String getHierarchyPath(NodeRef hierarchyNodeRef) {
		
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
	
	protected boolean isAllQuery(String query) {
		return query != null && query.trim().equals(SUFFIX_ALL);
	}

	
}
