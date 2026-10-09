package fr.becpg.repo.product.formulation.nutrient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Unit tests of the Chinese compliance tolerances (GB 28050-2011, table 2), per 100 g.
 */
public class ChineseNutrientRegulationTest {

	private static final String REGULATION_PATH = "beCPG/databases/nuts/ChineseNutrientRegulation.csv";

	private static final double DELTA = 1e-9;

	private final ChineseNutrientRegulation regulation = new ChineseNutrientRegulation(REGULATION_PATH);

	private Pair<Double, Double> tolerances(double value, String nutrientTypeCode) {
		return regulation.tolerances(value, nutrientTypeCode, null, NutrientToleranceCriteria.NONE);
	}

	@Test
	public void boundsANutrientToLimitAtHundredAndTwentyPercent() {
		Pair<Double, Double> fat = tolerances(10.34d, NutrientCode.Fat);

		assertEquals(12.36d, fat.getFirst(), DELTA);
		assertNull(fat.getSecond());
		assertEquals(1200d, tolerances(1000.4d, NutrientCode.EnergykJ).getFirst(), DELTA);
	}

	@Test
	public void boundsANutrientToPromoteAtEightyPercent() {
		Pair<Double, Double> protein = tolerances(8.04d, NutrientCode.Protein);

		assertNull(protein.getFirst());
		assertEquals(6.4d, protein.getSecond(), DELTA);
	}

	@Test
	public void boundsVitaminsAAndDOnBothSides() {
		Pair<Double, Double> vitaminD = tolerances(5d, NutrientCode.VitD);

		assertEquals(9d, vitaminD.getFirst(), DELTA);
		assertEquals(4d, vitaminD.getSecond(), DELTA);
	}

	@Test
	public void assessesAnAddedNutrientAsANutrientToPromote() {
		assertNull(tolerances(2d, NutrientCode.FatOmega3));
		assertEquals(1.6d, regulation.tolerances(2d, NutrientCode.FatOmega3, null, NutrientToleranceCriteria.ofLine(false, true)).getSecond(),
				DELTA);
	}

	@Test
	public void boundsAZeroDeclarationByTheThresholdBelowWhichZeroCanBeDeclared() {
		assertEquals(17d, tolerances(10d, NutrientCode.EnergykJ).getFirst(), DELTA);
	}

	@Test
	public void keepsSodiumAsANutrientToLimit() {
		assertNull(tolerances(400d, NutrientCode.Sodium).getSecond());
	}
}
