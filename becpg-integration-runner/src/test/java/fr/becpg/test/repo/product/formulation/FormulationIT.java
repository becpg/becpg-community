/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * The Class FormulationTest. TODO Split in several classes and refactor
 *
 * @author querephi
 */
public class FormulationIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationIT.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Test ingredients calculating.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testIngredientsCalculating()  {

		logger.info("testIngredientsCalculating");

		NodeRef finishedProductNodeRef1 = inWriteTx(() -> {

			/**
			 * Finished product 1
			 */
			logger.debug("/*************************************/");
			logger.debug("/*-- Create Finished product 1--*/");
			logger.debug("/*************************************/");
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product 1");
			finishedProduct1.setLegalName("Legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial12NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF12NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial13NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial14NodeRef));
			finishedProduct1.getCompoListView().setCompoList(compoList1);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct1 = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

			// verify IngList
			// 1 * lSF1 [Pâte, DETAIL]
			// 1 * RM1 [ , ] , ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ;
			// geo1|geo2 //
			// 2 * RM2 [ , DETAIL] , ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ;
			// bio2 ; geo1|geo2 //
			// 1 * lSF2 [Garniture, DETAIL]
			// 3 * RM3 [ , ] , ingList : // // 4 ing3 ; bio1|bio2 ; geo2
			// 3 * RM4 [ , ] , ingList : // // 4 ing3 ; bio1|bio2 ; geo2 // 2
			// ing4 ; bio1|bio2 ; geo2
			assertNotNull("IngList is null", formulatedProduct1.getIngList());
			assertEquals(4, formulatedProduct1.getIngList().size());
			for (IngListDataItem ingListDataItem1 : formulatedProduct1.getIngList()) {

				StringBuilder geoOriginsText1 = new StringBuilder();
				for (NodeRef geoOrigin1 : ingListDataItem1.getGeoOrigin()) {
					geoOriginsText1.append(nodeService.getProperty(geoOrigin1, BeCPGModel.PROP_CHARACT_NAME)).append(", ");
				}

				String bioOriginsText1 = "";
				for (NodeRef bioOrigin1 : ingListDataItem1.getBioOrigin()) {
					bioOriginsText1 += nodeService.getProperty(bioOrigin1, BeCPGModel.PROP_CHARACT_NAME) + ", ";
				}

				String trace1 = "ing: " + nodeService.getProperty(ingListDataItem1.getIng(), BeCPGModel.PROP_CHARACT_NAME) + " - qty: "
						+ ingListDataItem1.getQtyPerc() + " - geo origins: "
						+ geoOriginsText1.append(" - bio origins: ").append(bioOriginsText1).append(" is gmo: ").append(ingListDataItem1.getIsGMO())
								.append(" is ionized: ").append(ingListDataItem1.getIsIonized()).toString();
				logger.debug(trace1);

				DecimalFormat df1 = new DecimalFormat("0.000000");

				// ing: ing1 - qty: 9.25925925925926 - geo origins: geoOrigin1,
				// - bio origins: bioOrigin1, is gmo: true
				if (ingListDataItem1.getIng().equals(ing1)) {
					assertEquals("ing1.getQtyPerc() == 9.25925925925926, actual values: " + trace1, df1.format(9.25925925925926),
							df1.format(ingListDataItem1.getQtyPerc()));
					assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace1, true,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace1, false,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace1, true,
							ingListDataItem1.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace1, false,
							ingListDataItem1.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace1, true, Boolean.TRUE.equals(ingListDataItem1.getIsGMO()));
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace1, true, Boolean.TRUE.equals(ingListDataItem1.getIsIonized()));
				}
				// ing: ing2 - qty: 24.074074074074076 - geo origins:
				// geoOrigin1, geoOrigin2, - bio origins: bioOrigin1,
				// bioOrigin2, is gmo: false
				if (ingListDataItem1.getIng().equals(ing2)) {
					assertEquals("ing2.getQtyPerc() == 24.074074074074076, actual values: " + trace1, df1.format(24.074074074074076),
							df1.format(ingListDataItem1.getQtyPerc()));
					assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace1, true,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace1, true,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace1, true,
							ingListDataItem1.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace1, true,
							ingListDataItem1.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace1, false, Boolean.TRUE.equals(ingListDataItem1.getIsGMO()));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace1, false, Boolean.TRUE.equals(ingListDataItem1.getIsIonized()));
				}
				// ing: ing3 - qty: 55.55555555555556 - geo origins: geoOrigin2,
				// - bio origins: bioOrigin1, bioOrigin2, is gmo: true
				if (ingListDataItem1.getIng().equals(ing3)) {
					assertEquals("ing3.getQtyPerc() == 55.55555555555556, actual values: " + trace1, df1.format(55.55555555555556),
							df1.format(ingListDataItem1.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace1, false,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace1, true,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace1, true,
							ingListDataItem1.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace1, true,
							ingListDataItem1.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace1, true, Boolean.TRUE.equals(ingListDataItem1.getIsGMO()));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace1, true, Boolean.TRUE.equals(ingListDataItem1.getIsIonized()));
				}
				// ing: ing4 - qty: 11.11111111111111 - geo origins: geoOrigin2,
				// - bio origins: bioOrigin1, bioOrigin2, is gmo: true
				if (ingListDataItem1.getIng().equals(ing4)) {
					assertEquals("ing3.getQtyPerc() == 11.11111111111111, actual values: " + trace1, df1.format(11.11111111111111),
							df1.format(ingListDataItem1.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace1, false,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace1, true,
							ingListDataItem1.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace1, true,
							ingListDataItem1.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace1, true,
							ingListDataItem1.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace1, true, Boolean.TRUE.equals(ingListDataItem1.getIsGMO()));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace1, true, Boolean.TRUE.equals(ingListDataItem1.getIsIonized()));
				}
			}

			/**
			 * Finished product 2
			 */
			logger.debug("/**********************************/");
			logger.debug("/*-- Create Finished product 2 --*/");
			logger.debug("/**********************************/");
			FinishedProductData finishedProduct2 = new FinishedProductData();
			finishedProduct2.setName("Finished product 2");
			finishedProduct2.setLegalName("Legal Finished product 2");
			finishedProduct2.setQty(2d);
			finishedProduct2.setUnit(ProductUnit.kg);
			finishedProduct2.setDensity(1d);
			List<CompoListDataItem> compoList2 = new ArrayList<>();
			compoList2.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
			compoList2.add(CompoListDataItem.build().withParent(compoList2.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial11NodeRef));
			compoList2.add(CompoListDataItem.build().withParent(compoList2.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial12NodeRef));
			compoList2.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF12NodeRef));
			compoList2.add(CompoListDataItem.build().withParent(compoList2.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial13NodeRef));
			compoList2.add(CompoListDataItem.build().withParent(compoList2.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.DoNotDeclare).withProduct(rawMaterial14NodeRef));
			finishedProduct2.getCompoListView().setCompoList(compoList2);
			NodeRef finishedProductNodeRef2 = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct2).getNodeRef();

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef2);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct2 = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef2);

			// verify IngList
			// 1 * lSF1 [Pâte, DETAIL]
			// 1 * RM1 [ , ] , ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ;
			// geo1|geo2 //
			// 2 * RM2 [ , DETAIL] , ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ;
			// bio2 ; geo1|geo2 //
			// 1 * lSF2 [Garniture, DETAIL]
			// 3 * RM3 [ , ] , ingList : // // 4 ing3 ; bio1|bio2 ; geo2
			// 3 * RM4 [ , DO_NOT_LABEL] , ingList : // // 4 ing3 ; bio1|bio2 ;
			// geo2 // 2 ing4 ; bio1|bio2 ; geo2
			assertNotNull("IngList is null", formulatedProduct2.getIngList());
			for (IngListDataItem ingListDataItem2 : formulatedProduct2.getIngList()) {

				StringBuilder geoOriginsText2 = new StringBuilder();
				for (NodeRef geoOrigin2 : ingListDataItem2.getGeoOrigin()) {
					geoOriginsText2.append(nodeService.getProperty(geoOrigin2, BeCPGModel.PROP_CHARACT_NAME)).append(", ");
				}

				String bioOriginsText2 = "";
				for (NodeRef bioOrigin2 : ingListDataItem2.getBioOrigin()) {
					bioOriginsText2 += nodeService.getProperty(bioOrigin2, BeCPGModel.PROP_CHARACT_NAME) + ", ";
				}

				DecimalFormat df2 = new DecimalFormat("0.000000");
				String trace2 = "ing: " + nodeService.getProperty(ingListDataItem2.getIng(), BeCPGModel.PROP_CHARACT_NAME) + " - qty: "
						+ df2.format(ingListDataItem2.getQtyPerc()) + " - geo origins: "
						+ geoOriginsText2.append(" - bio origins: ").append(bioOriginsText2).append(" is gmo: ").append(ingListDataItem2.getIsGMO())
								.append(" is ionized: ").append(ingListDataItem2.getIsIonized()).toString();
				logger.debug(trace2);

				// ing: ing1 - qty: 9.25925925925926 - geo origins: geoOrigin1,
				// - bio origins: bioOrigin1, is gmo: true
				if (ingListDataItem2.getIng().equals(ing1)) {
					assertEquals(df2.format(9.25925925925926), df2.format(ingListDataItem2.getQtyPerc()));
					assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace2, true,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace2, false,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace2, true,
							ingListDataItem2.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace2, false,
							ingListDataItem2.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace2, true, Boolean.TRUE.equals(ingListDataItem2.getIsGMO()));
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace2, true, Boolean.TRUE.equals(ingListDataItem2.getIsIonized()));
				}
				// ing: ing2 - qty: 24.074074074074076 - geo origins:
				// geoOrigin1, geoOrigin2, - bio origins: bioOrigin1,
				// bioOrigin2, is gmo: false
				if (ingListDataItem2.getIng().equals(ing2)) {
					assertEquals(df2.format(24.074074074074076), df2.format(ingListDataItem2.getQtyPerc()));
					assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace2, true,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace2, true,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace2, true,
							ingListDataItem2.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace2, true,
							ingListDataItem2.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace2, false, Boolean.FALSE.equals(ingListDataItem2.getIsGMO()));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace2, false, Boolean.FALSE.equals(ingListDataItem2.getIsIonized()));
				}
				// ing: ing3 - qty: 55.55555555555556 - geo origins: geoOrigin2,
				// - bio origins: bioOrigin1, bioOrigin2, is gmo: true
				if (ingListDataItem2.getIng().equals(ing3)) {
					assertEquals(df2.format(55.55555555555556), df2.format(ingListDataItem2.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace2, false,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace2, true,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace2, true,
							ingListDataItem2.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace2, true,
							ingListDataItem2.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace2, true, Boolean.TRUE.equals(ingListDataItem2.getIsGMO()));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace2, true, Boolean.TRUE.equals(ingListDataItem2.getIsIonized()));
				}
				// ing: ing4 - qty: 11.11111111111111 - geo origins: geoOrigin2,
				// - bio origins: bioOrigin1, bioOrigin2, is gmo: true
				if (ingListDataItem2.getIng().equals(ing4)) {
					assertEquals(df2.format(11.11111111111111), df2.format(ingListDataItem2.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace2, false,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace2, true,
							ingListDataItem2.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace2, true,
							ingListDataItem2.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace2, true,
							ingListDataItem2.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace2, true, Boolean.TRUE.equals(ingListDataItem2.getIsGMO()));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace2, true, Boolean.TRUE.equals(ingListDataItem2.getIsIonized()));
				}
			}

			return null;

		});

	}

	/**
	 * Test the formulation of the costs and nuts in kg and g.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulateCostAndNutOfProductInkgAndg() {

		logger.info("testFormulateCostAndNutOfProductInkgAndg");

		inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.g)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			finishedProduct.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNut(nut1));
			nutList.add(NutListDataItem.build().withNut(nut2));
			finishedProduct.setNutList(nutList);

			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			logger.debug("unit of product to formulate: " + finishedProduct.getUnit());

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			logger.debug("unit of product formulated: " + finishedProduct.getUnit());

			// costs
			int checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace1 = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 3.001, actual values: " + trace1, 3.001d, costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
					checks++;
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 4.002, actual values: " + trace1, 4.002d, costListDataItem.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
					checks++;
				}
			}
			assertEquals(2, checks);
			// nuts
			checks = 0;
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
				String trace2 = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
				logger.debug(trace2);
				if (nutListDataItem.getNut().equals(nut1)) {
					assertEquals("nut1.getValue() == 2.001, actual values: " + trace2, 2.001d, nutListDataItem.getValue());
					assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace2, "kJ/100g", nutListDataItem.getUnit());
					assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					checks++;
				}
				if (nutListDataItem.getNut().equals(nut2)) {
					assertEquals("nut2.getValue() == 4.002, actual values: " + trace2, 4.002d, nutListDataItem.getValue());
					assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace2, "kcal/100g", nutListDataItem.getUnit());
					assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					checks++;
				}
			}
			assertEquals(2, checks);

			return null;

		});

	}

	/**
	 * Test the formulation of the costs and nuts in kg, g, mL and m.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulateCostAndNutOfProductInkgAndgAndmLAndm() throws Exception {

		logger.info("testFormulateCostAndNutOfProductInkgAndgAndmLAndm");

		inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setQty(20d);
			finishedProduct.setNetWeight(0.1d);
			finishedProduct.setUnit(ProductUnit.P);
			finishedProduct.setDensity(0.1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(42d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(40d).withUnit(ProductUnit.g)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.mL)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(30d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(30d).withUnit(ProductUnit.g)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(0.05d).withUnit(ProductUnit.P)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial5NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			finishedProduct.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNut(nut1));
			nutList.add(NutListDataItem.build().withNut(nut2));
			finishedProduct.setNutList(nutList);

			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			logger.debug("unit of product to formulate: " + finishedProduct.getUnit());

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			logger.debug("unit of product formulated: " + finishedProduct.getUnit());
			DecimalFormat df = new DecimalFormat("0.000");
			int checks = 0;
			// costs
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace1 = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("check cost", df.format(0.009d), df.format(costListDataItem.getValue()));
					assertEquals("check cost", df.format(0.177d), df.format(costListDataItem.getValuePerProduct()));
					assertEquals("check cost unit", "€/P", costListDataItem.getUnit());
					checks++;
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("check cost", df.format(0.009d), df.format(costListDataItem.getValue()));
					assertEquals("check cost", df.format(0.174d), df.format(costListDataItem.getValuePerProduct()));
					assertEquals("check cost unit", "€/P", costListDataItem.getUnit());
					checks++;
				}
			}
			assertEquals(2, checks);
			// nuts
			checks = 0;
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
				String trace2 = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
				logger.debug(trace2);
				if (nutListDataItem.getNut().equals(nut1)) {
					assertEquals("check nut", df.format(0.72d), df.format(nutListDataItem.getValue()));
					assertEquals("check nut unit", "kJ/100g", nutListDataItem.getUnit());
					assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					checks++;
				}
				if (nutListDataItem.getNut().equals(nut2)) {
					assertEquals("check nut", df.format(1.44d), df.format(nutListDataItem.getValue()));
					assertEquals("check nut unit", "kcal/100g", nutListDataItem.getUnit());
					assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					checks++;
				}
			}
			assertEquals(2, checks);

			return null;

		});

	}

	/**
	 * Test the formulation with density (kg and L)
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulateWithDensity() throws Exception {

		logger.info("testFormulateWithDensity");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setQty(2.5d);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.L).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial6NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			DecimalFormat df = new DecimalFormat("0.000");
			int checks = 0;
			assertNotNull("IngList is null", formulatedProduct.getIngList());
			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {

				String trace = "ing: " + nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME) + " - qty: "
						+ df.format(ingListDataItem.getQtyPerc());
				logger.debug(trace);

				if (ingListDataItem.getIng().equals(ing1)) {
					assertEquals(df.format(60.5555555555556), df.format(ingListDataItem.getQtyPerc()));
					checks++;
				}
				// ing: ing2 - qty: 0.394444444444444 - geo origins: geoOrigin1,
				// geoOrigin2, - bio origins: bioOrigin1, bioOrigin2, is gmo:
				// false
				if (ingListDataItem.getIng().equals(ing2)) {
					assertEquals(df.format(39.4444444444444), df.format(ingListDataItem.getQtyPerc()));
					checks++;
				}
			}

			assertEquals(2, checks);

			return null;

		});

	}

	/**
	 * Test allergen list calculating.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testAllergenListCalculating() throws Exception {

		logger.info("testAllergenListCalculating");

		final NodeRef SFProduct1NodeRef = inWriteTx(() -> {

			/*-- Create products --*/
			logger.debug("/*-- Create products --*/");

			// SF1
			SemiFinishedProductData SFProduct1 = new SemiFinishedProductData();
			SFProduct1.setName("semi fini 1");
			SFProduct1.setLegalName("Legal semi fini 1");
			SFProduct1.setUnit(ProductUnit.kg);
			SFProduct1.setQty(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			SFProduct1.getCompoListView().setCompoList(compoList1);
			return alfrescoRepository.create(getTestFolderNodeRef(), SFProduct1).getNodeRef();

		});

		final NodeRef SFProduct2NodeRef = inWriteTx(() -> {

			// SF2
			SemiFinishedProductData SFProduct2 = new SemiFinishedProductData();
			SFProduct2.setName("semi fini 2");
			SFProduct2.setLegalName("Legal semi fini 2");
			SFProduct2.setUnit(ProductUnit.kg);
			SFProduct2.setQty(1d);
			List<CompoListDataItem> compoList2 = new ArrayList<>();
			compoList2.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList2.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			SFProduct2.getCompoListView().setCompoList(compoList2);
			return alfrescoRepository.create(getTestFolderNodeRef(), SFProduct2).getNodeRef();

		});

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			// PF1
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(SFProduct1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(SFProduct2NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate products --*/");
			productService.formulate(SFProduct1NodeRef);
			productService.formulate(SFProduct2NodeRef);
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");

			// Verify SF1
			ProductData formulatedSF1 = (ProductData) alfrescoRepository.findOne(SFProduct1NodeRef);

			// allergens
			assertNotNull("AllergenList is not null", formulatedSF1.getAllergenList());
			for (AllergenListDataItem allergenListDataItem1 : formulatedSF1.getAllergenList()) {
				StringBuilder voluntarySources1 = new StringBuilder();
				for (NodeRef part1 : allergenListDataItem1.getVoluntarySources()) {
					voluntarySources1.append(nodeService.getProperty(part1, BeCPGModel.PROP_CHARACT_NAME)).append(", ");
				}

				String inVoluntarySources1 = "";
				for (NodeRef part2 : allergenListDataItem1.getInVoluntarySources()) {
					inVoluntarySources1 += nodeService.getProperty(part2, BeCPGModel.PROP_CHARACT_NAME) + ", ";
				}

				String trace1 = "SF1 allergen: " + nodeService.getProperty(allergenListDataItem1.getAllergen(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - voluntary: " + allergenListDataItem1.getVoluntary() + " - involuntary: " + allergenListDataItem1.getInVoluntary()
						+ " - voluntary sources:" + voluntarySources1.append(" - involuntary sources:").append(inVoluntarySources1).toString();
				logger.debug(trace1);

				// allergen1 - voluntary: true - involuntary: false - voluntary
				// sources:Raw material 1, Raw material 2 - involuntary sources:
				if (allergenListDataItem1.getAllergen().equals(allergen1)) {
					assertEquals("SF1 allergen1.getVoluntary()", true, Boolean.TRUE.equals(allergenListDataItem1.getVoluntary()));
					assertEquals("SF1 allergen1.getInVoluntary()", false, Boolean.TRUE.equals(allergenListDataItem1.getInVoluntary()));
					assertEquals("SF1 allergen1.getVoluntarySources()", true,
							allergenListDataItem1.getVoluntarySources().contains(rawMaterial1NodeRef));
					assertEquals("SF1 allergen1.getVoluntarySources()", true,
							allergenListDataItem1.getVoluntarySources().contains(rawMaterial2NodeRef));
					assertEquals("SF1 allergen1.getInVoluntarySources()", 0, allergenListDataItem1.getInVoluntarySources().size());
				}
				// allergen2 - voluntary: false - involuntary: true - voluntary
				// sources: - involuntary sources:Raw material 2,
				if (allergenListDataItem1.getAllergen().equals(allergen2)) {
					assertEquals("SF1 allergen2.getVoluntary() ", false, Boolean.TRUE.equals(allergenListDataItem1.getVoluntary()));
					assertEquals("SF1 allergen2.getInVoluntary() ", true, Boolean.TRUE.equals(allergenListDataItem1.getInVoluntary()));
					assertEquals("SF1 allergen2.getInVoluntarySources()", true,
							allergenListDataItem1.getInVoluntarySources().contains(rawMaterial2NodeRef));
					assertEquals("SF1 allergen2.getVoluntarySources()", 0, allergenListDataItem1.getVoluntarySources().size());
				}
				// allergen: allergen3 - voluntary: true - involuntary: true -
				// voluntary sources:Raw material 3, - involuntary sources:Raw
				// material 3,
				if (allergenListDataItem1.getAllergen().equals(allergen3)) {
					assertEquals("SF1 allergen3.getVoluntary() ", false, Boolean.TRUE.equals(allergenListDataItem1.getVoluntary()));
					assertEquals("SF1 allergen3.getInVoluntary() ", false, Boolean.TRUE.equals(allergenListDataItem1.getInVoluntary()));
					assertEquals("SF1 allergen3.getVoluntarySources()", 0, allergenListDataItem1.getVoluntarySources().size());
					assertEquals("SF1 allergen3.getInVoluntarySources() ", 0, allergenListDataItem1.getInVoluntarySources().size());
				}
				// allergen4 - voluntary: false - involuntary: false - voluntary
				// sources: - involuntary sources:
				if (allergenListDataItem1.getAllergen().equals(allergen4)) {
					assertEquals("SF1 allergen4.getVoluntary() == false, actual values: " + trace1, false,
							Boolean.TRUE.equals(allergenListDataItem1.getVoluntary()));
					assertEquals("SF1 allergen4.getInVoluntary() == false, actual values: " + trace1, false,
							Boolean.TRUE.equals(allergenListDataItem1.getInVoluntary()));
					assertEquals("SF1 allergen4.getVoluntarySources()", 0, allergenListDataItem1.getVoluntarySources().size());
					assertEquals("SF1 allergen4.getInVoluntarySources()", 0, allergenListDataItem1.getInVoluntarySources().size());
				}
			}

			// Verify SF1
			ProductData formulatedSF2 = (ProductData) alfrescoRepository.findOne(SFProduct2NodeRef);

			// allergens
			assertNotNull("AllergenList is not null", formulatedSF2.getAllergenList());
			for (AllergenListDataItem allergenListDataItem2 : formulatedSF2.getAllergenList()) {
				StringBuilder voluntarySources2 = new StringBuilder();
				for (NodeRef part3 : allergenListDataItem2.getVoluntarySources()) {
					voluntarySources2.append(nodeService.getProperty(part3, BeCPGModel.PROP_CHARACT_NAME)).append(", ");
				}

				String inVoluntarySources2 = "";
				for (NodeRef part4 : allergenListDataItem2.getInVoluntarySources()) {
					inVoluntarySources2 += nodeService.getProperty(part4, BeCPGModel.PROP_CHARACT_NAME) + ", ";
				}

				String trace2 = "SF2 allergen: " + nodeService.getProperty(allergenListDataItem2.getAllergen(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - voluntary: " + allergenListDataItem2.getVoluntary() + " - involuntary: " + allergenListDataItem2.getInVoluntary()
						+ " - voluntary sources:" + voluntarySources2.append(" - involuntary sources:").append(inVoluntarySources2).toString();
				logger.debug(trace2);

				// allergen1 - voluntary: true - involuntary: false - voluntary
				// sources:Raw material 1, Raw material 2 - involuntary sources:
				if (allergenListDataItem2.getAllergen().equals(allergen1)) {
					assertEquals("SF2 allergen1.getVoluntary()", false, Boolean.TRUE.equals(allergenListDataItem2.getVoluntary()));
					assertEquals("SF2 allergen1.getInVoluntary()", false, Boolean.TRUE.equals(allergenListDataItem2.getInVoluntary()));
					assertEquals("SF2 allergen1.getVoluntarySources()", 0, allergenListDataItem2.getVoluntarySources().size());
					assertEquals("SF2 allergen1.getVoluntarySources()", 0, allergenListDataItem2.getVoluntarySources().size());
					assertEquals("SF2 allergen1.getInVoluntarySources()", 0, allergenListDataItem2.getInVoluntarySources().size());
				}
				// allergen2 - voluntary: false - involuntary: true - voluntary
				// sources: - involuntary sources:Raw material 2,
				if (allergenListDataItem2.getAllergen().equals(allergen2)) {
					assertEquals("SF2 allergen2.getVoluntary() ", false, Boolean.TRUE.equals(allergenListDataItem2.getVoluntary()));
					assertEquals("SF2 allergen2.getInVoluntary() ", false, Boolean.TRUE.equals(allergenListDataItem2.getInVoluntary()));
					assertEquals("SF2 allergen2.getInVoluntarySources()", 0, allergenListDataItem2.getInVoluntarySources().size());
					assertEquals("SF2 allergen2.getVoluntarySources()", 0, allergenListDataItem2.getVoluntarySources().size());
				}
				// allergen: allergen3 - voluntary: true - involuntary: true -
				// voluntary sources:Raw material 3, - involuntary sources:Raw
				// material 3,
				if (allergenListDataItem2.getAllergen().equals(allergen3)) {
					assertEquals("SF2 allergen3.getVoluntary() ", true, Boolean.TRUE.equals(allergenListDataItem2.getVoluntary()));
					assertEquals("SF2 allergen3.getInVoluntary() ", true, Boolean.TRUE.equals(allergenListDataItem2.getInVoluntary()));
					assertEquals("SF2 allergen3.getVoluntarySources()", true,
							allergenListDataItem2.getVoluntarySources().contains(rawMaterial3NodeRef));
					assertEquals("SF2 allergen3.getInVoluntarySources() ", true,
							allergenListDataItem2.getInVoluntarySources().contains(rawMaterial3NodeRef));
				}
				// allergen4 - voluntary: false - involuntary: false - voluntary
				// sources: - involuntary sources:
				if (allergenListDataItem2.getAllergen().equals(allergen4)) {
					assertEquals("SF2 allergen4.getVoluntary() == false, actual values: " + trace2, false,
							Boolean.TRUE.equals(allergenListDataItem2.getVoluntary()));
					assertEquals("SF2 allergen4.getInVoluntary() == false, actual values: " + trace2, false,
							Boolean.TRUE.equals(allergenListDataItem2.getInVoluntary()));
					assertEquals("SF2 allergen4.getVoluntarySources()", 0, allergenListDataItem2.getVoluntarySources().size());
					assertEquals("SF1 allergen4.getInVoluntarySources()", 0, allergenListDataItem2.getInVoluntarySources().size());
				}
			}

			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// allergens
			assertNotNull("AllergenList is null", formulatedProduct.getAllergenList());
			for (AllergenListDataItem allergenListDataItem3 : formulatedProduct.getAllergenList()) {
				StringBuilder voluntarySources3 = new StringBuilder();
				for (NodeRef part5 : allergenListDataItem3.getVoluntarySources()) {
					voluntarySources3.append(nodeService.getProperty(part5, BeCPGModel.PROP_CHARACT_NAME)).append(", ");
				}

				String inVoluntarySources3 = "";
				for (NodeRef part6 : allergenListDataItem3.getInVoluntarySources()) {
					inVoluntarySources3 += nodeService.getProperty(part6, BeCPGModel.PROP_CHARACT_NAME) + ", ";
				}

				String trace3 = "PF allergen: " + nodeService.getProperty(allergenListDataItem3.getAllergen(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - voluntary: " + allergenListDataItem3.getVoluntary() + " - involuntary: " + allergenListDataItem3.getInVoluntary()
						+ " - voluntary sources:" + voluntarySources3.append(" - involuntary sources:").append(inVoluntarySources3).toString();
				logger.debug(trace3);

				// allergen1 - voluntary: true - involuntary: false - voluntary
				// sources:Raw material 1, Raw material 2 - involuntary sources:
				if (allergenListDataItem3.getAllergen().equals(allergen1)) {
					assertEquals("allergen1.getVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem3.getVoluntary()));
					assertEquals("allergen1.getInVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem3.getInVoluntary()));
					assertEquals("allergen1.getVoluntarySources() contains Raw material 1, actual values: " + trace3, true,
							allergenListDataItem3.getVoluntarySources().contains(rawMaterial1NodeRef));
					assertEquals("allergen1.getVoluntarySources() contains Raw material 2, actual values: " + trace3, true,
							allergenListDataItem3.getVoluntarySources().contains(rawMaterial2NodeRef));
					assertEquals("allergen1.getInVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem3.getInVoluntarySources().size());
				}
				// allergen2 - voluntary: false - involuntary: true - voluntary
				// sources: - involuntary sources:Raw material 2,
				if (allergenListDataItem3.getAllergen().equals(allergen2)) {
					assertEquals("allergen2.getVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem3.getVoluntary()));
					assertEquals("allergen2.getInVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem3.getInVoluntary()));
					assertEquals("allergen2.getInVoluntarySources() contains Raw material 2, actual values: " + trace3, true,
							allergenListDataItem3.getInVoluntarySources().contains(rawMaterial2NodeRef));
					assertEquals("allergen2.getVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem3.getVoluntarySources().size());
				}
				// allergen: allergen3 - voluntary: true - involuntary: true -
				// voluntary sources:Raw material 3, - involuntary sources:Raw
				// material 3,
				if (allergenListDataItem3.getAllergen().equals(allergen3)) {
					assertEquals("allergen3.getVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem3.getVoluntary()));
					assertEquals("allergen3.getInVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem3.getInVoluntary()));
					assertEquals("allergen3.getVoluntarySources() contains Raw material 3, actual values: " + trace3, true,
							allergenListDataItem3.getVoluntarySources().contains(rawMaterial3NodeRef));
					assertEquals("allergen3.getInVoluntarySources() contains Raw material 3, actual values: " + trace3, true,
							allergenListDataItem3.getInVoluntarySources().contains(rawMaterial3NodeRef));
				}
				// allergen4 - voluntary: false - involuntary: false - voluntary
				// sources: - involuntary sources:
				if (allergenListDataItem3.getAllergen().equals(allergen4)) {
					assertEquals("allergen4.getVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem3.getVoluntary()));
					assertEquals("allergen4.getInVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem3.getInVoluntary()));
					assertEquals("allergen4.getVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem3.getVoluntarySources().size());
					assertEquals("allergen4.getInVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem3.getInVoluntarySources().size());
				}
			}

			return null;

		});

	}

	/**
	 * Test formulate raw material.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulateRawMaterial() throws Exception {

		logger.info("testFormulateRawMaterial");

		inWriteTx(() -> {
			// check before formulation
			RawMaterialData rmData1 = (RawMaterialData) alfrescoRepository.findOne(rawMaterial1NodeRef);
			assertNotNull("check costList", rmData1.getCostList());
			assertEquals("check costList", 2, rmData1.getCostList().size());
			assertNotNull("check nutList", rmData1.getNutList());
			assertEquals("check nutList", 4, rmData1.getNutList().size());

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut5");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			properties.put(PLMModel.PROP_NUTGDA, 2000d);
			properties.put(PLMModel.PROP_NUT_FORMULA, "10d+50");
			NodeRef nut5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();

			NutListDataItem nutListDataItem = new NutListDataItem();
			nutListDataItem.setNut(nut5);
			nutListDataItem.setIsManual(false);

			// One nut added by the template and the synchronize
			productService.formulate(rmData1);
			assertEquals("check nutList", 5, rmData1.getNutList().size());

			rmData1.getNutList().add(nutListDataItem);

			assertNotNull("check allergenList", rmData1.getAllergenList());
			assertEquals("check allergenList", 4, rmData1.getAllergenList().size());
			assertNotNull("check ingList", rmData1.getIngList());
			assertEquals("check ingList", 2, rmData1.getIngList().size());
			assertEquals("check compo list", 0, rmData1.getCompoListView().getCompoList().size());

			// formulation
			productService.formulate(rmData1);

			assertNotNull("check costList", rmData1.getCostList());
			// one cost added from template
			assertEquals("check costList", 3, rmData1.getCostList().size());
			assertNotNull("check nutList", rmData1.getNutList());

			int assertCount = 0;
			for (NutListDataItem nutListEl : rmData1.getNutList()) {
				if (nutListEl.getNut().equals(nut5)) {
					assertEquals(nutListEl.getValue(), 60d);
					assertCount++;
				}
				if (nutListEl.getNut().equals(nut3)) {
					assertEquals(nutListEl.getValue(), 4.0);
					assertCount++;
				}
			}
			assertEquals(2, assertCount);
			assertEquals("check nutList", 6, rmData1.getNutList().size());
			assertNotNull("check allergenList", rmData1.getAllergenList());
			assertEquals("check allergenList", 4, rmData1.getAllergenList().size());
			assertNotNull("check ingList", rmData1.getIngList());
			assertEquals("check ingList", 2, rmData1.getIngList().size());
			assertEquals("check compo list", 0, rmData1.getCompoListView().getCompoList().size());

			return null;

		});

	}

	/**
	 * Test formulate product, that has loss perc defined
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateWithLoss() {

		logger.info("testCalculateWithLoss");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(10d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(20d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			DecimalFormat df = new DecimalFormat("0.####");

			// costs
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace1 = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 4.7425003, actual values: " + trace1, df.format(4.7425d),
							df.format(costListDataItem.getValue()));
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 7.175, actual values: " + trace1, df.format(7.175d), df.format(costListDataItem.getValue()));
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
				}
			}

			// nuts
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
				String trace2 = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
				logger.debug(trace2);
				if (nutListDataItem.getNut().equals(nut1)) {
					assertEquals("nut1.getValue() == 3, actual values: " + trace2, 3d, nutListDataItem.getValue());
					assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace2, "kJ/100g", nutListDataItem.getUnit());
					assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
				}
				if (nutListDataItem.getNut().equals(nut2)) {
					assertEquals("nut2.getValue() == 6, actual values: " + trace2, 6d, nutListDataItem.getValue());
					assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace2, "kcal/100g", nutListDataItem.getUnit());
					assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
				}
			}
			// allergens
			assertNotNull("AllergenList is null", formulatedProduct.getAllergenList());
			for (AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()) {
				StringBuilder voluntarySources = new StringBuilder();
				for (NodeRef part1 : allergenListDataItem.getVoluntarySources()) {
					voluntarySources.append(nodeService.getProperty(part1, BeCPGModel.PROP_CHARACT_NAME)).append(", ");
				}

				String inVoluntarySources = "";
				for (NodeRef part2 : allergenListDataItem.getInVoluntarySources()) {
					inVoluntarySources += nodeService.getProperty(part2, BeCPGModel.PROP_CHARACT_NAME) + ", ";
				}

				String trace3 = "allergen: " + nodeService.getProperty(allergenListDataItem.getAllergen(), BeCPGModel.PROP_CHARACT_NAME)
						+ " - voluntary: " + allergenListDataItem.getVoluntary() + " - involuntary: " + allergenListDataItem.getInVoluntary()
						+ " - voluntary sources:" + voluntarySources.append(" - involuntary sources:").append(inVoluntarySources).toString();
				logger.debug(trace3);

				// allergen1 - voluntary: true - involuntary: false - voluntary
				// sources:Raw material 1, Raw material 2 - involuntary sources:
				if (allergenListDataItem.getAllergen().equals(allergen1)) {
					assertEquals("allergen1.getVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
					assertEquals("allergen1.getInVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
					assertEquals("allergen1.getVoluntarySources() contains Raw material 1, actual values: " + trace3, true,
							allergenListDataItem.getVoluntarySources().contains(rawMaterial1NodeRef));
					assertEquals("allergen1.getVoluntarySources() contains Raw material 2, actual values: " + trace3, true,
							allergenListDataItem.getVoluntarySources().contains(rawMaterial2NodeRef));
					assertEquals("allergen1.getInVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem.getInVoluntarySources().size());
				}
				// allergen2 - voluntary: false - involuntary: true - voluntary
				// sources: - involuntary sources:Raw material 2,
				if (allergenListDataItem.getAllergen().equals(allergen2)) {
					assertEquals("allergen2.getVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
					assertEquals("allergen2.getInVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
					assertEquals("allergen2.getInVoluntarySources() contains Raw material 2, actual values: " + trace3, true,
							allergenListDataItem.getInVoluntarySources().contains(rawMaterial2NodeRef));
					assertEquals("allergen2.getVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem.getVoluntarySources().size());
				}
				// allergen: allergen3 - voluntary: true - involuntary: true -
				// voluntary sources:Raw material 3, - involuntary sources:Raw
				// material 3,
				if (allergenListDataItem.getAllergen().equals(allergen3)) {
					assertEquals("allergen3.getVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
					assertEquals("allergen3.getInVoluntary() == true, actual values: " + trace3, true,
							Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
					assertEquals("allergen3.getVoluntarySources() contains Raw material 3, actual values: " + trace3, true,
							allergenListDataItem.getVoluntarySources().contains(rawMaterial3NodeRef));
					assertEquals("allergen3.getInVoluntarySources() contains Raw material 3, actual values: " + trace3, true,
							allergenListDataItem.getInVoluntarySources().contains(rawMaterial3NodeRef));
				}
				// allergen4 - voluntary: false - involuntary: false - voluntary
				// sources: - involuntary sources:
				if (allergenListDataItem.getAllergen().equals(allergen4)) {
					assertEquals("allergen4.getVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
					assertEquals("allergen4.getInVoluntary() == false, actual values: " + trace3, false,
							Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
					assertEquals("allergen4.getVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem.getVoluntarySources().size());
					assertEquals("allergen4.getInVoluntarySources() is empty, actual values: " + trace3, 0,
							allergenListDataItem.getInVoluntarySources().size());
				}
			}

			// verify IngList
			// 1 * RM1 , ingList : 1/3 ing1 ; bio1 ; geo1 // 2/3 ing2 ; bio1 ;
			// geo1|geo2 //
			// 2 * RM2 , ingList : 1/4 ing1 ; bio1 ; geo1 // 3/4 ing2 ; bio2 ;
			// geo1|geo2 //
			// 3 * RM3 , ingList : // // 1 ing3 ; bio1|bio2 ; geo2
			// 3 * RM4 [OMIT] , ingList : // // 1 ing3 ; bio1|bio2 ; geo2

			// (1 * (1/3 Ing1 + 2/3 Ing2) + 2 * (1/4 Ing1 + 3/4 Ing2) + 3 * Ing3
			// + 3 * (0 * Ing3 + 0,3 * Ing2 + 0 * ing4)) / (1+2+3+3)
			// Ing1 =(1/3+2*1/4)/6
			// Ing2 =(2/3+2*3/4)/6
			// Ing3 =(3)/6
			int checks = 0;
			assertNotNull("IngList is null", formulatedProduct.getIngList());
			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {

				StringBuilder geoOriginsText = new StringBuilder();
				for (NodeRef geoOrigin : ingListDataItem.getGeoOrigin()) {
					geoOriginsText.append(nodeService.getProperty(geoOrigin, BeCPGModel.PROP_CHARACT_NAME)).append(", ");
				}

				String bioOriginsText = "";
				for (NodeRef bioOrigin : ingListDataItem.getBioOrigin()) {
					bioOriginsText += nodeService.getProperty(bioOrigin, BeCPGModel.PROP_CHARACT_NAME) + ", ";
				}

				String trace4 = "ing: " + nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME) + " - qty: "
						+ ingListDataItem.getQtyPerc() + " - geo origins: "
						+ geoOriginsText.append(" - bio origins: ").append(bioOriginsText).append(" is gmo: ").append(ingListDataItem.getIsGMO())
								.append(" is ionized: ").append(ingListDataItem.getIsIonized()).toString();
				logger.debug(trace4);

				df = new DecimalFormat("0.000000");

				// ing: ing1 - qty: 13.88888888888889 - geo origins: geoOrigin1,
				// - bio origins: bioOrigin1, is gmo: true
				if (ingListDataItem.getIng().equals(ing1)) {
					assertEquals("ing1.getQtyPerc() == 13.88888888888889, actual values: " + trace4, df.format(13.88888888888889),
							df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace4, true,
							ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace4, false,
							ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace4, true,
							ingListDataItem.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace4, false,
							ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace4, true, Boolean.TRUE.equals(ingListDataItem.getIsGMO()));
					assertEquals("ing1.getIsIonized() is false, actual values: " + trace4, true, Boolean.TRUE.equals(ingListDataItem.getIsIonized()));
					checks++;
				}
				// ing2 - qty: 36.111111111111114 - geo origins: geoOrigin1,
				// geoOrigin2, - bio origins: bioOrigin1, bioOrigin2, is gmo:
				// false
				if (ingListDataItem.getIng().equals(ing2)) {
					assertEquals("ing2.getQtyPerc() == 36.111111111111114, actual values: " + trace4, df.format(36.111111111111114),
							df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace4, true,
							ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace4, true,
							ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace4, true,
							ingListDataItem.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace4, true,
							ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing2.getIsGMO() is true, actual values: " + trace4, true, Boolean.FALSE.equals(ingListDataItem.getIsGMO()));
					assertEquals("ing2.getIsIonized() is true, actual values: " + trace4, true,
							Boolean.FALSE.equals(ingListDataItem.getIsIonized()));
					checks++;
				}
				// ing3 - qty: 50 - geo origins: geoOrigin2, - bio origins:
				// bioOrigin1, bioOrigin2, is gmo: true
				if (ingListDataItem.getIng().equals(ing3)) {
					assertEquals("ing3.getQtyPerc() == 50, actual values: " + trace4, df.format(50), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace4, false,
							ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace4, true,
							ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace4, true,
							ingListDataItem.getBioOrigin().contains(bioOrigin1));
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace4, true,
							ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace4, true, Boolean.TRUE.equals(ingListDataItem.getIsGMO()));
					assertEquals("ing3.getIsIonized() is false, actual values: " + trace4, true, Boolean.TRUE.equals(ingListDataItem.getIsIonized()));
					checks++;
				}
			}
			assertEquals(3, checks);

			return null;

		});

	}

	/**
	 * Test formulate product, that has loss perc defined
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateSubFormula() throws Exception {

		logger.info("testCalculateSubFormula");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(10d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(0.80d).withUnit(ProductUnit.kg)
					.withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(0.30d).withUnit(ProductUnit.kg)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(20d).withDeclarationType(DeclarationType.Detail).withProduct(localSF3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(0.170d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(0.40d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.P)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			int checks = 0;

			for (CompoListDataItem compoListDataItem : formulatedProduct.getCompoListView().getCompoList()) {

				if (compoListDataItem.getProduct().equals(localSF1NodeRef)) {
					assertEquals("check SF1 qty", 2d, compoListDataItem.getQty());
					assertEquals("check SF1 qty sub formula", 2d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(localSF2NodeRef)) {
					assertEquals("check SF2 qty", 1d, compoListDataItem.getQty());
					assertEquals("check SF2 qty sub formula", 1d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial1NodeRef)) {
					assertEquals("check MP1 qty", 0.8d, compoListDataItem.getQty());
					assertEquals("check MP1 qty sub formula", 0.8d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial2NodeRef)) {
					assertEquals("check MP2 qty", 0.3d, compoListDataItem.getQty());
					assertEquals("check MP2 qty sub formula", 0.3d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(localSF3NodeRef)) {
					assertEquals("check SF3 qty", 1d, compoListDataItem.getQty());
					assertEquals("check SF3 qty sub formula", 1d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial3NodeRef)) {
					assertEquals("check MP3 qty", 0.17d, compoListDataItem.getQty());
					assertEquals("check MP3 qty sub formula", 0.17d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial4NodeRef)) {
					assertEquals("check MP4 qty", 0.4d, compoListDataItem.getQty());
					assertEquals("check MP4 qty sub formula", 0.4d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial5NodeRef)) {
					assertEquals("check MP5 qty", 0.1d, compoListDataItem.getQty());
					assertEquals("check MP5 qty sub formula", 1d, compoListDataItem.getQtySubFormula());
					checks++;
				}
			}

			assertEquals(8, checks);

			return null;

		});

	}

	@Test
	public void testPackagingCosts() {

		logger.info("testPackagingCosts");

		inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<PackagingListDataItem> packagingList = new ArrayList<>();
			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary)
					.withIsMaster(true).withProduct(packagingMaterial1NodeRef));
			packagingList.add(PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.m).withPkgLevel(PackagingLevel.Primary)
					.withIsMaster(true).withProduct(packagingMaterial2NodeRef));
			packagingList.add(PackagingListDataItem.build().withQty(8d).withUnit(ProductUnit.PP).withPkgLevel(PackagingLevel.Tertiary)
					.withIsMaster(true).withProduct(packagingMaterial3NodeRef));
			finishedProduct.getPackagingListView().setPackagingList(packagingList);
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, pkgCost1, false));
			costList.add(new CostListDataItem(null, null, null, null, pkgCost2, false));
			finishedProduct.setCostList(costList);
			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			logger.debug("unit of product formulated: " + finishedProduct.getUnit());
			int checks = 0;

			// costs
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem1 : formulatedProduct.getCostList()) {
				String trace1 = "cost: " + nodeService.getProperty(costListDataItem1.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem1.getValue() + " - unit: " + costListDataItem1.getUnit();
				logger.debug(trace1);
				if (costListDataItem1.getCost().equals(pkgCost1)) {
					assertEquals("cost1.getValue() == 3.0625, actual values: " + trace1, 3.0625d, costListDataItem1.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem1.getUnit());
					checks++;
				}
				if (costListDataItem1.getCost().equals(pkgCost2)) {
					assertEquals("cost1.getValue() == 4.125, actual values: " + trace1, 4.125d, costListDataItem1.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem1.getUnit());
					checks++;
				}
			}
			assertEquals(2, checks);

			// add packaging kit
			formulatedProduct.getPackagingList().add(PackagingListDataItem.build().withQty(25d).withUnit(ProductUnit.PP)
					.withPkgLevel(PackagingLevel.Secondary).withIsMaster(true).withProduct(packagingKit1NodeRef));
			alfrescoRepository.save(formulatedProduct);
			productService.formulate(finishedProductNodeRef);
			formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// (1×3+3×1+1÷(8*25*40))/2 = 3,0000625
			// (1×2+3×2+1÷(8*25*40)*2)/2 = 4.125

			checks = 0;
			// costs
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem2 : formulatedProduct.getCostList()) {
				String trace2 = "cost: " + nodeService.getProperty(costListDataItem2.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem2.getValue() + " - unit: " + costListDataItem2.getUnit();
				logger.debug(trace2);
				if (costListDataItem2.getCost().equals(pkgCost1)) {
					assertEquals(3.0000625d, costListDataItem2.getValue());
					assertEquals("€/kg", costListDataItem2.getUnit());
					checks++;
				}
				if (costListDataItem2.getCost().equals(pkgCost2)) {
					assertEquals(4.000125d, costListDataItem2.getValue());
					assertEquals("€/kg", costListDataItem2.getUnit());
					checks++;
				}
			}
			assertEquals(2, checks);

			return null;

		});

	}

	/**
	 * Test formulate product, that has cost and nut mini/maxi defined
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationWithCostAndNutMiniMaxi() throws Exception {

		logger.info("testFormulationWithCostAndNutMiniMaxi");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			finishedProduct.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNut(nut1));
			nutList.add(NutListDataItem.build().withNut(nut2));
			finishedProduct.setNutList(nutList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			DecimalFormat df = new DecimalFormat("0.####");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// costs
			int checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace1 = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - maxi: " + costListDataItem.getMaxi() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace1);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 4.0, actual values: " + trace1, 4.0d, costListDataItem.getValue());
					assertEquals("cost1.getMaxi() == 5.15, actual values: " + trace1, 5.15d, costListDataItem.getMaxi());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
					checks++;
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace1, 6.0d, costListDataItem.getValue());
					assertEquals("cost1.getMaxi() == 6.25, actual values: " + trace1, 6.25d, costListDataItem.getMaxi());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem.getUnit());
					checks++;
				}
			}
			assertEquals(2, checks);

			// nuts
			checks = 0;
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem1 : formulatedProduct.getNutList()) {
				String trace2 = "nut: " + nodeService.getProperty(nutListDataItem1.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem1.getValue() + " - mini: " + nutListDataItem1.getMini() + " - maxi: " + nutListDataItem1.getMaxi()
						+ " - unit: " + nutListDataItem1.getUnit();
				logger.debug(trace2);
				if (nutListDataItem1.getNut().equals(nut1)) {
					assertEquals("nut1.getValue() == 3, actual values: " + trace2, 3d, nutListDataItem1.getValue());
					assertEquals("nut1.getMini() == 2.7, actual values: " + trace2, 2.7d, nutListDataItem1.getMini());
					assertEquals("nut1.getMaxi() == 3.65, actual values: " + trace2, df.format(3.65d), df.format(nutListDataItem1.getMaxi()));
					assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace2, "kJ/100g", nutListDataItem1.getUnit());
					assertEquals("must be group1", GROUP1, nutListDataItem1.getGroup());
					checks++;
				}
				if (nutListDataItem1.getNut().equals(nut2)) {
					assertEquals("nut2.getValue() == 6, actual values: " + trace2, 6d, nutListDataItem1.getValue());
					assertEquals("nut1.getMini() == 4.55, actual values: " + trace2, 4.55d, nutListDataItem1.getMini());
					assertEquals("nut1.getMaxi() == 6.2, actual values: " + trace2, 6.2d, nutListDataItem1.getMaxi());
					assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace2, "kcal/100g", nutListDataItem1.getUnit());
					assertEquals("must be group2", GROUP2, nutListDataItem1.getGroup());
					checks++;
				}
			}
			assertEquals(2, checks);

			/**
			 * Check mini and maxi are null if no rawMaterial has any mini or
			 * maxi
			 */
			formulatedProduct.getCompoListView().getCompoList().clear();
			formulatedProduct.getCompoListView().getCompoList().add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(3d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			alfrescoRepository.save(formulatedProduct);
			productService.formulate(finishedProductNodeRef);
			formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// nuts
			checks = 0;
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem2 : formulatedProduct.getNutList()) {
				String trace3 = "nut: " + nodeService.getProperty(nutListDataItem2.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem2.getValue() + " - mini: " + nutListDataItem2.getMini() + " - maxi: " + nutListDataItem2.getMaxi()
						+ " - unit: " + nutListDataItem2.getUnit();
				logger.debug(trace3);
				if (nutListDataItem2.getNut().equals(nut1)) {
					assertNull(nutListDataItem2.getMini());
					assertNull(nutListDataItem2.getMaxi());
					checks++;
				}
				if (nutListDataItem2.getNut().equals(nut2)) {
					assertNull(nutListDataItem2.getMini());
					assertNull(nutListDataItem2.getMaxi());
					checks++;
				}
			}
			assertEquals(2, checks);

			return null;

		});

	}

	/**
	 * Test formulate product, that the yield field is calculated
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateYieldField() throws Exception {

		logger.info("testCalculateYieldField");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(10d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(0.8d).withUnit(ProductUnit.kg)
					.withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(0.3d).withUnit(ProductUnit.kg)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(20d).withDeclarationType(DeclarationType.Detail).withProduct(localSF3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(0.17d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			CompoListDataItem temp = CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(0.40d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef);
			temp.setYieldPerc(200d);
			compoList.add(temp);
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.P)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			int checks = 0;
			DecimalFormat df = new DecimalFormat("0.00");

			for (CompoListDataItem c : formulatedProduct.getCompoListView().getCompoList()) {

				logger.debug("Yield: " + c.getYieldPerc());

				if (c.getProduct().equals(localSF1NodeRef)) {
					double result1 = (100d * 2d) / (1d + 2d);
					logger.debug("df.format(result): " + df.format(result1));
					assertEquals("verify yield", df.format(result1), df.format(c.getYieldPerc()));
					checks++;
				} else if (c.getProduct().equals(localSF2NodeRef)) {
					double result2 = (100d * 1d) / 1.1d;
					assertEquals("verify yield", df.format(result2), df.format(c.getYieldPerc()));
					checks++;
				} else if (c.getProduct().equals(localSF3NodeRef)) {
					double result3 = (100d * 2d) / (0.170d + 0.40d + 0.1d);
					assertEquals("verify yield", df.format(result3), df.format(c.getYieldPerc()));
					checks++;
				} else if (c.getProduct().equals(rawMaterial4NodeRef)) {
					double result4 = (100d * 0.40d) / 200d;
					assertEquals("verify qty", df.format(result4), df.format(c.getQty()));
					checks++;
				}
			}

			assertEquals("verify checks", 4, checks);
			return null;

		});

	}

	/**
	 * Test formulate product, when there is a manual listItem
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testManualListItem() throws Exception {

		logger.info("testManualListItem");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			CompoListDataItem parent = CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef);
			compoList.add(parent);
			compoList.add(CompoListDataItem.build().withParent(parent).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(parent).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			parent = CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef);
			compoList.add(parent);
			compoList.add(CompoListDataItem.build().withParent(parent).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(parent).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			finishedProduct.setCostList(costList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// costs
			int checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem1 : formulatedProduct.getCostList()) {
				String trace1 = "cost: " + nodeService.getProperty(costListDataItem1.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem1.getValue() + " - unit: " + costListDataItem1.getUnit();
				logger.debug(trace1);
				if (costListDataItem1.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 4.0, actual values: " + trace1, 4.0d, costListDataItem1.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem1.getUnit());
					checks++;
				}
				if (costListDataItem1.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace1, 6.0d, costListDataItem1.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace1, "€/kg", costListDataItem1.getUnit());
					checks++;
				}
			}
			assertEquals(2, checks);

			// manual modification
			for (CostListDataItem costListDataItem2 : formulatedProduct.getCostList()) {
				String trace2 = "cost: " + nodeService.getProperty(costListDataItem2.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem2.getValue() + " - unit: " + costListDataItem2.getUnit();
				logger.debug(trace2);
				if (costListDataItem2.getCost().equals(cost1)) {

					nodeService.setProperty(costListDataItem2.getNodeRef(), PLMModel.PROP_COSTLIST_VALUE, 5.0d);
					nodeService.setProperty(costListDataItem2.getNodeRef(), BeCPGModel.PROP_IS_MANUAL_LISTITEM, true);
				}
			}

			productService.formulate(finishedProductNodeRef);

			formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// check costs
			checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem3 : formulatedProduct.getCostList()) {
				String trace3 = "cost: " + nodeService.getProperty(costListDataItem3.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem3.getValue() + " - unit: " + costListDataItem3.getUnit();
				logger.debug(trace3);
				if (costListDataItem3.getCost().equals(cost1)) {
					assertEquals("cost1.getValue() == 5.0, actual values: " + trace3, 5.0d, costListDataItem3.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace3, "€/kg", costListDataItem3.getUnit());
					checks++;
				}
				if (costListDataItem3.getCost().equals(cost2)) {
					assertEquals("cost1.getValue() == 6.0, actual values: " + trace3, 6.0d, costListDataItem3.getValue());
					assertEquals("cost1.getUnit() == €/kg, actual values: " + trace3, "€/kg", costListDataItem3.getUnit());
					checks++;
				}
			}

			assertEquals(2, checks);

			return null;

		});

	}

	/**
	 * Test formulate product, when there is process list
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testProcess() throws Exception {

		logger.info("testProcess");

		inWriteTx(() -> {

			/*-- Create process steps, resources --*/
			logger.debug("/*-- Create process steps, resources --*/");
			Map<QName, Serializable> properties = new HashMap<>();
			// Costs
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "costTransfo");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef costTransfoNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "costMOTransfo");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef costMOTransfoNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "costMOMaintenance");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef costMOMaintenanceNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "costEmb");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef costEmbNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();

			// Steps
			logger.debug("Steps");
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Découpe");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef decoupeNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Hachage");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef hachageNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Cuisson");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef cuissonNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Mélange");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef melangeNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Etape Ligne");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef ligneStepNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Etape emb");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			NodeRef embStepNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();

			// resources
			logger.debug("Resources");
			ResourceProductData boucherResourceData = new ResourceProductData();
			boucherResourceData.setName("Boucher");
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 8d, "€/h", null, costMOTransfoNodeRef, false));
			boucherResourceData.setCostList(costList);
			NodeRef boucherResourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), boucherResourceData).getNodeRef();

			// ResourceProductData operateurResourceData = new
			// ResourceProductData();
			// operateurResourceData.setName("Operateur");
			// costList = new ArrayList<CostListDataItem>();
			// costList.add(new CostListDataItem(null, 15d, "€/h", null,
			// costMOTransfoNodeRef, false));
			// operateurResourceData.setCostList(costList);
			// NodeRef operateurResourceNodeRef =
			// alfrescoRepository.create(getTestFolderNodeRef(),
			// operateurResourceData).getNodeRef();

			ResourceProductData hachoirResourceData = new ResourceProductData();
			hachoirResourceData.setName("Hachoir");
			costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 10d, "€/h", null, costTransfoNodeRef, false));
			hachoirResourceData.setCostList(costList);
			NodeRef hachoirResourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), hachoirResourceData).getNodeRef();

			ResourceProductData cuiseurResourceData = new ResourceProductData();
			cuiseurResourceData.setName("Cuiseur");
			costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 30d, "€/h", null, costTransfoNodeRef, false));
			cuiseurResourceData.setCostList(costList);
			NodeRef cuiseurResourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), cuiseurResourceData).getNodeRef();

			ResourceProductData malaxeurResourceData = new ResourceProductData();
			malaxeurResourceData.setName("Malaxeur");
			costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 40d, "€/h", null, costTransfoNodeRef, false));
			malaxeurResourceData.setCostList(costList);
			NodeRef malaxeurResourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), malaxeurResourceData).getNodeRef();

			ResourceProductData ligneResourceData = new ResourceProductData();
			ligneResourceData.setName("Ligne");
			costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 30d, "€/h", null, costTransfoNodeRef, false));
			costList.add(new CostListDataItem(null, 15d, "€/h", null, costMOTransfoNodeRef, false));
			costList.add(new CostListDataItem(null, 5d, "€/h", null, costMOMaintenanceNodeRef, false));
			ligneResourceData.setCostList(costList);
			NodeRef ligneResourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), ligneResourceData).getNodeRef();

			ResourceProductData emballageResourceData = new ResourceProductData();
			emballageResourceData.setName("Emballage");
			costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 40d, "€/h", null, costEmbNodeRef, false));
			emballageResourceData.setCostList(costList);
			NodeRef emballageResourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), emballageResourceData).getNodeRef();

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setDensity(1d);
			List<ProcessListDataItem> processList = new ArrayList<>();
			// decoupe
			processList.add(new ProcessListDataItem(null, 0.4d, 50d, 200d, ProductUnit.kg, null, null, decoupeNodeRef, null, boucherResourceNodeRef));
			// hachage
			processList.add(new ProcessListDataItem(null, 0.4d, 1d, 200d, ProductUnit.kg, null, null, hachageNodeRef, null, hachoirResourceNodeRef));
			// cuisson
			processList.add(new ProcessListDataItem(null, 0.4d, 1d, 200d, ProductUnit.kg, null, null, cuissonNodeRef, null, cuiseurResourceNodeRef));
			// mélange
			processList
					.add(new ProcessListDataItem(null, 0.24d, 1d, 600d, ProductUnit.kg, null, null, melangeNodeRef, null, malaxeurResourceNodeRef));
			// ligne
			processList.add(new ProcessListDataItem(null, 1d, 1d, 500d, ProductUnit.kg, null, null, ligneStepNodeRef, null, ligneResourceNodeRef));
			// emballage
			processList.add(new ProcessListDataItem(null, 1d, 1d, 100d, ProductUnit.Box, null, null, embStepNodeRef, null, emballageResourceNodeRef));
			finishedProduct.getProcessListView().setProcessList(processList);

			costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, costTransfoNodeRef, null));
			costList.add(new CostListDataItem(null, null, null, null, costMOTransfoNodeRef, null));
			costList.add(new CostListDataItem(null, null, null, null, costMOMaintenanceNodeRef, null));
			costList.add(new CostListDataItem(null, null, null, null, costEmbNodeRef, null));
			finishedProduct.setCostList(costList);

			List<PackagingListDataItem> packList = new ArrayList<>();
			packList.add(PackagingListDataItem.build().withQty(25d).withUnit(ProductUnit.PP).withPkgLevel(PackagingLevel.Secondary).withIsMaster(true)
					.withProduct(packagingKit1NodeRef));
			finishedProduct.getPackagingListView().setPackagingList(packList);

			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// costs
			logger.debug("/*-- Verify costs --*/");
			DecimalFormat df = new DecimalFormat("0.00");
			int checks = 0;
			assertNotNull("CostList is null", formulatedProduct.getCostList());
			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
				logger.debug(trace);
				// Transfo
				if (costListDataItem.getCost().equals(costTransfoNodeRef)) {
					assertEquals(df.format(0.156d), df.format(costListDataItem.getValue()));
					assertEquals("€/kg", costListDataItem.getUnit());
					checks++;
				}
				// MOTransfo
				if (costListDataItem.getCost().equals(costMOTransfoNodeRef)) {
					assertEquals(df.format(0.83d), df.format(costListDataItem.getValue()));
					assertEquals("€/kg", costListDataItem.getUnit());
					checks++;
				}
				// Maintenance
				if (costListDataItem.getCost().equals(costMOMaintenanceNodeRef)) {
					assertEquals(df.format(0.01d), df.format(costListDataItem.getValue()));
					assertEquals("€/kg", costListDataItem.getUnit());
					checks++;
				}
				// Emb
				if (costListDataItem.getCost().equals(costEmbNodeRef)) {
					// =40/(100*25*1)
					assertEquals(df.format(0.016d), df.format(costListDataItem.getValue()));
					assertEquals("€/kg", costListDataItem.getUnit());
					checks++;
				}
			}
			assertEquals(4, checks);

			logger.debug("/*-- Verify process --*/");
			checks = 0;
			for (ProcessListDataItem p : formulatedProduct.getProcessListView().getProcessList()) {
				logger.debug(p.toString());

				if (p.getStep() != null) {

					// decoupe
					if (p.getStep().equals(decoupeNodeRef)) {
						assertEquals(0.4d, p.getQty());
						assertEquals(50.0d, p.getQtyResource());
						assertEquals(200.0d, p.getRateResource());
						assertEquals(500.0d, p.getRateProduct());
						checks++;
					}

					// hachage
					if (p.getStep().equals(hachageNodeRef)) {
						assertEquals(0.4d, p.getQty());
						assertEquals(1.0d, p.getQtyResource());
						assertEquals(200.0d, p.getRateResource());
						assertEquals(500.0d, p.getRateProduct());
						checks++;
					}

					// cuisson
					if (p.getStep().equals(cuissonNodeRef)) {
						assertEquals(0.4d, p.getQty());
						assertEquals(1.0d, p.getQtyResource());
						assertEquals(200.0d, p.getRateResource());
						assertEquals(500.0d, p.getRateProduct());
						checks++;
					}

					// mélange
					if (p.getStep().equals(melangeNodeRef)) {
						assertEquals(0.24d, p.getQty());
						assertEquals(1.0d, p.getQtyResource());
						assertEquals(600.0d, p.getRateResource());
						assertEquals(2500.0d, p.getRateProduct());
						checks++;
					}

					// ligne
					if (p.getStep().equals(ligneStepNodeRef)) {
						assertEquals(1.0d, p.getQty());
						assertEquals(1.0d, p.getQtyResource());
						assertEquals(500.0d, p.getRateResource());
						assertEquals(500.0d, p.getRateProduct());
						checks++;
					}

					// emb
					if (p.getStep().equals(embStepNodeRef)) {
						assertEquals(1.0d, p.getQty());
						assertEquals(1.0d, p.getQtyResource());
						assertEquals(100.0d, p.getRateResource());
						assertEquals(2500.0d, p.getRateProduct());
						checks++;
					}
				}
			}

			assertEquals(6, checks);

			return null;

		});

	}

	/**
	 * Test formulate product, where qty are defined in percentage
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateCompoPercent() throws Exception {

		logger.info("testCalculateCompoPercent");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(100d).withUnit(ProductUnit.Perc).withLossPerc(10d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(45d).withUnit(ProductUnit.Perc)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(20d).withUnit(ProductUnit.Perc)
					.withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(25d).withUnit(ProductUnit.Perc)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(55d).withUnit(ProductUnit.Perc)
					.withLossPerc(20d).withDeclarationType(DeclarationType.Detail).withProduct(localSF3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(10d).withUnit(ProductUnit.Perc)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(25d).withUnit(ProductUnit.Perc)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(20d).withUnit(ProductUnit.Perc)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			int checks = 0;

			for (CompoListDataItem compoListDataItem : formulatedProduct.getCompoListView().getCompoList()) {

				if (compoListDataItem.getProduct().equals(localSF1NodeRef)) {
					assertEquals("check SF1 qty", 2d, compoListDataItem.getQty());
					assertEquals("check SF1 qty sub formula", 100d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(localSF2NodeRef)) {
					assertEquals("check SF2 qty", 0.9d, compoListDataItem.getQty());
					assertEquals("check SF2 qty sub formula", 45d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial1NodeRef)) {
					assertEquals("check MP1 qty", 0.4d, compoListDataItem.getQty());
					assertEquals("check MP1 qty sub formula", 20d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial2NodeRef)) {
					assertEquals("check MP2 qty", 0.5d, compoListDataItem.getQty());
					assertEquals("check MP2 qty sub formula", 25d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(localSF3NodeRef)) {
					assertEquals("check SF3 qty", 1.1d, compoListDataItem.getQty());
					assertEquals("check SF3 qty sub formula", 55d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial3NodeRef)) {
					assertEquals("check MP3 qty", 0.2d, compoListDataItem.getQty());
					assertEquals("check MP3 qty sub formula", 10d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial4NodeRef)) {
					assertEquals("check MP4 qty", 0.5d, compoListDataItem.getQty());
					assertEquals("check MP4 qty sub formula", 25d, compoListDataItem.getQtySubFormula());
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial5NodeRef)) {
					assertEquals("check MP5 qty", 0.4d, compoListDataItem.getQty());
					assertEquals("check MP5 qty sub formula", 20d, compoListDataItem.getQtySubFormula());
					checks++;
				}
			}

			assertEquals(8, checks);

			return null;

		});

	}

	/**
	 * Test formulate product for PhysicoChem
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testPhysicoChem() throws Exception {

		logger.info("testPhysicoChem");

		inWriteTx(() -> {

			/*-- Create finished product --*/
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setUnitPrice(12.4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(null).withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem4));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, "%", null, null, physicoChem5));
			finishedProduct.setPhysicoChemList(physicoChemList);

			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			DecimalFormat df = new DecimalFormat("0.00");

			// physicoChem
			int checks = 0;
			assertNotNull("physicoChem is null", formulatedProduct.getPhysicoChemList());
			for (PhysicoChemListDataItem pcListDataItem : formulatedProduct.getPhysicoChemList()) {
				String trace = "physicoChem: " + nodeService.getProperty(pcListDataItem.getPhysicoChem(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ pcListDataItem.getValue() + " - unit: " + pcListDataItem.getUnit() + " - mini " + pcListDataItem.getMini() + " - maxi "
						+ pcListDataItem.getMaxi();
				logger.info(trace);
				if (pcListDataItem.getPhysicoChem().equals(physicoChem3)) {
					assertEquals(3d, pcListDataItem.getValue());
					assertEquals(2.8d, pcListDataItem.getMini());
					assertEquals(df.format(3.65d), df.format(pcListDataItem.getMaxi()));
					checks++;
				}
				if (pcListDataItem.getPhysicoChem().equals(physicoChem4)) {
					assertEquals(6d, pcListDataItem.getValue());
					assertEquals(4.55d, pcListDataItem.getMini());
					assertEquals(6.2d, pcListDataItem.getMaxi());
					checks++;
				}
				/*
				 * #1787: check physico in % cannot be over 100% if (pcListDataItem.getPhysicoChem().equals(physicoChem5)) { assertEquals(100d, pcListDataItem.getValue()); assertEquals(100d,
				 * pcListDataItem.getMini()); assertEquals(100d, pcListDataItem.getMaxi()); checks++; }
				 */
			}
			assertEquals(2, checks);

			return null;

		});

	}

	@Test
	public void testOverrunAndVolume() {

		logger.info("testOverrunAndVolume");

		inWriteTx(() -> {

			/**
			 * Finished product 1
			 */
			logger.debug("/*************************************/");
			logger.debug("/*-- Create Finished product 1--*/");
			logger.debug("/*************************************/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product 1");
			finishedProduct.setLegalName("Legal Finished product 1");
			finishedProduct.setQty(7.6d);
			finishedProduct.setUnit(ProductUnit.L);
			finishedProduct.setNetWeight(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(100d).withUnit(ProductUnit.Perc).withLossPerc(10d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(45d).withUnit(ProductUnit.Perc)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(20d).withUnit(ProductUnit.Perc)
					.withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(1)).withQty(null).withQtyUsed(25d).withUnit(ProductUnit.Perc)
					.withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(55d).withUnit(ProductUnit.Perc)
					.withLossPerc(20d).withDeclarationType(DeclarationType.Detail).withProduct(localSF3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(10d).withUnit(ProductUnit.Perc)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(25d).withUnit(ProductUnit.Perc)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(4)).withQty(null).withQtyUsed(20d).withUnit(ProductUnit.Perc)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(null)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial15NodeRef));

			// add overrun
			compoList.get(6).setOverrunPerc(80d);
			compoList.get(7).setOverrunPerc(70d);

			finishedProduct.getCompoListView().setCompoList(compoList);
			NodeRef finishedProductNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct1 = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

			assertNotNull(formulatedProduct1.getCompoList());
			int checks = 0;
			for (CompoListDataItem compoListDataItem : formulatedProduct1.getCompoList()) {

				ProductData partProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());

				Double volume = compoListDataItem.getVolume();
				Double overrun = compoListDataItem.getOverrunPerc();
				Double density = partProduct.getDensity();
				logger.info("Product: " + nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
				logger.info("overrun: " + overrun);
				logger.info("volume: " + volume);
				logger.info("density: " + density);

				if (compoListDataItem.getProduct().equals(rawMaterial3NodeRef)) {
					assertEquals(0.2, volume);
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial4NodeRef)) {
					assertEquals((2 * 0.25 * 1.8) / 1.1, volume);
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial5NodeRef)) {
					assertEquals((2 * 0.2 * 1.7) / 0.1, volume);
					checks++;
				} else if (compoListDataItem.getProduct().equals(rawMaterial15NodeRef)) {
					assertEquals(0.050d, volume);
					checks++;
				}
			}

			assertEquals(4, checks);

			// TODO : yieldVolume -> not store as prop so cannot test it
			// logger.info("yieldVolume: " + finishedProduct.getYieldVolume());
			// assertEquals(100 * sum / 7.7d, finishedProduct.getYieldVolume());

			return null;

		});

	}

	@Test
	public void testNutrientLost() {

		logger.info("testNutrientLost");

		inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product 1");
			finishedProduct.setServingSize(300d);
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withValue(12d).withMini(11d).withMaxi(13d).withNut(nut1));
			

			
			nutList.get(0).setLossPerc(30d);
			nutList.add(NutListDataItem.build().withValue(12d).withMini(11d).withMaxi(13d).withNut(nut2));
			finishedProduct.setNutList(nutList);
			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			assertEquals(8.4d, formulatedProduct.getNutList().get(0).getValue());
			assertEquals(7.7d, formulatedProduct.getNutList().get(0).getMini());
			assertEquals(9.1d, formulatedProduct.getNutList().get(0).getMaxi());
			assertEquals(25.2d, formulatedProduct.getNutList().get(0).getValuePerServing());

			assertEquals(12d, formulatedProduct.getNutList().get(1).getValue());
			assertEquals(11d, formulatedProduct.getNutList().get(1).getMini());
			assertEquals(13d, formulatedProduct.getNutList().get(1).getMaxi());
			assertEquals(36d, formulatedProduct.getNutList().get(1).getValuePerServing());

			return null;

		});

	}

	@Test
	public void testMiniMaxi() {
		logger.info("testMiniMaxi");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setLegalName("Legal " + name);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setUnitPrice(22.4d);
			finishedProduct.setDensity(1d);
			finishedProduct.setServingSize(50d);// 50g
			finishedProduct.setProjectedQty(10000l);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(null).withQtyUsed(2d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);

			FinishedProductData formulatedFinishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			int checks = 0;
			for (IngListDataItem ing : formulatedFinishedProduct.getIngList()) {
				if (ing.getIng().equals(ing1) && (ing.getMini() != null) && (ing.getMaxi() != null)) {
					logger.info("FP ing1 mini: " + ing.getMini() + " maxi: " + ing.getMaxi());
					checks++;
					assertEquals(ing.getMini(), 18.333333333333332);
					assertEquals(ing.getMaxi(), 80.0);
				} else if (ing.getIng().equals(ing2) && (ing.getMini() != null) && (ing.getMaxi() != null)) {
					logger.info("FP ing2 mini: " + ing.getMini() + " maxi: " + ing.getMaxi());
					assertEquals(ing.getMini(), 22.666666666666668);
					assertEquals(ing.getMaxi(), 84.0);
					checks++;
				}
			}
			assertEquals(2, checks);
			return null;

		});

	}
}
