package fr.becpg.repo.product.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.NutrientProfileVersion;
import fr.becpg.repo.product.data.ProductData;
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
public class Nutrient5C2021Helper implements InitializingBean, NutrientRegulatoryPlugin {
	
	private static Nutrient5C2021Helper INSTANCE = null;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private static final double[] sugarsRange = { 45d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d };
	private static final double[] sodiumRange = { 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d };
	private static final double[] fiberRange = { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d };
	private static final double[] nspFiberRange = { 3.5d, 2.8d, 2.1d, 1.4d, 0.7d };
	private static final double[] proteinRange = { 8.0d, 6.4d, 4.8d, 3.2d, 1.6d };
	private static final double[] fruitVegetableRange = { -1d, -1d, -1d, -1d, -1d, 80d, -1d, -1d, 60d, 40d };
	private static final double[] beveragesFruitVegetableRange = { 80d, -1d, -1d, -1d, -1d, -1d, 60d, -1d, 40d, -1d };
	private static final double[] beveragesEnergyRange = { 270d, 240d, 210d, 180d, 150d, 120d, 90d, 60d, 30d, 0d };
	private static final double[] beveragesSugarsRange = { 13.5d, 12d, 10.5d, 9d, 7.5d, 6d, 4.5d, 3d, 1.5d, 0d };
	private static final double[] fatsRange = { 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d };
	private static final double[] fatsFatsRange = { 64d, 58d, 52d, 46d, 40d, 34d, 28d, 22d, 16d, 10d };
	private static final double[] energyRange = { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d };

	private static final double[][] othersACategories = new double[][] { energyRange, fatsRange, sugarsRange, sodiumRange };
	private static final double[][] cheeseACategories = new double[][] { energyRange, fatsRange, sugarsRange, sodiumRange };
	private static final double[][] fatsACategories = new double[][] { energyRange, fatsFatsRange, sugarsRange, sodiumRange };

	private static final double[][] othersCCategories = new double[][] { fruitVegetableRange, nspFiberRange, fiberRange, proteinRange };
	private static final double[][] cheeseCCategories = new double[][] { fruitVegetableRange, nspFiberRange, fiberRange, proteinRange };
	private static final double[][] fatsCCategories = new double[][] { fruitVegetableRange, nspFiberRange, fiberRange, proteinRange };

	private static final double[][] beveragesACategories = new double[][] { beveragesEnergyRange, fatsRange, beveragesSugarsRange, sodiumRange };
	private static final double[][] beveragesCCategories = new double[][] { beveragesFruitVegetableRange, nspFiberRange, fiberRange, proteinRange };

	private static final List<Double> BEVERAGES_RANGES = Arrays.asList(9d, 5d, 1d, 0d);
	private static final List<Double> CHEESES_RANGES = Arrays.asList(18d, 10d, 2d, -1d);
	private static final List<Double> FATS_RANGES = CHEESES_RANGES;
	private static final List<Double> OTHERS_RANGES = CHEESES_RANGES;

	private Nutrient5C2021Helper() {
		
	}
	
	/** {@inheritDoc} */
	@Override
	public String getVersion() {
		return NutrientProfileVersion.VERSION_2017.toString();
	}
	
	/** {@inheritDoc} */
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
	
	/** {@inheritDoc} */
	@Override
	public NutriScoreContext buildContext(ProductData productData) {
		return buildNutriScoreContext(productData);
	}
	
	/** {@inheritDoc} */
	@Override
	public String extractClass(NutriScoreContext context) {
		return extractNutrientClass(context);
	}
	
	/** {@inheritDoc} */
	@Override
	public Double computeScore(NutriScoreContext context) {
		return (double) compute5CScore(context);
	}
	
	/**
	 * <p>extractClass.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.lang.String} object
	 */
	public static String extractClass(ProductData productData) {
		NutriScoreContext context = buildNutriScoreContext(productData);
		if (context != null) {
			compute5CScore(context);
			return extractNutrientClass(context);
		}
		throw new IllegalStateException("Product is not applicable for nutriscore: " + productData.getName());
	}
	
	/**
	 * <p>computeScore.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a int
	 */
	public static int computeScore(ProductData productData) {
		NutriScoreContext context = buildNutriScoreContext(productData);
		if (context != null) {
			return compute5CScore(context);
		}
		throw new IllegalStateException("Product is not applicable for nutriscore: " + productData.getName());
	}
	
