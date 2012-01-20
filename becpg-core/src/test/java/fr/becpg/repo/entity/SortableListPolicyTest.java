/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.search.BeCPGSearchService;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class SortableListPolicyTest.
 *
 * @author querephi
 */
public class SortableListPolicyTest extends RepoBaseTestCase {

	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";    
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(SortableListPolicyTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;	
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The repository helper. */
	private Repository repositoryHelper;
	
	private EntityListDAO entityListDAO;
	
	private BeCPGSearchService beCPGSearchService;
	
	private DictionaryService dictionaryService;
	
	/** The sf node ref. */
	private NodeRef sfNodeRef;
	
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
    	productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
    	repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
    	entityListDAO = (EntityListDAO)appCtx.getBean("entityListDAO");
    	beCPGSearchService = (BeCPGSearchService)appCtx.getBean("beCPGSearchService");
    	dictionaryService = (DictionaryService)appCtx.getBean("dictionaryService");
    	
    	transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {

 				deleteReportTpls();
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
        super.tearDown();
    }			

	/**
	 * Create a list item and check initialization
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	public void testInitSort() throws InterruptedException{		   		
	   	
		logger.debug("testChangeSortListItem()");
		
		// create product
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
								
				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(0), false));
				costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(1), false));
				costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(2), false));
				costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(3), false));
				sfData.setCostList(costList);
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
				
				// simulate the UI
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
				
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    	ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), BeCPGModel.TYPE_COSTLIST, properties);
		    	nodeService.createAssociation(childAssocRef.getChildRef(), costs.get(3), BeCPGModel.ASSOC_COSTLIST_COST);
		    	
		    	childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), BeCPGModel.TYPE_COSTLIST, properties);
		    	nodeService.createAssociation(childAssocRef.getChildRef(), costs.get(3), BeCPGModel.ASSOC_COSTLIST_COST);
		    	
		    	// load SF and test it
				sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);				
				
		    	
		    	assertEquals("Check cost order", 1, nodeService.getProperty(sfData.getCostList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 2, nodeService.getProperty(sfData.getCostList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 3, nodeService.getProperty(sfData.getCostList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 4, nodeService.getProperty(sfData.getCostList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 5, nodeService.getProperty(sfData.getCostList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 6, nodeService.getProperty(sfData.getCostList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));
		    	
				return null;
				
			}},false,true);	   				
	   
	}
	
//	/**
//	 * change sort, 4 to 2
//	 *
//	 * @throws InterruptedException the interrupted exception
//	 */
//	public void testChangeSortDecrease() throws InterruptedException{		   		
//	   	
//		logger.debug("testChangeSortListItem()");
//		
//		// create product
//		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
//			@Override
//			public NodeRef execute() throws Throwable {
//							   	
//				/*-- Create test folder --*/
//				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
//				if(folderNodeRef != null)
//				{
//					fileFolderService.delete(folderNodeRef);    		
//				}			
//				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
//				
//				// create SF
//				SemiFinishedProductData sfData = new SemiFinishedProductData();
//				sfData.setName("SF");
//				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
//				costList.add(new CostListDataItem(null, 3f, "€/kg", costs.get(0)));
//				costList.add(new CostListDataItem(null, 2f, "€/kg", costs.get(1)));
//				costList.add(new CostListDataItem(null, 3f, "€/kg", costs.get(2)));
//				costList.add(new CostListDataItem(null, 2f, "€/kg", costs.get(3)));
//				sfData.setCostList(costList);
//				Collection<QName> dataLists = productDictionaryService.getDataLists();
//				sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
//
//				// load SF and test it
//				sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
//				
//				assertNotNull("Check costs exist", sfData.getCostList());
//				assertEquals("Check cost order", costs.get(0), sfData.getCostList().get(0).getCost());
//				assertEquals("Check cost order", costs.get(1), sfData.getCostList().get(1).getCost());
//				assertEquals("Check cost order", costs.get(2), sfData.getCostList().get(2).getCost());
//				assertEquals("Check cost order", costs.get(3), sfData.getCostList().get(3).getCost());
//				
//				// change sort pos4 => pos2
//				nodeService.setProperty(sfData.getCostList().get(3).getNodeRef(), BeCPGModel.PROP_SORT, 2);
//				
//				sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
//				
//				assertNotNull("Check costs exist", sfData.getCostList());
//				assertEquals("Check cost order", costs.get(0), sfData.getCostList().get(0).getCost());
//				assertEquals("Check cost order", costs.get(1), sfData.getCostList().get(1).getCost());
//				assertEquals("Check cost order", costs.get(2), sfData.getCostList().get(2).getCost());
//				assertEquals("Check cost order", costs.get(3), sfData.getCostList().get(3).getCost());
//				
//				assertEquals("Check cost order", 1, nodeService.getProperty(sfData.getCostList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
//				assertEquals("Check cost order", 3, nodeService.getProperty(sfData.getCostList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
//				assertEquals("Check cost order", 4, nodeService.getProperty(sfData.getCostList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
//				assertEquals("Check cost order", 2, nodeService.getProperty(sfData.getCostList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
//				
//				return null;
//				
//			}},false,true);	   				
//	   
//	}	
//	
//	/**
//	 * change sort, 2 to 4
//	 *
//	 * @throws InterruptedException the interrupted exception
//	 */
//	public void testChangeSortIncrease() throws InterruptedException{		   		
//	   	
//		logger.debug("testChangeSortListItem()");
//		
//		// create product
//		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
//			@Override
//			public NodeRef execute() throws Throwable {
//							   	
//				/*-- Create test folder --*/
//				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
//				if(folderNodeRef != null)
//				{
//					fileFolderService.delete(folderNodeRef);    		
//				}			
//				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
//				
//				// create SF
//				SemiFinishedProductData sfData = new SemiFinishedProductData();
//				sfData.setName("SF");
//				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
//				costList.add(new CostListDataItem(null, 3f, "€/kg", costs.get(0)));
//				costList.add(new CostListDataItem(null, 2f, "€/kg", costs.get(1)));
//				costList.add(new CostListDataItem(null, 3f, "€/kg", costs.get(2)));
//				costList.add(new CostListDataItem(null, 2f, "€/kg", costs.get(3)));
//				sfData.setCostList(costList);
//				Collection<QName> dataLists = productDictionaryService.getDataLists();
//				sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
//
//				// load SF and test it
//				sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
//				
//				assertNotNull("Check costs exist", sfData.getCostList());
//				assertEquals("Check cost order", costs.get(0), sfData.getCostList().get(0).getCost());
//				assertEquals("Check cost order", costs.get(1), sfData.getCostList().get(1).getCost());
//				assertEquals("Check cost order", costs.get(2), sfData.getCostList().get(2).getCost());
//				assertEquals("Check cost order", costs.get(3), sfData.getCostList().get(3).getCost());
//				
//				// change sort pos2 => pos4
//				nodeService.setProperty(sfData.getCostList().get(1).getNodeRef(), BeCPGModel.PROP_SORT, 4);
//				
//				sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
//				
//				assertNotNull("Check costs exist", sfData.getCostList());
//				assertEquals("Check cost order", costs.get(0), sfData.getCostList().get(0).getCost());
//				assertEquals("Check cost order", costs.get(1), sfData.getCostList().get(1).getCost());
//				assertEquals("Check cost order", costs.get(2), sfData.getCostList().get(2).getCost());
//				assertEquals("Check cost order", costs.get(3), sfData.getCostList().get(3).getCost());
//				
//				assertEquals("Check cost order", 1, nodeService.getProperty(sfData.getCostList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
//				assertEquals("Check cost order", 4, nodeService.getProperty(sfData.getCostList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
//				assertEquals("Check cost order", 2, nodeService.getProperty(sfData.getCostList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
//				assertEquals("Check cost order", 3, nodeService.getProperty(sfData.getCostList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
//				
//				return null;
//				
//			}},false,true);	   				
//	   
//	}	
	
//	/**
//	 * Test the fastest method to listFiles, getChildAssocs is the fastest, lucene search is the slowest
//	 *
//	 * @throws InterruptedException the interrupted exception
//	 */
//	public void testPerfLoad() throws InterruptedException{		   		
//	   	
//		logger.debug("testChangeSortListItem()");
//		
//		// create product
//		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
//			@Override
//			public NodeRef execute() throws Throwable {
//				
//				int [] arrFiles = {1, 5, 20, 200, 1000};
//				
//				/*
//				 * Use listFiles
//				 */
//				for(int nbFiles : arrFiles){
//				
//					/*-- Create test folder --*/
//					NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
//					if(folderNodeRef != null)
//					{
//						fileFolderService.delete(folderNodeRef);    		
//					}			
//					folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
//					
//					// create SF
//					SemiFinishedProductData sfData = new SemiFinishedProductData();
//					sfData.setName("SF");
//					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
//					
//					for(int z_idx=0 ; z_idx<nbFiles ; z_idx++){
//						
//						costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(0), false));
//						costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(1), false));
//						costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(2), false));
//						costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(3), false));
//					}
//					
//					sfData.setCostList(costList);
//					Collection<QName> dataLists = productDictionaryService.getDataLists();
//					sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
//
//					// load SF and test it
//					sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
//					
//					NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
//					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
//					
//					StopWatch watch = new StopWatch();
//					watch.start();				
//					
//					List<FileInfo> files = fileFolderService.listFiles(listNodeRef);
//					
//					watch.stop();
//					logger.debug("listFiles nb files: " + files.size() + " - executed in  " + watch.getTotalTimeSeconds() + " seconds");
//				}
//				
//				/*
//				 * Use lucene search
//				 */
//				
//				for(int nbFiles : arrFiles){
//					
//					/*-- Create test folder --*/
//					NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
//					if(folderNodeRef != null)
//					{
//						fileFolderService.delete(folderNodeRef);    		
//					}			
//					folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
//					
//					// create SF
//					SemiFinishedProductData sfData = new SemiFinishedProductData();
//					sfData.setName("SF");
//					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
//					
//					for(int z_idx=0 ; z_idx<nbFiles ; z_idx++){
//						
//						costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(0), false));
//						costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(1), false));
//						costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(2), false));
//						costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(3), false));
//					}
//					
//					sfData.setCostList(costList);
//					Collection<QName> dataLists = productDictionaryService.getDataLists();
//					sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
//
//					// load SF and test it
//					sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
//					
//					NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
//					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
//					
//					String query = String.format("+PARENT:\"%s\"", listNodeRef);
//					Map<String, Boolean> sort = new HashMap<String, Boolean>();
//					sort.put("@" + BeCPGModel.PROP_SORT, true);
//					
//					StopWatch watch = new StopWatch();
//					watch.start();				
//					
//					List<NodeRef> nodeRefs = beCPGSearchService.unProtLuceneSearch(query, sort, -1);
//					
//					watch.stop();
//					logger.debug("search nb files: " + nodeRefs.size() + " - executed in  " + watch.getTotalTimeSeconds() + " seconds");
//				}
//				
//				/*
//				 * Use getChildAssocs
//				 */
//				
//				for(int nbFiles : arrFiles){
//					
//					/*-- Create test folder --*/
//					NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
//					if(folderNodeRef != null)
//					{
//						fileFolderService.delete(folderNodeRef);    		
//					}			
//					folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
//					
//					// create SF
//					SemiFinishedProductData sfData = new SemiFinishedProductData();
//					sfData.setName("SF");
//					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
//					
//					for(int z_idx=0 ; z_idx<nbFiles ; z_idx++){
//						
//						costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(0), false));
//						costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(1), false));
//						costList.add(new CostListDataItem(null, 3f, "€/kg", null, costs.get(2), false));
//						costList.add(new CostListDataItem(null, 2f, "€/kg", null, costs.get(3), false));
//					}
//					
//					sfData.setCostList(costList);
//					Collection<QName> dataLists = productDictionaryService.getDataLists();
//					sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
//
//					// load SF and test it
//					sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
//					
//					NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
//					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
//					
//					StopWatch watch = new StopWatch();
//					watch.start();				
//					
//					List<NodeRef> nodeRefs = listCostListItem(listNodeRef, false, true);
//					
//					watch.stop();
//					logger.debug("getChildAssocs nb files: " + nodeRefs.size() + " - executed in  " + watch.getTotalTimeSeconds() + " seconds");
//				}
//				
//				
//				return null;
//				
//			}},false,true);	   				
//	   
//	}	
//	
//	private List<NodeRef> listCostListItem(NodeRef contextNodeRef, boolean folders, boolean files)
//    {
//		Set<QName> searchTypeQNames = new HashSet<QName>(1);
//		searchTypeQNames.add(BeCPGModel.TYPE_COSTLIST);
//		
//        // Do the query
//        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(contextNodeRef, searchTypeQNames);
//        List<NodeRef> result = new ArrayList<NodeRef>(childAssocRefs.size());
//        for (ChildAssociationRef assocRef : childAssocRefs)
//        {
//            result.add(assocRef.getChildRef());
//        }
//        // Done
//        return result;
//    }
}
