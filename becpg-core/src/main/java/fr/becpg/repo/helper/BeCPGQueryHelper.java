package fr.becpg.repo.helper;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>BeCPGQueryHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGQueryHelper {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(BeCPGQueryHelper.class);

	/** Constant <code>SUFFIX_ALL="*"</code> */
	public static final String SUFFIX_ALL = "*";

	/** Constant <code>SUFFIX_SPACE=" "</code> */
	private static final String SUFFIX_SPACE = " ";
	/** Constant <code>SUFFIX_DOUBLE_QUOTE="\&quot;"</code> */
	private static final String SUFFIX_DOUBLE_QUOTE = "\"";
	/** Constant <code>SUFFIX_SIMPLE_QUOTE="'"</code> */
	private static final String SUFFIX_SIMPLE_QUOTE = "'";

	/** Anything but a letter or a digit separates two words of a search. */
	private static final Pattern NON_WORD_CHARACTERS = Pattern.compile("[^\\p{L}\\p{N}]+");

	/** Combining marks left by the Unicode decomposition of an accented letter. */
	private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

	/** A Han ideograph is a word on its own, Chinese and Japanese being written without spaces. */
	private static final Pattern HAN_CHARACTER = Pattern.compile("\\p{IsHan}");

	/** Spaces between the words of a separated text. */
	private static final Pattern SPACES = Pattern.compile(" +");

	/** French ligatures, which Unicode decomposition does not split. */
	private static final Map<String, String> LIGATURES = Map.of("\u0153", "oe", "\u0152", "OE", "\u00e6", "ae", "\u00c6", "AE");
	
	/**
	 * <p>Constructor for BeCPGQueryHelper.</p>
	 */
	private BeCPGQueryHelper() {
		
	}

	/**
	 * Tells whether a name matches a free-text query.
	 *
	 * Every word of the query must start a distinct word of the name, whatever the order, the case, the accents or
	 * the ligatures. Punctuation separates words, and no word is an operator: "AND", "OR" or "NOT" are searched as
	 * words. A query made of punctuation only is searched as a plain substring.
	 *
	 * @param query the text typed by the user; blank or "*" matches everything
	 * @param entityName the name to test
	 * @return true when the name matches the query
	 */
	public static boolean isQueryMatch(String query, String entityName) {
		if ((query == null) || (entityName == null)) {
			return false;
		}

		String trimmedQuery = query.trim();
		if (trimmedQuery.isEmpty() || SUFFIX_ALL.equals(trimmedQuery)) {
			return true;
		}

		List<SearchWord> queryWords = extractSearchWords(trimmedQuery);
		if (queryWords.isEmpty()) {
			return foldText(entityName).contains(foldText(trimmedQuery));
		}
		return startsDistinctWords(queryWords, extractSearchWords(entityName));
	}

	/**
	 * Splits a text into the words the search compares: lower-cased, without accents nor ligatures, and cut on any
	 * character that is neither a letter nor a digit. No word is dropped, so that every typed word counts.
	 *
	 * @param text the text to split
	 * @return the words, possibly empty
	 */
	private static List<SearchWord> extractSearchWords(String text) {
		String separated = HAN_CHARACTER.matcher(foldText(text)).replaceAll(" $0 ");
		separated = NON_WORD_CHARACTERS.matcher(separated).replaceAll(SUFFIX_SPACE).trim();

		List<SearchWord> words = new ArrayList<>();
		if (!separated.isEmpty()) {
			for (String word : SPACES.split(separated)) {
				words.add(SearchWord.of(word));
			}
		}
		return words;
	}

	/**
	 * Checks that each query word starts its own name word, so that a word typed twice must be found twice. Longer
	 * query words pick their name word first, to keep a short prefix from taking the only word a longer one fits.
	 *
	 * @param queryWords the analysed query words
	 * @param nameWords the analysed name words
	 * @return true when every query word found a name word
	 */
	private static boolean startsDistinctWords(List<SearchWord> queryWords, List<SearchWord> nameWords) {
		List<SearchWord> sortedQueryWords = new ArrayList<>(queryWords);
		sortedQueryWords.sort(Comparator.comparingInt((SearchWord word) -> word.raw().length()).reversed());

		List<SearchWord> availableNameWords = new ArrayList<>(nameWords);
		for (SearchWord queryWord : sortedQueryWords) {
			if (!removeWordStartingWith(availableNameWords, queryWord)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Removes the first word started by the given typed word.
	 *
	 * @param words the words to search, modified in place
	 * @param prefix the typed word
	 * @return true when a word was found and removed
	 */
	private static boolean removeWordStartingWith(List<SearchWord> words, SearchWord prefix) {
		Iterator<SearchWord> iterator = words.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().isStartedBy(prefix)) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Lower-cases a text and strips its accents and ligatures, for a plain substring comparison.
	 *
	 * @param text the text to fold
	 * @return the folded text
	 */
	private static String foldText(String text) {
		String decomposed = Normalizer.normalize(unfoldLigatures(text), Normalizer.Form.NFD);
		return COMBINING_MARKS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
	}

	/**
	 * Writes the French ligatures as two letters, which Unicode decomposition leaves untouched.
	 *
	 * @param text the text to unfold
	 * @return the text with œ, Œ, æ and Æ spelled out
	 */
	private static String unfoldLigatures(String text) {
		String unfolded = text;
		for (Map.Entry<String, String> ligature : LIGATURES.entrySet()) {
			unfolded = unfolded.replace(ligature.getKey(), ligature.getValue());
		}
		return unfolded;
	}

	/**
	 * <p>prepareQueryForSorting.</p>
	 *
	 * @param query a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String prepareQueryForSorting(String query) {

		logger.debug("Query before prepare:" + query);
		if ((query != null) && !(query.endsWith(SUFFIX_ALL) || query.endsWith(SUFFIX_SPACE) || query.endsWith(SUFFIX_DOUBLE_QUOTE)
				|| query.endsWith(SUFFIX_SIMPLE_QUOTE))) {
			
			List<SearchWord> queryWords = extractSearchWords(query.trim());
			
			StringBuilder buff = new StringBuilder();

			for (SearchWord queryWord : queryWords) {
				if (buff.length() > 0) {
					buff.append(' ');
				}
				buff.append(queryWord.stem());
			}
			
			buff.append(SUFFIX_ALL);
			query = buff.toString();
		}

		logger.debug("Query after prepare:" + query);

		return query;
	}

	/**
	 * <p>prepareQuery.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String prepareQuery(String query) {

		if ((query != null) && !(query.endsWith(SUFFIX_ALL) || query.endsWith(SUFFIX_SPACE) || query.endsWith(SUFFIX_DOUBLE_QUOTE) || query.endsWith(SUFFIX_SIMPLE_QUOTE))) {
			query += SUFFIX_ALL;
		}

		return query;
	}

	/**
	 * <p>isAllQuery.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isAllQuery(String query) {
		return (query != null) && query.trim().equals(BeCPGQueryHelper.SUFFIX_ALL);
	}

}
