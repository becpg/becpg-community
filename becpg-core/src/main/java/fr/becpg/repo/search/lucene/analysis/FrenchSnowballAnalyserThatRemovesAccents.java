package fr.becpg.repo.search.lucene.analysis;

import java.io.Reader;
import java.util.Set;

import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * http://snowball.tartarus.org/algorithms/french/stemmer.html
 */
public class FrenchSnowballAnalyserThatRemovesAccents extends Analyzer
{
	 private Set<?> stopSet;
	
    /**
     * An array containing some common English words that are usually not useful for searching.
     */
    public static final String[] STOP_WORDS = FrenchStopWords.STOP_WORDS;
    
    /** Builds an analyzer. */
    public FrenchSnowballAnalyserThatRemovesAccents()
    {
        this(STOP_WORDS);
    }

    /** Builds an analyzer with the given stop words. */
    public FrenchSnowballAnalyserThatRemovesAccents(String[] stopWords)
    {
        stopSet = StopFilter.makeStopSet(stopWords);
    }
    
    
    
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        TokenStream result = new StandardTokenizer(reader);
        result = new AlfrescoStandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopSet);
        result = new AccentFilter(result);
        result = new SnowballFilter(result, "French");
        return result;
    }

}