	/**
	 * <p>buildNutriScoreContext.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 */
	public static NutriScoreContext buildNutriScoreContext(ProductData productData) {
		return NutrientHelper.buildNutriScoreContext(productData, INSTANCE.alfrescoRepository, INSTANCE.nodeService);
	}

	/**
	 * <p>compute5CScore.</p>
	 *
	 * @param energyKj a {@link java.lang.Double} object
	 * @param satFat a {@link java.lang.Double} object
	 * @param totalFat a {@link java.lang.Double} object
	 * @param totalSugar a {@link java.lang.Double} object
	 * @param sodium a {@link java.lang.Double} object
	 * @param percFruitsAndVetgs a {@link java.lang.Double} object
	 * @param nspFibre a {@link java.lang.Double} object
	 * @param aoacFibre a {@link java.lang.Double} object
	 * @param protein a {@link java.lang.Double} object
	 * @param category a {@link java.lang.String} object
	 * @return a int
	 */
	public static int compute5CScore(Double energyKj, Double satFat, Double totalFat, Double totalSugar, Double sodium, Double percFruitsAndVetgs,
			Double nspFibre, Double aoacFibre, Double protein, String category) {
		
		NutriScoreContext nutriScoreContext = new NutriScoreContext(energyKj, satFat, totalFat, totalSugar, sodium,
				percFruitsAndVetgs, nspFibre, aoacFibre, protein, category);
		
		return compute5CScore(nutriScoreContext);
	}
	
	/**
	 * <p>compute5CScore.</p>
	 *
	 * @param nutriScoreContext a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 * @return a int
	 */
	public static int compute5CScore(NutriScoreContext nutriScoreContext) {

		String category = nutriScoreContext.getCategory();

		int aScore = 0;
		int cScore = 0;

//		les arrondis sont déja effectués

		double[][] aCategories = getACategory(NutrientProfileCategory.valueOf(category));
		double[][] cCategories = getCCategory(NutrientProfileCategory.valueOf(category));

		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE), aCategories[0]);
		aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.ENERGY_CODE).getDouble(NutriScoreContext.SCORE);
		
		if (NutrientProfileCategory.Fats.equals(NutrientProfileCategory.valueOf(category))) {

			double satFat = nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SATFAT_CODE).getDouble(NutriScoreContext.VALUE);
			
			double totalFat = nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE).getDouble(NutriScoreContext.VALUE);
			
			if (totalFat != 0) {
				nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE).put(NutriScoreContext.VALUE, (satFat / totalFat) * 100);
			}
			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE), aCategories[1]);
			
			aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FAT_CODE).getDouble(NutriScoreContext.SCORE);

		} else {
			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SATFAT_CODE), aCategories[1]);
			aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SATFAT_CODE).getDouble(NutriScoreContext.SCORE);
		}

		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE), aCategories[2]);
		aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SUGAR_CODE).getDouble(NutriScoreContext.SCORE);
		
		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SODIUM_CODE), aCategories[3]);
		aScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.SODIUM_CODE).getDouble(NutriScoreContext.SCORE);
		
		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE), cCategories[0], true);
		
		cScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.FRUIT_VEGETABLE_CODE).getDouble(NutriScoreContext.SCORE);

		if ((aScore < 11) || NutrientProfileCategory.Cheeses.equals(NutrientProfileCategory.valueOf(category))
				|| ((aScore >= 11) && (cScore >= 5)
						&& !NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))
				|| ((aScore >= 11) && (cScore >= 10)
						&& NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))) {

			NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE), cCategories[3]);
			
			cScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.PROTEIN_CODE).getDouble(NutriScoreContext.SCORE);
			
			nutriScoreContext.setHasProteinScore(true);
		}

		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.NSP_CODE), cCategories[1]);
		
		cScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.NSP_CODE).getDouble(NutriScoreContext.SCORE);

		NutrientHelper.buildNutriScorePart(nutriScoreContext.getParts().getJSONObject(NutriScoreContext.AOAC_CODE), cCategories[2]);
		
		cScore += nutriScoreContext.getParts().getJSONObject(NutriScoreContext.AOAC_CODE).getDouble(NutriScoreContext.SCORE);

		int result = aScore - cScore;

		nutriScoreContext.setNutriScore(result);
		nutriScoreContext.setAScore(aScore);
		nutriScoreContext.setCScore(cScore);

		return nutriScoreContext.getNutriScore();
	}

		/**
		 * <p>extractNutrientClass.</p>
		 *
		 * @param nutriScoreContext a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
		 * @return a {@link java.lang.String} object
		 */
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
