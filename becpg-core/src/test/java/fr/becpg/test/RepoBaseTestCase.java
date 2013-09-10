package fr.becpg.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenType;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * base class of test cases for product classes.
 * 
 * @author querephi
 */

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "classpath:alfresco/application-context.xml")
@ContextConfiguration(locations = {"classpath:alfresco/application-context.xml",
			"classpath:alfresco/web-scripts-application-context.xml",
			"classpath:alfresco/web-scripts-application-context-test.xml"})
public abstract class RepoBaseTestCase extends TestCase implements InitializingBean {

	private static Log logger = LogFactory.getLog(RepoBaseTestCase.class);

	protected static final String HIERARCHY1_SEA_FOOD = "Sea food";
	protected static final String HIERARCHY2_FISH = "Fish";
	protected static final String HIERARCHY2_CRUSTACEAN = "Crustacean";
	protected static final String HIERARCHY1_FROZEN = "Frozen";
	protected static final String HIERARCHY2_PIZZA = "Pizza";
	protected static final String HIERARCHY2_QUICHE = "Quiche";
	protected static final String VALUE_COST_CURRENCY = "€";

	protected NodeRef HIERARCHY1_SEA_FOOD_REF;
	protected NodeRef HIERARCHY2_FISH_REF;
	protected NodeRef HIERARCHY2_CRUSTACEAN_REF;
	protected NodeRef HIERARCHY1_FROZEN_REF;
	protected NodeRef HIERARCHY2_PIZZA_REF;
	protected NodeRef HIERARCHY2_QUICHE_REF;
	protected NodeRef PROJECT_HIERARCHY1_SEA_FOOD_REF;
	protected NodeRef PROJECT_HIERARCHY2_FISH_REF;
	protected NodeRef PROJECT_HIERARCHY2_CRUSTACEAN_REF;

	protected List<NodeRef> allergens = new ArrayList<NodeRef>();
	protected List<NodeRef> costs = new ArrayList<NodeRef>();
	protected List<NodeRef> ings = new ArrayList<NodeRef>();

	protected List<NodeRef> nuts = new ArrayList<NodeRef>();
	protected List<NodeRef> taskLegends = new ArrayList<NodeRef>();
	protected List<NodeRef> organos = new ArrayList<NodeRef>();
	protected List<NodeRef> labelClaims = new ArrayList<NodeRef>();

	protected NodeRef ingWater;
	protected NodeRef labelingTemplateNodeRef = null;

	protected NodeRef testFolderNodeRef;
	protected NodeRef systemFolderNodeRef;

	public static RepoBaseTestCase INSTANCE;

	public static Wiser wiser = new Wiser(2500);

	@Resource
	protected MimetypeService mimetypeService;

	@Resource
	protected Repository repositoryHelper;

	@Resource
	protected NodeService nodeService;

	@Resource
	protected RepoService repoService;

	@Resource
	protected FileFolderService fileFolderService;

	@Resource
	protected AlfrescoRepository<ProductData> alfrescoRepository;

	@Resource
	protected DictionaryDAO dictionaryDAO;

	@Resource
	protected EntitySystemService entitySystemService;

	@Resource
	protected ServiceRegistry serviceRegistry;

	@Resource
	protected InitVisitor initRepoVisitor;

	@Resource
	protected HierarchyService hierarchyService;

	@Resource
	protected AuthenticationComponent authenticationComponent;

	@Resource
	protected ContentService contentService;

	@Resource
	protected TransactionService transactionService;

	@Resource
	protected RetryingTransactionHelper retryingTransactionHelper;

	@Resource
	protected AuthorityService authorityService;

	@Resource
	protected MutableAuthenticationDao authenticationDAO;

	@Resource
	protected MutableAuthenticationService authenticationService;

	@Resource
	protected PersonService personService;

	@Resource
	protected BeCPGSearchService beCPGSearchService;

	@Resource
	protected EntityTplService entityTplService;
	
