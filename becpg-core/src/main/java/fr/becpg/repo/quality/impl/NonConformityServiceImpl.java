package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.quality.NonConformityService;

public class NonConformityServiceImpl implements NonConformityService {

	private static Log logger = LogFactory.getLog(NonConformityServiceImpl.class);

	private RepoService repoService;
	private Repository repositoryHelper;

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	@Override
	public NodeRef getStorageFolder(NodeRef productNodeRef) {

		NodeRef destFolderNodeRef = null;

		// calculate dest node
		if (productNodeRef != null) {

			destFolderNodeRef = repoService.getOrCreateFolderByPath(productNodeRef, RepoConsts.PATH_NC,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_NC));
		}

		// default folder
		if (destFolderNodeRef == null) {

			List<String> paths = new ArrayList<String>(2);
			paths.add(RepoConsts.PATH_QUALITY);
			paths.add(RepoConsts.PATH_NC);

			destFolderNodeRef = repoService.getOrCreateFolderByPaths(repositoryHelper.getCompanyHome(), paths);
		}

		return destFolderNodeRef;
	}

	@Override
	public void classifyNC(NodeRef ncNodeRef, NodeRef productNodeRef) {

		NodeRef destFolderNodeRef = getStorageFolder(productNodeRef);
		repoService.moveNode(ncNodeRef, destFolderNodeRef);				
	}

}
