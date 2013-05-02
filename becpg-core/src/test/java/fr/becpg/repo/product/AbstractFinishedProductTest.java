package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.test.RepoBaseTestCase;

public abstract class AbstractFinishedProductTest extends RepoBaseTestCase{
	

	/** The logger. */
	protected static Log logger = LogFactory.getLog(AbstractFinishedProductTest.class);

	
	/** The product service. */
	@Resource
	protected ProductService productService;    

    
    /** The GROU p1. */
	protected static String GROUP1 = "Groupe 1";      
    
    /** The GROU p2. */
	protected static String GROUP2 = "Groupe 2";
    
    /** The GROUPOTHER. */
	protected static String GROUPOTHER = "Autre";
    
	protected static String PACKAGING_PRIMAIRE = "Primaire";
	protected static String PACKAGING_TERTIAIRE = "Tertiaire";
    
	protected static final String  FLOAT_FORMAT = "0.0000";
    
    /** The local s f1 node ref. */
	protected NodeRef  localSF1NodeRef;
    
    /** The raw material1 node ref. */
	protected NodeRef  rawMaterial1NodeRef;
    
    /** The raw material2 node ref. */
	protected NodeRef  rawMaterial2NodeRef;
    
    /** The local s f2 node ref. */
	protected NodeRef  localSF2NodeRef;
    
	protected NodeRef  localSF3NodeRef;
    
    /** The raw material3 node ref. */
    protected NodeRef  rawMaterial3NodeRef;
    
    /** The raw material4 node ref. */
    protected NodeRef  rawMaterial4NodeRef;
    
    /** The raw material5 node ref. */
    protected NodeRef  rawMaterial5NodeRef;
    
    protected NodeRef rawMaterial6NodeRef;
    
    protected NodeRef rawMaterial7NodeRef;
    
    /** The local s f11 node ref. */
    protected NodeRef localSF11NodeRef;
    
    /** The raw material11 node ref. */
    protected NodeRef rawMaterial11NodeRef;
    
    /** The raw material12 node ref. */
    protected NodeRef rawMaterial12NodeRef;
    
    /** The local s f12 node ref. */
    protected NodeRef localSF12NodeRef;
    
    /** The raw material13 node ref. */
    protected NodeRef rawMaterial13NodeRef;
    
    /** The raw material14 node ref. */
    protected NodeRef rawMaterial14NodeRef;
    
    protected NodeRef rawMaterialWaterNodeRef;
    
    protected NodeRef packagingMaterial1NodeRef;
    protected NodeRef packagingMaterial2NodeRef;
    protected NodeRef packagingMaterial3NodeRef;
    
    /** The cost1. */
    protected NodeRef cost1;
    
    /** The cost2. */
    protected NodeRef cost2;
    
    protected NodeRef fixedCost;
    
    protected NodeRef pkgCost1;
    
    protected NodeRef pkgCost2;
    
    /** The nut1. */
    protected NodeRef nut1;
    
    /** The nut2. */
    protected NodeRef nut2;
    
    /** The allergen1. */
    protected NodeRef allergen1;
    
    /** The allergen2. */
    protected NodeRef allergen2;
    
    /** The allergen3. */
    protected NodeRef allergen3;
    
    /** The allergen4. */
    protected NodeRef allergen4;
    
    /** The ing1. */
    protected NodeRef ing1;
    
    /** The ing2. */
    protected NodeRef ing2;
    
    /** The ing3. */
    protected NodeRef ing3;
    
    /** The ing4. */
    protected NodeRef ing4;
    
    protected NodeRef ing5;
    
    protected NodeRef ingWater;
    
    /** The bio origin1. */
    protected NodeRef bioOrigin1;
    
    /** The bio origin2. */
    protected NodeRef bioOrigin2;
    
    /** The geo origin1. */
    protected NodeRef geoOrigin1;
    
    /** The geo origin2. */
    protected NodeRef geoOrigin2;    
    
    protected NodeRef physicoChem1;
    
    protected NodeRef physicoChem2;
    
    protected NodeRef physicoChem3;
    
    protected NodeRef physicoChem4;
    
