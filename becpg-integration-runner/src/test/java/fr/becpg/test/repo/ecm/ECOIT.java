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
package fr.becpg.test.repo.ecm;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

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
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
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
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
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
public class ECOIT extends AbstractFinishedProductTest {

	private static final Log logger = LogFactory.getLog(ECOIT.class);

	/** The product service. */
	@Autowired
	private ProductService productService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private ECOService ecoService;

	@Autowired
	private VersionService versionService;

	@Autowired
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
	 * @throws Exception the exception
	 */
	public NodeRef createFinishedProduct(final String finishedProductName) throws Exception {

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

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

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(2d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d,
			 * ProductUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(2d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail)
					.withProduct(rawMaterial2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial3NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial4NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial4NodeRef));

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
			/*
			 * packList.add(new PackagingListDataItem(null, 25d, ProductUnit.PP,
			 * PackagingLevel.Secondary, true, packagingKit1NodeRef));
			 */
			packList.add(PackagingListDataItem.build().withQty(25d).withUnit(ProductUnit.PP)
					.withPkgLevel(PackagingLevel.Secondary).withIsMaster(true).withProduct(packagingKit1NodeRef));
			finishedProduct.getPackagingListView().setPackagingList(packList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		return inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository
					.findOne(finishedProductNodeRef);

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

				String trace1 = "cost: "
						+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.info(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 4.0, actual values: " + trace1, 4.0d,
							costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg",
							costListDataItem.getUnit());
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace1, 6.0d,
							costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg",
							costListDataItem.getUnit());
				}
			}
			// nuts
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
				String trace2 = "nut: "
						+ nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
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

		});

	}

	/**
	 * Test ecoService
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testECOServiceSimulation() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by RM5
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			calculatedCharacts.add(cost1);
			calculatedCharacts.add(cost2);
			calculatedCharacts.add(nut1);
			calculatedCharacts.add(nut2);
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor,
					Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		inWriteTx(() -> {

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 3 WUsed are impacted", 3, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul1 : dbECOData.getWUsedList()) {

				wul1.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul1);

				assertNotNull(wul1.getSourceItems().get(0));
				logger.info("Source item " + wul1.getSourceItems().get(0));

			}

			return null;

		});

		waitForBatchEnd(ecoService.doSimulation(ecoNodeRef, false, false));

		inWriteTx(() -> {

			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

			logger.info("ChangeUnit map : " + dbECOData.getChangeUnitMap().toString());

			int checks = 0;

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

			return true;
		});

		inWriteTx(() -> {

			FinishedProductData FP1 = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			FinishedProductData FP2 = (FinishedProductData) alfrescoRepository.findOne(finishedProduct2NodeRef);

			assertEquals(0, Collections.frequency(getActivityTypes(finishedProduct1NodeRef), ActivityType.ChangeOrder));
			assertEquals(0, Collections.frequency(getActivityTypes(finishedProduct2NodeRef), ActivityType.ChangeOrder));

			assertEquals(6, FP1.getCompoList().size());
			assertEquals(6, FP2.getCompoList().size());

			boolean hasRM4 = false;
			boolean hasRM5 = false;

			for (CompoListDataItem compoList : FP1.getCompoList()) {
				if (rawMaterial5NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM5 = true;
				} else if (rawMaterial4NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM4 = true;
				}
			}

			assertTrue(hasRM4);
			assertFalse(hasRM5);

			hasRM4 = false;
			hasRM5 = false;

			for (CompoListDataItem compoList : FP2.getCompoList()) {
				if (rawMaterial5NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM5 = true;
					break;
				} else if (rawMaterial4NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM4 = true;
				}
			}

			assertTrue(hasRM4);
			assertFalse(hasRM5);

			return null;

		});

	}

	@Test
	public void testECOServiceApply() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by RM5
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			calculatedCharacts.add(cost1);
			calculatedCharacts.add(cost2);
			calculatedCharacts.add(nut1);
			calculatedCharacts.add(nut2);
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor,
					Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		logger.info("Version Before : " + getVersionLabel(finishedProduct1NodeRef));

		// apply
		waitForBatchEnd(ecoService.apply(ecoNodeRef, false, true, false));

		inWriteTx(() -> {

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 3 WUsed are impacted", 3, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul1 : dbECOData.getWUsedList()) {

				wul1.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul1);

				assertNotNull(wul1.getSourceItems().get(0));
				logger.info("Source item " + wul1.getSourceItems().get(0));

			}

			return null;

		});

		inWriteTx(() -> {

			logger.info("Version After : " + getVersionLabel(finishedProduct1NodeRef));

			assertEquals("Check version", "1.1", getVersionLabel(finishedProduct1NodeRef));

			VersionHistory versionHistory = versionService.getVersionHistory(finishedProduct1NodeRef);
			Version version = versionHistory.getVersion("1.1");
			assertNotNull(version);
			assertNotNull(entityVersionService.getEntityVersion(version));

			return null;

		});

		inWriteTx(() -> {

			FinishedProductData FP1 = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			FinishedProductData FP2 = (FinishedProductData) alfrescoRepository.findOne(finishedProduct2NodeRef);

			assertEquals(1, Collections.frequency(getActivityTypes(finishedProduct1NodeRef), ActivityType.ChangeOrder));
			assertEquals(1, Collections.frequency(getActivityTypes(finishedProduct2NodeRef), ActivityType.ChangeOrder));

			assertEquals(6, FP1.getCompoList().size());
			assertEquals(6, FP2.getCompoList().size());

			boolean hasRM4 = false;
			boolean hasRM5 = false;

			for (CompoListDataItem compoList : FP1.getCompoList()) {
				if (rawMaterial5NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM5 = true;
				} else if (rawMaterial4NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM4 = true;
				}
			}

			assertFalse(hasRM4);
			assertTrue(hasRM5);

			hasRM4 = false;
			hasRM5 = false;

			for (CompoListDataItem compoList : FP2.getCompoList()) {
				if (rawMaterial5NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM5 = true;
					break;
				} else if (rawMaterial4NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM4 = true;
				}
			}

			assertFalse(hasRM4);
			assertTrue(hasRM5);

			int checks = 0;

			for (CostListDataItem cost : FP1.getCostList()) {
				if (cost1.equals(cost.getCharactNodeRef())) {
					assertEquals(11.5d, cost.getValue());
					checks++;
				} else if (cost2.equals(cost.getCharactNodeRef())) {
					assertEquals(15d, cost.getValue());
					checks++;
				}
			}
			for (CostListDataItem cost : FP2.getCostList()) {
				if (cost1.equals(cost.getCharactNodeRef())) {
					assertEquals(11.5d, cost.getValue());
					checks++;
				} else if (cost2.equals(cost.getCharactNodeRef())) {
					assertEquals(15d, cost.getValue());
					checks++;
				}
			}

			for (NutListDataItem nut : FP1.getNutList()) {
				if (nut1.equals(nut.getCharactNodeRef())) {
					assertEquals(4.5d, nut.getValue());
					checks++;
				} else if (nut2.equals(nut.getCharactNodeRef())) {
					assertEquals(10.5d, nut.getValue());
					checks++;
				}
			}

			for (NutListDataItem nut : FP2.getNutList()) {
				if (nut1.equals(nut.getCharactNodeRef())) {
					assertEquals(4.5d, nut.getValue());
					checks++;
				} else if (nut2.equals(nut.getCharactNodeRef())) {
					assertEquals(10.5d, nut.getValue());
					checks++;
				}
			}

			assertEquals(8, checks);

			return null;

		});

	}

	private List<ActivityType> getActivityTypes(NodeRef entityNodeRef) {
		AuditQuery auditFilter = AuditQuery.createQuery().sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
				.filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString());
		List<ActivityListDataItem> activities = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditFilter)
				.stream().map(json -> AuditActivityHelper.parseActivity(json)).toList();
		return activities.stream().map(a -> a.getActivityType()).toList();
	}

	@Test
	public void testDeleteNode() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by null: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor,
					Collections.singletonList(rawMaterial4NodeRef), null, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		inWriteTx(() -> {

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

			return null;

		});

		// apply
		waitForBatchEnd(ecoService.apply(ecoNodeRef, false, false, false));

		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			assertEquals(5, productData.getCompoList().size());

			return null;

		});

	}

	@Test
	public void testTwoToOne() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by null: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor,
					Arrays.asList(rawMaterial4NodeRef, rawMaterial3NodeRef), rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		inWriteTx(() -> {

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

			return null;

		});

		waitForBatchEnd(ecoService.apply(ecoNodeRef, false, false, false));

		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			assertEquals(5, productData.getCompoList().size());

			return null;

		});

	}

	@Test
	public void testEffectivity() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO1", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 1);

			changeOrderData.setEffectiveDate(cal.getTime());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial4NodeRef),
					rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		inWriteTx(() -> {

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

			return null;

		});

		logger.info("BEFORE");
		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			for (CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(), ContentModel.PROP_NAME) + " "
						+ compoListDataItem.getStartEffectivity() + " - " + compoListDataItem.getEndEffectivity());
			}

			return null;

		});

		// apply
		waitForBatchEnd(ecoService.apply(ecoNodeRef, false, false, false));

		logger.info("APTER");
		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			for (CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(), ContentModel.PROP_NAME) + " "
						+ compoListDataItem.getStartEffectivity() + " - " + compoListDataItem.getEndEffectivity());

			}

			assertEquals(7, productData.getCompoList().size());

			return null;

		});

		final NodeRef ecoNodeRef2 = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial5NodeRef + " by RM5: " + rawMaterial4NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO2", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 2);

			changeOrderData.setEffectiveDate(cal.getTime());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial5NodeRef),
					rawMaterial4NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef2, false, false));

		inWriteTx(() -> {

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef2);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 2 WUsed are impacted", 2, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul);

				assertNotNull(wul.getSourceItems().get(0));
				logger.info("Source item " + wul.getSourceItems().get(0));

			}

			return null;

		});

		// apply
		waitForBatchEnd(ecoService.apply(ecoNodeRef2, false, false, false));

		logger.info("APTER 2");
		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			for (CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(), ContentModel.PROP_NAME) + " "
						+ compoListDataItem.getStartEffectivity() + " - " + compoListDataItem.getEndEffectivity());

			}

			return null;

		});

		final NodeRef ecoNodeRef3 = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by null
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO3", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			replacementList.add(new ReplacementListDataItem(RevisionType.Minor, Arrays.asList(rawMaterial4NodeRef),
					rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef3, false, false));

		inWriteTx(() -> {

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef3);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 3 WUsed are impacted", 3, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul);

				assertNotNull(wul.getSourceItems().get(0));
				logger.info("Source item " + wul.getSourceItems().get(0));

			}
			return null;

		});

		// apply
		waitForBatchEnd(ecoService.apply(ecoNodeRef3, false, false, false));

		logger.info("APTER 3");
		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			for (CompoListDataItem compoListDataItem : productData.getCompoList()) {
				logger.info(nodeService.getProperty(compoListDataItem.getComponent(), ContentModel.PROP_NAME) + " "
						+ compoListDataItem.getStartEffectivity() + " - " + compoListDataItem.getEndEffectivity());

			}

			assertEquals(8, productData.getCompoList().size());

			return null;

		});

	}

	/**
	 * Test ecoService in multi level compo
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testECOInMultiLeveCompoSimulation() throws Exception {
		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef finishedProduct3NodeRef = inWriteTx(() -> {

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
			/*
			 * compoList.add(new CompoListDataItem(null, null, 1d, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, finishedProduct1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(2d).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare));
			/*
			 * compoList.add(new CompoListDataItem(null, null, 2d, 2d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, finishedProduct2NodeRef));
			 */
			compoList
					.add(CompoListDataItem.build().withQty(2d).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
							.withDeclarationType(DeclarationType.Declare).withProduct(finishedProduct2NodeRef));

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

		});

		inWriteTx(() -> {

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
				String trace1 = "cost: "
						+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace1, 6.0d,
							costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg",
							costListDataItem.getUnit());
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 9.0, actual values: " + trace1, 9.0d,
							costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg",
							costListDataItem.getUnit());
				}
			}
			// nuts
			assertNotNull("NutList is null", formulatedProduct3.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct3.getNutList()) {
				String trace2 = "nut: "
						+ nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
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

		});

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by RM5
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			calculatedCharacts.add(cost1);
			calculatedCharacts.add(cost2);
			calculatedCharacts.add(nut1);
			calculatedCharacts.add(nut2);
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();
			replacementList.add(new ReplacementListDataItem(RevisionType.Major,
					Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		waitForBatchEnd(ecoService.doSimulation(ecoNodeRef, true, false));

		inWriteTx(() -> {

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());
			assertEquals("Check WUsed impacted", 5, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul1 : dbECOData.getWUsedList()) {

				wul1.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul1);

				assertNotNull(wul1.getSourceItems().get(0));

			}

			int checks = 0;

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
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check Simulation list", dbECOData.getSimulationList());

			for (SimulationListDataItem sim1 : dbECOData.getSimulationList()) {
				logger.info("Source - Target for "
						+ nodeService.getProperty(sim1.getSourceItem(), BeCPGModel.PROP_CHARACT_NAME) + " - "
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

			return null;

		});

	}

	@Test
	public void testECOInMultiLeveCompoApply() throws Exception {
		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef finishedProduct3NodeRef = inWriteTx(() -> {

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
			/*
			 * compoList.add(new CompoListDataItem(null, null, 1d, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, finishedProduct1NodeRef));
			 */
			compoList
					.add(CompoListDataItem.build().withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
							.withDeclarationType(DeclarationType.Declare).withProduct(finishedProduct1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, 2d, 2d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, finishedProduct2NodeRef));
			 */
			compoList
					.add(CompoListDataItem.build().withQty(2d).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
							.withDeclarationType(DeclarationType.Declare).withProduct(finishedProduct2NodeRef));

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

		});

		inWriteTx(() -> {

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
				String trace1 = "cost: "
						+ nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace1, 6.0d,
							costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg",
							costListDataItem.getUnit());
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 9.0, actual values: " + trace1, 9.0d,
							costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg",
							costListDataItem.getUnit());
				}
			}
			// nuts
			assertNotNull("NutList is null", formulatedProduct3.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct3.getNutList()) {
				String trace2 = "nut: "
						+ nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
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

		});

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			/*
			 * create a change order to replace RM4 by RM5
			 */

			logger.debug(
					"create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

			List<NodeRef> calculatedCharacts = new ArrayList<>();
			calculatedCharacts.add(cost1);
			calculatedCharacts.add(cost2);
			calculatedCharacts.add(nut1);
			calculatedCharacts.add(nut2);
			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, calculatedCharacts);

			List<ReplacementListDataItem> replacementList = new ArrayList<>();
			replacementList.add(new ReplacementListDataItem(RevisionType.Major,
					Collections.singletonList(rawMaterial4NodeRef), rawMaterial5NodeRef, 100));
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		inWriteTx(() -> {

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());
			assertEquals("Check WUsed impacted", 5, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul1 : dbECOData.getWUsedList()) {

				wul1.setIsWUsedImpacted(true);
				alfrescoRepository.save(wul1);

				assertNotNull(wul1.getSourceItems().get(0));

			}

			return null;

		});

		// apply
		waitForBatchEnd(ecoService.apply(ecoNodeRef, false, false, false));

		inWriteTx(() -> {

			logger.info("Version After : " + getVersionLabel(finishedProduct1NodeRef));

			assertEquals("Check version", "2.0", getVersionLabel(finishedProduct1NodeRef));

			return null;

		});

		inWriteTx(() -> {

			FinishedProductData FP1 = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			FinishedProductData FP2 = (FinishedProductData) alfrescoRepository.findOne(finishedProduct2NodeRef);

			FinishedProductData FP3 = (FinishedProductData) alfrescoRepository.findOne(finishedProduct3NodeRef);

			assertEquals(6, FP1.getCompoList().size());
			assertEquals(6, FP2.getCompoList().size());
			assertEquals(2, FP3.getCompoList().size());

			boolean hasRM4 = false;
			boolean hasRM5 = false;

			for (CompoListDataItem compoList : FP1.getCompoList()) {
				if (rawMaterial5NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM5 = true;
				} else if (rawMaterial4NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM4 = true;
				}
			}

			assertFalse(hasRM4);
			assertTrue(hasRM5);

			hasRM4 = false;
			hasRM5 = false;

			for (CompoListDataItem compoList : FP2.getCompoList()) {
				if (rawMaterial5NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM5 = true;
					break;
				} else if (rawMaterial4NodeRef.equals(compoList.getCharactNodeRef())) {
					hasRM4 = true;
				}
			}

			assertFalse(hasRM4);
			assertTrue(hasRM5);

			int checks = 0;

			for (CostListDataItem cost : FP1.getCostList()) {
				if (cost1.equals(cost.getCharactNodeRef())) {
					assertEquals(11.5d, cost.getValue());
					checks++;
				} else if (cost2.equals(cost.getCharactNodeRef())) {
					assertEquals(15d, cost.getValue());
					checks++;
				}
			}
			for (CostListDataItem cost : FP2.getCostList()) {
				if (cost1.equals(cost.getCharactNodeRef())) {
					assertEquals(11.5d, cost.getValue());
					checks++;
				} else if (cost2.equals(cost.getCharactNodeRef())) {
					assertEquals(15d, cost.getValue());
					checks++;
				}
			}
			for (CostListDataItem cost : FP3.getCostList()) {
				if (cost1.equals(cost.getCharactNodeRef())) {
					assertEquals(17.25d, cost.getValue());
					checks++;
				} else if (cost2.equals(cost.getCharactNodeRef())) {
					assertEquals(22.5d, cost.getValue());
					checks++;
				}
			}

			for (NutListDataItem nut : FP1.getNutList()) {
				if (nut1.equals(nut.getCharactNodeRef())) {
					assertEquals(4.5d, nut.getValue());
					checks++;
				} else if (nut2.equals(nut.getCharactNodeRef())) {
					assertEquals(10.5d, nut.getValue());
					checks++;
				}
			}
			for (NutListDataItem nut : FP2.getNutList()) {
				if (nut1.equals(nut.getCharactNodeRef())) {
					assertEquals(4.5d, nut.getValue());
					checks++;
				} else if (nut2.equals(nut.getCharactNodeRef())) {
					assertEquals(10.5d, nut.getValue());
					checks++;
				}
			}
			for (NutListDataItem nut : FP3.getNutList()) {
				if (nut1.equals(nut.getCharactNodeRef())) {
					assertEquals(6.75d, nut.getValue());
					checks++;
				} else if (nut2.equals(nut.getCharactNodeRef())) {
					assertEquals(15.75d, nut.getValue());
					checks++;
				}
			}

			assertEquals(12, checks);

			return null;

		});

	}

	@Test
	public void testReformulate() throws Exception {
		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef testNodeRef = getTestFolderNodeRef();

		final Date formulatedDate = inWriteTx(() -> {

			productService.formulate(finishedProduct1NodeRef);

			return ((ProductData) alfrescoRepository.findOne(finishedProduct1NodeRef)).getFormulatedDate();

		});

		final LongAdder adder = new LongAdder();

		Callable<Void> callable = () -> {
			String threadName = Thread.currentThread().getName();

			adder.increment();

			AuthenticationUtil.runAsSystem(() -> {

				final NodeRef ecoNodeRef = inWriteTx(() -> {

					/*
					 * create a change order to replace RM4 by RM5
					 */

					logger.debug("create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: "
							+ rawMaterial5NodeRef);

					ChangeOrderData changeOrderData = new ChangeOrderData(
							"ECO " + threadName + " " + adder.doubleValue(), ECOState.ToCalculateWUsed,
							ChangeOrderType.Replacement, new ArrayList<>());

					List<ReplacementListDataItem> replacementList = new ArrayList<>();

					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision,
							Collections.singletonList(rawMaterial4NodeRef), rawMaterial4NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision,
							Collections.singletonList(rawMaterial2NodeRef), rawMaterial2NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision,
							Collections.singletonList(rawMaterial4NodeRef), rawMaterial4NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision,
							Collections.singletonList(packagingKit1NodeRef), packagingKit1NodeRef, 100));
					replacementList.add(new ReplacementListDataItem(RevisionType.NoRevision,
							Collections.singletonList(rawMaterial2NodeRef), rawMaterial2NodeRef, 100));
					changeOrderData.setReplacementList(replacementList);

					NodeRef ecoNodeRef1 = alfrescoRepository.create(testNodeRef, changeOrderData).getNodeRef();

					return ecoNodeRef1;

				});

				// calculate WUsed
				waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

				inWriteTx(() -> {
					ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
					assertNotNull("check ECO exist in DB", dbECOData);
					assertNotNull("Check WUsed list", dbECOData.getWUsedList());

					assertEquals("Check 10 WUsed are impacted", 10, dbECOData.getWUsedList().size());

					for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

						wul.setIsWUsedImpacted(true);
						alfrescoRepository.save(wul);

					}

					return null;
				});

				logger.info("Name Before : " + alfrescoRepository.findOne(finishedProduct1NodeRef).getName()
						+ " in thread " + threadName);

				// apply
				waitForBatchEnd(ecoService.apply(ecoNodeRef, false, false, false));

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

		inWriteTx(() -> {

			ProductData product = (ProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			Assert.assertTrue(product.getFormulatedDate().getTime() > formulatedDate.getTime());
			Assert.assertEquals(6, product.getCompoList().size());
			Assert.assertEquals("PF1", product.getName());

			return null;

		});

	}

	@Test
	public void testQuantityAndLoss() throws Exception {

		final NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
		final NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

		final NodeRef ecoNodeRef = inWriteTx(() -> {

			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, new ArrayList<>());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			ReplacementListDataItem replacement = new ReplacementListDataItem(RevisionType.Minor,
					Arrays.asList(rawMaterial1NodeRef), rawMaterial5NodeRef, 75);
			replacement.setLoss(10.0);
			replacementList.add(replacement);
			changeOrderData.setReplacementList(replacementList);

			NodeRef ecoNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

			return ecoNodeRef1;

		});

		// calculate WUsed
		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		inWriteTx(() -> {

			// verify WUsed
			ChangeOrderData dbECOData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
			assertNotNull("check ECO exist in DB", dbECOData);
			assertNotNull("Check WUsed list", dbECOData.getWUsedList());

			assertEquals("Check 3 WUsed are impacted", 3, dbECOData.getWUsedList().size());

			for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

				wul.setIsWUsedImpacted(true);

				assertNotNull(wul.getSourceItems().get(0));

				if (wul.getSourceItems().contains(finishedProduct2NodeRef)) {
					wul.setQty(50.0);
					wul.setLoss(20.0);
				}

				alfrescoRepository.save(wul);

			}

			return null;

		});

		waitForBatchEnd(ecoService.apply(ecoNodeRef, false, false, false));

		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1NodeRef);

			assertEquals(6, productData.getCompoList().size());

			boolean check = false;

			for (CompoListDataItem compoItem : productData.getCompoList()) {
				if (compoItem.getComponent().equals(rawMaterial5NodeRef)) {
					assertEquals(10.0, compoItem.getLossPerc());
					assertEquals(0.75, compoItem.getQtySubFormula());
					check = true;
				}
			}

			assertTrue(check);

			return null;

		});

		inWriteTx(() -> {

			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct2NodeRef);

			assertEquals(6, productData.getCompoList().size());

			boolean check = false;

			for (CompoListDataItem compoItem : productData.getCompoList()) {
				if (compoItem.getComponent().equals(rawMaterial5NodeRef)) {
					assertEquals(20.0, compoItem.getLossPerc());
					assertEquals(50.0, compoItem.getQtySubFormula());
					check = true;
				}
			}

			assertTrue(check);

			return null;

		});

	}

	@Test
	public void testWUsedListSorting() throws InterruptedException {

		NodeRef FP1 = inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP1");
			finishedProduct.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		NodeRef FP2 = inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP2");
			finishedProduct.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		NodeRef FP3 = inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP3");
			finishedProduct.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(FP1);

			List<CompoListDataItem> compoList = finishedProduct.getCompoList();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));

			return alfrescoRepository.save(finishedProduct);
		});

		inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(FP2);

			List<CompoListDataItem> compoList = finishedProduct.getCompoList();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));
			return alfrescoRepository.save(finishedProduct);
		});

		inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(FP3);

			List<CompoListDataItem> compoList = finishedProduct.getCompoList();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));

			return alfrescoRepository.save(finishedProduct);
		});

		inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(FP1);

			List<CompoListDataItem> compoList = finishedProduct.getCompoList();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));
			return alfrescoRepository.save(finishedProduct);
		});

		NodeRef ecoNodeRef = inWriteTx(() -> {

			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, new ArrayList<>());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			ReplacementListDataItem replacement = new ReplacementListDataItem(RevisionType.Minor,
					Arrays.asList(rawMaterial1NodeRef), rawMaterial5NodeRef, 100);
			replacementList.add(replacement);
			changeOrderData.setReplacementList(replacementList);

			return alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

		});

		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		assertTrue(inReadTx(() -> {

			ChangeOrderData changeOrderData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

			List<WUsedListDataItem> wusedList = changeOrderData.getWUsedList();

			assertFalse(wusedList.isEmpty());

			wusedList.sort((wused1, wused2) -> {
				return wused1.getSort().compareTo(wused2.getSort());
			});

			HashSet<NodeRef> visitedSourceItems = new HashSet<>();

			NodeRef previousSourceItem = null;

			for (WUsedListDataItem wusedItem : wusedList) {

				NodeRef currentSourceItem = wusedItem.getSourceItems().get(0);

				if (previousSourceItem != null && !previousSourceItem.equals(currentSourceItem)) {

					if (visitedSourceItems.contains(currentSourceItem)) {
						return false;
					}
				}

				previousSourceItem = currentSourceItem;

				visitedSourceItems.add(currentSourceItem);
			}

			return true;
		}));
	}

	@Test
	public void testPropertiesToCopy() throws InterruptedException {

		final String descriptionToCopy = "description to copy";

		NodeRef semiFinishedProductNodeRef = inWriteTx(() -> {

			SemiFinishedProductData product = new SemiFinishedProductData();
			product.setName("SF1");
			product.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));

			product.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();

		});

		NodeRef finishedProductNodeRef = inWriteTx(() -> {

			FinishedProductData product = new FinishedProductData();
			product.setName("PF1");
			product.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, semiFinishedProductNodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(semiFinishedProductNodeRef));

			product.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();

		});

		NodeRef semiFinishedProduct2NodeRef = inWriteTx(() -> {

			SemiFinishedProductData product = new SemiFinishedProductData();
			product.setName("SF2");
			product.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));

			product.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();

		});

		NodeRef finishedProduct2NodeRef = inWriteTx(() -> {

			FinishedProductData product = new FinishedProductData();
			product.setName("PF2");
			product.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, semiFinishedProduct2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(semiFinishedProduct2NodeRef));

			product.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();

		});

		NodeRef ecoNodeRef = inWriteTx(() -> {

			ChangeOrderData changeOrderData = new ChangeOrderData("ECO", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, new ArrayList<>());

			changeOrderData.setDescription(descriptionToCopy);
			changeOrderData.setPropertiesToCopy(Arrays.asList("cm:description"));

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			ReplacementListDataItem replacement = new ReplacementListDataItem(RevisionType.Minor,
					Arrays.asList(rawMaterial1NodeRef), rawMaterial12NodeRef, 100);
			replacementList.add(replacement);
			changeOrderData.setReplacementList(replacementList);

			return alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

		});

		waitForBatchEnd(ecoService.calculateWUsedList(ecoNodeRef, false, false));

		inWriteTx(() -> {

			ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

			for (WUsedListDataItem wul : ecoData.getWUsedList()) {

				if (!wul.getSourceItems().contains(semiFinishedProduct2NodeRef)
						&& !wul.getSourceItems().contains(finishedProduct2NodeRef)) {
					wul.setIsWUsedImpacted(true);
				}
			}

			return alfrescoRepository.save(ecoData);

		});

		waitForBatchEnd(ecoService.apply(ecoNodeRef, false, false, false));

		inReadTx(() -> {
			assertNotEquals(descriptionToCopy,
					nodeService.getProperty(rawMaterial1NodeRef, ContentModel.PROP_DESCRIPTION));
			assertEquals(descriptionToCopy,
					nodeService.getProperty(semiFinishedProductNodeRef, ContentModel.PROP_DESCRIPTION));
			assertEquals(descriptionToCopy,
					nodeService.getProperty(finishedProductNodeRef, ContentModel.PROP_DESCRIPTION));
			assertNotEquals(descriptionToCopy,
					nodeService.getProperty(semiFinishedProduct2NodeRef, ContentModel.PROP_DESCRIPTION));
			assertNotEquals(descriptionToCopy,
					nodeService.getProperty(finishedProduct2NodeRef, ContentModel.PROP_DESCRIPTION));
			return null;
		});

	}

	@Test
	public void testSingleItemPersistence() throws InterruptedException {
		NodeRef semiFinishedProductNodeRef = inWriteTx(() -> {

			SemiFinishedProductData product = new SemiFinishedProductData();
			product.setName("SF1");
			product.setQty(2d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial1NodeRef));
			product.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();

		});

		NodeRef originalCompoListItem = findCompoListItemNodeRef(semiFinishedProductNodeRef, rawMaterial1NodeRef);

		NodeRef ecoNodeRef1 = inWriteTx(() -> {

			ChangeOrderData changeOrderData = new ChangeOrderData("ECO_1", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, new ArrayList<>());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			ReplacementListDataItem replacement = new ReplacementListDataItem(RevisionType.Minor,
					Arrays.asList(rawMaterial1NodeRef), rawMaterial2NodeRef, 100);
			replacementList.add(replacement);
			changeOrderData.setReplacementList(replacementList);

			return alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

		});

		waitForBatchEnd(ecoService.apply(ecoNodeRef1, false, true, false));

		NodeRef newCompoListItem = findCompoListItemNodeRef(semiFinishedProductNodeRef, rawMaterial2NodeRef);

		assertEquals(originalCompoListItem, newCompoListItem);

		NodeRef ecoNodeRef2 = inWriteTx(() -> {

			ChangeOrderData changeOrderData = new ChangeOrderData("ECO_2", ECOState.ToCalculateWUsed,
					ChangeOrderType.Replacement, new ArrayList<>());

			List<ReplacementListDataItem> replacementList = new ArrayList<>();

			ReplacementListDataItem replacement = new ReplacementListDataItem(RevisionType.Minor,
					Arrays.asList(rawMaterial2NodeRef), rawMaterial2NodeRef, 75);
			replacementList.add(replacement);
			changeOrderData.setReplacementList(replacementList);

			return alfrescoRepository.create(getTestFolderNodeRef(), changeOrderData).getNodeRef();

		});

		waitForBatchEnd(ecoService.apply(ecoNodeRef2, false, true, false));

		newCompoListItem = findCompoListItemNodeRef(semiFinishedProductNodeRef, rawMaterial2NodeRef);

		assertEquals(originalCompoListItem, newCompoListItem);

	}

	private NodeRef findCompoListItemNodeRef(NodeRef semiFinishedProductNodeRef, NodeRef productNodeRef) {
		return inReadTx(() -> {
			SemiFinishedProductData semiFinishedProductData = (SemiFinishedProductData) alfrescoRepository
					.findOne(semiFinishedProductNodeRef);

			for (CompoListDataItem compoListItem : semiFinishedProductData.getCompoList()) {
				if (compoListItem.getProduct().equals(productNodeRef)) {
					return compoListItem.getNodeRef();
				}
			}
			return null;
		});
	}

	private String getVersionLabel(NodeRef productNodeRef) {
		return (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_VERSION_LABEL);
	}

}
