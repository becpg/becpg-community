/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.comparison.CompareEntityService;
import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.entity.comparison.StructCompareOperator;
import fr.becpg.repo.entity.comparison.StructCompareResultDataItem;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class CompareProductServiceTest.
 * 
 * @author querephi
 */
public class CompareProductServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareProductServiceTest.class);

	@Resource
	private CompareEntityService compareEntityService;

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

	private NodeRef fp1NodeRef;

	private NodeRef fp2NodeRef;

	/** The costs. */
	private List<NodeRef> costs = new ArrayList<NodeRef>();

	/** The allergens. */
	private List<NodeRef> allergens = new ArrayList<NodeRef>();

	@Override
	public void setUp() throws Exception {
		super.setUp();

		initObjects();
	}

	/**
	 * Inits the objects.
	 */
	private void initObjects() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				// costs
				NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef costFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS));
				if (costFolder != null) {
					fileFolderService.delete(costFolder);
				}
				costFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_COSTS), ContentModel.TYPE_FOLDER).getNodeRef();
				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Cost " + i);
					properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
					ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
					costs.add(childAssocRef.getChildRef());
				}

				// allergens
				NodeRef allergensFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS));
				if (allergensFolder != null) {
					fileFolderService.delete(allergensFolder);
				}
				allergensFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(RepoConsts.PATH_ALLERGENS), ContentModel.TYPE_FOLDER).getNodeRef();

				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Allergen " + i);
					ChildAssociationRef childAssocRef = nodeService.createNode(allergensFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties);
					allergens.add(childAssocRef.getChildRef());
				}

				/*-- Create raw materials --*/
				logger.debug("/*-- Create raw materials --*/");
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");
				rawMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial1).getNodeRef();

				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");
				rawMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial2).getNodeRef();

				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");
				rawMaterial3NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial3).getNodeRef();

				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");
				rawMaterial4NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial4).getNodeRef();

				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");
				alfrescoRepository.create(testFolderNodeRef, rawMaterial5).getNodeRef();

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF1).getNodeRef();

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");
				localSF2NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF2).getNodeRef();

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
	private boolean checkCompareRow(List<CompareResultDataItem> compareResult, String productList, String characteristic, String property, String values) {

		for (CompareResultDataItem c : compareResult) {

			String tempProductList = c.getEntityList() == null ? "" : c.getEntityList().toString();
			String tempCharacteristic = c.getCharacteristic() == null ? "" : (String) nodeService.getProperty(c.getCharacteristic(), ContentModel.PROP_NAME);
			String tempProperty = c.getProperty() == null ? "" : c.getProperty().toString();

			if (productList.equals(tempProductList) && characteristic.equals(tempCharacteristic) && property.equals(tempProperty)
					&& c.getValues().toString().equals(values.toString())) {

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
	private boolean checkStructCompareRow(List<StructCompareResultDataItem> structCompareResult, String productList, int depthLevel, StructCompareOperator operator,
			String product1, String product2, String properties1, String properties2) {

		for (StructCompareResultDataItem c : structCompareResult) {

			String tempProductList = c.getEntityList() == null ? "" : c.getEntityList().toString();
			String tempProduct1 = "";
			if (c.getCharacteristic1() != null) {
				List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic1(), BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
				NodeRef productNodeRef = ((AssociationRef) compoAssocRefs.get(0)).getTargetRef();
				tempProduct1 = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
			}

			String tempProduct2 = "";
			if (c.getCharacteristic2() != null) {
				List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic2(), BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
				NodeRef productNodeRef = ((AssociationRef) compoAssocRefs.get(0)).getTargetRef();
				tempProduct2 = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
			}

			if (productList.equals(tempProductList) && depthLevel == c.getDepthLevel() && operator.equals(c.getOperator()) && product1.equals(tempProduct1)
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
	@Test
	public void testComparison() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");
				fp1.setUnit(ProductUnit.kg);

				// Costs €
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				for (int j = 0; j < 10; j++) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "", null, costs.get(j), false);
					costList.add(costListItemData);
				}
				fp1.setCostList(costList);

				// create an MP for the allergens
				RawMaterialData allergenRawMaterial = new RawMaterialData();
				allergenRawMaterial.setName("MP allergen");
				NodeRef allergenRawMaterialNodeRef = alfrescoRepository.create(testFolderNodeRef, allergenRawMaterial).getNodeRef();

				// Allergens
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				for (int j = 0; j < allergens.size(); j++) {
					List<NodeRef> volontarySources = new ArrayList<NodeRef>();
					volontarySources.add(allergenRawMaterialNodeRef);

					AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, true, false, volontarySources, null, allergens.get(j), false);
					allergenList.add(allergenListItemData);
				}
				fp1.setAllergenList(allergenList);

				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				
				CompoListDataItem parent1 = new CompoListDataItem(null,(CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef);
				
				compoList.add(parent1);
				compoList.add(new CompoListDataItem(null, parent1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, parent1, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				
				CompoListDataItem parent2 =new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef);
				compoList.add(parent2);
				compoList.add(new CompoListDataItem(null, parent2, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				// compoList.add(new CompoListDataItem(null, 2, 3d, 0d,
				// 0d, CompoListUnit.kg, "", DeclarationType.OMIT_FR,
				// rawMaterial4NodeRef));
				fp1.getCompoListView().setCompoList(compoList);

				fp1NodeRef = alfrescoRepository.create(testFolderNodeRef, fp1).getNodeRef();

				logger.debug("create FP 2");

				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");
				fp2.setUnit(ProductUnit.L);

				// Costs $
				costList = new ArrayList<CostListDataItem>();
				for (int j = 0; j < 10; j++) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.4d, "", null, costs.get(j), false);
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
						allergenListItemData = new AllergenListDataItem(null, true, false, allSources, null, allergens.get(j), false);
					} else {
						allergenListItemData = new AllergenListDataItem(null, false, true, null, allSources, allergens.get(j), false);
					}

					allergenList.add(allergenListItemData);
				}
				fp2.setAllergenList(allergenList);

				compoList = new ArrayList<CompoListDataItem>();
				CompoListDataItem parent11 = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef);
				compoList.add(parent11);
				compoList.add(new CompoListDataItem(null, parent11, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, parent11, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				CompoListDataItem parent22 = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef);
				compoList.add(parent22);
				compoList.add(new CompoListDataItem(null, parent22, 2d, 0d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, parent22, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
				fp2.getCompoListView().setCompoList(compoList);

				fp2NodeRef = alfrescoRepository.create(testFolderNodeRef, fp2).getNodeRef();

				return null;

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				List<NodeRef> productsNodeRef = new ArrayList<NodeRef>();
				productsNodeRef.add(fp2NodeRef);

				List<CompareResultDataItem> compareResult = compareEntityService.compare(fp1NodeRef, productsNodeRef);

				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 9",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources", "[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 5",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 6",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 1", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "", "", "{http://www.alfresco.org/model/content/1.0}name", "[FP 1, FP 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 1", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}compoListUnit",
						"[null, kg]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 3", "{http://www.bcpg.fr/model/becpg/1.0}compoListQty",
						"[3, 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 8",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 3", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 6",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources", "[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 2", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 9",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 7", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 7",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources", "[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 8",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources", "[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 4",
						"{http://www.bcpg.fr/model/becpg/1.0}compoListProduct", "[null, Raw material 4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 2", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 0", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				// code change everytime we test
				// assertTrue(checkCompareRow(compareResult, "", "",
				// "{http://www.bcpg.fr/model/becpg/1.0}productCode",
				// "[181, 182]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 7", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}compoListQty",
						"[null, 3]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 5",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources", "[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 3", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 6", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 5", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 4", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 6",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 7",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 4",
						"{http://www.bcpg.fr/model/becpg/1.0}compoListDeclType", "[null, Detail]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 4", "{http://www.bcpg.fr/model/becpg/1.0}depthLevel",
						"[null, 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 5",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Vrai, Faux]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 5",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources", "[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 8", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 9", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 8",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 8", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 8",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources", "[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 9", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 4", "{http://www.bcpg.fr/model/becpg/1.0}costListUnit",
						"[€/kg, €/L]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 9",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources", "[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 6", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 7",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 6",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources", "[null, MP allergen]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 9",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Faux, Vrai]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 1", "{http://www.bcpg.fr/model/becpg/1.0}compoListQty",
						"[1, 2]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 7",
						"{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources", "[MP allergen, null]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", "Raw material 3", "{http://www.bcpg.fr/model/becpg/1.0}compoListUnit",
						"[kg, P]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 5", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));
				assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Cost 0", "{http://www.bcpg.fr/model/becpg/1.0}costListValue",
						"[12,2, 12,4]"));

				return null;

			}
		}, false, true);
	}

	/**
	 * Test struct comparison.
	 */
	@Test
	public void testStructComparison() {

		fp1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");

				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				// compoList.add(new CompoListDataItem(null, 2, 3d, 0d,
				// 0d, CompoListUnit.kg, "", DeclarationType.OMIT_FR,
				// rawMaterial4NodeRef));
				fp1.getCompoListView().setCompoList(compoList);

				return alfrescoRepository.create(testFolderNodeRef, fp1).getNodeRef();
				
			}
		}, false, true);
		
		fp2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				logger.debug("createRawMaterial 1");

				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");

				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 2d, 0d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
				fp2.getCompoListView().setCompoList(compoList);

				return alfrescoRepository.create(testFolderNodeRef, fp2).getNodeRef();
				
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
		
				List<NodeRef> productsNodeRef = new ArrayList<NodeRef>();
				productsNodeRef.add(fp2NodeRef);

				List<StructCompareResultDataItem> structCompareResult = compareEntityService.compareStructDatalist(fp1NodeRef, fp2NodeRef, BeCPGModel.TYPE_COMPOLIST,
						BeCPGModel.ASSOC_COMPOLIST_PRODUCT);

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
				// logger.debug(c.getEntityList()+ " - " +
				// c.getDepthLevel() + " - " + c.getOperator() + " - " +
				// product1Name + " - " + product2Name + " - " +
				// c.getProperties1() + " - " + c.getProperties2());
				//
				// //Output for method checkCompareRow
				// //Uncomment debug line, copy/paste in spreadsheet =>
				// //you will get the test lines
				// String productList = c.getEntityList() == null ? ""
				// : c.getEntityList().toString();
				// logger.info("-assertTrue(checkStructCompareRow(structCompareResult, \""
				// + productList + "\", " + c.getDepthLevel() +
				// ", StructCompareOperator." + c.getOperator() + ", \""
				// + product1Name + "\", \"" + product2Name + "\", \"" +
				// c.getProperties1() + "\", \"" + c.getProperties2() +
				// "\"));");
				// }

				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 1, StructCompareOperator.Equal, "Local semi finished 1",
						"Local semi finished 1", "{}", "{}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 2, StructCompareOperator.Equal, "Raw material 2",
						"Raw material 2", "{}", "{}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 2, StructCompareOperator.Modified, "Raw material 1",
						"Raw material 1", "{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=1}", "{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=2}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 1, StructCompareOperator.Equal, "Local semi finished 2",
						"Local semi finished 2", "{}", "{}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 2, StructCompareOperator.Modified, "Raw material 3",
						"Raw material 3", "{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=2, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=P}"));
				assertTrue(checkStructCompareRow(
						structCompareResult,
						"{http://www.bcpg.fr/model/becpg/1.0}compoList",
						2,
						StructCompareOperator.Added,
						"",
						"Raw material 4",
						"{}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListQtyAfterProcess=0, {http://www.bcpg.fr/model/becpg/1.0}compoListProduct=Raw material 4, {http://www.alfresco.org/model/system/1.0}locale=fr_FR, {http://www.bcpg.fr/model/becpg/1.0}compoListQtySubFormula=0, {http://www.bcpg.fr/model/becpg/1.0}compoListDeclType=Detail, {http://www.bcpg.fr/model/becpg/1.0}compoListLossPerc=0, {http://www.bcpg.fr/model/becpg/1.0}depthLevel=2, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg}"));

				return null;

			}
		}, false, true);
	}
}
