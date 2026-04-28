package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationOmitConflictIT extends AbstractFinishedProductTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
	}

	@Test
	public void testIngredientsCalculatingWithOmitAndNotOmitConflict() {
		logger.info("testIngredientsCalculatingWithOmitAndNotOmitConflict");

		NodeRef finishedProductNodeRef = inWriteTx(() -> {
			// Create RM with Omitted Ingredient
			RawMaterialData rmOmit = new RawMaterialData();
			rmOmit.setName("RM Omit");
			rmOmit.setLegalName(new MLText("RM Omit"));
			rmOmit.setDensity(1d);
			rmOmit.setSuppliers(Collections.singletonList(supplier1));
			
			List<IngListDataItem> ingListOmit = new ArrayList<>();
			// ing1 is Omitted here. Qty = 50%
			IngListDataItem ingItemOmit = IngListDataItem.build()
					.withQtyPerc(50d)
					.withIngredient(ing1);
			ingItemOmit.setDeclType(DeclarationType.Omit);
			ingListOmit.add(ingItemOmit);
			rmOmit.setIngList(ingListOmit);
			
			NodeRef rmOmitRef = alfrescoRepository.create(getTestFolderNodeRef(), rmOmit).getNodeRef();

			// Create RM with Not Omitted Ingredient
			RawMaterialData rmNotOmit = new RawMaterialData();
			rmNotOmit.setName("RM Not Omit");
			rmNotOmit.setLegalName(new MLText("RM Not Omit"));
			rmNotOmit.setDensity(1d);
			rmNotOmit.setSuppliers(Collections.singletonList(supplier1));
			
			List<IngListDataItem> ingListNotOmit = new ArrayList<>();
			// ing1 is Declared here. Qty = 50%
			IngListDataItem ingItemNotOmit = IngListDataItem.build()
					.withQtyPerc(50d)
					.withIngredient(ing1);
			ingItemNotOmit.setDeclType(DeclarationType.Declare);
			ingListNotOmit.add(ingItemNotOmit);
			rmNotOmit.setIngList(ingListNotOmit);
			
			NodeRef rmNotOmitRef = alfrescoRepository.create(getTestFolderNodeRef(), rmNotOmit).getNodeRef();

			// Create Finished Product
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product Omit Conflict");
			finishedProduct.setLegalName("FP Omit Conflict");
			finishedProduct.setQty(2d); // 1kg RM Omit + 1kg RM Not Omit
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			// 1 kg of RM Omit (contains 0.5kg ing1 omitted)
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmOmitRef));
			// 1 kg of RM Not Omit (contains 0.5kg ing1 declared)
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmNotOmitRef));
			
			finishedProduct.getCompoListView().setCompoList(compoList);
			
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {
			// Formulate
			productService.formulate(finishedProductNodeRef);

			// Verify
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			
			assertNotNull("IngList should not be null", formulatedProduct.getIngList());
			
			// We expect ing1 to be present with 50% total.
			// Total Weight = 2kg.
			// Ing1 from RM Omit = 1kg * 50% = 0.5kg (Omitted in RM, but should be added to total because of conflict)
			// Ing1 from RM Not Omit = 1kg * 50% = 0.5kg
			// Total Ing1 = 1.0kg
			// % Ing1 = 1.0kg / 2kg = 50%
			
			boolean found = false;
			for (IngListDataItem item : formulatedProduct.getIngList()) {
				if (item.getIng().equals(ing1)) {
					found = true;
					logger.info("Found ing1 with qty: " + item.getQtyPerc() + " and declType: " + item.getDeclType());
					
					// Should be Declared (or Detail, but definitely not Omit if we want it visible, actually logic says if Omit and Not Omit -> Not Omit wins)
					// In the code: if (!isOmit) ... setDeclType(ingListDataItem.getDeclType()) which is Declare.
					
					// Verify Quantity
					assertEquals("Quantity should include omitted part", 50.00d, item.getQtyPerc(), 0.01d);
				}
			}
			if (!found) {
				throw new AssertionError("ing1 not found in formulated product ingredient list");
			}
			
			return null;
		});
	}
}
