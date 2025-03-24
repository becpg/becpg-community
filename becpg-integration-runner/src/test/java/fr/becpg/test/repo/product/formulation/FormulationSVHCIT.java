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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * @author Matthieu
 */
public class FormulationSVHCIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(FormulationSVHCIT.class);

	@Autowired
	protected ProductService productService;

	@Test
	public void testFormulationSVHC() {
		logger.info("Starting testFormulationPackMaterial");
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.info("Creating finished product");
			FinishedProductData finishedProduct = createTestProduct();
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);
			verifySVHCList(finishedProductNodeRef);
			return null;
		});
	}

	private FinishedProductData createTestProduct() {
		return FinishedProductData.build().withName("Produit fini 1").withUnit(ProductUnit.kg)
				.withCompoList(createCompoList()).withPackagingList(createPackagingList());
	}

	private List<PackagingListDataItem> createPackagingList() {

		PackagingMaterialData primaryPackagingMaterial1 = PackagingMaterialData.build().withName("Packaging material 1").withTare(1d, TareUnit.kg);

		primaryPackagingMaterial1.setSvhcList(List.of(SvhcListDataItem.build().withIngredient(ings.get(0)).withQtyPerc(50d).withMigrationPerc(10d),
				SvhcListDataItem.build().withIngredient(ings.get(1)).withQtyPerc(10d),
				SvhcListDataItem.build().withIngredient(ings.get(2)).withQtyPerc(40d).withMigrationPerc(100d)));

		PackagingMaterialData primaryPackagingMaterial2 = PackagingMaterialData.build().withName("Packaging material 2").withTare(1d, TareUnit.kg);

		primaryPackagingMaterial2.setSvhcList(List.of(SvhcListDataItem.build().withIngredient(ings.get(1)).withQtyPerc(50d).withMigrationPerc(100d)));
		
		PackagingMaterialData primaryPackagingMaterial3 = PackagingMaterialData.build().withName("Packaging material 3").withTare(1d, TareUnit.kg);

		primaryPackagingMaterial3.setSvhcList(List.of(SvhcListDataItem.build().withIngredient(ings.get(2)).withQtyPerc(50d).withMigrationPerc(100d)));
		
		PackagingKitData packagingKitData1 = PackagingKitData.build().withName("Packaging kit 1").withPackagingList(List.of(
				PackagingListDataItem.build().withQty(1d).withIsMaster(true).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Secondary)
						.withProduct(createNode(primaryPackagingMaterial2))));

		PackagingKitData packagingKitData2 = PackagingKitData.build().withName("Packaging kit 2").withPackagingList(List.of(
				PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary)
				.withProduct(createNode(primaryPackagingMaterial3))));

		
		return List.of(
				PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true).withPkgLevel(PackagingLevel.Primary)
						.withProduct(createNode(primaryPackagingMaterial1)),
				PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.PP).withIsMaster(true).withPkgLevel(PackagingLevel.Secondary)
						.withProduct(createNode(packagingKitData1)),
				PackagingListDataItem.build().withQty(5d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary)
				.withProduct(createNode(packagingKitData2)));
	}

	private List<CompoListDataItem> createCompoList() {

		RawMaterialData rawMaterial1 = RawMaterialData.build().withName("Raw material 1").withUnit(ProductUnit.g);

		rawMaterial1.setSvhcList(List.of(SvhcListDataItem.build().withIngredient(ings.get(0)).withQtyPerc(50d)));

		RawMaterialData rawMaterial2 = RawMaterialData.build().withName("Raw material 2").withUnit(ProductUnit.g);

		rawMaterial2.setSvhcList(List.of(SvhcListDataItem.build().withIngredient(ings.get(0)).withQtyPerc(10d)));

		RawMaterialData genericRawMaterial1 = RawMaterialData.build().withName("Generic raw material 1").withUnit(ProductUnit.g);

		genericRawMaterial1.getCompoListView()
				.setCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(createNode(rawMaterial1)),
						CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(createNode(rawMaterial2))));

		RawMaterialData rawMaterial3 = RawMaterialData.build().withName("Raw material 3").withUnit(ProductUnit.g);

		//Ensure ings3 is SVHC

		nodeService.setProperty(ings.get(3), PLMModel.PROP_IS_SVHC, true);

		rawMaterial3.setIngList(List.of(IngListDataItem.build().withIngredient(ings.get(3)).withQtyPerc(10d)));

		SemiFinishedProductData sf1 = SemiFinishedProductData.build().withName("Semi finished 1").withUnit(ProductUnit.g)
				.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.g).withProduct(createNode(rawMaterial3))

				));

		return List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(createNode(sf1)),
				CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withProduct(createNode(genericRawMaterial1)));
	}

	private NodeRef createNode(ProductData productData) {
		return alfrescoRepository.create(getTestFolderNodeRef(), productData).getNodeRef();
	}

	/**
	 * Finished Product:
	  - Name: Produit fini 1
	  - Unit: kg
	  - Quantity: 1
	  - Density: 1
	  
	  Compo List:
	    - Product: Semi finished 1
	      - Quantity: 1 kg
	      - Components:
	        - Raw material 3: 50% (SVHC Ingredient 4: 10 %)
	     - Expected SVHC:
	       - Ingredient: SVHC Ingredient 4 (10%)
	    - Product: Generic raw material 1
	      - Quantity: 3 kg
	      - Components:
	        - Raw material 1: 50% (SVHC Ingredient 0 : 50 %)
	        - Raw material 2: 50% (SVHC Ingredient 0 : 10 %)
	      - Expected SVHC:
	        - SVHC Ingredient 0: 50%
	  
	  Packaging List:
	    - Packaging material 1 (Primary Packaging / Master):
	      - Quantity: 1 Piece
	      - SVHC List:
	        - Ingredient: SVHC Ingredient 1 (50%)
	          - Migration Percentage: 10%
	        - Ingredient: SVHC Ingredient 2 (10%)
	        - Ingredient: SVHC Ingredient 3 (40%)
	          - Migration Percentage: 100%
	       Secondary Packaging Kit 1 (Master)
	       - Quantity: 1 Piece
	       - Packaging List:
	         - Secondary Packaging Material 2 (Primary)
	         	  - Ingredient: SVHC Ingredient 1 (50%)
	                 - Migration Percentage: 100%
			   
	      Primary Packaging Kit 2
			   - Quantity: 5 Pieces
			   - Packaging List:
			     - Primary Packaging Material 3
			     	 - Ingredient: SVHC Ingredient 3 (50%)
	                    - Migration Percentage: 100%
	          
	  - Expected SVHC:
	     - SVHC Ingredient 1 = 50 * 10/100 / 4  +  50*3/4 % = 38,75d
	     - SVHC Ingredient 2 = None
	     - SVHC Ingredient 3 = 40 /4d + 5* 50 /4  % =   72,5d
	     - SVHC Ingredient 4 = 10 * 1 / 4 % = 2.5d
	 */
	private void verifySVHCList(NodeRef finishedProductNodeRef) {
		logger.info("Verifying formulated SVHC");
		ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
		int checks = 0;

		for (SvhcListDataItem svhcListDataItem : formulatedProduct.getSvhcList()) {
			if (svhcListDataItem.getIng().equals(ings.get(0))) {
				// Calculate expected SVHC percentage for Ingredient 1
				assertEquals((50 * 10/100d) / 4d  +  50*3/4d, svhcListDataItem.getQtyPerc());
				checks++;
			} else if (svhcListDataItem.getIng().equals(ings.get(2))) {
				// SVHC percentage for Ingredient 3 is directly provided
				assertEquals( 40d /4d + 5d* 50d /4d , svhcListDataItem.getQtyPerc());
				checks++;
			}	else if (svhcListDataItem.getIng().equals(ings.get(3))) {
					// SVHC percentage for Ingredient 3 is directly provided
					assertEquals(10 * 1 / 4d , svhcListDataItem.getQtyPerc());
					checks++;
					
			} else if (svhcListDataItem.getIng().equals(ings.get(1))) {
				// Ensure ing2 is not present in the list
				fail("Ingredient 2 should not be present in the SVHC list.");
			}
		}

		// Ensure checks for ing1 and ing3 are performed
		assertEquals("Verify checks done", 3, checks);
	}

}
