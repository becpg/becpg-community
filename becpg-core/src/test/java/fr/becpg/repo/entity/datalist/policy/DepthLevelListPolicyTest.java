/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.datalist.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class DepthLevelListPolicyTest.
 * 
 * @author querephi
 */
public class DepthLevelListPolicyTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(DepthLevelListPolicyTest.class);

	@Resource
	private DataListSortService dataListSortService;

	
	/**
	 * Test get Multilevel of the compoList
	 */
	@Test
	public void testChangeParentLevelCompoList() {

		logger.debug("testChangeParentLevelCompoList");


		final RepoBaseTestCase repoBaseTestCase = this;
		
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
			}
		}, false, true);
		
		final ProductData finishedProductLoaded = alfrescoRepository.findOne(finishedProductNodeRef);						

		assertNotNull(finishedProductLoaded.getCompoListView().getCompoList());

		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		assertEquals(7, finishedProductLoaded.getCompoListView().getCompoList().size());
		assertEquals((Integer)1, finishedProductLoaded.getCompoListView().getCompoList().get(0).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoListView().getCompoList().get(1).getDepthLevel());
		assertEquals((Integer)3, finishedProductLoaded.getCompoListView().getCompoList().get(2).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoListView().getCompoList().get(3).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoListView().getCompoList().get(4).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoListView().getCompoList().get(5).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoListView().getCompoList().get(6).getDepthLevel());
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
				
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// change parentLevel
				logger.debug("Change parentLevel");
				nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), 
						BeCPGModel.PROP_PARENT_LEVEL, finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef());
				return null;
			}
		}, false, true);
		
		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		
		//check level have been propagated
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// change parentLevel
				logger.debug("Change parentLevel 2");
				nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), 
						BeCPGModel.PROP_PARENT_LEVEL, finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);

		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		//check level have been propagated
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));							
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// change parentLevel
				logger.debug("Change parentLevel 3");
				nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), 
						BeCPGModel.PROP_PARENT_LEVEL, finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);
		
		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		//check level have been propagated
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));												
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
								
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// delete parentLevel
				logger.debug("Change parentLevel");
				nodeService.deleteNode(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef());
				return null;
			}
		}, false, true);			
		
		ProductData finishedProductLoaded2 = alfrescoRepository.findOne(finishedProductNodeRef);
		
		assertNotNull(finishedProductLoaded2.getCompoListView().getCompoList());
		assertEquals(5, finishedProductLoaded2.getCompoListView().getCompoList().size());
	}	
	
	/**
	 * Test swap
	 */
	@Test
	public void testSwap() {

		logger.debug("testSwap");
		
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
			}
		}, false, true);		
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		final ProductData finishedProductLoaded = alfrescoRepository.findOne(finishedProductNodeRef);						

		assertNotNull(finishedProductLoaded.getCompoListView().getCompoList());

		printSort(finishedProductLoaded.getCompoListView().getCompoList());				
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.move(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), true);
				return null;
			}
		}, false, true);
		
		
		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));		
	}	
	
	/**
	 * Test insert after
	 */
	@Test
	public void testinsertAfter() {

		logger.debug("testinsertAfter");
		
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
			}
		}, false, true);		
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		final ProductData finishedProductLoaded = alfrescoRepository.findOne(finishedProductNodeRef);						

		assertNotNull(finishedProductLoaded.getCompoListView().getCompoList());

		printSort(finishedProductLoaded.getCompoListView().getCompoList());				
		
		assertEquals((Integer)1, finishedProductLoaded.getCompoListView().getCompoList().get(0).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoListView().getCompoList().get(1).getDepthLevel());
		assertEquals((Integer)3, finishedProductLoaded.getCompoListView().getCompoList().get(2).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoListView().getCompoList().get(3).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoListView().getCompoList().get(4).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoListView().getCompoList().get(5).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoListView().getCompoList().get(6).getDepthLevel());
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef());
				return null;
			}
		}, false, true);
				
		printSort(finishedProductLoaded.getCompoListView().getCompoList());
				
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter 2");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);
				
		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)200, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)300, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)400, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)601, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)602, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)700, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter 3");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef());
				return null;
			}
		}, false, true);
				
		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)200, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)300, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)701, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)702, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)703, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)700, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter 4");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);
		
		
		printSort(finishedProductLoaded.getCompoListView().getCompoList());
		
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)200, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)300, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)301, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)302, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)303, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)700, nodeService.getProperty(finishedProductLoaded.getCompoListView().getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
	}	
	
	/**
	 * Test if cycles are detected
	 */
	@Test
	public void testCycles() {

		logger.debug("testCycles");
		
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
			}
		}, false, true);		
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		final ProductData finishedProductLoaded = alfrescoRepository.findOne(finishedProductNodeRef);						

		assertNotNull(finishedProductLoaded.getCompoListView().getCompoList());
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// change parentLevel : choose itself to get a cycle
				logger.debug("Change parentLevel : choose itself to get a cycle");
				nodeService.setProperty(finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef(), 
						BeCPGModel.PROP_PARENT_LEVEL, finishedProductLoaded.getCompoListView().getCompoList().get(3).getNodeRef());
				return null;
			}
		}, false, true);		
	}	
	
	public void printSort(List<CompoListDataItem> compoListDataItem) {

		for (CompoListDataItem c : compoListDataItem) {

			logger.info("level : " + (Integer) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL)
					+ " - Product " + (String) nodeService.getProperty(c.getProduct(), ContentModel.PROP_NAME)
					+ " - Parent " + c.getParent()
					+ " - sorted: " + (Integer) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_SORT));
		}
	}
}
