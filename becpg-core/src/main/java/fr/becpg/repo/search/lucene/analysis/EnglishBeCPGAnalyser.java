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
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * <p>EnglishBeCPGAnalyser class.</p>
 *
 * @author "Matthieu Laborie"
 *
 *  http://snowball.tartarus.org/algorithms/english/stemmer.html
 * @version $Id: $Id
 */
public class EnglishBeCPGAnalyser extends AbstractBeCPGAnalyzer {

	/**
	 * An array containing some common English words that are usually not useful
	 * for searching.
	 */
	protected static final String[] STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS;

	/**
	 * Builds an analyzer.
	 */
	public EnglishBeCPGAnalyser() {
		super(STOP_WORDS);
	}


	/** {@inheritDoc} */
	public TokenStream tokenStream(String fieldName, Reader reader, boolean disableStopWords) {
		TokenStream result = new StandardTokenizer(reader);
		result = new AlfrescoStandardFilter(result);
		result = new LowerCaseFilter(result);
		if (!disableStopWords) {
			result = new StopFilter(result, stopSet);
		}
		result = new AccentFilter(result);
		result = new SnowballFilter(result, "English");
		return result;
	}

}