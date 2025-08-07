package fr.becpg.test.repo.product.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.formulation.score.FrenchEcoScore;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FrenchEcoScoreIT extends AbstractFinishedProductTest {

	@Autowired
	FrenchEcoScore frenchEcoScore;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
	}

	@Test
	public void testFrenchEcoScore() {

		// PRINCE GOUT CHOCOLAT

		final List<PackMaterialListDataItem> packMaterial1 = new ArrayList<>();

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, "Carton");
		properties.put(PackModel.PROP_PM_ECOSCORE, 91);
		properties.put(PackModel.PROP_PM_ISNOTRECYCLABLE, false);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> fillMaterial(packMaterial1, properties, 10d), false, true);

		properties.clear();
		properties.put(BeCPGModel.PROP_LV_VALUE, "Autre plastique");
		properties.put(PackModel.PROP_PM_ECOSCORE, 0);
		properties.put(PackModel.PROP_PM_ISNOTRECYCLABLE, false);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> fillMaterial(packMaterial1, properties, 1d), false, true);

		final NodeRef finishedProduct1 = createFinishedProduct("Biscuit sec chocolaté, préemballé", "24036", "Prince goût chocolat", geoOrigin1, 48d,
				ing1, packMaterial1, null);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct1);

			frenchEcoScore.formulateScore(finishedProductData);

			Assert.assertEquals((Double) 57d, finishedProductData.getEcoScore());

			Assert.assertEquals("C", finishedProductData.getEcoScoreClass());

			alfrescoRepository.save(finishedProductData);

			return null;
		}, false, true);

		// CRUESLI CHOCOLAT NOIR

		final List<PackMaterialListDataItem> packMaterial2 = new ArrayList<>();

		properties.clear();
		properties.put(BeCPGModel.PROP_LV_VALUE, "Carton");
		properties.put(PackModel.PROP_PM_ECOSCORE, 91);
		properties.put(PackModel.PROP_PM_ISNOTRECYCLABLE, true);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> fillMaterial(packMaterial2, properties, 1d), false, true);

		properties.clear();
		properties.put(BeCPGModel.PROP_LV_VALUE, "Autre plastique");
		properties.put(PackModel.PROP_PM_ECOSCORE, 0);
		properties.put(PackModel.PROP_PM_ISNOTRECYCLABLE, true);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> fillMaterial(packMaterial2, properties, 1d), false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(geoOrigin2, PLMModel.PROP_GEO_ORIGIN_ISOCODE, "GB");
			return null;
		}, false, true);

		final NodeRef finishedProduct2 = createFinishedProduct("Muesli croustillant au chocolat (non enrichi en vitamines et minéraux)", "32109",
				"Cruesli Chocolat noir", geoOrigin2, 37d, ing2, packMaterial2, null);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct2);

			frenchEcoScore.formulateScore(finishedProductData);

			Assert.assertEquals((Double) 77d, finishedProductData.getEcoScore());
			Assert.assertEquals("B", finishedProductData.getEcoScoreClass());

			alfrescoRepository.save(finishedProductData);

			return null;
		}, false, true);

		// SARDINES DE BRETAGNE

		final List<PackMaterialListDataItem> packMaterial3 = new ArrayList<>();
		final List<LabelClaimListDataItem> labelClaimList = new ArrayList<>();

		properties.clear();
		properties.put(BeCPGModel.PROP_LV_VALUE, "Alluminium");
		properties.put(PackModel.PROP_PM_ECOSCORE, 72);
		properties.put(PackModel.PROP_PM_ISNOTRECYCLABLE, false);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> fillMaterial(packMaterial3, properties, 1d), false, true);

		properties.clear();
		properties.put(ContentModel.PROP_NAME, "MSC");
		properties.put(PLMModel.PROP_LABEL_CLAIM_CODE, "MARINE_STEWARDSHIP_COUNCIL_LABEL");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> fillLabelClaim(labelClaimList, properties), false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(geoOrigin2, PLMModel.PROP_GEO_ORIGIN_ISOCODE, "FAO 27");
			return null;
		}, false, true);

		final NodeRef finishedProduct3 = createFinishedProduct("Sardine, filets sans arêtes à l'huile d'olive, appertisés, égouttés", "26231",
				"Sardines de Bretagne", geoOrigin2, 100d, ing2, packMaterial3, labelClaimList);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProduct3);

			finishedProductData.setLabelClaimList(labelClaimList);

			frenchEcoScore.formulateScore(finishedProductData);

			Assert.assertEquals((Double) 69d, finishedProductData.getEcoScore());
			Assert.assertEquals("B", finishedProductData.getEcoScoreClass());

			alfrescoRepository.save(finishedProductData);

			return null;
		}, false, true);

	}

	private List<LabelClaimListDataItem> fillLabelClaim(List<LabelClaimListDataItem> labelClaimList, Map<QName, Serializable> properties) {

		NodeRef labelClaimNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_LABEL_CLAIM,
				properties).getChildRef();

		LabelClaimListDataItem labelClaimItem = new LabelClaimListDataItem().withLabelClaim(labelClaimNodeRef).withType("Test claim type")
				.withIsClaimed(Boolean.TRUE);

		labelClaimList.add(labelClaimItem);

		return labelClaimList;
	}

	private List<PackMaterialListDataItem> fillMaterial(List<PackMaterialListDataItem> packMaterial, Map<QName, Serializable> properties,
			double qty) {
		NodeRef packMaterialNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
				PackModel.TYPE_PACKAGING_MATERIAL, properties).getChildRef();

		packMaterial.add(PackMaterialListDataItem.build().withMaterial(packMaterialNodeRef).withWeight(qty).withPkgLevel(PackagingLevel.Primary));

		return packMaterial;
	}

	private NodeRef createFinishedProduct(String query, String categoryCode, String name, NodeRef geoOrigin, double ingQty, NodeRef ing,
			List<PackMaterialListDataItem> packMaterial, List<LabelClaimListDataItem> labelClaimList) {

		AutoCompletePage ret = frenchEcoScore.suggest(FrenchEcoScore.ECO_SCORE_SOURCE_TYPE, query, 1, 500, null);

		Assert.assertTrue(ret.getFullListSize() > 0);

		boolean found = false;

		for (AutoCompleteEntry entry : ret.getResults()) {
			if (entry.getValue().equals(categoryCode)) {
				found = true;
				break;
			}
		}

		Assert.assertTrue(found);

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.setName(name);

			finishedProductData.setEcoScoreCategory(categoryCode);

			List<IngListDataItem> ingList = new ArrayList<>();

			ingList.add(IngListDataItem.build().withQtyPerc(ingQty).withGeoOrigin(Arrays.asList(geoOrigin)).withBioOrigin(new ArrayList<>())
					.withIsGMO(true).withIsIonized(true).withIsProcessingAid(false).withIngredient(ing).withIsManual(false));

			finishedProductData.setIngList(ingList);

			finishedProductData.setPackMaterialList(packMaterial);

			if (labelClaimList != null) {
				finishedProductData.setLabelClaimList(labelClaimList);
			}

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData).getNodeRef();

		}, false, true);

	}

}
