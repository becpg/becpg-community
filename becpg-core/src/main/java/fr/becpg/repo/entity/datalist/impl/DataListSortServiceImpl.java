/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * 
 * @author matthieu, philippe
 * 
 */
@Service("dataListSortService")
public class DataListSortServiceImpl implements DataListSortService {

	private static Log logger = LogFactory.getLog(DataListSortServiceImpl.class);	

	@Autowired
	private NodeService nodeService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Override
	public void computeDepthAndSort(Set<NodeRef> nodeRefs) {

		NodeRef prevParentLevel = null;

		NodeRef prevListContainer = null;
		int sort = RepoConsts.SORT_DEFAULT_STEP - RepoConsts.SORT_INSERTING_STEP;
		int level = RepoConsts.DEFAULT_LEVEL;

		HashSet<NodeRef> pendingNodeRefs = new HashSet<NodeRef>(nodeRefs);

		for (NodeRef nodeRef : nodeRefs) {

			QName dataType = nodeService.getType(nodeRef);
			NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

			if(nodeService.exists(listContainer)){
			
				// depthLevel manage sort
				if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
					NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
	
					// cycle detection
					if (nodeRef.equals(parentLevel)) {
						logger.error("Cannot select itself as parent, otherwise we get a cycle. nodeRef: " + nodeRef);
					} else {
	
						if (logger.isDebugEnabled()) {
							logger.debug("computeDepthAndSort for :" + tryGetName(nodeRef));
						}
	
						// #351 : we avoid lucene queries
						if (prevParentLevel == null || !prevParentLevel.equals(parentLevel)) {
							prevParentLevel = parentLevel;
							NodeRef prevSiblingNode = getLastChildOfLevel(dataType, listContainer, parentLevel, nodeRef);
							insertAfter(dataType, listContainer, prevSiblingNode, nodeRef, pendingNodeRefs);
							sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);
							level = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
						} else {
							sort += RepoConsts.SORT_INSERTING_STEP;
							setProperty(nodeRef, BeCPGModel.PROP_SORT, sort);
							setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
						}
					}
				} else {
	
					// #351 : we avoid lucene queries
					if (prevListContainer == null || !prevListContainer.equals(listContainer)) {
						prevListContainer = listContainer;
						NodeRef prevSiblingNode = getLastChild(dataType, null, listContainer, nodeRef, false);
						if (prevSiblingNode != null) {
							Integer s = (Integer) nodeService.getProperty(prevSiblingNode, BeCPGModel.PROP_SORT);
							if (s != null) {
								sort = s;
							}
						}
					}
	
					sort += RepoConsts.SORT_INSERTING_STEP;
					setProperty(nodeRef, BeCPGModel.PROP_SORT, sort);
				}
			} else {
				logger.error("No list Container skipping sort");
			}

