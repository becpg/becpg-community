package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

/**
 * <p>GSONutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GSONutrientRegulation extends EuropeanNutrientRegulation {
	
	/**
	 * <p>Constructor for GSONutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public GSONutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Locale getDisplayLocale(Locale locale) {
		return new Locale("en");
	}

	

}
