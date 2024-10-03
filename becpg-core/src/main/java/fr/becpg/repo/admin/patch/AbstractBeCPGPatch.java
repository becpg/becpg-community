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

/**
 * <p>Abstract AbstractBeCPGPatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractBeCPGPatch extends AbstractPatch {

	private static final Log logger = LogFactory.getLog(AbstractBeCPGPatch.class);

	protected Repository repository;
	protected RepoService repoService;
	protected EntitySystemService entitySystemService;
	protected ContentHelper contentHelper;
	
	/** Constant <code>BATCH_THREADS=4</code> */
	protected static final int BATCH_THREADS = 4;
	/** Constant <code>BATCH_SIZE=50</code> */
	protected static final int BATCH_SIZE = 50;
	/** Constant <code>INC=BATCH_THREADS * BATCH_SIZE * 1L</code> */
	protected static final long INC = BATCH_THREADS * BATCH_SIZE * 1L;
	
	
	/**
	 * <p>Setter for the field <code>repository</code>.</p>
	 *
	 * @param repository a {@link org.alfresco.repo.model.Repository} object.
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * <p>Setter for the field <code>contentHelper</code>.</p>
	 *
	 * @param contentHelper a {@link fr.becpg.repo.helper.ContentHelper} object.
	 */
	public void setContentHelper(ContentHelper contentHelper) {
		this.contentHelper = contentHelper;
	}
	
	/**
	 * <p>Setter for the field <code>entitySystemService</code>.</p>
	 *
	 * @param entitySystemService a {@link fr.becpg.repo.entity.EntitySystemService} object.
	 */
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object.
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	/**
	 * <p>searchFolder.</p>
	 *
	 * @param xpath a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
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

	/**
	 * <p>updateResource.</p>
	 *
	 * @param xPath a {@link java.lang.String} object.
	 * @param resourcePath a {@link java.lang.String} object.
	 */
	protected void updateResource(String xPath, String resourcePath) {
		NodeRef nodeRef = searchFolder(xPath);
		if (nodeRef != null) {
			logger.info("Update resource xPath: " + xPath + " with resourcePath: " + resourcePath);
			contentHelper.addFilesResources(nodeRef, resourcePath, true);
		}
	}
	

	/**
	 * <p>getFolder.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param folderPath a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getFolder(NodeRef parentNodeRef, String folderPath) {
		String folderName = TranslateHelper.getTranslatedPath(folderPath);
		if (folderName == null) {
		}
		return repoService.getFolderByPath(parentNodeRef, folderName);
	}
	
	
	/**
	 * <p>getSystemCharactsEntity.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getSystemCharactsEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_CHARACTS);
	}

	/**
	 * <p>getSystemListValuesEntity.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getSystemListValuesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_LISTS);
	}

	/**
	 * <p>getSystemHierachiesEntity.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getSystemHierachiesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);
	}

	/**
	 * <p>getCharactDataList.</p>
	 *
	 * @param systemEntityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListPath a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getCharactDataList(NodeRef systemEntityNodeRef, String dataListPath) {
		return entitySystemService.getSystemEntityDataList(systemEntityNodeRef, dataListPath);
	}


}
