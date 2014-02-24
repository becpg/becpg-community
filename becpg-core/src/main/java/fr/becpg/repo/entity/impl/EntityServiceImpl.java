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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.TranslateHelper;

/**
 * Entity Service implementation
 * 
 * @author querephi
 * 
 */
@Service("entityService")
public class EntityServiceImpl implements EntityService {

	private static Log logger = LogFactory.getLog(EntityServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private MimetypeService mimetypeService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private CopyService copyService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private DictionaryService dictionaryService;

	/**
	 * Load an image in the folder Images.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @param imgName
	 *            the img name
	 * @return the image
	 * @throws BeCPGException
	 */
	@Override
	public NodeRef getImage(NodeRef nodeRef, String imgName) throws BeCPGException {

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

	/**
	 * 
	 * @param nodeRef
	 * @param imgName
	 * @return List of images nodeRefs
	 * @throws BeCPGException
	 */
	@Override
	public List<NodeRef> getImages(NodeRef nodeRef) throws BeCPGException {

		NodeRef imagesFolderNodeRef = getImageFolder(nodeRef);

		List<NodeRef> ret = new ArrayList<NodeRef>();

		List<FileInfo> files = fileFolderService.listFiles(imagesFolderNodeRef);
		for (FileInfo file : files) {
			ret.add(file.getNodeRef());
		}

		return ret;
	}

	@Override
	public NodeRef getImageFolder(NodeRef nodeRef) throws BeCPGException {

		NodeRef imagesFolderNodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef == null) {
			throw new BeCPGException("Folder 'Images' doesn't exist.");
		}
		return imagesFolderNodeRef;
	}

	/**
	 * Load the image associated to the node.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return the image
	 */
	public byte[] getImage(NodeRef nodeRef) {

		byte[] imageBytes = null;

		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		if (reader != null) {
			InputStream in = reader.getContentInputStream();
			OutputStream out = null;
			try {
				Image image = ImageIO.read(in);
				out = new ByteArrayOutputStream();
				if (image != null) {
					ImageIO.write((RenderedImage) image, MimetypeMap.MIMETYPE_IMAGE_PNG.equals(reader.getMimetype()) ? "png" : "jpg", out);
					imageBytes = ((ByteArrayOutputStream) out).toByteArray();
				}
			} catch (IOException e) {
				logger.error("Failed to get the content for " + nodeRef, e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}

		return imageBytes;
	}


	@Override
	public void writeImages(NodeRef nodeRef, Map<String, byte[]> images) throws BeCPGException {

		NodeRef imagesFolderNodeRef = getImageFolder(nodeRef);

		for (Map.Entry<String, byte[]> image : images.entrySet()) {

			String filename = image.getKey();

			// create file if it doesn't exist
			NodeRef fileNodeRef = nodeService.getChildByName(imagesFolderNodeRef, ContentModel.ASSOC_CONTAINS, filename);
			if (fileNodeRef == null) {
				Map<QName, Serializable> fileProperties = new HashMap<QName, Serializable>();
				fileProperties.put(ContentModel.PROP_NAME, filename);
				fileNodeRef = nodeService.createNode(imagesFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(filename)), ContentModel.TYPE_CONTENT, fileProperties).getChildRef();
			}

			String mimetype = mimetypeService.guessMimetype(filename);

			BufferedImage bufferedImage = null;
			OutputStream out = null;
			try {
				bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getValue()));

				ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
				writer.setMimetype(mimetype);
				out = writer.getContentOutputStream();

				ImageIO.write(bufferedImage, "jpg", out);
				logger.debug("Write image " + filename);
				bufferedImage.flush();
			} catch (IOException e) {
				logger.error(e, e);
			} finally {
				IOUtils.closeQuietly(out);
			}
		}
	}

