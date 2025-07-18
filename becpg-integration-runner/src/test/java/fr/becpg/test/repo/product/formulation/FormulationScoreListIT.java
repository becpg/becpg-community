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
package fr.becpg.test.repo.product.formulation;

import org.alfresco.repo.model.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.repo.sample.GreenScoreSpecificationTestProduct;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.test.PLMBaseTestCase;

/**
 * @author frederic
 */
public class FormulationScoreListIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(FormulationScoreListIT.class);

	private static final double DELTA = .00001;
	
	@Autowired
	protected ProductService productService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	protected Repository repositoryHelper;

	@Test
	public void testGreenScoreCalculation() {
		ProductData greenScoreProductData = inWriteTx(
				() -> new GreenScoreSpecificationTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
						.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withSpecification(true)
						.build().createTestProduct());

		inWriteTx(() -> {
			productService.formulate(greenScoreProductData);
			return null;
		});
		
		verifyGreenScoreList(greenScoreProductData);
		
		Assert.assertNotNull(greenScoreProductData);
	}

	private void verifyGreenScoreList(ProductData formulatedProduct) {
		int checks = 0;

		for (ScoreListDataItem scoreListDataItem : formulatedProduct.getScoreList()) {
			if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, GreenScoreSpecificationTestProduct.FORMULATION))) {
				assertEquals("Score range should be C","C", scoreListDataItem.getRange());
				checks++;
			} 
			if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, GreenScoreSpecificationTestProduct.RESOURCE_CONSUMPTION))) {
				assertEquals("Score range should be A","A", scoreListDataItem.getRange());
				checks++;
			} 
			
		}

		assertEquals("Verify checks done", 2, checks);
		
	}

	@Test
	public void testFormulationSimpleScore() {
		logger.info("Starting testFormulationScore");

		FinishedProductData product = inWriteTx(() -> new StandardChocolateEclairTestProduct.Builder()
				.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
				.withDestFolder(getTestFolderNodeRef()).withCompo(true).withSurvey(true).withScoreList(true)
				.build().createTestProduct());

		inWriteTx(() -> {
			
			productService.formulate(product);
			verifyScoreList(product);
			return null;
		});

	}

	/**
	 * Finished Product:
	  - Name: Produit fini 1
	  - Unit: kg
	  - Quantity: 1

	  Score List (manually provided):
	    - Score Item 1:
		  - Question: SurveyQ1
	      - Question Type: A

	    - Score Item 2:
	      - Question: SurveyQ2
	      - Question Type: C
	      - Question Score: 80

	  Survey List:
	    - Question 1:
	      - Question: Q1
	      - Question Type: A
	      - Question Score: 20

	    - Question 2:
	      - Question: Q2
	      - Question Type: A
	      - Question Score : 40

	    - Question 3:
	      - Question: Q3
	      - Question Type: B
	      - Question Score: 50d

	  - Expected Score List:
	     - Score Item 1:
	     	- Question Type: PASTRY_QUALITY
	     	- Question Score: ~16.6 (100 * Q1 / Q1MAX)

	     - Score Item 2:
	     	- Question Type: FILLING_QUALITY
	     	- Question Score: ~23.07 (mean of responses of type B)

	     - Score Item 3:
	     	- Question Type: CCP_COMPLIANCE
	     	- Question Score: ~33.33 (mean of responses of type B)
	 */
	private void verifyScoreList(ProductData formulatedProduct) {
		logger.info("Verifying formulated Score List");
		int checks = 0;

		for (ScoreListDataItem scoreListDataItem : formulatedProduct.getScoreList()) {
			if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, StandardChocolateEclairTestProduct.PASTRY_QUALITY))) {
				// Calculate expected score for type A
				assertEquals("The mean of the type PASTRY_QUALITY is incorrect", 100d * 20 / 120, scoreListDataItem.getScore(), DELTA);
				checks++;
			} else if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, StandardChocolateEclairTestProduct.CCP_COMPLIANCE))) {
				// Calculate expected score for type B
				assertEquals("The mean of the type CCP_COMPLIANCE is incorrect", 100d * 50 / (50 + 100), scoreListDataItem.getScore(), DELTA);
				checks++;
			} else if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, StandardChocolateEclairTestProduct.FILLING_QUALITY))) {
				// Calculate expected score for type C
				assertEquals("The mean of the type FILLING_QUALITY is incorrect", 100d * 30 / (30 + 100), scoreListDataItem.getScore(), DELTA);
				checks++;
			}
			
		}

		// Ensure checks for type PASTRY_QUALITY, and FILLING_QUALITY are performed
		assertEquals("Verify checks done", 3, checks);
	}

}
