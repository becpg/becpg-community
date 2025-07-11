package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>SouthAfricanNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SouthAfricanNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for EuropeanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public SouthAfricanNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		
		if( nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.Energykcal)
					|| nutrientTypeCode.equals(NutrientCode.EnergykJ)
					|| nutrientTypeCode.equals(NutrientCode.FatOmega3)
					|| nutrientTypeCode.equals(NutrientCode.Cholesterol)
					|| nutrientTypeCode.equals(NutrientCode.Sodium)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)) {
				return roundValue(value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Fat) || nutrientTypeCode.equals(NutrientCode.FatTrans)
					|| nutrientTypeCode.equals(NutrientCode.FatMonounsaturated) || nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatSaturated) || nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.FiberSoluble) || nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.equals(NutrientCode.Sugar) || nutrientTypeCode.equals(NutrientCode.SugarAdded)
					|| nutrientTypeCode.equals(NutrientCode.Protein)) {
				return roundValue(value,0.1d);
			} else if (isVitamin(nutrientTypeCode)){
				if (nutrientTypeCode.equals(NutrientCode.VitE) || nutrientTypeCode.equals(NutrientCode.VitC)
						|| nutrientTypeCode.equals(NutrientCode.VitB6) || nutrientTypeCode.equals(NutrientCode.PantoAcid)
						|| nutrientTypeCode.equals(NutrientCode.Thiamin) || nutrientTypeCode.equals(NutrientCode.Riboflavin)
						|| nutrientTypeCode.equals(NutrientCode.Niacin) || nutrientTypeCode.equals(NutrientCode.Choline)) {
					return roundValue(value,0.1d);
				}
				return roundValue(value, 1d);
			} else if (isMineral(nutrientTypeCode)) {
				if (nutrientTypeCode.equals(NutrientCode.Iodine) || nutrientTypeCode.equals(NutrientCode.Selenium)
						|| nutrientTypeCode.equals(NutrientCode.Chromium)) {
					return roundValue(value, 1d);
				}
				return roundValue(value,0.1d);
			}
		}
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}


	/** {@inheritDoc} */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		return  roundValue(value, 1d);
	}


}
