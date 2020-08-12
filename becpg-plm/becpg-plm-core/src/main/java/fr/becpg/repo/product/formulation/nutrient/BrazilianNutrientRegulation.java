package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * <p>BrazilianNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BrazilianNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for BrazilianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public BrazilianNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if(value != null && nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)) {
				if (value <= 4){
					return 0.0;
				}
				return roundValue(value,1d);	
			}
			if (nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				if (value < 17){
					return 0.0;
				}
				return roundValue(value,1d);	
			}
			if (nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) || nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.equals(NutrientCode.Protein) || nutrientTypeCode.equals(NutrientCode.Fat)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary) || nutrientTypeCode.contentEquals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberInsoluble)) {
				if (value <= 0.5){
					return 0.0;
				} else if (value < 10) {
					return roundValue(value,0.1d);	
				} else {
					return roundValue(value,1d);	
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatSaturated) || nutrientTypeCode.equals(NutrientCode.FatTrans)){
				if (value <= 0.2){
					return 0.0;
				} else if (value < 10) {
					return roundValue(value,0.1d);	
				} else {
					return roundValue(value,1d);	
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				if (value <= 5) {
					return 0.0;
				} else if (value < 10) {
					return roundValue(value,0.1d);	
				} else {
					return roundValue(value,1d);	
				}
			} else if (isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)) {
				if (value < 1) {
					BigDecimal bd = new BigDecimal(value);
					bd = bd.round(new MathContext(2,RoundingMode.HALF_EVEN));
					return bd.doubleValue();
				}
			}
		}
		
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

	/** {@inheritDoc} */
	@Override
	protected String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		return formatDouble(roundedValue, locale);
	}

}
