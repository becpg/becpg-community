package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>IndonesianNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IndonesianNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for IndonesianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public IndonesianNutrientRegulation(String path)  {
		super(path);
	}
	
	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if( nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)) {
				if (value < 5) {
					return 0.0;
				} else if (value <= 50) {
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Fat)
					|| nutrientTypeCode.equals(NutrientCode.FatSaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatTrans)) {
				if (value < 0.5) {
					return 0.0;
				} else if (value <= 5) {
					return roundValue(value,0.5d);
				} else {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				if (value < 2) {
					return 0.0;
				} else if (value <= 5) {
					return roundValue(value,1d);
				} else {
					return roundValue(value,5d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateByDiff) 
					|| nutrientTypeCode.contentEquals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.contentEquals(NutrientCode.Sugar)
					|| nutrientTypeCode.contentEquals(NutrientCode.Protein)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.contentEquals(NutrientCode.FiberInsoluble)) {
				if (value < 0.5) {
					return 0.0;
				} else {
					return roundValue(value,1d);
				}
			} else if (nutrientTypeCode.contentEquals(NutrientCode.Salt)
					|| nutrientTypeCode.contentEquals(NutrientCode.Sodium)) {
				if (value < 5) {
					return 0.0;
				} else if (value <= 140){
					return roundValue(value,5d);
				} else {
					return roundValue(value,10d);
				}
			}
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}
	

	/** {@inheritDoc} */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if(value != null){
			if (isVitamin(nutrientTypeCode) || isMineral(nutrientTypeCode)){
				if (value >= 2 && value <= 10) {
					return roundValue(value, 2d);
				} else if (value > 10) {
					return roundValue(value, 5d);
				}
			}
		}
		return roundValue(value, 1d);
	}
	
}
