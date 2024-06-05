package fr.becpg.repo.product.formulation.nutrient;

import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>VietnamNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class VietnamNutrientRegulation extends EuropeanNutrientRegulation {

	/**
	 * <p>Constructor for VietnamNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public VietnamNutrientRegulation(String path) {
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
	protected Locale getDisplayLocale(Locale locale) {
		return new Locale("en");
	}

	/** {@inheritDoc} */
	protected NutrientRoundedValue extractNutrientRoundedValue(Double value, String nutrientTypeCode) {

		NutrientRoundedValue ret = new NutrientRoundedValue(nutrientTypeCode, value);
		ret.setRule(e -> new NutrientRoundedRule(3, RoundingMode.HALF_UP));

		if (value != null && nutrientTypeCode != null) {
			if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				ret.setRule(e -> new NutrientRoundedRule(1d));
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.Polyols) || nutrientTypeCode.equals(NutrientCode.Starch)
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

}
