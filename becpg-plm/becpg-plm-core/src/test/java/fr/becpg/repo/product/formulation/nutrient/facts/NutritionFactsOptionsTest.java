/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.nutrient.facts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

/**
 * Unit tests of {@link NutritionFactsOptions}: how a regulation spaces its figures and which
 * languages a panel is written in.
 *
 * @author matthieu
 */
public class NutritionFactsOptionsTest {

	private static final String NBSP = " ";

	@Test
	public void testCanadaSpacesAFigureFromItsUnitWithANoBreakSpace() {
		assertEquals(NBSP, NutritionFactsOptions.forRegulation(NutritionFactsOptions.CA_REGULATION_KEY).figureSeparator());
	}

	@Test
	public void testTheUnitedStatesSetTheUnitAgainstTheFigure() {
		assertEquals("", NutritionFactsOptions.forRegulation(NutritionFactsOptions.US_REGULATION_KEY).figureSeparator());
	}

	@Test
	public void testBilingualCanadianPanelIsWrittenInBothOfficialLanguages() {
		NutritionFactsOptions options = NutritionFactsOptions.forRegulation(NutritionFactsOptions.CA_REGULATION_KEY).withBothOfficialLanguages();

		assertEquals(List.of(Locale.ENGLISH, Locale.FRENCH), options.languages(Locale.CANADA_FRENCH));
	}

	@Test
	public void testSingleLanguageOptionsWriteThePanelInTheLanguageOfTheLabel() {
		NutritionFactsOptions options = NutritionFactsOptions.forRegulation(NutritionFactsOptions.CA_REGULATION_KEY).withBothOfficialLanguages()
				.withSingleLanguage();

		assertEquals(List.of(Locale.FRENCH), options.languages(Locale.FRENCH));
		assertFalse("Only the languages change, the shared daily values stay", options.sharedDailyValues().isEmpty());
	}

}
