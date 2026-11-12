package fr.becpg.repo.score.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

/**
 * Unit tests of the range and of the condition of a score threshold.
 */
public class ScoreThresholdListDataItemTest {

	private static final String ENERGY = "ENER-E14";

	@Test
	public void excludesTheLowerBoundByDefault() {
		ScoreThresholdListDataItem threshold = threshold(400d);

		assertFalse(threshold.matches(400d));
		assertTrue(threshold.matches(400.1d));
	}

	@Test
	public void includesTheLowerBoundWhenAsked() {
		ScoreThresholdListDataItem threshold = threshold(400d);
		threshold.setLowerBoundIncluded(true);

		assertTrue(threshold.matches(400d));
		assertFalse(threshold.matches(399.9d));
	}

	@Test
	public void holdsWithoutCondition() {
		assertTrue(threshold(400d).conditionHolds(Map.of()));
	}

	@Test
	public void holdsInsideTheConditionRange() {
		ScoreThresholdListDataItem threshold = noCalorieCondition();

		assertTrue(threshold.conditionHolds(Map.of(ENERGY, 4d)));
		assertFalse(threshold.conditionHolds(Map.of(ENERGY, 4.1d)));
	}

	@Test
	public void excludesTheConditionLowerBound() {
		ScoreThresholdListDataItem threshold = threshold(1d);
		threshold.setConditionNutCode(ENERGY);
		threshold.setConditionLowerBound(4d);

		assertFalse(threshold.conditionHolds(Map.of(ENERGY, 4d)));
		assertTrue(threshold.conditionHolds(Map.of(ENERGY, 4.1d)));
	}

	@Test
	public void doesNotHoldWhenTheConditionIsUndocumented() {
		assertFalse(noCalorieCondition().conditionHolds(Map.of()));
	}

	private ScoreThresholdListDataItem noCalorieCondition() {
		ScoreThresholdListDataItem threshold = threshold(45d);
		threshold.setConditionNutCode(ENERGY);
		threshold.setConditionUpperBound(4d);
		return threshold;
	}

	private ScoreThresholdListDataItem threshold(double lowerBound) {
		ScoreThresholdListDataItem threshold = new ScoreThresholdListDataItem();
		threshold.setNutCode("NA");
		threshold.setLowerBound(lowerBound);
		return threshold;
	}

}
