package fr.becpg.repo.product.formulation.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.importer.impl.ImportHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.helper.Nutrient5CHelper;

@Service("nutriScore")
public class NutriScore implements ScoreCalculatingPlugin {

	private static final Log logger = LogFactory.getLog(NutriScore.class);

	private static final String BCPG_PHYSICO_CHEM = "bcpg:physicoChem";
	private static final String BCPG_NUT = "bcpg:nut";
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private NamespaceService namespaceService;
	
	private static final List<String> NUTRIENT_PROFILE_CLASSES = Arrays.asList("E","D","C","B","A");

	@Override
	public boolean accept(ProductData productData) {
		return productData.getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
	}

	@Override
	public boolean formulateScore(ProductData productData) {
		
		Serializable nutrientProfileCategory = nodeService.getProperty(productData.getNodeRef(), QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileCategory"));
		
		if (nutrientProfileCategory instanceof String) {
			
			try {
				String category = (String) nutrientProfileCategory;
				
				List<Double> ranges = null;
				
				NodeRef energyKjNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.energykj"), nodeService);
				NodeRef satFatNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.satfat"), nodeService);
				NodeRef totalFatNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.totalfat"), nodeService);
				NodeRef totalSugarNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.sugar"), nodeService);
				NodeRef sodiumNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.sodium"), nodeService);
				NodeRef percFruitsAndVetgsNode = ImportHelper.findCharact(QName.createQName(BCPG_PHYSICO_CHEM, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.fruitsandveg"), nodeService);
				NodeRef nspFibreNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.psacns"), nodeService);
				NodeRef aoacFibreNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.fibtg"), nodeService);
				NodeRef proteinNode = ImportHelper.findCharact(QName.createQName(BCPG_NUT, namespaceService), BeCPGModel.PROP_CHARACT_NAME, I18NUtil.getMessage("plm.nut.protein"), nodeService);
				
				if (NutrientProfileCategory.Beverages.toString().equals(category)) {
					ranges = Arrays.asList(9d, 5d, 1d, 0d);
				} else if (NutrientProfileCategory.Cheeses.toString().equals(category)) {
					ranges = Arrays.asList(18d, 10d, 2d, -1d);
				} else if (NutrientProfileCategory.Fats.toString().equals(category)) {
					ranges = Arrays.asList(18d, 10d, 2d, -1d);
				} else if (NutrientProfileCategory.Others.toString().equals(category)) {
					ranges = Arrays.asList(18d, 10d, 2d, -1d);
				}
				
				Double energyKj = 0d;
				Double satFat = 0d;
				Double totalFat = 0d;
				Double totalSugar = 0d;
				Double sodium = 0d;
				Double percFruitsAndVetgs = 0d;
				Double nspFibre = 0d;
				Double aoacFibre = 0d;
				Double protein = 0d;
				
				for (NutListDataItem nut : productData.getNutList()) {
					if (nut.getNut().equals(energyKjNode)) {
						energyKj = nut.getValue();
					} else if (nut.getNut().equals(satFatNode)) {
						satFat = nut.getValue();
					} else if (nut.getNut().equals(totalFatNode)) {
						totalFat = nut.getValue();
					} else if (nut.getNut().equals(totalSugarNode)) {
						totalSugar = nut.getValue();
					} else if (nut.getNut().equals(sodiumNode)) {
						sodium = nut.getValue();
					} else if (nut.getNut().equals(nspFibreNode)) {
						nspFibre = nut.getValue();
					} else if (nut.getNut().equals(aoacFibreNode)) {
						aoacFibre = nut.getValue();
					} else if (nut.getNut().equals(proteinNode)) {
						protein = nut.getValue();
					}
				}
				
				for (PhysicoChemListDataItem physico : productData.getPhysicoChemList()) {
					if (physico.getPhysicoChem().equals(percFruitsAndVetgsNode)) {
						percFruitsAndVetgs = physico.getValue();
					}
				}
				
				int nutriScore = Nutrient5CHelper.compute5CScore(energyKj, satFat, totalFat, totalSugar, sodium, percFruitsAndVetgs, nspFibre, aoacFibre, protein, category);
				
				String nutrientClass = Nutrient5CHelper.buildNutrientClass((double) nutriScore, ranges, NUTRIENT_PROFILE_CLASSES);
				
				productData.setNutrientScore((double) nutriScore);
				productData.setNutrientClass(nutrientClass);
				
			} catch (Exception e) {
				MLText errorMsg = MLTextHelper.getI18NMessage("message.formulate.formula.incorrect.nutrientProfile", e.getLocalizedMessage());
				
				productData.setNutrientClass(MLTextHelper.getClosestValue(errorMsg, Locale.getDefault()));
				
				productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, errorMsg, null, new ArrayList<>(),
						RequirementDataType.Formulation));
				
				if (logger.isDebugEnabled()) {
					logger.warn("Error in nutrient score formulation :" + productData.getNodeRef());
					logger.trace(e, e);
				}
				
				return false;
			}
		} else {
			productData.setNutrientScore(null);
			productData.setNutrientClass(null);
		}
		
		return true;
	}
	
}
