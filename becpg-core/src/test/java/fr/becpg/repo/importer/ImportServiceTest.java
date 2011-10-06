/*
 * 
 */
package fr.becpg.repo.importer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ImportServiceTest.
 *
 * @author querephi
 */
public class ImportServiceTest extends RepoBaseTestCase {
	
	/** The PAT h_ temp. */
	private static String PATH_TEMP = "Temp";
	
	/** The PAT h_ products. */
	private static String PATH_PRODUCTS = "Products";
	
	/** The PAT h_ classi f_ folder. */
	private static String PATH_CLASSIF_FOLDER_RM = "./cm:Products/cm:ToValidate/cm:RawMaterial/cm:Sea_x0020_food/cm:Fish";
	
	private static String PATH_CLASSIF_FOLDER_FP = "./cm:Products/cm:ToValidate/cm:FinishedProduct/cm:Sea_x0020_food/cm:Fish";
	
	private static String PATH_SITE_FOLDER = "./st:sites/cm:folder";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportServiceTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();	
	
	/** The import service. */
	private ImportService importService;
	
	/** The repository. */
	private Repository repository;
	
	/** The ml node service impl. */
	private NodeService mlNodeServiceImpl;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The search service. */
	private SearchService searchService;
	
	/** The namespace service. */
	private NamespaceService namespaceService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The content service. */
	private ContentService contentService;
	
	/** The repo service. */
	private RepoService repoService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private EntityTplService entityTplService;
	
	private DictionaryDAO dictionaryDAO;
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	importService = (ImportService)appCtx.getBean("importService");
        authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
        repository = (Repository)appCtx.getBean("repositoryHelper");
        mlNodeServiceImpl = (NodeService) appCtx.getBean("mlAwareNodeService");
        mimetypeService = (MimetypeService)appCtx.getBean("mimetypeService");
        fileFolderService = (FileFolderService)appCtx.getBean("FileFolderService");
        productDAO = (ProductDAO)appCtx.getBean("productDAO");
        searchService = (SearchService)appCtx.getBean("searchService");
        namespaceService = (NamespaceService)appCtx.getBean("namespaceService");
        nodeService = (NodeService)appCtx.getBean("nodeService");
        contentService = (ContentService)appCtx.getBean("contentService");
        repoService = (RepoService)appCtx.getBean("repoService");
        policyBehaviourFilter = (BehaviourFilter)appCtx.getBean("policyBehaviourFilter");
        entityTplService = (EntityTplService)appCtx.getBean("entityTplService");
        dictionaryDAO = (DictionaryDAO)appCtx.getBean("dictionaryDAO");
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {

 				// should not exist...
 				NodeRef systemFolder = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM);
 				if(systemFolder != null){
 					nodeService.deleteNode(systemFolder); 					 					
 				}
 				 				
 				systemFolder = repoService.createFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
 				
