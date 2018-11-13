package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

public class CanadianNutrientRegulation extends AbstractNutrientRegulation {

	public CanadianNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)) {
				if (value > 50) {
					return roundValue(value,10d);
				} else if ((value >= 5) && (value <= 50)) {
					return roundValue(value,5d);
				} else{
					return roundValue(value, 1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated) || nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				if (value >= 5) {
					return roundValue(value,1d);
				} else if ((value >= 0.5) && (value < 5)) {
					return roundValue(value,0.5d);
				} else if(value>0){
					return roundValue(value,0.1d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				
				if (value > 140) {
					return roundValue(value,10d);
				}
				else if ((value >= 5) && (value <= 140)) {
					return roundValue(value,5d);
				} else if(value>0){
					return roundValue(value,1d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				if (value > 0) {
					return roundValue(value,5d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.FiberSoluble) 
					|| nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.SugarAdded)){
				if (value >= 0.5) {
					return roundValue(value,1d);
				} else {
					return 0.0;
				}		
			} else if (nutrientTypeCode.startsWith(NutrientCode.Protein)){
				if (value >= 0.5) {
					return roundValue(value,1d);
				} else {
					return roundValue(value,0.1d);
				}		
			} else if (nutrientTypeCode.equals(NutrientCode.Potassium)
					|| nutrientTypeCode.equals(NutrientCode.Calcium)) {
				
				if (value > 250) {
					return roundValue(value,50d);
				} else if ((value > 50) && (value <= 250)) {
					return roundValue(value,25d);
				} else if ((value >= 5) && (value <= 50)) {
					return roundValue(value,10d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Iron)) {
				
				if (value > 2.5) {
					return roundValue(value,0.5d);
				} else if ((value > 0.5) && (value <= 2.5)) {
					return roundValue(value,0.25d);
				} else if ((value >= 0.05) && (value <= 0.5)) {
					return roundValue(value,0.1d);
				} else{
					return 0.0;
				}
			}
		}
		
		return roundValue(value,0.1d);
	}

	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		return formatDouble(roundedValue, locale);
	}
}
