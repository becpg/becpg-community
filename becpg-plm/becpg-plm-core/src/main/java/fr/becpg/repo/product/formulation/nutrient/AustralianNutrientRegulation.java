package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

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
			BigDecimal bd = BigDecimal.valueOf(value);
			bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
			return bd.doubleValue();
		}
		else{
			return null;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		return formatDouble(roundedValue, locale);
	}
}
