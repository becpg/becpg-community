/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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

import java.util.*;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.WUsedFilter;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

@Service("wUsedListService")
public class WUsedListServiceImpl implements WUsedListService {

	private static final Log logger = LogFactory.getLog(WUsedListServiceImpl.class);

	private static final int MAX_LEVEL = 50;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Override
	public MultiLevelListData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int maxDepthLevel) {
		return getWUsedEntity(Collections.singletonList(entityNodeRef), WUsedOperator.AND, null, associationName, 0, maxDepthLevel, new HashSet<>());
	}

	@Override
	public MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, WUsedFilter filter, QName associationName,
			int maxDepthLevel) {
		return getWUsedEntity(entityNodeRefs, operator, filter, associationName, 0, maxDepthLevel, new HashSet<>());
	}

	@Override
	public MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, QName associationName, int maxDepthLevel) {
		return getWUsedEntity(entityNodeRefs, operator, null, associationName, 0, maxDepthLevel, new HashSet<>());
	}

	private MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, WUsedFilter filter, QName associationName,
			int depthLevel, int maxDepthLevel, Set<NodeRef> parentNodeRefs) {

		if (maxDepthLevel == -1) {
			// Avoid infinite loop
			maxDepthLevel = MAX_LEVEL;
		}

		MultiLevelListData ret = new MultiLevelListData(entityNodeRefs, depthLevel);

		if (entityNodeRefs!=null && !entityNodeRefs.isEmpty() &&!parentNodeRefs.contains(entityNodeRefs.get(0))) {
			parentNodeRefs.addAll(entityNodeRefs);
			if (WUsedOperator.OR.equals(operator)) {
				for (NodeRef entityNodeRef : entityNodeRefs) {
					MultiLevelListData entityLevel = new MultiLevelListData(entityNodeRef, depthLevel + 1);
					appendAssocs(entityLevel, new HashSet<>(nodeService.getSourceAssocs(entityNodeRef, associationName)), depthLevel + 1,
							maxDepthLevel, associationName, filter, parentNodeRefs);
					if (!entityLevel.getTree().isEmpty()) {
						ret.getTree().put(entityNodeRef, entityLevel);
					}

				}
			} else {
				Set<AssociationRef> associationRefs = new HashSet<>();
				for (NodeRef entityNodeRef : entityNodeRefs) {
					if (associationRefs.isEmpty()) {

						associationRefs.addAll(nodeService.getSourceAssocs(entityNodeRef, associationName));
						if (logger.isDebugEnabled()) {
							logger.debug("associationRefs size" + associationRefs.size() + "  for entityNodeRef " + entityNodeRef + " and assocs"
									+ associationName);
						}
						if (associationRefs.isEmpty()) {
							break;
						}

					} else {

						// Test for join
						for (Iterator<AssociationRef> iterator = associationRefs.iterator(); iterator.hasNext();) {
							AssociationRef associationRef = iterator.next();
							boolean delete = true;
							for (AssociationRef associationRef2 : nodeService.getSourceAssocs(entityNodeRef, associationName)) {
								// Test that assoc is also include in product
								if (getEntity(associationRef.getSourceRef()).equals(getEntity(associationRef2.getSourceRef()))) {
									// TODO Test same parent
									delete = false;
									break;
								}
							}
							if (delete) {
								iterator.remove();
							}
						}
					}
				}

				appendAssocs(ret, associationRefs, depthLevel, maxDepthLevel, associationName, filter, parentNodeRefs);

			}
		}

		return ret;
	}

	private void appendAssocs(MultiLevelListData ret, Set<AssociationRef> associationRefs, int depthLevel, int maxDepthLevel, QName associationName,
			WUsedFilter filter, Set<NodeRef> parentNodeRefs) {
		for (AssociationRef associationRef : associationRefs) {

			NodeRef nodeRef = associationRef.getSourceRef();

			// we display nodes that are in workspace
			if (nodeRef != null && nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)
					&& permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
				NodeRef rootNodeRef = getEntity(nodeRef);

				if (rootNodeRef != null) {

					// we don't display history version and simulation entities
					if (!nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
							&& permissionService.hasReadPermission(rootNodeRef) == AccessStatus.ALLOWED) {

						Set<NodeRef> curVisitedNodeRef = new HashSet<>(parentNodeRefs);

						MultiLevelListData multiLevelListData;
						// next level
						if (maxDepthLevel < 0 || depthLevel + 1 < maxDepthLevel) {
							multiLevelListData = getWUsedEntity(Collections.singletonList(rootNodeRef), WUsedOperator.AND, filter, associationName,
									depthLevel + 1, maxDepthLevel, curVisitedNodeRef);
						} else {
							multiLevelListData = new MultiLevelListData(rootNodeRef, depthLevel + 1);
						}
						ret.getTree().put(nodeRef, multiLevelListData);
					}
				}
			}
		}

		if (filter != null) {
			filter.filter(ret);
		}

	}

	private NodeRef getEntity(NodeRef sourceRef) {
		if (entityDictionaryService.isSubClass(nodeService.getType(sourceRef), BeCPGModel.TYPE_ENTITY_V2)) {
			return sourceRef;
		}
		return entityListDAO.getEntity(sourceRef);
	}

}
