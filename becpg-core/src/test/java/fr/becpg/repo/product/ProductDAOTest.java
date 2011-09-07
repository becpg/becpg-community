/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDAOTest.
 *
 * @author querephi
 */
public class ProductDAOTest  extends RepoBaseTestCase  {
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";       
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductDAOTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The search service. */
	private SearchService searchService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The ml node service impl. */
	private NodeService mlNodeServiceImpl;
	
	/** The product dao. */
	private ProductDAO productDAO;	
	
	/** The repository helper. */
	private Repository repositoryHelper;   
	
	private EntityListDAO entityListDAO;
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	searchService = (SearchService)appCtx.getBean("searchService");
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");
    	productDAO = (ProductDAO)appCtx.getBean("productDAO");
    	productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
        authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
        mlNodeServiceImpl = (NodeService) appCtx.getBean("mlAwareNodeService");
        repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");  
        entityListDAO = (EntityListDAO)appCtx.getBean("entityListDAO");
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {

 				deleteCharacteristics();
 				initCharacteristics();
 		        
 				return null;

 			}},false,true);  
                        
    }
    
    
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
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
    * Test allergen list dao.
    */
   public void testAllergenListDAO(){
	   
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
				
				// create RM
				RawMaterialData rmData = new RawMaterialData();
				rmData.setName("RM");								
				NodeRef rmNodeRef = productDAO.create(folderNodeRef, rmData, null);
				
				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<NodeRef> allSources = new ArrayList<NodeRef>();
				allSources.add(rmNodeRef);
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, allSources, null, allergens.get(0)));
				allergenList.add(new AllergenListDataItem(null, false, true, null, allSources, allergens.get(1)));
				allergenList.add(new AllergenListDataItem(null, true, false, null, allSources, allergens.get(2)));
				allergenList.add(new AllergenListDataItem(null, false, false, allSources, null, allergens.get(3)));
				sfData.setAllergenList(allergenList);
				
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				NodeRef sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);
				
				// load SF and test it
				sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);				
				assertNotNull("check allergenList", sfData.getAllergenList());
				
				for(AllergenListDataItem d : sfData.getAllergenList()){
					
					if(d.getAllergen().equals(allergens.get(0))){
						assertEquals(true, d.getVoluntary().booleanValue());
						assertEquals(true, d.getInVoluntary().booleanValue());
						assertEquals(1, d.getVoluntarySources().size());
						assertEquals(rmNodeRef, d.getVoluntarySources().get(0));
						assertEquals(true, d.getInVoluntarySources().isEmpty());
					}
					
					if(d.getAllergen().equals(allergens.get(1))){
						assertEquals(false, d.getVoluntary().booleanValue());
						assertEquals(true, d.getInVoluntary().booleanValue());
						assertEquals(true, d.getVoluntarySources().isEmpty());
						assertEquals(1, d.getInVoluntarySources().size());
						assertEquals(rmNodeRef, d.getInVoluntarySources().get(0));						
					}
					
					if(d.getAllergen().equals(allergens.get(2))){
						assertEquals(true, d.getVoluntary().booleanValue());
						assertEquals(false, d.getInVoluntary().booleanValue());
						assertEquals(true, d.getVoluntarySources().isEmpty());
						assertEquals(1, d.getInVoluntarySources().size());
						assertEquals(rmNodeRef, d.getInVoluntarySources().get(0));
					}
					
					if(d.getAllergen().equals(allergens.get(3))){
						assertEquals(false, d.getVoluntary().booleanValue());
						assertEquals(false, d.getInVoluntary().booleanValue());
						assertEquals(1, d.getVoluntarySources().size());
						assertEquals(rmNodeRef, d.getVoluntarySources().get(0));
						assertEquals(true, d.getInVoluntarySources().isEmpty());
					}
				}
				
				return null;
				
			}},false,true);
	   
   }
   
   /**
    * Test get link.
    */
   public void testGetLink(){
	   
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
				
				
				NodeRef rawMaterialNodeRef = createRawMaterial(folderNodeRef,"MP test report");				
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				ProductData rawMaterial = productDAO.find(rawMaterialNodeRef, dataLists);
				
				NodeRef costNodeRef = costs.get(3);
				
				NodeRef costListDataItemNodeRef = null;
				
				for(CostListDataItem c : rawMaterial.getCostList()){
					if(costNodeRef.equals(c.getCost())){
						costListDataItemNodeRef = c.getNodeRef();
					}
				}
				
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
				NodeRef nodeRef = entityListDAO.getLink(listNodeRef, BeCPGModel.ASSOC_COSTLIST_COST, costNodeRef);
				
				assertEquals("Cost list data item should be the same", costListDataItemNodeRef, nodeRef);
				
				return null;
				
			}},false,true);
	   
   }
   
   /**
    * Test ing labeling list.
    */
   public void testIngLabelingList(){	   	 
	   
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
				
				// create node
				MLText mlTextILL = new MLText();
				mlTextILL.addValue(Locale.ENGLISH, "English value");
				mlTextILL.addValue(Locale.FRENCH, "French value");					
				
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(BeCPGModel.PROP_ILL_GRP, "-");
	    		properties.put(BeCPGModel.PROP_ILL_VALUE, mlTextILL);
	    									
				NodeRef illNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, BeCPGModel.TYPE_INGLABELINGLIST, properties).getChildRef();
				
				nodeService.setProperty(illNodeRef, BeCPGModel.PROP_ILL_VALUE, mlTextILL);
								
				// check node saved
				logger.debug("get property : " + mlNodeServiceImpl.getProperty(illNodeRef, BeCPGModel.PROP_ILL_VALUE));
				logger.debug("get property fr : " + mlNodeServiceImpl.getProperty(illNodeRef, QName.createQName(BeCPGModel.BECPG_PREFIX, "illValue_fr")));
				logger.debug("get properties : " + mlNodeServiceImpl.getProperties(illNodeRef));
				logger.debug("get property 2 : " + mlNodeServiceImpl.getProperties(illNodeRef).get(BeCPGModel.PROP_ILL_VALUE));
				MLText mlTextILLSaved = (MLText)mlNodeServiceImpl.getProperty(illNodeRef, BeCPGModel.PROP_ILL_VALUE);
				
				
				assertNotNull("MLText exist", mlTextILLSaved);
				assertEquals("MLText exist has 2 Locales", 2, mlTextILL.getLocales().size());
				assertEquals("Check english value", mlTextILL.getValue(Locale.ENGLISH), mlTextILLSaved.getValue(Locale.ENGLISH));
				assertEquals("Check french value", mlTextILL.getValue(Locale.FRENCH), mlTextILLSaved.getValue(Locale.FRENCH));
				
				
				return null;
				
			}},false,true);
	   
   }
   
 	
 	
}
