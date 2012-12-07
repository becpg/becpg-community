/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.repository.AlfrescoRepository;

// TODO: Auto-generated Javadoc
/**
 * The Class CheckOutCheckInWebScriptTest.
 *
 * @author querephi
 */
public class CheckOutCheckInWebScriptTest extends BaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(CheckOutCheckInWebScriptTest.class);
	
	/** The Constant USER_ADMIN. */
	private static final String USER_ADMIN = "admin";
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
    
    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;
    
    /** The product dao. */
    private AlfrescoRepository<ProductData> alfrescoRepository;
    
    /** The transaction service. */
    private TransactionService transactionService;
    
    /** The repository helper. */
    private Repository repositoryHelper;
    
    private NodeService nodeService;
    
	/** The temp folder. */
	private NodeRef tempFolder = null;
	
	/** The raw material node ref. */
	private NodeRef rawMaterialNodeRef = null;
	
	/** The working copy node ref. */
	private NodeRef workingCopyNodeRef = null;
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	
		fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("fileFolderService");		
		authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");
		alfrescoRepository = (AlfrescoRepository) getServer().getApplicationContext().getBean("alfrescoRepository");
		transactionService = (TransactionService) getServer().getApplicationContext().getBean("transactionService");
		repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
		nodeService = (NodeService) getServer().getApplicationContext().getBean("nodeService");
		
	    // Authenticate as user
	    this.authenticationComponent.setCurrentUser(USER_ADMIN);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}	
	
	/**
	 * Test faik method.
	 */
	public void testFaikMethod(){
		
	}
		
	/**
	 * Test check out check in entity.
	 *
	 * @throws Exception the exception
	 */
	public void testCheckOutCheckInProduct() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					/*-- create folders --*/
					logger.debug("/*-- create folders --*/");
			    	tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
			    	
			    	/*-- Create raw material --*/
	 				logger.debug("/*-- Create raw material --*/");
	 				RawMaterialData rawMaterial = new RawMaterialData();
	 				rawMaterial.setName("Raw material");
	 				rawMaterialNodeRef = alfrescoRepository.create(tempFolder, rawMaterial).getNodeRef();	 					 				 		 				
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material to check out
			String url = "/slingshot/doclib/action/checkout/node/" + rawMaterialNodeRef.toString().replace(":/", "");
			String data = "{}";
			logger.debug("url : " + url);				

			Response response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content checkout: " + response.getContentAsString());

			 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
					@Override
					public NodeRef execute() throws Throwable {					   
				
						NodeRef parentFolder = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
						List<FileInfo> files = fileFolderService.listFiles(parentFolder);
						
						for(FileInfo file : files){
							if(!file.getNodeRef().equals(rawMaterialNodeRef)){
								workingCopyNodeRef = file.getNodeRef();
							}
						}
						
						return null;

					}},false,true);
			 
			 assertNotNull("working copy should exist", workingCopyNodeRef);
			 
			 //Call webscript on raw material to check in
			 url = "/slingshot/doclib/action/checkin/node/" + workingCopyNodeRef.toString().replace(":/", "");			
			 logger.debug("url : " + url);				

			 response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			 logger.debug("content checkin: " + response.getContentAsString());

    }
	
	/**
	 * Test check out cancel check out entity.
	 *
	 * @throws Exception the exception
	 */
	public void testCheckOutCancelCheckOutProduct() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					/*-- create folders --*/
					logger.debug("/*-- create folders --*/");
			    	tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
			    	
			    	/*-- Create raw material --*/
	 				logger.debug("/*-- Create raw material --*/");
	 				RawMaterialData rawMaterial = new RawMaterialData();
	 				rawMaterial.setName("Raw material");
	 				rawMaterialNodeRef = alfrescoRepository.create(tempFolder, rawMaterial).getNodeRef();	 					 				 	
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material to check out
			String url = "/slingshot/doclib/action/checkout/node/" + rawMaterialNodeRef.toString().replace(":/", "");
			String data = "{}";
			logger.debug("url : " + url);				

			Response response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content checkout: " + response.getContentAsString());

			 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
					@Override
					public NodeRef execute() throws Throwable {					   
				
						NodeRef parentFolder = nodeService.getPrimaryParent(rawMaterialNodeRef).getParentRef();
						List<FileInfo> files = fileFolderService.listFiles(parentFolder);
						
						for(FileInfo file : files){
							if(!file.getNodeRef().equals(rawMaterialNodeRef)){
								workingCopyNodeRef = file.getNodeRef();
							}
						}
						
						
						return null;

					}},false,true);
			 
			 assertNotNull("working copy should exist", workingCopyNodeRef);
			
			 //Call webscript on raw material to cancel check out
			 url = "/slingshot/doclib/action/cancel-checkout/node/" + workingCopyNodeRef.toString().replace(":/", "");			
			 logger.debug("url : " + url);				

			 response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			 logger.debug("content checkin: " + response.getContentAsString());
   }
    	
}
