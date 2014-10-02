/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.policy.productListUnits;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductListPoliciesTest.
 * 
 * @author querephi
 */
public class PriceListPolicyTest extends PLMBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(PriceListPolicyTest.class);

	private NodeRef cost1 = null;
	private NodeRef cost2 = null;

	/**
	 * Create a cost list and price list and check policy
	 */
	@Test
	public void testCreateProductLists() {

		final Set<QName> dataLists = new HashSet<QName>();
		dataLists.add(PLMModel.TYPE_COSTLIST);
		dataLists.add(PLMModel.TYPE_PRICELIST);

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*
				 * Create raw material
				 */

				RawMaterialData rawMaterialData = new RawMaterialData();
				rawMaterialData.setUnit(ProductUnit.kg);
				rawMaterialData.setName("RM");

				cost1 = costs.get(0);
				nodeService.setProperty(cost1, PLMModel.PROP_COSTCURRENCY, "€");
				cost2 = costs.get(1);
				nodeService.setProperty(cost2, PLMModel.PROP_COSTCURRENCY, "$");

				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 12d, "", null, cost1, false));
				costList.add(new CostListDataItem(null, 11d, "", null, cost2, false));
				rawMaterialData.setCostList(costList);

				List<PriceListDataItem> priceList = new ArrayList<PriceListDataItem>();
				priceList.add(new PriceListDataItem(null, 22d, "€/kg", 1000d, "kg", 1, null, null, cost1, null));
				priceList.add(new PriceListDataItem(null, 23d, "€/kg", 1000d, "kg", 2, null, null, cost1, null));
				rawMaterialData.setPriceList(priceList);

				NodeRef rawMaterialNodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterialData).getNodeRef();

				return rawMaterialNodeRef;

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				RawMaterialData rawMaterialDBData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);
				int checks = 0;

				for (CostListDataItem c : rawMaterialDBData.getCostList()) {

					logger.debug("costList unit: " + c.getUnit());
					if (c.getCost().equals(cost1)) {
						assertEquals("Check 1st costList", "€/kg", c.getUnit());
						assertEquals("Check 1st costList", 22d, c.getValue());
						checks++;
					} else if (c.getCost().equals(cost2)) {
						assertEquals("Check 2nd costList", "$/kg", c.getUnit());
						assertEquals("Check 2nd costList", 11d, c.getValue());
						checks++;
					} else {
						assertTrue(false);
					}
				}

				assertEquals(2, checks);

				/*
				 * Change pref rank
				 */
				for (PriceListDataItem p : rawMaterialDBData.getPriceList()) {

					if (p.getValue().equals(23d)) {
						p.setPrefRank(1);
					} else if (p.getValue().equals(22d)) {

						p.setPrefRank(2);
					} else {
						assertTrue(false);
					}
				}

				alfrescoRepository.save(rawMaterialDBData);

				return rawMaterialNodeRef;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				RawMaterialData rawMaterialDBData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);

				for (CostListDataItem c : rawMaterialDBData.getCostList()) {

					logger.debug("costList unit: " + c.getUnit());
					if (c.getCost().equals(cost1)) {
						assertEquals("Check 1st costList", "€/kg", c.getUnit());
						assertEquals("Check 1st costList", 23d, c.getValue());
					} else if (c.getCost().equals(cost2)) {
						assertEquals("Check 2nd costList", "$/kg", c.getUnit());
					} else {
						assertTrue(false);
					}
				}

				/*
				 * Change price value
				 */
				for (PriceListDataItem p : rawMaterialDBData.getPriceList()) {

					if (p.getValue().equals(23d)) {
						p.setValue(40d);
					} else if (p.getValue().equals(22d)) {

					} else {
						assertTrue(false);
					}
				}

				alfrescoRepository.save( rawMaterialDBData);

				return rawMaterialNodeRef;

			}
		}, false, true);

		RawMaterialData rawMaterialDBData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);

		for (CostListDataItem c : rawMaterialDBData.getCostList()) {

			logger.debug("costList unit: " + c.getUnit());
			if (c.getCost().equals(cost1)) {
				assertEquals("Check 1st costList", "€/kg", c.getUnit());
				assertEquals("Check 1st costList", 40d, c.getValue());
			} else if (c.getCost().equals(cost2)) {
				assertEquals("Check 2nd costList", "$/kg", c.getUnit());
			} else {
				assertTrue(false);
			}
		}
		
		//Reset cost 
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				nodeService.setProperty(cost2, PLMModel.PROP_COSTCURRENCY, "€");
				return null;

			}
		}, false, true);

		

	}

}
