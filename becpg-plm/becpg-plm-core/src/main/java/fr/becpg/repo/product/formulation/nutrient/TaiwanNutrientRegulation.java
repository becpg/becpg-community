package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

public class TaiwanNutrientRegulation  extends EuropeanNutrientRegulation {
	
	/**
	 * <p>Constructor for TaiwanNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public TaiwanNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Locale getDisplayLocale(Locale locale) {
		return new Locale("en");
	}

	

}
