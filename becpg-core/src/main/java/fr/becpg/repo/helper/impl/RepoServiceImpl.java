/*
 *
 */
package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * The Class RepoServiceImpl.
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("repoService")
public class RepoServiceImpl implements RepoService {

	
	private static final Log logger = LogFactory.getLog(RepoServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private Repository repository;

	@Autowired
	private NamespaceService namespaceService;


	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateFolderByPaths(NodeRef parentNodeRef, List<String> paths) {

		for (String folderName : paths) {
			if ((folderName != null) && !folderName.isEmpty()) {
				parentNodeRef = getOrCreateFolderByPath(parentNodeRef, folderName, folderName);
			}
		}
		return parentNodeRef;
	}

	
	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateFolderByPath(NodeRef parentNodeRef, String path, String name) {

		NodeRef folderNodeRef = getFolderByPath(parentNodeRef, path);

		if (folderNodeRef == null) {
			logger.debug("Create folder : " + name + "  ");
			folderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, PropertiesHelper.cleanFolderName(name));
			if (folderNodeRef == null) {
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, PropertiesHelper.cleanFolderName(name));
				folderNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.resolveToQName(namespaceService, path),
						ContentModel.TYPE_FOLDER, properties).getChildRef();
			}
		}

		return folderNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getFolderByPath(NodeRef parentNodeRef, String path) {

		NodeRef ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(parentNodeRef, AbstractBeCPGQueryBuilder.encodePath(path));
		if ((ret == null) && !path.contains(RepoConsts.MODEL_PREFIX_SEPARATOR)) {
			// try by name...
			return nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, path);
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getFolderByPath(String path) {
		return getFolderByPath(repository.getCompanyHome(), path);
	}

	/** {@inheritDoc} */
	@Override
	public void moveNode(NodeRef nodeRefToMove, NodeRef destinationNodeRef) {

		logger.debug("start moveNode");

		// check the nodeRefToMove is not already moved !
		NodeRef parentOfNodeRefToMove = nodeService.getPrimaryParent(nodeRefToMove).getParentRef();
		if (destinationNodeRef.equals(parentOfNodeRefToMove)) {
			// nothing to do...
			logger.debug("nodeRefToMove is not already moved, nothing to do...");
			return;
		}

		// Check there is not a node with the same name, then rename node
		String name = getAvailableName(destinationNodeRef, (String) nodeService.getProperty(nodeRefToMove, ContentModel.PROP_NAME), false);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Move node '%s' in folder '%s'", name, destinationNodeRef));
		}

		try {
			fileFolderService.move(nodeRefToMove, destinationNodeRef, name);
		} catch (Exception e) {
			Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
			if (validCause != null) {
				throw (RuntimeException) validCause;
			}
			logger.error("Failed to move node", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getAvailableName(NodeRef folderNodeRef, String name, boolean forceRename) {

		List<FileInfo> fileInfos = fileFolderService.list(folderNodeRef);
		if (!fileInfos.isEmpty()) {
			int count =  0;
			NodeRef nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, name);

			while (nodeRef != null || forceRename) {
				count++;
				forceRename = false;
				String nameWithCounter = String.format("%s (%d)", name, count);
				nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, nameWithCounter);
			}

			if (count > 0 ) {
				name = String.format("%s (%d)", name, count);
			}
		}

		return name;
	}

}
