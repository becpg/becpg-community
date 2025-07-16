package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>RussianNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class RussianNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for RussianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public RussianNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if (value == null) {
			return null;
		}
		
		if( nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykJ)){
				if (value <= 1) {
					return 1.0;
				} else if (value <= 5) {
					return roundValue(value,1d);
				} else if (value <= 100) {
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateByDiff) 
					|| nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar)
					|| nutrientTypeCode.contentEquals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.contentEquals(NutrientCode.Protein)
					|| nutrientTypeCode.contentEquals(NutrientCode.Fat)
					|| nutrientTypeCode.contentEquals(NutrientCode.FatTrans)
					|| nutrientTypeCode.contentEquals(NutrientCode.FatSaturated)
					|| nutrientTypeCode.contentEquals(NutrientCode.FatPolyunsaturated)
					|| nutrientTypeCode.contentEquals(NutrientCode.FatMonounsaturated)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value <= 10) {
					return roundValue(value,0.5d);
				} else {
					return roundValue(value,1d);
				}
			}
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

}
