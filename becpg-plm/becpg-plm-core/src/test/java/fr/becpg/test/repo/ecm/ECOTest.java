/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
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
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * ECO test class
 * 
 * @author quere
 * 
 */
public class ECOTest extends AbstractFinishedProductTest {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(ECOTest.class);

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

	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
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
				List<CompoListDataItem> compoList = new ArrayList<>();

				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));

				finishedProduct.getCompoListView().setCompoList(compoList);

				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				finishedProduct.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
				finishedProduct.setNutList(nutList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

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

					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + costListDataItem.getValue()
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
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: "
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

				List<NodeRef> calculatedCharacts = new ArrayList<>();
				calculatedCharacts.add(cost1);
				calculatedCharacts.add(cost2);
				calculatedCharacts.add(nut1);
				calculatedCharacts.add(nut2);
				ChangeOrderData changeOrderData = new ChangeOrderData( "ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<>();

				replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

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
	
	
	@Test
	public void testDeleteNode() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*
				 * create a change order to replace RM4 by null
				 */

				logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by null: " + rawMaterial5NodeRef);

				List<NodeRef> calculatedCharacts = new ArrayList<>();
				ChangeOrderData changeOrderData = new ChangeOrderData( "ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<>();

				replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Collections.singletonList(rawMaterial4NodeRef), null, 100));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

				// calculate WUsed
				ecoService.calculateWUsedList(ecoNodeRef,false);

				// verify WUsed
				ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check WUsed list", dbECOData.getWUsedList());

				assertEquals("Check 2 WUsed are impacted", 2, dbECOData.getWUsedList().size());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					wul.setIsWUsedImpacted(true);
					alfrescoRepository.save(wul);

					assertNotNull(wul.getSourceItems().get(0));
					logger.info("Source item " + wul.getSourceItems().get(0));

				}


				return ecoNodeRef;

			}

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				// apply
				ecoService.apply(ecoNodeRef);


				return null;

			}

		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);
				
				assertTrue(productData.getCompoList().size() == 5);
				
				return null;

			}
		}, false, true);

	}

	
	@Test
	public void testTwoToOne() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*
				 * create a change order to replace RM4 by null
				 */

				logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by null: " + rawMaterial5NodeRef);

				List<NodeRef> calculatedCharacts = new ArrayList<>();
				ChangeOrderData changeOrderData = new ChangeOrderData( "ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<>();

				replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial4NodeRef, rawMaterial3NodeRef), rawMaterial5NodeRef, 100));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

				// calculate WUsed
				ecoService.calculateWUsedList(ecoNodeRef,false);

				// verify WUsed
				ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check WUsed list", dbECOData.getWUsedList());

				assertEquals("Check 2 WUsed are impacted", 2, dbECOData.getWUsedList().size());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					wul.setIsWUsedImpacted(true);
					alfrescoRepository.save(wul);

					assertNotNull(wul.getSourceItems().get(0));
					logger.info("Source item " + wul.getSourceItems().get(0));

				}


				return ecoNodeRef;

			}

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				// apply
				ecoService.apply(ecoNodeRef);


				return null;

			}

		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {


				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);
				
				assertTrue(productData.getCompoList().size() == 5);
				
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
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 1d, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, finishedProduct1NodeRef));
				compoList.add(new CompoListDataItem(null, null, 2d, 2d, CompoListUnit.kg, 0d, DeclarationType.Declare, finishedProduct2NodeRef));
				finishedProduct3.getCompoListView().setCompoList(compoList);


				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				finishedProduct3.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
				finishedProduct3.setNutList(nutList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct3).getNodeRef();

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
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + costListDataItem.getValue()
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
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: "
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

				List<NodeRef> calculatedCharacts = new ArrayList<>();
				calculatedCharacts.add(cost1);
				calculatedCharacts.add(cost2);
				calculatedCharacts.add(nut1);
				calculatedCharacts.add(nut2);
				ChangeOrderData changeOrderData = new ChangeOrderData( "ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<>();
				replacementList.add(new ReplacementListDataItem(RevisionType.Major, Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

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
					logger.info("Source - Target for " + nodeService.getProperty(sim.getSourceItem(), BeCPGModel.PROP_CHARACT_NAME) + " - " + sim.getSourceValue() + " - "
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
