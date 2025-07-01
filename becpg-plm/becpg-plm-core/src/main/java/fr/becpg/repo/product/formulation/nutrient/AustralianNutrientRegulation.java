package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>AustralianNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AustralianNutrientRegulation extends AbstractNutrientRegulation {
	
	/**
	 * <p>Constructor for AustralianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public AustralianNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal) || nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				return roundValue(value,1d);
			} else if ((nutrientTypeCode.equals(NutrientCode.Protein) ||
					nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber) ||
					nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff) ||
					nutrientTypeCode.equals(NutrientCode.Sugar)) && value > 1d) {
				BigDecimal bd = BigDecimal.valueOf(value);
				bd = bd.setScale(0, RoundingMode.CEILING);
				return bd.doubleValue();
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				if (value < 5d) {
					BigDecimal bd = BigDecimal.valueOf(value);
					BigDecimal bd2 = new BigDecimal("2");
					bd = bd.multiply(bd2).setScale(0, RoundingMode.CEILING);
					return bd.divide(bd2).doubleValue();
				} else {
					BigDecimal bd = BigDecimal.valueOf(value);
					bd = bd.setScale(0, RoundingMode.CEILING);
					return bd.doubleValue();
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				if (value > 5d) {
					if (value <= 140d) {
						BigDecimal bd = BigDecimal.valueOf(value);
						BigDecimal bd2 = new BigDecimal("2");
						bd = bd.multiply(bd2).setScale(-1, RoundingMode.CEILING);
						return bd.divide(bd2).doubleValue();
					} else {
						BigDecimal bd = BigDecimal.valueOf(value);
						bd = bd.setScale(-1, RoundingMode.CEILING);
						return bd.doubleValue();
					}
				}
			}
		}
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(1, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}
	
	
}
