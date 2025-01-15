package fr.becpg.repo.product.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Helper to compute 5C Nutrient Profile Score
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class Nutrient5C2023Helper implements InitializingBean, NutrientRegulatoryPlugin {
	
	private static Nutrient5C2023Helper INSTANCE = null;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private static final double[] sugarsRange = { 51d, 48d, 44d, 41d, 37d, 34d, 31d, 27d, 24d, 20d, 17d, 14d, 10d, 6.8d, 3.4d };
	private static final double[] beveragesSugarsRange = { 11d, 10d, 9d, 8d, 7d, 6d, 5d, 3.5d, 2d, 0.5d };
	private static final double[] saltRange = { 4d, 3.8d, 3.6d, 3.4d, 3.2d, 3.0d, 2.8d, 2.6, 2.4, 2.2, 2.0, 1.8, 1.6, 1.4, 1.2, 1.0, 0.8, 0.6, 0.4, 0.2};
	private static final double[] fiberRange = { 7.4d, 6.3d, 5.2d, 4.1d, 3d };
	private static final double[] nspFiberRange = { 3.5d, 2.8d, 2.1d, 1.4d, 0.7d };
	private static final double[] proteinRange = { 17d, 14d, 12d, 9.6d, 7.2d, 4.8d, 2.4d };
	private static final double[] beveragesProteinRange = { 3.0d, 2.7d, 2.4d, 2.1d, 1.8d, 1.5d, 1.2d };
	private static final double[] fruitVegetableRange = { -1d, -1d, -1d, -1d, -1d, 80d, -1d, -1d, 60d, 40d };
	private static final double[] beveragesFruitVegetableRange = { 80d, -1d, 60d, -1d, 40d, -1d };
	private static final double[] fatsRange = { 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d };
	private static final double[] fatsFatsRange = { 64d, 58d, 52d, 46d, 40d, 34d, 28d, 22d, 16d, 10d };
	private static final double[] energyRange = { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d };
	private static final double[] beveragesEnergyRange = { 390d, 360d, 330d, 300d, 270d, 240d, 210d, 150d, 90d, 30d };
	private static final double[] fatsEnergyRange = { 1200d, 1080d, 960d, 840d, 720d, 600d, 480d, 360d, 240d, 120d };
	
	private static final double[][] othersACategories = new double[][] { energyRange, fatsRange, sugarsRange, saltRange };
	private static final double[][] cheeseACategories = new double[][] { energyRange, fatsRange, sugarsRange, saltRange };
	private static final double[][] fatsACategories = new double[][] { fatsEnergyRange, fatsFatsRange, sugarsRange, saltRange };
	private static final double[][] beveragesACategories = new double[][] { beveragesEnergyRange, fatsRange, beveragesSugarsRange, saltRange };

	private static final double[][] othersCCategories = new double[][] { fruitVegetableRange, nspFiberRange, fiberRange, proteinRange };
	private static final double[][] cheeseCCategories = new double[][] { fruitVegetableRange, nspFiberRange, fiberRange, proteinRange };
	private static final double[][] fatsCCategories = new double[][] { fruitVegetableRange, nspFiberRange, fiberRange, proteinRange };
	private static final double[][] beveragesCCategories = new double[][] { beveragesFruitVegetableRange, nspFiberRange, fiberRange, beveragesProteinRange };


	private static final List<Double> OTHERS_RANGES = Arrays.asList(18d, 10d, 2d, 0d);
	private static final List<Double> CHEESES_RANGES = OTHERS_RANGES;
	private static final List<Double> FATS_RANGES = Arrays.asList(18d, 10d, 2d, -6d);
	private static final List<Double> BEVERAGES_RANGES = Arrays.asList(9d, 6d, 2d, 0d);
	
	private static final List<String> NON_NUTRITIVE_SUGARS = List.of( "E950", "E951", "E952", "E954", "E955", "E957", "E959", "E960a", "E961", "E962", "E969" );
			
	private Nutrient5C2023Helper() {
		
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;
	}
	
	

	private static double[][] getACategory(NutrientProfileCategory category) {
		switch (category) {
		case Fats:
			return fatsACategories;
		case Beverages:
			return beveragesACategories;
		case Cheeses:
			return cheeseACategories;
		default:
			return othersACategories;
		}
	}

	private static double[][] getCCategory(NutrientProfileCategory category) {
		switch (category) {
		case Fats:
			return fatsCCategories;
		case Beverages:
			return beveragesCCategories;
		case Cheeses:
			return cheeseCCategories;
		default:
			return othersCCategories;
		}
	}
	
	@Override
	public NutriScoreContext buildContext(ProductData productData) throws JSONException {
		return buildNutriScoreContext(productData);
	}
	
	@Override
	public String extractClass(NutriScoreContext context) {
		return extractNutrientClass(context);
	}
	
	@Override
	public Double computeScore(NutriScoreContext context) throws JSONException {
		return (double) compute5CScore(context);
	}
	
	public static String extractClass(ProductData productData) throws JSONException {
		NutriScoreContext context = buildNutriScoreContext(productData);
		if (context != null) {
			compute5CScore(context);
			return extractNutrientClass(context);
		}
		throw new IllegalStateException("Product is not applicable for nutriscore: " + productData.getName());
	}
	
	public static int computeScore(ProductData productData) throws JSONException {
		NutriScoreContext context = buildNutriScoreContext(productData);
		if (context != null) {
			return compute5CScore(context);
		}
		throw new IllegalStateException("Product is not applicable for nutriscore: " + productData.getName());
	}
	
	public static NutriScoreContext buildNutriScoreContext(ProductData productData) throws JSONException {
		NutriScoreContext context = NutrientHelper.buildNutriScoreContext(productData, INSTANCE.alfrescoRepository, INSTANCE.nodeService);
		if (context != null) {
			context.setDisplaySaltScore(true);
			for (IngListDataItem ing : productData.getIngList()) {
				String ceeCode = (String) INSTANCE.nodeService.getProperty(ing.getIng(), PLMModel.PROP_ING_CEECODE);
				if (NutrientProfileCategory.Beverages.toString().equals(context.getCategory()) && isNonNutritiveSugar(ceeCode)) {
					context.getNonNutritiveSugars().add((String) INSTANCE.nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_CHARACT_NAME));
				}
			}
		}
		return context;
	}
	
	private static boolean isNonNutritiveSugar(String ceeCode) {
		if (ceeCode == null) {
			return false;
		}
		for (String nonNutritiveSugar : NON_NUTRITIVE_SUGARS) {
			if (ceeCode.startsWith(nonNutritiveSugar)) {
				return true;
			}
		}
		return false;
	}
	
	public static int compute5CScore(NutriScoreContext nutriScoreContext) throws JSONException {

		String category = nutriScoreContext.getCategory();

		int aScore = 0;
		int cScore = 0;

//		les arrondis sont déja effectués

		double[][] aCategories = getACategory(NutrientProfileCategory.valueOf(category));
		double[][] cCategories = getCCategory(NutrientProfileCategory.valueOf(category));

		if (NutrientProfileCategory.Fats.equals(NutrientProfileCategory.valueOf(category))) {

			double satFat = nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SATFAT_CODE).getDouble(NutriScoreContext.VALUE);
			
			double energyFats = satFat * 37;
			nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).put(NutriScoreContext.VALUE, energyFats);
			
			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE), aCategories[0]);
			aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).getDouble(NutriScoreContext.SCORE);
			
			double totalFat = nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE).getDouble(NutriScoreContext.VALUE);
			if (totalFat != 0) {
				nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE).put(NutriScoreContext.VALUE, (satFat / totalFat) * 100);
			}
			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE), aCategories[1]);
			aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE).getDouble(NutriScoreContext.SCORE);

		} else {
			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE), aCategories[0]);
			aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).getDouble(NutriScoreContext.SCORE);
			
			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SATFAT_CODE), aCategories[1]);
			aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SATFAT_CODE).getDouble(NutriScoreContext.SCORE);
		}

		
		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE), aCategories[2]);
		
		aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE);
		
		JSONObject sodiumPart = nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SODIUM_CODE);
		
		if (sodiumPart.has(NutriScoreContext.VALUE)) {
			double saltValue = sodiumPart.getDouble(NutriScoreContext.VALUE) * 2.5 / 1000;
			sodiumPart.put(NutriScoreContext.VALUE, saltValue);
		}
		
		NutrientHelper.buildNutriScorePart(sodiumPart, aCategories[3]);
		
		aScore += sodiumPart.getDouble(NutriScoreContext.SCORE);

		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE), cCategories[0], true);
		
		cScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE);

		boolean addProteins = false;
		
		if (NutrientProfileCategory.valueOf(category) == NutrientProfileCategory.Fats) {
			addProteins = aScore < 7;
		} else if (NutrientProfileCategory.valueOf(category) == NutrientProfileCategory.Cheeses) {
			addProteins = true;
		} else if (NutrientProfileCategory.valueOf(category) == NutrientProfileCategory.Beverages) {
			addProteins = true;
		} else {
			addProteins = aScore < 11;
		}
		
		if (addProteins) {
			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE), cCategories[3]);
			double proteinScore = nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).getDouble(NutriScoreContext.SCORE);
			if (NutrientProfileCategory.valueOf(category) == NutrientProfileCategory.RedMeats) {
				proteinScore = Math.min(proteinScore, 2);
				nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).put(NutriScoreContext.SCORE, proteinScore);
				nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).put(NutriScoreContext.UPPER_VALUE, "+Inf");
			}
			cScore += proteinScore;
			nutriScoreContext.setHasProteinScore(true);
		}

		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.NSP_CODE), cCategories[1]);
		
		cScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.NSP_CODE).getDouble(NutriScoreContext.SCORE);

		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.AOAC_CODE), cCategories[2]);
		
		cScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.AOAC_CODE).getDouble(NutriScoreContext.SCORE);

		if (!nutriScoreContext.getNonNutritiveSugars().isEmpty()) {
			aScore += 4;
		}
		
		int result = aScore - cScore;

		nutriScoreContext.setNutriScore(result);
		nutriScoreContext.setAScore(aScore);
		nutriScoreContext.setCScore(cScore);

		return nutriScoreContext.getNutriScore();
	}

	public static String extractNutrientClass(NutriScoreContext nutriScoreContext) {

		List<Double> ranges = new ArrayList<>();

		if (NutrientProfileCategory.Beverages.toString().equals(nutriScoreContext.getCategory())) {
			ranges = BEVERAGES_RANGES;
		} else if (NutrientProfileCategory.Cheeses.toString().equals(nutriScoreContext.getCategory())) {
			ranges = CHEESES_RANGES;
		} else if (NutrientProfileCategory.Fats.toString().equals(nutriScoreContext.getCategory())) {
			ranges = FATS_RANGES;
		} else if (NutrientProfileCategory.Others.toString().equals(nutriScoreContext.getCategory())) {
			ranges = OTHERS_RANGES;
		} else if (NutrientProfileCategory.RedMeats.toString().equals(nutriScoreContext.getCategory())) {
			ranges = OTHERS_RANGES;
		}

		double score = nutriScoreContext.getNutriScore();

		Double lower = Double.NEGATIVE_INFINITY;
		Double upper = Double.POSITIVE_INFINITY;

		for (int i = 0; i < ranges.size(); i++) {
			lower = ranges.get(i);
			if (score > ranges.get(i)) {
				nutriScoreContext.setClassLowerValue(lower == Double.NEGATIVE_INFINITY ? "-Inf" : lower.toString());
				nutriScoreContext.setClassUpperValue(upper == Double.POSITIVE_INFINITY ? "+Inf" : upper.toString());
				nutriScoreContext.setNutrientClass(NutriScoreContext.NUTRIENT_PROFILE_CLASSES.get(i));
				return nutriScoreContext.getNutrientClass();
			}
			upper = ranges.get(i);
		}

		if (lower.equals(upper)) {
			lower = Double.NEGATIVE_INFINITY;
		}

		nutriScoreContext.setClassLowerValue(lower == Double.NEGATIVE_INFINITY ? "-Inf" : lower.toString());
		nutriScoreContext.setClassUpperValue(upper == Double.POSITIVE_INFINITY ? "+Inf" : upper.toString());
		nutriScoreContext.setNutrientClass(NutriScoreContext.NUTRIENT_PROFILE_CLASSES.get(NutriScoreContext.NUTRIENT_PROFILE_CLASSES.size() - 1));

		// case of beverages : never get "A" class except for water
		if (NutrientProfileCategory.Beverages.toString().equals(nutriScoreContext.getCategory())
				&& "A".equals(nutriScoreContext.getNutrientClass()) && !nutriScoreContext.isWater()) {
			nutriScoreContext.setNutrientClass("B");
		}
		
		return nutriScoreContext.getNutrientClass();
	}

}
