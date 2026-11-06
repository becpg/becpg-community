/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.security.SecurityService;

/**
 * Extract MultiLevelDataList at corresponding level
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("multiLevelDataListService")
public class MultiLevelDataListServiceImpl implements MultiLevelDataListService {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(MultiLevelDataListServiceImpl.class);

	/** Constant <code>CACHE_KEY="MultiLevelDataListService.class.getName"{trunked}</code> */
	private static final String CACHE_KEY = MultiLevelDataListService.class.getName();

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AdvSearchService advSearchService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	/** {@inheritDoc} */
	@Override
	public MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter) {
		return getMultiLevelListData(dataListFilter, false, false);
	}

	/** {@inheritDoc} */
	@Override
	public MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter, boolean useExpandedCache, boolean resetTree) {
		if (resetTree) {
			beCPGCacheService.removeFromCache(CACHE_KEY, AuthenticationUtil.getFullyAuthenticatedUser());
		}

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		try {
			return getMultiLevelListData(dataListFilter, dataListFilter.getEntityNodeRef(), 0, dataListFilter.getMaxDepth(), null, new HashSet<>(),
					useExpandedCache, resetTree);
		} finally {
			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug("getMultiLevelListData at depth " + dataListFilter.getMaxDepth() + " in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	/**
	 * <p>getMultiLevelListData.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param currDepth a int
	 * @param maxDepthLevel a int
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param parentNodeRefs a {@link java.util.Set} object
	 * @param useExpandedCache a boolean
	 * @param resetTree a boolean
	 * @return a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object
	 */
	private MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter, NodeRef entityNodeRef, int currDepth, int maxDepthLevel,
			NodeRef dataListNodeRef, Set<NodeRef> parentNodeRefs, boolean useExpandedCache, boolean resetTree) {

		MultiLevelListData ret = new MultiLevelListData(entityNodeRef, currDepth);

		// This check prevents stack over flow when we have a cyclic node
		if (!parentNodeRefs.contains(entityNodeRef)) {
			parentNodeRefs.add(entityNodeRef);
			QName nodeType = nodeService.getType(entityNodeRef);

			if (isExpandedNode(useExpandedCache ? dataListNodeRef : null,
					((maxDepthLevel == 0) && (currDepth == 0)) || (maxDepthLevel < 0) || (currDepth < maxDepthLevel), resetTree)) {
				logger.debug("getMultiLevelListData depth :" + currDepth + " max " + maxDepthLevel);

				if ((currDepth == 0) || !entityDictionaryService.isMultiLevelLeaf(nodeType)) {
					NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					if (listsContainerNodeRef != null) {

						visitMultiLevelListData(ret, dataListFilter, listsContainerNodeRef, currDepth, maxDepthLevel, nodeType,
								dataListFilter.getDataType(), parentNodeRefs, useExpandedCache, resetTree);

						QName secondaryType = entityDictionaryService.getMultiLevelSecondaryPivot(dataListFilter.getDataType());

						if (secondaryType != null) {
							logger.debug("Visiting secondary type:" + secondaryType);

							visitMultiLevelListData(ret, dataListFilter, listsContainerNodeRef, currDepth, maxDepthLevel, nodeType, secondaryType,
									parentNodeRefs, useExpandedCache, resetTree);

						}

					}
				}
			}

			if (entityDictionaryService.isMultiLevelLeaf(nodeType)) {
				ret.setLeaf(true);
			}
		}
		return ret;
	}

	/**
	 * <p>visitMultiLevelListData.</p>
	 *
	 * @param ret a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @param listsContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param currDepth a int
	 * @param maxDepthLevel a int
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object
	 * @param dataType a {@link org.alfresco.service.namespace.QName} object
	 * @param parentNodeRefs a {@link java.util.Set} object
	 * @param useExpandedCache a boolean
	 * @param resetTree a boolean
	 */
	private void visitMultiLevelListData(MultiLevelListData ret, DataListFilter dataListFilter, NodeRef listsContainerNodeRef, int currDepth,
			int maxDepthLevel, QName nodeType, QName dataType, Set<NodeRef> parentNodeRefs, boolean useExpandedCache, boolean resetTree) {
		int accessMode = securityService.computeAccessMode(ret.getEntityNodeRef(), nodeType, dataType.toPrefixString(namespaceService));
		if (SecurityService.NONE_ACCESS == accessMode) {
			return;
		}

		NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataType);
		if (dataListNodeRef == null) {
			return;
		}

		// Adv search already filter by perm
		List<NodeRef> rows = getListNodeRef(dataListNodeRef, dataListFilter, dataType);
		TreeVisit visit = new TreeVisit(dataListFilter, currDepth, maxDepthLevel, parentNodeRefs, useExpandedCache, resetTree,
				!dataType.equals(dataListFilter.getDataType()), groupRowsByParent(rows, dataListNodeRef), new HashSet<>());

		appendChildren(ret, dataListNodeRef, visit);
		appendUnreachedRows(ret, rows, visit);
	}

	/**
	 * State shared while the rows of one list are appended to the tree. Rows are grouped by the row they hang
	 * under, the rows at the root of the list being grouped under the list itself, each group keeping the order
	 * the list was sorted in.
	 */
	private record TreeVisit(DataListFilter dataListFilter, int currDepth, int maxDepthLevel, Set<NodeRef> parentNodeRefs,
			boolean useExpandedCache, boolean resetTree, boolean isSecondary, Map<NodeRef, List<NodeRef>> childrenByParent,
			Set<NodeRef> visitedRows) {

		List<NodeRef> childrenOf(NodeRef nodeRef) {
			return childrenByParent.getOrDefault(nodeRef, List.of());
		}
	}

	/**
	 * Groups the rows of a list under the row they hang under, keeping the order the list was sorted in, so that
	 * a sort applies among the roots first and then among the children of each row. A row without parent, being
	 * its own parent or whose parent is filtered out of the list hangs under the list itself.
	 *
	 * @param rows the rows of the list, in the requested order
	 * @param dataListNodeRef the list, key of the rows displayed at its root
	 * @return the rows grouped by the row they hang under
	 */
	private Map<NodeRef, List<NodeRef>> groupRowsByParent(List<NodeRef> rows, NodeRef dataListNodeRef) {
		Set<NodeRef> rowSet = new HashSet<>(rows);
		Map<NodeRef, List<NodeRef>> childrenByParent = new HashMap<>();
		for (NodeRef row : rows) {
			NodeRef parentNodeRef = (NodeRef) nodeService.getProperty(row, BeCPGModel.PROP_PARENT_LEVEL);
			boolean hangsUnderParent = (parentNodeRef != null) && !parentNodeRef.equals(row) && rowSet.contains(parentNodeRef);
			childrenByParent.computeIfAbsent(hangsUnderParent ? parentNodeRef : dataListNodeRef, k -> new ArrayList<>()).add(row);
		}
		return childrenByParent;
	}

	/**
	 * Appends, in the order they were sorted in, the rows hanging under a row (or under the list for its roots).
	 *
	 * @param parentData the tree node the rows are appended to
	 * @param parentNodeRef the row, or the list, the rows hang under
	 * @param visit the state of the list being visited
	 */
	private void appendChildren(MultiLevelListData parentData, NodeRef parentNodeRef, TreeVisit visit) {
		for (NodeRef row : visit.childrenOf(parentNodeRef)) {
			if (visit.visitedRows().add(row)) {
				appendRow(parentData, row, visit);
			}
		}
	}

	/**
	 * Appends at the root the rows no root leads to, ie. rows whose parents form a cycle, so that they are still
	 * displayed.
	 *
	 * @param ret the tree of the list
	 * @param rows the rows of the list, in the requested order
	 * @param visit the state of the list being visited
	 */
	private void appendUnreachedRows(MultiLevelListData ret, List<NodeRef> rows, TreeVisit visit) {
		List<NodeRef> unreachedRows = rows.stream().filter(row -> !visit.visitedRows().contains(row)).toList();
		visit.visitedRows().addAll(unreachedRows);
		for (NodeRef row : unreachedRows) {
			appendRow(ret, row, visit);
		}
	}

	/**
	 * Appends a row and its descendants under a tree node, unless a collapsed ancestor hides it: its descendants
	 * are then hidden too.
	 *
	 * @param parentData the tree node the row is appended to
	 * @param row the row
	 * @param visit the state of the list being visited
	 */
	private void appendRow(MultiLevelListData parentData, NodeRef row, TreeVisit visit) {
		NodeRef rowEntityNodeRef = getEntityNodeRef(row);
		if (rowEntityNodeRef == null) {
			parentData.setLeaf(false);
		}

		int depthLevel = getDepthLevel(row);
		int nextDepth = visit.currDepth() + depthLevel;
		if (!isRowDisplayed(row, depthLevel, nextDepth, visit)) {
			markDescendantsVisited(row, visit);
			return;
		}

		MultiLevelListData rowData = createRowData(row, rowEntityNodeRef, nextDepth, visit);
		if (!visit.isSecondary() || !rowData.getTree().isEmpty()) {
			parentData.getTree().put(row, rowData);
		}
		appendChildren(rowData, row, visit);
	}

	private int getDepthLevel(NodeRef row) {
		Integer depthLevel = (Integer) nodeService.getProperty(row, BeCPGModel.PROP_DEPTH_LEVEL);
		return depthLevel != null ? depthLevel : 1;
	}

	private boolean isRowDisplayed(NodeRef row, int depthLevel, int nextDepth, TreeVisit visit) {
		NodeRef parentNodeRef = (NodeRef) nodeService.getProperty(row, BeCPGModel.PROP_PARENT_LEVEL);
		int maxDepthLevel = visit.maxDepthLevel();
		boolean displayedByDepth = ((maxDepthLevel == 0) && (parentNodeRef != null)) || (maxDepthLevel < 0) || (nextDepth <= maxDepthLevel)
				|| (depthLevel == 1);
		return isExpandedNode(visit.useExpandedCache() ? parentNodeRef : null, displayedByDepth, visit.resetTree());
	}

	private void markDescendantsVisited(NodeRef row, TreeVisit visit) {
		for (NodeRef child : visit.childrenOf(row)) {
			if (visit.visitedRows().add(child)) {
				markDescendantsVisited(child, visit);
			}
		}
	}

	/**
	 * Creates the tree node of a row: the sub tree of the entity it points to, or an empty node for a row
	 * pointing to no entity.
	 *
	 * @param row the row
	 * @param rowEntityNodeRef the entity the row points to, null if none
	 * @param nextDepth the depth of the row
	 * @param visit the state of the list being visited
	 * @return the tree node of the row
	 */
	private MultiLevelListData createRowData(NodeRef row, NodeRef rowEntityNodeRef, int nextDepth, TreeVisit visit) {
		if (rowEntityNodeRef == null) {
			MultiLevelListData rowData = new MultiLevelListData(new ArrayList<>(), nextDepth);
			rowData.setLeaf(!getIsGroup(row));
			return rowData;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Append depth " + nextDepth + " at currLevel " + visit.currDepth() + " for "
					+ nodeService.getProperty(rowEntityNodeRef, ContentModel.PROP_NAME));
		}
		return getMultiLevelListData(visit.dataListFilter(), rowEntityNodeRef, nextDepth, visit.maxDepthLevel(), row,
				new HashSet<>(visit.parentNodeRefs()), visit.useExpandedCache(), visit.resetTree());
	}

	/**
	 * <p>getListNodeRef.</p>
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @param dataType a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link java.util.List} object
	 */
	private List<NodeRef> getListNodeRef(NodeRef dataListNodeRef, DataListFilter dataListFilter, QName dataType) {

		if (dataListFilter.isAllFilter() && entityDictionaryService.isSubClass(dataType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
			return entityListDAO.getListItems(dataListNodeRef, dataType, dataListFilter.getSortMap());
		} else {
			int depth = dataListFilter.getMaxDepth();
			try {
				dataListFilter.updateMaxDepth(-1);

				return advSearchService.queryAdvSearch(dataType, dataListFilter.getSearchQuery(dataListNodeRef), dataListFilter.getCriteriaMap(),
						RepoConsts.MAX_RESULTS_UNLIMITED);
			} finally {
				dataListFilter.updateMaxDepth(depth);
			}
		}
	}

	/**
	 * <p>getEntityNodeRef.</p>
	 *
	 * @param listItemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef getEntityNodeRef(NodeRef listItemNodeRef) {
		QName pivotAssoc = entityDictionaryService.getDefaultPivotAssoc(nodeService.getType(listItemNodeRef));
		if (pivotAssoc != null) {
			NodeRef part = associationService.getTargetAssoc(listItemNodeRef, pivotAssoc);
			if ((part != null) && (permissionService.hasPermission(part, PermissionService.READ) == AccessStatus.ALLOWED) 
					&& entityDictionaryService.isSubClass(nodeService.getType(part), BeCPGModel.TYPE_ENTITY_V2)) {
				return part;
			}
		}
		return null;
	}
	

	/**
	 * <p>getIsGroup.</p>
	 *
	 * @param listItemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	private boolean getIsGroup(NodeRef listItemNodeRef) {
		QName propQname = entityDictionaryService.getMultiLevelGroupProperty(nodeService.getType(listItemNodeRef));
		if (propQname != null) {
			return Boolean.TRUE.equals(nodeService.getProperty(listItemNodeRef, propQname));
		}
		return false;
	}


	/** {@inheritDoc} */
	@Override
	public boolean isExpandedNode(NodeRef entityFolder, boolean condition, boolean resetTree) {
		if (resetTree) {
			return condition;
		}
		if (entityFolder != null) {
			Map<NodeRef, Boolean> expandedNodes = beCPGCacheService.getFromCache(CACHE_KEY, AuthenticationUtil.getFullyAuthenticatedUser(), () -> new LRUCache(100));
			if ((expandedNodes != null) && expandedNodes.containsKey(entityFolder)) {
				if (logger.isDebugEnabled()) {
					logger.debug("found Expanded node : " + entityFolder + " for " + AuthenticationUtil.getFullyAuthenticatedUser());
				}
				return expandedNodes.get(entityFolder);
			}
		}
		return condition;
	}

	public class LRUCache extends LinkedHashMap<NodeRef, Boolean> {
		private static final long serialVersionUID = 1L;
		protected int maxElements;

		public LRUCache(int maxSize) {
			super(maxSize, 0.75F, true);
			this.maxElements = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(Entry<NodeRef, Boolean> eldest) {
			return (size() > this.maxElements);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = (prime * result) + getEnclosingInstance().hashCode();
			result = (prime * result) + Objects.hash(maxElements);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			LRUCache other = (LRUCache) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
				return false;
			}
			return maxElements == other.maxElements;
		}

		private MultiLevelDataListServiceImpl getEnclosingInstance() {
			return MultiLevelDataListServiceImpl.this;
		}

	}

	/** {@inheritDoc} */
	@Override
	public void expandOrColapseNode(NodeRef nodeToExpand, boolean expand) {
		Map<NodeRef, Boolean> expandedNodes = beCPGCacheService.getFromCache(CACHE_KEY, AuthenticationUtil.getFullyAuthenticatedUser(),
				 () -> new LRUCache(100)
				);
		expandedNodes.put(nodeToExpand, expand);

		beCPGCacheService.storeInCache(CACHE_KEY, AuthenticationUtil.getFullyAuthenticatedUser(), expandedNodes);
	}

}
