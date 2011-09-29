package fr.becpg.repo.search.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * http://snowball.tartarus.org/algorithms/french/stemmer.html
 */
public class FrenchSnowballAnalyserThatRemovesAccents extends Analyzer
{
    Analyzer analyzer = new SnowballAnalyzer("French");

    
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        TokenStream result = analyzer.tokenStream(fieldName, reader);
        result = new ISOLatin1AccentFilter(result);
        return result;
    }
    
    
    @Override
    //Memory issue FIX
    public void close() {
    	super.close();
    	analyzer.close();
    }

}
