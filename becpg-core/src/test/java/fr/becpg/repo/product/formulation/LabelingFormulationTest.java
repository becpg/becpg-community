package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.AbstractFinishedProductTest;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleType;

public class LabelingFormulationTest extends AbstractFinishedProductTest {

	protected static Log logger = LogFactory.getLog(LabelingFormulationTest.class);

	@Resource
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

//	ing3 french 45,5%, ing2 french 19,7%, ing4 french 9,1%, ing1 french 7,6%	

	// sous ingrédients
	
	
	
	

	private NodeRef createTestProduct(final List<LabelingRuleListDataItem> labelingRuleList) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/**
				 * Finished product 1
				 */
				logger.debug("/*************************************/");
				logger.debug("/*-- Create Finished product 1--*/");
				logger.debug("/*************************************/");
				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished product 1");
				finishedProduct1.setLegalName("Legal Finished product 1");
				finishedProduct1.setQty(2d);
				finishedProduct1.setUnit(ProductUnit.kg);
				finishedProduct1.setDensity(1d);
				List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 1d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial11NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 2d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial12NodeRef));
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial13NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial14NodeRef));

				finishedProduct1.getCompoListView().setCompoList(compoList1);

				finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);


				return alfrescoRepository.create(testFolderNodeRef, finishedProduct1).getNodeRef();
			}
		}, false, true);
	}

	/**
	 * Test ingredients calculating.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	 //@Test
	public void testLabelingFormulation() throws Exception {

		logger.info("testLabelingFormulation");

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		//Test locale + format %
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Langue", "fr,en", LabelingRuleType.Locale));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing2, ing3, ing4), null));

		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef1);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

				assertTrue(formulatedProduct1.getLabelingListView().getLabelingRuleList().size() > 0);

				// verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct1.getLabelingListView().getIngLabelingList());
				assertTrue(formulatedProduct1.getLabelingListView().getIngLabelingList().size() > 0);

				for (IngLabelingListDataItem illDataItem : formulatedProduct1.getLabelingListView().getIngLabelingList()) {

					checkILL("Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)",
							"Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french), ing2 french 22,2%, ing1 french)",
							illDataItem.getValue().getValue(Locale.FRENCH));

					checkILL("Garniture english 50% (ing3 english 83,3%, ing4 english 16,7%)",
							"Pâte english 50% (Legal Raw material 12 66,7% (ing2 english 75%, ing1 english), ing2 english 22,2%, ing1 english)",
							illDataItem.getValue().getValue(Locale.ENGLISH));
					
				}

				return null;

			}
		}, false, true);

	}

	/**
	 * Test ingredients calculating.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateILLWithRMWithNullPerc() throws Exception {
		logger.info("testIngredientsCalculating");
		
	// Par défaut	
	//		└──[root - 0.0 (2.0)]
		//	    ├──[Pâte french - 1.0 (3.0)]
		//	    │   ├──[ing1 french - 0.33333333333333337]
		//	    │   ├──[ing2 french - 0.6666666666666667]
		//	    │   └──[Legal Raw material 12 - 2.0 (2.0)]
		//	    │       ├──[ing1 french - 0.5]
		//	    │       └──[ing2 french - 1.5]
		//	    └──[Garniture french - 1.0 (6.0)]
		//	        ├──[ing3 french - 5.0]
		//	        └──[ing4 french - 1.0]

		final NodeRef finishedProductNodeRef1 = createTestProduct(null);
		
		//Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare, Arrays.asList(localSF11NodeRef, rawMaterial12NodeRef, localSF12NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		
	//		└──[root - 0.0 (11.0)]
	//			    ├──[ing1 french - 0.8333333333333334]
	//			    ├──[ing2 french - 2.166666666666667]
	//			    ├──[ing3 french - 5.0]
	//			    └──[ing4 french - 1.0]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 45,5%, ing2 french 19,7%, ing4 french 9,1%, ing1 french 7,6%", Locale.FRENCH);
		
		//Omit
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Omit", null, LabelingRuleType.Omit, Arrays.asList(localSF12NodeRef), null));
		//Ing2 dans rawMaterial 12 est un auxiliare
		labelingRuleList.add(new LabelingRuleListDataItem("Auxiliare", "ingListDataItem.isProcessingAid == true", LabelingRuleType.Omit, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		
		
//		└──[root - 0.0 (1.0)]
//			    └──[Pâte french - 1.0 (3.0)]
//			        ├──[ing1 french - 0.33333333333333337]
//			        ├──[ing2 french - 0.6666666666666667]
//			        └──[Legal Raw material 12 - 2.0 (2.0)]
//			            └──[ing1 french - 0.5]

		
		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 100% (Legal Raw material 12 66,7% (ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%)", Locale.FRENCH);
		
		//Test Omit IngType
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.Aggregate, Arrays.asList(ing4),Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Omit", null, LabelingRuleType.Omit, Arrays.asList(ingType1), null));

		//		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.33333333333333337]
//			    │   ├──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing5 french - 1.0]



		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing2 french, ing1 french), ing2 french, ing1 french), Garniture french 50% (ing3 french, ing5 french)", Locale.FRENCH);



		//Details
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Details", null, LabelingRuleType.Detail, Arrays.asList(rawMaterial11NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));

		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[Legal Raw material 11 - 1.0 (1.0)]
//			    │   │   ├──[ing1 french - 0.33333333333333337]
//			    │   │   └──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]
		
		checkILL(finishedProductNodeRef1, labelingRuleList,"Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), Legal Raw material 11 33,3% (ing2 french 66,7%, ing1 french 33,3%)), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)", Locale.FRENCH);
		
		//Rename 
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 1", null, LabelingRuleType.Rename, Arrays.asList(ing1), Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.Aggregate, Arrays.asList(ing4),Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 2", "Test rename2", LabelingRuleType.Rename, Arrays.asList(rawMaterial12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 3", "path.allergens", LabelingRuleType.Rename, Arrays.asList(ingType1), null));
		
		
		
		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.33333333333333337]
//			    │   ├──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing5 french - 1.0]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Test rename2 66,7% (ing2 french, ing5 french), ing2 french, ing5 french), Garniture french 50% (ing3 french, Allergènes: ing5 french)", Locale.FRENCH);
		

		
		//Aggregate
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.Aggregate, Arrays.asList(ing1,ing2),Arrays.asList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.Aggregate, Arrays.asList(ing4),Arrays.asList(ing5)));
		
		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing3 french - 1.0]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       └──[ing3 french - 2.0]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing5 french - 1.0]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing3 french), ing3 french), Garniture french 50% (ing3 french, Epaississant french: ing5 french)", Locale.FRENCH);
		
		
		// Group
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "renderGroupList()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 1", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 2", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Group ", null, LabelingRuleType.Group, Arrays.asList(localSF11NodeRef, localSF12NodeRef),null));
		
		
//		[
//		└──[Pâte french - 1.0 (4.0)]
//		    ├──[ing1 french - 0.33333333333333337]
//		    ├──[ing2 french - 0.6666666666666667]
//		    └──[Legal Raw material 12 - 2.0 (2.0)]
//		        ├──[ing1 french - 0.5]
//		        └──[ing2 french - 1.5]
//		, 
//		└──[Garniture french - 1.0 (7.0)]
//		    ├──[ing3 french - 5.0]
//		    └──[ing4 french - 1.0]
//		]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "ing2 french 34,07 %, ing1 french 9,26 %, ing3 french, ing4 french", Locale.FRENCH);
		
		//Todo test aggregate type or MP
		//TODO omit type
		

		
		//Combine
													
		
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Combine 1", "{20,30}", LabelingRuleType.Combine, Arrays.asList(ing1,ing2),null));
		
		
		
		//Do not Declare
				labelingRuleList = new ArrayList<>();
				labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
				labelingRuleList.add(new LabelingRuleListDataItem("Do not declare", null, LabelingRuleType.Detail, Arrays.asList(rawMaterial11NodeRef), null));
				labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
				
				
				checkILL(finishedProductNodeRef1, labelingRuleList, "ing2 french 34,07 %, ing1 french 9,26 %, ing3 french, ing4 french", Locale.FRENCH);
				
			
		checkILL(finishedProductNodeRef1, labelingRuleList, "ing2 french 34,07 %, ing1 french 9,26 %, ing3 french, ing4 french", Locale.FRENCH);

		
	
		

	}

	private void checkILL(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String ill, Locale french) {
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				ProductData formulatedProduct = alfrescoRepository.findOne(productNodeRef);

				formulatedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

				productService.formulate(formulatedProduct);

				assertTrue(formulatedProduct.getLabelingListView().getLabelingRuleList().size() > 0);

				// verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct.getLabelingListView().getIngLabelingList());
				assertTrue(formulatedProduct.getLabelingListView().getIngLabelingList().size() > 0);

				for (IngLabelingListDataItem illDataItem : formulatedProduct.getLabelingListView().getIngLabelingList()) {
					String formulatedIll = illDataItem.getValue().getValue(Locale.FRENCH);
					assertEquals("Incorrect label :"+formulatedIll+"\n   - compare to "+ill, ill,formulatedIll );

				}

				return null;

			}
		}, false, true);

	}

	// @Test
	public void testILLwithIngTypes() throws Exception {

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Langue", "fr,en", LabelingRuleType.Locale));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing2, ing3, ing4, ing5), null));

		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

				formulatedProduct1
						.getCompoListView()
						.getCompoList()
						.add(new CompoListDataItem(null, formulatedProduct1.getCompoListView().getCompoList().get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare,
								rawMaterial7NodeRef));

				alfrescoRepository.save(formulatedProduct1);

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef1);

				/*-- Verify formulation --*/
				formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

				// verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct1.getLabelingListView().getIngLabelingList());
				for (IngLabelingListDataItem illDataItem : formulatedProduct1.getLabelingListView().getIngLabelingList()) {

					String trace = "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
					logger.info(trace);

					checkILL("Garniture french 50% (ing3 french 55,6%, ing4 french 11,1%, Epaississant french: ing5 french 33,3%)",
							"Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french), ing2 french 22,2%, ing1 french)",
							illDataItem.getValue().getValue(Locale.FRENCH));

					checkILL("Garniture english 50% (ing3 english 55,6%, ing4 english 11,1%, Epaississant english: ing5 english 33,3%)",
							"Pâte english 50% (Legal Raw material 12 66,7% (ing2 english 75%, ing1 english), ing2 english 22,2%, ing1 english)",
							illDataItem.getValue().getValue(Locale.ENGLISH));
				}

				return null;

			}
		}, false, true);

	}

}
