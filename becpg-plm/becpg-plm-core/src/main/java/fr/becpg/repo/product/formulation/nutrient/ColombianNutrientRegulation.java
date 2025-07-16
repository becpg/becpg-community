package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>ColombianNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ColombianNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for ColombianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public ColombianNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if( nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykcalUS)){
				if (value < 5) {
					return 0.0;
				} else if (value <= 50) {
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatTrans)) {
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
			} else if (nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateByDiff) 
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar)
					|| nutrientTypeCode.contentEquals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.contentEquals(NutrientCode.Protein)) {
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
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Fluoride)) {
				if (value < 0.1) {
					return 0.0;
				} else if (value <= 0.8){
					return roundValue(value,0.1d);
				} else {
					return roundValue(value,0.2d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.VitA)
					|| nutrientTypeCode.contentEquals(NutrientCode.Phosphorus)
					|| nutrientTypeCode.contentEquals(NutrientCode.Chloride)
					|| nutrientTypeCode.contentEquals(NutrientCode.Choline)) {
					return roundValue(value,10d);
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Magnesium)
					|| nutrientTypeCode.contentEquals(NutrientCode.Folate)
					|| nutrientTypeCode.contentEquals(NutrientCode.FolateDFE)) {
					return roundValue(value,5d);
			} else if (nutrientTypeCode.contentEquals(NutrientCode.VitC)
					|| nutrientTypeCode.contentEquals(NutrientCode.VitK1)
					|| nutrientTypeCode.contentEquals(NutrientCode.VitK2)
					|| nutrientTypeCode.contentEquals(NutrientCode.Selenium)
					|| nutrientTypeCode.contentEquals(NutrientCode.Iodine)) {
					return roundValue(value,1d);
			} else if (nutrientTypeCode.contentEquals(NutrientCode.VitE)
					|| nutrientTypeCode.contentEquals(NutrientCode.Niacin)
					|| nutrientTypeCode.contentEquals(NutrientCode.Biotin)
					|| nutrientTypeCode.contentEquals(NutrientCode.PantoAcid)
					|| nutrientTypeCode.contentEquals(NutrientCode.Zinc)
					|| nutrientTypeCode.contentEquals(NutrientCode.Chromium)
					|| nutrientTypeCode.contentEquals(NutrientCode.Molybdenum)) {
					return roundValue(value,0.1d);
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Thiamin)
					|| nutrientTypeCode.contentEquals(NutrientCode.Riboflavin)
					|| nutrientTypeCode.contentEquals(NutrientCode.VitB6)
					|| nutrientTypeCode.contentEquals(NutrientCode.VitB12)
					|| nutrientTypeCode.contentEquals(NutrientCode.Copper)
					|| nutrientTypeCode.contentEquals(NutrientCode.Manganese)) {
					return roundValue(value,0.01d);
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
			if (nutrientTypeCode.equals(NutrientCode.Cholesterol) && value > 2 && value < 5) {
				return "less than 5mg";
			} else if ((nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateByDiff) 
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar)
					|| nutrientTypeCode.contentEquals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.contentEquals(NutrientCode.Protein))
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
				if (value <= 10) {
					return roundValue(value, 2d);
				} else if (value <= 50) {
					return roundValue(value, 5d);
				} else {
					return roundValue(value, 10d);
				}
			}
		}
		return roundValue(value, 1d);
	}
}
