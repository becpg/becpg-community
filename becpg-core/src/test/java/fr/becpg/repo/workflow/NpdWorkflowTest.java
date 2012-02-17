package fr.becpg.repo.workflow;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NPDModel;
import fr.becpg.repo.admin.NPDGroup;
import fr.becpg.repo.admin.SystemGroup;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.RepoBaseTestCase;

public class NpdWorkflowTest extends RepoBaseTestCase {

	protected static final String USER_ONE = "matthieuWF";

	protected static final String USER_TWO = "philippeWF";

	protected static String[] groups = { NPDGroup.MarketingBrief.toString(), NPDGroup.NeedDefinition.toString(),
			NPDGroup.ValidateNeedDefinition.toString(), NPDGroup.DoPrototype.toString(),
			NPDGroup.StartProduction.toString(), NPDGroup.ValidateFaisability.toString(),
			NPDGroup.FaisabilityAssignersGroup.toString(),SystemGroup.Quality.toString() };

	/** The logger. */
	private static Log logger = LogFactory.getLog(NpdWorkflowTest.class);

	private AuthorityService authorityService;

	private MutableAuthenticationDao authenticationDAO;

	private MutableAuthenticationService authenticationService;

	private PersonService personService;

	private WorkflowService workflowService;

	private  SearchService searchService;
	
	private NodeRef productNodeRef;

	
	   /** The PAT h_ productfolder. */
    private static String PATH_PRODUCTFOLDER = "TestProductFolder";
    
    /** The GROU p1. */
    private static String GROUP1 = "Groupe 1";      
    
    /** The GROU p2. */
    private static String GROUP2 = "Groupe 2";
    
    
    /** The GROU p_ garniture. */
    private static String GROUP_GARNITURE = "Garniture";
    
    /** The GROU p_ pate. */
    private static String GROUP_PATE = "Pâte";
    
    public static final String  FLOAT_FORMAT = "0.0000";
    
    /** The folder node ref. */
    private NodeRef folderNodeRef;
    
    
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
    
    
    /** The cost1. */
    private NodeRef cost1;
    
    /** The cost2. */
    private NodeRef cost2;
    
    
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
    
    /**
     * The client
     */
    private NodeRef client;
	
	
	private EntityService entityService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		fileFolderService = (FileFolderService) ctx.getBean("fileFolderService");
		repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
		
		entityService = (EntityService) ctx.getBean("entityService");
		searchService = serviceRegistry.getSearchService();
		workflowService = serviceRegistry.getWorkflowService();
		 productDAO = (ProductDAO)ctx.getBean("productDAO");
	        productDictionaryService = (ProductDictionaryService)ctx.getBean("productDictionaryService");

		authenticationService =  serviceRegistry.getAuthenticationService();
		authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
		authorityService = (AuthorityService) ctx.getBean("authorityService");

