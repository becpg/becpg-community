package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

@Service
public class ExcelDataListOutputWriter implements DataListOutputWriter {

	@Autowired
	private ExcelDataListOutputPlugin[] plugins;
	
	@Autowired
	private NodeService nodeService;

	@Override
	public void write(WebScriptRequest req, WebScriptResponse res, DataListFilter dataListFilter, PaginatedExtractedItems extractedItems) throws IOException {

		res.setContentType("application/vnd.ms-excel");
		
		AttachmentHelper.setAttachment(req, res, getFileName(dataListFilter));
		

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
		XSSFCellStyle style = workbook.createCellStyle();

		XSSFColor green = new XSSFColor(new java.awt.Color(0, 102, 0));

		style.setFillForegroundColor(green);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);

		int cellnum = 0;
		cell = headerRow.createCell(cellnum);
		headerRow.setZeroHeight(true);
		cell.setCellValue("COLUMNS");
		cell = labelRow.createCell(cellnum++);
		cell.setCellValue("#");

		ExcelDataListOutputPlugin plugin = getPlugin(dataListFilter);
		ExcelFieldTitleProvider titleProvider = plugin.getExcelFieldTitleProvider(dataListFilter);

		List<AttributeExtractorStructure> fields = extractedItems.getComputedFields().stream()
				.filter(field -> titleProvider.isAllowed(field)).collect(Collectors.toList());

		ExcelHelper.appendExcelHeader(fields, null, null, headerRow, labelRow, style, cellnum, titleProvider);

		for (Map<String, Object> item : plugin.decorate(extractedItems.getPageItems())) {
			Row row = sheet.createRow(rownum++);

			cell = row.createCell(0);
			cell.setCellValue("VALUES");

			ExcelHelper.appendExcelField(fields, null, item, row, 1);

		}

		workbook.write(res.getOutputStream());

	}

	private String getFileName(DataListFilter dataListFilter) {
		if(dataListFilter.getEntityNodeRef()!=null){
			return(String) nodeService.getProperty(dataListFilter.getEntityNodeRef(),
					ContentModel.PROP_NAME)+"_"+dataListFilter.getDataListName()+".xlsx";
		}
		return "export.xlsx";
		
	}

	private ExcelDataListOutputPlugin getPlugin(DataListFilter dataListFilter) {
		ExcelDataListOutputPlugin ret = null;

		for (ExcelDataListOutputPlugin plugin : plugins) {
			if (plugin.applyTo(dataListFilter)) {
				ret = plugin;
			} else if (plugin.isDefault() && (ret == null)) {
				ret = plugin;
			}
		}
		return ret;

	}
}
