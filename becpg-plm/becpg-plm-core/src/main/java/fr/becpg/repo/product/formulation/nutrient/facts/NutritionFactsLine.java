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

/**
 * <p>One line of a nutrition facts panel, fully formatted: a template only has to place it.</p>
 *
 * <p>{@code abbreviatedLabel} is the shortened wording the linear format uses ("Sat. Fat"), the
 * full one everywhere else. {@code plainLabel} and {@code plainAbbreviatedLabel} never embed the
 * value, which the column formats need since they print the figures in their own columns.
 * {@code value} already carries its unit ("8g", "160mg") and {@code dailyValuePercent} already
 * carries its percent sign, both rounded according to the regulation. {@code indentLevel} starts at
 * 1 for a top level nutrient, which is what drives the horizontal offset of the line.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public record NutritionFactsLine(String nutCode, String label, String abbreviatedLabel, String plainLabel, String plainAbbreviatedLabel, String value,
		String valuePerContainer, String dailyValuePercent, String dailyValuePercentPerContainer, int indentLevel, boolean bold,
		boolean showDailyValue, boolean valueInLabel) {

	/**
	 * <p>Tells whether the amount has to be printed after the wording. It must not be when the
	 * regulated sentence already carries it, as "Includes 10g Added Sugars" does.</p>
	 *
	 * @return a boolean
	 */
	public boolean printsValue() {
		return (value != null) && !valueInLabel;
	}

	/**
	 * <p>Wording of the line where the figures are printed in their own columns. A regulated
	 * sentence that embeds the amount reads wrong once the amount is pulled out of it, so the
	 * shortened form the regulation gives is used instead, "Incl. Added Sugars".</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String columnLabel() {
		return valueInLabel ? plainAbbreviatedLabel : plainLabel;
	}

	/**
	 * <p>Tells whether the daily value column has to be filled for this line.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasDailyValue() {
		return showDailyValue && (dailyValuePercent != null);
	}

}
