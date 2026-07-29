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
package fr.becpg.test.repo.sample;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.sample.GreenScoreSpecificationTestProduct;
import fr.becpg.repo.sample.StandardBodyMilkTestProduct;
import fr.becpg.repo.sample.StandardCakeWithLocalSemiFinishedTestProduct;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.sample.StandardSoapTestProduct;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Checks that every sample product can be built and formulated with all its optional data lists
 * enabled, which is the configuration produced by {@code SampleProductHelper} for scripts.
 */
public class SampleProductBuilderIT extends PLMBaseTestCase {

	@Autowired
	protected ProductService productService;

	@Test
	public void testChocolateEclairWithAllLists() {
		final NodeRef productNodeRef = inWriteTx(() -> new StandardChocolateEclairTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true).withLabeling(true).withGenericRawMaterial(true)
				.withStocks(true).withIngredients(true).withSurvey(true).withScoreList(true).withClaim(true).withSpecification(true).withNuts(true)
				.withProcess(true).build().createTestProduct().getNodeRef());

		assertFormulates(productNodeRef);

		inReadTx(() -> {
			ProductData product = (ProductData) alfrescoRepository.findOne(productNodeRef);
			Assert.assertNotNull("Composition should be created", product.getCompoList());
			Assert.assertFalse("Composition should not be empty", product.getCompoList().isEmpty());
			Assert.assertNotNull("Ingredient list should be created", product.getIngList());
			Assert.assertFalse("Ingredient list should not be empty", product.getIngList().isEmpty());
			return null;
		});
	}

	@Test
	public void testSoapWithAllLists() {
		final NodeRef productNodeRef = inWriteTx(() -> new StandardSoapTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true).withPhysico(true).withSpecification(true)
				.withScore(true).withToxicology(true).build().createTestProduct().getNodeRef());

		assertFormulates(productNodeRef);
	}

	@Test
	public void testGreenScoreWithAllLists() {
		final NodeRef productNodeRef = inWriteTx(() -> new GreenScoreSpecificationTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true).withPhysico(true).withSpecification(true)
				.withScore(true).withToxicology(true).build().createTestProduct().getNodeRef());

		assertFormulates(productNodeRef);
	}

	@Test
	public void testBodyMilkWithAllLists() {
		final NodeRef productNodeRef = inWriteTx(() -> new StandardBodyMilkTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true).withPhysico(true).withSpecification(true)
				.withScore(true).withToxicology(true).build().createTestProduct().getNodeRef());

		assertFormulates(productNodeRef);
	}

	@Test
	public void testCakeWithLocalSemiFinished() {
		final NodeRef productNodeRef = inWriteTx(
				() -> new StandardCakeWithLocalSemiFinishedTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
						.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true).build().createTestProduct().getNodeRef());

		assertFormulates(productNodeRef);

		inReadTx(() -> {
			ProductData product = (ProductData) alfrescoRepository.findOne(productNodeRef);
			Assert.assertNotNull("Composition should be created", product.getCompoList());
			Assert.assertFalse("Composition should not be empty", product.getCompoList().isEmpty());
			return null;
		});
	}

	/**
	 * Formulates the product and checks it is still readable afterwards.
	 *
	 * @param productNodeRef the product to formulate
	 */
	private void assertFormulates(NodeRef productNodeRef) {
		Assert.assertNotNull("Product should have been created", productNodeRef);

		inWriteTx(() -> {
			productService.formulate(productNodeRef);
			return null;
		});

		inReadTx(() -> {
			Assert.assertNotNull("Product should be readable after formulation", alfrescoRepository.findOne(productNodeRef));
			return null;
		});
	}

}
