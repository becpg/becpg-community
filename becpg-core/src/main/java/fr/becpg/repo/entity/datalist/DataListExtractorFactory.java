package fr.becpg.repo.entity.datalist;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

public interface DataListExtractorFactory {

	DataListExtractor getExtractor(DataListFilter dataListFilter,String dataListName);

	void registerExtractor(DataListExtractor extractor);

	
}
