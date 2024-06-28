/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity.datalist.policy;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class DepthLevelListPolicyTest.
 *
 * @author querephi
 */
public class DepthLevelListPolicyIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(DepthLevelListPolicyIT.class);

	@Autowired
	private DataListSortService dataListSortService;

	@Autowired
	private CopyService copyService;

	/**
	 * Test get Multilevel of the compoList
	 */
	@Test
	public void testChangeParentLevelCompoList() {

		logger.debug("testChangeParentLevelCompoList");

		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()), false, true);

		final ProductData finishedProductLoaded = inWriteTx(() -> {

			final ProductData tmp = alfrescoRepository.findOne(finishedProductNodeRef);

			assertNotNull(tmp.getCompoListView().getCompoList());

			printSort(tmp.getCompoListView().getCompoList());

			assertEquals(7, tmp.getCompoListView().getCompoList().size());
			assertEquals((Integer) 1, tmp.getCompoListView().getCompoList().get(0).getDepthLevel());
			assertEquals((Integer) 2, tmp.getCompoListView().getCompoList().get(1).getDepthLevel());
			assertEquals((Integer) 3, tmp.getCompoListView().getCompoList().get(2).getDepthLevel());
			assertEquals((Integer) 1, tmp.getCompoListView().getCompoList().get(3).getDepthLevel());
			assertEquals((Integer) 2, tmp.getCompoListView().getCompoList().get(4).getDepthLevel());
			assertEquals((Integer) 2, tmp.getCompoListView().getCompoList().get(5).getDepthLevel());
			assertEquals((Integer) 1, tmp.getCompoListView().getCompoList().get(6).getDepthLevel());

			assertEquals(100, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(0).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(1).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(2).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(3).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(4).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(5).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(6).getNodeRef(),
					BeCPGModel.PROP_SORT));
			return tmp;
		});

		inWriteTx(() -> {

			// change parentLevel
			logger.debug("Change parentLevel");
			nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
					BeCPGModel.PROP_PARENT_LEVEL,
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef());
			return null;
		});

		inWriteTx(() -> {
			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			// check level have been propagated
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(4,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(5,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(5,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));

			return null;
		});

		inWriteTx(() -> {

			// change parentLevel
			logger.debug("Change parentLevel 2");
			nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
					BeCPGModel.PROP_PARENT_LEVEL,
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
			return null;
		});

		inWriteTx(() -> {
			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			// check level have been propagated
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(4,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(5,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
			return null;
		});

		inWriteTx(() -> {

			// change parentLevel
			logger.debug("Change parentLevel 3");
			nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
					BeCPGModel.PROP_PARENT_LEVEL,
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
			return null;
		});

		inWriteTx(() -> {
			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			// check level have been propagated
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(4,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(5,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
			return null;
		});

		inWriteTx(() -> {

			// delete parentLevel
			logger.debug("Delete parentLevel: "
					+ finishedProductLoaded.getCompoListView().getCompoList().get(2).getProduct());
			nodeService.deleteNode(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef());
			return null;
		});

		inWriteTx(() -> {
			ProductData finishedProductLoaded2 = alfrescoRepository.findOne(finishedProductNodeRef);

			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			assertNotNull(finishedProductLoaded2.getCompoListView().getCompoList());
			assertEquals(5, finishedProductLoaded2.getCompoListView().getCompoList().size());
			return null;
		});
	}

	/**
	 * Test swap
	 */
	@Test
	public void testSwap() {

		logger.debug("testSwap");

		NodeRef finishedProductNodeRef = inWriteTx(
				() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()));

		final ProductData finishedProductLoaded = inWriteTx(() -> {

			ProductData tmp = alfrescoRepository.findOne(finishedProductNodeRef);

			assertNotNull(tmp.getCompoListView().getCompoList());

			printSort(tmp.getCompoListView().getCompoList());

			assertEquals(100, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(0).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(1).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(2).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(3).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(4).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(5).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(6).getNodeRef(),
					BeCPGModel.PROP_SORT));

			return tmp;
		});

		inWriteTx(() -> {

			dataListSortService.move(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), true);
			return null;
		});

		inWriteTx(() -> {

			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));

			return null;
		});
	}

	/**
	 * Test insert after
	 */
	@Test
	public void testinsertAfter() {

		logger.debug("testinsertAfter");

		NodeRef finishedProductNodeRef = inWriteTx(
				() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()));

		final ProductData finishedProductLoaded = inWriteTx(() -> {

			ProductData tmp = alfrescoRepository.findOne(finishedProductNodeRef);

			assertNotNull(tmp.getCompoListView().getCompoList());

			printSort(tmp.getCompoListView().getCompoList());

			assertEquals((Integer) 1, tmp.getCompoListView().getCompoList().get(0).getDepthLevel());
			assertEquals((Integer) 2, tmp.getCompoListView().getCompoList().get(1).getDepthLevel());
			assertEquals((Integer) 3, tmp.getCompoListView().getCompoList().get(2).getDepthLevel());
			assertEquals((Integer) 1, tmp.getCompoListView().getCompoList().get(3).getDepthLevel());
			assertEquals((Integer) 2, tmp.getCompoListView().getCompoList().get(4).getDepthLevel());
			assertEquals((Integer) 2, tmp.getCompoListView().getCompoList().get(5).getDepthLevel());
			assertEquals((Integer) 1, tmp.getCompoListView().getCompoList().get(6).getDepthLevel());

			assertEquals(100, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(0).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(1).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(2).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(3).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(4).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(5).getNodeRef(),
					BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(tmp.getCompoListView().getCompoList().get(6).getNodeRef(),
					BeCPGModel.PROP_SORT));

			return tmp;
		});

		logger.debug("Test InsertAfter");
		inWriteTx(() -> {

			dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef());
			return null;
		});

		inWriteTx(() -> {

			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(4,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(4,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(101, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(102, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(103, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(104, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(105, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(106, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
			return null;
		});
		logger.debug("Test InsertAfter 2");
		inWriteTx(() -> {

			dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
			return null;
		});

		inWriteTx(() -> {

			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(200, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(300, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(400, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(601, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(602, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(700, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
			return null;
		});
		logger.debug("Test InsertAfter 3");
		inWriteTx(() -> {

			dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef());
			return null;
		});

		inWriteTx(() -> {

			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(200, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(300, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(701, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(702, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(703, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(700, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
			return null;
		});
		logger.debug("Test InsertAfter 4");
		inWriteTx(() -> {

			dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
			return null;
		});

		inWriteTx(() -> {

			printSort(finishedProductLoaded.getCompoListView().getCompoList());

			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(2,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(3,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));
			assertEquals(1,
					nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(),
							BeCPGModel.PROP_DEPTH_LEVEL));

			assertEquals(100, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(200, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(300, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(301, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(302, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(303, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
			assertEquals(700, nodeService.getProperty(
					finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
			return null;
		});
	}

	/**
	 * Test if cycles are detected
	 */
	@Test
	public void testCycles() {

		logger.debug("testCycles");

		NodeRef finishedProductNodeRef = inWriteTx(
				() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()));

		final ProductData finishedProductLoaded = alfrescoRepository.findOne(finishedProductNodeRef);

		assertNotNull(finishedProductLoaded.getCompoListView().getCompoList());

		inWriteTx(() -> {

			// change parentLevel : choose itself to get a cycle
			logger.debug("Change parentLevel : choose itself to get a cycle");
			nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(),
					BeCPGModel.PROP_PARENT_LEVEL,
					finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef());
			return null;
		});
	}

	/**
	 * Test parent level copy (#637)
	 *
	 * PF MP SFL
	 */
	@Test
	public void testParentLevelCopy() {

		logger.debug("testParentLevelCopy");

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			NodeRef rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

			LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
			lSF1.setName("Local semi finished 1");
			NodeRef lSF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF1).getNodeRef();

			/*-- Create finished product --*/
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
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
			 * DeclarationType.Declare, lSF1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(lSF1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		inWriteTx(() -> {

			ProductData finishedProductLoaded = alfrescoRepository.findOne(finishedProductNodeRef);

			finishedProductLoaded.getCompoListView().getCompoList().get(0)
					.setParent(finishedProductLoaded.getCompoListView().getCompoList().get(1));
			alfrescoRepository.save(finishedProductLoaded);

			return finishedProductNodeRef;
		});

		final NodeRef copiedNodeRef = inWriteTx(() -> {

			logger.debug("copy product");
			return copyService.copy(finishedProductNodeRef, getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					ContentModel.ASSOC_CHILDREN, true);

		});

		inWriteTx(() -> {

			final ProductData finishedProductLoaded = alfrescoRepository.findOne(finishedProductNodeRef);
			final ProductData copiedProductLoaded = alfrescoRepository.findOne(copiedNodeRef);

			printSort(finishedProductLoaded.getCompoList());
			logger.debug("copied sort :" + copiedProductLoaded.getName());
			printSort(copiedProductLoaded.getCompoList());

			assertNull(finishedProductLoaded.getCompoListView().getCompoList().get(0).getParent());
			assertNull(copiedProductLoaded.getCompoListView().getCompoList().get(0).getParent());
			assertNotNull(finishedProductLoaded.getCompoListView().getCompoList().get(1).getParent());
			assertNotNull(copiedProductLoaded.getCompoListView().getCompoList().get(1).getParent());

			logger.debug("origin "
					+ finishedProductLoaded.getCompoListView().getCompoList().get(1).getParent().getNodeRef());
			logger.debug(
					"target " + copiedProductLoaded.getCompoListView().getCompoList().get(1).getParent().getNodeRef());
			assertNotSame(finishedProductLoaded.getCompoListView().getCompoList().get(1).getParent().getNodeRef(),
					copiedProductLoaded.getCompoListView().getCompoList().get(1).getParent().getNodeRef());
			return null;
		});
	}

	public void printSort(List<CompoListDataItem> compoListDataItem) {

		for (CompoListDataItem c : compoListDataItem) {
			if (nodeService.exists(c.getNodeRef())) {

				Integer level = (Integer) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL);

				logger.info("  ".repeat(level != null ? level : 1) + " - Product "
						+ nodeService.getProperty(c.getProduct(), ContentModel.PROP_NAME)
						+ ((c.getParent() != null) && (c.getParent().getProduct() != null)
								? " - Parent: "
										+ nodeService.getProperty(c.getParent().getProduct(), ContentModel.PROP_NAME)
								: "")
						+ " - sorted: " + nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_SORT));
			}
		}
	}
}
