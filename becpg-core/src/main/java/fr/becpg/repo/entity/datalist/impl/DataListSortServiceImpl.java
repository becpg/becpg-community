/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>DataListSortServiceImpl class.</p>
 *
 * @author matthieu, philippe
 * @version $Id: $Id
 */
@Service("dataListSortService")
public class DataListSortServiceImpl implements DataListSortService {

	private static final Log logger = LogFactory.getLog(DataListSortServiceImpl.class);

	@Autowired
	@Qualifier("NodeService")
	private NodeService nodeService;
	
	@Autowired
	private AssociationService associationService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	private class SortableDataList {
		
		public SortableDataList(NodeRef nodeRef) {
			dataType = nodeService.getType(nodeRef);
			listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();
		}
		
	
		public SortableDataList(QName dataType, NodeRef listContainer) {
			super();
			this.dataType = dataType;
			this.listContainer = listContainer;
		}

		QName dataType;
		NodeRef listContainer;

		public boolean exists() {
			return (listContainer != null) && nodeService.exists(listContainer);
		}

		public void normalize() {

			if (logger.isDebugEnabled()) {
				logger.debug("###FixSortableList. parentNodeRef: " + listContainer + "");
			}

			MutableInt newSort = new MutableInt(RepoConsts.SORT_DEFAULT_STEP);
			BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType).isNotNull(BeCPGModel.PROP_SORT).addSort(BeCPGModel.PROP_SORT, true)
					.inDB().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list().forEach(listItem -> 
						nodeService.setProperty(listItem, BeCPGModel.PROP_SORT, newSort.getAndAdd(RepoConsts.SORT_DEFAULT_STEP)));
		}

		/*
		 * Get the last sibling node of the level
		 */
		public NodeRef getLastChildOfLevel(NodeRef parentLevel, Set<NodeRef> pendingNodeRefs) {
			List<NodeRef> ret = getQueryByParentLevel(parentLevel, true).isNotNull(BeCPGModel.PROP_SORT)
					.addSort(BeCPGModel.PROP_SORT, false).inDB().maxResults(pendingNodeRefs.size()+1).list();
			
			ret.removeAll(pendingNodeRefs);
			return !ret.isEmpty() ? ret.get(0) : null;
			
		}

		/*
		 * Get the node with max or min sort in Level
		 */
		public NodeRef getNextSiblingNode(NodeRef nodeRef, boolean moveUp) {

			Integer level = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
			Integer sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);

