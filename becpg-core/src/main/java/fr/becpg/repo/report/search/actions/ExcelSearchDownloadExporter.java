
package fr.becpg.repo.report.search.actions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static Log logger = LogFactory.getLog(ExcelSearchDownloadExporter.class);

	private ExcelReportSearchRenderer excelReportSearchRenderer;

	private ContentService contentService;

	private XSSFWorkbook workbook;

	private List<XSSFSheet> sheets = new ArrayList<>();

	Map<String, ExcelSheetExportContext> context = new HashMap<>();

	private int nodesSinceLastCacheClear = 0;
	private final int cacheClearEvery;

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
		super(transactionHelper, updateService, downloadStorage, downloadNodeRef, templateNodeRef, nbOfLines);
		this.contentService = contentService;
		this.excelReportSearchRenderer = excelReportSearchRenderer;
		this.cacheClearEvery = 1000;
	}

	/** {@inheritDoc} */
	@Override
	public void start(final ExporterContext context) {
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

	/** {@inheritDoc} */
	@Override
	public void startNode(NodeRef entityNodeRef) {

	    incFilesAddedCount();
	    AtomicReference<QName> mainTypeRef = new AtomicReference<>();

	    for (XSSFSheet sheet : sheets) {
	        QName type = transactionHelper.doInTransaction(() -> {

	            ExcelSheetExportContext excelSheetExportContext = context.get(sheet.getSheetName());
	            if (excelSheetExportContext == null) {
	                excelSheetExportContext = excelReportSearchRenderer.readHeader(sheet, mainTypeRef.get());
	                context.put(sheet.getSheetName(), excelSheetExportContext);
	            }

	            QName t = excelReportSearchRenderer.fillSheet(
	                sheet,
	                List.of(entityNodeRef),
	                excelSheetExportContext
	            );

	            sheet.setForceFormulaRecalculation(true);
	            return t;

	        }, true, true);

	        if (mainTypeRef.get() == null && type != null) {
	            mainTypeRef.set(type);
	        }

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
	public void end() {
		if (tempFile != null) {
			try (OutputStream outputStream = new FileOutputStream(tempFile)) {
				workbook.setForceFormulaRecalculation(true);
				workbook.write(outputStream);
				workbook.close();
			} catch (FileNotFoundException e) {
				logger.error("Failed to create excel file", e);
			} catch (ContentIOException | IOException e) {
				logger.error("Error generating excel report", e);
			}
		}
	}

}
