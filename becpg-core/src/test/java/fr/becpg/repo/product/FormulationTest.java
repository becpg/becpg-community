/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem.NullableBoolean;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.sort.NutListDataItemDecorator;
import fr.becpg.repo.product.data.productList.sort.NutListSortComparator;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class FormulationTest.
 *
 * @author querephi
 */
public class FormulationTest extends RepoBaseTestCase {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(FormulationTest.class);
	
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
	
	/** The product service. */
	private ProductService productService;    
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	private TransactionService transactionService;
	
	/** The repository helper. */
	private Repository repositoryHelper;   
    
    /** The PAT h_ productfolder. */
    private static String PATH_PRODUCTFOLDER = "TestProductFolder";
    
    /** The GROU p1. */
    private static String GROUP1 = "Groupe 1";      
    
    /** The GROU p2. */
    private static String GROUP2 = "Groupe 2";
    
    /** The GROUPOTHER. */
    private static String GROUPOTHER = "Autre";
    
    /** The GROU p_ garniture. */
    private static String GROUP_GARNITURE = "Garniture";
    
    /** The GROU p_ pate. */
    private static String GROUP_PATE = "Pâte";
    
    private static String PACKAGING_PRIMAIRE = "Primaire";
    private static String PACKAGING_TERTIAIRE = "Tertiaire";
    
    public static final String  FLOAT_FORMAT = "0.0000";
    
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
    
    private NodeRef  localSF3NodeRef;
    
    /** The raw material3 node ref. */
    private NodeRef  rawMaterial3NodeRef;
    
    /** The raw material4 node ref. */
    private NodeRef  rawMaterial4NodeRef;
    
    /** The raw material5 node ref. */
    private NodeRef  rawMaterial5NodeRef;
    
    /** The local s f11 node ref. */
    private NodeRef localSF11NodeRef;
    
    /** The raw material11 node ref. */
    private NodeRef rawMaterial11NodeRef;
    
    /** The raw material12 node ref. */
    private NodeRef rawMaterial12NodeRef;
    
    /** The local s f12 node ref. */
    private NodeRef localSF12NodeRef;
    
    /** The raw material13 node ref. */
    private NodeRef rawMaterial13NodeRef;
    
    /** The raw material14 node ref. */
    private NodeRef rawMaterial14NodeRef;
    
    private NodeRef packagingMaterial1NodeRef;
    private NodeRef packagingMaterial2NodeRef;
    private NodeRef packagingMaterial3NodeRef;
    
    /** The cost1. */
    private NodeRef cost1;
    
    /** The cost2. */
    private NodeRef cost2;
    
    private NodeRef pkgCost1;
    
    private NodeRef pkgCost2;
    
    /** The nut1. */
    private NodeRef nut1;
    
    /** The nut2. */
    private NodeRef nut2;
    
    /** The allergen1. */
    private NodeRef allergen1;
    
    /** The allergen2. */
    private NodeRef allergen2;
    
    /** The allergen3. */
    private NodeRef allergen3;
    
    /** The allergen4. */
    private NodeRef allergen4;
    
    /** The ing1. */
    private NodeRef ing1;
    
    /** The ing2. */
    private NodeRef ing2;
    
    /** The ing3. */
    private NodeRef ing3;
    
    /** The ing4. */
    private NodeRef ing4;
    
    /** The bio origin1. */
    private NodeRef bioOrigin1;
    
    /** The bio origin2. */
    private NodeRef bioOrigin2;
    
    /** The geo origin1. */
    private NodeRef geoOrigin1;
    
    /** The geo origin2. */
    private NodeRef geoOrigin2;
    
    /* (non-Javadoc)
     * @see fr.becpg.test.RepoBaseTestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
    	super.setUp();		
   
    	logger.debug("ProductMgrTest:setUp");
    	    	
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	searchService = (SearchService)appCtx.getBean("searchService");
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");
    	productService = (ProductService)appCtx.getBean("productService");       
        productDAO = (ProductDAO)appCtx.getBean("productDAO");
        productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
        transactionService = (TransactionService)appCtx.getBean("TransactionService");
        repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			public NodeRef execute() throws Throwable {

 				// delete report tpls to avoid report generation
 				deleteReportTpls();
 				
 				return null;

 			}},false,true);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			public NodeRef execute() throws Throwable {
 				
 				//create RM and lSF
 				initParts();
 		        
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
	 * Inits the parts.
	 */
	private void initParts(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   
			
			/*-- Create test folder --*/
			folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_PRODUCTFOLDER);			
			if(folderNodeRef != null)
			{
				nodeService.deleteNode(folderNodeRef);    		
			}			
			folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_PRODUCTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
			
