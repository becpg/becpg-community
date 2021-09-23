package fr.becpg.repo.helper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.search.lucene.analysis.AbstractBeCPGAnalyzer;
import fr.becpg.repo.search.lucene.analysis.EnglishBeCPGAnalyser;
import fr.becpg.repo.search.lucene.analysis.FrenchBeCPGAnalyser;

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

	private static Analyzer luceneAnaLyzer = null;

	/**
	 * <p>isQueryMatch.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @param entityName a {@link java.lang.String} object.
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 * @return a boolean.
	 */
	public static boolean isQueryMatch(String query, String entityName) {
		if (query != null) {

			if (SUFFIX_ALL.equals(query)) {
				return true;
			}

			Analyzer analyzer = getTextAnalyzer();

			if (logger.isDebugEnabled()) {
				logger.debug("Analyzing " + entityName + " with query " + query + " using analyzer : " + analyzer.getClass().getName());
			}

			TokenStream querySource = null;
			Reader queryReader;
			TokenStream productNameSource = null;
			Reader productNameReader;
			try {

				queryReader = new StringReader(query);
				productNameReader = new StringReader(entityName);
				querySource = analyzer.tokenStream("isQueryMatch", queryReader);
				productNameSource = analyzer.tokenStream("isQueryMatch", productNameReader);

				Token reusableToken = new Token();
				boolean match = true;
				while ((reusableToken = querySource.next(reusableToken)) != null) {
					Token tmpToken = new Token();
					while ((tmpToken = productNameSource.next(tmpToken)) != null) {
						match = false;
						if (logger.isDebugEnabled()) {
							logger.debug("Test StartWith : " + reusableToken.term() + " with " + tmpToken.term());
						}

						if (tmpToken.term().startsWith(reusableToken.term())) {
							match = true;
							break;
						}
					}
					if (!match) {
						break;
					}
				}
				querySource.reset();
				productNameSource.reset();
				return match;
			} catch (Exception e) {
				logger.error(e, e);
			} finally {

				try {
					if (querySource != null) {
						querySource.close();
					}
					if (productNameSource != null) {
						productNameSource.close();
					}

				} catch (IOException e) {
					logger.error(e, e);
				}

			}

		}
		return false;
	}

	// TODO escape + - && || ! ( ) { } [ ] ^ " ~ * ? : \
	/**
	 * <p>prepareQuery.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 * @param query a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String prepareQuery(String query) {

		logger.debug("Query before prepare:" + query);
		if ((query != null) && !(query.endsWith(SUFFIX_ALL) || query.endsWith(SUFFIX_SPACE) || query.endsWith(SUFFIX_DOUBLE_QUOTE)
				|| query.endsWith(SUFFIX_SIMPLE_QUOTE))) {
			// Query with wildcard are not getting analyzed by stemmers
			// so do it manually
			Analyzer analyzer = getTextAnalyzer();

			if (logger.isDebugEnabled()) {
				logger.debug("Using analyzer : " + analyzer.getClass().getName());
			}
			TokenStream source = null;
			Reader reader;
			try {

				reader = new StringReader(query.trim());

				if (analyzer instanceof AbstractBeCPGAnalyzer) {
					source = ((AbstractBeCPGAnalyzer) analyzer).tokenStream("prepareQuery", reader, true);
				} else {
					source = analyzer.tokenStream("prepareQuery", reader);
				}

				StringBuilder buff = new StringBuilder();
				Token reusableToken = new Token();
				while ((reusableToken = source.next(reusableToken)) != null) {
					if (buff.length() > 0) {
						buff.append(' ');
					}
					buff.append(reusableToken.term());
				}
				source.reset();
				buff.append(SUFFIX_ALL);
				query = buff.toString();
			} catch (Exception e) {
				logger.error(e, e);
			} finally {

				try {
					if (source != null) {
						source.close();
					}

				} catch (IOException e) {
					logger.error(e, e);
				}

			}

		}

		logger.debug("Query after prepare:" + query);

		return query;
	}

	private static Analyzer getTextAnalyzer() {
		if (luceneAnaLyzer == null) {
			if (Locale.FRENCH.equals(I18NUtil.getLocale())) {
				luceneAnaLyzer = new FrenchBeCPGAnalyser();
			} else {
				luceneAnaLyzer = new EnglishBeCPGAnalyser();
			}
		}
		return luceneAnaLyzer;

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
