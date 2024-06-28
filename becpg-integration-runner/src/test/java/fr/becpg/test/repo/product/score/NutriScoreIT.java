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

		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.formulation.score.nutriscore.regulatoryClass",
					"fr.becpg.repo.product.helper.Nutrient5C2021Helper");
			return null;
		});

		NodeRef energyKjNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "ENER-KJO");
		NodeRef satFatNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FASAT");
		NodeRef totalFatNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FAT");
		NodeRef totalSugarNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "SUGAR");
		NodeRef sodiumNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "NA");
		NodeRef saltNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "NACL");
		NodeRef percFruitsAndVetgsNode = findOrCreateNode(PLMModel.TYPE_PHYSICO_CHEM, PLMModel.PROP_PHYSICO_CHEM_CODE,
				"FRUIT_VEGETABLE");
		NodeRef nspFibreNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "PSACNS");
		NodeRef aoacFibreNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FIBTG");
		NodeRef proteinNode = findOrCreateNode(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "PRO-");

		// 2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others"

		NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 2084d, null, null, null, null, energyKjNode, true));
			nutList.add(new NutListDataItem(null, 2.8d, null, null, null, null, satFatNode, true));
			nutList.add(new NutListDataItem(null, 22.9d, null, null, null, null, totalFatNode, true));
			nutList.add(new NutListDataItem(null, 4.73d, null, null, null, null, totalSugarNode, true));
			nutList.add(new NutListDataItem(null, 672d, null, null, null, null, sodiumNode, true));
			nutList.add(new NutListDataItem(null, 0d, null, null, null, null, nspFibreNode, true));
			nutList.add(new NutListDataItem(null, 4.13d, null, null, null, null, aoacFibreNode, true));
			nutList.add(new NutListDataItem(null, 5.81d, null, null, null, null, proteinNode, true));

			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 0d, null, null, null, percFruitsAndVetgsNode));

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.getAspects().add(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
			finishedProductData.setName("Test1");
			finishedProductData.setNutList(nutList);
			finishedProductData.setPhysicoChemList(physicoChemList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData).getNodeRef();
		});

		inWriteTx(() -> {
			nodeService.setProperty(finishedProductNodeRef1,
					QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileCategory"), NutrientProfileCategory.Others);
			return null;
		});

		inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository
					.findOne(finishedProductNodeRef1);

			productService.formulate(finishedProduct);

			Assert.assertEquals((Double) 12d, finishedProduct.getNutrientScore());

			Assert.assertEquals("D", finishedProduct.getNutrientClass());

			alfrescoRepository.save(finishedProduct);

			return null;
		});

		NodeRef finishedProductNodeRef2 = inWriteTx(() -> {
			// 2596.0642d, 15.16d, 70.172d, 0.003d, 159d, 0d, 0d, 0d, 0.15d, "Fats"
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 2596.0642d, null, null, null, null, energyKjNode, true));
			nutList.add(new NutListDataItem(null, 15.16d, null, null, null, null, satFatNode, true));
			nutList.add(new NutListDataItem(null, 70.172d, null, null, null, null, totalFatNode, true));
			nutList.add(new NutListDataItem(null, 0.003d, null, null, null, null, totalSugarNode, true));
			nutList.add(new NutListDataItem(null, 159d, null, null, null, null, sodiumNode, true));
			nutList.add(new NutListDataItem(null, 0d, null, null, null, null, nspFibreNode, true));
			nutList.add(new NutListDataItem(null, 0d, null, null, null, null, aoacFibreNode, true));
			nutList.add(new NutListDataItem(null, 0.15d, null, null, null, null, proteinNode, true));

			ArrayList<PhysicoChemListDataItem> physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 0d, null, null, null, percFruitsAndVetgsNode));

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.getAspects().add(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
			finishedProductData.setName("Test2");
			finishedProductData.setNutList(nutList);
			finishedProductData.setPhysicoChemList(physicoChemList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData).getNodeRef();
		});

		inWriteTx(() -> {
			nodeService.setProperty(finishedProductNodeRef2,
					QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileCategory"), NutrientProfileCategory.Fats);
			return null;
		});

		inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository
					.findOne(finishedProductNodeRef2);

			productService.formulate(finishedProduct);

			Assert.assertEquals((Double) 10d, finishedProduct.getNutrientScore());

			Assert.assertEquals("C", finishedProduct.getNutrientClass());

			alfrescoRepository.save(finishedProduct);

			return null;
		});

		NodeRef finishedProductNodeRef3 = inWriteTx(() -> {
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 797.0, null, null, null, null, energyKjNode, true));
			nutList.add(new NutListDataItem(null, 4.6, null, null, null, null, satFatNode, true));
			nutList.add(new NutListDataItem(null, 7.1, null, null, null, null, totalFatNode, true));
			nutList.add(new NutListDataItem(null, 19.0, null, null, null, null, totalSugarNode, true));
			nutList.add(new NutListDataItem(null, 92.0, null, null, null, null, sodiumNode, true));
			nutList.add(new NutListDataItem(null, 0.23, null, null, null, null, saltNode, true));
			nutList.add(new NutListDataItem(null, 0.2, null, null, null, null, nspFibreNode, true));
			nutList.add(new NutListDataItem(null, 0.0, null, null, null, null, aoacFibreNode, true));
			nutList.add(new NutListDataItem(null, 3.3, null, null, null, null, proteinNode, true));

			ArrayList<PhysicoChemListDataItem> physicoChemList = new ArrayList<PhysicoChemListDataItem>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 0d, null, null, null, percFruitsAndVetgsNode));

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.getAspects().add(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
			finishedProductData.setName("Test3");
			finishedProductData.setNutList(nutList);
			finishedProductData.setPhysicoChemList(physicoChemList);
			finishedProductData.setNutrientProfileCategory(NutrientProfileCategory.Others.toString());
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData).getNodeRef();
		});

		inWriteTx(() -> {

			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository
					.findOne(finishedProductNodeRef3);

			productService.formulate(finishedProduct);

			Assert.assertEquals((Double) 11d, finishedProduct.getNutrientScore());

			Assert.assertEquals("D", finishedProduct.getNutrientClass());

			alfrescoRepository.save(finishedProduct);

			return null;
		});

		inWriteTx(() -> {
			systemConfigurationService.resetConfValue("beCPG.formulation.score.nutriscore.regulatoryClass");
			return null;
		});
	}

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
				return nodeService
						.createNode(finalParentRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
										(String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
								type, properties)
						.getChildRef();
			}, false, true);
		}

		return node;

	}
}