			/*-- characteristics --*/
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			//Costs
			properties.put(ContentModel.PROP_NAME, "cost1");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			cost1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "cost2");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			cost2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "pkgCost1");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			pkgCost1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "pkgCost2");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			pkgCost2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			//Nuts
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "nut1");
			properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
			properties.put(BeCPGModel.PROP_NUTGROUP, GROUP1);
			nut1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "nut2");
			properties.put(BeCPGModel.PROP_NUTUNIT, "kcal");
			properties.put(BeCPGModel.PROP_NUTGROUP, GROUP2);
			nut2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();			
			//Allergens
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen1");			 					 				
			allergen1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen2");			 					 				
			allergen2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen3");			 					 				
			allergen3 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen4");			 					 				
			allergen4 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			//Ings
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing1");
			MLText mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing1 default");
			mlName.addValue(Locale.ENGLISH, "ing1 english");
			mlName.addValue(Locale.FRENCH, "ing1 french");	
			properties.put(BeCPGModel.PROP_ING_MLNAME, mlName);
			properties.put(BeCPGModel.PROP_ING_TYPE, "Ingrédient");
			ing1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing2");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing2 default");
			mlName.addValue(Locale.ENGLISH, "ing2 english");
			mlName.addValue(Locale.FRENCH, "ing2 french");	
			properties.put(BeCPGModel.PROP_ING_MLNAME, mlName);
			properties.put(BeCPGModel.PROP_ING_TYPE, "Ingrédient");
			ing2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing3");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing3 default");
			mlName.addValue(Locale.ENGLISH, "ing3 english");
			mlName.addValue(Locale.FRENCH, "ing3 french");	
			properties.put(BeCPGModel.PROP_ING_MLNAME, mlName);
			properties.put(BeCPGModel.PROP_ING_TYPE, "Ingrédient");
			ing3 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.put(ContentModel.PROP_NAME, "ing4");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing4 default");
			mlName.addValue(Locale.ENGLISH, "ing4 english");
			mlName.addValue(Locale.FRENCH, "ing4 french");	
			properties.put(BeCPGModel.PROP_ING_MLNAME, mlName);
			properties.put(BeCPGModel.PROP_ING_TYPE, "Ingrédient");
			ing4 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			//Geo origins
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "geoOrigin1");
			geoOrigin1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "geoOrigin2");
			geoOrigin2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			//Bio origins
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "bioOrigin1");
			bioOrigin1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_BIO_ORIGIN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "bioOrigin2");
			bioOrigin2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_BIO_ORIGIN, properties).getChildRef();
			
			/*-- Create raw materials --*/
			logger.debug("/*-- Create raw materials --*/");
			 Collection<QName> dataLists = productDictionaryService.getDataLists();
			/*-- Raw material 1 --*/
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			rawMaterial1.setLegalName("Legal Raw material 1");
			//costList
			List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 3f, "€/kg", cost1));
			costList.add(new CostListDataItem(null, 2f, "€/kg", cost2));
			rawMaterial1.setCostList(costList);
			//nutList
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f, 0f, "Groupe 1", nut1));
			nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f, 0f, "Groupe 1", nut2));
			rawMaterial1.setNutList(nutList);
			//allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
			allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergen1));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen2));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen3));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen4));
			rawMaterial1.setAllergenList(allergenList);
			//ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ; geo1|geo2 
			List<IngListDataItem> ingList = new ArrayList<IngListDataItem>();
			List<NodeRef> bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			List<NodeRef> geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, false, false, ing1));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 2f, geoOrigins, bioOrigins, false, false, ing2));
			rawMaterial1.setIngList(ingList);
			rawMaterial1NodeRef = productDAO.create(folderNodeRef, rawMaterial1, dataLists);
			
			/*-- Raw material 2 --*/
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			rawMaterial2.setLegalName("Legal Raw material 2");
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 1f, "€/kg", cost1));
			costList.add(new CostListDataItem(null, 2f, "€/kg", cost2));
			rawMaterial2.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut1));
			nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f,  0f, "Groupe 1", nut2));
			rawMaterial2.setNutList(nutList);
			//allergenList
			allergenList = new ArrayList<AllergenListDataItem>();
			allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergen1));
			allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergen2));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen3));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen4));
			rawMaterial2.setAllergenList(allergenList);
			//ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, true, true, ing1));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 3f, geoOrigins, bioOrigins, false, false, ing2));
			rawMaterial2.setIngList(ingList);			
			rawMaterial2NodeRef = productDAO.create(folderNodeRef, rawMaterial2, dataLists);
			
			/*-- Raw material 3 --*/
			RawMaterialData rawMaterial3 = new RawMaterialData();
			rawMaterial3.setName("Raw material 3");
			rawMaterial3.setLegalName("Legal Raw material 3");
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 1f, "€/kg", cost1));
			costList.add(new CostListDataItem(null, 2f, "€/kg", cost2));
			rawMaterial3.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut1));
			nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f,  0f, "Groupe 1", nut2));
			rawMaterial3.setNutList(nutList);
			//allergenList
			allergenList = new ArrayList<AllergenListDataItem>();
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen1));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen2));
			allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergen3));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen4));
			rawMaterial3.setAllergenList(allergenList);
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3));			
			rawMaterial3.setIngList(ingList);		
			rawMaterial3NodeRef = productDAO.create(folderNodeRef, rawMaterial3, dataLists);
			
			/*-- Raw material 4 --*/
			RawMaterialData rawMaterial4 = new RawMaterialData();
			rawMaterial4.setName("Raw material 4");
			rawMaterial4.setLegalName("Legal Raw material 4");	
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3));			
			rawMaterial4.setIngList(ingList);		
			rawMaterial4NodeRef = productDAO.create(folderNodeRef, rawMaterial4, dataLists);
			
			/*-- Raw material 5 --*/
			RawMaterialData rawMaterial5 = new RawMaterialData();
			rawMaterial5.setName("Raw material 5");
			rawMaterial5.setLegalName("Legal Raw material 5");
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 5f, "€/m", cost1));
			costList.add(new CostListDataItem(null, 6f, "€/m", cost2));
			rawMaterial5.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut1));
			nutList.add(new NutListDataItem(null, 3f, "g/100g", 0f,  0f, "Groupe 1", nut2));
			rawMaterial5.setNutList(nutList);					
			rawMaterial5.setIngList(ingList);		
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
			
			LocalSemiFinishedProduct localSF3 = new LocalSemiFinishedProduct();
			localSF3.setName("Local semi finished 3");
			localSF3.setLegalName("Legal Local semi finished 3");							
			localSF3NodeRef = productDAO.create(folderNodeRef, localSF3, dataLists);			
			
			logger.debug("/*-- Create raw materials 11 => 14 with ingList only--*/");
			/*-- Raw material 11 --*/
			RawMaterialData rawMaterial11 = new RawMaterialData();
			rawMaterial11.setName("Raw material 11");
			rawMaterial11.setLegalName("Legal Raw material 11");
			//ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ; geo1|geo2 
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, false, false, ing1));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 2f, geoOrigins, bioOrigins, false, false, ing2));
			rawMaterial11.setIngList(ingList);
			rawMaterial11NodeRef = productDAO.create(folderNodeRef, rawMaterial11, dataLists);
			
			/*-- Raw material 12 --*/
			RawMaterialData rawMaterial12 = new RawMaterialData();
			rawMaterial12.setName("Raw material 12");
			rawMaterial12.setLegalName("Legal Raw material 12");
			//ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, true, true, ing1));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 3f, geoOrigins, bioOrigins, false, false, ing2));
			rawMaterial12.setIngList(ingList);			
			rawMaterial12NodeRef = productDAO.create(folderNodeRef, rawMaterial12, dataLists);
			
			/*-- Raw material 13 --*/
			RawMaterialData rawMaterial13 = new RawMaterialData();
			rawMaterial13.setName("Raw material 13");
			rawMaterial13.setLegalName("Legal Raw material 13");	
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3));			
			rawMaterial13.setIngList(ingList);		
			rawMaterial13NodeRef = productDAO.create(folderNodeRef, rawMaterial13, dataLists);
			
			/*-- Raw material 14 --*/
			RawMaterialData rawMaterial14 = new RawMaterialData();
			rawMaterial14.setName("Raw material 14");
			rawMaterial14.setLegalName("Legal Raw material 14");
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3));
			ingList.add(new IngListDataItem(null, 2f, geoOrigins, bioOrigins, true, true, ing4));
			rawMaterial14.setIngList(ingList);		
			rawMaterial14NodeRef = productDAO.create(folderNodeRef, rawMaterial14, dataLists);
			
			/*-- Local semi finished product 11 --*/
			LocalSemiFinishedProduct localSF11 = new LocalSemiFinishedProduct();
			localSF11.setName("Local semi finished 11");
			localSF11.setLegalName("Legal Local semi finished 11");			
			localSF11NodeRef = productDAO.create(folderNodeRef, localSF11, dataLists);
			
			/*-- Local semi finished product 12 --*/
			LocalSemiFinishedProduct localSF12 = new LocalSemiFinishedProduct();
			localSF12.setName("Local semi finished 12");
			localSF12.setLegalName("Legal Local semi finished 12");					
			localSF12NodeRef = productDAO.create(folderNodeRef, localSF12, dataLists);
			
			return null;

			}},false,true);
	}
	
	/**
	 * Test formulate product.
	 *
	 * @throws Exception the exception
	 */
	public void testFormulateProduct() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2f);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);
				
				logger.debug("unit of product to formulate: " + finishedProduct.getUnit());
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				logger.debug("unit of product formulated: " + finishedProduct.getUnit());
				
				//costs
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0f, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0f, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 3, actual values: " + trace, 3f, nutListDataItem.getValue());
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6f, nutListDataItem.getValue());
						assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					}
				}
				//allergens			
				assertNotNull("AllergenList is null", formulatedProduct.getAllergenList());
				for(AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()){
					String voluntarySources = "";
					for(NodeRef part : allergenListDataItem.getVoluntarySources())
						voluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String inVoluntarySources = "";
					for(NodeRef part : allergenListDataItem.getInVoluntarySources())
						inVoluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String trace= "allergen: " + nodeService.getProperty(allergenListDataItem.getAllergen(), ContentModel.PROP_NAME) + " - voluntary: " + allergenListDataItem.getVoluntary().booleanValue() + " - involuntary: " + allergenListDataItem.getInVoluntary().booleanValue() + " - voluntary sources:" + voluntarySources + " - involuntary sources:" + inVoluntarySources;
					logger.debug(trace);
					
					//allergen1 - voluntary: true - involuntary: false - voluntary sources:Raw material 1, Raw material 2 - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen1)){
						assertEquals("allergen1.getVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen1.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen1.getVoluntarySources() contains Raw material 1, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial1NodeRef));
						assertEquals("allergen1.getVoluntarySources() contains Raw material 2, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial2NodeRef));		
						assertEquals("allergen1.getInVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getInVoluntarySources().size());
					}
					//allergen2 - voluntary: false - involuntary: true - voluntary sources: - involuntary sources:Raw material 2,
					if(allergenListDataItem.getAllergen().equals(allergen2)){
						assertEquals("allergen2.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen2.getInVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getInVoluntary().booleanValue());					
						assertEquals("allergen2.getInVoluntarySources() contains Raw material 2, actual values: " + trace, true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial2NodeRef));				
						assertEquals("allergen2.getVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getVoluntarySources().size());
					}
					//allergen: allergen3 - voluntary: true - involuntary: true - voluntary sources:Raw material 3,  - involuntary sources:Raw material 3,
					if(allergenListDataItem.getAllergen().equals(allergen3)){
						assertEquals("allergen3.getVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen3.getInVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen3.getVoluntarySources() contains Raw material 3, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial3NodeRef));				
						assertEquals("allergen3.getInVoluntarySources() contains Raw material 3, actual values: " + trace, true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial3NodeRef));
					}
					//allergen4 - voluntary: false - involuntary: false - voluntary sources: - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen4)){
						assertEquals("allergen4.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen4.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen4.getVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getVoluntarySources().size());
						assertEquals("allergen4.getInVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getInVoluntarySources().size());
					}				
				}
				//verify IngList
				// 1 * RM1				, ingList : 		1 ing1 ; bio1 ; geo1		// 2 ing2 ; bio1 ; geo1|geo2	//
				// 2 * RM2				, ingList : 		1 ing1 ; bio1 ; geo1		// 3 ing2 ; bio2 ; geo1|geo2	//
				// 3 * RM3				, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2
				// 3 * RM4 [OMIT]	, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2
				assertNotNull("IngList is null", formulatedProduct.getIngList());
				for(IngListDataItem ingListDataItem : formulatedProduct.getIngList()){
				
					String geoOriginsText = "";
					for(NodeRef geoOrigin : ingListDataItem.getGeoOrigin())
						geoOriginsText += nodeService.getProperty(geoOrigin, ContentModel.PROP_NAME) + ", ";
					
					String bioOriginsText = "";
					for(NodeRef bioOrigin : ingListDataItem.getBioOrigin())
						bioOriginsText += nodeService.getProperty(bioOrigin, ContentModel.PROP_NAME) + ", ";
					
					String trace= "ing: " + nodeService.getProperty(ingListDataItem.getIng(), ContentModel.PROP_NAME) + " - qty: " + ingListDataItem.getQtyPerc() + " - geo origins: " + geoOriginsText + " - bio origins: " + bioOriginsText + " is gmo: " + ingListDataItem.isGMO().booleanValue() + " is ionized: " + ingListDataItem.isIonized().booleanValue();
					logger.debug(trace);
					
					DecimalFormat df = new DecimalFormat("0.000000");
					
					//ing: ing1 - qty: 13.043478 - geo origins: geoOrigin1,  - bio origins: bioOrigin1,  is gmo: true
					if(ingListDataItem.getIng().equals(ing1)){
						assertEquals("ing1.getQtyPerc() == 13.043478, actual values: " + trace,  df.format(13.043478), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace, false, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing1.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue() == true);
						assertEquals("ing1.isIonized().booleanValue() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue() == true);
					}
					//ing2 - qty: 34.782608 - geo origins: geoOrigin1, geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: false
					if(ingListDataItem.getIng().equals(ing2)){
						assertEquals("ing2.getQtyPerc() == 34.782608, actual values: " + trace, df.format(34.782608), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
						assertEquals("ing2.isIonized().booleanValue() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
					}
					//ing3 - qty: 52.173912 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
					if(ingListDataItem.getIng().equals(ing3)){
						assertEquals("ing3.getQtyPerc() == 52.173912, actual values: " + trace, df.format(52.173912), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
						assertEquals("ing3.isIonized().booleanValue() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
					}
				}
				
				//verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct.getIngLabelingList());
				for(IngLabelingListDataItem illDataItem : formulatedProduct.getIngLabelingList()){				
					
					String trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
					logger.debug(trace);	
					
					//Garniture 52,17 % (ing3 100,00 %), Pâte 47,83 % (Legal Raw material 2 72,73 % (ing2 75,00 %, ing1 25,00 %), ing2 18,18 %, ing1 9,09 %)
					if(illDataItem.getGrp().equals("-")){		
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
						assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture 52,17 % (ing3 french 100,00 %), Pâte 47,83 % (Legal Raw material 2 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
						assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture 52.17 % (ing3 english 100.00 %), Pâte 47.83 % (Legal Raw material 2 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
					}
				}
				
				return null;

			}},false,true);
		   
	   }

	
	/**
	 * Test ingredients calculating.
	 *
	 * @throws Exception the exception
	 */
	public void testIngredientsCalculating() throws Exception{
		   		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   
			
			
			/**
			 *  		Finished product 1
			 */
			logger.debug("/*************************************/");
			logger.debug("/*-- Create Finished product 1--*/");
			logger.debug("/*************************************/");
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product 1");
			finishedProduct1.setLegalName("Legal Finished product 1");
			finishedProduct1.setQty(2f);
			finishedProduct1.setUnit(ProductUnit.kg);				
			List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
			compoList1.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF11NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial11NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DETAIL_FR, rawMaterial12NodeRef));
			compoList1.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f,  GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF12NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial13NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial14NodeRef));
			finishedProduct1.setCompoList(compoList1);
			 Collection<QName> dataLists = productDictionaryService.getDataLists();
			NodeRef finishedProductNodeRef1 = productDAO.create(folderNodeRef, finishedProduct1, dataLists);
			
			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);
			
			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct1 = productDAO.find(finishedProductNodeRef1, productDictionaryService.getDataLists());

			//verify IngList
			// 1 * lSF1 [Pâte, DETAIL]
			// 1 * RM1	[ , ]					, ingList : 		1 ing1 ; bio1 ; geo1		// 2 ing2 ; bio1 ; geo1|geo2	//
			// 2 * RM2	[ , DETAIL]			, ingList : 		1 ing1 ; bio1 ; geo1		// 3 ing2 ; bio2 ; geo1|geo2	//
			// 1 * lSF2 [Garniture, DETAIL]
			// 3 * RM3	[ , ]					, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2
			// 3 * RM4	[ , ]					, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2		//	2 ing4 ; bio1|bio2 ; geo2
			assertNotNull("IngList is null", formulatedProduct1.getIngList());
			for(IngListDataItem ingListDataItem : formulatedProduct1.getIngList()){
			
				String geoOriginsText = "";
				for(NodeRef geoOrigin : ingListDataItem.getGeoOrigin())
					geoOriginsText += nodeService.getProperty(geoOrigin, ContentModel.PROP_NAME) + ", ";
				
				String bioOriginsText = "";
				for(NodeRef bioOrigin : ingListDataItem.getBioOrigin())
					bioOriginsText += nodeService.getProperty(bioOrigin, ContentModel.PROP_NAME) + ", ";
				
				String trace= "ing: " + nodeService.getProperty(ingListDataItem.getIng(), ContentModel.PROP_NAME) + " - qty: " + ingListDataItem.getQtyPerc() + " - geo origins: " + geoOriginsText + " - bio origins: " + bioOriginsText + " is gmo: " + ingListDataItem.isGMO().booleanValue()  + " is ionized: " + ingListDataItem.isIonized().booleanValue();
				logger.debug(trace);
				
				DecimalFormat df = new DecimalFormat("0.000000");
				
				//ing: ing1 - qty: 7.3170733 - geo origins: geoOrigin1,  - bio origins: bioOrigin1,  is gmo: true
				if(ingListDataItem.getIng().equals(ing1)){
					assertEquals("ing1.getQtyPerc() == 13.043478, actual values: " + trace,  df.format(7.3170733), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace, false, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing2 - qty: 19.512196 - geo origins: geoOrigin1, geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: false
				if(ingListDataItem.getIng().equals(ing2)){
					assertEquals("ing2.getQtyPerc() == 34.782608, actual values: " + trace, df.format(19.512196), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing3 - qty: 58.536587 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing3)){
					assertEquals("ing3.getQtyPerc() == 52.173912, actual values: " + trace, df.format(58.536587), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing4 - qty: 14.634147 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing4)){
					assertEquals("ing3.getQtyPerc() == 52.173912, actual values: " + trace, df.format(14.634147), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
				}
			}
			
			//verify IngLabelingList
			assertNotNull("IngLabelingList is null", formulatedProduct1.getIngLabelingList());
			for(IngLabelingListDataItem illDataItem : formulatedProduct1.getIngLabelingList()){				
				
				String trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
				logger.debug(trace);			
				
				//Garniture 73,17 % (ing3 80,00 %, ing4 20,00 %), Pâte 26,83 % (Legal Raw material 12 72,73 % (ing2 75,00 %, ing1 25,00 %), ing2 18,18 %, ing1 9,09 %)
				if(illDataItem.getGrp().equals("-")){
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
					assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture 73,17 % (ing3 french 80,00 %, ing4 french 20,00 %), Pâte 26,83 % (Legal Raw material 12 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
					assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture 73.17 % (ing3 english 80.00 %, ing4 english 20.00 %), Pâte 26.83 % (Legal Raw material 12 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
				}
			}
			
			/**
			 *  		Finished product 2
			 */
			logger.debug("/**********************************/");
			logger.debug("/*-- Create Finished product 2 --*/");
			logger.debug("/**********************************/");
			FinishedProductData finishedProduct2 = new FinishedProductData();
			finishedProduct2.setName("Finished product 2");
			finishedProduct2.setLegalName("Legal Finished product 2");
			finishedProduct2.setQty(2f);
			finishedProduct2.setUnit(ProductUnit.kg);
			List<CompoListDataItem> compoList2 = new ArrayList<CompoListDataItem>();
			compoList2.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF11NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f,  "", DeclarationType.DECLARE_FR, rawMaterial11NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DETAIL_FR, rawMaterial12NodeRef));
			compoList2.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF12NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial13NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DO_NOT_DECLARE_FR, rawMaterial14NodeRef));
			finishedProduct2.setCompoList(compoList2);
			NodeRef finishedProductNodeRef2 = productDAO.create(folderNodeRef, finishedProduct2, dataLists);			
			
			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef2);
			
			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct2 = productDAO.find(finishedProductNodeRef2, productDictionaryService.getDataLists());
							
			//verify IngList
			// 1 * lSF1 [Pâte, DETAIL]
			// 1 * RM1	[ , ]							, ingList : 		1 ing1 ; bio1 ; geo1		// 2 ing2 ; bio1 ; geo1|geo2	//
			// 2 * RM2	[ , DETAIL]					, ingList : 		1 ing1 ; bio1 ; geo1		// 3 ing2 ; bio2 ; geo1|geo2	//
			// 1 * lSF2 [Garniture, DETAIL]
			// 3 * RM3	[ , ]							, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2
			// 3 * RM4	[ , DO_NOT_LABEL]	, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2		//	2 ing4 ; bio1|bio2 ; geo2
			assertNotNull("IngList is null", formulatedProduct2.getIngList());
			for(IngListDataItem ingListDataItem : formulatedProduct2.getIngList()){
			
				String geoOriginsText = "";
				for(NodeRef geoOrigin : ingListDataItem.getGeoOrigin())
					geoOriginsText += nodeService.getProperty(geoOrigin, ContentModel.PROP_NAME) + ", ";
				
				String bioOriginsText = "";
				for(NodeRef bioOrigin : ingListDataItem.getBioOrigin())
					bioOriginsText += nodeService.getProperty(bioOrigin, ContentModel.PROP_NAME) + ", ";
				
				String trace= "ing: " + nodeService.getProperty(ingListDataItem.getIng(), ContentModel.PROP_NAME) + " - qty: " + ingListDataItem.getQtyPerc() + " - geo origins: " + geoOriginsText + " - bio origins: " + bioOriginsText + " is gmo: " + ingListDataItem.isGMO().booleanValue() + " is ionized: " + ingListDataItem.isIonized().booleanValue();
				logger.debug(trace);
				
				DecimalFormat df = new DecimalFormat("0.000000");
				
				//ing: ing1 - qty: 7.3170733 - geo origins: geoOrigin1,  - bio origins: bioOrigin1,  is gmo: true
				if(ingListDataItem.getIng().equals(ing1)){
					assertEquals("ing1.getQtyPerc() == 13.043478, actual values: " + trace,  df.format(7.3170733), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace, false, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue() == true);
					assertEquals("ing1.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue() == true);
				}
				//ing: ing2 - qty: 19.512196 - geo origins: geoOrigin1, geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: false
				if(ingListDataItem.getIng().equals(ing2)){
					assertEquals("ing2.getQtyPerc() == 34.782608, actual values: " + trace, df.format(19.512196), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing3 - qty: 58.536587 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing3)){
					assertEquals("ing3.getQtyPerc() == 52.173912, actual values: " + trace, df.format(58.536587), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing4 - qty: 14.634147 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing4)){
					assertEquals("ing3.getQtyPerc() == 52.173912, actual values: " + trace, df.format(14.634147), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
				}
			}
			
			//verify IngLabelingList
			assertNotNull("IngLabelingList is null", formulatedProduct2.getIngLabelingList());
			for(IngLabelingListDataItem illDataItem : formulatedProduct2.getIngLabelingList()){				
				
				String trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
				logger.debug(trace);			
				
				//Garniture 73,17 % (ing3 40,00 %), Pâte 26,83 % (Legal Raw material 12 72,73 % (ing2 75,00 %, ing1 25,00 %), ing2 18,18 %, ing1 9,09 %)
				if(illDataItem.getGrp().equals("-")){
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
					assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture 73,17 % (ing3 french 40,00 %), Pâte 26,83 % (Legal Raw material 12 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
					assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture 73.17 % (ing3 english 40.00 %), Pâte 26.83 % (Legal Raw material 12 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
				}
			}
			
			return null;

			}},false,true);
		   
	   }
	
	/**
	 * Test the formulation of the costs and nuts in kg and g.
	 *
	 * @throws Exception the exception
	 */
	public void testFormulateCostAndNutOfProductInkgAndg() throws Exception{
		   
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   				
								
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2f);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.g, 0f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);
				
				logger.debug("unit of product to formulate: " + finishedProduct.getUnit());
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				logger.debug("unit of product formulated: " + finishedProduct.getUnit());
				
				//costs
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("cost1.getValue() == 3.001, actual values: " + trace, 3.001f, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 4.002, actual values: " + trace, 4.002f, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 2.001, actual values: " + trace, 2.001f, nutListDataItem.getValue());
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 4.002, actual values: " + trace, 4.002f, nutListDataItem.getValue());
						assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					}
				}				
				
				return null;

				}},false,true);
		   
	   }
	
	/**
	 * Test the formulation of the costs and nuts in kg, g, mL and m.
	 *
	 * @throws Exception the exception
	 */
	public void testFormulateCostAndNutOfProductInkgAndgAndmLAndm() throws Exception{
		   
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   				
								
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setQty(20f);
				finishedProduct.setUnit(ProductUnit.P);
				finishedProduct.setDensity(0.1f);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.P, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 40f, 0f, 0f, CompoListUnit.g, 0f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.mL, 0f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.P, 0f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 30f, 0f, 0f, CompoListUnit.g, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 0.05f, 0f, 0f, CompoListUnit.m, 0f, "", DeclarationType.OMIT_FR, rawMaterial5NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);
				
				logger.debug("unit of product to formulate: " + finishedProduct.getUnit());
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				logger.debug("unit of product formulated: " + finishedProduct.getUnit());
				DecimalFormat df = new DecimalFormat("0.000");
				//costs
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("cost1.getValue() == 0.402, actual values: " + trace, df.format(0.402f), df.format(costListDataItem.getValue()));
						assertEquals("cost1.getUnit() == €/P, actual values: " + trace, "€/P", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 0.444, actual values: " + trace, df.format(0.444f), df.format(costListDataItem.getValue()));
						assertEquals("cost1.getUnit() == €/P, actual values: " + trace, "€/P", costListDataItem.getUnit());
					}
				}
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 1.22, actual values: " + trace, df.format(1.22f), df.format(nutListDataItem.getValue()));
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 2.94, actual values: " + trace, df.format(2.94f), df.format(nutListDataItem.getValue()));
						assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					}
				}				
				
				return null;

				}},false,true);
		   
	   }
	
	/**
	 * Test sort nut list.
	 */
	public void testSortNutList(){
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   				
								
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "nut3");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUP1);
					NodeRef nut3 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "nut14");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUP1);
					NodeRef nut14 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "nut5");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUP1);
					NodeRef nut5 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "nut26");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUP2);
					NodeRef nut26 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "nut17");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUP2);
					NodeRef nut17 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "nut8");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUPOTHER);
					NodeRef nut8 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					properties.clear();		
					properties.put(ContentModel.PROP_NAME, "nut9");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUPOTHER);
					NodeRef nut9 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					properties.clear();
					properties.put(ContentModel.PROP_NAME, "nut10");
					properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
					properties.put(BeCPGModel.PROP_NUTGROUP, GROUPOTHER);
					NodeRef nut10 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
					
					List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
					nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Autre", nut10));
					nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut3));
					nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut5));
					nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut14));
					nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Autre", nut9));
					nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut1));
					nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f,  0f, "Groupe 2", nut26));
					nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f,  0f, "Groupe 2", nut2));
					nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f,  0f, "Groupe 2", nut17));
					nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Autre", nut8));
					
					
					List<NutListDataItemDecorator> nutListDecorated = new ArrayList<NutListDataItemDecorator>();
					for(NutListDataItem nutListDataItem : nutList){
						NutListDataItemDecorator d = new NutListDataItemDecorator();
						d.setNutListDataItem(nutListDataItem);
						d.setNutName((String)nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME));			
						nutListDecorated.add(d);
					}
			        Collections.sort(nutListDecorated, new NutListSortComparator());
			       
			        List<NutListDataItem> nutListSorted = new ArrayList<NutListDataItem>();
			        for(NutListDataItemDecorator n : nutListDecorated)
			        	nutListSorted.add(n.getNutListDataItem());
			        
			        for(NutListDataItem n : nutListSorted)
			        	logger.debug((String)nodeService.getProperty(n.getNut(), ContentModel.PROP_NAME) + "- " + n.getGroup());
			        
			        String actualNut0 = (String)nodeService.getProperty(nutListSorted.get(0).getNut(), ContentModel.PROP_NAME);
			        String actualNut1 = (String)nodeService.getProperty(nutListSorted.get(1).getNut(), ContentModel.PROP_NAME);
			        String actualNut2 = (String)nodeService.getProperty(nutListSorted.get(2).getNut(), ContentModel.PROP_NAME);
			        String actualNut3 = (String)nodeService.getProperty(nutListSorted.get(3).getNut(), ContentModel.PROP_NAME);
			        String actualNut4 = (String)nodeService.getProperty(nutListSorted.get(4).getNut(), ContentModel.PROP_NAME);
			        String actualNut5 = (String)nodeService.getProperty(nutListSorted.get(5).getNut(), ContentModel.PROP_NAME);
			        String actualNut6 = (String)nodeService.getProperty(nutListSorted.get(6).getNut(), ContentModel.PROP_NAME);
			        String actualNut7 = (String)nodeService.getProperty(nutListSorted.get(7).getNut(), ContentModel.PROP_NAME);
			        String actualNut8 = (String)nodeService.getProperty(nutListSorted.get(8).getNut(), ContentModel.PROP_NAME);
			        String actualNut9 = (String)nodeService.getProperty(nutListSorted.get(9).getNut(), ContentModel.PROP_NAME);
			        assertEquals("nut 1 " + actualNut0, nut1, nutListSorted.get(0).getNut());
			        assertEquals("nut 14 " + actualNut1, nut14, nutListSorted.get(1).getNut());
			        assertEquals("nut 3 " + actualNut2, nut3, nutListSorted.get(2).getNut());
			        assertEquals("nut 5 " + actualNut3, nut5, nutListSorted.get(3).getNut());
			        assertEquals("nut 17 " + actualNut4, nut17, nutListSorted.get(4).getNut());
			        assertEquals("nut 2 " + actualNut5, nut2, nutListSorted.get(5).getNut());
			        assertEquals("nut 26 " + actualNut6, nut26, nutListSorted.get(6).getNut());
			        assertEquals("nut 10 " + actualNut7, nut10, nutListSorted.get(7).getNut());
			        assertEquals("nut 8 " + actualNut8, nut8, nutListSorted.get(8).getNut());
			        assertEquals("nut 9 " + actualNut9, nut9, nutListSorted.get(9).getNut());
        
			        return null;
			
				}},false,true);
	}
	

	/**
	 * Test allergen list calculating.
	 *
	 * @throws Exception the exception
	 */
	public void testAllergenListCalculating() throws Exception{
		   
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   
				
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				
				/*-- Create products --*/
				logger.debug("/*-- Create products --*/");
				
				//SF1
				SemiFinishedProductData SFProduct1 = new SemiFinishedProductData();
				SFProduct1.setName("semi fini 1");
				SFProduct1.setLegalName("Legal semi fini 1");
				SFProduct1.setUnit(ProductUnit.kg);
				SFProduct1.setQty(1f);
				List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
				compoList1.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList1.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));					
				SFProduct1.setCompoList(compoList1);
				NodeRef SFProduct1NodeRef = productDAO.create(folderNodeRef, SFProduct1, dataLists);
				
				//SF2
				SemiFinishedProductData SFProduct2 = new SemiFinishedProductData();
				SFProduct2.setName("semi fini 2");
				SFProduct2.setLegalName("Legal semi fini 2");
				SFProduct2.setUnit(ProductUnit.kg);
				SFProduct2.setQty(1f);
				List<CompoListDataItem> compoList2 = new ArrayList<CompoListDataItem>();
				compoList2.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList2.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.OMIT_FR, rawMaterial4NodeRef));					
				SFProduct2.setCompoList(compoList2);
				NodeRef SFProduct2NodeRef = productDAO.create(folderNodeRef, SFProduct2, dataLists);
						
				//PF1
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2f);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE, DeclarationType.DETAIL_FR, SFProduct1NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, SFProduct2NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);				
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate products --*/");
				productService.formulate(SFProduct1NodeRef);
				productService.formulate(SFProduct2NodeRef);
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				
				//Verify SF1
				ProductData formulatedSF1 = productDAO.find(SFProduct1NodeRef, productDictionaryService.getDataLists());
				
				//allergens			
				assertNotNull("AllergenList is not null", formulatedSF1.getAllergenList());
				for(AllergenListDataItem allergenListDataItem : formulatedSF1.getAllergenList()){
					String voluntarySources = "";
					for(NodeRef part : allergenListDataItem.getVoluntarySources())
						voluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String inVoluntarySources = "";
					for(NodeRef part : allergenListDataItem.getInVoluntarySources())
						inVoluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String trace= "SF1 allergen: " + nodeService.getProperty(allergenListDataItem.getAllergen(), ContentModel.PROP_NAME) + " - voluntary: " + allergenListDataItem.getVoluntary().booleanValue() + " - involuntary: " + allergenListDataItem.getInVoluntary().booleanValue() + " - voluntary sources:" + voluntarySources + " - involuntary sources:" + inVoluntarySources;
					logger.debug(trace);
					
					//allergen1 - voluntary: true - involuntary: false - voluntary sources:Raw material 1, Raw material 2 - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen1)){
						assertEquals("SF1 allergen1.getVoluntary().booleanValue()", true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF1 allergen1.getInVoluntary().booleanValue()", false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("SF1 allergen1.getVoluntarySources()", true, allergenListDataItem.getVoluntarySources().contains(rawMaterial1NodeRef));
						assertEquals("SF1 allergen1.getVoluntarySources()", true, allergenListDataItem.getVoluntarySources().contains(rawMaterial2NodeRef));		
						assertEquals("SF1 allergen1.getInVoluntarySources()", 0, allergenListDataItem.getInVoluntarySources().size());
					}
					//allergen2 - voluntary: false - involuntary: true - voluntary sources: - involuntary sources:Raw material 2,
					if(allergenListDataItem.getAllergen().equals(allergen2)){
						assertEquals("SF1 allergen2.getVoluntary().booleanValue() ", false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF1 allergen2.getInVoluntary().booleanValue() ", true, allergenListDataItem.getInVoluntary().booleanValue());					
						assertEquals("SF1 allergen2.getInVoluntarySources()", true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial2NodeRef));				
						assertEquals("SF1 allergen2.getVoluntarySources()", 0, allergenListDataItem.getVoluntarySources().size());
					}
					//allergen: allergen3 - voluntary: true - involuntary: true - voluntary sources:Raw material 3,  - involuntary sources:Raw material 3,
					if(allergenListDataItem.getAllergen().equals(allergen3)){
						assertEquals("SF1 allergen3.getVoluntary().booleanValue() ", false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF1 allergen3.getInVoluntary().booleanValue() ", false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("SF1 allergen3.getVoluntarySources()", 0, allergenListDataItem.getVoluntarySources().size());				
						assertEquals("SF1 allergen3.getInVoluntarySources() ", 0, allergenListDataItem.getInVoluntarySources().size());
					}
					//allergen4 - voluntary: false - involuntary: false - voluntary sources: - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen4)){
						assertEquals("SF1 allergen4.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF1 allergen4.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("SF1 allergen4.getVoluntarySources()", 0, allergenListDataItem.getVoluntarySources().size());
						assertEquals("SF1 allergen4.getInVoluntarySources()", 0, allergenListDataItem.getInVoluntarySources().size());
					}				
				}	
				
				//Verify SF1
				ProductData formulatedSF2 = productDAO.find(SFProduct2NodeRef, productDictionaryService.getDataLists());
				
				//allergens			
				assertNotNull("AllergenList is not null", formulatedSF2.getAllergenList());
				for(AllergenListDataItem allergenListDataItem : formulatedSF2.getAllergenList()){
					String voluntarySources = "";
					for(NodeRef part : allergenListDataItem.getVoluntarySources())
						voluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String inVoluntarySources = "";
					for(NodeRef part : allergenListDataItem.getInVoluntarySources())
						inVoluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String trace= "SF2 allergen: " + nodeService.getProperty(allergenListDataItem.getAllergen(), ContentModel.PROP_NAME) + " - voluntary: " + allergenListDataItem.getVoluntary().booleanValue() + " - involuntary: " + allergenListDataItem.getInVoluntary().booleanValue() + " - voluntary sources:" + voluntarySources + " - involuntary sources:" + inVoluntarySources;
					logger.debug(trace);
					
					//allergen1 - voluntary: true - involuntary: false - voluntary sources:Raw material 1, Raw material 2 - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen1)){
						assertEquals("SF2 allergen1.getVoluntary().booleanValue()", false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF2 allergen1.getInVoluntary().booleanValue()", false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("SF2 allergen1.getVoluntarySources()", 0, allergenListDataItem.getVoluntarySources().size());
						assertEquals("SF2 allergen1.getVoluntarySources()", 0, allergenListDataItem.getVoluntarySources().size());		
						assertEquals("SF2 allergen1.getInVoluntarySources()", 0, allergenListDataItem.getInVoluntarySources().size());
					}
					//allergen2 - voluntary: false - involuntary: true - voluntary sources: - involuntary sources:Raw material 2,
					if(allergenListDataItem.getAllergen().equals(allergen2)){
						assertEquals("SF2 allergen2.getVoluntary().booleanValue() ", false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF2 allergen2.getInVoluntary().booleanValue() ", false, allergenListDataItem.getInVoluntary().booleanValue());					
						assertEquals("SF2 allergen2.getInVoluntarySources()", 0, allergenListDataItem.getInVoluntarySources().size());				
						assertEquals("SF2 allergen2.getVoluntarySources()", 0, allergenListDataItem.getVoluntarySources().size());
					}
					//allergen: allergen3 - voluntary: true - involuntary: true - voluntary sources:Raw material 3,  - involuntary sources:Raw material 3,
					if(allergenListDataItem.getAllergen().equals(allergen3)){
						assertEquals("SF2 allergen3.getVoluntary().booleanValue() ", true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF2 allergen3.getInVoluntary().booleanValue() ", true, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("SF2 allergen3.getVoluntarySources()", true, allergenListDataItem.getVoluntarySources().contains(rawMaterial3NodeRef));				
						assertEquals("SF2 allergen3.getInVoluntarySources() ", true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial3NodeRef));
					}
					//allergen4 - voluntary: false - involuntary: false - voluntary sources: - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen4)){
						assertEquals("SF2 allergen4.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("SF2 allergen4.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("SF2 allergen4.getVoluntarySources()", 0, allergenListDataItem.getVoluntarySources().size());
						assertEquals("SF1 allergen4.getInVoluntarySources()", 0, allergenListDataItem.getInVoluntarySources().size());
					}				
				}	
				
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				//allergens			
				assertNotNull("AllergenList is null", formulatedProduct.getAllergenList());
				for(AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()){
					String voluntarySources = "";
					for(NodeRef part : allergenListDataItem.getVoluntarySources())
						voluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String inVoluntarySources = "";
					for(NodeRef part : allergenListDataItem.getInVoluntarySources())
						inVoluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String trace= "PF allergen: " + nodeService.getProperty(allergenListDataItem.getAllergen(), ContentModel.PROP_NAME) + " - voluntary: " + allergenListDataItem.getVoluntary().booleanValue() + " - involuntary: " + allergenListDataItem.getInVoluntary().booleanValue() + " - voluntary sources:" + voluntarySources + " - involuntary sources:" + inVoluntarySources;
					logger.debug(trace);
					
					//allergen1 - voluntary: true - involuntary: false - voluntary sources:Raw material 1, Raw material 2 - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen1)){
						assertEquals("allergen1.getVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen1.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen1.getVoluntarySources() contains Raw material 1, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial1NodeRef));
						assertEquals("allergen1.getVoluntarySources() contains Raw material 2, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial2NodeRef));		
						assertEquals("allergen1.getInVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getInVoluntarySources().size());
					}
					//allergen2 - voluntary: false - involuntary: true - voluntary sources: - involuntary sources:Raw material 2,
					if(allergenListDataItem.getAllergen().equals(allergen2)){
						assertEquals("allergen2.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen2.getInVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getInVoluntary().booleanValue());					
						assertEquals("allergen2.getInVoluntarySources() contains Raw material 2, actual values: " + trace, true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial2NodeRef));				
						assertEquals("allergen2.getVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getVoluntarySources().size());
					}
					//allergen: allergen3 - voluntary: true - involuntary: true - voluntary sources:Raw material 3,  - involuntary sources:Raw material 3,
					if(allergenListDataItem.getAllergen().equals(allergen3)){
						assertEquals("allergen3.getVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen3.getInVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen3.getVoluntarySources() contains Raw material 3, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial3NodeRef));				
						assertEquals("allergen3.getInVoluntarySources() contains Raw material 3, actual values: " + trace, true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial3NodeRef));
					}
					//allergen4 - voluntary: false - involuntary: false - voluntary sources: - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen4)){
						assertEquals("allergen4.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen4.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen4.getVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getVoluntarySources().size());
						assertEquals("allergen4.getInVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getInVoluntarySources().size());
					}				
				}					
				
				return null;

			}},false,true);
		   
	}
	
	/**
	 * Test formulate raw material.
	 *
	 * @throws Exception the exception
	 */
	public void testFormulateRawMaterial() throws Exception{
		   
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   
				
					Collection<QName> dataLists = productDictionaryService.getDataLists();															
					
					// check before formulation
					RawMaterialData rmData1 = (RawMaterialData)productDAO.find(rawMaterial1NodeRef, dataLists);
					assertNotNull("check costList", rmData1.getCostList());
					assertEquals("check costList", 2, rmData1.getCostList().size());
					assertNotNull("check nutList", rmData1.getNutList());
					assertEquals("check nutList", 2, rmData1.getNutList().size());
					assertNotNull("check allergenList", rmData1.getAllergenList());
					assertEquals("check allergenList", 4, rmData1.getAllergenList().size());
					assertNotNull("check ingList", rmData1.getIngList());
					assertEquals("check ingList", 2, rmData1.getIngList().size());
					assertNull("check compo list", rmData1.getCompoList());
					
					// formulation
					productService.formulate(rawMaterial1NodeRef);
					
					// check after formulation
					rmData1 = (RawMaterialData)productDAO.find(rawMaterial1NodeRef, dataLists);
					assertNotNull("check costList", rmData1.getCostList());
					assertEquals("check costList", 2, rmData1.getCostList().size());
					assertNotNull("check nutList", rmData1.getNutList());
					assertEquals("check nutList", 2, rmData1.getNutList().size());
					assertNotNull("check allergenList", rmData1.getAllergenList());
					assertEquals("check allergenList", 4, rmData1.getAllergenList().size());
					assertNotNull("check ingList", rmData1.getIngList());
					assertEquals("check ingList", 2, rmData1.getIngList().size());
					assertNull("check compo list", rmData1.getCompoList());
					
					return null;

				}},false,true);
		   
	   }
	
	/**
	 * Test formulate product, that has loss perc defined
	 *
	 * @throws Exception the exception
	 */
	public void testCalculateWithLoss() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2f);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 10f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 5f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 10f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 20f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "", DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);				
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				logger.debug("unit of product formulated: " + finishedProduct.getUnit());
				
				//costs
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("cost1.getValue() == 4.7425003, actual values: " + trace, 4.7425003f, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 7.175, actual values: " + trace, 7.175f, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 3, actual values: " + trace, 3f, nutListDataItem.getValue());
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6f, nutListDataItem.getValue());
						assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					}
				}
				//allergens			
				assertNotNull("AllergenList is null", formulatedProduct.getAllergenList());
				for(AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()){
					String voluntarySources = "";
					for(NodeRef part : allergenListDataItem.getVoluntarySources())
						voluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String inVoluntarySources = "";
					for(NodeRef part : allergenListDataItem.getInVoluntarySources())
						inVoluntarySources += nodeService.getProperty(part, ContentModel.PROP_NAME) + ", ";
					
					String trace= "allergen: " + nodeService.getProperty(allergenListDataItem.getAllergen(), ContentModel.PROP_NAME) + " - voluntary: " + allergenListDataItem.getVoluntary().booleanValue() + " - involuntary: " + allergenListDataItem.getInVoluntary().booleanValue() + " - voluntary sources:" + voluntarySources + " - involuntary sources:" + inVoluntarySources;
					logger.debug(trace);
					
					//allergen1 - voluntary: true - involuntary: false - voluntary sources:Raw material 1, Raw material 2 - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen1)){
						assertEquals("allergen1.getVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen1.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen1.getVoluntarySources() contains Raw material 1, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial1NodeRef));
						assertEquals("allergen1.getVoluntarySources() contains Raw material 2, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial2NodeRef));		
						assertEquals("allergen1.getInVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getInVoluntarySources().size());
					}
					//allergen2 - voluntary: false - involuntary: true - voluntary sources: - involuntary sources:Raw material 2,
					if(allergenListDataItem.getAllergen().equals(allergen2)){
						assertEquals("allergen2.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen2.getInVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getInVoluntary().booleanValue());					
						assertEquals("allergen2.getInVoluntarySources() contains Raw material 2, actual values: " + trace, true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial2NodeRef));				
						assertEquals("allergen2.getVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getVoluntarySources().size());
					}
					//allergen: allergen3 - voluntary: true - involuntary: true - voluntary sources:Raw material 3,  - involuntary sources:Raw material 3,
					if(allergenListDataItem.getAllergen().equals(allergen3)){
						assertEquals("allergen3.getVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen3.getInVoluntary().booleanValue() == true, actual values: " + trace, true, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen3.getVoluntarySources() contains Raw material 3, actual values: " + trace, true, allergenListDataItem.getVoluntarySources().contains(rawMaterial3NodeRef));				
						assertEquals("allergen3.getInVoluntarySources() contains Raw material 3, actual values: " + trace, true, allergenListDataItem.getInVoluntarySources().contains(rawMaterial3NodeRef));
					}
					//allergen4 - voluntary: false - involuntary: false - voluntary sources: - involuntary sources:
					if(allergenListDataItem.getAllergen().equals(allergen4)){
						assertEquals("allergen4.getVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getVoluntary().booleanValue());
						assertEquals("allergen4.getInVoluntary().booleanValue() == false, actual values: " + trace, false, allergenListDataItem.getInVoluntary().booleanValue());
						assertEquals("allergen4.getVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getVoluntarySources().size());
						assertEquals("allergen4.getInVoluntarySources() is empty, actual values: " + trace, 0, allergenListDataItem.getInVoluntarySources().size());
					}				
				}
				//verify IngList
				// 1 * RM1				, ingList : 		1 ing1 ; bio1 ; geo1		// 2 ing2 ; bio1 ; geo1|geo2	//
				// 2 * RM2				, ingList : 		1 ing1 ; bio1 ; geo1		// 3 ing2 ; bio2 ; geo1|geo2	//
				// 3 * RM3				, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2
				// 3 * RM4 [OMIT]	, ingList : 											//											//	4 ing3 ; bio1|bio2 ; geo2
				assertNotNull("IngList is null", formulatedProduct.getIngList());
				for(IngListDataItem ingListDataItem : formulatedProduct.getIngList()){
				
					String geoOriginsText = "";
					for(NodeRef geoOrigin : ingListDataItem.getGeoOrigin())
						geoOriginsText += nodeService.getProperty(geoOrigin, ContentModel.PROP_NAME) + ", ";
					
					String bioOriginsText = "";
					for(NodeRef bioOrigin : ingListDataItem.getBioOrigin())
						bioOriginsText += nodeService.getProperty(bioOrigin, ContentModel.PROP_NAME) + ", ";
					
					String trace= "ing: " + nodeService.getProperty(ingListDataItem.getIng(), ContentModel.PROP_NAME) + " - qty: " + ingListDataItem.getQtyPerc() + " - geo origins: " + geoOriginsText + " - bio origins: " + bioOriginsText + " is gmo: " + ingListDataItem.isGMO().booleanValue() + " is ionized: " + ingListDataItem.isIonized().booleanValue();
					logger.debug(trace);
					
					DecimalFormat df = new DecimalFormat("0.000000");
					
					//ing: ing1 - qty: 13.043478 - geo origins: geoOrigin1,  - bio origins: bioOrigin1,  is gmo: true
					if(ingListDataItem.getIng().equals(ing1)){
						assertEquals("ing1.getQtyPerc() == 13.043478, actual values: " + trace,  df.format(13.043478), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace, false, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing1.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue() == true);
						assertEquals("ing1.isIonized().booleanValue() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue() == true);
					}
					//ing2 - qty: 34.782608 - geo origins: geoOrigin1, geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: false
					if(ingListDataItem.getIng().equals(ing2)){
						assertEquals("ing2.getQtyPerc() == 34.782608, actual values: " + trace, df.format(34.782608), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
						assertEquals("ing2.isIonized().booleanValue() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
					}
					//ing3 - qty: 52.173912 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
					if(ingListDataItem.getIng().equals(ing3)){
						assertEquals("ing3.getQtyPerc() == 52.173912, actual values: " + trace, df.format(52.173912), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
						assertEquals("ing3.isIonized().booleanValue() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
					}
				}
				
				//verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct.getIngLabelingList());
				for(IngLabelingListDataItem illDataItem : formulatedProduct.getIngLabelingList()){				
					
					String trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
					logger.debug(trace);	
					
					//Garniture 52,17 % (ing3 100,00 %), Pâte 47,83 % (Legal Raw material 2 72,73 % (ing2 75,00 %, ing1 25,00 %), ing2 18,18 %, ing1 9,09 %)
					if(illDataItem.getGrp().equals("-")){		
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
						assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture 52,17 % (ing3 french 100,00 %), Pâte 47,83 % (Legal Raw material 2 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
						assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture 52.17 % (ing3 english 100.00 %), Pâte 47.83 % (Legal Raw material 2 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
					}
				}
								
				return null;

			}},false,true);
		   
	   }
	
	/**
	 * Test formulate product, that has loss perc defined
	 *
	 * @throws Exception the exception
	 */
	public void testCalculateSubFormula() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2f);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, null, 3f, 2f, CompoListUnit.kg, 10f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, null, 1f, 100f, CompoListUnit.kg, 10f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 80f, null, CompoListUnit.kg, 5f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 30f, null, CompoListUnit.kg, 10f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1f, 1f, 200f, CompoListUnit.kg, 20f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 170f, null, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 40f, null, CompoListUnit.kg, 0f, "", DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 1f, null, CompoListUnit.P, 0f, "", DeclarationType.DECLARE_FR, rawMaterial5NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);				
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				for(CompoListDataItem compoListDataItem : formulatedProduct.getCompoList()){
					
					if(compoListDataItem.getProduct().equals(localSF1NodeRef)){
						assertEquals("check SF1 qty", 3f, compoListDataItem.getQty());
						assertEquals("check SF1 qty sub formula", 3f, compoListDataItem.getQtySubFormula());
						assertEquals("check SF1 after process", 2f, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(localSF2NodeRef)){
						assertEquals("check SF2 qty", 1.5f, compoListDataItem.getQty());
						assertEquals("check SF2 qty sub formula", 1f, compoListDataItem.getQtySubFormula());
						assertEquals("check SF2 after process", 100f, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial1NodeRef)){
						assertEquals("check MP1 qty", 1.2f, compoListDataItem.getQty());
						assertEquals("check MP1 qty sub formula", 80f, compoListDataItem.getQtySubFormula());
						assertEquals("check MP1 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial2NodeRef)){
						assertEquals("check MP2 qty", 0.45f, compoListDataItem.getQty());
						assertEquals("check MP2 qty sub formula", 30f, compoListDataItem.getQtySubFormula());
						assertEquals("check MP2 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(localSF3NodeRef)){
						assertEquals("check SF3 qty", 1.5f, compoListDataItem.getQty());
						assertEquals("check SF3 qty sub formula", 1f, compoListDataItem.getQtySubFormula());
						assertEquals("check SF3 after process", 200f, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial3NodeRef)){
						assertEquals("check MP3 qty", 1.275f, compoListDataItem.getQty());
						assertEquals("check MP3 qty sub formula", 170f, compoListDataItem.getQtySubFormula());
						assertEquals("check MP3 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial4NodeRef)){
						assertEquals("check MP4 qty", 0.3f, compoListDataItem.getQty());
						assertEquals("check MP4 qty sub formula", 40f, compoListDataItem.getQtySubFormula());
						assertEquals("check MP4 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial5NodeRef)){
						assertEquals("check MP5 qty", 0.0075f, compoListDataItem.getQty());
						assertEquals("check MP5 qty sub formula", 1f, compoListDataItem.getQtySubFormula());
						assertEquals("check MP5 after process", null, compoListDataItem.getQtyAfterProcess());
					}
				}
				
				logger.debug("unit of product formulated: " + finishedProduct.getUnit());
							
				return null;

			}},false,true);
		   
	   }
	
	public void testPackagingCosts() throws Exception{
		   
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   							
						
					Collection<QName> dataLists = productDictionaryService.getDataLists();
					
					/*-- Packaging material 1 --*/					
					PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
					packagingMaterial1.setName("Packaging material 1");
					packagingMaterial1.setLegalName("Legal Packaging material 1");
					//costList
					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
					costList.add(new CostListDataItem(null, 3f, "€/P", pkgCost1));
					costList.add(new CostListDataItem(null, 2f, "€/P", pkgCost2));
					packagingMaterial1.setCostList(costList);					
					packagingMaterial1NodeRef = productDAO.create(folderNodeRef, packagingMaterial1, dataLists);
					
					/*-- Packaging material 2 --*/					
					PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
					packagingMaterial2.setName("Packaging material 2");
					packagingMaterial2.setLegalName("Legal Packaging material 2");
					//costList
					costList.clear();
					costList.add(new CostListDataItem(null, 1f, "€/m", pkgCost1));
					costList.add(new CostListDataItem(null, 2f, "€/m", pkgCost2));
					packagingMaterial2.setCostList(costList);					
					packagingMaterial2NodeRef = productDAO.create(folderNodeRef, packagingMaterial2, dataLists);
					
					/*-- Packaging material 1 --*/					
					PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
					packagingMaterial3.setName("Packaging material 3");
					packagingMaterial3.setLegalName("Legal Packaging material 3");
					//costList
					costList.clear();
					costList.add(new CostListDataItem(null, 1f, "€/P", pkgCost1));
					costList.add(new CostListDataItem(null, 2f, "€/P", pkgCost2));
					packagingMaterial3.setCostList(costList);					
					packagingMaterial3NodeRef = productDAO.create(folderNodeRef, packagingMaterial3, dataLists);
					
					/*-- Create finished product --*/
					logger.debug("/*-- Create finished product --*/");
					FinishedProductData finishedProduct = new FinishedProductData();
					finishedProduct.setName("Produit fini 1");
					finishedProduct.setLegalName("Legal Produit fini 1");
					finishedProduct.setUnit(ProductUnit.kg);
					finishedProduct.setQty(2f);
					List<PackagingListDataItem> packagingList = new ArrayList<PackagingListDataItem>();
					packagingList.add(new PackagingListDataItem(null, 1f, PackagingListUnit.P, PACKAGING_PRIMAIRE, packagingMaterial1NodeRef));
					packagingList.add(new PackagingListDataItem(null, 3f, PackagingListUnit.m, PACKAGING_PRIMAIRE, packagingMaterial2NodeRef));
					packagingList.add(new PackagingListDataItem(null, 8f, PackagingListUnit.PP, PACKAGING_TERTIAIRE, packagingMaterial3NodeRef));
					finishedProduct.setPackagingList(packagingList);
					NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);				
					
					/*-- Formulate product --*/
					logger.debug("/*-- Formulate product --*/");
					productService.formulate(finishedProductNodeRef);
					
					/*-- Verify formulation --*/
					logger.debug("/*-- Verify formulation --*/");
					ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
					
					logger.debug("unit of product formulated: " + finishedProduct.getUnit());
					
					//costs
					assertNotNull("CostList is null", formulatedProduct.getCostList());
					for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
						String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
						logger.debug(trace);
						if(costListDataItem.getCost().equals(pkgCost1)){
							assertEquals("cost1.getValue() == 3.0625, actual values: " + trace, 3.0625f, costListDataItem.getValue());
							assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						}
						if(costListDataItem.getCost().equals(pkgCost2)){
							assertEquals("cost1.getValue() == 4.125, actual values: " + trace, 4.125f, costListDataItem.getValue());
							assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						}
					}
					
									
					return null;

				}},false,true);
			   
		   }
	
	/**
	 * Test formulate product, that has a specification defined
	 *
	 * @throws Exception the exception
	 */
	public void testFormulationWithSpecification() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				
				// specification
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
				properties.put(ContentModel.PROP_NAME, "Spec");
				NodeRef productSpecificationNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, 
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
								(String)properties.get(ContentModel.PROP_NAME)), 
								BeCPGModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();
				
				ProductData productSpecification = productDAO.find(productSpecificationNodeRef, dataLists);
				
				List<NodeRef> ings = new ArrayList<NodeRef>();
				List<NodeRef> geoOrigins = new ArrayList<NodeRef>();
				List<NodeRef> bioOrigins = new ArrayList<NodeRef>();
				
				List<ForbiddenIngListDataItem> forbiddenIngList = new ArrayList<ForbiddenIngListDataItem>();
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, "Interdit", "OGM interdit", null, NullableBoolean.True, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, "Interdit", "Ionisation interdite", null, NullableBoolean.Null, NullableBoolean.True, ings, geoOrigins, bioOrigins));
				
				ings = new ArrayList<NodeRef>();
				geoOrigins = new ArrayList<NodeRef>();
				ings.add(ing3);				
				geoOrigins.add(geoOrigin1);
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, "Toléré", "Ing3 geoOrigin1 toléré", null, NullableBoolean.Null, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				
				ings = new ArrayList<NodeRef>();
				geoOrigins = new ArrayList<NodeRef>();
				ings.add(ing1);
				ings.add(ing4);
				geoOrigins.clear();
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, "Interdit", "Ing1 et ing4 interdits", null, NullableBoolean.Null, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				
				ings = new ArrayList<NodeRef>();
				geoOrigins = new ArrayList<NodeRef>();
				ings.add(ing2);				
				geoOrigins.add(geoOrigin2);
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, "Info", "Ing2 geoOrigin2 interdit sur charcuterie", null, NullableBoolean.Null, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				
				productSpecification.setForbiddenIngList(forbiddenIngList);
				productDAO.update(productSpecificationNodeRef, productSpecification, dataLists);
				
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");				 
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2f);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, null, 3f, 2f, CompoListUnit.kg, 10f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, null, 1f, 100f, CompoListUnit.kg, 10f, GROUP_PATE, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 80f, null, CompoListUnit.kg, 5f, "", DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 30f, null, CompoListUnit.kg, 10f, "", DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1f, 1f, 200f, CompoListUnit.kg, 20f, GROUP_GARNITURE, DeclarationType.DETAIL_FR, localSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 170f, null, CompoListUnit.kg, 0f, "", DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 40f, null, CompoListUnit.kg, 0f, "", DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 1f, null, CompoListUnit.P, 0f, "", DeclarationType.DECLARE_FR, rawMaterial5NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);	
				
				// create association
				nodeService.createAssociation(finishedProductNodeRef, productSpecificationNodeRef, BeCPGModel.ASSOC_PRODUCT_SPECIFICATION);
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				int checks = 0;
				for(ReqCtrlListDataItem reqCtrlList : formulatedProduct.getReqCtrlList()){
					
					if(reqCtrlList.getReqMessage().equals("OGM interdit")){
						
						assertEquals("Interdit", reqCtrlList.getReqType());
						assertEquals(4, reqCtrlList.getSources().size());
						assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial3NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial4NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial5NodeRef));
						checks++;
					}
					else if(reqCtrlList.getReqMessage().equals("Ionisation interdite")){
						
						assertEquals("Interdit", reqCtrlList.getReqType());
						assertEquals(4, reqCtrlList.getSources().size());
						assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial3NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial4NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial5NodeRef));
						checks++;
					}
					else if(reqCtrlList.getReqMessage().equals("Ing3 geoOrigin1 toléré")){
						
						// should not occured
						assertTrue(false);
						assertEquals("Toléré", reqCtrlList.getReqType());
					}
					else if(reqCtrlList.getReqMessage().equals("Ing1 et ing4 interdits")){
						
						assertEquals("Interdit", reqCtrlList.getReqType());
						assertEquals(2, reqCtrlList.getSources().size());
						assertTrue(reqCtrlList.getSources().contains(rawMaterial1NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
						checks++;
					}
					else if(reqCtrlList.getReqMessage().equals("Ing2 geoOrigin2 interdit sur charcuterie")){
						
						assertEquals("Info", reqCtrlList.getReqType());
						assertEquals(2, reqCtrlList.getSources().size());
						assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
						checks++;
					}										
				}				
					
				assertEquals(4, checks);
				
				return null;

			}},false,true);
		   
	   }
}
