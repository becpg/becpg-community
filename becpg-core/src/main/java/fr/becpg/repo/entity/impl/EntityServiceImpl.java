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
package fr.becpg.repo.entity.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.EntityListState;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Entity Service implementation
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("entityService")
public class EntityServiceImpl implements EntityService {

	private static final Integer MAX_DEPTH_LEVEL = 6;

	private static final String ICON_NAME = "generic-%s-%s.png";
	private static final String ICON_NAME_TEMPLATE = "generic-%s-%s-%s.png";

	private static final String ENTITY_ICONS_CACHE_KEY = "entityIcons";

	private static final Pattern ENTITY_ICONS_PATTERN = Pattern.compile("generic-.*\\.png");

	private static final Log logger = LogFactory.getLog(EntityServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private MimetypeService mimetypeService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private CopyService copyService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;
	
	@Autowired
	private AssociationService associationService;

	private static final Set<QName> IGNORE_PARENT_ASSOC_TYPES = new HashSet<>(7);
	static {
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_MEMBER);
		IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_IN_ZONE);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Load an image in the folder Images.
	 */
	@Override
	public NodeRef getImage(NodeRef nodeRef, String imgName) {

		NodeRef imagesFolderNodeRef = getImageFolder(nodeRef);

		NodeRef imageNodeRef = null;
		List<FileInfo> files = fileFolderService.listFiles(imagesFolderNodeRef);
		for (FileInfo file : files) {
			if (file.getName().toLowerCase().startsWith(imgName.toLowerCase())) {
				imageNodeRef = file.getNodeRef();
			}
		}

		if (imageNodeRef == null) {
			throw new BeCPGException("image not found. imgName: " + imgName);
		}

		return imageNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntityDefaultIcon(NodeRef entityNodeRef, String imageResolution) {

		Map<String, NodeRef> icons = getEntityIcons();
		QName type = nodeService.getType(entityNodeRef);

		if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
			String iconName = String.format(ICON_NAME_TEMPLATE, type.getLocalName(), entityNodeRef.getId(), imageResolution);
			if (icons.containsKey(iconName)) {
				return icons.get(iconName);
			}
		}

		// Try to find a logo for the specific type
		List<AssociationRef> entityTplAssocs = nodeService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);

		if (!entityTplAssocs.isEmpty()) {
			NodeRef entityTplNodeRef = entityTplAssocs.get(0).getTargetRef();
			String iconName = String.format(ICON_NAME_TEMPLATE, type.getLocalName(), entityTplNodeRef.getId(), imageResolution);
			if (icons.containsKey(iconName)) {
				return icons.get(iconName);
			}
		}

		String iconName = String.format(ICON_NAME, type.getLocalName(), imageResolution);

		if (icons.containsKey(iconName)) {
			return icons.get(iconName);
		}

		iconName = String.format(ICON_NAME, "entity", imageResolution);

