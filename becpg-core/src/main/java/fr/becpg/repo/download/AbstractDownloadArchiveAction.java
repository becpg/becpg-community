/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.download;

import java.io.File;
import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.download.ContentServiceHelper;
import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadServiceException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.report.helpers.ExportSearchNodesHelper;

/**
 * Base class for the actions building a download archive from a set of nodes.
 *
 * It owns everything the download protocol requires - crawler setup, status publication, content
 * update and failure handling - so that each action only describes what it exports.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractDownloadArchiveAction extends ActionExecuterAbstractBase {

	/** Constant <code>CREATION_ERROR="Unexpected error creating file for download"</code> */
	protected static final String CREATION_ERROR = "Unexpected error creating file for download";

	private static final Log logger = LogFactory.getLog(AbstractDownloadArchiveAction.class);

	protected NodeService nodeService;
	protected ContentServiceHelper contentServiceHelper;
	protected DownloadStorage downloadStorage;
	protected ExporterService exporterService;
	protected RetryingTransactionHelper transactionHelper;
	protected DownloadStatusUpdateService updateService;
	protected MimetypeService mimetypeService;
	protected ContentService contentService;

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>mimetypeService</code>.</p>
	 *
	 * @param mimetypeService a {@link org.alfresco.service.cmr.repository.MimetypeService} object
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>contentServiceHelper</code>.</p>
	 *
	 * @param contentServiceHelper a {@link org.alfresco.repo.download.ContentServiceHelper} object
	 */
	public void setContentServiceHelper(ContentServiceHelper contentServiceHelper) {
		this.contentServiceHelper = contentServiceHelper;
	}

	/**
	 * <p>Setter for the field <code>downloadStorage</code>.</p>
	 *
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object
	 */
	public void setDownloadStorage(DownloadStorage downloadStorage) {
		this.downloadStorage = downloadStorage;
	}

	/**
	 * <p>Setter for the field <code>exporterService</code>.</p>
	 *
	 * @param exporterService a {@link org.alfresco.service.cmr.view.ExporterService} object
	 */
	public void setExporterService(ExporterService exporterService) {
		this.exporterService = exporterService;
	}

	/**
	 * <p>Setter for the field <code>transactionHelper</code>.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object
	 */
	public void setTransactionHelper(RetryingTransactionHelper transactionHelper) {
		this.transactionHelper = transactionHelper;
	}

	/**
	 * <p>Setter for the field <code>updateService</code>.</p>
	 *
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object
	 */
	public void setUpdateService(DownloadStatusUpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Build the crawler parameters used to walk the requested nodes without their children, their
	 * associations nor their content.
	 *
	 * @param nodeRefs an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects
	 * @return a {@link org.alfresco.service.cmr.view.ExporterCrawlerParameters} object
	 */
	protected ExporterCrawlerParameters createCrawlerParameters(NodeRef[] nodeRefs) {
		ExporterCrawlerParameters crawlerParameters = new ExporterCrawlerParameters();

		crawlerParameters.setExportFrom(new Location(nodeRefs));
		crawlerParameters.setCrawlSelf(true);
		crawlerParameters.setCrawlChildNodes(false);
		crawlerParameters.setCrawlAssociations(false);
		crawlerParameters.setCrawlContent(false);
		crawlerParameters.setExcludeAspects(new QName[] { ContentModel.ASPECT_WORKING_COPY });

		return crawlerParameters;
	}

	/**
	 * Publish a terminal status when there is nothing to export.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nodeRefs an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects
	 * @return true when the request was empty and has been completed
	 */
	protected boolean completeIfEmpty(NodeRef downloadNodeRef, NodeRef[] nodeRefs) {
		if ((nodeRefs != null) && (nodeRefs.length > 0)) {
			return false;
		}

		logger.warn("No node to export for download: " + downloadNodeRef);
		publishStatus(downloadNodeRef, new DownloadStatus(Status.DONE, 0, 0, 0, 0), 1);

		return true;
	}

	/**
	 * Run the export and publish the resulting status, whatever the outcome.
	 *
	 * A failure leaves the download node in a terminal state: without it the client polls a status
	 * that never changes and the user waits for a file that will never come.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param crawlerParameters a {@link org.alfresco.service.cmr.view.ExporterCrawlerParameters} object
	 * @param handler the exporter building the archive
	 * @param tempFile a {@link java.io.File} object
	 * @param <T> the exporter type, reporting its own progress
	 */
	protected <T extends Exporter & DownloadProgressReporter> void runExport(NodeRef downloadNodeRef,
			ExporterCrawlerParameters crawlerParameters, T handler, File tempFile) {
		runExport(downloadNodeRef, () -> exporterService.exportView(handler, crawlerParameters, null), handler, tempFile);
	}

	/**
	 * Run the given export task and publish the resulting status, whatever the outcome.
	 *
	 * A retryable failure is rethrown so that the transaction can be replayed: reporting a transient
	 * collision as a cancelled download would lose the archive for good.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param exportTask the task producing the archive
	 * @param reporter a {@link fr.becpg.repo.download.DownloadProgressReporter} object
	 * @param tempFile a {@link java.io.File} object
	 */
	protected void runExport(NodeRef downloadNodeRef, Runnable exportTask, DownloadProgressReporter reporter, File tempFile) {
		try {
			exportTask.run();
			completeDownload(downloadNodeRef, tempFile, reporter);
		} catch (DownloadCancelledException e) {
			publishStatus(downloadNodeRef, reporter.buildStatus(Status.CANCELLED), reporter.getNextSequenceNumber());
		} catch (Exception e) {
			if (RetryingTransactionHelper.extractRetryCause(e) != null) {
				throw e;
			}
			logger.error("Failed to create download archive for node: " + downloadNodeRef, e);
			publishStatus(downloadNodeRef, reporter.buildStatus(Status.CANCELLED), reporter.getNextSequenceNumber());
		} finally {
			reporter.releaseResources();
			deleteTempFile(tempFile);
		}
	}

	/**
	 * Attach the produced file to the download node and mark it as done.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param tempFile a {@link java.io.File} object
	 * @param reporter a {@link fr.becpg.repo.download.DownloadProgressReporter} object
	 */
	protected void completeDownload(NodeRef downloadNodeRef, File tempFile, DownloadProgressReporter reporter) {
		transactionHelper.doInTransaction(() -> {
			try {
				contentServiceHelper.updateContent(downloadNodeRef, tempFile);
				updateService.update(downloadNodeRef, reporter.buildStatus(Status.DONE), reporter.getNextSequenceNumber());
				applyMimetype(downloadNodeRef, reporter.getExtension());

				return null;
			} catch (ContentIOException | IOException e) {
				throw new DownloadServiceException(CREATION_ERROR, e);
			}
		}, false, true);
	}

	private void applyMimetype(NodeRef downloadNodeRef, String extension) {
		if ((extension == null) || (mimetypeService == null)) {
			return;
		}

		ContentData contentData = (ContentData) nodeService.getProperty(downloadNodeRef, ContentModel.PROP_CONTENT);

		nodeService.setProperty(downloadNodeRef, ContentModel.PROP_CONTENT,
				ContentData.setMimetype(contentData, mimetypeService.getMimetype(extension)));
	}

	/**
	 * Publish a status on the download node, in its own transaction.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param status a {@link org.alfresco.service.cmr.download.DownloadStatus} object
	 * @param sequenceNumber the sequence number of the status update
	 */
	protected void publishStatus(NodeRef downloadNodeRef, DownloadStatus status, int sequenceNumber) {
		transactionHelper.doInTransaction(() -> {
			updateService.update(downloadNodeRef, status, sequenceNumber);
			return null;
		}, false, true);
	}

	/**
	 * Publish the status telling the client that the requested content is too large to be downloaded.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param maximumContentSize the configured maximum size
	 * @param size the size of the requested content
	 * @param fileCount the number of requested files
	 */
	protected void maximumContentSizeExceeded(NodeRef downloadNodeRef, long maximumContentSize, long size, long fileCount) {
		if (logger.isDebugEnabled()) {
			logger.debug("Maximum content size (" + maximumContentSize + ") exceeded (" + size + ")");
		}

		publishStatus(downloadNodeRef, new DownloadStatus(Status.MAX_CONTENT_SIZE_EXCEEDED, maximumContentSize, size, 0, fileCount), 1);
	}

	/**
	 * The nodes to export: those stored on the download node itself, falling back on the associations
	 * of the download request when it carries none.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param downloadRequest a {@link org.alfresco.service.cmr.download.DownloadRequest} object
	 * @return an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects
	 */
	protected NodeRef[] getNodeRefsToExport(NodeRef downloadNodeRef, DownloadRequest downloadRequest) {
		NodeRef[] storedNodeRefs = ExportSearchNodesHelper.readNodes(contentService, nodeService, downloadNodeRef);

		return storedNodeRefs.length > 0 ? storedNodeRefs : downloadRequest.getRequetedNodeRefs();
	}

	private void deleteTempFile(File tempFile) {
		if ((tempFile != null) && !tempFile.delete()) {
			logger.error("Cannot delete temp file: " + tempFile.getAbsolutePath());
		}
	}

}
