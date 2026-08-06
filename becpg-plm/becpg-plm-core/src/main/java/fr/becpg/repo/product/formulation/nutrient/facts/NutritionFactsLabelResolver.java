/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation.nutrient.facts;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Resolves the wording of a nutrition facts panel.</p>
 *
 * <p>A regulation imposes the exact wording of its nutrient names: the FDA requires "Total Fat"
 * where the characteristic of the repository may well be named "Fat, total (NLEA)". The regulated
 * wording therefore comes from a message bundle keyed by regulation and nutrient code, and the name
 * of the characteristic is only a fallback for a nutrient the regulation does not name.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutritionFactsLabelResolver {

	private static final Log logger = LogFactory.getLog(NutritionFactsLabelResolver.class);

	/** Bundle holding the regulated wording of every panel. */
	private static final String BUNDLE_NAME = "alfresco/module/becpg-plm-core/messages/nutrition-facts";

	/** Resolution that skips the locale of the server, see {@link #message(String, Locale)}. */
	private static final ResourceBundle.Control NO_DEFAULT_LOCALE_FALLBACK = ResourceBundle.Control
			.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

	/** Key of the panel title, "Nutrition Facts". */
	public static final String LABEL_TITLE = "title";

	/** Key of the servings per container line. */
	public static final String LABEL_SERVINGS_PER_CONTAINER = "servingsPerContainer";

	/** Key of the serving size line. */
	public static final String LABEL_SERVING_SIZE = "servingSize";

	/** Key of the "Amount per serving" caption. */
	public static final String LABEL_AMOUNT_PER_SERVING = "amountPerServing";

	/** Key of the "% Daily Value*" column header. */
	public static final String LABEL_DAILY_VALUE = "dailyValue";

	/** Key of the "Per serving" column header of the dual column format. */
	public static final String LABEL_PER_SERVING = "perServing";

	/** Key of the "Per container" column header of the dual column format. */
	public static final String LABEL_PER_CONTAINER = "perContainer";

	private static final List<String> PANEL_LABEL_KEYS = List.of(LABEL_TITLE, LABEL_SERVINGS_PER_CONTAINER, LABEL_SERVING_SIZE,
			LABEL_AMOUNT_PER_SERVING, LABEL_DAILY_VALUE, LABEL_PER_SERVING, LABEL_PER_CONTAINER);

	private static final String NUTRIENT_KEY_PREFIX = "nutritionFacts.nutrient.";

	private static final String ABBREVIATION_KEY_PREFIX = "nutritionFacts.abbrev.";

	private static final String PANEL_KEY_PREFIX = "nutritionFacts.panel.";

	private static final String FOOTNOTE_KEY_PREFIX = "nutritionFacts.footNote.";

	private static final String NOT_SIGNIFICANT_SOURCE_KEY_PREFIX = "nutritionFacts.notSignificantSource.";

	private static final String KEY_SEPARATOR = ".";

	/** Placeholder a regulated wording uses when it carries the value inside the sentence. */
	private static final String VALUE_PLACEHOLDER = "{0}";

	private NutritionFactsLabelResolver() {
		// Do nothing
	}

	/**
	 * <p>Regulated wording of a nutrient, falling back to the name of the characteristic.</p>
	 *
	 * @param regulationKey a {@link java.lang.String} object
	 * @param nutCode a {@link java.lang.String} object
	 * @param charactName a {@link java.lang.String} object, used when the regulation names nothing
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public static String nutrientLabel(String regulationKey, String nutCode, String charactName, Locale locale) {
		String label = message(NUTRIENT_KEY_PREFIX + regulationKey + KEY_SEPARATOR + nutCode, locale);
		return label != null ? label : charactName;
	}

	/**
	 * <p>Shortened wording a nutrient takes in the linear format, where the whole panel is a single
	 * sentence: the FDA writes "Sat. Fat" and "Potas." there. Falls back to the full wording.</p>
	 *
	 * @param regulationKey a {@link java.lang.String} object
	 * @param nutCode a {@link java.lang.String} object
	 * @param fullLabel a {@link java.lang.String} object, used when no abbreviation is declared
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public static String nutrientAbbreviation(String regulationKey, String nutCode, String fullLabel, Locale locale) {
		String abbreviation = message(ABBREVIATION_KEY_PREFIX + regulationKey + KEY_SEPARATOR + nutCode, locale);
		return abbreviation != null ? abbreviation : fullLabel;
	}

	/**
	 * <p>Tells whether a regulated wording carries the value inside the sentence rather than after
	 * it, the way the FDA requires "Includes 10g Added Sugars" to be written.</p>
	 *
	 * @param label a {@link java.lang.String} object
	 * @return a boolean
	 */
	public static boolean embedsValue(String label) {
		return (label != null) && label.contains(VALUE_PLACEHOLDER);
	}

	/**
	 * <p>Fills the value into a wording that embeds it. The value is then printed only once, as
	 * part of the sentence.</p>
	 *
	 * @param label a {@link java.lang.String} object
	 * @param value a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public static String formatWithValue(String label, String value, Locale locale) {
		return new MessageFormat(label, locale).format(new Object[] { value != null ? value : "" });
	}

	/**
	 * <p>Same wording with the value left out, for the formats that print the figures in their own
	 * columns: a dual column panel writes "Includes Added Sugars" and puts the amounts alongside.</p>
	 *
	 * @param label a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public static String withoutValue(String label, Locale locale) {
		if (!embedsValue(label)) {
			return label;
		}
		return formatWithValue(label, "", locale).replaceAll("\\s+", " ").trim();
	}

	/**
	 * <p>Fixed wording of the panel, by key, for the given regulation and locale.</p>
	 *
	 * @param regulationKey a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.util.Map} object
	 */
	public static Map<String, String> panelLabels(String regulationKey, Locale locale) {
		Map<String, String> labels = new LinkedHashMap<>();
		for (String key : PANEL_LABEL_KEYS) {
			String label = message(PANEL_KEY_PREFIX + regulationKey + KEY_SEPARATOR + key, locale);
			labels.put(key, label != null ? label : "");
		}
		return labels;
	}

	/**
	 * <p>Footnote of the panel, the daily value disclaimer the regulation imposes.</p>
	 *
	 * @param regulationKey a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public static String footNote(String regulationKey, Locale locale) {
		String footNote = message(FOOTNOTE_KEY_PREFIX + regulationKey, locale);
		return footNote != null ? footNote : "";
	}

	/**
	 * <p>Statement closing a simplified panel, covering the nutrients it does not list.</p>
	 *
	 * @param regulationKey a {@link java.lang.String} object
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public static String notSignificantSource(String regulationKey, Locale locale) {
		String statement = message(NOT_SIGNIFICANT_SOURCE_KEY_PREFIX + regulationKey, locale);
		return statement != null ? statement : "";
	}

	/**
	 * Resolves a wording without ever falling back to the default locale of the server.
	 *
	 * <p>The standard resolution walks the requested locale, then the locale of the machine, then
	 * the base bundle. On a server configured in French, an American panel would therefore print
	 * "saturés" instead of "Saturated Fat" merely because a French bundle exists: a compliance
	 * defect on a regulated label. Denying that middle step leaves the requested language, then
	 * the base bundle, which carries the English wording the regulations state.</p>
	 */
	private static String message(String key, Locale locale) {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale != null ? locale : Locale.ENGLISH, NO_DEFAULT_LOCALE_FALLBACK);
			return bundle.containsKey(key) ? bundle.getString(key) : null;
		} catch (MissingResourceException e) {
			logger.debug("No nutrition facts bundle for " + locale, e);
			return null;
		}
	}

}
