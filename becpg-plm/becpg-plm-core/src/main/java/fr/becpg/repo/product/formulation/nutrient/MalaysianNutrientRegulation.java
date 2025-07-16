package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>MalaysianNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MalaysianNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for MalaysianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public MalaysianNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if(nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.Sugar)) {
				if (value < 0.05) {
					return 0.0;
				} else {
					return roundValue(value,0.1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated) || nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatMonounsaturated) || nutrientTypeCode.equals(NutrientCode.FatTrans)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)){
				return roundValue(value,0.1d);
			} else if (isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode) 
					|| nutrientTypeCode.equals(NutrientCode.Potassium)) {
				return roundValue(value,0.01d);
			}
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, String measurementPrecision, Locale locale) {
		if (nutrientTypeCode.equals(NutrientCode.Energykcal)
				|| nutrientTypeCode.equals(NutrientCode.Cholesterol) || nutrientTypeCode.equals(NutrientCode.Sodium)
				|| nutrientTypeCode.equals(NutrientCode.Salt) && value != null){
			Integer intValue = value.intValue();
			return intValue.toString();
		} 
		return formatDouble(roundedValue, locale);		
	}

}
