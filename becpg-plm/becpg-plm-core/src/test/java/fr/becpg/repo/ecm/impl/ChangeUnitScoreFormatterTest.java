package fr.becpg.repo.ecm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiFunction;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Unit tests of the text rendering of the scores of a change unit.
 */
public class ChangeUnitScoreFormatterTest {

	private static final BiFunction<String, Object[], String> MESSAGES = (key, args) -> key + Arrays.toString(args);

	private static final String CADMIUM = "Cadmium above the limit";

	private static final String MISSING_FIELD = "Mandatory field missing";

	private static final String SOY = "Soy not declared";

	@Test
	public void summaryStartsWithWhatTheChangeIntroduces() {
		String text = ChangeUnitScoreFormatter.format(score(), Locale.FRENCH, MESSAGES);

		assertTrue(text.startsWith(ChangeUnitScoreFormatter.MESSAGE_NEW + "[1, 1]"));
	}

	@Test
	public void summaryGivesTheCompletionBeforeAndAfter() {
		String text = ChangeUnitScoreFormatter.format(score(), Locale.FRENCH, MESSAGES);

		assertTrue(text.contains(ChangeUnitScoreFormatter.MESSAGE_COMPLETION + "[48, 46]"));
	}

	@Test
	public void onlyNewRequirementsAreListedAsNew() {
		String text = ChangeUnitScoreFormatter.format(score(), Locale.FRENCH, MESSAGES);

		assertTrue(text.contains(ChangeUnitScoreFormatter.MESSAGE_NEW_LIST + "[" + CADMIUM + "]"));
		assertFalse(text.contains(MISSING_FIELD));
	}

	@Test
	public void resolvedRequirementsAreListed() {
		String text = ChangeUnitScoreFormatter.format(score(), Locale.FRENCH, MESSAGES);

		assertTrue(text.contains(ChangeUnitScoreFormatter.MESSAGE_RESOLVED_LIST + "[" + SOY + "]"));
	}

	@Test
	public void unchangedProductSaysSo() {
		JSONObject score = new JSONObject().put("global", 55).put(ChangeUnitScoreBuilder.PROP_PREVIOUS, new JSONObject().put("global", 55));

		assertEquals(ChangeUnitScoreFormatter.MESSAGE_NO_CHANGE + "[], " + ChangeUnitScoreFormatter.MESSAGE_COMPLETION + "[55, 55]",
				ChangeUnitScoreFormatter.format(score, Locale.FRENCH, MESSAGES));
	}

	@Test
	public void messageFallsBackOnTheDefaultTranslation() {
		JSONObject message = new JSONObject().put(ChangeUnitScoreBuilder.DEFAULT_LOCALE_KEY, CADMIUM).put("de", "Cadmium zu hoch");

		assertEquals(CADMIUM, ChangeUnitScoreFormatter.localizedMessage(message, Locale.FRENCH));
		assertEquals("Cadmium zu hoch", ChangeUnitScoreFormatter.localizedMessage(message, Locale.GERMANY));
	}

	private static JSONObject score() {
		JSONArray requirements = new JSONArray().put(requirement(CADMIUM).put(ChangeUnitScoreBuilder.PROP_IS_NEW, true))
				.put(requirement(MISSING_FIELD).put(ChangeUnitScoreBuilder.PROP_IS_NEW, false));
		return new JSONObject().put("global", 48.6).put(ChangeUnitScoreBuilder.PROP_PREVIOUS, new JSONObject().put("global", 46.2))
				.put(ChangeUnitScoreBuilder.PROP_NEW_COUNT, 1).put(ChangeUnitScoreBuilder.PROP_NEW_FORBIDDEN_COUNT, 1)
				.put(ChangeUnitScoreBuilder.PROP_RESOLVED_COUNT, 1).put(ChangeUnitScoreBuilder.PROP_REQUIREMENTS, requirements)
				.put(ChangeUnitScoreBuilder.PROP_RESOLVED, new JSONArray().put(requirement(SOY)));
	}

	private static JSONObject requirement(String message) {
		return new JSONObject().put(ChangeUnitScoreBuilder.PROP_MESSAGE, new JSONObject().put(ChangeUnitScoreBuilder.DEFAULT_LOCALE_KEY, message));
	}
}