			pendingNodeRefs.remove(nodeRef);
		}
	}

	@Override
	public void insertAfter(NodeRef selectedNodeRef, NodeRef nodeRef) {

		QName dataType = nodeService.getType(nodeRef);
		NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

		// Put at same level
		if (parentLevel != null || nodeService.getProperty(selectedNodeRef, BeCPGModel.PROP_PARENT_LEVEL) != null) {
			setProperty(selectedNodeRef, BeCPGModel.PROP_PARENT_LEVEL, parentLevel);
		}

		NodeRef listContainer = nodeService.getPrimaryParent(selectedNodeRef).getParentRef();
		insertAfter(dataType, listContainer, nodeRef, selectedNodeRef, new HashSet<NodeRef>());
	}

	private void insertAfter(QName dataType, NodeRef listContainer, NodeRef siblingNode, NodeRef nodeRef, HashSet<NodeRef> pendingNodeRefs) {

		logger.debug("insertAfter");

		Integer level = null;
		NodeRef parentLevel = null;
		Integer sort = null;
		boolean isDepthList = nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
		logger.debug("isDepthList: " + isDepthList);

		if (isDepthList) {

			parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

			if (parentLevel != null) {

				level = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_DEPTH_LEVEL);
				if (level == null) {
					level = RepoConsts.DEFAULT_LEVEL + 1;
				} else {
					level++;
				}
			} else {
				level = RepoConsts.DEFAULT_LEVEL;
			}
		}

		if (siblingNode != null) {

			if (isDepthList) {

				NodeRef lastChild = getLastChild(dataType, siblingNode, listContainer, nodeRef, isDepthList);
				sort = (Integer) nodeService.getProperty(lastChild, BeCPGModel.PROP_SORT);
				// sibling node can be after lastchild (drag n drop)
				Integer siblingSort = (Integer) nodeService.getProperty(siblingNode, BeCPGModel.PROP_SORT);
				if (siblingSort != null && siblingSort > sort) {
					sort = siblingSort;
				}
			} else {
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
			nextSort = sort + RepoConsts.SORT_INSERTING_STEP;
		}

		// is next free ?
		NodeRef sortedNodeRef = getSortedNode(dataType, listContainer, nextSort, nodeRef);

		if (sortedNodeRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(" nextSort not available : " + nextSort + " - node: " + tryGetName(nodeRef) + " - taken by: "
						+ tryGetName(sortedNodeRef));
			}
			fixSortableList(dataType, listContainer);
			insertAfter(dataType, listContainer, siblingNode, nodeRef, pendingNodeRefs);
		} else {

			setProperty(nodeRef, BeCPGModel.PROP_SORT, nextSort);

			if (isDepthList) {

				// start search before setProperty, otherwise it duplicates
				// nodeRef in lucene index !!!
				List<NodeRef> listItems = getChildren(dataType, listContainer, nodeRef, true);

				setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
				NodeRef prevNode = nodeRef;

				for (NodeRef tmp : listItems) {
					if(logger.isDebugEnabled()){
						logger.debug("start call insertAfter: " + tryGetName(tmp));
					}
					if (!pendingNodeRefs.contains(tmp)) {
						insertAfter(dataType, listContainer, prevNode, tmp, pendingNodeRefs);
					}
					prevNode = tmp;
				}
			}
		}
	}

	private void fixSortableList(QName dataType, NodeRef listContainer) {

		logger.info("###FixSortableList. parentNodeRef: " + listContainer + "");

		List<NodeRef> listItems = BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType).isNotNull(BeCPGModel.PROP_SORT)
				.addSort(BeCPGModel.PROP_SORT, true).inDB().list();
		int newSort = RepoConsts.SORT_DEFAULT_STEP;

		for (NodeRef listItem : listItems) {
			setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			newSort = newSort + RepoConsts.SORT_DEFAULT_STEP;
		}
	}

	@Override
	public NodeRef getLastChild(NodeRef nodeRef) {

		if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			// depthLevel manage sort
			return null;
		}

		QName dataType = nodeService.getType(nodeRef);
		NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

		return getLastChild(dataType, parentLevel, listContainer, nodeRef, true);
	}

	private NodeRef getLastChild(QName dataType, NodeRef destNodeRef, NodeRef listContainer, NodeRef nodeRef, boolean isDepthList) {

		/*
		 * Special case for drag & drop: - 1.1 - 2.1 //destNodeRef - 1.2 - 1.3
		 * //nodeRef - 2.2 - 2.3
		 * 
		 * We drag 1.3 on 2.1. Last child found is 2.3 but it should not be
		 * after 1.2 so we need to look for 1.2 to know when level stops
		 */

		String stopSortCond = "MAX";
		Integer startSort = null;

		if (destNodeRef != null) {

			if(logger.isDebugEnabled()){
				logger.debug("getLastChild of " + tryGetName(destNodeRef));
			}
			startSort = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);
			Integer level = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_DEPTH_LEVEL);

			if (startSort != null && level != null) {

				NodeRef tmpNodeRef = BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType)
						.andBetween(BeCPGModel.PROP_SORT, String.valueOf(startSort + 1), "MAX")
						.andBetween(BeCPGModel.PROP_DEPTH_LEVEL, "1", Integer.toString(level)).isNotNull(BeCPGModel.PROP_SORT)
						.addSort(BeCPGModel.PROP_SORT, true).inDB().singleValue();

				if (tmpNodeRef != null) {
					Integer stopSort = (Integer) nodeService.getProperty(tmpNodeRef, BeCPGModel.PROP_SORT);
					logger.debug("stopSort: " + stopSort);
					if (stopSort != null && startSort < stopSort) {
						stopSortCond = stopSort.toString();
					}
				}
			}
		}

		logger.debug("startSort: " + startSort + " - stopCond: " + stopSortCond);

		NodeRef tmpNodeRef = getQueryByParentLevel(dataType, listContainer, destNodeRef, isDepthList).andNotID(nodeRef)
				.andBetween(BeCPGModel.PROP_SORT, startSort != null ? Integer.toString(startSort + 1) : "1", stopSortCond)
				.addSort(BeCPGModel.PROP_SORT, false).inDB().singleValue();

		if (tmpNodeRef != null) {
			if (isDepthList) {
				destNodeRef = getLastChild(dataType, tmpNodeRef, listContainer, nodeRef, isDepthList);
				if (destNodeRef == null) {
					destNodeRef = tmpNodeRef;
				}
			} else {
				destNodeRef = tmpNodeRef;
			}
		}
		return destNodeRef;
	}

	/*
	 * Get the last sibling node of the level
	 */
	private NodeRef getLastChildOfLevel(QName dataType, NodeRef listContainer, NodeRef parentLevel, NodeRef nodeRef) {
		return getQueryByParentLevel(dataType, listContainer, parentLevel, true).andNotID(nodeRef).isNotNull(BeCPGModel.PROP_SORT)
				.addSort(BeCPGModel.PROP_SORT, false).inDB().singleValue();
	}

	private NodeRef getNextSiblingNode(QName dataType, NodeRef listContainer, NodeRef nodeRef, boolean moveUp) {

		Integer level = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
		Integer sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);

		String startSort = moveUp ? "MIN" : Integer.toString(sort + 1);
		String endSort = moveUp ? Integer.toString(sort - 1) : "MAX";

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType)
				.andBetween(BeCPGModel.PROP_SORT, startSort, endSort).addSort(BeCPGModel.PROP_SORT, !moveUp);

		if (level != null) {
			queryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, level.toString());
		}

		return queryBuilder.inDB().singleValue();
	}

	/*
	 * look for another node that has already the sort value
	 */
	private NodeRef getSortedNode(QName dataType, NodeRef listContainer, Integer sort, NodeRef nodeRef) {

		return BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType).andPropEquals(BeCPGModel.PROP_SORT, String.valueOf(sort))
				.andNotID(nodeRef).inDB().singleValue();

	}

	/*
	 * Get the children of the children of the parent
	 */
	private List<NodeRef> getChildren(QName dataType, NodeRef listContainer, NodeRef parentLevel, boolean isDepthList) {
		return getQueryByParentLevel(dataType, listContainer, parentLevel, isDepthList).addSort(BeCPGModel.PROP_SORT, true).inDB()
				.list();
	}

	/*
	 * Get the query that return children of parent
	 */
	private BeCPGQueryBuilder getQueryByParentLevel(QName dataType, NodeRef listContainer, NodeRef parentLevel, boolean isDepthList) {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType);

		if (parentLevel == null) {
			if (isDepthList) {
				queryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, "1");
			}
		} else {
			queryBuilder.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, parentLevel.toString());
		}

		return queryBuilder;
	}

	/*
	 * Debug function used to get the name of the product stored in the
	 * compoList
	 */
	@Deprecated
	private String tryGetName(NodeRef nodeRef) {

		if (nodeRef == null) {
			return null;
		}

		// List<AssociationRef> compoAssocRefs =
		// nodeService.getTargetAssocs(nodeRef,
		// BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		// NodeRef part = compoAssocRefs!=null && !compoAssocRefs.isEmpty() ?
		// (compoAssocRefs.get(0)).getTargetRef() : null;
		//
		// return part != null ? (String) nodeService.getProperty(part,
		// ContentModel.PROP_NAME) : (String) nodeService.getProperty(nodeRef,
		// ContentModel.PROP_NAME);
		return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}

	@Override
	public void deleteChildrens(NodeRef listContainer, NodeRef nodeRef) {

		List<NodeRef> listItems = getChildren(null, listContainer, nodeRef, true);

		for (NodeRef tmp : listItems) {
			if (nodeService.exists(tmp)) {
				nodeService.deleteNode(tmp);
			}
		}
	}

	@Override
	public void move(NodeRef nodeRef, boolean moveUp) {

		QName dataType = nodeService.getType(nodeRef);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();
		Integer sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);

		if(logger.isDebugEnabled()){
			logger.debug("node: " + tryGetName(nodeRef));
			logger.debug("moveUp: " + moveUp);
		}

		// look for the right destNode (before or after sibling)
		NodeRef destNodeRef = getNextSiblingNode(dataType, listContainer, nodeRef, moveUp);

		if (destNodeRef == null) {			
			if(getSortedNode(dataType, listContainer, sort, nodeRef) != null){
				// several node with same sort
				fixSortableList(dataType, listContainer);
				sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);
				destNodeRef = getNextSiblingNode(dataType, listContainer, nodeRef, moveUp);
			}			
		} 

		if (destNodeRef == null) {
			// cannot swap
			logger.debug("Cannot swap.");
		} else {

			/*
			 * Calculate children lists first, then update sort otherwise we get
			 * the second time all children
			 */

			NodeRef lastChild = getLastChild(dataType, nodeRef, listContainer, null, true);
			Integer lastSort = (Integer) nodeService.getProperty(lastChild, BeCPGModel.PROP_SORT);

			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().parent(listContainer)
					.andBetween(BeCPGModel.PROP_SORT, String.valueOf(sort + 1), String.valueOf(lastSort)).ofType(dataType)
					.addSort(BeCPGModel.PROP_SORT, true);

			List<NodeRef> children = queryBuilder.inDB().list();

			Integer destSort = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);
			NodeRef lastDestChild = getLastChild(dataType, destNodeRef, listContainer, null, true);
			Integer lastDestSort = (Integer) nodeService.getProperty(lastDestChild, BeCPGModel.PROP_SORT);

			queryBuilder = BeCPGQueryBuilder.createQuery().parent(listContainer)
					.andBetween(BeCPGModel.PROP_SORT, String.valueOf(destSort + 1), String.valueOf(lastDestSort)).ofType(dataType)
					.addSort(BeCPGModel.PROP_SORT, true);

			List<NodeRef> destChildren = queryBuilder.inDB().list();

			// udpate sort of nodeRef and children
			int newSort = destSort;
			for (NodeRef listItem : children) {
				newSort++;
				setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			}

			// update sort of destNodeRef and children
			newSort = sort;
			for (NodeRef listItem : destChildren) {
				newSort++;
				setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			}

			// swap parent
			setProperty(nodeRef, BeCPGModel.PROP_SORT, destSort);
			setProperty(destNodeRef, BeCPGModel.PROP_SORT, sort);
		}
	}

	private void setProperty(NodeRef nodeRef, QName property, Serializable value) {

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
