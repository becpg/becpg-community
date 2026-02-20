package fr.becpg.test.repo.product.score;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.PLMBaseTestCase;

public class NutriScoreIT extends PLMBaseTestCase {

	@Autowired
	private ProductService productService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;

	private NodeRef energyKjNode;
	private NodeRef satFatNode;
	private NodeRef totalFatNode;
	private NodeRef totalSugarNode;
	private NodeRef sodiumNode;
	private NodeRef saltNode;
	private NodeRef percFruitsAndVetgsNode;
	private NodeRef nspFibreNode;
	private NodeRef aoacFibreNode;
	private NodeRef proteinNode;

	@Override
	protected void doInitRepo(boolean shouldInit) {
		super.doInitRepo(shouldInit);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			energyKjNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.ENERGY_CODE, "kJ");
			satFatNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.SATFAT_CODE, "g");
			totalFatNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.FAT_CODE, "g");
			totalSugarNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.SUGAR_CODE, "g");
			sodiumNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.SODIUM_CODE, "mg");
			saltNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.SALT_CODE, "g");
			percFruitsAndVetgsNode = CharactTestHelper.getOrCreatePhysicoChem(nodeService, NutriScoreContext.FRUIT_VEGETABLE_CODE, "%");
			nspFibreNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.NSP_CODE, "g");
			aoacFibreNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.AOAC_CODE, "g");
			proteinNode = CharactTestHelper.getOrCreateNutrient(nodeService, NutriScoreContext.PROTEIN_CODE, "g");
			return null;
		}, false, true);
	}

	@Before
	public void setUp() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			systemConfigurationService.updateConfValue("beCPG.formulation.score.nutriscore.regulatoryClass", "fr.becpg.repo.product.helper.Nutrient5C2021Helper");
			return null;
		}, false, true);
	}

	@After
	public void tearDown() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			systemConfigurationService.resetConfValue("beCPG.formulation.score.nutriscore.regulatoryClass");
			return null;
		}, false, true);
	}

	@Test
	public void testNutriScore() {

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

	}

}
