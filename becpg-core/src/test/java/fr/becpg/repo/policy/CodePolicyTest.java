/*
 * 
 */
package fr.becpg.repo.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.AutoNumService;

// TODO: Auto-generated Javadoc
/**
 * The Class CodePolicyTest.
 *
 * @author querephi
 */
public class CodePolicyTest  extends BaseAlfrescoTestCase  {
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";       
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CodePolicyTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;	
	
	/** The repository helper. */
	private Repository repositoryHelper;    
	
	/** The auto num service. */
	private AutoNumService autoNumService;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");  
    	authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
        repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
        autoNumService = (AutoNumService)appCtx.getBean("autoNumService"); 
                        
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
    * Test supplier code.
    */
   public void testSupplierCode(){
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
			
				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
				
				// delete autonum value
				autoNumService.deleteAutoNumValue(BeCPGModel.TYPE_SUPPLIER, BeCPGModel.PROP_CODE);
				
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				String name = "Supplier 1";
				properties.put(ContentModel.PROP_NAME, name);
				NodeRef supplier1NodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();								
				assertNotNull("Check supplier created", supplier1NodeRef);				
				long code1 = (Long)nodeService.getProperty(supplier1NodeRef, BeCPGModel.PROP_CODE);
				
				properties.clear();
				name = "Supplier 2";
				properties.put(ContentModel.PROP_NAME, name);
				NodeRef supplier2NodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();								
				assertNotNull("Check supplier created", supplier2NodeRef);				
				long code2 = (Long)nodeService.getProperty(supplier2NodeRef, BeCPGModel.PROP_CODE);
				
				// auto num defined not taken
				properties.clear();
				name = "Supplier 3";
				properties.put(ContentModel.PROP_NAME, name);
				properties.put(BeCPGModel.PROP_CODE, 3);
				NodeRef supplier3NodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();								
				assertNotNull("Check supplier created", supplier3NodeRef);				
				long code3 = (Long)nodeService.getProperty(supplier3NodeRef, BeCPGModel.PROP_CODE);
				
				// auto num defined but already taken
				properties.clear();
				name = "Supplier 4";
				properties.put(ContentModel.PROP_NAME, name);
				properties.put(BeCPGModel.PROP_CODE, 3);
				NodeRef supplier4NodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();								
				assertNotNull("Check supplier created", supplier4NodeRef);				
				long code4 = (Long)nodeService.getProperty(supplier4NodeRef, BeCPGModel.PROP_CODE);				
				
				assertEquals("Check code 1", 1, code1);
				assertEquals("Check code 2", 2, code2);
				assertEquals("Check code 3", 3, code3);
				assertEquals("Check code 4", 4, code4);
								
				return null;
				
			}},false,true);
	   
   }   
 	
}
