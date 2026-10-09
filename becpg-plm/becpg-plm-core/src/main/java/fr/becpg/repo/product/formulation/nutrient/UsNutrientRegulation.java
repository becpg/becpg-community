package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.alfresco.util.Pair;

/**
 * <p>UsNutrientRegulation class.</p>
 *
 * <p>Tolerances follow 21 CFR 101.9(g), on the amount declared per serving: an added (class I)
 * vitamin, mineral, protein or dietary fiber must reach the declared value, a naturally occurring
 * (class II) one 80 % of it, and calories, total sugars, total fat, saturated fat, trans fat,
 * cholesterol and sodium must not exceed 120 % of it.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UsNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * Nutrients that must not exceed 120 % of the declared value. Added sugars are left out: the
	 * rule only applies to them when they are the only source of sugars, which the list cannot tell.
	 */
	private static final Set<String> NUTRIENTS_TO_LIMIT = Set.of(NutrientCode.EnergykcalUS, NutrientCode.Sugar, NutrientCode.Fat,
			NutrientCode.FatSaturated, NutrientCode.FatTrans, NutrientCode.Cholesterol, NutrientCode.Sodium);

	/** Nutrients that can be class I when added. Vitamins and minerals are added to them. */
	private static final Set<String> NUTRIENTS_TO_FORTIFY = Set.of(NutrientCode.FiberDietary);

	/** Nutrients only ever assessed as class II. */
	private static final Set<String> NATURALLY_OCCURRING_NUTRIENTS = Set.of(NutrientCode.CarbohydrateWithFiber,
			NutrientCode.FatPolyunsaturated, NutrientCode.FatMonounsaturated);

	/** Codes the mineral list holds although they are not minerals of the rule. */
	private static final Set<String> NOT_MINERALS = Set.of(NutrientCode.Starch, NutrientCode.Salt);

	private final boolean assessesTolerances;

	/**
	 * <p>Constructor for UsNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public UsNutrientRegulation(String path) {
		this(path, true);
	}

	private UsNutrientRegulation(String path, boolean assessesTolerances) {
		super(path);
		this.assessesTolerances = assessesTolerances;
	}

	/**
	 * <p>Regulation of a country that follows the US rounding rules but not the FDA compliance
	 * tolerances (Trinidad and Tobago, Dominican Republic, Peru).</p>
	 *
	 * @param path the path of the regulation definition
	 * @return the regulation, without tolerances
	 */
	public static UsNutrientRegulation withoutTolerances(String path) {
		return new UsNutrientRegulation(path, false);
	}

	/** {@inheritDoc} */
	@Override
	protected Pair<Double, Double> tolerancesByCode(Double value, String nutrientTypeCode, NutrientToleranceCriteria criteria) {
		Optional<DeclaredValueTolerance> tolerance = findTolerance(nutrientTypeCode, criteria.added());
		if (!assessesTolerances || (value == null) || tolerance.isEmpty()) {
			return null;
		}
		return tolerance.get().limits(value, ServingBasis.of(criteria), v -> roundByCode(v, nutrientTypeCode));
	}

	/**
	 * <p>Gives the tolerance of a nutrient. A nutrient both naturally present and added is assessed
	 * on its whole value as an added one.</p>
	 *
	 * @param nutrientTypeCode the nutrient code
	 * @param added whether the nutrient was added to the product
	 * @return the tolerance, empty when the rule does not assess the nutrient
	 */
	private Optional<DeclaredValueTolerance> findTolerance(String nutrientTypeCode, boolean added) {
		if (NUTRIENTS_TO_LIMIT.contains(nutrientTypeCode)) {
			return Optional.of(DeclaredValueTolerance.AT_MOST_120_PERCENT);
		}
		if (NATURALLY_OCCURRING_NUTRIENTS.contains(nutrientTypeCode)) {
			return Optional.of(DeclaredValueTolerance.AT_LEAST_80_PERCENT);
		}
		if (canBeFortified(nutrientTypeCode)) {
			return Optional.of(added ? DeclaredValueTolerance.AT_LEAST_DECLARED : DeclaredValueTolerance.AT_LEAST_80_PERCENT);
		}
		return Optional.empty();
	}

	/**
	 * <p>Tells whether a nutrient is class I when added: vitamins, minerals, protein and dietary fiber.</p>
	 *
	 * @param nutrientTypeCode the nutrient code
	 * @return {@code true} when the nutrient can be a class I nutrient
	 */
	private boolean canBeFortified(String nutrientTypeCode) {
		return NUTRIENTS_TO_FORTIFY.contains(nutrientTypeCode) || nutrientTypeCode.startsWith(NutrientCode.Protein) || isVitamin(nutrientTypeCode)
				|| (isMineral(nutrientTypeCode) && !NOT_MINERALS.contains(nutrientTypeCode));
	}

	/** {@inheritDoc} */
	@Override
	public Double convertValue(Double value, String nutUnit, String regulUnit) {
		if (regulUnit != null) {
			regulUnit = regulUnit.replace("mcg", "µg");
		}
		return super.convertValue(value, nutUnit, regulUnit);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if(nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.EnergykcalUS)) {
				if (value > 50) {
					return roundValue(value,10d);
				} else if ((value >= 5) && (value <= 50)) {
					return roundValue(value,5d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated) || nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatTrans)) {
				if (value >= 5) {
					return roundValue(value,1d);
				} else if ((value >= 0.5) && (value < 5)) {
					return roundValue(value,0.5d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {

				if (value > 140) {
					return roundValue(value,10d);
				}
				else if ((value >= 5) && (value <= 140)) {
					return roundValue(value,5d);
				}
				else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				if (value > 5) {
					return roundValue(value,5d);
				} else if ((value >= 2) && (value <= 5)) {
					return roundValue(value,1d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.FiberSoluble) 
					|| nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.startsWith(NutrientCode.Protein)
					|| nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.equals(NutrientCode.Polyols)){
				if (value > 1) {
					return roundValue(value,1d);
				} else if ((value >= 0.5) && (value <= 1)) {
					return 1.0;
				} else {
					return 0.0;
				}		

			} else if (nutrientTypeCode.equals(NutrientCode.Calcium)
					|| nutrientTypeCode.equals(NutrientCode.Potassium)
					|| nutrientTypeCode.equals(NutrientCode.VitA)
					|| nutrientTypeCode.equals(NutrientCode.Choline)
					|| nutrientTypeCode.equals(NutrientCode.Chloride)
					|| nutrientTypeCode.equals(NutrientCode.Phosphorus)) {
				return roundValue(value,10d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitD)
					|| nutrientTypeCode.equals(NutrientCode.Iron)
					|| nutrientTypeCode.equals(NutrientCode.VitE)
					|| nutrientTypeCode.equals(NutrientCode.Niacin)
					|| nutrientTypeCode.equals(NutrientCode.Biotin)
					|| nutrientTypeCode.equals(NutrientCode.PantoAcid)
					|| nutrientTypeCode.equals(NutrientCode.Zinc)
					|| nutrientTypeCode.equals(NutrientCode.Chromium)
					|| nutrientTypeCode.equals(NutrientCode.Molybdenum)) {
				return roundValue(value,0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Thiamin)
					|| nutrientTypeCode.equals(NutrientCode.Riboflavin)
					|| nutrientTypeCode.equals(NutrientCode.VitB6)
					|| nutrientTypeCode.equals(NutrientCode.VitB12)
					|| nutrientTypeCode.equals(NutrientCode.Copper)
					|| nutrientTypeCode.equals(NutrientCode.Manganese)) {
				return roundValue(value,0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Folate)
					|| nutrientTypeCode.equals(NutrientCode.Magnesium)) {
				return roundValue(value,5d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitC)
					|| nutrientTypeCode.equals(NutrientCode.VitK1)
					|| nutrientTypeCode.equals(NutrientCode.VitK2)
					|| nutrientTypeCode.equals(NutrientCode.Iodine)
					|| nutrientTypeCode.equals(NutrientCode.Selenium)) {
				return roundValue(value,1d);
			}
		}

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value<=5 && value >= 2) {
				return "<5";
			} else if ((nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) 
					|| nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.equals(NutrientCode.Protein)
					|| nutrientTypeCode.equals(NutrientCode.Polyols)
					) && value<1 && value >= 0.5) {
				return "<1";
				/*} else if (nutrientTypeCode.equals(NutrientCode.VitA)
					|| nutrientTypeCode.equals(NutrientCode.VitC)
					|| nutrientTypeCode.equals(NutrientCode.VitE)
					|| nutrientTypeCode.equals(NutrientCode.VitK1)
					|| nutrientTypeCode.equals(NutrientCode.VitK2)
					|| nutrientTypeCode.equals(NutrientCode.Thiamin)
					|| nutrientTypeCode.equals(NutrientCode.Riboflavin)
					|| nutrientTypeCode.equals(NutrientCode.Niacin)
					|| nutrientTypeCode.equals(NutrientCode.VitB6)
					|| nutrientTypeCode.equals(NutrientCode.Folate)
					|| nutrientTypeCode.equals(NutrientCode.VitB12)
					|| nutrientTypeCode.equals(NutrientCode.Biotin)
					|| nutrientTypeCode.equals(NutrientCode.PantoAcid)
					|| nutrientTypeCode.equals(NutrientCode.Phosphorus)
					|| nutrientTypeCode.equals(NutrientCode.Iodine)
					|| nutrientTypeCode.equals(NutrientCode.Magnesium)
					|| nutrientTypeCode.equals(NutrientCode.Zinc)
					|| nutrientTypeCode.equals(NutrientCode.Selenium)
					|| nutrientTypeCode.equals(NutrientCode.Copper)
					|| nutrientTypeCode.equals(NutrientCode.Manganese)
					|| nutrientTypeCode.equals(NutrientCode.Chromium)
					|| nutrientTypeCode.equals(NutrientCode.Choline)) {
				return "";*/
			}
		}
		return formatDouble(roundedValue, locale);
	}

	/** {@inheritDoc} */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if(value != null && 
				(nutrientTypeCode.equals(NutrientCode.VitD)
						|| nutrientTypeCode.equals(NutrientCode.Calcium)
						|| nutrientTypeCode.equals(NutrientCode.Iron)
						|| nutrientTypeCode.equals(NutrientCode.Potassium)
						|| nutrientTypeCode.equals(NutrientCode.VitA)
						|| nutrientTypeCode.equals(NutrientCode.VitC)
						|| nutrientTypeCode.equals(NutrientCode.VitE)
						|| nutrientTypeCode.equals(NutrientCode.VitK1)
						|| nutrientTypeCode.equals(NutrientCode.VitK2)
						|| nutrientTypeCode.equals(NutrientCode.Thiamin)
						|| nutrientTypeCode.equals(NutrientCode.Riboflavin)
						|| nutrientTypeCode.equals(NutrientCode.Niacin)
						|| nutrientTypeCode.equals(NutrientCode.VitB6)
						|| nutrientTypeCode.equals(NutrientCode.Folate)
						|| nutrientTypeCode.equals(NutrientCode.VitB12)
						|| nutrientTypeCode.equals(NutrientCode.Biotin)
						|| nutrientTypeCode.equals(NutrientCode.PantoAcid)
						|| nutrientTypeCode.equals(NutrientCode.Phosphorus)
						|| nutrientTypeCode.equals(NutrientCode.Iodine)
						|| nutrientTypeCode.equals(NutrientCode.Magnesium)
						|| nutrientTypeCode.equals(NutrientCode.Zinc)
						|| nutrientTypeCode.equals(NutrientCode.Selenium)
						|| nutrientTypeCode.equals(NutrientCode.Copper)
						|| nutrientTypeCode.equals(NutrientCode.Manganese)
						|| nutrientTypeCode.equals(NutrientCode.Chromium)
						|| nutrientTypeCode.equals(NutrientCode.Molybdenum)
						|| nutrientTypeCode.equals(NutrientCode.Chloride)
						|| nutrientTypeCode.equals(NutrientCode.Choline))) {
			if (value > 50) {
				return roundValue(value, 10d);
			} else if (value > 10) {
				return roundValue(value, 5d);
			} else {
				return roundValue(value, 2d);
			}
		}
		return roundValue(value, 1d);
	}
}
