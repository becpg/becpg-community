package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

import org.alfresco.util.Pair;

import fr.becpg.repo.product.data.constraints.NutMeasurementPrecision;

/**
 * <p>JapanNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JapanNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for JapanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public JapanNutrientRegulation(String path) {
		super(path);
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
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		NutrientRoundedValue ret = extractNutrientRoundedValue(value, nutrientTypeCode);

		return ret.getRoundedValue();
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
		ret.setRule(e -> new NutrientRoundedRule(0.1d));

		if (value != null && nutrientTypeCode != null) {
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)) {
				ret.setRule(val -> {
					if (val >= 5d) {
						return new NutrientRoundedRule(1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.startsWith(NutrientCode.Protein) || nutrientTypeCode.startsWith(NutrientCode.Sugar)) {
				ret.setRule(val -> {
					if (val >= 0.5d) {
						return new NutrientRoundedRule(1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium) || nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				ret.setRule(val -> {
					if (val >= 0.005d) {
						return new NutrientRoundedRule(1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				ret.setRule(val -> {
					if (val >= 0.1d) {
						return new NutrientRoundedRule(1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.FiberDietary) || nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.equals(NutrientCode.FiberSoluble)) {
				ret.setRule(val -> {
					if (val >= 0.005d) {
						return new NutrientRoundedRule(1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.VitA) || nutrientTypeCode.equals(NutrientCode.VitK1)
					|| nutrientTypeCode.equals(NutrientCode.VitK2) || nutrientTypeCode.equals(NutrientCode.Selenium)
					|| nutrientTypeCode.equals(NutrientCode.Potassium) || nutrientTypeCode.equals(NutrientCode.Iodine)
					|| nutrientTypeCode.equals(NutrientCode.Magnesium) || nutrientTypeCode.equals(NutrientCode.Phosphorus)
					|| nutrientTypeCode.equals(NutrientCode.Calcium) || nutrientTypeCode.equals(NutrientCode.Niacin)
					|| nutrientTypeCode.equals(NutrientCode.FolicAcid) || nutrientTypeCode.equals(NutrientCode.VitC)
					|| nutrientTypeCode.equals(NutrientCode.Biotin)) {
				ret.setRule(val -> new NutrientRoundedRule(1d));
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

		if (nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff) || nutrientTypeCode.startsWith(NutrientCode.Cholesterol)
				|| nutrientTypeCode.startsWith(NutrientCode.FiberDietary) || nutrientTypeCode.startsWith(NutrientCode.FiberInsoluble)
				|| nutrientTypeCode.startsWith(NutrientCode.FiberSoluble) || nutrientTypeCode.startsWith(NutrientCode.FatOmega3)
				|| nutrientTypeCode.startsWith(NutrientCode.FatOmega6) || nutrientTypeCode.startsWith(NutrientCode.Salt)) {
			ret.setTolerances(20d, true);

		} else if (nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.Fat)
				|| nutrientTypeCode.equals(NutrientCode.Sugar)) {
			if (ret.value >= 2.5d) {
				ret.setTolerances(20d, true);
			} else {
				ret.setTolerances(0.5d, false);
			}

		} else if (nutrientTypeCode.equals(NutrientCode.Energykcal)) {
			if (ret.value >= 25d) {
				ret.setTolerances(20d, true);
			} else {
				ret.setTolerances(5d, false);
			}

		} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
			if (ret.value >= 0.0025d) {
				ret.setTolerances(20d, true);
			} else {
				ret.setTolerances(0.005d, false);
			}

		} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
			if (ret.value >= 0.5d) {
				ret.setTolerances(20d, true);
			} else {
				ret.setTolerances(0.1d, false);
			}

		} else if (nutrientTypeCode.equals(NutrientCode.PantoAcid) || nutrientTypeCode.equals(NutrientCode.VitB6)
				|| nutrientTypeCode.equals(NutrientCode.VitB12) || nutrientTypeCode.equals(NutrientCode.VitB2)
				|| nutrientTypeCode.equals(NutrientCode.VitB1) || nutrientTypeCode.equals(NutrientCode.VitC)
				|| nutrientTypeCode.equals(NutrientCode.Niacin) || nutrientTypeCode.equals(NutrientCode.FolicAcid)
				|| nutrientTypeCode.equals(NutrientCode.Biotin) || nutrientTypeCode.equals(NutrientCode.Chromium)) {
			ret.setTolerances(80d, 20d, true);

		} else {
			ret.setTolerances(50d, 20d, true);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {

		if (NutMeasurementPrecision.LessThan.toString().equals(measurementPrecision) //
						&& (value != null && roundedValue != null && nutrientTypeCode != null //
								&& (nutrientTypeCode.equals(NutrientCode.Energykcal) && value <= 5
										|| ((nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
												|| nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.Sugar))
												&& value <= 0.5)
										|| (nutrientTypeCode.equals(NutrientCode.Sodium) && value <= 5)
										|| (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value <= 0.005)
										|| (nutrientTypeCode.equals(NutrientCode.FatSaturated) && value <= 0.1)))) {
			return "0";
		}

		return formatDouble(roundedValue, locale);
	}
}
