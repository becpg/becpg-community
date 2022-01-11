package fr.becpg.repo.product.formulation.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
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
				
				Map<Double, double[]> map = new HashMap<>();
				
				sodium = sodium * 1000;
				
				int nutriScore = Nutrient5C2021Helper.compute5CScore(energyKj, satFat, totalFat, totalSugar, sodium, percFruitsAndVetgs, nspFibre, aoacFibre, protein, nutrientProfileCategory.toString(), map);
				
				double[] minMax = new double[2];
				
				String nutrientClass = Nutrient5C2021Helper.buildNutrientClass((double) nutriScore, ranges, NUTRIENT_PROFILE_CLASSES, minMax);
				
				productData.setNutrientScore((double) nutriScore);
				productData.setNutrientClass(nutrientClass);
				
				JSONObject nutrientScoreDetails = new JSONObject();
				
				 String prettyScore = formatPrettyScore(energyKjNode, satFatNode, totalFatNode, totalSugarNode, sodiumNode,
						percFruitsAndVetgsNode, nspFibreNode, aoacFibreNode, proteinNode, energyKj, satFat, totalFat,
						totalSugar, sodium, percFruitsAndVetgs, nspFibre, aoacFibre, protein, map, nutriScore, minMax,
						nutrientClass);
				
				nutrientScoreDetails.put("class", nutrientClass);
				nutrientScoreDetails.put("prettyScore", prettyScore);
				
				productData.setNutrientDetails(nutrientScoreDetails.toString());
				
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
			productData.setNutrientDetails(null);
		}
		
		return true;
	}

	private String formatPrettyScore(NodeRef energyKjNode, NodeRef satFatNode, NodeRef totalFatNode,
			NodeRef totalSugarNode, NodeRef sodiumNode, NodeRef percFruitsAndVetgsNode, NodeRef nspFibreNode,
			NodeRef aoacFibreNode, NodeRef proteinNode, Double energyKj, Double satFat, Double totalFat,
			Double totalSugar, Double sodium, Double percFruitsAndVetgs, Double nspFibre, Double aoacFibre,
			Double protein, Map<Double, double[]> map, int nutriScore, double[] minMax, String nutrientClass) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(I18NUtil.getMessage("nutriscore.positive") + "\n");
		sb.append(formatDetails(energyKj, energyKjNode, map));
		sb.append(formatDetails(satFat, satFatNode, map));
		sb.append(formatDetails(totalFat, totalFatNode, map));
		sb.append(formatDetails(totalSugar, totalSugarNode, map));
		sb.append(formatDetails(sodium, sodiumNode, map));
		sb.append("\n" + I18NUtil.getMessage("nutriscore.negative") + "\n");
		sb.append(formatDetails(protein, proteinNode, map));
		sb.append(formatDetails(percFruitsAndVetgs, percFruitsAndVetgsNode, map));
		sb.append(formatDetails(nspFibre, nspFibreNode, map));
		sb.append(formatDetails(aoacFibre, aoacFibreNode, map));

		double positiveScore = 0;
		if (map.get(energyKj) != null) {
			positiveScore += map.get(energyKj)[0];
		}
		if (map.get(satFat) != null) {
			positiveScore += map.get(satFat)[0];
		}
		if (map.get(totalFat) != null) {
			positiveScore += map.get(totalFat)[0];
		}
		if (map.get(totalSugar) != null) {
			positiveScore += map.get(totalSugar)[0];
		}
		if (map.get(sodium) != null) {
			positiveScore += map.get(sodium)[0];
		}

		double negativeScore = 0;
		if (map.get(protein) != null) {
			negativeScore += map.get(protein)[0];
		}
		if (map.get(percFruitsAndVetgs) != null) {
			negativeScore += map.get(percFruitsAndVetgs)[0];
		}
		if (map.get(nspFibre) != null) {
			negativeScore += map.get(nspFibre)[0];
		}
		if (map.get(aoacFibre) != null) {
			negativeScore += map.get(aoacFibre)[0];
		}

		sb.append("\n" + I18NUtil.getMessage("nutriscore.finalScore", positiveScore, negativeScore, nutriScore));
		sb.append("\n" + I18NUtil.getMessage("nutriscore.category", minMax[0] == Integer.MIN_VALUE ? "-Inf" : minMax[0], nutriScore,
				minMax[1] == Integer.MAX_VALUE ? "Inf" : minMax[1], nutrientClass));
		return sb.toString();
	}
	
	private String formatDetails(Double value, NodeRef node, Map<Double, double[]> map) {
		
		String name = (String) nodeService.getProperty(node, BeCPGModel.PROP_CHARACT_NAME);
		
		double actualValue = map.get(value).length == 4 ? map.get(value)[3] : value;
		
		Object upperValue = (map.get(value)[2] == Integer.MAX_VALUE) ? "Inf" : map.get(value)[2];
		
		return (map.get(value) == null) ? "" : name + " (" + map.get(value)[1] + " < " + actualValue + " <= " + upperValue + ") = " + (int) map.get(value)[0] + "\n";
	}
	
}
