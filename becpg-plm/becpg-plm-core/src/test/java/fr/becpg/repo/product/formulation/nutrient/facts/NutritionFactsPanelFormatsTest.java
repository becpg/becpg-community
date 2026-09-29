/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.nutrient.facts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests of {@link NutritionFactsPanelFormats} and of the formats the technical sheet prints.
 *
 * @author matthieu
 */
public class NutritionFactsPanelFormatsTest {

	@Test
	public void testFormatIsDrawnByItsTemplate() {
		assertEquals("nutritionFacts-vertical.ftlx", NutritionFactsPanelFormats.templateName("vertical"));
	}

	@Test
	public void testBilingualFormatSharesTheTemplateOfItsFormat() {
		assertEquals("nutritionFacts-canada.ftlx", NutritionFactsPanelFormats.templateName("canadaBilingual"));
	}

	@Test
	public void testReportParameterIsFoundInAListOrAsASingleValue() {
		assertTrue(NutritionFactsPanelFormats.hasReportParameter(new ArrayList<>(List.of("Show traffic lights", "Show dual NFP")), "Show dual NFP"));
		assertTrue(NutritionFactsPanelFormats.hasReportParameter("Show dual NFP", "Show dual NFP"));
		assertFalse(NutritionFactsPanelFormats.hasReportParameter(null, "Show dual NFP"));
		assertFalse(NutritionFactsPanelFormats.hasReportParameter("Show traffic lights", "Show dual NFP"));
	}

	@Test
	public void testUnitedStatesSheetPrintsTheVerticalPanel() {
		assertEquals(Optional.of("vertical"), NutritionFactsPanelRenderer.panelFormat("US", null));
	}

	@Test
	public void testUnitedStatesSheetPrintsTheDualColumnPanelOnRequest() {
		assertEquals(Optional.of("dualColumn"), NutritionFactsPanelRenderer.panelFormat("US", new ArrayList<>(List.of("Show dual NFP"))));
	}

	@Test
	public void testCanadianSheetPrintsTheBilingualCanadianPanel() {
		assertEquals(Optional.of("canadaBilingual"), NutritionFactsPanelRenderer.panelFormat("CA", null));
	}

	@Test
	public void testRegulationWithoutPanelPrintsNone() {
		assertEquals(Optional.empty(), NutritionFactsPanelRenderer.panelFormat("EU", null));
	}

}
