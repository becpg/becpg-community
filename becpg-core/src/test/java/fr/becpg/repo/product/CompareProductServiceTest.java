/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.comparison.CompareEntityService;
import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.entity.comparison.StructCompareOperator;
import fr.becpg.repo.entity.comparison.StructCompareResultDataItem;
import fr.becpg.repo.helper.TranslateHelper;
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

// TODO: Auto-generated Javadoc
/**
 * The Class CompareProductServiceTest.
 * 
 * @author querephi
 */
public class CompareProductServiceTest extends BaseAlfrescoTestCase {

	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";

	/** The GROU p_ garniture. */
	private static String GROUP_GARNITURE = "Garniture";

	/** The GROU p_ pate. */
	private static String GROUP_PATE = "Pâte";

	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareProductServiceTest.class);

	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();

	/** The node service. */
	private NodeService nodeService;

	/** The file folder service. */
	private FileFolderService fileFolderService;

	/** The product dao. */
	private ProductDAO productDAO;

	/** The repository. */
	private Repository repository;

	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;

	/** The compare product service. */
	private CompareEntityService compareEntityService;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The folder node ref. */
	private NodeRef folderNodeRef;

	/** The local s f1 node ref. */
	private NodeRef localSF1NodeRef;

	/** The raw material1 node ref. */
	private NodeRef rawMaterial1NodeRef;

	/** The raw material2 node ref. */
	private NodeRef rawMaterial2NodeRef;

	/** The local s f2 node ref. */
	private NodeRef localSF2NodeRef;

	/** The raw material3 node ref. */
	private NodeRef rawMaterial3NodeRef;

	/** The raw material4 node ref. */
	private NodeRef rawMaterial4NodeRef;

	/** The raw material5 node ref. */
	private NodeRef rawMaterial5NodeRef;

	/** The costs. */
	private List<NodeRef> costs = new ArrayList<NodeRef>();

	/** The allergens. */
	private List<NodeRef> allergens = new ArrayList<NodeRef>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("ProductServiceTest:setUp");

		nodeService = (NodeService) appCtx.getBean("nodeService");
		fileFolderService = (FileFolderService) appCtx.getBean("fileFolderService");
		productDAO = (ProductDAO) appCtx.getBean("productDAO");
		repository = (Repository) appCtx.getBean("repositoryHelper");
		productDictionaryService = (ProductDictionaryService) appCtx.getBean("productDictionaryService");
		compareEntityService = (CompareEntityService) appCtx.getBean("compareEntityService");
		dictionaryService = (DictionaryService) appCtx.getBean("dictionaryService");

		initObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		try {
			// authenticationComponent.clearCurrentSecurityContext();
		} catch (Throwable e) {
			e.printStackTrace();
			// Don't let this mask any previous exceptions
		}
		super.tearDown();

	}

	/**
	 * Inits the objects.
	 */
	private void initObjects() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create test folder --*/
				folderNodeRef = nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
						PATH_TESTFOLDER);
				if (folderNodeRef != null) {
					fileFolderService.delete(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repository.getCompanyHome(), PATH_TESTFOLDER,
						ContentModel.TYPE_FOLDER).getNodeRef();

				// costs
				NodeRef systemFolder = nodeService.getChildByName(repository.getCompanyHome(),
						ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef costFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
				if (costFolder != null) {
					fileFolderService.delete(costFolder);
				}
				costFolder = fileFolderService.create(systemFolder,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS), ContentModel.TYPE_FOLDER)
						.getNodeRef();
				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Cost " + i);
					properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
					ChildAssociationRef childAssocRef = nodeService.createNode(
							costFolder,
							ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									(String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
					costs.add(childAssocRef.getChildRef());
				}

				// allergens
				NodeRef allergensFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS));
				if (allergensFolder != null) {
					fileFolderService.delete(allergensFolder);
				}
				allergensFolder = fileFolderService.create(systemFolder,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS), ContentModel.TYPE_FOLDER)
						.getNodeRef();

				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Allergen " + i);
					ChildAssociationRef childAssocRef = nodeService.createNode(
							allergensFolder,
							ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									(String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN,
							properties);
					allergens.add(childAssocRef.getChildRef());
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

				return null;

			}
		}, false, true);

	}

	/**
	 * Check the row is present in the comparison result.
	 * 
	 * @param compareResult
	 *            the compare result
	 * @param productList
	 *            the product list
	 * @param characteristic
	 *            the characteristic
	 * @param property
	 *            the property
	 * @param values
	 *            the values
	 * @return true, if successful
	 */
	private boolean checkCompareRow(List<CompareResultDataItem> compareResult, String productList,
			String characteristic, String property, String values) {

		for (CompareResultDataItem c : compareResult) {

			String tempProductList = c.getEntityList() == null ? "" : c.getEntityList().toString();
			String tempCharacteristic = c.getCharacteristic() == null ? "" : (String) nodeService.getProperty(
					c.getCharacteristic(), ContentModel.PROP_NAME);
			String tempProperty = c.getProperty() == null ? "" : c.getProperty().toString();

			if (productList.equals(tempProductList) && characteristic.equals(tempCharacteristic)
					&& property.equals(tempProperty) && c.getValues().toString().equals(values.toString())) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Check the row is present in the structural comparison result.
	 * 
	 * @param structCompareResult
	 *            the struct compare result
	 * @param productList
	 *            the product list
	 * @param depthLevel
	 *            the depth level
	 * @param operator
	 *            the operator
	 * @param product1
	 *            the product1
	 * @param product2
	 *            the product2
	 * @param properties1
	 *            the properties1
	 * @param properties2
	 *            the properties2
	 * @return true, if successful
	 */
	private boolean checkStructCompareRow(List<StructCompareResultDataItem> structCompareResult, String productList,
			int depthLevel, StructCompareOperator operator, String product1, String product2, String properties1,
			String properties2) {

		for (StructCompareResultDataItem c : structCompareResult) {

			String tempProductList = c.getEntityList() == null ? "" : c.getEntityList().toString();
			String tempProduct1 = "";
			if (c.getCharacteristic1() != null) {
				List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic1(),
						BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
				NodeRef productNodeRef = ((AssociationRef) compoAssocRefs.get(0)).getTargetRef();
				tempProduct1 = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
			}

			String tempProduct2 = "";
			if (c.getCharacteristic2() != null) {
				List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic2(),
						BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
				NodeRef productNodeRef = ((AssociationRef) compoAssocRefs.get(0)).getTargetRef();
				tempProduct2 = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
			}

			if (productList.equals(tempProductList) && depthLevel == c.getDepthLevel()
					&& operator.equals(c.getOperator()) && product1.equals(tempProduct1)
					&& product2.equals(tempProduct2) && properties1.toString().equals(c.getProperties1().toString())
					&& properties2.toString().equals(c.getProperties2().toString())) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Test comparison.
	 */
	public void testComparison() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				Collection<QName> dataLists = productDictionaryService.getDataLists();

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");
				fp1.setUnit(ProductUnit.kg);

				// Costs €
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				for (int j = 0; j < 10; j++) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.2f, "", costs.get(j));
					costList.add(costListItemData);
				}
				fp1.setCostList(costList);

				// create an MP for the allergens
				RawMaterialData allergenRawMaterial = new RawMaterialData();
				allergenRawMaterial.setName("MP allergen");
				NodeRef allergenRawMaterialNodeRef = productDAO.create(folderNodeRef, allergenRawMaterial, dataLists);

				// Allergens
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				for (int j = 0; j < allergens.size(); j++) {
					List<NodeRef> volontarySources = new ArrayList<NodeRef>();
					volontarySources.add(allergenRawMaterialNodeRef);

					AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, true, false,
							volontarySources, null, allergens.get(j));
					allergenList.add(allergenListItemData);
				}
				fp1.setAllergenList(allergenList);

				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE,
						DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE,
						DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				// compoList.add(new CompoListDataItem(null, 2, 3f, 0f,
				// 0f, CompoListUnit.kg, "", DeclarationType.OMIT_FR,
				// rawMaterial4NodeRef));
				fp1.setCompoList(compoList);

				NodeRef fp1NodeRef = productDAO.create(folderNodeRef, fp1, dataLists);

				logger.debug("create FP 2");

				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");
				fp2.setUnit(ProductUnit.L);

				// Costs $
				costList = new ArrayList<CostListDataItem>();
				for (int j = 0; j < 10; j++) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.4f, "", costs.get(j));
					costList.add(costListItemData);
				}
				fp2.setCostList(costList);

				// Allergens
				allergenList = new ArrayList<AllergenListDataItem>();
				for (int j = 0; j < allergens.size(); j++) {
					List<NodeRef> allSources = new ArrayList<NodeRef>();
					allSources.add(allergenRawMaterialNodeRef);
					AllergenListDataItem allergenListItemData = null;

					if (j < 5) {
						allergenListItemData = new AllergenListDataItem(null, true, false, allSources, null,
								allergens.get(j));
					} else {
						allergenListItemData = new AllergenListDataItem(null, false, true, null, allSources,
								allergens.get(j));
					}

					allergenList.add(allergenListItemData);
				}
				fp2.setAllergenList(allergenList);

				compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE,
						DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE,
						DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.P, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DETAIL_FR, rawMaterial4NodeRef));
				fp2.setCompoList(compoList);

				NodeRef fp2NodeRef = productDAO.create(folderNodeRef, fp2, dataLists);
				List<NodeRef> productsNodeRef = new ArrayList<NodeRef>();
				productsNodeRef.add(fp2NodeRef);

				List<CompareResultDataItem> compareResult = compareEntityService.compare(fp1NodeRef, productsNodeRef);

				// for(CompareResultDataItem c : compareResult){
				//
				// String productListTitle = "";
				// if(c.getProductList() != null){
				// TypeDefinition typeDef =
				// dictionaryService.getType(c.getProductList());
				// productListTitle = typeDef.getTitle();
				// }
				//
				// String charactName = c.getCharacteristic() == null ?
				// "" :
				// (String)nodeService.getProperty(c.getCharacteristic(),
				// ContentModel.PROP_NAME);
				// String propertyTitle = "";
				// PropertyDefinition propertyDef =
				// dictionaryService.getProperty(c.getProperty());
				// if(propertyDef != null){
				// propertyTitle = propertyDef.getTitle();
				// }
				// else{
				// AssociationDefinition assocDef =
				// dictionaryService.getAssociation(c.getProperty());
				// if(assocDef != null)
				// propertyTitle = assocDef.getTitle();
				// }
				//
				// logger.debug(" - " + productListTitle + " - " +
				// charactName + " - " + propertyTitle + " - " +
				// c.getValues().toString());
				//
				// //Output for method checkCompareRow
				// //Uncomment debug line, copy/paste in spreadsheet =>
				// you will get the test lines
				// //String productList = c.getProductList() == null ?
				// "" : c.getProductList().toString();
				// //logger.debug("-assertTrue(checkCompareRow(compareResult, \""
				// + productList + "\", \"" + charactName + "\", \"" +
				// c.getProperty() + "\", \"" + c.getValues().toString()
				// + "\"));");
				// }

				// assertEquals("size result", 52,
				// compareResult.size());

				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 9", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources",
						"[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 5", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 6", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 1",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "", "", "{http://www.alfresco.org/model/content/1.0}name",
						"[FP 1, FP 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 1",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}compoListUnit", "[null, kg]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 3", "{http://www.bcpg.fr/model/becpg/1.0}compoListQty", "[3, 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 8", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 3",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 6", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources",
						"[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 2",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 9", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 7",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 7", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources",
						"[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 8", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources",
						"[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}compoListProduct",
						"[null, Raw material 4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 2",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 0",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				// code change everytime we test
				// assertTrue(checkCompareRow(compareResult, "", "",
				// "{http://www.bcpg.fr/model/becpg/1.0}productCode",
				// "[181, 182]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 7",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}compoListQty", "[null, 3]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 5", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources",
						"[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 3",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 6",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 5",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 4",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 6", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 7", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}compoListDeclType", "[null, Détailler]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}depthLevel", "[null, 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 5", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 5", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources",
						"[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 8",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 9",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 8", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 8",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 8", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources",
						"[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 9",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 4",
						"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 9", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources",
						"[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 6",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 7", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 6", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources",
						"[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 9", "{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}compoListDeclGrp", "[null, ]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 1", "{http://www.bcpg.fr/model/becpg/1.0}compoListQty", "[1, 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList",
						"Allergen 7", "{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources",
						"[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						"Raw material 3", "{http://www.bcpg.fr/model/becpg/1.0}compoListUnit", "[kg, P]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 5",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 0",
						"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));

				return null;

			}
		}, false, true);
	}

	/**
	 * Test struct comparison.
	 */
	public void testStructComparison() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				Collection<QName> dataLists = productDictionaryService.getDataLists();

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");

				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE,
						DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE,
						DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				// compoList.add(new CompoListDataItem(null, 2, 3f, 0f,
				// 0f, CompoListUnit.kg, "", DeclarationType.OMIT_FR,
				// rawMaterial4NodeRef));
				fp1.setCompoList(compoList);

				NodeRef fp1NodeRef = productDAO.create(folderNodeRef, fp1, dataLists);

				logger.debug("createRawMaterial 1");

				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");

				compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_PATE,
						DeclarationType.DETAIL_FR, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DETAIL_FR, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1f, 0f, 0f, CompoListUnit.kg, 0f, GROUP_GARNITURE,
						DeclarationType.DETAIL_FR, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2f, 0f, 0f, CompoListUnit.P, 0f, "",
						DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3f, 0f, 0f, CompoListUnit.kg, 0f, "",
						DeclarationType.DETAIL_FR, rawMaterial4NodeRef));
				fp2.setCompoList(compoList);

				NodeRef fp2NodeRef = productDAO.create(folderNodeRef, fp2, dataLists);
				List<NodeRef> productsNodeRef = new ArrayList<NodeRef>();
				productsNodeRef.add(fp2NodeRef);

				List<StructCompareResultDataItem> structCompareResult = compareEntityService.compareStructDatalist(
						fp1NodeRef, fp2NodeRef, BeCPGModel.TYPE_COMPOLIST, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);

				// for(StructCompareResultDataItem c :
				// structCompareResult){
				//
				// String product1Name = "";
				// if(c.getCharacteristic1() != null){
				// List<AssociationRef> compoAssocRefs =
				// nodeService.getTargetAssocs(c.getCharacteristic1(),
				// BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
				// NodeRef productNodeRef = ((AssociationRef)
				// compoAssocRefs.get(0)).getTargetRef();
				// product1Name =
				// (String)nodeService.getProperty(productNodeRef,
				// ContentModel.PROP_NAME);
				// }
				//
				// String product2Name = "";
				// if(c.getCharacteristic2() != null){
				// List<AssociationRef> compoAssocRefs =
				// nodeService.getTargetAssocs(c.getCharacteristic2(),
				// BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
				// NodeRef productNodeRef = ((AssociationRef)
				// compoAssocRefs.get(0)).getTargetRef();
				// product2Name =
				// (String)nodeService.getProperty(productNodeRef,
				// ContentModel.PROP_NAME);
				// }
				//
				// logger.debug(c.getProductList() + " - " +
				// c.getDepthLevel() + " - " + c.getOperator() + " - " +
				// product1Name + " - " + product2Name + " - " +
				// c.getProperties1() + " - " + c.getProperties2());
				//
				// //Output for method checkCompareRow
				// //Uncomment debug line, copy/paste in spreadsheet =>
				// you will get the test lines
				// String productList = c.getProductList() == null ? ""
				// : c.getProductList().toString();
				// logger.debug("-assertTrue(checkStructCompareRow(structCompareResult, \""
				// + productList + "\", " + c.getDepthLevel() +
				// ", StructCompareOperator." + c.getOperator() + ", \""
				// + product1Name + "\", \"" + product2Name + "\", \"" +
				// c.getProperties1() + "\", \"" + c.getProperties2() +
				// "\"));");
				// }

				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						1, StructCompareOperator.Equal, "Local semi finished 1", "Local semi finished 1", "{}", "{}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						2, StructCompareOperator.Equal, "Raw material 2", "Raw material 2", "{}", "{}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						2, StructCompareOperator.Modified, "Raw material 1", "Raw material 1",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=1}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=2}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList",
						1, StructCompareOperator.Equal, "Local semi finished 2", "Local semi finished 2", "{}", "{}"));
				assertTrue(checkStructCompareRow(
						structCompareResult,
						"{http://www.bcpg.fr/model/becpg/1.0}compoList",
						2,
						StructCompareOperator.Modified,
						"Raw material 3",
						"Raw material 3",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=2, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=P}"));
				assertTrue(checkStructCompareRow(
						structCompareResult,
						"{http://www.bcpg.fr/model/becpg/1.0}compoList",
						2,
						StructCompareOperator.Added,
						"",
						"Raw material 4",
						"{}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListDeclGrp=, {http://www.bcpg.fr/model/becpg/1.0}compoListQtyAfterProcess=0, {http://www.bcpg.fr/model/becpg/1.0}compoListProduct=Raw material 4, {http://www.bcpg.fr/model/becpg/1.0}compoListQtySubFormula=0, {http://www.bcpg.fr/model/becpg/1.0}sort=4, {http://www.bcpg.fr/model/becpg/1.0}compoListDeclType=Détailler, {http://www.bcpg.fr/model/becpg/1.0}compoListLossPerc=0, {http://www.bcpg.fr/model/becpg/1.0}depthLevel=2, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg}"));

				return null;

			}
		}, false, true);
	}
}
