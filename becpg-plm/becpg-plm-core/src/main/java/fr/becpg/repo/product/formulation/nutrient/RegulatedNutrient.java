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
 * <p>Display-ready view of one nutrient under one regulation: the rounded values and the strings
 * that a label or a report shows, so that no consumer has to round or format anything itself.</p>
 *
 * <p>Single-regulation counterpart of
 * {@link fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper#extractXMLAttribute},
 * which serializes the same information as report attributes for every regulation at once.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public record RegulatedNutrient(String nutCode, NutrientDisplayRule displayRule, Double value, Double valuePerServing, Double valuePerContainer,
		Double gdaPerc, Double gdaPercPerContainer, String displayValue, String displayValuePerServing, String displayValuePerContainer) {

	/**
	 * <p>Tells whether the regulation requires this nutrient to be declared, in which case it must
	 * stay on the label even when its value is zero.</p>
	 *
	 * @return a boolean
	 */
	public boolean isMandatory() {
		return displayRule.mandatory();
	}

	/**
	 * <p>Tells whether a percentage of the daily value has to be shown for this nutrient.</p>
	 *
	 * @return a boolean
	 */
	public boolean showsDailyValue() {
		return displayRule.showGDAPerc() && (gdaPerc != null);
	}

}
