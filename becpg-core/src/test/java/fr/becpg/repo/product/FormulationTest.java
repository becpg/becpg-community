/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.text.DecimalFormat;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostDetailsListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.DynamicCharachListItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem.NullableBoolean;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
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
	
	/** The product service. */
	private ProductService productService;    
	
	/** The product dao. */
	private ProductDAO productDAO;
    
    /** The PAT h_ productfolder. */
    private static String PATH_PRODUCTFOLDER = "TestProductFolder";
    
    /** The GROU p1. */
    private static String GROUP1 = "Groupe 1";      
    
    /** The GROU p2. */
    private static String GROUP2 = "Groupe 2";
    
    /** The GROUPOTHER. */
    private static String GROUPOTHER = "Autre";
    
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
    
    private NodeRef rawMaterial6NodeRef;
    
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
    
    private NodeRef grpGarniture;
    
    private NodeRef grpPate;
    
    /* (non-Javadoc)
     * @see fr.becpg.test.RepoBaseTestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
    	super.setUp();		
   
    	logger.debug("ProductMgrTest:setUp");
    	    	
    
    	productService = (ProductService)ctx.getBean("productService");       
        productDAO = (ProductDAO)ctx.getBean("productDAO");

 		//create RM and lSF
 		initParts();
 		       
        
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
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing2");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing2 default");
			mlName.addValue(Locale.ENGLISH, "ing2 english");
			mlName.addValue(Locale.FRENCH, "ing2 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing3");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing3 default");
			mlName.addValue(Locale.ENGLISH, "ing3 english");
			mlName.addValue(Locale.FRENCH, "ing3 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing3 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.put(ContentModel.PROP_NAME, "ing4");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing4 default");
			mlName.addValue(Locale.ENGLISH, "ing4 english");
			mlName.addValue(Locale.FRENCH, "ing4 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
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
			
			//Groupes
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "grpGarniture");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			grpGarniture = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_DECL_GROUP, properties).getChildRef();
			
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "grpPâte");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			grpPate = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_DECL_GROUP, properties).getChildRef();
			
			/*-- Create raw materials --*/
			logger.debug("/*-- Create raw materials --*/");
			 Collection<QName> dataLists = productDictionaryService.getDataLists();
			/*-- Raw material 1 --*/
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			MLText legalName = new MLText("Legal Raw material 1");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 1");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 1");
			rawMaterial1.setLegalName(legalName);
			//costList
			List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 3d, "€/kg", 3.1d, cost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/kg", 2.1d, cost2, false));
			rawMaterial1.setCostList(costList);
			//nutList
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1d, "g/100g", 0.8d, 2.1d, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 2d, "g/100g", 1.5d, 2.2d, "Groupe 1", nut2, false));
			rawMaterial1.setNutList(nutList);
			//allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
			allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen4, false));
			rawMaterial1.setAllergenList(allergenList);
			//ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ; geo1|geo2 
			List<IngListDataItem> ingList = new ArrayList<IngListDataItem>();
			List<NodeRef> bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			List<NodeRef> geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1d, geoOrigins, bioOrigins, false, false, ing1, false));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 2d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial1.setIngList(ingList);
			rawMaterial1NodeRef = productDAO.create(folderNodeRef, rawMaterial1, dataLists);
			
			/*-- Raw material 2 --*/
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			legalName = new MLText("Legal Raw material 2");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 2");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 2");
			rawMaterial2.setLegalName(legalName);
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 1d, "€/kg", 2.1d, cost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/kg", 2.2d, cost2, false));
			rawMaterial2.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1d, "g/100g", 0.8d, 1.1d, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 2d, "g/100g", 0.8d, 2.1d, "Groupe 1", nut2, false));
			rawMaterial2.setNutList(nutList);
			//allergenList
			allergenList = new ArrayList<AllergenListDataItem>();
			allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen4, false));
			rawMaterial2.setAllergenList(allergenList);
			//ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1d, geoOrigins, bioOrigins, true, true, ing1, false));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 3d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial2.setIngList(ingList);			
			rawMaterial2NodeRef = productDAO.create(folderNodeRef, rawMaterial2, dataLists);
			
			/*-- Raw material 3 --*/
			RawMaterialData rawMaterial3 = new RawMaterialData();
			rawMaterial3.setName("Raw material 3");
			legalName = new MLText("Legal Raw material 3");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 3");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 3");
			rawMaterial3.setLegalName(legalName);
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 1d, "€/kg", null, cost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false));
			rawMaterial3.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1d, "g/100g", null, null, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 2d, "g/100g", null, null, "Groupe 1", nut2, false));
			rawMaterial3.setNutList(nutList);
			//allergenList
			allergenList = new ArrayList<AllergenListDataItem>();
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen4, false));
			rawMaterial3.setAllergenList(allergenList);
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4d, geoOrigins, bioOrigins, true, true, ing3, false));			
			rawMaterial3.setIngList(ingList);		
			rawMaterial3NodeRef = productDAO.create(folderNodeRef, rawMaterial3, dataLists);
			
			/*-- Raw material 4 --*/
			RawMaterialData rawMaterial4 = new RawMaterialData();
			rawMaterial4.setName("Raw material 4");
			legalName = new MLText("Legal Raw material 4");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 4");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 4");
			rawMaterial4.setLegalName(legalName);
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4d, geoOrigins, bioOrigins, true, true, ing3, false));			
			rawMaterial4.setIngList(ingList);		
			rawMaterial4NodeRef = productDAO.create(folderNodeRef, rawMaterial4, dataLists);
			
			/*-- Raw material 5 --*/
			RawMaterialData rawMaterial5 = new RawMaterialData();
			rawMaterial5.setName("Raw material 5");
			legalName = new MLText("Legal Raw material 5");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 5");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 5");
			rawMaterial5.setLegalName(legalName);
			rawMaterial5.setDensity(0.1d);
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 5d, "€/m", null, cost1, false));
			costList.add(new CostListDataItem(null, 6d, "€/m", null, cost2, false));
			rawMaterial5.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 3d, "g/100g", 0d,  0d, "Groupe 1", nut2, false));
			rawMaterial5.setNutList(nutList);					
			rawMaterial5.setIngList(ingList);		
			rawMaterial5NodeRef = productDAO.create(folderNodeRef, rawMaterial5, dataLists);
			
			/*-- Raw material 6 --*/
			RawMaterialData rawMaterial6 = new RawMaterialData();
			rawMaterial6.setName("Raw material 6");
			legalName = new MLText("Legal Raw material 6");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 6");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 6");
			rawMaterial6.setLegalName(legalName);
			rawMaterial6.setUnit(ProductUnit.L);
			rawMaterial6.setDensity(0.7d);
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 1d, "€/L", 2.1d, cost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/L", 2.2d, cost2, false));
			rawMaterial6.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1d, "g/100mL", 0.8d, 1.1d, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 2d, "g/100mL", 0.8d, 2.1d, "Groupe 1", nut2, false));
			rawMaterial6.setNutList(nutList);
			//allergenList
			allergenList = new ArrayList<AllergenListDataItem>();
			allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergen4, false));
			rawMaterial6.setAllergenList(allergenList);
			//ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 80d, geoOrigins, bioOrigins, true, true, ing1, false));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 20d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial6.setIngList(ingList);			
			rawMaterial6NodeRef = productDAO.create(folderNodeRef, rawMaterial6, dataLists);
			
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
			legalName = new MLText("Legal Raw material 11");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 11");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 11");
			rawMaterial11.setLegalName(legalName);
			//ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ; geo1|geo2 
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1d, geoOrigins, bioOrigins, false, false, ing1, false));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 2d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial11.setIngList(ingList);
			rawMaterial11NodeRef = productDAO.create(folderNodeRef, rawMaterial11, dataLists);
			
			/*-- Raw material 12 --*/
			RawMaterialData rawMaterial12 = new RawMaterialData();
			rawMaterial12.setName("Raw material 12");
			legalName = new MLText("Legal Raw material 12");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 12");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 12");
			rawMaterial12.setLegalName(legalName);
			//ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);			
			ingList.add(new IngListDataItem(null, 1d, geoOrigins, bioOrigins, true, true, ing1, false));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 3d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial12.setIngList(ingList);			
			rawMaterial12NodeRef = productDAO.create(folderNodeRef, rawMaterial12, dataLists);
			
			/*-- Raw material 13 --*/
			RawMaterialData rawMaterial13 = new RawMaterialData();
			rawMaterial13.setName("Raw material 13");
			legalName = new MLText("Legal Raw material 13");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 13");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 13");
			rawMaterial13.setLegalName(legalName);
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4d, geoOrigins, bioOrigins, true, true, ing3, false));			
			rawMaterial13.setIngList(ingList);		
			rawMaterial13NodeRef = productDAO.create(folderNodeRef, rawMaterial13, dataLists);
			
			/*-- Raw material 14 --*/
			RawMaterialData rawMaterial14 = new RawMaterialData();
			rawMaterial14.setName("Raw material 14");
			legalName = new MLText("Legal Raw material 14");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 14");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 14");
			rawMaterial14.setLegalName(legalName);
			//ingList : 4 ing3 ; bio1|bio2 ; geo2
			ingList = new ArrayList<IngListDataItem>();
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);			
			ingList.add(new IngListDataItem(null, 4d, geoOrigins, bioOrigins, true, true, ing3, false));
			ingList.add(new IngListDataItem(null, 2d, geoOrigins, bioOrigins, true, true, ing4, false));
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
				
				//fixed cost
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();				
				properties.put(ContentModel.PROP_NAME, "fixedCost");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				properties.put(BeCPGModel.PROP_COSTFIXED, true);
				NodeRef fixedCost = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
				
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setUnitPrice(12.4d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 4000d, "€", null, fixedCost, true));
				finishedProduct.setCostList(costList);
				
				List<DynamicCharachListItem> dynamicCharachListItems = new ArrayList<DynamicCharachListItem>();
				//Literal formula
				dynamicCharachListItems.add(new DynamicCharachListItem("Literal 1","'Hello World'" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Literal 2","6.0221415E+23" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Literal 3","1+1+10-(4/100)" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Literal 4","0x7dFFFFFF" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Literal 5","true" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Literal 6","null" ));
				//Properties formulae
				dynamicCharachListItems.add(new DynamicCharachListItem("Property  1","costList[0].value" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Property  1Bis","costList[1].value" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Property  2","costList[0].unit" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Property  3","costList[0].value / costList[1].value" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Property  4","profitability" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Collection Selection  1","costList.?[value == 4.0][0].unit" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Collection Selection  2","costList.?[value < 5.0][0].value" ));
				dynamicCharachListItems.add(new DynamicCharachListItem("Collection Projection  1","costList.![value]" ));
				//Template need Template Context
				//dynamicCharachListItems.add(new DynamicCharachListItem("Template  1","Cost1/Cost2 : #{costList[1].value / costList[2].value}% Profitability : #{profitability}" ));
				//Elvis 
				dynamicCharachListItems.add(new DynamicCharachListItem("Elvis  1","null?:'Unknown'" ));
				//Boolean
				dynamicCharachListItems.add(new DynamicCharachListItem("Boolean  1","costList[1].value > 1" ));
				//Assignment
				dynamicCharachListItems.add(new DynamicCharachListItem("Assignement  1","nutList.?[nut.toString() == '"+nut1+"' ][0].value = 4d" ));
				
				
				finishedProduct.setDynamicCharachList(dynamicCharachListItems);
				
				
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
						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				
				// profitability
				DecimalFormat df = new DecimalFormat("0.00");
				assertEquals("check unitPrice", 12.4d, formulatedProduct.getUnitPrice());
				assertEquals("check unitTotalCost", 10d, formulatedProduct.getUnitTotalCost());
				assertEquals("check profitability", df.format(19.35d), df.format(formulatedProduct.getProfitability()));
				assertEquals("check breakEven", (Long)1667L, formulatedProduct.getBreakEven());				
				
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertNotSame("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
						assertEquals("nut1.getValue() == 4 (Formula), actual values: " + trace, 4d, nutListDataItem.getValue());
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6d, nutListDataItem.getValue());
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
					
					df = new DecimalFormat("0.000000");
					
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
						assertEquals("ing2.getQtyPerc() == 34.782608, actual values: " + trace, df.format(34.78260869565217), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
						assertEquals("ing2.isIonized().booleanValue() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
					}
					//ing3 - qty: 52.173912 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
					if(ingListDataItem.getIng().equals(ing3)){
						assertEquals("ing3.getQtyPerc() == 52.173913, actual values: " + trace, df.format(52.17391304347826), df.format(ingListDataItem.getQtyPerc()));
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
					if(illDataItem.getGrp() == null){		
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
						assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture french 52,17 % (ing3 french 100,00 %), Pâte french 47,83 % (Legal Raw material 2 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
						assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture english 52.17 % (ing3 english 100.00 %), Pâte english 47.83 % (Legal Raw material 2 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
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
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);				
			List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
			compoList1.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, localSF11NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial11NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR, rawMaterial12NodeRef));
			compoList1.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d,  grpGarniture, DeclarationType.DETAIL_FR, localSF12NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial13NodeRef));
			compoList1.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial14NodeRef));
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
					assertEquals("ing2.getQtyPerc() == 34.782608, actual values: " + trace, df.format(19.512195), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing3 - qty: 58.536587 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing3)){
					assertEquals("ing3.getQtyPerc() == 58.536585, actual values: " + trace, df.format(58.53658536585366), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing4 - qty: 14.634146 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing4)){
					assertEquals("ing3.getQtyPerc() == 14.634146, actual values: " + trace, df.format(14.634146), df.format(ingListDataItem.getQtyPerc()));
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
				if(illDataItem.getGrp() == null){
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
					assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture french 73,17 % (ing3 french 80,00 %, ing4 french 20,00 %), Pâte french 26,83 % (Legal Raw material 12 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
					assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture english 73.17 % (ing3 english 80.00 %, ing4 english 20.00 %), Pâte english 26.83 % (Legal Raw material 12 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
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
			finishedProduct2.setQty(2d);
			finishedProduct2.setUnit(ProductUnit.kg);
			List<CompoListDataItem> compoList2 = new ArrayList<CompoListDataItem>();
			compoList2.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, localSF11NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d,  null, DeclarationType.DECLARE_FR, rawMaterial11NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR, rawMaterial12NodeRef));
			compoList2.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpGarniture, DeclarationType.DETAIL_FR, localSF12NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial13NodeRef));
			compoList2.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DO_NOT_DECLARE_FR, rawMaterial14NodeRef));
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
				
				DecimalFormat df = new DecimalFormat("0.000000");
				String trace= "ing: " + nodeService.getProperty(ingListDataItem.getIng(), ContentModel.PROP_NAME) + " - qty: " + df.format(ingListDataItem.getQtyPerc()) + " - geo origins: " + geoOriginsText + " - bio origins: " + bioOriginsText + " is gmo: " + ingListDataItem.isGMO().booleanValue() + " is ionized: " + ingListDataItem.isIonized().booleanValue();
				logger.debug(trace);
				
				
				
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
				//ing: ing2 - qty: 19.512195 - geo origins: geoOrigin1, geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: false
				if(ingListDataItem.getIng().equals(ing2)){
					assertEquals("ing2.getQtyPerc() == 19.512195, actual values: " + trace, df.format(19.512195), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing3 - qty: 58.536585 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing3)){
					assertEquals("ing3.getQtyPerc() == 58.536585, actual values: " + trace, df.format(58.536585), df.format(ingListDataItem.getQtyPerc()));
					assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
					assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
					assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
					assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isGMO().booleanValue());
					assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.isIonized().booleanValue());
				}
				//ing: ing4 - qty: 14.634147 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
				if(ingListDataItem.getIng().equals(ing4)){
					assertEquals("ing3.getQtyPerc() == 14.634147, actual values: " + trace, df.format(14.63414634), df.format(ingListDataItem.getQtyPerc()));
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
				if(illDataItem.getGrp() == null){
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
					assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture french 73,17 % (ing3 french 40,00 %), Pâte french 26,83 % (Legal Raw material 12 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
					trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
					assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture english 73.17 % (ing3 english 40.00 %), Pâte english 26.83 % (Legal Raw material 12 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
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
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.g, 0d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
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
						assertEquals("cost1.getValue() == 3.001, actual values: " + trace, 3.001d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 4.002, actual values: " + trace, 4.002d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 2.001, actual values: " + trace, 2.001d, nutListDataItem.getValue());
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 4.002, actual values: " + trace, 4.002d, nutListDataItem.getValue());
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
				finishedProduct.setQty(20d);
				finishedProduct.setUnit(ProductUnit.P);
				finishedProduct.setDensity(0.1d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.P, 0d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 40d, 0d, 0d, CompoListUnit.g, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.mL, 0d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.P, 0d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 30d, 0d, 0d, CompoListUnit.g, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 0.05d, 0d, 0d, CompoListUnit.P, 0d, null, DeclarationType.OMIT_FR, rawMaterial5NodeRef));
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
						assertEquals("cost1.getValue() == 0.402, actual values: " + trace, df.format(0.402d), df.format(costListDataItem.getValue()));
						assertEquals("cost1.getUnit() == €/P, actual values: " + trace, "€/P", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 0.444, actual values: " + trace, df.format(0.444d), df.format(costListDataItem.getValue()));
						assertEquals("cost1.getUnit() == €/P, actual values: " + trace, "€/P", costListDataItem.getUnit());
					}
				}
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 0.77, actual values: " + trace, df.format(0.77d), df.format(nutListDataItem.getValue()));
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 1.59, actual values: " + trace, df.format(1.59d), df.format(nutListDataItem.getValue()));
						assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					}
				}				
				
				return null;

				}},false,true);
		   
	   }
	
	/**
	 * Test the formulation with density (kg and L)
	 *
	 * @throws Exception the exception
	 */
	public void testFormulateWithDensity() throws Exception{
		   
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				public NodeRef execute() throws Throwable {					   				
								
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setQty(2.5d);
				finishedProduct.setUnit(ProductUnit.kg);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();				
				compoList.add(new CompoListDataItem(null, 1, 0d, 1d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));				
				compoList.add(new CompoListDataItem(null, 1, 0d, 2d, 0d, CompoListUnit.L, 0d, null, DeclarationType.DECLARE_FR, rawMaterial6NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
			
				DecimalFormat df = new DecimalFormat("0.000");
				int checks = 0;
				assertNotNull("IngList is null", formulatedProduct.getIngList());
				for(IngListDataItem ingListDataItem : formulatedProduct.getIngList()){									
					
					String trace= "ing: " + nodeService.getProperty(ingListDataItem.getIng(), ContentModel.PROP_NAME) + " - qty: " + df.format(ingListDataItem.getQtyPerc());
					logger.debug(trace);
										
					if(ingListDataItem.getIng().equals(ing1)){
						assertEquals("ing1.getQtyPerc() == 79.02098, actual values: " + trace,  df.format(79.02098), df.format(ingListDataItem.getQtyPerc()));
						checks++;
					}
					//ing: ing2 - qty: 19.512195 - geo origins: geoOrigin1, geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: false
					if(ingListDataItem.getIng().equals(ing2)){
						assertEquals("ing2.getQtyPerc() == 20.97902, actual values: " + trace, df.format(20.97902), df.format(ingListDataItem.getQtyPerc()));
						checks++;
					}					
				}
				
				assertEquals(2, checks);	
				
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
					nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Autre", nut10, false));
					nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Groupe 1", nut3, false));
					nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Groupe 1", nut5, false));
					nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Groupe 1", nut14, false));
					nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Autre", nut9, false));
					nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Groupe 1", nut1, false));
					nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d,  0d, "Groupe 2", nut26, false));
					nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d,  0d, "Groupe 2", nut2, false));
					nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d,  0d, "Groupe 2", nut17, false));
					nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d,  0d, "Autre", nut8, false));
					
					Collection<QName> dataLists = productDictionaryService.getDataLists();
					
					//SF1
					SemiFinishedProductData SFProduct1 = new SemiFinishedProductData();
					SFProduct1.setName("semi fini 1");
					SFProduct1.setLegalName("Legal semi fini 1");
					SFProduct1.setUnit(ProductUnit.kg);
					SFProduct1.setQty(1d);
					SFProduct1.setNutList(nutList);					
					NodeRef SFProduct1NodeRef = productDAO.create(folderNodeRef, SFProduct1, dataLists);
					
					productDAO.find(SFProduct1NodeRef, dataLists);					
					
					//SF2
					SemiFinishedProductData SFProduct2 = new SemiFinishedProductData();
					SFProduct2.setName("semi fini 2");
					SFProduct2.setLegalName("Legal semi fini 2");
					SFProduct2.setUnit(ProductUnit.kg);
					SFProduct2.setQty(1d);
					List<CompoListDataItem> compoList2 = new ArrayList<CompoListDataItem>();
					compoList2.add(new CompoListDataItem(null, 1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, SFProduct1NodeRef));			
					SFProduct2.setCompoList(compoList2);
					NodeRef SFProduct2NodeRef = productDAO.create(folderNodeRef, SFProduct2, dataLists);
					
					productService.formulate(SFProduct2NodeRef);
									
					ProductData formulatedSF2 = productDAO.find(SFProduct2NodeRef, dataLists);
					
			        String actualNut0 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(0).getNut(), ContentModel.PROP_NAME);
			        String actualNut1 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(1).getNut(), ContentModel.PROP_NAME);
			        String actualNut2 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(2).getNut(), ContentModel.PROP_NAME);
			        String actualNut3 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(3).getNut(), ContentModel.PROP_NAME);
			        String actualNut4 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(4).getNut(), ContentModel.PROP_NAME);
			        String actualNut5 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(5).getNut(), ContentModel.PROP_NAME);
			        String actualNut6 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(6).getNut(), ContentModel.PROP_NAME);
			        String actualNut7 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(7).getNut(), ContentModel.PROP_NAME);
			        String actualNut8 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(8).getNut(), ContentModel.PROP_NAME);
			        String actualNut9 = (String)nodeService.getProperty(formulatedSF2.getNutList().get(9).getNut(), ContentModel.PROP_NAME);
			        assertEquals("nut 1 " + actualNut0, nut1, formulatedSF2.getNutList().get(0).getNut());
			        assertEquals("nut 14 " + actualNut1, nut14, formulatedSF2.getNutList().get(1).getNut());
			        assertEquals("nut 3 " + actualNut2, nut3, formulatedSF2.getNutList().get(2).getNut());
			        assertEquals("nut 5 " + actualNut3, nut5, formulatedSF2.getNutList().get(3).getNut());
			        assertEquals("nut 17 " + actualNut4, nut17, formulatedSF2.getNutList().get(4).getNut());
			        assertEquals("nut 2 " + actualNut5, nut2, formulatedSF2.getNutList().get(5).getNut());
			        assertEquals("nut 26 " + actualNut6, nut26, formulatedSF2.getNutList().get(6).getNut());
			        assertEquals("nut 10 " + actualNut7, nut10, formulatedSF2.getNutList().get(7).getNut());
			        assertEquals("nut 8 " + actualNut8, nut8, formulatedSF2.getNutList().get(8).getNut());
			        assertEquals("nut 9 " + actualNut9, nut9, formulatedSF2.getNutList().get(9).getNut());
        
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
				SFProduct1.setQty(1d);
				List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
				compoList1.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList1.add(new CompoListDataItem(null, 1, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));					
				SFProduct1.setCompoList(compoList1);
				NodeRef SFProduct1NodeRef = productDAO.create(folderNodeRef, SFProduct1, dataLists);
				
				//SF2
				SemiFinishedProductData SFProduct2 = new SemiFinishedProductData();
				SFProduct2.setName("semi fini 2");
				SFProduct2.setLegalName("Legal semi fini 2");
				SFProduct2.setUnit(ProductUnit.kg);
				SFProduct2.setQty(1d);
				List<CompoListDataItem> compoList2 = new ArrayList<CompoListDataItem>();
				compoList2.add(new CompoListDataItem(null, 1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList2.add(new CompoListDataItem(null, 1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));					
				SFProduct2.setCompoList(compoList2);
				NodeRef SFProduct2NodeRef = productDAO.create(folderNodeRef, SFProduct2, dataLists);
						
				//PF1
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, SFProduct1NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpGarniture, DeclarationType.DETAIL_FR, SFProduct2NodeRef));
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
					assertEquals("check compo list", 0 ,rmData1.getCompoList().size());
					
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
					assertEquals("check compo list", 0, rmData1.getCompoList().size());
					
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
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 5d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 10d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 20d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
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
						assertEquals("cost1.getValue() == 4.7425003, actual values: " + trace, 4.7425d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 7.175, actual values: " + trace, 7.175d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				
				//nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6d, nutListDataItem.getValue());
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
					//ing2 - qty: 34.782609 - geo origins: geoOrigin1, geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: false
					if(ingListDataItem.getIng().equals(ing2)){
						assertEquals("ing2.getQtyPerc() == 34.782609, actual values: " + trace, df.format(34.782609), df.format(ingListDataItem.getQtyPerc()));
						assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
						assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
						assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));					
						assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
						assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.isGMO().booleanValue());
						assertEquals("ing2.isIonized().booleanValue() is false, actual values: " + trace, false, ingListDataItem.isIonized().booleanValue());
					}
					//ing3 - qty: 52.173913 - geo origins: geoOrigin2,  - bio origins: bioOrigin1, bioOrigin2,  is gmo: true
					if(ingListDataItem.getIng().equals(ing3)){
						assertEquals("ing3.getQtyPerc() == 52.173913, actual values: " + trace, df.format(52.173913), df.format(ingListDataItem.getQtyPerc()));
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
					if(illDataItem.getGrp() == null){		
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH);
						assertEquals("illDataItem.getValue().getValue(Locale.FRENCH) check: " + trace,  "Garniture french 52,17 % (ing3 french 100,00 %), Pâte french 47,83 % (Legal Raw material 2 72,73 % (ing2 french 75,00 %, ing1 french 25,00 %), ing2 french 18,18 %, ing1 french 9,09 %)", illDataItem.getValue().getValue(Locale.FRENCH));
						trace= "grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH);
						assertEquals("illDataItem.getValue().getValue(Locale.ENGLISH) check: " + trace,  "Garniture english 52.17 % (ing3 english 100.00 %), Pâte english 47.83 % (Legal Raw material 2 72.73 % (ing2 english 75.00 %, ing1 english 25.00 %), ing2 english 18.18 %, ing1 english 9.09 %)", illDataItem.getValue().getValue(Locale.ENGLISH));						
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
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, null, 3d, 2d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, null, 1d, 100d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 80d, null, CompoListUnit.kg, 5d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 30d, null, CompoListUnit.kg, 10d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 1d, 200d, CompoListUnit.kg, 20d, grpGarniture, DeclarationType.DETAIL_FR, localSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 170d, null, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 40d, null, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 1d, null, CompoListUnit.P, 0d, null, DeclarationType.DECLARE_FR, rawMaterial5NodeRef));
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
						assertEquals("check SF1 qty", 3d, compoListDataItem.getQty());
						assertEquals("check SF1 qty sub formula", 3d, compoListDataItem.getQtySubFormula());
						assertEquals("check SF1 after process", 2d, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(localSF2NodeRef)){
						assertEquals("check SF2 qty", 1.5d, compoListDataItem.getQty());
						assertEquals("check SF2 qty sub formula", 1d, compoListDataItem.getQtySubFormula());
						assertEquals("check SF2 after process", 100d, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial1NodeRef)){
						assertEquals("check MP1 qty", 1.2d, compoListDataItem.getQty());
						assertEquals("check MP1 qty sub formula", 80d, compoListDataItem.getQtySubFormula());
						assertEquals("check MP1 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial2NodeRef)){
						assertEquals("check MP2 qty", 0.45d, compoListDataItem.getQty());
						assertEquals("check MP2 qty sub formula", 30d, compoListDataItem.getQtySubFormula());
						assertEquals("check MP2 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(localSF3NodeRef)){
						assertEquals("check SF3 qty", 1.5d, compoListDataItem.getQty());
						assertEquals("check SF3 qty sub formula", 1d, compoListDataItem.getQtySubFormula());
						assertEquals("check SF3 after process", 200d, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial3NodeRef)){
						assertEquals("check MP3 qty", 1.275d, compoListDataItem.getQty());
						assertEquals("check MP3 qty sub formula", 170d, compoListDataItem.getQtySubFormula());
						assertEquals("check MP3 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial4NodeRef)){
						assertEquals("check MP4 qty", 0.3d, compoListDataItem.getQty());
						assertEquals("check MP4 qty sub formula", 40d, compoListDataItem.getQtySubFormula());
						assertEquals("check MP4 after process", null, compoListDataItem.getQtyAfterProcess());
					}
					else if(compoListDataItem.getProduct().equals(rawMaterial5NodeRef)){
						assertEquals("check MP5 qty", 0.0075d, compoListDataItem.getQty());
						assertEquals("check MP5 qty sub formula", 1d, compoListDataItem.getQtySubFormula());
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
					costList.add(new CostListDataItem(null, 3d, "€/P", null, pkgCost1, false));
					costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
					packagingMaterial1.setCostList(costList);					
					packagingMaterial1NodeRef = productDAO.create(folderNodeRef, packagingMaterial1, dataLists);
					
					/*-- Packaging material 2 --*/					
					PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
					packagingMaterial2.setName("Packaging material 2");
					packagingMaterial2.setLegalName("Legal Packaging material 2");
					//costList
					costList.clear();
					costList.add(new CostListDataItem(null, 1d, "€/m", null, pkgCost1, false));
					costList.add(new CostListDataItem(null, 2d, "€/m", null, pkgCost2, false));
					packagingMaterial2.setCostList(costList);					
					packagingMaterial2NodeRef = productDAO.create(folderNodeRef, packagingMaterial2, dataLists);
					
					/*-- Packaging material 1 --*/					
					PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
					packagingMaterial3.setName("Packaging material 3");
					packagingMaterial3.setLegalName("Legal Packaging material 3");
					//costList
					costList.clear();
					costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
					costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
					packagingMaterial3.setCostList(costList);					
					packagingMaterial3NodeRef = productDAO.create(folderNodeRef, packagingMaterial3, dataLists);
					
					/*-- Create finished product --*/
					logger.debug("/*-- Create finished product --*/");
					FinishedProductData finishedProduct = new FinishedProductData();
					finishedProduct.setName("Produit fini 1");
					finishedProduct.setLegalName("Legal Produit fini 1");
					finishedProduct.setUnit(ProductUnit.kg);
					finishedProduct.setQty(2d);
					List<PackagingListDataItem> packagingList = new ArrayList<PackagingListDataItem>();
					packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PACKAGING_PRIMAIRE, packagingMaterial1NodeRef));
					packagingList.add(new PackagingListDataItem(null, 3d, PackagingListUnit.m, PACKAGING_PRIMAIRE, packagingMaterial2NodeRef));
					packagingList.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, PACKAGING_TERTIAIRE, packagingMaterial3NodeRef));
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
							assertEquals("cost1.getValue() == 3.0625, actual values: " + trace, 3.0625d, costListDataItem.getValue());
							assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						}
						if(costListDataItem.getCost().equals(pkgCost2)){
							assertEquals("cost1.getValue() == 4.125, actual values: " + trace, 4.125d, costListDataItem.getValue());
							assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						}
					}
					
									
					return null;

				}},false,true);
			   
		   }
	
	/**
	 * Test formulate product, that has ings requirements defined
	 *
	 * @throws Exception the exception
	 */
	public void testFormulationWithIngRequirements() throws Exception{
		   
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
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "OGM interdit", null, NullableBoolean.True, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Ionisation interdite", null, NullableBoolean.Null, NullableBoolean.True, ings, geoOrigins, bioOrigins));
				
				ings = new ArrayList<NodeRef>();
				geoOrigins = new ArrayList<NodeRef>();
				ings.add(ing3);				
				geoOrigins.add(geoOrigin1);
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, RequirementType.Tolerated, "Ing3 geoOrigin1 toléré", null, NullableBoolean.Null, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				
				ings = new ArrayList<NodeRef>();
				geoOrigins = new ArrayList<NodeRef>();
				ings.add(ing3);				
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Ing3 < 40%", 0.4d, NullableBoolean.Null, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				
				ings = new ArrayList<NodeRef>();
				geoOrigins = new ArrayList<NodeRef>();
				ings.add(ing1);
				ings.add(ing4);
				geoOrigins.clear();
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Ing1 et ing4 interdits", null, NullableBoolean.Null, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				
				ings = new ArrayList<NodeRef>();
				geoOrigins = new ArrayList<NodeRef>();
				ings.add(ing2);				
				geoOrigins.add(geoOrigin2);
				forbiddenIngList.add(new ForbiddenIngListDataItem(null, RequirementType.Info, "Ing2 geoOrigin2 interdit sur charcuterie", null, NullableBoolean.Null, NullableBoolean.Null, ings, geoOrigins, bioOrigins));
				
				productSpecification.setForbiddenIngList(forbiddenIngList);
				productDAO.update(productSpecificationNodeRef, productSpecification, dataLists);
				
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");				 
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, null, 3d, 2d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, null, 1d, 100d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 80d, null, CompoListUnit.kg, 5d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 30d, null, CompoListUnit.kg, 10d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 1d, 200d, CompoListUnit.kg, 20d, grpGarniture, DeclarationType.DETAIL_FR, localSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 170d, null, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 40d, null, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 1d, null, CompoListUnit.P, 0d, null, DeclarationType.DECLARE_FR, rawMaterial5NodeRef));
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
						
						assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
						assertEquals(4, reqCtrlList.getSources().size());
						assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial3NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial4NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial5NodeRef));
						checks++;
					}
					else if(reqCtrlList.getReqMessage().equals("Ionisation interdite")){
						
						assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
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
						assertEquals(RequirementType.Tolerated, reqCtrlList.getReqType());
					}
					else if(reqCtrlList.getReqMessage().equals("Ing3 < 40%")){
						
						assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
						assertEquals(0, reqCtrlList.getSources().size());						
						checks++;
					}
					else if(reqCtrlList.getReqMessage().equals("Ing1 et ing4 interdits")){
						
						assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
						assertEquals(2, reqCtrlList.getSources().size());
						assertTrue(reqCtrlList.getSources().contains(rawMaterial1NodeRef));
						assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
						checks++;
					}
					else if(reqCtrlList.getReqMessage().equals("Ing2 geoOrigin2 interdit sur charcuterie")){
						
						assertEquals(RequirementType.Info, reqCtrlList.getReqType());
						assertEquals(2, reqCtrlList.getSources().size());
						assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
						checks++;
					}										
				}				
					
				assertEquals(5, checks);
				
				return null;

			}},false,true);
		   
	   }
	
	/**
	 * Test formulate product, that has cost and nut mini/maxi defined
	 *
	 * @throws Exception the exception
	 */
	public void testFormulationWithCostAndNutMiniMaxi() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				Collection<QName> dataLists = productDictionaryService.getDataLists();								
				
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");				 
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);				
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				DecimalFormat df = new DecimalFormat("0.####");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				//costs
				int checks = 0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - maxi: " + costListDataItem.getMaxi() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0d, costListDataItem.getValue());
						assertEquals("cost1.getMaxi() == 5.15, actual values: " + trace, 5.15d, costListDataItem.getMaxi());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						checks++;
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getMaxi() == 6.25, actual values: " + trace, 6.25d, costListDataItem.getMaxi());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						checks++;
					}
				}							
				assertEquals(2, checks);
				
				//nuts
				checks = 0;
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - mini: " + nutListDataItem.getMini() + " - maxi: " + nutListDataItem.getMaxi() + " - unit: " + nutListDataItem.getUnit();
					logger.debug(trace);
					if(nutListDataItem.getNut().equals(nut1)){
						assertEquals("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
						assertEquals("nut1.getMini() == 2.7, actual values: " + trace, 2.7d, nutListDataItem.getMini());
						assertEquals("nut1.getMaxi() == 3.65, actual values: " + trace, df.format(3.65d), df.format(nutListDataItem.getMaxi()));
						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
						checks++;
					}
					if(nutListDataItem.getNut().equals(nut2)){
						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6d, nutListDataItem.getValue());
						assertEquals("nut1.getMini() == 4.55, actual values: " + trace, 4.55d, nutListDataItem.getMini());
						assertEquals("nut1.getMaxi() == 6.2, actual values: " + trace, 6.2d, nutListDataItem.getMaxi());
						assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
						checks++;
					}
				}					
				assertEquals(2, checks);
				
				return null;

			}},false,true);
		   
	   }
	
