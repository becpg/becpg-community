/*
 * 
 */
package fr.becpg.repo.web.scripts.document;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.RawMaterialData;

// TODO: Auto-generated Javadoc
/**
 * The Class VersionHistoryWebScriptTest.
 *
 * @author querephi
 */
public class VersionHistoryWebScriptTest extends BaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(VersionHistoryWebScriptTest.class);
	
	/** The Constant PATH_TEMPFOLDER. */
	private static final String PATH_TEMPFOLDER = "TempFolder";
	
	/** The Constant USER_ADMIN. */
	private static final String USER_ADMIN = "admin";
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The repository helper. */
	private Repository repositoryHelper;

    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;
    
    /** The product dao. */
    private ProductDAO productDAO;
    
    /** The transaction service. */
    private TransactionService transactionService;
    
    /** The product version service. */
    private EntityVersionService entityVersionService;
    
    private CheckOutCheckInService checkOutCheckInService;
    
	/** The raw material node ref. */
	private NodeRef rawMaterialNodeRef = null;
	
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
				
		nodeService = (NodeService) getServer().getApplicationContext().getBean("nodeService");
		fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("fileFolderService");		
		authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");
		productDAO = (ProductDAO) getServer().getApplicationContext().getBean("productDAO");
		transactionService = (TransactionService) getServer().getApplicationContext().getBean("transactionService");
		entityVersionService = (EntityVersionService) getServer().getApplicationContext().getBean("entityVersionService");
		repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
		checkOutCheckInService = (CheckOutCheckInService) getServer().getApplicationContext().getBean("checkOutCheckInService");
		
		
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
	 * Test get version history.
	 *
	 * @throws Exception the exception
	 */
	public void testGetVersionHistory() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					/*-- create folders --*/
					logger.debug("/*-- create folders --*/");			    	
			    	NodeRef tempFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);			
					if(tempFolder != null)
					{
						fileFolderService.delete(tempFolder);    		
					}			
					tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
							
			    	
			    	/*-- Create raw material --*/
	 				logger.debug("/*-- Create raw material --*/");
	 				RawMaterialData rawMaterial = new RawMaterialData();
	 				rawMaterial.setName("Raw material");
	 				rawMaterialNodeRef = productDAO.create(tempFolder, rawMaterial, null);	 					
	 				
	 				NodeRef checkedOutNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);
	 				NodeRef checkedInNodeRef = checkOutCheckInService.checkin(checkedOutNodeRef, null);
	 				
	 				NodeRef checkedOutNodeRef2 = checkOutCheckInService.checkout(checkedInNodeRef);
	 				checkOutCheckInService.checkin(checkedOutNodeRef2, null);
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material to check out
			String url = "/api/version?nodeRef=" + rawMaterialNodeRef;
			logger.debug("url : " + url);				

			Response response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("version history: " + response.getContentAsString());

    }
    	
}
