package fr.becpg.repo.helper;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A word of a free-text search, kept both as typed and reduced to a common form, so that a plural, a feminine or an
 * irregular form finds its base word while a word being typed still finds the words it starts.
 *
 * The word is expected lower-cased and without accents.
 *
 * @param raw the word as typed
 * @param stem the word without its plural or feminine ending
 */
record SearchWord(String raw, String stem) {

	/**
	 * Endings rewritten, in this order, to reach the common form: English "-ies" plural, French "-aux" plural of an
	 * "-al" word (but not "-eaux", the plural of "-eau"), "s" or "x" plural, then the "-ee" of a feminine past
	 * participle. A short word is left untouched, to keep "les", "taux" or "pies" readable.
	 */
	private static final List<Map.Entry<Pattern, String>> ENDING_RULES = List.of(
			Map.entry(Pattern.compile("(?<=\\p{L}{2})ies$"), "y"),
			Map.entry(Pattern.compile("(?<=\\p{L}{2})(?<!e)aux$"), "al"),
			Map.entry(Pattern.compile("(?<=\\p{L}{3})[sx]$"), ""),
			Map.entry(Pattern.compile("(?<=\\p{L}{2})ee$"), "e"));

	/**
	 * Builds the search word of a lower-cased, accent-free word.
	 *
	 * @param word the word
	 * @return the word with its common form
	 */
	static SearchWord of(String word) {
		String stem = word;
		for (Map.Entry<Pattern, String> rule : ENDING_RULES) {
			stem = rule.getKey().matcher(stem).replaceFirst(rule.getValue());
		}
		return new SearchWord(word, stem);
	}

	/**
	 * Tells whether a typed word starts this word, as typed or in its common form.
	 *
	 * @param prefix the typed word
	 * @return true when this word starts with it
	 */
	boolean isStartedBy(SearchWord prefix) {
		return raw.startsWith(prefix.raw) || stem.startsWith(prefix.stem);
	}
}
