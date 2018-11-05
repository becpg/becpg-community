package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

public class AustralianNutrientRegulation extends AbstractNutrientRegulation {
	
	public AustralianNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if(value != null){
			BigDecimal bd = new BigDecimal(value);
			bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
			return bd.doubleValue();
		}
		else{
			return null;
		}
	}
	
	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		return formatDouble(roundedValue, locale);
	}
}
