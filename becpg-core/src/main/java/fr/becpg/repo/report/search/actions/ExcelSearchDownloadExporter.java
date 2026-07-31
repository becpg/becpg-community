
package fr.becpg.repo.report.search.actions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer;
import fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer.ExcelSheetExportContext;

/**
 * Handler for exporting node content to Excel file
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ExcelSearchDownloadExporter extends AbstractSearchDownloadExporter {

	/** Constant <code>logger</code> */
	private static Log logger = LogFactory.getLog(ExcelSearchDownloadExporter.class);

	/** Number of rows kept in memory before being flushed to disk */
	private static final int ROW_ACCESS_WINDOW_SIZE = 100;

	private ExcelReportSearchRenderer excelReportSearchRenderer;

	private ContentService contentService;

	private SXSSFWorkbook workbook;

	private List<Sheet> sheets = new ArrayList<>();

	Map<String, ExcelSheetExportContext> context = new HashMap<>();

	private int nodesSinceLastCacheClear = 0;
	private final int cacheClearEvery;
	private String[] parameters;

	/**
	 * <p>Constructor for ExcelSearchDownloadExporter.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 * @param excelReportSearchRenderer a {@link fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer} object
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nbOfLines a {@link java.lang.Long} object
	 */
	public ExcelSearchDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage, ContentService contentService, ExcelReportSearchRenderer excelReportSearchRenderer,
			NodeRef downloadNodeRef, NodeRef templateNodeRef, Long nbOfLines) {
		this(transactionHelper, updateService, downloadStorage, contentService, excelReportSearchRenderer, downloadNodeRef, templateNodeRef, nbOfLines, null);
	}

	/**
	 * <p>Constructor for ExcelSearchDownloadExporter.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 * @param excelReportSearchRenderer a {@link fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer} object
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nbOfLines a {@link java.lang.Long} object
	 * @param parameters an array of {@link java.lang.String} objects.
	 * @since 25.3.0.34
	 */
	public ExcelSearchDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage, ContentService contentService, ExcelReportSearchRenderer excelReportSearchRenderer,
			NodeRef downloadNodeRef, NodeRef templateNodeRef, Long nbOfLines, String[] parameters) {
		super(transactionHelper, updateService, downloadStorage, downloadNodeRef, templateNodeRef, nbOfLines);
		this.contentService = contentService;
		this.excelReportSearchRenderer = excelReportSearchRenderer;
		this.cacheClearEvery = 1000;
		this.parameters = parameters;
	}

	/** {@inheritDoc} */
	@Override
	public void startExport() {
		ContentReader reader = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT);

		try {
			XSSFWorkbook template = new XSSFWorkbook(reader.getContentInputStream());

			readTemplateHeaders(template);

			workbook = new SXSSFWorkbook(template, ROW_ACCESS_WINDOW_SIZE);
			workbook.setCompressTempFiles(true);

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				SXSSFSheet sheet = workbook.getSheetAt(i);

				// Columns have to be tracked to stay auto-sizeable once the rows are flushed to disk
				sheet.trackAllColumnsForAutoSizing();

				sheets.add(i, sheet);
			}

		} catch (ContentIOException | IOException e) {
			logger.error("Error generating excel report", e);
		}
	}

	/**
	 * Read the header of each sheet on the template itself: once the workbook is wrapped for
	 * streaming, the rows already written are no longer randomly accessible.
	 *
	 * @param template a {@link org.apache.poi.xssf.usermodel.XSSFWorkbook} object
	 */
	private void readTemplateHeaders(XSSFWorkbook template) {
		for (int i = 0; i < template.getNumberOfSheets(); i++) {
			XSSFSheet templateSheet = template.getSheetAt(i);

			ExcelSheetExportContext sheetContext = transactionHelper
					.doInTransaction(() -> excelReportSearchRenderer.readHeader(templateSheet, null, parameters), true, true);

			context.put(templateSheet.getSheetName(), sheetContext);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void startNode(NodeRef entityNodeRef) {

	    incFilesAddedCount();

	    for (Sheet sheet : sheets) {
	        ExcelSheetExportContext excelSheetExportContext = context.get(sheet.getSheetName());

	        if (excelSheetExportContext == null) {
	            continue;
	        }

	        transactionHelper.doInTransaction(() -> {

	            excelReportSearchRenderer.fillSheet(sheet, List.of(entityNodeRef), excelSheetExportContext);

	            sheet.setForceFormulaRecalculation(true);
	            return null;

	        }, true, true);

	        updateStatus();
	    }

	    // Periodically clear per-sheet caches to limit memory growth
	    nodesSinceLastCacheClear++;
	    if (nodesSinceLastCacheClear >= cacheClearEvery) {
	      for (ExcelSheetExportContext ctx : context.values()) {
	        if (ctx != null) {
	          ctx.clearCache();
	        }
	      }
	      nodesSinceLastCacheClear = 0;
	      if (logger.isDebugEnabled()) {
	        logger.debug("Cleared Excel sheet caches after processing batch of " + cacheClearEvery + " nodes");
	      }
	    }
	}



	/** {@inheritDoc} */
	@Override
	public void endExport() {
		if ((tempFile == null) || (workbook == null)) {
			return;
		}

		try (OutputStream outputStream = new FileOutputStream(tempFile)) {
			workbook.setForceFormulaRecalculation(true);
			workbook.write(outputStream);
		} catch (FileNotFoundException e) {
			logger.error("Failed to create excel file", e);
		} catch (ContentIOException | IOException e) {
			logger.error("Error generating excel report", e);
		} finally {
			closeWorkbook();
		}
	}

	private void closeWorkbook() {
		try {
			workbook.close();
		} catch (IOException e) {
			logger.error("Cannot close excel workbook", e);
		} finally {
			// Streaming keeps the flushed rows in temporary files that POI only removes on demand
			workbook.dispose();
		}
	}

}
