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
package fr.becpg.repo.project.admin;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.TranslateHelper;

@Service
public class ProjectInitVisitor extends AbstractInitVisitorImpl {

	@Autowired
	EntitySystemService entitySystemService;

	@Autowired
	private EntityTplService entityTplService;

	@Override
	public void visitContainer(NodeRef companyHome) {
		
		logger.info("Run ProjectInitVisitor ...");

		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM);

		// Lists of characteristics for Project
		visitSystemProjectListValuesEntity(systemNodeRef, RepoConsts.PATH_PROJECT_LISTS);

		// Projects
		visitFolder(companyHome, RepoConsts.PATH_PROJECTS);

		// EntityTemplates
		visitEntityTpls(systemNodeRef);

		// Project Tpl
		visitFolder(systemNodeRef, RepoConsts.PATH_PROJECT_TEMPLATES);

	}

	/**
	 * Initialize the rules of the repository
	 */
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {

		if (folderName == RepoConsts.PATH_PROJECT_TEMPLATES) {
			createRuleAspect(nodeRef, true, ProjectModel.TYPE_PROJECT, BeCPGModel.ASPECT_ENTITY_TPL);
		}

	}

	/**
	 * Create the entity templates
	 * 
	 * @param productTplsNodeRef
	 */
	private void visitEntityTpls(NodeRef systemNodeRef) {

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);

		Set<String> subFolders = new HashSet<String>();
		subFolders.add(RepoConsts.PATH_DOCUMENTS);
		subFolders.add(RepoConsts.PATH_IMAGES);

		// visit supplier
		Set<QName> dataLists = new LinkedHashSet<QName>();
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
		dataLists.add(ProjectModel.TYPE_SCORE_LIST);
		entityTplService.createEntityTpl(entityTplsNodeRef, ProjectModel.TYPE_PROJECT, true, dataLists, null);
	}

	/**
	 * Create NPD List values
	 * 
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	private NodeRef visitSystemProjectListValuesEntity(NodeRef parentNodeRef, String path) {

		Map<String, QName> entityLists = new LinkedHashMap<String, QName>();

		entityLists.put(RepoConsts.PATH_TASK_LEGENDS, ProjectModel.TYPE_TASK_LEGEND);
		entityLists.put(RepoConsts.PATH_PROJECT_HIERARCHY, BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(RepoConsts.PATH_REQUEST_STATES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_REQUEST_ORIGINS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_SCORE_CRITERIA, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_SPONSORS, BeCPGModel.TYPE_LIST_VALUE);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	@Override
	protected void vivitFolderAspects(NodeRef folderNodeRef, String folderName) {
		switch (folderName) {
		case RepoConsts.PATH_PROJECT_TEMPLATES:
			if (!nodeService.hasAspect(folderNodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER)) {
				nodeService.addAspect(folderNodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER, null);
			}
			break;
		default:
			break;
		}

	}

	@Override
	public boolean shouldInit(NodeRef companyHomeNodeRef) {
		NodeRef systemNodeRef = visitFolder(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
		
		return nodeService.getChildByName(systemNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PROJECT_TEMPLATES)) == null;
		
	}
	
}
