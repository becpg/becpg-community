/*
 * 
 */
package fr.becpg.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.wiser.Wiser;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;

// TODO: Auto-generated Javadoc
/**
 * base class of test cases for product classes.
 *
 * @author querephi
 */

public class RepoBaseTestCase extends BaseAlfrescoTestCase {
	
	/** The Constant HIERARCHY1_SEA_FOOD. */
	protected static final String HIERARCHY1_SEA_FOOD= "Sea food";
	
	/** The Constant HIERARCHY2_FISH. */
	protected static final String HIERARCHY2_FISH = "Fish";
	
	/** The Constant HIERARCHY2_CRUSTACEAN. */
	protected static final String HIERARCHY2_CRUSTACEAN = "Crustacean";
	
	/** The Constant HIERARCHY1_FROZEN. */
	protected static final String HIERARCHY1_FROZEN = "Frozen";
	
	/** The Constant HIERARCHY2_PIZZA. */
	protected static final String HIERARCHY2_PIZZA = "Pizza";
	
	/** The Constant HIERARCHY2_QUICHE. */
	protected static final String HIERARCHY2_QUICHE = "Quiche";
	
	private static String VALUE_ALLERGEN_TYPE = "Allergène majeur";
	private static String VALUE_COST_CURRENCY = "€";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(RepoBaseTestCase.class);
	
	
	/** The mimetype service. */
	protected MimetypeService mimetypeService;
	
	/** The repository helper. */
	protected Repository repositoryHelper;    	
	
	/** The repo service. */
	protected RepoService repoService;
	
	/** The file folder service. */
	protected FileFolderService fileFolderService;
	
	/** The product dao. */
	protected ProductDAO productDAO;
	
	/** The product dictionary service. */
	protected ProductDictionaryService productDictionaryService;
	
	
	private InitVisitor initRepoVisitor;
	
	/** The allergens. */
	protected List<NodeRef> allergens = new ArrayList<NodeRef>();
    
    /** The costs. */
    protected List<NodeRef> costs = new ArrayList<NodeRef>();
    
    /** The ings. */
    protected List<NodeRef> ings = new ArrayList<NodeRef>();
    
    /** The nuts. */
    protected List<NodeRef> nuts = new ArrayList<NodeRef>();
    
    /** The organos. */
    protected List<NodeRef> organos = new ArrayList<NodeRef>();
    
    protected List<NodeRef> declGroups = new ArrayList<NodeRef>();
	
	protected Wiser wiser = new Wiser(2500);

	    
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		//First start wiser 
		try {
			wiser.start();
		} catch (Exception e) {
			logger.warn("cannot open wiser!");
		}
		//Then context
		super.setUp();	
		

