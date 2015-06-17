package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.commons.csv.writer.CSVConfig;
import org.apache.commons.csv.writer.CSVField;
import org.apache.commons.csv.writer.CSVWriter;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public class CSVDataListOutputWriter implements DataListOutputWriter{
	
	private final DictionaryService dictionaryService;
	

	public CSVDataListOutputWriter(DictionaryService dictionaryService) {
		super();
		this.dictionaryService = dictionaryService;
	}



	@Override
	public void write(WebScriptResponse res, PaginatedExtractedItems extractedItems) throws IOException {
		res.setContentType("application/vnd.ms-excel");
		res.setContentEncoding("ISO-8859-1");

		CSVConfig csvConfig = new CSVConfig();

		csvConfig.setDelimiter(';');
		csvConfig.setValueDelimiter('"');
		csvConfig.setIgnoreValueDelimiter(false);

		appendCSVField(csvConfig, extractedItems.getComputedFields(), null);

		CSVWriter csvWriter = new CSVWriter(csvConfig);

		csvWriter.setWriter(res.getWriter());

		Map<String, String> headers = new HashMap<>();
		appendCSVHeader(headers, extractedItems.getComputedFields(), null, null);
		csvWriter.writeRecord(headers);

		writeToCSV(extractedItems, csvWriter);

		res.setHeader("Content-disposition", "attachment; filename=export.csv");
		
	}



	private void writeToCSV(PaginatedExtractedItems extractedItems, CSVWriter csvWriter) {
		for (Map<String, Object> item : extractedItems.getPageItems()) {
			csvWriter.writeRecord(item);
		}
	}
	
	private void appendCSVField(CSVConfig csvConfig, List<AttributeExtractorStructure> fields, String prefix) {
		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {
					appendCSVField(csvConfig, field.getChildrens(), field.getFieldName());
				} else {
					if (prefix != null) {
						csvConfig.addField(new CSVField(prefix + "_" + field.getFieldName()));
					} else {
						csvConfig.addField(new CSVField(field.getFieldName()));
					}
				}
			}
		}
	}

	private void appendCSVHeader(Map<String, String> headers, List<AttributeExtractorStructure> fields, String fieldNamePrefix, String titlePrefix) {
		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {
					appendCSVHeader(headers, field.getChildrens(), field.getFieldName(), field.getFieldDef() != null ? field.getFieldDef().getTitle(dictionaryService) : null);
				} else {
					String fieldName = fieldNamePrefix != null ? fieldNamePrefix + "_" + field.getFieldName() : field.getFieldName();
					String fullTitle = titlePrefix != null ? titlePrefix + " - " + field.getFieldDef().getTitle(dictionaryService) : field.getFieldDef().getTitle(dictionaryService);
					headers.put(fieldName, fullTitle);
				}
			}
		}
	}
	
}
