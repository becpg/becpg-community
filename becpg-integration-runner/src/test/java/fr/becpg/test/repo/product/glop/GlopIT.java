/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.glop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.glop.GlopService;
import fr.becpg.repo.glop.model.GlopConstraint;
import fr.becpg.repo.glop.model.GlopContext;
import fr.becpg.repo.glop.model.GlopData;
import fr.becpg.repo.glop.model.GlopTarget;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Integration tests for the Glop service.
 * 
 * @author pierrecolin
 */
public class GlopIT extends AbstractFinishedProductTest {

	private static final String GLOP_SERVER_URL_KEY = "beCPG.glop.serverUrl";

	protected static final Log logger = LogFactory.getLog(GlopIT.class);

	private static MockWebServer mockWebServer;

	private static String mockServerUrl;

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Autowired
	private GlopService glopService;

	@BeforeClass
	public static void beforeClass() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockServerUrl = "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		mockWebServer.shutdown();
	}

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
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue(GLOP_SERVER_URL_KEY, mockServerUrl);
			return null;
		});
	}

	@Override
	public void tearDown() throws Exception {
		inWriteTx(() -> {
			systemConfigurationService.resetConfValue(GLOP_SERVER_URL_KEY);
			return null;
		});
		super.tearDown();
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

		mockWebServer.enqueue(new MockResponse().setBody(
				String.format("{\"coefficients\":{\"%s\":0.05,\"%s\":0.0},\"status\":\"optimal\",\"value\":4.0}",
						rawMaterial6NodeRef, rawMaterial2NodeRef)));
		mockWebServer.enqueue(new MockResponse().setBody(String.format(
				"{\"coefficients\":{\"%s\":1.3333333333333333,\"%s\":0.7777777777777778},\"status\":\"optimal\",\"value\":4.777777777777778}",
				rawMaterial1NodeRef, rawMaterial2NodeRef)));

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
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, rawMaterial6NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial6NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

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
			GlopTarget target = null;
			List<GlopConstraint> characts = new ArrayList<>();
			for (IngListDataItem ing : ingList1) {
				if (ing.getIng().equals(ing1)) {
					target = new GlopTarget(ing, "max");
					logger.debug("Added " + target.toString());
				} else if (ing.getIng().equals(ing2)) {
					characts.add(new GlopConstraint(ing, 0d, 1d));
				}
			}
			assertNotNull("target is null", target);
			GlopData result1 = glopService.optimize(formulatedProduct1, new GlopContext(target, characts));

			assertEpsilon(4, result1.getDouble("value"), 1e-6);

			List<CompoListDataItem> compoList1 = formulatedProduct1.getCompoList();
			assertNotNull("Component list is null", compoList1);
			assertEquals(2, compoList1.size());

			int checks = 0;

			for (CompoListDataItem compoItem : compoList1) {
				double value = result1.getComponentValue(compoItem.getProduct());
				if (rawMaterial6NodeRef.equals(compoItem.getProduct())) {
					assertEquals(compoItem.getProduct().toString() + "not found in composition list",
							rawMaterial6NodeRef, compoItem.getProduct());
					assertEpsilon(0.05d, value, 1e-6);
					checks++;
				} else if (rawMaterial2NodeRef.equals(compoItem.getProduct())) {
					assertEquals(compoItem.getProduct().toString() + "not found in composition list",
							rawMaterial2NodeRef, compoItem.getProduct());
					assertEpsilon(0d, value, 1e-6);
					checks++;
				}
			}

			assertEquals(2, checks);

			return null;

		});

		NodeRef finishedProductNodeRef2 = inWriteTx(() -> {

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
			/*
			 * compoList2.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg,
			 * 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			compoList2.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			/*
			 * compoList2.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg,
			 * 0d, DeclarationType.Declare, rawMaterial2NodeRef));
			 */
			compoList2.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
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

		});

		inWriteTx(() -> {

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
			GlopTarget target = null;
			List<GlopConstraint> characts2 = new ArrayList<>();
			for (CostListDataItem cost : costList2) {
				if (cost.getCost().equals(cost1)) {
					target = new GlopTarget(cost, "min");
				}
			}
			for (NutListDataItem nut : nutList2) {
				if (nut.getNut().equals(nut3)) {
					characts2.add(new GlopConstraint(nut, 10d, Double.POSITIVE_INFINITY));
				} else if (nut.getNut().equals(nut4)) {
					characts2.add(new GlopConstraint(nut, 4d, 4d));
				}
			}
			assertNotNull("target is null", target);
			assertEquals(2, characts2.size());
			GlopData result2 = glopService.optimize(formulatedProduct2, new GlopContext(target, characts2));
			logger.debug("Server returned " + result2.toString());

			assertEpsilon(4d + 7d / 9d, result2.getDouble("value"), 1e-6);

			List<CompoListDataItem> compoList2 = formulatedProduct2.getCompoList();
			assertNotNull("Component list is null", compoList2);
			assertEquals(2, compoList2.size());
			for (CompoListDataItem compoItem : compoList2) {
				double value = result2.getComponentValue(compoItem.getProduct());
				if (compoItem.getProduct().equals(rawMaterial1NodeRef)) {
					assertEpsilon(4d / 3d, value, 1e-6);
				} else if (compoItem.getProduct().equals(rawMaterial2NodeRef)) {
					assertEpsilon(7d / 9d, value, 1e-6);
				} else {
					fail(compoItem.getProduct().toString() + " not found in composition list");
				}
			}

			return null;

		});

	}

	/**
	 * Composition list tests for the Glop service
	 */
	@Test
	public void testCompoList() {

		mockWebServer.enqueue(new MockResponse().setBody(String
				.format("{\"coefficients\":{\"%s\":1.0},\"status\":\"optimal\",\"value\":1.0}", rawMaterial2NodeRef)));

		logger.info("testGlopService");

		NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/**
			 * Finished product
			 */
			logger.debug("/********************************/");
			logger.debug("/*-- Create Finished product --*/");
			logger.debug("/********************************/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product");
			finishedProduct.setLegalName("Legal Finished product");
			finishedProduct.setQty(1d);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 2d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			/*-- Verify IngList size --*/
			logger.debug("/*-- Verify IngList size --*/");
			List<CompoListDataItem> compoList = formulatedProduct.getCompoList();
			assertNotNull("CompoList is null", compoList);
			assertEquals(1, compoList.size());

			/*-- Make optimization problem --*/
			logger.debug("/*-- Make optimization problem --*/");
			GlopTarget target = null;
			List<GlopConstraint> characts = new ArrayList<>();
			CompoListDataItem compo = compoList.get(0);
			assertEquals(rawMaterial2NodeRef, compo.getProduct());
			target = new GlopTarget(compo, "max");
			logger.debug("Added " + target.toString());
			characts.add(new GlopConstraint(compo, 0d, 1d));
			logger.debug("Added " + characts.get(0));

			GlopData result = glopService.optimize(formulatedProduct, new GlopContext(target, characts));

			assertEpsilon(1, result.getDouble("value"), 1e-6);

			double value = result.getComponentValue(compo.getProduct());
			assertEpsilon(1d, value, 1e-6);

			return null;

		});
	}

	@Test
	public void testSpelFunctions() {

		mockWebServer.enqueue(new MockResponse().setBody(String.format(
				"{\"coefficients\":{\"%s\":0.7777777777777778,\"%s\":1.3333333333333333},\"status\":\"optimal\",\"value\":4.777777777777778}",
				rawMaterial2NodeRef, rawMaterial1NodeRef)));

		NodeRef finishedProductNodeRef = inWriteTx(() -> {

			/**
			 * Finished product
			 */
			logger.debug("/*******************************/");
			logger.debug("/*-- Create Finished product --*/");
			logger.debug("/*******************************/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product");
			finishedProduct.setLegalName("Legal Finished product");
			finishedProduct.setQty(2d);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, "€/kg", null, cost1, null));
			finishedProduct.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut3, null));
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut4, null));
			finishedProduct.setNutList(nutList);

			List<DynamicCharactListItem> dynamicCharactList = new ArrayList<>();
			dynamicCharactList.add(new DynamicCharactListItem("test1",
					"var glopData = @glop.optimize({target: {var: cost['" + cost1
							+ "'], task: \"min\"}, constraints: {{var: nut['" + nut3
							+ "'], min: 10, max: \"inf\"}, {var: nut['" + nut4
							+ "'], min: 4, max: 4},{var:\"recipeQtyUsed\", min:\"-inf\", max:\"inf\"}}}); #glopData.toString();"));
			finishedProduct.getCompoListView().setDynamicCharactList(dynamicCharactList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			List<DynamicCharactListItem> dynamicCharacts = formulatedProduct.getCompoListView().getDynamicCharactList();
			DynamicCharactListItem dynamicCharact = dynamicCharacts.get(0);
			JSONObject result = new JSONObject((String) dynamicCharact.getValue());
			assertEpsilon(4d + 7d / 9d, (double) result.getDouble("value"), 1e-6);
			JSONArray components = (JSONArray) result.get("components");
			logger.debug(components);

			int check = 0;

			for (int i = 0; i < components.length(); i++) {
				JSONObject component = components.getJSONObject(i);

				if ("Raw material 1".equals(component.getString("name"))) {
					assertEpsilon(4d / 3d, component.getDouble("value"), 1e-6);
					check++;
				} else if ("Raw material 2".equals(component.getString("name"))) {
					assertEpsilon(7d / 9d, component.getDouble("value"), 1e-6);
					check++;
				}
			}

			assertEquals(2, check);

			return null;
		});

	}

	@Test
	public void testToleranceApplication() {

		mockWebServer.enqueue(new MockResponse().setBody(""));
		mockWebServer.enqueue(new MockResponse().setBody(
				String.format("{\"coefficients\":{\"%s\":-0.0,\"%s\":2.0},\"status\":\"optimal\",\"value\":2.0}",
						rawMaterial1NodeRef, rawMaterial2NodeRef)));

		NodeRef fpNodeRef = inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP tolerance application");
			finishedProduct.setQty(2d);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d,
			 * DeclarationType.Declare, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, "€/kg", null, cost1, null));
			finishedProduct.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut3, null));
			finishedProduct.setNutList(nutList);

			List<DynamicCharactListItem> dynamicCharactList = new ArrayList<>();
			dynamicCharactList.add(new DynamicCharactListItem("test1",
					"var glopData = @glop.optimize({target: {var: cost['" + cost1
							+ "'], task: \"min\"}, constraints: {{var: nut['" + nut1
							+ "'], min: 2, max: 2, tol:50}, {var: nut['" + nut2
							+ "'], min: 3, max: 3, tol:50}, {var: nut['" + nut3
							+ "'], min: 8, max: 8, tol:50}}}); #glopData.toString();"));
			finishedProduct.getCompoListView().setDynamicCharactList(dynamicCharactList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		});

		inWriteTx(() -> {

			productService.formulate(fpNodeRef);

			ProductData formulatedProduct = alfrescoRepository.findOne(fpNodeRef);

			List<DynamicCharactListItem> dynamicCharacts = formulatedProduct.getCompoListView().getDynamicCharactList();
			DynamicCharactListItem dynamicCharact = dynamicCharacts.get(0);
			JSONObject result = new JSONObject((String) dynamicCharact.getValue());
			assertEpsilon(2d, (double) result.getDouble("value"), 1e-6);

			assertEquals("suboptimal", result.getString("status"));

			return null;
		});
	}

}