		personService = (PersonService) ctx.getBean("PersonService");



	}

	private void createUsers() {

		/*
		 * Matthieu : user Philippe : validators
		 */

		for (String group : groups) {

			if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)) {
				logger.debug("create group: " + group);
				authorityService.createAuthority(AuthorityType.GROUP, group);
			}
		}
		Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
				PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + NPDGroup.ValidateFaisability.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString(),
					PermissionService.GROUP_PREFIX + NPDGroup.ValidateFaisability.toString());
		
		
		NodeRef userOne = this.personService.getPerson(USER_ONE);
		if (userOne != null) {
			this.personService.deletePerson(userOne);
		}

		if (!authenticationDAO.userExists(USER_ONE)) {
			createUser(USER_ONE);
			authorityService
					.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.MarketingBrief.toString(), USER_ONE);
			authorityService
					.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.NeedDefinition.toString(), USER_ONE);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.StartProduction.toString(),
					USER_ONE);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.DoPrototype.toString(), USER_ONE);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString(),
					USER_ONE);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.ValidateFaisability.toString(),
					USER_ONE);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.ValidateNeedDefinition.toString(),
					USER_ONE);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Quality.toString(),
					USER_ONE);

		}

		if (!authenticationDAO.userExists(USER_TWO)) {
			createUser(USER_TWO);

			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.ValidateFaisability.toString(),
					USER_TWO);
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.ValidateNeedDefinition.toString(),
					USER_TWO);

		}

		for (String s : authorityService.getAuthoritiesForUser(USER_ONE)) {
			logger.debug("user in group: " + s);
		}

	}

	private void createUser(String userName) {
		if (this.authenticationService.authenticationExists(userName) == false) {
			this.authenticationService.createAuthentication(userName, "PWD".toCharArray());

			PropertyMap ppOne = new PropertyMap(4);
			ppOne.put(ContentModel.PROP_USERNAME, userName);
			ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
			ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
			ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
			ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

			this.personService.createPerson(ppOne);
		}
	}

	

	/**
	 * Inits the parts.
	 */
	private void initParts(){
		
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
			nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "pkgCost2");			 					 				
			properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
			nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();
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
			properties.put(BeCPGModel.PROP_ING_TYPE, "Ingrédient");
			ing1 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing2");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing2 default");
			mlName.addValue(Locale.ENGLISH, "ing2 english");
			mlName.addValue(Locale.FRENCH, "ing2 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			properties.put(BeCPGModel.PROP_ING_TYPE, "Ingrédient");
			ing2 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "ing3");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing3 default");
			mlName.addValue(Locale.ENGLISH, "ing3 english");
			mlName.addValue(Locale.FRENCH, "ing3 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			properties.put(BeCPGModel.PROP_ING_TYPE, "Ingrédient");
			ing3 = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
			properties.put(ContentModel.PROP_NAME, "ing4");
			mlName = new MLText();
			mlName.addValue(Locale.getDefault(), "ing4 default");
			mlName.addValue(Locale.ENGLISH, "ing4 english");
			mlName.addValue(Locale.FRENCH, "ing4 french");	
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
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
			costList.add(new CostListDataItem(null, 3f, "€/kg", null, cost1, false));
			costList.add(new CostListDataItem(null, 2f, "€/kg", null, cost2, false));
			rawMaterial1.setCostList(costList);
			//nutList
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f, 0f, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f, 0f, "Groupe 1", nut2, false));
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
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, false, false, ing1, false));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 2f, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial1.setIngList(ingList);
			productDAO.create(folderNodeRef, rawMaterial1, dataLists);
			
			/*-- Raw material 2 --*/
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			rawMaterial2.setLegalName("Legal Raw material 2");
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 1f, "€/kg", null, cost1, false));
			costList.add(new CostListDataItem(null, 2f, "€/kg", null, cost1, false));
			rawMaterial2.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f,  0f, "Groupe 1", nut2, false));
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
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, true, true, ing1, false));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 3f, geoOrigins, bioOrigins, false, false, ing2, false));
			rawMaterial2.setIngList(ingList);			
			productDAO.create(folderNodeRef, rawMaterial2, dataLists);
			
			/*-- Raw material 3 --*/
			RawMaterialData rawMaterial3 = new RawMaterialData();
			rawMaterial3.setName("Raw material 3");
			rawMaterial3.setLegalName("Legal Raw material 3");
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 1f, "€/kg", null, cost1, false));
			costList.add(new CostListDataItem(null, 2f, "€/kg", null, cost1, false));
			rawMaterial3.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 2f, "g/100g", 0f,  0f, "Groupe 1", nut2, false));
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
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3, false));			
			rawMaterial3.setIngList(ingList);		
			productDAO.create(folderNodeRef, rawMaterial3, dataLists);
			
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
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3, false));			
			rawMaterial4.setIngList(ingList);		
			productDAO.create(folderNodeRef, rawMaterial4, dataLists);
			
			/*-- Raw material 5 --*/
			RawMaterialData rawMaterial5 = new RawMaterialData();
			rawMaterial5.setName("Raw material 5");
			rawMaterial5.setLegalName("Legal Raw material 5");
			//costList
			costList = new ArrayList<CostListDataItem>();
			costList.add(new CostListDataItem(null, 5f, "€/m", null, cost1, false));
			costList.add(new CostListDataItem(null, 6f, "€/m", null, cost1, false));
			rawMaterial5.setCostList(costList);
			//nutList
			nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 1f, "g/100g", 0f,  0f, "Groupe 1", nut1, false));
			nutList.add(new NutListDataItem(null, 3f, "g/100g", 0f,  0f, "Groupe 1", nut2, false));
			rawMaterial5.setNutList(nutList);					
			rawMaterial5.setIngList(ingList);		
			productDAO.create(folderNodeRef, rawMaterial5, dataLists);
			
			/*-- Local semi finished product 1 --*/
			LocalSemiFinishedProduct localSF1 = new LocalSemiFinishedProduct();
			localSF1.setName("Local semi finished 1");
			localSF1.setLegalName("Legal Local semi finished 1");
			productDAO.create(folderNodeRef, localSF1, dataLists);
			
			/*-- Local semi finished product 1 --*/
			LocalSemiFinishedProduct localSF2 = new LocalSemiFinishedProduct();
			localSF2.setName("Local semi finished 2");
			localSF2.setLegalName("Legal Local semi finished 2");							
			productDAO.create(folderNodeRef, localSF2, dataLists);
			
			LocalSemiFinishedProduct localSF3 = new LocalSemiFinishedProduct();
			localSF3.setName("Local semi finished 3");
			localSF3.setLegalName("Legal Local semi finished 3");							
			productDAO.create(folderNodeRef, localSF3, dataLists);			
			
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
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, false, false, ing1, false));
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 2f, geoOrigins, bioOrigins, false, false, ing2, false));
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
			ingList.add(new IngListDataItem(null, 1f, geoOrigins, bioOrigins, true, true, ing1, false));
			bioOrigins = new ArrayList<NodeRef>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<NodeRef>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 3f, geoOrigins, bioOrigins, false, false, ing2, false));
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
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3, false));			
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
			ingList.add(new IngListDataItem(null, 4f, geoOrigins, bioOrigins, true, true, ing3, false));
			ingList.add(new IngListDataItem(null, 2f, geoOrigins, bioOrigins, true, true, ing4, false));
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
			
			/* -- The client --*/
			
			
			client = entityService.createOrCopyFrom(folderNodeRef, null, BeCPGModel.TYPE_CLIENT, "Test client");	
			
			/*-- The finishProduct --*/
			
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
			 dataLists = productDictionaryService.getDataLists();
			 productNodeRef = productDAO.create(folderNodeRef, finishedProduct1, dataLists);
			
			
	}

	public void testNPDWorkFlow() {

		
		authenticationComponent.setSystemUserAsCurrentUser();
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
	 			public NodeRef execute() throws Throwable {
	 				
	 				createUsers();
	 				initParts();
	 		        
	 				return null;

	 			}},false,true); 

	
		NodeRef npfFile = transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {
						return createNPDFile();
					}
				});

		String workflowId = "";
		for (WorkflowDefinition def : workflowService.getAllDefinitions()) {
			logger.debug(def.getId() + " " + def.getName());
			if ("jbpm$bcpgwf:productValidationWF".equals(def.getName())) {
				try {
					for (WorkflowInstance instance : workflowService.getWorkflows(def.getId())) {
						workflowService.deleteWorkflow(instance.getId());
					}

				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			if ("jbpm$npdwf:newProductDevelopmentWF".equals(def.getName())) {
				workflowId = def.getId();
				break;
			}

		}
		// workflowService.undeployDefinition(wfId);
		authenticationComponent.setCurrentUser(USER_ONE);

		// Fill a map of default properties to start the workflow with
		// Start NPD
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		Date dueDate = Calendar.getInstance().getTime();
	//	properties.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "description123");
		properties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
		properties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);

		WorkflowPath path = workflowService.startWorkflow(workflowId, properties);
		assertNotNull("The workflow path is null!", path);

		WorkflowInstance instance = path.getInstance();
		assertNotNull("The workflow instance is null!", instance);

		String workflowInstanceId = instance.getId();

		WorkflowNode node = path.getNode();
		assertNotNull("The workflow node is null!", node);

		assertEquals(node.getName(), "initiate-npd");

		// Update start task

		WorkflowTask task = getNextTaskForWorkflow(workflowInstanceId);

		logger.info("Set start information " + task.getName());
		properties = new HashMap<QName, Serializable>();
		
		NodeRef  workflowPackage = workflowService.createPackage(null);
		ChildAssociationRef childAssoc = nodeService.getPrimaryParent(npfFile);
		nodeService.addChild(workflowPackage, npfFile, WorkflowModel.ASSOC_PACKAGE_CONTAINS, childAssoc.getQName());

		properties.put(NPDModel.PROP_NPD_TYPE, "Etude");
		properties.put(NPDModel.PROP_NPD_PRODUCT_NAME, "Test NPD Product");
		properties.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
		List<NodeRef> folderNodeRefs = new ArrayList<NodeRef>();
		folderNodeRefs.add(folderNodeRef);
		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
		assocs.put(NPDModel.ASSOC_NPD_FOLDER, folderNodeRefs);		
		
		// Info de l'appel d'offre
		properties.put(NPDModel.PROP_CFT_TRANSMITTER, "matthieu");
		properties.put(NPDModel.PROP_CFT_COMPANY, "cftCompany");
		properties.put(NPDModel.PROP_CFT_OPENING_DATE, new Date());
		properties.put(NPDModel.PROP_CFT_SAMPLING_DATE, new Date());
		properties.put(NPDModel.PROP_CFT_LAUNCH_DATE_DESIRED, new Date());
		properties.put(NPDModel.PROP_CFT_RESPONSE_DATE_DESIRED, new Date());
		properties.put(NPDModel.PROP_CFT_REPONSE_DATEREALIZED, new Date());
		properties.put(NPDModel.PROP_UNIT_PRICE, new Float(10));
		assocs.put(NPDModel.ASSOC_CFT_CLIENT, getClientsNodeRef());
		

		workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());

		task = workflowService.endTask(task.getId(), "start");

		logger.debug("NpdNUmber :"+task.getProperties().get(NPDModel.PROP_NPD_NUMBER));
		logger.debug("End task"+task.getName());

		path = task.getPath();

		
		// Go to marketing brief

		//assertEquals(path.getNode().getName(), "marketing-brief");
	
		//path = doMarketingBriefStep(workflowInstanceId);

		// //Go to needDefinition
		assertEquals(path.getNode().getName(), "needDefinition");
		path = doNeedDefinition(workflowInstanceId);
		

		//warning fork here
		path = doNeedValidationStep(workflowInstanceId, "approve-needDefinition");

		
		
		// recipe // Packaging

		doFeasibilityAssigment(workflowInstanceId);


		// Do

		doFeasibilityAnalisys(workflowInstanceId);

		// Join
		
		doValidateAnalisys(workflowInstanceId);

		assertTrue(workflowService.getWorkflowById(workflowInstanceId).isActive());
		workflowService.cancelWorkflow(workflowInstanceId);

	}

	protected WorkflowTask getNextTaskForWorkflow(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		assertEquals(1, workflowTasks.size());
		return workflowTasks.get(0);
	}

	private NodeRef createNPDFile() {
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, "test.np");

		NodeRef nodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
				(String) properties.get(ContentModel.PROP_NAME));
		if (nodeRef != null) {
			nodeService.deleteNode(nodeRef);
		}
		nodeRef = nodeService.createNode(
				repositoryHelper.getCompanyHome(),
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
						(String) properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties)
				.getChildRef();

		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/import/Import.csv");

		writer.putContent(in);
		return nodeRef;
	}
