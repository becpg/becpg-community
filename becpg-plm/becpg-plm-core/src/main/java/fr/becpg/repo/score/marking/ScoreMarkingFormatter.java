/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * <p>Figures of a marking, formatted in its locale: amounts with their unit, and shares of the
 * reference intake.</p>
 *
 * @author matthieu
 */
public class ScoreMarkingFormatter {

	private static final String AMOUNT_PATTERN = "0.##";

	private static final String WHOLE_PATTERN = "0";

	private static final String EMPTY = "";

	private final Locale locale;

	private final ScoreMarkingLabels labels;

	/**
	 * <p>Constructor for ScoreMarkingFormatter.</p>
	 *
	 * @param locale the locale of the marking
	 * @param labels the wordings of the marking
	 */
	public ScoreMarkingFormatter(Locale locale, ScoreMarkingLabels labels) {
		this.locale = locale;
		this.labels = labels;
	}

	/**
	 * <p>An amount followed by its unit, "3.2g", empty when there is none.</p>
	 *
	 * @param value the amount, may be null
	 * @param unit the unit, stated right after the figure
	 * @return the formatted amount
	 */
	public String amount(Double value, String unit) {
		return value != null ? format(value, AMOUNT_PATTERN) + unit : EMPTY;
	}

	/**
	 * <p>An amount rounded to the unit, as the energy is stated, "1046kJ".</p>
	 *
	 * @param value the amount, may be null
	 * @param unit the unit
	 * @return the formatted amount
	 */
	public String wholeAmount(Double value, String unit) {
		return value != null ? format(value, WHOLE_PATTERN) + unit : EMPTY;
	}

	/**
	 * <p>A share of the reference intake, "12% RI", null when there is none.</p>
	 *
	 * @param share the share in percent, may be null
	 * @return the statement of the share
	 */
	public String share(Double share) {
		return share != null ? labels.referenceIntake(format(share, WHOLE_PATTERN)) : null;
	}

	private String format(Double value, String pattern) {
		return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(locale)).format(value);
	}

}
