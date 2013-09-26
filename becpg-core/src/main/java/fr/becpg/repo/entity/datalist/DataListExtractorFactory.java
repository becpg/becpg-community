package fr.becpg.repo.entity.datalist;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

public interface DataListExtractorFactory {

	
	DataListExtractor getExtractor(DataListFilter dataListFilter);

	void registerExtractor(DataListExtractor extractor);

	
	
}
