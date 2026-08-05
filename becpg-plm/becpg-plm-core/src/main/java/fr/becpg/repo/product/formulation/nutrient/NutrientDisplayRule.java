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
package fr.becpg.repo.product.formulation.nutrient;

/**
 * <p>How a nutrient must be presented under a given regulation, as declared by the regulation CSV
 * (columns {@code sort}, {@code depthLevel}, {@code mandatory}, {@code optionnal}, {@code bold},
 * {@code gda}, {@code ul}, {@code unit}, {@code showGDAPerc}).</p>
 *
 * <p>This is the same information that feeds the {@code regulSort}, {@code regulDepthLevel},
 * {@code regulBold} and {@code regulDisplayMode} attributes of the BIRT data source.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public record NutrientDisplayRule(Integer sort, Integer depthLevel, boolean bold, boolean mandatory, boolean optional, boolean showGDAPerc,
		Double gda, Double ul, String unit) {

	/** Display mode of a nutrient the regulation requires. */
	public static final String DISPLAY_MODE_MANDATORY = "M";

	/** Display mode of a nutrient the regulation allows but does not require. */
	public static final String DISPLAY_MODE_OPTIONAL = "O";

	/** Indentation level used when the regulation does not declare one. */
	private static final int DEFAULT_DEPTH_LEVEL = 1;

	/**
	 * <p>Neutral rule, used when the regulation does not define the nutrient at all.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.NutrientDisplayRule} object
	 */
	public static NutrientDisplayRule undefined() {
		return new NutrientDisplayRule(null, null, false, false, false, false, null, null, null);
	}

	/**
	 * <p>Display mode, with the same semantics as the {@code regulDisplayMode} report attribute.</p>
	 *
	 * @return {@code M} when mandatory, {@code O} when optional, null otherwise
	 */
	public String displayMode() {
		if (mandatory) {
			return DISPLAY_MODE_MANDATORY;
		}
		if (optional) {
			return DISPLAY_MODE_OPTIONAL;
		}
		return null;
	}

	/**
	 * <p>Indentation level of the nutrient line, starting at 1 for a top level nutrient.</p>
	 *
	 * @return a int
	 */
	public int indentLevel() {
		return depthLevel != null ? depthLevel : DEFAULT_DEPTH_LEVEL;
	}

	/**
	 * <p>Tells whether the regulation declares this nutrient at all.</p>
	 *
	 * @return a boolean
	 */
	public boolean isDefined() {
		return sort != null;
	}

}
