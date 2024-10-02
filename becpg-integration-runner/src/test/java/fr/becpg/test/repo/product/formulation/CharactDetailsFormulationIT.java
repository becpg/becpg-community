/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.icu.text.DecimalFormat;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * The Class FormulationTest.
 *
 * @author querephi
 */
public class CharactDetailsFormulationIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(CharactDetailsFormulationIT.class);

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Test formulate product.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulateCharactDetails() throws Exception {

		logger.info("testFormulateCharactDetails");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setUnitPrice(12.4d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			CompoListDataItem item = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef);

			compoList.add(item);
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			item = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef);
			compoList.add(item);
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			NodeRef finishedProductNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate details --*/");
			productService.formulate(finishedProductNodeRef1);
			CharactDetails ret = productService.formulateDetails(finishedProductNodeRef1, PLMModel.TYPE_NUTLIST, "nutList", null, null);

			Assert.assertNotNull(ret);

			logger.info(CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService).toString(3));
			return finishedProductNodeRef1;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			productService.formulate(finishedProductNodeRef);

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			Assert.assertNotNull(finishedProduct.getNutList());
			for (NutListDataItem nutItem : finishedProduct.getNutList()) {
				Assert.assertTrue(nodeService.hasAspect(nutItem.getNodeRef(), BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM));
			}
			return null;

		}, false, true);

	}

	/**
	 * Test formulate product and check cost details message
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateCostDetails() throws Exception {

		logger.info("testCalculateCostDetails");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			List<PackagingListDataItem> packagingList = new ArrayList<>();
			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial1NodeRef)
);
			packagingList.add(PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.m).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial2NodeRef)
);
			packagingList.add(PackagingListDataItem.build().withQty(8d).withUnit(ProductUnit.PP).withPkgLevel(PackagingLevel.Tertiary).withIsMaster(true).withProduct(packagingMaterial3NodeRef)
);
			finishedProduct.getPackagingListView().setPackagingList(packagingList);

			/*
			 * Composition
			 */
			List<CompoListDataItem> compoList = new ArrayList<>();
			CompoListDataItem item = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef);

			compoList.add(item);
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(10d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			item = CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(20d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef);
			compoList.add(item);
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(item).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// formulate Details
			List<NodeRef> costNodeRefs = new ArrayList<>();
			productService.formulate(finishedProductNodeRef);
			CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_COSTLIST, "costList", costNodeRefs, null);

			Assert.assertNotNull(ret);
			logger.info(CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService).toString(3));

			// costs
			int checks = 0;
			DecimalFormat df = new DecimalFormat("0.####");
			for (Map.Entry<NodeRef, List<CharactDetailsValue>> kv : ret.getData().entrySet()) {

				for (CharactDetailsValue kv2 : kv.getValue()) {

					String trace = "cost: " + nodeService.getProperty(kv.getKey(), BeCPGModel.PROP_CHARACT_NAME) + " - source: "
							+ nodeService.getProperty(kv2.getKeyNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - value: " + kv2.getValue();
					logger.debug(trace);

					// cost1
					if (kv.getKey().equals(cost1)) {

						if (kv2.getKeyNodeRef().equals(rawMaterial1NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 1.7325, actual values: " + trace, df.format(1.7325d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 36.5314,
							// actual values: " + trace, df.format(36.5314),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(rawMaterial2NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 1.21, actual values: " + trace, df.format(1.21d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 25.5140,
							// actual values: " + trace, df.format(25.5140),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(rawMaterial3NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 1.8, actual values: " + trace, df.format(1.8d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 37.9547,
							// actual values: " + trace, df.format(37.9547),
							// df.format(kv2.getPercentage()));
						} else {
							checks++;
						}
					}

					// cost2
					else if (kv.getKey().equals(cost2)) {

						if (kv2.getKeyNodeRef().equals(rawMaterial1NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 1.155, actual values: " + trace, df.format(1.155d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 16.0976,
							// actual values: " + trace, df.format(16.0976),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(rawMaterial2NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 2.42, actual values: " + trace, df.format(2.42d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 33.7282,
							// actual values: " + trace, df.format(33.7282),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(rawMaterial3NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 3.6, actual values: " + trace, df.format(3.6d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 50.1742,
							// actual values: " + trace, df.format(50.1742),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(rawMaterial4NodeRef)) {
							checks++;
						} else {
							checks++;
						}
					}

					// pkgCost1
					else if (kv.getKey().equals(pkgCost1)) {

						if (kv2.getKeyNodeRef().equals(packagingMaterial1NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 48.9796,
							// actual values: " + trace, df.format(48.9796),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(packagingMaterial2NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 48.9796,
							// actual values: " + trace, df.format(48.9796),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(packagingMaterial3NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 0.0625, actual values: " + trace, df.format(0.0625d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 2.0408,
							// actual values: " + trace, df.format(2.0408),
							// df.format(kv2.getPercentage()));
						} else {
							checks++;
						}
					}

					// pkgCost2
					else if (kv.getKey().equals(pkgCost2)) {

						if (kv2.getKeyNodeRef().equals(packagingMaterial1NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 1, actual values: " + trace, df.format(1d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 24.2424,
							// actual values: " + trace, df.format(24.2424),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(packagingMaterial2NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 3, actual values: " + trace, df.format(3d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 72.7273,
							// actual values: " + trace, df.format(72.7273),
							// df.format(kv2.getPercentage()));
						} else if (kv2.getKeyNodeRef().equals(packagingMaterial3NodeRef)) {

							checks++;
							assertEquals("cost.getValue() == 0.125, actual values: " + trace, df.format(0.125d), df.format(kv2.getValue()));
							// assertEquals("cost.getPercentage() == 3.0303,
							// actual values: " + trace, df.format(3.0303),
							// df.format(kv2.getPercentage()));
						} else {
							checks++;
						}
					} else {
						checks++;
					}
				}

			}

			assertEquals("Verify checks done", 12, checks);

			return null;

		}, false, true);

	}

	@Test
	public void testCalculateNutDetails() throws Exception {

		logger.info("testCalculateNutDetails");

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material");
			rawMaterial.setQty(0.1d);
			rawMaterial.setUnit(ProductUnit.kg);
			rawMaterial.setNetWeight(0.1d);
			rawMaterial.setDensity(0.1d);
			rawMaterial.setTare(9d);
			rawMaterial.setTareUnit(TareUnit.g);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withValue(1d).withUnit("g/100g").withMini(0.75d).withMaxi(null).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(3d).withUnit("g/100g").withMini(null).withMaxi(4d).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			rawMaterial.setNutList(nutList);
			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();

		}, false, true);

		final NodeRef semiFinishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Semi finished product

			SemiFinishedProductData semiFinishedProduct = new SemiFinishedProductData();
			semiFinishedProduct.setName("Semi fini 1");
			semiFinishedProduct.setUnit(ProductUnit.kg);
			List<CompoListDataItem> compoListSF = new ArrayList<>();
			compoListSF.add(CompoListDataItem.build().withQtyUsed(0.75d).withUnit(ProductUnit.kg).withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoListSF.add(CompoListDataItem.build().withQtyUsed(1.5d).withUnit(ProductUnit.kg).withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterialNodeRef));
			semiFinishedProduct.getCompoListView().setCompoList(compoListSF);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut3).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut4).withIsManual(false)
);

			semiFinishedProduct.setNutList(nutList);

			return alfrescoRepository.create(getTestFolderNodeRef(), semiFinishedProduct).getNodeRef();

		}, false, true);

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Finished Product

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 2");
			finishedProduct.setLegalName("Legal Produit fini 2");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1.5d);

			// Composition

			List<CompoListDataItem> compoList = new ArrayList<>();
			logger.info("semiFinishedNR: " + semiFinishedProductNodeRef);
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(semiFinishedProductNodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(5d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1.5d).withUnit(ProductUnit.kg).withLossPerc(10d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut3).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut4).withIsManual(false)
);

			finishedProduct.setNutList(nutList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// formulate Details
				List<NodeRef> nutsNodeRefs = new ArrayList<>();
				productService.formulate(finishedProductNodeRef);
				CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, PLMModel.TYPE_NUTLIST, "nutList", nutsNodeRefs, null);

				Assert.assertNotNull(ret);
				JSONObject jsonRet = CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService);

				logger.info(jsonRet.toString(3));
				Assert.assertTrue("no metadata array", jsonRet.has("metadatas"));
				JSONArray metadataArray = jsonRet.getJSONArray("metadatas");

				Assert.assertEquals(8, metadataArray.length());
				Assert.assertEquals("nut1 unset", ((JSONObject) metadataArray.get(1)).get("colName"), "nut1");
				Assert.assertEquals("nut2 unset", ((JSONObject) metadataArray.get(2)).get("colName"), "nut2");
				Assert.assertEquals("nut3 unset", ((JSONObject) metadataArray.get(3)).get("colName"), "nut3");
				Assert.assertEquals("nut4 unset", ((JSONObject) metadataArray.get(4)).get("colName"), "nut4");
				Assert.assertEquals("nut3 mini unset", ((JSONObject) metadataArray.get(5)).get("colName"), "Mini");
				Assert.assertEquals("nut3 maxi unset", ((JSONObject) metadataArray.get(6)).get("colName"), "Maxi");

				Assert.assertTrue("no resultsets", jsonRet.has("resultsets"));
				JSONArray resultsArray = jsonRet.getJSONArray("resultsets");
				Assert.assertEquals("result array does not have 4 arrays inside", 4, resultsArray.length());

				DecimalFormat df = new DecimalFormat("0.###");

				/*
				 * SF 1
				 */
				JSONArray tmpResultsArray = (JSONArray) resultsArray.getJSONArray(0);
				Assert.assertEquals("Semi fini 1", tmpResultsArray.get(0));
				Assert.assertEquals(df.format(0.667d), df.format(tmpResultsArray.get(1)));
				// Assert.assertEquals(df.format(0.556d),
				// df.format(tmpResultsArray.get(2)));

				Assert.assertEquals(df.format(1.778d), df.format(tmpResultsArray.get(2)));
				// Assert.assertEquals(df.format(2.222d),
				// df.format(tmpResultsArray.get(5)));
				// Assert.assertEquals("\u2014",tmpResultsArray.get(6));
				// Assert.assertNull(tmpResultsArray.get(4));
				// Assert.assertNull(tmpResultsArray.get(5));

				Assert.assertEquals(df.format(0.889d), df.format(tmpResultsArray.get(3)));

				/*
				 * RM 1
				 */
				tmpResultsArray = (JSONArray) resultsArray.getJSONArray(1);
				Assert.assertEquals("Raw material 1", tmpResultsArray.get(0));
				Assert.assertEquals(df.format(1.333d), df.format(tmpResultsArray.get(1)));
				// Assert.assertEquals(df.format(1.067d),
				// df.format(tmpResultsArray.get(2)));
				// Assert.assertEquals(df.format(2.8d),
				// df.format(tmpResultsArray.get(4)));

				Assert.assertEquals(df.format(2.667d), df.format(tmpResultsArray.get(2)));
				// Assert.assertEquals(df.format(2.933d),
				// df.format(tmpResultsArray.get(5)));
				// Assert.assertEquals(df.format(2d),
				// df.format(tmpResultsArray.get(5)));

				Assert.assertEquals(df.format(5.333d), df.format(tmpResultsArray.get(3)));

				/*
				 * RM 2
				 */
				tmpResultsArray = (JSONArray) resultsArray.getJSONArray(2);
				Assert.assertEquals("Raw material 2", tmpResultsArray.get(0));
				Assert.assertEquals(df.format(1d), df.format(tmpResultsArray.get(1)));
				// Assert.assertEquals(df.format(0.8d),
				// df.format(tmpResultsArray.get(2)));
				// Assert.assertEquals(df.format(1.1d),
				// df.format(tmpResultsArray.get(3)));
				//
				Assert.assertEquals(df.format(2d), df.format(tmpResultsArray.get(2)));
				// Assert.assertEquals(df.format(2.1d),
				// df.format(tmpResultsArray.get(5)));
				// Assert.assertEquals(df.format(0.8d),
				// df.format(tmpResultsArray.get(6)));
				//
				Assert.assertEquals(df.format(6d), df.format(tmpResultsArray.get(3)));

				/*
				 * Totals
				 */
				JSONArray totalArray = (JSONArray) resultsArray.get(3);
				// TODO put entity.datalist.item.details.totals language key
				// instead ?
				Assert.assertEquals("Totaux ", totalArray.get(0));
				Assert.assertEquals(df.format(3d), df.format(totalArray.get(1)));
				// Assert.assertEquals(df.format(2.422d),
				// df.format(totalArray.get(2)));
				// Assert.assertEquals(df.format(4.567d),
				// df.format(totalArray.get(3)));
				//
				Assert.assertEquals(df.format(6.444d), df.format(totalArray.get(2)));
				// Assert.assertEquals(df.format(7.256d),
				// df.format(totalArray.get(5)));
				// Assert.assertEquals(df.format(4.578d),
				// df.format(totalArray.get(6)));

				Assert.assertEquals(df.format(12.222d), df.format(totalArray.get(3)));
				// Assert.assertEquals(df.format(2.044d),
				// df.format(totalArray.get(8)));
				// Assert.assertEquals(df.format(5.367d),
				// df.format(totalArray.get(9)));

				Assert.assertEquals(df.format(4d), df.format(totalArray.get(4)));
				Assert.assertEquals(df.format(2.8d), df.format(totalArray.get(6)));
				Assert.assertEquals(df.format(1.067d), df.format(totalArray.get(5)));

				return null;

			}
		}, false, true);

	}

}