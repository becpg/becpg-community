/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.test.repo.ecm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.PLMBaseTestCase;

/**
 * ECO test class
 * 
 * @author quere
 * 
 */
public class ECOTest extends PLMBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ECOTest.class);

	/** The product service. */
	@Resource
	private ProductService productService;

	@Resource
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Resource
	private ECOService ecoService;
	
	@Resource
	private VersionService versionService;
	
	@Resource
	private EntityVersionService entityVersionService;

	public static final String Double_FORMAT = "0.0000";

	/** The local s f1 node ref. */
	private NodeRef localSF1NodeRef;

	/** The raw material1 node ref. */
	private NodeRef rawMaterial1NodeRef;

	/** The raw material2 node ref. */
	private NodeRef rawMaterial2NodeRef;

	/** The local s f2 node ref. */
	private NodeRef localSF2NodeRef;

	/** The raw material3 node ref. */
	private NodeRef rawMaterial3NodeRef;

	/** The raw material4 node ref. */
	private NodeRef rawMaterial4NodeRef;

	/** The raw material5 node ref. */
	private NodeRef rawMaterial5NodeRef;

	/** The cost1. */
	private NodeRef cost1;

	/** The cost2. */
	private NodeRef cost2;

	/** The nut1. */
	private NodeRef nut1;

	/** The nut2. */
	private NodeRef nut2;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		cost1 = costs.get(0);
		cost2 = costs.get(1);

		nut1 = nuts.get(0);
		nut2 = nuts.get(1);

		// create RM and lSF
		initParts();

	}

	/**
	 * Inits the parts.
	 */
	private void initParts() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- create raw materials --*/
				logger.debug("/*-- create raw materials --*/");
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");
				rawMaterial1.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial1.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3d, "€/kg", null, cost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false));
				rawMaterial1.setCostList(costList);
				// nutList
				List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial1.setNutList(nutList);

				rawMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial1).getNodeRef();

				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");
				rawMaterial2.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial2.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 1d, "€/kg", null, cost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false));
				rawMaterial2.setCostList(costList);
				// nutList
				nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial2.setNutList(nutList);
				rawMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial2).getNodeRef();

				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");
				rawMaterial3.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial3.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 1d, "€/kg", null, cost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false));
				rawMaterial3.setCostList(costList);
				// nutList
				nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial3.setNutList(nutList);
				rawMaterial3NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial3).getNodeRef();

				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");
				rawMaterial4.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial4.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				rawMaterial4NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial4).getNodeRef();

				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");
				rawMaterial5.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial5.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 5d, "€/m", null, cost1, false));
				costList.add(new CostListDataItem(null, 6d, "€/m", null, cost2, false));
				rawMaterial5.setCostList(costList);
				// nutList
				nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 3d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial5.setNutList(nutList);
				rawMaterial5NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial5).getNodeRef();

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				localSF1.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				localSF1NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF1).getNodeRef();

				/*-- Local semi finished product 2 --*/
				LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");
				localSF2.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				localSF2.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				localSF2NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF2).getNodeRef();

				return null;

			}
		}, false, true);
	}

	/**
	 * create a finished product
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public NodeRef createFinishedProduct(final String finishedProductName) throws Exception {

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- create finished product --*/
				logger.debug("/*-- create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName(finishedProductName);
				finishedProduct.setLegalName("Legal name");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setHierarchy1(HIERARCHY1_FROZEN_REF);
				finishedProduct.setHierarchy2(HIERARCHY2_PIZZA_REF);
				finishedProduct.setDensity(1d);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();

				compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, null, CompoListUnit.g, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));

				finishedProduct.getCompoListView().setCompoList(compoList);

				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				finishedProduct.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
				finishedProduct.setNutList(nutList);

				return alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();

			}
		}, false, true);

		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				logger.debug("unit of product to formulate: " + finishedProduct.getUnit());

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);

				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");

				ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				logger.debug("unit of product formulated: " + finishedProduct.getUnit());
				logger.debug("Finish product: " + formulatedProduct.toString());
				// costs
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {

					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue()
							+ " - unit: " + costListDataItem.getUnit();
					logger.info(trace);
					if (costListDataItem.getCost().equals(cost1)) {
						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if (costListDataItem.getCost().equals(cost2)) {
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				// nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: "
							+ nutListDataItem.getUnit();
					logger.info(trace);
					if (nutListDataItem.getNut().equals(nut1)) {
						assertEquals("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
					}
					if (nutListDataItem.getNut().equals(nut2)) {
						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6d, nutListDataItem.getValue());
					}
				}

				return finishedProductNodeRef;

			}
		}, false, true);

	}

	/**
	 * Test ecoService
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testECOService() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*
				 * create a change order to replace RM4 by RM5
				 */

				logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

				List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>();
				calculatedCharacts.add(cost1);
				calculatedCharacts.add(cost2);
				calculatedCharacts.add(nut1);
				calculatedCharacts.add(nut2);
				ChangeOrderData changeOrderData = new ChangeOrderData( "ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<ReplacementListDataItem>();

				replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = alfrescoRepository.create(testFolderNodeRef, changeOrderData).getNodeRef();

				// calculate WUsed
				ecoService.calculateWUsedList(ecoNodeRef,false);

				// verify WUsed
				int checks = 0;
				ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check WUsed list", dbECOData.getWUsedList());

				assertEquals("Check 3 WUsed are impacted", 3, dbECOData.getWUsedList().size());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					wul.setIsWUsedImpacted(true);
					alfrescoRepository.save(wul);

					assertNotNull(wul.getSourceItems().get(0));
					logger.info("Source item " + wul.getSourceItems().get(0));

				}

				// simulation
				ecoService.doSimulation(ecoNodeRef);

				logger.info("ChangeUnit map : " + dbECOData.getChangeUnitMap().toString());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					ChangeUnitDataItem changeUnitData = dbECOData.getChangeUnitMap().get(wul.getSourceItems().get(0));

					if (changeUnitData != null) {
						if (changeUnitData.getSourceItem().equals(finishedProduct1NodeRef)) {

							checks++;
							assertEquals(RevisionType.Minor, changeUnitData.getRevision());
						} else if (changeUnitData.getSourceItem().equals(finishedProduct2NodeRef)) {

							checks++;
							assertEquals(RevisionType.Minor, changeUnitData.getRevision());
						}
					}
				}

				assertEquals(2, checks);

				// verify Simulation
				checks = 0;
				dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check Simulation list", dbECOData.getSimulationList());
				assertEquals("Check Simulation list", 8, dbECOData.getSimulationList().size());

				for (SimulationListDataItem sim : dbECOData.getSimulationList()) {

					if (sim.getSourceItem().equals(finishedProduct1NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF1", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF1", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF1", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF1", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF1", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF1", 10.5d, sim.getTargetValue());
						}
					} else if (sim.getSourceItem().equals(finishedProduct2NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF2", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF2", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF2", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF2", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF2", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF2", 10.5d, sim.getTargetValue());
						}
					}
				}
				assertEquals(8, checks);

				return ecoNodeRef;

			}

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				logger.info("Version Before : "+getVersionLabel(finishedProduct1NodeRef));
				// apply
				ecoService.apply(ecoNodeRef);


				return null;

			}

		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.info("Version After : "+getVersionLabel(finishedProduct1NodeRef));
				
				assertEquals("Check version", "1.1", getVersionLabel(finishedProduct1NodeRef));
				
				VersionHistory versionHistory = versionService.getVersionHistory(finishedProduct1NodeRef);
				Version version = versionHistory.getVersion("1.1");
				assertNotNull(version);
				assertNotNull(entityVersionService.getEntityVersion(version));

				return null;

			}
		}, false, true);

	}

	/**
	 * Test ecoService in multi level compo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testECOInMultiLeveCompo() throws Exception {
		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef finishedProduct3NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*
				 * create multi level compo
				 */

				FinishedProductData finishedProduct3 = new FinishedProductData();
				finishedProduct3.setName("PF3");
				finishedProduct3.setLegalName("Legal name");
				finishedProduct3.setUnit(ProductUnit.kg);
				finishedProduct3.setQty(2d);
				finishedProduct3.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				finishedProduct3.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, finishedProduct1NodeRef));
				compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, 2d, 2d, CompoListUnit.kg, 0d, DeclarationType.Declare, finishedProduct2NodeRef));
				finishedProduct3.getCompoListView().setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(PLMModel.TYPE_COMPOLIST);

				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				finishedProduct3.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
				finishedProduct3.setNutList(nutList);

				return alfrescoRepository.create(testFolderNodeRef, finishedProduct3).getNodeRef();

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product PF3 --*/");
				productService.formulate(finishedProduct3NodeRef);

				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct3 = (ProductData) alfrescoRepository.findOne(finishedProduct3NodeRef);

				logger.debug("unit of product formulated: " + formulatedProduct3.getUnit());

				// costs
				assertNotNull("CostList is null", formulatedProduct3.getCostList());
				for (CostListDataItem costListDataItem : formulatedProduct3.getCostList()) {
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue()
							+ " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if (costListDataItem.getCost().equals(cost1)) {
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if (costListDataItem.getCost().equals(cost2)) {
						assertEquals("cost1.getValue() == 9.0, actual values: " + trace, 9.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				// nuts
				assertNotNull("NutList is null", formulatedProduct3.getNutList());
				for (NutListDataItem nutListDataItem : formulatedProduct3.getNutList()) {
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: "
							+ nutListDataItem.getUnit();
					logger.debug(trace);
					if (nutListDataItem.getNut().equals(nut1)) {
						assertEquals("nut1.getValue() == 4.5, actual values: " + trace, 4.5d, nutListDataItem.getValue());
					}
					if (nutListDataItem.getNut().equals(nut2)) {
						assertEquals("nut2.getValue() == 9, actual values: " + trace, 9d, nutListDataItem.getValue());
					}
				}

				return null;
				
			}
		}, false, true);

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*
				 * create a change order to replace RM4 by RM5
				 */

				logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

				List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>();
				calculatedCharacts.add(cost1);
				calculatedCharacts.add(cost2);
				calculatedCharacts.add(nut1);
				calculatedCharacts.add(nut2);
				ChangeOrderData changeOrderData = new ChangeOrderData( "ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<ReplacementListDataItem>();
				replacementList.add(new ReplacementListDataItem(RevisionType.Major, Arrays.asList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = alfrescoRepository.create(testFolderNodeRef, changeOrderData).getNodeRef();

				// calculate WUsed
				ecoService.calculateWUsedList(ecoNodeRef,false);

				// verify WUsed
				int checks = 0;
				ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check WUsed list", dbECOData.getWUsedList());
				assertEquals("Check WUsed impacted", 5, dbECOData.getWUsedList().size());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					wul.setIsWUsedImpacted(true);
					alfrescoRepository.save(wul);

					assertNotNull(wul.getSourceItems().get(0));

				}

				// simulation
				ecoService.doSimulation(ecoNodeRef);

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					ChangeUnitDataItem changeUnitData = dbECOData.getChangeUnitMap().get(wul.getSourceItems().get(0));
					if (changeUnitData != null) {

						if (changeUnitData.getSourceItem().equals(finishedProduct1NodeRef)) {

							checks++;
							assertEquals(RevisionType.Major, changeUnitData.getRevision());
						} else if (changeUnitData.getSourceItem().equals(finishedProduct2NodeRef)) {

							checks++;
							assertEquals(RevisionType.Major, changeUnitData.getRevision());
						} else if (changeUnitData.getSourceItem().equals(finishedProduct3NodeRef)) {

							checks++;
							assertEquals(RevisionType.Major, changeUnitData.getRevision());
						}
					}
				}

				assertEquals(4, checks);

				// verify Simulation
				checks = 0;
				dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check Simulation list", dbECOData.getSimulationList());

				for (SimulationListDataItem sim : dbECOData.getSimulationList()) {
					logger.info("Source - Target for " + nodeService.getProperty(sim.getSourceItem(), ContentModel.PROP_NAME) + " - " + sim.getSourceValue() + " - "
							+ sim.getTargetValue());
				}

				assertEquals("Check changeUnitDataSimulation list", 12, dbECOData.getSimulationList().size());

				for (SimulationListDataItem sim : dbECOData.getSimulationList()) {

					if (sim.getSourceItem().equals(finishedProduct1NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF1", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF1", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF1", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF1", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF1", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF1", 10.5d, sim.getTargetValue());
						}
					} else if (sim.getSourceItem().equals(finishedProduct2NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF2", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF2", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF2", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF2", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF2", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF2", 10.5d, sim.getTargetValue());
						}
					} else if (sim.getSourceItem().equals(finishedProduct3NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF3", 6.0d, sim.getSourceValue());
							assertEquals("check cost1 PF3", 17.25d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF3", 9.0d, sim.getSourceValue());
							assertEquals("check cost2 PF3", 22.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF3", 4.5d, sim.getSourceValue());
							assertEquals("check nut1 PF3", 6.75d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF3", 9.0d, sim.getSourceValue());
							assertEquals("check nut2 PF3", 15.75d, sim.getTargetValue());
						}
					}
				}
				assertEquals(12, checks);

				return ecoNodeRef;

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.info("Version Before : "+getVersionLabel(finishedProduct1NodeRef));
				
				// apply
				ecoService.apply(ecoNodeRef);
				
			

				return null;

			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.info("Version After : "+getVersionLabel(finishedProduct1NodeRef));
				
				assertEquals("Check version", "2.0", getVersionLabel(finishedProduct1NodeRef));

				return null;

			}
		}, false, true);

	}
	

	private String getVersionLabel(NodeRef productNodeRef) {
		return (String)nodeService.getProperty(productNodeRef,ContentModel.PROP_VERSION_LABEL);
	}

}
