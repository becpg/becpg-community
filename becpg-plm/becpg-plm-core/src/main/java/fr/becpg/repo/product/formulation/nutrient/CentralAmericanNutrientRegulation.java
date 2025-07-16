package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>CentralAmericanNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CentralAmericanNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for CentralAmericanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public CentralAmericanNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		
		if (value == null) {
			return null;
		}
		
		if( nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)) {
				if (value < 5){
					return 0.0;
				} else if (value <= 50) {
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			}
			if (nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				if (value < 20){
					return 0.0;
				} else if (value <= 200) {
					return roundValue(value,25d);
				} else {
					return roundValue(value,50d);
				}
			}
			if (nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.Polyols)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary) || nutrientTypeCode.equals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberInsoluble)) {
				if (value >= 1){
					return roundValue(value, 1d);
				} 
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated) 
					|| nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated) || nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)){
				if (value <= 5){
					return roundValue(value,0.5d);	
				} else {
					return roundValue(value,1d);	
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)){
				if (value < 2){
					return 0.0;	
				} else {
					return roundValue(value,5d);	
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium) || nutrientTypeCode.equals(NutrientCode.Potassium)) {
				if (value <= 5) {
					return 0.0;
				} else if (value <= 140) {
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
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value <= 5 && value >= 2) {
				return "Less than 5mg";
			} else if ((nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.Polyols)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary) || nutrientTypeCode.equals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberInsoluble)) && value < 1) {
				return "Less than 1g";
			}
		}
		return formatDouble(roundedValue, locale);
	}
	
	/** {@inheritDoc} */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if(value != null && 
				((isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode))
						&& !nutrientTypeCode.equals(NutrientCode.Sodium) && !nutrientTypeCode.equals(NutrientCode.Potassium))) {
					
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
