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
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
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
	

	
	/** The ml node service impl. */
	private NodeService mlNodeServiceImpl;
	
	/** The product dao. */
	private ProductDAO productDAO;	
	
	
	private EntityListDAO entityListDAO;
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	
    	productDAO = (ProductDAO)ctx.getBean("productDAO");
        mlNodeServiceImpl = (NodeService) ctx.getBean("mlAwareNodeService");
        entityListDAO = (EntityListDAO)ctx.getBean("entityListDAO");

                        
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
			
				// create RM
				RawMaterialData rmData = new RawMaterialData();
				rmData.setName("RM");								
				NodeRef rmNodeRef = productDAO.create(testFolderNodeRef, rmData, null);
				
				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<NodeRef> allSources = new ArrayList<NodeRef>();
				allSources.add(rmNodeRef);
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, allSources, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, allSources, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, allSources, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, allSources, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);
				
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				NodeRef sfNodeRef = productDAO.create(testFolderNodeRef, sfData, dataLists);
				
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
   public void testGetListItem(){
	   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
							
				NodeRef rawMaterialNodeRef = createRawMaterial(testFolderNodeRef,"MP test report");				
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
				NodeRef nodeRef = entityListDAO.getListItem(listNodeRef, BeCPGModel.ASSOC_COSTLIST_COST, costNodeRef);
				
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
			
				// create node
				MLText mlTextILL = new MLText();
				mlTextILL.addValue(Locale.ENGLISH, "English value");
				mlTextILL.addValue(Locale.FRENCH, "French value");					
				
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(BeCPGModel.PROP_ILL_VALUE, mlTextILL);
	    									
				NodeRef illNodeRef = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, BeCPGModel.TYPE_INGLABELINGLIST, properties).getChildRef();
				
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
