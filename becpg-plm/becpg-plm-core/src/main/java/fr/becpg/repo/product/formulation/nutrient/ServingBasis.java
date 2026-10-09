package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>Moves amounts between the basis of the nutrient list (per 100 g) and the basis a regulation
 * declares, and therefore assesses, them on: the serving. Regulations that assess the amount per
 * serving (Canada, United States) compute their compliance limits on it, then bring them back per
 * 100 g so that they read next to the value of the list.</p>
 *
 * @author matthieu
 */
final class ServingBasis {

	/** Number of 100 g in a kg: turns a serving size in kg into its ratio to 100 g. */
	private static final BigDecimal HUNDRED_GRAMS_PER_KG = BigDecimal.TEN;

	private static final MathContext LIMIT_PRECISION = new MathContext(4, RoundingMode.HALF_UP);

	private final BigDecimal servingFactor;

	private ServingBasis(BigDecimal servingFactor) {
		this.servingFactor = servingFactor;
	}

	/**
	 * <p>Basis of a product: its serving when it has one, 100 g otherwise.</p>
	 *
	 * @param criteria the tolerance criteria, carrying the serving size in kg
	 * @return the basis
	 */
	static ServingBasis of(NutrientToleranceCriteria criteria) {
		if (!criteria.hasServingSize()) {
			return perHundredGrams();
		}
		return new ServingBasis(BigDecimal.valueOf(criteria.servingSize()).multiply(HUNDRED_GRAMS_PER_KG));
	}

	/**
	 * <p>Basis of a regulation that assesses the amounts per 100 g, as the nutrient list holds them.</p>
	 *
	 * @return the basis
	 */
	static ServingBasis perHundredGrams() {
		return new ServingBasis(BigDecimal.ONE);
	}

	/**
	 * <p>Converts an amount per 100 g to the declared basis.</p>
	 *
	 * @param amountPer100g the amount per 100 g
	 * @return the amount on the declared basis
	 */
	double toDeclaredBasis(double amountPer100g) {
		return BigDecimal.valueOf(amountPer100g).multiply(servingFactor).doubleValue();
	}

	/**
	 * <p>Brings a compliance limit back per 100 g. A limit cannot be negative, and one brought back
	 * from a serving keeps four significant digits.</p>
	 *
	 * @param limit the limit on the declared basis, {@code null} for an unbounded side
	 * @return the limit per 100 g, {@code null} for an unbounded side
	 */
	Double toListBasis(BigDecimal limit) {
		if (limit == null) {
			return null;
		}
		BigDecimal positiveLimit = limit.max(BigDecimal.ZERO);
		if (servingFactor.compareTo(BigDecimal.ONE) == 0) {
			return positiveLimit.doubleValue();
		}
		return positiveLimit.divide(servingFactor, LIMIT_PRECISION).doubleValue();
	}
}
