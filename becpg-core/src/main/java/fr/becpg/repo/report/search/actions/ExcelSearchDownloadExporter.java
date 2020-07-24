
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
public class ExcelSearchDownloadExporter implements Exporter {
	private static Log logger = LogFactory.getLog(ExcelSearchDownloadExporter.class);

	private ExcelReportSearchRenderer excelReportSearchRenderer;

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
	private ContentService contentService;

	Map<String, ExcelSheetExportContext> context = new HashMap<>();
	

	/**
	 * <p>Constructor for ExcelSearchDownloadExporter.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object.
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object.
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object.
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 * @param excelReportSearchRenderer a {@link fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer} object.
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nbOfLines a {@link java.lang.Long} object.
	 */
	public ExcelSearchDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage, ContentService contentService, ExcelReportSearchRenderer excelReportSearchRenderer,
			NodeRef downloadNodeRef, NodeRef templateNodeRef, Long nbOfLines) {

		this.updateService = updateService;
		this.transactionHelper = transactionHelper;
		this.downloadStorage = downloadStorage;

		this.excelReportSearchRenderer = excelReportSearchRenderer;

		this.downloadNodeRef = downloadNodeRef;
		this.contentService = contentService;

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

	/**
	 * <p>Getter for the field <code>filesAddedCount</code>.</p>
	 *
	 * @return a long.
	 */
	public long getFilesAddedCount() {
		return filesAddedCount;
	}

	/**
	 * <p>Getter for the field <code>fileCount</code>.</p>
	 *
	 * @return a long.
	 */
	public long getFileCount() {
		return fileCount;
	}

	/**
	 * <p>setTemplateFile.</p>
	 *
	 * @param excelFile a {@link java.io.File} object.
	 */
	public void setTemplateFile(File excelFile) {
		try {
			this.outputStream = new FileOutputStream(excelFile);
		} catch (FileNotFoundException e) {
			throw new ExporterException("Failed to create excel file", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void start(final ExporterContext context) {

	}

	/** {@inheritDoc} */
	@Override
	public void startNode(NodeRef entityNodeRef) {

		filesAddedCount++;
		QName mainType = null;

		for (XSSFSheet sheet : sheets) {

			ExcelSheetExportContext excelSheetExportContext = context.get(sheet.getSheetName());
			if (excelSheetExportContext == null) {
				excelSheetExportContext = excelReportSearchRenderer.readHeader(sheet, mainType);
				context.put(sheet.getSheetName(), excelSheetExportContext);
			}

			mainType = excelReportSearchRenderer.fillSheet(sheet, Arrays.asList(entityNodeRef), excelSheetExportContext);

			updateStatus();
			checkCancelled();
		}

	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		if (outputStream != null) {
			try {
				//Disable for XLSM (break #2259 ?) workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
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

	/**
	 * <p>getNextSequenceNumber.</p>
	 *
	 * @return a int.
	 */
	public int getNextSequenceNumber() {
		return sequenceNumber++;
	}

	/** {@inheritDoc} */
	@Override
	public void startNamespace(String prefix, String uri) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endNamespace(String prefix) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endNode(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startReference(NodeRef nodeRef, QName childName) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endReference(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startAspects(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startAspect(NodeRef nodeRef, QName aspect) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endAspect(NodeRef nodeRef, QName aspect) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endAspects(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startACL(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void permission(NodeRef nodeRef, AccessPermission permission) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endACL(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startProperties(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startProperty(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endProperty(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endProperties(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startValueCollection(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endValueMLText(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void value(NodeRef nodeRef, QName property, Object value, int index) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endValueCollection(NodeRef nodeRef, QName property) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startAssocs(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void startAssoc(NodeRef nodeRef, QName assoc) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endAssoc(NodeRef nodeRef, QName assoc) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void endAssocs(NodeRef nodeRef) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public void warning(String warning) {
		// TODO Auto-generated method stub

	}
}
