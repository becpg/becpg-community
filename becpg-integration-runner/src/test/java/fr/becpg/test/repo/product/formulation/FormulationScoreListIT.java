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
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.repo.GreenScoreSpecificationTestProduct;
import fr.becpg.test.repo.StandardChocolateEclairTestProduct;
import fr.becpg.test.utils.CharactTestHelper;

/**
 * @author frederic
 */
public class FormulationScoreListIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(FormulationScoreListIT.class);

	@Autowired
	protected ProductService productService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	protected Repository repositoryHelper;

	@Test
	public void testGreenScoreCalculation() {
		ProductData greenScoreProductData = inWriteTx(
				() -> new GreenScoreSpecificationTestProduct.Builder().withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
						.withDestFolder(getTestFolderNodeRef()).withSpecification(true).build().createTestProduct());

		Assert.assertNotNull(greenScoreProductData);
	}

	@Test
	public void testFormulationSimpleScore() {
		logger.info("Starting testFormulationScore");

		FinishedProductData product = inWriteTx(() -> new StandardChocolateEclairTestProduct.Builder().withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService)
				.withDestFolder(getTestFolderNodeRef()).withCompo(true).withSurvey(true).withScoreList(true).build().createTestProduct());

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
	      - Question Score: 66

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
	      - Question Score: 0

	    - Question 4:
	      - Question: Q4
	      - Question Type: B
	      - Question Score: 100

	  - Expected Score List:
	     - Score Item 1:
	     	- Question Type: A
	     	- Question Score: 30 (the survey questions results override the previous score we had and put the mean
	     	of the scores of the responses of type A)

	     - Score Item 2:
	     	- Question Type: B
	     	- Question Score: 50 (mean of responses of type B)

	     - Score Item 3:
	     	- Question Type: C
	     	- Question Score: 80 (no survey question of this type, we keep the score provided manually
	 */
	private void verifyScoreList(ProductData formulatedProduct) {
		logger.info("Verifying formulated Score List");
		int checks = 0;

		for (ScoreListDataItem scoreListDataItem : formulatedProduct.getScoreList()) {
			if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, StandardChocolateEclairTestProduct.PASTRY_QUALITY))) {
				// Calculate expected score for type A
				assertEquals("The mean of the type A is incorrect", Integer.valueOf(30), scoreListDataItem.getScore());
			} else if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, StandardChocolateEclairTestProduct.CCP_COMPLIANCE))) {
				// Calculate expected score for type B
				fail("B should not exists");
			} else if (scoreListDataItem.getScoreCriterion()
					.equals(CharactTestHelper.getOrCreateScoreCriterion(nodeService, StandardChocolateEclairTestProduct.FILLING_QUALITY))) {
				// Calculate expected score for type C
				assertEquals("The mean of the type C is incorrect", Integer.valueOf(50), scoreListDataItem.getScore());
			}
			checks++;
		}

		// Ensure checks for type A, and C are performed
		assertEquals("Verify checks done", 2, checks);
	}

}
