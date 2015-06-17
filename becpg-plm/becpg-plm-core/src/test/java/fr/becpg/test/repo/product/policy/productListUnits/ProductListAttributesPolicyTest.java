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
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductListPoliciesTest.
 * 
 * @author querephi
 */
public class ProductListAttributesPolicyTest extends PLMBaseTestCase {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(ProductListAttributesPolicyTest.class);

	final Set<QName> dataLists = new HashSet<>();
	NodeRef cost1 = null;
	NodeRef cost2 = null;
	NodeRef cost3 = null;
	NodeRef nut1 = null;
	NodeRef nut2 = null;
	NodeRef costListItem1NodeRef = null;
	NodeRef costListItem2NodeRef = null;
	NodeRef costListItem3NodeRef = null;
	NodeRef nutListItem1NodeRef = null;
	NodeRef nutListItem2NodeRef = null;

	/**
	 * Create a cost list and nut list without filling units
	 */
	@Test
	public void testCreateProductLists() {

		dataLists.add(PLMModel.TYPE_COSTLIST);
		dataLists.add(PLMModel.TYPE_NUTLIST);

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				cost1 = costs.get(0);
				nodeService.setProperty(cost1, PLMModel.PROP_COSTCURRENCY, "€");
				cost2 = costs.get(1);
				nodeService.setProperty(cost2, PLMModel.PROP_COSTCURRENCY, "$");
				cost3 = costs.get(2);
				nodeService.setProperty(cost3, PLMModel.PROP_COSTCURRENCY, "€");
				nodeService.setProperty(cost3, PLMModel.PROP_COSTFIXED, true);

				nut1 = nuts.get(0);
				nodeService.setProperty(nut1, PLMModel.PROP_NUTUNIT, "kcal");
				nut2 = nuts.get(1);
				nodeService.setProperty(nut2, PLMModel.PROP_NUTUNIT, "kJ");

				/*
				 * Create raw material
				 */

				RawMaterialData rawMaterialData = new RawMaterialData();
				rawMaterialData.setUnit(ProductUnit.kg);
				rawMaterialData.setName("RM");

				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, 12d, "", null, cost1, false));
				costList.add(new CostListDataItem(null, 11d, "", null, cost2, false));
				costList.add(new CostListDataItem(null, 13d, "", null, cost3, false));
				rawMaterialData.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, 12.4d, "", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 12.5d, "", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterialData.setNutList(nutList);

				return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterialData).getNodeRef();

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
					} else if (c.getCost().equals(cost2)) {
						assertEquals("Check 2nd costList", "$/kg", c.getUnit());
					} else if (c.getCost().equals(cost3)) {
						assertEquals("Check 3rd costList", "€", c.getUnit());
					} else {
						assertTrue(false);
					}
				}

				for (NutListDataItem n : rawMaterialDBData.getNutList()) {

					logger.debug("nutList unit: " + n.getUnit());
					if (n.getNut().equals(nut1)) {
						assertEquals("Check 1st nutList", "kcal/100g", n.getUnit());
					} else if (n.getNut().equals(nut2)) {
						assertEquals("Check 2nd nutList", "kJ/100g", n.getUnit());
					} else {
						assertTrue(false);
					}
				}

				/*
				 * Change product unit
				 */
				nodeService.setProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_UNIT, ProductUnit.L);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				RawMaterialData rawMaterialDBData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);

				for (CostListDataItem c : rawMaterialDBData.getCostList()) {

					logger.debug("costList unit: " + c.getUnit());
					if (c.getCost().equals(cost1)) {
						assertEquals("Check 1st costList", "€/L", c.getUnit());
					} else if (c.getCost().equals(cost2)) {
						assertEquals("Check 2nd costList", "$/L", c.getUnit());
					} else if (c.getCost().equals(cost3)) {
						assertEquals("Check 3rd costList", "€", c.getUnit());
					} else {
						assertTrue(false);
					}
				}

				for (NutListDataItem n : rawMaterialDBData.getNutList()) {

					logger.debug("nutList unit: " + n.getUnit());
					if (n.getNut().equals(nut1)) {
						assertEquals("Check 1st nutList", "kcal/100mL", n.getUnit());
					} else if (n.getNut().equals(nut2)) {
						assertEquals("Check 2nd nutList", "kJ/100mL", n.getUnit());
					} else {
						assertTrue(false);
					}
				}

				return null;
			}
		}, false, true);

		/*
		 * Change cost, nut
		 */

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				RawMaterialData rawMaterialDBData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);

				/*
				 * Change cost, nut
				 */
				int checks = 0;
				for (CostListDataItem c : rawMaterialDBData.getCostList()) {

					if (c.getCost().equals(cost1)) {
						costListItem1NodeRef = c.getNodeRef();
						checks++;
					} else if (c.getCost().equals(cost2)) {
						costListItem2NodeRef = c.getNodeRef();
						checks++;
					} else if (c.getCost().equals(cost3)) {
						costListItem3NodeRef = c.getNodeRef();
						checks++;
					}
				}
				assertEquals(3, checks);

				nodeService.removeAssociation(costListItem1NodeRef, cost1, PLMModel.ASSOC_COSTLIST_COST);
				nodeService.removeAssociation(costListItem2NodeRef, cost2, PLMModel.ASSOC_COSTLIST_COST);
				nodeService.removeAssociation(costListItem3NodeRef, cost3, PLMModel.ASSOC_COSTLIST_COST);
				nodeService.createAssociation(costListItem1NodeRef, cost2, PLMModel.ASSOC_COSTLIST_COST);
				nodeService.createAssociation(costListItem2NodeRef, cost3, PLMModel.ASSOC_COSTLIST_COST);
				nodeService.createAssociation(costListItem3NodeRef, cost1, PLMModel.ASSOC_COSTLIST_COST);

				checks = 0;
				for (NutListDataItem n : rawMaterialDBData.getNutList()) {

					if (n.getNut().equals(nut1)) {
						nutListItem1NodeRef = n.getNodeRef();
						checks++;
					} else if (n.getNut().equals(nut2)) {
						nutListItem2NodeRef = n.getNodeRef();
						checks++;
					}
				}
				assertEquals(2, checks);

				nodeService.removeAssociation(nutListItem1NodeRef, nut1, PLMModel.ASSOC_NUTLIST_NUT);
				nodeService.removeAssociation(nutListItem2NodeRef, nut2, PLMModel.ASSOC_NUTLIST_NUT);
				nodeService.createAssociation(nutListItem1NodeRef, nut2, PLMModel.ASSOC_NUTLIST_NUT);
				nodeService.createAssociation(nutListItem2NodeRef, nut1, PLMModel.ASSOC_NUTLIST_NUT);

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				RawMaterialData rawMaterialDBData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);

				rawMaterialDBData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);

				for (CostListDataItem c : rawMaterialDBData.getCostList()) {

					logger.debug("costList unit: " + c.getUnit());
					if (c.getNodeRef().equals(costListItem1NodeRef)) {
						assertEquals("Check 1st costList", "$/L", c.getUnit());
					} else if (c.getNodeRef().equals(costListItem2NodeRef)) {
						assertEquals("Check 2nd costList", "€", c.getUnit());
					} else if (c.getNodeRef().equals(costListItem3NodeRef)) {
						assertEquals("Check 2nd costList", "€/L", c.getUnit());
					} else {
						assertTrue(false);
					}
				}

				for (NutListDataItem n : rawMaterialDBData.getNutList()) {

					logger.debug("nutList unit: " + n.getUnit());
					if (n.getNodeRef().equals(nutListItem1NodeRef)) {
						assertEquals("Check 1st nutList", "kJ/100mL", n.getUnit());
					} else if (n.getNodeRef().equals(nutListItem2NodeRef)) {
						assertEquals("Check 2nd nutList", "kcal/100mL", n.getUnit());
					} else {
						assertTrue(false);
					}
				}

				return null;

			}
		}, false, true);
		
		// reset for other tests
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				
				cost1 = costs.get(0);
				nodeService.setProperty(cost1, PLMModel.PROP_COSTCURRENCY, "€");
				cost2 = costs.get(1);
				nodeService.setProperty(cost2, PLMModel.PROP_COSTCURRENCY, "€");
				cost3 = costs.get(2);
				nodeService.setProperty(cost3, PLMModel.PROP_COSTCURRENCY, "€");
				nodeService.setProperty(cost3, PLMModel.PROP_COSTFIXED, false);
				
				return null;

			}
		}, false, true);		
	}

}
