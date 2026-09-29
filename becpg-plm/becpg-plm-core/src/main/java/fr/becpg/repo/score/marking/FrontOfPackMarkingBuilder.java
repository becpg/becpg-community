/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreScale;

/**
 * <p>Lays out the UK front of pack marking a technical sheet prints, as the Food Standards Agency
 * guidance describes it: the amounts and the shares of the reference intake per serving, the
 * colours graded per 100 g.</p>
 *
 * <p>A colour is turned red by the portion as well, but only above 100 g, or 150 ml for a drink:
 * the per portion thresholds do not apply below. The verdicts come from the Multiple Traffic
 * Lights scores, so the thresholds are those of the score repository and nowhere else.</p>
 *
 * @author matthieu
 */
public class FrontOfPackMarkingBuilder {

	/** Constant <code>CODE="MTL_FOP"</code>, the code the report selects the marking by. */
	public static final String CODE = "MTL_FOP";

	private static final String ENERGY_KJ = "ENER-KJO";

	private static final String ENERGY_KCAL = "ENER-E14";

	private static final String KJ = "kJ";

	private static final String KCAL = "kcal";

	private static final String ENERGY_SEPARATOR = "/";

	private static final double FOOD_PORTION_LIMIT = 100d;

	private static final double DRINK_PORTION_LIMIT = 150d;

	private static final double PERCENT = 100d;

	private static final String CAPTION_KEY = "score.marking.fop.caption";

	private static final String CAPTION_FALLBACK = "Each serving ({0}) contains";

	private static final String INTAKE_NOTE_KEY = "score.marking.fop.referenceIntakeNote";

	/** A MessageFormat pattern like every wording of the bundle, hence the doubled quote. */
	private static final String INTAKE_NOTE_FALLBACK = "% of an adult''s reference intake";

	private static final String TYPICAL_VALUES_KEY = "score.marking.fop.typicalValues";

	private static final String TYPICAL_VALUES_FALLBACK = "Typical values per {0}: Energy {1}";

	private static final String HUNDRED_GRAMS = "100g";

	private static final String HUNDRED_MILLILITRES = "100ml";

	private static final String MILLILITRES = "ml";

	private final ScoreMarkingLabels labels;

	private final ScoreMarkingFormatter formatter;

	/**
	 * <p>Constructor for FrontOfPackMarkingBuilder.</p>
	 *
	 * @param locale the locale of the marking
	 */
	public FrontOfPackMarkingBuilder(Locale locale) {
		Locale markingLocale = locale != null ? locale : Locale.ENGLISH;
		this.labels = new ScoreMarkingLabels(markingLocale);
		this.formatter = new ScoreMarkingFormatter(markingLocale, labels);
	}

	/**
	 * <p>Builds the front of pack marking of a product.</p>
	 *
	 * @param facts what the marking is drawn from
	 * @return the marking, empty without a serving size, which the marking is stated per
	 */
	public Optional<ScoreMarking> build(FrontOfPackFacts facts) {
		if ((facts.servingSize() == null) || (facts.servingSize() <= 0d)) {
			return Optional.empty();
		}

		List<ScoreMarkingPart> parts = new ArrayList<>();
		energyPart(facts).ifPresent(parts::add);
		for (ScorePart part : facts.perHundred().getParts()) {
			parts.add(nutrientPart(part, facts));
		}

		return Optional.of(new ScoreMarking(CODE, ScoreScale.Traffic.name(), facts.perHundred().getScoreClass(), parts, caption(facts), footnotes(facts),
				facts.perHundred().getVersion()));
	}

	private Optional<ScoreMarkingPart> energyPart(FrontOfPackFacts facts) {
		Double kilojoules = facts.perServing().get(ENERGY_KJ);
		if (kilojoules == null) {
			return Optional.empty();
		}
		String kilocalories = facts.perServing().containsKey(ENERGY_KCAL) ? formatter.wholeAmount(facts.perServing().get(ENERGY_KCAL), KCAL) : null;
		return Optional.of(new ScoreMarkingPart(ENERGY_KJ, labels.partName(ENERGY_KJ), formatter.wholeAmount(kilojoules, KJ), kilocalories, null, null,
				formatter.share(share(ENERGY_KJ, kilojoules, facts)), true));
	}

