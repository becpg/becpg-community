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

import junit.framework.Assert;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.wiser.Wiser;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.charact.AllergenType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.hierarchy.HierarchyService;

// TODO: Auto-generated Javadoc
/**
 * base class of test cases for product classes.
 * 
 * @author querephi
 */

public abstract class RepoBaseTestCase extends BaseAlfrescoTestCase {

	/** The Constant HIERARCHY1_SEA_FOOD. */
	protected static final String HIERARCHY1_SEA_FOOD = "Sea food";

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

	protected NodeRef HIERARCHY1_SEA_FOOD_REF;

	protected NodeRef HIERARCHY2_FISH_REF;

	protected NodeRef HIERARCHY2_CRUSTACEAN_REF;

	protected NodeRef HIERARCHY1_FROZEN_REF;

	protected NodeRef HIERARCHY2_PIZZA_REF;

	protected NodeRef HIERARCHY2_QUICHE_REF;

	protected NodeRef testFolderNodeRef; 
	
	private static String VALUE_COST_CURRENCY = "€";

	/** The logger. */
	private static Log logger = LogFactory.getLog(RepoBaseTestCase.class);

	protected MimetypeService mimetypeService;

	protected Repository repositoryHelper;

	protected NodeService nodeService;

	protected RepoService repoService;

	protected FileFolderService fileFolderService;

	protected ProductDAO productDAO;

	protected DictionaryDAO dictionaryDAO;

	protected ProductDictionaryService productDictionaryService;

	protected EntitySystemService entitySystemService;

	private InitVisitor initRepoVisitor;

	private HierarchyService hierarchyService;

	protected RepoBaseTestCase repoBaseTestCase;

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

	protected Wiser wiser = new Wiser(2500);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// First start wiser
		try {
			wiser.start();
		} catch (Exception e) {
			logger.warn("cannot open wiser!");
		}
		// Then context
		super.setUp();
		
		repoBaseTestCase = this;

		mimetypeService = (MimetypeService) ctx.getBean("mimetypeService");
		repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
		repoService = (RepoService) ctx.getBean("repoService");
		fileFolderService = (FileFolderService) ctx.getBean("fileFolderService");
		productDAO = (ProductDAO) ctx.getBean("productDAO");
		productDictionaryService = (ProductDictionaryService) ctx.getBean("productDictionaryService");
		initRepoVisitor = (InitVisitor) ctx.getBean("initRepoVisitor");
		entitySystemService = (EntitySystemService) ctx.getBean("entitySystemService");
		dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
		hierarchyService = (HierarchyService) ctx.getBean("hierarchyService");
		nodeService = (NodeService) ctx.getBean("nodeService");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				testFolderNodeRef = BeCPGTestHelper.createTestFolder(repoBaseTestCase);;
				
				// Delete initialyzed repo
				deleteSystemFolder();
				// Init repo for tes
				initRepoVisitor.visitContainer(repositoryHelper.getCompanyHome());

				Assert.assertEquals(3, entitySystemService.getSystemEntities().size());

				initConstraints();

				return null;

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				dictionaryDAO.reset();

