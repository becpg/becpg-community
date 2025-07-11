package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>HongKongNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class HongKongNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for HongKongNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public HongKongNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if( nutrientTypeCode != null){
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
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

}
