/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Turns the JSON stored on a change unit by {@link ChangeUnitScoreBuilder} into plain text, for the
 * places that cannot render the notifications widget, such as the Excel export of the change order.
 *
 * <p>The text follows the same priorities as the widget: what the change order introduces and resolves,
 * then the completion of the simulated product.</p>
 */
public final class ChangeUnitScoreFormatter {

	static final String MESSAGE_COMPLETION = "message.ecm.change-unit.completion";
	static final String MESSAGE_NEW = "message.ecm.change-unit.new";
	static final String MESSAGE_RESOLVED = "message.ecm.change-unit.resolved";
	static final String MESSAGE_LESS_SEVERE = "message.ecm.change-unit.less-severe";
	static final String MESSAGE_NO_CHANGE = "message.ecm.change-unit.no-change";
	static final String MESSAGE_NEW_LIST = "message.ecm.change-unit.new-list";
	static final String MESSAGE_RESOLVED_LIST = "message.ecm.change-unit.resolved-list";

	private static final String PROP_GLOBAL = "global";
	private static final String SUMMARY_SEPARATOR = ", ";
	private static final String LIST_SEPARATOR = " ; ";
	private static final String LINE_SEPARATOR = "\n";

	private ChangeUnitScoreFormatter() {
	}

	/**
	 * Formats the scores of a change unit as text.
	 *
	 * @param score the JSON stored in {@code ecm:culEntityScore}
	 * @param locale the locale of the messages of the requirements to keep
	 * @param messages resolves a message key and its arguments in the user locale
	 * @return a summary line, followed by the new and the resolved requirements when there are some
	 */
	public static String format(JSONObject score, Locale locale, BiFunction<String, Object[], String> messages) {
		List<String> lines = new ArrayList<>();
		lines.add(formatSummary(score, messages));

		List<String> newMessages = extractMessages(score.optJSONArray(ChangeUnitScoreBuilder.PROP_REQUIREMENTS), locale, true);
		if (!newMessages.isEmpty()) {
			lines.add(messages.apply(MESSAGE_NEW_LIST, new Object[] { String.join(LIST_SEPARATOR, newMessages) }));
		}
		List<String> resolvedMessages = extractMessages(score.optJSONArray(ChangeUnitScoreBuilder.PROP_RESOLVED), locale, false);
		if (!resolvedMessages.isEmpty()) {
			lines.add(messages.apply(MESSAGE_RESOLVED_LIST, new Object[] { String.join(LIST_SEPARATOR, resolvedMessages) }));
		}
		return String.join(LINE_SEPARATOR, lines);
	}

	private static String formatSummary(JSONObject score, BiFunction<String, Object[], String> messages) {
		List<String> parts = new ArrayList<>();
		int newCount = score.optInt(ChangeUnitScoreBuilder.PROP_NEW_COUNT);
		int resolvedCount = score.optInt(ChangeUnitScoreBuilder.PROP_RESOLVED_COUNT);
		int lessSevereCount = score.optInt(ChangeUnitScoreBuilder.PROP_LESS_SEVERE_COUNT);

		if (newCount > 0) {
			parts.add(messages.apply(MESSAGE_NEW, new Object[] { newCount, score.optInt(ChangeUnitScoreBuilder.PROP_NEW_FORBIDDEN_COUNT) }));
		}
		if (resolvedCount > 0) {
			parts.add(messages.apply(MESSAGE_RESOLVED, new Object[] { resolvedCount }));
		}
		if (lessSevereCount > 0) {
			parts.add(messages.apply(MESSAGE_LESS_SEVERE, new Object[] { lessSevereCount }));
		}
		if (parts.isEmpty()) {
			parts.add(messages.apply(MESSAGE_NO_CHANGE, new Object[0]));
		}
		parts.add(formatCompletion(score, messages));
		return String.join(SUMMARY_SEPARATOR, parts);
	}

	private static String formatCompletion(JSONObject score, BiFunction<String, Object[], String> messages) {
		JSONObject previous = score.optJSONObject(ChangeUnitScoreBuilder.PROP_PREVIOUS);
		long completion = (long) Math.floor(score.optDouble(PROP_GLOBAL, 0d));
		long previousCompletion = previous != null ? (long) Math.floor(previous.optDouble(PROP_GLOBAL, 0d)) : completion;
		return messages.apply(MESSAGE_COMPLETION, new Object[] { completion, previousCompletion });
	}

	private static List<String> extractMessages(JSONArray requirements, Locale locale, boolean onlyNew) {
		List<String> messages = new ArrayList<>();
		if (requirements != null) {
			for (int i = 0; i < requirements.length(); i++) {
				JSONObject requirement = requirements.getJSONObject(i);
				if (!onlyNew || requirement.optBoolean(ChangeUnitScoreBuilder.PROP_IS_NEW)) {
					String message = localizedMessage(requirement.optJSONObject(ChangeUnitScoreBuilder.PROP_MESSAGE), locale);
					if (!message.isEmpty()) {
						messages.add(message);
					}
				}
			}
		}
		return messages;
	}

	/**
	 * Same fallback as the notifications widget: exact locale, then language, then the default message.
	 */
	static String localizedMessage(JSONObject message, Locale locale) {
		if (message == null) {
			return "";
		}
		for (String key : new String[] { locale.toString(), locale.getLanguage(), ChangeUnitScoreBuilder.DEFAULT_LOCALE_KEY }) {
			if (message.has(key)) {
				return message.getString(key);
			}
		}
		return message.keySet().isEmpty() ? "" : message.getString(message.keys().next());
	}
}
