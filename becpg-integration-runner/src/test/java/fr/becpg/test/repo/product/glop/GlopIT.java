/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.glop;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.glop.GlopConstraintSpecification;
import fr.becpg.repo.glop.GlopService;
import fr.becpg.repo.glop.GlopTargetSpecification;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Integration tests for the Glop service.
 * 
 * @author pierrecolin
 */
public class GlopIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(GlopIT.class);

	@Autowired
	private GlopService glopService;

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
	
	private static JSONObject exampleRequest() throws JSONException {
		JSONObject request = new JSONObject();
		
		// variables
		JSONArray variables = new JSONArray();
		variables.put("x");
		variables.put("y");
		request.put("variables", variables);
		
		// constraints
		JSONArray constraints = new JSONArray();
		
		JSONObject constraintA = new JSONObject();
		constraintA.put("lower", "-inf");
		constraintA.put("upper", 14);
		JSONObject coefA = new JSONObject();
		coefA.put("x", 1);
		coefA.put("y", 2);
		constraintA.put("coefficients", coefA);
		constraints.put(constraintA);
		
		JSONObject constraintB = new JSONObject();
		constraintB.put("lower", 0);
		constraintB.put("upper", "inf");
		JSONObject coefB = new JSONObject();
		coefB.put("x", 3);
		coefB.put("y", -1);
		constraintB.put("coefficients", coefB);
		constraints.put(constraintB);
		
		JSONObject constraintC = new JSONObject();
		constraintC.put("lower", "-inf");
		constraintC.put("upper", 2);
		JSONObject coefC = new JSONObject();
		coefC.put("x", 1);
		coefC.put("y", -1);
		constraintC.put("coefficients", coefC);
		constraints.put(constraintC);
		
		request.put("constraints", constraints);
		
		// objective
		JSONObject objective = new JSONObject();
		objective.put("task", "max");
		JSONObject coefObj = new JSONObject();
		coefObj.put("x", 3);
		coefObj.put("y", 4);
		objective.put("coefficients", coefObj);
		request.put("objective", objective);
		
		return request;
	}

	/**
	 * Makes a sample Glop request and tries to send it to the Glop server.
	 */
	@Test
	public void testSendRequest() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			JSONObject request = exampleRequest();
			logger.debug("Sending " + request.toString());
			JSONObject response = glopService.sendRequest(request);
			logger.debug(response.toString());
			return null;
		}, false, true);
	}
	
	private static void assertEpsilon(double expected, double actual, double epsilon) {
		double x = expected;
		double y = actual;
		
		// Make sure x has the smallest absolute value
		if (Math.abs(x) > Math.abs(y)) {
			double temp = x;
			x = y;
			y = temp;
		}
		
		if (x - epsilon > y || x + epsilon < y) {
			fail("Expected <" + expected + " +/- " + epsilon + ">, got <" + actual + ">");
		}
	}

	/**
	 * Base tests for the Glop service
	 */
	@Test
	public void testGlopService() {

		logger.info("testGlopService");
		
		NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			/**
			 * Finished product 1
			 */
			logger.debug("/*************************************/");
			logger.debug("/*-- Create Finished product 1--*/");
			logger.debug("/*************************************/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product 1");
			finishedProduct.setLegalName("Legal Finished product 1");
			finishedProduct.setQty(1d);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);
			
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial6NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
			
			
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			
			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);
			
			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

			/*-- Verify IngList size --*/
			logger.debug("/*-- Verify IngList size --*/");
			List<IngListDataItem> ingList1 = formulatedProduct1.getIngList();
			assertNotNull("IngList is null", ingList1);
			assertEquals(2, ingList1.size());
			
			/*-- Make optimization problem --*/
			logger.debug("/*-- Make optimization problem --*/");
			GlopTargetSpecification target = null;
			List<GlopConstraintSpecification> characts = new ArrayList<>();
			for (IngListDataItem ing: ingList1) {
				if (ing.getIng().equals(ing1)) {
					target = new GlopTargetSpecification(ing, "max");
					logger.debug("Added " + target.toString());
				} else if (ing.getIng().equals(ing2)) {
					characts.add(new GlopConstraintSpecification(ing, 0d, 1d));
				}
			}
			assertNotNull("target is null", target);
			JSONObject result1 = glopService.optimize(formulatedProduct1, characts, target);
			
			assertEpsilon(4, result1.getDouble("value"), 1e-6);
			
			JSONObject resultCoefs1 = result1.getJSONObject("coefficients");
			List<CompoListDataItem> compoList1 = formulatedProduct1.getCompoList();
			assertNotNull("Component list is null", compoList1);
			assertEquals(2, compoList1.size());
			for (CompoListDataItem compoItem: compoList1) {
				try {
					double value = resultCoefs1.getDouble(compoItem.getProduct().toString());
					assertEquals(compoItem.getProduct().toString() + "not found in composition list", rawMaterial6NodeRef, compoItem.getProduct());
					assertEpsilon(0.05d, value, 1e-6);
				} catch (JSONException e) {
					assertEquals(compoItem.getProduct().toString() + "not found in composition list", rawMaterial2NodeRef, compoItem.getProduct());
				}
			}
			
			return null;
			
		}, false, true);
		
		NodeRef finishedProductNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
			compoList2.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList2.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial2NodeRef));
			finishedProduct2.getCompoListView().setCompoList(compoList2);
			
			List<CostListDataItem> costList2 = new ArrayList<>();
			costList2.add(new CostListDataItem(null, null, "€/kg", null, cost1, null));
			finishedProduct2.setCostList(costList2);
			
			List<NutListDataItem> nutList2 = new ArrayList<>();
			nutList2.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
			nutList2.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
			nutList2.add(new NutListDataItem(null, null, null, null, null, null, nut3, null));
			nutList2.add(new NutListDataItem(null, null, null, null, null, null, nut4, null));
			finishedProduct2.setNutList(nutList2);
			
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct2).getNodeRef();
			
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef2);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct2 = alfrescoRepository.findOne(finishedProductNodeRef2);
			
			/*-- Verify CostList size --*/
			logger.debug("/*-- Verify CostList size --*/");
			List<CostListDataItem> costList2 = formulatedProduct2.getCostList();
			assertNotNull("CostList is null", costList2);
			assertEquals(1, costList2.size());
			
			/*-- Verify NutList size --*/
			logger.debug("/*-- Verify NutList size --*/");
			List<NutListDataItem> nutList2 = formulatedProduct2.getNutList();
			assertNotNull("NutList is null", nutList2);
			assertEquals(4, nutList2.size());

			/*-- Make optimization problem --*/
			logger.debug("/*-- Make optimization problem --*/");
			GlopTargetSpecification target = null;
			List<GlopConstraintSpecification> characts2 = new ArrayList<>();
			for (CostListDataItem cost: costList2) {
				if (cost.getCost().equals(cost1)) {
					target = new GlopTargetSpecification(cost, "min");
				}
			}
			for (NutListDataItem nut: nutList2) {
				if (nut.getNut().equals(nut3)) {
					characts2.add(new GlopConstraintSpecification(nut, 10d, Double.POSITIVE_INFINITY));
				} else if (nut.getNut().equals(nut4)) {
					characts2.add(new GlopConstraintSpecification(nut, 4d, 4d));
				}
			}
			assertNotNull("target is null", target);
			JSONObject result2 = glopService.optimize(formulatedProduct2, characts2, target); 
			logger.debug("Server returned " + result2.toString());
			
			assertEpsilon(4d + 7d/9d, result2.getDouble("value"), 1e-6);
			
			JSONObject resultCoefs2 = result2.getJSONObject("coefficients");
			List<CompoListDataItem> compoList2 = formulatedProduct2.getCompoList();
			assertNotNull("Component list is null", compoList2);
			assertEquals(2, compoList2.size());
			for (CompoListDataItem compoItem: compoList2) {
				double value = resultCoefs2.getDouble(compoItem.getProduct().toString());
				if (compoItem.getProduct().equals(rawMaterial1NodeRef)) {
					assertEpsilon(4d/3d, value, 1e-6);
				} else if (compoItem.getProduct().equals(rawMaterial2NodeRef)) {
					assertEpsilon(7d/9d, value, 1e-6);
				} else {
					fail(compoItem.getProduct().toString() + " not found in composition list");
				}
			}

			return null;

		}, false, true);

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

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setQty(2.5d);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 2d, ProductUnit.L, 0d, DeclarationType.Declare, rawMaterial6NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

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

		}, false, true);

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

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 2d, ProductUnit.kg, 10d, DeclarationType.Detail, localSF1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d, ProductUnit.kg, 10d, DeclarationType.Detail, localSF2NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(1), null, 0.80d, ProductUnit.kg, 5d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(1), null, 0.30d, ProductUnit.kg, 10d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 1d, ProductUnit.kg, 20d, DeclarationType.Detail, localSF3NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(4), null, 0.170d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(4), null, 0.40d, ProductUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(4), null, 1d, ProductUnit.P, 0d, DeclarationType.Declare, rawMaterial5NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
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

		}, false, true);

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

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 100d, ProductUnit.Perc, 10d, DeclarationType.Detail, localSF1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 45d, ProductUnit.Perc, 10d, DeclarationType.Detail, localSF2NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(1), null, 20d, ProductUnit.Perc, 5d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(1), null, 25d, ProductUnit.Perc, 10d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 55d, ProductUnit.Perc, 20d, DeclarationType.Detail, localSF3NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(4), null, 10d, ProductUnit.Perc, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(4), null, 25d, ProductUnit.Perc, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(4), null, 20d, ProductUnit.Perc, 0d, DeclarationType.Declare, rawMaterial5NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
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

		}, false, true);

	}

	@Test
	public void testOverrunAndVolume() throws Exception {

		logger.info("testOverrunAndVolume");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
			compoList.add(new CompoListDataItem(null, null, null, 100d, ProductUnit.Perc, 10d, DeclarationType.Detail, localSF1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 45d, ProductUnit.Perc, 10d, DeclarationType.Detail, localSF2NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(1), null, 20d, ProductUnit.Perc, 5d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(1), null, 25d, ProductUnit.Perc, 10d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 55d, ProductUnit.Perc, 20d, DeclarationType.Detail, localSF3NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(4), null, 10d, ProductUnit.Perc, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(4), null, 25d, ProductUnit.Perc, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
			compoList.add(
					new CompoListDataItem(null, compoList.get(4), null, 20d, ProductUnit.Perc, 0d, DeclarationType.Declare, rawMaterial5NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.P, null, DeclarationType.Declare, rawMaterial15NodeRef));

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
			ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

			assertNotNull(formulatedProduct1.getCompoList());
			int checks = 0;
			for (CompoListDataItem compoListDataItem : formulatedProduct1.getCompoList()) {

				ProductData partProduct = alfrescoRepository.findOne(compoListDataItem.getProduct());

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

		}, false, true);

	}

	@Test
	public void testNutrientLost() throws Exception {

		logger.info("testNutrientLost");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product 1");
			finishedProduct.setServingSize(300d);
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(new NutListDataItem(null, 12d, null, 11d, 13d, null, nut1, false));
			nutList.get(0).setLossPerc(30d);
			nutList.add(new NutListDataItem(null, 12d, null, 11d, 13d, null, nut2, false));
			finishedProduct.setNutList(nutList);
			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
			assertEquals(8.4d, formulatedProduct.getNutList().get(0).getValue());
			assertEquals(7.7d, formulatedProduct.getNutList().get(0).getMini());
			assertEquals(9.1d, formulatedProduct.getNutList().get(0).getMaxi());
			assertEquals(25.2d, formulatedProduct.getNutList().get(0).getValuePerServing());

			assertEquals(12d, formulatedProduct.getNutList().get(1).getValue());
			assertEquals(11d, formulatedProduct.getNutList().get(1).getMini());
			assertEquals(13d, formulatedProduct.getNutList().get(1).getMaxi());
			assertEquals(36d, formulatedProduct.getNutList().get(1).getValuePerServing());

			return null;

		}, false, true);

	}
}
