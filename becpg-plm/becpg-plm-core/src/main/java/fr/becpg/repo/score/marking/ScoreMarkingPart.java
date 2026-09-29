/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

/**
 * <p>One part of a score marking, fully formatted in the locale of the marking.</p>
 *
 * @param code the code of the part, a nutrient code for the front of pack marks
 * @param name the wording of the part, "Fat"
 * @param amount the amount with its unit, "3.2g", empty when the score holds none
 * @param secondaryAmount a second statement of the amount, the energy in kcal under its kJ, may be null
 * @param level the verdict as a stable key, {@code low}, {@code medium} or {@code high}, null for the energy
 * @param verdict the wording of the verdict, "MED", null for the energy
 * @param share the share of the reference intake, "12% RI", null when the score holds none
 * @param energy whether the part is the energy, which the marks state without a verdict
 * @author matthieu
 */
public record ScoreMarkingPart(String code, String name, String amount, String secondaryAmount, String level, String verdict, String share, boolean energy) {

}
