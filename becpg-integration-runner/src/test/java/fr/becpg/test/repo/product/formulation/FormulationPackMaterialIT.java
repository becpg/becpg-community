/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

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

	/** The product service. */
	@Autowired
	protected ProductService productService;

	/** The pf noderef. */
	private NodeRef PF1NodeRef;

	/** The sf noderef. */
	private NodeRef SF1NodeRef;

	/** The raw material noderef. */
	private NodeRef rawMaterial1NodeRef;
	private NodeRef rawMaterial2NodeRef;

	/** The packaging noderef. */
	private NodeRef packaging1NodeRef;
	private NodeRef packaging2NodeRef;
	private NodeRef packaging3NodeRef;

	/** The packaging material noderef. */
	private NodeRef packMaterial1NodeRef;
	private NodeRef packMaterial2NodeRef;
	private NodeRef packMaterial3NodeRef;
	private NodeRef packMaterial4NodeRef;
	private NodeRef packMaterial5NodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// Creation of compo and packaging noderef
		initPart();
	}

	/**
	 * Test formulate product.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationPackMaterial() throws Exception {

		logger.info("testFormulationPackMaterial");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, PF1NodeRef));// Allu
																																// 20
																																// /
																																// Carton
																																// 40
			compoList.add(new CompoListDataItem(null, null, null, 500d, ProductUnit.g, 0d, DeclarationType.Declare, SF1NodeRef));// Fer
																																	// 60
																																	// /
																																	// Plastique
																																	// 80
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.lb, 0d, DeclarationType.Declare, rawMaterial1NodeRef));// Verre
																																			// 56.699
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.oz, 0d, DeclarationType.Declare, rawMaterial2NodeRef));// no
																																			// material
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<PackagingListDataItem> packList = new ArrayList<>();
			packList.add(new PackagingListDataItem(null, 3d, ProductUnit.g, PackagingLevel.Primary, true, packaging1NodeRef));// Allu
																																// 20
																																// +
																																// 3g
																																// =
																																// 23
			packList.add(new PackagingListDataItem(null, 1d, ProductUnit.oz, PackagingLevel.Primary, true, packaging2NodeRef));// Carton
																																// 40
																																// +
																																// 28.349523125g
																																// =
																																// 68.35
			packList.add(new PackagingListDataItem(null, 1d, ProductUnit.lb, PackagingLevel.Primary, true, packaging3NodeRef));// Fer
																																// 60
																																// +
																																// 226.80
																																// =
																																// 286.80
																																// /
																																// Plastique
																																// =
																																// 80
																																// +
																																// 226.8
																																// =
																																// 306.8
			finishedProduct.getPackagingListView().setPackagingList(packList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(finishedProductNodeRef);
			if(entityListDAO.getList(listContainerNodeRef, PackModel.PACK_MATERIAL_LIST_TYPE) == null) {
				entityListDAO.createList(listContainerNodeRef, PackModel.PACK_MATERIAL_LIST_TYPE);
			}
			productService.formulate(finishedProductNodeRef);

			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			// Mon test
			int checks = 0;
			assertEquals(formulatedProduct.getPackMaterialList().size(), 5);
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
			}

			assertEquals("Verify checks done", 5, checks);

			return null;

		}, false, true);

	}

	private void initPart() {

		logger.info("/*-- Create compo product --*/");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Characteristics --*/
			Map<QName, Serializable> properties = new HashMap<>();

			/*-- Pack materials --*/
			properties.put(BeCPGModel.PROP_LV_VALUE, "Alluminium");
			packMaterial1NodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PackModel.TYPE_PACKAGING_MATERIAL, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Carton");
			packMaterial2NodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PackModel.TYPE_PACKAGING_MATERIAL, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Fer");
			packMaterial3NodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PackModel.TYPE_PACKAGING_MATERIAL, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Plastique");
			packMaterial4NodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PackModel.TYPE_PACKAGING_MATERIAL, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Verre");
			packMaterial5NodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PackModel.TYPE_PACKAGING_MATERIAL, properties).getChildRef();

			/*-- Creation of CompoList Elements --*/

			/*-- Finished product 1 --*/
			FinishedProductData PF1 = new FinishedProductData();
			PF1.setName("Finished product 1");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");
			PF1.setLegalName(mlName);
			PF1.setQty(500d);
			PF1.setUnit(ProductUnit.g);

			List<PackMaterialListDataItem> packMaterial = new ArrayList<>();
			packMaterial.add(new PackMaterialListDataItem(packMaterial1NodeRef, 10d, PackagingLevel.Primary));
			packMaterial.add(new PackMaterialListDataItem(packMaterial2NodeRef, 20d, PackagingLevel.Primary));
			PF1.setPackMaterialList(packMaterial);
			PF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), PF1).getNodeRef();

			/*-- Semi finished product 1 --*/
			SemiFinishedProductData SF1 = new SemiFinishedProductData();
			SF1.setName("Semi finished 1");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");
			SF1.setLegalName(mlName);
			SF1.setQty(250d);
			SF1.setUnit(ProductUnit.g);

			packMaterial = new ArrayList<>();
			packMaterial.add(new PackMaterialListDataItem(packMaterial3NodeRef, 30d, PackagingLevel.Primary));
			packMaterial.add(new PackMaterialListDataItem(packMaterial4NodeRef, 40d, PackagingLevel.Primary));
			SF1.setPackMaterialList(packMaterial);
			SF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), SF1).getNodeRef();

			/*-- Raw material 1 --*/
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			rawMaterial1.setDensity(1d);
			MLText legalName = new MLText("Legal Raw material 1");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 1");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 1");
			rawMaterial1.setLegalName(legalName);
			rawMaterial1.setQty(400d);
			rawMaterial1.setUnit(ProductUnit.g);

			packMaterial = new ArrayList<>();
			packMaterial.add(new PackMaterialListDataItem(packMaterial5NodeRef, 50d, PackagingLevel.Primary));
			rawMaterial1.setPackMaterialList(packMaterial);
			rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

			/*-- Raw material 2 (no packMaterial list) --*/
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			legalName = new MLText("Legal Raw material 2");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 2");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 2");
			rawMaterial2.setLegalName(legalName);
			rawMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

			/*-- Creation of PackagingList Elements --*/

			/*-- Packaging 1 --*/
			PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
			packagingMaterial1.setName("Packaging material 1");
			packagingMaterial1.setLegalName("Legal Packaging material 1");
			packagingMaterial1.setTare(0.015d);
			packagingMaterial1.setTareUnit(TareUnit.kg);
			packagingMaterial1.getPackagingMaterials().add(packMaterial1NodeRef);
			packaging1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial1).getNodeRef();

			/*-- Packaging 2 --*/
			PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
			packagingMaterial2.setName("Packaging material 2");
			packagingMaterial2.setLegalName("Legal Packaging material 2");
			packagingMaterial2.setTare(0.5d);
			packagingMaterial2.setTareUnit(TareUnit.oz);
			packagingMaterial2.getPackagingMaterials().add(packMaterial2NodeRef);
			packaging2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial2).getNodeRef();

			/*-- Packaging 3 --*/
			PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
			packagingMaterial3.setName("Packaging material 3");
			packagingMaterial3.setLegalName("Legal Packaging material 3");
			packagingMaterial3.getPackagingMaterials().add(packMaterial3NodeRef);
			packagingMaterial3.getPackagingMaterials().add(packMaterial4NodeRef);
			packaging3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial3).getNodeRef();

			return null;

		}, false, true);
	}

}
