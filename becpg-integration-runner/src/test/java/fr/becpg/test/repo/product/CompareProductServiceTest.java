/*
 *
 */
package fr.becpg.test.repo.product;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.entity.comparison.StructCompareOperator;
import fr.becpg.repo.entity.comparison.StructCompareResultDataItem;
import fr.becpg.repo.helper.ProductAttributeExtractorPlugin;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;

/**
 * The Class CompareProductServiceTest.
 *
 * @author querephi
 */
public class CompareProductServiceTest extends AbstractCompareProductTest {

	private static final Log logger = LogFactory.getLog(CompareProductServiceTest.class);

	@Resource
	private ProductAttributeExtractorPlugin nameExtractor;

	/**
	 * Test comparison.
	 */
	@Test
	public void testComparison() {

		final RawMaterialData allergenRawMateria = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.debug("createRawMaterial 1");

			FinishedProductData fp1 = new FinishedProductData();
			fp1.setName("FP 1");
			fp1.setUnit(ProductUnit.kg);

			// Costs €
			List<CostListDataItem> costList = new ArrayList<>();
			for (NodeRef cost : costs) {
				CostListDataItem costListItemData1 = new CostListDataItem(null, 12.2d, "", null, cost, false);
				costList.add(costListItemData1);
			}
			fp1.setCostList(costList);

			// create an MP for the allergens
			RawMaterialData allergenRawMaterial = new RawMaterialData();
			allergenRawMaterial.setName("MP allergen");
			NodeRef allergenRawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), allergenRawMaterial).getNodeRef();

			// Allergens
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			for (NodeRef allergen : allergens) {
				List<NodeRef> volontarySources = new ArrayList<>();
				volontarySources.add(allergenRawMaterialNodeRef);

				AllergenListDataItem allergenListItemData1 = new AllergenListDataItem(null, null, true, false, volontarySources, null, allergen,
						false);
				allergenList.add(allergenListItemData1);
			}
			fp1.setAllergenList(allergenList);

			List<CompoListDataItem> compoList = new ArrayList<>();

			CompoListDataItem parent1 = new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef);

			compoList.add(parent1);
			compoList.add(new CompoListDataItem(null, parent1, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, parent1, 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));

			CompoListDataItem parent2 = new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef);
			compoList.add(parent2);
			compoList.add(new CompoListDataItem(null, parent2, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			// compoList.add(new CompoListDataItem(null, 2, 3d, 0d,
			// 0d, CompoListUnit.kg, "", DeclarationType.OMIT_FR,
			// rawMaterial4NodeRef));
			fp1.getCompoListView().setCompoList(compoList);

			fp1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

			logger.debug("create FP 2");

			FinishedProductData fp2 = new FinishedProductData();
			fp2.setName("FP 2");
			fp2.setUnit(ProductUnit.L);

			// Costs $
			costList = new ArrayList<>();
			for (int j1 = 0; j1 < costs.size(); j1++) {
				CostListDataItem costListItemData2 = new CostListDataItem(null, 12.4d, "", null, costs.get(j1), false);
				costList.add(costListItemData2);
			}
			fp2.setCostList(costList);

			// Allergens
			allergenList = new ArrayList<>();
			for (int j2 = 0; j2 < allergens.size(); j2++) {
				List<NodeRef> allSources = new ArrayList<>();
				allSources.add(allergenRawMaterialNodeRef);
				AllergenListDataItem allergenListItemData2;

				if (j2 < 5) {
					allergenListItemData2 = new AllergenListDataItem(null, null, true, false, allSources, null, allergens.get(j2), false);
				} else {
					allergenListItemData2 = new AllergenListDataItem(null, null, false, true, null, allSources, allergens.get(j2), false);
				}

				allergenList.add(allergenListItemData2);
			}
			fp2.setAllergenList(allergenList);

			compoList = new ArrayList<>();
			CompoListDataItem parent11 = new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef);
			compoList.add(parent11);
			compoList.add(new CompoListDataItem(null, parent11, 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, parent11, 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			CompoListDataItem parent22 = new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef);
			compoList.add(parent22);
			compoList.add(new CompoListDataItem(null, parent22, 2d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, parent22, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
			fp2.getCompoListView().setCompoList(compoList);

			fp2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp2).getNodeRef();

			return allergenRawMaterial;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> productsNodeRef = new ArrayList<>();
			productsNodeRef.add(fp2NodeRef);

			List<CompareResultDataItem> compareResult = new ArrayList<>();
			Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<>();
			compareEntityService.compare(fp1NodeRef, productsNodeRef, compareResult, structCompareResults);

			// for (CompareResultDataItem c : compareResult) {
			// logger.info("CompareResultDataItem : " + c.toString());
			// }

			String allergenMPName = nameExtractor.extractPropName(PLMModel.TYPE_RAWMATERIAL, allergenRawMateria.getNodeRef());

			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 9",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources", "[null, " + allergenMPName + "]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 5",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Non, Oui]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 6",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVoluntary", "[Non, Oui]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Coût MP",
					"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
			assertTrue(checkCompareRow(compareResult, "", "", "{http://www.alfresco.org/model/content/1.0}name", "[FP 1, FP 2]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Coût MP",
					"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 8",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Oui, Non]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Coût prév MP",
					"{http://www.bcpg.fr/model/becpg/1.0}costListUnit", "[€/kg, €/L]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 6",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources", "[" + allergenMPName + ", null]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Coût prév MP",
					"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 9",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListVoluntary", "[Oui, Non]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}costList", "Coût Emb",
					"{http://www.bcpg.fr/model/becpg/1.0}costListValue", "[12,2, 12,4]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 7",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListInVolSources", "[null, " + allergenMPName + "]"));
			assertTrue(checkCompareRow(compareResult, "{http://www.bcpg.fr/model/becpg/1.0}allergenList", "Allergen 8",
					"{http://www.bcpg.fr/model/becpg/1.0}allergenListVolSources", "[" + allergenMPName + ", null]"));

			return null;

		}, false, true);
	}

	/**
	 * Test struct comparison.
	 */
	@Test
	public void testStructComparison() {

		Date startTime = new Date();
		String previousNameFormat = nameExtractor.getProductNameFormat();
		try {

			nameExtractor.setProductNameFormat("{cm:name}");

			fp1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");

				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(
						new CompoListDataItem(null, compoList.get(0), 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(
						new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(
						new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				// compoList.add(new CompoListDataItem(null, 2, 3d, 0d,
				// 0d, CompoListUnit.kg, "", DeclarationType.OMIT_FR,
				// rawMaterial4NodeRef));
				fp1.getCompoListView().setCompoList(compoList);

				return alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

			}, false, true);

			fp2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				logger.debug("createRawMaterial 1");

				SemiFinishedProductData sf2 = new SemiFinishedProductData();
				sf2.setName("SF 2");

				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, null, 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				sf2.getCompoListView().setCompoList(compoList);

				NodeRef sf2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sf2).getNodeRef();

				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");

				compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(
						new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(
						new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(
						new CompoListDataItem(null, compoList.get(3), 2d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(
						new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
				compoList.add(new CompoListDataItem(null, null, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, sf2NodeRef));
				fp2.getCompoListView().setCompoList(compoList);

				return alfrescoRepository.create(getTestFolderNodeRef(), fp2).getNodeRef();

			}, false, true);

			waitForSolr(startTime);

			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<>();
				compareEntityService.compareStructDatalist(fp1NodeRef, fp2NodeRef, PLMModel.TYPE_COMPOLIST, structCompareResults);
				List<StructCompareResultDataItem> structCompareResult = structCompareResults.get("FP 1 - FP 2 - Composition");

				for (StructCompareResultDataItem c : structCompareResult) {

					String product1Name = "";
					if (c.getCharacteristic1() != null) {
						List<AssociationRef> compoAssocRefs1 = nodeService.getTargetAssocs(c.getCharacteristic1(), PLMModel.ASSOC_COMPOLIST_PRODUCT);
						NodeRef productNodeRef1 = compoAssocRefs1.get(0).getTargetRef();
						product1Name = (String) nodeService.getProperty(productNodeRef1, ContentModel.PROP_NAME);
					}

					String product2Name = "";
					if (c.getCharacteristic2() != null) {
						List<AssociationRef> compoAssocRefs2 = nodeService.getTargetAssocs(c.getCharacteristic2(), PLMModel.ASSOC_COMPOLIST_PRODUCT);
						NodeRef productNodeRef2 = compoAssocRefs2.get(0).getTargetRef();
						product2Name = (String) nodeService.getProperty(productNodeRef2, ContentModel.PROP_NAME);
					}

					logger.debug(c.getEntityList() + " - " + c.getDepthLevel() + " - " + c.getOperator() + " - " + product1Name + " - " + product2Name
							+ " - " + c.getProperties1() + " - " + c.getProperties2());

					// Output for method checkCompareRow
					// Uncomment debug line, copy/paste in spreadsheet =>
					// you will get the test lines
					String productList = c.getEntityList() == null ? "" : c.getEntityList().toString();
					logger.info("-assertTrue(checkStructCompareRow(structCompareResult, \"" + productList + "\", " + c.getDepthLevel()
							+ ", StructCompareOperator." + c.getOperator() + ", \"" + product1Name + "\", \"" + product2Name + "\", \""
							+ c.getProperties1() + "\", \"" + c.getProperties2() + "\"));");
				}

				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 2,
						StructCompareOperator.Modified, "Raw material 1", "Raw material 1", "{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=1}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=2}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 2,
						StructCompareOperator.Modified, "Raw material 3", "Raw material 3",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=2, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=P}"));

				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 2, StructCompareOperator.Added,
						"", "Raw material 4", "{}",
						"{{http://www.alfresco.org/model/system/1.0}locale=fr_FR, {http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg, {http://www.bcpg.fr/model/becpg/1.0}compoListQtySubFormula=0, {http://www.bcpg.fr/model/becpg/1.0}compoListDeclType=Detail, {http://www.bcpg.fr/model/becpg/1.0}compoListLossPerc=0, {http://www.bcpg.fr/model/becpg/1.0}compoListProduct=Raw material 4, {http://www.bcpg.fr/model/becpg/1.0}depthLevel=2}"));

				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 1, StructCompareOperator.Added,
						"", "SF 2", "{}",
						"{{http://www.alfresco.org/model/system/1.0}locale=fr_FR, {http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg, {http://www.bcpg.fr/model/becpg/1.0}compoListQtySubFormula=0, {http://www.bcpg.fr/model/becpg/1.0}compoListDeclType=Detail, {http://www.bcpg.fr/model/becpg/1.0}compoListLossPerc=0, {http://www.bcpg.fr/model/becpg/1.0}compoListProduct=SF 2, {http://www.bcpg.fr/model/becpg/1.0}depthLevel=1}"));

				return null;

			}, false, true);

		} finally {
			nameExtractor.setProductNameFormat(previousNameFormat);
		}
	}

}
