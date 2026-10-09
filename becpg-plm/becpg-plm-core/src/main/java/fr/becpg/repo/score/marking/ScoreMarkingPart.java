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
 * @param label the wording the score publishes for the part, a warning such as "ALTO EN AZUCARES"
 *            or the class of an axis, may be null
 * @param fill the share of the reference intake as a ratio from 0 to 1, clamped, which a battery is
 *            filled to, null when the score holds none
 * @author matthieu
 */
public record ScoreMarkingPart(String code, String name, String amount, String secondaryAmount, String level, String verdict, String share, boolean energy,
		String label, Double fill) {

	/**
	 * <p>A part without published wording nor fill, as the front of pack marks state them.</p>
	 *
	 * @param code the code of the part
	 * @param name the wording of the part
	 * @param amount the amount with its unit
	 * @param secondaryAmount a second statement of the amount, may be null
	 * @param level the verdict as a stable key, may be null
	 * @param verdict the wording of the verdict, may be null
	 * @param share the share of the reference intake, may be null
	 * @param energy whether the part is the energy
	 */
	public ScoreMarkingPart(String code, String name, String amount, String secondaryAmount, String level, String verdict, String share, boolean energy) {
		this(code, name, amount, secondaryAmount, level, verdict, share, energy, null, null);
	}

}
