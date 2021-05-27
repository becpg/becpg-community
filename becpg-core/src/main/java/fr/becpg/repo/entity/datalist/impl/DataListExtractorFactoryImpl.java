/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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
package fr.becpg.repo.entity.datalist.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * <p>DataListExtractorFactoryImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DataListExtractorFactoryImpl implements DataListExtractorFactory {


	DataListExtractor defaultExtractor;
	
	// extractor with the highest priority goes first
	private static final Comparator<DataListExtractor> extractorComparator = (DataListExtractor e1, DataListExtractor e2) -> ((Integer) e2.getPriority()).compareTo((Integer) e1.getPriority());	
	
	final List<DataListExtractor> extractors = new ArrayList<>();
	
	/** {@inheritDoc} */
	@Override
	public void registerExtractor(DataListExtractor extractor){
		if(extractor.isDefaultExtractor()){
			defaultExtractor = extractor;
		}
		
		extractors.add(extractor);
	}


	/** {@inheritDoc} */
	@Override
	public DataListExtractor getExtractor(DataListFilter dataListFilter) {
	
		List<DataListExtractor> candidateExtractors = new ArrayList<>();
		
		for (DataListExtractor extractor : extractors) {
			 if (extractor.applyTo(dataListFilter)) {
				 candidateExtractors.add(extractor);
			 }
		}
		
		if (!candidateExtractors.isEmpty()) {
			candidateExtractors.sort(extractorComparator);
			return candidateExtractors.get(0);
		}
		
		return defaultExtractor;
	}

}
