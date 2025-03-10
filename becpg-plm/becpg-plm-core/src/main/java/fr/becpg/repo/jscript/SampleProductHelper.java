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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.sample.GreenScoreSpecificationTestProduct;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.sample.StandardSoapTestProduct;

/**
 * Utility script methods for create test product
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class SampleProductHelper extends BaseScopableProcessorExtension {

	private NodeService nodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public fr.becpg.repo.sample.StandardChocolateEclairTestProduct.Builder chocolateEclairBuilder(final ScriptNode destFolder) {
		return new StandardChocolateEclairTestProduct.Builder().withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
				.withDestFolder(destFolder.getNodeRef());
	}

	public fr.becpg.repo.sample.StandardSoapTestProduct.Builder soapProductBuilder(final ScriptNode destFolder) {
		return new StandardSoapTestProduct.Builder().withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
				.withDestFolder(destFolder.getNodeRef());
	}

	public fr.becpg.repo.sample.StandardSoapTestProduct.Builder greenScoreProductBuilder(final ScriptNode destFolder) {
		return new GreenScoreSpecificationTestProduct.Builder().withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
				.withDestFolder(destFolder.getNodeRef());
	}

}
