package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
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
	private WorkflowService workflowService;

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	@Override
	public NodeRef getStorageFolder(NodeRef productNodeRef) {

		NodeRef destFolderNodeRef = null;

		// calculate dest node
		if (productNodeRef != null) {

			destFolderNodeRef = repoService.getOrCreateFolderByPath(productNodeRef, RepoConsts.PATH_NC, TranslateHelper.getTranslatedPath(RepoConsts.PATH_NC));
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
		logger.debug("Classify NC");
		NodeRef destFolderNodeRef = getStorageFolder(productNodeRef);
		repoService.moveNode(ncNodeRef, destFolderNodeRef);
	}

	@Override
	public List<String> getAssociatedWorkflow(NodeRef ncNodeRef) {

		List<String> ret = new ArrayList<>();
		List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(ncNodeRef, true);

		if (logger.isDebugEnabled()) {
			logger.debug("Found " + workflows.size() + " associated workflows ");
		}

		for (WorkflowInstance workflowInstance : workflows) {
			ret.add(workflowInstance.getId());
		}

		return ret;
	}

	@Override
	public void deleteWorkflows(List<String> instanceIds) {
		if (logger.isDebugEnabled()) {
			logger.debug(instanceIds.size() + " workflows to delete");
		}

		workflowService.cancelWorkflows(instanceIds);

	}

}
