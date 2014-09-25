package fr.becpg.repo.helper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * 
 * @author matthieu
 *
 */
public class ExcelHelper {

	public interface ExcelFieldTitleProvider {

		String getTitle(AttributeExtractorStructure field);

	}

	public static int appendExcelField(List<AttributeExtractorStructure> computedFields, String prefix, Map<String, Object> item, Row row, int cellnum) {
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
				if(obj!=null){
					if (obj instanceof Date) 
						cell.setCellValue((Date) obj);
					else if (obj instanceof Boolean)
						cell.setCellValue((boolean) obj);
					else if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Double)
						cell.setCellValue((double) obj);
				}
			}
		}
		return cellnum;
	}

	public static int appendExcelHeader(List<AttributeExtractorStructure> fields, String prefix, String titlePrefix, Row headerRow, Row labelRow,
			XSSFCellStyle style, int cellnum, ExcelFieldTitleProvider titleProvider) {
		for (AttributeExtractorStructure field : fields) {

			if (field.isNested()) {

				cellnum = appendExcelHeader(field.getChildrens(), field.getFieldName(), titleProvider.getTitle(field), headerRow, labelRow, style,
						cellnum, titleProvider);
			} else {
				Cell cell = headerRow.createCell(cellnum);

				if (prefix != null) {
					cell.setCellValue(prefix + "_" + field.getFieldDef().getName().toPrefixString());
				} else {
					cell.setCellValue(field.getFieldDef().getName().toPrefixString());
				}

				cell = labelRow.createCell(cellnum++);
				if (titlePrefix != null) {
					cell.setCellValue(titlePrefix + " - " + titleProvider.getTitle(field));
				} else {
					cell.setCellValue(titleProvider.getTitle(field));
				}
				cell.setCellStyle(style);
			}
		}
		return cellnum;
	}

}
