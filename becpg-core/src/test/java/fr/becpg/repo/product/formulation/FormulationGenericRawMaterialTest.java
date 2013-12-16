package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.AbstractFinishedProductTest;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;

/**
 * Test the formulation of a generic raw material
 * @author quere
 *
 */
public class FormulationGenericRawMaterialTest extends AbstractFinishedProductTest {

	protected static Log logger = LogFactory.getLog(FormulationGenericRawMaterialTest.class);

	@Resource
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testFormulationGenericRawMaterial() throws Exception {

		logger.info("testFormulationGenericRawMaterial");

		final NodeRef genRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				RawMaterialData genRawMaterial = new RawMaterialData();
				genRawMaterial.setName("Gen RM");
				genRawMaterial.setUnit(ProductUnit.kg);
				genRawMaterial.setQty(1d);				
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));				
				genRawMaterial.getCompoListView().setCompoList(compoList);

				return alfrescoRepository.create(testFolderNodeRef, genRawMaterial).getNodeRef();

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				productService.formulate(genRawMaterialNodeRef);

				RawMaterialData formulatedProduct = (RawMaterialData) alfrescoRepository.findOne(genRawMaterialNodeRef);
				
				assertEquals(2, formulatedProduct.getSuppliers().size());
				assertTrue(formulatedProduct.getSuppliers().contains(supplier1));
				assertTrue(formulatedProduct.getSuppliers().contains(supplier2));

				return null;

			}
		}, false, true);
	}
}
