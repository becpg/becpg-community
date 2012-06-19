package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.quality.NonConformityService;

public class NonConformityServiceImpl implements NonConformityService {

	private static Log logger = LogFactory.getLog(NonConformityServiceImpl.class);

	private RepoService repoService;
	private NodeService nodeService;
	private Repository repositoryHelper;

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	@Override
	public NodeRef getStorageFolder(NodeRef productNodeRef) {

		NodeRef destFolderNodeRef = null;

		// calculate dest node
		if (productNodeRef != null) {

			NodeRef productFolderNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
			if (nodeService.getType(productFolderNodeRef).equals(BeCPGModel.TYPE_ENTITY_FOLDER)) {
				destFolderNodeRef = repoService.createFolderByPath(productFolderNodeRef, RepoConsts.PATH_NC,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_NC));
			} else {
				logger.warn("Product doesn't have an entity folder so NC is moved to default folder.");
			}
		}

		// default folder
		if (destFolderNodeRef == null) {

			List<String> paths = new ArrayList<String>(2);
			paths.add(RepoConsts.PATH_QUALITY);
			paths.add(RepoConsts.PATH_NC);

			destFolderNodeRef = repoService.createFolderByPaths(repositoryHelper.getCompanyHome(), paths);
		}

		return destFolderNodeRef;
	}

	@Override
	public void classifyNC(NodeRef ncNodeRef, NodeRef productNodeRef) {

		NodeRef nodeToMoveNodeRef = null;
		NodeRef ncParentNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();

		// calculate node to move
		if (nodeService.getType(ncParentNodeRef).equals(BeCPGModel.TYPE_ENTITY_FOLDER)) {
			nodeToMoveNodeRef = ncParentNodeRef;
		} else {
			nodeToMoveNodeRef = ncNodeRef;
		}

		NodeRef destFolderNodeRef = getStorageFolder(productNodeRef);

		repoService.moveNode(nodeToMoveNodeRef, destFolderNodeRef);				
	}

}
