/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
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
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.RawMaterialData;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductListPoliciesTest.
 *
 * @author querephi
 */
public class ProductListPoliciesTest  extends BaseAlfrescoTestCase  {
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";       
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductListPoliciesTest.class);
	
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
	
	/** The repository helper. */
	private Repository repositoryHelper;    
	
	private EntityListDAO entityListDAO;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");  
    	productDAO = (ProductDAO)appCtx.getBean("productDAO");
        authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
        repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
        entityListDAO = (EntityListDAO)appCtx.getBean("entityListDAO");
                        
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
	 * simulate UI creation of datalist, we create a datalist with a GUID in the property name.
	 */
	   public void testGetList(){
		   
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
									
					RawMaterialData rawMaterialData = new RawMaterialData();
					rawMaterialData.setName("RM");
					NodeRef rawMaterialNodeRef = productDAO.create(folderNodeRef, rawMaterialData, null);											
					
		    		NodeRef containerListNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);
		    		
		    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(ContentModel.PROP_NAME, GUID.generate());
		    		properties.put(DataListModel.PROP_DATALISTITEMTYPE, BeCPGModel.BECPG_PREFIX + ":" + BeCPGModel.TYPE_COSTLIST.getLocalName());
		    		NodeRef costListCreatedNodeRef = nodeService.createNode(containerListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), DataListModel.TYPE_DATALIST, properties).getChildRef();
		    		
					NodeRef costListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(rawMaterialNodeRef), BeCPGModel.TYPE_COSTLIST);
					assertNotNull("cost list should exist", costListNodeRef);
					assertEquals("cost list should be the same", costListCreatedNodeRef, costListNodeRef);
					
					costListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(rawMaterialNodeRef), BeCPGModel.TYPE_COSTLIST);
					assertNotNull("cost list should exist", costListNodeRef);
					assertEquals("cost list should be the same", costListCreatedNodeRef, costListNodeRef);
					
					return null;
					
				}},false,true);
		   
	   }  
 	
}
