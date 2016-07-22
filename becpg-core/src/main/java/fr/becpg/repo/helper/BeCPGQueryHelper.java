package fr.becpg.repo.helper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import fr.becpg.repo.search.lucene.analysis.EnglishBeCPGAnalyser;
import fr.becpg.repo.search.lucene.analysis.FrenchBeCPGAnalyser;

public class BeCPGQueryHelper {

	private static final Log logger = LogFactory.getLog(BeCPGQueryHelper.class);
	protected static final String SUFFIX_ALL = "*";

	public static boolean isQueryMatch(String query, String entityName, DictionaryService dictionaryService) {
		if (query != null) {

			if (SUFFIX_ALL.equals(query)) {
				return true;
			}

			Analyzer analyzer = getTextAnalyzer(dictionaryService);

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
				querySource = analyzer.tokenStream(null, queryReader);
				productNameSource = analyzer.tokenStream(null, productNameReader);

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
					// Nothing todo here
					logger.error(e, e);
				}

			}

		}
		return false;
	}

	private static Analyzer getTextAnalyzer(DictionaryService dictionaryService) {
		DataTypeDefinition def = dictionaryService.getDataType(DataTypeDefinition.TEXT);
		try {
			return (Analyzer) Class.forName(def.resolveAnalyserClassName(Locale.getDefault())).newInstance();
		} catch (Exception e) {
			logger.error(e, e);
			if (Locale.FRENCH.equals(Locale.getDefault())) {
				return new FrenchBeCPGAnalyser();
			} else {
				return new EnglishBeCPGAnalyser();
			}
		}
	}

}
