package fr.becpg.repo.product.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.repo.product.formulation.score.NutriScoreContext;
import fr.becpg.repo.product.formulation.score.NutriScoreFrame;

/**
 * Helper to compute 5C Nutrient Profile Score
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Nutrient5C2021Helper {
	
	private static final List<String> NUTRIENT_PROFILE_CLASSES = Arrays.asList("E","D","C","B","A");

	private static final double[][] othersACategories = new double[][] { { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d },
			{ 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d }, { 45d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d },
			{ 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d } };

	private static final double[][] othersCCategories = new double[][] { { -1d, -1d, -1d, -1d, -1d, 80d, -1d, -1d, 60d, 40d },
			{ 3.5d, 2.8d, 2.1d, 1.4d, 0.7d }, { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d }, { 8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static final double[][] cheeseACategories = new double[][] { { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d },
			{ 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d }, { 45d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d },
			{ 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d } };

	private static final double[][] cheeseCCategories = new double[][] { { -1d, -1d, -1d, -1d, -1d, 80d, -1d, -1d, 60d, 40d },
			{ 3.5d, 2.8d, 2.1d, 1.4d, 0.7d }, { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d }, { 8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static final double[][] fatsACategories = new double[][] { { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d },
			{ 64d, 58d, 52d, 46d, 40d, 34d, 28d, 22d, 16d, 10d }, { 45d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d },
			{ 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d } };

	private static final double[][] fatsCCategories = new double[][] { { -1d, -1d, -1d, -1d, -1d, 80d, -1d, -1d, 60d, 40d },
			{ 3.5d, 2.8d, 2.1d, 1.4d, 0.7d }, { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d }, { 8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static final double[][] beveragesACategories = new double[][] { { 270d, 240d, 210d, 180d, 150d, 120d, 90d, 60d, 30d, 0d },
			{ 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d }, { 13.5d, 12d, 10.5d, 9d, 7.5d, 6d, 4.5d, 3d, 1.5d, 0d },
			{ 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d } };

	private static final double[][] beveragesCCategories = new double[][] { { 80d, -1d, -1d, -1d, -1d, -1d, 60d, -1d, 40d, -1d },
			{ 3.5d, 2.8d, 2.1d, 1.4d, 0.7d }, { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d }, { 8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static final List<Double> BEVERAGES_RANGES = Arrays.asList(9d, 5d, 1d, 0d);
	private static final List<Double> CHEESES_RANGES = Arrays.asList(18d, 10d, 2d, -1d);
	private static final List<Double> FATS_RANGES = CHEESES_RANGES;
	private static final List<Double> OTHERS_RANGES = CHEESES_RANGES;
			
	private Nutrient5C2021Helper() {
		
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

	private static void buildNutriScoreFrame(NutriScoreFrame frame, int score, double[] categories) {
		
		Double value = frame.getValue();
		
		if (value != null) {
			double lower = 0;
			double upper = Double.MAX_VALUE;
			
			for (double val : categories) {
				
				lower = val;
				
				if ((value > val) && (val > 0)) {
					break;
				}
				
				if (val > 0) {
					upper = val;
				}
				
				score--;
			}
			
			if (lower == upper) {
				lower = 0;
			}
			
			frame.setLowerValue(lower);
			frame.setUpperValue(upper);
			frame.setScore(score);
		}
	}
	
	public static int build5CScore(NutriScoreContext nutriScoreContext) {

		String category = nutriScoreContext.getCategory();

		int aScore = 0;
		int cScore = 0;

//		les arrondis sont déja effectués

		double[][] aCategories = getACategory(NutrientProfileCategory.valueOf(category));
		double[][] cCategories = getCCategory(NutrientProfileCategory.valueOf(category));

		buildNutriScoreFrame(nutriScoreContext.getEnergy(), 10, aCategories[0]);
		aScore += nutriScoreContext.getEnergy().getScore();

		if (NutrientProfileCategory.Fats.equals(NutrientProfileCategory.valueOf(category))) {

			double satFat = nutriScoreContext.getSatFat().getValue();
			double totalFat = nutriScoreContext.getTotalFat().getValue();

			if (totalFat != 0) {
				nutriScoreContext.getTotalFat().setValue((satFat / totalFat) * 100);
			}

			buildNutriScoreFrame(nutriScoreContext.getTotalFat(), 10, aCategories[1]);

			aScore += nutriScoreContext.getTotalFat().getScore();
		} else {
			buildNutriScoreFrame(nutriScoreContext.getSatFat(), 10, aCategories[1]);
			aScore += nutriScoreContext.getSatFat().getScore();
		}

		buildNutriScoreFrame(nutriScoreContext.getTotalSugar(), 10, aCategories[2]);

		aScore += nutriScoreContext.getTotalSugar().getScore();

		buildNutriScoreFrame(nutriScoreContext.getSodium(), 10, aCategories[3]);

		aScore += nutriScoreContext.getSodium().getScore();

		buildNutriScoreFrame(nutriScoreContext.getPercFruitsAndVetgs(), 10, cCategories[0]);

		cScore += nutriScoreContext.getPercFruitsAndVetgs().getScore();

		if ((aScore < 11) || NutrientProfileCategory.Cheeses.equals(NutrientProfileCategory.valueOf(category))
				|| ((aScore >= 11) && (cScore >= 5)
						&& !NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))
				|| ((aScore >= 11) && (cScore >= 10)
						&& NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))) {

			buildNutriScoreFrame(nutriScoreContext.getProtein(), 5, cCategories[3]);

			cScore += nutriScoreContext.getProtein().getScore();
		}

		buildNutriScoreFrame(nutriScoreContext.getNspFibre(), 5, cCategories[1]);

		cScore += nutriScoreContext.getNspFibre().getScore();

		buildNutriScoreFrame(nutriScoreContext.getAoacFibre(), 5, cCategories[2]);

		cScore += nutriScoreContext.getAoacFibre().getScore();

		int result = aScore - cScore;

		nutriScoreContext.setNutriScore(result);
		nutriScoreContext.setAScore(aScore);
		nutriScoreContext.setCScore(cScore);

		return nutriScoreContext.getNutriScore();
	}

		public static String buildNutrientClass(NutriScoreContext nutriScoreContext) {

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

			double lower = Double.MIN_VALUE;
			double upper = Double.MAX_VALUE;

			for (int i = 0; i < ranges.size(); i++) {
				lower = ranges.get(i);
				if (score > ranges.get(i)) {
					nutriScoreContext.setClassLowerValue(lower);
					nutriScoreContext.setClassUpperValue(upper);
					nutriScoreContext.setNutrientClass(NUTRIENT_PROFILE_CLASSES.get(i));
					return nutriScoreContext.getNutrientClass();
				}
				upper = ranges.get(i);
			}

			if (lower == upper) {
				lower = Double.MIN_VALUE;
			}

			nutriScoreContext.setClassLowerValue(lower);
			nutriScoreContext.setClassUpperValue(upper);
			nutriScoreContext.setNutrientClass(NUTRIENT_PROFILE_CLASSES.get(NUTRIENT_PROFILE_CLASSES.size() - 1));

			// case of beverages : never get "A" class except for water
			if (NutrientProfileCategory.Beverages.toString().equals(nutriScoreContext.getCategory())
					&& "A".equals(nutriScoreContext.getNutrientClass()) && !nutriScoreContext.isWater()) {
				nutriScoreContext.setNutrientClass("B");
			}
			
			return nutriScoreContext.getNutrientClass();
		}

}
