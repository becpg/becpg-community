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

			NodeRef siblingNode = getSiblingNode(listContainer, parentLevel, nodeRef);

			if (logger.isDebugEnabled()) {
				logger.debug("computeDepthAndSort for :" + tryGetName(nodeRef));
			}

			insertAfter(listContainer, siblingNode, nodeRef);
		} else {

			insertAfter(listContainer, getLastChild(null, listContainer, nodeRef, false), nodeRef);
		}

	}

	@Override
	public void insertAfter(NodeRef destNodeRef, NodeRef nodeRef) {

		NodeRef parentLevel = (NodeRef) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_PARENT_LEVEL);

		// Put at same level
		setProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL, parentLevel);

		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();
		insertAfter(listContainer, destNodeRef, nodeRef);
	}

	private void insertAfter(NodeRef listContainer, NodeRef siblingNode, NodeRef nodeRef) {

		logger.debug("### insertAfter");
		
		NodeRef parentLevel = null;
		Integer level = null;
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
			fixSortableList(listContainer, nodeRef);
			insertAfter(listContainer, siblingNode, nodeRef);
		} else {

			setProperty(nodeRef, BeCPGModel.PROP_SORT, nextSort);

			if (isDepthList) {

				// start search before setProperty, otherwise it duplicates nodeRef in lucene index !!!
				List<NodeRef> listItems = getChildren(listContainer, nodeRef, true);
				
				setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
				NodeRef prevNode = nodeRef;
				
				for (NodeRef tmp : listItems) {
					insertAfter(listContainer, prevNode, tmp);
					prevNode = tmp;
				}
			}
		}
	}

	private void fixSortableList(NodeRef parentNodeRef,NodeRef nodeRef) {

		logger.info("###FixSortableList. parentNodeRef: " + parentNodeRef + "");
	
		Set<NodeRef> listItems = new LinkedHashSet<NodeRef>(getChildren(parentNodeRef));

		int newSort = RepoConsts.SORT_DEFAULT_STEP;
		for (NodeRef listItem : listItems) {
			setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			newSort = newSort + RepoConsts.SORT_DEFAULT_STEP;
		}
	}

	@Override
	public NodeRef getLastSiblingNode(NodeRef nodeRef) {
		// depthLevel manage sort
		if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			return null;
		}

		NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

		return getLastChild(getSiblingNode(listContainer, parentLevel, nodeRef), listContainer, nodeRef, true);
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
	private NodeRef getSiblingNode(NodeRef listContainer, NodeRef parentLevel, NodeRef nodeRef) {

		String query = getQueryByParentLevel(listContainer, parentLevel,true);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
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
	public void swap(NodeRef nodeRef, NodeRef destNodeRef) {
		// TODO manage level
		Integer sortIndex = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);

		setProperty(nodeRef, BeCPGModel.PROP_SORT, nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT));
		setProperty(destNodeRef, BeCPGModel.PROP_SORT, sortIndex);
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
