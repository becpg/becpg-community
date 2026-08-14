/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.helper.AssociationService;
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
import fr.becpg.repo.product.report.ProductReportExtractorPlugin;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.test.PLMBaseTestCase;

public class FormulationPackMaterialIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(FormulationPackMaterialIT.class);

	@Autowired
	protected ProductService productService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	@Qualifier("productReportExtractor")
	private ProductReportExtractorPlugin productReportExtractor;

	protected NodeRef PF1NodeRef;
	protected NodeRef SF1NodeRef;
	protected NodeRef rawMaterial1NodeRef;
	protected NodeRef rawMaterial2NodeRef;
	protected NodeRef rawMaterial3NodeRef;
	protected NodeRef rawMaterial4NodeRef;
	protected NodeRef packaging1NodeRef;
	protected NodeRef packaging2NodeRef;
	protected NodeRef packaging3NodeRef;
	protected NodeRef packaging4NodeRef;
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

	/**
	 * A packaging flagged as recycled is not destroyed by the product: it must contribute neither to
	 * the costs nor to the packaging materials (see #35780). Only the packaging materials coming from
	 * the composition and from the non-recycled packaging are expected here.
	 */
	@Test
	public void testRecycledPackagingIsExcludedFromMaterials() throws Exception {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			FinishedProductData finishedProduct = FinishedProductData.build().withName("Produit fini recyclage").withUnit(ProductUnit.kg).withQty(1d)
					.withDensity(1d)
					.withCompoList(List.of(
							// Allu 20 / Carton 40
							CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
									.withProduct(PF1NodeRef),
							// Plastique 2 pieces * 12g = 24g
							CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.P).withDeclarationType(DeclarationType.Declare)
									.withProduct(rawMaterial4NodeRef)))
					.withPackagingList(List.of(
							// recycled: its 3g of Alluminium must be ignored
							PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Primary)
									.withIsRecycle(true).withProduct(packaging1NodeRef),
							// kept: adds 28.349523125g of Carton
							PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.oz).withPkgLevel(PackagingLevel.Primary)
									.withProduct(packaging2NodeRef),
							// recycled: its Fer and Plastique must be ignored
							PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.lb).withPkgLevel(PackagingLevel.Primary)
									.withIsRecycle(true).withProduct(packaging3NodeRef)));

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);

			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			DecimalFormat df = new DecimalFormat("0.###");
			int checks = 0;

			for (PackMaterialListDataItem packMaterialListDataItem : formulatedProduct.getPackMaterialList()) {
				if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial1NodeRef)) {
					assertEquals("Recycled packaging must not add Alluminium", df.format(20d), df.format(packMaterialListDataItem.getPmlWeight()));
					checks++;
				}
				if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial2NodeRef)) {
					assertEquals("Non recycled packaging must still add Carton", df.format(40d + 28.349523125d),
							df.format(packMaterialListDataItem.getPmlWeight()));
					checks++;
				}
				if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial3NodeRef)) {
					fail("Fer only comes from the recycled packaging and must not appear");
				}
				if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial4NodeRef)) {
					assertEquals("Recycled packaging must not add Plastique", df.format(24d),
							df.format(packMaterialListDataItem.getPmlWeight()));
					checks++;
				}
			}

			assertEquals("Verify checks done", 3, checks);
			return null;
		});
	}

	/**
	 * A packaging that only references its materials through pack:pmMaterialRefs splits its tare evenly
	 * between them.
	 */
	@Test
	public void testFormulationPackMaterialFromPackagingMaterials() throws Exception {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			FinishedProductData finishedProduct = FinishedProductData.build().withName("Produit fini pmMaterialRefs").withUnit(ProductUnit.P)
					.withQty(1d)
					.withPackagingList(List.of(PackagingListDataItem.build().withQty(2d).withUnit(ProductUnit.P)
							.withPkgLevel(PackagingLevel.Primary).withProduct(packaging4NodeRef)));

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);

			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			DecimalFormat df = new DecimalFormat("0.###");
			int checks = 0;

			// 0.02kg * 2 pieces = 40g, evenly split between Alluminium and Papier
			for (PackMaterialListDataItem packMaterialListDataItem : formulatedProduct.getPackMaterialList()) {
				if (packMaterialListDataItem.getPmlMaterial().equals(packMaterial1NodeRef)
						|| packMaterialListDataItem.getPmlMaterial().equals(packMaterial6NodeRef)) {
					assertEquals(df.format(20d), df.format(packMaterialListDataItem.getPmlWeight()));
					checks++;
				}
			}

			assertEquals("Verify checks done", 2, checks);
			return null;
		});
	}

	/**
	 * The extractPackagingMaterials preference details, for each packaging line, the materials it is
	 * made of. The weights must match the contribution of that line to the formulated packMaterialList
	 * (see #31702).
	 */
	@Test
	public void testExtractPackagingMaterialsInReport() throws Exception {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			FinishedProductData finishedProduct = FinishedProductData.build().withName("Produit fini rapport").withUnit(ProductUnit.kg).withQty(1d)
					.withDensity(1d)
					.withPackagingList(List.of(
							// Alluminium: 3g
							PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Primary)
									.withProduct(packaging1NodeRef),
							// Fer and Plastique: 453.592g / 2
							PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.lb).withPkgLevel(PackagingLevel.Primary)
									.withProduct(packaging3NodeRef),
							// Alluminium and Papier: 40g / 2
							PackagingListDataItem.build().withQty(2d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary)
									.withProduct(packaging4NodeRef)));

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);

			Map<String, String> preferences = new HashMap<>();
			preferences.put("extractPackagingMaterials", "true");

			EntityReportData reportData = productReportExtractor.extract(finishedProductNodeRef, preferences);
			assertNotNull(reportData.getXmlDataSource());

			List<String> extractedMaterials = extractPackagingMaterials(reportData);
			logger.info("Extracted packaging materials: " + extractedMaterials);

			assertEquals("One line per material of each packaging", 5, extractedMaterials.size());
			assertTrue(extractedMaterials.contains("Alluminium|3|100"));
			assertTrue(extractedMaterials.contains("Fer|226.796|50"));
			assertTrue(extractedMaterials.contains("Plastique|226.796|50"));
			assertTrue(extractedMaterials.contains("Alluminium|20|50"));
			assertTrue(extractedMaterials.contains("Papier|20|50"));

			return null;
		});
	}

	@SuppressWarnings("unchecked")
	private List<String> extractPackagingMaterials(EntityReportData reportData) {
		DecimalFormat df = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		List<String> extractedMaterials = new ArrayList<>();

		List<Node> packMaterialElts = reportData.getXmlDataSource()
				.selectNodes("/entity/dataLists/packagingLists/packagingList/packMaterialLists/packMaterialList");

		for (Node packMaterialElt : packMaterialElts) {
			Element packMaterialListElt = (Element) packMaterialElt;
			extractedMaterials.add(String.join("|", packMaterialListElt.attributeValue(PackModel.ASSOC_PACK_MATERIAL_LIST_MATERIAL.getLocalName()),
					df.format(Double.valueOf(packMaterialListElt.attributeValue(PackModel.PROP_PACK_MATERIAL_LIST_WEIGHT.getLocalName()))),
					df.format(Double.valueOf(packMaterialListElt.attributeValue(PackModel.PROP_PACK_MATERIAL_LIST_PERC.getLocalName())))));
		}

		return extractedMaterials;
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
				.withProduct(rawMaterial3NodeRef),
				// Plastique 2 pieces * 12g = 24g
				CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.P).withDeclarationType(DeclarationType.Declare)
				.withProduct(rawMaterial4NodeRef))
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
				assertEquals(df.format(80d + (453.592 / 2) + 24d), df.format(packMaterialListDataItem.getPmlWeight()));
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

		/*-- Raw material 4 (piece based) --*/
		RawMaterialData rawMaterial4 = RawMaterialData.build().withName("Raw material 4").withUnit(ProductUnit.P).withNetWeight(0.05);
		
		rawMaterial4.setPackMaterialList(
				List.of(PackMaterialListDataItem.build().withMaterial(packMaterial4NodeRef).withWeight(12d).withPkgLevel(PackagingLevel.Primary)));

		rawMaterial4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial4).getNodeRef();

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

		/*-- Packaging 4 (materials referenced through pack:pmMaterialRefs, no packMaterialList) --*/

		PackagingMaterialData packagingMaterial4 = PackagingMaterialData.build().withName("Packaging material 4").withTare(0.02d, TareUnit.kg);

		packaging4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial4).getNodeRef();
		associationService.update(packaging4NodeRef, PackModel.ASSOC_PM_MATERIAL, List.of(packMaterial1NodeRef, packMaterial6NodeRef));
	}

}
