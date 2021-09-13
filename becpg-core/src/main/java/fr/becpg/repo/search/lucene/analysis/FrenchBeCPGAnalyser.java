/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.search.lucene.analysis;

import java.io.Reader;

import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.fr.ElisionFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * <p>FrenchBeCPGAnalyser class.</p>
 *
 * @author "Matthieu Laborie"
 *
 * http://snowball.tartarus.org/algorithms/french/stemmer.html
 * @version $Id: $Id
 */
public class FrenchBeCPGAnalyser extends AbstractBeCPGAnalyzer
{
	
    /**
     * An array containing some common English words that are usually not useful for searching.
     */
    protected static final String[] STOP_WORDS  =
        {
			"a",
            "au",
            "aux",
            "avec",
            "ce",
            "ces",
            "dans",
            "de",
            "des",
            "du",
            "elle",
            "en",
            "et",
            "eux",
            "il",
            "je",
            "la",
            "le",
            "leur",
            "lui",
            "ma",
            "mais",
            "me",
            "même",
            "mes",
            "moi",
            "mon",
            "ne",
            "nos",
            "notre",
            "nous",
            "on",
            "ou",
            "par",
            "pas",
            "pour",
            "qu",
            "que",
            "qui",
            "sa",
            "se",
            "ses",
            "son",
            "sur",
            "ta",
            "te",
            "tes",
            "toi",
            "ton",
            "tu",
            "un",
            "une",
            "vos",
            "votre",
            "vous",
            "c",
            "d",
            "j",
            "l",
            "à",
            "m",
            "n",
            "s",
            "t",
            "y",
            "été",
            "étés",
            "suis",
            "es",
            "est",
            "sommes",
            "êtes",
            "sont",
            "serai",
            "seras",
            "sera",
            "serons",
            "serez",
            "seront",
            "serais",
            "serait",
            "serions",
            "seriez",
            "seraient",
            "étais",
			"etre",
            "était",
            "étions",
            "étiez",
            "étaient",
            "fus",
            "fut",
            "fûmes",
            "fûtes",
            "furent",
            "sois",
            "soit",
            "soyons",
            "soyez",
            "soient",
            "fusse",
            "fusses",
            "fût",
            "fussions",
            "fussiez",
            "fussent",
            "ayant",
            "ayante",
            "ayantes",
            "ayants",
            "eu",
            "eue",
            "eues",
            "eus",
            "ai",
            "as",
            "avons",
            "avez",
            "ont",
            "aurai",
            "auras",
            "aura",
            "aurons",
            "aurez",
            "auront",
            "aurais",
            "aurait",
            "aurions",
            "auriez",
            "auraient",
            "avais",
            "avait",
            "avions",
            "aviez",
            "avaient",
            "eut",
            "eûmes",
            "eûtes",
            "eurent",
            "aie",
            "aies",
            "ait",
            "ayons",
            "ayez",
            "aient",
            "eusse",
            "eusses",
            "eut",
            "eussions",
            "eussiez",
            /** Constant <code>STOP_WORDS</code> */
            "eussent" };
    
    /**
     * Builds an analyzer.
     */
    public FrenchBeCPGAnalyser()
    {
        super(STOP_WORDS);
    }


  
    /** {@inheritDoc} */
    public TokenStream tokenStream(String fieldName, Reader reader, boolean disableStopWords)
    {
        TokenStream result = new StandardTokenizer(reader);
        result = new ElisionFilter(result, STOP_WORDS);
        result = new AlfrescoStandardFilter(result);
        result = new LowerCaseFilter(result);
        
        if(!disableStopWords){
        	result = new StopFilter(result, stopSet);
        }
        result = new AccentFilter(result);
        result = new SnowballFilter(result, "French");
        return result;
    }

}
