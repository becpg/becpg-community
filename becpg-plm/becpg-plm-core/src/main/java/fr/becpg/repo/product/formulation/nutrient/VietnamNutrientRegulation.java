package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

public class VietnamNutrientRegulation extends EuropeanNutrientRegulation {
	
	/**
	 * <p>Constructor for VietnamNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public VietnamNutrientRegulation(String path)  {
		super(path);
	}

	@Override
	protected Locale getDisplayLocale(Locale locale) {
		return new Locale("en");
	}

	

}