		if (icons.containsKey(iconName)) {
			return icons.get(iconName);
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, NodeRef> getEntityIcons() {
		return beCPGCacheService.getFromCache(EntityServiceImpl.class.getName(), ENTITY_ICONS_CACHE_KEY, () -> {
			return AuthenticationUtil.runAsSystem(() -> {

				Map<String, NodeRef> ret = new HashMap<>();
				NodeRef iconsSystemFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
						"/app:company_home" + RepoConsts.FULL_PATH_THUMBNAIL);
				if (iconsSystemFolder != null) {

					List<FileInfo> files = fileFolderService.listFiles(iconsSystemFolder);
					for (FileInfo file : files) {

						// Check if the input matches either pattern
						Matcher matcher1 = ENTITY_ICONS_PATTERN.matcher(file.getName());

						if (matcher1.matches()) {
							ret.put(file.getName(), file.getNodeRef());
						}
					}
				} else {
					logger.warn(" Icon Folder doesn't exist.");
				}
				return ret;
			});
		});
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getImages(NodeRef nodeRef) {

		NodeRef imagesFolderNodeRef = getImageFolder(nodeRef);

		List<NodeRef> ret = new ArrayList<>();

		List<FileInfo> files = fileFolderService.listFiles(imagesFolderNodeRef);
		for (FileInfo file : files) {
			ret.add(file.getNodeRef());
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getImageFolder(NodeRef nodeRef) {

		NodeRef imagesFolderNodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef == null) {
			throw new BeCPGException("Folder 'Images' doesn't exist.");
		}
		return imagesFolderNodeRef;
	}

	// TODO Refactor and avoid duplicate
	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateImageFolder(NodeRef entityNodeRef) {
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef == null) {
			imagesFolderNodeRef = fileFolderService
					.create(entityNodeRef, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES), ContentModel.TYPE_FOLDER).getNodeRef();
		}
		return imagesFolderNodeRef;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Load the image associated to the node.
	 */

	@Override
	public byte[] getImage(NodeRef nodeRef) {

		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		if (reader != null) {
			try {
				return StreamUtils.copyToByteArray(reader.getContentInputStream());
			} catch (IOException e) {
				logger.error("Failed to get the content for " + nodeRef, e);
			}
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void writeImages(NodeRef nodeRef, Map<String, byte[]> images) {

		NodeRef imagesFolderNodeRef = getImageFolder(nodeRef);

		for (Map.Entry<String, byte[]> image : images.entrySet()) {

			String filename = image.getKey();

			// create file if it doesn't exist
			NodeRef fileNodeRef = nodeService.getChildByName(imagesFolderNodeRef, ContentModel.ASSOC_CONTAINS, filename);
			if (fileNodeRef == null) {
				Map<QName, Serializable> fileProperties = new HashMap<>();
				fileProperties.put(ContentModel.PROP_NAME, filename);
				fileNodeRef = nodeService.createNode(imagesFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(filename)), ContentModel.TYPE_CONTENT,
						fileProperties).getChildRef();
			}

			String mimetype = mimetypeService.guessMimetype(filename);

			BufferedImage bufferedImage;
			try {
				bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getValue()));

				ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
				writer.setMimetype(mimetype);
				try (OutputStream out = writer.getContentOutputStream()) {

					ImageIO.write(bufferedImage, "jpg", out);
					logger.debug("Write image " + filename);
					bufferedImage.flush();
				}
			} catch (IOException e) {
				logger.error(e, e);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntityDefaultImage(NodeRef entityNodeRef) {

		String imgName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

		// manage workingCopy
		String wcLabel = CheckOutCheckInServiceImpl.getWorkingCopyLabel();
		if (wcLabel!=null && imgName.endsWith(wcLabel)) {
			imgName = getNameFromWorkingCopyName(imgName, wcLabel);
		}

		try {
			return getImage(entityNodeRef, imgName);
		} catch (BeCPGException e) {
			logger.debug("No image found for cm:name");
		}

		return getImage(entityNodeRef, getDefaultImageName(nodeService.getType(entityNodeRef)));
	}

	/** {@inheritDoc} */
	@Override
	public String getDefaultImageName(QName entityTypeQName) {
		String imgName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_LOGO_IMAGE + "." + entityTypeQName.getLocalName());
		if ((imgName == null) || imgName.isEmpty()) {
			imgName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_LOGO_IMAGE);
		}
		if (imgName != null) {
			imgName = imgName.toLowerCase();
		}

		return imgName;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createDefaultImage(NodeRef entityNodeRef) {
		NodeRef imagesFolderNodeRef = getOrCreateImageFolder(entityNodeRef);
		String name = getDefaultImageName(nodeService.getType(entityNodeRef));
		Map<QName, Serializable> props = new HashMap<>();
		props.put(ContentModel.PROP_NAME, name);
		if (logger.isDebugEnabled()) {
			logger.debug("Create new Image node: " + name + " under " + imagesFolderNodeRef);
		}
		return nodeService
				.createNode(imagesFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), ContentModel.TYPE_CONTENT, props)
				.getChildRef();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAssociatedImages(QName type) {
		return entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateDocumentsFolder(NodeRef entityNodeRef) {
		return getDocumentsFolder(entityNodeRef, true);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getDocumentsFolder(NodeRef entityNodeRef, boolean create) {
		String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
		NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);
		if ((documentsFolderNodeRef == null) && create) {
			documentsFolderNodeRef = fileFolderService.create(entityNodeRef, documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
		}
		return documentsFolderNodeRef;
	}
	
	
	@Override
	public 	Map<NodeRef, NodeRef>  getDocumentsByType(NodeRef entityNodeRef) {
    	Map<NodeRef, NodeRef> docByType  = new HashMap<>();
            for (FileInfo folder : fileFolderService.listFolders(entityNodeRef)) {
                for (FileInfo fileInfo : fileFolderService.listFiles(folder.getNodeRef())) {
                    NodeRef fileNodeRef = fileInfo.getNodeRef();
                    if (nodeService.hasAspect(fileNodeRef, BeCPGModel.ASPECT_DOCUMENT_ASPECT)) {
                        NodeRef docType = associationService.getTargetAssoc(fileNodeRef, BeCPGModel.ASSOC_DOCUMENT_TYPE_REF);
                        if (docType!=null) {
                            docByType.put(docType, fileNodeRef);
                        }
                    }
                }
            }
           return docByType;
    }

	/** {@inheritDoc} */
	@Override
	public NodeRef createOrCopyFrom(final NodeRef sourceNodeRef, final NodeRef parentNodeRef, final QName entityType, final String entityName) {
		NodeRef ret;
		Map<QName, Serializable> props = new HashMap<>();
		props.put(ContentModel.PROP_NAME, entityName);

		if ((sourceNodeRef != null) && nodeService.exists(sourceNodeRef)) {
			logger.debug("Copy existing entity");

			ret = AuthenticationUtil.runAsSystem(() -> {
				NodeRef ret1 = copyService.copyAndRename(sourceNodeRef, parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN,
						true);
				nodeService.setProperty(ret1, ContentModel.PROP_NAME, entityName);
				return ret1;
			});

		} else {
			logger.debug("Create new entity with name " + entityName);
			ret = nodeService
					.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(entityName)), entityType, props)
					.getChildRef();
		}

		if (nodeService.hasAspect(ret, ContentModel.ASPECT_VERSIONABLE)) {
			nodeService.removeAspect(ret, ContentModel.ASPECT_VERSIONABLE);
		}

		return ret;
	}

	/**
	 * Get original name from the working copy name and the cm:workingCopyLabel that
	 * was used to create it.
	 *
	 * @param workingCopyLabel
	 * @return original name
	 */
	private String getNameFromWorkingCopyName(String workingCopyName, String workingCopyLabel) {
		String workingCopyLabelRegEx = workingCopyLabel.replaceAll("\\(", "\\\\(");
		workingCopyLabelRegEx = workingCopyLabelRegEx.replaceAll("\\)", "\\\\)");
		if (workingCopyName.contains(" " + workingCopyLabel)) {
			workingCopyName = workingCopyName.replaceFirst(" " + workingCopyLabelRegEx, "");
		} else if (workingCopyName.contains(workingCopyLabel)) {
			workingCopyName = workingCopyName.replaceFirst(workingCopyLabelRegEx, "");
		}
		return workingCopyName;
	}

	/** {@inheritDoc} */
	@Override
	public void copyFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		copyOrMoveFiles(sourceNodeRef, targetNodeRef, true);
	}

	/** {@inheritDoc} */
	@Override
	public void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		copyOrMoveFiles(sourceNodeRef, targetNodeRef, false);
	}