//	/**
//	 * Test formulate product, that has requirements
//	 *
//	 * @throws Exception the exception
//	 */
//	public void testFormulationWithRequirements() throws Exception{
//		   
//	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
//			public NodeRef execute() throws Throwable {					   							
//					
//				Collection<QName> dataLists = productDictionaryService.getDataLists();								
//				
//				/*-- Create finished product --*/
//				logger.debug("/*-- Create finished product --*/");				 
//				FinishedProductData finishedProduct = new FinishedProductData();
//				finishedProduct.setName("Produit fini 1");
//				finishedProduct.setLegalName("Legal Produit fini 1");
//				finishedProduct.setUnit(ProductUnit.kg);
//				finishedProduct.setQty(2d);
//				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
//				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
//				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
//				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
//				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
//				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
//				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
//				finishedProduct.setCompoList(compoList);
//				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);				
//				
//				/*-- Formulate product --*/
//				logger.debug("/*-- Formulate product --*/");
//				productService.formulate(finishedProductNodeRef);
//				
//				/*-- Verify formulation --*/
//				logger.debug("/*-- Verify formulation --*/");
//				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
//				
//				//costs
//				assertNotNull("CostList is null", formulatedProduct.getCostList());
//				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
//					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
//					logger.debug(trace);
//					if(costListDataItem.getCost().equals(cost1)){
//						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0d, costListDataItem.getValue());
//						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
//					}
//					if(costListDataItem.getCost().equals(cost2)){
//						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
//						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
//					}
//				}				
//				//nuts
//				assertNotNull("NutList is null", formulatedProduct.getNutList());
//				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
//					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
//					logger.debug(trace);
//					if(nutListDataItem.getNut().equals(nut1)){
//						assertEquals("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
//						assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
//						assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
//					}
//					if(nutListDataItem.getNut().equals(nut2)){
//						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6d, nutListDataItem.getValue());
//						assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
//						assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
//					}
//				}
//				
//				/*
//				 * Add requirements 				
//				 */
//				
//				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
//					
//					if(costListDataItem.getCost().equals(cost1)){
//						costListDataItem.setMaxi(3d);
//					}
//				}				
//				//nuts
//				assertNotNull("NutList is null", formulatedProduct.getNutList());
//				for(NutListDataItem nutListDataItem : 	formulatedProduct.getNutList()){
//					if(nutListDataItem.getNut().equals(nut1)){
//						nutListDataItem.setMini(3.1d);
//					}
//					if(nutListDataItem.getNut().equals(nut2)){
//						nutListDataItem.setMaxi(5d);
//					}
//				}
//				
//				productDAO.update(finishedProductNodeRef, formulatedProduct, productDictionaryService.getDataLists());
//				
//				productService.formulate(finishedProductNodeRef);
//				
//				/*
//				 * Checks requirements
//				 */
//								
//				formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
//				
//				int checks = 0;
//				for(ReqCtrlListDataItem reqCtrlList : formulatedProduct.getReqCtrlList()){
//					
//					logger.debug("reqCtrlList.getReqMessage(): " + reqCtrlList.getReqMessage());
//					if(reqCtrlList.getReqMessage().equals("Exigence non respectée sur le coût 'cost1'. Valeur:'4' - Max:'3'")){
//						
//						assertEquals(RequirementType.Tolerated, reqCtrlList.getReqType());						
//						checks++;
//					}
//					else if(reqCtrlList.getReqMessage().equals("Exigence non respectée sur le nutriment 'nut1'. Valeur:'3' - Min:'3,1' - Max:'null'")){
//						
//						assertEquals(RequirementType.Tolerated, reqCtrlList.getReqType());
//						checks++;
//					}
//					else if(reqCtrlList.getReqMessage().equals("Exigence non respectée sur le nutriment 'nut2'. Valeur:'6' - Min:'null' - Max:'5'")){
//						
//						assertEquals(RequirementType.Tolerated, reqCtrlList.getReqType());
//						checks++;
//					}
//					else{
//						checks++;
//					}
//				}				
//					
//				assertEquals(3, checks);
//				
//				return null;
//
//			}},false,true);
//		   
//	   }
	
	/**
	 * Test formulate product and check cost details
	 *
	 * @throws Exception the exception
	 */
	public void testCalculateCostDetails() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				
				/*
				 * Prepare packaging 
				 */
				
				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
				packagingMaterial1.setName("Packaging material 1");
				packagingMaterial1.setLegalName("Legal Packaging material 1");
				//costList
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial1.setCostList(costList);					
				packagingMaterial1NodeRef = productDAO.create(folderNodeRef, packagingMaterial1, dataLists);
				
				/*-- Packaging material 2 --*/					
				PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
				packagingMaterial2.setName("Packaging material 2");
				packagingMaterial2.setLegalName("Legal Packaging material 2");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/m", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/m", null, pkgCost2, false));
				packagingMaterial2.setCostList(costList);					
				packagingMaterial2NodeRef = productDAO.create(folderNodeRef, packagingMaterial2, dataLists);
				
				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
				packagingMaterial3.setName("Packaging material 3");
				packagingMaterial3.setLegalName("Legal Packaging material 3");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial3.setCostList(costList);					
				packagingMaterial3NodeRef = productDAO.create(folderNodeRef, packagingMaterial3, dataLists);
				
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<PackagingListDataItem> packagingList = new ArrayList<PackagingListDataItem>();
				packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PACKAGING_PRIMAIRE, packagingMaterial1NodeRef));
				packagingList.add(new PackagingListDataItem(null, 3d, PackagingListUnit.m, PACKAGING_PRIMAIRE, packagingMaterial2NodeRef));
				packagingList.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, PACKAGING_TERTIAIRE, packagingMaterial3NodeRef));
				finishedProduct.setPackagingList(packagingList);		
				
				
				/*
				 * Composition
				 */
				
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 5d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 10d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 20d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
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
				int checks = 0;
				DecimalFormat df = new DecimalFormat("0.####");
				assertNotNull("CostDetailsList is null", formulatedProduct.getCostDetailsList());
				for(CostDetailsListDataItem costDetailsListDataItem : formulatedProduct.getCostDetailsList()){
					String trace = "cost: " + nodeService.getProperty(costDetailsListDataItem.getCost(), ContentModel.PROP_NAME) + "source: " + nodeService.getProperty(costDetailsListDataItem.getSource(), ContentModel.PROP_NAME) + " - value: " + costDetailsListDataItem.getValue() + " - unit: " + costDetailsListDataItem.getUnit();
					logger.debug(trace);
					
					//cost1
					if(costDetailsListDataItem.getCost().equals(cost1)){
						
						if(costDetailsListDataItem.getSource().equals(rawMaterial1NodeRef)){
						
							checks++;
							assertEquals("cost.getValue() == 1.7325, actual values: " + trace, df.format(1.7325d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 36.5314, actual values: " + trace, df.format(36.5314), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}
						else if(costDetailsListDataItem.getSource().equals(rawMaterial2NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 1.21, actual values: " + trace, df.format(1.21d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 25.5140, actual values: " + trace, df.format(25.5140), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}
						else if(costDetailsListDataItem.getSource().equals(rawMaterial3NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 1.8, actual values: " + trace, df.format(1.8d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 37.9547, actual values: " + trace, df.format(37.9547), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}	
						else{
							checks++;
						}
					}
					
					//cost2
					else if(costDetailsListDataItem.getCost().equals(cost2)){
						
						if(costDetailsListDataItem.getSource().equals(rawMaterial1NodeRef)){
						
							checks++;
							assertEquals("cost.getValue() == 1.155, actual values: " + trace, df.format(1.155d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 16.0976, actual values: " + trace, df.format(16.0976), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}
						else if(costDetailsListDataItem.getSource().equals(rawMaterial2NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 2.42, actual values: " + trace, df.format(2.42d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 33.7282, actual values: " + trace, df.format(33.7282), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}
						else if(costDetailsListDataItem.getSource().equals(rawMaterial3NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 3.6, actual values: " + trace, df.format(3.6d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 50.1742, actual values: " + trace, df.format(50.1742), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}	
						else{
							checks++;
						}
					}
					
					//pkgCost1
					else if(costDetailsListDataItem.getCost().equals(pkgCost1)){
						
						if(costDetailsListDataItem.getSource().equals(packagingMaterial1NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 48.9796, actual values: " + trace, df.format(48.9796), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}						
						else if(costDetailsListDataItem.getSource().equals(packagingMaterial2NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 1.5, actual values: " + trace, df.format(1.5d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 48.9796, actual values: " + trace, df.format(48.9796), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}						
						else if(costDetailsListDataItem.getSource().equals(packagingMaterial3NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 0.0625, actual values: " + trace, df.format(0.0625d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 2.0408, actual values: " + trace, df.format(2.0408), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}
						else{
							checks++;
						}
					}		
					
					//pkgCost2
					else if(costDetailsListDataItem.getCost().equals(pkgCost2)){
						
						if(costDetailsListDataItem.getSource().equals(packagingMaterial1NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 1, actual values: " + trace, df.format(1d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 24.2424, actual values: " + trace, df.format(24.2424), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}
						else if(costDetailsListDataItem.getSource().equals(packagingMaterial2NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 3, actual values: " + trace, df.format(3d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 72.7273, actual values: " + trace, df.format(72.7273), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}						
						else if(costDetailsListDataItem.getSource().equals(packagingMaterial3NodeRef)){
							
							checks++;
							assertEquals("cost.getValue() == 0.125, actual values: " + trace, df.format(0.125d), df.format(costDetailsListDataItem.getValue()));
							assertEquals("cost.getPercentage() == 3.0303, actual values: " + trace, df.format(3.0303), df.format(costDetailsListDataItem.getPercentage()));
							assertEquals("cost.getUnit() == €/kg, actual values: " + trace, "€/kg", costDetailsListDataItem.getUnit());
						}
						else{
							checks++;
						}
					}
					else{
						checks++;
					}
				}
				
				assertEquals("Verify checks done", 12, checks);
								
				return null;

			}},false,true);
		   
	   }
	
	/**
	 * Test formulate product, that the yield field is calculated
	 *
	 * @throws Exception the exception
	 */
	public void testCalculateYieldField() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, null, 3d, 2d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, null, 1d, 100d, CompoListUnit.kg, 10d, grpPate, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 80d, null, CompoListUnit.kg, 5d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 30d, null, CompoListUnit.kg, 10d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 1d, 200d, CompoListUnit.kg, 20d, grpGarniture, DeclarationType.DETAIL_FR, localSF3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 170d, null, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 40d, null, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
				compoList.add(new CompoListDataItem(null, 3, null, 1d, null, CompoListUnit.P, 0d, null, DeclarationType.DECLARE_FR, rawMaterial5NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);				
				
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				int checks = 0;
				DecimalFormat df = new DecimalFormat("0.00");
				
				for(CompoListDataItem c : formulatedProduct.getCompoList()){
					
					logger.debug("Yield: " + c.getYieldPerc());
					
					if(c.getProduct().equals(localSF1NodeRef)){
						double result = 100;
						assertEquals("verify yield", df.format(result), df.format(c.getYieldPerc()));
						checks++;
					}
					else if(c.getProduct().equals(localSF2NodeRef)){
						double result = 100d * 100d / 110d;
						assertEquals("verify yield", df.format(result), df.format(c.getYieldPerc()));
						checks++;
					}
					else if(c.getProduct().equals(localSF3NodeRef)){
						double result = 100d * 200d / (170d + 40d + 0.1d);
						assertEquals("verify yield", df.format(result), df.format(c.getYieldPerc()));
						checks++;
					}
				}
				
				assertEquals("verify checks", 3, checks);
								
				return null;

			}},false,true);
		   
	   }
	
	/**
	 * Test formulate product, when there is a manual listItem
	 *
	 * @throws Exception the exception
	 */
	public void testManualListItem() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
					
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				 Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpPate, DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, grpGarniture, DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.OMIT_FR, rawMaterial4NodeRef));
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
				int checks = 0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						checks++;
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						checks++;
					}
				}
				assertEquals(2, checks);
				
				// manual modification
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						
						nodeService.setProperty(costListDataItem.getNodeRef(), BeCPGModel.PROP_COSTLIST_VALUE, 5.0d);
						nodeService.setProperty(costListDataItem.getNodeRef(), BeCPGModel.PROP_IS_MANUAL_LISTITEM, true);
					}					
				}
				
				productService.formulate(finishedProductNodeRef);
				
				formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				//check costs	
				checks = 0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if(costListDataItem.getCost().equals(cost1)){
						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 5.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						checks++;
					}
					if(costListDataItem.getCost().equals(cost2)){
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
						checks++;
					}
				}
				
				assertEquals(2, checks);
								
				return null;

			}},false,true);
		   
	   }
	
	/**
	 * Test formulate product, when there is process list
	 *
	 * @throws Exception the exception
	 */
	public void testProcess() throws Exception{
		   
	   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   							
				
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COSTLIST);
				
				/*-- Create process steps, resources --*/
				logger.debug("/*-- Create process steps, resources --*/");
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				//Costs
				properties.put(ContentModel.PROP_NAME, "costTransfo");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef costTransfoNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
				
				properties.put(ContentModel.PROP_NAME, "costMOTransfo");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef costMOTransfoNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
				
				properties.put(ContentModel.PROP_NAME, "costMOMaintenance");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef costMOMaintenanceNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
				
				//Steps
				logger.debug("Steps");
				properties.put(ContentModel.PROP_NAME, "Découpe");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef decoupeNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();
				
				properties.put(ContentModel.PROP_NAME, "Hachage");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef hachageNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();
				
				properties.put(ContentModel.PROP_NAME, "Cuisson");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef cuissonNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();
				
				properties.put(ContentModel.PROP_NAME, "Mélange");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef melangeNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();
				
				properties.put(ContentModel.PROP_NAME, "Etape Ligne");			 					 				
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef ligneStepNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), MPMModel.TYPE_PROCESSSTEP, properties).getChildRef();
				
				// resources
				logger.debug("Resources");
				ResourceProductData boucherResourceData = new ResourceProductData();
				boucherResourceData.setName("Boucher");
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 8d, "€/h", null, costMOTransfoNodeRef, false));
				boucherResourceData.setCostList(costList);
				NodeRef boucherResourceNodeRef = productDAO.create(folderNodeRef, boucherResourceData, dataLists);
				
				ResourceProductData operateurResourceData = new ResourceProductData();
				operateurResourceData.setName("Operateur");
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 15d, "€/h", null, costMOTransfoNodeRef, false));
				operateurResourceData.setCostList(costList);
				NodeRef operateurResourceNodeRef = productDAO.create(folderNodeRef, operateurResourceData, dataLists);
				
				ResourceProductData hachoirResourceData = new ResourceProductData();
				hachoirResourceData.setName("Hachoir");
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 10d, "€/h", null, costTransfoNodeRef, false));
				hachoirResourceData.setCostList(costList);
				NodeRef hachoirResourceNodeRef = productDAO.create(folderNodeRef, hachoirResourceData, dataLists);
				
				ResourceProductData cuiseurResourceData = new ResourceProductData();
				cuiseurResourceData.setName("Cuiseur");
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 30d, "€/h", null, costTransfoNodeRef, false));
				cuiseurResourceData.setCostList(costList);
				NodeRef cuiseurResourceNodeRef = productDAO.create(folderNodeRef, cuiseurResourceData, dataLists);
				
				ResourceProductData malaxeurResourceData = new ResourceProductData();
				malaxeurResourceData.setName("Malaxeur");
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 40d, "€/h", null, costTransfoNodeRef, false));
				malaxeurResourceData.setCostList(costList);
				NodeRef malaxeurResourceNodeRef = productDAO.create(folderNodeRef, malaxeurResourceData, dataLists);
				
				ResourceProductData ligneResourceData = new ResourceProductData();
				ligneResourceData.setName("Ligne");
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 30d, "€/h", null, costTransfoNodeRef, false));
				costList.add(new CostListDataItem(null, 15d, "€/h", null, costMOTransfoNodeRef, false));
				costList.add(new CostListDataItem(null, 5d, "€/h", null, costMOMaintenanceNodeRef, false));
				ligneResourceData.setCostList(costList);
				NodeRef ligneResourceNodeRef= productDAO.create(folderNodeRef, ligneResourceData, dataLists);
				
				/*-- Create finished product --*/
				dataLists.clear();
				dataLists.add(MPMModel.TYPE_PROCESSLIST);
				logger.debug("/*-- Create finished product --*/");				 
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(1d);
				List<ProcessListDataItem> processList = new ArrayList<ProcessListDataItem>();
				//decoupe
				processList.add(new ProcessListDataItem(null, 0.4d, 50d, 4d, null, null, null, decoupeNodeRef, null, boucherResourceNodeRef));
				//hachage
				processList.add(new ProcessListDataItem(null, 0.4d, null, null, null, null, null, hachageNodeRef, null, null));
				processList.add(new ProcessListDataItem(null, null, 0.1d, null, null, null, null, null, null, operateurResourceNodeRef));
				processList.add(new ProcessListDataItem(null, null, 1d, 200d, null, null, null, null, null, hachoirResourceNodeRef));
				//cuisson
				processList.add(new ProcessListDataItem(null, 0.4d, null, null, null, null, null, cuissonNodeRef, null, null));
				processList.add(new ProcessListDataItem(null, null, 0.1d, null, null, null, null, null, null, operateurResourceNodeRef));
				processList.add(new ProcessListDataItem(null, null, 1d, 200d, null, null, null, null, null, cuiseurResourceNodeRef));
				//mélange
				processList.add(new ProcessListDataItem(null, 0.24d, null, null, null, null, null, melangeNodeRef, null, null));
				processList.add(new ProcessListDataItem(null, null, 0.1d, null, null, null, null, null, null, operateurResourceNodeRef));
				processList.add(new ProcessListDataItem(null, null, 1d, 600d, null, null, null, null, null, malaxeurResourceNodeRef));
				//ligne
				processList.add(new ProcessListDataItem(null, 1d, 1d, 500d, null, null, null, ligneStepNodeRef, null, ligneResourceNodeRef));				
				finishedProduct.setProcessList(processList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);
								
				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);
				
				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, productDictionaryService.getDataLists());
				
				//costs
				logger.debug("/*-- Verify costs --*/");
				DecimalFormat df = new DecimalFormat("0.00");
				int checks = 0;
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for(CostListDataItem costListDataItem : formulatedProduct.getCostList()){
					//String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
					//logger.debug(trace);
					//Transfo
					if(costListDataItem.getCost().equals(costTransfoNodeRef)){
						assertEquals(df.format(0.156d), df.format(costListDataItem.getValue()));
						assertEquals("€/kg", costListDataItem.getUnit());
						checks++;
					}
					//MOTransfo
					if(costListDataItem.getCost().equals(costMOTransfoNodeRef)){
						assertEquals(df.format(0.8366d), df.format(costListDataItem.getValue()));
						assertEquals("€/kg", costListDataItem.getUnit());
						checks++;
					}
					//Maintenance
					if(costListDataItem.getCost().equals(costMOMaintenanceNodeRef)){
						assertEquals(df.format(0.01d), df.format(costListDataItem.getValue()));
						assertEquals("€/kg", costListDataItem.getUnit());
						checks++;
					}
				}
				assertEquals(3, checks);
				
				logger.debug("/*-- Verify process --*/");							
				checks = 0;
				for(ProcessListDataItem p : formulatedProduct.getProcessList()){
					//logger.debug(p.toString());
					
					if(p.getStep() != null){						
					
						//decoupe
						if(p.getStep().equals(decoupeNodeRef)){
							assertEquals(0.4d, p.getQty());
							assertEquals(50.0d, p.getQtyResource());
							assertEquals(4.0d, p.getRateResource());						
							assertEquals(200.0d, p.getRateProcess());
							assertEquals(500.0d, p.getRateProduct());						
							checks++;
						}
						
						//hachage
						if(p.getStep().equals(hachageNodeRef)){
							assertEquals(0.4d, p.getQty());
							assertEquals(null, p.getQtyResource());
							assertEquals(null, p.getRateResource());						
							assertEquals(200.0d, p.getRateProcess());
							assertEquals(500.0d, p.getRateProduct());						
							checks++;
						}
						
						//cuisson
						if(p.getStep().equals(cuissonNodeRef)){
							assertEquals(0.4d, p.getQty());
							assertEquals(null, p.getQtyResource());
							assertEquals(null, p.getRateResource());						
							assertEquals(200.0d, p.getRateProcess());
							assertEquals(500.0d, p.getRateProduct());						
							checks++;
						}
						
						//mélange
						if(p.getStep().equals(melangeNodeRef)){
							assertEquals(0.24d, p.getQty());
							assertEquals(null, p.getQtyResource());
							assertEquals(null, p.getRateResource());						
							assertEquals(600.0d, p.getRateProcess());
							assertEquals(2500.0d, p.getRateProduct());						
							checks++;
						}
						
						//ligne
						if(p.getStep().equals(ligneStepNodeRef)){
							assertEquals(1.0d, p.getQty());
							assertEquals(1.0d, p.getQtyResource());
							assertEquals(500.0d, p.getRateResource());						
							assertEquals(500.0d, p.getRateProcess());
							assertEquals(500.0d, p.getRateProduct());						
							checks++;
						}
					}
				}
				
				assertEquals(5, checks);
								
				return null;

			}},false,true);
		   
	   }
}

