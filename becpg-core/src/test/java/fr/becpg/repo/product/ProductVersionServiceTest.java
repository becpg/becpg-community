/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.version.ProductVersionService;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
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
	
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The version service. */
	private VersionService versionService;
	
	/** The repository. */
	private Repository repository;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The product version service. */
	private ProductVersionService productVersionService;
	
	/** The product check out check in service. */
	private CheckOutCheckInService productCheckOutCheckInService;
	
	/** The costs. */
	private List<NodeRef> costs = new ArrayList<NodeRef>();
	
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
    	versionService = (VersionService)appCtx.getBean("VersionService");
    	repository = (Repository)appCtx.getBean("repositoryHelper");
    	productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
        productVersionService = (ProductVersionService)appCtx.getBean("productVersionService");
        productCheckOutCheckInService = (CheckOutCheckInService)appCtx.getBean("productCheckOutCheckInService");
    }
    
    
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {
		try
        {
            //authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Don't let this mask any previous exceptions
        }
        super.tearDown();

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
				
				NodeRef vRawMaterialNodeRefV1_0 = productVersionService.createVersion(rawMaterialNodeRef, null);
				
				String versionLabel = (String)nodeService.getProperty(vRawMaterialNodeRefV1_0, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel);
				assertEquals("1.0", versionLabel);
				
				List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(rawMaterialNodeRef, BeCPGModel.ASSOC_PRODUCTLISTS, BeCPGModel.ASSOC_PRODUCTLISTS);
				assertEquals("Has one child", 1, childAssocRefs.size());
				
				childAssocRefs = nodeService.getChildAssocs(vRawMaterialNodeRefV1_0);
				assertEquals("Has one child", 1, childAssocRefs.size());
				
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);				
				ProductData vRawMaterial = productDAO.find(vRawMaterialNodeRefV1_0, dataLists);
				
				assertEquals("Check costs size", rawMaterial.getCostList().size(), vRawMaterial.getCostList().size());
				
				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
					CostListDataItem vCostListDataItem = vRawMaterial.getCostList().get(i);
					
					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
					assertEquals("Check cost value", costListDataItem.getValue(), vCostListDataItem.getValue());
					assertNotSame("Check cost noderef", costListDataItem.getNodeRef(), vCostListDataItem.getNodeRef());
				}
				
				NodeRef vRawMaterialNodeRefV1_1 = productVersionService.createVersion(rawMaterialNodeRef, null);
				String versionLabel1_1 = (String)nodeService.getProperty(vRawMaterialNodeRefV1_1, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel1_1);
				assertEquals("1.1", versionLabel1_1);
				
				Map<String, Serializable> properties = new HashMap<String, Serializable>();
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.4");
				NodeRef vRawMaterialNodeRefV1_4 = productVersionService.createVersion(rawMaterialNodeRef, properties);
				String versionLabel1_4 = (String)nodeService.getProperty(vRawMaterialNodeRefV1_4, BeCPGModel.PROP_VERSION_LABEL);
				assertEquals("1.4", versionLabel1_4);
				
				NodeRef vRawMaterialNodeRefV1_3 = null;
				Exception exception = null;
				properties.clear();				
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.3");
				try{
					vRawMaterialNodeRefV1_3 = productVersionService.createVersion(rawMaterialNodeRef, properties);
				}
				catch(Exception e){
					exception = e;
					logger.debug(e.toString());
				}
				
				assertNotNull(exception);
				assertNull(vRawMaterialNodeRefV1_3);
				
				properties.clear();
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "2.0");
				NodeRef vRawMaterialNodeRefV2_0 = productVersionService.createVersion(rawMaterialNodeRef, properties);
				String versionLabel2_0 = (String)nodeService.getProperty(vRawMaterialNodeRefV2_0, BeCPGModel.PROP_VERSION_LABEL);
				assertEquals("2.0", versionLabel2_0);
				
				NodeRef rawMaterialNodeRefV1_5 = null;
				exception = null;
				properties.clear();				
				properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), "1.5");
				try{
				rawMaterialNodeRefV1_5 = productVersionService.createVersion(rawMaterialNodeRef, properties);
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
				NodeRef workingCopyNodeRef = productCheckOutCheckInService.checkout(rawMaterialNodeRef);
				assertNotNull("Check working copy exists", workingCopyNodeRef);				
				
				// Check productCode
				assertEquals("productCode should be the same after checkout", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_CODE), nodeService.getProperty(workingCopyNodeRef, BeCPGModel.PROP_PRODUCT_CODE));
				
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
				float newDensity = 2.33f;
				workingCopyRawMaterial.setDensity(newDensity);
				for(CostListDataItem c : workingCopyRawMaterial.getCostList()){
					c.setValue(c.getValue() + valueAdded);
				}
				productDAO.update(workingCopyNodeRef, workingCopyRawMaterial, dataLists);
				
				//Check in
				NodeRef newRawMaterialNodeRef = null;
				try{
					newRawMaterialNodeRef = productCheckOutCheckInService.checkin(workingCopyNodeRef, null);
				}
				catch(Exception e){
					logger.error("Failed to checkin", e);
					assertNull(e);
				}
				
				assertNotNull("Check new version exists", newRawMaterialNodeRef);
				ProductData newRawMaterial = productDAO.find(newRawMaterialNodeRef, dataLists);
				assertEquals("Check version", "1.0", newRawMaterial.getVersionLabel());
				assertEquals("Check density", newDensity, newRawMaterial.getDensity());
				
				// Check productCode
				assertEquals("productCode should be the same after checkin", nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_CODE), nodeService.getProperty(newRawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_CODE));
				
				//Check costs on new version				
				assertEquals("Check costs size", rawMaterial.getCostList().size(), newRawMaterial.getCostList().size());
				
				for(int i=0 ; i < rawMaterial.getCostList().size() ; i++){
					CostListDataItem costListDataItem = rawMaterial.getCostList().get(i);
					CostListDataItem vCostListDataItem = newRawMaterial.getCostList().get(i);
					
					assertEquals("Check cost", costListDataItem.getCost(), vCostListDataItem.getCost());
					assertEquals("Check cost unit", costListDataItem.getUnit(), vCostListDataItem.getUnit());
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
				NodeRef workingCopyNodeRef = productCheckOutCheckInService.checkout(rawMaterialNodeRef);
				assertNotNull("Check working copy exists", workingCopyNodeRef);
				
				//modify
				float newDensity2 = 4.33f;
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				ProductData workingCopyRawMaterial = productDAO.find(workingCopyNodeRef, dataLists);
				workingCopyRawMaterial.setDensity(newDensity2);
				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				assertNull("Check density", rawMaterial.getDensity());
				assertEquals("Check density", newDensity2, workingCopyRawMaterial.getDensity());
								
				//cancel check out
				productCheckOutCheckInService.cancelCheckout(workingCopyNodeRef);
				
				//Check
				rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				assertNull("Check density", rawMaterial.getDensity());
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
				NodeRef vRawMaterialNodeRef = productVersionService.createVersion(rawMaterialNodeRef, null);
				
				String versionLabel = (String)nodeService.getProperty(vRawMaterialNodeRef, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel);
				assertEquals("1.0", versionLabel);
							
				NodeRef vRawMaterialNodeRefV1_1 = productVersionService.createVersion(rawMaterialNodeRef, null);
				String versionLabel1_1 = (String)nodeService.getProperty(vRawMaterialNodeRefV1_1, BeCPGModel.PROP_VERSION_LABEL);
				logger.debug("version: " + versionLabel1_1);
				assertEquals("1.1", versionLabel1_1);
				
				List<NodeRef> versionHistory = productVersionService.getVersionHistory(rawMaterialNodeRef);
				assertEquals("Should have 2 versions", 2, versionHistory.size());
				assertEquals("Check 2st version", vRawMaterialNodeRef, versionHistory.get(0));
				assertEquals("Check 3st version", vRawMaterialNodeRefV1_1, versionHistory.get(1));
				
				return null;
				
			}},false,true);
	}
}
