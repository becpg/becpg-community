/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScorePart;

/**
 * Unit tests of {@link ScoreMarkingBuilder}: verdicts, wordings and figures of a marking.
 *
 * @author matthieu
 */
public class ScoreMarkingBuilderTest {

	private static final String MTL_DETAILS = """
			{"code":"MTL","scale":"Traffic","class":"High","parts":[
			{"code":"ENER-KJO","value":1046,"unit":"kJ","share":12.45},
			{"code":"FAT","label":"Medium","value":10.24,"share":14.63},
			{"code":"FASAT","label":"High","value":6.5},
			{"code":"SUGAR","label":"Low","value":2},
			{"code":"NACL","value":0.3,"contribution":1}]}""";

	private ScoreMarking build(Locale locale) {
		return new ScoreMarkingBuilder(locale).build(ScoreContext.parse(MTL_DETAILS));
	}

	private ScoreMarkingPart part(Locale locale, int index) {
		return build(locale).parts().get(index);
	}

	@Test
	public void testMarkingKeepsTheScoreAndTheOrderOfItsParts() {
		ScoreMarking marking = build(Locale.ENGLISH);

		assertEquals("MTL", marking.code());
		assertEquals("Traffic", marking.scale());
		assertEquals("High", marking.scoreClass());
		assertEquals(5, marking.parts().size());
		assertEquals("SUGAR", marking.parts().get(3).code());
	}

	@Test
	public void testVerdictIsReadFromTheLabelTheThresholdEnginePublishes() {
		assertEquals(ScoreMarkingBuilder.LEVEL_MEDIUM, part(Locale.ENGLISH, 1).level());
		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, part(Locale.ENGLISH, 2).level());
		assertEquals(ScoreMarkingBuilder.LEVEL_LOW, part(Locale.ENGLISH, 3).level());
	}

	@Test
	public void testVerdictEnteredInFrenchIsRecognised() {
		assertEquals(ScoreMarkingBuilder.LEVEL_LOW, ScoreMarkingBuilder.trafficLevel(new ScorePart("FAT").withLabel("Faible")));
		assertEquals(ScoreMarkingBuilder.LEVEL_MEDIUM, ScoreMarkingBuilder.trafficLevel(new ScorePart("FAT").withLabel("Moyen")));
		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, ScoreMarkingBuilder.trafficLevel(new ScorePart("FAT").withLabel("Élevé")));
	}

	@Test
	public void testVerdictFallsBackOnTheContributionOfAPointScheme() {
		assertEquals(ScoreMarkingBuilder.LEVEL_LOW, part(Locale.ENGLISH, 4).level());
		assertEquals(ScoreMarkingBuilder.LEVEL_MEDIUM, ScoreMarkingBuilder.trafficLevel(new ScorePart("FAT").withContribution(2d)));
		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, ScoreMarkingBuilder.trafficLevel(new ScorePart("FAT").withContribution(3d)));
	}

	@Test
	public void testVerdictIsHighWhenNothingTellsIt() {
		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, ScoreMarkingBuilder.trafficLevel(new ScorePart("FAT")));
	}

	@Test
	public void testWordingsFollowTheFsaMarkInEnglish() {
		ScoreMarkingPart fat = part(Locale.ENGLISH, 1);

		assertEquals("Fat", fat.name());
		assertEquals("MED", fat.verdict());
		assertEquals("Saturates", part(Locale.ENGLISH, 2).name());
	}

	@Test
	public void testWordingsAreTranslated() {
		ScoreMarkingPart fat = part(Locale.FRENCH, 1);

		assertEquals("Matières grasses", fat.name());
		assertEquals("MOYEN", fat.verdict());
	}

	@Test
	public void testUnknownPartIsNamedByItsCode() {
		ScoreContext score = ScoreContext.parse("{\"code\":\"X\",\"scale\":\"Traffic\",\"parts\":[{\"code\":\"POLYOL\",\"label\":\"Low\"}]}");

		assertEquals("POLYOL", new ScoreMarkingBuilder(Locale.ENGLISH).build(score).parts().get(0).name());
	}

	@Test
	public void testAmountDefaultsToGramsAndDropsTrailingZeros() {
		assertEquals("10.24g", part(Locale.ENGLISH, 1).amount());
		assertEquals("2g", part(Locale.ENGLISH, 3).amount());
	}

	@Test
	public void testAmountFollowsTheDecimalSeparatorOfTheLocale() {
		assertEquals("10,24g", part(Locale.FRENCH, 1).amount());
	}

	@Test
	public void testEnergyIsFlaggedAndKeepsItsOwnUnit() {
		ScoreMarkingPart energy = part(Locale.ENGLISH, 0);

		assertTrue(energy.energy());
		assertEquals("Energy", energy.name());
		assertEquals("1046kJ", energy.amount());
		assertFalse(part(Locale.ENGLISH, 1).energy());
	}

	@Test
	public void testShareIsStatedAsAWholePercentageOfTheReferenceIntake() {
		assertEquals("15% RI", part(Locale.ENGLISH, 1).share());
		assertEquals("15 % AR", part(Locale.FRENCH, 1).share());
	}

	@Test
	public void testShareIsAbsentWhenTheScoreHoldsNone() {
		assertNull(part(Locale.ENGLISH, 2).share());
	}

	@Test
	public void testPartWithoutValueHasNoAmount() {
		ScoreContext score = ScoreContext.parse("{\"code\":\"X\",\"scale\":\"Traffic\",\"parts\":[{\"code\":\"FAT\",\"label\":\"Low\"}]}");

		assertEquals("", new ScoreMarkingBuilder(Locale.ENGLISH).build(score).parts().get(0).amount());
	}

	@Test
	public void testMarkingCarriesTheVersionOfTheScore() {
		ScoreContext score = ScoreContext.parse("{\"code\":\"ECOVADIS\",\"version\":\"2024\",\"scale\":\"Grade\",\"class\":\"Gold\"}");

		assertEquals("2024", new ScoreMarkingBuilder(Locale.ENGLISH).build(score).version());
	}

	@Test
	public void testCaptionIsTheOneTheBundleGivesTheScore() {
		ScoreContext beauty = ScoreContext.parse("{\"code\":\"ECOBEAUTYSCORE\",\"scale\":\"Letter\",\"class\":\"A\"}");

		assertEquals("Impact environnemental", new ScoreMarkingBuilder(Locale.FRENCH).build(beauty).caption());
		assertNull(build(Locale.ENGLISH).caption());
	}

}
