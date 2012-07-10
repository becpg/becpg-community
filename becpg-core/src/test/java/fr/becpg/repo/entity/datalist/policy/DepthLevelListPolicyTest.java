/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.datalist.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class DepthLevelListPolicyTest.
 * 
 * @author querephi
 */
public class DepthLevelListPolicyTest extends RepoBaseTestCase {

	private static String PATH_TESTFOLDER = "TestFolder";

	/** The logger. */
	private static Log logger = LogFactory.getLog(DepthLevelListPolicyTest.class);

	private DataListSortService dataListSortService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		dataListSortService = (DataListSortService) ctx.getBean("dataListSortService");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		try {
			authenticationComponent.clearCurrentSecurityContext();
		} catch (Throwable e) {
			e.printStackTrace();
			// Don't let this mask any previous exceptions
		}
		super.tearDown();

	}

	/**
	 * Test get Multilevel of the compoList
	 */
	public void testChangeParentLevelCompoList() {

		logger.debug("testGetWUsedProduct");

		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- create folders : Test--*/
				logger.debug("/*-- create folders --*/");
				NodeRef testFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
				if (testFolder != null) {
					fileFolderService.delete(testFolder);
				}
				testFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				NodeRef rawMaterial1NodeRef = productDAO.create(testFolder, rawMaterial1, null);
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				NodeRef rawMaterial2NodeRef = productDAO.create(testFolder, rawMaterial2, null);
				LocalSemiFinishedProduct lSF1 = new LocalSemiFinishedProduct();
				lSF1.setName("Local semi finished 1");
				NodeRef lSF1NodeRef = productDAO.create(testFolder, lSF1, null);

				LocalSemiFinishedProduct lSF2 = new LocalSemiFinishedProduct();
				lSF2.setName("Local semi finished 2");
				NodeRef lSF2NodeRef = productDAO.create(testFolder, lSF2, null);
				
				LocalSemiFinishedProduct lSF3 = new LocalSemiFinishedProduct();
				lSF3.setName("Local semi finished 3");
				NodeRef lSF3NodeRef = productDAO.create(testFolder, lSF3, null);
				
				LocalSemiFinishedProduct lSF4 = new LocalSemiFinishedProduct();
				lSF4.setName("Local semi finished 4");
				NodeRef lSF4NodeRef = productDAO.create(testFolder, lSF4, null);

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Finished Product");
				List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, lSF4NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
				finishedProduct.setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				return productDAO.create(testFolder, finishedProduct, dataLists);

			}
		}, false, true);
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		final ProductData finishedProductLoaded = productDAO.find(finishedProductNodeRef, dataLists);						

		assertNotNull(finishedProductLoaded.getCompoList());

		printSort(finishedProductLoaded.getCompoList());
		
		assertEquals(7, finishedProductLoaded.getCompoList().size());
		assertEquals((Integer)1, finishedProductLoaded.getCompoList().get(0).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoList().get(1).getDepthLevel());
		assertEquals((Integer)3, finishedProductLoaded.getCompoList().get(2).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoList().get(3).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoList().get(4).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoList().get(5).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoList().get(6).getDepthLevel());
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
				
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// change parentLevel
				logger.debug("Change parentLevel");
				nodeService.setProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), 
						BeCPGModel.PROP_PARENT_LEVEL, finishedProductLoaded.getCompoList().get(2).getNodeRef());
				return null;
			}
		}, false, true);
		
		printSort(finishedProductLoaded.getCompoList());
		
		
		//check level have been propagated
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// change parentLevel
				logger.debug("Change parentLevel 2");
				nodeService.setProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), 
						BeCPGModel.PROP_PARENT_LEVEL, finishedProductLoaded.getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);

		printSort(finishedProductLoaded.getCompoList());
		
		//check level have been propagated
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));							
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// change parentLevel
				logger.debug("Change parentLevel 3");
				nodeService.setProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), 
						BeCPGModel.PROP_PARENT_LEVEL, finishedProductLoaded.getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);
		
		printSort(finishedProductLoaded.getCompoList());
		
		//check level have been propagated
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));												
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
								
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// delete parentLevel
				logger.debug("Change parentLevel");
				nodeService.deleteNode(finishedProductLoaded.getCompoList().get(2).getNodeRef());
				return null;
			}
		}, false, true);			
		
		ProductData finishedProductLoaded2 = productDAO.find(finishedProductNodeRef, dataLists);
		
		assertNotNull(finishedProductLoaded2.getCompoList());
		assertEquals(5, finishedProductLoaded2.getCompoList().size());
	}	
	
	/**
	 * Test swap
	 */
	public void testSwap() {

		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- create folders : Test--*/
				logger.debug("/*-- create folders --*/");
				NodeRef testFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
				if (testFolder != null) {
					fileFolderService.delete(testFolder);
				}
				testFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				NodeRef rawMaterial1NodeRef = productDAO.create(testFolder, rawMaterial1, null);
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				NodeRef rawMaterial2NodeRef = productDAO.create(testFolder, rawMaterial2, null);
				LocalSemiFinishedProduct lSF1 = new LocalSemiFinishedProduct();
				lSF1.setName("Local semi finished 1");
				NodeRef lSF1NodeRef = productDAO.create(testFolder, lSF1, null);

				LocalSemiFinishedProduct lSF2 = new LocalSemiFinishedProduct();
				lSF2.setName("Local semi finished 2");
				NodeRef lSF2NodeRef = productDAO.create(testFolder, lSF2, null);
				
				LocalSemiFinishedProduct lSF3 = new LocalSemiFinishedProduct();
				lSF3.setName("Local semi finished 3");
				NodeRef lSF3NodeRef = productDAO.create(testFolder, lSF3, null);
				
				LocalSemiFinishedProduct lSF4 = new LocalSemiFinishedProduct();
				lSF4.setName("Local semi finished 4");
				NodeRef lSF4NodeRef = productDAO.create(testFolder, lSF4, null);

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Finished Product");
				List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, lSF4NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
				finishedProduct.setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				return productDAO.create(testFolder, finishedProduct, dataLists);
			}
		}, false, true);		
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		final ProductData finishedProductLoaded = productDAO.find(finishedProductNodeRef, dataLists);						

		assertNotNull(finishedProductLoaded.getCompoList());

		printSort(finishedProductLoaded.getCompoList());				
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.move(finishedProductLoaded.getCompoList().get(3).getNodeRef(), true);
				return null;
			}
		}, false, true);
		
		
		printSort(finishedProductLoaded.getCompoList());
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));		
	}	
	
	/**
	 * Test insert after
	 */
	public void testinsertAfter() {

		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- create folders : Test--*/
				logger.debug("/*-- create folders --*/");
				NodeRef testFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
				if (testFolder != null) {
					fileFolderService.delete(testFolder);
				}
				testFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				NodeRef rawMaterial1NodeRef = productDAO.create(testFolder, rawMaterial1, null);
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				NodeRef rawMaterial2NodeRef = productDAO.create(testFolder, rawMaterial2, null);
				LocalSemiFinishedProduct lSF1 = new LocalSemiFinishedProduct();
				lSF1.setName("Local semi finished 1");
				NodeRef lSF1NodeRef = productDAO.create(testFolder, lSF1, null);

				LocalSemiFinishedProduct lSF2 = new LocalSemiFinishedProduct();
				lSF2.setName("Local semi finished 2");
				NodeRef lSF2NodeRef = productDAO.create(testFolder, lSF2, null);
				
				LocalSemiFinishedProduct lSF3 = new LocalSemiFinishedProduct();
				lSF3.setName("Local semi finished 3");
				NodeRef lSF3NodeRef = productDAO.create(testFolder, lSF3, null);
				
				LocalSemiFinishedProduct lSF4 = new LocalSemiFinishedProduct();
				lSF4.setName("Local semi finished 4");
				NodeRef lSF4NodeRef = productDAO.create(testFolder, lSF4, null);

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Finished Product");
				List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, lSF4NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
				finishedProduct.setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				return productDAO.create(testFolder, finishedProduct, dataLists);
			}
		}, false, true);		
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		final ProductData finishedProductLoaded = productDAO.find(finishedProductNodeRef, dataLists);						

		assertNotNull(finishedProductLoaded.getCompoList());

		printSort(finishedProductLoaded.getCompoList());				
		
		assertEquals((Integer)1, finishedProductLoaded.getCompoList().get(0).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoList().get(1).getDepthLevel());
		assertEquals((Integer)3, finishedProductLoaded.getCompoList().get(2).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoList().get(3).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoList().get(4).getDepthLevel());
		assertEquals((Integer)2, finishedProductLoaded.getCompoList().get(5).getDepthLevel());
		assertEquals((Integer)1, finishedProductLoaded.getCompoList().get(6).getDepthLevel());
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoList().get(2).getNodeRef());
				return null;
			}
		}, false, true);
				
		printSort(finishedProductLoaded.getCompoList());
				
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)101, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)102, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)103, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)104, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)105, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)106, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter 2");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);
				
		printSort(finishedProductLoaded.getCompoList());
		
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)200, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)300, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)400, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)601, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)602, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)700, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter 3");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoList().get(6).getNodeRef());
				return null;
			}
		}, false, true);
				
		printSort(finishedProductLoaded.getCompoList());
		
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)200, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)300, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)701, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)702, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)703, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)700, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
		logger.debug("Test InsertAfter 4");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dataListSortService.insertAfter(finishedProductLoaded.getCompoList().get(3).getNodeRef(), finishedProductLoaded.getCompoList().get(1).getNodeRef());
				return null;
			}
		}, false, true);
		
		
		printSort(finishedProductLoaded.getCompoList());
		
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));				
		assertEquals((Integer)2, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		assertEquals((Integer)3, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));							
		assertEquals((Integer)1, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
		
		assertEquals((Integer)100, nodeService.getProperty(finishedProductLoaded.getCompoList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)200, nodeService.getProperty(finishedProductLoaded.getCompoList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)300, nodeService.getProperty(finishedProductLoaded.getCompoList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)301, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)302, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)303, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		assertEquals((Integer)700, nodeService.getProperty(finishedProductLoaded.getCompoList().get(6).getNodeRef(), BeCPGModel.PROP_SORT));
		
	}	
	
	public void printSort(List<CompoListDataItem> compoListDataItem) {

		for (CompoListDataItem c : compoListDataItem) {

			logger.info("level : " + (Integer) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL)
					+ " - Product " + (String) nodeService.getProperty(c.getProduct(), ContentModel.PROP_NAME)
					+ " - sorted: " + (Integer) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_SORT));
		}
	}
}
