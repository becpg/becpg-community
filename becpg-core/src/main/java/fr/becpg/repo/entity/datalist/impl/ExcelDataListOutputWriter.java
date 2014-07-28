package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public class ExcelDataListOutputWriter implements DataListOutputWriter {

	private DictionaryService dictionaryService;

	private DataListFilter dataListFilter;

	public ExcelDataListOutputWriter(DictionaryService dictionaryService, DataListFilter dataListFilter) {
		super();
		this.dictionaryService = dictionaryService;
		this.dataListFilter = dataListFilter;
	}

	@Override
	public void write(WebScriptResponse res, PaginatedExtractedItems extractedItems) throws IOException {
		res.setContentType("application/vnd.ms-excel");

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(dataListFilter.getDataListName());
		int rownum = 0;
		Row headerRow = sheet.createRow(rownum++);
		Cell cell = headerRow.createCell(0);
		cell.setCellValue("TYPE");
		cell = headerRow.createCell(1);
		cell.setCellValue(dataListFilter.getDataType().toPrefixString());
		headerRow = sheet.createRow(rownum++);
		Row labelRow = sheet.createRow(rownum++);
		CellStyle style = workbook.createCellStyle();
		style.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		style.setFillPattern(CellStyle.ALIGN_FILL);
		int cellnum = 0;
		cell = headerRow.createCell(cellnum);
		cell.setCellValue("COLUMNS");
		cell = labelRow.createCell(cellnum++);
		cell.setCellValue("#");
		for (AttributeExtractorStructure field : extractedItems.getComputedFields()) {

			if (field.isNested()) {

			} else {
				cell = headerRow.createCell(cellnum);
				cell.setCellValue(field.getFieldDef().getName().toPrefixString());
				cell = labelRow.createCell(cellnum++);
				cell.setCellValue(getTitle(field));
				cell.setCellStyle(style);
			}
		}

		for (Map<String, Object> item : extractedItems.getPageItems()) {
			Row row = sheet.createRow(rownum++);
			cellnum = 0;
			cell = row.createCell(cellnum++);
			cell.setCellValue("VALUES");
			for (AttributeExtractorStructure field : extractedItems.getComputedFields()) {
				if (field.isNested()) {

				} else {
					cell = row.createCell(cellnum++);
					Object obj = item.get(field.getFieldName());
					if (obj instanceof Date)
						cell.setCellValue((Date) obj);
					else if (obj instanceof Boolean)
						cell.setCellValue((Boolean) obj);
					else if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Double)
						cell.setCellValue((Double) obj);
				}
			}

		}

		workbook.write(res.getOutputStream());
		res.setHeader("Content-disposition", "attachment; filename=export.xls");

	}

	private String getTitle(AttributeExtractorStructure field) {
		return field.getFieldDef().getTitle(dictionaryService);
	}

//	private void setNamedRangeCellDataValidation(HSSFSheet sheet, int firstRow, int firstColumn, int lastRow, int lastColumn, String rangeName) {
//
//		CellRangeAddressList rangeList = new CellRangeAddressList();
//		rangeList.addCellRangeAddress(new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn));
//
//		DVConstraint dvconstraint = DVConstraint.createFormulaListConstraint(rangeName);
//
//		HSSFDataValidation dataValidation = new HSSFDataValidation(rangeList, dvconstraint);
//
//		sheet.addValidationData(dataValidation);
//	}

}
