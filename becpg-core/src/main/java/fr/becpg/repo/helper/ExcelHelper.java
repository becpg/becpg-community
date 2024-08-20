package fr.becpg.repo.helper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>ExcelHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ExcelHelper {

	public interface ExcelFieldTitleProvider {

		String getTitle(AttributeExtractorStructure field);

		boolean isAllowed(AttributeExtractorStructure field);
	}
	
	public static class ExcelCellStyles {


	    private XSSFWorkbook workbook;


	    private XSSFCellStyle fullDateCellStyle;
	    private XSSFCellStyle shortDateCellStyle;
	    private XSSFCellStyle headerStyle;
	    private XSSFCellStyle headerTextStyle;

	    public ExcelCellStyles(XSSFWorkbook workbook) {
	        this.workbook = workbook;
	    }

	    private XSSFCellStyle createDateStyle(boolean full) {
	        XSSFCellStyle style = workbook.createCellStyle();
	        style.setDataFormat(full ? (short) 14 : (short) 22);
	        return style;
	    }


	    public XSSFCellStyle getFullDateCellStyle() {
	        if (fullDateCellStyle == null) {
	            fullDateCellStyle = createDateStyle(true);
	        }
	        return fullDateCellStyle;
	    }

	    public XSSFCellStyle getShortDateCellStyle() {
	        if (shortDateCellStyle == null) {
	            shortDateCellStyle = createDateStyle(false);
	        }
	        return shortDateCellStyle;
	    }

	    public XSSFCellStyle getHeaderStyle() {
	        if (headerStyle == null) {
	            headerStyle = workbook.createCellStyle();
	            headerStyle.setFillForegroundColor(ExcelHelper.beCPGHeaderColor());
	            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

	            XSSFFont font = workbook.createFont();
	            font.setColor(HSSFColorPredefined.WHITE.getIndex());
	            headerStyle.setFont(font);
	        }
	        return headerStyle;
	    }

	    public XSSFCellStyle getHeaderTextStyle() {
	        if (headerTextStyle == null) {
	            headerTextStyle = workbook.createCellStyle();
	            headerTextStyle.setFillForegroundColor(ExcelHelper.beCPGHeaderTextColor());
	            headerTextStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        }
	        return headerTextStyle;
	    }
	}

	/**
	 * <p>appendExcelField.</p>
	 *
	 * @param computedFields a {@link java.util.List} object.
	 * @param prefix a {@link java.lang.String} object.
	 * @param item a {@link java.util.Map} object.
	 * @param sheet a {@link org.apache.poi.xssf.usermodel.XSSFSheet} object.
	 * @param row a {@link org.apache.poi.ss.usermodel.Row} object.
	 * @param cellnum a int.
	 * @param rowNum a int.
	 * @param supportedLocales a {@link java.util.List} object.
	 * @return a int.
	 */
	public static int appendExcelField(List<AttributeExtractorStructure> computedFields, String prefix, Map<String, Object> item, XSSFSheet sheet,
			Row row, int cellnum, int rowNum, List<Locale> supportedLocales, ExcelCellStyles excelCellStyles) {
		for (AttributeExtractorStructure field : computedFields) {

			if (field.isNested()) {
				cellnum = appendExcelField(field.getChildrens(), field.getFieldName(), item, sheet, row, cellnum, rowNum, supportedLocales, excelCellStyles);
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
						if ((obj instanceof MLText)) {

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
							cell.setCellFormula(shiftFormula((String) obj, rowNum - 1, sheet));
						} else if (field.isFormulaField() && field.getFieldName().startsWith("image") && obj instanceof byte[]) {

							byte[] imageBytes = (byte[]) obj;
							int pictureID = sheet.getWorkbook().addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
							XSSFDrawing drawing = sheet.createDrawingPatriarch();
							XSSFClientAnchor imgAnchor = new XSSFClientAnchor();
							imgAnchor.setCol1(cell.getColumnIndex()); // Sets the column (0 based) of the first cell.
							imgAnchor.setCol2(cell.getColumnIndex() + 1); // Sets the column (0 based) of the Second cell.
							imgAnchor.setRow1(cell.getRowIndex()); // Sets the row (0 based) of the first cell.
							imgAnchor.setRow2(cell.getRowIndex() + 1); // Sets the row (0 based) of the Second cell.

							drawing.createPicture(imgAnchor, pictureID);
							sheet.autoSizeColumn(cell.getColumnIndex());
							cell.getRow().setHeight((short) 1000);

						} else if (obj instanceof Date) {

							cell.setCellValue((Date) obj);
							if (DataTypeDefinition.DATETIME.toString().equals(((PropertyDefinition) field.getFieldDef()).getDataType().toString())) {
								cell.setCellStyle(excelCellStyles.getFullDateCellStyle());
							} else {
								cell.setCellStyle(excelCellStyles.getShortDateCellStyle());
							}
						} else if (obj instanceof Boolean) {
							cell.setCellValue(TranslateHelper.getTranslatedBoolean((Boolean)obj, true) );
						} else if (obj instanceof String) {
							cell.setCellValue((String) obj);
						} else if (obj instanceof Double) {
							cell.setCellValue((double) obj);
						} else if (obj instanceof Integer) {
							cell.setCellValue((int) obj);
						} else if (obj instanceof BigDecimal) {
							cell.setCellValue(((BigDecimal) obj).doubleValue());
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

	

	/**
	 * <p>isExcelType.</p>
	 *
	 * @param value a {@link java.io.Serializable} object.
	 * @return a boolean.
	 */
	public static boolean isExcelType(Serializable value) {
		return (value instanceof Date) || (value instanceof Boolean) || (value instanceof Double) || (value instanceof MLText);
	}

	/**
	 * <p>appendExcelHeader.</p>
	 *
	 * @param fields a {@link java.util.List} object.
	 * @param prefix a {@link java.lang.String} object.
	 * @param titlePrefix a {@link java.lang.String} object.
	 * @param headerRow a {@link org.apache.poi.ss.usermodel.Row} object.
	 * @param labelRow a {@link org.apache.poi.ss.usermodel.Row} object.
	 * @param style a {@link org.apache.poi.xssf.usermodel.XSSFCellStyle} object.
	 * @param sheet a {@link org.apache.poi.xssf.usermodel.XSSFSheet} object.
	 * @param cellnum a int.
	 * @param titleProvider a {@link fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider} object.
	 * @param supportedLocales a {@link java.util.List} object.
	 * @return a int.
	 */
	public static int appendExcelHeader(List<AttributeExtractorStructure> fields, String prefix, String titlePrefix, Row headerRow, Row labelRow,
			ExcelCellStyles excelCellStyles, XSSFSheet sheet, int cellnum, ExcelFieldTitleProvider titleProvider, List<Locale> supportedLocales) {

		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {

					cellnum = appendExcelHeader(field.getChildrens(), field.getFieldName(), titleProvider.getTitle(field), headerRow, labelRow, excelCellStyles,
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
							cell.setCellStyle(excelCellStyles.getHeaderTextStyle());

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
						cell.setCellStyle(excelCellStyles.getHeaderTextStyle());

					}
				}
			}
		}
		return cellnum;

	}


	public static XSSFColor createGreenColor() {
		byte[] rgb = { (byte) 0, (byte) 255, (byte) 0 };
		return new XSSFColor(rgb, new DefaultIndexedColorMap());
	}

	public static XSSFColor createRedColor() {
		byte[] rgb = { (byte) 255, (byte) 0, (byte) 0 };
		return new XSSFColor(rgb, new DefaultIndexedColorMap());
	}

	public static XSSFColor beCPGHeaderColor() {
		byte[] rgb = { (byte) 242, (byte) 247, (byte) 250 };
		return new XSSFColor(rgb, new DefaultIndexedColorMap());
	}

	public static XSSFColor beCPGHeaderTextColor() {
		byte[] rgb = { (byte) 0, (byte) 66, (byte) 84 };
		return new XSSFColor(rgb, new DefaultIndexedColorMap());
	}


}
