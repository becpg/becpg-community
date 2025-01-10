package fr.becpg.test.repo.product.score;

import java.io.Serializable;
import java.util.ArrayList;
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
import fr.becpg.model.GS1Model;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.importer.impl.ImportHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.PLMBaseTestCase;

public class NutriScoreIT extends PLMBaseTestCase {

	@Autowired
	private ProductService productService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Test
	public void testNutriScore() {
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			systemConfigurationService.updateConfValue("beCPG.formulation.score.nutriscore.regulatoryClass", "fr.becpg.repo.product.helper.Nutrient5C2021Helper");
			return null;
		}, false, true);

		NodeRef energyKjNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "ENER-KJO");
		NodeRef satFatNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FASAT");
		NodeRef totalFatNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FAT");
		NodeRef totalSugarNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "SUGAR");
		NodeRef sodiumNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "NA");
		NodeRef saltNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "NACL");
		NodeRef percFruitsAndVetgsNode = findOrCreateNode(PLMModel.TYPE_PHYSICO_CHEM, PLMModel.PROP_PHYSICO_CHEM_CODE, "FRUIT_VEGETABLE");
		NodeRef nspFibreNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "PSACNS");
		NodeRef aoacFibreNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FIBTG");
		NodeRef proteinNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "PRO-");

		// 2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others"

		NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(NutListDataItem.build().withValue(2084d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(energyKjNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(2.8d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(satFatNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(22.9d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(totalFatNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(4.73d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(totalSugarNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(672d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(sodiumNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(nspFibreNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(4.13d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(aoacFibreNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(5.81d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(proteinNode).withIsManual(true)
);

			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 0d, null, null, null, percFruitsAndVetgsNode));

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.getAspects().add(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
			finishedProductData.setName("Test1");
			finishedProductData.setNutList(nutList);
			finishedProductData.setPhysicoChemList(physicoChemList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData).getNodeRef();
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(finishedProductNodeRef1, QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileCategory"),
					NutrientProfileCategory.Others);
			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

			productService.formulate(finishedProduct);

			Assert.assertEquals((Double) 12d, finishedProduct.getNutrientScore());

			Assert.assertEquals("D", finishedProduct.getNutrientClass());

			alfrescoRepository.save(finishedProduct);

			return null;
		}, false, true);

		NodeRef finishedProductNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// 2596.0642d, 15.16d, 70.172d, 0.003d, 159d, 0d, 0d, 0d, 0.15d, "Fats"
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(NutListDataItem.build().withValue(2596.0642d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(energyKjNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(15.16d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(satFatNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(70.172d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(totalFatNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0.003d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(totalSugarNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(159d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(sodiumNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(nspFibreNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(aoacFibreNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0.15d).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(proteinNode).withIsManual(true)
);

			ArrayList<PhysicoChemListDataItem> physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 0d, null, null, null, percFruitsAndVetgsNode));

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.getAspects().add(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
			finishedProductData.setName("Test2");
			finishedProductData.setNutList(nutList);
			finishedProductData.setPhysicoChemList(physicoChemList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData).getNodeRef();
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(finishedProductNodeRef2, QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileCategory"),
					NutrientProfileCategory.Fats);
			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef2);

			productService.formulate(finishedProduct);

			Assert.assertEquals((Double) 10d, finishedProduct.getNutrientScore());

			Assert.assertEquals("C", finishedProduct.getNutrientClass());

			alfrescoRepository.save(finishedProduct);

			return null;
		}, false, true);

		NodeRef finishedProductNodeRef3 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(NutListDataItem.build().withValue(797.0).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(energyKjNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(4.6).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(satFatNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(7.1).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(totalFatNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(19.0).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(totalSugarNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(92.0).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(sodiumNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0.23).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(saltNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0.2).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(nspFibreNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(0.0).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(aoacFibreNode).withIsManual(true)
);
			nutList.add(NutListDataItem.build().withValue(3.3).withUnit(null).withMini(null).withMaxi(null).withGroup(null).withNut(proteinNode).withIsManual(true)
);

			ArrayList<PhysicoChemListDataItem> physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 0d, null, null, null, percFruitsAndVetgsNode));

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.getAspects().add(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
			finishedProductData.setName("Test3");
			finishedProductData.setNutList(nutList);
			finishedProductData.setPhysicoChemList(physicoChemList);
			finishedProductData.setNutrientProfileCategory(NutrientProfileCategory.Others.toString());
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData).getNodeRef();
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef3);

			productService.formulate(finishedProduct);

			Assert.assertEquals((Double) 11d, finishedProduct.getNutrientScore());

			Assert.assertEquals("D", finishedProduct.getNutrientClass());

			alfrescoRepository.save(finishedProduct);

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			systemConfigurationService.resetConfValue("beCPG.formulation.score.nutriscore.regulatoryClass");
			return null;
		}, false, true);
	}

	@Deprecated //Merge with CharactTestHelper
	private NodeRef findOrCreateNode(QName type, QName property, String value) {
		NodeRef node = ImportHelper.findCharact(type, property, value, nodeService);

		if (node == null) {
			NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

			NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);

			NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);

			NodeRef entityListNodeRef = repoService.getFolderByPath(charactsNodeRef, "bcpg:entityLists");
			;

			NodeRef parentRef = null;

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, value);
			properties.put(property, value);
			if (type.equals(PLMModel.TYPE_NUT)) {
				parentRef = repoService.getFolderByPath(entityListNodeRef, "cm:Nuts");
				if ("NA".equals(value)) {
					properties.put(PLMModel.PROP_NUTUNIT, "mg");
				} else {
					properties.put(PLMModel.PROP_NUTUNIT, "g");
				}

			} else if (type.equals(PLMModel.TYPE_PHYSICO_CHEM)) {
				parentRef = repoService.getFolderByPath(entityListNodeRef, "cm:PhysicoChems");
				properties.put(PLMModel.PROP_PHYSICO_CHEM_UNIT, "%");
			}

			NodeRef finalParentRef = parentRef;

			node = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				return nodeService.createNode(finalParentRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)), type,
						properties).getChildRef();
			}, false, true);
		}

		return node;

	}
}