    	mimetypeService = (MimetypeService)ctx.getBean("mimetypeService");
    	repositoryHelper = (Repository)ctx.getBean("repositoryHelper");
    	repoService = (RepoService)ctx.getBean("repoService");
    	fileFolderService = (FileFolderService)ctx.getBean("fileFolderService");
    	productDAO = (ProductDAO)ctx.getBean("productDAO");
        productDictionaryService = (ProductDictionaryService)ctx.getBean("productDictionaryService");
        initRepoVisitor = (InitVisitor)ctx.getBean("initRepoVisitor");
    }
    
    
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
    {
		try {
			wiser.stop();
		} catch (Exception e) {
			logger.warn("cannot stop wiser!");
		}
		
        super.tearDown();
        
    }
	
	/**
	 * Test fake method.
	 */
	public void testFakeMethod(){
		
	}
	
	
	/**
	 * Delete the product report tpls.
	 */
	protected void deleteReportTpls(){
				
	   	NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,  TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
	   	
	   	if(systemFolder != null){	   		
	   		
	   		NodeRef reportsNodeRef = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,  TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
	   		
	   		if(reportsNodeRef != null){
	   		
	   			NodeRef productReportTplsNodeRef = nodeService.getChildByName(reportsNodeRef, ContentModel.ASSOC_CONTAINS,  TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));
		   		
		   		if(productReportTplsNodeRef != null){
		   			nodeService.deleteNode(productReportTplsNodeRef);
		   		}
	   		}	   	   		
	   	}   		
	}

	/**
	 * Delete the existing characteristics of the repository.
	 */
	protected void deleteCharacteristics(){
		
		NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
		
		if(systemFolder != null){
		
			NodeRef charactsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_CHARACTS));
			
			//allergens
			NodeRef allergenFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS));
			if(allergenFolder != null){
				nodeService.deleteNode(allergenFolder);
			}		
			
			//costs
			NodeRef costFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
			if(costFolder != null){
				nodeService.deleteNode(costFolder);
			}
			
			
			//ings
			NodeRef ingFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_INGS));
			if(ingFolder != null){
				nodeService.deleteNode(ingFolder);
			}
			
			//nuts
			NodeRef nutFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_NUTS));
			if(nutFolder != null){
				nodeService.deleteNode(nutFolder);
			}
			
			//organos
			NodeRef organoFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ORGANOS));
			if(organoFolder != null){
				nodeService.deleteNode(organoFolder);
			}
			
			//declGroups
			NodeRef declGroupFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_DECL_GROUPS));
			if(declGroupFolder != null){
				nodeService.deleteNode(declGroupFolder);
			}
		}		
	}	

	/**
	 * Initialize the characteristics of the repository.
	 */
	protected void initCharacteristics(){
		
		NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
		NodeRef charactsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_CHARACTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_CHARACTS));
		
		//allergens
		NodeRef allergenFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS));
		if(allergenFolder == null){
			allergenFolder = repoService.createFolderByPath(charactsFolder, RepoConsts.PATH_ALLERGENS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS));
		}		
		List<FileInfo> allergensFileInfo = fileFolderService.listFiles(allergenFolder);		
		if(allergensFileInfo.size() == 0){
			for(int i=0 ; i<10 ; i++)
	    	{    		
	    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, "Allergen " + i);
	    		properties.put(BeCPGModel.PROP_ALLERGEN_TYPE, VALUE_ALLERGEN_TYPE);
	    		ChildAssociationRef childAssocRef = nodeService.createNode(allergenFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties);
	    		allergens.add(childAssocRef.getChildRef());
	    	}
		}
		else{
			for(FileInfo fileInfo : allergensFileInfo){
				allergens.add(fileInfo.getNodeRef());
			}
		}				
		
		//costs
		NodeRef costFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
		if(costFolder == null){
			costFolder = repoService.createFolderByPath(charactsFolder, RepoConsts.PATH_COSTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
		}
		List<FileInfo> costsFileInfo = fileFolderService.listFiles(costFolder);
		if(costsFileInfo.size() == 0){
			
			String [] costNames = {"Coût MP","Coût prév MP","Coût Emb","Coût prév Emb"};
			for(String costName : costNames)
	    	{    		
	    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, costName);
	    		properties.put(BeCPGModel.PROP_COSTCURRENCY, VALUE_COST_CURRENCY);
	    		ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
	    		costs.add(childAssocRef.getChildRef());
	    	}
		}
		else{
			for(FileInfo fileInfo : costsFileInfo){
				costs.add(fileInfo.getNodeRef());
			}
		}
		
		//ings
		NodeRef ingFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_INGS));
		if(ingFolder == null){
			ingFolder = repoService.createFolderByPath(charactsFolder, RepoConsts.PATH_INGS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_INGS));
		}
		List<FileInfo> ingsFileInfo = fileFolderService.listFiles(ingFolder);
		if(ingsFileInfo.size() == 0){
			for(int i=0 ; i<10 ; i++)
	    	{    		
	    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, "Ing " + i);	    		
	    		ChildAssociationRef childAssocRef = nodeService.createNode(ingFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties);
	    		ings.add(childAssocRef.getChildRef());
	    	}
		}
		else{
			for(FileInfo fileInfo : ingsFileInfo){
				ings.add(fileInfo.getNodeRef());
			}
		}
		
		//nuts
		NodeRef nutFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_NUTS));
		if(nutFolder == null){
			nutFolder = repoService.createFolderByPath(charactsFolder, RepoConsts.PATH_NUTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_NUTS));
		}
		List<FileInfo> nutsFileInfo = fileFolderService.listFiles(nutFolder);
		if(nutsFileInfo.size() == 0){
			for(int i=0 ; i<10 ; i++)
	    	{    		
	    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, "Nut " + i);
	    		properties.put(BeCPGModel.PROP_NUTUNIT, "kcal");
	    		properties.put(BeCPGModel.PROP_NUTGROUP, "Groupe 1");
	    		ChildAssociationRef childAssocRef = nodeService.createNode(nutFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties);
	    		nuts.add(childAssocRef.getChildRef());
	    	}
		}
		else{
			for(FileInfo fileInfo : nutsFileInfo){
				nuts.add(fileInfo.getNodeRef());
			}
		}
		
		//organos
		NodeRef organoFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ORGANOS));
		if(organoFolder == null){
			organoFolder = repoService.createFolderByPath(charactsFolder, RepoConsts.PATH_ORGANOS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ORGANOS));
		}
		List<FileInfo> organosFileInfo = fileFolderService.listFiles(organoFolder);
		if(organosFileInfo.size() == 0){
			for(int i=0 ; i<10 ; i++)
	    	{    		
	    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, "Organo " + i);
	    		ChildAssociationRef childAssocRef = nodeService.createNode(organoFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ORGANO, properties);
	    		organos.add(childAssocRef.getChildRef());
	    	}
		}
		else{
			for(FileInfo fileInfo : organosFileInfo){
				organos.add(fileInfo.getNodeRef());
			}
		}		
		
		//declGroups
		NodeRef declGroupFolder = nodeService.getChildByName(charactsFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_DECL_GROUPS));
		if(declGroupFolder == null){
			declGroupFolder = repoService.createFolderByPath(charactsFolder, RepoConsts.PATH_DECL_GROUPS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_DECL_GROUPS));
		}
		List<FileInfo> declGroupsFileInfo = fileFolderService.listFiles(declGroupFolder);
		if(declGroupsFileInfo.size() == 0){
			for(int i=0 ; i<10 ; i++)
	    	{    		
	    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, "DeclGrp " + i);
	    		ChildAssociationRef childAssocRef = nodeService.createNode(declGroupFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_DECL_GROUP, properties);
	    		declGroups.add(childAssocRef.getChildRef());
	    	}
		}
		else{
			for(FileInfo fileInfo : declGroupsFileInfo){
				declGroups.add(fileInfo.getNodeRef());
			}
		}
	}	
	
	/**
	 * Create a raw material.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param name the name
	 * @return the node ref
	 */
	protected NodeRef createRawMaterial(NodeRef parentNodeRef, String name){
		  
		logger.debug("createRawMaterial");							
		
		logger.debug("Create MP");
		RawMaterialData rawMaterial = new RawMaterialData();		
		rawMaterial.setName(name);
		rawMaterial.setHierarchy1(HIERARCHY1_FROZEN);
		rawMaterial.setHierarchy2(HIERARCHY2_FISH);
		
		//Allergens		
		List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();		    		
		for(int j=0 ; j<allergens.size() ; j++)
		{		    			
			AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, false, false, null, null, allergens.get(j), false);
			allergenList.add(allergenListItemData);
		}		
		rawMaterial.setAllergenList(allergenList);
		
		//Costs
		List<CostListDataItem> costList = new ArrayList<CostListDataItem>();		    		
		for(int j=0 ; j<costs.size() ; j++)
		{		    			
			CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, costs.get(j), false);
			costList.add(costListItemData);
		}		
		rawMaterial.setCostList(costList);
		
		//Ings		
		List<IngListDataItem> ingList = new ArrayList<IngListDataItem>();		    		
		for(int j=0 ; j<ings.size() ; j++)
		{		    			
			IngListDataItem ingListItemData = new IngListDataItem(null, 12.2d, null, null, false, false, ings.get(j), false);
			ingList.add(ingListItemData);
		}		
		rawMaterial.setIngList(ingList);
		
		//Nuts		
		List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();		    		
		for(int j=0 ; j<nuts.size() ; j++)
		{		    			
			NutListDataItem nutListItemData = new NutListDataItem(null, 2d, "kJ/100g", 0d, 0d, "Groupe 1", nuts.get(j), false);
			nutList.add(nutListItemData);
		}		
		rawMaterial.setNutList(nutList);
		
		//Organos
		List<OrganoListDataItem> organoList = new ArrayList<OrganoListDataItem>();		    		
		for(int j=0 ; j<organos.size() ; j++)
		{		    			
			OrganoListDataItem organoListItemData = new OrganoListDataItem(null, "Descr organo....", organos.get(j));
			organoList.add(organoListItemData);
		}		
		rawMaterial.setOrganoList(organoList);
		
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
		dataLists.add(BeCPGModel.TYPE_COSTLIST);
		dataLists.add(BeCPGModel.TYPE_INGLIST);
		dataLists.add(BeCPGModel.TYPE_NUTLIST);
		dataLists.add(BeCPGModel.TYPE_ORGANOLIST);
		return productDAO.create(parentNodeRef, rawMaterial, dataLists);
					        	   
	}   
	
	/**
	 * Init the hierarchy lists
	 */
	protected void initRepoAndHierarchyLists(){
		
		logger.debug("initHierarchyLists");
		
		initRepoVisitor.visitContainer(repositoryHelper.getCompanyHome());
		
		//check init repo
		NodeRef systemNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
		assertNotNull("System folder not found", systemNodeRef);		
		NodeRef productHierarchyNodeRef = nodeService.getChildByName(systemNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_HIERARCHY));
		assertNotNull("Product hierarchy folder not found", productHierarchyNodeRef);
		
		NodeRef rawMaterialHierarchy1NodeRef = nodeService.getChildByName(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_HIERARCHY_RAWMATERIAL_HIERARCHY1));
		assertNotNull("raw material hierarchy1 folder not found", rawMaterialHierarchy1NodeRef);
		NodeRef rawMaterialHierarchy2NodeRef = nodeService.getChildByName(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_HIERARCHY_RAWMATERIAL_HIERARCHY2));
		assertNotNull("raw material hierarchy2 folder not found", rawMaterialHierarchy2NodeRef);
		
		NodeRef finishedProductHierarchy1NodeRef = nodeService.getChildByName(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY1));
		assertNotNull("Finished product hierarchy1 folder not found", finishedProductHierarchy1NodeRef);
		NodeRef finishedProductHierarchy2NodeRef = nodeService.getChildByName(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY2));
		assertNotNull("Finished product hierarchy2 folder not found", finishedProductHierarchy2NodeRef);
		
		/*-- create hierarchy --*/
		//RawMaterial - Sea food
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
		properties.put(ContentModel.PROP_NAME, HIERARCHY1_SEA_FOOD);
		nodeService.createNode(rawMaterialHierarchy1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		//FinishedProduct - Frozen
		properties.clear();		
		properties.put(ContentModel.PROP_NAME, HIERARCHY1_FROZEN);
		nodeService.createNode(finishedProductHierarchy1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		//Sea food - Fish
		properties.clear();
		properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, String.format("%s - %s", HIERARCHY1_SEA_FOOD, HIERARCHY2_FISH));
		properties.put(BeCPGModel.PROP_LINKED_VALUE_PREV_VALUE, HIERARCHY1_SEA_FOOD);
		properties.put(BeCPGModel.PROP_LINKED_VALUE_VALUE, HIERARCHY2_FISH);
		nodeService.createNode(rawMaterialHierarchy2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LINKED_VALUE, properties);
		//Sea food - Crustacean
		properties.clear();
		properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, String.format("%s - %s", HIERARCHY1_SEA_FOOD, HIERARCHY2_CRUSTACEAN));
		properties.put(BeCPGModel.PROP_LINKED_VALUE_PREV_VALUE, HIERARCHY1_SEA_FOOD);
		properties.put(BeCPGModel.PROP_LINKED_VALUE_VALUE, HIERARCHY2_CRUSTACEAN);
		nodeService.createNode(rawMaterialHierarchy2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LINKED_VALUE, properties);
		//Frozen - Pizza
		properties.clear();
		properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, String.format("%s - %s", HIERARCHY1_FROZEN, HIERARCHY2_PIZZA));
		properties.put(BeCPGModel.PROP_LINKED_VALUE_PREV_VALUE, HIERARCHY1_FROZEN);
		properties.put(BeCPGModel.PROP_LINKED_VALUE_VALUE, HIERARCHY2_PIZZA);
		nodeService.createNode(finishedProductHierarchy2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LINKED_VALUE, properties);
		//Frozen - Quiche
		properties.clear();
		properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, String.format("%s - %s", HIERARCHY1_FROZEN, HIERARCHY2_QUICHE));
		properties.put(BeCPGModel.PROP_LINKED_VALUE_PREV_VALUE, HIERARCHY1_FROZEN);
		properties.put(BeCPGModel.PROP_LINKED_VALUE_VALUE, HIERARCHY2_QUICHE);
		nodeService.createNode(finishedProductHierarchy2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LINKED_VALUE, properties);			
	}
}
