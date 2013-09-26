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

	private NodeRef createTestProduct(final List<LabelingRuleListDataItem> labelingRuleList ) {
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

				NodeRef finishedProductNodeRef1 = alfrescoRepository.create(testFolderNodeRef, finishedProduct1).getNodeRef();

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef1);

				return finishedProductNodeRef1;
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
	public void testLabelingFormulation() throws Exception {

		logger.info("testLabelingFormulation");

		
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Langue", "fr,en", LabelingRuleType.Locale));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format ,Arrays.asList(ing2, ing3, ing4),null));
		
		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
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
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Declare",null, LabelingRuleType.Declare ,Arrays.asList(localSF11NodeRef, rawMaterial12NodeRef, localSF12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format ,Arrays.asList(ing2, ing3, ing4),null));
		
		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

				// (1 * (1/3 Ing1 + 2/3 Ing2) + 2 * (1/4 Ing1 + 3/4 Ing2) + 3 *
				// Ing3 + 3 * (0 * Ing3 + 0,3 * Ing2 + 0 * ing4)) / (1+2+3+3)
				// Ing1 =(1/3+2*1/4+3)/9
				// Ing2 =(2/3+2*3/4+3*0,3)/9
				// no detail
//				formulatedProduct1.getCompoListView().getCompoList().get(0).setDeclType(DeclarationType.Declare);
//				formulatedProduct1.getCompoListView().getCompoList().get(2).setDeclType(DeclarationType.Declare);
//				formulatedProduct1.getCompoListView().getCompoList().get(3).setDeclType(DeclarationType.Declare);
//				formulatedProduct1.getCompoListView().getCompoList().get(5).setDeclType(DeclarationType.Declare);
//				alfrescoRepository.save(formulatedProduct1);
//
//				productService.formulate(finishedProductNodeRef1);
//
//				formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

				// verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct1.getLabelingListView().getIngLabelingList());
				assertEquals(1, formulatedProduct1.getLabelingListView().getIngLabelingList().size());
				
				
				//Pâte english 50% (Legal Raw material 12 66,7% (ing2 english 75%, ing1 english), ing2 english 22,2%, ing1 english), 
				  //Garniture english 50% (ing3 english 83,3%, ing4 english 16,7%
				
				
				//ing3 french 83,3%, ing4 french 16,7%
				logger.error(formulatedProduct1.getLabelingListView().getIngLabelingList().get(0).getValue()
						.getValue(Locale.FRENCH));
				
				assertEquals("ing2 french 34,07 %, ing1 french 9,26 %, ing3 french, ing4 french", formulatedProduct1.getLabelingListView().getIngLabelingList().get(0).getValue()
						.getValue(Locale.FRENCH));
				
				return null;

			}
		}, false, true);

	}

	@Test
	public void testILLwithIngTypes() throws Exception {


		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Langue", "fr,en", LabelingRuleType.Locale));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format ,Arrays.asList(ing2, ing3, ing4,ing5),null));
		
		
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
