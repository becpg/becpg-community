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

	private static final Log logger = LogFactory.getLog(MultiLevelDataListServiceImpl.class);

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

	private void visitMultiLevelListData(MultiLevelListData ret, DataListFilter dataListFilter, NodeRef listsContainerNodeRef, int currDepth,
			int maxDepthLevel, QName nodeType, QName dataType, Set<NodeRef> parentNodeRefs, boolean useExpandedCache, boolean resetTree) {
		int accessMode = securityService.computeAccessMode(dataListFilter.getEntityNodeRef() ,nodeType, dataType.toPrefixString(namespaceService));

		if (SecurityService.NONE_ACCESS != accessMode) {
			NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataType);

			if (dataListNodeRef != null) {

				boolean isSecondary = !dataType.equals(dataListFilter.getDataType());

				List<NodeRef> childRefs = getListNodeRef(dataListNodeRef, dataListFilter, dataType);
				// Adv search already filter by perm

				Map<NodeRef, MultiLevelListData> currTmp = new HashMap<>();

				for (NodeRef childRef : childRefs) {
					NodeRef currEntityNodeRef = getEntityNodeRef(childRef);

					Integer depthLevel = (Integer) nodeService.getProperty(childRef, BeCPGModel.PROP_DEPTH_LEVEL);
					if (depthLevel == null) {
						depthLevel = 1;
					}
					int nextDepth = currDepth + depthLevel;
					NodeRef parentNodeRef = (NodeRef) nodeService.getProperty(childRef, BeCPGModel.PROP_PARENT_LEVEL);
					if (isExpandedNode(useExpandedCache ? parentNodeRef : null, ((maxDepthLevel == 0) && (parentNodeRef != null))
							|| (maxDepthLevel < 0) || (nextDepth <= maxDepthLevel) || (depthLevel == 1), resetTree)) {
						MultiLevelListData tmp;
						if (currEntityNodeRef != null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Append level:" + depthLevel + " at currLevel " + currDepth + " for "
										+ nodeService.getProperty(currEntityNodeRef, ContentModel.PROP_NAME));
							}

							Set<NodeRef> curVisitedNodeRef = new HashSet<>(parentNodeRefs);

							tmp = getMultiLevelListData(dataListFilter, currEntityNodeRef, nextDepth, maxDepthLevel, childRef, curVisitedNodeRef,
									useExpandedCache, resetTree);
						} else {
							tmp = new MultiLevelListData(new ArrayList<>(), nextDepth);
							tmp.setLeaf(!getIsGroup(childRef));
						}

						currTmp.put(childRef, tmp);

						if (!isSecondary || (!tmp.getTree().isEmpty())) {
							if ((parentNodeRef != null) && currTmp.containsKey(parentNodeRef)) {
								MultiLevelListData parent = currTmp.get(parentNodeRef);
								parent.getTree().put(childRef, tmp);
							} else if(parentNodeRef == null ){
								ret.getTree().put(childRef, tmp);
							
							}
						}

					} 
					
					if(parentNodeRef!=null  && currTmp.containsKey(parentNodeRef) && currEntityNodeRef==null ) {
						MultiLevelListData parent = currTmp.get(parentNodeRef);
						if(currEntityNodeRef == null ) {
							parent.setLeaf(false);
						}
					}
				}

			}
		}
	}

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

	private NodeRef getEntityNodeRef(NodeRef listItemNodeRef) {
		QName pivotAssoc = entityDictionaryService.getDefaultPivotAssoc(nodeService.getType(listItemNodeRef));
		if (pivotAssoc != null) {
			NodeRef part = associationService.getTargetAssoc(listItemNodeRef, pivotAssoc);
			if ((part != null) && (permissionService.hasPermission(part, PermissionService.READ) == AccessStatus.ALLOWED)) {
				return part;
			}
		}
		return null;
	}
	

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
		if (entityFolder != null) {
			Map<NodeRef, Boolean> expandedNodes = beCPGCacheService.getFromCache(CACHE_KEY, AuthenticationUtil.getFullyAuthenticatedUser());
			if ((expandedNodes != null) && expandedNodes.containsKey(entityFolder)) {
				if (resetTree) {
					expandedNodes.remove(entityFolder);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("found Expanded node : " + entityFolder + " for " + AuthenticationUtil.getFullyAuthenticatedUser());
					}
					return expandedNodes.get(entityFolder);
				}
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
		Map<NodeRef, Boolean> expandedNodes = beCPGCacheService.getFromCache(CACHE_KEY, AuthenticationUtil.getFullyAuthenticatedUser());
		if (expandedNodes == null) {
			expandedNodes = new LRUCache(100);
		}

		expandedNodes.put(nodeToExpand, expand);

		beCPGCacheService.storeInCache(CACHE_KEY, AuthenticationUtil.getFullyAuthenticatedUser(), expandedNodes);
	}

}
