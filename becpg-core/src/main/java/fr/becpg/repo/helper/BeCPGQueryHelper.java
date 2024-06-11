package fr.becpg.repo.helper;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.search.lucene.analysis.AccentFilter;

/**
 * <p>BeCPGQueryHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGQueryHelper {

	private static final Log logger = LogFactory.getLog(BeCPGQueryHelper.class);

	/** Constant <code>SUFFIX_ALL="*"</code> */
	public static final String SUFFIX_ALL = "*";

	private static final String SUFFIX_SPACE = " ";
	private static final String SUFFIX_DOUBLE_QUOTE = "\"";
	private static final String SUFFIX_SIMPLE_QUOTE = "'";
	
	private BeCPGQueryHelper() {
		
	}

	/**
	 * <p>isQueryMatch.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @param entityName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isQueryMatch(String query, String entityName) {
		if (query != null) {

			if (SUFFIX_ALL.equals(query)) {
				return true;
			}

			List<String> queryTokens = extractTokens(query.trim(), true);
			
			List<String> entityNameTokens = extractTokens(entityName, true);
			
			boolean match = true;
			
			for (String queryToken : queryTokens) {
				for (String productNameToken : entityNameTokens) {
					match = false;
					if (productNameToken.startsWith(queryToken)) {
						match = true;
						break;
					}
				}
				if (!match) {
					break;
				}
			}
			
			return match;
			
		}
		return false;
	}

	private static List<String> extractTokens(String input, boolean enableStopWords) {
		
		InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(input.getBytes()), StandardCharsets.UTF_8);

		List<String> tokens = new ArrayList<>();
		
		try (TokenStream tokenFilter = new AccentFilter(getTextAnalyzer(enableStopWords).tokenStream("isQueryMatch", reader))) {
			tokenFilter.reset();
			while (tokenFilter.incrementToken()) {
				tokens.add(tokenFilter.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
		
		return tokens;
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
			
			List<String> queryTokens = extractTokens(query.trim(), false);
			
			StringBuilder buff = new StringBuilder();

			for (String queryToken : queryTokens) {
				if (buff.length() > 0) {
					buff.append(' ');
				}
				buff.append(queryToken);
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

	private static Analyzer getTextAnalyzer(boolean enableStopWords) {
		if (Locale.FRENCH.equals(I18NUtil.getLocale())) {
			if (enableStopWords) {
				return new FrenchAnalyzer();
			}
			return new FrenchAnalyzer(CharArraySet.EMPTY_SET);
		}
		if (enableStopWords) {
			return new EnglishAnalyzer();
		}
		return new EnglishAnalyzer(CharArraySet.EMPTY_SET);
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
