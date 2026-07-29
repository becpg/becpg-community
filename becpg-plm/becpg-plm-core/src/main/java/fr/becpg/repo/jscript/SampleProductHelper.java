/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
import fr.becpg.repo.sample.StandardBodyMilkTestProduct;
import fr.becpg.repo.sample.StandardCakeWithLocalSemiFinishedTestProduct;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.sample.StandardSoapTestProduct;

/**
 * Utility script methods for create test product
 * <p>
 * Every builder returned here enables all the optional data lists of its product, so a sample
 * product created from a script is as complete as possible. A script that wants a lighter product
 * can still switch any option off on the returned builder.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class SampleProductHelper extends BaseScopableProcessorExtension {

	private NodeService nodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	
	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>chocolateEclairBuilder.</p>
	 * <p>
	 * All the optional data lists are enabled: composition, labeling, generic raw material, stocks,
	 * ingredients, survey, score list, claims, specification, nutrients and process.
	 *
	 * @param destFolder a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link fr.becpg.repo.sample.StandardChocolateEclairTestProduct.Builder} object
	 */
	public fr.becpg.repo.sample.StandardChocolateEclairTestProduct.Builder chocolateEclairBuilder(final ScriptNode destFolder) {
		return new StandardChocolateEclairTestProduct.Builder().withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
				.withDestFolder(destFolder.getNodeRef()).withCompo(true).withLabeling(true).withGenericRawMaterial(true).withStocks(true)
				.withIngredients(true).withSurvey(true).withScoreList(true).withClaim(true).withSpecification(true).withNuts(true).withProcess(true);
	}

	/**
	 * <p>soapProductBuilder.</p>
	 * <p>
	 * All the optional data lists are enabled: composition, physico-chemical values, specification,
	 * score and toxicology.
	 *
	 * @param destFolder a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link fr.becpg.repo.sample.StandardSoapTestProduct.Builder} object
	 */
	public fr.becpg.repo.sample.StandardSoapTestProduct.Builder soapProductBuilder(final ScriptNode destFolder) {
		return withAllSoapLists(new StandardSoapTestProduct.Builder(), destFolder);
	}

	/**
	 * <p>greenScoreProductBuilder.</p>
	 * <p>
	 * All the optional data lists are enabled, as for {@link #soapProductBuilder(ScriptNode)}.
	 *
	 * @param destFolder a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link fr.becpg.repo.sample.StandardSoapTestProduct.Builder} object
	 */
	public fr.becpg.repo.sample.StandardSoapTestProduct.Builder greenScoreProductBuilder(final ScriptNode destFolder) {
		return withAllSoapLists(new GreenScoreSpecificationTestProduct.Builder(), destFolder);
	}

	/**
	 * <p>bodyMilkProductBuilder.</p>
	 * <p>
	 * All the optional data lists are enabled, as for {@link #soapProductBuilder(ScriptNode)}. The
	 * regulatory countries are left to the product default and can be set on the returned builder.
	 *
	 * @param destFolder a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link fr.becpg.repo.sample.StandardSoapTestProduct.Builder} object
	 */
	public fr.becpg.repo.sample.StandardSoapTestProduct.Builder bodyMilkProductBuilder(final ScriptNode destFolder) {
		return withAllSoapLists(new StandardBodyMilkTestProduct.Builder(), destFolder);
	}

	/**
	 * <p>cakeWithLocalSemiFinishedBuilder.</p>
	 * <p>
	 * Builds a cake whose composition holds a local (in place) semi-finished product.
	 *
	 * @param destFolder a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link fr.becpg.repo.sample.StandardCakeWithLocalSemiFinishedTestProduct.Builder} object
	 */
	public fr.becpg.repo.sample.StandardCakeWithLocalSemiFinishedTestProduct.Builder cakeWithLocalSemiFinishedBuilder(final ScriptNode destFolder) {
		return new StandardCakeWithLocalSemiFinishedTestProduct.Builder().withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
				.withDestFolder(destFolder.getNodeRef()).withCompo(true);
	}

	/**
	 * Applies the repository dependencies and enables every optional data list of a soap based
	 * product.
	 *
	 * @param builder the builder to configure
	 * @param destFolder the folder the product is created in
	 * @return the configured builder
	 */
	private fr.becpg.repo.sample.StandardSoapTestProduct.Builder withAllSoapLists(
			final fr.becpg.repo.sample.StandardSoapTestProduct.Builder builder, final ScriptNode destFolder) {
		return builder.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(destFolder.getNodeRef())
				.withCompo(true).withPhysico(true).withSpecification(true).withScore(true).withToxicology(true);
	}

}
