/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class SortableListPolicyTest.
 * 
 * @author querephi
 */
public class SortableListPolicyTest extends PLMBaseTestCase {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(SortableListPolicyTest.class);

	@Resource
	private EntityListDAO entityListDAO;

	/** The sf node ref. */
	private NodeRef sfNodeRef;

	/**
	 * Create a list item and check initialization
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testInitSort() throws InterruptedException {

		logger.debug("testChangeSortListItem()");


		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(0), false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(1), false));
				costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(2), false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(3), false));
				sfData.setCostList(costList);

				sfNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sfData).getNodeRef();

				// simulate the UI
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST);

				Map<QName, Serializable> properties = new HashMap<>();
				ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
						PLMModel.TYPE_COSTLIST, properties);
				nodeService.createAssociation(childAssocRef.getChildRef(), costs.get(3), PLMModel.ASSOC_COSTLIST_COST);

				childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
						PLMModel.TYPE_COSTLIST, properties);
				nodeService.createAssociation(childAssocRef.getChildRef(), costs.get(3), PLMModel.ASSOC_COSTLIST_COST);

				return null;

			}
		}, false, true);

		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// load SF and test it
				SemiFinishedProductData sfData = (SemiFinishedProductData) alfrescoRepository.findOne(sfNodeRef);

				printSort(sfData.getCostList());

				assertEquals("Check cost order", 100,
						nodeService.getProperty(sfData.getCostList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 101,
						nodeService.getProperty(sfData.getCostList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 102,
						nodeService.getProperty(sfData.getCostList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 103,
						nodeService.getProperty(sfData.getCostList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 104,
						nodeService.getProperty(sfData.getCostList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 105,
						nodeService.getProperty(sfData.getCostList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));

				return null;

			}
		}, false, true);

	}

	public void printSort(List<CostListDataItem> costListDataItem) {

		for (CostListDataItem c : costListDataItem) {

			logger.info("level : " + nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL)
					+ " - Cost " + nodeService.getProperty(c.getCost(), ContentModel.PROP_NAME)
					+ " - sorted: " + nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_SORT));
		}
	}
}
