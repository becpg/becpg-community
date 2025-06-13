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
			}
		}
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(1, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}
	
	
}
