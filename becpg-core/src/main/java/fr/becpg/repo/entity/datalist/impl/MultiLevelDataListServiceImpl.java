/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
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
import fr.becpg.repo.search.AdvSearchService;

/**
 * Extract MultiLevelDataList at corresponding level
 * 
 * @author matthieu
 */
@Service("multiLevelDataListService")
public class MultiLevelDataListServiceImpl implements MultiLevelDataListService {

	private static Log logger = LogFactory.getLog(MultiLevelDataListServiceImpl.class);

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
	
	
	@Override
	public MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {
			return getMultiLevelListData(dataListFilter, dataListFilter.getEntityNodeRefs().get(0), 0, dataListFilter.getMaxDepth());
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("getMultiLevelListData at depth " + dataListFilter.getMaxDepth() + " in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	public MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter, NodeRef entityNodeRef, int currDepth, int maxDepthLevel) {
		MultiLevelListData ret = new MultiLevelListData(entityNodeRef, currDepth);
		if (maxDepthLevel < 0 || currDepth < maxDepthLevel) {
			logger.debug("getMultiLevelListData depth :" + currDepth + " max " + maxDepthLevel);
			NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
			if (listsContainerNodeRef != null) {

				NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataListFilter.getDataType());
				if (dataListNodeRef != null) {

					List<NodeRef> childRefs = advSearchService.queryAdvSearch(dataListFilter.getSearchQuery(dataListNodeRef), SearchService.LANGUAGE_LUCENE,
							dataListFilter.getDataType(), dataListFilter.getCriteriaMap(), dataListFilter.getSortMap(), RepoConsts.MAX_RESULTS_UNLIMITED);

					//Adv search already filter by perm 
					
					for (NodeRef childRef : childRefs) {
						entityNodeRef = getEntityNodeRef(childRef);
						if (entityNodeRef != null) {
							Integer depthLevel = (Integer) nodeService.getProperty(childRef, BeCPGModel.PROP_DEPTH_LEVEL);
							if (logger.isDebugEnabled()) {
								logger.debug("Append level:" + depthLevel + " at currLevel " + currDepth + " for "
										+ nodeService.getProperty(entityNodeRef, org.alfresco.model.ContentModel.PROP_NAME));
							}

							MultiLevelListData tmp = getMultiLevelListData(dataListFilter, entityNodeRef, currDepth + (depthLevel != null ? depthLevel : 1), maxDepthLevel);
							ret.getTree().put(childRef, tmp);
						}
					}
				}

			}
		}
		return ret;
	}

	// TODO more generic
	// Use alfresco repository to make it more generic 
	private NodeRef getEntityNodeRef(NodeRef listItemNodeRef) {
		List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, entityDictionaryService.getDefaultPivotAssoc(nodeService.getType(listItemNodeRef)));
		NodeRef part = compoAssocRefs!=null && !compoAssocRefs.isEmpty() ? (compoAssocRefs.get(0)).getTargetRef() : null;
		if( part!=null && permissionService.hasPermission( part,PermissionService.READ) == AccessStatus.ALLOWED){
			return part;
		}
		return null;
	}

}
