/*
 * 
 */
package fr.becpg.repo.web.scripts.listvalue;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.RawMaterialData;

// TODO: Auto-generated Javadoc
/**
 * The Class AutoCompleteWebScriptTest.
 *
 * @author querephi
 */
public class AutoCompleteWebScriptTest extends BaseWebScriptTest  {
	
	private static String PATH_PRODUCTFOLDER = "TestProductFolder";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AutoCompleteWebScriptTest.class);
	
	
	private NodeService nodeService;
	
	private ProductDAO productDAO;
	
	private FileFolderService fileFolderService;
	
	private Repository repositoryHelper;
	
	private TransactionService transactionService;
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{	
		nodeService = (NodeService) getServer().getApplicationContext().getBean("nodeService");
		productDAO = (ProductDAO)  getServer().getApplicationContext().getBean("productDAO");
		fileFolderService = (FileFolderService)  getServer().getApplicationContext().getBean("fileFolderService");
		repositoryHelper = (Repository)  getServer().getApplicationContext().getBean("repositoryHelper");
		transactionService = (TransactionService) getServer().getApplicationContext().getBean("transactionService");
		
		super.setUp();		
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	private void initProduct(){
		
		NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_PRODUCTFOLDER);			
		if(folderNodeRef != null)
		{
			nodeService.deleteNode(folderNodeRef);    		
		}			
		folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_PRODUCTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
		
		RawMaterialData rawMaterial1 = new RawMaterialData();
		rawMaterial1.setName("Raw material 1");
		
		productDAO.create(folderNodeRef, rawMaterial1, null);
	}
	
	/**
	 * Test suggest target assoc.
	 *
	 * @throws Exception the exception
	 */
	public void testSuggestTargetAssoc() throws Exception {
						
		String url = "/becpg/autocomplete/targetassoc/associations/bcpg:nut?q=pro";
		
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
		
		url = "/becpg/autocomplete/targetassoc/associations/bcpg:nut?q=nut11";
		
		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
	
    }
	
	/**
	 * Test suggest list value.
	 *
	 * @throws Exception the exception
	 */
	public void testSuggestListValue() throws Exception {
				
		String url = "/becpg/autocomplete/listvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=f";
		
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestListValue content : " + response.getContentAsString());		
		
		url = "/becpg/autocomplete/listvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=F";
		
		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestListValue content : " + response.getContentAsString());
    }
	
	/**
	 * Test suggest linked values.
	 *
	 * @throws Exception the exception
	 */
	public void testSuggestLinkedValues() throws Exception {
		
		String url = "/becpg/autocomplete/linkedvalue/values/System/ProductHierarchy/RawMaterial_Hierarchy1?q=s&parent=Fam4";
		
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestLinkedValues : " + response.getContentAsString());
				
    }
    
	/**
	 * Test suggest product.
	 *
	 * @throws Exception the exception
	 */
	public void testSuggestProduct() throws Exception {
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				initProduct();
 				
 				return null;

 			}},false,true);
		
		
		String url = "/becpg/autocomplete/product?q=ra";					
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());
		
		url = "/becpg/autocomplete/product?classNames=bcpg:rawMaterial,bcpg:finishedProduct,bcpg:localSemiFinishedProduct,bcpg:semiFinishedProduct&q=ra";		
		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());
		
		url = "/becpg/autocomplete/product?classNames=bcpg:packagingMaterial&q=ra";		
		response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testSuggestProduct : " + response.getContentAsString());
    }
	
	/**
	 * Test product report tpls.
	 *
	 * @throws Exception the exception
	 */
	public void testProductReportTpls() throws Exception {		
		
		String url = "/becpg/autocomplete/productreport/reports/SemiFinishedProduct?q=u";
		
		Response response = sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("testProductReportTpls : " + response.getContentAsString());
    }

}
