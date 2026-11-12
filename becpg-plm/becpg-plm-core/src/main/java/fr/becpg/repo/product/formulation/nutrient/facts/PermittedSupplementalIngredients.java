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

import java.util.Comparator;
import java.util.List;

/**
 * <p>Ingredients a regulation permits a supplemented food to be supplemented with, in the order its
 * list states them. Only Canada has such a list, the <em>List of Permitted Supplemental
 * Ingredients</em> referred to by SOR/2022-143: its parts I (vitamins), II (mineral nutrients) and IV
 * (other supplemental ingredients), by the nutrient codes of the Canadian regulation file.</p>
 *
 * <p>The supplemented food facts table declares these ingredients and nothing else, in the order of
 * the list (note 2 of figure 1.1 of the directory of formats): the guidelines only count as a
 * supplemental ingredient what the list names. Several nutrient codes standing for the same entry,
 * the forms of vitamin A or of niacin, share its rank. Iron, folic acid, vitamin K and manganese are
 * not on the list.</p>
 *
 * @author matthieu
 */
public final class PermittedSupplementalIngredients {

	private static final List<List<String>> CA_LIST = List.of(List.of("BIOT"), List.of("CHOLN"), List.of("NIA", "NIAEQ"),
			List.of("PANTAC"), List.of("RIBF"), List.of("THIA"), List.of("CARTB", "CARTBEQ"),
			List.of("VITA-", "VITA", "VITA-RAE", "VITA-IU", "RETOL"), List.of("VITB6-"), List.of("VITB12"), List.of("VITC-"),
			List.of("VITD-", "VITDEQ"), List.of("VITE-", "TOCPHA"), List.of("CA"), List.of("CR"), List.of("CU"), List.of("ID"),
			List.of("MG"), List.of("P"), List.of("K"), List.of("SE"), List.of("ZN"), List.of("CAFFN"));

	private static final PermittedSupplementalIngredients CANADA = new PermittedSupplementalIngredients(CA_LIST);

	private static final PermittedSupplementalIngredients NONE = new PermittedSupplementalIngredients(List.of());

	private final List<List<String>> entries;

	private PermittedSupplementalIngredients(List<List<String>> entries) {
		this.entries = entries;
	}

	/**
	 * <p>List of the regulation, empty for a regulation that has no supplemented foods.</p>
	 *
	 * @param regulationKey a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.facts.PermittedSupplementalIngredients} object
	 */
	public static PermittedSupplementalIngredients forRegulation(String regulationKey) {
		return NutritionFactsOptions.CA_REGULATION_KEY.equals(regulationKey) ? CANADA : NONE;
	}

	/**
	 * <p>List naming nothing, for a panel that declares no supplemental ingredient.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.facts.PermittedSupplementalIngredients} object
	 */
	public static PermittedSupplementalIngredients none() {
		return NONE;
	}

	/**
	 * <p>Tells whether the list names that nutrient.</p>
	 *
	 * @param nutCode a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean isPermitted(String nutCode) {
		return rankOf(nutCode) < entries.size();
	}

	/**
	 * <p>Orders nutrient codes the way the list states them, a code the list does not name coming last.</p>
	 *
	 * @return a {@link java.util.Comparator} object
	 */
	public Comparator<String> listOrder() {
		return Comparator.comparingInt(this::rankOf);
	}

	private int rankOf(String nutCode) {
		for (int rank = 0; rank < entries.size(); rank++) {
			if (entries.get(rank).contains(nutCode)) {
				return rank;
			}
		}
		return entries.size();
	}
}
