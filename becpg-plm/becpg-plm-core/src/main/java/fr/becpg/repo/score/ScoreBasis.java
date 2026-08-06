/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import fr.becpg.repo.product.data.ProductData;

/**
 * Quantity a score refers to.
 *
 * <p>beCPG holds its nutrients per 100 g. A scheme reasoning per 100 mL or per serving
 * therefore needs a conversion, and reading the raw value instead would silently score the
 * wrong quantity: a drink of density 1.03 is not scored on the same numbers per 100 g and
 * per 100 mL, and a scheme like NutrInform reasons on the portion the consumer eats.</p>
 *
 * @author matthieu
 */
public enum ScoreBasis {

	/** The nutrients as beCPG holds them */
	Per100g,

	/** Per 100 millilitres, obtained by dividing by the density */
	Per100ml,

	/** Per serving, obtained by scaling to the serving size */
	PerServing,

	/** Per kilogram of finished product */
	PerKg,

	/** Per product, no conversion */
	PerProduct;

	/** Constant <code>HUNDRED_GRAMS=100d</code> */
	private static final double HUNDRED_GRAMS = 100d;

	/** Constant <code>GRAMS_PER_KILOGRAM=1000d</code> */
	private static final double GRAMS_PER_KILOGRAM = 1000d;

	/**
	 * <p>Parses a basis name, falling back on {@link #Per100g}.</p>
	 *
	 * @param value the stored basis name
	 * @return the matching basis, never null
	 */
	public static ScoreBasis parse(String value) {
		for (ScoreBasis basis : values()) {
			if (basis.name().equals(value)) {
				return basis;
			}
		}
		return Per100g;
	}

	/**
	 * Factor turning a value held per 100 g into the quantity this basis refers to.
	 *
	 * <p>Returns one when the conversion cannot be made, so that a product missing its
	 * density or its serving size keeps being scored on its per 100 g values rather than not
	 * scored at all.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a double, never zero
	 */
	public double conversionFactor(ProductData product) {
		switch (this) {
		case Per100ml:
			return isUsable(product.getDensity()) ? product.getDensity() : 1d;
		case PerServing:
			return isUsable(product.getServingSize()) ? product.getServingSize() / HUNDRED_GRAMS : 1d;
		case PerKg:
			return GRAMS_PER_KILOGRAM / HUNDRED_GRAMS;
		default:
			return 1d;
		}
	}

	/**
	 * <p>isUsable.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @return a boolean
	 */
	private boolean isUsable(Double value) {
		return (value != null) && (value > 0d);
	}

}
