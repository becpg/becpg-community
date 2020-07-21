package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

/**
 * <p>CanadianNutrientRegulation2013 class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CanadianNutrientRegulation2013 extends CanadianNutrientRegulation {

	/**
	 * <p>Constructor for CanadianNutrientRegulation2013.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public CanadianNutrientRegulation2013(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		if(nutrientTypeCode.equals(NutrientCode.Sodium) == false && (isMineral(nutrientTypeCode) || isVitamin(nutrientTypeCode))){
			return "";
		}else{
			return formatDouble(roundedValue, locale);
		}
	}
}
