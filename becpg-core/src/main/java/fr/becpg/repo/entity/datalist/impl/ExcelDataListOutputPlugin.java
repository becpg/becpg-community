package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public interface ExcelDataListOutputPlugin {

	boolean isDefault();

	boolean applyTo(DataListFilter dataListFilter);
	
    ExcelFieldTitleProvider getExcelFieldTitleProvider(DataListFilter dataListFilter);

	List<Map<String, Object>> decorate(List<Map<String, Object>> items) throws IOException;


}
