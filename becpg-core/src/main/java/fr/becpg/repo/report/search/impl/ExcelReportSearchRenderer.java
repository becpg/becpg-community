package fr.becpg.repo.report.search.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.report.search.SearchReportRenderer;
import fr.becpg.repo.report.search.actions.ExcelSearchAction;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.report.client.ReportFormat;

@Service
public class ExcelReportSearchRenderer implements SearchReportRenderer {

	private final static Log logger = LogFactory.getLog(ExcelReportSearchRenderer.class);
	
	@Autowired
	private ActionService actionService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private ExcelReportSearchPlugin[] excelReportSearchPlugins;

	@Override
	public void renderReport(NodeRef tplNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream) {

		ContentReader reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);

		StopWatch watch = null;

		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(reader.getContentInputStream())){;

			QName mainType = null;
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				XSSFSheet sheet = workbook.getSheetAt(i);
				mainType = fillSheet(sheet, searchResults, mainType);
			}
		  //Disable for XLSM (break #2259 ?) workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
			workbook.setForceFormulaRecalculation(true);
			workbook.write(outputStream);

		} catch (ContentIOException | IOException e) {
			logger.error("Error generating xls report", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.warn("Render excel report executed in  " + watch.getTotalTimeSeconds() + " seconds");
			}
		}

	}

	public class ExcelSheetExportContext {

		List<AttributeExtractorStructure> metadataFields;
		String[] parameters;
		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();
		QName itemType;
		QName mainType;
		AttributeExtractorStructure keyColumn;
		int rownum = 0;

		public ExcelSheetExportContext(QName mainType, QName itemType, int rownum, List<String> parameters, AttributeExtractorStructure keyColumn,
				List<AttributeExtractorStructure> metadataFields) {
			this.parameters = parameters.toArray(new String[parameters.size()]);
			this.rownum = rownum;
			this.keyColumn = keyColumn;
			this.metadataFields = metadataFields;
			this.itemType = itemType;
			this.mainType = mainType;
		}

		public List<AttributeExtractorStructure> getMetadataFields() {
			return metadataFields;
		}

		public String[] getParameters() {
			return parameters;
		}

		public Map<NodeRef, Map<String, Object>> getCache() {
			return cache;
		}

		public QName getItemType() {
			return itemType;
		}

		public AttributeExtractorStructure getKeyColumn() {
			return keyColumn;
		}

		public int getRownum() {
			return rownum;
		}


		public void setRownum(int rownum) {
			this.rownum = rownum;
		}

		public QName getMainType() {
			return mainType;
		}

		
		
	}

	public ExcelSheetExportContext readHeader(XSSFSheet sheet, QName mainType) {
		int rownum = 0;
		Row headerRow = sheet.getRow(rownum++);

		if ((headerRow.getCell(0) != null) && "TYPE".equals(headerRow.getCell(0).getStringCellValue())) {
			sheet.setColumnHidden(0, true);

			QName itemType = QName.createQName(headerRow.getCell(1).getStringCellValue(), namespaceService);

			List<String> parameters = new LinkedList<>();
			if (headerRow.getCell(2) != null) {
				parameters.add(headerRow.getCell(2).getStringCellValue());
			}

			if (headerRow.getCell(3) != null) {
				parameters.add(headerRow.getCell(3).getStringCellValue());
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Sheet type : " + itemType.toPrefixString());
			}

			headerRow.setZeroHeight(true);
			headerRow = sheet.getRow(rownum++);
			headerRow.setZeroHeight(true);
			List<AttributeExtractorStructure> metadataFields = extractListStruct(itemType, headerRow);
			AttributeExtractorStructure keyColumn = null;
			if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				keyColumn = metadataFields.remove(0);
				logger.debug("Datalist key column : " + keyColumn.getFieldDef().getName());
			} else {
				mainType = itemType;
			}

			rownum++;

			while ((sheet.getRow(rownum) != null) && (sheet.getRow(rownum).getCell(0) != null)
					&& "#".equals(sheet.getRow(rownum).getCell(0).getStringCellValue())) {
				rownum++;
			}

			return new ExcelSheetExportContext(mainType, itemType, rownum, parameters, keyColumn, metadataFields);
		}
		return null;
	}

	public QName fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, ExcelSheetExportContext excelSheetExportContext) {
		if (excelSheetExportContext != null) {
			ExcelReportSearchPlugin plugin = null;

			for (ExcelReportSearchPlugin tmp : excelReportSearchPlugins) {
				if ((tmp.isDefault() && (plugin == null))
						|| tmp.isApplicable(excelSheetExportContext.getItemType(), excelSheetExportContext.getParameters())) {
					plugin = tmp;
				}
			}

			if (plugin != null) {
				excelSheetExportContext.setRownum(plugin.fillSheet(sheet, searchResults, excelSheetExportContext.getMainType(), excelSheetExportContext.getItemType(), excelSheetExportContext.getRownum(),
						excelSheetExportContext.getParameters(), excelSheetExportContext.getKeyColumn(), excelSheetExportContext.getMetadataFields(),
						excelSheetExportContext.getCache()));
			
			
			} else {
				logger.error("No plugin found for : " + excelSheetExportContext.getItemType().toString());
			}
			
			return excelSheetExportContext.getMainType();

		}
		
		return null;
	}

	private QName fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType) {

		ExcelSheetExportContext excelSheetExportContext = readHeader(sheet, mainType);

		return fillSheet(sheet, searchResults, excelSheetExportContext);

	}

	private List<AttributeExtractorStructure> extractListStruct(QName itemType, Row headerRow) {
		List<String> metadataFields = new LinkedList<>();
		String currentNested = "";
		for (int i = 1; i < headerRow.getLastCellNum(); i++) {
			if (headerRow.getCell(i) != null) {
				if (headerRow.getCell(i).getCellType() == CellType.STRING) {
					String cellValue = headerRow.getCell(i).getStringCellValue();
					if ((cellValue != null) && !cellValue.isEmpty() && !cellValue.startsWith("#")) {
						if (cellValue.contains("_") && !cellValue.contains("formula") && !cellValue.startsWith("dyn_")) {
							if (!currentNested.isEmpty() && currentNested.startsWith(cellValue.split("_")[0])) {
								currentNested += "|" + cellValue.split("_")[1];
							} else {
								if (!currentNested.isEmpty()) {
									logger.debug("Add nested field : " + currentNested);
									metadataFields.add(currentNested);
									currentNested = "";
								}
								
								if (MLTextHelper.getSupportedLocalesList() != null && MLTextHelper.getSupportedLocalesList().contains(cellValue.substring(cellValue.indexOf("_")+1))){
									currentNested = cellValue.replaceFirst("_", "|");
									metadataFields.add(currentNested);
									currentNested = "";
									
								} else {
									currentNested = cellValue.replace("_", "|");
								}
							}

						} else {
							if (!currentNested.isEmpty() && !cellValue.contains("formula")) {
								logger.debug("Add nested field : " + currentNested);
								metadataFields.add(currentNested);
								currentNested = "";
							}
							logger.debug("Add field : " + cellValue);
							metadataFields.add(cellValue);
						}
					}
				} else if (headerRow.getCell(i).getCellType() == CellType.FORMULA) {
					String cellFormula = headerRow.getCell(i).getCellFormula();
					metadataFields.add("excel|" + cellFormula);
				}
			}

		}

		if (!currentNested.isEmpty()) {
			logger.debug("Add nested field : " + currentNested);
			metadataFields.add(currentNested);
		}

		return attributeExtractorService.readExtractStructure(itemType, metadataFields);
	}

	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return ReportFormat.XLSX.equals(reportFormat) &&  (((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME))
				.endsWith(ReportTplService.PARAM_VALUE_XLSXREPORT_EXTENSION) || ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME))
				.endsWith(ReportTplService.PARAM_VALUE_XLSMREPORT_EXTENSION));
	}

	@Override
	public void executeAction(NodeRef templateNodeRef, NodeRef downloadNode, ReportFormat reportFormat) {
		Action action = actionService.createAction("excelSearchAction");
		action.setExecuteAsynchronously(true);
		action.setParameterValue(ExcelSearchAction.PARAM_TPL_NODEREF, templateNodeRef);
		actionService.executeAction(action, downloadNode);

	}

}
