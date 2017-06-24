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
package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.quality.NonConformityService;

public class NonConformityServiceImpl implements NonConformityService {

	private static final Log logger = LogFactory.getLog(NonConformityServiceImpl.class);

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

			destFolderNodeRef = repoService.getOrCreateFolderByPath(productNodeRef, PlmRepoConsts.PATH_NC, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_NC));
		}

		// default folder
		if (destFolderNodeRef == null) {

			List<String> paths = new ArrayList<>(2);
			paths.add(PlmRepoConsts.PATH_QUALITY);
			paths.add(PlmRepoConsts.PATH_NC);

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
