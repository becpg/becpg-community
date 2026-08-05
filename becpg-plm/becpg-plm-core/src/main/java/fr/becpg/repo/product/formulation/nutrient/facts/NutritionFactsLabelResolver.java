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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.extensions.surf.util.I18NUtil;

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

	private static final String PANEL_KEY_PREFIX = "nutritionFacts.panel.";

	private static final String FOOTNOTE_KEY_PREFIX = "nutritionFacts.footNote.";

	private static final String KEY_SEPARATOR = ".";

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
	 * I18NUtil echoes the key back when it resolves nothing, which would print
	 * "nutritionFacts.nutrient.US.FASAT" on a label instead of falling back.
	 */
	private static String message(String key, Locale locale) {
		String message = I18NUtil.getMessage(key, locale);
		return ((message == null) || message.equals(key)) ? null : message;
	}

}
