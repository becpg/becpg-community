/*
 *
 */
package fr.becpg.test.repo.web.scripts.product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.Arrays;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductWUsedWebScriptTest.
 *
 * @author querephi
 */
public class ProductWUsedWebScriptIT extends fr.becpg.test.PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(ProductWUsedWebScriptIT.class);

	private NodeRef rawMaterialNodeRef = null;
	private NodeRef finishedProductNodeRef = null;

	/**
	 * Testget product wused.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testgetProductWused() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			logger.debug("/*-- Create raw material --*/");
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material");
			rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
			LocalSemiFinishedProductData lSF = new LocalSemiFinishedProductData();
			lSF.setName("Local semi finished");
			NodeRef lSFNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF).getNodeRef();

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product");
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(lSFNodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			logger.debug("local semi finished: " + lSFNodeRef);
			logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);

			return null;

		}, false, true);

		// Call webscript on raw material
		String url = "/becpg/entity/datalists/data/node?entityNodeRef=" + rawMaterialNodeRef.toString()
				+ "&itemType=bcpg%3AcompoList&dataListName=WUsed";
		String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"all\",\"filterData\":\"\"}}";
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
		response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content : " + response.getContentAsString());

	}

	/**
	 * Test get product wused with model filter.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testgetProductWusedWithModelFilter() throws Exception {

		final List<NodeRef> tplNodeRefContainer = new ArrayList<>();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			logger.debug("/*-- Create raw material 2 --*/");
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material 2");
			rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
			LocalSemiFinishedProductData lSF = new LocalSemiFinishedProductData();
			lSF.setName("Local semi finished 2");
			NodeRef lSFNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF).getNodeRef();

			/*-- Create finished product model --*/
			FinishedProductData finishedProductTpl = new FinishedProductData();
			finishedProductTpl.setName("My Product Model");
			NodeRef tplNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProductTpl).getNodeRef();
			tplNodeRefContainer.add(tplNodeRef);

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product 2 --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product 2");
			finishedProduct.setEntityTpl(finishedProductTpl);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(lSFNodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			return null;

		}, false, true);

		NodeRef tplNodeRef = tplNodeRefContainer.get(0);

		// Call webscript on raw material with model filter
		String url = "/becpg/entity/datalists/data/node?entityNodeRef=" + rawMaterialNodeRef.toString()
				+ "&itemType=bcpg%3AcompoList&dataListName=WUsed";
		String filterDataStr = "{\\\"nested_bcpg_compoListProduct_assoc_bcpg_entityTplRef_added\\\":\\\"" + tplNodeRef.toString() + "\\\"}";
		String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"filterform\",\"filterData\":\"" + filterDataStr + "\"}}";
		logger.debug("url : " + url);
		logger.debug("data : " + data);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
		org.junit.Assert.assertTrue("Should return matches using the model filter", response.getContentAsString().contains("Finished Product 2"));

		// Try with a non-matching model filter
		String nonMatchingFilterDataStr = "{\\\"nested_bcpg_compoListProduct_assoc_bcpg_entityTplRef_added\\\":\\\"workspace://SpacesStore/non-existent-node\\\"}";
		String dataNonMatching = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"filterform\",\"filterData\":\"" + nonMatchingFilterDataStr + "\"}}";
		Response responseNonMatching = TestWebscriptExecuters.sendRequest(new PostRequest(url, dataNonMatching, "application/json"), 200, "admin");
		org.junit.Assert.assertFalse("Should not return matches with non-matching model filter", responseNonMatching.getContentAsString().contains("Finished Product 2"));

	}

	/**
	 * Reproduces #34682 (test failed): the WUsed filter form for product attributes
	 * (erpCode, effectivity dates) must keep only the matching parent products.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testgetProductWusedWithPropertyFilters() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Shared raw material");
			rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();

			// Two finished products using the same raw material, with distinct
			// erpCodes (one a substring of the other) and effectivity dates.
			finishedProductNodeRef = createUsingProduct("WUsed Match Product", "TESTXYZ", buildDate(2030, Calendar.JUNE, 15));
			createUsingProduct("WUsed Other Product", "TEST", buildDate(2020, Calendar.JUNE, 15));

			return null;

		}, false, true);

		String url = "/becpg/entity/datalists/data/node?entityNodeRef=" + rawMaterialNodeRef.toString()
				+ "&itemType=bcpg%3AcompoList&dataListName=WUsed";

		// --- erpCode filter (nested) : "TESTXYZ" must not match the product whose code is "TEST" ---
		String erpContent = sendFilter(url, "{\\\"nested_bcpg_compoListProduct_prop_bcpg_erpCode\\\":\\\"TESTXYZ\\\"}");
		logger.info("erpCode filter content : " + erpContent);
		org.junit.Assert.assertTrue("erpCode filter should keep the matching product", erpContent.contains("WUsed Match Product"));
		org.junit.Assert.assertFalse("erpCode filter must not keep a product whose code is only a substring of the search term",
				erpContent.contains("WUsed Other Product"));

		// --- startEffectivity range filter (root, as sent by the WUsed filter form) :
		// 2029..2031 keeps only the product effective in 2030 ---
		String dateContent = sendFilter(url,
				"{\\\"prop_bcpg_startEffectivity-date-range\\\":\\\"2029-01-01T00:00:00.000Z|2031-01-01T00:00:00.000Z\\\"}");
		logger.info("startEffectivity range filter content : " + dateContent);
		org.junit.Assert.assertTrue("startEffectivity range filter should keep the product effective within the range",
				dateContent.contains("WUsed Match Product"));
		org.junit.Assert.assertFalse("startEffectivity range filter must drop the product effective outside the range",
				dateContent.contains("WUsed Other Product"));

		// A range that excludes both products must return no row.
		String emptyDateContent = sendFilter(url,
				"{\\\"prop_bcpg_startEffectivity-date-range\\\":\\\"2040-01-01T00:00:00.000Z|2041-01-01T00:00:00.000Z\\\"}");
		org.junit.Assert.assertFalse("out-of-range filter must drop the 2030 product", emptyDateContent.contains("WUsed Match Product"));
		org.junit.Assert.assertFalse("out-of-range filter must drop the 2020 product", emptyDateContent.contains("WUsed Other Product"));

		// --- single-day startEffectivity filter (from = to = the exact effectivity day) : the product
		// effective that day must be kept. Effectivity is a d:datetime stored with a time and a timezone
		// offset; comparing the full ISO string against the day bounds used to drop every row (#34682). ---
		String singleDayContent = sendFilter(url,
				"{\\\"prop_bcpg_startEffectivity-date-range\\\":\\\"2030-06-15T00:00:00.000Z|2030-06-15T00:00:00.000Z\\\"}");
		org.junit.Assert.assertTrue("single-day startEffectivity filter must keep the product effective that day",
				singleDayContent.contains("WUsed Match Product"));
		org.junit.Assert.assertFalse("single-day startEffectivity filter must drop a product effective another day",
				singleDayContent.contains("WUsed Other Product"));

		// --- single-date startEffectivity filter, exactly as emitted by the WUsed filter form's
		// single date picker (no -date-range suffix, a single YYYY-MM-DD value) : it must match on
		// the date part of the d:datetime, like the day range above. This is the flow Pamela tested
		// (#34682) that used to return an empty list because the value fell into an exact EQUALS. ---
		String singleDateContent = sendFilter(url, "{\\\"prop_bcpg_startEffectivity\\\":\\\"2030-06-15\\\"}");
		org.junit.Assert.assertTrue("single-date startEffectivity filter must keep the product effective that day",
				singleDateContent.contains("WUsed Match Product"));
		org.junit.Assert.assertFalse("single-date startEffectivity filter must drop a product effective another day",
				singleDateContent.contains("WUsed Other Product"));

		// The same key with a non-matching day must drop the row.
		String otherDateContent = sendFilter(url, "{\\\"prop_bcpg_startEffectivity\\\":\\\"2020-06-15\\\"}");
		org.junit.Assert.assertFalse("single-date startEffectivity filter must drop the 2030 product on a 2020 day",
				otherDateContent.contains("WUsed Match Product"));
		org.junit.Assert.assertTrue("single-date startEffectivity filter must keep the 2020 product on its day",
				otherDateContent.contains("WUsed Other Product"));

	}

	/**
	 * Reproduces #28166: the where-used list must honour the column sort sent by the data grid.
	 * Builds an entity-level where-used (bcpg:suppliers) and checks that the rows come back ordered
	 * by name both ascending and descending.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testgetProductWusedSortedByColumn() throws Exception {

		final List<NodeRef> supplierContainer = new ArrayList<>();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			SupplierData supplier = new SupplierData();
			supplier.setName("WUsed Sort Supplier");
			NodeRef supplierNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), supplier).getNodeRef();
			supplierContainer.add(supplierNodeRef);

			// Three raw materials sharing the same supplier, created in a non-alphabetical order.
			createSuppliedRawMaterial("WUsed Sort Zebra", supplierNodeRef);
			createSuppliedRawMaterial("WUsed Sort Alpha", supplierNodeRef);
			createSuppliedRawMaterial("WUsed Sort Mango", supplierNodeRef);

			return null;

		}, false, true);

		NodeRef supplierNodeRef = supplierContainer.get(0);

		String url = "/becpg/entity/datalists/data/node?entityNodeRef=" + supplierNodeRef.toString()
				+ "&itemType=bcpg%3ArawMaterial&dataListName=WUsed-bcpg_suppliers";
		String data = "{\"fields\":[\"cm_name\"],\"filter\":{\"filterId\":\"all\",\"filterData\":\"\"}}";

		// Ascending sort by name : Alpha, Mango, Zebra
		Response ascResponse = TestWebscriptExecuters.sendRequest(new PostRequest(url + "&sort=cm%3Aname%7Ctrue", data, "application/json"), 200,
				"admin");
		assertSortedOrder(ascResponse.getContentAsString(), Arrays.asList("WUsed Sort Alpha", "WUsed Sort Mango", "WUsed Sort Zebra"));

		// Descending sort by name : Zebra, Mango, Alpha
		Response descResponse = TestWebscriptExecuters.sendRequest(new PostRequest(url + "&sort=cm%3Aname%7Cfalse", data, "application/json"), 200,
				"admin");
		assertSortedOrder(descResponse.getContentAsString(), Arrays.asList("WUsed Sort Zebra", "WUsed Sort Mango", "WUsed Sort Alpha"));

	}

	private void assertSortedOrder(String content, List<String> expectedOrder) {
		int previousIndex = -1;
		for (String name : expectedOrder) {
			int index = content.indexOf(name);
			org.junit.Assert.assertTrue("Expected '" + name + "' to be present in the where-used response", index >= 0);
			org.junit.Assert.assertTrue("Where-used rows are not sorted as expected (" + expectedOrder + "), content: " + content,
					index > previousIndex);
			previousIndex = index;
		}
	}

	private NodeRef createSuppliedRawMaterial(String name, NodeRef supplierNodeRef) {
		RawMaterialData rawMaterial = new RawMaterialData();
		rawMaterial.setName(name);
		rawMaterial.setSuppliers(new ArrayList<>(List.of(supplierNodeRef)));
		return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
	}

	private NodeRef createUsingProduct(String name, String erpCode, Date startEffectivity) {
		LocalSemiFinishedProductData lSF = new LocalSemiFinishedProductData();
		lSF.setName("SF for " + name);
		NodeRef lSFNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF).getNodeRef();

		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName(name);
		finishedProduct.setErpCode(erpCode);
		finishedProduct.setStartEffectivity(startEffectivity);
		List<CompoListDataItem> compoList = new ArrayList<>();
		compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d)
				.withDeclarationType(DeclarationType.Omit).withProduct(lSFNodeRef));
		compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d)
				.withDeclarationType(DeclarationType.Omit).withProduct(rawMaterialNodeRef));
		finishedProduct.getCompoListView().setCompoList(compoList);
		return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	}

	private String sendFilter(String url, String filterDataStr) throws Exception {
		String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"filterform\",\"filterData\":\""
				+ filterDataStr + "\"}}";
		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		return response.getContentAsString();
	}

	private static Date buildDate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month, day, 12, 0, 0);
		return calendar.getTime();
	}

}
