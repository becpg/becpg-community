package fr.becpg.repo.product.formulation.nutrient;

/**
 * <p>What a regulation needs to know about a nutrient line, besides its value, to compute its
 * tolerances.</p>
 *
 * <p>{@code claimed} tells that the nutrient is subject to a nutritional or health claim (EU
 * Table 3 tolerances). {@code added} tells that the nutrient was added to the product
 * (fortification): the Canadian regulation then assesses it as a class I nutrient, with no
 * tolerance at all. {@code servingSize} is the serving size of the product in kg, or {@code null}
 * when the product has none: a regulation that assesses the amount declared per serving (Canada)
 * computes its limits on that amount.</p>
 *
 * @param claimed whether the nutrient is subject to a claim
 * @param added whether the nutrient was added to the product
 * @param servingSize the serving size in kg, {@code null} when unknown
 * @author matthieu
 */
public record NutrientToleranceCriteria(boolean claimed, boolean added, Double servingSize) {

	/** Criteria of a nutrient that is neither claimed nor added, on a product without serving size. */
	public static final NutrientToleranceCriteria NONE = new NutrientToleranceCriteria(false, false, null);

	/**
	 * <p>Criteria of a nutrient that is only known to be claimed or not.</p>
	 *
	 * @param claimed whether the nutrient is subject to a claim
	 * @return the criteria
	 */
	public static NutrientToleranceCriteria ofClaim(boolean claimed) {
		return new NutrientToleranceCriteria(claimed, false, null);
	}

	/**
	 * <p>Criteria of a nutrient line, before the serving size of the product is known.</p>
	 *
	 * @param claimed whether the nutrient is subject to a claim
	 * @param added whether the nutrient was added to the product
	 * @return the criteria
	 */
	public static NutrientToleranceCriteria ofLine(boolean claimed, boolean added) {
		return new NutrientToleranceCriteria(claimed, added, null);
	}

	/**
	 * <p>Same criteria on a product with the given serving size.</p>
	 *
	 * @param servingSizeInKg the serving size in kg, {@code null} when unknown
	 * @return the criteria
	 */
	public NutrientToleranceCriteria withServingSize(Double servingSizeInKg) {
		return new NutrientToleranceCriteria(claimed, added, servingSizeInKg);
	}

	/**
	 * <p>Tells whether the product has a usable serving size.</p>
	 *
	 * @return {@code true} when the serving size is known and strictly positive
	 */
	public boolean hasServingSize() {
		return (servingSize != null) && (servingSize > 0);
	}
}
