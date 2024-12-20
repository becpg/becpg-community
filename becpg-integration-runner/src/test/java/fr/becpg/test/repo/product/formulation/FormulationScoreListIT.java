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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.impl.SurveyServiceImpl.ResponseType;
import fr.becpg.test.PLMBaseTestCase;

/**
 * @author frederic
 */
public class FormulationScoreListIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(FormulationScoreListIT.class);

	private List<NodeRef> nodesToDelete = new ArrayList<>();

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	protected ProductService productService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	protected Repository repositoryHelper;

	@Test
	public void testFormulationScore() throws Exception {
		logger.info("Starting testFormulationScore");

		inWriteTx(() -> {
			logger.info("Creating value list for criteria");
			createTestEvaluationCriteria();
			return null;
		});

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.info("Creating finished product");
			FinishedProductData finishedProduct = createTestProduct();
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);
			verifyScoreList(finishedProductNodeRef);
			return null;
		});

		inWriteTx(() -> {
			// Disable policies to be able to delete listValues
			policyBehaviourFilter.disableBehaviour();

			// Deleting product before trying to delete the created nodes			
			nodeService.deleteNode(finishedProductNodeRef);

			// Deleting created nodes
			for (NodeRef node : nodesToDelete) {
				nodeService.deleteNode(node);
			}

			return null;
		});
	}

	/**
	 * @return
	 */
	private FinishedProductData createTestEvaluationCriteria() {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();

		NodeRef parent = queryBuilder.selectNodeByPath(repositoryHelper.getCompanyHome(),
				"cm:System/cm:ProjectLists/bcpg:entityLists/cm:ScoreCriteria");

		/*-- characteristics --*/
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, "A");
		properties.put(BeCPGModel.PROP_LV_CODE, "A");
		nodesToDelete.add(nodeService
				.createNode(parent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_LIST_VALUE, properties).getChildRef());
		properties.clear();

		properties.put(BeCPGModel.PROP_LV_VALUE, "B");
		properties.put(BeCPGModel.PROP_LV_CODE, "B");
		nodesToDelete.add(nodeService
				.createNode(parent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_LIST_VALUE, properties).getChildRef());
		properties.clear();

		properties.put(BeCPGModel.PROP_LV_VALUE, "C");
		properties.put(BeCPGModel.PROP_LV_CODE, "C");
		nodesToDelete.add(nodeService
				.createNode(parent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_LIST_VALUE, properties).getChildRef());
		properties.clear();

		return null;
	}

	private FinishedProductData createTestProduct() {
		return FinishedProductData.build().withName("Produit fini 1").withUnit(ProductUnit.kg).withSurveyList(createSurveyList())
				.withScoreList(createScoreList());
	}

	private List<SurveyListDataItem> createSurveyList() {

		final SurveyQuestion question1 = new SurveyQuestion();

		question1.setLabel("Q1");
		question1.setSurveyCriterion("A");
		question1.setResponseType(ResponseType.multiChoicelist.name());
		final NodeRef q1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), question1).getNodeRef();
		nodesToDelete.add(q1NodeRef);

		final SurveyQuestion q1Answer1 = new SurveyQuestion();
		q1Answer1.setParent(question1);	
		q1Answer1.setLabel("A1");
		q1Answer1.setQuestionScore(20);
		final NodeRef q1A1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q1Answer1).getNodeRef();
		nodesToDelete.add(q1A1NodeRef);
		
		final SurveyQuestion q1Answer2 = new SurveyQuestion();
		q1Answer2.setParent(question1);
		q1Answer2.setLabel("A2");
		q1Answer2.setQuestionScore(40);
		final NodeRef q1A2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q1Answer2).getNodeRef();
		nodesToDelete.add(q1A2NodeRef);
		
		final SurveyQuestion q1Answer3 = new SurveyQuestion();
		q1Answer3.setParent(question1);
		q1Answer3.setLabel("A3");
		q1Answer3.setQuestionScore(-10);
		final NodeRef q1A3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q1Answer3).getNodeRef();
		nodesToDelete.add(q1A3NodeRef);

		final SurveyListDataItem survey1 = new SurveyListDataItem();
		survey1.setQuestion(q1NodeRef);
		survey1.setChoices(List.of(q1A1NodeRef, q1A3NodeRef));

		final SurveyQuestion question2 = new SurveyQuestion();
		question2.setQuestionNote("Q2");
		question2.setSurveyCriterion("A");
		question2.setQuestionScore(40);
		final NodeRef q2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), question2).getNodeRef();
		nodesToDelete.add(q2NodeRef);
		
		final SurveyQuestion q2Answer1 = new SurveyQuestion();
		q2Answer1.setParent(question2);
		q2Answer1.setQuestionNote("A1");
		q2Answer1.setQuestionScore(40);
		final NodeRef q2A1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q2Answer1).getNodeRef();
		nodesToDelete.add(q2A1NodeRef);
		
		final SurveyQuestion q2Answer2 = new SurveyQuestion();
		q2Answer2.setParent(question2);
		q2Answer2.setQuestionNote("A2");
		q2Answer2.setQuestionScore(20);
		final NodeRef q2A2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q2Answer2).getNodeRef();
		nodesToDelete.add(q2A2NodeRef);
		
		final SurveyQuestion q2Answer3 = new SurveyQuestion();
		q2Answer3.setParent(question2);
		q2Answer3.setQuestionNote("A3");
		q2Answer3.setQuestionScore(0);
		final NodeRef q2A3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q2Answer3).getNodeRef();
		nodesToDelete.add(q2A3NodeRef);

		final SurveyListDataItem survey2 = new SurveyListDataItem();
		survey2.setQuestion(q2NodeRef);
		survey2.setChoices(List.of(q2A2NodeRef));
		
		final SurveyQuestion question3 = new SurveyQuestion();
		question3.setQuestionNote("Q3");
		question3.setSurveyCriterion("C");
		question3.setQuestionScore(100);
		final NodeRef q3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), question3).getNodeRef();
		nodesToDelete.add(q3NodeRef);
		
		final SurveyQuestion q3Answer1 = new SurveyQuestion();
		q3Answer1.setParent(question3);
		q3Answer1.setQuestionNote("A1");
		q3Answer1.setQuestionScore(100);
		final NodeRef q3A1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q3Answer1).getNodeRef();
		nodesToDelete.add(q3A1NodeRef);
		
		final SurveyQuestion q3Answer2 = new SurveyQuestion();
		q3Answer2.setParent(question2);
		q3Answer2.setQuestionNote("A2");
		q3Answer2.setQuestionScore(50);
		final NodeRef q3A2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), q3Answer2).getNodeRef();
		nodesToDelete.add(q3A2NodeRef);

		final SurveyListDataItem survey3 = new SurveyListDataItem();
		survey3.setQuestion(q3NodeRef);
		survey3.setChoices(List.of(q3A2NodeRef));
		
		return List.of(survey1, survey2, survey3);
	}

	private List<ScoreListDataItem> createScoreList() {

		ScoreListDataItem scoreItem1 = new ScoreListDataItem();
		scoreItem1.setCriterion("A");

		ScoreListDataItem scoreItem2 = new ScoreListDataItem();
		scoreItem2.setCriterion("C");

		return List.of(scoreItem1, scoreItem2);

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
	private void verifyScoreList(NodeRef finishedProductNodeRef) {
		logger.info("Verifying formulated Score List");
		ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
		int checks = 0;

		for (ScoreListDataItem scoreListDataItem : formulatedProduct.getScoreList()) {
			if (scoreListDataItem.getCriterion().equals("A")) {
				// Calculate expected score for type A
				assertEquals("The mean of the type A is incorrect", Integer.valueOf(30), scoreListDataItem.getScore());
			} else if (scoreListDataItem.getCriterion().equals("B")) {
				// Calculate expected score for type B
				fail("B should not exists");
			} else if (scoreListDataItem.getCriterion().equals("C")) {
				// Calculate expected score for type C
				assertEquals("The mean of the type C is incorrect", Integer.valueOf(50), scoreListDataItem.getScore());
			}
			checks++;
		}

		// Ensure checks for type A, and C are performed
		assertEquals("Verify checks done", 2, checks);
	}

}
