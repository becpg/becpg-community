/*
 *
 */
package fr.becpg.test.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.entity.comparison.StructCompareOperator;
import fr.becpg.repo.entity.comparison.StructCompareResultDataItem;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.ProductAttributeExtractorPlugin;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * The Class CompareProductServiceTest.
 *
 * @author querephi
 */
public class CompareProductServiceIT extends AbstractCompareProductTest {

	private static final Log logger = LogFactory.getLog(CompareProductServiceIT.class);

	@Autowired
	private ProductAttributeExtractorPlugin nameExtractor;
	
	@Autowired
	private EntityVersionService entityVersionService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	private final QName ASSOC_PRODUCT_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "productGeoOrigin");
	
	/**
	 * Test Raw Material comparison.
	 */
	@Test
	public void testRawMaterialComparison() {
		inWriteTx(() -> {
			List<NodeRef> geoOrigins = new ArrayList<>();
			List<NodeRef> nodeRefs = new ArrayList<>();
			List<CompareResultDataItem> compareResult = new ArrayList<>();
			Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<>();
			
			/*-- Retrieve Raw Material 1 --*/
			RawMaterialData rawMaterial1 = (RawMaterialData) alfrescoRepository.findOne(rawMaterial1NodeRef);
			
			/*-- Characteristics --*/
			Map<QName, Serializable> properties = new HashMap<>();
			
			/*-- Geo origins --*/
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "geoOrigin1");
			NodeRef geoOrigin1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			nodeService.setProperty(geoOrigin1, PLMModel.PROP_GEO_ORIGIN_ISOCODE, "FR");
			geoOrigins.add(geoOrigin1);
			
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "geoOrigin2");
			NodeRef geoOrigin2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			nodeService.setProperty(geoOrigin2, PLMModel.PROP_GEO_ORIGIN_ISOCODE, "GB");
			geoOrigins.add(geoOrigin2);
			
			rawMaterial1.setGeoOrigins(geoOrigins);
			
			alfrescoRepository.save(rawMaterial1);
			
			/*-- Create initial version --*/
			entityVersionService.createInitialVersion(rawMaterial1NodeRef);
			
			/*-- Create second version (1.0) --*/
			NodeRef secondVersionNodeRef = entityVersionService.createVersion(rawMaterial1NodeRef, null);
			
			/*-- Geo origins --*/
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "geoOrigin3");
			NodeRef geoOrigin3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			nodeService.setProperty(geoOrigin3, PLMModel.PROP_GEO_ORIGIN_ISOCODE, "SP");
			geoOrigins.add(geoOrigin3);
			rawMaterial1.setGeoOrigins(geoOrigins);
			
			alfrescoRepository.save(rawMaterial1);
		
			/*-- Test assoc comparison --*/
			
			/*-- Comparison direction : rawMaterial1 - secondVersion --*/
			nodeRefs.add(secondVersionNodeRef);
			compareEntityService.compare(rawMaterial1NodeRef, nodeRefs, compareResult, structCompareResults);
			
			assertTrue(checkCompareRow(compareResult, "", "", ASSOC_PRODUCT_GEO_ORIGIN.toString(), 
					"[geoOrigin1, geoOrigin2, geoOrigin3, geoOrigin1, geoOrigin2]"));
			CompareResultDataItem cmpResGeoOrigin = compareResult.stream()
				.filter(c -> c.getProperty().toString().equals(ASSOC_PRODUCT_GEO_ORIGIN.toString()))
				.findFirst().orElse(null);
			assertTrue((cmpResGeoOrigin != null) && cmpResGeoOrigin.isDifferent());
			
			/*-- Comparison direction : secondVersion - rawMaterial1 --*/
			nodeRefs.clear();
			nodeRefs.add(rawMaterial1NodeRef);
			
			compareResult.clear();
			structCompareResults.clear();
			compareEntityService.compare(secondVersionNodeRef, nodeRefs, compareResult, structCompareResults);
			
			assertTrue(checkCompareRow(compareResult, "", "", ASSOC_PRODUCT_GEO_ORIGIN.toString(), 
					"[geoOrigin1, geoOrigin2, geoOrigin1, geoOrigin2, geoOrigin3]"));
			cmpResGeoOrigin = compareResult.stream()
				.filter(c -> c.getProperty().toString().equals(ASSOC_PRODUCT_GEO_ORIGIN.toString()))
				.findFirst().orElse(null);
			assertTrue((cmpResGeoOrigin != null) && cmpResGeoOrigin.isDifferent());
			
			/*-- Remove geoOrigin3 in rawMaterial1 and check if reverse list gets sorted --*/
			geoOrigins.remove(geoOrigins.size() - 1);
			Collections.reverse(geoOrigins);
			rawMaterial1.setGeoOrigins(geoOrigins);
			
			alfrescoRepository.save(rawMaterial1);
			
			compareResult.clear();
			structCompareResults.clear();
			compareEntityService.compare(secondVersionNodeRef, nodeRefs, compareResult, structCompareResults);
			
			assertTrue(checkCompareRow(compareResult, "", "", ASSOC_PRODUCT_GEO_ORIGIN.toString(), 
					"[geoOrigin1, geoOrigin2, geoOrigin1, geoOrigin2]"));
			
			/*-- Put geoOrigin2 twice to check duplicates removal --*/
			NodeRef geoOrigin2NodeRef = geoOrigins.get(geoOrigins.size() - 1);
			geoOrigins.add(geoOrigin2NodeRef);
			
			alfrescoRepository.save(rawMaterial1);
			
			compareResult.clear();
			structCompareResults.clear();
			compareEntityService.compare(secondVersionNodeRef, nodeRefs, compareResult, structCompareResults);
			
			assertTrue(checkCompareRow(compareResult, "", "", ASSOC_PRODUCT_GEO_ORIGIN.toString(), 
					"[geoOrigin1, geoOrigin2, geoOrigin1, geoOrigin2]"));
			
			return null;
		});
	}
	
	/**
	 * Test comparison.
	 */
	@Test
	public void testComparison() {

		final RawMaterialData allergenRawMateria = inWriteTx(() -> {

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

			CompoListDataItem parent1 = CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef);

			compoList.add(parent1);
			compoList.add(CompoListDataItem.build().withParent(parent1).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(parent1).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));

			CompoListDataItem parent2 = CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef);
			compoList.add(parent2);
			compoList.add(CompoListDataItem.build().withParent(parent2).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			// compoList.add(CompoListDataItem.build().withParent(2).withQty(3d).withQtyUsed(0d).withUnit(// 0d).withLossPerc(ProductUnit.kg).withDeclarationType("").withProduct(DeclarationType.OMIT_FR,
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
			CompoListDataItem parent11 = CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef);
			compoList.add(parent11);
			compoList.add(CompoListDataItem.build().withParent(parent11).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(parent11).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			CompoListDataItem parent22 = CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef);
			compoList.add(parent22);
			compoList.add(CompoListDataItem.build().withParent(parent22).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(parent22).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial4NodeRef));
			fp2.getCompoListView().setCompoList(compoList);

			fp2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp2).getNodeRef();

			return allergenRawMaterial;

		});

		inReadTx(() -> {

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

		});
	}

	/**
	 * Test struct comparison.
	 */
	@Test
	public void testStructComparison() {

		try {

			inWriteTx(() -> {
				systemConfigurationService.updateConfValue("beCPG.product.name.format", "{cm:name}");
				return null;
			});

			fp1NodeRef = inWriteTx(() -> {

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");

				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
				compoList
						.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
				compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
				compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
				compoList
						.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
				// compoList.add(CompoListDataItem.build().withParent(2).withQty(3d).withQtyUsed(0d).withUnit(// 0d).withLossPerc(ProductUnit.kg).withDeclarationType("").withProduct(DeclarationType.OMIT_FR,
				// rawMaterial4NodeRef));
				fp1.getCompoListView().setCompoList(compoList);

				return alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

			});

			fp2NodeRef = inWriteTx(() -> {

				logger.debug("createRawMaterial 1");

				SemiFinishedProductData sf2 = new SemiFinishedProductData();
				sf2.setName("SF 2");

				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(CompoListDataItem.build().withParent(null).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
				compoList.add(CompoListDataItem.build().withParent(null).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
				sf2.getCompoListView().setCompoList(compoList);

				NodeRef sf2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sf2).getNodeRef();

				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");

				compoList = new ArrayList<>();
				compoList.add(CompoListDataItem.build().withParent(null).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
				compoList
						.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
				compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
				compoList.add(CompoListDataItem.build().withParent(null).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
				compoList
						.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
				compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial4NodeRef));
				compoList.add(CompoListDataItem.build().withParent(null).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(sf2NodeRef));
				fp2.getCompoListView().setCompoList(compoList);

				return alfrescoRepository.create(getTestFolderNodeRef(), fp2).getNodeRef();

			});

			waitForSolr();

			inReadTx(() -> {

				Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<>();
				defaultCompareEntityServicePlugin.compareStructDatalist(fp1NodeRef, fp2NodeRef, PLMModel.TYPE_COMPOLIST, structCompareResults);
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

				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 1,
						StructCompareOperator.Modified, "Local semi finished 1", "Local semi finished 1",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=1}", "{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=2}"));
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 1,
						StructCompareOperator.Modified, "Local semi finished 2", "Local semi finished 2",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=1, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg}",
						"{{http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=Pièce}"));

				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 2, StructCompareOperator.Added,
						"", "Raw material 4", "{}",
						"{{http://www.alfresco.org/model/system/1.0}locale=fr, {http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg, {http://www.bcpg.fr/model/becpg/1.0}compoListQtySubFormula=0, {http://www.bcpg.fr/model/becpg/1.0}compoListDeclType=Détailler, {http://www.bcpg.fr/model/becpg/1.0}compoListLossPerc=0, {http://www.bcpg.fr/model/becpg/1.0}compoListProduct=Raw material 4, {http://www.bcpg.fr/model/becpg/1.0}depthLevel=2}"));
  
				assertTrue(checkStructCompareRow(structCompareResult, "{http://www.bcpg.fr/model/becpg/1.0}compoList", 1, StructCompareOperator.Added,
						"", "SF 2", "{}",
						"{{http://www.alfresco.org/model/system/1.0}locale=fr, {http://www.bcpg.fr/model/becpg/1.0}compoListQty=3, {http://www.bcpg.fr/model/becpg/1.0}compoListUnit=kg, {http://www.bcpg.fr/model/becpg/1.0}compoListQtySubFormula=0, {http://www.bcpg.fr/model/becpg/1.0}compoListDeclType=Détailler, {http://www.bcpg.fr/model/becpg/1.0}compoListLossPerc=0, {http://www.bcpg.fr/model/becpg/1.0}compoListProduct=SF 2, {http://www.bcpg.fr/model/becpg/1.0}depthLevel=1}"));
				return null;

			});

		} finally {
			systemConfigurationService.resetConfValue("beCPG.product.name.format");
		}
	}

}
