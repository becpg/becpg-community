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

import java.util.List;
import java.util.Map;

/**
 * <p>Everything a nutrition facts template needs, already rounded and already formatted, so that no
 * arithmetic and no lookup ever happens inside a template.</p>
 *
 * <p>{@code nutrients} holds the macronutrient block, {@code micronutrients} the vitamin and mineral
 * block and {@code supplementalIngredients} what a supplemented food declares as added, all three
 * already filtered and ordered by the regulation. {@code labels} is a snapshot
 * of the fixed wording of the panel ("Serving size", "% Daily Value*"), so a template never calls
 * into Java to translate anything, and {@code secondary} carries that same wording in the other
 * official language when the regulation asks for a bilingual table.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public record NutritionFactsData(String format, String regulationKey, NutritionFactsServing serving, NutritionFactsLine calories,
		List<NutritionFactsLine> nutrients, List<NutritionFactsLine> micronutrients, List<NutritionFactsLine> supplementalIngredients,
		String footNote, String notSignificantSource, Map<String, String> labels, NutritionFactsTranslation secondary) {

	/**
	 * <p>Tells whether the panel carries any nutrient at all, an empty panel being usually the sign
	 * of a product that was never formulated.</p>
	 *
	 * @return a boolean
	 */
	public boolean isEmpty() {
		return nutrients.isEmpty() && micronutrients.isEmpty() && supplementalIngredients.isEmpty();
	}

	/**
	 * <p>Tells whether the product declares supplemental ingredients, which is what turns a
	 * nutrition facts table into a supplemented food facts table.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasSupplementalIngredients() {
		return !supplementalIngredients.isEmpty();
	}

	/**
	 * <p>Fixed wording of the panel, by key, never null so a template can print it directly.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String label(String key) {
		return labels.getOrDefault(key, "");
	}

	/**
	 * <p>Same wording in the second official language, empty on a panel written in a single one.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String secondaryLabel(String key) {
		return secondary.label(key);
	}

	/**
	 * <p>Disclaimer of the panel in the second official language, empty on a panel written in a
	 * single one.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String secondaryFootNote() {
		return secondary.footNote();
	}

	/**
	 * <p>Tells whether the panel states everything in both official languages, which is what makes
	 * a template print the title, the serving and the footnote twice.</p>
	 *
	 * @return a boolean
	 */
	public boolean isBilingual() {
		return secondary.isPresent();
	}

}
