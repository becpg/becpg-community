package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

import org.alfresco.util.Pair;

/**
 * <p>CanadianNutrientRegulation class.</p>
 *
 * <p>Rounding follows the Food and Drug Regulations. Tolerances are the compliance limits of the
 * CFIA nutrition labelling compliance test (criterion 2): a class I nutrient (a vitamin or a
 * mineral added to the product) gets no tolerance, a class II nutrient gets 20 % of the declared
 * value, below the minimum pre-rounded value for the nutrients to promote and above the maximum
 * pre-rounded value for the nutrients to limit. Each class bounds a single side.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CanadianNutrientRegulation extends AbstractNutrientRegulation {

	private enum NutrientClass {
		CLASS_I, CLASS_II_MIN, CLASS_II_MAX
	}

	private static final BigDecimal CLASS_II_TOLERANCE = new BigDecimal("0.2");

	private static final BigDecimal TWO = BigDecimal.valueOf(2);


	private static final Set<String> CLASS_II_MAX_NUTRIENTS = Set.of(NutrientCode.EnergykcalUS, NutrientCode.Fat, NutrientCode.FatSaturated,
			NutrientCode.FatTrans, NutrientCode.Cholesterol, NutrientCode.Sodium, NutrientCode.Sugar, NutrientCode.Polyols);

	private static final Set<String> CLASS_II_MIN_NUTRIENTS = Set.of(NutrientCode.CarbohydrateWithFiber, NutrientCode.Starch,
			NutrientCode.FiberDietary, NutrientCode.FiberSoluble, NutrientCode.FiberInsoluble, NutrientCode.FatMonounsaturated,
			NutrientCode.FatPolyunsaturated, NutrientCode.FatOmega3, NutrientCode.FatOmega6);

	/**
	 * Rounding step of each nutrient, as a function of the value. Below the lowest threshold of a
	 * nutrient, the value is declared as 0: the lowest step is the one that rounds it to 0.
	 */
	private static final Map<String, DoubleUnaryOperator> ROUNDING_STEPS = new HashMap<>();

	static {
		register(v -> (v > 50) ? 10d : ((v >= 5) ? 5d : 1d), NutrientCode.EnergykcalUS);
		register(v -> 10d, NutrientCode.EnergykJ);
		register(v -> (v > 5) ? 1d : ((v >= 0.5) ? 0.5d : 0.1d), NutrientCode.FatTrans, NutrientCode.Fat, NutrientCode.FatSaturated);
		register(v -> (v > 140) ? 10d : ((v >= 5) ? 5d : 1d), NutrientCode.Sodium);
		register(v -> 5d, NutrientCode.Cholesterol);
		register(v -> 1d, NutrientCode.CarbohydrateWithFiber, NutrientCode.FiberDietary, NutrientCode.FiberSoluble, NutrientCode.FiberInsoluble,
				NutrientCode.Sugar, NutrientCode.SugarAdded, NutrientCode.Polyols, NutrientCode.Starch);
		register(v -> (v >= 250) ? 50d : ((v >= 50) ? 25d : 10d), NutrientCode.Potassium, NutrientCode.Calcium, NutrientCode.Phosphorus,
				NutrientCode.Chloride);
		register(v -> (v > 5) ? 1d : ((v >= 1) ? 0.5d : 0.1d), NutrientCode.FatPolyunsaturated, NutrientCode.FatMonounsaturated,
				NutrientCode.FatOmega3, NutrientCode.FatOmega6);
		register(v -> (v >= 250) ? 100d : ((v >= 50) ? 50d : 10d), NutrientCode.VitA);
		register(v -> (v >= 5) ? 1d : ((v >= 1) ? 0.5d : 0.2d), NutrientCode.VitC, NutrientCode.VitD, NutrientCode.Selenium);
		register(v -> (v >= 2.5) ? 0.5d : ((v >= 0.5) ? 0.25d : 0.1d), NutrientCode.Iron, NutrientCode.VitE, NutrientCode.VitK1,
				NutrientCode.VitK2, NutrientCode.Niacin, NutrientCode.Biotin, NutrientCode.Zinc, NutrientCode.Molybdenum, NutrientCode.Chromium);
		register(v -> (v >= 0.25) ? 0.05d : ((v >= 0.05) ? 0.025d : 0.01d), NutrientCode.Thiamin, NutrientCode.Riboflavin, NutrientCode.VitB6,
				NutrientCode.VitB12, NutrientCode.Manganese);
		register(v -> (v >= 50) ? 10d : ((v >= 10) ? 5d : 2d), NutrientCode.Folate, NutrientCode.FolateDFE, NutrientCode.Choline,
				NutrientCode.Iodine, NutrientCode.Magnesium);
		register(v -> (v >= 0.5) ? 0.1d : ((v >= 0.1) ? 0.05d : 0.02d), NutrientCode.PantoAcid);
		register(v -> (v >= 0.05) ? 0.01d : ((v >= 0.025) ? 0.025d : 0.01d), NutrientCode.Copper);
	}

	private static void register(DoubleUnaryOperator roundingStep, String... nutrientTypeCodes) {
		for (String nutrientTypeCode : nutrientTypeCodes) {
			ROUNDING_STEPS.put(nutrientTypeCode, roundingStep);
		}
	}

	/**
	 * <p>Constructor for CanadianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public CanadianNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		Optional<DoubleUnaryOperator> roundingStep = findRoundingStep(nutrientTypeCode);
		if (roundingStep.isEmpty()) {
			return BigDecimal.valueOf(value).round(new MathContext(3, RoundingMode.HALF_EVEN)).doubleValue();
		}
		return Math.max(0d, roundValue(value, roundingStep.get().applyAsDouble(value)));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The compliance test assesses the amount declared per serving: when the product has a
	 * serving size, the limits are computed on the amount per serving, then brought back to the
	 * basis of the nutrient list (per 100 g) so that they read next to its value.</p>
	 */
	@Override
	protected Pair<Double, Double> tolerancesByCode(Double value, String nutrientTypeCode, NutrientToleranceCriteria criteria) {
		Optional<DoubleUnaryOperator> roundingStep = findRoundingStep(nutrientTypeCode);
		Optional<NutrientClass> nutrientClass = classify(nutrientTypeCode, criteria.added());
		if ((value == null) || roundingStep.isEmpty() || nutrientClass.isEmpty()) {
			return null;
		}
		ServingBasis basis = ServingBasis.of(criteria);
		Pair<BigDecimal, BigDecimal> limits = complianceLimits(basis.toDeclaredBasis(value), nutrientTypeCode, roundingStep.get(),
				nutrientClass.get());
		return new Pair<>(basis.toListBasis(limits.getFirst()), basis.toListBasis(limits.getSecond()));
	}

	/**
	 * <p>Computes the compliance limits of a declared amount. The rounding step is the one of the
	 * measured amount: at a band boundary the declared value alone does not tell which step
	 * produced it (5.3 g of fat is declared 5 g with a 1 g step).</p>
	 *
	 * @param amount the amount on the declared basis, in the unit of the regulation
	 * @param nutrientTypeCode the nutrient code
	 * @param roundingStep the rounding step function of the nutrient
	 * @param nutrientClass the CFIA class of the nutrient
	 * @return the (maximum, minimum) compliance limits, the unbounded side being {@code null}
	 */
	private Pair<BigDecimal, BigDecimal> complianceLimits(double amount, String nutrientTypeCode, DoubleUnaryOperator roundingStep,
			NutrientClass nutrientClass) {
		BigDecimal declared = BigDecimal.valueOf(roundByCode(amount, nutrientTypeCode));
		BigDecimal step = BigDecimal.valueOf(roundingStep.applyAsDouble(Math.max(0d, amount)));
		return switch (nutrientClass) {
		case CLASS_I -> new Pair<>(null, minPreRoundedValue(declared, step));
		case CLASS_II_MIN -> new Pair<>(null, minPreRoundedValue(declared, step).subtract(CLASS_II_TOLERANCE.multiply(declared)));
		case CLASS_II_MAX -> new Pair<>(maxComplianceLimit(declared, step), null);
		};
	}

	/**
	 * <p>Gets the rounding step function of a nutrient.</p>
	 *
	 * @param nutrientTypeCode the nutrient code
	 * @return the rounding step function, empty when the regulation does not round that nutrient
	 */
	private Optional<DoubleUnaryOperator> findRoundingStep(String nutrientTypeCode) {
		if (nutrientTypeCode == null) {
			return Optional.empty();
		}
		if (nutrientTypeCode.startsWith(NutrientCode.Protein)) {
			return Optional.of(v -> (v >= 0.5) ? 1d : 0.1d);
		}
		return Optional.ofNullable(ROUNDING_STEPS.get(nutrientTypeCode));
	}

	/**
	 * <p>Gives the CFIA class of a nutrient. A nutrient both naturally present and added is assessed
	 * on its whole value as an added one, the most restrictive rule. Sodium is a nutrient to limit
	 * even when added.</p>
	 *
	 * @param nutrientTypeCode the nutrient code
	 * @param added whether the nutrient was added to the product
	 * @return the class of the nutrient, empty when the compliance test does not assess it
	 */
	private Optional<NutrientClass> classify(String nutrientTypeCode, boolean added) {
		if (CLASS_II_MAX_NUTRIENTS.contains(nutrientTypeCode)) {
			return Optional.of(NutrientClass.CLASS_II_MAX);
		}
		if (CLASS_II_MIN_NUTRIENTS.contains(nutrientTypeCode) || nutrientTypeCode.startsWith(NutrientCode.Protein)) {
			return Optional.of(NutrientClass.CLASS_II_MIN);
		}
		if (isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)) {
			return Optional.of(added ? NutrientClass.CLASS_I : NutrientClass.CLASS_II_MIN);
		}
		return Optional.empty();
	}

	/**
	 * <p>Lowest value that rounds to the declared value.</p>
	 *
	 * @param declared the declared (rounded) value
	 * @param step the rounding step of the declared value
	 * @return the minimum pre-rounded value
	 */
	private BigDecimal minPreRoundedValue(BigDecimal declared, BigDecimal step) {
		return declared.subtract(step.divide(TWO));
	}

	/**
	 * <p>Compliance limit of a nutrient to limit: the maximum pre-rounded value plus 20 % of the
	 * declared value. The CFIA gives the maximum pre-rounded value one digit finer than the rounding
	 * step (7.4 g for 7 g of fat, 64 calories for 60). A value declared as 0 has no declared value to
	 * take 20 % of: the tolerance is then 20 % of the maximum pre-rounded value, itself two digits
	 * finer than the threshold (0.499 g for sugars).</p>
	 *
	 * @param declared the declared (rounded) value
	 * @param step the rounding step of the declared value
	 * @return the maximum compliance limit
	 */
	private BigDecimal maxComplianceLimit(BigDecimal declared, BigDecimal step) {
		BigDecimal halfStep = step.divide(TWO);
		if (declared.signum() == 0) {
			BigDecimal maxPreRounded = halfStep.subtract(digitBelow(halfStep, 2));
			return maxPreRounded.add(CLASS_II_TOLERANCE.multiply(maxPreRounded));
		}
		BigDecimal maxPreRounded = declared.add(halfStep).subtract(digitBelow(step, 1));
		return maxPreRounded.add(CLASS_II_TOLERANCE.multiply(declared));
	}

	/**
	 * <p>Gives the unit of the digit found {@code digits} places below the leading digit of a value,
	 * eg. 0.1 for 5 and 1 place, 0.001 for 0.5 and 2 places.</p>
	 *
	 * @param value a strictly positive value
	 * @param digits the number of places below the leading digit
	 * @return the unit of that digit
	 */
	private BigDecimal digitBelow(BigDecimal value, int digits) {
		int leadingDigitExponent = (value.precision() - value.scale()) - 1;
		return BigDecimal.ONE.scaleByPowerOfTen(leadingDigitExponent - digits);
	}

}
