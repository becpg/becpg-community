package fr.becpg.repo.helper;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
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

	/** Blanks typed between two expressions of a query, the no-break space included. */
	private static final Pattern BLANKS = Pattern.compile("[\\s\\p{Z}]+");

	/** French ligatures, which Unicode decomposition does not split. */
	private static final Map<String, String> LIGATURES = Map.of("\u0153", "oe", "\u0152", "OE", "\u00e6", "ae", "\u00c6", "AE");

	/** Quotes around a typed query and its closing wildcard, which are search syntax, not part of the searched label. */
	private static final Pattern SURROUNDING_QUERY_SYNTAX = Pattern.compile("^[\"' ]+|[*\"' ]+$");

	/** Relevance ranks of a suggestion label, best first. */
	private static final int RANK_EXACT = 0;
	private static final int RANK_PREFIX = 1;
	private static final int RANK_WORD_PREFIX = 2;
	private static final int RANK_OTHER = 3;

	/**
	 * A suggestion with its relevance rank, computed once before sorting.
	 *
	 * @param item the suggestion
	 * @param rank its relevance rank
	 */
	private record RankedItem<T>(T item, int rank) {
	}
	
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
	 * words. Words joined by punctuation without a blank, such as "R&D", "1=1" or "brief:marketing", form an
	 * expression: they must follow each other in the name, in the same order, the last one being possibly incomplete.
	 * A query made of punctuation only is searched as a plain substring.
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

		List<List<SearchWord>> queryExpressions = extractSearchExpressions(trimmedQuery);
		if (queryExpressions.isEmpty()) {
			return foldText(entityName).contains(foldText(trimmedQuery));
		}
		return startsDistinctExpressions(queryExpressions, extractSearchWords(entityName));
	}

	/**
	 * Splits a query on its blanks into expressions, each expression holding the words its punctuation separates. A
	 * Han ideograph stays an expression of its own, and an expression made of punctuation only is dropped.
	 *
	 * @param query the typed query
	 * @return the expressions, possibly empty
	 */
	private static List<List<SearchWord>> extractSearchExpressions(String query) {
		List<List<SearchWord>> expressions = new ArrayList<>();
		for (String blankSeparated : BLANKS.split(separateHanCharacters(foldText(query)).trim())) {
			List<SearchWord> words = splitFoldedWords(blankSeparated);
			if (!words.isEmpty()) {
				expressions.add(words);
			}
		}
		return expressions;
	}

	/**
	 * Splits a text into the words the search compares: lower-cased, without accents nor ligatures, and cut on any
	 * character that is neither a letter nor a digit. No word is dropped, so that every typed word counts.
	 *
	 * @param text the text to split
	 * @return the words, possibly empty
	 */
	private static List<SearchWord> extractSearchWords(String text) {
		return splitFoldedWords(separateHanCharacters(foldText(text)));
	}

	/**
	 * Surrounds every Han ideograph with spaces, so that it becomes a word of its own.
	 *
	 * @param foldedText the folded text
	 * @return the text with its ideographs set apart
	 */
	private static String separateHanCharacters(String foldedText) {
		return HAN_CHARACTER.matcher(foldedText).replaceAll(" $0 ");
	}

	/**
	 * Cuts a folded text on any character that is neither a letter nor a digit.
	 *
	 * @param foldedText the folded text
	 * @return the words, possibly empty
	 */
	private static List<SearchWord> splitFoldedWords(String foldedText) {
		String separated = NON_WORD_CHARACTERS.matcher(foldedText).replaceAll(SUFFIX_SPACE).trim();

		List<SearchWord> words = new ArrayList<>();
		if (!separated.isEmpty()) {
			for (String word : SPACES.split(separated)) {
				words.add(SearchWord.of(word));
			}
		}
		return words;
	}

	/**
	 * Checks that each query expression is found on its own name words, so that a word typed twice must be found
	 * twice. Longer expressions pick their name words first, to keep a short prefix from taking the only word a longer
	 * one fits.
	 *
	 * @param queryExpressions the analysed query expressions
	 * @param nameWords the analysed name words
	 * @return true when every query expression found its name words
	 */
	private static boolean startsDistinctExpressions(List<List<SearchWord>> queryExpressions, List<SearchWord> nameWords) {
		List<List<SearchWord>> sortedExpressions = new ArrayList<>(queryExpressions);
		sortedExpressions.sort(Comparator.comparingInt((List<SearchWord> expression) -> expression.size())
				.thenComparingInt(BeCPGQueryHelper::countTypedCharacters).reversed());

		boolean[] takenNameWords = new boolean[nameWords.size()];
		for (List<SearchWord> expression : sortedExpressions) {
			int start = findExpressionStart(expression, nameWords, takenNameWords);
			if (start < 0) {
				return false;
			}
			Arrays.fill(takenNameWords, start, start + expression.size(), true);
		}
		return true;
	}

	/**
	 * Counts the characters of the words of an expression, as typed.
	 *
	 * @param expression the expression
	 * @return its number of characters, punctuation excluded
	 */
	private static int countTypedCharacters(List<SearchWord> expression) {
		int characters = 0;
		for (SearchWord word : expression) {
			characters += word.raw().length();
		}
		return characters;
	}

	/**
	 * Finds the first run of free name words matching an expression: every word but the last is the same word, the
	 * last one is started by the typed word.
	 *
	 * @param expression the typed expression
	 * @param nameWords the name words
	 * @param takenNameWords the name words already matched by another expression
	 * @return the index of the first name word of the run, or -1 when there is none
	 */
	private static int findExpressionStart(List<SearchWord> expression, List<SearchWord> nameWords, boolean[] takenNameWords) {
		for (int start = 0; start <= nameWords.size() - expression.size(); start++) {
			if (isExpressionAt(expression, nameWords, takenNameWords, start)) {
				return start;
			}
		}
		return -1;
	}

	/**
	 * Tells whether an expression matches the free name words starting at the given index.
	 *
	 * @param expression the typed expression
	 * @param nameWords the name words
	 * @param takenNameWords the name words already matched by another expression
	 * @param start the index of the first name word
	 * @return true when every word of the expression matches its name word
	 */
	private static boolean isExpressionAt(List<SearchWord> expression, List<SearchWord> nameWords, boolean[] takenNameWords, int start) {
		int lastIndex = expression.size() - 1;
		for (int index = 0; index <= lastIndex; index++) {
			SearchWord nameWord = nameWords.get(start + index);
			SearchWord typedWord = expression.get(index);
			boolean matches = index == lastIndex ? nameWord.isStartedBy(typedWord) : nameWord.isSameWordAs(typedWord);
			if (takenNameWords[start + index] || !matches) {
				return false;
			}
		}
		return true;
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

	/**
	 * Sorts suggestions by relevance to the typed query: the label equal to the query first, then the labels starting
	 * with it, then the labels having a word starting with it, then the others. Case, accents and ligatures are
	 * ignored. The sort is stable, so suggestions of the same rank keep the order of the search engine.
	 *
	 * @param <T> the suggestion type
	 * @param query the text typed by the user
	 * @param items the suggestions, in the order of the search engine; left untouched
	 * @param labelOf reads the label of a suggestion, may return null
	 * @return the sorted suggestions, or <code>items</code> when there is nothing to sort
	 */
	public static <T> List<T> sortByRelevance(String query, List<T> items, Function<T, String> labelOf) {
		String foldedQuery = foldQuery(query);
		if (foldedQuery.isEmpty() || (items.size() < 2)) {
			return items;
		}

		List<RankedItem<T>> rankedItems = new ArrayList<>(items.size());
		for (T item : items) {
			rankedItems.add(new RankedItem<>(item, rankLabel(foldedQuery, labelOf.apply(item))));
		}
		rankedItems.sort(Comparator.comparingInt(RankedItem::rank));

		List<T> sortedItems = new ArrayList<>(items.size());
		for (RankedItem<T> rankedItem : rankedItems) {
			sortedItems.add(rankedItem.item());
		}
		return sortedItems;
	}

	/**
	 * Folds a typed query for a label comparison, without its surrounding quotes nor its closing wildcard.
	 *
	 * @param query the typed query, may be null
	 * @return the folded query, empty when nothing is left to compare
	 */
	private static String foldQuery(String query) {
		if ((query == null) || isAllQuery(query)) {
			return "";
		}
		return foldText(SURROUNDING_QUERY_SYNTAX.matcher(query.trim()).replaceAll(""));
	}

	/**
	 * Ranks a label against a folded query.
	 *
	 * @param foldedQuery the folded query, not empty
	 * @param label the label, may be null
	 * @return the relevance rank, lower is better
	 */
	private static int rankLabel(String foldedQuery, String label) {
		if (label == null) {
			return RANK_OTHER;
		}
		String foldedLabel = foldText(label.trim());
		if (foldedLabel.equals(foldedQuery)) {
			return RANK_EXACT;
		}
		if (foldedLabel.startsWith(foldedQuery)) {
			return RANK_PREFIX;
		}
		return hasWordStartingWith(foldedLabel, foldedQuery) ? RANK_WORD_PREFIX : RANK_OTHER;
	}

	/**
	 * Tells whether the query starts a word of the label other than the first one, a word starting after any
	 * character that is neither a letter nor a digit.
	 *
	 * @param foldedLabel the folded label
	 * @param foldedQuery the folded query
	 * @return true when a word of the label starts with the query
	 */
	private static boolean hasWordStartingWith(String foldedLabel, String foldedQuery) {
		int index = foldedLabel.indexOf(foldedQuery, 1);
		while (index > 0) {
			if (!Character.isLetterOrDigit(foldedLabel.charAt(index - 1))) {
				return true;
			}
			index = foldedLabel.indexOf(foldedQuery, index + 1);
		}
		return false;
	}

}
