package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
			if (nutrientTypeCode.equals(NutrientCode.EnergykcalUS)) {
				if (value > 50) {
					return roundValue(value,10d);
				} else if ((value >= 5) && (value <= 50)) {
					return roundValue(value,5d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated) || nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatTrans)) {
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
				} else if ((value >= 2) && (value <= 5)) {
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
			} else if (nutrientTypeCode.equals(NutrientCode.VitD)
					|| nutrientTypeCode.equals(NutrientCode.Calcium)
					|| nutrientTypeCode.equals(NutrientCode.Iron)
					|| nutrientTypeCode.equals(NutrientCode.Potassium)) {
				return roundValue(value,1d);
			}
		}
		
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}
	
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value<=5 && value >= 2) {
				return "<5";
			} else if ((nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) 
						|| nutrientTypeCode.equals(NutrientCode.Sugar)
						|| nutrientTypeCode.equals(NutrientCode.SugarAdded)
						|| nutrientTypeCode.equals(NutrientCode.FiberDietary)
						|| nutrientTypeCode.equals(NutrientCode.FiberSoluble)
						|| nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
						|| nutrientTypeCode.equals(NutrientCode.Protein)
						|| nutrientTypeCode.equals(NutrientCode.Polyols)
						) && value<1 && value >= 0.5) {
				return "<1";
			} else if (nutrientTypeCode.equals(NutrientCode.VitA)
					|| nutrientTypeCode.equals(NutrientCode.VitC)
					|| nutrientTypeCode.equals(NutrientCode.VitE)
					|| nutrientTypeCode.equals(NutrientCode.VitK1)
					|| nutrientTypeCode.equals(NutrientCode.VitK2)
					|| nutrientTypeCode.equals(NutrientCode.Thiamin)
					|| nutrientTypeCode.equals(NutrientCode.Riboflavin)
					|| nutrientTypeCode.equals(NutrientCode.Niacin)
					|| nutrientTypeCode.equals(NutrientCode.VitB6)
					|| nutrientTypeCode.equals(NutrientCode.Folate)
					|| nutrientTypeCode.equals(NutrientCode.VitB12)
					|| nutrientTypeCode.equals(NutrientCode.Biotin)
					|| nutrientTypeCode.equals(NutrientCode.PantoAcid)
					|| nutrientTypeCode.equals(NutrientCode.Phosphorus)
					|| nutrientTypeCode.equals(NutrientCode.Iodine)
					|| nutrientTypeCode.equals(NutrientCode.Magnesium)
					|| nutrientTypeCode.equals(NutrientCode.Zinc)
					|| nutrientTypeCode.equals(NutrientCode.Selenium)
					|| nutrientTypeCode.equals(NutrientCode.Copper)
					|| nutrientTypeCode.equals(NutrientCode.Manganese)
					|| nutrientTypeCode.equals(NutrientCode.Chromium)
					|| nutrientTypeCode.equals(NutrientCode.Choline)) {
				return "";
			}
		}
		return formatDouble(roundedValue, locale);
	}
	
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.VitD)
				|| nutrientTypeCode.equals(NutrientCode.Calcium)
				|| nutrientTypeCode.equals(NutrientCode.Iron)
				|| nutrientTypeCode.equals(NutrientCode.Potassium)
				|| nutrientTypeCode.equals(NutrientCode.VitA)
				|| nutrientTypeCode.equals(NutrientCode.VitC)
				|| nutrientTypeCode.equals(NutrientCode.VitE)
				|| nutrientTypeCode.equals(NutrientCode.VitK1)
				|| nutrientTypeCode.equals(NutrientCode.VitK2)
				|| nutrientTypeCode.equals(NutrientCode.Thiamin)
				|| nutrientTypeCode.equals(NutrientCode.Riboflavin)
				|| nutrientTypeCode.equals(NutrientCode.Niacin)
				|| nutrientTypeCode.equals(NutrientCode.VitB6)
				|| nutrientTypeCode.equals(NutrientCode.Folate)
				|| nutrientTypeCode.equals(NutrientCode.VitB12)
				|| nutrientTypeCode.equals(NutrientCode.Biotin)
				|| nutrientTypeCode.equals(NutrientCode.PantoAcid)
				|| nutrientTypeCode.equals(NutrientCode.Phosphorus)
				|| nutrientTypeCode.equals(NutrientCode.Iodine)
				|| nutrientTypeCode.equals(NutrientCode.Magnesium)
				|| nutrientTypeCode.equals(NutrientCode.Zinc)
				|| nutrientTypeCode.equals(NutrientCode.Selenium)
				|| nutrientTypeCode.equals(NutrientCode.Copper)
				|| nutrientTypeCode.equals(NutrientCode.Manganese)
				|| nutrientTypeCode.equals(NutrientCode.Chromium)
				|| nutrientTypeCode.equals(NutrientCode.Molybdenum)
				|| nutrientTypeCode.equals(NutrientCode.Chloride)
				|| nutrientTypeCode.equals(NutrientCode.Choline)) {
				if (value > 50) {
					return roundValue(value, 10d);
				} else if (value > 10) {
					return roundValue(value, 5d);
				} else {
					return roundValue(value, 2d);
				}
			}
		}
		return roundValue(value, 1d);
	}
}
