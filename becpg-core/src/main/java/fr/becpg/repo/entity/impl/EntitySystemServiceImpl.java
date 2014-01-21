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
package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * 
 * @author matthieu
 * 
 */
public class EntitySystemServiceImpl implements EntitySystemService {

	private static final String XPATH = "./%s:%s";

	private EntityListDAO entityListDAO;

	private BehaviourFilter policyBehaviourFilter;

	private NodeService nodeService;

	private BeCPGSearchService beCPGSearchService;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	public NodeRef createSystemEntity(NodeRef parentNodeRef, String entityPath, Map<String, QName> entitySystemDataLists) {

		try {

			// disable policy in order to have getTranslatedPath in cm:name
			policyBehaviourFilter.disableBehaviour(DataListModel.TYPE_DATALIST);

			String entityName = TranslateHelper.getTranslatedPath(entityPath);
			if (entityName == null) {
				entityName = entityPath;
			}

			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, entityName);

			NodeRef entityNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, entityName);

			if (entityNodeRef == null) {
				entityNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(entityPath)), BeCPGModel.TYPE_SYSTEM_ENTITY, properties).getChildRef();
			}

			// entityLists
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
			if (listContainerNodeRef == null) {
				listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
			}

			if (entitySystemDataLists != null) {
				for (Map.Entry<String, QName> entityList : entitySystemDataLists.entrySet()) {

					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, entityList.getKey());
					if (listNodeRef == null) {
						entityListDAO.createList(listContainerNodeRef, entityList.getKey(), entityList.getValue());
					}
				}
			}

			return entityNodeRef;

		} finally {
			policyBehaviourFilter.enableBehaviour(DataListModel.TYPE_DATALIST);
		}
	}

	@Override
	public NodeRef getSystemEntity(NodeRef parentNodeRef, String systemEntityPath) {
		String xPath = systemEntityPath.contains(RepoConsts.MODEL_PREFIX_SEPARATOR) ? systemEntityPath : String.format(XPATH, NamespaceService.CONTENT_MODEL_PREFIX,
				ISO9075.encode(systemEntityPath));

		List<NodeRef> nodes = beCPGSearchService.searchByPath(parentNodeRef, xPath);

		if (!nodes.isEmpty()) {
			return nodes.get(0);
		}

		return null;
	}

	@Override
	public NodeRef getSystemEntityDataList(NodeRef systemEntityNodeRef, String dataListPath) {
		String entityName = TranslateHelper.getTranslatedPath(dataListPath);
		if (entityName == null) {
			entityName = dataListPath;
		}		
		return entityListDAO.getList(entityListDAO.getListContainer(systemEntityNodeRef), dataListPath);
	}

	@Override
	public NodeRef getSystemEntityDataList(NodeRef parentNodeRef, String systemEntityPath, String dataListPath) {
		return getSystemEntityDataList(getSystemEntity(parentNodeRef, systemEntityPath), dataListPath);
	}

	@Override
	public List<NodeRef> getSystemEntities() {
		String searchQuery = "+TYPE:\"" + BeCPGModel.TYPE_SYSTEM_ENTITY + "\""
				+ " -@cm\\:lockType:READ_ONLY_LOCK"
				+ " -ASPECT:\"bcpg:compositeVersion\"";
		return beCPGSearchService.luceneSearch(searchQuery);
	}

	@Override
	public List<NodeRef> getSystemFolders() {
		String searchQuery = "+TYPE:\"" + ContentModel.TYPE_FOLDER + "\" +ASPECT:\""+BeCPGModel.ASPECT_SYSTEM_FOLDER+"\"";
		return beCPGSearchService.luceneSearch(searchQuery);
	}

}
