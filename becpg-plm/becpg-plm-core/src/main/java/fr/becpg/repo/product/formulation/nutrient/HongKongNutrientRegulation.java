package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

public class HongKongNutrientRegulation extends AbstractNutrientRegulation {

	public HongKongNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if(value != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykJ)
					|| nutrientTypeCode.equals(NutrientCode.Sodium) || nutrientTypeCode.equals(NutrientCode.Cholesterol)){
					return roundValue(value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) 
					|| nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatTrans) || nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)) {
					return roundValue(value,0.1d);
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
