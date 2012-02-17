/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 *
 * @author querephi
 */
public class ProductVersionServiceTest  extends RepoBaseTestCase{

	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductVersionServiceTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;	
	
	/** The product dao. */
	private ProductDAO productDAO;	
	
	/** The repository. */
	private Repository repository;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The product version service. */
	private EntityVersionService entityVersionService;
	
	private EntityListDAO entityListDAO;
	
	private CheckOutCheckInService checkOutCheckInService;
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");
    	productDAO = (ProductDAO)appCtx.getBean("productDAO");
    	repository = (Repository)appCtx.getBean("repositoryHelper");
    	productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
        entityVersionService = (EntityVersionService)appCtx.getBean("entityVersionService");
        entityListDAO = (EntityListDAO)appCtx.getBean("entityListDAO");
        checkOutCheckInService = (CheckOutCheckInService)appCtx.getBean("checkOutCheckInService");
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {

 				initCharacteristics();
 		        
 				return null;

 			}},false,true); 
    }
    	
	/**
	 * Test create version.
	 */
	public void testCreateVersion(){
 		
 		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
			
				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
								
				NodeRef rawMaterialNodeRef = createRawMaterial(folderNodeRef,"MP test report");
				
				NodeRef vRawMaterialNodeRefV1_1 = entityVersionService.createVersion(rawMaterialNodeRef, null);
				
				String versionLabel = (String)nodeService.getProperty(vRawMaterialNodeRefV1_1, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel);
				assertEquals("1.1", versionLabel);
				
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);
				assertNotNull("Has list container", listContainerNodeRef);
				
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);				
				ProductData vRawMaterial = productDAO.find(vRawMaterialNodeRefV1_1, dataLists);
				
				assertEquals("Check costs size", rawMaterial.getCostList().size(), vRawMaterial.getCostList().size());
				
				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
					CostListDataItem vCostListDataItem = vRawMaterial.getCostList().get(i);
					
					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
					assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
				}
				
				NodeRef vRawMaterialNodeRefV1_2 = entityVersionService.createVersion(rawMaterialNodeRef, null);
				String versionLabel1_2 = (String)nodeService.getProperty(vRawMaterialNodeRefV1_2, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel1_2);
				assertEquals("1.2", versionLabel1_2);
				
				Map<String, Serializable> properties = new HashMap<String, Serializable>();
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.4");
				NodeRef vRawMaterialNodeRefV1_4 = entityVersionService.createVersion(rawMaterialNodeRef, properties);
				String versionLabel1_4 = (String)nodeService.getProperty(vRawMaterialNodeRefV1_4, BeCPGModel.PROP_VERSION_LABEL);
				assertEquals("1.4", versionLabel1_4);
				
				NodeRef vRawMaterialNodeRefV1_3 = null;
				Exception exception = null;
				properties.clear();				
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.3");
				try{
					vRawMaterialNodeRefV1_3 = entityVersionService.createVersion(rawMaterialNodeRef, properties);
				}
				catch(Exception e){
					exception = e;
					logger.debug(e.toString());
				}
				
				assertNotNull(exception);
				assertNull(vRawMaterialNodeRefV1_3);
				
				properties.clear();
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "2.0");
				NodeRef vRawMaterialNodeRefV2_0 = entityVersionService.createVersion(rawMaterialNodeRef, properties);
				String versionLabel2_0 = (String)nodeService.getProperty(vRawMaterialNodeRefV2_0, BeCPGModel.PROP_VERSION_LABEL);
				assertEquals("2.0", versionLabel2_0);
				
				NodeRef rawMaterialNodeRefV1_5 = null;
				exception = null;
				properties.clear();				
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.5");
				try{
				rawMaterialNodeRefV1_5 = entityVersionService.createVersion(rawMaterialNodeRef, properties);
				}
				catch(Exception e){
					exception = e;
					logger.debug(e.toString());
				}
				
				assertNotNull(exception);
				assertNull(rawMaterialNodeRefV1_5);
				
				return null;
				
			}},false,true);
 		
 	}
	
	/**
	 * Test check out check in.
	 */
	public void testCheckOutCheckIn(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				/*-- create folders --*/
				logger.debug("/*-- create folders --*/");
				NodeRef folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
					
		    	
		    	/*-- Create raw material --*/
				NodeRef rawMaterialNodeRef = createRawMaterial(folderNodeRef,"MP test report");
				
				//Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
				assertNotNull("Check working copy exists", workingCopyNodeRef);				
				
				// Check productCode
				assertEquals("productCode should be the same after checkout", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE), nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_CODE));
				
				//Check costs on working copy
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);				
				ProductData workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);
				assertEquals("Check costs size", rawMaterial.getCostList().size(), workingCopyRawMaterial.getCostList().size());
				
				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
					CostListDataItem vCostListDataItem = workingCopyRawMaterial.getCostList().get(i);
					
					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
					assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
				}
				
				//Modify working copy
				int valueAdded = 1;
				ProductUnit productUnit = ProductUnit.P;
				workingCopyRawMaterial.setUnit(productUnit);
				for(CostListDataItem c : workingCopyRawMaterial.getCostList()){
					c.setValue(c.getValue() + valueAdded);
				}
				productDAO.update(workingCopyNodeRef, workingCopyRawMaterial, dataLists);
				
				//Check in
				NodeRef newRawMaterialNodeRef = null;
				try{
					newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, null);
				}
				catch(Exception e){
					logger.error("Failed to checkin", e);
					assertNull(e);
				}
				
				assertNotNull("Check new version exists", newRawMaterialNodeRef);
				ProductData newRawMaterial = productDAO.find(newRawMaterialNodeRef, dataLists);
				assertEquals("Check version", "1.1", newRawMaterial.getVersionLabel());
				assertEquals("Check unit", productUnit, newRawMaterial.getUnit());
				
				// Check productCode
				assertEquals("productCode should be the same after checkin", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE), nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_CODE));
				
				//Check costs on new version				
				assertEquals("Check costs size", rawMaterial.getCostList().size(), newRawMaterial.getCostList().size());
				
				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
					CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);
					
					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
					assertEquals("Check cost unit", costListDataItem.getUnit().replace(ProductUnit.kg.toString(), ProductUnit.P.toString()), vCostListDataItem.getUnit());
					assertEquals("Check cost value", costListDataItem.getValue()  + valueAdded, vCostListDataItem.getValue());
					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
				}
				
				assertEquals("Check products are the same", rawMaterialNodeRef, newRawMaterialNodeRef);			
			return null;
			
			}},false,true);
	}
	
	/**
	 * Test cancel check out.
	 */
	public void testCancelCheckOut(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				/*-- create folders --*/
				logger.debug("/*-- create folders --*/");
				NodeRef folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
					
		    	
		    	/*-- Create raw material --*/
				NodeRef rawMaterialNodeRef = createRawMaterial(folderNodeRef,"MP test report");								
				
				//Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
				assertNotNull("Check working copy exists", workingCopyNodeRef);
				
				//modify
				ProductUnit productUnit2 = ProductUnit.m;
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				ProductData workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);
				workingCopyRawMaterial.setUnit(productUnit2);
				productDAO.update(workingCopyNodeRef, workingCopyRawMaterial, dataLists);
				workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);
				
				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				assertEquals("Check unit", ProductUnit.kg, rawMaterial.getUnit());
				assertEquals("Check unit", productUnit2, workingCopyRawMaterial.getUnit());
								
				//cancel check out
				checkOutCheckInService.cancelCheckout(workingCopyNodeRef);
				
				//Check
				rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				assertEquals("Check unit", ProductUnit.kg, rawMaterial.getUnit());
			return null;
			
			}},false,true);
	}
    

	/**
	 * Test get version history.
	 */
	public void testGetVersionHistory(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
			
				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
								
				NodeRef rawMaterialNodeRef = createRawMaterial(folderNodeRef,"MP test report");										
				NodeRef vRawMaterialNodeRef = entityVersionService.createVersion(rawMaterialNodeRef, null);
				
				String versionLabel = (String)nodeService.getProperty(vRawMaterialNodeRef, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel);
				assertEquals("1.1", versionLabel);
							
				NodeRef vRawMaterialNodeRefV1_2 = entityVersionService.createVersion(rawMaterialNodeRef, null);
				String versionLabel1_2 = (String)nodeService.getProperty(vRawMaterialNodeRefV1_2, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel1_2);
				assertEquals("1.2", versionLabel1_2);
				
				List<NodeRef> versionHistory = entityVersionService.getVersionHistory(rawMaterialNodeRef);
				assertEquals("Should have 2 versions", 3, versionHistory.size());
				assertEquals("Check 2st version", vRawMaterialNodeRef, versionHistory.get(1));
				assertEquals("Check 3st version", vRawMaterialNodeRefV1_2, versionHistory.get(2));
				
				return null;
				
			}},false,true);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	TEST WITH ALFRESCO API AND VERSION STORE
	// 
	//  return 
	//	java.lang.UnsupportedOperationException: This operation is not supported by a version store implementation of the node service.
	//	at org.alfresco.repo.version.NodeServiceImpl.getChildByName(NodeServiceImpl.java:606)
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
//	/**
//	 * Test create version.
//	 */
//	public void xxtestCreateVersion2(){
// 		
// 		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
//			@Override
//			public NodeRef execute() throws Throwable {
//			
//				/*-- Create test folder --*/
//				NodeRef folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
//				if(folderNodeRef != null)
//				{
//					fileFolderService.delete(folderNodeRef);    		
//				}			
//				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
//								
//				NodeRef rawMaterialNodeRef = createRawMaterial(folderNodeRef,"MP test report");
//				
//				Version vRawMaterialNodeRefV1_1 = versionService.createVersion(rawMaterialNodeRef, null);
//				
//				String versionLabel = vRawMaterialNodeRefV1_1.getVersionLabel();
//				logger.debug("version: " + versionLabel);
//				assertEquals("0.1", versionLabel);
//				
//				NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);
//				assertNotNull("Has list container", listContainerNodeRef);
//				
//				Collection<QName> dataLists = productDictionaryService.getDataLists();
//				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);				
//				ProductData vRawMaterial = productDAO.find(vRawMaterialNodeRefV1_1.getFrozenStateNodeRef(), dataLists);
//				
//				logger.info("###rawMaterialNodeRef: " + rawMaterialNodeRef);
//				logger.info("###vRawMaterialNodeRefV1_1.getVersionedNodeRef(): " + vRawMaterialNodeRefV1_1.getVersionedNodeRef());
//				logger.info("###vRawMaterialNodeRefV1_1.getFrozenStateNodeRef(): " + vRawMaterialNodeRefV1_1.getFrozenStateNodeRef());
//				
//				assertEquals("Check costs size", rawMaterial.getCostList().size(), vRawMaterial.getCostList().size());
//				
//				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
//					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
//					CostListDataItem vCostListDataItem = vRawMaterial.getCostList().get(i);
//					
//					logger.info("###costListDataItem.getNodeRef(): " + costListDataItem.getNodeRef());
//					logger.info("###vCostListDataItem.getNodeRef(): " + vCostListDataItem.getNodeRef());
//					
//					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
//					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
//					assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
//					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
//				}
//				
//				Version vRawMaterialNodeRefV1_2 = versionService.createVersion(rawMaterialNodeRef, null);
//				String versionLabel1_2 = vRawMaterialNodeRefV1_2.getVersionLabel();
//				logger.debug("version: " + versionLabel1_2);
//				assertEquals("0.2", versionLabel1_2);
//				
//				Map<String, Serializable> properties = new HashMap<String, Serializable>();
//				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "0.4");
//				Version vRawMaterialNodeRefV1_4 = versionService.createVersion(rawMaterialNodeRef, properties);
//				String versionLabel1_4 = vRawMaterialNodeRefV1_4.getVersionLabel();
//				assertEquals("0.4", versionLabel1_4);
//				
//				Version vRawMaterialNodeRefV1_3 = null;
//				Exception exception = null;
//				properties.clear();				
//				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "0.3");
//				try{
//					vRawMaterialNodeRefV1_3 = versionService.createVersion(rawMaterialNodeRef, properties);
//				}
//				catch(Exception e){
//					exception = e;
//					logger.debug(e.toString());
//				}
//				
//				assertNotNull(exception);
//				assertNull(vRawMaterialNodeRefV1_3);
//				
//				properties.clear();
//				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "2.0");
//				Version vRawMaterialNodeRefV2_0 = versionService.createVersion(rawMaterialNodeRef, properties);
//				String versionLabel2_0 = vRawMaterialNodeRefV2_0.getVersionLabel();
//				assertEquals("2.0", versionLabel2_0);
//				
//				Version rawMaterialNodeRefV1_5 = null;
//				exception = null;
//				properties.clear();				
//				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.5");
//				try{
//				rawMaterialNodeRefV1_5 = versionService.createVersion(rawMaterialNodeRef, properties);
//				}
//				catch(Exception e){
//					exception = e;
//					logger.debug(e.toString());
//				}
//				
//				assertNotNull(exception);
//				assertNull(rawMaterialNodeRefV1_5);
//				
//				return null;
//				
//			}},false,true);
// 		
// 	}
//	
//	/**
//	 * Test check out check in.
//	 */
//	public void xxtestCheckOutCheckIn2(){
//		
//		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
//			@Override
//			public NodeRef execute() throws Throwable {
//				
//				/*-- create folders --*/
//				logger.debug("/*-- create folders --*/");
//				NodeRef folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
//				if(folderNodeRef != null)
//				{
//					fileFolderService.delete(folderNodeRef);    		
//				}			
//				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
//					
//		    	
//		    	/*-- Create raw material --*/
//				final NodeRef rawMaterialNodeRef = createRawMaterial(folderNodeRef,"MP test report");
//				
//				//Check out
//				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
//				
//				final NodeRef finalWorkingCopy = workingCopyNodeRef;
//				AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>(){
//			         @Override
//					public Void doWork() throws Exception
//			         {      
//			         	entityListDAO.copyDataLists(rawMaterialNodeRef, finalWorkingCopy, true);
//			         	return null;
//			         	
//			         }
//			     }, AuthenticationUtil.getSystemUserName());
//				
//				assertNotNull("Check working copy exists", workingCopyNodeRef);				
//				
//				// Check productCode
//				//assertEquals("productCode should be the same after checkout", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE), nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_CODE));
//				
//				//Check costs on working copy
//				Collection<QName> dataLists = productDictionaryService.getDataLists();
//				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);				
//				ProductData workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);
//				assertEquals("Check costs size", rawMaterial.getCostList().size(), workingCopyRawMaterial.getCostList().size());
//				
//				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
//					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
//					CostListDataItem vCostListDataItem = workingCopyRawMaterial.getCostList().get(i);
//					
//					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
//					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
//					assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
//					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
//				}
//				
//				//Modify working copy
//				int valueAdded = 1;
//				ProductUnit productUnit = ProductUnit.P;
//				workingCopyRawMaterial.setUnit(productUnit);
//				for(CostListDataItem c : workingCopyRawMaterial.getCostList()){
//					c.setValue(c.getValue() + valueAdded);
//				}
//				productDAO.update(workingCopyNodeRef, workingCopyRawMaterial, dataLists);
//				
//				//Check in
//				NodeRef newRawMaterialNodeRef = null;
//				try{
//					
//					NodeRef containerListNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);
//					if(containerListNodeRef != null){
//						nodeService.deleteNode(containerListNodeRef);
//					}
//					
//					newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, null);
//				}
//				catch(Exception e){
//					logger.error("Failed to checkin", e);
//					assertNull(e);
//				}
//								
//				assertNotNull("Check new version exists", newRawMaterialNodeRef);
//				ProductData newRawMaterial = productDAO.find(newRawMaterialNodeRef, dataLists);
//				//assertEquals("Check version", "1.1", newRawMaterial.getVersionLabel());
//				assertEquals("Check unit", productUnit, newRawMaterial.getUnit());
//				
//				// Check productCode
//				//assertEquals("productCode should be the same after checkin", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_CODE), nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_CODE));
//				
//				//Check costs on new version				
//				assertEquals("Check costs size", rawMaterial.getCostList().size(), newRawMaterial.getCostList().size());
//				
//				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
//					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
//					CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);
//					
//					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
//					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
//					assertEquals("Check cost value", costListDataItem.getValue()  + valueAdded, vCostListDataItem.getValue());
//					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
//				}
//				
//				assertEquals("Check products are the same", rawMaterialNodeRef, newRawMaterialNodeRef);			
//			return null;
//			
//			}},false,true);
//	}
}
