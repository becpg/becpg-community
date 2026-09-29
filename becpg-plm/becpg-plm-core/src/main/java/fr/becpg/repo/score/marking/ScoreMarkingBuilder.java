/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScorePart;

/**
 * <p>Lays a computed score out for its marking: every wording resolved and every figure formatted
 * in the locale of the marking, so that a template only has to place them.</p>
 *
 * <p>The verdict of a part is read the way the threshold engine publishes it, as the label of the
 * part; the contribution only serves the schemes that score in points. This is the rule
 * {@code score-badge.js} applies on screen, so both draw the same verdict.</p>
 *
 * @author matthieu
 */
public class ScoreMarkingBuilder {

	/** Constant <code>LEVEL_LOW="low"</code> */
	public static final String LEVEL_LOW = "low";

	/** Constant <code>LEVEL_MEDIUM="medium"</code> */
	public static final String LEVEL_MEDIUM = "medium";

	/** Constant <code>LEVEL_HIGH="high"</code> */
	public static final String LEVEL_HIGH = "high";

	/** Wordings a threshold may name its verdict with, customers entering theirs in French. */
	private static final Map<String, String> LEVEL_PREFIXES = Map.of(LEVEL_LOW, LEVEL_LOW, "faible", LEVEL_LOW, LEVEL_MEDIUM, LEVEL_MEDIUM,
			"moyen", LEVEL_MEDIUM, LEVEL_HIGH, LEVEL_HIGH, "\u00e9lev", LEVEL_HIGH);

	/** Energy carries no verdict on the front of pack marks, it is stated on a plain panel. */
	static final Set<String> ENERGY_CODES = Set.of("ENER-KJO", "ENER-KJ", "ENER-E14", "ENER-KCAL", "ENERGY");

	static final String DEFAULT_MASS_UNIT = "g";

	private static final String EMPTY = "";

	private final ScoreMarkingLabels labels;

	private final ScoreMarkingFormatter formatter;

	/**
	 * <p>Constructor for ScoreMarkingBuilder.</p>
	 *
	 * @param locale the locale of the marking
	 */
	public ScoreMarkingBuilder(Locale locale) {
		Locale markingLocale = locale != null ? locale : Locale.ENGLISH;
		this.labels = new ScoreMarkingLabels(markingLocale);
		this.formatter = new ScoreMarkingFormatter(markingLocale, labels);
	}

	/**
	 * <p>Builds the marking of a computed score.</p>
	 *
	 * @param score the computed score, as parsed from {@code bcpg:rslDetails}
	 * @return the marking, without parts when the score holds none
	 */
	public ScoreMarking build(ScoreContext score) {
		List<ScoreMarkingPart> parts = new ArrayList<>(score.getParts().size());
		for (ScorePart part : score.getParts()) {
			parts.add(buildPart(part));
		}
		return new ScoreMarking(score.getCode(), score.getScale(), score.getScoreClass(), parts, labels.caption(score.getCode()), List.of(),
				score.getVersion());
	}

	private ScoreMarkingPart buildPart(ScorePart part) {
		boolean energy = ENERGY_CODES.contains(part.getCode());
		String level = trafficLevel(part);
		return new ScoreMarkingPart(part.getCode(), labels.partName(part.getCode()), amount(part, energy), null, level, labels.verdict(level),
				formatter.share(part.getShare()), energy);
	}

	/**
	 * <p>Verdict of a part as a stable key.</p>
	 *
	 * @param part the part of the score
	 * @return {@code low}, {@code medium} or {@code high}, the latter when nothing tells
	 */
	public static String trafficLevel(ScorePart part) {
		String label = part.getLabel() != null ? part.getLabel().toLowerCase(Locale.ROOT) : EMPTY;

		for (Map.Entry<String, String> prefix : LEVEL_PREFIXES.entrySet()) {
			if (label.startsWith(prefix.getKey())) {
				return prefix.getValue();
			}
		}
		return levelFromContribution(part.getContribution());
	}

	private static String levelFromContribution(Double contribution) {
		if (contribution == null) {
			return LEVEL_HIGH;
		}
		if (contribution <= 1d) {
			return LEVEL_LOW;
		}
		return contribution <= 2d ? LEVEL_MEDIUM : LEVEL_HIGH;
	}

	private String amount(ScorePart part, boolean energy) {
		String unit = part.getUnit() != null ? part.getUnit() : defaultUnit(energy);
		return formatter.amount(part.getValue(), unit);
	}

	private static String defaultUnit(boolean energy) {
		return energy ? EMPTY : DEFAULT_MASS_UNIT;
	}

}
