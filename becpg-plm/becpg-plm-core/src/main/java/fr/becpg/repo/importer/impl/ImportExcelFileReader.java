package fr.becpg.repo.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.repo.importer.ImportFileReader;

public class ImportExcelFileReader implements ImportFileReader {

	private XSSFWorkbook workbook;
	private XSSFSheet sheet;

	private PropertyFormats propertyFormats;

	public ImportExcelFileReader(InputStream is, PropertyFormats propertyFormats) throws IOException {
		workbook = new XSSFWorkbook(is);
		if (workbook.getNumberOfSheets() > 0) {
			sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
		}
		this.propertyFormats = propertyFormats;
	}

	@Override
	public String[] getLineAt(int importIndex, List<AbstractAttributeMapping> columns) {
		String[] line = null;
		if (sheet != null && importIndex < getTotalLineCount()) {
			Row row = sheet.getRow(importIndex);
			if (row != null) {
				line = extractRow(row,columns);
			} else {
				line = new String[1];
				line[0] = "";
			}
		}
		return line;
	}

	private String[] extractRow(Row row, List<AbstractAttributeMapping> columns) {
		
		List<String> line = new LinkedList<>();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				line.add("");
			} else {
				
				AbstractAttributeMapping attributeMapping = null;
				if(columns!=null && columns.size()>=i && i>1){
				  attributeMapping = columns.get(i-1);	
				}

				int cellType = cell.getCellType();
				
				if(cellType == Cell.CELL_TYPE_FORMULA){
					cellType = cell.getCachedFormulaResultType();
				}
				
				switch (cellType) {
				case Cell.CELL_TYPE_BLANK:
					line.add("");
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					line.add("" + cell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_NUMERIC:	
					if(attributeMapping!=null && attributeMapping.getAttribute() instanceof PropertyDefinition
					&& DataTypeDefinition.TEXT.equals(((PropertyDefinition)attributeMapping.getAttribute()).getDataType().getName())
					|| DataTypeDefinition.MLTEXT.equals(((PropertyDefinition)attributeMapping.getAttribute()).getDataType().getName())
						 ){
						line.add(cell.getStringCellValue());
					} else	
					if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
						line.add(propertyFormats.formatDate(cell.getDateCellValue()));
					} else {
						line.add(propertyFormats.formatDecimal(cell.getNumericCellValue()));
					}
					break;
				case Cell.CELL_TYPE_STRING:
					line.add(cell.getStringCellValue());
					break;
				default:
					line.add("");
					break;
				}
			}

		}
		return line.toArray(new String[line.size()]);
	}

	@Override
	public int getTotalLineCount() {
		int rowCount = sheet.getLastRowNum() ;
		
		if(rowCount>1000) {
			while(rowCount > 2) {
				if( sheet.getRow(rowCount)!=null && sheet.getRow(rowCount).getCell(0)!=null && sheet.getRow(rowCount).getCell(0).getStringCellValue().equals("VALUES")) {
					break;
				}
				rowCount --;
			}
		}
		return rowCount + 1;
	}

	@Override
	public void reportError(int importIndex, String errorMsg, int columnIdx) {
		if (sheet != null && importIndex < sheet.getLastRowNum() + 1) {
			Row row = sheet.getRow(importIndex);
			if (row != null) {
				XSSFCellStyle style = workbook.createCellStyle();

				XSSFColor green = new XSSFColor(new java.awt.Color(255, 0, 0));

				style.setFillForegroundColor(green);
				style.setFillPattern(CellStyle.SOLID_FOREGROUND);

				Cell cell = row.createCell(columnIdx + 1);
				cell.setCellValue(errorMsg);

				for (int i = 0; i < row.getLastCellNum(); i++) {
					cell = row.getCell(i);
					if (cell != null) {
						cell.setCellStyle(style);
					}
				}
			}

		}
	}

	@Override
	public void writeErrorInFile(ContentWriter writer) throws IOException {
		if (workbook != null) {

			try (OutputStream out = writer.getContentOutputStream()) {
				workbook.write(out);
			}

		}
	}

	@Override
	public void reportSuccess(int index, int columnIdx) {
		if (sheet != null && index < sheet.getLastRowNum() + 1) {
			Row row = sheet.getRow(index);
			if (row != null) {
				XSSFCellStyle style = workbook.createCellStyle();

				XSSFColor green = new XSSFColor(new java.awt.Color(0, 255, 0));

				style.setFillForegroundColor(green);
				style.setFillPattern(CellStyle.SOLID_FOREGROUND);

				for (int i = 0; i < row.getLastCellNum(); i++) {
					Cell cell = row.getCell(i);
					if (cell != null) {
						cell.setCellStyle(style);
					}
				}
			}

		}

	}

}
