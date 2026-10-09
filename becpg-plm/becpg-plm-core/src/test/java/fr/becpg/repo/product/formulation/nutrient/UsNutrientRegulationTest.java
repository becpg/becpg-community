package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Unit tests of the US compliance tolerances (21 CFR 101.9(g)): class I at least 100 %, class II at
 * least 80 %, nutrients to limit at most 120 % of the declared value, on the serving.
 */
public class UsNutrientRegulationTest {

	private static final String REGULATION_PATH = "beCPG/databases/nuts/UsNutrientRegulation_2016.csv";

	private static final NutrientToleranceCriteria ADDED = NutrientToleranceCriteria.ofLine(false, true);

	private static final double DELTA = 1e-9;

	private final UsNutrientRegulation regulation = new UsNutrientRegulation(REGULATION_PATH);

	private Pair<Double, Double> tolerances(double value, String nutrientTypeCode) {
		return regulation.tolerances(value, nutrientTypeCode, null, NutrientToleranceCriteria.NONE);
	}

	@Test
	public void boundsANutrientToLimitAtHundredAndTwentyPercentOfTheDeclaredValue() {
		Pair<Double, Double> fat = tolerances(10.3d, NutrientCode.Fat);

		assertEquals(12d, fat.getFirst(), DELTA);
		assertNull(fat.getSecond());
	}

	@Test
	public void takesTheShareOfTheRoundedValue() {
		assertEquals(168d, tolerances(143d, NutrientCode.Sodium).getFirst(), DELTA);
	}

	@Test
	public void boundsANaturallyOccurringNutrientAtEightyPercent() {
		Pair<Double, Double> protein = tolerances(12.4d, NutrientCode.Protein);

		assertNull(protein.getFirst());
		assertEquals(9.6d, protein.getSecond(), DELTA);
	}

	@Test
	public void requiresTheWholeDeclaredValueOfAnAddedNutrient() {
		assertEquals(12d, regulation.tolerances(12.4d, NutrientCode.Protein, null, ADDED).getSecond(), DELTA);
		assertEquals(2.3d, regulation.tolerances(2.34d, NutrientCode.Iron, null, ADDED).getSecond(), DELTA);
	}

	@Test
	public void keepsTotalCarbohydrateInClassTwoWhenAdded() {
		assertEquals(24.8d, regulation.tolerances(30.6d, NutrientCode.CarbohydrateWithFiber, null, ADDED).getSecond(), DELTA);
	}

	@Test
	public void boundsAZeroDeclarationByTheThresholdBelowWhichZeroCanBeDeclared() {
		assertEquals(0.5d, tolerances(0.3d, NutrientCode.Sugar).getFirst(), DELTA);
		assertEquals(0.5d, tolerances(0.45d, NutrientCode.FatTrans).getFirst(), DELTA);
		assertEquals(5d, tolerances(3d, NutrientCode.EnergykcalUS).getFirst(), DELTA);
	}

	@Test
	public void givesNoToleranceToACountryThatOnlyFollowsTheUsRounding() {
		UsNutrientRegulation peruvianRegulation = UsNutrientRegulation.withoutTolerances(REGULATION_PATH);

		assertNull(peruvianRegulation.tolerances(10.3d, NutrientCode.Fat, null, NutrientToleranceCriteria.NONE));
	}

	@Test
	public void computesTheLimitsOnTheServing() {
		NutrientToleranceCriteria thirtyGramServing = NutrientToleranceCriteria.NONE.withServingSize(0.03d);

		assertEquals(36d, regulation.tolerances(30d, NutrientCode.Fat, null, thirtyGramServing).getFirst(), DELTA);
	}

	@Test
	public void givesNoToleranceToANutrientTheRuleDoesNotAssess() {
		assertNull(tolerances(1.2d, NutrientCode.Salt));
		assertNull(tolerances(5d, NutrientCode.SugarAdded));
	}
}
