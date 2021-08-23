package fr.becpg.repo.product.helper;

import java.util.List;

/**
 * Helper to compute 5C Nutrient Profile Score
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Nutrient5C2021Helper {
	

	enum NutrientCategory {
		Cheeses, Fats, Beverages, Others
	}

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
	 * <p>compute5CScore.</p>
	 *
	 * @param energyKj a {@link java.lang.Double} object.
	 * @param satFat a {@link java.lang.Double} object.
	 * @param totalFat a {@link java.lang.Double} object.
	 * @param totalSugar a {@link java.lang.Double} object.
	 * @param sodium a {@link java.lang.Double} object.
	 * @param percFruitsAndVetgs a {@link java.lang.Double} object.
	 * @param nspFibre a {@link java.lang.Double} object.
	 * @param aoacFibre a {@link java.lang.Double} object.
	 * @param protein a {@link java.lang.Double} object.
	 * @param category a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int compute5CScore(Double energyKj, Double satFat, Double totalFat, Double totalSugar, Double sodium, Double percFruitsAndVetgs,
			Double nspFibre, Double aoacFibre, Double protein, String category) {

		int aScore = 0;
		int cScore = 0;
		int score = 10;

//		les arrondis sont déja utilisés
		
		double[][] aCategories = getACategory(NutrientCategory.valueOf(category));
		double[][] cCategories = getCCategory(NutrientCategory.valueOf(category));

		if (energyKj != null) {

			for (double val : aCategories[0]) {
				if ((energyKj > val) && (val > 0)) {
					break;
				}
				score--;
			}
			aScore += score;
		}

		if (NutrientCategory.Fats.equals(NutrientCategory.valueOf(category))) {
			if ((satFat != null) && (totalFat != null)) {
				score = 10;
				for (double val : aCategories[1]) {
					double rounded = ((satFat / totalFat) * 100);
					
					if (((rounded > val) || (score == 10 && rounded == val))) {
						break;
					}
					score--;
				}
				
				
				aScore += score;
			}
		} else if (satFat != null) {
			score = 10;
			for (double val : aCategories[1]) {
				if ((satFat > val) && (val > 0)) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (totalSugar != null) {
			score = 10;
			for (double val : aCategories[2]) {
				if ((totalSugar > val) && (val > 0)) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (sodium != null) {
			score = 10;
			for (double val : aCategories[3]) {
				if ((sodium > val) && (val > 0)) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (percFruitsAndVetgs != null) {
			score = 10;
			for (double val : cCategories[0]) {
				if ((percFruitsAndVetgs > val) && (val > 0)) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		if ((aScore < 11) || NutrientCategory.Cheeses.equals(NutrientCategory.valueOf(category))
				|| ((aScore >= 11) && (cScore >= 5) && !NutrientCategory.Beverages.equals(NutrientCategory.valueOf(category)))
				|| ((aScore >= 11) && (cScore >= 10) && NutrientCategory.Beverages.equals(NutrientCategory.valueOf(category)))) {
			if (protein != null) {
				score = 5;
				for (double val : cCategories[3]) {
					if ((protein > val) && (val > 0)) {
						break;
					}
					score--;
				}
				cScore += score;
			}

		}

		if (nspFibre != null) {
			score = 5;
			for (double val : cCategories[1]) {
				if ((nspFibre > val) && (val > 0)) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		if (aoacFibre != null) {
			score = 5;
			for (double val : cCategories[2]) {
				if (aoacFibre > val && (val > 0)) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		return aScore - cScore;

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

}
