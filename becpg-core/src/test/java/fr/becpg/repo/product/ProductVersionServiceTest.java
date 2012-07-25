/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 * 
 * @author querephi
 */
public class ProductVersionServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductVersionServiceTest.class);

	private CheckOutCheckInService checkOutCheckInService;

	private VersionService versionService;

	private ProductService productService;

	private NamespaceService namespaceService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("ProductServiceTest:setUp");

		checkOutCheckInService = (CheckOutCheckInService) ctx.getBean("checkOutCheckInService");
		versionService = (VersionService) ctx.getBean("versionService");
		productService = (ProductService) ctx.getBean("productService");
		namespaceService = (NamespaceService) ctx.getBean("namespaceService");

	}

	// /**
	// * Test create version.
	// */
	// public void testCreateVersion(){
	//
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>(){
	// @Override
	// public NodeRef execute() throws Throwable {
	//
	// logger.info("testCreateVersion");
	//
	// NodeRef rawMaterialNodeRef =
	// createRawMaterial(testFolderNodeRef,"MP test report");
	//
	// Version vRawMaterialNodeRefV0_1 =
	// versionService.createVersion(rawMaterialNodeRef, null);
	//
	// logger.debug("version: " + vRawMaterialNodeRefV0_1.getVersionLabel());
	// assertEquals("0.1", vRawMaterialNodeRefV0_1.getVersionLabel());
	//
	// NodeRef listContainerNodeRef =
	// entityListDAO.getListContainer(rawMaterialNodeRef);
	// assertNotNull("Has list container", listContainerNodeRef);
	//
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
	// NodeRef evRawMaterialNodeRefV0_1 =
	// entityVersionService.getEntityVersion(vRawMaterialNodeRefV0_1);
	// assertNotNull(evRawMaterialNodeRefV0_1);
	// ProductData vRawMaterial = productDAO.find(evRawMaterialNodeRefV0_1,
	// dataLists);
	//
	// assertEquals("Check costs size", rawMaterial.getCostList().size(),
	// vRawMaterial.getCostList().size());
	//
	// for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
	// CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
	// CostListDataItem vCostListDataItem = vRawMaterial.getCostList().get(i);
	//
	// assertEquals("Check cost", costListDataItem.getCost(),
	// vCostListDataItem.getCost());
	// assertEquals("Check cost unit", costListDataItem.getUnit(),
	// vCostListDataItem.getUnit());
	// assertEquals("Check cost value", costListDataItem.getValue(),
	// vCostListDataItem.getValue());
	// assertNotSame("Check cost noderef", costListDataItem.getNodeRef(),
	// vCostListDataItem.getNodeRef());
	// }
	//
	// Version vRawMaterialNodeRefV0_2 =
	// versionService.createVersion(rawMaterialNodeRef, null);
	// logger.debug("version: " + vRawMaterialNodeRefV0_2.getVersionLabel());
	// assertEquals("0.2", vRawMaterialNodeRefV0_2.getVersionLabel());
	//
	// Map<String, Serializable> properties = new HashMap<String,
	// Serializable>();
	// properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
	// Version vRawMaterialNodeRefV0_3 =
	// versionService.createVersion(rawMaterialNodeRef, properties);
	// assertEquals("0.3", vRawMaterialNodeRefV0_3.getVersionLabel());
	//
	// properties.clear();
	// properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
	// Version vRawMaterialNodeRefV1_0 =
	// versionService.createVersion(rawMaterialNodeRef, properties);
	// assertEquals("1.0", vRawMaterialNodeRefV1_0.getVersionLabel());
	//
	// return null;
	//
	// }},false,true);
	//
	// }

	/**
	 * Test check out check in.
	 */
	public void testCheckOutCheckIn() {
		
		final Collection<QName> dataLists = productDictionaryService.getDataLists();
		final ProductUnit productUnit = ProductUnit.P;
		final int valueAdded = 1;

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- Create raw material --*/
				return createRawMaterial(testFolderNodeRef, "MP test report");
				
			}
		}, false, true);
		
		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

				// Check out
				return checkOutCheckInService.checkout(rawMaterialNodeRef);
				
			}
		}, false, true);
		
		final ProductData rawMaterial = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<ProductData>() {
			@Override
			public ProductData execute() throws Throwable {

				assertNotNull("Check working copy exists", workingCopyNodeRef);

				// Check productCode
				assertEquals("productCode should be the same after checkout", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
						nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_CODE));

				// Check costs on working copy				
				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				ProductData workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);
				assertEquals("Check costs size", rawMaterial.getCostList().size(), workingCopyRawMaterial.getCostList().size());

				for (int i = 0; i < rawMaterial.getCostList().size(); i++) {
					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
					CostListDataItem vCostListDataItem = workingCopyRawMaterial.getCostList().get(i);

					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
					assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
				}

				// Modify working copy							
				workingCopyRawMaterial.setUnit(productUnit);
				for (CostListDataItem c : workingCopyRawMaterial.getCostList()) {
					c.setValue(c.getValue() + valueAdded);
				}
				productDAO.update(workingCopyNodeRef, workingCopyRawMaterial, dataLists);				
			
				return rawMaterial;
			}
		}, false, true);
		
		final ProductData newRawMaterial = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<ProductData>() {
			@Override
			public ProductData execute() throws Throwable {

				// Check in
				NodeRef newRawMaterialNodeRef = null;
				try {
					Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
					versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
					newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
				} catch (Exception e) {
					logger.error("Failed to checkin", e);
					assertNull(e);
				}

				assertNotNull("Check new version exists", newRawMaterialNodeRef);
				ProductData newRawMaterial = productDAO.find(newRawMaterialNodeRef, dataLists);
				assertEquals("Check version", "0.2", newRawMaterial.getVersionLabel());
				assertEquals("Check unit", productUnit, newRawMaterial.getUnit());

				// Check productCode
				assertEquals("productCode should be the same after checkin", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
						nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_CODE));

				// Check costs on new version
				assertEquals("Check costs size", rawMaterial.getCostList().size(), newRawMaterial.getCostList().size());

				for (int i = 0; i < rawMaterial.getCostList().size(); i++) {
					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
					CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);

					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
					assertEquals("Check cost value", costListDataItem.getValue() + valueAdded, vCostListDataItem.getValue());
					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
				}

				assertEquals("Check products are the same", rawMaterialNodeRef, newRawMaterialNodeRef);

				// 2nd Check out, Check in
				NodeRef workingCopy2NodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);

				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				versionProperties.put(Version.PROP_DESCRIPTION, "description");
				newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopy2NodeRef, versionProperties);

				newRawMaterial = productDAO.find(newRawMaterialNodeRef, dataLists);
				assertEquals("Check version", "1.0", newRawMaterial.getVersionLabel());

				return newRawMaterial;

			}
		}, false, true);

		// Check cost Unit has changed after transaction
		for (int i = 0; i < newRawMaterial.getCostList().size(); i++) {
			CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);

			assertEquals("Check cost unit", "€/P", vCostListDataItem.getUnit());
		}

	}

	/**
	 * Test cancel check out.
	 */
	public void testCancelCheckOut() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- Create raw material --*/
				NodeRef rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "MP test report");

				// Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
				assertNotNull("Check working copy exists", workingCopyNodeRef);

				// modify
				ProductUnit productUnit2 = ProductUnit.m;
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				ProductData workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);
				workingCopyRawMaterial.setUnit(productUnit2);
				productDAO.update(workingCopyNodeRef, workingCopyRawMaterial, dataLists);
				workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);

				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				assertEquals("Check unit", ProductUnit.kg, rawMaterial.getUnit());
				assertEquals("Check unit", productUnit2, workingCopyRawMaterial.getUnit());

				// cancel check out
				checkOutCheckInService.cancelCheckout(workingCopyNodeRef);

				// Check
				rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				assertEquals("Check unit", ProductUnit.kg, rawMaterial.getUnit());
				return null;

			}
		}, false, true);
	}

	/**
	 * Test get version history.
	 */
	public void testGetVersionHistory() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "MP test report");
				Map<String, Serializable> properties = new HashMap<String, Serializable>();
				properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
				Version vRawMaterialNodeRef = versionService.createVersion(rawMaterialNodeRef, properties);

				assertEquals("0.1", vRawMaterialNodeRef.getVersionLabel());

				Version vRawMaterialNodeRefV0_2 = versionService.createVersion(rawMaterialNodeRef, properties);
				assertEquals("0.2", vRawMaterialNodeRefV0_2.getVersionLabel());

				VersionHistory versionHistory = versionService.getVersionHistory(rawMaterialNodeRef);
				assertEquals("Should have 2 versions", 2, versionHistory.getAllVersions().size());
				Version[] versions = versionHistory.getAllVersions().toArray(new Version[2]);

				assertEquals("Check 1st version", versions[0].getVersionedNodeRef(), vRawMaterialNodeRef.getVersionedNodeRef());
				assertEquals("Check 2nd version", versions[1].getVersionedNodeRef(), vRawMaterialNodeRefV0_2.getVersionedNodeRef());

				return null;

			}
		}, false, true);
	}

	/**
	 * Test check out check in.
	 */
	public void testCheckOutCheckInValidProduct() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef rawMaterialNodeRef = createRawMaterial(testFolderNodeRef, "MP test report");

				// Valid it
				nodeService.setProperty(rawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_STATE, SystemState.Valid);
				productService.classifyProduct(repositoryHelper.getCompanyHome(), rawMaterialNodeRef);

				String path = nodeService.getPath(rawMaterialNodeRef).toPrefixString(namespaceService);
				String expected = "/app:company_home/cm:Products/cm:Valid/cm:RawMaterial/cm:Frozen/cm:Fish/";
				assertEquals("check path", expected, path.substring(0, expected.length()));

				// Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);

				// Check in
				NodeRef newRawMaterialNodeRef = null;

				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
				newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);

				assertNotNull("Check new version exists", newRawMaterialNodeRef);
				assertEquals("Check state new version", SystemState.ToValidate.toString(), nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_STATE));

				path = nodeService.getPath(rawMaterialNodeRef).toPrefixString(namespaceService);
				expected = "/app:company_home/cm:Products/cm:ToValidate/cm:RawMaterial/cm:Frozen/cm:Fish/";
				assertEquals("check path", expected, path.substring(0, expected.length()));

				return null;

			}
		}, false, true);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST WITH ALFRESCO API AND VERSION STORE
	//
	// return
	// java.lang.UnsupportedOperationException: This operation is not supported
	// by a version store implementation of the node service.
	// at
	// org.alfresco.repo.version.NodeServiceImpl.getChildByName(NodeServiceImpl.java:606)
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// /**
	// * Test create version.
	// */
	// public void xxtestCreateVersion2(){
	//
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>(){
	// @Override
	// public NodeRef execute() throws Throwable {
	//
	// /*-- Create test folder --*/
	// NodeRef folderNodeRef =
	// nodeService.getChildByName(repository.getCompanyHome(),
	// ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
	// if(folderNodeRef != null)
	// {
	// fileFolderService.delete(folderNodeRef);
	// }
	// folderNodeRef = fileFolderService.create(repository.getCompanyHome(),
	// PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	//
	// NodeRef rawMaterialNodeRef =
	// createRawMaterial(folderNodeRef,"MP test report");
	//
	// Version vRawMaterialNodeRefV1_1 =
	// versionService.createVersion(rawMaterialNodeRef, null);
	//
	// String versionLabel = vRawMaterialNodeRefV1_1.getVersionLabel();
	// logger.debug("version: " + versionLabel);
	// assertEquals("0.1", versionLabel);
	//
	// NodeRef listContainerNodeRef =
	// entityListDAO.getListContainer(rawMaterialNodeRef);
	// assertNotNull("Has list container", listContainerNodeRef);
	//
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
	// ProductData vRawMaterial =
	// productDAO.find(vRawMaterialNodeRefV1_1.getFrozenStateNodeRef(),
	// dataLists);
	//
	// logger.info("###rawMaterialNodeRef: " + rawMaterialNodeRef);
	// logger.info("###vRawMaterialNodeRefV1_1.getVersionedNodeRef(): " +
	// vRawMaterialNodeRefV1_1.getVersionedNodeRef());
	// logger.info("###vRawMaterialNodeRefV1_1.getFrozenStateNodeRef(): " +
	// vRawMaterialNodeRefV1_1.getFrozenStateNodeRef());
	//
	// assertEquals("Check costs size", rawMaterial.getCostList().size(),
	// vRawMaterial.getCostList().size());
	//
	// for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
	// CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
	// CostListDataItem vCostListDataItem = vRawMaterial.getCostList().get(i);
	//
	// logger.info("###costListDataItem.getNodeRef(): " +
	// costListDataItem.getNodeRef());
	// logger.info("###vCostListDataItem.getNodeRef(): " +
	// vCostListDataItem.getNodeRef());
	//
	// assertEquals("Check cost", costListDataItem.getCost(),
	// vCostListDataItem.getCost());
	// assertEquals("Check cost unit", costListDataItem.getUnit(),
	// vCostListDataItem.getUnit());
	// assertEquals("Check cost value", costListDataItem.getValue(),
	// vCostListDataItem.getValue());
	// assertNotSame("Check cost noderef", costListDataItem.getNodeRef(),
	// vCostListDataItem.getNodeRef());
	// }
	//
	// Version vRawMaterialNodeRefV1_2 =
	// versionService.createVersion(rawMaterialNodeRef, null);
	// String versionLabel1_2 = vRawMaterialNodeRefV1_2.getVersionLabel();
	// logger.debug("version: " + versionLabel1_2);
	// assertEquals("0.2", versionLabel1_2);
	//
	// Map<String, Serializable> properties = new HashMap<String,
	// Serializable>();
	// properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "0.4");
	// Version vRawMaterialNodeRefV1_4 =
	// versionService.createVersion(rawMaterialNodeRef, properties);
	// String versionLabel1_4 = vRawMaterialNodeRefV1_4.getVersionLabel();
	// assertEquals("0.4", versionLabel1_4);
	//
	// Version vRawMaterialNodeRefV1_3 = null;
	// Exception exception = null;
	// properties.clear();
	// properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "0.3");
	// try{
	// vRawMaterialNodeRefV1_3 =
	// versionService.createVersion(rawMaterialNodeRef, properties);
	// }
	// catch(Exception e){
	// exception = e;
	// logger.debug(e.toString());
	// }
	//
	// assertNotNull(exception);
	// assertNull(vRawMaterialNodeRefV1_3);
	//
	// properties.clear();
	// properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "2.0");
	// Version vRawMaterialNodeRefV2_0 =
	// versionService.createVersion(rawMaterialNodeRef, properties);
	// String versionLabel2_0 = vRawMaterialNodeRefV2_0.getVersionLabel();
	// assertEquals("2.0", versionLabel2_0);
	//
	// Version rawMaterialNodeRefV1_5 = null;
	// exception = null;
	// properties.clear();
	// properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.5");
	// try{
	// rawMaterialNodeRefV1_5 = versionService.createVersion(rawMaterialNodeRef,
	// properties);
	// }
	// catch(Exception e){
	// exception = e;
	// logger.debug(e.toString());
	// }
	//
	// assertNotNull(exception);
	// assertNull(rawMaterialNodeRefV1_5);
	//
	// return null;
	//
	// }},false,true);
	//
	// }
	//
	// /**
	// * Test check out check in.
	// */
	// public void xxtestCheckOutCheckIn2(){
	//
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>(){
	// @Override
	// public NodeRef execute() throws Throwable {
	//
	// /*-- create folders --*/
	// logger.debug("/*-- create folders --*/");
	// NodeRef folderNodeRef =
	// nodeService.getChildByName(repository.getCompanyHome(),
	// ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
	// if(folderNodeRef != null)
	// {
	// fileFolderService.delete(folderNodeRef);
	// }
	// folderNodeRef = fileFolderService.create(repository.getCompanyHome(),
	// PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	//
	//
	// /*-- Create raw material --*/
	// final NodeRef rawMaterialNodeRef =
	// createRawMaterial(folderNodeRef,"MP test report");
	//
	// //Check out
	// NodeRef workingCopyNodeRef =
	// checkOutCheckInService.checkout(rawMaterialNodeRef);
	//
	// final NodeRef finalWorkingCopy = workingCopyNodeRef;
	// AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>(){
	// @Override
	// public Void doWork() throws Exception
	// {
	// entityListDAO.copyDataLists(rawMaterialNodeRef, finalWorkingCopy, true);
	// return null;
	//
	// }
	// }, AuthenticationUtil.getSystemUserName());
	//
	// assertNotNull("Check working copy exists", workingCopyNodeRef);
	//
	// // Check productCode
	// //assertEquals("productCode should be the same after checkout",
	// nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
	// nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_CODE));
	//
	// //Check costs on working copy
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
	// ProductData workingCopyRawMaterial = productDAO.find(workingCopyNodeRef,
	// dataLists);
	// assertEquals("Check costs size", rawMaterial.getCostList().size(),
	// workingCopyRawMaterial.getCostList().size());
	//
	// for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
	// CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
	// CostListDataItem vCostListDataItem =
	// workingCopyRawMaterial.getCostList().get(i);
	//
	// assertEquals("Check cost", costListDataItem.getCost(),
	// vCostListDataItem.getCost());
	// assertEquals("Check cost unit", costListDataItem.getUnit(),
	// vCostListDataItem.getUnit());
	// assertEquals("Check cost value", costListDataItem.getValue(),
	// vCostListDataItem.getValue());
	// assertNotSame("Check cost noderef", costListDataItem.getNodeRef(),
	// vCostListDataItem.getNodeRef());
	// }
	//
	// //Modify working copy
	// int valueAdded = 1;
	// ProductUnit productUnit = ProductUnit.P;
	// workingCopyRawMaterial.setUnit(productUnit);
	// for(CostListDataItem c : workingCopyRawMaterial.getCostList()){
	// c.setValue(c.getValue() + valueAdded);
	// }
	// productDAO.update(workingCopyNodeRef, workingCopyRawMaterial, dataLists);
	//
	// //Check in
	// NodeRef newRawMaterialNodeRef = null;
	// try{
	//
	// NodeRef containerListNodeRef =
	// entityListDAO.getListContainer(rawMaterialNodeRef);
	// if(containerListNodeRef != null){
	// nodeService.deleteNode(containerListNodeRef);
	// }
	//
	// newRawMaterialNodeRef =
	// checkOutCheckInService.checkin(workingCopyNodeRef, null);
	// }
	// catch(Exception e){
	// logger.error("Failed to checkin", e);
	// assertNull(e);
	// }
	//
	// assertNotNull("Check new version exists", newRawMaterialNodeRef);
	// ProductData newRawMaterial = productDAO.find(newRawMaterialNodeRef,
	// dataLists);
	// //assertEquals("Check version", "1.1", newRawMaterial.getVersionLabel());
	// assertEquals("Check unit", productUnit, newRawMaterial.getUnit());
	//
	// // Check productCode
	// //assertEquals("productCode should be the same after checkin",
	// nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE),
	// nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_CODE));
	//
	// //Check costs on new version
	// assertEquals("Check costs size", rawMaterial.getCostList().size(),
	// newRawMaterial.getCostList().size());
	//
	// for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
	// CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
	// CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);
	//
	// assertEquals("Check cost", costListDataItem.getCost(),
	// vCostListDataItem.getCost());
	// assertEquals("Check cost unit", costListDataItem.getUnit(),
	// vCostListDataItem.getUnit());
	// assertEquals("Check cost value", costListDataItem.getValue() +
	// valueAdded, vCostListDataItem.getValue());
	// assertNotSame("Check cost noderef", costListDataItem.getNodeRef(),
	// vCostListDataItem.getNodeRef());
	// }
	//
	// assertEquals("Check products are the same", rawMaterialNodeRef,
	// newRawMaterialNodeRef);
	// return null;
	//
	// }},false,true);
	// }
}