	private void copyOrMoveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean isCopy) {

		if ((targetNodeRef != null) && (sourceNodeRef != null) && !nodeService.hasAspect(sourceNodeRef, VirtualContentModel.ASPECT_VIRTUAL)) {

			for (FileInfo file : fileFolderService.list(sourceNodeRef)) {

				if (file.getName().equals(TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS))) {

					// create Documents folder if needed
					String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
					NodeRef documentsFolderNodeRef = nodeService.getChildByName(targetNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);
					if (documentsFolderNodeRef == null) {
						documentsFolderNodeRef = fileFolderService.create(targetNodeRef, documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
					}

					for (FileInfo file2 : fileFolderService.list(file.getNodeRef())) {

						// copy/move files that are not report
						if (!ReportModel.TYPE_REPORT.equals(file2.getType())) {
							copyOrMoveFile(file2, documentsFolderNodeRef, isCopy);
						} else {
							logger.debug("Ignore " + file2.getName() + " for copy");
						}
					}
				} else {
					copyOrMoveFile(file, targetNodeRef, isCopy);
				}
			}
		}
	}

	private void copyOrMoveFile(FileInfo file, NodeRef parentNodeRef, boolean isCopy) {

		NodeRef documentNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, file.getName());
		if (documentNodeRef == null) {

			logger.debug("copy or move file in Documents: " + file.getName() + " parentNodeRef: " + parentNodeRef);

			if (isCopy) {
				ChildAssociationRef primaryAssocRef = nodeService.getPrimaryParent(file.getNodeRef());

				NodeRef subFolderNodeRef = copyService.copy(file.getNodeRef(), parentNodeRef, ContentModel.ASSOC_CONTAINS, primaryAssocRef.getQName(),
						true);
				nodeService.setProperty(subFolderNodeRef, ContentModel.PROP_NAME, file.getName());
			} else {

				nodeService.moveNode(file.getNodeRef(), parentNodeRef, ContentModel.ASSOC_CONTAINS,
						nodeService.getPrimaryParent(file.getNodeRef()).getQName());

			}

		} else {
			logger.debug("file already exists so no copy, neither move file: " + file.getName() + " in parentNodeRef: " + parentNodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteFiles(NodeRef entityNodeRef, boolean deleteArchivedNodes) {

		if ((entityNodeRef != null) && !nodeService.hasAspect(entityNodeRef, VirtualContentModel.ASPECT_VIRTUAL)) {
			for (FileInfo file : fileFolderService.list(entityNodeRef)) {

				if (logger.isDebugEnabled()) {
					logger.debug("delete file: " + file.getName() + " entityFolderNodeRef: " + entityNodeRef);
				}
				deleteNode(file.getNodeRef(), deleteArchivedNodes);
			}
		}
	}

	private void deleteNode(NodeRef nodeRef, boolean deleteArchivedNode) {

		// Test folder exists
		if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, VirtualContentModel.ASPECT_VIRTUAL)) {
			// delete from trash
			if (deleteArchivedNode) {
				nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
			}
			nodeService.deleteNode(nodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteDataLists(NodeRef entityNodeRef, boolean deleteArchivedNodes) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			deleteNode(listContainerNodeRef, deleteArchivedNodes);
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType) {
		return getEntityNodeRef(nodeRef, itemType, new HashSet<>());
	}

	private NodeRef getEntityNodeRef(NodeRef nodeRef, QName itemType, Set<NodeRef> visitedNodeRefs) {
		if (nodeService.exists(nodeRef)) {

			if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2)) {
				return nodeRef;
			}

			if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				return entityListDAO.getEntity(nodeRef);
			}

			// Create the visited nodes set if it has not already been created
			if (visitedNodeRefs == null) {
				visitedNodeRefs = new HashSet<>();
			}

			// This check prevents stack over flow when we have a cyclic node
			// graph
			if ((!visitedNodeRefs.contains(nodeRef)) && (visitedNodeRefs.size() < MAX_DEPTH_LEVEL)) {
				visitedNodeRefs.add(nodeRef);

				List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
				for (ChildAssociationRef parent : parents) {
					// We are not interested in following potentially massive
					// person group membership trees!
					if (IGNORE_PARENT_ASSOC_TYPES.contains(parent.getTypeQName())) {
						continue;
					}

					NodeRef entityNodeRef = getEntityNodeRef(parent.getParentRef(), nodeService.getType(parent.getParentRef()), visitedNodeRefs);
					if (entityNodeRef != null) {
						return entityNodeRef;
					}

				}

			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean changeEntityListStates(NodeRef entityNodeRef, EntityListState state) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

		if (listContainerNodeRef != null) {
			for (NodeRef listNodeRef : entityListDAO.getExistingListsNodeRef(listContainerNodeRef)) {
				nodeService.setProperty(listNodeRef, BeCPGModel.PROP_ENTITYLIST_STATE, state.toString());
			}
		}

		return true;

	}
}
