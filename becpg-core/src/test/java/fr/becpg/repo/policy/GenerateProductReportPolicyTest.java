/*
 * 
 */
package fr.becpg.repo.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

// TODO: Auto-generated Javadoc
/**
 * The Class GenerateProductReportPolicyTest.
 *
 * @author querephi
 */
public class GenerateProductReportPolicyTest extends BaseAlfrescoTestCase {

	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder"; 
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(GenerateProductReportPolicyTest.class);
	
	/** The repository helper. */
	private Repository repositoryHelper;    
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The product node ref. */
	private NodeRef productNodeRef;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("GenerateProductReportPolicyTest:setUp");
    
    	nodeService = (NodeService)ctx.getBean("nodeService");  
    	repositoryHelper = (Repository)ctx.getBean("repositoryHelper");
    	fileFolderService = (FileFolderService)ctx.getBean("fileFolderService");
    	                        
    }
    
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {
		try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Don't let this mask any previous exceptions
        }
        super.tearDown();

    }	
	
	/**
	 * Test product report generator.
	 */
	public void testProductReportGeneratorOnUpdateProperties(){
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
			
				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					nodeService.deleteNode(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
				
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				String name = "Product";
				properties.put(ContentModel.PROP_NAME, name);
				productNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), BeCPGModel.TYPE_PRODUCT, properties).getChildRef();								
				assertNotNull("Check product created", productNodeRef);									
								
				nodeService.setProperty(productNodeRef, ContentModel.PROP_NAME, "Product1");
				nodeService.setProperty(productNodeRef, ContentModel.PROP_NAME, "Product2");
				
				nodeService.setProperty(productNodeRef, ContentModel.PROP_DESCRIPTION, "descr");
				
				
				return null;
				
			}},false,true);
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
			
													
								
				nodeService.setProperty(productNodeRef, ContentModel.PROP_NAME, "Product1");
				nodeService.setProperty(productNodeRef, ContentModel.PROP_NAME, "Product2");
				
				nodeService.setProperty(productNodeRef, ContentModel.PROP_DESCRIPTION, "descr");
				
				
				return null;
				
			}},false,true);
	   
   }   
	
}
