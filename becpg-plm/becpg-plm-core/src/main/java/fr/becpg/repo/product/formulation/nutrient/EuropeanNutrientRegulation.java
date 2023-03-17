package fr.becpg.repo.product.formulation.nutrient;

import java.math.RoundingMode;
import java.util.Locale;

import org.alfresco.util.Pair;

/**
 * <p>EuropeanNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EuropeanNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for EuropeanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public EuropeanNutrientRegulation(String path) {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		NutrientRoundedValue ret = roundByCode2(value, nutrientTypeCode);
		return ret.getRoundedValue();
	}

	@Override
	protected Pair<Double, Double> tolerancesByCode(Double value, String nutrientTypeCode) {
		NutrientRoundedValue ret = roundByCode2(value, nutrientTypeCode);
		return new Pair<>(ret.getMaxToleratedValue(), ret.getMinToleratedValue());
	}

	protected NutrientRoundedValue roundByCode2(Double value, String nutrientTypeCode) {

		NutrientRoundedValue ret = new NutrientRoundedValue(nutrientTypeCode, value);
		ret.setRule(e -> new NutrientRoundedRule(3, RoundingMode.HALF_EVEN));

		if (value != null && nutrientTypeCode != null) {
			if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				ret.setRule(e -> new NutrientRoundedRule(1d));
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.startsWith(NutrientCode.Protein)) {
				ret.setRule(val -> {
					if (val >= 10) {
						return new NutrientRoundedRule(1d);
					} else if ((val > 0.5) && (val < 10)) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				ret.setRule(val -> {
					if (val >= 10) {
						return new NutrientRoundedRule(1d);
					} else if ((val > 0.1) && (val < 10)) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				ret.setRule(val -> {
					if (val >= 1) {
						return new NutrientRoundedRule(0.1d);
					} else if ((val > 0.005) && (val < 1)) {
						return new NutrientRoundedRule(0.01d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.Salt)) {
				ret.setRule(val -> {
					if (val >= 1) {
						return new NutrientRoundedRule(0.1d);
					} else if ((val > 0.0125) && (val < 1)) {
						return new NutrientRoundedRule(0.01d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.VitA) || nutrientTypeCode.equals(NutrientCode.FolicAcid)
					|| nutrientTypeCode.equals(NutrientCode.Chloride) || nutrientTypeCode.equals(NutrientCode.Calcium)
					|| nutrientTypeCode.equals(NutrientCode.Phosphorus) || nutrientTypeCode.equals(NutrientCode.Magnesium)
					|| nutrientTypeCode.equals(NutrientCode.Iodine) || nutrientTypeCode.equals(NutrientCode.Potassium)) {
				ret.setRule(e -> new NutrientRoundedRule(3, RoundingMode.HALF_EVEN));
			} else if (isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)) {
				ret.setRule(e -> new NutrientRoundedRule(2, RoundingMode.HALF_EVEN));
			}

			if (nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff) || nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary) || nutrientTypeCode.startsWith(NutrientCode.Protein)) {
				if (ret.getRoundedValue() > 40) {
					ret.setTolerances(8d, false);
				} else if ((ret.getRoundedValue() >= 10) && (ret.getRoundedValue() <= 40)) {
					ret.setTolerances(20d, true);
				} else {
					ret.setTolerances(2d, false);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Fat)) {
				if (ret.getRoundedValue() > 40) {
					ret.setTolerances(8d, false);
				} else if ((ret.getRoundedValue() >= 10) && (ret.getRoundedValue() <= 40)) {
					ret.setTolerances(20d, true);
				} else {
					ret.setTolerances(1.5d, false);
				}

			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated) || nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)) {
				if (ret.getRoundedValue() >= 4) {
					ret.setTolerances(20d, true);
				} else {
					ret.setTolerances(0.8d, false);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				if (ret.getRoundedValue() >= 0.5d) {
					ret.setTolerances(20d, true);
				} else {
					ret.setTolerances(0.15d, false);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Salt)) {
				if (ret.getRoundedValue() >= 1.25d) {
					ret.setTolerances(20d, true);
				} else {
					ret.setTolerances(0.375d, false);
				}
			} else if (isVitamin(nutrientTypeCode)) {
				ret.setTolerances(50d, -35d, true);
			} else if (isMineral(nutrientTypeCode)) {
				ret.setTolerances(45d, -35d, true);
			}
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		if ("MM".equals(locale.getCountry())) {
			locale = new Locale("en");
		}
		if (value != null && roundedValue != null && nutrientTypeCode != null) {
			if (nutrientTypeCode.equals(NutrientCode.FatSaturated) && value <= 0.1) {
				return "< " + formatDouble(0.1, locale);
			} else if ((nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.Protein)) && value <= 0.5) {
				return "< " + formatDouble(0.5, locale);
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium) && value < 0.005) {
				return "< " + formatDouble(0.005, locale);
			} else if (nutrientTypeCode.equals(NutrientCode.Salt) && value < 0.0125) {
				return "< " + formatDouble(0.01, locale);
			}
		}
		return formatDouble(roundedValue, locale);
	}

	/** {@inheritDoc} */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		return roundValue(value, 0.1d);
	}

}
