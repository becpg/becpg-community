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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

@Service("wUsedListService")
public class WUsedListServiceImpl implements WUsedListService {

	private static Log logger = LogFactory.getLog(WUsedListServiceImpl.class);

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
		return getWUsedEntity(Arrays.asList(entityNodeRef), WUsedOperator.AND, associationName, 0, maxDepthLevel);
	}

	@Override
	public MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, WUsedOperator operator, QName associationName, int maxDepthLevel) {
		return getWUsedEntity(entityNodeRefs,operator,  associationName, 0, maxDepthLevel);
	}

	private MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs,WUsedOperator operator, QName associationName, int depthLevel, int maxDepthLevel) {

		MultiLevelListData ret = new MultiLevelListData(entityNodeRefs, depthLevel);

		Set<AssociationRef> associationRefs = new HashSet<>();
		boolean first = true;
		for (NodeRef entityNodeRef : entityNodeRefs) {
			if (first  || WUsedOperator.OR.equals(operator) ) {
				associationRefs.addAll(nodeService.getSourceAssocs(entityNodeRef, associationName));
				if(logger.isDebugEnabled()){
					logger.debug("associationRefs size" + associationRefs.size()+ "  for entityNodeRef "+entityNodeRef+" and assocs"+associationName);
				}
				first = false;
			} else {
				
				//Test for join
				for (Iterator<AssociationRef> iterator = associationRefs.iterator(); iterator.hasNext();) {
					AssociationRef associationRef = (AssociationRef) iterator.next();
					boolean delete = true;
					for (AssociationRef associationRef2 : nodeService.getSourceAssocs(entityNodeRef, associationName)) {
						//Test that assoc is also include in product
						if (getEntity(associationRef.getSourceRef()).equals(getEntity(associationRef2.getSourceRef()))) {
							// TODO Test same parent
				
							delete = false;
							break;
						}
					}
					if(delete){
						iterator.remove();
					}
				}
			}
		}

		for (AssociationRef associationRef : associationRefs) {

			NodeRef nodeRef = associationRef.getSourceRef();

			// we display nodes that are in workspace
			if (nodeRef != null && nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE) && permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
				NodeRef rootNodeRef = getEntity(nodeRef);

				// we don't display history version and simulation entities
				if (!nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION) && permissionService.hasReadPermission(rootNodeRef) == AccessStatus.ALLOWED) {

					MultiLevelListData multiLevelListData = null;
					// next level
					if (maxDepthLevel < 0 || depthLevel + 1 < maxDepthLevel) {
						multiLevelListData = getWUsedEntity(Arrays.asList(rootNodeRef),operator, associationName, depthLevel + 1, maxDepthLevel);
					} else {
						multiLevelListData = new MultiLevelListData(rootNodeRef, depthLevel + 1);
					}
					ret.getTree().put(nodeRef, multiLevelListData);
				}
			}
		}
		return ret;
	}

	private NodeRef getEntity(NodeRef sourceRef) {
		if(entityDictionaryService.isSubClass(nodeService.getType(sourceRef), BeCPGModel.TYPE_ENTITY_V2)){
			return sourceRef;
		} 
		return entityListDAO.getEntity(sourceRef);
	}



}
