package fr.becpg.repo.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.importer.ImportFileReader;
import fr.becpg.repo.importer.ImporterException;

/**
 * <p>ImportExcelFileReader class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ImportExcelFileReader implements ImportFileReader {

	private XSSFWorkbook workbook;
	private XSSFSheet sheet;

	private PropertyFormats propertyFormats;

	/**
	 * <p>Constructor for ImportExcelFileReader.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @param propertyFormats a {@link fr.becpg.config.format.PropertyFormats} object.
	 * @throws java.io.IOException if any.
	 */
	public ImportExcelFileReader(InputStream is, PropertyFormats propertyFormats) throws IOException {
		workbook = new XSSFWorkbook(is);
		if (workbook.getNumberOfSheets() > 0) {
			sheet = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
		}
		this.propertyFormats = propertyFormats;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getLineAt(int importIndex, List<AbstractAttributeMapping> columns) throws ImporterException{
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
				if(columns!=null && columns.size()>(i-1) && i>0){
				  attributeMapping = columns.get(i-1);	
				}

				CellType cellType = cell.getCellType();
				
				if(cellType == CellType.FORMULA){
					cellType = cell.getCachedFormulaResultType();
				}
				
				switch (cellType) {
				case  BLANK:
					line.add("");
					break;
				case BOOLEAN:
					line.add("" + cell.getBooleanCellValue());
					break;
				case NUMERIC:	
					if(attributeMapping!=null && attributeMapping.getAttribute() instanceof PropertyDefinition
					&& (DataTypeDefinition.TEXT.equals(((PropertyDefinition)attributeMapping.getAttribute()).getDataType().getName())
					|| DataTypeDefinition.MLTEXT.equals(((PropertyDefinition)attributeMapping.getAttribute()).getDataType().getName())
					|| DataTypeDefinition.NODE_REF.equals(((PropertyDefinition)attributeMapping.getAttribute()).getDataType().getName())
							)
						 ){
						line.add(new DecimalFormat("#########.###").format(cell.getNumericCellValue()));
					} else	
					if (DateUtil.isCellDateFormatted(cell) || DateUtil.isCellInternalDateFormatted(cell)) {
						line.add(propertyFormats.formatDateTime(cell.getDateCellValue()));
					} else {
						line.add(propertyFormats.formatDecimal(cell.getNumericCellValue()));
					}
					break;
				case STRING:
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

	/** {@inheritDoc} */
	@Override
	public int getTotalLineCount() {
		int rowCount = sheet.getLastRowNum() ;
		
		while (rowCount > 2) {
			if( sheet.getRow(rowCount)!=null && sheet.getRow(rowCount).getCell(0)!=null && sheet.getRow(rowCount).getCell(0).getStringCellValue().equals("VALUES")) {
				break;
			}
			rowCount --;
		}
		return rowCount + 1;
	}

	/** {@inheritDoc} */
	@Override
	public void reportError(int importIndex, String errorMsg, int columnIdx) {
		if (sheet != null && importIndex < sheet.getLastRowNum() + 1) {
			Row row = sheet.getRow(importIndex);
			if (row != null) {
				XSSFCellStyle style = workbook.createCellStyle();
			

				style.setFillForegroundColor(ExcelHelper.createRedColor());
				style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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

	/** {@inheritDoc} */
	@Override
	public void writeErrorInFile(ContentWriter writer) throws IOException {
		if (workbook != null) {

			try (OutputStream out = writer.getContentOutputStream()) {
				workbook.write(out);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public void reportSuccess(int index, int columnIdx) {
		if (sheet != null && index < sheet.getLastRowNum() + 1) {
			Row row = sheet.getRow(index);
			if (row != null) {
				XSSFCellStyle style = workbook.createCellStyle();

				style.setFillForegroundColor(ExcelHelper.createGreenColor());
				style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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