 				// create folderTpl
 				NodeRef folderTplsNodeRef = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_FOLDER_TEMPLATES, TranslateHelper.getTranslatedPath(RepoConsts.PATH_FOLDER_TEMPLATES));
 				NodeRef productTplsNodeRef = repoService.createFolderByPath(folderTplsNodeRef, RepoConsts.PATH_PRODUCT_TEMPLATES, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_TEMPLATES));
 				entityTplService.createFolderTpl(productTplsNodeRef, BeCPGModel.TYPE_RAWMATERIAL, true, null);
 				
 				// remove ings 				
 				NodeRef ingsFolder = repoService.getFolderByPath(systemFolder, RepoConsts.PATH_INGS);
					
				if(ingsFolder != null){
					nodeService.deleteNode(ingsFolder);
				}
				
				// remove temp folder				
				NodeRef tempNodeRef = repoService.getFolderByPath(repository.getCompanyHome(), PATH_TEMP);			
 				if(tempNodeRef != null)
 				{
 					logger.debug("delete temp folder");
 					fileFolderService.delete(tempNodeRef);    		
 				}
 				
 				// create hierarchy
				initRepoAndHierarchyLists();
 				
 				return null;

 			}},false,true);
        
        dictionaryDAO.reset();
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
	 * Test import text.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ImporterException the be cpg exception
	 */
	public void testImportText() throws IOException, ImporterException{
	
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				/*-- Create file to import --*/
 		    	logger.debug("create file to import");
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
 		    	properties.put(ContentModel.PROP_NAME, "import.csv");
 		    	
 		    	NodeRef nodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(nodeRef != null){
 		    		nodeService.deleteNode(nodeRef);   		
 		    	}    	
 		    	nodeRef = nodeService.createNode(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
 		    	
 		    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
 		    	logger.debug("Load import.csv");
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import.csv");			
 		    	logger.debug("import.csv loaded");
 		    	writer.putContent(in);
 		    	
 				logger.debug("Start import");
 				importService.importText(nodeRef, true, false);
 				
 				return null;

 			}},false,true);

		/*-- Check MLText property --*/
		logger.debug("Check MLText properties");
		NodeRef systemFolder = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		assertNotNull("system folder should exist", systemFolder);
		NodeRef ingsFolder = repoService.getFolderByPath(systemFolder, RepoConsts.PATH_INGS);
		assertNotNull("ings folder should exist", ingsFolder);
		
		// Abricot 				 				 				
		NodeRef ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Abricot");
		assertNotNull("abricot ing should exist", ingNodeRef);
		MLText mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_ING_MLNAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Abricot default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Abricot english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Abricot french", mlText.getValue(Locale.FRENCH));
		
		// Acerola
		ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Acerola");
		assertNotNull("Acerola ing should exist", ingNodeRef);
		mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_ING_MLNAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Acerola default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Acerola english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Acerola french", mlText.getValue(Locale.FRENCH));
		
		// Abricot1
		ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Abricot1");
		assertNotNull("Abricot1 ing should exist", ingNodeRef);
		mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_ING_MLNAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Abricot1 default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Abricot1 english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Abricot1 french", mlText.getValue(Locale.FRENCH));
		
		// Acerola1
		ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Acerola1");
		assertNotNull("Acerola1 ing should exist", ingNodeRef);
		mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_ING_MLNAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Acerola1 default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Acerola1 english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Acerola1 french", mlText.getValue(Locale.FRENCH));
	}
	
	/**
	 * Adds the mapping file.
	 */
	private void addMappingFile(){
					
		logger.debug("/*-- Add mapping file --*/");
		
		NodeRef systemFolder = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		if(systemFolder == null) 
			systemFolder = repoService.createFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
		
		NodeRef exchangeFolder = repoService.getFolderByPath(systemFolder, RepoConsts.PATH_EXCHANGE);
		if(exchangeFolder == null) 
			exchangeFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_EXCHANGE, TranslateHelper.getTranslatedPath(RepoConsts.PATH_EXCHANGE));
		
		NodeRef importFolder = repoService.getFolderByPath(exchangeFolder, RepoConsts.PATH_IMPORT);
		if(importFolder == null)			
			importFolder = repoService.createFolderByPath(exchangeFolder, RepoConsts.PATH_IMPORT, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMPORT));
		
		NodeRef mappingFolder = repoService.getFolderByPath(importFolder, RepoConsts.PATH_MAPPING);
		
		if(mappingFolder != null){
			nodeService.deleteNode(mappingFolder);
		}
		mappingFolder = repoService.createFolderByPath(importFolder, RepoConsts.PATH_MAPPING, TranslateHelper.getTranslatedPath(RepoConsts.PATH_MAPPING));
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, "Products.xml");
    	NodeRef productsMappingNodeRef = nodeService.createNode(mappingFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();    	    	
    	
    	ContentWriter writer = contentService.getWriter(productsMappingNodeRef, ContentModel.PROP_CONTENT, true);
    	String mappingFilePath = System.getProperty("user.dir" )  + "/src/main/resources/beCPG/import-mapping/Products.xml";
    	
    	logger.debug("Load birt file " + mappingFilePath);
    	FileInputStream in = null;
    	
    	try{
    		in = new FileInputStream(mappingFilePath);	    		
    	}
    	catch(FileNotFoundException e){
    		logger.error("Failed to get user.dir", e);
    	}
    	
    	assertNotNull("check input stream", in);
    	
    	String mimetype = mimetypeService.guessMimetype(mappingFilePath);	    	
    	String encoding = "UTF-8"; 		    		    		        	    
    	
    	writer.setMimetype(mimetype);
    	writer.setEncoding(encoding);
    	
    	logger.debug("mimetype : " + mimetype);
    	logger.debug("encoding : " + encoding);
    	
    	writer.putContent(in);
    	
    	//check
    	logger.debug("check mapping file. Path: " + nodeService.getPath(productsMappingNodeRef));
    	ContentReader reader = contentService.getReader(productsMappingNodeRef, ContentModel.PROP_CONTENT);
		InputStream inputStream = reader.getContentInputStream();
		assertNotNull(inputStream);
    	
    	logger.debug("file writen.");		    
	}
	
	/**
	 * Test import products.
	 * @throws Exception 
	 * @throws ParseException 
	 */
	public void testImportProducts() throws ParseException, Exception{
		
		/*
		 * Delete temp, products folder
		 * Add mapping file
		 */
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable { 				 				
 				
 				NodeRef tempNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);			
 				if(tempNodeRef != null)
 				{
 					logger.debug("delete temp folder");
 					fileFolderService.delete(tempNodeRef);    		
 				} 				
 				
 				// remove products 				
 				NodeRef productsNodeRef = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_PRODUCTS);			
 				if(productsNodeRef != null)
 				{
 					logger.debug("delete products folder");
 					fileFolderService.delete(productsNodeRef);    		
 				}
 				
				// remove companies				
				NodeRef companiesFolder = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_COMPANIES);
				
				if(companiesFolder != null){
					logger.debug("delete companies folder");
					nodeService.deleteNode(companiesFolder);
				}
				
				// remove site folder
				List<NodeRef> siteFoldernode = searchService.selectNodes(repository.getCompanyHome(), 
						PATH_SITE_FOLDER, 
						null, namespaceService, false);
				
				if(siteFoldernode != null && siteFoldernode.size() > 0){
					logger.debug("delete site folder");
					nodeService.deleteNode(siteFoldernode.get(0));
				}			
				
 				addMappingFile();
 				
 				return null;

 			}},false,true);
		
		/*
		 * Create file to import
		 */
		
		NodeRef nodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable { 				 				
 				
 		    	logger.debug("create file to import");
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
 		    	properties.put(ContentModel.PROP_NAME, "Import-Products.csv");
 		    	
 		    	NodeRef nodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(nodeRef != null){
 		    		nodeService.deleteNode(nodeRef);   		
 		    	}    	
 		    	nodeRef = nodeService.createNode(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef(); 		    	
 		    	
 		    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
 		    	logger.debug("Load import.csv");
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import-Products.csv");			
 		    	logger.debug("import.csv loaded");
 		    	writer.putContent(in);
 		
 		    	logger.debug("Start import");
 				importService.importText(nodeRef, true, false);
 				
 				return null;

 			}},false,true);
		
		
		
		/*
		 *  check imported values 
		 */ 				
		NodeRef tempNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
		assertNotNull("Temp folder should exist", tempNodeRef);
		NodeRef importFolderNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, PATH_PRODUCTS);
		assertNotNull("import folder should exist", importFolderNodeRef);
		assertEquals("import folder has one the productTpl", (int)1 , fileFolderService.listFiles(importFolderNodeRef).size());
		
		// load folder where products have been moved in ./cm:Products/cm:ToValidate/cm:RawMaterial/cm:Sea_x0020_food/cm:Fish 				
		List<NodeRef> nodes = searchService.selectNodes(repository.getCompanyHome(), 
													PATH_CLASSIF_FOLDER_RM, 
													null, namespaceService, false);
		assertEquals("classif folder should exist", (int)1 , nodes.size());
		NodeRef productsNodeRef = nodes.get(0);		
		assertEquals("4 products should exist", (int)4 , fileFolderService.list(productsNodeRef).size()); 				
		
		/*
		 * check products in repo
		 */
		
		String productName = "Saumon surgelé 80x20x4";
		NodeRef product1NodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, productName); 				
		// productFolder => look for product
		if(product1NodeRef != null && nodeService.getType(product1NodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
			product1NodeRef = nodeService.getChildByName(product1NodeRef, ContentModel.ASSOC_CONTAINS, productName);
		}
		assertNotNull("product 1 should exist", product1NodeRef);
		
		List<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COSTLIST);
		dataLists.add(BeCPGModel.TYPE_NUTLIST);
		ProductData productData = productDAO.find(product1NodeRef, dataLists);
		
		logger.debug("Props: " + nodeService.getProperties(product1NodeRef));
		
		// check Props : Saumon surgelé 80x20x4		saumon sugelé	Sea food	Fish	ToValidate					Thu Mar 17 17:44:13 CET 2011	admin	Mon Mar 21 22:44:26 CET 2011	admin		0.0	0.0	0.0	0.0	0.0	0.0	/home/querephi/Documents/beCPG/projets/demo/jeu de données/Sushi/sushi saumon/produit.jpg
		assertEquals("Saumon surgelé 80x20x4", productData.getName());
		assertEquals(SystemState.ToValidate, productData.getState());
		assertEquals("saumon sugelé", productData.getLegalName());
		assertEquals("Sea food", productData.getHierarchy1());
		assertEquals("Fish", productData.getHierarchy2());
		
		/*-- check associations --*/
		List<AssociationRef> supplierAssocRefs = nodeService.getTargetAssocs(product1NodeRef, BeCPGModel.ASSOC_SUPPLIERS);
		assertEquals("check product has 2 suppliers defined", 2, supplierAssocRefs.size());
		String supplier1Code = (String)nodeService.getProperty(supplierAssocRefs.get(0).getTargetRef(), BeCPGModel.PROP_CODE);
		String supplier2Code = (String)nodeService.getProperty(supplierAssocRefs.get(1).getTargetRef(), BeCPGModel.PROP_CODE);
		assertEquals("check supplier name", "12", supplier1Code);
		assertEquals("check supplier name", "13", supplier2Code);
		// does space between association values work ?
		
		/*
		 * Check Saumon
		 */
		productName = "Saumon 80x20x3";
		NodeRef product2NodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, productName);
		// productFolder => look for product
		if(product2NodeRef != null && nodeService.getType(product2NodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
			product2NodeRef = nodeService.getChildByName(product2NodeRef, ContentModel.ASSOC_CONTAINS, productName);
		}
		assertNotNull("product 2 should exist", product2NodeRef);
		supplierAssocRefs = nodeService.getTargetAssocs(product2NodeRef, BeCPGModel.ASSOC_SUPPLIERS);
		assertEquals("check product has 2 suppliers defined", 2, supplierAssocRefs.size());
		supplier1Code = (String)nodeService.getProperty(supplierAssocRefs.get(0).getTargetRef(), BeCPGModel.PROP_CODE);
		supplier2Code = (String)nodeService.getProperty(supplierAssocRefs.get(1).getTargetRef(), BeCPGModel.PROP_CODE);
		assertEquals("check supplier name", "12", supplier1Code);
		assertEquals("check supplier name", "14", supplier2Code);
		
		/*-- check productLists --*/
		assertEquals("costs should exist", (int)3, productData.getCostList().size());
		assertEquals("nuts should exist", (int)3, productData.getNutList().size());
		String [] costNames = {"Coût MP","Coût prév MP", "Coût Emb"};
		float [] costValues = {1.0f, 2.0f, 3.1f};
		String [] nutNames = {"Protéines","Lipides", "Glucides"};
		float [] nutValues = {2.5f, 3.6f, 5.6f};
		
		// check costs
		int costChecked=0;
		int z_idx=0;
		for(CostListDataItem c : productData.getCostList()){
			String costName = (String)nodeService.getProperty(c.getCost(), ContentModel.PROP_NAME);			 			
			
			for(String s : costNames){
				if(s.equals(costName)){ 							
					assertEquals("Check cost value", costValues[z_idx], nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_COSTLIST_VALUE));
					costChecked++;
					break;
				} 						
			}
			z_idx++;
		}
		assertEquals("3 costs have been checked", (int)3, costChecked); 				 				
		
		// check nuts
		int nutChecked=0;
		z_idx=0;
		for(NutListDataItem n : productData.getNutList()){
			String nutName = (String)nodeService.getProperty(n.getNut(), ContentModel.PROP_NAME); 					
			
			for(String s : nutNames){
				if(s.equals(nutName)){ 							
					assertEquals("Check nut value", nutValues[z_idx], nodeService.getProperty(n.getNodeRef(), BeCPGModel.PROP_NUTLIST_VALUE) );
					nutChecked++;
					break;
				} 						
			}
			z_idx++;
		}
		assertEquals("3 nuts have been checked", (int)3, nutChecked);
		
		// check that file Images/produit.jpg has been imported and check title
		NodeRef parentNodeRef = nodeService.getPrimaryParent(product1NodeRef).getParentRef(); 				
		NodeRef imagesNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, "Images");
		assertNotNull("check Images exits", imagesNodeRef);
		NodeRef imgNodeRef = nodeService.getChildByName(imagesNodeRef, ContentModel.ASSOC_CONTAINS, "produit.jpg");
		assertNotNull("check produit.jpg exits", imgNodeRef);
		assertEquals("Check title on image", "saumon", nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));
		
		
		/*
		 *  check trim is done by CSVReader
		 */
		
		NodeRef product4NodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, "Thon 80x20x8");
		assertNotNull("product 4 should exist", product4NodeRef);
		
		/*
		 *  check productTpl
		 */
		
		NodeRef productTplNodeRef = nodeService.getChildByName(importFolderNodeRef, ContentModel.ASSOC_CONTAINS, "productTpl");
		assertNotNull("productTpl should exist", productTplNodeRef);
		dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COSTLIST);
		dataLists.add(BeCPGModel.TYPE_NUTLIST);
		ProductData productTplData = productDAO.find(product1NodeRef, dataLists);
		
		/*-- check productLists of productTpl --*/
		assertEquals("costs should exist", (int)3, productData.getCostList().size());
		assertEquals("nuts should exist", (int)3, productData.getNutList().size());
		
		// check costs
			costChecked=0;
		z_idx=0;
			for(CostListDataItem c : productTplData.getCostList()){
				String costName = (String)nodeService.getProperty(c.getCost(), ContentModel.PROP_NAME);
				 					
				for(String s : costNames){
					if(s.equals(costName)){ 							
						assertEquals("Check cost value", costValues[z_idx], nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_COSTLIST_VALUE));
					costChecked++;
					break;
				} 						
			}
			z_idx++;
		}
		assertEquals("3 costs have been checked", (int)3, costChecked); 				 				
		
		// check nuts
		nutChecked=0;
		z_idx=0;
		for(NutListDataItem n : productTplData.getNutList()){
			String nutName = (String)nodeService.getProperty(n.getNut(), ContentModel.PROP_NAME); 					
			
			for(String s : nutNames){
				if(s.equals(nutName)){ 							
					assertEquals("Check nut value", nutValues[z_idx], nodeService.getProperty(n.getNodeRef(), BeCPGModel.PROP_NUTLIST_VALUE) );
					nutChecked++;
					break;
				} 						
			}
			z_idx++;
		}
		
		assertEquals("3 nuts have been checked", (int)3, nutChecked);
		
		/*
		* check products import in site, it is not classified
		*/		
		
		List<NodeRef> siteFoldernode = searchService.selectNodes(repository.getCompanyHome(), 
						PATH_SITE_FOLDER, 
						null, namespaceService, false);
		assertEquals("classif folder should exist", (int)1 , siteFoldernode.size());
		NodeRef siteFolderNodeRef = siteFoldernode.get(0);
		assertEquals("1 product should exist", (int)1 , fileFolderService.list(siteFolderNodeRef).size()); 				
		
		
		
		productName = "Saumon surgelé 80x20x4";
		product1NodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, productName); 				
		// productFolder => look for product
		if(product1NodeRef != null && nodeService.getType(product1NodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
			product1NodeRef = nodeService.getChildByName(product1NodeRef, ContentModel.ASSOC_CONTAINS, productName);
		}
		assertNotNull("product 1 should exist", product1NodeRef);		
		
	}
	
	public void testCatchIntegrityException() throws IOException, ImporterException{
		
		/**
		 * Test the catch of integrity exception
		 */
		Exception exception = null;
		try{
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
	 			@Override
				public NodeRef execute() throws Throwable {
	 				
	 				NodeRef tempFolder = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
	 				if(tempFolder != null){
	 					nodeService.deleteNode(tempFolder); 					 					
	 				}
	 				 				 				
	 				tempFolder = repoService.createFolderByPath(repository.getCompanyHome(), PATH_TEMP, PATH_TEMP);			
	 				
	 				RawMaterialData rmData = new RawMaterialData();
	 				rmData.setName("Name");
	 				rmData.setHierarchy1("ZZZZZZZ");
	 				
	 				try{
	 					NodeRef rmNodeRef = productDAO.create(repository.getCompanyHome(), rmData, null);
	 				}
	 				catch(Exception e){
	 					assertNotNull("Should not be null" ,e);
	 				}
	 				
	 				return null;

	 			}},false,true);	
		}
		catch(Exception e){
			exception = e;
		}
		
		assertNotNull("Check exception was thrown", exception);
		exception = null;
		
		/**
		 * Test the catch of integrity exception during import
		 */
		try{
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
	 			@Override
				public NodeRef execute() throws Throwable {
	 				
	 				/*-- Clean costs --*/ 				
	 				NodeRef systemFolder = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
	 				NodeRef costsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
	 				
	 				if(costsFolder != null){
	 					nodeService.deleteNode(costsFolder);
	 				}
	 				
	 				/*-- Create file to import --*/
	 		    	logger.debug("create file to import");
	 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
	 		    	properties.put(ContentModel.PROP_NAME, "Import-with-IntegrityException.csv");
	 		    	
	 		    	NodeRef nodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
	 		    	if(nodeRef != null){
	 		    		nodeService.deleteNode(nodeRef);   		
	 		    	}    	
	 		    	nodeRef = nodeService.createNode(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
	 		    	
	 		    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true); 		    	
	 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import-with-IntegrityException.csv");			
	 		    	writer.putContent(in);
	 		    	
	 				logger.debug("Start import");
	 				importService.importText(nodeRef, true, false); 		
	 				
	 				/*-- check nothing is imported --*/
	 				systemFolder = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
	 				costsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
	 				assertNull("costs should not exist", costsFolder);
	 				
	 				return null;
	
	 			}},false,true);
			
		}
		catch(Exception e){
			//logger.error("error as expected while importing file.", e);
			exception = e;
		}
		
		assertNotNull("Check exception was thrown", exception);
	}
	
	/**
	 * Test import product lists
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ImporterException the be cpg exception
	 */
	public void testImportProductLists() throws IOException, ImporterException{
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				/*-- Delete temp, products folder --*/
 				NodeRef tempNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);			
 				if(tempNodeRef != null)
 				{
 					fileFolderService.delete(tempNodeRef);    		
 				}
 				
 				NodeRef productsNodeRef = repoService.getFolderByPath(repository.getCompanyHome(), RepoConsts.PATH_PRODUCTS);			
 				if(productsNodeRef != null)
 				{
 					fileFolderService.delete(productsNodeRef);    		
 				}
 				
 				/*-- Add mapping file --*/
 				addMappingFile();
 				
 				/*-- Create file to import --*/
 		    	logger.debug("create file to import");
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
 		    	properties.put(ContentModel.PROP_NAME, "Import-Products.csv");
 		    	
 		    	NodeRef nodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(nodeRef != null){
 		    		nodeService.deleteNode(nodeRef);   		
 		    	}    	
 		    	nodeRef = nodeService.createNode(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef(); 		    	
 		    	
 		    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
 		    	logger.debug("Load import.csv");
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import-ProductLists.csv");			
 		    	logger.debug("import.csv loaded");
 		    	writer.putContent(in);
 		    	
 		    	/*
 		    	 * Disable product policy to avoid productCode policy
 		    	 */
 		    	policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_PRODUCT);
 		    	
 		    	try{
 		    		
 		    		logger.debug("Start import");
 	 				importService.importText(nodeRef, true, false);
 	 				
 		    	}
 		    	finally{
 		    		policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_PRODUCT);
 		    	}
 		    	
 				
 				/*-- check imported values --*/
 				tempNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
 				assertNotNull("Temp folder should exist", tempNodeRef);
 				NodeRef importFolderNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, PATH_PRODUCTS);
 				assertNotNull("import folder should exist", importFolderNodeRef);
 				assertEquals("import folder should be empty", (int)0 , fileFolderService.listFiles(importFolderNodeRef).size());
 				
 				// load folder where products have been moved in ./cm:Products/cm:ToValidate/cm:RawMaterial/cm:Sea_x0020_food/cm:Fish 				
 				List<NodeRef> nodes = searchService.selectNodes(repository.getCompanyHome(), 
																PATH_CLASSIF_FOLDER_RM, 
																null, namespaceService, false);
 				assertEquals("classif folder should exist", (int)1 , nodes.size());
 				productsNodeRef = nodes.get(0);
 				assertEquals("3 rm should exist", (int)3 , fileFolderService.listFiles(productsNodeRef).size());
 				
 				nodes = searchService.selectNodes(repository.getCompanyHome(), 
						PATH_CLASSIF_FOLDER_FP, 
						null, namespaceService, false);
				assertEquals("classif folder should exist", (int)1 , nodes.size());
				productsNodeRef = nodes.get(0);
				assertEquals("1 finished product should exist", (int)1 , fileFolderService.listFiles(productsNodeRef).size());
 				
 				
 				/*
 				 * check products
 				 */
 				
 				NodeRef product1NodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, "Saumon surgelé 80x20x4");
 				assertNotNull("product 1 should exist", product1NodeRef);
 				List<QName> dataLists = new ArrayList<QName>();
 				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
 				ProductData productData = productDAO.find(product1NodeRef, dataLists); 				 			
 				
 				/*-- check productLists --*/
 				assertEquals("compoList should exist", (int)3, productData.getCompoList().size()); 				
 				String [] rmNames = {"MP1","MP2", "MP3"};
 				float [] qtyValues = {1.0f, 2.0f, 3.2f};
 				String [] unitValues = {"g","kg", "g"};
 				
 				// check MP
 				int rmChecked=0;
				int z_idx=0;
 				for(CompoListDataItem c : productData.getCompoList()){
 					String rmName = (String)nodeService.getProperty(c.getProduct(), ContentModel.PROP_NAME);
 					 					
 					for(String s : rmNames){
 						if(s.equals(rmName)){ 							
 							assertEquals("Check rm value", qtyValues[z_idx], nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_COMPOLIST_QTY));
 							assertEquals("Check rm unit", unitValues[z_idx], nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_COMPOLIST_UNIT));
 							rmChecked++;
 							break;
 						} 						
 					}
 					z_idx++;
 				}
 				assertEquals("3 rm have been checked", (int)3, rmChecked); 				 				
 				
 				return null;

 			}},false,true);		
	}
}
