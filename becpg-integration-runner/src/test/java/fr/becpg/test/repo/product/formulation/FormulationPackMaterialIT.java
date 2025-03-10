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
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.PLMBaseTestCase;

public class FormulationPackMaterialIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(FormulationPackMaterialIT.class);

	@Autowired
	protected ProductService productService;

	protected NodeRef PF1NodeRef;
	protected NodeRef SF1NodeRef;
	protected NodeRef rawMaterial1NodeRef;
	protected NodeRef rawMaterial2NodeRef;
	protected NodeRef rawMaterial3NodeRef;
	protected NodeRef packaging1NodeRef;
	protected NodeRef packaging2NodeRef;
	protected NodeRef packaging3NodeRef;
	protected NodeRef packMaterial1NodeRef;
	protected NodeRef packMaterial2NodeRef;
	protected NodeRef packMaterial3NodeRef;
	protected NodeRef packMaterial4NodeRef;
	protected NodeRef packMaterial5NodeRef;
	protected NodeRef packMaterial6NodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initPart();
	}

	@Test
	public void testFormulationPackMaterial() throws Exception {
		logger.info("Starting testFormulationPackMaterial");
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.info("Creating finished product");
			FinishedProductData finishedProduct = createFinishedProduct();
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);
			verifyFormulatedPackMaterial(finishedProductNodeRef);
			return null;
		});
	}

	protected FinishedProductData createFinishedProduct() {
		return FinishedProductData.build().withName("Produit fini 1").withUnit(ProductUnit.kg).withQty(1d).withDensity(1d).withCompoList(List.of(
				CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare).withProduct(PF1NodeRef),
				// Allu 20 / Carton 40
				CompoListDataItem.build().withQtyUsed(500d).withUnit(ProductUnit.g).withDeclarationType(DeclarationType.Declare).withProduct(SF1NodeRef),
				// Fer 60 / Plastique 80
				CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.lb).withDeclarationType(DeclarationType.Declare)
						.withProduct(rawMaterial1NodeRef),
				// Verre 56.699
				CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.oz).withDeclarationType(DeclarationType.Declare)
						.withProduct(rawMaterial2NodeRef),
				// no material
				CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.g).withDeclarationType(DeclarationType.Declare)
				.withProduct(rawMaterial3NodeRef))
				// Papier 50 % 30 * 12 = 20g
				)
				.withPackagingList(List.of(
						PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Primary)
								.withProduct(packaging1NodeRef),
								// Allu 20 + 3g = 23
						PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.oz).withPkgLevel(PackagingLevel.Primary)
								.withProduct(packaging2NodeRef),
								// Carton 40 + 28.349523125g = 68.35
						PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.lb).withPkgLevel(PackagingLevel.Primary)
								.withProduct(packaging3NodeRef))
							// Fer 60 + 226,796 = 286.796 / Plastique = 80 + 226,796 = 306.796);
						);
	}

	private void verifyFormulatedPackMaterial(NodeRef finishedProductNodeRef) {
		logger.info("Verifying formulated pack materials");
		ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
		int checks = 0;
		DecimalFormat df = new DecimalFormat("0.###");

		for (PackMaterialListDataItem packMaterialListDataItem : formulatedProduct.getPackMaterialList()) {
			if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial1NodeRef)) {
				assertEquals(df.format(23d), df.format(packMaterialListDataItem.getPmlWeight()));
				checks++;
			}
			if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial2NodeRef)) {
				assertEquals(df.format(40d + 28.349523125d), df.format(packMaterialListDataItem.getPmlWeight()));
				checks++;
			}
			if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial3NodeRef)) {
				assertEquals(df.format(60d + (453.592 / 2)), df.format(packMaterialListDataItem.getPmlWeight()));
				checks++;
			}
			if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial4NodeRef)) {
				assertEquals(df.format(80d + (453.592 / 2)), df.format(packMaterialListDataItem.getPmlWeight()));
				checks++;
			}
			if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial5NodeRef)) {
				assertEquals(df.format(56.699d), df.format(packMaterialListDataItem.getPmlWeight()));
				checks++;
			}
			if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial6NodeRef)) {
				assertEquals(df.format(20d), df.format(packMaterialListDataItem.getPmlWeight()));
				checks++;
			}
		}
		assertEquals("Verify checks done", 6, checks);
	}

	private void initPart() {
		logger.info("Initializing test data");
		inWriteTx(() -> {
			createPackMaterials();
			createCompoProducts();
			createPackagingMaterials();
			return null;
		});
	}

	private void createPackMaterials() {
		packMaterial1NodeRef = createPackMaterialNode("Alluminium");
		packMaterial2NodeRef = createPackMaterialNode("Carton");
		packMaterial3NodeRef = createPackMaterialNode("Fer");
		packMaterial4NodeRef = createPackMaterialNode("Plastique");
		packMaterial5NodeRef = createPackMaterialNode("Verre");
		packMaterial6NodeRef = createPackMaterialNode("Papier");
	}

	private NodeRef createPackMaterialNode(String materialName) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, materialName);
		return nodeService
				.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, materialName), PackModel.TYPE_PACKAGING_MATERIAL, properties)
				.getChildRef();
	}

	private void createCompoProducts() {
		FinishedProductData PF1 = FinishedProductData.build().withName("Finished product 1").withQty(500d).withUnit(ProductUnit.g);

		PF1.setPackMaterialList(Arrays.asList(
				PackMaterialListDataItem.build().withMaterial(packMaterial1NodeRef).withWeight(10d).withPerc(5d).withPkgLevel(PackagingLevel.Primary),
				PackMaterialListDataItem.build().withMaterial(packMaterial2NodeRef).withWeight(20d).withPkgLevel(PackagingLevel.Primary)));

		PF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), PF1).getNodeRef();

		/*-- Semi finished product 1 --*/

		SemiFinishedProductData SF1 = SemiFinishedProductData.build().withName("Semi finished 1").withQty(250d).withUnit(ProductUnit.g);

		SF1.setPackMaterialList(
				List.of(PackMaterialListDataItem.build().withMaterial(packMaterial3NodeRef).withWeight(30d).withPkgLevel(PackagingLevel.Primary),
						PackMaterialListDataItem.build().withMaterial(packMaterial4NodeRef).withWeight(40d).withRecycledPerc(10d)
								.withPkgLevel(PackagingLevel.Primary)));

		SF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), SF1).getNodeRef();

		/*-- Raw material 1 --*/

		RawMaterialData rawMaterial1 = RawMaterialData.build().withName("Raw material 1").withQty(400d).withUnit(ProductUnit.g);

		rawMaterial1.setPackMaterialList(
				List.of(PackMaterialListDataItem.build().withMaterial(packMaterial5NodeRef).withWeight(50d).withPkgLevel(PackagingLevel.Primary)));

		rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

		/*-- Raw material 2 (no packMaterial list) --*/
		RawMaterialData rawMaterial2 = RawMaterialData.build().withName("Raw material 2");

		rawMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();
		
		/*-- Raw material 2 (no packMaterial list) --*/
		RawMaterialData rawMaterial3 = RawMaterialData.build().withName("Raw material 3").withUnit(ProductUnit.P).withNetWeight(0.03);
		
		rawMaterial3.setPackMaterialList(
				List.of(PackMaterialListDataItem.build().withMaterial(packMaterial6NodeRef).withWeight(12d).withPkgLevel(PackagingLevel.Primary)));

		rawMaterial3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial3).getNodeRef();

	}

	private void createPackagingMaterials() {
		PackagingMaterialData packagingMaterial1 = PackagingMaterialData.build().withName("Packaging material 1").withTare(0.015d, TareUnit.kg)
				.withPackMaterialList(List.of(PackMaterialListDataItem.build().withMaterial(packMaterial1NodeRef).withWeight(0.015d * 1000)
						.withRecycledPerc(50d).withPkgLevel(PackagingLevel.Primary)));

		packaging1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial1).getNodeRef();

		/*-- Packaging 2 --*/

		PackagingMaterialData packagingMaterial2 = PackagingMaterialData.build().withName("Packaging material 2").withTare(0.5d, TareUnit.oz)
				.withPackMaterialList(List.of(PackMaterialListDataItem.build().withMaterial(packMaterial2NodeRef).withWeight(14.1748d)
						.withPkgLevel(PackagingLevel.Primary)));
		packaging2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial2).getNodeRef();

		/*-- Packaging 3 --*/

		PackagingMaterialData packagingMaterial3 = PackagingMaterialData.build().withName("Packaging material 3").withTare(1d, TareUnit.kg)
				.withPackMaterialList(List.of(
						PackMaterialListDataItem.build().withMaterial(packMaterial3NodeRef).withWeight(500d).withPkgLevel(PackagingLevel.Primary),
						PackMaterialListDataItem.build().withMaterial(packMaterial4NodeRef).withWeight(500d).withPkgLevel(PackagingLevel.Primary)));

		packaging3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial3).getNodeRef();

	}

}