    /**
	 * Inits the parts.
	 */
	protected void initParts(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			public NodeRef execute() throws Throwable {					   
			
			/*-- characteristics --*/
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			//Costs
			properties.put(ContentModel.PROP_NAME, "cost1");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			cost1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "cost2");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			cost2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "pkgCost1");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			pkgCost1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "pkgCost2");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			pkgCost2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();						
			properties.clear();				
			properties.put(ContentModel.PROP_NAME, "fixedCost");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			properties.put(BeCPGModel.PROP_COSTFIXED, true);
			fixedCost = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			
			//Nuts
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "nut1");
			properties.put(BeCPGModel.PROP_NUTUNIT, "kJ");
			properties.put(BeCPGModel.PROP_NUTGROUP, GROUP1);
			nut1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "nut2");
			properties.put(BeCPGModel.PROP_NUTUNIT, "kcal");
			properties.put(BeCPGModel.PROP_NUTGROUP, GROUP2);
			nut2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties).getChildRef();			
			//Allergens
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen1");			 					 				
			allergen1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen2");			 					 				
			allergen2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen3");			 					 				
			allergen3 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "allergen4");			 					 				
			allergen4 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties).getChildRef();
			//Ings
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing1");
			MLText mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing1 default");
			mlName.addValue(Locale.ENGLISH, "ing1 english");
			mlName.addValue(Locale.FRENCH, "ing1 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing2");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing2 default");
			mlName.addValue(Locale.ENGLISH, "ing2 english");
			mlName.addValue(Locale.FRENCH, "ing2 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing3");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing3 default");
			mlName.addValue(Locale.ENGLISH, "ing3 english");
			mlName.addValue(Locale.FRENCH, "ing3 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing3 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing4");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing4 default");
			mlName.addValue(Locale.ENGLISH, "ing4 english");
			mlName.addValue(Locale.FRENCH, "ing4 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing4 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing5");
			properties.put(BeCPGModel.PROP_ING_TYPE, "Epaississant");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing5 default");
			mlName.addValue(Locale.ENGLISH, "ing5 english");
			mlName.addValue(Locale.FRENCH, "ing5 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing5 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "eau");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "eau default");
			mlName.addValue(Locale.ENGLISH, "eau english");
			mlName.addValue(Locale.FRENCH, "eau french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ingWater = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			//Geo origins
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "geoOrigin1");
			geoOrigin1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "geoOrigin2");
			geoOrigin2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			//Bio origins
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "bioOrigin1");
			bioOrigin1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_BIO_ORIGIN, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "bioOrigin2");
			bioOrigin2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_BIO_ORIGIN, properties).getChildRef();
			//physicoChem
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "physicoChem1");			 					 				
			physicoChem1 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "physicoChem2");			 					 				
			physicoChem2 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "physicoChem3");
			properties.put(BeCPGModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			physicoChem3 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "physicoChem4");			 					 				
			properties.put(BeCPGModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			physicoChem4 = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_PHYSICO_CHEM, properties).getChildRef();			
			
			/*-- Create raw materials --*/
			logger.debug("/*-- Create raw materials --*/");
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
			ingList.add(new IngListDataItem(null, 100/3d, geoOrigins, bioOrigins, false, false, ing1, false));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 200/3d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial1.setIngList(ingList);
			//physicoChem
			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 3d, "-", null, 3.1d, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, 2.1d, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", 0.8d, 2.1d, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", 1.5d, 2.2d, physicoChem4));
			rawMaterial1.setPhysicoChemList(physicoChemList);
			rawMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial1).getNodeRef();
			
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
			ingList.add(new IngListDataItem(null, 100/4d, geoOrigins, bioOrigins, true, true, ing1, false));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 300/4d, geoOrigins, bioOrigins, false, false, ing2, false));
			//physicoChem
			physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, 2.1d, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, 2.2d, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", 0.8d, 1.1d, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", 0.8d, 2.1d, physicoChem4));
			rawMaterial2.setPhysicoChemList(physicoChemList);
			rawMaterial2.setIngList(ingList);	
			
			rawMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial2).getNodeRef();
			
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
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, ing3, false));
			//physicoChem
			physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, null, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, null, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, null, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, null, physicoChem4));
			rawMaterial3.setPhysicoChemList(physicoChemList);
			rawMaterial3.setIngList(ingList);		
			rawMaterial3NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial3).getNodeRef();
			
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
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, ing3, false));			
			rawMaterial4.setIngList(ingList);		
			rawMaterial4.setCostList(new LinkedList<CostListDataItem>());
			rawMaterial4.setNutList(new LinkedList<NutListDataItem>());
			rawMaterial4NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial4).getNodeRef();
			
			/*-- Raw material 5 --*/
			ingList = new ArrayList<IngListDataItem>();
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, ing3, false));	
			
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
			rawMaterial5NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial5).getNodeRef();
			
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
			rawMaterial6NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial6).getNodeRef();
			
			/*-- Raw material 7 --*/
			RawMaterialData rawMaterial7 = new RawMaterialData();
			rawMaterial7.setName("Raw material 7");
			legalName = new MLText("Legal Raw material 7");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 7");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 7");
			rawMaterial7.setLegalName(legalName);
			rawMaterial7.setUnit(ProductUnit.kg);
			rawMaterial7.setDensity(1d);			
			//ingList : ing5
			ingList = new ArrayList<IngListDataItem>();			
			ingList.add(new IngListDataItem(null, 100d, null, null, false, false, ing5, false));
			rawMaterial7.setIngList(ingList);
			rawMaterial7NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial7).getNodeRef();
			
			/*-- Local semi finished product 1 --*/
			LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
			localSF1.setName("Local semi finished 1");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");				
			localSF1.setLegalName(mlName);
			localSF1NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF1).getNodeRef();
			
			/*-- Local semi finished product 1 --*/
			LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
			localSF2.setName("Local semi finished 2");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");
			localSF2.setLegalName(mlName);							
			localSF2NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF2).getNodeRef();
			
			LocalSemiFinishedProductData localSF3 = new LocalSemiFinishedProductData();
			localSF3.setName("Local semi finished 3");
			localSF3.setLegalName("Legal Local semi finished 3");							
			localSF3NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF3).getNodeRef();			
			
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
			ingList.add(new IngListDataItem(null, 100/3d, geoOrigins, bioOrigins, false, false, ing1, false));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 200/3d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial11.setIngList(ingList);
			rawMaterial11NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial11).getNodeRef();
			
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
			ingList.add(new IngListDataItem(null, 100/4d, geoOrigins, bioOrigins, true, true, ing1, false));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 300/4d, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial12.setIngList(ingList);			
			rawMaterial12NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial12).getNodeRef();
			
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
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, ing3, false));			
			rawMaterial13.setIngList(ingList);		
			rawMaterial13NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial13).getNodeRef();
			
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
			ingList.add(new IngListDataItem(null, 200/3d, geoOrigins, bioOrigins, true, true, ing3, false));
			ingList.add(new IngListDataItem(null, 100/3d, geoOrigins, bioOrigins, true, true, ing4, false));
			rawMaterial14.setIngList(ingList);		
			rawMaterial14NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial14).getNodeRef();
			
			/*-- Raw material Water --*/
			RawMaterialData rawMaterialWater = new RawMaterialData();
			rawMaterialWater.setName("Eau réseau");
			legalName = new MLText("Legal Raw material Eau");
			legalName.addValue(Locale.FRENCH, "Legal Raw material Eau");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material Eau");
			rawMaterialWater.setLegalName(legalName);
			ingList = new ArrayList<IngListDataItem>();
			ingList.add(new IngListDataItem(null, 100d, null, null, false, false, ingWater, false));
			rawMaterialWater.setIngList(ingList);		
			rawMaterialWaterNodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterialWater).getNodeRef();
			
			/*-- Local semi finished product 11 --*/
			LocalSemiFinishedProductData localSF11 = new LocalSemiFinishedProductData();
			localSF11.setName("Local semi finished 11");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");
			localSF11.setLegalName(mlName);			
			localSF11NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF11).getNodeRef();
			
			/*-- Local semi finished product 12 --*/
			LocalSemiFinishedProductData localSF12 = new LocalSemiFinishedProductData();
			localSF12.setName("Local semi finished 12");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");
			localSF12.setLegalName(mlName);					
			localSF12NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF12).getNodeRef();
			
			return null;

			}},false,true);
	}
}