				initCharacteristics();
				initHierarchyLists();
				// reset dictionary to reload constraints on list_values
				dictionaryDAO.reset();
				return null;

			}
		}, false, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		try {
			wiser.stop();
		} catch (Exception e) {
			logger.warn("cannot stop wiser!");
		}

		super.tearDown();

	}

	/**
	 * Delete the product report tpls.
	 */
	protected void deleteReportTpls() {

		NodeRef systemFolder = nodeService
				.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

		if (systemFolder != null) {

			NodeRef reportsNodeRef = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));

			if (reportsNodeRef != null) {

				NodeRef productReportTplsNodeRef = nodeService.getChildByName(reportsNodeRef, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

				if (productReportTplsNodeRef != null) {
					nodeService.deleteNode(productReportTplsNodeRef);
				}
			}
		}

	}

	private void deleteSystemFolder() {
		NodeRef systemFolder = nodeService
				.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

		if (systemFolder != null) {
			nodeService.deleteNode(systemFolder);
		}

	}

	private void initConstraints() {

		NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolder, RepoConsts.PATH_LISTS);

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
		// packagingLevels
		NodeRef packagingLevelsFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_PACKAGING_LEVELS);
		String[] packagingLevels = { "Primaire", "Secondaire", "Tertiaire" };
		for (String packagingLevel : packagingLevels) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, packagingLevel);
			nodeService.createNode(packagingLevelsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		// allergenTypes
		NodeRef allergenTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_ALLERGEN_TYPES);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, AllergenType.Major.toString());
		nodeService.createNode(allergenTypesFolder, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);

	}

	/**
	 * Initialize the characteristics of the repository.
	 */
	private void initCharacteristics() {

		NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

		NodeRef charactsFolder = entitySystemService.getSystemEntity(systemFolder, RepoConsts.PATH_CHARACTS);

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
	}

	/**
	 * Create a raw material.
	 * 
	 * @param parentNodeRef
	 *            the parent node ref
	 * @param name
	 *            the name
	 * @return the node ref
	 */
	protected NodeRef createRawMaterial(NodeRef parentNodeRef, String name) {

		logger.debug("createRawMaterial");

		logger.debug("Create MP");
		RawMaterialData rawMaterial = new RawMaterialData();
		rawMaterial.setName(name);
		rawMaterial.setHierarchy1(HIERARCHY1_FROZEN_REF);
		rawMaterial.setHierarchy2(HIERARCHY2_FISH_REF);

		// Allergens
		List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
		for (int j = 0; j < allergens.size(); j++) {
			AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, false, false, null, null, allergens.get(j), false);
			allergenList.add(allergenListItemData);
		}
		rawMaterial.setAllergenList(allergenList);

		// Costs
		List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
		for (int j = 0; j < costs.size(); j++) {
			CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, costs.get(j), false);
			costList.add(costListItemData);
		}
		rawMaterial.setCostList(costList);

		// Ings
		List<IngListDataItem> ingList = new ArrayList<IngListDataItem>();
		for (int j = 0; j < ings.size(); j++) {
			IngListDataItem ingListItemData = new IngListDataItem(null, 12.2d, null, null, false, false, ings.get(j), false);
			ingList.add(ingListItemData);
		}
		rawMaterial.setIngList(ingList);

		// Nuts
		List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
		for (int j = 0; j < nuts.size(); j++) {
			NutListDataItem nutListItemData = new NutListDataItem(null, 2d, "kJ/100g", 0d, 0d, "Groupe 1", nuts.get(j), false);
			nutList.add(nutListItemData);
		}
		rawMaterial.setNutList(nutList);

		// Organos
		List<OrganoListDataItem> organoList = new ArrayList<OrganoListDataItem>();
		for (int j = 0; j < organos.size(); j++) {
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
	private void initHierarchyLists() {

		logger.debug("initHierarchyLists");

		// check init repo
		NodeRef systemNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

		assertNotNull("System folder not found", systemNodeRef);
		NodeRef productHierarchyNodeRef = entitySystemService.getSystemEntity(systemNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);

		assertNotNull("Product hierarchy system entity not found", productHierarchyNodeRef);

		NodeRef rawMaterialHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_RAWMATERIAL));
		assertNotNull("raw material hierarchy dataList not found", rawMaterialHierarchyNodeRef);

		NodeRef finishedProductHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_FINISHEDPRODUCT));
		assertNotNull("Finished product hierarchy dataList not found", finishedProductHierarchyNodeRef);

		/*-- create hierarchy --*/
		// RawMaterial - Sea food
		HIERARCHY1_SEA_FOOD_REF = hierarchyService.createHierarchy1(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD);
		HIERARCHY2_FISH_REF = hierarchyService.createHierarchy2(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_FISH);
		HIERARCHY2_CRUSTACEAN_REF = hierarchyService.createHierarchy2(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_CRUSTACEAN);

		// FinishedProduct - Frozen
		HIERARCHY1_FROZEN_REF = hierarchyService.createHierarchy1(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN);
		HIERARCHY2_PIZZA_REF = hierarchyService.createHierarchy2(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_PIZZA);
		HIERARCHY2_QUICHE_REF = hierarchyService.createHierarchy2(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_QUICHE);
	}
}
