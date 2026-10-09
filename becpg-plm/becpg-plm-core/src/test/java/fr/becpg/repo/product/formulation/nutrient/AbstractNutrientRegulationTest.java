package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests of the delegation of the criteria-aware tolerances to the claim-aware ones, for a
 * regulation that ignores added nutrients (EU). Expected values are the EU claim tolerances.
 */
public class AbstractNutrientRegulationTest {

	private static final String REGULATION_PATH = "beCPG/databases/nuts/EuNutrientRegulation.csv";

	private static final double DELTA = 1e-9;

	private final EuropeanNutrientRegulation regulation = new EuropeanNutrientRegulation(REGULATION_PATH);

	@Test
	public void appliesTheClaimToleranceCarriedByTheCriteria() {
		assertEquals(17d, regulation.tolerances(12d, NutrientCode.Protein, null, NutrientToleranceCriteria.ofClaim(true)).getFirst(), DELTA);
	}

	@Test
	public void ignoresTheAddedFlagOutsideTheCanadianRegulation() {
		assertEquals(15d, regulation.tolerances(12d, NutrientCode.Protein, null, NutrientToleranceCriteria.ofLine(false, true)).getFirst(), DELTA);
	}
}
