/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.datalist.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class MultiLevelDataServiceTest.
 * 
 * @author querephi
 */
public class DepthLevelListPolicyTest extends RepoBaseTestCase {

	private static String PATH_TESTFOLDER = "TestFolder";

	/** The logger. */
	private static Log logger = LogFactory.getLog(DepthLevelListPolicyTest.class);


	/** The product dao. */
	private ProductDAO productDAO;


	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		productDAO = (ProductDAO) ctx.getBean("productDAO");
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
	public void testChangeFatherCompoList() {

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
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.DECLARE_FR, lSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.DECLARE_FR, lSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.DECLARE_FR, lSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, lSF4NodeRef));
				finishedProduct.setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				NodeRef finishedProductNodeRef = productDAO.create(testFolder, finishedProduct, dataLists);

				ProductData finishedProductLoaded = productDAO.find(finishedProductNodeRef, dataLists);				
				
				assertNotNull(finishedProductLoaded.getCompoList());
				assertEquals(6, finishedProductLoaded.getCompoList().size());
				assertEquals((Integer)3, finishedProductLoaded.getCompoList().get(2).getDepthLevel());
				assertEquals((Integer)1, finishedProductLoaded.getCompoList().get(3).getDepthLevel());
				
				// change father
				logger.debug("Change father");
				nodeService.setProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), 
						BeCPGModel.PROP_FATHER, finishedProductLoaded.getCompoList().get(2).getNodeRef());
				
				//check level have been propagated
				assertEquals((Integer)4, nodeService.getProperty(finishedProductLoaded.getCompoList().get(3).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
				assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoList().get(4).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
				assertEquals((Integer)5, nodeService.getProperty(finishedProductLoaded.getCompoList().get(5).getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL));
				
				// delete father
				logger.debug("Change father");
				nodeService.deleteNode(finishedProductLoaded.getCompoList().get(3).getNodeRef());
				
				
				
				return finishedProductNodeRef;

			}
		}, false, true);
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		ProductData finishedProductLoaded = productDAO.find(finishedProductNodeRef, dataLists);
		
		assertNotNull(finishedProductLoaded.getCompoList());
		assertEquals(3, finishedProductLoaded.getCompoList().size());
	}
}
