package fr.becpg.repo.product.helper;

import java.util.List;

/**
 * Helper to compute OfCom Nutrient Profile Score
 * 
 * @author matthieu
 *
 */
public class NutrientHelper {

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
	 * 
	 * @param percFruitsAndVetgs
	 *            Fruit, Veg & Nuts (%)
	 * @param fibre
	 *            AOAC Fibre ' (g)
	 * @param protein
	 *            Protein (g)
	 * @return
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
