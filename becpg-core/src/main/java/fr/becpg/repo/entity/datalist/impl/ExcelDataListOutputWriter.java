package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
		res.setHeader("Content-disposition", "attachment; filename=export.xls");

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(dataListFilter.getDataListName());
		sheet.setColumnHidden(0, true);
		int rownum = 0;
		Row headerRow = sheet.createRow(rownum++);
		Cell cell = headerRow.createCell(0);
		cell.setCellValue("TYPE");
		cell = headerRow.createCell(1);
		cell.setCellValue(dataListFilter.getDataType().toPrefixString());
		headerRow.setZeroHeight(true);
		headerRow = sheet.createRow(rownum++);
		headerRow.setZeroHeight(true);
		Row labelRow = sheet.createRow(rownum++);
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();

		XSSFColor green = new XSSFColor(new java.awt.Color(0, 102, 0));

		style.setFillForegroundColor(green);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		int cellnum = 0;
		cell = headerRow.createCell(cellnum);
		headerRow.setZeroHeight(true);
		cell.setCellValue("COLUMNS");
		cell = labelRow.createCell(cellnum++);
		cell.setCellValue("#");

		appendExcelHeader(extractedItems.getComputedFields(), null, null, headerRow, labelRow, style, cellnum);

		for (Map<String, Object> item : extractedItems.getPageItems()) {
			Row row = sheet.createRow(rownum++);
			
			cell = row.createCell(0);
			cell.setCellValue("VALUES");

			appendExcelField(extractedItems.getComputedFields(), null, item, row, 1);

		}

		workbook.write(res.getOutputStream());

	}

	private int appendExcelField(List<AttributeExtractorStructure> computedFields, String prefix, Map<String, Object> item, Row row, int cellnum) {
		for (AttributeExtractorStructure field : computedFields) {
			if (field.isNested()) {
			cellnum = appendExcelField(field.getChildrens(), field.getFieldName(), item, row, cellnum);
			} else {
				Cell cell = row.createCell(cellnum++);

				Object obj = null;
				if (prefix != null) {
					obj = item.get(prefix + "_" + field.getFieldName());
				} else {
					obj = item.get(field.getFieldName());
				}
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
		return cellnum;
	}

	private int appendExcelHeader(List<AttributeExtractorStructure> fields, String prefix, String titlePrefix, Row headerRow, Row labelRow,
			XSSFCellStyle style, int cellnum) {
		for (AttributeExtractorStructure field : fields) {

			if (field.isNested()) {

				cellnum = appendExcelHeader(field.getChildrens(), field.getFieldName(), getTitle(field), headerRow, labelRow, style, cellnum);
			} else {
				Cell cell = headerRow.createCell(cellnum);

				if (prefix != null) {
					cell.setCellValue(prefix + "_" + field.getFieldDef().getName().toPrefixString());
				} else {
					cell.setCellValue(field.getFieldDef().getName().toPrefixString());
				}

				cell = labelRow.createCell(cellnum++);
				if (titlePrefix != null) {
					cell.setCellValue(titlePrefix + " - " + getTitle(field));
				} else {
					cell.setCellValue(getTitle(field));
				}
				cell.setCellStyle(style);
			}
		}
		return cellnum;
	}

	private String getTitle(AttributeExtractorStructure field) {
		return field.getFieldDef().getTitle(dictionaryService);
	}

	// private void setNamedRangeCellDataValidation(HSSFSheet sheet, int
	// firstRow, int firstColumn, int lastRow, int lastColumn, String rangeName)
	// {
	//
	// CellRangeAddressList rangeList = new CellRangeAddressList();
	// rangeList.addCellRangeAddress(new CellRangeAddress(firstRow, lastRow,
	// firstColumn, lastColumn));
	//
	// DVConstraint dvconstraint =
	// DVConstraint.createFormulaListConstraint(rangeName);
	//
	// HSSFDataValidation dataValidation = new HSSFDataValidation(rangeList,
	// dvconstraint);
	//
	// sheet.addValidationData(dataValidation);
	// }

}
