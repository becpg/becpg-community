
package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.report.search.impl.ExcelReportSearchPlugin;

/**
 * Handler for exporting node content to Excel file
 *
 */
public class ExcelSearchDownloadExporter implements Exporter {
	private static Log logger = LogFactory.getLog(ExcelSearchDownloadExporter.class);

	private ExcelReportSearchPlugin[] excelReportSearchPlugins;
	private AttributeExtractorService attributeExtractorService;
	private EntityDictionaryService entityDictionaryService;

	private XSSFWorkbook workbook;

	private List<XSSFSheet> sheets = new ArrayList<>();

	private OutputStream outputStream;
	private NodeRef downloadNodeRef;
	private int sequenceNumber = 1;
	private long filesAddedCount;
	private long fileCount;

	private RetryingTransactionHelper transactionHelper;
	private DownloadStorage downloadStorage;
	private DownloadStatusUpdateService updateService;
	private NamespaceService namespaceService;
	private ContentService contentService;

	public ExcelSearchDownloadExporter(NamespaceService namespaceService, RetryingTransactionHelper transactionHelper,
			DownloadStatusUpdateService updateService, DownloadStorage downloadStorage, ContentService contentService,
			ExcelReportSearchPlugin[] excelReportSearchPlugins, AttributeExtractorService attributeExtractorService,
			EntityDictionaryService entityDictionaryService, NodeRef downloadNodeRef, NodeRef templateNodeRef, Long nbOfLines) {

		this.updateService = updateService;
		this.transactionHelper = transactionHelper;
		this.downloadStorage = downloadStorage;

		this.excelReportSearchPlugins = excelReportSearchPlugins;
		this.attributeExtractorService = attributeExtractorService;
		this.entityDictionaryService = entityDictionaryService;

		this.downloadNodeRef = downloadNodeRef;
		this.contentService = contentService;
		this.namespaceService = namespaceService;

		this.fileCount = nbOfLines;

		try {
			readFileMapping(templateNodeRef);
		} catch (Exception e) {
			throw new ExporterException("Failed to read excel search mapping", e);
		}

	}

	private void readFileMapping(NodeRef templateNodeRef) throws Exception {
		ContentReader reader = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT);

		try {
			workbook = new XSSFWorkbook(reader.getContentInputStream());
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				sheets.add(i, workbook.getSheetAt(i));
			}

		} catch (ContentIOException | IOException e) {
			logger.error("Error generating excel report", e);
		}
	}

	public long getFilesAddedCount() {
		return filesAddedCount;
	}

	public long getFileCount() {
		return fileCount;
	}

	public void setTemplateFile(File excelFile) {
		try {
			this.outputStream = new FileOutputStream(excelFile);
		} catch (FileNotFoundException e) {
			throw new ExporterException("Failed to create excel file", e);
		}
	}

	@Override
	public void start(final ExporterContext context) {

	}

	@Override
	public void startNode(NodeRef entityNodeRef) {

		filesAddedCount++;
		QName mainType = null;
		for (XSSFSheet sheet : sheets) {
			mainType = fillSheet(sheet, entityNodeRef, mainType);

			updateStatus();
			checkCancelled();
		}

	}

	@Override
	public void end() {
		if (outputStream != null) {
			try {
				workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
				workbook.setForceFormulaRecalculation(true);
				workbook.write(outputStream);
			} catch (ContentIOException | IOException e) {
				logger.error("Error generating excel report", e);
			}
		}
	}

	private void checkCancelled() {
		boolean downloadCancelled = transactionHelper.doInTransaction(() -> downloadStorage.isCancelled(downloadNodeRef), true, true);

		if (downloadCancelled == true) {
			logger.debug("Download cancelled");
			throw new DownloadCancelledException();
		}
	}

	private void updateStatus() {
		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.IN_PROGRESS, filesAddedCount, fileCount, filesAddedCount, fileCount);

			updateService.update(downloadNodeRef, status, getNextSequenceNumber());
			return null;
		}, false, true);
	}

	public int getNextSequenceNumber() {
		return sequenceNumber++;
	}

	private QName fillSheet(XSSFSheet sheet, NodeRef entityNodeRef, QName mainType) {
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

			while ((sheet.getRow(rownum) != null) && (sheet.getRow(rownum).getCell(0) != null)) {
				rownum++;
			}

			Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

			ExcelReportSearchPlugin plugin = null;

			for (ExcelReportSearchPlugin tmp : excelReportSearchPlugins) {
				if ((tmp.isDefault() && (plugin == null)) || tmp.isApplicable(itemType, parameters.toArray(new String[parameters.size()]))) {
					plugin = tmp;
				}
			}

			if (plugin != null) {
				plugin.fillSheet(sheet, Arrays.asList(entityNodeRef), mainType, itemType, rownum, parameters.toArray(new String[parameters.size()]),
						keyColumn, metadataFields, cache);

			} else {
				logger.error("No plugin found for : " + itemType.toString());
			}

		}
		return mainType;

	}

	private List<AttributeExtractorStructure> extractListStruct(QName itemType, Row headerRow) {

		List<String> metadataFields = new LinkedList<>();
		String currentNested = "";
		for (int i = 1; i < headerRow.getLastCellNum(); i++) {
			if (headerRow.getCell(i) != null) {
				if (headerRow.getCell(i).getCellType() == Cell.CELL_TYPE_STRING) {
					String cellValue = headerRow.getCell(i).getStringCellValue();
					if ((cellValue != null) && !cellValue.isEmpty() && !cellValue.startsWith("#")) {
						if (cellValue.contains("_") && !cellValue.contains("formula") && !cellValue.startsWith("dyn_")) {
							if (!currentNested.isEmpty() && currentNested.startsWith(cellValue.split("_")[0])) {
								currentNested += "|" + cellValue.split("_")[1];
							} else {
								if (!currentNested.isEmpty()) {
									logger.debug("Add nested field : " + currentNested);
									metadataFields.add(currentNested);
								}
								currentNested = cellValue.replace("_", "|");
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
				} else if(headerRow.getCell(i).getCellType() == Cell.CELL_TYPE_FORMULA) {
					String cellFormula = headerRow.getCell(i).getCellFormula();
					metadataFields.add("excel|"+cellFormula);
				}
			}

		}
		return attributeExtractorService.readExtractStructure(itemType, metadataFields);
	}

	@Override
	public void startNamespace(String prefix, String uri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endNamespace(String prefix) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endNode(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startReference(NodeRef nodeRef, QName childName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endReference(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startAspects(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startAspect(NodeRef nodeRef, QName aspect) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endAspect(NodeRef nodeRef, QName aspect) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endAspects(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startACL(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void permission(NodeRef nodeRef, AccessPermission permission) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endACL(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startProperties(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startProperty(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endProperty(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endProperties(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startValueCollection(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endValueMLText(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void value(NodeRef nodeRef, QName property, Object value, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endValueCollection(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startAssocs(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startAssoc(NodeRef nodeRef, QName assoc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endAssoc(NodeRef nodeRef, QName assoc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endAssocs(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void warning(String warning) {
		// TODO Auto-generated method stub

	}
}
