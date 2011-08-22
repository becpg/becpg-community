/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.web.scripts.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductListsWebScriptTest.
 *
 * @author querephi
 */
public class ProductListsWebScriptTest extends BaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductListsWebScriptTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The Constant PATH_TEMPFOLDER. */
	private static final String PATH_TEMPFOLDER = "TempFolder";
	
	/** The Constant USER_ADMIN. */
	private static final String USER_ADMIN = "admin";
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;

    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;
    
    /** The product dao. */
    private ProductDAO productDAO;
    
    /** The product dictionary service. */
    private ProductDictionaryService productDictionaryService;
    
    /** The transaction service. */
    private TransactionService transactionService;
    
    /** The repository helper. */
    private Repository repositoryHelper;
    
	/** The raw material node ref. */
	private NodeRef rawMaterialNodeRef = null;
	
	/** The finished product node ref. */
	private NodeRef finishedProductNodeRef = null;
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
				
		nodeService = (NodeService)appCtx.getBean("NodeService");
		fileFolderService = (FileFolderService)appCtx.getBean("FileFolderService");		
		authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
		productDAO = (ProductDAO)appCtx.getBean("productDAO");
		productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
		transactionService = (TransactionService)appCtx.getBean("transactionService");
		repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
		
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
	 * Test product list.
	 *
	 * @throws Exception the exception
	 */
	public void testProductList() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
					/*-- create folders --*/
					logger.debug("/*-- create folders --*/");
					NodeRef tempFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);    	
			    	if(tempFolder != null){
			    		fileFolderService.delete(tempFolder);    		
			    	}
			    	tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
			    	    	
			    	NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));    	
			    	if(systemFolder == null){
			    		systemFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM), ContentModel.TYPE_FOLDER).getNodeRef();    		
			    	}
			    	NodeRef productTemplateFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_TEMPLATES));    	
			    	if(productTemplateFolder != null){
			    		nodeService.deleteNode(productTemplateFolder);    		
			    	}
			    	productTemplateFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_TEMPLATES), ContentModel.TYPE_FOLDER).getNodeRef();    	
			   	
			    	/*-- characteristics --*/
			    	logger.debug("/*-- characteristics --*/");
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					//Costs
					properties.put(ContentModel.PROP_NAME, "cost1");			 					 				
					properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");					
					NodeRef cost1 = nodeService.createNode(tempFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "cost2");			 					 				
					properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
					NodeRef cost2 = nodeService.createNode(tempFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
					//Nuts
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "nut1");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					NodeRef nut1 = nodeService.createNode(tempFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();		
					
					/*-- Create raw material Tpl --*/
					Collection<QName> dataLists = productDictionaryService.getDataLists();
					logger.debug("/*-- Create raw material Tpl --*/");
					RawMaterialData rawMaterialTpl = new RawMaterialData();
					rawMaterialTpl.setName("Raw material Tpl");
					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
					costList.add(new CostListDataItem(null, 3f, "€/kg", cost1));
					costList.add(new CostListDataItem(null, 2f, "€/kg", cost2));
					rawMaterialTpl.setCostList(costList);
					List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
					nutList.add(new NutListDataItem(null, 1f, "kJ/100g", "Groupe 1", nut1));
					rawMaterialTpl.setNutList(nutList);		
					
					NodeRef rawMaterialTplNodeRef = productDAO.create(productTemplateFolder, rawMaterialTpl, dataLists);
					
					/*-- Create raw material --*/
					logger.debug("/*-- Create raw material --*/");
					RawMaterialData rawMaterial = new RawMaterialData();
					rawMaterial.setName("Raw material");
					rawMaterialNodeRef = productDAO.create(tempFolder, rawMaterial, dataLists);
					
					/*-- Create finished product --*/
					logger.debug("/*-- Create finished product --*/");
					FinishedProductData finishedProduct = new FinishedProductData();
					finishedProduct.setName("Finished Product");
					finishedProductNodeRef = productDAO.create(tempFolder, finishedProduct, dataLists);

					return null;

				}},false,true);
		 
			//Call webscript on raw material
			String url = "/becpg/productlists/node/" + rawMaterialNodeRef.toString().replace(":/", "");
			logger.debug("url : " + url);				
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			
//			//Check product
//			RawMaterialData rawMaterial = (RawMaterialData)productFactory.getProduct(rawMaterialNodeRef, productDictionaryService.getDataLists());
//			assertEquals("Raw material must have 2 costs", rawMaterial.getCostList().size()==2);
//			assertEquals("Raw material must have 1 nut", rawMaterial.getNutList().size()==1);		
			
			//Call webscript on finished product
			url = "/becpg/productlists/node/" + finishedProductNodeRef.toString().replace(":/", "");
			logger.debug("url : " + url);				
			response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			
//			//Check product
//			FinishedProductData finishedProduct = (FinishedProductData)productFactory.getProduct(finishedProductNodeRef, productDictionaryService.getDataLists());
//			assertEquals("Finished product don't have costs", finishedProduct.getCostList()==null);
//			assertEquals("Finished product don't have nuts", finishedProduct.getNutList()==null);
		

    }
    	
}
