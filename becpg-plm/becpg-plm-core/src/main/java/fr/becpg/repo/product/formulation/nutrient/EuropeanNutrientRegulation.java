package fr.becpg.repo.product.formulation.nutrient;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.alfresco.util.Pair;

import fr.becpg.repo.product.data.constraints.NutMeasurementPrecision;

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
		NutrientRoundedValue ret = extractNutrientRoundedValue(value, nutrientTypeCode);

		return ret.getRoundedValue();
	}

	/** {@inheritDoc} */
	@Override
	protected Pair<Double, Double> tolerancesByCode(Double value, String nutrientTypeCode) {
		NutrientRoundedValue ret = extractNutrientRoundedValue(value, nutrientTypeCode);

		applyTolerance(ret, nutrientTypeCode);

		return new Pair<>(ret.getMaxToleratedValue(), ret.getMinToleratedValue());
	}

	/** {@inheritDoc} */
	@Override
	protected Pair<Double, Double> tolerancesByCode(Double value, String nutrientTypeCode, boolean isClaimed) {
		if (!isClaimed) {
			return tolerancesByCode(value, nutrientTypeCode);
		}

		NutrientRoundedValue ret = extractNutrientRoundedValue(value, nutrientTypeCode);

		applyClaimTolerance(ret, nutrientTypeCode);

		return new Pair<>(ret.getMaxToleratedValue(), ret.getMinToleratedValue());
	}

	/**
	 * <p>extractNutrientRoundedValue.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @param nutrientTypeCode a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.NutrientRoundedValue} object
	 */
	protected NutrientRoundedValue extractNutrientRoundedValue(Double value, String nutrientTypeCode) {

		NutrientRoundedValue ret = new NutrientRoundedValue(nutrientTypeCode, value);
		ret.setRule(e -> new NutrientRoundedRule(3, RoundingMode.HALF_EVEN));

		if (value != null && nutrientTypeCode != null) {
			if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				ret.setRule(e -> new NutrientRoundedRule(1d));
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.startsWith(NutrientCode.Protein) || nutrientTypeCode.startsWith(NutrientCode.Polyols)) {
				ret.setRule(val -> {
					if (val >= 10) {
						return new NutrientRoundedRule(1d);
					} else if ((val > 0.5) && (val < 10)) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated) || nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)) {
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

		}

		return ret;
	}

	/**
	 * <p>applyTolerance.</p>
	 *
	 * @param ret a {@link fr.becpg.repo.product.formulation.nutrient.NutrientRoundedValue} object
	 * @param nutrientTypeCode a {@link java.lang.String} object
	 */
	protected void applyTolerance(NutrientRoundedValue ret, String nutrientTypeCode) {
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
			ret.setTolerances(50d, 35d, true);
		} else if (isMineral(nutrientTypeCode)) {
			ret.setTolerances(45d, 35d, true);
		}

	}

	/**
	 * <p>applyClaimTolerance.</p>
	 *
	 * <p>Applies the EU guidance "Table 3" tolerances used when the nutrient is subject to a
	 * nutritional or health claim. Only the claim-guaranteed direction ("side 1", measurement
	 * uncertainty included) gets the global margin; the opposite direction ("side 2") is limited
	 * to the measurement uncertainty (approximated here by the rounding band, i.e. a zero
	 * additional tolerance). The guaranteed direction is intrinsic to each nutrient family (a
	 * downward margin for nutrients consumers want to reduce, an upward margin for those they
	 * want to increase), which matches the regulated claim families ("low/free/reduced" vs
	 * "source/high/increased").</p>
	 *
	 * @param ret a {@link fr.becpg.repo.product.formulation.nutrient.NutrientRoundedValue} object
	 * @param nutrientTypeCode a {@link java.lang.String} object
	 */
	protected void applyClaimTolerance(NutrientRoundedValue ret, String nutrientTypeCode) {

		Double roundedValue = ret.getRoundedValue();
		if (roundedValue == null) {
			return;
		}

		if (nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
				|| nutrientTypeCode.startsWith(NutrientCode.Protein)) {
			// Side 1 = upward margin
			if (roundedValue < 10) {
				ret.setTolerances(4d, 0d, false);
			} else if (roundedValue <= 40) {
				ret.setTolerances(40d, 0d, true);
			} else {
				ret.setTolerances(16d, 0d, false);
			}
		} else if (nutrientTypeCode.equals(NutrientCode.Sugar)) {
			// Side 1 = downward margin
			if (roundedValue < 10) {
				ret.setTolerances(0d, 4d, false);
			} else if (roundedValue <= 40) {
				ret.setTolerances(0d, 40d, true);
			} else {
				ret.setTolerances(0d, 16d, false);
			}
		} else if (nutrientTypeCode.equals(NutrientCode.Fat)) {
			// Side 1 = downward margin
			if (roundedValue < 10) {
				ret.setTolerances(0d, 3d, false);
			} else if (roundedValue <= 40) {
				ret.setTolerances(0d, 40d, true);
			} else {
				ret.setTolerances(0d, 16d, false);
			}
		} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
			// Side 1 = downward margin
			if (roundedValue < 4) {
				ret.setTolerances(0d, 1.6d, false);
			} else {
				ret.setTolerances(0d, 40d, true);
			}
		} else if (nutrientTypeCode.equals(NutrientCode.FatMonounsaturated) || nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)
				|| nutrientTypeCode.equals(NutrientCode.FatOmega3) || nutrientTypeCode.equals(NutrientCode.FatOmega6)) {
			// Side 1 = upward margin
			if (roundedValue < 4) {
				ret.setTolerances(1.6d, 0d, false);
			} else {
				ret.setTolerances(40d, 0d, true);
			}
		} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
			// Side 1 = downward margin
			if (roundedValue < 0.5d) {
				ret.setTolerances(0d, 0.3d, false);
			} else {
				ret.setTolerances(0d, 40d, true);
			}
		} else if (nutrientTypeCode.equals(NutrientCode.Salt)) {
			// Side 1 = downward margin
			if (roundedValue < 1.25d) {
				ret.setTolerances(0d, 0.75d, false);
			} else {
				ret.setTolerances(0d, 40d, true);
			}
		} else if (isVitamin(nutrientTypeCode)) {
			ret.setTolerances(50d, 0d, true);
		} else if (isMineral(nutrientTypeCode)) {
			ret.setTolerances(45d, 0d, true);
		} else {
			// No Table 3 entry for this nutrient (e.g. energy): keep the standard tolerances
			applyTolerance(ret, nutrientTypeCode);
		}

	}

	/** {@inheritDoc} */
	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {
		locale = getDisplayLocale(locale);

		if (measurementPrecision == null || NutMeasurementPrecision.LessThan.toString().equals(measurementPrecision)) {
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
		}

		if (value != null && value < 10 && value >= 1 && roundedValue != null && nutrientTypeCode != null
				&& !(nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykJ))) {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
			DecimalFormat df = new DecimalFormat("#,###.0#####", symbols);
			return df.format(roundedValue);

		}

		return formatDouble(roundedValue, locale);
	}

	/**
	 * <p>getDisplayLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.util.Locale} object
	 */
	protected Locale getDisplayLocale(Locale locale) {
		if ("MM".equals(locale.getCountry())) {
			locale = Locale.ENGLISH;
		}
		return locale;
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
