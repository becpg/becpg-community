package fr.becpg.repo.download;

import java.io.File;

import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Abstract AbstractDownloadExporter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractDownloadExporter {
	private static Log logger = LogFactory.getLog(AbstractDownloadExporter.class);

	private NodeRef downloadNodeRef;
	private int sequenceNumber = 1;
	private long filesAddedCount;
	private long fileCount;

	protected RetryingTransactionHelper transactionHelper;
	protected DownloadStorage downloadStorage;
	protected DownloadStatusUpdateService updateService;
	protected File tempFile;

	/**
	 * <p>Constructor for ExcelSearchDownloadExporter.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object.
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object.
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object.
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nbOfLines a {@link java.lang.Long} object.
	 */
	protected AbstractDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage, NodeRef downloadNodeRef, Long nbOfLines) {

		this.updateService = updateService;
		this.transactionHelper = transactionHelper;
		this.downloadStorage = downloadStorage;
		this.downloadNodeRef = downloadNodeRef;
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

	/**
	 * <p>updateStatus.</p>
	 */
	public void updateStatus() {

		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.IN_PROGRESS, filesAddedCount, fileCount, filesAddedCount, fileCount);

			updateService.update(downloadNodeRef, status, getNextSequenceNumber());
			return null;
		}, false, true);

		boolean downloadCancelled = transactionHelper.doInTransaction(() -> downloadStorage.isCancelled(downloadNodeRef), true, true);

		if (downloadCancelled) {
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

	/**
	 * <p>Setter for the field <code>tempFile</code>.</p>
	 *
	 * @param tempFile a {@link java.io.File} object
	 */
	public void setTempFile(File tempFile) {
		this.tempFile = tempFile;

	}

}
