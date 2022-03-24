package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

/**
 * <p>ChineseNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ChineseNutrientRegulation extends AbstractNutrientRegulation {
	
	/**
	 * <p>Constructor for ChineseNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public ChineseNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				//delta=1, limit to declare0 is <=17
				return roundValue(value<=17?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Protein)
					|| nutrientTypeCode.equals(NutrientCode.Fat)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)) {
				return roundValue(value<=0.5?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatSaturated)){
				return roundValue(value<=0.1?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.FatTrans)){
				return roundValue(value<=0.3?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol) 
					|| nutrientTypeCode.equals(NutrientCode.Sodium)){
				return roundValue(value<=5?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitA)
					|| nutrientTypeCode.equals(NutrientCode.FolicAcid)){
				return roundValue(value<=8?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Calcium)){
				return roundValue(value<=8?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitD)){
				return roundValue(value<=0.1?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitE)
					|| nutrientTypeCode.equals(NutrientCode.VitTocpha)
					|| nutrientTypeCode.equals(NutrientCode.VitB3)){
				return roundValue(value<=0.28?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitK1)){
				return roundValue(value<=1.6?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitB1)
					|| nutrientTypeCode.equals(NutrientCode.VitB2)
					|| nutrientTypeCode.equals(NutrientCode.VitB6)
					|| nutrientTypeCode.equals(NutrientCode.Copper)){
				return roundValue(value<=0.03?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitB12)){
				return roundValue(value<=0.05?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitC)){
				return roundValue(value<=2?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.PantoAcid)){
				return roundValue(value<=0.1?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Biotin)){
				return roundValue(value<=0.6?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Choline)){
				return roundValue(value<=9?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Phosphorus)){
				return roundValue(value<=14?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Potassium)){
				return roundValue(value<=20?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Magnesium)){
				return roundValue(value<=6?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Iron)){
				return roundValue(value<=0.3?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Zinc)){
				return roundValue(value<=0.3?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Iodine)){
				return roundValue(value<=3?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Fluoride)){
				return roundValue(value<=0.02?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Manganese)){
				return roundValue(value<=0.06?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Selenium)) {
				return roundValue(value<=1?0:value, 0.1d);
			}

		}
		
		return roundValue(value,0.1d);
	}
	
	/** {@inheritDoc} */
	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		return formatDouble(roundedValue, locale);
	}
}
