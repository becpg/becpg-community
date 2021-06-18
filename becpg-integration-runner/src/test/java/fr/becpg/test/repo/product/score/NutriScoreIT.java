package fr.becpg.test.repo.product.score;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.importer.impl.ImportHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.formulation.score.NutriScore;
import fr.becpg.test.PLMBaseTestCase;

public class NutriScoreIT extends PLMBaseTestCase {

	@Autowired
	NutriScore nutriScore;
	
	@Autowired
	private NamespaceService namespaceService;

	private static final String BCPG_PHYSICO_CHEM = "bcpg:physicoChem";
	private static final String BCPG_NUT = "bcpg:nut";

	@Test
	public void testNutriScore() {
		
		NodeRef energyKjNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.energykj"), nodeService);
		NodeRef satFatNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.satfat"), nodeService);
		NodeRef totalFatNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.totalfat"), nodeService);
		NodeRef totalSugarNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.sugar"), nodeService);
		NodeRef sodiumNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.sodium"), nodeService);
		NodeRef percFruitsAndVetgsNode = ImportHelper.findCharact(QName.createQName(BCPG_PHYSICO_CHEM, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.fruitsandveg"), nodeService);
		NodeRef nspFibreNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.psacns"), nodeService);
		NodeRef aoacFibreNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.fibtg"), nodeService);
		NodeRef proteinNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.protein"), nodeService);
		
		// 2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others"

		NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 2084d, null, null, null, null, energyKjNode, null));
			nutList.add(new NutListDataItem(null, 2.8d, null, null, null, null, satFatNode, null));
			nutList.add(new NutListDataItem(null, 22.9d, null, null, null, null, totalFatNode, null));
			nutList.add(new NutListDataItem(null, 4.73d, null, null, null, null, totalSugarNode, null));
			nutList.add(new NutListDataItem(null, 672d, null, null, null, null, sodiumNode, null));
			nutList.add(new NutListDataItem(null, 0d, null, null, null, null, nspFibreNode, null));
			nutList.add(new NutListDataItem(null, 4.13d, null, null, null, null, aoacFibreNode, null));
			nutList.add(new NutListDataItem(null, 5.81d, null, null, null, null, proteinNode, null));
			
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
			nodeService.setProperty(finishedProductNodeRef1, QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileCategory"), NutrientProfileCategory.Others);
			return null;
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef1);
			
			nutriScore.formulateScore(finishedProduct);
			
			Assert.assertEquals((Double) 12d, finishedProduct.getNutrientScore());
			
			Assert.assertEquals("D", finishedProduct.getNutrientClass());
			
			alfrescoRepository.save(finishedProduct);

			return null;
		}, false, true);
		

		NodeRef finishedProductNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// 2596.0642d, 15.16d, 70.172d, 0.003d, 159d, 0d, 0d, 0d, 0.15d, "Fats"
			List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
			nutList.add(new NutListDataItem(null, 2596.0642d, null, null, null, null, energyKjNode, null));
			nutList.add(new NutListDataItem(null, 15.16d, null, null, null, null, satFatNode, null));
			nutList.add(new NutListDataItem(null, 70.172d, null, null, null, null, totalFatNode, null));
			nutList.add(new NutListDataItem(null, 0.003d, null, null, null, null, totalSugarNode, null));
			nutList.add(new NutListDataItem(null, 159d, null, null, null, null, sodiumNode, null));
			nutList.add(new NutListDataItem(null, 0d, null, null, null, null, nspFibreNode, null));
			nutList.add(new NutListDataItem(null, 0d, null, null, null, null, aoacFibreNode, null));
			nutList.add(new NutListDataItem(null, 0.15d, null, null, null, null, proteinNode, null));
			
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
			nodeService.setProperty(finishedProductNodeRef2, QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileCategory"), NutrientProfileCategory.Fats);
			return null;
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef2);
			
			nutriScore.formulateScore(finishedProduct);
			
			Assert.assertEquals((Double) 10d, finishedProduct.getNutrientScore());
			
			Assert.assertEquals("C", finishedProduct.getNutrientClass());
			
			alfrescoRepository.save(finishedProduct);

			return null;
		}, false, true);

	}
	
}
