package fr.becpg.repo.product.helper;

import java.util.List;
import java.util.Map;

import fr.becpg.model.NutrientProfileCategory;

/**
 * Helper to compute 5C Nutrient Profile Score
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Nutrient5C2021Helper {
	

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
		return compute5CScore(energyKj, satFat, totalFat, totalSugar, sodium, percFruitsAndVetgs, nspFibre, aoacFibre, protein, category, null);
	}

	
	
	
	
	public static int compute5CScore(Double energyKj, Double satFat, Double totalFat, Double totalSugar, Double sodium, Double percFruitsAndVetgs,
				Double nspFibre, Double aoacFibre, Double protein, String category, Map<Double, double[]> resultMap) {
	
			int aScore = 0;
			int cScore = 0;
	
	//		les arrondis sont déja effectués
			
			double[][] aCategories = getACategory(NutrientProfileCategory.valueOf(category));
			double[][] cCategories = getCCategory(NutrientProfileCategory.valueOf(category));
	
			if (energyKj != null) {
				
				double[] scoreDetails = getScoreDetails(energyKj, 10, aCategories[0]);
				
				aScore += (int) scoreDetails[0];
	
				if (resultMap != null) {
					resultMap.put(energyKj, scoreDetails);
				}
			}
	
			if (NutrientProfileCategory.Fats.equals(NutrientProfileCategory.valueOf(category))) {
				if ((satFat != null) && (totalFat != null)) {
					
					double[] scoreDetails = getTotalFatScoreDetails(totalFat, satFat, 10, aCategories[1]);
					
					aScore += (int) scoreDetails[0];
					
					if (resultMap != null) {
						resultMap.put(totalFat, scoreDetails);
					}
				}
			} else if (satFat != null) {
				
				double[] scoreDetails = getScoreDetails(satFat, 10, aCategories[1]);
				
				aScore += (int) scoreDetails[0];
				
				if (resultMap != null) {
					resultMap.put(satFat, scoreDetails);
				}
			}
	
			if (totalSugar != null) {
	
				double[] scoreDetails = getScoreDetails(totalSugar, 10, aCategories[2]);
				
				aScore += (int) scoreDetails[0];
				
				if (resultMap != null) {
					resultMap.put(totalSugar, scoreDetails);
				}
			}
	
			if (sodium != null) {
				
				double[] scoreDetails = getScoreDetails(sodium, 10, aCategories[3]);
				
				aScore += (int) scoreDetails[0];
				
				if (resultMap != null) {
					resultMap.put(sodium, scoreDetails);
				}
			}
	
			if (percFruitsAndVetgs != null) {
				
				double[] scoreDetails = getScoreDetails(percFruitsAndVetgs, 10, cCategories[0]);
				
				cScore += (int) scoreDetails[0];
				
				if (resultMap != null) {
					resultMap.put(percFruitsAndVetgs, scoreDetails);
				}
			}
	
			if ((aScore < 11) || NutrientProfileCategory.Cheeses.equals(NutrientProfileCategory.valueOf(category))
					|| ((aScore >= 11) && (cScore >= 5) && !NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))
					|| ((aScore >= 11) && (cScore >= 10) && NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))) {
				if (protein != null) {
					
					double[] scoreDetails = getScoreDetails(protein, 5, cCategories[3]);
					
					cScore += (int) scoreDetails[0];
					
					if (resultMap != null) {
						resultMap.put(protein, scoreDetails);
					}
				}
	
			}
	
			if (nspFibre != null) {
				
				double[] scoreDetails = getScoreDetails(nspFibre, 5, cCategories[1]);
				
				cScore += (int) scoreDetails[0];
				
				if (resultMap != null) {
					resultMap.put(nspFibre, scoreDetails);
				}
			}
	
			if (aoacFibre != null) {
				
				double[] scoreDetails = getScoreDetails(aoacFibre, 5, cCategories[2]);
				
				cScore += (int) scoreDetails[0];
				
				if (resultMap != null) {
					resultMap.put(aoacFibre, scoreDetails);
				}
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
		return buildNutrientClass(score, ranges, clazz, null);
	}
	
	public static String buildNutrientClass(Double score, List<Double> ranges, List<String> clazz, double[] minMax) {
		if (score != null) {
			if (minMax != null) {
				minMax[1] = Integer.MAX_VALUE;
			}
			for (int i = 0; i < ranges.size(); i++) {
				if (minMax != null) {
					minMax[0] = ranges.get(i);
				}
				if (score > ranges.get(i)) {
					return clazz.get(i);
				}
				if (minMax != null) {
					minMax[1] = ranges.get(i);
				}
			}
			
			if (minMax[0] == minMax[1]) {
				minMax[0] = Integer.MIN_VALUE;
			}
			
			return clazz.get(clazz.size() - 1);
		}
		return null;

	}

	public static double[] getScoreDetails(Double value, int score, double[] categories) {
		double[] result = new double[3];
		
		double min = 0;
		double max = Integer.MAX_VALUE;
		
		for (double val : categories) {
			
			min = val;
			
			if ((value > val) && (val > 0)) {
				break;
			}
			
			if (val > 0) {
				max = val;
			}
			
			score--;
		}
		
		if (min == max) {
			min = 0;
		}
		
		result[0] = score;
		result[1] = min;
		result[2] = max;
		
		return result;
	}
	
	public static double[] getTotalFatScoreDetails(Double totalFat, Double satFat, int score, double[] categories) {
		double[] result = new double[4];
		
		double min = 0;
		double max = Integer.MAX_VALUE;
		double rounded = ((satFat / totalFat) * 100);

		for (double val : categories) {
			
			min = val;

			if (((rounded > val) || (score == 10 && rounded == val))) {
				break;
			}
			
			if (val > 0) {
				max = val;
			}
			score--;
		}
		
		if (min == max) {
			min = 0;
		}
		
		result[0] = score;
		result[1] = min;
		result[2] = max;
		result[3] = rounded;
		
		return result;
	}

}
