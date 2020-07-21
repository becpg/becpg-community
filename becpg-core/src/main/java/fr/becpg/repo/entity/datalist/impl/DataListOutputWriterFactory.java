package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * <p>DataListOutputWriterFactory class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("datalistOutputWriterFactory")
public class DataListOutputWriterFactory {

	@Autowired
	private CSVDataListOutputWriter csvDataListOutputWriter;
	@Autowired
	private ExcelDataListOutputWriter excelDataListOutputWriter;
	@Autowired
	private JSONDataListOutputWriter jsonDataListOutputWriter;

	/**
	 * <p>write.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @param res a {@link org.springframework.extensions.webscripts.WebScriptResponse} object.
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @param extractedItems a {@link fr.becpg.repo.entity.datalist.PaginatedExtractedItems} object.
	 * @throws java.io.IOException if any.
	 */
	public void write(WebScriptRequest req, WebScriptResponse res, DataListFilter dataListFilter, PaginatedExtractedItems extractedItems) throws IOException {
		if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat())) {
			csvDataListOutputWriter.write(req, res, dataListFilter, extractedItems);
		} else if (RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
			excelDataListOutputWriter.write(req, res, dataListFilter, extractedItems);
		} else {
			jsonDataListOutputWriter.write(req, res, dataListFilter, extractedItems);
		}

	}

}
