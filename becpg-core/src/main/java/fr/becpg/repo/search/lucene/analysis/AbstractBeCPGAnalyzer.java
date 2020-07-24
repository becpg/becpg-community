package fr.becpg.repo.search.lucene.analysis;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * <p>Abstract AbstractBeCPGAnalyzer class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractBeCPGAnalyzer extends Analyzer{
	
	@SuppressWarnings("rawtypes")
	protected Set stopSet;


	/**
	 * Builds an analyzer with the given stop words.
	 *
	 * @param stopWords an array of {@link java.lang.String} objects.
	 */
	public AbstractBeCPGAnalyzer(String[] stopWords) {
		stopSet = StopFilter.makeStopSet(stopWords);
	}
	
	/**
	 * <p>tokenStream.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param reader a {@link java.io.Reader} object.
	 * @param disableStopWords a boolean.
	 * @return a {@link org.apache.lucene.analysis.TokenStream} object.
	 */
	public abstract TokenStream tokenStream(String fieldName, Reader reader, boolean disableStopWords);
	
	  
    /** {@inheritDoc} */
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
       return tokenStream(fieldName, reader,false);
    }
    
	
}
