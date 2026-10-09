package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.becpg.repo.PlmRepoConsts;

/**
 * Unit tests of the reading of the "added nutrient" marking carried by a nutrient line.
 */
public class SupplementalIngredientMarkingTest {

	private static final String OTHER_REPORT_KIND = "Other";

	@Test
	public void readsTheMarkingAmongSeveralReportKinds() {
		ArrayList<String> reportKinds = new ArrayList<>(List.of(OTHER_REPORT_KIND, PlmRepoConsts.REPORT_KIND_SUPPLEMENTAL_INGREDIENT));

		assertTrue(SupplementalIngredientMarking.isMarked(reportKinds));
	}

	@Test
	public void readsTheMarkingAsASingleReportKind() {
		assertTrue(SupplementalIngredientMarking.isMarked(PlmRepoConsts.REPORT_KIND_SUPPLEMENTAL_INGREDIENT));
	}

	@Test
	public void ignoresALineWithoutTheMarking() {
		assertFalse(SupplementalIngredientMarking.isMarked(new ArrayList<>(List.of(OTHER_REPORT_KIND))));
		assertFalse(SupplementalIngredientMarking.isMarked(null));
	}
}
