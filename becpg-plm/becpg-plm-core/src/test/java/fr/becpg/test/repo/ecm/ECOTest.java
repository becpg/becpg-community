/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import com.ibm.icu.util.Calendar;

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
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
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

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
			compoList
					.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
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

			List<PackagingListDataItem> packList = new ArrayList<>();
			packList.add(new PackagingListDataItem(null, 25d, PackagingListUnit.PP, PackagingLevel.Secondary, true, packagingKit1NodeRef));			
			finishedProduct.getPackagingListView().setPackagingList(packList);
			

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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

				String trace1 = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.info(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 4.0, actual values: " + trace1, 4.0d, costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace1, 6.0d, costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
				}
			}
			// nuts
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
				String trace2 = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
				logger.info(trace2);
				if (nutListDataItem.getNut().equals(nut1)) {
					assertEquals("nut1.getValue() == 3, actual values: " + trace2, 3d, nutListDataItem.getValue());
				}
				if (nutListDataItem.getNut().equals(nut2)) {
					assertEquals("nut2.getValue() == 6, actual values: " + trace2, 6d, nutListDataItem.getValue());
				}
			}

			return finishedProductNodeRef;

		}, false, true);

	}

	/**
	 * Test ecoService
	 *
	 * @throws Exception
	 *             the exception
	 */
	//@Test
	public void testECOService() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * create a change order to replace RM4 by RM5
			 */

			logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			calculatedCharacts.add(cost1);
			calculatedCharacts.add(cost2);
			calculatedCharacts.add(nut1);
			calculatedCharacts.add(nut2);
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList
					.add(new ReplacementListDataItem(RevisionType.Minor, Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			// calculate WUsed
			ecoService.calculateWUsedList(ecoNodeRef1, false);

			// verify WUsed
			int checks = 0;
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 3 WUsed are impacted", 3, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul1 : dbECOData.getWUsedList()) {

				wul1.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul1);

				assertNotNull(wul1.getSourceItems().get(0));
				logger.info("Source item " + wul1.getSourceItems().get(0));

			}

			// simulation
			ecoService.doSimulation(ecoNodeRef1);

			logger.info("ChangeUnit map : " + dbECOData.getChangeUnitMap().toString());

			for (WUsedListDataItem wul2 : dbECOData.getWUsedList()) {

				ChangeUnitDataItem changeUnitData = dbECOData.getChangeUnitMap().get(wul2.getSourceItems().get(0));

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
			dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
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

			return ecoNodeRef1;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("Version Before : " + getVersionLabel(finishedProduct1NodeRef));
			// apply
			ecoService.apply(ecoNodeRef);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("Version After : " + getVersionLabel(finishedProduct1NodeRef));

			assertEquals("Check version", "1.1", getVersionLabel(finishedProduct1NodeRef));

			VersionHistory versionHistory = versionService.getVersionHistory(finishedProduct1NodeRef);
			Version version = versionHistory.getVersion("1.1");
			assertNotNull(version);
			assertNotNull(entityVersionService.getEntityVersion(version));

			return null;

		}, false, true);

	}

	//@Test
	public void testDeleteNode() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by null: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Collections.singletonList(rawMaterial4NodeRef), null, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			// calculate WUsed
			ecoService.calculateWUsedList(ecoNodeRef1, false);

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 2 WUsed are impacted", 2, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul);

				assertNotNull(wul.getSourceItems().get(0));
				logger.info("Source item " + wul.getSourceItems().get(0));

			}

			return ecoNodeRef1;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// apply
			ecoService.apply(ecoNodeRef);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			assertTrue(productData.getCompoList().size() == 5);

			return null;

		}, false, true);

	}

	//@Test
	public void testTwoToOne() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by null: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial4NodeRef, rawMaterial3NodeRef),
					rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			// calculate WUsed
			ecoService.calculateWUsedList(ecoNodeRef1, false);

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 2 WUsed are impacted", 2, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul);

				assertNotNull(wul.getSourceItems().get(0));
				logger.info("Source item " + wul.getSourceItems().get(0));

			}

			return ecoNodeRef1;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// apply
			ecoService.apply(ecoNodeRef);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			assertTrue(productData.getCompoList().size() == 5);

			return null;

		}, false, true);

	}
	
	
	
	
	
	@Test
	public void testEffectivity() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO1", ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, calculatedCharacts);
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			
			changeOrderData.setEffectiveDate(cal.getTime());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial4NodeRef),
					rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			// calculate WUsed
			ecoService.calculateWUsedList(ecoNodeRef1, false);

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 2 WUsed are impacted", 2, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul);

				assertNotNull(wul.getSourceItems().get(0));
				logger.info("Source item " + wul.getSourceItems().get(0));

			}

			return ecoNodeRef1;

		}, false, true);
	
	
		
		
		logger.info("BEFORE");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);
			
			for(CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(),ContentModel.PROP_NAME)
						+" "+compoListDataItem.getStartEffectivity() + " - " +compoListDataItem.getEndEffectivity());
				
			}

			return null;

		}, false, true);

		

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// apply
			ecoService.apply(ecoNodeRef);

			return null;

		}, false, true);

		logger.info("APTER");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);
			
			for(CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(),ContentModel.PROP_NAME)
						+" "+compoListDataItem.getStartEffectivity() + " - " +compoListDataItem.getEndEffectivity());
				
			}
			

			assertTrue(productData.getCompoList().size() == 7);
			
			
			

			return null;

		}, false, true);
		
		
		final NodeRef ecoNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug("create Change order to replace RM4: " + rawMaterial5NodeRef + " by RM5: " + rawMaterial4NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO2", ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, calculatedCharacts);
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 2);
			
			changeOrderData.setEffectiveDate(cal.getTime());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial5NodeRef),
					rawMaterial4NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			// calculate WUsed
			ecoService.calculateWUsedList(ecoNodeRef1, false);

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 2 WUsed are impacted", 2, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul);

				assertNotNull(wul.getSourceItems().get(0));
				logger.info("Source item " + wul.getSourceItems().get(0));

			}

			return ecoNodeRef1;

		}, false, true);
		

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// apply
			ecoService.apply(ecoNodeRef2);

			return null;

		}, false, true);

		logger.info("APTER 2");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);
			
			for(CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(),ContentModel.PROP_NAME)
						+" "+compoListDataItem.getStartEffectivity() + " - " +compoListDataItem.getEndEffectivity());
				
			}

			
			

			return null;

		}, false, true);
		

		final NodeRef ecoNodeRef3 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO3", ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, calculatedCharacts);
			
			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial4NodeRef),
					rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			// calculate WUsed
			ecoService.calculateWUsedList(ecoNodeRef1, false);

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 3 WUsed are impacted", 3, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul);

				assertNotNull(wul.getSourceItems().get(0));
				logger.info("Source item " + wul.getSourceItems().get(0));

			}

			return ecoNodeRef1;

		}, false, true);
		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// apply
			ecoService.apply(ecoNodeRef3);

			return null;

		}, false, true);

		logger.info("APTER 3");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);
			
			for(CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(),ContentModel.PROP_NAME)
						+" "+compoListDataItem.getStartEffectivity() + " - " +compoListDataItem.getEndEffectivity());
				
			}
			

			assertTrue(productData.getCompoList().size() == 8);
			
			
			

			return null;

		}, false, true);
		

	}
	
	
	
	
	
	
	
	
	
	

	/**
	 * Test ecoService in multi level compo
	 *
	 * @throws Exception
	 *             the exception
	 */
	//@Test
	public void testECOInMultiLeveCompo() throws Exception {
		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef finishedProduct3NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
				String trace1 = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace1, 6.0d, costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 9.0, actual values: " + trace1, 9.0d, costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
				}
			}
			// nuts
			assertNotNull("NutList is null", formulatedProduct3.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct3.getNutList()) {
				String trace2 = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
				logger.debug(trace2);
				if (nutListDataItem.getNut().equals(nut1)) {
					assertEquals("nut1.getValue() == 4.5, actual values: " + trace2, 4.5d, nutListDataItem.getValue());
				}
				if (nutListDataItem.getNut().equals(nut2)) {
					assertEquals("nut2.getValue() == 9, actual values: " + trace2, 9d, nutListDataItem.getValue());
				}
			}

			return null;

		}, false, true);

		final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*
			 * create a change order to replace RM4 by RM5
			 */

			logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			calculatedCharacts.add(cost1);
			calculatedCharacts.add(cost2);
			calculatedCharacts.add(nut1);
			calculatedCharacts.add(nut2);
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();
			replacementList
					.add(new ReplacementListDataItem(RevisionType.Major, Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			// calculate WUsed
			ecoService.calculateWUsedList(ecoNodeRef1, false);

			// verify WUsed
			int checks = 0;
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());
			assertEquals("Check WUsed impacted", 5, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul1 : dbECOData.getWUsedList()) {

				wul1.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul1);

				assertNotNull(wul1.getSourceItems().get(0));

			}

			// simulation
			ecoService.doSimulation(ecoNodeRef1);

			for (WUsedListDataItem wul2 : dbECOData.getWUsedList()) {

				ChangeUnitDataItem changeUnitData = dbECOData.getChangeUnitMap().get(wul2.getSourceItems().get(0));
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
			dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check Simulation list", dbECOData.getSimulationList());

			for (SimulationListDataItem sim1 : dbECOData.getSimulationList()) {
				logger.info("Source - Target for " + nodeService.getProperty(sim1.getSourceItem(), BeCPGModel.PROP_CHARACT_NAME) + " - "
						+ sim1.getSourceValue() + " - " + sim1.getTargetValue());
			}

			assertEquals("Check changeUnitDataSimulation list", 12, dbECOData.getSimulationList().size());

			for (SimulationListDataItem sim2 : dbECOData.getSimulationList()) {

				if (sim2.getSourceItem().equals(finishedProduct1NodeRef)) {

					if (sim2.getCharact().equals(cost1)) {

						checks++;
						assertEquals("check cost1 PF1", 4.0d, sim2.getSourceValue());
						assertEquals("check cost1 PF1", 11.5d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(cost2)) {

						checks++;
						assertEquals("check cost2 PF1", 6.0d, sim2.getSourceValue());
						assertEquals("check cost2 PF1", 15d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(nut1)) {

						checks++;
						assertEquals("check nut1 PF1", 3.0d, sim2.getSourceValue());
						assertEquals("check nut1 PF1", 4.5d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(nut2)) {

						checks++;
						assertEquals("check nut2 PF1", 6.0d, sim2.getSourceValue());
						assertEquals("check nut2 PF1", 10.5d, sim2.getTargetValue());
					}
				} else if (sim2.getSourceItem().equals(finishedProduct2NodeRef)) {

					if (sim2.getCharact().equals(cost1)) {

						checks++;
						assertEquals("check cost1 PF2", 4.0d, sim2.getSourceValue());
						assertEquals("check cost1 PF2", 11.5d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(cost2)) {

						checks++;
						assertEquals("check cost2 PF2", 6.0d, sim2.getSourceValue());
						assertEquals("check cost2 PF2", 15d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(nut1)) {

						checks++;
						assertEquals("check nut1 PF2", 3.0d, sim2.getSourceValue());
						assertEquals("check nut1 PF2", 4.5d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(nut2)) {

						checks++;
						assertEquals("check nut2 PF2", 6.0d, sim2.getSourceValue());
						assertEquals("check nut2 PF2", 10.5d, sim2.getTargetValue());
					}
				} else if (sim2.getSourceItem().equals(finishedProduct3NodeRef)) {

					if (sim2.getCharact().equals(cost1)) {

						checks++;
						assertEquals("check cost1 PF3", 6.0d, sim2.getSourceValue());
						assertEquals("check cost1 PF3", 17.25d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(cost2)) {

						checks++;
						assertEquals("check cost2 PF3", 9.0d, sim2.getSourceValue());
						assertEquals("check cost2 PF3", 22.5d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(nut1)) {

						checks++;
						assertEquals("check nut1 PF3", 4.5d, sim2.getSourceValue());
						assertEquals("check nut1 PF3", 6.75d, sim2.getTargetValue());
					} else if (sim2.getCharact().equals(nut2)) {

						checks++;
						assertEquals("check nut2 PF3", 9.0d, sim2.getSourceValue());
						assertEquals("check nut2 PF3", 15.75d, sim2.getTargetValue());
					}
				}
			}
			assertEquals(12, checks);

			return ecoNodeRef1;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("Version Before : " + getVersionLabel(finishedProduct1NodeRef));

			// apply
			ecoService.apply(ecoNodeRef);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("Version After : " + getVersionLabel(finishedProduct1NodeRef));

			assertEquals("Check version", "2.0", getVersionLabel(finishedProduct1NodeRef));

			return null;

		}, false, true);

	}

	//@Test
	public void testReformulate() throws Exception {
		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef testNodeRef = getTestFolderNodeRef();
		
		
		final  Date formulatedDate = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			productService.formulate(finishedProduct1NodeRef);
			
			return ((ProductData) alfrescoRepository.findOne(finishedProduct1NodeRef)).getFormulatedDate();

		}, false, true);
		

		final LongAdder adder = new LongAdder();

		Callable<Void> callable = () -> {
			String threadName = Thread.currentThread().getName();

			adder.increment();

			AuthenticationUtil.runAsSystem(() -> {

				final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					/*
					 * create a change order to replace RM4 by RM5
					 */

					logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

					ChangeOrderData changeOrderData = new ChangeOrderData("ECO " + threadName + " " + adder.doubleValue(), ECOState.ToCalculateWUsed,
							ChangeOrderType.Replacement, new ArrayList<>());

					List<ReplacementListDataItem> replacementList = new ArrayList<>();

					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision, Collections.singletonList(rawMaterial4NodeRef),
							rawMaterial4NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision, Collections.singletonList(rawMaterial2NodeRef),
							rawMaterial2NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision, Collections.singletonList(rawMaterial4NodeRef),
							rawMaterial4NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision, Collections.singletonList(packagingKit1NodeRef),
							packagingKit1NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision, Collections.singletonList(rawMaterial2NodeRef),
							rawMaterial2NodeRef, 100));
					changeOrderData.setReplacementList(replacementList);

					NodeRef ecoNodeRef1 = alfrescoRepository.create(testNodeRef, changeOrderData).getNodeRef();

					// calculate WUsed
					ecoService.calculateWUsedList(ecoNodeRef1, false);

					ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef1);
					assertNotNull("check ECO exist in DB", dbECOData);
					assertNotNull("Check WUsed list", dbECOData.getWUsedList());

					 assertEquals("Check 10 WUsed are impacted", 10,
					 dbECOData.getWUsedList().size());

					for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

						wul.setIsWUsedImpacted(true);
						alfrescoRepository.save(wul);

					}

					return ecoNodeRef1;

				}, false, true);

				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					logger.info("Name Before : " + alfrescoRepository.findOne(finishedProduct1NodeRef).getName() + " in thread " + threadName);
					// apply
					ecoService.apply(ecoNodeRef);

					return null;

				}, false, true);
				
				return null;
			});

			return null;
		};

		ExecutorService executor = Executors.newFixedThreadPool(20);
		List<Future<Void>> results = new ArrayList<>();

		for (int i = 0; i < 20; i++) {
			results.add(executor.submit(callable));

		}

		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		for (Future<Void> future : results) {
			try {

				future.get();
			} catch (ExecutionException ex) {
				logger.error(ex, ex);
				Assert.fail(ex.getMessage());
			}
		}

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProductData product = (ProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);
			
			Assert.assertTrue(product.getFormulatedDate().getTime()> formulatedDate.getTime());
			Assert.assertTrue(product.getCompoList().size() == 6);
			Assert.assertTrue(product.getName() == "PF1");

			return null;

		}, false, true);

	}

	private String getVersionLabel(NodeRef productNodeRef) {
		return (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_VERSION_LABEL);
	}

}
