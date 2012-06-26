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
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.hierarchy.HierarchyService;
import fr.becpg.repo.search.BeCPGSearchService;
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
	
	private static String PATH_CLASSIF_FOLDER_FP = "./cm:Products/cm:ToValidate/cm:FinishedProduct/cm:Frozen/cm:Pizza";
	
	private static String PATH_SITE_FOLDER = "./st:sites/cm:folder";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportServiceTest.class);
	
	
	/** The import service. */
	private ImportService importService;
	
	/** The ml node service impl. */
	private NodeService mlNodeServiceImpl;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The search service. */
	private SearchService searchService;
	
	/** The namespace service. */
	private NamespaceService namespaceService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private HierarchyService hierarchyService;
	
	private BeCPGSearchService beCPGSearchService;
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
    	super.setUp();		
    	
    	importService = (ImportService)ctx.getBean("importService");
        authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");      
        mlNodeServiceImpl = (NodeService) ctx.getBean("mlAwareNodeService");
        mimetypeService = (MimetypeService)ctx.getBean("mimetypeService");
        fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        productDAO = (ProductDAO)ctx.getBean("productDAO");
        searchService = (SearchService)ctx.getBean("searchService");
        namespaceService = (NamespaceService)ctx.getBean("namespaceService");      
        policyBehaviourFilter = (BehaviourFilter)ctx.getBean("policyBehaviourFilter");
        hierarchyService = (HierarchyService)ctx.getBean("hierarchyService");
        beCPGSearchService = (BeCPGSearchService)ctx.getBean("beCPGSearchService");
        
        cleanTempFolder();
    }
    
	private void cleanTempFolder(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
				if (folderNodeRef != null) {
					fileFolderService.delete(folderNodeRef);
				}				
				return null;
			}
		}, false, true);
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
 		    	
 		    	NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(nodeRef != null){
 		    		nodeService.deleteNode(nodeRef);   		
 		    	}    	
 		    	nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
 		    	
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
		NodeRef systemFolder = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		assertNotNull("system folder should exist", systemFolder);
		NodeRef ingsFolder = entitySystemService.getSystemEntityDataList(systemFolder, RepoConsts.PATH_CHARACTS, RepoConsts.PATH_INGS);
		assertNotNull("ings folder should exist", ingsFolder);
		
		// Abricot 				 				 				
		NodeRef ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Abricot");
		assertNotNull("abricot ing should exist", ingNodeRef);
		MLText mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Abricot default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Abricot english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Abricot french", mlText.getValue(Locale.FRENCH));
		
		// Acerola
		ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Acerola");
		assertNotNull("Acerola ing should exist", ingNodeRef);
		mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Acerola default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Acerola english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Acerola french", mlText.getValue(Locale.FRENCH));
		
		// Abricot1
		ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Abricot1");
		assertNotNull("Abricot1 ing should exist", ingNodeRef);
		mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Abricot1 default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Abricot1 english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Abricot1 french", mlText.getValue(Locale.FRENCH));
		
		// Acerola1
		ingNodeRef = nodeService.getChildByName(ingsFolder, ContentModel.ASSOC_CONTAINS, "Acerola1");
		assertNotNull("Acerola1 ing should exist", ingNodeRef);
		mlText = (MLText)mlNodeServiceImpl.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
		assertNotNull("MLText exist", mlText);
		assertEquals("MLText exist has 3 Locales", 3, mlText.getLocales().size());
		assertEquals("Check default value", "Acerola1 default", mlText.getValue(Locale.getDefault()));
		assertEquals("Check english value", "Acerola1 english", mlText.getValue(Locale.ENGLISH));
		assertEquals("Check french value", "Acerola1 french", mlText.getValue(Locale.FRENCH));
	}
	
	/**
	 * Adds the mapping file.
	 */
	private void addMappingFile(String mappingName, String mappingPath){
					
		logger.debug("/*-- Add mapping file --*/");
		
		NodeRef systemFolder = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		if(systemFolder == null) 
			systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
		
		NodeRef exchangeFolder = repoService.getFolderByPath(systemFolder, RepoConsts.PATH_EXCHANGE);
		if(exchangeFolder == null) 
			exchangeFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_EXCHANGE, TranslateHelper.getTranslatedPath(RepoConsts.PATH_EXCHANGE));
		
		NodeRef importFolder = repoService.getFolderByPath(exchangeFolder, RepoConsts.PATH_IMPORT);
		if(importFolder == null)			
			importFolder = repoService.createFolderByPath(exchangeFolder, RepoConsts.PATH_IMPORT, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMPORT));
		
		NodeRef mappingFolder = repoService.getFolderByPath(importFolder, RepoConsts.PATH_MAPPING);
		if(mappingFolder == null)			
			mappingFolder = repoService.createFolderByPath(importFolder, RepoConsts.PATH_MAPPING, TranslateHelper.getTranslatedPath(RepoConsts.PATH_MAPPING));
		
		NodeRef productsMappingNodeRef = nodeService.getChildByName(mappingFolder, ContentModel.ASSOC_CONTAINS, mappingName);
		if(productsMappingNodeRef != null){
			nodeService.deleteNode(productsMappingNodeRef);
		}
				
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, mappingName);
    	productsMappingNodeRef = nodeService.createNode(mappingFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();    	    	
    	
    	ContentWriter writer = contentService.getWriter(productsMappingNodeRef, ContentModel.PROP_CONTENT, true);
    	String mappingFilePath = System.getProperty("user.dir" )  + mappingPath;
    	
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
 				
 				NodeRef tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);			
 				if(tempNodeRef != null)
 				{
 					logger.debug("delete temp folder");
 					fileFolderService.delete(tempNodeRef);    		
 				} 				
 				
 				// remove products 				
 				NodeRef productsNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_PRODUCTS);			
 				if(productsNodeRef != null)
 				{
 					logger.debug("delete products folder");
 					fileFolderService.delete(productsNodeRef);    		
 				}
 				
				// remove companies				
				NodeRef companiesFolder = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_COMPANIES);
				
				if(companiesFolder != null){
					logger.debug("delete companies folder");
					nodeService.deleteNode(companiesFolder);
				}
				
				// remove site folder
				List<NodeRef> siteFoldernode = searchService.selectNodes(repositoryHelper.getCompanyHome(), 
						PATH_SITE_FOLDER, 
						null, namespaceService, false);
				
				if(siteFoldernode != null && siteFoldernode.size() > 0){
					logger.debug("delete site folder");
					nodeService.deleteNode(siteFoldernode.get(0));
				}			
				
 				addMappingFile("Products.xml", "/src/main/resources/beCPG/import/mapping/Products.xml");
 				addMappingFile("TestProducts.xml", "/src/test/resources/beCPG/import/mapping/TestProducts.xml");
 				
 				return null;

 			}},false,true);
		
		/*
		 * Create file to import
		 */
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable { 				 				
 				
 		    	logger.debug("create file to import");
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
 		    	properties.put(ContentModel.PROP_NAME, "Import-Products.csv");
 		    	
 		    	NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(nodeRef != null){
 		    		nodeService.deleteNode(nodeRef);   		
 		    	}    	
 		    	nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef(); 		    	
 		    	
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
		NodeRef tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
		assertNotNull("Temp folder should exist", tempNodeRef);
		NodeRef importFolderNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, PATH_PRODUCTS);
		assertNotNull("import folder should exist", importFolderNodeRef);
		assertEquals("import folder has one the productTpl", (int)1 , fileFolderService.listFiles(importFolderNodeRef).size());
		
		// load folder where products have been moved in ./cm:Products/cm:ToValidate/cm:RawMaterial/cm:Sea_x0020_food/cm:Fish 				
		List<NodeRef> nodes = searchService.selectNodes(repositoryHelper.getCompanyHome(), 
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
		assertEquals("saumon sugelé", productData.getLegalName().getDefaultValue());
		assertEquals("Sea food",  HierarchyHelper.getHierachyName(productData.getHierarchy1(),nodeService));
		assertEquals("Fish", HierarchyHelper.getHierachyName(productData.getHierarchy2(),nodeService));
		
		/*-- check associations --*/
		List<AssociationRef> supplierAssocRefs = nodeService.getTargetAssocs(product1NodeRef, BeCPGModel.ASSOC_SUPPLIERS);
		assertEquals("check product has 2 suppliers defined", 2, supplierAssocRefs.size());
		String supplier1Code = (String)nodeService.getProperty(supplierAssocRefs.get(0).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
		String supplier2Code = (String)nodeService.getProperty(supplierAssocRefs.get(1).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
		assertEquals("check supplier name", "1000012", supplier1Code);
		assertEquals("check supplier name", "1000013", supplier2Code);
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
		supplier1Code = (String)nodeService.getProperty(supplierAssocRefs.get(0).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
		supplier2Code = (String)nodeService.getProperty(supplierAssocRefs.get(1).getTargetRef(), BeCPGModel.PROP_ERP_CODE);
		assertEquals("check supplier name", "1000012", supplier1Code);
		assertEquals("check supplier name", "1000014", supplier2Code);
		
		/*-- check productLists --*/
		assertEquals("costs should exist", (int)2, productData.getCostList().size());
		assertEquals("nuts should exist", (int)3, productData.getNutList().size());
		String [] costNames = {"Coût MP", "Coût Emb"};
		double [] costValues = {1.0d, 3.1d};
		String [] nutNames = {"Protéines","Lipides", "Glucides"};
		double [] nutValues = {2.5d, 3.6d, 5.6d};
		
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
		assertEquals("2 costs have been checked", (int)2, costChecked); 				 				
		
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
		NodeRef parentNodeRef = nodeService.getPrimaryParent(product2NodeRef).getParentRef(); 				
		NodeRef imagesNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, "Images");
		assertNotNull("check Images exits", imagesNodeRef);
		NodeRef imgNodeRef = nodeService.getChildByName(imagesNodeRef, ContentModel.ASSOC_CONTAINS, "produit.jpg");
		assertNotNull("check produit.jpg exits", imgNodeRef);
		assertEquals("Check title on image", "sushi saumon", nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));
		
		
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
		assertEquals("costs should exist", (int)2, productData.getCostList().size());
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
		assertEquals("2 costs have been checked", (int)2, costChecked); 				 				
		
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
		
		List<NodeRef> siteFoldernode = searchService.selectNodes(repositoryHelper.getCompanyHome(), 
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
		
		Exception exception = null;
		
		/**
		 * Test the catch of integrity exception during import
		 */
		try{
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
	 			@Override
				public NodeRef execute() throws Throwable {
	 				
	 				/*-- Clean costs --*/ 				
	 				NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
	 				NodeRef costsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
	 				
	 				if(costsFolder != null){
	 					nodeService.deleteNode(costsFolder);
	 				}
	 				
	 				/*-- Create file to import --*/
	 		    	logger.debug("create file to import");
	 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
	 		    	properties.put(ContentModel.PROP_NAME, "Import-with-IntegrityException.csv");
	 		    	
	 		    	NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
	 		    	if(nodeRef != null){
	 		    		nodeService.deleteNode(nodeRef);   		
	 		    	}    	
	 		    	nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
	 		    	
	 		    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true); 		    	
	 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import-with-IntegrityException.csv");			
	 		    	writer.putContent(in);
	 		    	
	 				logger.debug("Start import");
	 				importService.importText(nodeRef, true, false); 		
	 				
	 				/*-- check nothing is imported --*/
	 				systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
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
 				NodeRef tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);			
 				if(tempNodeRef != null)
 				{
 					fileFolderService.delete(tempNodeRef);    		
 				}
 				
 				NodeRef productsNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_PRODUCTS);			
 				if(productsNodeRef != null)
 				{
 					fileFolderService.delete(productsNodeRef);    		
 				}
 				
 				/*-- Add mapping file --*/
 				addMappingFile("Products.xml", "/src/main/resources/beCPG/import/mapping/Products.xml");
 				
 				/*-- Create file to import --*/
 		    	logger.debug("create file to import");
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
 		    	properties.put(ContentModel.PROP_NAME, "Import-Products.csv");
 		    	
 		    	NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(nodeRef != null){
 		    		nodeService.deleteNode(nodeRef);   		
 		    	}    	
 		    	nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef(); 		    	
 		    	
 		    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
 		    	logger.debug("Load import.csv");
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import-ProductLists.csv");			
 		    	logger.debug("import.csv loaded");
 		    	writer.putContent(in);
 		    	
 		    	/*
 		    	 * Disable product policy to avoid productCode policy
 		    	 */
 		    	policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY);
 		    		
	    		logger.debug("Start import");
 				importService.importText(nodeRef, true, false);
 	 			
 				/*-- check imported values --*/
 				tempNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMP);
 				assertNotNull("Temp folder should exist", tempNodeRef);
 				NodeRef importFolderNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, PATH_PRODUCTS);
 				assertNotNull("import folder should exist", importFolderNodeRef);
 				assertEquals("import folder should be empty", (int)0 , fileFolderService.listFiles(importFolderNodeRef).size());
 				
 				// load folder where products have been moved in ./cm:Products/cm:ToValidate/cm:RawMaterial/cm:Sea_x0020_food/cm:Fish 				
 				List<NodeRef> nodes = searchService.selectNodes(repositoryHelper.getCompanyHome(), 
																PATH_CLASSIF_FOLDER_RM, 
																null, namespaceService, false);
 				assertEquals("classif folder should exist", (int)1 , nodes.size());
 				productsNodeRef = nodes.get(0);
 				assertEquals("3 rm should exist", (int)3 , fileFolderService.listFolders(productsNodeRef).size());
 				
 				nodes = searchService.selectNodes(repositoryHelper.getCompanyHome(), 
						PATH_CLASSIF_FOLDER_FP, 
						null, namespaceService, false);
				assertEquals("classif folder should exist", (int)1 , nodes.size());
				productsNodeRef = nodes.get(0);
				assertEquals("1 finished product should exist", (int)1 , fileFolderService.listFolders(productsNodeRef).size());
 				
 				
 				/*
 				 * check products
 				 */
 				
 				NodeRef product1NodeRef = nodeService.getChildByName(productsNodeRef, ContentModel.ASSOC_CONTAINS, "Saumon surgelé 80x20x4");
 				if(product1NodeRef != null && nodeService.getType(product1NodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
 					product1NodeRef = nodeService.getChildByName(product1NodeRef, ContentModel.ASSOC_CONTAINS, "Saumon surgelé 80x20x4");
 				}
 				assertNotNull("product 1 should exist", product1NodeRef);
 				List<QName> dataLists = new ArrayList<QName>();
 				dataLists.add(BeCPGModel.TYPE_COMPOLIST); 				
 				ProductData productData = productDAO.find(product1NodeRef, dataLists); 				 			
 				
 				/*-- check productLists --*/
 				assertEquals("compoList should exist", (int)3, productData.getCompoList().size()); 				
 				String [] rmNames = {"MP1","MP2", "MP3"};
 				double [] qtyValues = {1.0d, 2.0d, 3.2d};
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
	
	/**
	 * Test import text.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ImporterException the be cpg exception
	 */
	public void testImportHierarchies() throws IOException, ImporterException{
	
		importHierarchies();
		
		// 2nd time to check they are updated and not recreated
		importHierarchies();
		
		//check		
		String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ROOT, 
				LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(BeCPGModel.TYPE_RAWMATERIAL,namespaceService)),
				"USDA");
		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_NO_LIMIT);
		assertEquals(1, ret.size());
		
		queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, 
				LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(BeCPGModel.TYPE_RAWMATERIAL,namespaceService)), ret.get(0),
				"Dairy and Egg Products");
		ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_NO_LIMIT);
		assertEquals(1, ret.size());
		
	}
	
	private void importHierarchies(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 				
 				/*-- Create file to import --*/
 		    	logger.debug("create file to import");
 		    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
 		    	properties.put(ContentModel.PROP_NAME, "import-productHierarchies.csv");
 		    	
 		    	NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));    	
 		    	if(nodeRef != null){
 		    		nodeService.deleteNode(nodeRef);   		
 		    	}    	
 		    	nodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
 		    	
 		    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
 		    	logger.debug("Load import.csv");
 		    	InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/import-productHierarchies.csv");			
 		    	logger.debug("import.csv loaded");
 		    	writer.putContent(in);
 		    	
 				logger.debug("Start import");
 				importService.importText(nodeRef, true, false);
 				
 				return null;

 			}},false,true);

		/*-- Check hierarchies --*/
		logger.debug("Check hierarchies");
		NodeRef hierarchy1USDA = hierarchyService.getHierarchy1(BeCPGModel.TYPE_RAWMATERIAL, "USDA");
		assertNotNull(hierarchy1USDA);
		NodeRef hierarchy2Dairy = hierarchyService.getHierarchy2(BeCPGModel.TYPE_RAWMATERIAL, hierarchy1USDA, "Dairy and Egg Products");
		assertNotNull(hierarchy2Dairy);
		NodeRef hierarchy2Spices = hierarchyService.getHierarchy2(BeCPGModel.TYPE_RAWMATERIAL, hierarchy1USDA, "Spices and Herbs");	
		assertNotNull(hierarchy2Spices);
		
		// check unicity
		List<NodeRef> listItems = beCPGSearchService.luceneSearch(LuceneHelper.getCondEqualID(hierarchy1USDA, null), RepoConsts.MAX_RESULTS_NO_LIMIT);
		assertEquals(1, listItems.size());
		listItems = beCPGSearchService.luceneSearch(LuceneHelper.getCondEqualID(hierarchy2Dairy, null), RepoConsts.MAX_RESULTS_NO_LIMIT);
		assertEquals(1, listItems.size());
		listItems = beCPGSearchService.luceneSearch(LuceneHelper.getCondEqualID(hierarchy2Spices, null), RepoConsts.MAX_RESULTS_NO_LIMIT);
		assertEquals(1, listItems.size());		
	}
}
