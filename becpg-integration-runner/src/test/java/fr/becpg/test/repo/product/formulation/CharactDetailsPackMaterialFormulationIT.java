/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.icu.text.DecimalFormat;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;

/**
 * The Class FormulationTest.
 *
 */
public class CharactDetailsPackMaterialFormulationIT extends FormulationPackMaterialIT {

	protected static final Log logger = LogFactory.getLog(CharactDetailsPackMaterialFormulationIT.class);

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test formulate product and check pack material details
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFormulateCharactDetailsPackMaterial() throws Exception {

		logger.info("testFormulateCharactDetailsPackMaterial");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(PF1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, PF1NodeRef));
			 */
			// Allu
			// 20
			// /
			// Carton
			// 40
			compoList.add(CompoListDataItem.build().withQtyUsed(500d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(SF1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 500d, ProductUnit.g,
			 * 0d, DeclarationType.Declare, SF1NodeRef));
			 */
			// Fer
			// 60
			// /
			// Plastique
			// 80
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.lb).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.lb, 0d,
			 * DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			// Verre
			// 56.699
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.oz).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.oz, 0d,
			 * DeclarationType.Declare, rawMaterial2NodeRef));
			 */
			// no
			// material
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<PackagingListDataItem> packList = new ArrayList<>();
			packList.add(PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.g)
					.withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packaging1NodeRef));
			/*
			 * packList.add(new PackagingListDataItem(null, 3d, ProductUnit.g,
			 * PackagingLevel.Primary, true, packaging1NodeRef));
			 */
			// Allu
			// 20
			// +
			// 3g
			// =
			// 23
			packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.oz)
					.withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packaging2NodeRef));
			/*
			 * packList.add(new PackagingListDataItem(null, 1d, ProductUnit.oz,
			 * PackagingLevel.Primary, true, packaging2NodeRef));
			 */
			// Carton
			// 40
			// +
			// 28.349523125g
			//
			// 68.35
			packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.lb)
					.withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packaging3NodeRef));
			/*
			 * packList.add(new PackagingListDataItem(null, 1d, ProductUnit.lb,
			 * PackagingLevel.Primary, true, packaging3NodeRef));
			 */
			// Fer
			// 60
			// +
			// 226,796
			// =
			// 286.796
			// /
			// Plastique
			// =
			// 80
			// +
			// 226,796
			// =
			// 306.796
			finishedProduct.getPackagingListView().setPackagingList(packList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(finishedProductNodeRef);
			if (entityListDAO.getList(listContainerNodeRef, PackModel.PACK_MATERIAL_LIST_TYPE) == null) {
				entityListDAO.createList(listContainerNodeRef, PackModel.PACK_MATERIAL_LIST_TYPE);
			}

			// formulate Details
			List<NodeRef> packMaterialNodeRefs = new ArrayList<>();
			productService.formulate(finishedProductNodeRef);
			CharactDetails ret = productService.formulateDetails(finishedProductNodeRef,
					PackModel.PACK_MATERIAL_LIST_TYPE, "packMaterialList", packMaterialNodeRefs, null);

			Assert.assertNotNull(ret);
			logger.info(CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService).toString(3));

			// pack material
			int checks = 0;
			DecimalFormat df = new DecimalFormat("0.###");
			for (Map.Entry<NodeRef, List<CharactDetailsValue>> kv : ret.getData().entrySet()) {

				for (CharactDetailsValue kv2 : kv.getValue()) {

					String trace = "material: " + nodeService.getProperty(kv.getKey(), BeCPGModel.PROP_LV_VALUE)
							+ " - source: " + kv2.getKeyNodeRef() + " - value: " + kv2.getValue();
					logger.info(trace);

					// material 1 (Alluminium)
					if (kv.getKey().equals(packMaterial1NodeRef)) {

						if (kv2.getKeyNodeRef().equals(PF1NodeRef)) {
							checks++;
							assertEquals(df.format(20d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging1NodeRef)) {
							checks++;
							assertEquals(df.format(3d), df.format(kv2.getValue()));
						}
					}

					// material 2 (Carton)
					else if (kv.getKey().equals(packMaterial2NodeRef)) {

						if (kv2.getKeyNodeRef().equals(PF1NodeRef)) {
							checks++;
							assertEquals(df.format(40d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging2NodeRef)) {
							checks++;
							assertEquals(df.format(28.349523125d), df.format(kv2.getValue()));
						}
					}

					// material 3 (Fer)
					else if (kv.getKey().equals(packMaterial3NodeRef)) {

						if (kv2.getKeyNodeRef().equals(SF1NodeRef)) {
							checks++;
							assertEquals(df.format(60d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging3NodeRef)) {
							checks++;
							assertEquals(df.format(226.796d), df.format(kv2.getValue()));
						}
					}

					// material 4 (Plastique)
					else if (kv.getKey().equals(packMaterial4NodeRef)) {

						if (kv2.getKeyNodeRef().equals(SF1NodeRef)) {
							checks++;
							assertEquals(df.format(80d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging3NodeRef)) {
							checks++;
							assertEquals(df.format(226.796d), df.format(kv2.getValue()));
						}
					}
					// material 5 (Verre)
					else if (kv.getKey().equals(packMaterial5NodeRef)) {

						if (kv2.getKeyNodeRef().equals(rawMaterial1NodeRef)) {
							checks++;
							assertEquals(df.format(56.699d), df.format(kv2.getValue()));
						}
					}

				}

			}

			assertEquals("Verify checks done", 9, checks);

			return null;

		});

	}

}
