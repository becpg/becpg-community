package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

import org.alfresco.util.Pair;

import fr.becpg.repo.product.data.constraints.NutMeasurementPrecision;

/**
 * <p>TaiwanNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TaiwanNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for TaiwanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public TaiwanNutrientRegulation(String path) {
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
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {

		if (measurementPrecision == null || //
				NutMeasurementPrecision.LessThan.toString().equals(measurementPrecision) //
						&& (value != null && roundedValue != null && nutrientTypeCode != null //
								&& (nutrientTypeCode.equals(NutrientCode.Energykcal) && value <= 4
										|| ((nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
												|| nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.Sugar))
												&& value <= 0.5)
										|| (nutrientTypeCode.equals(NutrientCode.Sodium) && value <= 0.5)
										|| (nutrientTypeCode.equals(NutrientCode.FatSaturated) && value <= 0.1)
										|| (nutrientTypeCode.equals(NutrientCode.FatTrans) && value <= 0.3)))) {
			return "0";
		}

		return formatDouble(roundedValue, locale);
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
					if (val >= 4d) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.startsWith(NutrientCode.Protein) || nutrientTypeCode.startsWith(NutrientCode.Sugar)) {
				ret.setRule(val -> {
					if (val >= 0.5d) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				ret.setRule(val -> {
					if (val >= 0.005d) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				ret.setRule(val -> {
					if (val >= 0.1d) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
			} else if (nutrientTypeCode.equals(NutrientCode.FatTrans)) {
				ret.setRule(val -> {
					if (val >= 0.3d) {
						return new NutrientRoundedRule(0.1d);
					} else {
						return new NutrientRoundedRule(true);
					}
				});
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
		if (nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff) || nutrientTypeCode.startsWith(NutrientCode.Protein)) {
			ret.setTolerances(120d, 80d, true);

		} else if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.Fat)
				|| nutrientTypeCode.equals(NutrientCode.FatSaturated) || nutrientTypeCode.equals(NutrientCode.FatTrans)
				|| nutrientTypeCode.equals(NutrientCode.Cholesterol) || nutrientTypeCode.equals(NutrientCode.Sodium)
				|| nutrientTypeCode.equals(NutrientCode.Sugar)) {
			ret.setTolerances(120d, 0d, true);

		} else if (nutrientTypeCode.equals(NutrientCode.VitA) || nutrientTypeCode.equals(NutrientCode.VitD)) {
			ret.setTolerances(120d, 80d, true);

		} else {
			ret.setTolerances(300d, 80d, true);
		}
	}

}
