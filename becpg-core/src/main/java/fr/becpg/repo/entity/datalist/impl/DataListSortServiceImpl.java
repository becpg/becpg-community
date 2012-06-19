package fr.becpg.repo.entity.datalist.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
 * @author matthieu
 * 
 */
public class DataListSortServiceImpl implements DataListSortService {

	private static Log logger = LogFactory.getLog(DataListSortServiceImpl.class);

	private static final String QUERY_LIST_ITEMS = "+PARENT:\"%s\"";

	private static final String QUERY_LIST_ITEMS_BY_SORT = "+PARENT:\"%s\" AND +@bcpg\\:sort:[%s TO MAX]";

	private NodeService nodeService;

	private BeCPGSearchService beCPGSearchService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	public void createSortIndex(NodeRef nodeRef) {

		// depthLevel manage sort
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			return;
		}

		Integer sortIndex = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);

		if (sortIndex == null) {

			NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			String query = String.format(QUERY_LIST_ITEMS, parentNodeRef);

			Map<String, Boolean> sort = new HashMap<String, Boolean>();
			sort.put("@" + BeCPGModel.PROP_SORT, false);

			List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, sort, RepoConsts.MAX_RESULTS_SINGLE_VALUE);

			if (listItems.isEmpty()) {
				sortIndex = RepoConsts.SORT_DEFAULT_STEP;
			} else if (listItems.size() == 1) {

				NodeRef lastIndexNodeRef = listItems.get(0);
				sortIndex = (Integer) nodeService.getProperty(lastIndexNodeRef, BeCPGModel.PROP_SORT);

				if (sortIndex != null) {
					sortIndex = sortIndex + RepoConsts.SORT_DEFAULT_STEP;
				} else {
					fixSortableList(parentNodeRef);
				}
			} else {
				logger.error("Returned several results. Query: " + query);
			}

			if (sortIndex != null) {
				logger.debug("set property sort: " + sortIndex + " - node: " + nodeRef);
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, sortIndex);
			}
		}

	}

	/*
	 * Calculate the next sort value of the node. May do a rebuild of the sort
	 * index
	 */
	@Override
	public void calculateNextSort(NodeRef listContainer, NodeRef nodeRef, NodeRef parentLevel) {

		// sibling nodes
		NodeRef lastSiblingNode = getSiblingNode(listContainer, parentLevel, nodeRef);

		insertAfter(lastSiblingNode, nodeRef);

	}

	@Override
	public void insertAfter(NodeRef destNodeRef, NodeRef nodeRef) {

		NodeRef parentLevel = null;

		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			parentLevel = (NodeRef) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_PARENT_LEVEL);
			// nodeService.setProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL, parentLevel);
		}

		Integer sort = null;

		if (destNodeRef != null) {

			sort = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);

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
			fixSortableList(listContainer);
			insertAfter(destNodeRef, nodeRef);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("update sort of currentNodeRef: sort: " + sort + " -nextSort : " + nextSort + " - node: " + tryGetName(nodeRef));
			}
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, nextSort);
		}

	}


	private void fixSortableList(NodeRef parentNodeRef) {

		// rebuild index
		List<NodeRef> listItems = getChildren(parentNodeRef);

		int newSort = RepoConsts.SORT_DEFAULT_STEP;
		for (NodeRef listItem : listItems) {

			logger.debug("set property of " + tryGetName(listItem) + " sort " + nodeService.getProperty(listItem, BeCPGModel.PROP_SORT) + " new sort " + newSort);
			nodeService.setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			newSort = newSort + RepoConsts.SORT_DEFAULT_STEP;

		}

		logger.info("FixSortableList. parentNodeRef: " + parentNodeRef + ", last sortIndex: " + newSort);
	}

	// debug
	private String tryGetName(NodeRef nodeRef) {

		List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		NodeRef part = compoAssocRefs.size() > 0 ? (compoAssocRefs.get(0)).getTargetRef() : null;

		return part != null ? (String) nodeService.getProperty(part, ContentModel.PROP_NAME) : (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}

	@Override
	public NodeRef getLastSiblingNode(NodeRef nodeRef) {
		// depthLevel manage sort
		if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			return null;
		}

		NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

		return getLastChildren(getSiblingNode(listContainer, parentLevel, nodeRef),listContainer );
	}



	private NodeRef getLastChildren(NodeRef destNodeRef, NodeRef listContainer) {
		String query = getQueryByParentLevel(listContainer, destNodeRef);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		if (listItems.size() > 0) {
			destNodeRef = getLastChildren(listContainer,listItems.get(0));
		}
		return destNodeRef;
	}
	
	
	/*
	 * Get the last sibling node of the level
	 */
	private NodeRef getSiblingNode(NodeRef listContainer, NodeRef parentLevel, NodeRef nodeRef) {

		String query = getQueryByParentLevel(listContainer, parentLevel);
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
	 * Get the query that return children of parent
	 */
	private String getQueryByParentLevel(NodeRef listContainer, NodeRef parentLevel) {

		String query = String.format(QUERY_LIST_ITEMS, listContainer);
		if (parentLevel == null) {
			query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_PARENT_LEVEL, Operator.AND);
		} else {
			query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PARENT_LEVEL, parentLevel.toString(), Operator.AND);
		}

		return query;
	}

}