	private ScoreMarkingPart nutrientPart(ScorePart part, FrontOfPackFacts facts) {
		String level = level(part, facts);
		Double amount = facts.perServing().get(part.getCode());
		return new ScoreMarkingPart(part.getCode(), labels.partName(part.getCode()), formatter.amount(amount, ScoreMarkingBuilder.DEFAULT_MASS_UNIT),
				null, level, labels.verdict(level), formatter.share(share(part.getCode(), amount, facts)), false);
	}

	/**
	 * <p>Colour of a nutrient: graded per 100 g, turned red by a large portion.</p>
	 *
	 * @param part the part of the score per 100 g
	 * @param facts what the marking is drawn from
	 * @return {@code low}, {@code medium} or {@code high}
	 */
	static String level(ScorePart part, FrontOfPackFacts facts) {
		String level = ScoreMarkingBuilder.trafficLevel(part);
		if (!ScoreMarkingBuilder.LEVEL_HIGH.equals(level) && portionRuleApplies(facts) && isHighPerPortion(part.getCode(), facts.perPortion())) {
			return ScoreMarkingBuilder.LEVEL_HIGH;
		}
		return level;
	}

	private static boolean portionRuleApplies(FrontOfPackFacts facts) {
		return facts.servingSize() > (facts.beverage() ? DRINK_PORTION_LIMIT : FOOD_PORTION_LIMIT);
	}

	private static boolean isHighPerPortion(String code, ScoreContext perPortion) {
		if (perPortion == null) {
			return false;
		}
		for (ScorePart part : perPortion.getParts()) {
			if (code.equals(part.getCode()) && ScoreMarkingBuilder.LEVEL_HIGH.equals(ScoreMarkingBuilder.trafficLevel(part))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The reference intake of the characteristic first, then the one the score was graded with,
	 * which is what the thresholds of the score repository hold.
	 */
	private static Double share(String code, Double amount, FrontOfPackFacts facts) {
		Double intake = facts.referenceIntakes().get(code);
		if ((intake == null) || (intake == 0d)) {
			intake = intakeOfScore(code, facts.perHundred());
		}
		return (amount != null) && (intake != null) ? (amount / intake) * PERCENT : null;
	}

	private static Double intakeOfScore(String code, ScoreContext score) {
		for (ScorePart part : score.getParts()) {
			if (code.equals(part.getCode()) && (part.getShare() != null) && (part.getShare() != 0d) && (part.getValue() != null)) {
				return (part.getValue() / part.getShare()) * PERCENT;
			}
		}
		return null;
	}

	private String caption(FrontOfPackFacts facts) {
		return labels.format(CAPTION_KEY, CAPTION_FALLBACK, formatter.amount(facts.servingSize(), facts.beverage() ? MILLILITRES : ScoreMarkingBuilder.DEFAULT_MASS_UNIT));
	}

	private List<String> footnotes(FrontOfPackFacts facts) {
		List<String> footnotes = new ArrayList<>(2);
		footnotes.add(labels.format(INTAKE_NOTE_KEY, INTAKE_NOTE_FALLBACK));
		String energy = typicalEnergy(facts);
		if (energy != null) {
			footnotes.add(labels.format(TYPICAL_VALUES_KEY, TYPICAL_VALUES_FALLBACK, facts.beverage() ? HUNDRED_MILLILITRES : HUNDRED_GRAMS, energy));
		}
		return footnotes;
	}

	private String typicalEnergy(FrontOfPackFacts facts) {
		Double kilojoules = facts.perHundredValues().get(ENERGY_KJ);
		if (kilojoules == null) {
			return null;
		}
		String energy = formatter.wholeAmount(kilojoules, KJ);
		Double kilocalories = facts.perHundredValues().get(ENERGY_KCAL);
		return kilocalories != null ? String.join(ENERGY_SEPARATOR, energy, formatter.wholeAmount(kilocalories, KCAL)) : energy;
	}

}
