/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
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
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;

// TODO: Auto-generated Javadoc
/**
 * The Class CompareProductReportWebScriptTest.
 *
 * @author querephi
 */
public class CompareProductReportWebScriptTest extends BaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareProductReportWebScriptTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";
	
	/** The GROU p_ garniture. */
	private static String GROUP_GARNITURE = "Garniture";
	
	/** The GROU p_ pate. */
	private static String GROUP_PATE = "Pâte";
	
	/** The Constant USER_ADMIN. */
	private static final String USER_ADMIN = "admin";
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The search service. */
	private SearchService searchService;

    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;
    
    /** The product dao. */
    private ProductDAO productDAO;
    
    /** The product dictionary service. */
    private ProductDictionaryService productDictionaryService;
    
    /** The transaction service. */
    private TransactionService transactionService;
    
    /** The repository. */
    private Repository repository;
    
    /** The content service. */
    private ContentService contentService;
    
    /** The mimetype service. */
    private MimetypeService mimetypeService;
    
	/** The folder node ref. */
	private NodeRef folderNodeRef;
	
	/** The local s f1 node ref. */
	private NodeRef  localSF1NodeRef;
    
    /** The raw material1 node ref. */
    private NodeRef  rawMaterial1NodeRef;
    
    /** The raw material2 node ref. */
    private NodeRef  rawMaterial2NodeRef;
    
    /** The local s f2 node ref. */
    private NodeRef  localSF2NodeRef;
    
    /** The raw material3 node ref. */
    private NodeRef  rawMaterial3NodeRef;
    
    /** The raw material4 node ref. */
    private NodeRef  rawMaterial4NodeRef;
    
    /** The raw material5 node ref. */
    private NodeRef  rawMaterial5NodeRef;
    
    /** The fp1 node ref. */
    private NodeRef fp1NodeRef;
    
    /** The fp2 node ref. */
    private NodeRef fp2NodeRef;
    
    
    /** The costs. */
    private List<NodeRef> costs = new ArrayList<NodeRef>();
	
	/** The allergens. */
	private List<NodeRef> allergens = new ArrayList<NodeRef>();
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.web.scripts.BaseWebScriptTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
				
		nodeService = (NodeService)appCtx.getBean("NodeService");
		fileFolderService = (FileFolderService)appCtx.getBean("FileFolderService");
		searchService = (SearchService)appCtx.getBean("SearchService");		
		authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
		productDAO = (ProductDAO)appCtx.getBean("productDAO");
		productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
		transactionService = (TransactionService)appCtx.getBean("transactionService");
		repository = (Repository)appCtx.getBean("repositoryHelper");
		contentService = (ContentService)appCtx.getBean("contentService");
		mimetypeService = (MimetypeService)appCtx.getBean("mimetypeService");				
		
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
 * Inits the objects.
 */
