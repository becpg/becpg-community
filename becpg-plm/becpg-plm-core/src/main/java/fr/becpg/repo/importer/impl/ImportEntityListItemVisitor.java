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
package fr.becpg.repo.importer.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;

public class ImportEntityListItemVisitor extends AbstractImportVisitor implements ImportVisitor {

	protected static final String MSG_ERROR_FIND_ENTITY = "import_service.error.err_find_entity";

	protected static final String MSG_ERROR_NO_MAPPING_FOR = "import_service.error.no_mapping_for";

	/** The logger. */
	private static final Log logger = LogFactory.getLog(ImportEntityListItemVisitor.class);

	private FileFolderService fileFolderService;

	private AssociationService associationService;

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * Check if the node exists, according to : - keys or entityCode
	 * 
	 * @param importContext
	 *            the import context
	 * @param type
	 *            the type
	 * @param properties
	 *            the properties
	 * @return the node ref
	 * @throws InvalidTargetNodeException
	 */
	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException {

		ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());

		if (classMapping == null) {
			throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_NO_MAPPING_FOR, importContext.getType()));

		}

		importContext.setEntityNodeRef(null);

		/*
		 * Look for entity
		 */

		Map<QName, Serializable> propValues = getNodePropertiesToImport(importContext, values);
		Map<QName, Serializable> entityProperties = new HashMap<>();

		// calculate entity properties
		for (QName qName : classMapping.getNodeColumnKeys()) {
			entityProperties.put(qName, propValues.get(qName));
		}

		QName entityType = importContext.getEntityType() != null ? importContext.getEntityType() : PLMModel.TYPE_PRODUCT;
		NodeRef entityNodeRef = findNodeByKeyOrCode(importContext, entityType, entityProperties);

		if (entityNodeRef == null) {
			throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_FIND_ENTITY, values));
		}

		importContext.setEntityNodeRef(entityNodeRef);

		/*
		 * Look for entityList
		 */

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
		}

		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, importContext.getType());

		if (listNodeRef == null) {
			listNodeRef = entityListDAO.createList(listContainerNodeRef, importContext.getType());
		} else if (importContext.isDeleteDataList(entityNodeRef)) {
			List<NodeRef> dataListItems = entityListDAO.getListItems(listNodeRef, importContext.getType());
			for (NodeRef dataListItem : dataListItems) {
				nodeService.deleteNode(dataListItem);
			}
		}

		/*
		 * Look for entityListItem
		 */

		NodeRef entityListItemNodeRef = null;
		Map<QName, String> dataListColumnsProps = new HashMap<>();
		Map<QName, List<NodeRef>> dataListColumnsAssocs = new HashMap<>();

		if (!classMapping.getDataListColumnKeys().isEmpty()) {

			for (QName qName : classMapping.getDataListColumnKeys()) {

				int z_idx = -1, i = 0;
				for (AbstractAttributeMapping a : importContext.getColumns()) {

					if (qName.isMatch(a.getAttribute().getName())) {
						z_idx = i;
					}
					i++;
				}

				String value = values.get(z_idx);
				PropertyDefinition propertyDef = dictionaryService.getProperty(qName);

				if (propertyDef instanceof PropertyDefinition) {
					dataListColumnsProps.put(qName, value);
				} else {

					AssociationDefinition associationDef = dictionaryService.getAssociation(qName);
					List<NodeRef> targetRefs = findTargetNodesByValue(importContext, associationDef.isTargetMany(), associationDef.getTargetClass()
							.getName(), value);
					dataListColumnsAssocs.put(qName, targetRefs);
				}
			}

			entityListItemNodeRef = findEntityListItem(listNodeRef, dataListColumnsProps, dataListColumnsAssocs);
		}

		/*
		 * Create or update entity list item
		 */

		// Read again variant
		propValues = getNodePropertiesToImport(importContext, values);

		Map<QName, Serializable> entityListItemProperties = new HashMap<>();

		// calculate entity list properties (there are not entity properties)
		for (QName qName : propValues.keySet()) {

			if (!entityProperties.containsKey(qName)) {
				entityListItemProperties.put(qName, propValues.get(qName));
			}
		}

		if (entityListItemNodeRef == null) {
			logger.debug("create entity list item. Properties: " + entityListItemProperties);

			entityListItemNodeRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN,
					importContext.getType(), ImportHelper.cleanProperties(entityListItemProperties)).getChildRef();

		} else if (importContext.isDoUpdate()) {

			logger.debug("update entity list item. Properties: " + entityListItemProperties);
			nodeService.setType(entityListItemNodeRef, importContext.getType());

			for (Map.Entry<QName, Serializable> entry : entityListItemProperties.entrySet()) {
				if (entry.getValue() != null && ImportHelper.NULL_VALUE.equals(entry.getValue())) {
					logger.debug("Remove property: " + entry.getKey());
					nodeService.removeProperty(entityListItemNodeRef, entry.getKey());
				} else {
					nodeService.setProperty(entityListItemNodeRef, entry.getKey(), entry.getValue());
				}
			}
		} else {
			logger.debug("Update mode is not enabled so no update is done.");
		}

		// import associations
		importAssociations(importContext, values, entityListItemNodeRef);

		return entityListItemNodeRef;
	}

	/**
	 * Look for the entity list item (check props and assocs match)
	 * 
	 * @param listNodeRef
	 * @param dataListColumnsProps
	 * @param dataListColumnsAssocs
	 * @return
	 */
	private NodeRef findEntityListItem(NodeRef listNodeRef, Map<QName, String> dataListColumnsProps, Map<QName, List<NodeRef>> dataListColumnsAssocs) {

		List<FileInfo> nodes = fileFolderService.list(listNodeRef);
		NodeRef nodeRef = null;
		boolean isFound = true;

		for (FileInfo node : nodes) {

			isFound = true;
			nodeRef = node.getNodeRef();

			// check properties match
			for (Map.Entry<QName, String> dataListColumnProps : dataListColumnsProps.entrySet()) {

				Serializable s = nodeService.getProperty(nodeRef, dataListColumnProps.getKey());

				if (dataListColumnProps.getValue() == null) {
					if (s != null) {
						isFound = false;
						break;
					}
				} else if (!dataListColumnProps.getValue().equals(s)) {
					isFound = false;
					break;
				}
			}

			// check associations match
			for (Map.Entry<QName, List<NodeRef>> dataListColumnAssocs : dataListColumnsAssocs.entrySet()) {

				List<NodeRef> targetRefs1 = dataListColumnAssocs.getValue();
				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, dataListColumnAssocs.getKey());
				List<NodeRef> targetRefs2 = new ArrayList<>();
				for (AssociationRef assocRef : assocRefs)
					targetRefs2.add(assocRef.getTargetRef());

				if (targetRefs1 == null) {
					if (targetRefs2 != null) {
						isFound = false;
						break;
					}
				} else if (!targetRefs1.equals(targetRefs2)) {
					isFound = false;
					break;
				}
			}

			// OK, we found it
			if (isFound) {
				break;
			}
		}

		return isFound ? nodeRef : null;
	}

	@Override
	protected NodeRef findPropertyTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef,
			AbstractAttributeMapping attributeMapping, String value, Map<QName, Serializable> properties) throws ImporterException {

		if (PLMModel.PROP_VARIANTIDS.equals(propDef.getName())) {
			NodeRef entityNodeRef = importContext.getEntityNodeRef();
			if (entityNodeRef != null && value != null && !value.isEmpty()) {
				List<NodeRef> variants = associationService.getChildAssocs(entityNodeRef, PLMModel.ASSOC_VARIANTS);
				boolean isDefault = false;
				String name = value;
				if (value.contains("|")) {
					name = value.split("\\|")[0];
					isDefault = Boolean.parseBoolean(value.split("\\|")[1]);
				}

				for (NodeRef variant : variants) {
					if (nodeService.getProperty(variant, ContentModel.PROP_NAME).equals(name)) {
						logger.debug("Find variant for name : " + name);
						return variant;
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Create variant : " + name);
				}

				Map<QName, Serializable> props = new HashMap<>();
				props.put(ContentModel.PROP_NAME, name);
				props.put(PLMModel.PROP_IS_DEFAULT_VARIANT, isDefault);

				return nodeService.createNode(entityNodeRef, PLMModel.ASSOC_VARIANTS, PLMModel.ASSOC_VARIANTS, PLMModel.TYPE_VARIANT, props)
						.getChildRef();

			} else {
				logger.debug("EntityNodeRef not yet set in importContext");
				return null;
			}

		} else if (BeCPGModel.PROP_PARENT_LEVEL.equals(propDef.getName())) {
			NodeRef entityNodeRef = importContext.getEntityNodeRef();
			if (entityNodeRef != null && value != null && !value.isEmpty()) {

				Map<QName, String> dataListColumnsProps = new HashMap<>();
				Map<QName, List<NodeRef>> dataListColumnsAssocs = new HashMap<>();

				ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());

				if (!classMapping.getDataListColumnKeys().isEmpty()) {

					QName qName = classMapping.getDataListColumnKeys().get(0);

					PropertyDefinition propertyDef = dictionaryService.getProperty(qName);

					if (propertyDef instanceof PropertyDefinition) {
						dataListColumnsProps.put(qName, value);
					} else {

						AssociationDefinition associationDef = dictionaryService.getAssociation(qName);
						List<NodeRef> targetRefs = findTargetNodesByValue(importContext, associationDef.isSourceMany(), associationDef
								.getTargetClass().getName(), value);
						dataListColumnsAssocs.put(qName, targetRefs);
					}
					
					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					if (listContainerNodeRef == null) {
						listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
					}

					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, importContext.getType());

					return findEntityListItem(listNodeRef, dataListColumnsProps, dataListColumnsAssocs);
				}

			} else {
				logger.debug("EntityNodeRef not yet set in importContext");
				return null;
			}

		}

		return super.findPropertyTargetNodeByValue(importContext, propDef, attributeMapping, value, properties);
	}

}
