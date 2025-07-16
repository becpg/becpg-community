package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>ThailandNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ThailandNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for ThailandNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public ThailandNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if (value == null) {
			return null;
		}
		
		if( nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)){
				if (value < 5) {
					return 0.0;
				} else if (value < 50) {
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value < 5) {
					return roundValue(value,0.5d);
				} else {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				if (value < 2) {
					return 0.0;
				} else if (value > 5){
					return roundValue(value,5d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateByDiff) 
					|| nutrientTypeCode.contentEquals(NutrientCode.Protein)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value > 1) {
					return roundValue(value,1d);
				}
			
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Sodium)) {
				if (value < 5) {
					return 0.0;
				} else if (value <= 140){
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
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
			if(!nutrientTypeCode.equals(NutrientCode.Sodium) && (isMineral(nutrientTypeCode) || isVitamin(nutrientTypeCode))){
				return "";
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value > 2 && value < 5) {
				return "less than 5mg";
			} else if ((nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateByDiff) 
					|| nutrientTypeCode.contentEquals(NutrientCode.Protein)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar))
					 && value > 0.5 && value < 1 ) {
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
				} else if (value < 10) {
					return roundValue(value, 2d);
				} else if (value < 50) {
					return roundValue(value, 5d);
				} else {
					return roundValue(value, 10d);
				}
			}
		}
		return roundValue(value, 1d);
	}
}
