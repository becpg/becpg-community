package fr.becpg.repo.web.scripts.report;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
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
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchWebScriptTest.
 *
 * @author querephi
 */
public class ExportSearchWebScriptTest extends BaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ExportSearchWebScriptTest.class);
	
	/** The app ctx. */
	private  ApplicationContext appCtx = getServer().getApplicationContext();
	
	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearchQuery.xml";
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";
	
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
    
    /** The repository. */
    private Repository repository;
    
    private ReportTplService reportTplService;
    
    /** The content service. */
    private ContentService contentService;
    
    /** The mimetype service. */
    private MimetypeService mimetypeService;
    
    private RepoService repoService;
    	
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
    

    
    /** The export product report tpl. */
    private NodeRef exportProductReportTpl;
    
    
    /** The costs. */
    private List<NodeRef> costs = new ArrayList<NodeRef>();
    
    private List<NodeRef> nuts = new ArrayList<NodeRef>();
	
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
		authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
		productDAO = (ProductDAO)appCtx.getBean("productDAO");
		productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
		transactionService = (TransactionService)appCtx.getBean("transactionService");
		repository = (Repository)appCtx.getBean("repositoryHelper");
		contentService = (ContentService)appCtx.getBean("contentService");
		mimetypeService = (MimetypeService)appCtx.getBean("mimetypeService");
		reportTplService = (ReportTplService)appCtx.getBean("reportTplService");
		repoService = (RepoService)appCtx.getBean("repoService");
		
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
				if(costFolder != null){
					fileFolderService.delete(costFolder);
				}				
				costFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS), ContentModel.TYPE_FOLDER).getNodeRef();
				List<FileInfo> costsFileInfo = fileFolderService.listFiles(costFolder);
				if(costsFileInfo.size() == 0){
					
					String [] costNames = {"Coût MP","Coût prév MP","Coût Emb","Coût prév emb"};
					for(String costName : costNames)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, costName);
			    		properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			    		ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
			    		costs.add(childAssocRef.getChildRef());
			    	}
				}
				
				//nuts
				NodeRef nutFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_NUTS));				
				if(nutFolder != null){
					fileFolderService.delete(nutFolder);
				}				
				nutFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_NUTS), ContentModel.TYPE_FOLDER).getNodeRef();
				List<FileInfo> nutsFileInfo = fileFolderService.listFiles(nutFolder);
				if(nutsFileInfo.size() == 0){
					
					String [] nutNames = {"Protéines","Lipides","Glucides",};
					for(String nutName : nutNames)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, nutName);
			    		properties.put(BeCPGModel.PROP_NUTGROUP, "Groupe 1");
			    		properties.put(BeCPGModel.PROP_NUTUNIT, "g");
			    		ChildAssociationRef childAssocRef = nodeService.createNode(nutFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties);
			    		nuts.add(childAssocRef.getChildRef());
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
				 productDAO.create(folderNodeRef, rawMaterial5, dataLists);
				
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
	 * @throws IOException 
	 */
	private void initTests() throws IOException{
		
		logger.debug("look for report template");
	   	
		// system folder
		NodeRef systemFolder = repoService.createFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
	   	assertNotNull("Check system folder", systemFolder);
	   	
	   	// reports folder
	   	NodeRef reportsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));    	
	   	assertNotNull("Check reports folder", reportsFolder);
	   	
	   	// export search report
		NodeRef exportSearchNodeRef = repoService.createFolderByPath(reportsFolder, RepoConsts.PATH_REPORTS_EXPORT_SEARCH, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH));
		NodeRef exportSearchProductsNodeRef = repoService.createFolderByPath(exportSearchNodeRef, RepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS));
		
		exportProductReportTpl = reportTplService.createTplRptDesign(exportSearchProductsNodeRef, 
											TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS), 
											EXPORT_PRODUCTS_REPORT_RPTFILE_PATH, 
											ReportType.ExportSearch, 	
											ReportFormat.XLS,
											BeCPGModel.TYPE_PRODUCT, 
											false, 
											true, 
											true);
		
		reportTplService.createTplRessource(exportSearchProductsNodeRef, 												
											EXPORT_PRODUCTS_REPORT_XMLFILE_PATH, 												
											false);						   		  
	}
	
	/**
	 * Adds the product image.
	 *
	 * @param parentNodeRef the parent node ref
	 * @throws FileNotFoundException the file not found exception
	 */
	private void addProductImage(NodeRef parentNodeRef) throws FileNotFoundException{
		/*-- add product image--*/
		logger.debug("/*-- add product image--*/");
		String imageName = I18NUtil.getMessage(RepoConsts.PATH_PRODUCT_IMAGE) + ".jpg";			
		logger.debug("image name: " + imageName);	
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, imageName);
		NodeRef imageNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
		
		ContentWriter writer = contentService.getWriter(imageNodeRef, ContentModel.PROP_CONTENT, true);
		String imageFullPath = System.getProperty("user.dir" )  + "/src/test/resources/beCPG/birt/productImage.jpg";
		logger.debug("Load image file " + imageFullPath + " - imgNodeRef: " + imageNodeRef);			
		FileInputStream imageStream = new FileInputStream(imageFullPath);
		logger.debug("image file loaded "  + imageStream);
		
		String mimetype = mimetypeService.guessMimetype(imageFullPath);
		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
		Charset charset = charsetFinder.getCharset(imageStream, mimetype);
		String encoding = charset.name();
		
		logger.debug("mimetype : " + mimetype);
		logger.debug("encoding : " + encoding);
		writer.setMimetype(mimetype);
		writer.setEncoding(encoding);
		writer.putContent(imageStream);		
   }
		
	/**
	 * Test export search.
	 */
	public void testExportSearch(){
	
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
					CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, costs.get(j), false);
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
					
					AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, true, false, voluntarySources, null, allergens.get(j), false);
					allergenList.add(allergenListItemData);
				}		
				fp1.setAllergenList(allergenList);
					
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Declare, rawMaterial3NodeRef));
				fp1.setCompoList(compoList);
				
				 productDAO.create(folderNodeRef, fp1, dataLists);
				
				logger.debug("create FP 2");
				
				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");			
		
				//Costs
				costList = new ArrayList<CostListDataItem>();		    		
				for(int j=0 ; j<costs.size() ; j++)
				{		    			
					CostListDataItem costListItemData = new CostListDataItem(null, 12.4d, "$/kg", null, costs.get(j), false);
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
						allergenListItemData = new AllergenListDataItem(null, true, false, allSources, null, allergens.get(j), false);
					}
					else{
						allergenListItemData = new AllergenListDataItem(null, false, true, null, allSources, allergens.get(j), false);
					}						
					
					allergenList.add(allergenListItemData);
				}		
				fp2.setAllergenList(allergenList);
				
				compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail, rawMaterial4NodeRef));
				fp2.setCompoList(compoList);
				
				productDAO.create(folderNodeRef, fp2, dataLists);
				
				/*-- Create images folder --*/					
				NodeRef imagesNodeRef = fileFolderService.create(folderNodeRef, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES), ContentModel.TYPE_FOLDER).getNodeRef();					
				addProductImage(imagesNodeRef);
				
				return null;
				
			}},false,true);						
		
		// search on date range
		try{
						
			String url = "/becpg/report/exportsearch/" + exportProductReportTpl.toString().replace("://", "/") + "/Excel.xls?repo=true&term=&query={\"prop_cm_name\"%3A\"\"%2C\"prop_bcpg_legalName\"%3A\"\"%2C\"prop_bcpg_productHierarchy1\"%3A\"\"%2C\"prop_bcpg_productHierarchy2\"%3A\"\"%2C\"prop_bcpg_productState\"%3A\"\"%2C\"prop_bcpg_productCode\"%3A\"\"%2C\"prop_bcpg_eanCode\"%3A\"\"%2C\"assoc_bcpg_supplierAssoc\"%3A\"\"%2C\"assoc_bcpg_supplierAssoc_added\"%3A\"\"%2C\"assoc_bcpg_supplierAssoc_removed\"%3A\"\"%2C\"prop_cm_modified-date-range\"%3A\"2011-04-17T00%3A00%3A00%2B02%3A00|2011-05-23T00%3A00%3A00%2B02%3A00\"%2C\"prop_cm_modifier\"%3A\"\"%2C\"assoc_bcpg_ingListIng\"%3A\"\"%2C\"assoc_bcpg_ingListIng_added\"%3A\"\"%2C\"assoc_bcpg_ingListIng_removed\"%3A\"\"%2C\"assoc_bcpg_ingListGeoOrigin\"%3A\"\"%2C\"assoc_bcpg_ingListGeoOrigin_added\"%3A\"\"%2C\"assoc_bcpg_ingListGeoOrigin_removed\"%3A\"\"%2C\"assoc_bcpg_ingListBioOrigin\"%3A\"\"%2C\"assoc_bcpg_ingListBioOrigin_added\"%3A\"\"%2C\"assoc_bcpg_ingListBioOrigin_removed\"%3A\"\"%2C\"datatype\"%3A\"bcpg%3Aproduct\"}";
							
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("Response: "+response.getContentAsString() );
		}
		catch(Exception e){				
			logger.error("Failed to execute webscript", e);
			assertNull("Should not throw an exception", e);
		}
		
		// search on cm:name
		try{
			
			String url = "/becpg/report/exportsearch/" + exportProductReportTpl.toString().replace("://", "/") + "/Excel.xls?repo=true&term=&query={\"prop_cm_name\"%3A\"FP\"%2C\"prop_cm_title\"%3A\"\"%2C\"prop_cm_description\"%3A\"\"%2C\"prop_mimetype\"%3A\"\"%2C\"prop_cm_modified-date-range\"%3A\"\"%2C\"prop_cm_modifier\"%3A\"\"%2C\"datatype\"%3A\"cm%3Acontent\"}";				
							
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("Response: "+response.getContentAsString() );
		}
		catch(Exception e){				
			logger.error("Failed to execute webscript", e);
			assertNull("Should not throw an exception", e);
			}
			
	   }
		
		/**
		 * Test get export search tpls.
		 */
		public void testGetExportSearchTpls(){
		
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {
										
				//Create product report
				initTests();
							
				//List<NodeRef> reportTpls = exportSearchService.getReportTpls();
				List<NodeRef> reportTpls = reportTplService.suggestUserReportTemplates(ReportType.ExportSearch, BeCPGModel.TYPE_PRODUCT, "*");
				
				for(NodeRef n : reportTpls){
					logger.debug("report name: " + nodeService.getProperty(n, ContentModel.PROP_NAME));
				}
				
				assertEquals("There is one report", 1, reportTpls.size());
				assertEquals("Check report nodeRef", exportProductReportTpl, reportTpls.get(0));
				
				return null;
				
			}},false,true);
		
		try{
		
			String url = "/becpg/report/exportsearch/templates/bcpg:product";
			
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			
			logger.debug("response: " + response.getContentAsString());
		}
		catch(Exception e){
			logger.error("Failed to execute webscript", e);
			}
			
	   }
	
	
    	
}
