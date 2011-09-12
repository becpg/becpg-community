/*
 * 
 */
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.listvalue.ListValueService;

// TODO: Auto-generated Javadoc
/**
 * The Class ListValueServiceTest.
 *
 * @author querephi
 */
public class ListValueServiceTest extends BaseAlfrescoTestCase {

	/** The Constant PATH_TEMPFOLDER. */
	private static final String PATH_TEMPFOLDER = "TempFolder";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();	
	
	/** The list value service. */
	private ListValueService listValueService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The repo service. */
	private RepoService repoService;
	
	/** The repository helper. */
	private Repository repositoryHelper;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	listValueService = (ListValueService)appCtx.getBean("listValueService");
    	fileFolderService = (FileFolderService) appCtx.getBean("FileFolderService");
    	repoService = (RepoService)appCtx.getBean("repoService");
    	repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				logger.debug("create dollard");
 				
 				//create dollard currency
 				List<String> paths = new ArrayList<String>();
 				paths.add("System");
 				paths.add("Lists");
 				paths.add("Currencies"); 				
 				NodeRef currencyFolder = repoService.createFolderByPaths(repositoryHelper.getCompanyHome(), paths);
 				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
 				properties.put(ContentModel.PROP_NAME, "dollard");
 				NodeRef dollardNodeRef = nodeService.getChildByName(currencyFolder, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
 				if(dollardNodeRef == null){ 					 				
 					nodeService.createNode(currencyFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties).getChildRef();    	
 				}
 				
 				return null;

 			}},false,true);
	}
    
    
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
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
	 * Test product creation.
	 */
	public void testProductCreation(){
		
		logger.debug("look for temp folder");
		NodeRef tempFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);    	
    	if(tempFolder != null){
    		fileFolderService.delete(tempFolder);    		
    	}
    	tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

//    	//Create temp product 1 with allowed constraint
//    	logger.debug("create temp product 1");
//    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
//		properties.put(ContentModel.PROP_NAME, "Temp product");
//		properties.put(FoodModel.PROP_PRODUCT_TYPE, ProductType.RAW_MATERIAL_FR);
//		properties.put(FoodModel.PROP_PRODUCT_CURRENCY, "dollard");
//    	NodeRef productNodeRef = nodeService.createNode(tempFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), FoodModel.TYPE_PRODUCT, properties).getChildRef();
//    	
//    	//Create temp product 2 with non allowed constraint
//    	logger.debug("create temp product 2");
//    	properties = new HashMap<QName, Serializable>();
//		properties.put(ContentModel.PROP_NAME, "Temp product 2");
//		properties.put(FoodModel.PROP_PRODUCT_TYPE, ProductType.RAW_MATERIAL_FR);
//		properties.put(FoodModel.PROP_PRODUCT_CURRENCY, "dollard1");
//		
//		try
//		{
//			productNodeRef = null;
//			productNodeRef = nodeService.createNode(tempFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), FoodModel.TYPE_PRODUCT, properties).getChildRef();
//		}
//		catch(IntegrityException e)
//		{
//			assertNotNull(e);
//		}
//		
//		assertNull(productNodeRef);    	

	}
	
	/**
	 * Test suggest supplier.
	 */
	public void testSuggestSupplier(){
		
		logger.debug("look for temp folder");
		NodeRef tempFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);    	
    	if(tempFolder != null){
    		fileFolderService.delete(tempFolder);    		
    	}
    	tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

    	//Create supplier 1 with allowed constraint
    	logger.debug("create temp supplier 1");
    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, "Supplier 1");		
    	NodeRef supplierNodeRef = nodeService.createNode(tempFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();

    	Map<String, String> suggestions = listValueService.suggestTargetAssoc(BeCPGModel.TYPE_SUPPLIER, "Supplier 1");
    	
    	for(String s : suggestions.keySet()){
    		logger.debug("supplier: " + nodeService.getProperty(new NodeRef(s), ContentModel.PROP_NAME));
    	}
    	
    	assertEquals("1 suggestion", 1, suggestions.size());
    	assertTrue("check supplier key", suggestions.containsKey(supplierNodeRef.toString()));
    	assertTrue("check supplier value", suggestions.containsValue("Supplier 1"));
    	
    	
	}
	
}
