/*
 *
 */
package fr.becpg.repo.product.formulation.allergen;

/**
 * Reference dose of one allergen within a PAL / VITAL regulatory framework.
 *
 * <p>The reference dose ({@code rfdMg}) is expressed in milligrams of allergenic
 * protein, as published by the frameworks (NVWA ED05, VITAL 3.0, VITAL 4.0). The
 * beCPG allergen quantity, on the contrary, is a percentage of the allergenic
 * <i>food</i>. {@code proteinPerc} bridges the two: when it is provided, the
 * action limit is converted from protein to food. When it is left empty the
 * quantities carried by the products are assumed to be already expressed in
 * allergenic protein, which is the convention used by VITAL practitioners.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public record PALReferenceDose(String allergenCode, Double rfdMg, Double maxActionPpm, Double proteinPerc) {

	private static final double PPM_PER_PERCENT = 10000d;

	private static final double PERCENT = 100d;

	private static final double MG_PER_GRAM = 1000d;

	/**
	 * Computes the action limit of this allergen for a given consumption amount,
	 * expressed in percent so that it can be compared to
	 * {@code bcpg:allergenListQtyPerc}.
	 *
	 * @param servingSizeInKg the realistic consumption amount, in kilograms
	 * @return the action limit in percent, or {@code null} when it cannot be computed
	 */
	public Double toActionLimitPerc(Double servingSizeInKg) {
		if ((rfdMg == null) || (servingSizeInKg == null) || (servingSizeInKg <= 0d)) {
			return null;
		}

		double actionLimitPpm = toFoodBasis(rfdMg / servingSizeInKg);

		if ((maxActionPpm != null) && (actionLimitPpm > maxActionPpm)) {
			actionLimitPpm = maxActionPpm;
		}

		return actionLimitPpm / PPM_PER_PERCENT;
	}

	/**
	 * Tells whether a single whole particle carries more allergenic protein than the
	 * reference dose, following the worst-case approach of the frameworks.
	 *
	 * @param particleWeightInGram the weight of one isolated particle, in grams
	 * @param particleProteinPerc the protein content of that particle, in percent
	 * @return true when the particle alone exceeds the reference dose
	 */
	public boolean exceedsParticleDose(Double particleWeightInGram, Double particleProteinPerc) {
		if ((rfdMg == null) || (particleWeightInGram == null) || (particleProteinPerc == null)) {
			return false;
		}

		return particleDose(particleWeightInGram, particleProteinPerc) > rfdMg;
	}

	/**
	 * Computes the protein dose carried by a single whole particle.
	 *
	 * @param particleWeightInGram the weight of one isolated particle, in grams
	 * @param particleProteinPerc the protein content of that particle, in percent
	 * @return the dose in milligrams of allergenic protein
	 */
	public static double particleDose(Double particleWeightInGram, Double particleProteinPerc) {
		if ((particleWeightInGram == null) || (particleProteinPerc == null)) {
			return 0d;
		}

		return particleWeightInGram * (particleProteinPerc / PERCENT) * MG_PER_GRAM;
	}

	/**
	 * Converts a concentration of allergenic protein into a concentration of
	 * allergenic food, leaving it untouched when no protein ratio is declared.
	 *
	 * @param proteinPpm the concentration expressed in allergenic protein
	 * @return the concentration expressed in allergenic food
	 */
	private double toFoodBasis(double proteinPpm) {
		if ((proteinPerc == null) || (proteinPerc <= 0d)) {
			return proteinPpm;
		}

		return proteinPpm / (proteinPerc / PERCENT);
	}

}
