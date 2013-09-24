package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
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

	/**
	 * Test ingredients calculating.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testLabelingFormulation() throws Exception {

		logger.info("testLabelingFormulation");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
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
			
				
				List<LabelingRuleListDataItem> labelingRuleList =  new ArrayList<>();
				
				labelingRuleList.add(new LabelingRuleListDataItem("Test","render()",LabelingRuleType.Render));
				
				finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);
				
				
				NodeRef finishedProductNodeRef1 = alfrescoRepository.create(testFolderNodeRef, finishedProduct1).getNodeRef();
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef1);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

				
				assertTrue(formulatedProduct1.getLabelingListView().getLabelingRuleList().size()>0);

				// verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct1.getLabelingListView().getIngLabelingList());
				assertTrue(formulatedProduct1.getLabelingListView().getIngLabelingList().size()>0);
				
				for (IngLabelingListDataItem illDataItem : formulatedProduct1.getLabelingListView().getIngLabelingList()) {

					logger.error("grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH));
					
					   //Pâte french 50,00 (ing1 french, ing2 frenchLegal Raw material 12 66,67 (ing1 french, ing2 french))Garniture french 50,00 (ing3 french, ing4 french)
					
						checkILL("Garniture french 50,00 % (ing3 french 83,33 %, ing4 french 16,67 %)",
								"Pâte french 50,00 % (Legal Raw material 12 66,67 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 22,22 %, ing1 french 11,11 %)",
								illDataItem.getValue().getValue(Locale.FRENCH));

						checkILL("Garniture english 50.00 % (ing3 english 83.33 %, ing4 english 16.67 %)",
								"Pâte english 50.00 % (Legal Raw material 12 66.67 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 22.22 %, ing1 english 11.11 %)",
								illDataItem.getValue().getValue(Locale.ENGLISH));
					
				} 

				
				return null;

			}
		}, false, true);

	}

}
