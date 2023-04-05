package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>GSONutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GSONutrientRegulation extends AbstractNutrientRegulation {
	
	/**
	 * <p>Constructor for GSONutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public GSONutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if(value != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)
					|| nutrientTypeCode.equals(NutrientCode.EnergykcalUS)
					|| nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
					return roundValue(value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) || nutrientTypeCode.equals(NutrientCode.Fat)
					|| nutrientTypeCode.equals(NutrientCode.Cholesterol) || nutrientTypeCode.equals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.Polyols) || nutrientTypeCode.equals(NutrientCode.Starch)) {
				if (value >= 10) {
					return roundValue(value, 1d);
				} else if (value > 0.5) {
					return roundValue(value, 0.1d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatMonounsaturated) || nutrientTypeCode.equals(NutrientCode.FatSaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated) || nutrientTypeCode.equals(NutrientCode.FatTrans)) {
				if (value >= 10) {
					return roundValue(value, 1d);
				} else if (value > 0.1) {
					return roundValue(value, 0.1d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				if (value >= 1) {
					return roundValue(value, 0.1d);
				} else if (value > 0.005) {
					return roundValue(value, 0.01d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Salt)) {
				if (value > 1) {
					return roundValue(value, 0.1d);
				} else if (value > 0.0125) {
					return roundValue(value, 0.01d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.VitA)
					|| nutrientTypeCode.equals(NutrientCode.FolicAcid)
					|| nutrientTypeCode.equals(NutrientCode.Chloride)
					|| nutrientTypeCode.equals(NutrientCode.Calcium)
					|| nutrientTypeCode.equals(NutrientCode.Phosphorus)
					|| nutrientTypeCode.equals(NutrientCode.Magnesium)
					|| nutrientTypeCode.equals(NutrientCode.Iodine)
					|| nutrientTypeCode.equals(NutrientCode.Potassium)) {
				BigDecimal bd = BigDecimal.valueOf(value);
				bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
				return bd.doubleValue();
			} else if(isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)){
				BigDecimal bd = BigDecimal.valueOf(value);
				bd = bd.round(new MathContext(2,RoundingMode.HALF_EVEN));
				return bd.doubleValue();
			}
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}
	
	/** {@inheritDoc} */
	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {
		return formatDouble(roundedValue, new Locale("en"));
	}
}