	@Resource
	protected PermissionService permissionService;

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;
	}

	@BeforeClass
	public static void setupBeforeClass() {
		try {
			logger.debug("setupBeforeClass : Start wiser");
			wiser.start();
		} catch (Exception e) {
			logger.warn("cannot open wiser!",e);
		}
	}

	@AfterClass
	public static void tearDownBeforeClass() {
		try {
			logger.debug("tearDownBeforeClass : Stop wiser");
			wiser.stop();
		} catch (Exception e) {
			logger.warn("cannot stop wiser!",e);
		}

	}

	@Before
	public void setUp() throws Exception {
		
		testFolderNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				// As system user
				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

				// test folder
				return BeCPGTestHelper.createTestFolder();
			}
		}, false, true);

		boolean shouldInit = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Throwable {

				return nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)) == null;
			}
		}, false, true);

		logger.debug("setUp shouldInit :" + shouldInit);

		systemFolderNodeRef  = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				return repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

			}
		}, false, true);

	
		doInitRepo(shouldInit);
		

	}

	private void doInitRepo(final boolean shouldInit) {

		if(shouldInit){
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
				public Boolean execute() throws Throwable {
	
					
					// Init repo for test
					initRepoVisitor.visitContainer(repositoryHelper.getCompanyHome());
	
					Assert.assertEquals(5, entitySystemService.getSystemEntities().size());
	
					initConstraints();
					initTasks();
	
					return false;
	
				}
			}, false, true);
		}

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				if(shouldInit){
					dictionaryDAO.reset();
				}
				initCharacteristics();
				if(shouldInit){
					initEntityTemplates();
				}
				initHierarchyLists();
				// initSystemProducts();
				initLabelingTemplate();
				// reset dictionary to reload constraints on list_values
				dictionaryDAO.reset();
				return null;

			}
		}, false, true);

	}

	@After
	public void tearDown() throws Exception {
		logger.debug("TearDown :");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Throwable {

				// products
				String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_PRODUCT))
						+ LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITY_TPL))
						+ LuceneHelper.exclude(LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, "Eau"));
				List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(query);

				for (NodeRef productNodeRef : productNodeRefs) {
					if (nodeService.exists(productNodeRef)) {
						
						String path = nodeService.getPath(productNodeRef).toDisplayPath(nodeService, permissionService );
					//	if(!path.contains(BeCPGTestHelper.PATH_TESTFOLDER)){
							logger.debug("   - Deleting :"+nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME));
							logger.debug("   - PATH :"+path);
							nodeService.deleteNode(productNodeRef);
					//	}
					}
				}
				logger.debug("   - Deleting :" + nodeService.getProperty(testFolderNodeRef, ContentModel.PROP_NAME));
				nodeService.deleteNode(testFolderNodeRef);
				return true;

			}
		}, false, true);
	}

	private void initConstraints() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_LISTS);

		// nutGroups
		NodeRef nutGroupsFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_NUT_GROUPS);
		String[] nutGroups = { "Groupe 1", "Groupe 2", "Autre" };
		for (String nutGroup : nutGroups) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, nutGroup);
			nodeService.createNode(nutGroupsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		// nutTypes
		NodeRef nutTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_NUT_TYPES);
		String[] nutTypes = { "Nutriment", "Vitamine", "Minéraux", "Valeur énergétique" };
		for (String nutType : nutTypes) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, nutType);
			nodeService.createNode(nutTypesFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		// nutFactsMethods
		NodeRef nutFactsMethodsFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_NUT_FACTS_METHODS);
		String[] nutFactsMethods = { "Formulation", "CIQUAL", "USDA" };
		for (String nutFactsMethod : nutFactsMethods) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, nutFactsMethod);
			nodeService.createNode(nutFactsMethodsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		// ingTypes
		NodeRef ingTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_ING_TYPES);
		String[] ingTypes = { "Epaississant" };
		for (String ingType : ingTypes) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, ingType);
			nodeService.createNode(ingTypesFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		// allergenTypes
		NodeRef allergenTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_ALLERGEN_TYPES);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, AllergenType.Major.toString());
		nodeService.createNode(allergenTypesFolder, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);

		// labelingPosition
		NodeRef labelingPositionFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_LABELING_POSITIONS);
		String[] labelingPositions = { "Côté de la boîte", "Dessus de la boite" };
		for (String labelingPosition : labelingPositions) {
			properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, labelingPosition);
			nodeService.createNode(labelingPositionFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}
	}

	private void initTasks() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_PROJECT_LISTS);

		// taskLegends
		NodeRef taskLegendsFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_TASK_LEGENDS);
		String[] taskLegendNames = { "TaskLegend1", "TaskLegend2", "TaskLegend3" };
		for (String taskLegendName : taskLegendNames) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, taskLegendName);
			nodeService.createNode(taskLegendsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), ProjectModel.TYPE_TASK_LEGEND, properties)
					.getChildRef();
		}

		// score criteria
		NodeRef criteriaFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_SCORE_CRITERIA);
		for (int i = 0; i < 5; i++) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, "Criterion" + i);
			nodeService.createNode(criteriaFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties)
					.getChildRef();
		}

	}

	/**
	 * Initialize the characteristics of the repository.
	 */
	private void initCharacteristics() {

		NodeRef charactsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_CHARACTS);

		// allergens
		NodeRef allergenFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_ALLERGENS);
		List<FileInfo> allergensFileInfo = fileFolderService.listFiles(allergenFolder);
		if (allergensFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Allergen " + i);
				properties.put(BeCPGModel.PROP_ALLERGEN_TYPE, AllergenType.Major.toString());
				ChildAssociationRef childAssocRef = nodeService.createNode(allergenFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties);
				allergens.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : allergensFileInfo) {
				allergens.add(fileInfo.getNodeRef());
			}
		}

		// costs
		NodeRef costFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_COSTS);
		List<FileInfo> costsFileInfo = fileFolderService.listFiles(costFolder);
		if (costsFileInfo.size() == 0) {

			String[] costNames = { "Coût MP", "Coût prév MP", "Coût Emb", "Coût prév Emb" };
			for (String costName : costNames) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, costName);
				properties.put(BeCPGModel.PROP_COSTCURRENCY, VALUE_COST_CURRENCY);
				ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
				costs.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : costsFileInfo) {
				costs.add(fileInfo.getNodeRef());
			}
		}

		// ings
		NodeRef ingFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_INGS);
		List<FileInfo> ingsFileInfo = fileFolderService.listFiles(ingFolder);
		if (ingsFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Ing " + i);
				ChildAssociationRef childAssocRef = nodeService.createNode(ingFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties);
				ings.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : ingsFileInfo) {
				ings.add(fileInfo.getNodeRef());
			}
		}

		ingWater = nodeService.getChildByName(ingFolder, ContentModel.ASSOC_CONTAINS, "eau");

		if (ingWater == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, "eau");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "eau default");
			mlName.addValue(Locale.ENGLISH, "eau english");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ingWater = nodeService.createNode(ingFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties).getChildRef();
		}

		// nuts
		NodeRef nutFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_NUTS);
		List<FileInfo> nutsFileInfo = fileFolderService.listFiles(nutFolder);
		if (nutsFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Nut " + i);
				properties.put(BeCPGModel.PROP_NUTUNIT, "kcal");
				properties.put(BeCPGModel.PROP_NUTGROUP, "Groupe 1");
				ChildAssociationRef childAssocRef = nodeService.createNode(nutFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties);
				nuts.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : nutsFileInfo) {
				nuts.add(fileInfo.getNodeRef());
			}
		}

		// organos
		NodeRef organoFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_ORGANOS);
		List<FileInfo> organosFileInfo = fileFolderService.listFiles(organoFolder);
		if (organosFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Organo " + i);
				ChildAssociationRef childAssocRef = nodeService.createNode(organoFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ORGANO, properties);
				organos.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : organosFileInfo) {
				organos.add(fileInfo.getNodeRef());
			}
		}

		// taskLegends
		NodeRef npdListsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_PROJECT_LISTS);
		NodeRef taskLegendFolder = entitySystemService.getSystemEntityDataList(npdListsFolder, RepoConsts.PATH_TASK_LEGENDS);
		List<FileInfo> taskLegendsFileInfo = fileFolderService.listFiles(taskLegendFolder);
		for (FileInfo fileInfo : taskLegendsFileInfo) {
			taskLegends.add(fileInfo.getNodeRef());
		}
		
		
		// claim labelling
		NodeRef labelClaimListsFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_LABELCLAIMS);
		List<FileInfo> labelClaimsFileInfo = fileFolderService.listFiles(labelClaimListsFolder);
		if (labelClaimsFileInfo.size() == 0) {

			String[] labelClaimNames = { "Faible valeur énergétique","Sans apport énergétique" };
			for (String labelClaim : labelClaimNames) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, labelClaim);
				properties.put(BeCPGModel.PROP_LABEL_CLAIM_TYPE, "Nutritionnelle");
				ChildAssociationRef childAssocRef = nodeService.createNode(labelClaimListsFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LABEL_CLAIM, properties);
				labelClaims.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : labelClaimsFileInfo) {
				labelClaims.add(fileInfo.getNodeRef());
			}
		}
		
	}

	// private void initSystemProducts(){
	//
	// NodeRef contextTestFolderNodeRef =
	// BeCPGTestHelper.createContextTestFolder(repoBaseTestCase);
	//
	// /*-- Raw material Water --*/
	// NodeRef rawMaterialWaterNodeRef =
	// nodeService.getChildByName(contextTestFolderNodeRef,
	// ContentModel.ASSOC_CONTAINS, "Eau réseau");
	//
	// if(rawMaterialWaterNodeRef == null){
	// RawMaterialData rawMaterialWater = new RawMaterialData();
	// rawMaterialWater.setName("Eau réseau");
	// MLText legalName = new MLText("Legal Raw material Eau");
	// legalName.addValue(Locale.FRENCH, "Legal Raw material Eau");
	// legalName.addValue(Locale.ENGLISH, "Legal Raw material Eau");
	// rawMaterialWater.setLegalName(legalName);
	// List<IngListDataItem> ingList = new ArrayList<IngListDataItem>();
	// ingList.add(new IngListDataItem(null, 100d, null, null, false, false,
	// ingWater, false));
	// rawMaterialWater.setIngList(ingList);
	// alfrescoRepository.create(contextTestFolderNodeRef,
	// rawMaterialWater).getNodeRef();
	// }
	// }

	private void initEntityTemplates() {

		NodeRef rawMaterialTplNodeRef = entityTplService.getEntityTpl(BeCPGModel.TYPE_RAWMATERIAL);
		ProductData rawMaterialData = (ProductData) alfrescoRepository.findOne(rawMaterialTplNodeRef);
		rawMaterialData.getCostList().add(new CostListDataItem(null, null, null, null, costs.get(0), null));
		rawMaterialData.getNutList().add(new NutListDataItem(null, null, null, null, null, null, nuts.get(0), null));
		rawMaterialData.getNutList().add(new NutListDataItem(null, null, null, null, null, null, nuts.get(0), null));
		alfrescoRepository.save(rawMaterialData);

		NodeRef packMaterialTplNodeRef = entityTplService.getEntityTpl(BeCPGModel.TYPE_PACKAGINGMATERIAL);
		ProductData packMaterialTplData = (ProductData) alfrescoRepository.findOne(packMaterialTplNodeRef);
		packMaterialTplData.getCostList().add(new CostListDataItem(null, null, null, null, costs.get(3), null));
		alfrescoRepository.save(packMaterialTplData);

	}

	/**
	 * Init the hierarchy lists
	 */
	private void initHierarchyLists() {

		logger.debug("initHierarchyLists");

		NodeRef productHierarchyNodeRef = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);

		Assert.assertNotNull("Product hierarchy system entity not found", productHierarchyNodeRef);

		NodeRef rawMaterialHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_RAWMATERIAL));
		Assert.assertNotNull("raw material hierarchy dataList not found", rawMaterialHierarchyNodeRef);

		NodeRef finishedProductHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_FINISHEDPRODUCT));
		Assert.assertNotNull("Finished product hierarchy dataList not found", finishedProductHierarchyNodeRef);

		/*-- create hierarchy --*/
		// RawMaterial - Sea food
		HIERARCHY1_SEA_FOOD_REF = hierarchyService.createRootHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD);
		HIERARCHY2_FISH_REF = hierarchyService.createHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_FISH);
		HIERARCHY2_CRUSTACEAN_REF = hierarchyService.createHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_CRUSTACEAN);

		// FinishedProduct - Frozen
		HIERARCHY1_FROZEN_REF = hierarchyService.createRootHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN);
		HIERARCHY2_PIZZA_REF = hierarchyService.createHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_PIZZA);
		HIERARCHY2_QUICHE_REF = hierarchyService.createHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_QUICHE);

		// Project
		NodeRef projectListsNodeRef = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_PROJECT_LISTS);
		NodeRef projectHierarchyNodeRef = entitySystemService.getSystemEntityDataList(projectListsNodeRef, HierarchyHelper.getHierarchyPathName(ProjectModel.TYPE_PROJECT));
		PROJECT_HIERARCHY1_SEA_FOOD_REF = hierarchyService.createRootHierarchy(projectHierarchyNodeRef, HIERARCHY1_SEA_FOOD);
		PROJECT_HIERARCHY2_FISH_REF = hierarchyService.createHierarchy(projectHierarchyNodeRef, PROJECT_HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_FISH);
		PROJECT_HIERARCHY2_CRUSTACEAN_REF = hierarchyService.createHierarchy(projectHierarchyNodeRef, PROJECT_HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_CRUSTACEAN);

	}

	private void initLabelingTemplate() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_CHARACTS);

		// labelingTemplate
		NodeRef labelingTemplateFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_LABELING_TEMPLATES);
		List<FileInfo> labelingTemplatesFileInfo = fileFolderService.listFiles(labelingTemplateFolder);
		if (labelingTemplatesFileInfo.size() == 0) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, "Marquage 1");
			properties.put(ContentModel.PROP_DESCRIPTION, "N° de lot : AAJJJ (AA : derniers chiffres de l’année ; JJJ : quantième du jour de fabrication)");
			labelingTemplateNodeRef = nodeService.createNode(labelingTemplateFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), PackModel.TYPE_LABELING_TEMPLATE, properties)
					.getChildRef();
		} else {
			labelingTemplateNodeRef = labelingTemplatesFileInfo.get(0).getNodeRef();
		}

	}

}
