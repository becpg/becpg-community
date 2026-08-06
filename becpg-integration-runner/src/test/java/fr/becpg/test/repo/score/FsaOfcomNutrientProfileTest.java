package fr.becpg.test.repo.score;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.FsaOfcomNutrientProfile;
import fr.becpg.repo.score.NutrientValueProvider;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Checks the UK nutrient profiling model against the worked examples of the technical
 * guidance.
 *
 * @author matthieu
 */
public class FsaOfcomNutrientProfileTest {

	private static final double PRECISION = 0.001d;

	private final Map<String, Double> nutrients = new LinkedHashMap<>();

	private FsaOfcomNutrientProfile plugin;

	private ProductData product;

	@Before
	public void setUp() {
		nutrients.clear();
		product = new FinishedProductData();
		plugin = new FsaOfcomNutrientProfile(new NutrientValueProvider(null) {
			@Override
			public Map<String, Double> extractNutrients(ProductData productData) {
				return nutrients;
			}
		}, null, null);
	}

	@Test
	public void testPointsAreCountedOnEachNutrient() {
		// 1000 kJ is 2 points, 3.5 g of saturates 3 points, 20 g of sugars 4 points,
		// 400 mg of sodium 4 points, that is 13 A points
		nutrients.put("ENER-KJO", 1000d);
		nutrients.put("FASAT", 3.5d);
		nutrients.put("SUGAR", 20d);
		nutrients.put("NA", 400d);

		assertEquals(13d, score(), PRECISION);
	}

	@Test
	public void testCPointsAreSubtracted() {
		nutrients.put("ENER-KJO", 400d);
		nutrients.put("FASAT", 1.5d);
		nutrients.put("SUGAR", 5d);
		nutrients.put("NA", 100d);
		nutrients.put("FRUIT_VEGETABLE", 70d);
		nutrients.put("FIBTG", 3d);
		nutrients.put("PRO-", 5d);

		// A points: energy 1, saturates 1, sugars 1, sodium 1, that is 4
		// C points: fruit 2, AOAC fibre 3, protein 3, that is 8
		assertEquals(-4d, score(), PRECISION);
	}

	@Test
	public void testProteinIsLostAboveElevenAPointsWithoutFullFruitPoints() {
		nutrients.put("ENER-KJO", 3400d);
		nutrients.put("FASAT", 11d);
		nutrients.put("SUGAR", 46d);
		nutrients.put("NA", 950d);
		nutrients.put("PRO-", 10d);

		// 40 A points, protein would have earned 5 but the product earns no fruit points
		assertEquals(40d, score(), PRECISION);
	}

	@Test
	public void testProteinIsKeptAboveElevenAPointsWithFullFruitPoints() {
		nutrients.put("ENER-KJO", 3400d);
		nutrients.put("FASAT", 11d);
		nutrients.put("SUGAR", 46d);
		nutrients.put("NA", 950d);
		nutrients.put("PRO-", 10d);
		nutrients.put("FRUIT_VEGETABLE", 85d);

		// 40 A points, fruit 5 and protein 5 are both counted
		assertEquals(30d, score(), PRECISION);
	}

	@Test
	public void testSaltIsConvertedToSodium() {
		// 1 g of salt is 400 mg of sodium, that is 4 points
		nutrients.put("NACL", 1d);

		assertEquals(4d, score(), PRECISION);
	}

	@Test
	public void testAoacFibreWinsOverNsp() {
		nutrients.put("FIBTG", 5d);
		nutrients.put("PSACNS", 0.5d);

		// AOAC 5 g earns 5 points, NSP 0.5 g would have earned none
		assertEquals(-5d, score(), PRECISION);
	}

	@Test
	public void testUndocumentedNutrientsScoreNothing() {
		assertEquals(0d, score(), PRECISION);
	}

	/**
	 * The context builder is private, the plugin publishing through the score writer, so the
	 * test reaches it by reflection rather than by standing up a repository.
	 *
	 * @return the overall score of the product
	 */
	private double score() {
		try {
			Method method = FsaOfcomNutrientProfile.class.getDeclaredMethod("buildContext", ProductData.class, ScoreDefinitionItem.class);
			method.setAccessible(true);
			ScoreContext context = (ScoreContext) method.invoke(plugin, product, new ScoreDefinitionItem());
			return context.getValue();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

}
