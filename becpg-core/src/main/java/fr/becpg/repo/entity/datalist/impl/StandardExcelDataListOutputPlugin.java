package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

@Service
public class StandardExcelDataListOutputPlugin implements ExcelDataListOutputPlugin {


	@Autowired
	private DictionaryService dictionaryService;
	
	
	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return false;
	}

	@Override
	public ExcelFieldTitleProvider getExcelFieldTitleProvider(DataListFilter dataListFilter) {
		return new  ExcelFieldTitleProvider(){

			@Override
			public String getTitle(AttributeExtractorStructure field) {
				return field.getFieldDef().getTitle(dictionaryService);
			}

			@Override
			public boolean isAllowed(AttributeExtractorStructure field) {
				return !ForumModel.PROP_COMMENT_COUNT.equals(field.getFieldDef().getName());
			}
			
		};
	}

	@Override
	public List<Map<String, Object>> decorate(List<Map<String, Object>> items) throws IOException {
		return items;
	}

}
