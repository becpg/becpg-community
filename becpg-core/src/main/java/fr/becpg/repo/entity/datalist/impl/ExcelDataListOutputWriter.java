package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
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
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
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
		res.setHeader("Content-disposition", "attachment; filename=export.xlsx");

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

		ExcelHelper.appendExcelHeader(extractedItems.getComputedFields(), null, null, headerRow, labelRow, style, cellnum, new ExcelFieldTitleProvider() {
			
			@Override
			public String getTitle(AttributeExtractorStructure field) {
				return field.getFieldDef().getTitle(dictionaryService);
			}
		});

		for (Map<String, Object> item : extractedItems.getPageItems()) {
			Row row = sheet.createRow(rownum++);
			
			cell = row.createCell(0);
			cell.setCellValue("VALUES");

			ExcelHelper.appendExcelField(extractedItems.getComputedFields(), null, item, row, 1);

		}

		workbook.write(res.getOutputStream());

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
