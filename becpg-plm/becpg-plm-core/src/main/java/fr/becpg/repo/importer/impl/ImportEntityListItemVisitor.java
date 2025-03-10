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
package fr.becpg.repo.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;

/**
 * <p>ImportEntityListItemVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ImportEntityListItemVisitor extends AbstractImportVisitor implements ImportVisitor {

	/** Constant <code>MSG_ERROR_FIND_ENTITY="import_service.error.err_find_entity"</code> */
	protected static final String MSG_ERROR_FIND_ENTITY = "import_service.error.err_find_entity";

	/** Constant <code>MSG_ERROR_NO_MAPPING_FOR="import_service.error.no_mapping_for"</code> */
	protected static final String MSG_ERROR_NO_MAPPING_FOR = "import_service.error.no_mapping_for";
	
	private CommentService commentService;

	private static final Log logger = LogFactory.getLog(ImportEntityListItemVisitor.class);

	private FileFolderService fileFolderService;
	
	/**
	 * <p>Setter for the field <code>commentService</code>.</p>
	 *
	 * @param commentService a {@link org.alfresco.repo.forum.CommentService} object.
	 */
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	/**
	 * <p>Setter for the field <code>fileFolderService</code>.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object.
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Check if the node exists, according to : - keys or entityCode
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
		NodeRef entityNodeRef = findNodeByKeyOrCode(importContext, null, entityType, entityProperties, null, true);

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

		QName listType = importContext.getListType() != null ? importContext.getListType() : importContext.getType();

		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, listType);

		if (listNodeRef == null) {
			listNodeRef = entityListDAO.createList(listContainerNodeRef, listType);
		} else if (importContext.isDeleteDataList(entityNodeRef)) {
			List<NodeRef> dataListItems = entityListDAO.getListItems(listNodeRef, listType);
			for (NodeRef dataListItem : dataListItems) {
				nodeService.addAspect(dataListItem, ContentModel.ASPECT_TEMPORARY, null);
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
						break;
					}
					i++;
				}

				String value = null;
				if( z_idx < values.size()	) {
				   value = values.get(z_idx);
				}
				
				PropertyDefinition propertyDef = entityDictionaryService.getProperty(qName);

				if (propertyDef instanceof PropertyDefinition) {
					dataListColumnsProps.put(qName, value);
				} else if(value!=null){

					AssociationDefinition associationDef = entityDictionaryService.getAssociation(qName);
					List<NodeRef> targetRefs = findTargetNodesByValue(importContext, associationDef.isTargetMany(),
							associationDef.getTargetClass().getName(), value, qName);
					dataListColumnsAssocs.put(qName, targetRefs);
				}
			}

			entityListItemNodeRef = findEntityListItem(importContext, listNodeRef, dataListColumnsProps, dataListColumnsAssocs);
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
			if (entityDictionaryService.isSubClass(importContext.getType(), nodeService.getType(entityListItemNodeRef))) {
				nodeService.setType(entityListItemNodeRef, importContext.getType());
			}

			for (Map.Entry<QName, Serializable> entry : entityListItemProperties.entrySet()) {
				if ((entry.getValue() != null) && ImportHelper.NULL_VALUE.equals(entry.getValue())) {
					logger.debug("Remove property: " + entry.getKey());
					nodeService.removeProperty(entityListItemNodeRef, entry.getKey());
				} else {

					if ((entry.getValue() instanceof MLText)) {
						boolean mlAware = MLPropertyInterceptor.isMLAware();
						try {
							MLPropertyInterceptor.setMLAware(true);
							nodeService.setProperty(entityListItemNodeRef, entry.getKey(), ImportHelper.mergeMLText((MLText) entry.getValue(),
									(MLText) nodeService.getProperty(entityListItemNodeRef, entry.getKey())));
						} finally {
							MLPropertyInterceptor.setMLAware(mlAware);
						}
					} else if (ContentModel.PROP_CONTENT.equals(entry.getKey())) {

						try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
							oos.writeObject(entry.getValue());
							InputStream in = new ByteArrayInputStream(baos.toByteArray());

							String mimetype = mimetypeService.guessMimetype(entry.getValue() != null ? entry.getValue().toString() : null);
							ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
							Charset charset = charsetFinder.getCharset(in, mimetype);
							String encoding = charset.name();

							ContentWriter writer = contentService.getWriter(entityListItemNodeRef, entry.getKey(), true);
							writer.setEncoding(encoding);
							writer.setMimetype(mimetype);
							writer.putContent(in);
						} catch (IOException e) {
							throw new ImporterException(e.getMessage());
						}

					} else {
						nodeService.setProperty(entityListItemNodeRef, entry.getKey(), entry.getValue());
					}

				}

			}
		} else {
			logger.debug("Update mode is not enabled so no update is done.");
		}

		// import associations
		importAssociations(importContext, values, entityListItemNodeRef);
		
		MLText comment = (MLText) propValues.get(PLMModel.PROP_PRODUCT_COMMENTS);
		logger.debug("Import comments :"+comment +" for product :"+entityListItemNodeRef);
		if(comment!=null){
			commentService.createComment(entityListItemNodeRef, "", comment.getDefaultValue(), false);
		}

		return entityListItemNodeRef;
	}

	/**
	 * Look for the entity list item (check props and assocs match)
	 */
	private NodeRef findEntityListItem(ImportContext importContext, NodeRef listNodeRef, Map<QName, String> dataListColumnsProps,
			Map<QName, List<NodeRef>> dataListColumnsAssocs) {

		List<FileInfo> nodes = fileFolderService.list(listNodeRef);
		NodeRef nodeRef = null;
		boolean isFound = true;

		for (FileInfo node : nodes) {

			isFound = true;
			nodeRef = node.getNodeRef();

			// check properties match
			for (Map.Entry<QName, String> dataListColumnProps : dataListColumnsProps.entrySet()) {

				Serializable s = nodeService.getProperty(nodeRef, dataListColumnProps.getKey());

				String value = dataListColumnProps.getValue();

				if (BeCPGModel.PROP_VARIANTIDS.equals(dataListColumnProps.getKey())) {
					NodeRef variantNodeRef = getOrCreateVariant(importContext, value, false);
					if (variantNodeRef != null) {
						List<NodeRef> ret = new ArrayList<>();
						ret.add(variantNodeRef);
						value = ret.toString();
					}
				}

				if (value == null) {
					if (s != null) {
						isFound = false;
						break;
					}
				} else if ((s == null) || !value.equals(s.toString())) {
					isFound = false;
					break;
				}
			}

			// check associations match
			for (Map.Entry<QName, List<NodeRef>> dataListColumnAssocs : dataListColumnsAssocs.entrySet()) {

				List<NodeRef> targetRefs1 = dataListColumnAssocs.getValue();
				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, dataListColumnAssocs.getKey());
				List<NodeRef> targetRefs2 = new ArrayList<>();
				for (AssociationRef assocRef : assocRefs) {
					targetRefs2.add(assocRef.getTargetRef());
				}

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

	private NodeRef getOrCreateVariant(ImportContext importContext, String value, boolean shouldCreate) {

		NodeRef entityNodeRef = importContext.getEntityNodeRef();
		if ((entityNodeRef != null) && (value != null) && !value.isEmpty()) {
			List<NodeRef> variants = new ArrayList<>(associationService.getChildAssocs(entityNodeRef, BeCPGModel.ASSOC_VARIANTS));
			NodeRef entityTplNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
			if (entityTplNodeRef != null) {
				List<NodeRef> entityTplVariants = associationService.getChildAssocs(entityTplNodeRef, BeCPGModel.ASSOC_VARIANTS);
				if ((entityTplVariants != null) && !entityTplVariants.isEmpty()) {
					variants.addAll(entityTplVariants);
				}
			}

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
			if (shouldCreate) {
				if (logger.isDebugEnabled()) {
					logger.debug("Create variant : " + name);
				}

				Map<QName, Serializable> props = new HashMap<>();
				props.put(ContentModel.PROP_NAME, name);
				props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, isDefault);

				return nodeService.createNode(entityNodeRef, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props)
						.getChildRef();
			}
			return null;

		} else {
			logger.debug("EntityNodeRef not yet set in importContext");
			return null;
		}

	}

	/** {@inheritDoc} */
	@Override
	protected NodeRef findPropertyTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef,
			AbstractAttributeMapping attributeMapping, String value, Map<QName, Serializable> properties) throws ImporterException {

		if (BeCPGModel.PROP_VARIANTIDS.equals(propDef.getName())) {
			return getOrCreateVariant(importContext, value, true);

		} else if (BeCPGModel.PROP_PARENT_LEVEL.equals(propDef.getName())) {
			NodeRef entityNodeRef = importContext.getEntityNodeRef();
			if ((entityNodeRef != null) && (value != null) && !value.isEmpty()) {

				Map<QName, String> dataListColumnsProps = new HashMap<>();
				Map<QName, List<NodeRef>> dataListColumnsAssocs = new HashMap<>();

				ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());

				if (!classMapping.getDataListColumnKeys().isEmpty()) {

					QName qName = classMapping.getDataListColumnKeys().get(0);

					PropertyDefinition propertyDef = entityDictionaryService.getProperty(qName);

					if (propertyDef instanceof PropertyDefinition) {
						dataListColumnsProps.put(qName, value);
					} else {

						AssociationDefinition associationDef = entityDictionaryService.getAssociation(qName);
						List<NodeRef> targetRefs = findTargetNodesByValue(importContext, associationDef.isSourceMany(),
								associationDef.getTargetClass().getName(), value, null);
						dataListColumnsAssocs.put(qName, targetRefs);
					}

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					if (listContainerNodeRef == null) {
						listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
					}

					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, importContext.getType());

					return findEntityListItem(importContext, listNodeRef, dataListColumnsProps, dataListColumnsAssocs);
				}

			} else {
				logger.debug("EntityNodeRef not yet set in importContext");
				return null;
			}

		}

		return super.findPropertyTargetNodeByValue(importContext, propDef, attributeMapping, value, properties);
	}

}
