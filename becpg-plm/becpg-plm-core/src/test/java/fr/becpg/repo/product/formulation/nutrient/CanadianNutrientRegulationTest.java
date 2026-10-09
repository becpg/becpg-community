package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Unit tests of the Canadian rounding rules and of the CFIA compliance limits used as tolerances.
 * Expected limits come from the CFIA nutrition labelling compliance test (tables 2 and 3 and worked
 * examples).
 */
public class CanadianNutrientRegulationTest {

	private static final String REGULATION_PATH = "beCPG/databases/nuts/CanadianNutrientRegulation_2017.csv";

	private static final String ALCOHOL = "ALC";

	private static final NutrientToleranceCriteria ADDED = NutrientToleranceCriteria.ofLine(false, true);

	private static final double DELTA = 1e-9;

	private final CanadianNutrientRegulation regulation = new CanadianNutrientRegulation(REGULATION_PATH);

	private Pair<Double, Double> tolerances(double value, String nutrientTypeCode) {
		return regulation.tolerances(value, nutrientTypeCode, null, NutrientToleranceCriteria.NONE);
	}

	@Test
	public void roundsAtTheBandBoundariesOfTheRegulation() {
		assertEquals(2.5d, regulation.round(2.5d, NutrientCode.Iron, null), DELTA);
		assertEquals(250d, regulation.round(250d, NutrientCode.Potassium, null), DELTA);
		assertEquals(0d, regulation.round(4.9d, NutrientCode.Potassium, null), DELTA);
		assertEquals(0.3d, regulation.round(0.26d, NutrientCode.Fat, null), DELTA);
	}

	@Test
	public void roundsKilojoulesToTheNearestMultipleOfTen() {
		assertEquals(100d, regulation.round(95.4d, NutrientCode.EnergykJ, null), DELTA);
		assertEquals(1280d, regulation.round(1275.7d, NutrientCode.EnergykJ, null), DELTA);
	}

	@Test
	public void givesNoToleranceToKilojoules() {
		assertNull(tolerances(1275.7d, NutrientCode.EnergykJ));
	}

	@Test
	public void neverDeclaresANegativeValue() {
		assertEquals(0d, regulation.round(-0.2d, NutrientCode.Protein, null), DELTA);
	}

	@Test
	public void keepsThreeSignificantDigitsForAnUnregulatedNutrient() {
		assertEquals(1.23d, regulation.round(1.2345d, NutrientCode.Salt, null), DELTA);
	}

	@Test
	public void boundsANutrientToLimitAboveOnly() {
		Pair<Double, Double> fat = tolerances(9d, NutrientCode.Fat);

		assertEquals(11.2d, fat.getFirst(), DELTA);
		assertNull(fat.getSecond());
	}

	@Test
	public void takesTheMaximumPreRoundedValueOneDigitFinerThanTheStep() {
		assertEquals(76d, tolerances(61d, NutrientCode.EnergykcalUS).getFirst(), DELTA);
		assertEquals(6.24d, tolerances(5d, NutrientCode.Fat).getFirst(), DELTA);
		assertEquals(0.52d, tolerances(0.4d, NutrientCode.FatSaturated).getFirst(), DELTA);
		assertEquals(184d, tolerances(150d, NutrientCode.Sodium).getFirst(), DELTA);
	}

	@Test
	public void takesTheRoundingStepOfTheMeasuredValueAtABandBoundary() {
		assertEquals(6.4d, tolerances(5.3d, NutrientCode.Fat).getFirst(), DELTA);
		assertEquals(0.35d, tolerances(0.46d, NutrientCode.Protein).getSecond(), DELTA);
	}

	@Test
	public void computesTheLimitsOnTheServingAndBringsThemBackPerHundredGrams() {
		NutrientToleranceCriteria thirtyGramServing = NutrientToleranceCriteria.NONE.withServingSize(0.03d);

		assertEquals(37.33d, regulation.tolerances(30d, NutrientCode.Fat, null, thirtyGramServing).getFirst(), DELTA);
	}

	@Test
	public void basesTheToleranceOfAZeroDeclarationOnTheMaximumPreRoundedValue() {
		assertEquals(0.5988d, tolerances(0.3d, NutrientCode.Sugar).getFirst(), DELTA);
	}

	@Test
	public void boundsANutrientToPromoteBelowOnly() {
		Pair<Double, Double> iron = tolerances(1.75d, NutrientCode.Iron);

		assertNull(iron.getFirst());
		assertEquals(1.275d, iron.getSecond(), DELTA);
	}

	@Test
	public void subtractsTwentyPercentOfTheDeclaredValueFromTheMinimumPreRoundedValue() {
		assertEquals(0.03d, tolerances(0.1d, NutrientCode.Protein).getSecond(), DELTA);
		assertEquals(4.3d, tolerances(6d, NutrientCode.FatPolyunsaturated).getSecond(), DELTA);
		assertEquals(175d, tolerances(250d, NutrientCode.Potassium).getSecond(), DELTA);
		assertEquals(0.0275d, tolerances(0.05d, NutrientCode.Thiamin).getSecond(), DELTA);
	}

	@Test
	public void neverGivesANegativeMinimum() {
		assertEquals(0d, tolerances(0.02d, NutrientCode.Protein).getSecond(), DELTA);
	}

	@Test
	public void givesNoToleranceToAnAddedVitaminOrMineral() {
		Pair<Double, Double> iron = regulation.tolerances(2.5d, NutrientCode.Iron, null, ADDED);

		assertNull(iron.getFirst());
		assertEquals(2.25d, iron.getSecond(), DELTA);
	}

	@Test
	public void keepsAddedSodiumAsANutrientToLimit() {
		assertEquals(184d, regulation.tolerances(150d, NutrientCode.Sodium, null, ADDED).getFirst(), DELTA);
	}

	@Test
	public void keepsAnAddedMacronutrientInClassTwo() {
		assertEquals(4.3d, regulation.tolerances(6d, NutrientCode.Protein, null, ADDED).getSecond(), DELTA);
	}

	@Test
	public void convertsTheValueToTheUnitOfTheRegulation() {
		assertEquals(1.275d, regulation.tolerances(1750d, NutrientCode.Iron, "µg/100g", NutrientToleranceCriteria.NONE).getSecond(), DELTA);
	}

	@Test
	public void givesNoToleranceToANutrientTheTestDoesNotAssess() {
		assertNull(tolerances(1.2d, NutrientCode.Salt));
		assertNull(tolerances(12d, ALCOHOL));
	}
}
