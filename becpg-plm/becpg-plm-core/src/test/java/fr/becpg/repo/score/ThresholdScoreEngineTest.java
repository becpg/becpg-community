package fr.becpg.repo.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.GS1Model;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.NutrientCode;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.score.data.ScoreThresholdListDataItem;

/**
 * Unit tests of the sodium warning labels, from the nutrient list of a product to the
 * number of warnings.
 *
 * <p>The thresholds are the Mexican and Chilean ones of the referential: the sodium is read
 * through the EU rounding, which stores it in grams, and compared to bounds in milligrams.</p>
 */
public class ThresholdScoreEngineTest {

	private static final double PRECISION = 0.0001d;

	private static final String EXCESS_SODIUM = "EXCESO SODIO";

	private static final String HIGH_IN_SODIUM = "ALTO EN SODIO";

	private static final String BEVERAGES = "Beverages";

	private static final double NO_CALORIE_ENERGY = 4d;

	private final NodeRef sodium = new NodeRef("workspace://SpacesStore/sodium");

	private final NodeRef energy = new NodeRef("workspace://SpacesStore/energy");

	private ThresholdScoreEngine engine;

	private FinishedProductData product;

	@Before
	public void setUp() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales("en,fr");

		NodeService nodeService = mock(NodeService.class);
		when(nodeService.getProperty(sodium, GS1Model.PROP_NUTRIENT_TYPE_CODE)).thenReturn(NutrientCode.Sodium);
		when(nodeService.getProperty(energy, GS1Model.PROP_NUTRIENT_TYPE_CODE)).thenReturn(NutrientCode.Energykcal);
		engine = new ThresholdScoreEngine(new NutrientValueProvider(nodeService));

		product = new FinishedProductData();
		product.setUnit(ProductUnit.kg);
		product.setQty(1d);
	}

	@After
	public void tearDown() {
		MLTextHelper.flushCache();
	}

	@Test
	public void warnsOnTheSodiumPerKilocalorie() {
		// 437 mg for 124 kcal is 3.5 mg per kcal, below the 300 mg absolute bound
		withNutrients(437d, 124d);

		assertEquals(1d, mexico().getValue(), PRECISION);
	}

	@Test
	public void staysSilentBelowBothSodiumBounds() {
		// 200 mg for 400 kcal is 0.5 mg per kcal
		withNutrients(200d, 400d);

		assertEquals(0d, mexico().getValue(), PRECISION);
	}

	@Test
	public void warnsAtTheExactAbsoluteBound() {
		// 300 mg for 500 kcal is 0.6 mg per kcal: only the 300 mg bound, included, fires
		withNutrients(300d, 500d);

		assertEquals(1d, mexico().getValue(), PRECISION);
	}

	@Test
	public void judgesANoCalorieBeverageOnItsOwnBound() {
		// 15 mg for 1 kcal is 15 mg per kcal, yet below the 45 mg of the no calorie beverages
		product.setNutrientProfileCategory(BEVERAGES);
		withNutrients(15d, 1d);

		assertEquals(0d, mexico().getValue(), PRECISION);
	}

	@Test
	public void warnsOnANoCalorieBeverageAboveItsBound() {
		product.setNutrientProfileCategory(BEVERAGES);
		withNutrients(50d, 1d);

		assertEquals(1d, mexico().getValue(), PRECISION);
	}

	@Test
	public void excludesTheChileanBound() {
		withNutrients(400d, 300d);

		assertEquals(0d, chile().getValue(), PRECISION);
	}

	@Test
	public void warnsAboveTheChileanBound() {
		withNutrients(410d, 300d);

		assertEquals(1d, chile().getValue(), PRECISION);
	}

	@Test
	public void aProductBelowEveryBoundIsDocumented() {
		withNutrients(200d, 400d);

		assertTrue(engine.isDocumented(product, definition(List.of(threshold(400d, false, HIGH_IN_SODIUM)))));
	}

	@Test
	public void aProductWithoutTheNutrientIsNotDocumented() {
		product.setNutList(new ArrayList<>());

		assertFalse(engine.isDocumented(product, definition(List.of(threshold(400d, false, HIGH_IN_SODIUM)))));
	}

	private ScoreContext mexico() {
		ScoreThresholdListDataItem perKcal = threshold(1d, true, EXCESS_SODIUM);
		perKcal.setRatioNutCode(NutrientCode.Energykcal);
		perKcal.setRatioFactor(1d);
		perKcal.setConditionNutCode(NutrientCode.Energykcal);
		perKcal.setConditionLowerBound(NO_CALORIE_ENERGY);

		ScoreThresholdListDataItem noCalorieBeverage = threshold(45d, true, EXCESS_SODIUM);
		noCalorieBeverage.setCategory(BEVERAGES);
		noCalorieBeverage.setConditionNutCode(NutrientCode.Energykcal);
		noCalorieBeverage.setConditionUpperBound(NO_CALORIE_ENERGY);

		return engine.compute(product, definition(List.of(perKcal, threshold(300d, true, EXCESS_SODIUM), noCalorieBeverage)));
	}

	private ScoreContext chile() {
		return engine.compute(product, definition(List.of(threshold(400d, false, HIGH_IN_SODIUM))));
	}

	private void withNutrients(double sodiumInMilligrams, double energyInKcal) {
		List<NutListDataItem> nutList = new ArrayList<>();
		nutList.add(nutLine(sodium, NutrientCode.Sodium, sodiumInMilligrams, "mg/100g"));
		nutList.add(nutLine(energy, NutrientCode.Energykcal, energyInKcal, "kcal/100g"));
		product.setNutList(nutList);
	}

	private NutListDataItem nutLine(NodeRef nut, String code, double value, String unit) {
		NutListDataItem line = NutListDataItem.build().withNut(nut).withValue(value).withUnit(unit);
		RegulationFormulationHelper.extractRoundedValue(product, code, line);
		return line;
	}

	private ScoreDefinitionItem definition(List<ScoreThresholdListDataItem> thresholds) {
		ScoreDefinitionItem definition = new ScoreDefinitionItem();
		definition.setCode("WARNINGS");
		definition.setVersion("1.0");
		definition.setEngine(ScoreEngine.Threshold.name());
		definition.setScale(ScoreScale.Letter.name());
		definition.setAggregation(ScoreAggregation.Count.name());
		definition.setThresholdList(thresholds);
		return definition;
	}

	private ScoreThresholdListDataItem threshold(double lowerBound, boolean included, String result) {
		ScoreThresholdListDataItem threshold = new ScoreThresholdListDataItem();
		threshold.setNutCode(NutrientCode.Sodium);
		threshold.setLowerBound(lowerBound);
		threshold.setLowerBoundIncluded(included);
		threshold.setResult(result);
		return threshold;
	}

}
