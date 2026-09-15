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

import java.util.Map;

/**
 * <p>Wording of a panel in the second official language, for a bilingual table.</p>
 *
 * <p>Canada regulates panels that state everything in both official languages, B.01.454: the
 * wording of the panel, the serving and the footnote are then printed twice, one language under the
 * other, while the name of a nutrient is printed once with the two languages joined. Only what a
 * template draws on its own line therefore lands here; a nutrient carries its two languages in the
 * single wording of its line.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public record NutritionFactsTranslation(Map<String, String> labels, String servingSize, String footNote) {

	/**
	 * <p>No second language: what a panel written in a single language carries.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsTranslation} object
	 */
	public static NutritionFactsTranslation none() {
		return new NutritionFactsTranslation(Map.of(), null, "");
	}

	/**
	 * <p>Tells whether the panel really is bilingual, which is what a template branches on.</p>
	 *
	 * @return a boolean
	 */
	public boolean isPresent() {
		return !labels.isEmpty();
	}

	/**
	 * <p>Fixed wording of the panel in the second language, by key, never null.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String label(String key) {
		return labels.getOrDefault(key, "");
	}

	/**
	 * <p>Tells whether the serving is worded differently in the second language, "1 cup" and
	 * "1 tasse" being the case the regulation shows. A serving that reads the same in both is
	 * printed once.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasServingSize() {
		return (servingSize != null) && !servingSize.isBlank();
	}

}
