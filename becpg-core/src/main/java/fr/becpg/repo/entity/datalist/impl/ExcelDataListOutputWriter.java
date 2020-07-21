package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>ExcelDataListOutputWriter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ExcelDataListOutputWriter implements DataListOutputWriter {

	@Autowired
	private ExcelDataListOutputPlugin[] plugins;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;

	/** {@inheritDoc} */
	@Override
	public void write(WebScriptRequest req, WebScriptResponse res, DataListFilter dataListFilter, PaginatedExtractedItems extractedItems)
			throws IOException {

		res.setContentType("application/vnd.ms-excel");

		AttachmentHelper.setAttachment(req, res, getFileName(dataListFilter));

		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			PaginatedExtractedItems extractedExtrasItems = null;
			boolean hasExtrasSheet;

			do {
				hasExtrasSheet = false;
				String sheetName = "";

				if (dataListFilter.getDataType() != null) {
					TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(dataListFilter.getDataType());

					if (typeDef != null) {
						sheetName = typeDef.getTitle(serviceRegistry.getDictionaryService());
					} else {
						AspectDefinition aspectDef = serviceRegistry.getDictionaryService().getAspect(dataListFilter.getDataType());
						if (aspectDef != null) {
							sheetName = aspectDef.getTitle(serviceRegistry.getDictionaryService());
						}
					}
				}

				if ((sheetName == null) || sheetName.isEmpty()) {
					sheetName = "Values";
				}
				XSSFSheet sheet = workbook.createSheet(sheetName);
				XSSFCellStyle style = workbook.createCellStyle();
				
				byte[] rgb = {(byte)  242, (byte) 247, (byte) 250};
				
				style.setFillForegroundColor(new XSSFColor(rgb, new DefaultIndexedColorMap()));
				style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

				int rownum = 0;

				Row headerRow = sheet.createRow(rownum++);
				headerRow.setRowStyle(style);
				Cell cell = headerRow.createCell(0);
				cell.setCellValue("MAPPING");
				cell = headerRow.createCell(1);
				cell.setCellValue("Default");

				headerRow = sheet.createRow(rownum++);
				cell = headerRow.createCell(0);
				headerRow.setRowStyle(style);
				cell.setCellValue("TYPE");
				cell = headerRow.createCell(1);
				cell.setCellValue(dataListFilter.getDataType().toPrefixString());

				String nodePath = null;
				String bcpgCode = null;

				if (dataListFilter.getEntityNodeRef() != null) {
					if (entityDictionaryService.isSubClass(nodeService.getType(dataListFilter.getEntityNodeRef()), BeCPGModel.TYPE_SYSTEM_ENTITY)) {
						nodePath = cleanPath(nodeService.getPath(dataListFilter.getParentNodeRef()).toPrefixString(namespaceService));
					} else {

						bcpgCode = (String) nodeService.getProperty(dataListFilter.getEntityNodeRef(), BeCPGModel.PROP_CODE);

						nodePath = cleanPath(nodeService.getPath(nodeService.getPrimaryParent(dataListFilter.getEntityNodeRef()).getParentRef())
										.toPrefixString(namespaceService));
					}
					
				} else if (dataListFilter.getParentNodeRef() != null) {
					nodePath = cleanPath(nodeService.getPath(dataListFilter.getParentNodeRef()).toPrefixString(namespaceService));
					
				} else if (dataListFilter.getFilterId().equals(DataListFilter.NODE_PATH_FILTER)) {
					nodePath = cleanPath(nodeService.getPath(new NodeRef(dataListFilter.getFilterData())).toPrefixString(namespaceService));
				}
				
				if(nodePath != null) {
					headerRow = sheet.createRow(rownum++);
					headerRow.setRowStyle(style);
					cell = headerRow.createCell(0);
					cell.setCellValue("PATH");
					cell = headerRow.createCell(1);
					cell.setCellValue(nodePath);
				}
				
				if (entityDictionaryService.isSubClass(dataListFilter.getDataType(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
					if(nodePath != null && !nodePath.startsWith("/System/")) {
						headerRow = sheet.createRow(rownum++);
						headerRow.setRowStyle(style);
						cell = headerRow.createCell(0);
						cell.setCellValue("IMPORT_TYPE");
						cell = headerRow.createCell(1);
						cell.setCellValue("EntityListItem");
					}

					headerRow = sheet.createRow(rownum++);
					headerRow.setRowStyle(style);
					cell = headerRow.createCell(0);
					cell.setCellValue("DELETE_DATALIST");
					cell = headerRow.createCell(1);
					cell.setCellValue("false");

				} else {
					bcpgCode = null;
				}

				headerRow = sheet.createRow(rownum++);
				headerRow.setRowStyle(style);
				cell = headerRow.createCell(0);
				cell.setCellValue("STOP_ON_FIRST_ERROR");
				cell = headerRow.createCell(1);
				cell.setCellValue("false");

				headerRow = sheet.createRow(rownum++);
				headerRow.setRowStyle(style);

				sheet.groupRow(0, rownum);
				sheet.setRowGroupCollapsed(0, true);
				if (bcpgCode != null) {
					sheet.groupColumn(0, 1);
				} else {
					sheet.groupColumn(0, 0);
				}
				sheet.setColumnGroupCollapsed(0, true);

				Row labelRow = sheet.createRow(rownum++);
				int cellnum = 0;
				cell = headerRow.createCell(cellnum);
				cell.setCellValue("COLUMNS");
				cell = labelRow.createCell(cellnum++);
				cell.setCellValue("#");
				cell.setCellStyle(style);

				XSSFCellStyle headerStyle = workbook.createCellStyle();
				
				byte[] rgb2 = {(byte)  0, (byte) 157, (byte) 204};
				
				
				headerStyle.setFillForegroundColor(new XSSFColor(rgb2, new DefaultIndexedColorMap()));
				headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				XSSFFont font = workbook.createFont();
				font.setColor(HSSFColorPredefined.WHITE.getIndex());
				headerStyle.setFont(font);

				if (bcpgCode != null) {
					cell = headerRow.createCell(cellnum);
					cell.setCellValue("bcpg:code");
					cell = labelRow.createCell(cellnum++);
					cell.setCellValue(I18NUtil.getMessage("message.becpg.export.entity"));
					cell.setCellStyle(headerStyle);
				}

				ExcelDataListOutputPlugin plugin = getPlugin(dataListFilter);
				ExcelFieldTitleProvider titleProvider = plugin.getExcelFieldTitleProvider(dataListFilter);

				Row row = null;

				if ((extractedItems.getComputedFields() != null) && (extractedExtrasItems == null)) {
					List<AttributeExtractorStructure> fields = extractedItems.getComputedFields().stream()
							.filter(field -> titleProvider.isAllowed(field)).collect(Collectors.toList());

					ExcelHelper.appendExcelHeader(fields, null, null, headerRow, labelRow, headerStyle, sheet, cellnum, titleProvider,
							MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null);

					for (Map<String, Object> item : plugin.decorate(extractedItems.getPageItems())) {
						cellnum = 0;
						row = sheet.createRow(rownum++);

						cell = row.createCell(cellnum++);
						cell.setCellValue("VALUES");
						cell.setCellStyle(style);

						if (bcpgCode != null) {
							cell = row.createCell(cellnum++);
							cell.setCellValue(bcpgCode);
						}

						ExcelHelper.appendExcelField(fields, null, item, sheet, row, cellnum, rownum,
								MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null);

					}

				}

				if (row != null) {
					for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
						sheet.autoSizeColumn(colNum);
					}
				}

				// Extract extras sheets
				if (extractedExtrasItems != null) {
					ExcelHelper.appendExcelHeader(extractedExtrasItems.getComputedFields(), null, null, headerRow, labelRow, headerStyle, sheet,
							cellnum, titleProvider, MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null);

					for (Map<String, Object> item : extractedExtrasItems.getPageItems()) {
						cellnum = 0;
						row = sheet.createRow(rownum++);

						cell = row.createCell(cellnum++);
						cell.setCellValue("VALUES");
						cell.setCellStyle(style);

						if (bcpgCode != null) {
							cell = row.createCell(cellnum++);
							cell.setCellValue(bcpgCode);
						}

						ExcelHelper.appendExcelField(extractedExtrasItems.getComputedFields(), null, item, sheet, row, cellnum, rownum,
								MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null);

					}
				}

				extractedExtrasItems = plugin.extractExtrasSheet(dataListFilter);

				if ((extractedExtrasItems != null) && !extractedExtrasItems.getPageItems().isEmpty()) {
					hasExtrasSheet = true;
				} else {
					hasExtrasSheet = false;
				}

			} while (hasExtrasSheet);

			workbook.write(res.getOutputStream());
		}
	}

	void writeHeaders() {

	}

	void writeSheet(XSSFSheet sheet) {

	}

	private String cleanPath(String path) {
		return path.replace("/app:company_home", "").replaceAll("cm:", "");
	}

	private String getFileName(DataListFilter dataListFilter) {
		if (dataListFilter.getEntityNodeRef() != null) {
			return (String) nodeService.getProperty(dataListFilter.getEntityNodeRef(), ContentModel.PROP_NAME) + "_"
					+ dataListFilter.getDataListName() + ".xlsx";
		}
		return "export.xlsx";

	}

	@Nonnull
	private ExcelDataListOutputPlugin getPlugin(DataListFilter dataListFilter) {
		ExcelDataListOutputPlugin ret = null;

		for (ExcelDataListOutputPlugin plugin : plugins) {
			if (plugin.applyTo(dataListFilter)) {
				ret = plugin;
			} else if (plugin.isDefault() && (ret == null)) {
				ret = plugin;
			}
		}
		
		if(ret == null) {
		
			throw new IllegalStateException("No default plugin");
		}
		
		return ret;

	}
}
