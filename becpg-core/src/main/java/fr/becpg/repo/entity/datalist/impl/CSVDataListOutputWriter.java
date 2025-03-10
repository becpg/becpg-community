package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>CSVDataListOutputWriter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class CSVDataListOutputWriter implements DataListOutputWriter {

	@Autowired
	private DictionaryService dictionaryService;

	/** {@inheritDoc} */
	@Override
	public void write(WebScriptRequest req, WebScriptResponse res, DataListFilter dataListFilter, PaginatedExtractedItems extractedItems)
			throws IOException {
		res.setContentType("application/vnd.ms-excel");
		res.setContentEncoding("ISO-8859-1");

		CSVFormat format = CSVFormat.EXCEL.builder().setQuoteMode(QuoteMode.ALL).setDelimiter(';').build();

		appendCSVField(format, extractedItems.getComputedFields(), null);

		try (CSVPrinter printer = new CSVPrinter(res.getWriter(), format)) {

			Map<String, String> headers = new HashMap<>();
			appendCSVHeader(headers, extractedItems.getComputedFields(), null, null);
			printer.printRecord(headers);

			writeToCSV(extractedItems, printer);

		}

		AttachmentHelper.setAttachment(req, res, "export.csv");

	}

	private void writeToCSV(PaginatedExtractedItems extractedItems, CSVPrinter printer) throws IOException {
		for (Map<String, Object> item : extractedItems.getPageItems()) {
			printer.printRecord(item);
		}
	}

	private void appendCSVField(CSVFormat csvConfig, List<AttributeExtractorStructure> fields, String prefix) {
		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {
					appendCSVField(csvConfig, field.getChildrens(), field.getFieldName());
				} else {
					if (prefix != null) {
						csvConfig.builder().setHeader(prefix + "_" + field.getFieldName()).build();
					} else {
						csvConfig.builder().setHeader(field.getFieldName()).build();
					}
				}
			}
		}
	}

	private void appendCSVHeader(Map<String, String> headers, List<AttributeExtractorStructure> fields, String fieldNamePrefix, String titlePrefix) {
		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {
					appendCSVHeader(headers, field.getChildrens(), field.getFieldName(),
							field.getFieldDef() != null ? field.getFieldDef().getTitle(dictionaryService) : null);
				} else {
					String fieldName = fieldNamePrefix != null ? fieldNamePrefix + "_" + field.getFieldName() : field.getFieldName();
					String fullTitle = titlePrefix != null ? titlePrefix + " - " + field.getFieldDef().getTitle(dictionaryService)
							: field.getFieldDef().getTitle(dictionaryService);
					headers.put(fieldName, fullTitle);
				}
			}
		}
	}

}
