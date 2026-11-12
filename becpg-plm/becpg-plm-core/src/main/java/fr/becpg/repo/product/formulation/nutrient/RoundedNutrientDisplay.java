package fr.becpg.repo.product.formulation.nutrient;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats the labelled values of one nutrient for one regulation. A value rounded
 * with a step declared by the product is shown as it was rounded: the display rules
 * of the regulation (such as the European {@code "< 0,01"} for salt) are written for
 * its own rounding and would hide the extra precision.
 *
 * @param customRounding whether the value was rounded with a step declared by the product
 * @param nutCode the regulatory code of the nutrient
 * @param measurementPrecision the measurement precision of the nutrient line
 * @param locale the locale driving the number formatting
 * @param regulationKey the key of the regulation
 * @author matthieu
 */
record RoundedNutrientDisplay(boolean customRounding, String nutCode, String measurementPrecision, Locale locale, String regulationKey) {

	private static final String CUSTOM_ROUNDING_PATTERN = "#,##0.######";

	/**
	 * Formats a value of the nutrient.
	 *
	 * @param value the raw value, in {@code nutUnit}, may be {@code null}
	 * @param roundedValue the rounded value, in the unit of the regulation
	 * @param nutUnit the unit of the raw value, may be {@code null}
	 * @return the text to display, {@code null} when there is no value
	 */
	String format(Double value, Double roundedValue, String nutUnit) {
		if (value == null) {
			return null;
		}
		if (customRounding && (roundedValue != null)) {
			return new DecimalFormat(CUSTOM_ROUNDING_PATTERN, new DecimalFormatSymbols(locale)).format(roundedValue);
		}
		return RegulationFormulationHelper.displayValue(value, roundedValue, nutCode, measurementPrecision, locale, regulationKey, nutUnit);
	}
}
