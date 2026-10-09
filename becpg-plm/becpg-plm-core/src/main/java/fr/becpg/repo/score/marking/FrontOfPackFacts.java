/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.util.Map;

import fr.becpg.repo.score.ScoreContext;

/**
 * <p>What the UK front of pack marking of a product is drawn from.</p>
 *
 * @param perHundred the Multiple Traffic Lights score, graded per 100 g, which gives the colours
 * @param perPortion the Multiple Traffic Lights per portion score, may be null
 * @param perServing the nutrients per serving, by nutrient code
 * @param perHundredValues the nutrients per 100 g, by nutrient code
 * @param referenceIntakes the reference intakes, by nutrient code
 * @param servingSize the serving size in grams, null when the product declares none
 * @param beverage whether the product is graded as a drink, whose portion rule starts at 150 ml
 * @param servedInVolume whether the serving size is declared in a volume unit, which the caption states
 * @param nutrientsPerVolume whether the nutrients are declared per 100 ml, which the typical values state
 * @author matthieu
 */
public record FrontOfPackFacts(ScoreContext perHundred, ScoreContext perPortion, Map<String, Double> perServing, Map<String, Double> perHundredValues,
		Map<String, Double> referenceIntakes, Double servingSize, boolean beverage, boolean servedInVolume, boolean nutrientsPerVolume) {

}
