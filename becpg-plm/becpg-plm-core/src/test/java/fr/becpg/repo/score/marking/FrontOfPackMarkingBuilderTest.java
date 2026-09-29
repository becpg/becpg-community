/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import fr.becpg.repo.score.ScoreContext;

/**
 * Unit tests of {@link FrontOfPackMarkingBuilder}: the UK front of pack marking of a technical sheet.
 *
 * @author matthieu
 */
public class FrontOfPackMarkingBuilderTest {

	/** Graded per 100 g: fat amber, saturates red, sugars green, salt amber. */
	private static final String PER_HUNDRED = """
			{"code":"MTL","scale":"Traffic","class":"High","parts":[
			{"code":"FAT","label":"Medium","value":10,"share":14.29},
			{"code":"FASAT","label":"High","value":6,"share":30},
			{"code":"SUGAR","label":"Low","value":4,"share":4.44},
			{"code":"NACL","label":"Medium","value":1,"share":16.67}]}""";

	/** A portion of the product breaches the per portion threshold of sugars and salt. */
	private static final String PER_PORTION = """
			{"code":"MTL_PORTION","scale":"Traffic","class":"High","parts":[
			{"code":"SUGAR","label":"High","value":30},
			{"code":"NACL","label":"High","value":2}]}""";

	private static final Map<String, Double> PER_SERVING = Map.of("ENER-KJO", 523d, "ENER-E14", 125d, "FAT", 5d, "FASAT", 3d, "SUGAR", 2d, "NACL",
			0.5d);

	private static final Map<String, Double> PER_100G = Map.of("ENER-KJO", 1046d, "ENER-E14", 250d);

	private static final Map<String, Double> INTAKES = Map.of("ENER-KJO", 8400d, "FAT", 70d, "SUGAR", 90d);

	private static FrontOfPackFacts facts(Double servingSize, boolean beverage, String perPortion) {
		return new FrontOfPackFacts(ScoreContext.parse(PER_HUNDRED), perPortion != null ? ScoreContext.parse(perPortion) : null, PER_SERVING,
				PER_100G, INTAKES, servingSize, beverage, beverage, beverage);
	}

	private static FrontOfPackFacts drinkDeclaredInGrams(Double servingSize) {
		return new FrontOfPackFacts(ScoreContext.parse(PER_HUNDRED), null, PER_SERVING, PER_100G, INTAKES, servingSize, true, false, false);
	}

	private static ScoreMarking build(FrontOfPackFacts facts) {
		return new FrontOfPackMarkingBuilder(Locale.ENGLISH).build(facts).orElseThrow();
	}

	private static ScoreMarkingPart part(ScoreMarking marking, String code) {
		for (ScoreMarkingPart part : marking.parts()) {
			if (code.equals(part.code())) {
				return part;
			}
		}
		throw new AssertionError("No part " + code);
	}

	@Test
	public void testMarkingIsNamedForTheReportToSelectIt() {
		ScoreMarking marking = build(facts(50d, false, null));

		assertEquals(FrontOfPackMarkingBuilder.CODE, marking.code());
		assertEquals("Traffic", marking.scale());
	}

	@Test
	public void testEnergyComesFirstInKilojoulesAndKilocalories() {
		ScoreMarkingPart energy = build(facts(50d, false, null)).parts().get(0);

		assertTrue(energy.energy());
		assertEquals("523kJ", energy.amount());
		assertEquals("125kcal", energy.secondaryAmount());
		assertEquals("6% RI", energy.share());
	}

	@Test
	public void testAmountsAndSharesArePerServing() {
		ScoreMarkingPart fat = part(build(facts(50d, false, null)), "FAT");

		assertEquals("5g", fat.amount());
		assertEquals("7% RI", fat.share());
	}

	@Test
	public void testShareFallsBackOnTheIntakeTheScoreWasGradedWith() {
		ScoreMarkingPart saturates = part(build(facts(50d, false, null)), "FASAT");

		assertEquals("15% RI", saturates.share());
	}

	@Test
	public void testColoursAreGradedPerHundredGrams() {
		ScoreMarking marking = build(facts(50d, false, PER_PORTION));

		assertEquals(ScoreMarkingBuilder.LEVEL_MEDIUM, part(marking, "FAT").level());
		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, part(marking, "FASAT").level());
		assertEquals("A portion of 50 g does not bring in the portion rule", ScoreMarkingBuilder.LEVEL_LOW, part(marking, "SUGAR").level());
	}

	@Test
	public void testLargePortionTurnsTheColourRed() {
		ScoreMarking marking = build(facts(120d, false, PER_PORTION));

		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, part(marking, "SUGAR").level());
		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, part(marking, "NACL").level());
		assertEquals("Fat stays below the portion threshold", ScoreMarkingBuilder.LEVEL_MEDIUM, part(marking, "FAT").level());
	}

	@Test
	public void testPortionOfExactlyOneHundredGramsDoesNotBringInThePortionRule() {
		assertEquals(ScoreMarkingBuilder.LEVEL_LOW, part(build(facts(100d, false, PER_PORTION)), "SUGAR").level());
	}

	@Test
	public void testDrinkPortionRuleStartsAboveOneHundredAndFiftyMillilitres() {
		assertEquals(ScoreMarkingBuilder.LEVEL_LOW, part(build(facts(120d, true, PER_PORTION)), "SUGAR").level());
		assertEquals(ScoreMarkingBuilder.LEVEL_HIGH, part(build(facts(250d, true, PER_PORTION)), "SUGAR").level());
	}

	@Test
	public void testCaptionStatesTheServing() {
		assertEquals("Each serving (30g) contains", build(facts(30d, false, null)).caption());
		assertEquals("Each serving (250ml) contains", build(facts(250d, true, null)).caption());
	}

	@Test
	public void testUnitsFollowWhatTheProductDeclaresNotItsGrading() {
		ScoreMarking marking = build(drinkDeclaredInGrams(30d));

		assertEquals("Each serving (30g) contains", marking.caption());
		assertEquals("Typical values per 100g: Energy 1046kJ/250kcal", marking.footnotes().get(1));
	}

	@Test
	public void testFootnotesStateTheIntakeAndTheTypicalEnergy() {
		assertEquals(List.of("% of an adult's reference intake", "Typical values per 100g: Energy 1046kJ/250kcal"), build(facts(30d, false, null)).footnotes());
	}

	@Test
	public void testWordingsAreTranslated() {
		ScoreMarking marking = new FrontOfPackMarkingBuilder(Locale.FRENCH).build(facts(30d, false, null)).orElseThrow();

		assertEquals("Chaque portion (30g) contient", marking.caption());
		assertEquals("% de l'apport de référence d'un adulte", marking.footnotes().get(0));
	}

	@Test
	public void testNoMarkingWithoutServingSize() {
		assertEquals(Optional.empty(), new FrontOfPackMarkingBuilder(Locale.ENGLISH).build(facts(null, false, null)));
		assertEquals(Optional.empty(), new FrontOfPackMarkingBuilder(Locale.ENGLISH).build(facts(0d, false, null)));
	}

	@Test
	public void testEnergyIsLeftOutWhenTheProductStatesNone() {
		FrontOfPackFacts facts = new FrontOfPackFacts(ScoreContext.parse(PER_HUNDRED), null, Map.of("FAT", 5d), Map.of(), Map.of(), 30d, false, false, false);

		ScoreMarking marking = build(facts);

		assertEquals("FAT", marking.parts().get(0).code());
		assertEquals(1, marking.footnotes().size());
		assertNull(part(marking, "SUGAR").share());
	}

}
