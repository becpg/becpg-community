package fr.becpg.test.repo.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.score.NutrientValueProvider;
import fr.becpg.repo.score.ScoreAggregation;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreScale;
import fr.becpg.repo.score.ThresholdScoreEngine;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.score.data.ScoreThresholdListDataItem;

/**
 * Checks the engine serving the front of pack schemes driven by nutrient thresholds.
 *
 * <p>The nutrients are injected rather than read from a product, so the arithmetic of each
 * scheme is checked without a repository.</p>
 *
 * @author matthieu
 */
public class ThresholdScoreEngineTest {

	private static final double PRECISION = 0.01d;

	private static final String SUGAR = "SUGAR";
	private static final String SATFAT = "FASAT";
	private static final String SALT = "NACL";
	private static final String ENERGY = "ENER-KCA";

	private final Map<String, Double> nutrients = new LinkedHashMap<>();

	private ThresholdScoreEngine engine;

	private ProductData product;

	@Before
	public void setUp() {
		nutrients.clear();
		product = new FinishedProductData();
		engine = new ThresholdScoreEngine(new NutrientValueProvider(null) {
			@Override
			public Map<String, Double> extractNutrients(ProductData productData) {
				return nutrients;
			}
		});
	}

	@Test
	public void testTrafficLightsPublishOneVerdictPerNutrient() {
		nutrients.put(SUGAR, 30d);
		nutrients.put(SALT, 0.2d);

		ScoreDefinitionItem definition = definition(ScoreAggregation.None, null);
		definition.setThresholdList(List.of(threshold(SUGAR, null, 5d, "Medium"), threshold(SUGAR, 22.5d, null, "High"),
				threshold(SALT, null, 0.3d, "Low"), threshold(SALT, 1.5d, null, "High")));

		ScoreContext context = engine.compute(product, definition);

		assertEquals(2, context.getParts().size());
		assertEquals("High", partOf(context, SUGAR).getLabel());
		assertEquals("Low", partOf(context, SALT).getLabel());
		assertNull("Traffic lights have no single value", context.getValue());
	}

	@Test
	public void testWorstVerdictDecidesTheGrade() {
		nutrients.put(SUGAR, 7d);
		nutrients.put(SATFAT, 0.5d);

		ScoreDefinitionItem definition = definition(ScoreAggregation.Worst, "A,B,C,D");
		definition.setThresholdList(List.of(threshold(SUGAR, 5d, 10d, "C"), threshold(SATFAT, null, 0.7d, "A")));

		ScoreContext context = engine.compute(product, definition);

		assertEquals("C", context.getScoreClass());
	}

	@Test
	public void testWarningsAreCounted() {
		nutrients.put(ENERGY, 300d);
		nutrients.put(SUGAR, 12d);
		nutrients.put(SATFAT, 1d);

		ScoreDefinitionItem definition = definition(ScoreAggregation.Count, null);
		definition.setThresholdList(List.of(threshold(ENERGY, 275d, null, "ALTO EN CALORIAS"), threshold(SUGAR, 10d, null, "ALTO EN AZUCARES"),
				threshold(SATFAT, 4d, null, "ALTO EN GRASAS SATURADAS")));

		ScoreContext context = engine.compute(product, definition);

		assertEquals(2d, context.getValue(), PRECISION);
		assertEquals("2", context.getScoreClass());
	}

	@Test
	public void testReferenceIntakeIsExpressedAsAShare() {
		nutrients.put(SALT, 3d);

		ScoreDefinitionItem definition = definition(ScoreAggregation.None, null);
		ScoreThresholdListDataItem threshold = threshold(SALT, null, null, "Salt");
		threshold.setReferenceIntake(6d);
		definition.setThresholdList(List.of(threshold));

		ScoreContext context = engine.compute(product, definition);

		assertEquals(50d, partOf(context, SALT).getValue(), PRECISION);
		assertEquals("%", partOf(context, SALT).getUnit());
	}

	@Test
	public void testUndocumentedNutrientsAreSkipped() {
		ScoreDefinitionItem definition = definition(ScoreAggregation.Count, null);
		definition.setThresholdList(List.of(threshold(SUGAR, 10d, null, "ALTO EN AZUCARES")));

		ScoreContext context = engine.compute(product, definition);

		assertEquals(0, context.getParts().size());
		assertEquals(0d, context.getValue(), PRECISION);
	}

	@Test
	public void testCategoryThresholdsWin() {
		nutrients.put(SUGAR, 6d);
		product.setNutrientProfileCategory("Beverages");

		ScoreDefinitionItem definition = definition(ScoreAggregation.None, null);
		List<ScoreThresholdListDataItem> thresholds = new ArrayList<>();
		thresholds.add(threshold(SUGAR, 2.5d, 11.25d, "Medium", "Beverages"));
		thresholds.add(threshold(SUGAR, 5d, 22.5d, "Medium solid"));
		definition.setThresholdList(thresholds);

		ScoreContext context = engine.compute(product, definition);

		assertEquals(1, context.getParts().size());
		assertEquals("Medium", partOf(context, SUGAR).getLabel());
	}

	private ScoreDefinitionItem definition(ScoreAggregation aggregation, String classOrder) {
		ScoreDefinitionItem definition = new ScoreDefinitionItem();
		definition.setCode("TEST");
		definition.setVersion("1.0");
		definition.setEngine(ScoreEngine.Threshold.name());
		definition.setScale(ScoreScale.Letter.name());
		definition.setAggregation(aggregation.name());
		definition.setClassOrder(classOrder);
		return definition;
	}

	private ScoreThresholdListDataItem threshold(String nutCode, Double lower, Double upper, String result) {
		return threshold(nutCode, lower, upper, result, null);
	}

	private ScoreThresholdListDataItem threshold(String nutCode, Double lower, Double upper, String result, String category) {
		ScoreThresholdListDataItem threshold = new ScoreThresholdListDataItem();
		threshold.setNutCode(nutCode);
		threshold.setLowerBound(lower);
		threshold.setUpperBound(upper);
		threshold.setResult(result);
		threshold.setCategory(category);
		return threshold;
	}

	private ScorePart partOf(ScoreContext context, String code) {
		for (ScorePart part : context.getParts()) {
			if (code.equals(part.getCode())) {
				return part;
			}
		}
		throw new IllegalStateException("No part for " + code);
	}

}
