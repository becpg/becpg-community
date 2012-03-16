package fr.becpg.repo.entity.datalist;

import org.alfresco.service.namespace.QName;

public interface DataListExtractorFactory {

	DataListExtractor getExtractor(String dataListName, QName dataType);
	
}
