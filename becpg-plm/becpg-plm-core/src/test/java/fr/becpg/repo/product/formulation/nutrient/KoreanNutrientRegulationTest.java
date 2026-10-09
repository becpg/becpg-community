package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Unit tests of the Korean compliance tolerances (MFDS food labelling standards), per 100 g.
 */
public class KoreanNutrientRegulationTest {

	private static final String REGULATION_PATH = "beCPG/databases/nuts/KoreanNutrientRegulation.csv";

	private static final double DELTA = 1e-9;

	private final KoreanNutrientRegulation regulation = new KoreanNutrientRegulation(REGULATION_PATH);

	private Pair<Double, Double> tolerances(double value, String nutrientTypeCode) {
		return regulation.tolerances(value, nutrientTypeCode, null, NutrientToleranceCriteria.NONE);
	}

	@Test
	public void boundsANutrientToLimitAtHundredAndTwentyPercent() {
		Pair<Double, Double> fat = tolerances(10.3d, NutrientCode.Fat);

		assertEquals(12d, fat.getFirst(), DELTA);
		assertNull(fat.getSecond());
		assertEquals(240d, tolerances(200d, NutrientCode.Sodium).getFirst(), DELTA);
	}

	@Test
	public void boundsANutrientToPromoteAtEightyPercent() {
		assertEquals(9.6d, tolerances(12.4d, NutrientCode.Protein).getSecond(), DELTA);
	}

	@Test
	public void widensALowContentToItsAbsoluteAllowance() {
		assertEquals(25d, tolerances(20d, NutrientCode.Sodium).getFirst(), DELTA);
		assertEquals(2.5d, tolerances(2d, NutrientCode.Sugar).getFirst(), DELTA);
	}

	@Test
	public void boundsAZeroDeclarationByItsAbsoluteAllowance() {
		assertEquals(0.5d, tolerances(0.3d, NutrientCode.Sugar).getFirst(), DELTA);
	}

	@Test
	public void boundsAZeroDeclarationWithoutAllowanceByItsThreshold() {
		assertEquals(0.5d, tolerances(0.3d, NutrientCode.Fat).getFirst(), DELTA);
	}

	@Test
	public void givesNoToleranceToANutrientTheRuleDoesNotAssess() {
		assertNull(tolerances(1.2d, NutrientCode.Salt));
	}
}
