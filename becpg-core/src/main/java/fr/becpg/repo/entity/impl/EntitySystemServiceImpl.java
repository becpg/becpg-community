/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 *
 * @author matthieu
 *
 */
@Service("entitySystemService")
public class EntitySystemServiceImpl implements EntitySystemService {

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private NodeService nodeService;

	@Override
	public NodeRef createSystemEntity(NodeRef parentNodeRef, String entityPath, Map<String, QName> entitySystemDataLists) {

		try {

			// disable policy in order to have getTranslatedPath in cm:name
			policyBehaviourFilter.disableBehaviour(DataListModel.TYPE_DATALIST);

			MLText translatedPathMLText = TranslateHelper.getTranslatedPathMLText(entityPath);

			String entityName = TranslateHelper.getTranslatedPath(entityPath);
			if (entityName == null) {
				entityName = entityPath;
			}

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, entityName);
			properties.put(ContentModel.PROP_TITLE, translatedPathMLText);

			NodeRef entityNodeRef = null;
			List<NodeRef> matchingEntities = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_SYSTEM_ENTITY)
					.andPropEquals(ContentModel.PROP_TITLE, translatedPathMLText.getDefaultValue()).inDB().list();

			if (!matchingEntities.isEmpty()) {
				entityNodeRef = matchingEntities.get(0);
			}

			if (entityNodeRef == null) {
				entityNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(entityPath)),
						BeCPGModel.TYPE_SYSTEM_ENTITY, properties).getChildRef();
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
						MLText entityListTranslated = TranslateHelper.getTranslatedPathMLText(entityList.getKey());
						NodeRef newDataList = entityListDAO.createList(listContainerNodeRef, entityList.getKey(), entityList.getValue());
						nodeService.setProperty(newDataList, ContentModel.PROP_TITLE, entityListTranslated);

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
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(parentNodeRef, AbstractBeCPGQueryBuilder.encodePath(systemEntityPath));
	}

	@Override
	public NodeRef getSystemEntityDataList(NodeRef systemEntityNodeRef, String dataListPath) {
		String entityName = TranslateHelper.getTranslatedPath(dataListPath);
		if (entityName == null) {
		}
		return entityListDAO.getList(entityListDAO.getListContainer(systemEntityNodeRef), dataListPath);
	}

	@Override
	public NodeRef getSystemEntityDataList(NodeRef parentNodeRef, String systemEntityPath, String dataListPath) {
		return getSystemEntityDataList(getSystemEntity(parentNodeRef, systemEntityPath), dataListPath);
	}

	@Override
	public List<NodeRef> getSystemEntities() {

		List<NodeRef> tplsNodeRef = new LinkedList<>();

		for (NodeRef tpl : BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_SYSTEM_ENTITY).inDB().list()) {
			if (!nodeService.hasAspect(tpl, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
				tplsNodeRef.add(tpl);
			}
		}
		return tplsNodeRef;

	}

	@Override
	public List<NodeRef> getSystemFolders() {
		return BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_FOLDER).withAspect(BeCPGModel.ASPECT_SYSTEM_FOLDER).inDB().list();
	}

}
