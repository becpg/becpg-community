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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Entity Service implementation
 * 
 * @author querephi
 * 
 */
@Service
public class EntityServiceImpl implements EntityService {

	/** The Constant QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE. */
	private static final String QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE = "(%s) AND +@cm\\:modified:[%s TO MAX]";

	/** The Constant QUERY_OPERATOR_OR. */
	private static final String QUERY_OPERATOR_OR = " OR ";

	/** The Constant QUERY_PARENT. */
	private static final String QUERY_PARENT = " PARENT:\"%s\"";

	private static Log logger = LogFactory.getLog(EntityServiceImpl.class);

	private NodeService nodeService;

	private MimetypeService mimetypeService;

	private EntityListDAO entityListDAO;

	private BeCPGSearchService beCPGSearchService;

	private FileFolderService fileFolderService;

	private PermissionService permissionService;

	private EntityTplService entityTplService;

	private EntityVersionService entityVersionService;

	private CopyService copyService;

	private ContentService contentService;

	private DictionaryService dictionaryService;

	private BehaviourFilter policyBehaviourFilter;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	public boolean hasDataListModified(NodeRef nodeRef) {

		Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);

		// check data lists
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);

		if (listContainerNodeRef != null) {

			String queryParentsSearch = "";

			for (NodeRef listNodeRef : entityListDAO.getExistingListsNodeRef(listContainerNodeRef)) {

				// check list folder modified date
				Date dataListModified = (Date) nodeService.getProperty(listNodeRef, ContentModel.PROP_MODIFIED);
				logger.debug("list modified: " + ISO8601DateFormat.format(dataListModified) + " - modified: " + ISO8601DateFormat.format(modified));
				if (dataListModified.after(modified)) {
					logger.debug("list folder has been modified");
					return true;
				}

				if (!queryParentsSearch.isEmpty()) {
					queryParentsSearch += QUERY_OPERATOR_OR;
				}
				queryParentsSearch += String.format(QUERY_PARENT, listNodeRef);
			}

			// check list children modified date
			if (!queryParentsSearch.isEmpty()) {

				String querySearch = String.format(QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE, queryParentsSearch, ISO8601DateFormat.format(modified));

				logger.debug("queryPath: " + querySearch);
				List<NodeRef> resultSet = beCPGSearchService.luceneSearch(querySearch, null, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
				logger.debug("resultSet.length() : " + resultSet.size());

				if (resultSet!=null && !resultSet.isEmpty()) {
					if (logger.isDebugEnabled()) {
						logger.debug("list children has been modified");
						logger.debug("Date :" + ISO8601DateFormat.format((Date) nodeService.getProperty(resultSet.get(0), ContentModel.PROP_MODIFIED)));
					}

					return true;
				}

			}
		}

		return false;
	}

	/**
	 * get the entity folder of the template and copy it for the entity.
	 * 
	 * @param entityNodeRef
	 *            the entity node ref
	 */
	@Override
	public NodeRef getEntityTplNodeRef(NodeRef entityNodeRef) {

		logger.debug("initializeEntityFolder");
		QName entityType = nodeService.getType(entityNodeRef);

		// entity => exit
		if (entityType.isMatch(BeCPGModel.TYPE_ENTITY_V2)) {
			return null;
		}		
		
		NodeRef entityTplNodeRef = null;
		
		 if(dictionaryService.isSubClass(entityType, BeCPGModel.TYPE_ENTITY_V2)){		
			 entityTplNodeRef = entityTplService.getFolderTpl(entityType);
		}
		else{
			logger.debug("entityNodeRef doesn't inherit from  entityV2");
			return null;
		}
		
		return entityTplNodeRef;
	}

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

	private NodeRef getImageFolder(NodeRef nodeRef) throws BeCPGException {
		
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
					ImageIO.write((RenderedImage) image, "jpg", out);
					imageBytes = ((ByteArrayOutputStream) out).toByteArray();
				}
			} catch (IOException e) {
				logger.error("Failed to get the content", e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}

		return imageBytes;
	}

	@Override
	public void deleteEntity(NodeRef entityNodeRef) {
		logger.debug("delete entity");
		entityVersionService.deleteVersionHistory(entityNodeRef);
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

					try {

						policyBehaviourFilter.disableBehaviour(entityType);

						NodeRef ret = copyService.copyAndRename(sourceNodeRef, parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);
						nodeService.setProperty(ret, ContentModel.PROP_NAME, entityName);

						//Done by copy initializeEntityFolder(ret);

						return ret;

					} finally {

						policyBehaviourFilter.enableBehaviour(entityType);

					}

				}
			}, AuthenticationUtil.getSystemUserName());

		} else {
			logger.debug("Create new entity");
			ret = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(entityName)), entityType, props).getChildRef();
		}
		return ret;
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

		imgName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_LOGO_IMAGE).toLowerCase();

		if (dictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_PRODUCT)) {
			imgName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_IMAGE).toLowerCase();

		}

		return getImage(entityNodeRef, imgName);
	}

	@Override
	public boolean hasAssociatedImages(QName type) {

		return BeCPGModel.TYPE_CLIENT.isMatch(type) || BeCPGModel.TYPE_SUPPLIER.isMatch(type) || dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCT) || ProjectModel.TYPE_PROJECT.isMatch(type);
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
		
		// copy files
		if(targetNodeRef != null && sourceNodeRef != null){			
			for (FileInfo file : fileFolderService.list(sourceNodeRef)) {
				
				if(nodeService.getChildByName(targetNodeRef, ContentModel.ASSOC_CONTAINS, file.getName()) == null){
					
					logger.debug("copy file: " + file.getName() + " entityFolderNodeRef: " + targetNodeRef);
					NodeRef subFolderNodeRef = copyService.copy(file.getNodeRef(), targetNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);
					nodeService.setProperty(subFolderNodeRef, ContentModel.PROP_NAME, file.getName());
					
					// initialize permissions according to template
					if (file.isFolder() && nodeService.hasAspect(file.getNodeRef(), BeCPGModel.ASPECT_PERMISSIONS_TPL)) {

						QName[] permissionGroupAssociations = { BeCPGModel.ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS, BeCPGModel.ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS,
								BeCPGModel.ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS, BeCPGModel.ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS };
						String[] permissionNames = { RepoConsts.PERMISSION_CONSUMER, RepoConsts.PERMISSION_EDITOR, RepoConsts.PERMISSION_CONTRIBUTOR,
								RepoConsts.PERMISSION_COLLABORATOR };

						for (int cnt = 0; cnt < permissionGroupAssociations.length; cnt++) {

							QName permissionGroupAssociation = permissionGroupAssociations[cnt];
							String permissionName = permissionNames[cnt];
							List<AssociationRef> groups = nodeService.getTargetAssocs(file.getNodeRef(), permissionGroupAssociation);

							if (groups!=null && !groups.isEmpty()) {
								for (AssociationRef assocRef : groups) {
									NodeRef groupNodeRef = assocRef.getTargetRef();
									String authorityName = (String) nodeService.getProperty(groupNodeRef, ContentModel.PROP_AUTHORITY_NAME);
									logger.debug("add permission, folder: " + file.getName() + " authority: " + authorityName + " perm: " + permissionName);
									permissionService.setPermission(subFolderNodeRef, authorityName, permissionName, true);

									// remove association
									nodeService.removeAssociation(subFolderNodeRef, groupNodeRef, permissionGroupAssociation);
								}
							}
						}

						// TODO
						// remove aspect when every association has been
						// removed
						// nodeService.removeAspect(subFolderNodeRef,
						// BeCPGModel.ASPECT_PERMISSIONS_TPL);
						
						//TODO also copy datalist
					}
				}				
			}
		}
		
	}

	@Override
	public void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		
		if(targetNodeRef != null && sourceNodeRef != null){			
			for (FileInfo file : fileFolderService.list(sourceNodeRef)) {
				
				logger.debug("move file: " + file.getName() + " entityFolderNodeRef: " + targetNodeRef);				
				nodeService.moveNode(file.getNodeRef(), targetNodeRef, ContentModel.ASSOC_CONTAINS, nodeService.getPrimaryParent(file.getNodeRef()).getQName());
			}
		}
		
	}

}
