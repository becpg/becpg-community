package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

public class JapanNutrientRegulation extends EuropeanNutrientRegulation {
	
	/**
	 * <p>Constructor for JapanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public JapanNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Locale getDisplayLocale(Locale locale) {
		return new Locale("en");
	}

	

}
