package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>IsraeliNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IsraeliNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for IsraeliNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public IsraeliNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if(nutrientTypeCode != null){
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
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

}
