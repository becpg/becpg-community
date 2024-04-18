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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Test costs of template are taken in account
 *
 * @author quere
 *
 */
public class FormulationCostsIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationCostsIT.class);

	@Autowired
	protected AlfrescoRepository<ClientData> clientRepository;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Test cost product.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFormulationCostsFromTemplate() throws Exception {

		logger.info("testFormulationCostsFromTemplate");

		final ProductData entityTpl = inWriteTx(() -> {

			// template
			FinishedProductData templateFinishedProduct = new FinishedProductData();
			templateFinishedProduct.setName("Template Produit fini");
			List<CostListDataItem> costList = new LinkedList<>();
			costList.add(new CostListDataItem(null, null, "€/kg", null, parentCost, false));
			costList.add(new CostListDataItem(null, 12d, "€/kg", 24d, cost1, true));
			costList.get(1).setParent(costList.get(0));
			costList.add(new CostListDataItem(null, 16d, "€/P", 24d, cost2, false));
			costList.get(2).setParent(costList.get(0));
			List<NodeRef> plants = new ArrayList<>();
			plants.add(plant1);
			costList.add(new CostListDataItem(null, 2000d, "€/Pal", 2400d, cost3, false, plants, null, null));
			plants = new ArrayList<>();
			plants.add(plant2);
			costList.add(new CostListDataItem(null, 4000d, "€/Pal", 4400d, cost3, false, plants, null, null));
			templateFinishedProduct.setCostList(costList);
			ProductData entityTpl1 = alfrescoRepository.create(getTestFolderNodeRef(), templateFinishedProduct);
			nodeService.addAspect(entityTpl1.getNodeRef(), BeCPGModel.ASPECT_ENTITY_TPL, null);

			return entityTpl1;
		});

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			// Client
			ClientData client = new ClientData();
			client.setName("client");
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(new CostListDataItem(null, 1d, "€/kg", 3d, cost4, false)));
			client.setCostList(costList);
			client = clientRepository.create(getTestFolderNodeRef(), client);

			// product
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setEntityTpl(entityTpl);
			List<NodeRef> plants = new ArrayList<>();
			plants.add(plant1);
			finishedProduct.setPlants(plants);

			List<PackagingListDataItem> packList = new ArrayList<>();
			packList.add(PackagingListDataItem.build().withQty(25d).withUnit(ProductUnit.PP)
					.withPkgLevel(PackagingLevel.Secondary).withIsMaster(true).withProduct(packagingKit1NodeRef));
			/*
			 * packList.add(new PackagingListDataItem(null, 25d, ProductUnit.PP,
			 * PackagingLevel.Secondary, true, packagingKit1NodeRef));
			 */
			finishedProduct.getPackagingListView().setPackagingList(packList);

			finishedProduct = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);
			// assoc is readonly
			ArrayList<NodeRef> clientNodeRefs = new ArrayList<>();
			clientNodeRefs.add(client.getNodeRef());
			associationService.update(finishedProduct.getNodeRef(), PLMModel.ASSOC_CLIENTS, clientNodeRefs);

			return finishedProduct.getNodeRef();

		});

		inWriteTx(() -> {

			productService.formulate(finishedProductNodeRef);
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			for (CostListDataItem c1 : formulatedProduct.getCostList()) {
				String trace1 = "cost: " + nodeService.getProperty(c1.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + c1.getValue() + " - unit: " + c1.getUnit() + " level: " + c1.getDepthLevel();
				logger.info(trace1);

				assertEquals("€/kg", c1.getUnit());
				if (c1.getCost().equals(parentCost)) {
					assertEquals(20d, c1.getValue());
					assertEquals(36d, c1.getMaxi());
				} else if (c1.getCost().equals(cost1)) {
					assertEquals(12d, c1.getValue());
					assertEquals(24d, c1.getMaxi());
				} else if (c1.getCost().equals(cost2)) {
					assertEquals(8d, c1.getValue());
					assertEquals(12d, c1.getMaxi());
				} else if (c1.getCost().equals(cost3)) {
					// 1000 finished product on pallet
					assertEquals(1d, c1.getValue());
					assertEquals(1.2d, c1.getMaxi());
				} else if (c1.getCost().equals(cost4)) {
					assertEquals(1d, c1.getValue());
					assertEquals(3d, c1.getMaxi());
				} else {
					assertFalse(true);
				}
			}

			assertEquals(5, formulatedProduct.getCostList().size());
			assertEquals(TareUnit.g, formulatedProduct.getTareUnit());

			assertEquals(44d, formulatedProduct.getUnitTotalCost());

			// change data
			for (CostListDataItem c2 : formulatedProduct.getCostList()) {
				if (c2.getCost().equals(cost1)) {
					c2.setValue(11d);
				} else if (c2.getCost().equals(cost2)) {
					c2.setValue(300d);
				} else if (c2.getCost().equals(cost3)) {
					c2.setValue(20d);
				} else if (c2.getCost().equals(cost4)) {
					c2.setValue(20d);
				}
			}

			alfrescoRepository.save(formulatedProduct);
			productService.formulate(finishedProductNodeRef);
			formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			for (CostListDataItem c3 : formulatedProduct.getCostList()) {

				String trace2 = "cost: " + nodeService.getProperty(c3.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + c3.getValue() + " - unit: " + c3.getUnit();
				logger.info(trace2);

				assertEquals("€/kg", c3.getUnit());
				if (c3.getCost().equals(parentCost)) {
					assertEquals(19d, c3.getValue());
					assertEquals(36d, c3.getMaxi());
				} else if (c3.getCost().equals(cost1)) {
					assertEquals(11d, c3.getValue());
					assertEquals(24d, c3.getMaxi());
				} else if (c3.getCost().equals(cost2)) {
					assertEquals(8d, c3.getValue());
					assertEquals(12d, c3.getMaxi());
				} else if (c3.getCost().equals(cost3)) {
					// 1000 finished product on pallet
					assertEquals(1d, c3.getValue());
					assertEquals(1.2d, c3.getMaxi());
				} else if (c3.getCost().equals(cost4)) {
					assertEquals(1d, c3.getValue());
					assertEquals(3d, c3.getMaxi());
				} else {
					assertFalse(true);
				}
			}

			assertEquals(42d, formulatedProduct.getUnitTotalCost());

			return null;

		});

	}

	@Test
	public void testFormulationCostsWithSimulation() throws Exception {

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(1d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(2d)
					.withUnit(ProductUnit.kg).withLossPerc(1d).withDeclarationType(DeclarationType.Detail)
					.withProduct(rawMaterial2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d,
			 * ProductUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial3NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit)
					.withProduct(rawMaterial4NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
			 */
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new LinkedList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			costList.add(new CostListDataItem(null, null, "€/kg", null, cost5, true));
			costList.get(2).setParent(costList.get(0));
			costList.get(2).setComponentNodeRef(rawMaterial1NodeRef);
			costList.get(2).setSimulatedValue(2d);
			finishedProduct.setCostList(costList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			productService.formulate(finishedProductNodeRef);
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			// costs
			int checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace = "cost: "
						+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + costListDataItem.getValue() + " - previous value: "
						+ costListDataItem.getPreviousValue() + " - future value: " + costListDataItem.getFutureValue()
						+ " - unit: " + costListDataItem.getUnit();
				logger.info(trace);
				if (costListDataItem.getCost().equals(cost1) && (costListDataItem.getComponentNodeRef() == null)) {
					assertEquals(3.5d, costListDataItem.getValue());
					assertEquals(2d, costListDataItem.getPreviousValue());
					assertEquals(8d, costListDataItem.getFutureValue());
					assertEquals("€/kg", costListDataItem.getUnit());
					assertEquals(7d, costListDataItem.getValuePerProduct());
					checks++;
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals(6.0d, costListDataItem.getValue());
					assertEquals(3d, costListDataItem.getPreviousValue());
					assertEquals(12d, costListDataItem.getFutureValue());
					assertEquals("€/kg", costListDataItem.getUnit());
					assertEquals(12d, costListDataItem.getValuePerProduct());
					checks++;
				}
				if (costListDataItem.getCost().equals(cost5) && (costListDataItem.getComponentNodeRef() != null)) {
					assertEquals(2d, costListDataItem.getSimulatedValue());
					assertEquals(-0.5d, costListDataItem.getValue());
					assertEquals("€/kg", costListDataItem.getUnit());
					assertEquals(-1d, costListDataItem.getValuePerProduct());
					checks++;
				}
			}
			assertEquals(3, checks);

			return null;

		});
	}

	@Test
	public void testFormulationCostsWithPackagingKitCostSimulation() {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Packaging kit --*/
			ProductData packagingKit = alfrescoRepository.findOne(packagingKit1NodeRef);

			// Packaging list Of packaging kit
			List<PackagingListDataItem> kitPackList = new ArrayList<>();
			kitPackList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P)
					.withPkgLevel(PackagingLevel.Secondary).withIsMaster(true).withProduct(packagingMaterial2NodeRef));
			/*
			 * kitPackList.add(new PackagingListDataItem(null, 1d, ProductUnit.P,
			 * PackagingLevel.Secondary, true, packagingMaterial2NodeRef));
			 */
			kitPackList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P)
					.withPkgLevel(PackagingLevel.Tertiary).withIsMaster(true).withProduct(packagingMaterial3NodeRef));
			/*
			 * kitPackList.add(new PackagingListDataItem(null, 1d, ProductUnit.P,
			 * PackagingLevel.Tertiary, true, packagingMaterial3NodeRef));
			 */

			packagingKit.getPackagingListView().setPackagingList(kitPackList);

			List<CostListDataItem> costList = new LinkedList<>();

			// Cost list Of packaging kit
			costList.add(new CostListDataItem(null, null, null, null, pkgCost1, null));
			packagingKit.setCostList(costList);

			alfrescoRepository.save(packagingKit);

			/*-- Finished product --*/
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("finished product 1");
			finishedProduct.setUnit(ProductUnit.P);
			finishedProduct.setQty(2d);

			// Packaging list Of finished product
			List<PackagingListDataItem> finishedProductPackList = new ArrayList<>();
			finishedProduct.getPackagingListView().setPackagingList(finishedProductPackList);
			finishedProductPackList.add(PackagingListDataItem.build().withQty(8d).withUnit(ProductUnit.PP)
					.withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial1NodeRef));
			/*
			 * finishedProductPackList.add(new PackagingListDataItem(null, 8d,
			 * ProductUnit.P, PackagingLevel.Primary, true, packagingMaterial1NodeRef));
			 */
			finishedProductPackList.add(PackagingListDataItem.build().withQty(10d).withUnit(ProductUnit.PP)
					.withPkgLevel(PackagingLevel.Secondary).withIsMaster(true).withProduct(packagingKit1NodeRef));
			/*
			 * finishedProductPackList.add(new PackagingListDataItem(null, 10d,
			 * ProductUnit.PP, PackagingLevel.Secondary, true, packagingKit1NodeRef));
			 */

			finishedProduct = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);

			// Cost list Of finished product
			costList.clear();
			costList.add(new CostListDataItem(null, null, null, null, pkgCost1, null));
			costList.add(new CostListDataItem(null, null, "€/P", null, cost5, true));
			costList.get(1).setParent(costList.get(0));
			costList.get(1).setComponentNodeRef(packagingMaterial2NodeRef);
			costList.get(1).setSimulatedValue(3d);
			finishedProduct.setCostList(costList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			productService.formulate(finishedProductNodeRef);
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			int checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace = "cost: "
						+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + costListDataItem.getValue() + " - previous value: "
						+ costListDataItem.getPreviousValue() + " - future value: " + costListDataItem.getFutureValue()
						+ " - unit: " + costListDataItem.getUnit();
				logger.info(trace);
				if (costListDataItem.getCost().equals(pkgCost1) && (costListDataItem.getComponentNodeRef() == null)) {
					assertEquals(12.15125d, costListDataItem.getValue());
					checks++;
				}
				if (costListDataItem.getCost().equals(cost5)) {
					logger.info("cost 5 simulated value :" + costListDataItem.getSimulatedValue());
					assertEquals(0.1d, costListDataItem.getValue());
					assertEquals(3d, costListDataItem.getSimulatedValue());
					checks++;
				}
			}
			assertEquals(2, checks);

			return null;
		});

	}

	@Test
	public void testFormulationCostsWithKeepProductUnit() throws Exception {
		try {

			inWriteTx(() -> {
				systemConfigurationService.updateConfValue("beCPG.formulation.costList.keepProductUnit", "true");
				return null;
			});

			final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

				RawMaterialData rawMaterial = new RawMaterialData();
				rawMaterial.setName("Raw material keepProductUnit cost");
				rawMaterial.setUnit(ProductUnit.g);
				// costList
				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, 20d, null, null, cost1, false));
				rawMaterial.setCostList(costList);

				return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
			});

			final NodeRef finishedProductNodeRef = inWriteTx(() -> {

				/*-- Create finished product --*/
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.g).withLossPerc(0d)
						.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
				/*
				 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.g, 0d,
				 * DeclarationType.Detail, rawMaterialNodeRef));
				 */
				finishedProduct.getCompoListView().setCompoList(compoList);

				List<CostListDataItem> costList = new LinkedList<>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				finishedProduct.setCostList(costList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			});

			inWriteTx(() -> {

				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

				ProductData rawMaterialProduct = alfrescoRepository.findOne(rawMaterialNodeRef);

				assertEquals("€/g", rawMaterialProduct.getCostList().get(0).getUnit());

				// costs
				int checks = 0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
					String trace = "cost: "
							+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
							+ " - value: " + costListDataItem.getValue() + " - previous value: "
							+ costListDataItem.getPreviousValue() + " - future value: "
							+ costListDataItem.getFutureValue() + " - unit: " + costListDataItem.getUnit();
					logger.info(trace);
					if (costListDataItem.getCost().equals(cost1) && (costListDataItem.getComponentNodeRef() == null)) {
						assertEquals(20000d, costListDataItem.getValue());
						assertEquals("€/kg", costListDataItem.getUnit());
						assertEquals(20d, costListDataItem.getValuePerProduct());
						checks++;
					}

				}
				assertEquals(1, checks);

				return null;

			});
		} finally {
			systemConfigurationService.resetConfValue("beCPG.formulation.costList.keepProductUnit");
		}
	}

	@Test
	public void testFormulationCostsWithoutKeepProductUnit() throws Exception {

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material keepProductUnit cost");
			rawMaterial.setUnit(ProductUnit.g);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 20d, null, null, cost1, false));
			rawMaterial.setCostList(costList);

			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
		});

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.g, 0d,
			 * DeclarationType.Detail, rawMaterialNodeRef));
			 */
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new LinkedList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			finishedProduct.setCostList(costList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			productService.formulate(finishedProductNodeRef);
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			ProductData rawMaterialProduct = alfrescoRepository.findOne(rawMaterialNodeRef);

			assertEquals("€/kg", rawMaterialProduct.getCostList().get(0).getUnit());

			// costs
			int checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace = "cost: "
						+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + costListDataItem.getValue() + " - previous value: "
						+ costListDataItem.getPreviousValue() + " - future value: " + costListDataItem.getFutureValue()
						+ " - unit: " + costListDataItem.getUnit();
				logger.info(trace);
				if (costListDataItem.getCost().equals(cost1) && (costListDataItem.getComponentNodeRef() == null)) {
					assertEquals(20d, costListDataItem.getValue());
					assertEquals("€/kg", costListDataItem.getUnit());
					assertEquals(0.02d, costListDataItem.getValuePerProduct());
					checks++;
				}

			}
			assertEquals(1, checks);

			return null;

		});

	}

	@Test
	public void testFormulationCostsWithLandLbUnits() throws Exception {

		try {

			final NodeRef varnishNodeRef = inWriteTx(() -> {

				// varnish packList
				PackagingMaterialData varnish = new PackagingMaterialData();
				varnish.setName("varnish Packaging keepProductUnit cost");
				varnish.setUnit(ProductUnit.L);

				// varnish costList
				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, 5d, null, null, pkgCost1, null));
				varnish.setCostList(costList);

				return alfrescoRepository.create(getTestFolderNodeRef(), varnish).getNodeRef();

			});

			final NodeRef finishedProductNodeRef = inWriteTx(() -> {
				/*-- Create process steps, resources --*/
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(BeCPGModel.PROP_CHARACT_NAME, "Etape emb");
				properties.put(PLMModel.PROP_COSTCURRENCY, "€");

				ResourceProductData processRessource = new ResourceProductData();
				processRessource.setName("Process");
				List<CostListDataItem> costList = new LinkedList<>();
				costList.add(new CostListDataItem(null, 5d, "€/h", null, cost3, false));
				processRessource.setCostList(costList);
				NodeRef emballageResourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), processRessource)
						.getNodeRef();

				/*-- Create finished product --*/
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setUnit(ProductUnit.lb);

				// compoList
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.lb).withLossPerc(0d)
						.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial8NodeRef));
				/*
				 * compoList.add(new CompoListDataItem(null, null, null, 0.5d, ProductUnit.lb,
				 * 0d, DeclarationType.Detail, rawMaterial8NodeRef));
				 */
				// 5€/lb
				// ->
				// 2.5
				compoList.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.lb).withLossPerc(0d)
						.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial8NodeRef));
				/*
				 * compoList.add(new CompoListDataItem(null, null, null, 0.5d, ProductUnit.lb,
				 * 0d, DeclarationType.Detail, rawMaterial8NodeRef));
				 */
				// 1€/kg
				// ->
				// 0.226796185
				finishedProduct.getCompoListView().setCompoList(compoList);

				// packList
				List<PackagingListDataItem> packList = new ArrayList<>();
				packList.add(PackagingListDataItem.build().withQty(2d).withUnit(ProductUnit.L)
						.withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(varnishNodeRef));
				/*
				 * packList.add(new PackagingListDataItem(null, 2d, ProductUnit.L,
				 * PackagingLevel.Primary, true, varnishNodeRef));
				 */
				// 5€/L
				// ->
				// 10€/lb
				finishedProduct.getPackagingListView().setPackagingList(packList);

				// processList
				List<ProcessListDataItem> processList = new ArrayList<>();

				processList.add(new ProcessListDataItem(null, 1d, 1d, 1d, ProductUnit.lb, null, null, null, null,
						emballageResourceNodeRef));
				// 5€/h

				finishedProduct.getProcessListView().setProcessList(processList);

				// costList
				costList = new LinkedList<>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, pkgCost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost3, null));
				finishedProduct.setCostList(costList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			});

			inWriteTx(() -> {

				productService.formulate(finishedProductNodeRef);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

				ProductData varnishProduct = alfrescoRepository.findOne(varnishNodeRef);

				// costs test
				assertEquals("€/L", varnishProduct.getCostList().get(0).getUnit());

				DecimalFormat df = new DecimalFormat("0.####");
				int checks = 0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
					String trace = "cost: "
							+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
							+ " - value: " + costListDataItem.getValue() + " - previous value: "
							+ costListDataItem.getPreviousValue() + " - future value: "
							+ costListDataItem.getFutureValue() + " - unit: " + costListDataItem.getUnit();
					logger.info(trace);
					if (costListDataItem.getCost().equals(cost1) && (costListDataItem.getComponentNodeRef() == null)) {
						assertEquals("€/lb", costListDataItem.getUnit());
						assertEquals(df.format(2.726796185d), df.format(costListDataItem.getValue()));
						assertEquals(df.format(2.726796185d), df.format(costListDataItem.getValuePerProduct()));
					}
					if (costListDataItem.getCost().equals(pkgCost1)
							&& (costListDataItem.getComponentNodeRef() == null)) {
						assertEquals("€/lb", costListDataItem.getUnit());
						assertEquals(df.format(10d), df.format(costListDataItem.getValue()));
						assertEquals(df.format(10d), df.format(costListDataItem.getValuePerProduct()));
					}
					if (costListDataItem.getCost().equals(cost3) && (costListDataItem.getComponentNodeRef() == null)) {
						assertEquals("€/lb", costListDataItem.getUnit());
						assertEquals(df.format(5d), df.format(costListDataItem.getValue()));
						assertEquals(df.format(5d), df.format(costListDataItem.getValuePerProduct()));
					}

					checks++;

				}
				assertEquals(3, checks);

				return null;

			});

		} finally {
		}
	}
}
