package fr.becpg.repo.search.lucene.analysis;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

public abstract class AbstractBeCPGAnalyzer extends Analyzer{
	
	@SuppressWarnings("rawtypes")
	protected Set stopSet;


	/** Builds an analyzer with the given stop words. */
	public AbstractBeCPGAnalyzer(String[] stopWords) {
		stopSet = StopFilter.makeStopSet(stopWords);
	}
	
	public abstract TokenStream tokenStream(String fieldName, Reader reader, boolean disableStopWords);
	
	  
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
       return tokenStream(fieldName, reader,false);
    }
    
	
}
