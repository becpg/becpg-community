package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSearchDownloadExporter implements Exporter {
	private static Log logger = LogFactory.getLog(AbstractSearchDownloadExporter.class);

	private NodeRef downloadNodeRef;
	private int sequenceNumber = 1;
	private long filesAddedCount;
	private long fileCount;

	protected RetryingTransactionHelper transactionHelper;
	protected DownloadStorage downloadStorage;
	protected DownloadStatusUpdateService updateService;
	protected NodeRef templateNodeRef;
	protected File tempFile;
	

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
	public AbstractSearchDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage,
			NodeRef downloadNodeRef, NodeRef templateNodeRef, Long nbOfLines) {

		this.updateService = updateService;
		this.transactionHelper = transactionHelper;
		this.downloadStorage = downloadStorage;

		this.downloadNodeRef = downloadNodeRef;
		this.templateNodeRef = templateNodeRef;

		this.fileCount = nbOfLines;

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


	protected void updateStatus() {

		
		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.IN_PROGRESS, filesAddedCount, fileCount, filesAddedCount, fileCount);

			updateService.update(downloadNodeRef, status, getNextSequenceNumber());
			return null;
		}, false, true);
		
		boolean downloadCancelled = transactionHelper.doInTransaction(() -> downloadStorage.isCancelled(downloadNodeRef), true, true);

		if (downloadCancelled == true) {
			logger.debug("Download cancelled");
			throw new DownloadCancelledException();
		}
	}

	/**
	 * <p>getNextSequenceNumber.</p>
	 *
	 * @return a int.
	 */
	public int getNextSequenceNumber() {
		return sequenceNumber++;
	}
	
	/**
	 * <p>getNextFilesAddedCount.</p>
	 *
	 * @return a int.
	 */
	public long incFilesAddedCount() {
		return filesAddedCount++;
	}

	/** {@inheritDoc} */
	@Override
	public void startNamespace(String prefix, String uri) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endNamespace(String prefix) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endNode(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startReference(NodeRef nodeRef, QName childName) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endReference(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAspects(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAspect(NodeRef nodeRef, QName aspect) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAspect(NodeRef nodeRef, QName aspect) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAspects(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startACL(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void permission(NodeRef nodeRef, AccessPermission permission) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endACL(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startProperties(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startProperty(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endProperty(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endProperties(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startValueCollection(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endValueMLText(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void value(NodeRef nodeRef, QName property, Object value, int index) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endValueCollection(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAssocs(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAssoc(NodeRef nodeRef, QName assoc) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAssoc(NodeRef nodeRef, QName assoc) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAssocs(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void warning(String warning) {
		// Empty method

	}


	public void setTempFile(File tempFile) {
		this.tempFile =  tempFile;
		
	}
}
