package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

public class CanadianNutrientRegulation2013 extends CanadianNutrientRegulation {

	public CanadianNutrientRegulation2013(String path)  {
		super(path);
	}

	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		if(isMineral(nutrientTypeCode) || isVitamin(nutrientTypeCode)){
			return "";
		}else{
			return formatDouble(roundedValue, locale);
		}
	}
}
