
package fr.becpg.repo.report.search.actions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.mapping.MappingException;
import fr.becpg.repo.report.search.impl.ReportServerSearchContext;
import fr.becpg.repo.report.search.impl.ReportServerSearchRenderer;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;

/**
 * Handler for exporting node content to Excel file
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportSearchDownloadExporter extends AbstractSearchDownloadExporter {

	private static Log logger = LogFactory.getLog(ReportSearchDownloadExporter.class);
	
	private ReportServerSearchRenderer reportServerSearchRenderer;

	
	ReportServerSearchContext exportSearchCtx;
	
	ReportFormat format;
	
	
	/**
	 * <p>Constructor for ReportSearchDownloadExporter.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object
	 * @param reportServerSearchRenderer a {@link fr.becpg.repo.report.search.impl.ReportServerSearchRenderer} object
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nbOfLines a {@link java.lang.Long} object
	 * @param format a {@link fr.becpg.report.client.ReportFormat} object
	 */
	public ReportSearchDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage, ReportServerSearchRenderer reportServerSearchRenderer,
			NodeRef downloadNodeRef, NodeRef templateNodeRef, Long nbOfLines, ReportFormat format) {

		
		super(transactionHelper, updateService, downloadStorage, downloadNodeRef, templateNodeRef, nbOfLines);

		this.reportServerSearchRenderer = reportServerSearchRenderer;
		
		this.format = format;
	
		
		
	}

	/** {@inheritDoc} */
	@Override
	public void start(final ExporterContext context) {
	
		 try {
			exportSearchCtx = reportServerSearchRenderer.createContext(templateNodeRef);
		} catch (MappingException e) {
			logger.error("Failed to read report mapping", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void startNode(NodeRef entityNodeRef) {
			reportServerSearchRenderer.exportNode(exportSearchCtx, entityNodeRef, incFilesAddedCount());

			updateStatus();
	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		if (tempFile != null) {
			try (OutputStream outputStream = new FileOutputStream(tempFile)) {
				reportServerSearchRenderer.createReport(templateNodeRef, exportSearchCtx, outputStream, format);
			} catch (FileNotFoundException e) {
				logger.error("Failed to create report file", e);
			} catch (ReportException | IOException e) {
				logger.error("Error generating report", e);
			}
		}

	}

}