//
//	private void printExecutionContext(WorkflowPath path) {
//		for (Map.Entry<QName, Serializable> prop : workflowService.getPathProperties(path.getId()).entrySet()) {
//			logger.info(prop.getKey() + " " + prop.getValue());
//
//		}
//
//	}

	private void doValidateAnalisys(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		
		for (WorkflowTask task : workflowTasks) {
			if ("npdwf:validate-analysis".equals(task.getName())) {
				logger.debug("End task"+task.getName());
				workflowService.endTask(task.getId(),null );
			}
		}
	}

	private void doFeasibilityAnalisys(String workflowInstanceId) {
		
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		
		for (WorkflowTask task : workflowTasks) {
			if ("npdwf:feasibility-analysis".equals(task.getName())) {
//				List<NodeRef> contents = workflowService.getPackageContents(task.getId());
//				assertTrue(contents.size()==1);
//				assertEquals( "Test NPD Product", nodeService.getProperty(contents.get(0),ContentModel.PROP_NAME));
				
				logger.debug("End task"+task.getName());
				
				workflowService.endTask(task.getId(),null );
			}

		}

	}

	private void doFeasibilityAssigment(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
		
		Map<QName, Serializable> props = new HashMap<QName, Serializable>();

		List<NodeRef> assignees = new ArrayList<NodeRef>();
		assignees.add(personService.getPerson(USER_TWO));
		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
		assocs.put(WorkflowModel.ASSOC_ASSIGNEES, assignees);

		for (WorkflowTask task : workflowTasks) {
			if ("npdwf:feasibility-analysis-assignement".equals(task.getName())) {
				logger.info("Assign feasibity " + task.getName());
				workflowService.updateTask(task.getId(), props, assocs, new HashMap<QName, List<NodeRef>>());
				logger.debug("End task"+task.getName());
				workflowService.endTask(task.getId(),null );
			}

		}

	}
	
	private NodeRef findGroupNode(String groupShortName)
    {
        //TODO Use new AuthorityService.getNode() method on HEAD
        NodeRef group = null;
        
        String query = "+TYPE:\"cm:authorityContainer\" AND @cm\\:authorityName:*" + groupShortName;
        
        ResultSet results = null;
        try
        {
            results = searchService.query(
                    new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), 
                    SearchService.LANGUAGE_LUCENE, query);
            
            if (results.length() > 0)
            {
                group = results.getNodeRefs().get(0);
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
        return group;
    }
	

//	private WorkflowPath doMarketingBriefStep(String workflowInstanceId) {
//
//		WorkflowTask task = getNextTaskForWorkflow(workflowInstanceId);
//		logger.info("Set marketing brief information " + task.getName());
//		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
//		properties.put(NPDModel.PROP_CFT_TRANSMITTER, "matthieu");
//		properties.put(NPDModel.PROP_CFT_COMPANY, "cftCompany");
//		properties.put(NPDModel.PROP_CFT_OPENING_DATE, new Date());
//		properties.put(NPDModel.PROP_CFT_SAMPLING_DATE, new Date());
//		properties.put(NPDModel.PROP_CFT_LAUNCH_DATE_DESIRED, new Date());
//		properties.put(NPDModel.PROP_CFT_RESPONSE_DATE_DESIRED, new Date());
//		properties.put(NPDModel.PROP_CFT_REPONSE_DATEREALIZED, new Date());
//		properties.put(NPDModel.PROP_UNIT_PRICE, new Float(10));
//		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
//		assocs.put(NPDModel.ASSOC_CFT_CLIENT, getClientsNodeRef());
//
//		workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
//		logger.debug("End task"+task.getName());
//		task = workflowService.endTask(task.getId(), "submit");
//
//		return task.getPath();
//
//	}


	private WorkflowPath doNeedDefinition(String workflowInstanceId) {
		WorkflowTask task = getNextTaskForWorkflow(workflowInstanceId);
		logger.debug("End task"+task.getName());
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(NPDModel.PROP_RECIPE_DESCRIPTION, "Test recette");
		properties.put(NPDModel.PROP_PACKAGING_DESCRIPTION, "Test packaging");
		
		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
		assocs.put(NPDModel.ASSOC_NEED_DEFINITION_PRODUCT, getProductNodeRef());
		assocs.put(NPDModel.ASSOC_NEED_DEFINITION_RECIPE, getRecipeNodeRef());
		assocs.put(NPDModel.ASSOC_NEED_DEFINITION_PACKAGING, getPackagingNodeRef());
		
		// groupe Asssignees
		List<NodeRef> assignees = new ArrayList<NodeRef>();
		assignees.add(findGroupNode(PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString()));		
		assocs.put(WorkflowModel.ASSOC_GROUP_ASSIGNEES, assignees);
		
		workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
		task = workflowService.endTask(task.getId(), "submit");
		return task.getPath();
	}

	private List<NodeRef> getPackagingNodeRef() {
		List<NodeRef> ret = new ArrayList<NodeRef>();
		ret.add(productNodeRef);

		return ret;
	}

	private List<NodeRef> getRecipeNodeRef() {
		List<NodeRef> ret = new ArrayList<NodeRef>();
		ret.add(productNodeRef);

		return ret;
	}

	private List<NodeRef> getProductNodeRef() {
		List<NodeRef> ret = new ArrayList<NodeRef>();
		ret.add(productNodeRef);
		
		return ret;
	}
	

	private List<NodeRef> getClientsNodeRef() {
		List<NodeRef> ret = new ArrayList<NodeRef>();
		ret.add(client);
		
		return ret;
	}

	private WorkflowPath doNeedValidationStep(String workflowInstanceId, String transition) {
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(workflowInstanceId);
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

		List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);

		WorkflowPath path = null;
		

		List<NodeRef> assignees = new ArrayList<NodeRef>();
		assignees.add(findGroupNode(PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString()));
		
		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
		assocs.put(WorkflowModel.ASSOC_GROUP_ASSIGNEES, assignees);
		for(WorkflowTask task : workflowTasks){
			logger.debug("End task"+task.getName());
		///	workflowService.updateTask(task.getId(), properties, assocs,new HashMap<QName, List<NodeRef>>());
			task = workflowService.endTask(task.getId(), transition);
			path = task.getPath();
		}
		
		
		
		return path;
	}

}
