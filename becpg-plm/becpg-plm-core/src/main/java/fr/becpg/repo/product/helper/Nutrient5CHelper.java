package fr.becpg.repo.product.helper;

import java.util.List;

/**
 * Helper to compute 5C Nutrient Profile Score
 *
 * @author matthieu
 *
 */
public class Nutrient5CHelper {

	enum NutrientCategory {
		Cheeses, Fats, Beverages, Others
	}

	private static final double[][] othersACategories = new double[][] { 
		    { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d },
			{ 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d },
			{ 45d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d },
			{ 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d }
		};

	private static final double[][] othersCCategories = new double[][] { 
		    { 80d, 80d, 80d , 80d, 80d, 80d, 80d, 80d, 60d, 40d },
		    { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d },
			{ 8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static final double[][] cheeseACategories = new double[][] { 
		    { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d },
			{ 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d }, { 45d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d},
			{ 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d } };

	private static final double[][] cheeseCCategories = new double[][] { { 80d, 80d, 80d , 80d, 80d, 80d, 80d, 80d, 60d, 40d }, {4.7d, 3.7d, 2.8d, 1.9d, 0.9d },
			{8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static final double[][] fatsACategories = new double[][] { { 3350d, 3015d, 2680d, 2345d, 2010d, 1675d, 1340d, 1005d, 670d, 335d },
			{ 63d, 57d, 51d, 45d, 39d, 33d, 27d, 21d, 15d, 9d }, { 5d, 40d, 36d, 31d, 27d, 22.5d, 18d, 13.5d, 9d, 4.5d},
			{  900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d } };

	private static final double[][] fatsCCategories = new double[][] { 
		    { 80d, 80d, 80d , 80d, 80d, 80d, 80d, 80d, 60d, 40d },
		    { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d },
			{ 8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static final double[][] beveragesACategories = new double[][] { 
		    { 270d, 270d, 240d, 210d, 180d, 150d, 120d, 90d, 60d, 30d },
			{ 10d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d }, 
			{ 13.5d, 13.5d, 12d, 10.5d, 9d, 7.5d, 6d, 4.5d, 3d, 1.5d },
			{ 900d, 810d, 720d, 630d, 540d, 450d, 360d, 270d, 180d, 90d} };

	private static final double[][] beveragesCCategories = new double[][] { 
		    { 80d, 60d, 60d, 60d, 60d, 60d, 60d, 40d, 40d, 39d  },
		    { 4.7d, 3.7d, 2.8d, 1.9d, 0.9d  },
			{8.0d, 6.4d, 4.8d, 3.2d, 1.6d } };

	private static double[][] getACategory(NutrientCategory category) {
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

	private static double[][] getCCategory(NutrientCategory category) {
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

	/**
	 *
	 * @param energy
	 *            (kJ)
	 * @param satFat
	 *            (g)
	 * @param totalSugar
	 *            (g)
	 * @param sodium
	 *            (mg)
	 * @return
	 */
	public static int compute5CScore(Double energyKj, Double satFat, Double totalSugar, Double sodium, Double percFruitsAndVetgs, Double fibre,
			Double protein, String category) {

		int aScore = 0;
		int cScore = 0;
		int score = 10;

		double[][] aCategories = getACategory(NutrientCategory.valueOf(category));
		double[][] cCategories = getCCategory(NutrientCategory.valueOf(category));

		if (energyKj != null) {

			for (double val : aCategories[0]) {
				if (energyKj > val) {
					break;
				}
				score--;
			}
			aScore += score;
		}

		if (satFat != null) {
			score = 10;
			for (double val : aCategories[1]) {
				if (satFat > val) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (totalSugar != null) {
			score = 10;
			for (double val : aCategories[2]) {
				if (totalSugar > val) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (sodium != null) {
			score = 10;
			for (double val : aCategories[3]) {
				if (sodium > val) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (percFruitsAndVetgs != null) {
			score = 10;
			for (double val : cCategories[0]) {
				if (percFruitsAndVetgs > val) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		if (fibre != null) {
			score = 5;
			for (double val : cCategories[1]) {
				if (fibre > val) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		if ((aScore >= 11) && ((percFruitsAndVetgs != null) && (percFruitsAndVetgs <= 80d))
				&& !NutrientCategory.Cheeses.equals(NutrientCategory.valueOf(category))) {
			if (protein != null) {
				score = 5;
				for (double val : cCategories[2]) {
					if (protein > val) {
						break;
					}
					score--;
				}
				cScore += score;
			}
		}

		return aScore - cScore;

	}

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

}
