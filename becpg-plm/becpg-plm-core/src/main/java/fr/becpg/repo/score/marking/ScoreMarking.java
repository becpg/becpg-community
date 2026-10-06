/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.util.List;

/**
 * <p>A computed score laid out for its regulatory marking: a template only has to draw it.</p>
 *
 * <p>Model access in a template uses the method form ({@code score.parts()}): record accessors are
 * not JavaBean getters, so FreeMarker only sees them as methods.</p>
 *
 * @param code the score code, as held by {@code bcpg:scoreDefCode}
 * @param scale the scale the score is graded on, which names the default template
 * @param scoreClass the class reached by the score, may be null
 * @param parts the parts of the marking, in the order the score states them
 * @param caption the line stated above the parts, "Each serving (30 g) contains", may be null
 * @param footnotes the lines stated under the parts, empty when there are none
 * @param version the version of the score, the year a rating was granted for, may be null
 * @param value the value of the score formatted in the locale of the marking, "0.052", may be null
 * @param unit the unit of the value, "Pt", may be null
 * @param rating the value of the score as a number, the stars of a rating, may be null
 * @author matthieu
 */
public record ScoreMarking(String code, String scale, String scoreClass, List<ScoreMarkingPart> parts, String caption, List<String> footnotes,
		String version, String value, String unit, Double rating) {

	/**
	 * <p>A marking that states no value of its own, as the front of pack marking does.</p>
	 *
	 * @param code the score code
	 * @param scale the scale of the score
	 * @param scoreClass the class reached by the score, may be null
	 * @param parts the parts of the marking
	 * @param caption the line stated above the parts, may be null
	 * @param footnotes the lines stated under the parts
	 * @param version the version of the score, may be null
	 */
	public ScoreMarking(String code, String scale, String scoreClass, List<ScoreMarkingPart> parts, String caption, List<String> footnotes,
			String version) {
		this(code, scale, scoreClass, parts, caption, footnotes, version, null, null, null);
	}

	/**
	 * <p>A marking stating its parts alone, as the marking of a score does.</p>
	 *
	 * @param code the score code
	 * @param scale the scale of the score
	 * @param scoreClass the class reached by the score, may be null
	 * @param parts the parts of the marking
	 */
	public ScoreMarking(String code, String scale, String scoreClass, List<ScoreMarkingPart> parts) {
		this(code, scale, scoreClass, parts, null, List.of(), null, null, null, null);
	}

}