	@Override
	public NodeRef getEntityDefaultImage(NodeRef entityNodeRef) throws BeCPGException {

		String imgName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

		// manage workingCopy
		String wcLabel = CheckOutCheckInServiceImpl.getWorkingCopyLabel();
		if (imgName.endsWith(wcLabel)) {
			imgName = getNameFromWorkingCopyName(imgName, wcLabel);
		}

		try {
			return getImage(entityNodeRef, imgName);
		} catch (BeCPGException e) {
			logger.debug("No image found for cm:name");
		}
		

		return getImage(entityNodeRef, getDefaultImageName(nodeService.getType(entityNodeRef)));
	}
	
	
	@Override 
	public String getDefaultImageName(QName entityTypeQName) {
		String imgName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_LOGO_IMAGE+"."+entityTypeQName.getLocalName());
		if(imgName==null || imgName.isEmpty()) {
			imgName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_LOGO_IMAGE);
		}
		if(imgName!=null) {
			imgName = imgName.toLowerCase();
		}
		
		return imgName;
	}


	
	//TODO Supprimer
	@Override
	public boolean hasAssociatedImages(QName type) {
		return dictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2);
	}

	
	@Override
	public NodeRef getOrCreateDocumentFolder(NodeRef entityNodeRef) {
		String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
		NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);
		if (documentsFolderNodeRef == null) {
			logger.warn("No folder: " + documentsFolderName + " found ");
			 documentsFolderNodeRef = fileFolderService.create(entityNodeRef,
			 documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
		}
		return documentsFolderNodeRef;
	}
	
	@Override
	public NodeRef createOrCopyFrom(final NodeRef sourceNodeRef, final NodeRef parentNodeRef, final QName entityType, final String entityName) {
		NodeRef ret = null;
		Map<QName, Serializable> props = new HashMap<QName, Serializable>();
		props.put(ContentModel.PROP_NAME, entityName);

		if (sourceNodeRef != null && nodeService.exists(sourceNodeRef)) {
			logger.debug("Copy existing entity");

			ret = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
				@Override
				public NodeRef doWork() throws Exception {
					NodeRef ret = copyService.copyAndRename(sourceNodeRef, parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);
					nodeService.setProperty(ret, ContentModel.PROP_NAME, entityName);
					return ret;
				}
			}, AuthenticationUtil.getSystemUserName());

		} else {
			logger.debug("Create new entity with name " + entityName);
			ret = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(entityName)), entityType, props).getChildRef();
		}
		
		if(nodeService.hasAspect(ret, ContentModel.ASPECT_VERSIONABLE)){
			nodeService.removeAspect(ret, ContentModel.ASPECT_VERSIONABLE);
		}
		
		return ret;
	}
	
	/**
	 * Get original name from the working copy name and the cm:workingCopyLabel
	 * that was used to create it.
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

	@Override
	public void copyFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		copyOrMoveFiles(sourceNodeRef, targetNodeRef, true);
	}

	@Override
	public void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		copyOrMoveFiles(sourceNodeRef, targetNodeRef, false);
	}

	private void copyOrMoveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean isCopy) {

		if (targetNodeRef != null && sourceNodeRef != null) {

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
							logger.debug("Ignore "+file2.getName()+" for copy");
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
				NodeRef subFolderNodeRef = copyService.copy(file.getNodeRef(), parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);
				nodeService.setProperty(subFolderNodeRef, ContentModel.PROP_NAME, file.getName());
			} else {
				nodeService.moveNode(file.getNodeRef(), parentNodeRef, ContentModel.ASSOC_CONTAINS, nodeService.getPrimaryParent(file.getNodeRef()).getQName());
			}
		} else {
			logger.debug("file already exists so no copy, neither move file: " + file.getName() + " in parentNodeRef: " + parentNodeRef);
		}
	}

	@Override
	public void deleteFiles(NodeRef entityNodeRef, boolean deleteArchivedNodes) {

		if (entityNodeRef != null) {
			for (FileInfo file : fileFolderService.list(entityNodeRef)) {

				logger.debug("delete file: " + file.getName() + " entityFolderNodeRef: " + entityNodeRef);
				deleteNode(file.getNodeRef(), deleteArchivedNodes);
			}
		}
	}

	private void deleteNode(NodeRef nodeRef, boolean deleteArchivedNode) {

		// delete from trash
		if (deleteArchivedNode) {
			nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
		}
		nodeService.deleteNode(nodeRef);
	}

	@Override
	public void deleteDataLists(NodeRef entityNodeRef, boolean deleteArchivedNodes) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			deleteNode(listContainerNodeRef, deleteArchivedNodes);
		}
	}

	

}
