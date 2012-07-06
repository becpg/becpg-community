package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * 
 * @author matthieu, philippe
 * 
 */
public class DataListSortServiceImpl implements DataListSortService {

	private static Log logger = LogFactory.getLog(DataListSortServiceImpl.class);

	private static final String QUERY_LIST_ITEMS = "+PARENT:\"%s\"";

	private static final String QUERY_LIST_ITEMS_BY_SORT = "+PARENT:\"%s\" AND +@bcpg\\:sort:[%s TO MAX]";

	private static int DEFAULT_LEVEL = 1;
	
	private static int MAX_LEVEL = 256;

	private NodeService nodeService;

	private BeCPGSearchService beCPGSearchService;
	
	private BehaviourFilter policyBehaviourFilter;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	public void computeDepthAndSort(NodeRef nodeRef) {

		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

		// depthLevel manage sort
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

			NodeRef siblingNode = getLastSiblingNode(listContainer, parentLevel, nodeRef);

			if (logger.isDebugEnabled()) {
				logger.debug("computeDepthAndSort for :" + tryGetName(nodeRef));
			}

			insertAfter(listContainer, siblingNode, nodeRef, null);
		} else {

			insertAfter(listContainer, getLastChild(null, listContainer, nodeRef, false), nodeRef, null);
		}

	}

	@Override
	public void insertAfter(NodeRef destNodeRef, NodeRef nodeRef) {

		NodeRef parentLevel = (NodeRef) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_PARENT_LEVEL);

		// Put at same level
		setProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL, parentLevel);

		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();
		insertAfter(listContainer, destNodeRef, nodeRef, null);
	}

	private void insertAfter(NodeRef listContainer, NodeRef siblingNode, NodeRef nodeRef, Integer level) {

		logger.debug("### insertAfter");		
		if(level != null && level > MAX_LEVEL){
			logger.warn("insertAfter is over MAX_LEVEL, exit.");
			return;
		}
		
		
		NodeRef parentLevel = null;
		Integer sort = null;
		boolean isDepthList = nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
		
		if (isDepthList) {

			parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

			if (parentLevel != null) {

				level = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_DEPTH_LEVEL);
				level++;
			} else {
				level = DEFAULT_LEVEL;
			}
		}

		if (siblingNode != null) {

			if(isDepthList){
			
				NodeRef lastChild = getLastChild(siblingNode, listContainer, nodeRef, isDepthList);
				sort = (Integer) nodeService.getProperty(lastChild, BeCPGModel.PROP_SORT);
				logger.debug("###lastChild of: " + tryGetName(siblingNode) + " is " + tryGetName(lastChild) + " -sort: " + sort);
			}
			else{
				sort = (Integer) nodeService.getProperty(siblingNode, BeCPGModel.PROP_SORT);
			}			

		} else {
			// first node of level
			if (parentLevel != null) {
				sort = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_SORT);

				if (logger.isDebugEnabled()) {
					logger.debug("first node of level, parent: " + tryGetName(parentLevel) + " - sort: " + sort);
				}
			}// first node of list
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("first node of list - sort: " + sort);
				}
			}
		}

		// calculate next sort
		Integer nextSort = RepoConsts.SORT_DEFAULT_STEP;
		if (sort == null) {
			nextSort = RepoConsts.SORT_DEFAULT_STEP;
		} else {
			if (parentLevel == null) {
				nextSort = sort + RepoConsts.SORT_DEFAULT_STEP;
			} else {
				nextSort = sort + RepoConsts.SORT_INSERTING_STEP;
			}
		}

		// is next free ?
		NodeRef sortedNodeRef = getSortedNode(listContainer, nextSort, nodeRef);

		if (sortedNodeRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(" nextSort not available : " + nextSort + " - node: " + tryGetName(nodeRef) + " - taken by: " + tryGetName(sortedNodeRef));
			}
			fixSortableList(listContainer, parentLevel, RepoConsts.SORT_DEFAULT_STEP);
			insertAfter(listContainer, siblingNode, nodeRef, level != null ? level + 1 : level);
		} else {			

			if (isDepthList) {

				// start search before setProperty, otherwise it duplicates nodeRef in lucene index !!!
				List<NodeRef> listItems = getChildren(listContainer, nodeRef, true);
				
				setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
				NodeRef prevNode = nodeRef;
				
				for (NodeRef tmp : listItems) {
					insertAfter(listContainer, prevNode, tmp, level != null ? level + 1 : level);
					prevNode = tmp;
				}
			}
			
			//set property after, otherwise it duplicates nodeRef in lucene index !!!
			setProperty(nodeRef, BeCPGModel.PROP_SORT, nextSort);
		}
	}

	private int fixSortableList(NodeRef parentNodeRef, NodeRef parentLevel,  int newSort) {

		logger.info("###FixSortableList. parentNodeRef: " + parentNodeRef + "");
	
		Set<NodeRef> listItems = new LinkedHashSet<NodeRef>(getChildren(parentNodeRef, parentLevel, true));

		for (NodeRef listItem : listItems) {
			setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			newSort = newSort + RepoConsts.SORT_DEFAULT_STEP;
			
			newSort = fixSortableList(parentNodeRef, listItem, newSort);
		}
		
		return newSort;
	}

	@Override
	public NodeRef getLastSiblingNode(NodeRef nodeRef) {
		// depthLevel manage sort
		if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			return null;
		}

		NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

		return getLastChild(getLastSiblingNode(listContainer, parentLevel, nodeRef), listContainer, nodeRef, true);
	}

	private NodeRef getLastChild(NodeRef destNodeRef, NodeRef listContainer, NodeRef nodeRef, boolean isDepthList) {
		
		String query = getQueryByParentLevel(listContainer, destNodeRef,isDepthList);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_SORT, Operator.NOT);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		if (listItems.size() > 0 ) {
			if(isDepthList){
				destNodeRef = getLastChild(listItems.get(0), listContainer, nodeRef, isDepthList);
				if(destNodeRef == null){
					destNodeRef = listItems.get(0);
				}
			} else {
				destNodeRef = listItems.get(0);
			}
			
		} 
		return destNodeRef;
	}

	/*
	 * Get the last sibling node of the level
	 */
	private NodeRef getLastSiblingNode(NodeRef listContainer, NodeRef parentLevel, NodeRef nodeRef) {

		return getSiblingNode(listContainer, parentLevel, nodeRef, false); 
	}	
	
	/*
	 * Get the last or first sibling node of the level (depending of sort)
	 */
	private NodeRef getSiblingNode(NodeRef listContainer, NodeRef parentLevel, NodeRef nodeRef, boolean sort) {

		String query = getQueryByParentLevel(listContainer, parentLevel,true);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, sort), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return listItems.size() > 0 ? listItems.get(0) : null;
	}

	/*
	 * look for another node that has already the sort value
	 */
	private NodeRef getSortedNode(NodeRef listContainer, Integer sort, NodeRef nodeRef) {

		String query = String.format(QUERY_LIST_ITEMS_BY_SORT, listContainer, sort);
		query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_SORT, sort.toString(), Operator.AND);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return listItems.size() > 0 ? listItems.get(0) : null;
	}

	/*
	 * Get all children of the list container
	 */
	private List<NodeRef> getChildren(NodeRef listContainer) {

		return beCPGSearchService.unProtLuceneSearch(String.format(QUERY_LIST_ITEMS, listContainer), LuceneHelper.getSort(BeCPGModel.PROP_SORT, true),
				RepoConsts.MAX_RESULTS_NO_LIMIT);
	}
	
	/*
	 * Check node has child
	 */
	private boolean hasChild(NodeRef listContainer, NodeRef nodeRef) {

		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(String.format(QUERY_LIST_ITEMS, listContainer), LuceneHelper.getSort(BeCPGModel.PROP_SORT, true),
				RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return listItems.size() > 0;
	}

	/*
	 * Get the children of the children of the parent
	 */
	private List<NodeRef> getChildren(NodeRef listContainer, NodeRef parentLevel, boolean isDepthList) {

		return beCPGSearchService.unProtLuceneSearch(getQueryByParentLevel(listContainer, parentLevel,isDepthList), LuceneHelper.getSort(BeCPGModel.PROP_SORT, true),
				RepoConsts.MAX_RESULTS_NO_LIMIT);
	}

	/*
	 * Get the query that return children of parent
	 */
	private String getQueryByParentLevel(NodeRef listContainer, NodeRef parentLevel, boolean isDepthList) {

		String query = String.format(QUERY_LIST_ITEMS, listContainer);
		if (parentLevel == null) {
			if(isDepthList){
				query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_PARENT_LEVEL, Operator.AND);
			}
		} else {
			query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PARENT_LEVEL, parentLevel.toString(), Operator.AND);
		}

		return query;
	}

	/*
	 * Debug function used to get the name of the product stored in the
	 * compoList
	 */
	private String tryGetName(NodeRef nodeRef) {

		List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		NodeRef part = compoAssocRefs.size() > 0 ? (compoAssocRefs.get(0)).getTargetRef() : null;

		return part != null ? (String) nodeService.getProperty(part, ContentModel.PROP_NAME) : (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}
	
	@Override
	public void deleteChildrens(NodeRef listContainer, NodeRef nodeRef) {

		List<NodeRef> listItems = getChildren(listContainer, nodeRef,true);

		for (NodeRef tmp : listItems) {
			nodeService.deleteNode(tmp);
		}

	}

	@Override
	public void swap(NodeRef nodeRef, NodeRef destNodeRef, boolean moveUp) {
		
		// TODO manage level
		Integer sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);
		Integer level = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
		Integer destLevel = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();
		
		if(level == destLevel){
			// don't need to look for the right destNode						
		}
		else if((moveUp && level < destLevel) || (!moveUp && level > destLevel)){
			// look for the right destNode (first or last sibling)
			destNodeRef = getSiblingNode(listContainer, 
										(NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL), 
										nodeRef, 
										moveUp);
		}
		else{
			// cannot swap
			return;
		}				
		
		if(destNodeRef != null){
		
			NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
			Integer destSort = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);
			setProperty(nodeRef, BeCPGModel.PROP_SORT, destSort);
			setProperty(destNodeRef, BeCPGModel.PROP_SORT, sort);
			
			// We need to fix sort when one of the 2 nodes have a child
			if(hasChild(listContainer, nodeRef) || hasChild(listContainer, destNodeRef)){
				fixSortableList(listContainer, parentLevel, RepoConsts.SORT_DEFAULT_STEP);
			}
		}		
	}
	
	private void setProperty(NodeRef nodeRef, QName property, Serializable value){
		
		try {

			policyBehaviourFilter.disableBehaviour(nodeRef, BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.disableBehaviour(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
			
			if (logger.isDebugEnabled()) {
				logger.debug("set property " + property + " with value " + value + " for node " + tryGetName(nodeRef));
			}
						
			nodeService.setProperty(nodeRef, property, value);
			
		} finally {
			policyBehaviourFilter.enableBehaviour(nodeRef, BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.enableBehaviour(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
		}
	}

}
