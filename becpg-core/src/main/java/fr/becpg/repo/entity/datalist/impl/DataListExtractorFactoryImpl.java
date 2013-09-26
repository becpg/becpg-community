package fr.becpg.repo.entity.datalist.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

@Service
public class DataListExtractorFactoryImpl implements DataListExtractorFactory {


	DataListExtractor defaultExtractor;
	
	
	List<DataListExtractor> extractors = new ArrayList<>();
	
	@Override
	public void registerExtractor(DataListExtractor extractor){
		if(extractor.isDefaultExtractor()){
			defaultExtractor = extractor;
		}
		
		extractors.add(extractor);
	}


	@Override
	public DataListExtractor getExtractor(DataListFilter dataListFilter) {
	
		for(DataListExtractor extractor : extractors){
			 if(extractor.applyTo(dataListFilter )){
				 return extractor;
			 }
		}
		return defaultExtractor;
	}

}
