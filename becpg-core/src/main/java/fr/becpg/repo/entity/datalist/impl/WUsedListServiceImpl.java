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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import fr.becpg.repo.entity.datalist.WUsedFilter;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.impl.EntitySourceAssoc;

/**
 * <p>WUsedListServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("wUsedListService")
public class WUsedListServiceImpl implements WUsedListService {

	private static final Log logger = LogFactory.getLog(WUsedListServiceImpl.class);

	private static final int MAX_LEVEL = 50;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	public MultiLevelListData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int maxDepthLevel) {
		return getWUsedEntity(Collections.singletonList(entityNodeRef), WUsedOperator.AND, null, associationName, 0, maxDepthLevel, new HashSet<>(),
				new HashMap<>());
	}

	/** {@inheritDoc} */
	@Override
	public MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, WUsedFilter filter, QName associationName,
			int maxDepthLevel) {
		return getWUsedEntity(entityNodeRefs, operator, filter, associationName, 0, maxDepthLevel, new HashSet<>(), new HashMap<>());
	}

	/** {@inheritDoc} */
	@Override
	public MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, QName associationName, int maxDepthLevel) {
		return getWUsedEntity(entityNodeRefs, operator, null, associationName, 0, maxDepthLevel, new HashSet<>(), new HashMap<>());
	}

	private MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, WUsedFilter filter, QName associationName,
			int depthLevel, int maxDepthLevel, Set<NodeRef> parentNodeRefs, Map<NodeRef, Boolean> permCache) {

		if (maxDepthLevel == -1) {
			// Avoid infinite loop
			maxDepthLevel = MAX_LEVEL;
		}

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		MultiLevelListData ret = new MultiLevelListData(entityNodeRefs, depthLevel);

		if ((entityNodeRefs != null) && !entityNodeRefs.isEmpty() && !parentNodeRefs.contains(entityNodeRefs.get(0))) {
			parentNodeRefs.addAll(entityNodeRefs);

			appendAssocs(ret, associationService.getEntitySourceAssocs(entityNodeRefs, associationName, null, WUsedOperator.OR.equals(operator), null),
					depthLevel, maxDepthLevel, associationName, filter, parentNodeRefs, permCache, WUsedOperator.OR.equals(operator));

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("Append wused assoc takes " + watch.getTotalTimeSeconds() + " seconds");
		}

		return ret;
	}

	private void appendAssocs(MultiLevelListData ret, List<EntitySourceAssoc> associationRefs, int depthLevel, int maxDepthLevel, QName associationName,
			WUsedFilter filter, Set<NodeRef> parentNodeRefs, Map<NodeRef, Boolean> permCache, boolean isOrOperator) {

		Map<NodeRef, MultiLevelListData> tmp = new HashMap<>();

		if (isOrOperator) {

			for (NodeRef entityNodeRef : parentNodeRefs) {
				tmp.put(entityNodeRef, new MultiLevelListData(entityNodeRef, depthLevel + 1));
			}

			depthLevel++;
		}

		for (EntitySourceAssoc associationRef : associationRefs) {

			NodeRef nodeRef = associationRef.getDataListItemNodeRef();

			NodeRef rootNodeRef = associationRef.getEntityNodeRef();

			Boolean visible = permCache.get(rootNodeRef);

			if (visible == null) {
				visible = nodeService.exists(rootNodeRef) && (permissionService.hasReadPermission(rootNodeRef) == AccessStatus.ALLOWED);

				permCache.put(rootNodeRef, visible);
			}

			if (visible) {

				Set<NodeRef> curVisitedNodeRef = new HashSet<>(parentNodeRefs);

				MultiLevelListData multiLevelListData;
				// next level
				if ((maxDepthLevel < 0) || ((depthLevel + 1) < maxDepthLevel)) {
					multiLevelListData = getWUsedEntity(Collections.singletonList(rootNodeRef), WUsedOperator.AND, filter, associationName,
							depthLevel + 1, maxDepthLevel, curVisitedNodeRef, permCache);
				} else {
					multiLevelListData = new MultiLevelListData(rootNodeRef, depthLevel + 1);
				}
				if (tmp.containsKey(associationRef.getSourceNodeRef())) {
					tmp.get(associationRef.getSourceNodeRef()).getTree().put(nodeRef, multiLevelListData);
				} else {
					ret.getTree().put(nodeRef, multiLevelListData);
				}
			}

		}

		if (isOrOperator) {
			for (NodeRef entityNodeRef : parentNodeRefs) {
				MultiLevelListData entityLevel = tmp.get(entityNodeRef);
				if (filter != null) {
					filter.filter(entityLevel);
				}
				if (!entityLevel.getTree().isEmpty()) {
					ret.getTree().put(entityNodeRef, entityLevel);
				}
			}

		} else {
			if (filter != null) {
				filter.filter(ret);
			}
		}

	}

}
