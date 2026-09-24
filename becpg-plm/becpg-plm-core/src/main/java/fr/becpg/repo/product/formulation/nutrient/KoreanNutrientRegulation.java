package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.util.Pair;

/**
 * <p>KoreanNutrientRegulation class.</p>
 *
 * <p>Tolerances follow the MFDS food labelling standards (식품등의 표시기준), on the amount per
 * 100 g: energy, sodium, sugars, fat, trans fat, saturated fat and cholesterol must stay below
 * 120 % of the declared value, carbohydrate, dietary fiber, protein, vitamins and minerals must
 * reach 80 % of it. A low content of sodium, sugars, saturated fat or cholesterol gets an absolute
 * allowance instead when it is wider (below 25 mg of sodium: 5 mg more).</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class KoreanNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Absolute allowance above the declared value of a content below a threshold.</p>
	 *
	 * @param threshold the content below which the allowance applies, in the unit of the regulation
	 * @param allowance the amount the content may exceed the declared value by
	 */
	private record LowContentAllowance(BigDecimal threshold, BigDecimal allowance) {

		private LowContentAllowance(String threshold, String allowance) {
			this(new BigDecimal(threshold), new BigDecimal(allowance));
		}
	}

	private static final Set<String> NUTRIENTS_TO_LIMIT = Set.of(NutrientCode.Energykcal, NutrientCode.Sodium, NutrientCode.Sugar,
			NutrientCode.Fat, NutrientCode.FatTrans, NutrientCode.FatSaturated, NutrientCode.Cholesterol);

	private static final Set<String> NUTRIENTS_TO_PROMOTE = Set.of(NutrientCode.CarbohydrateWithFiber, NutrientCode.CarbohydrateByDiff,
			NutrientCode.FiberDietary);

	/** Codes the mineral list holds although they are not minerals of the rule. */
	private static final Set<String> NOT_MINERALS = Set.of(NutrientCode.Starch, NutrientCode.Salt);

	private static final Map<String, LowContentAllowance> LOW_CONTENT_ALLOWANCES = Map.of(
			NutrientCode.Sodium, new LowContentAllowance("25", "5"),
			NutrientCode.Sugar, new LowContentAllowance("2.5", "0.5"),
			NutrientCode.FatSaturated, new LowContentAllowance("4", "0.8"),
			NutrientCode.Cholesterol, new LowContentAllowance("25", "5"));

	/**
	 * <p>Constructor for KoreanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public KoreanNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Pair<Double, Double> tolerancesByCode(Double value, String nutrientTypeCode, NutrientToleranceCriteria criteria) {
		Optional<DeclaredValueTolerance> tolerance = findTolerance(nutrientTypeCode);
		if ((value == null) || tolerance.isEmpty()) {
			return null;
		}
		ServingBasis basis = ServingBasis.perHundredGrams();
		BigDecimal declared = BigDecimal.valueOf(roundByCode(value, nutrientTypeCode));
		BigDecimal maximum = widenForLowContent(tolerance.get().maximum(declared, v -> roundByCode(v, nutrientTypeCode)), declared,
				Optional.ofNullable(LOW_CONTENT_ALLOWANCES.get(nutrientTypeCode)));
		return new Pair<>(basis.toListBasis(maximum), basis.toListBasis(tolerance.get().minimum(declared)));
	}

	/**
	 * <p>Gives the tolerance of a nutrient.</p>
	 *
	 * @param nutrientTypeCode the nutrient code
	 * @return the tolerance, empty when the rule does not assess the nutrient
	 */
	private Optional<DeclaredValueTolerance> findTolerance(String nutrientTypeCode) {
		if (NUTRIENTS_TO_LIMIT.contains(nutrientTypeCode)) {
			return Optional.of(DeclaredValueTolerance.AT_MOST_120_PERCENT);
		}
		if (NUTRIENTS_TO_PROMOTE.contains(nutrientTypeCode) || nutrientTypeCode.startsWith(NutrientCode.Protein) || isVitamin(nutrientTypeCode)
				|| (isMineral(nutrientTypeCode) && !NOT_MINERALS.contains(nutrientTypeCode))) {
			return Optional.of(DeclaredValueTolerance.AT_LEAST_80_PERCENT);
		}
		return Optional.empty();
	}

	/**
	 * <p>Widens the maximum of a low content to its absolute allowance when that one is wider.</p>
	 *
	 * @param maximum the maximum from the share of the declared value, {@code null} when unbounded
	 * @param declared the declared value
	 * @param lowContentAllowance the allowance of the nutrient, empty when it has none
	 * @return the maximum
	 */
	private BigDecimal widenForLowContent(BigDecimal maximum, BigDecimal declared, Optional<LowContentAllowance> lowContentAllowance) {
		if (lowContentAllowance.isEmpty() || (declared.compareTo(lowContentAllowance.get().threshold()) >= 0)) {
			return maximum;
		}
		BigDecimal allowedMaximum = declared.add(lowContentAllowance.get().allowance());
		return (maximum == null) ? allowedMaximum : maximum.max(allowedMaximum);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if(nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)){
				if (value < 5) {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value < 5) {
					return roundValue(value,0.1d);
				} else {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatTrans)) {
				if (value < 0.2) {
					return 0.0;
				} else if (value >= 0.5) {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				if (value < 2) {
					return 0.0;
				} else if (value >= 5) {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Sodium)) {
				if (value < 5) {
					return 0.0;
				} else if (value <= 120){
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateWithFiber) 
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value >= 1) {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Protein)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value >= 1) {
					return roundValue(value,1d);
				}
			}
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {

		if(value != null && roundedValue != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.FatTrans) && value > 0.2 && value < 0.5) {
				return "less than 0.5g";
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value > 2 && value < 5) {
				return "less than 5mg";
			} else if ((nutrientTypeCode.equals(NutrientCode.Protein)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.Sugar))
					&& value > 0.5 && value < 1){
				return "less than 1g";
			}
		}
		return formatDouble(roundedValue, locale);
	}

	/** {@inheritDoc} */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if(value != null){
			if (isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)){
				if (value < 2) {
					return 0.0;
				}
			}
		}
		return roundValue(value, 1d);
	}

}
