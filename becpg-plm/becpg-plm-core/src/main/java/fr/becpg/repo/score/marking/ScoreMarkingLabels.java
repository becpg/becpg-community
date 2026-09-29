/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Wordings of the score markings, in the locale the marking is drawn for.</p>
 *
 * <p>The bundle is read without falling back on the default locale of the server: a marking
 * drawn for a French report must fall back on the base wording, never on whatever language the
 * JVM happens to run in.</p>
 *
 * @author matthieu
 */
public class ScoreMarkingLabels {

	private static final Log logger = LogFactory.getLog(ScoreMarkingLabels.class);

	private static final String BUNDLE_NAME = "alfresco/module/becpg-plm-core/messages/score-marking";

	private static final ResourceBundle.Control NO_DEFAULT_LOCALE_FALLBACK = ResourceBundle.Control
			.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

	private static final String PART_KEY_PREFIX = "score.marking.part.";

	private static final String VERDICT_KEY_PREFIX = "score.marking.verdict.";

	private static final String REFERENCE_INTAKE_KEY = "score.marking.referenceIntake";

	private static final String CAPTION_KEY_PREFIX = "score.marking.caption.";

	private static final String REFERENCE_INTAKE_FALLBACK = "{0}% RI";

	private final Locale locale;

	private final ResourceBundle bundle;

	/**
	 * <p>Constructor for ScoreMarkingLabels.</p>
	 *
	 * @param locale the locale of the marking, English when null
	 */
	public ScoreMarkingLabels(Locale locale) {
		this.locale = locale != null ? locale : Locale.ENGLISH;
		this.bundle = loadBundle(this.locale);
	}

	/**
	 * <p>Wording of a part, its code when the bundle does not name it.</p>
	 *
	 * @param code the code of the part
	 * @return the wording of the part
	 */
	public String partName(String code) {
		return message(PART_KEY_PREFIX + code, code);
	}

	/**
	 * <p>Wording of a verdict, the level itself in capitals when the bundle does not name it.</p>
	 *
	 * @param level the verdict key, {@code low}, {@code medium} or {@code high}
	 * @return the wording of the verdict
	 */
	public String verdict(String level) {
		return message(VERDICT_KEY_PREFIX + level, level.toUpperCase(locale));
	}

	/**
	 * <p>Caption a marking states under its name, the kind of impact it grades.</p>
	 *
	 * @param code the score code
	 * @return the caption, null when the marking states none
	 */
	public String caption(String code) {
		return message(CAPTION_KEY_PREFIX + code, null);
	}

	/**
	 * <p>Statement of a share of the reference intake, "12% RI".</p>
	 *
	 * @param share the share, already formatted
	 * @return the statement of the share
	 */
	public String referenceIntake(String share) {
		return format(REFERENCE_INTAKE_KEY, REFERENCE_INTAKE_FALLBACK, share);
	}

	/**
	 * <p>A wording of the markings stating figures, {0} being the first of them.</p>
	 *
	 * @param key the key of the wording
	 * @param fallback the pattern used when the bundle holds none
	 * @param arguments the figures, already formatted
	 * @return the wording
	 */
	public String format(String key, String fallback, Object... arguments) {
		return new MessageFormat(message(key, fallback), locale).format(arguments);
	}

	private String message(String key, String fallback) {
		return (bundle != null) && bundle.containsKey(key) ? bundle.getString(key) : fallback;
	}

	private static ResourceBundle loadBundle(Locale locale) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME, locale, NO_DEFAULT_LOCALE_FALLBACK);
		} catch (MissingResourceException e) {
			logger.debug("No score marking bundle for " + locale, e);
			return null;
		}
	}

}
