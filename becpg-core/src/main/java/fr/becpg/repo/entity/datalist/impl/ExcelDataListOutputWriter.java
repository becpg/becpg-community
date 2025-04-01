package fr.becpg.repo.entity.datalist.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.ContentServiceHelper;
import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadServiceException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.download.AbstractDownloadExporter;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.AsyncPaginatedExtractorWrapper;
import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;
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

	private static final String CREATION_ERROR = "Unexpected error creating file for download";

	@Autowired
	private ExcelDataListOutputPlugin[] plugins;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	@Qualifier("defaultAsyncThreadPool")
	private ThreadPoolExecutor threadExecuter;

	@Autowired
	private ContentServiceHelper contentServiceHelper;
	@Autowired
	private DownloadStorage downloadStorage;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private DownloadStatusUpdateService updateService;
	@Autowired
	private MimetypeService mimetypeService;
	@Autowired
	private PersonService personService;

	@Autowired
	private EntityActivityService entityActivityService;

	private static Log logger = LogFactory.getLog(ExcelDataListOutputWriter.class);

	/** {@inheritDoc} */
	@Override
	public void write(WebScriptRequest req, WebScriptResponse res, DataListFilter dataListFilter, PaginatedExtractedItems extractedItems)
			throws IOException {

		if (extractedItems instanceof AsyncPaginatedExtractorWrapper ) {

			NodeRef downloadNodeRef = transactionService.getRetryingTransactionHelper()
					.doInTransaction(() -> downloadStorage.createDownloadNode(false), false, true);

			Runnable command = new AsyncExcelDataListOutputWriter((AsyncPaginatedExtractorWrapper) extractedItems, downloadNodeRef);
			if (!threadExecuter.getQueue().contains(command)) {
				threadExecuter.execute(command);
			} else {
				logger.warn("AsyncExcelDataListOutputWriter job already in queue for " + downloadNodeRef);
			}

			try {
				JSONObject ret = new JSONObject();
				ret.put("nodeRef", downloadNodeRef);
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				res.getWriter().write(ret.toString(3));
			} catch (JSONException e) {
				logger.error(e, e);
			}

		} else {

			res.setContentType("application/vnd.ms-excel");

			AttachmentHelper.setAttachment(req, res, getFileName(dataListFilter));

			createExcelFile(extractedItems, dataListFilter, null, res.getOutputStream());

		}
	}

	private class AsyncExcelDataListOutputWriter implements Runnable {

		private AsyncPaginatedExtractorWrapper asynExtractor;
		private NodeRef downloadNodeRef;
		private final String userName;

		public AsyncExcelDataListOutputWriter(AsyncPaginatedExtractorWrapper asynExtractor, NodeRef downloadNodeRef) {
			super();
			this.asynExtractor = asynExtractor;
			this.downloadNodeRef = downloadNodeRef;
			this.userName = AuthenticationUtil.getFullyAuthenticatedUser();
		}

		@Override
		public void run() {

			AuthenticationUtil.runAs(() -> {

				final File tempFile = TempFileProvider.createTempFile("export", "xlsx");

				ExcelDataListDownloadExporter handler = new ExcelDataListDownloadExporter(transactionService.getRetryingTransactionHelper(),
						updateService, downloadStorage, downloadNodeRef, Long.valueOf(asynExtractor.getFullListSize()));

				Locale currentLocal = I18NUtil.getLocale();
				Locale currentContentLocal = I18NUtil.getContentLocale();

				String userId = userName;

				if ((userId != null) && !userId.isEmpty() && !AuthenticationUtil.getGuestUserName().equals(userId)
						&& personService.personExists(userId)) {
					NodeRef personNodeRef = personService.getPerson(userId);
					if ((personNodeRef != null) && nodeService.exists(personNodeRef)) {

						if (logger.isDebugEnabled()) {
							logger.debug("Set content locale:" + MLTextHelper.getUserContentLocale(nodeService, personNodeRef));
						}

						I18NUtil.setLocale(MLTextHelper.getUserLocale(nodeService, personNodeRef));
						I18NUtil.setContentLocale(MLTextHelper.getUserContentLocale(nodeService, personNodeRef));
					}
				}

				try (OutputStream out = new FileOutputStream(tempFile)) {

					transactionService.getRetryingTransactionHelper()
							.doInTransaction(() -> createExcelFile(asynExtractor, asynExtractor.getDataListFilter(), handler, out), false, true);

					fileCreationComplete(downloadNodeRef, "xlsx", tempFile, handler);

				} catch (DownloadCancelledException ex) {
					downloadCancelled(downloadNodeRef, handler);
				} finally {

					I18NUtil.setLocale(currentLocal);
					I18NUtil.setContentLocale(currentContentLocal);
					Files.delete(tempFile.toPath());
				}

				return true;

			}, userName);

		}

		private void fileCreationComplete(final NodeRef actionedUponNodeRef, String format, final File tempFile,
				final AbstractDownloadExporter handler) {
			// Update the content and set the status to done.
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				try {

					contentServiceHelper.updateContent(actionedUponNodeRef, tempFile);
					DownloadStatus status = new DownloadStatus(Status.DONE, handler.getFilesAddedCount(), handler.getFileCount(),
							handler.getFilesAddedCount(), handler.getFileCount());
					updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());
					ContentData contentData = (ContentData) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT);

					nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT,
							ContentData.setMimetype(contentData, mimetypeService.getMimetype(format)));
					return null;
				} catch (ContentIOException | IOException ex1) {
					throw new DownloadServiceException(CREATION_ERROR, ex1);
				}
			}, false, true);

		}

		private void downloadCancelled(final NodeRef actionedUponNodeRef, final AbstractDownloadExporter handler) {
			// Update the content and set the status to done.
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				DownloadStatus status = new DownloadStatus(Status.CANCELLED, handler.getFilesAddedCount(), handler.getFileCount(),
						handler.getFilesAddedCount(), handler.getFileCount());
				updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());

				return null;
			}, false, true);

		}

	}

	private String cleanPath(String path) {
		return path.replace("/app:company_home", "").replace("cm:", "");
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
			if (plugin.applyTo(dataListFilter) || (plugin.isDefault() && (ret == null))) {
				ret = plugin;
			}
		}

		if (ret == null) {

			throw new IllegalStateException("No default plugin");
		}

		return ret;

	}

	public boolean createExcelFile(PaginatedExtractedItems extractedItems, DataListFilter dataListFilter,
			@Nullable ExcelDataListDownloadExporter handler, OutputStream outputStream) throws IOException {

		try (XSSFWorkbook workbook = new XSSFWorkbook()) {

			ExcelDataListOutputPlugin plugin = getPlugin(dataListFilter);
			ExcelFieldTitleProvider titleProvider = plugin.getExcelFieldTitleProvider(dataListFilter);

			PaginatedExtractedItems extractedExtrasItems = null;
			boolean hasExtrasSheet;

			do {
				hasExtrasSheet = false;
				String sheetName = "";

				QName type = dataListFilter.getDataType();

				if (type != null) {
					TypeDefinition typeDef = entityDictionaryService.getType(type);

					if (typeDef != null) {
						sheetName = typeDef.getTitle(entityDictionaryService);
					} else {
						AspectDefinition aspectDef = entityDictionaryService.getAspect(type);
						if (aspectDef != null) {
							sheetName = aspectDef.getTitle(entityDictionaryService);
						}
					}
				}

				if ((sheetName == null) || sheetName.isEmpty()) {
					sheetName = "Values";
				}
				XSSFSheet sheet = workbook.createSheet(sheetName);

				ExcelCellStyles exeCellStyles = new ExcelCellStyles(workbook);

				int rownum = 0;

				Row headerRow = sheet.createRow(rownum++);
				headerRow.setRowStyle(exeCellStyles.getHeaderStyle());

				Cell cell = headerRow.createCell(0);
				cell.setCellStyle(exeCellStyles.getHeaderStyle());
				cell.setCellValue("MAPPING");
				cell = headerRow.createCell(1);
				cell.setCellStyle(exeCellStyles.getHeaderStyle());
				cell.setCellValue("Default");

				if (type != null) {
					headerRow = sheet.createRow(rownum++);
					headerRow.setRowStyle(exeCellStyles.getHeaderStyle());

					cell = headerRow.createCell(0);
					cell.setCellValue("TYPE");
					cell.setCellStyle(exeCellStyles.getHeaderStyle());
					cell = headerRow.createCell(1);
					cell.setCellStyle(exeCellStyles.getHeaderStyle());
					cell.setCellValue(type.toPrefixString());
				}

				String nodePath = null;
				String bcpgCode = null;
				QName entityType = null;

				if (dataListFilter.getEntityNodeRef() != null) {
					entityType = nodeService.getType(dataListFilter.getEntityNodeRef());
					if (entityDictionaryService.isSubClass(entityType, BeCPGModel.TYPE_SYSTEM_ENTITY)) {
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

				if (nodePath != null) {
					headerRow = sheet.createRow(rownum++);
					headerRow.setRowStyle(exeCellStyles.getHeaderStyle());
					cell = headerRow.createCell(0);
					cell.setCellStyle(exeCellStyles.getHeaderStyle());
					cell.setCellValue("PATH");
					cell = headerRow.createCell(1);
					cell.setCellValue(nodePath);
					cell.setCellStyle(exeCellStyles.getHeaderStyle());
				}

				if (entityDictionaryService.isSubClass(dataListFilter.getDataType(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
					if ((nodePath != null) && !nodePath.startsWith("/System/")) {
						headerRow = sheet.createRow(rownum++);
						headerRow.setRowStyle(exeCellStyles.getHeaderStyle());
						cell = headerRow.createCell(0);
						cell.setCellStyle(exeCellStyles.getHeaderStyle());
						cell.setCellValue("IMPORT_TYPE");
						cell = headerRow.createCell(1);
						cell.setCellStyle(exeCellStyles.getHeaderStyle());
						cell.setCellValue("EntityListItem");
					}

					if (entityType != null) {
						headerRow = sheet.createRow(rownum++);
						headerRow.setRowStyle(exeCellStyles.getHeaderStyle());
						cell = headerRow.createCell(0);
						cell.setCellStyle(exeCellStyles.getHeaderStyle());
						cell.setCellValue("ENTITY_TYPE");
						cell = headerRow.createCell(1);
						cell.setCellStyle(exeCellStyles.getHeaderStyle());
						cell.setCellValue(entityType.toPrefixString(namespaceService));
					}

					headerRow = sheet.createRow(rownum++);
					headerRow.setRowStyle(exeCellStyles.getHeaderStyle());

					cell = headerRow.createCell(0);
					cell.setCellStyle(exeCellStyles.getHeaderStyle());
					cell.setCellValue("DELETE_DATALIST");
					cell = headerRow.createCell(1);
					cell.setCellStyle(exeCellStyles.getHeaderStyle());
					cell.setCellValue("false");

				} else {
					bcpgCode = null;
				}

				headerRow = sheet.createRow(rownum++);
				headerRow.setRowStyle(exeCellStyles.getHeaderStyle());

				cell = headerRow.createCell(0);
				cell.setCellStyle(exeCellStyles.getHeaderStyle());
				cell.setCellValue("STOP_ON_FIRST_ERROR");
				cell = headerRow.createCell(1);
				cell.setCellStyle(exeCellStyles.getHeaderStyle());
				cell.setCellValue("false");

				headerRow = sheet.createRow(rownum++);
				headerRow.setRowStyle(exeCellStyles.getHeaderStyle());

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
				cell.setCellStyle(exeCellStyles.getHeaderStyle());
				cell = labelRow.createCell(cellnum++);
				cell.setCellValue("#");
				cell.setCellStyle(exeCellStyles.getHeaderStyle());

				if (bcpgCode != null) {
					cell = headerRow.createCell(cellnum);
					cell.setCellValue("bcpg:code");
					cell.setCellStyle(exeCellStyles.getHeaderStyle());

					cell = labelRow.createCell(cellnum++);
					cell.setCellValue(I18NUtil.getMessage("message.becpg.export.entity"));
					cell.setCellStyle(exeCellStyles.getHeaderTextStyle());
				}

				Row row = null;

				if ((extractedExtrasItems == null) && (extractedItems.getComputedFields() != null)) {
					List<AttributeExtractorStructure> fields = extractedItems.getComputedFields().stream().filter(titleProvider::isAllowed).collect(Collectors.toList());

					ExcelHelper.appendExcelHeader(fields, null, null, headerRow, labelRow, exeCellStyles, sheet, cellnum, titleProvider,
							MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null);

					List<Map<String, Object>> items = null;
					if (extractedItems instanceof AsyncPaginatedExtractorWrapper ) {
						items = plugin.decorate(((AsyncPaginatedExtractorWrapper) extractedItems).getNextWork());
					} else {
						items = plugin.decorate(extractedItems.getPageItems());
					}

					while ((items != null) && !items.isEmpty()) {
						for (Map<String, Object> item : items) {
							if (handler != null) {
								handler.incFilesAddedCount();
							}
							cellnum = 0;
							row = sheet.createRow(rownum++);

							cell = row.createCell(cellnum++);
							cell.setCellValue("VALUES");
							cell.setCellStyle(exeCellStyles.getHeaderStyle());

							if (bcpgCode != null) {
								cell = row.createCell(cellnum++);
								cell.setCellValue(bcpgCode);
							}

							ExcelHelper.appendExcelField(fields, null, item, sheet, row, cellnum, rownum,
									MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null, exeCellStyles);

							if (handler != null) {
								handler.updateStatus();
							}
						}
						if (extractedItems instanceof AsyncPaginatedExtractorWrapper) {
							items = plugin.decorate(((AsyncPaginatedExtractorWrapper) extractedItems).getNextWork());
						} else {
							items = null;
						}

					}
				}

				if (row != null) {
					for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
						sheet.autoSizeColumn(colNum);
					}
				}

				// Extract extras sheets
				if (extractedExtrasItems != null) {
					ExcelHelper.appendExcelHeader(extractedExtrasItems.getComputedFields(), null, null, headerRow, labelRow, exeCellStyles, sheet,
							cellnum, titleProvider, MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null);

					for (Map<String, Object> item : extractedExtrasItems.getPageItems()) {
						cellnum = 0;
						row = sheet.createRow(rownum++);

						cell = row.createCell(cellnum++);
						cell.setCellValue("VALUES");
						cell.setCellStyle(exeCellStyles.getHeaderStyle());

						if (bcpgCode != null) {
							cell = row.createCell(cellnum++);
							cell.setCellValue(bcpgCode);
						}

						ExcelHelper.appendExcelField(extractedExtrasItems.getComputedFields(), null, item, sheet, row, cellnum, rownum,
								MLTextHelper.shouldExtractMLText() ? MLTextHelper.getSupportedLocales() : null, exeCellStyles);

					}
				}

				extractedExtrasItems = plugin.extractExtrasSheet(dataListFilter);

				if ((extractedExtrasItems != null) && !extractedExtrasItems.getPageItems().isEmpty()) {
					hasExtrasSheet = true;
				} else {
					hasExtrasSheet = false;
				}

			} while (hasExtrasSheet);

			workbook.write(outputStream);

			entityActivityService.postExportActivity(dataListFilter.getEntityNodeRef(), dataListFilter.getDataType(), getFileName(dataListFilter));
		}
		return true;
	}
}