private void initObjects(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				/*-- Create test folder --*/
				folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
													
		
				//costs
				NodeRef systemFolder = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef costFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
				if(costFolder == null){
					costFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS), ContentModel.TYPE_FOLDER).getNodeRef();
				}
				List<FileInfo> costsFileInfo = fileFolderService.listFiles(costFolder);
				if(costsFileInfo.size() == 0){
					for(int i=0 ; i<10 ; i++)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, "Cost " + i);
			    		properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			    		ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
			    		costs.add(childAssocRef.getChildRef());
			    	}
				}
				else{
					for(FileInfo fileInfo : costsFileInfo){
						costs.add(fileInfo.getNodeRef());
					}
				}
				
				//allergens
				NodeRef allergensFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS));
				if(allergensFolder == null){
					allergensFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS), ContentModel.TYPE_FOLDER).getNodeRef();
				}
				List<FileInfo> allergensFileInfo = fileFolderService.listFiles(allergensFolder);
				if(allergensFileInfo.size() == 0){
					for(int i=0 ; i<10 ; i++)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, "Allergen " + i);
			    		ChildAssociationRef childAssocRef = nodeService.createNode(allergensFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties);
			    		allergens.add(childAssocRef.getChildRef());
			    	}
				}
				else{
					for(FileInfo fileInfo : allergensFileInfo){
						allergens.add(fileInfo.getNodeRef());
					}
				}
			
				/*-- Create raw materials --*/
				logger.debug("/*-- Create raw materials --*/");
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");				
				rawMaterial1NodeRef = productDAO.create(folderNodeRef, rawMaterial1, dataLists);
				
				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");					
				rawMaterial2NodeRef = productDAO.create(folderNodeRef, rawMaterial2, dataLists);
				
				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");				
				rawMaterial3NodeRef = productDAO.create(folderNodeRef, rawMaterial3, dataLists);
				
				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");					
				rawMaterial4NodeRef = productDAO.create(folderNodeRef, rawMaterial4, dataLists);
				
				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");				
				rawMaterial5NodeRef = productDAO.create(folderNodeRef, rawMaterial5, dataLists);
				
				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProduct localSF1 = new LocalSemiFinishedProduct();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1NodeRef = productDAO.create(folderNodeRef, localSF1, dataLists);
				
				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProduct localSF2 = new LocalSemiFinishedProduct();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");							
				localSF2NodeRef = productDAO.create(folderNodeRef, localSF2, dataLists);	
		
		return null;
		
			}},false,true);
		
	}

	/**
	 * Inits the tests.
	 */
	private void initTests(){
		
		logger.debug("look for report template");
	   	NodeRef systemFolder = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));    	   		
	   	assertNotNull("Check system folder", systemFolder);
	   	
	   	NodeRef reportsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));    	
	   	assertNotNull("Check reports folder", reportsFolder);
	   	
	   	NodeRef productComparisonReportsFolder = nodeService.getChildByName(reportsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS));
	   	if(productComparisonReportsFolder != null){
	   		nodeService.deleteNode(productComparisonReportsFolder);
	   	}
	   	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS));
    	productComparisonReportsFolder = nodeService.createNode(reportsFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_FOLDER, properties).getChildRef();
	   	
    	assertNotNull("Check product comparison folder", productComparisonReportsFolder);
   		
	   	//create report template folder
	   	logger.debug("create report");	   	
   		
    	//create birt file
    	String birtDir = "/src/main/resources/beCPG/birt/";
		String [] birtFiles = {"CompareProducts.rptdesign"};			
		
		for(String birtFile : birtFiles){			
			
			logger.debug("create birt file " + birtFile);
			
			properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, birtFile);
	    	NodeRef fileNodeRef = nodeService.createNode(productComparisonReportsFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
	    	
	    	ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
	    	logger.debug("Load birt file " + System.getProperty("user.dir" )  + birtDir + birtFile);
	    	FileInputStream in = null;
	    	
	    	try{
	    		in = new FileInputStream(System.getProperty("user.dir" )  + birtDir + birtFile);	    		
	    	}
	    	catch(FileNotFoundException e){
	    		logger.error("Failed to get user.dir", e);
	    	}
	    	
	    	assertNotNull("check input stream", in);
	    	
	    	String mimetype = mimetypeService.guessMimetype(birtFile);
			ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
	        Charset charset = charsetFinder.getCharset(in, mimetype);
	        String encoding = charset.name();

	        logger.debug("mimetype : " + mimetype);
	        logger.debug("encoding : " + encoding);
	    	writer.setMimetype(mimetype);
	    	writer.setEncoding(encoding);
	    	writer.putContent(in);
	    	
	    	//check
	    	ContentReader reader = contentService.getReader(fileNodeRef, ContentModel.PROP_CONTENT);
			InputStream inputStream = reader.getContentInputStream();
	    	
	    	logger.debug("file writen.");		    	
		}			   
	}
		
		/**
		 * Test compare products.
		 */
		public void testCompareProducts(){

			//TODO : merge CompareProductReportWebScript avec CompareProductServiceTest => beaucoup de code en commun !
			// init objects
			initObjects();
		
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {
										
					//Create comparison product report
					initTests();
								
					 Collection<QName> dataLists = productDictionaryService.getDataLists();
					
					logger.debug("createRawMaterial 1");
					
					FinishedProductData fp1 = new FinishedProductData();
					fp1.setName("FP 1");			
			
					//Costs
					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();		    		
					for(int j=0 ; j<costs.size() ; j++)
					{		    			
						CostListDataItem costListItemData = new CostListDataItem(null, 12.2f, "€/kg", costs.get(j));
						costList.add(costListItemData);
					}		
					fp1.setCostList(costList);
					
					// create an MP for the allergens
					RawMaterialData allergenRawMaterial = new RawMaterialData();
					allergenRawMaterial.setName("MP allergen");
					NodeRef allergenRawMaterialNodeRef = productDAO.create(folderNodeRef, allergenRawMaterial, dataLists);
					
					//Allergens
					List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();		    		
					for(int j=0 ; j<allergens.size() ; j++)
					{		    			
						List<NodeRef> voluntarySources = new ArrayList<NodeRef>();
						voluntarySources.add(allergenRawMaterialNodeRef);
						
						AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, true, false, voluntarySources, null, allergens.get(j));
						allergenList.add(allergenListItemData);
					}		
					fp1.setAllergenList(allergenList);
						
					List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
					compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
					compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF2NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
					fp1.setCompoList(compoList);
					
					fp1NodeRef = productDAO.create(folderNodeRef, fp1, dataLists);
					
					logger.debug("create FP 2");
					
					FinishedProductData fp2 = new FinishedProductData();
					fp2.setName("FP 2");			
			
					//Costs
					costList = new ArrayList<CostListDataItem>();		    		
					for(int j=0 ; j<costs.size() ; j++)
					{		    			
						CostListDataItem costListItemData = new CostListDataItem(null, 12.4f, "$/kg", costs.get(j));
						costList.add(costListItemData);
					}		
					fp2.setCostList(costList);
						
					//Allergens
					allergenList = new ArrayList<AllergenListDataItem>();		    		
					for(int j=0 ; j<allergens.size() ; j++)
					{		    			
						List<NodeRef> allSources = new ArrayList<NodeRef>();
						allSources.add(allergenRawMaterialNodeRef);
						AllergenListDataItem allergenListItemData = null;
						
						if(j < 5){
							allergenListItemData = new AllergenListDataItem(null, true, false, allSources, null, allergens.get(j));
						}
						else{
							allergenListItemData = new AllergenListDataItem(null, false, true, null, allSources, allergens.get(j));
						}						
						
						allergenList.add(allergenListItemData);
					}		
					fp2.setAllergenList(allergenList);
					
					compoList = new ArrayList<CompoListDataItem>();
					compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
					compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF2NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.P, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
					compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DETAIL_FR, rawMaterial4NodeRef));
					fp2.setCompoList(compoList);
					
					fp2NodeRef = productDAO.create(folderNodeRef, fp2, dataLists);
					
					return null;
					
				}},false,true);
			
			try{
			
				String url = String.format("/becpg/product/compare/Produit?product1=%s&product2=%s", fp1NodeRef, fp2NodeRef);;
				Response response = sendRequest(new GetRequest(url), 200, "admin");
				
				//logger.debug("response: " + response.getContentAsString());
			}
			catch(Exception e){
				logger.error("Failed to execute webscript", e);
			}
			
	   }
	
	
    	
}
