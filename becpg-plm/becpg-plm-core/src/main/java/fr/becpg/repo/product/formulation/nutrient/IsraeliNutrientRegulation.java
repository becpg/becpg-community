package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

public class IsraeliNutrientRegulation extends AbstractNutrientRegulation {

	public IsraeliNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if(value != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) || nutrientTypeCode.equals(NutrientCode.Fat)) {
				if (value < 0.5) {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				if (value < 5) {
					return 0.0;
				}
			}
		}
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		return formatDouble(roundedValue, locale);
	}
}
