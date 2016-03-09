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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.AdvSearchService;

/**
 * Extract MultiLevelDataList at corresponding level
 *
 * @author matthieu
 */
@Service("multiLevelDataListService")
public class MultiLevelDataListServiceImpl implements MultiLevelDataListService {

	private static final Log logger = LogFactory.getLog(MultiLevelDataListServiceImpl.class);

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

	@Override
	public MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {
			return getMultiLevelListData(dataListFilter, dataListFilter.getEntityNodeRef(), 0, dataListFilter.getMaxDepth(), new HashSet<NodeRef>());
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("getMultiLevelListData at depth " + dataListFilter.getMaxDepth() + " in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	private MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter, NodeRef entityNodeRef, int currDepth, int maxDepthLevel,
			Set<NodeRef> visitedNodeRefs) {

		// Create the visited nodes set if it has not already been created
		if (visitedNodeRefs == null) {
			visitedNodeRefs = new HashSet<NodeRef>();
		}

		MultiLevelListData ret = new MultiLevelListData(entityNodeRef, currDepth);

		// This check prevents stack over flow when we have a cyclic node
		if (!visitedNodeRefs.contains(entityNodeRef) || currDepth < 100) {
			visitedNodeRefs.add(entityNodeRef);
			if ((maxDepthLevel < 0) || (currDepth < maxDepthLevel)) {
				logger.debug("getMultiLevelListData depth :" + currDepth + " max " + maxDepthLevel);
				if ((currDepth == 0) || !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(entityNodeRef))) {
					NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					if (listsContainerNodeRef != null) {

						NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataListFilter.getDataType());
						if (dataListNodeRef != null) {

							List<NodeRef> childRefs = getListNodeRef(dataListNodeRef, dataListFilter);
							// Adv search already filter by perm

							for (NodeRef childRef : childRefs) {
								entityNodeRef = getEntityNodeRef(childRef);
								if (entityNodeRef != null) {
									Integer depthLevel = (Integer) nodeService.getProperty(childRef, BeCPGModel.PROP_DEPTH_LEVEL);
									if (logger.isDebugEnabled()) {
										logger.debug("Append level:" + depthLevel + " at currLevel " + currDepth + " for "
												+ nodeService.getProperty(entityNodeRef, org.alfresco.model.ContentModel.PROP_NAME));
									}
									MultiLevelListData tmp = getMultiLevelListData(dataListFilter, entityNodeRef,
											currDepth + (depthLevel != null ? depthLevel : 1), maxDepthLevel, visitedNodeRefs);
									ret.getTree().put(childRef, tmp);
								} else {
									Integer depthLevel = (Integer) nodeService.getProperty(childRef, BeCPGModel.PROP_DEPTH_LEVEL);
									
									ret.getTree().put(childRef,new MultiLevelListData(new ArrayList<>(), 
											currDepth + (depthLevel != null ? depthLevel : 1)));
								}
							}
						}
					}
				}
			}
		} else {
		  logger.debug("Detected cycle for: "+entityNodeRef);
		}
		return ret;
	}

	private List<NodeRef> getListNodeRef(NodeRef dataListNodeRef, DataListFilter dataListFilter) {
		if (dataListFilter.isAllFilter() && entityDictionaryService.isSubClass(dataListFilter.getDataType(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
			return entityListDAO.getListItems(dataListNodeRef, dataListFilter.getDataType(), dataListFilter.getSortMap());
		} else {
			return advSearchService.queryAdvSearch(dataListFilter.getDataType(), dataListFilter.getSearchQuery(dataListNodeRef),
					dataListFilter.getCriteriaMap(), RepoConsts.MAX_RESULTS_UNLIMITED);
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

}
