package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

public class EuropeanNutrientRegulation extends AbstractNutrientRegulation {

	public EuropeanNutrientRegulation(String path)  {
		super(path);
	}
	
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		
		if(value != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)
				|| nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				return roundValue(value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.startsWith(NutrientCode.Protein)) {
				if (value >= 10) {
					return roundValue(value,1d);
				} else if ((value > 0.5) && (value < 10)) {
					return roundValue(value,0.1d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				if (value >= 10) {
					return roundValue(value,1d);
				} else if ((value > 0.1) && (value < 10)) {
					return roundValue(value,0.1d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				if (value >= 1) {
					return roundValue(value,0.1d);
				} else if ((value > 0.005) && (value < 1)) {
					return roundValue(value,0.01d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Salt)) {
				if (value >= 1) {
					return roundValue(value,0.1d);
				} else if ((value > 0.0125) && (value < 1)) {
					return roundValue(value,0.01d);
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
				BigDecimal bd = new BigDecimal(value);
				bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
				return bd.doubleValue();
			} else if(isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)){
				BigDecimal bd = new BigDecimal(value);
				bd = bd.round(new MathContext(2,RoundingMode.HALF_EVEN));
				return bd.doubleValue();
			}
		}
		logger.info("Round 33 sig");
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		
		if(value != null && roundedValue != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Fat) && value<=0.5) {
				return "< " + formatDouble(0.5, locale);
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated) && value<=0.1) {
				return "< " + formatDouble(0.1, locale);
			} else if ((nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff) 
						|| nutrientTypeCode.equals(NutrientCode.Sugar)
						|| nutrientTypeCode.equals(NutrientCode.FiberDietary)
						|| nutrientTypeCode.equals(NutrientCode.Protein)
						) && value<0.5) {
				return "< " + formatDouble(0.5, locale);
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium) && value<0.005) {
				return "< " + formatDouble(0.005, locale);
			} else if (nutrientTypeCode.equals(NutrientCode.Salt) && value<0.0125) {
				return "< " + formatDouble(0.01, locale);
			}
		}
		return formatDouble(roundedValue, locale);
	}
	
	
}
