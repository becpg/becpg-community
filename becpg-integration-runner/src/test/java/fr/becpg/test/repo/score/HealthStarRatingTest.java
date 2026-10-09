package fr.becpg.test.repo.score;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.score.HealthStarRating;
import fr.becpg.repo.product.formulation.score.HsrCategory;
import fr.becpg.repo.score.NutrientValueProvider;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Checks the Health Star Rating against the tables of the implementation guide.
 *
 * @author matthieu
 */
public class HealthStarRatingTest {

	private static final double PRECISION = 0.001d;

	private final Map<String, Double> nutrients = new LinkedHashMap<>();

	private HealthStarRating plugin;

	private ProductData product;

	@Before
	public void setUp() {
		nutrients.clear();
		product = new FinishedProductData();
		plugin = new HealthStarRating(new NutrientValueProvider(null) {
			@Override
			public Map<String, Double> extractNutrients(ProductData productData) {
				return nutrients;
			}
		}, null, null);
	}

	@Test
	public void testCategoryDefaultsToTheCatchAllOne() {
		assertEquals(HsrCategory.CATEGORY_2, HsrCategory.of(null));
		assertEquals(HsrCategory.CATEGORY_2, HsrCategory.of("unknown"));
		assertEquals(HsrCategory.CATEGORY_3D, HsrCategory.of("3D"));
	}

	@Test
	public void testUndocumentedBeverageEarnsFourStars() {
		product.setNutrientProfileCategory("1");

		// nothing documented: zero baseline, which is the best a score can reach in category 1
		// since the five and four and a half star rows are reserved to water by identity
		assertEquals(4d, stars(), PRECISION);
	}

	@Test
	public void testCategoryOneEnergyStartsAtOnePoint() {
		product.setNutrientProfileCategory("1");
		nutrients.put("ENER-KJO", 20d);

		// table 3 gives one point at 31 kJ or less, there being no zero point for energy
		assertEquals(1d, score(), PRECISION);
	}

	@Test
	public void testSugaryDrinkFallsToTheLowestRating() {
		product.setNutrientProfileCategory("1");
		nutrients.put("ENER-KJO", 300d);
		nutrients.put("SUGAR", 14d);

		// energy 10 points and sugars 10 points, that is 20, well past the 12 of half a star
		assertEquals(0.5d, stars(), PRECISION);
	}

	@Test
	public void testCategoryOneScoresNoSaturatedFatNorSodium() {
		product.setNutrientProfileCategory("1");
		nutrients.put("FASAT", 30d);
		nutrients.put("NA", 2000d);

		assertEquals("Category 1 ignores saturated fat and sodium", 0d, score(), PRECISION);
	}

	@Test
	public void testProteinIsLostAboveThirteenBaselinePointsWithoutFvnl() {
		product.setNutrientProfileCategory("2");
		nutrients.put("ENER-KJO", 2100d);
		nutrients.put("FASAT", 11.5d);
		nutrients.put("SUGAR", 50d);
		nutrients.put("NA", 700d);
		nutrients.put("PRO-", 25d);

		// baseline 6 + 11 + 12 + 7 = 36, so the eleven protein points are lost
		assertEquals(36d, score(), PRECISION);
	}

	@Test
	public void testProteinIsKeptWithFiveFvnlPoints() {
		product.setNutrientProfileCategory("2");
		nutrients.put("ENER-KJO", 2100d);
		nutrients.put("FASAT", 11.5d);
		nutrients.put("SUGAR", 50d);
		nutrients.put("NA", 700d);
		nutrients.put("PRO-", 25d);
		nutrients.put("FRUIT_VEGETABLE", 85d);

		// baseline 36, FVNL 85 percent earns 5 points, protein 25 g earns 11
		assertEquals(20d, score(), PRECISION);
	}

	@Test
	public void testDairyBeverageEarnsNoFibrePoints() {
		product.setNutrientProfileCategory("1D");
		nutrients.put("FIBTG", 20d);

		assertEquals("Categories 1 and 1D are not eligible to fibre points", 0d, score(), PRECISION);
	}

	@Test
	public void testOilUsesItsOwnSaturatedFatTable() {
		product.setNutrientProfileCategory("3");
		nutrients.put("FASAT", 15.5d);

		// table 2 gives 15 points at 15.5 g, where table 1 would have given 14
		assertEquals(15d, score(), PRECISION);
	}

	@Test
	public void testScoreConvertsToStarsPerCategory() {
		assertEquals(5d, HsrCategory.CATEGORY_2.stars(-11), PRECISION);
		assertEquals(4.5d, HsrCategory.CATEGORY_2.stars(-8), PRECISION);
		assertEquals(3d, HsrCategory.CATEGORY_2.stars(6), PRECISION);
		assertEquals(0.5d, HsrCategory.CATEGORY_2.stars(25), PRECISION);

		assertEquals(5d, HsrCategory.CATEGORY_3D.stars(24), PRECISION);
		assertEquals(0.5d, HsrCategory.CATEGORY_3D.stars(40), PRECISION);
	}

	private double stars() {
		return context().getValue();
	}

	private double score() {
		return context().getSteps().stream().filter(part -> "hsrScore".equals(part.getCode())).findFirst().orElseThrow().getContribution();
	}

	/**
	 * The context builder is private, the plugin publishing through the score writer, so the
	 * test reaches it by reflection rather than by standing up a repository.
	 *
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext context() {
		try {
			Method method = HealthStarRating.class.getDeclaredMethod("buildContext", ProductData.class, ScoreDefinitionItem.class);
			method.setAccessible(true);
			return (ScoreContext) method.invoke(plugin, product, new ScoreDefinitionItem());
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

}
