package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.DoubleUnaryOperator;

import org.alfresco.util.Pair;

/**
 * <p>Compliance tolerance expressed as a share of the declared (rounded) value, the rule of the
 * United States, China and Korea: at least 80 % of the declared value for a nutrient to promote,
 * at most 120 % for a nutrient to limit. Either side may be unbounded.</p>
 *
 * <p>A share of a value declared as 0 is 0: the maximum of a nutrient declared as 0 is instead the
 * threshold below which the regulation lets it be declared as 0 (0.5 g of trans fat in the United
 * States).</p>
 *
 * @param minimumShare share of the declared value the content must reach, {@code null} when unbounded
 * @param maximumShare share of the declared value the content must not exceed, {@code null} when unbounded
 * @author matthieu
 */
record DeclaredValueTolerance(BigDecimal minimumShare, BigDecimal maximumShare) {

	/** Added nutrient that must reach the declared value (US class I). */
	static final DeclaredValueTolerance AT_LEAST_DECLARED = new DeclaredValueTolerance(BigDecimal.ONE, null);

	/** Nutrient to promote. */
	static final DeclaredValueTolerance AT_LEAST_80_PERCENT = new DeclaredValueTolerance(new BigDecimal("0.8"), null);

	/** Nutrient to limit. */
	static final DeclaredValueTolerance AT_MOST_120_PERCENT = new DeclaredValueTolerance(null, new BigDecimal("1.2"));

	/** Chinese vitamins A and D (GB 28050-2011). */
	static final DeclaredValueTolerance BETWEEN_80_AND_180_PERCENT = new DeclaredValueTolerance(new BigDecimal("0.8"), new BigDecimal("1.8"));

	/** Smallest amount tried when looking for the threshold of a zero declaration. */
	private static final double SMALLEST_AMOUNT = 1e-9;

	/** Largest amount tried when looking for the threshold of a zero declaration. */
	private static final double LARGEST_AMOUNT = 1e9;

	private static final int BISECTION_STEPS = 100;

	private static final MathContext THRESHOLD_PRECISION = new MathContext(3, RoundingMode.HALF_UP);

	/**
	 * <p>Computes the limits of a value per 100 g: the value is moved to the declared basis,
	 * rounded as the regulation declares it, and the limits are brought back per 100 g.</p>
	 *
	 * @param valuePer100g the value per 100 g, in the unit of the regulation
	 * @param basis the declared basis
	 * @param rounding the rounding of the regulation for that nutrient
	 * @return the (maximum, minimum) limits per 100 g, the unbounded side being {@code null}
	 */
	Pair<Double, Double> limits(double valuePer100g, ServingBasis basis, DoubleUnaryOperator rounding) {
		BigDecimal declared = BigDecimal.valueOf(rounding.applyAsDouble(basis.toDeclaredBasis(valuePer100g)));
		return new Pair<>(basis.toListBasis(maximum(declared, rounding)), basis.toListBasis(minimum(declared)));
	}

	/**
	 * <p>Maximum limit of a declared value.</p>
	 *
	 * @param declared the declared value
	 * @param rounding the rounding of the regulation for that nutrient, which tells the threshold of a
	 *            zero declaration
	 * @return the maximum, {@code null} when unbounded
	 */
	BigDecimal maximum(BigDecimal declared, DoubleUnaryOperator rounding) {
		if (maximumShare == null) {
			return null;
		}
		if (declared.signum() == 0) {
			return zeroDeclarationThreshold(rounding);
		}
		return declared.multiply(maximumShare);
	}

	/**
	 * <p>Finds, by bisection, the amount from which the rounding stops declaring 0. The rounding of
	 * a nutrient never decreases with the amount.</p>
	 *
	 * @param rounding the rounding of the regulation for that nutrient
	 * @return the threshold with three significant digits, {@code null} when only 0 is declared as 0
	 */
	static BigDecimal zeroDeclarationThreshold(DoubleUnaryOperator rounding) {
		double declaredAsZero = SMALLEST_AMOUNT;
		double declaredAsNonZero = SMALLEST_AMOUNT;
		while ((rounding.applyAsDouble(declaredAsNonZero) == 0d) && (declaredAsNonZero < LARGEST_AMOUNT)) {
			declaredAsZero = declaredAsNonZero;
			declaredAsNonZero *= 2;
		}
		if ((declaredAsNonZero == SMALLEST_AMOUNT) || (rounding.applyAsDouble(declaredAsNonZero) == 0d)) {
			return null;
		}
		for (int i = 0; i < BISECTION_STEPS; i++) {
			double middle = (declaredAsZero + declaredAsNonZero) / 2;
			if (rounding.applyAsDouble(middle) == 0d) {
				declaredAsZero = middle;
			} else {
				declaredAsNonZero = middle;
			}
		}
		return BigDecimal.valueOf(declaredAsNonZero).round(THRESHOLD_PRECISION);
	}

	/**
	 * <p>Minimum limit of a declared value.</p>
	 *
	 * @param declared the declared value
	 * @return the minimum, {@code null} when unbounded
	 */
	BigDecimal minimum(BigDecimal declared) {
		if (minimumShare == null) {
			return null;
		}
		return declared.multiply(minimumShare);
	}
}
