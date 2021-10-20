package fr.becpg.repo.product.formulation.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.GS1Model;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.importer.impl.ImportHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.helper.Nutrient5C2021Helper;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@Service("nutriScore")
public class NutriScore implements ScoreCalculatingPlugin {

	private static final Log logger = LogFactory.getLog(NutriScore.class);

	@Autowired
	private NodeService nodeService;
	
	private static final List<String> NUTRIENT_PROFILE_CLASSES = Arrays.asList("E","D","C","B","A");

	@Override
	public boolean accept(ScorableEntity productData) {
		return (productData instanceof ProductData) &&  ((BeCPGDataObject) productData).getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
	}

	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		ProductData productData  = (ProductData) scorableEntity;
		
		Serializable prop = nodeService.getProperty(productData.getNodeRef(), PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY);
		
		if (prop != null && !prop.equals("")) {
			
			try {
				
				NutrientProfileCategory nutrientProfileCategory = null;
				
				if (prop instanceof String) {
					nutrientProfileCategory = NutrientProfileCategory.valueOf((String) prop);
				} else if (prop instanceof NutrientProfileCategory) {
					nutrientProfileCategory = (NutrientProfileCategory) prop;
				}
				
				List<Double> ranges = null;
				
				NodeRef energyKjNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "ENER-KJO", nodeService);
				NodeRef satFatNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FASAT", nodeService);
				NodeRef totalFatNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FAT", nodeService);
				NodeRef totalSugarNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "SUGAR", nodeService);
				NodeRef sodiumNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "NA", nodeService);
				NodeRef percFruitsAndVetgsNode = ImportHelper.findCharact(PLMModel.TYPE_PHYSICO_CHEM, PLMModel.PROP_PHYSICO_CHEM_CODE, "FRUIT_VEGETABLE", nodeService);
				NodeRef nspFibreNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "PSACNS", nodeService);
				NodeRef aoacFibreNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "FIBTG", nodeService);
				NodeRef proteinNode = ImportHelper.findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, "PRO-", nodeService);
				
				if (NutrientProfileCategory.Beverages.equals(nutrientProfileCategory)) {
					ranges = Arrays.asList(9d, 5d, 1d, 0d);
				} else if (NutrientProfileCategory.Cheeses.equals(nutrientProfileCategory)) {
					ranges = Arrays.asList(18d, 10d, 2d, -1d);
				} else if (NutrientProfileCategory.Fats.equals(nutrientProfileCategory)) {
					ranges = Arrays.asList(18d, 10d, 2d, -1d);
				} else if (NutrientProfileCategory.Others.equals(nutrientProfileCategory)) {
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
				
				int check = 0;
				
				for (NutListDataItem nut : productData.getNutList()) {
					if (nut.getNut().equals(energyKjNode)) {
						energyKj = nut.value("EU");
						check++;
					} else if (nut.getNut().equals(satFatNode)) {
						satFat = nut.value("EU");
						check++;
					} else if (nut.getNut().equals(totalFatNode)) {
						totalFat = nut.value("EU");
						check++;
					} else if (nut.getNut().equals(totalSugarNode)) {
						totalSugar = nut.value("EU");
						check++;
					} else if (nut.getNut().equals(sodiumNode)) {
						sodium = nut.value("EU");
						check++;
					} else if (nut.getNut().equals(nspFibreNode)) {
						nspFibre = nut.value("EU");
						check++;
					} else if (nut.getNut().equals(aoacFibreNode)) {
						aoacFibre = nut.value("EU");
						check++;
					} else if (nut.getNut().equals(proteinNode)) {
						protein = nut.value("EU");
						check++;
					}
					
					if (check == 8) {
						break;
					}
				}
				
				for (PhysicoChemListDataItem physico : productData.getPhysicoChemList()) {
					if (physico.getPhysicoChem().equals(percFruitsAndVetgsNode)) {
						percFruitsAndVetgs = physico.getValue();
						break;
					}
				}
				
				int nutriScore = Nutrient5C2021Helper.compute5CScore(energyKj, satFat, totalFat, totalSugar, sodium * 1000, percFruitsAndVetgs, nspFibre, aoacFibre, protein, nutrientProfileCategory.toString());
				
				String nutrientClass = Nutrient5C2021Helper.buildNutrientClass((double) nutriScore, ranges, NUTRIENT_PROFILE_CLASSES);
				
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
