package fr.becpg.repo.product.formulation.nutrient;

import java.text.DecimalFormat;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UsNutrientRegulation extends AbstractNutrientRegulation {
	
	public Log logger = LogFactory.getLog(UsNutrientRegulation.class);

	public UsNutrientRegulation(String path) {
		super(path);
	}
	
	

	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)
				|| nutrientTypeCode.equals(NutrientCode.EnergyFromFat)
				|| nutrientTypeCode.equals(NutrientCode.EnergyFromSatFat)) {
				if (value > 50) {
					return roundValue(value,10d);
				} else if ((value >= 5) && (value <= 50)) {
					return roundValue(value,5d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated) || nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
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
				} else if ((value > 2) && (value <= 5)) {
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
			}
		}
		
		return roundValue(value,1d);
	}
	
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value<=5) {
				return "< 5";
			} else if ((nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) 
						|| nutrientTypeCode.equals(NutrientCode.Sugar)
						|| nutrientTypeCode.equals(NutrientCode.SugarAdded)
						|| nutrientTypeCode.equals(NutrientCode.FiberDietary)
						|| nutrientTypeCode.equals(NutrientCode.FiberSoluble)
						|| nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
						|| nutrientTypeCode.equals(NutrientCode.Protein)
						|| nutrientTypeCode.equals(NutrientCode.Polyols)
						) && value<1) {
				return "< 1";
			}
		}
		return formatDouble(roundedValue, locale);
	}
}
