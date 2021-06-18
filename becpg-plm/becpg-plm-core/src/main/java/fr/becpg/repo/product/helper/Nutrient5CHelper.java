package fr.becpg.repo.product.helper;

import java.util.List;

import fr.becpg.model.NutrientProfileCategory;

/**
 * Helper to compute 5C Nutrient Profile Score
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Nutrient5CHelper {

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

		int aScore = 0;
		int cScore = 0;
		int score = 10;

//		DOIT - ON ARRONDIR LES RESULTATS POUR LE CALCUL DU SCORE ?
//				Exemple : si une teneur en sucres simples est de 9,001, le système doit-il considérer que l'arrondi est 9 et donc, attribuer 1 point
//				ou doit-il considérer que 9.001>9 et donc attribuer 2 points ?
//				L’attribution des points pour un nutriment donné se fait sur la base de la teneur de l’aliment dans le
//				nutriment considéré, avec un arrondi correspondant à un chiffre supplémentaire par rapport à la définition
//				du seuil d’attribution des points. Pour le calcul du score, pour les fibres les seuils sont définis avec un chiffre
//				après la virgule. Pour les sucres les seuils sont définis à l’unité ou avec un chiffre après la virgule. Pour les
//				protéines, hormis pour le seuil supérieur où il n’y a pas de chiffre après la virgule, les autres seuils sont
//				définis avec un chiffre après la virgule. Pour tous les autres ils sont toujours définis à l’unité, sans chiffre
//				après la virgule (Energie, AGS, sodium, fruits, légumes, légumineuses, fruits à coque, ratio AGS/lipides
//				totaux).
//				Pour l’exemple proposé, le seuil étant à 9g/100g, (il convient de compter 2 points si la teneur en sucres
//				simples est strictement supérieure à 9 et compter 1 point si la teneur est inférieure ou égale à 9) l’arrondi
//				se fait à un chiffre après la virgule. Pour une mesure à 9,001, l’arrondi est donc égal à 9,0. De ce fait, 1 seul
//				point est donc attribué. Si la teneur avait été mesurée à 9,05 ou 9,06, alors l’arrondi aurait été de 9,1 et il
//				aurait fallu attribuer 2 points. Toujours avec l’exemple des sucres simples : pour un seuil à 4,5 (1 point si la
//				valeur est strictement supérieure, 0 si elle est inférieure ou égale à 4,5g/100g), l’arrondi est à deux chiffres
//				après la virgule. Pour un produit à 4,502, l’arrondi est à 4,50, aucun point n’est attribué. Pour un produit à
//				4,505 ou 4,506, l’arrondi est à 4,51, un point est attribué.
		
		
		
		
		double[][] aCategories = getACategory(NutrientProfileCategory.valueOf(category));
		double[][] cCategories = getCCategory(NutrientProfileCategory.valueOf(category));

		if (energyKj != null) {

			for (double val : aCategories[0]) {
				if ((round(energyKj,val) > val) && (val > 0)) {
					break;
				}
				score--;
			}
			aScore += score;
		}

		if (NutrientProfileCategory.Fats.equals(NutrientProfileCategory.valueOf(category))) {
			if ((satFat != null) && (totalFat != null)) {
				score = 10;
				for (double val : aCategories[1]) {
					double rounded = ((round(satFat,val) / round(totalFat,val)) * 100);
					
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
				if ((round(satFat,val) > val) && (val > 0)) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (totalSugar != null) {
			score = 10;
			for (double val : aCategories[2]) {
				if ((round(totalSugar,val) > val) && (val > 0)) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (sodium != null) {
			score = 10;
			for (double val : aCategories[3]) {
				if ((round(sodium,val) > val) && (val > 0)) {
					break;
				}
				score--;
			}

			aScore += score;
		}

		if (percFruitsAndVetgs != null) {
			score = 10;
			for (double val : cCategories[0]) {
				if ((round(percFruitsAndVetgs,val) > val) && (val > 0)) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		if ((aScore < 11) || NutrientProfileCategory.Cheeses.equals(NutrientProfileCategory.valueOf(category))
				|| ((aScore >= 11) && (cScore >= 5) && !NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))
				|| ((aScore >= 11) && (cScore >= 10) && NutrientProfileCategory.Beverages.equals(NutrientProfileCategory.valueOf(category)))) {
			if (protein != null) {
				score = 5;
				for (double val : cCategories[3]) {
					if ((round(protein,val) > val) && (val > 0)) {
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
				if ((round(nspFibre,val) > val) && (val > 0)) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		if (aoacFibre != null) {
			score = 5;
			for (double val : cCategories[2]) {
				if ((round(aoacFibre,val) > val) && (val > 0)) {
					break;
				}
				score--;
			}
			cScore += score;
		}

		return aScore - cScore;

	}

	
	
	
	
	private static double round(Double toRound, Double val) {
		if(val!=null && val.toString().contains(".")) {
			return Math.round(toRound*100)/100d;
		}

		return Math.round(toRound*10)/10d;
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
