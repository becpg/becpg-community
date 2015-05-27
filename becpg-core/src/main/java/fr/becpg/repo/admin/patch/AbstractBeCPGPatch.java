/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public abstract class AbstractBeCPGPatch extends AbstractPatch {

	private static Log logger = LogFactory.getLog(AbstractBeCPGPatch.class);

	protected Repository repository;
	
	protected RepoService repoService;
	
	protected EntitySystemService entitySystemService;

	protected ContentHelper contentHelper;
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setContentHelper(ContentHelper contentHelper) {
		this.contentHelper = contentHelper;
	}
	
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	protected NodeRef searchFolder(String xpath) {
		List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(repository.getRootHome(), xpath);
		if (nodeRefs.size() > 1) {
			throw new PatchException("XPath returned too many results: \n" + "   xpath: " + xpath + "\n"
					+ "   results: " + nodeRefs);
		} else if (nodeRefs.size() == 0) {
			// the node does not exist
			return null;
		} else {
			return nodeRefs.get(0);
		}
	}

	protected void updateResource(String xPath, String resourcePath) {
		NodeRef nodeRef = searchFolder(xPath);
		if (nodeRef != null) {
			logger.info("Update resource xPath: " + xPath + " with resourcePath: " + resourcePath);
			contentHelper.addFilesResources(nodeRef, resourcePath, true);
		}
	}
	

	public NodeRef getFolder(NodeRef parentNodeRef, String folderPath) {
		String folderName = TranslateHelper.getTranslatedPath(folderPath);
		if (folderName == null) {
			folderName = folderPath;
		}
		return repoService.getFolderByPath(parentNodeRef, folderPath);
	}
	
	
	public NodeRef getSystemCharactsEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_CHARACTS);
	}

	public NodeRef getSystemListValuesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_LISTS);
	}

	public NodeRef getSystemHierachiesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);
	}

	public NodeRef getCharactDataList(NodeRef systemEntityNodeRef, String dataListPath) {
		return entitySystemService.getSystemEntityDataList(systemEntityNodeRef, dataListPath);
	}


}
