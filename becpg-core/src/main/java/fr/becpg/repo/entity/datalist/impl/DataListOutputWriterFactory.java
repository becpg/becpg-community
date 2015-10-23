package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

@Service("datalistOutputWriterFactory")
public class DataListOutputWriterFactory {

	@Autowired
	private CSVDataListOutputWriter csvDataListOutputWriter;
	@Autowired
	private ExcelDataListOutputWriter excelDataListOutputWriter;
	@Autowired
	private JSONDataListOutputWriter jsonDataListOutputWriter;

	public void write(WebScriptResponse res, DataListFilter dataListFilter, PaginatedExtractedItems extractedItems) throws IOException {
		if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat())) {
			csvDataListOutputWriter.write(res, dataListFilter, extractedItems);
		} else if (RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
			excelDataListOutputWriter.write(res, dataListFilter, extractedItems);
		} else {
			jsonDataListOutputWriter.write(res, dataListFilter, extractedItems);
		}

	}

}
