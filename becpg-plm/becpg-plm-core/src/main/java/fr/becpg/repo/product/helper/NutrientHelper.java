package fr.becpg.repo.product.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONObject;

import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Helper to compute OfCom Nutrient Profile Score
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutrientHelper {

	/**
	 * <p>ofComNutrientAScore.</p>
	 *
	 * @param energyKj
	 *            (kJ)
	 * @param satFat
	 *            (g)
	 * @param totalSugar
	 *            (g)
	 * @param sodium
	 *            (mg)
	 * @return a int.
	 */
	public static int ofComNutrientAScore(Double energyKj, Double satFat, Double totalSugar, Double sodium) {

		int aScore = 0;

		int score = 10;

		if (energyKj != null) {

			for (double val : new double[] { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d }) {
				if (energyKj > val) {
					break;
				}
				score--;
			}
			aScore += score;
		}

		if (satFat != null) {
			score = 10;
			for (double val : new double[] { 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d }) {
				if (satFat > val) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (totalSugar != null) {
			score = 10;
			for (double val : new double[] { 45d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d }) {
				if (totalSugar > val) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (sodium != null) {
			score = 10;
			for (double val : new double[] { 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d }) {
				if (sodium > val) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		return aScore;

	}

	/**
	 * <p>ofComNutrientCScore.</p>
	 *
	 * @param percFruitsAndVetgs
	 *            Fruit, Veg and Nuts (%)
	 * @param fibre
	 *            AOAC Fibre (g)
	 * @param protein
	 *            Protein (g)
	 * @return a int.
	 */
	public static int ofComNutrientCScore(Double percFruitsAndVetgs, Double fibre, Double protein) {
		int bScore = 0;

		int score = 5;

		if (percFruitsAndVetgs != null) {

			for (double val : new double[] { 80d, 80d, 80d, 60d, 40d }) {
				if (percFruitsAndVetgs > val) {
					break;
				}
				score--;
			}
			bScore += score;
		}

		if (fibre != null) {

			for (double val : new double[] { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d }) {
				if (fibre > val) {
					break;
				}
				score--;
			}
			bScore += score;
		}

		if (protein != null) {

			for (double val : new double[] { 8.0d, 6.4d, 4.8d, 3.2d, 1.6d }) {
				if (protein > val) {
					break;
				}
				score--;
			}
			bScore += score;
		}

		return bScore;
	}

	/**
	 * <p>buildNutrientClass.</p>
	 *
	 * @param score a {@link java.lang.Double} object.
	 * @param ranges a {@link java.util.List} object.
	 * @param clazz a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String buildNutrientClass(Double score, List<Double> ranges, List<String> clazz) {
		if (score != null) {
			for (int i = 0; i < ranges.size(); i++) {
				if (score > ranges.get(i)) {
					return clazz.get(i);
				}
			}
			return clazz.get(clazz.size() - 1);
		}
		return null;

	}

	/**
	 * <p>buildNutriScoreContext.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @return a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 */
	public static NutriScoreContext buildNutriScoreContext(ProductData productData, AlfrescoRepository<RepositoryEntity> alfrescoRepository,
			NodeService nodeService) {

		String nutrientProfileCategory = productData.getNutrientProfileCategory();

		boolean isApplicable = nutrientProfileCategory != null && !nutrientProfileCategory.isBlank()
				&& !NutrientProfileCategory.NonApplicable.equals(NutrientProfileCategory.valueOf(nutrientProfileCategory));

		if (isApplicable) {
			NutriScoreContext nutriScoreContext = new NutriScoreContext();
			nutriScoreContext.setCategory(nutrientProfileCategory);
			nutriScoreContext.setVersion(productData.getNutrientProfileVersion());
			boolean containsWaterAspect = productData.getAspects().contains(PLMModel.ASPECT_WATER)
					|| (productData.getAspects().contains(PLMModel.ASPECT_EVAPORABLE) && productData.getNodeRef() != null
							&& (Double) nodeService.getProperty(productData.getNodeRef(), PLMModel.PROP_EVAPORATED_RATE)!=null 
							&& (Double) nodeService.getProperty(productData.getNodeRef(), PLMModel.PROP_EVAPORATED_RATE) == 100d);
			nutriScoreContext.setWater(containsWaterAspect);

			Map<String, NodeRef> missingCharacts = visitCharactLists(productData, nutriScoreContext, alfrescoRepository, nodeService);

			if (!missingCharacts.isEmpty()) {
				productData.getReqCtrlList()
						.add(ReqCtrlListDataItem.forbidden().withMessage(MLTextHelper.getI18NMessage("nutriscore.message.missingCharacts"))
								.ofDataType(RequirementDataType.Nutrient).withSources(new ArrayList<>(missingCharacts.values())));
			}

			return nutriScoreContext;
		}

		return null;
	}

	private static Map<String, NodeRef> visitCharactLists(ProductData productData, NutriScoreContext nutriScoreContext,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository, NodeService nodeService) {
		Map<String, NodeRef> missingCharacts = new HashMap<>();
		visitNutrientList(productData, nutriScoreContext, missingCharacts, alfrescoRepository);
		visitPhysicoChemList(productData, nutriScoreContext, missingCharacts, nodeService);
		if (missingCharacts.containsKey(NutriScoreContext.NSP_CODE) && !missingCharacts.containsKey(NutriScoreContext.AOAC_CODE)) {
			missingCharacts.remove(NutriScoreContext.NSP_CODE);
		}
		if (missingCharacts.containsKey(NutriScoreContext.AOAC_CODE) && !missingCharacts.containsKey(NutriScoreContext.NSP_CODE)) {
			missingCharacts.remove(NutriScoreContext.AOAC_CODE);
		}
		return missingCharacts;
	}

	private static void visitNutrientList(ProductData productData, NutriScoreContext nutriScoreContext, Map<String, NodeRef> missingCharacts,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository) {

		boolean hasSalt = false;

		for (String nutrientCode : NutriScoreContext.NUTRIENT_CODE_LIST) {
			// do not set sodium because salt is already set
			if (NutriScoreContext.SODIUM_CODE.equals(nutrientCode) && hasSalt) {
				continue;
			}

			NutListDataItem nutListItem = findNutrient(productData, nutrientCode, missingCharacts, alfrescoRepository);

			if (nutListItem != null) {
				Double value = productData.isPrepared() && nutListItem.preparedValue("EU") != null ? nutListItem.preparedValue("EU")
						: nutListItem.value("EU");

				if (value == null) {
					missingCharacts.put(nutrientCode, nutListItem.getNut());
					continue;
				}

				JSONObject nutrientPart = new JSONObject();

				nutrientPart.put(NutriScoreContext.VALUE, value);

				// specific case of Salt/Sodium
				if (NutriScoreContext.SALT_CODE.equals(nutrientCode)) {
					nutrientCode = NutriScoreContext.SODIUM_CODE;
					nutrientPart.put(NutriScoreContext.VALUE, value * 1000 / 2.5);
					hasSalt = true;
				} else if (NutriScoreContext.SODIUM_CODE.equals(nutrientCode)) {
					nutrientPart.put(NutriScoreContext.VALUE, value * 1000);
				}

				nutriScoreContext.getParts().put(nutrientCode, nutrientPart);
			}
		}
	}

	private static void visitPhysicoChemList(ProductData productData, NutriScoreContext nutriScoreContext, Map<String, NodeRef> missingCharacts,
			NodeService nodeService) {
		for (String physicoCode : NutriScoreContext.PHYSICO_CODE_LIST) {
			PhysicoChemListDataItem physicoListItem = findPhysico(productData, physicoCode, missingCharacts, nodeService);

			if (physicoListItem != null) {
				Double value = physicoListItem.getValue();

				if (value == null) {
					missingCharacts.put(physicoCode, physicoListItem.getPhysicoChem());
					continue;
				}

				JSONObject nutrientPart = new JSONObject();

				nutrientPart.put(NutriScoreContext.VALUE, value);

				nutriScoreContext.getParts().put(physicoCode, nutrientPart);
			}
		}
	}

	private static PhysicoChemListDataItem findPhysico(ProductData productData, String physicoCode, Map<String, NodeRef> missingCharacts,
			NodeService nodeService) {
		for (PhysicoChemListDataItem physico : productData.getPhysicoChemList()) {

			if (physicoCode.equals(nodeService.getProperty(physico.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_CODE))) {

				for (ReqCtrlListDataItem reqCtrl : productData.getReqCtrlList()) {
					if (RequirementType.Forbidden.equals(reqCtrl.getReqType()) && RequirementDataType.Physicochem.equals(reqCtrl.getReqDataType())
							&& physico.getPhysicoChem().equals(reqCtrl.getCharact())) {
						missingCharacts.put(physicoCode, reqCtrl.getCharact());
						break;
					}
				}

				return physico;
			}
		}

		return null;
	}

	private static NutListDataItem findNutrient(ProductData productData, String nutrientCode, Map<String, NodeRef> missingCharacts,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		for (NutListDataItem nutList : productData.getNutList()) {

			NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(nutList.getNut());

			if (nutrientCode.equals(nut.getNutCode())) {

				for (ReqCtrlListDataItem reqCtrl : productData.getReqCtrlList()) {
					if (RequirementType.Forbidden.equals(reqCtrl.getReqType()) && RequirementDataType.Nutrient.equals(reqCtrl.getReqDataType())
							&& nutList.getNut().equals(reqCtrl.getCharact())) {
						missingCharacts.put(nutrientCode, reqCtrl.getCharact());
						break;
					}
				}

				return nutList;
			}
		}

		return null;
	}

	/**
	 * <p>buildNutriScorePart.</p>
	 *
	 * @param part a {@link org.json.JSONObject} object
	 * @param categories an array of {@link double} objects
	 */
	public static void buildNutriScorePart(JSONObject part, double[] categories) {
		buildNutriScorePart(part, categories, false);
	}

	/**
	 * <p>buildNutriScorePart.</p>
	 *
	 * @param part a {@link org.json.JSONObject} object
	 * @param categories an array of {@link double} objects
	 * @param includeLower a boolean
	 */
	public static void buildNutriScorePart(JSONObject part, double[] categories, boolean includeLower) {

		int score = categories.length;

		Double value = 0d;

		if (part.has(NutriScoreContext.VALUE)) {
			value = part.getDouble(NutriScoreContext.VALUE);
		}

		double lower = 0;
		double upper = Double.POSITIVE_INFINITY;

		for (double threshold : categories) {

			lower = threshold;

			if ((value > threshold || includeLower && value == threshold) && (threshold != -1)) {
				break;
			}

			if (threshold != -1) {
				upper = threshold;
			}

			score--;
		}

		if (lower == upper) {
			lower = Double.NEGATIVE_INFINITY;
		}

		part.put(NutriScoreContext.LOWER_VALUE, lower == Double.NEGATIVE_INFINITY ? "-Inf" : lower);
		part.put(NutriScoreContext.UPPER_VALUE, upper == Double.POSITIVE_INFINITY ? "+Inf" : upper);
		part.put(NutriScoreContext.SCORE, score);
	}

}
