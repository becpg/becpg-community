package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 *
 * @author matthieu
 *
 */
public class ExcelHelper {

	public interface ExcelFieldTitleProvider {

		String getTitle(AttributeExtractorStructure field);

		boolean isAllowed(AttributeExtractorStructure field);
	}

	public static int appendExcelField(List<AttributeExtractorStructure> computedFields, String prefix, Map<String, Object> item, XSSFSheet sheet,
			Row row, int cellnum, int rowNum, List<Locale> supportedLocales) {
		for (AttributeExtractorStructure field : computedFields) {
			if (field.isNested()) {
				cellnum = appendExcelField(field.getChildrens(), field.getFieldName(), item, sheet, row, cellnum, rowNum, supportedLocales);
			} else {

				Object obj;
				if (prefix != null) {
					obj = item.get(prefix + "_" + field.getFieldName());
				} else {
					obj = item.get(field.getFieldName());
				}

				if ((supportedLocales != null) && !supportedLocales.isEmpty() && (field.getFieldDef() instanceof PropertyDefinition)
						&& DataTypeDefinition.MLTEXT.toString().equals(((PropertyDefinition) field.getFieldDef()).getDataType().toString())) {

					for (Locale locale : supportedLocales) {
						Cell cell = row.createCell(cellnum++);
						if ((obj != null) && (obj instanceof MLText)) {

							String value = null;

							if (MLTextHelper.isDefaultLocale(locale)) {
								value = MLTextHelper.getClosestValue(((MLText) obj), locale);
							} else {
								value = ((MLText) obj).get(locale);
							}

							if (value != null) {
								cell.setCellValue(value);
							}

						}

					}
				} else {

					Cell cell = row.createCell(cellnum++);

					if (obj != null) {

						if (field.isFormulaField() && field.getFieldName().startsWith("excel")) {
							cell.setCellFormula(shiftFormula((String) obj, rowNum-1, sheet));
						} else if (obj instanceof Date) {
							cell.setCellValue((Date) obj);
							if (DataTypeDefinition.DATETIME.toString().equals(((PropertyDefinition) field.getFieldDef()).getDataType().toString())) {
								cell.setCellStyle(createDateStyle(sheet.getWorkbook(), true));
							} else {
								cell.setCellStyle(createDateStyle(sheet.getWorkbook(), true));
							}
						} else if (obj instanceof Boolean) {
							cell.setCellValue((boolean) obj);
						} else if (obj instanceof String) {
							cell.setCellValue((String) obj);
						} else if (obj instanceof Double) {
							cell.setCellValue((double) obj);
						} else if (obj instanceof Integer) {
							cell.setCellValue((int) obj);
						}
					}
				}
			}
		}
		return cellnum;
	}

	private static String shiftFormula(String formula, int rowNum, XSSFSheet sheet) {
		XSSFEvaluationWorkbook workbookWrapper = XSSFEvaluationWorkbook.create(sheet.getWorkbook());
		Ptg[] ptgs = FormulaParser.parse(formula, workbookWrapper, FormulaType.CELL, sheet.getWorkbook().getSheetIndex(sheet));

		for (Ptg ptg : ptgs) {
			if (ptg instanceof RefPtgBase) { // base class for cell references
				RefPtgBase ref = (RefPtgBase) ptg;

				if (ref.isRowRelative()) {
					ref.setRow(rowNum);
				}
			} else if (ptg instanceof AreaPtgBase) { // base class for range
														// references
				AreaPtgBase ref = (AreaPtgBase) ptg;

				if (ref.isFirstRowRelative()) {
					ref.setFirstRow(rowNum);
				}
				if (ref.isLastRowRelative()) {
					ref.setLastRow(rowNum);
				}
			}
		}

		formula = FormulaRenderer.toFormulaString(workbookWrapper, ptgs);
		return formula;
	}

	private static CellStyle createDateStyle(XSSFWorkbook workbook, boolean full) {

		XSSFCellStyle style = workbook.createCellStyle();
		if (full) {
			style.setDataFormat((short) 14);
		} else {
			style.setDataFormat((short) 22);
		}
		return style;
	}

	public static boolean isExcelType(Serializable value) {
		return (value instanceof Date) || (value instanceof Boolean) || (value instanceof Double) || (value instanceof MLText);
	}

	public static int appendExcelHeader(List<AttributeExtractorStructure> fields, String prefix, String titlePrefix, Row headerRow, Row labelRow,
			XSSFCellStyle style, XSSFSheet sheet, int cellnum, ExcelFieldTitleProvider titleProvider, List<Locale> supportedLocales) {

		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {

					cellnum = appendExcelHeader(field.getChildrens(), field.getFieldName(), titleProvider.getTitle(field), headerRow, labelRow, style,
							sheet, cellnum, titleProvider, supportedLocales);
				} else {

					if ((supportedLocales != null) && !supportedLocales.isEmpty() && (field.getFieldDef() instanceof PropertyDefinition)
							&& DataTypeDefinition.MLTEXT.toString().equals(((PropertyDefinition) field.getFieldDef()).getDataType().toString())) {

						int groupFirstColumn = cellnum;

						for (Locale locale : supportedLocales) {

							Cell cell = headerRow.createCell(cellnum);

							if (prefix != null) {
								cell.setCellValue(
										prefix + "_" + field.getFieldDef().getName().toPrefixString() + "_" + MLTextHelper.localeKey(locale));
							} else {
								if (MLTextHelper.isDefaultLocale(locale)) {
									cell.setCellValue(field.getFieldDef().getName().toPrefixString());
								} else {
									cell.setCellValue(field.getFieldDef().getName().toPrefixString() + "_" + MLTextHelper.localeKey(locale));
								}
							}

							cell = labelRow.createCell(cellnum++);
							if (titlePrefix != null) {
								cell.setCellValue(titlePrefix + " - " + titleProvider.getTitle(field) + " - " + MLTextHelper.localeLabel(locale));
							} else {
								cell.setCellValue(titleProvider.getTitle(field) + " - " + MLTextHelper.localeLabel(locale));
							}
							cell.setCellStyle(style);

						}

						sheet.groupColumn(groupFirstColumn, cellnum - 1);

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
			}
		}
		return cellnum;

	}

}
