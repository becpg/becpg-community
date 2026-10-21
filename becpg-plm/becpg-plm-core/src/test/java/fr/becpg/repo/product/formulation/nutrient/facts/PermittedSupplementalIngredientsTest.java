/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.nutrient.facts;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Checks the List of Permitted Supplemental Ingredients the supplemented food facts table declares
 * its block from: what it names, and the order it names it in.
 */
public class PermittedSupplementalIngredientsTest {

	private static final PermittedSupplementalIngredients CANADA = PermittedSupplementalIngredients
			.forRegulation(NutritionFactsOptions.CA_REGULATION_KEY);

	@Test
	public void testListNamesVitaminsMineralsAndCaffeine() {
		for (String nutCode : List.of("VITB6-", "VITD-", "K", "CA", "CAFFN")) {
			Assert.assertTrue(nutCode + " is a permitted supplemental ingredient", CANADA.isPermitted(nutCode));
		}
	}

	@Test
	public void testListLeavesOutWhatIsNoSupplementalIngredient() {
		for (String nutCode : List.of("SUGAD", "WATER", "NA", "FE", "VITK1")) {
			Assert.assertFalse(nutCode + " is not on the list", CANADA.isPermitted(nutCode));
		}
	}

	@Test
	public void testOrderFollowsThePartsAndItemsOfTheList() {
		List<String> nutCodes = new ArrayList<>(List.of("CAFFN", "ZN", "VITD-", "CA", "VITB6-", "BIOT"));

		nutCodes.sort(CANADA.listOrder());

		Assert.assertEquals(List.of("BIOT", "VITB6-", "VITD-", "CA", "ZN", "CAFFN"), nutCodes);
	}

	@Test
	public void testFormsOfOneEntryShareItsRank() {
		Assert.assertEquals(0, CANADA.listOrder().compare("VITD-", "VITDEQ"));
	}

	@Test
	public void testRegulationWithoutSupplementedFoodsPermitsNothing() {
		Assert.assertFalse(PermittedSupplementalIngredients.forRegulation(NutritionFactsOptions.US_REGULATION_KEY).isPermitted("CAFFN"));
	}
}
