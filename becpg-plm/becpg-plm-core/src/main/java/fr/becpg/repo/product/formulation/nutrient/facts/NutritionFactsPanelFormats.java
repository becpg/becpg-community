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

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>Rules shared by everything that renders a nutrition facts panel, a Render labeling rule and
 * the technical sheet alike: which template a format is drawn by, and what the model is told.</p>
 *
 * @author matthieu
 */
public final class NutritionFactsPanelFormats {

	/** Name under which the nutrition facts model is exposed to a template. */
	public static final String MODEL_NUTRITION_FACTS = "nf_data";

	/**
	 * Code of the report parameter that declares the nutrients the regulation authorises without
	 * requiring them. The technical sheet reads it by this code, and a panel of the same product
	 * has to declare the same nutrients, so it reads the very same parameter.
	 */
	public static final String SHOW_OPTIONAL_NUTRIENTS_PARAMETER = "showOptionalNutrients";

	private static final String TEMPLATE_PREFIX = "nutritionFacts-";

	private static final String TEMPLATE_SUFFIX = ".ftlx";

	/**
	 * What a format code ends with when the panel has to state everything in both official
	 * languages: "canadaBilingual" is the Canadian standard panel of "canada", written twice. The
	 * two share the same template, only the model they are given differs.
	 */
	private static final String BILINGUAL_FORMAT_SUFFIX = "Bilingual";

	private NutritionFactsPanelFormats() {
		// Do nothing
	}

	/**
	 * <p>Template drawing a panel format.</p>
	 *
	 * @param format the panel format code, "vertical" or "canadaBilingual"
	 * @return the template name
	 */
	public static String templateName(String format) {
		String template = isBilingual(format) ? format.substring(0, format.length() - BILINGUAL_FORMAT_SUFFIX.length()) : format;
		return TEMPLATE_PREFIX + template + TEMPLATE_SUFFIX;
	}

	/**
	 * <p>Options of the model of a panel.</p>
	 *
	 * @param regulation the regulation key, "US" or "CA"
	 * @param format the panel format code
	 * @param showOptionalNutrients whether the panel declares the nutrients the regulation merely authorises
	 * @return the options
	 */
	public static NutritionFactsOptions options(String regulation, String format, boolean showOptionalNutrients) {
		NutritionFactsOptions options = NutritionFactsOptions.forRegulation(regulation);
		if (showOptionalNutrients) {
			options = options.withOptionalNutrients();
		}
		return isBilingual(format) ? options.withBothOfficialLanguages() : options;
	}

	/**
	 * <p>Tells whether the report parameters of a product hold a parameter. A single value is
	 * accepted as well as a list: a property declared multiple still comes back as a bare string
	 * when only one value was ever written to it.</p>
	 *
	 * @param reportParameters the value of {@code rep:reportParameters}, may be null
	 * @param parameter the parameter looked for
	 * @return a boolean
	 */
	public static boolean hasReportParameter(Serializable reportParameters, String parameter) {
		if (reportParameters instanceof Collection<?> values) {
			return values.contains(parameter);
		}
		return parameter.equals(reportParameters);
	}

	private static boolean isBilingual(String format) {
		return format.endsWith(BILINGUAL_FORMAT_SUFFIX);
	}

}