			String startSort = moveUp ? "MIN" : Integer.toString(sort + 1);
			String endSort = moveUp ? Integer.toString(sort - 1) : "MAX";

			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType)
					.andBetween(BeCPGModel.PROP_SORT, startSort, endSort).addSort(BeCPGModel.PROP_SORT, !moveUp);

			if (level != null) {
				queryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, level.toString());
			}

			NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
			if (parentLevel != null) {
				queryBuilder.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, parentLevel.toString());
			}

			return queryBuilder.inDB().singleValue();
		}

		/*
		 * look for another node that has already the sort value
		 */
		public boolean checkSortIsFree(Integer sort, NodeRef nodeRef) {

			return BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType).andPropEquals(BeCPGModel.PROP_SORT, String.valueOf(sort))
					.andNotID(nodeRef).inDB().singleValue() == null;

		}

		/*
		 * Get the children of the children of the parent
		 */
		public List<NodeRef> getChildren(NodeRef parentLevel, boolean isDepthList) {
			return getQueryByParentLevel(parentLevel, isDepthList).addSort(BeCPGModel.PROP_SORT, true).inDB().list();
		}

		/*
		 * Special case for drag & drop: - 1.1 - 2.1 //destNodeRef - 1.2 - 1.3
		 * //nodeRef - 2.2 - 2.3
		 *
		 * We drag 1.3 on 2.1. Last child found is 2.3 but it should not be
		 * after 1.2 so we need to look for 1.2 to know when level stops
		 */

		public NodeRef getLastChild(NodeRef destNodeRef, NodeRef nodeRef, boolean isDepthList) {

			String stopSortCond = "MAX";
			Integer startSort = null;

			if (destNodeRef != null) {

				if (logger.isTraceEnabled()) {
					logger.trace("getLastChild of " + tryGetName(destNodeRef));
				}
				startSort = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);
				Integer level = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_DEPTH_LEVEL);

				if ((startSort != null) && (level != null)) {

					NodeRef tmpNodeRef = BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType)
							.andBetween(BeCPGModel.PROP_SORT, String.valueOf(startSort + 1), "MAX")
							.andBetween(BeCPGModel.PROP_DEPTH_LEVEL, "1", Integer.toString(level)).isNotNull(BeCPGModel.PROP_SORT)
							.addSort(BeCPGModel.PROP_SORT, true).inDB().singleValue();

					if (tmpNodeRef != null) {
						Integer stopSort = (Integer) nodeService.getProperty(tmpNodeRef, BeCPGModel.PROP_SORT);
						logger.trace(" - stopSort: " + stopSort);
						if ((stopSort != null) && (startSort < stopSort)) {
							stopSortCond = stopSort.toString();
						}
					}
				}
			}

			logger.trace(" - startSort: " + startSort + " - stopCond: " + stopSortCond);

			NodeRef tmpNodeRef = getQueryByParentLevel(destNodeRef, isDepthList).andNotID(nodeRef)
					.andBetween(BeCPGModel.PROP_SORT, startSort != null ? Integer.toString(startSort + 1) : "1", stopSortCond)
					.addSort(BeCPGModel.PROP_SORT, false).inDB().singleValue();

			if (tmpNodeRef != null) {
				if (isDepthList) {
					destNodeRef = getLastChild(tmpNodeRef, nodeRef, isDepthList);
					if (destNodeRef == null) {
						destNodeRef = tmpNodeRef;
					}
				} else {
					destNodeRef = tmpNodeRef;
				}
			}
			return destNodeRef;
		}

		public List<NodeRef> getSortRange(Integer sort, Integer lastSort) {
			return BeCPGQueryBuilder.createQuery().parent(listContainer).ofType(dataType)
					.andBetween(BeCPGModel.PROP_SORT, String.valueOf(sort + 1), String.valueOf(lastSort)).addSort(BeCPGModel.PROP_SORT, true).inDB()
					.list();
		}

		/*
		 * Get the query that return children of parent
		 */
		private BeCPGQueryBuilder getQueryByParentLevel(NodeRef parentLevel, boolean isDepthList) {

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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + Objects.hash(dataType, listContainer);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SortableDataList other = (SortableDataList) obj;
			return Objects.equals(dataType, other.dataType) && Objects.equals(listContainer, other.listContainer);
		}

	}
	

	/** {@inheritDoc} */
	@Override
	public void computeDepthAndSort(Set<NodeRef> nodeRefs) {

		try {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

			NodeRef prevParentLevel = null;

			SortableDataList prevList = null;
			int sort = RepoConsts.SORT_DEFAULT_STEP - RepoConsts.SORT_INSERTING_STEP;
			int level = RepoConsts.DEFAULT_LEVEL;

			Map<NodeRef,Set<NodeRef>> pendingNodeRefsMap = new HashMap<>();
			Map<NodeRef, SortableDataList> cache = new HashMap<>();
			
			for (NodeRef nodeRef : nodeRefs) {

				if (nodeService.exists(nodeRef)) {
					SortableDataList list = new SortableDataList(nodeRef);

					if (list.exists()) {
						cache.put(nodeRef, list);
						
						Set<NodeRef> nodesByParent = new HashSet<>();
						if(pendingNodeRefsMap.containsKey(list.listContainer)) {
							nodesByParent = pendingNodeRefsMap.get(list.listContainer);
						}
						nodesByParent.add(nodeRef);
						pendingNodeRefsMap.put(list.listContainer, nodesByParent);				
					} else {
						logger.error("No list Container skipping sort");
					}
					
				}
			}
			
			
			

			for (NodeRef nodeRef : nodeRefs) {

				if (cache.containsKey(nodeRef)) {

					SortableDataList list = cache.get(nodeRef);
					Set<NodeRef> pendingNodeRefs = pendingNodeRefsMap.get(list.listContainer);

						// depthLevel manage sort
						if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
							NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

							// cycle detection
							if (nodeRef.equals(parentLevel)) {
								logger.error("Cannot select itself as parent, otherwise we get a cycle. nodeRef: " + nodeRef);
							} else {

								if (logger.isDebugEnabled()) {
									logger.debug("computeDepthAndSort for :" + tryGetName(nodeRef) + " type: " + list.dataType);
								}

								// #351 : we avoid lucene queries
								if ((prevParentLevel == null) || !prevParentLevel.equals(parentLevel)) {
									prevParentLevel = parentLevel;
									NodeRef prevSiblingNode = list.getLastChildOfLevel(parentLevel, pendingNodeRefs);
									insertAfter(list, prevSiblingNode, nodeRef, pendingNodeRefs);
									sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);
									level = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
								} else {
									sort += RepoConsts.SORT_INSERTING_STEP;
									nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, sort);
									nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
								}
							}
						} else {

							// #351 : we avoid lucene queries
							if (!list.equals(prevList)) {
								prevList = list;
								NodeRef prevSiblingNode = list.getLastChild(null, nodeRef, false);
								if (prevSiblingNode != null) {
									Integer s = (Integer) nodeService.getProperty(prevSiblingNode, BeCPGModel.PROP_SORT);
									if (s != null) {
										sort = s;
									}
								}
							}

							sort += RepoConsts.SORT_INSERTING_STEP;
							nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, sort);
						}

					pendingNodeRefs.remove(nodeRef);
					
					associationService.removeChildCachedAssoc(nodeService.getPrimaryParent(nodeRef).getParentRef(), ContentModel.ASSOC_CONTAINS);

				}
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteChildrens(NodeRef parentNodeRef, NodeRef nodeRef) {
		try {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

				SortableDataList list = new SortableDataList(BeCPGModel.TYPE_ENTITYLIST_ITEM, parentNodeRef);
				List<NodeRef> listItems = list.getChildren(nodeRef, true);

				logger.debug("Delete childrens");
				for (NodeRef tmp : listItems) {
					if (nodeService.exists(tmp) && !nodeService.hasAspect(tmp, ContentModel.ASPECT_PENDING_DELETE)) {
						logger.debug(tmp);
						nodeService.deleteNode(tmp);
					}
				}
			

		} finally {
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void move(NodeRef nodeRef, boolean moveUp) {

		try {

			policyBehaviourFilter.disableBehaviour( BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.disableBehaviour( BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.disableBehaviour( BeCPGModel.TYPE_ACTIVITY_LIST);

			SortableDataList list = new SortableDataList(nodeRef);

			Integer sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);

			if (logger.isDebugEnabled()) {
				logger.debug("node: " + tryGetName(nodeRef));
				logger.debug("moveUp: " + moveUp);
			}

			// look for the right destNode (before or after sibling)
			NodeRef destNodeRef = list.getNextSiblingNode(nodeRef, moveUp);
			logger.debug("destNodeRef " + destNodeRef);

			if (destNodeRef == null && !list.checkSortIsFree(sort, nodeRef)) {
				// several node with same sort
				list.normalize();
				sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);
				destNodeRef = list.getNextSiblingNode(nodeRef, moveUp);
			}

			if (destNodeRef != null) {
				/*
				 * Calculate children lists first, then update sort otherwise we
				 * get the second time all children
				 */
				NodeRef lastChild = list.getLastChild(nodeRef, null, true);
				Integer lastSort = (Integer) nodeService.getProperty(lastChild, BeCPGModel.PROP_SORT);

				List<NodeRef> children = list.getSortRange(sort, lastSort);

				Integer destSort = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);
				NodeRef lastDestChild = list.getLastChild(destNodeRef, null, true);
				Integer lastDestSort = (Integer) nodeService.getProperty(lastDestChild, BeCPGModel.PROP_SORT);

				List<NodeRef> destChildren = list.getSortRange(destSort, lastDestSort);
				// udpate sort of nodeRef and children
				logger.debug("udpate sort of nodeRef and children");
				int newSort = moveUp ? destSort : sort + destChildren.size() + 2;

				for (NodeRef listItem : children) {
					newSort++;
					nodeService.setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
				}

				// update sort of destNodeRef and children
				logger.debug("update sort of destNodeRef and children");

				if (moveUp) {
					newSort++;
					sort = newSort;
				} else {
					newSort = sort;
				}

				for (NodeRef listItem : destChildren) {
					newSort++;
					nodeService.setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
				}

				// swap parent
				logger.debug("swap parent");
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, moveUp ? destSort : newSort + 1);
				nodeService.setProperty(destNodeRef, BeCPGModel.PROP_SORT, sort);

			} else {
				logger.debug("Cannot swap.");
			}
			//Purge assoc cache
			associationService.removeChildCachedAssoc(nodeService.getPrimaryParent(nodeRef).getParentRef(), ContentModel.ASSOC_CONTAINS);
		} finally {
			policyBehaviourFilter.enableBehaviour( BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.enableBehaviour( BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.enableBehaviour( BeCPGModel.TYPE_ACTIVITY_LIST);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void insertAfter(NodeRef selectedNodeRef, NodeRef nodeRef) {

		try {

			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.disableBehaviour( BeCPGModel.TYPE_ACTIVITY_LIST);

			SortableDataList list = new SortableDataList(nodeRef);

			NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
			
			if (logger.isDebugEnabled()) {
				logger.debug("node: " + tryGetName(selectedNodeRef));
				logger.debug("insertAfter: " + tryGetName(nodeRef));
			}

			// Put at same level
			if ((parentLevel != null) || (nodeService.getProperty(selectedNodeRef, BeCPGModel.PROP_PARENT_LEVEL) != null)) {
				nodeService.setProperty(selectedNodeRef, BeCPGModel.PROP_PARENT_LEVEL, parentLevel);
			}

			if (list.exists()) {
				insertAfter(list, nodeRef, selectedNodeRef, new HashSet<>());
			} else {
				logger.debug("list doesn't exists");
			}
			
			//Purge assoc cache
			associationService.removeChildCachedAssoc(nodeService.getPrimaryParent(nodeRef).getParentRef(), ContentModel.ASSOC_CONTAINS);
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);
		}
	}

	private void insertAfter(SortableDataList list, NodeRef siblingNode, NodeRef nodeRef, Set<NodeRef> pendingNodeRefs) {


		Integer nextSort = getNextSort(list, siblingNode, nodeRef);

		if (!list.checkSortIsFree(nextSort, nodeRef)) {
			if (logger.isDebugEnabled()) {
				logger.debug("nextSort not available : " + nextSort + " - node: " + tryGetName(nodeRef));
			}
			list.normalize();

			nextSort = getNextSort(list, siblingNode, nodeRef);
			if (logger.isDebugEnabled()) {
				logger.debug("new nextSort  : " + nextSort);
			}
		}

		nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, nextSort);

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {

			Integer level = RepoConsts.DEFAULT_LEVEL;

			NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

			if (parentLevel != null) {

				level = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_DEPTH_LEVEL);
				if (level == null) {
					level = RepoConsts.DEFAULT_LEVEL + 1;
				} else {
					level++;
				}
			}

			nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);

			NodeRef prevNode = nodeRef;

			for (NodeRef tmp : list.getChildren(nodeRef, true)) {
				
				if (!pendingNodeRefs.contains(tmp)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Recur call insertAfter for children: " + tryGetName(tmp));
					}
					insertAfter(list, prevNode, tmp, pendingNodeRefs);
				}
				prevNode = tmp;
			}
		}

	}

	private Integer getNextSort(SortableDataList list, NodeRef siblingNode, NodeRef nodeRef) {

		Integer sort = null;

		if (siblingNode != null) {

			if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {

				NodeRef lastChild = list.getLastChild(siblingNode, nodeRef, true);
				sort = (Integer) nodeService.getProperty(lastChild, BeCPGModel.PROP_SORT);
				// sibling node can be after lastchild (drag n drop)
				Integer siblingSort = (Integer) nodeService.getProperty(siblingNode, BeCPGModel.PROP_SORT);
				if ((siblingSort != null) && (siblingSort > sort)) {
					sort = siblingSort;
				}
			} else {
				sort = (Integer) nodeService.getProperty(siblingNode, BeCPGModel.PROP_SORT);
			}

		} else {

			NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

			// first node of level
			if (parentLevel != null) {
				sort = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_SORT);

				if (logger.isDebugEnabled()) {
					logger.debug("first node of level, parent: " + tryGetName(parentLevel) + " - sort: " + sort);
				}
			} // first node of list
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("first node of list - sort: " + sort);
				}
			}
		}

		if (sort == null) {
			return RepoConsts.SORT_DEFAULT_STEP;
		}

		return sort + RepoConsts.SORT_INSERTING_STEP;

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getLastChild(NodeRef nodeRef) {

		if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			// depthLevel manage sort
			return null;
		}

		return (new SortableDataList(nodeRef)).getLastChild((NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL), nodeRef, true);
	}

	/*
	 * Debug function used to get the name of the product stored in the
	 * compoList
	 */
	private String tryGetName(NodeRef nodeRef) {

		if (nodeRef == null) {
			return null;
		}

		return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}

}
