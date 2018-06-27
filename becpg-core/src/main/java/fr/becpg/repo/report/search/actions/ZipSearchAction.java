
package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.download.ContentServiceHelper;
import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadServiceException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ActionExecuter} for creating an archive (ie. zip) file containing
 * content from the repository.
 * 
 * The maximum total size of the content which can be downloaded is controlled
 * by the maximumContentSie property. -1 indicates no limit.
 *
 * @author matthieu form Alex Miller code
 */
public class ZipSearchAction extends ActionExecuterAbstractBase {
	private static final Logger log = LoggerFactory.getLogger(ZipSearchAction.class);

	private static final String CREATION_ERROR = "Unexpected error creating archive file for download";
	private static final String TEMP_FILE_PREFIX = "download";
	private static final String TEMP_FILE_SUFFIX = ".zip";

	public static final String PARAM_TPL_NODEREF = "templateNodeRef";

	// Dependencies
	private CheckOutCheckInService checkOutCheckInService;
	private ContentServiceHelper contentServiceHelper;
	private DownloadStorage downloadStorage;
	private ExporterService exporterService;
	private NodeService nodeService;
	private RetryingTransactionHelper transactionHelper;
	private DownloadStatusUpdateService updateService;
	private ContentService contentService;
	private NamespaceService namespaceService;
	private long maximumContentSize = -1l;

	
	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	public void setContentServiceHelper(ContentServiceHelper contentServiceHelper) {
		this.contentServiceHelper = contentServiceHelper;
	}

	public void setDownloadStorage(DownloadStorage downloadStorage) {
		this.downloadStorage = downloadStorage;
	}

	public void setExporterService(ExporterService exporterService) {
		this.exporterService = exporterService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTransactionHelper(RetryingTransactionHelper transactionHelper) {
		this.transactionHelper = transactionHelper;
	}

	public void setUpdateService(DownloadStatusUpdateService updateService) {
		this.updateService = updateService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setMaximumContentSize(long maximumContentSize) {
		this.maximumContentSize = maximumContentSize;
	}

	
	
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * Create an archive file containing content from the repository.
	 * 
	 * Uses the {@link ExporterService} with custom exporters to create the
	 * archive files.
	 * 
	 * @param actionedUponNodeRef
	 *            Download node containing information required to create the
	 *            archive file, and which will eventually have its content
	 *            updated with the archive file.
	 */
	@Override
	protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {
		

		NodeRef templateNodeRef =  (NodeRef) action.getParameterValue(PARAM_TPL_NODEREF);
		
		ParameterCheck.mandatory("templateNodeRef", templateNodeRef);
		
		// Get the download request data and set up the exporter crawler
		// parameters.
		final DownloadRequest downloadRequest = downloadStorage.getDownloadRequest(actionedUponNodeRef);

		AuthenticationUtil.runAs(() -> {

			ExporterCrawlerParameters crawlerParameters = new ExporterCrawlerParameters();

			Location exportFrom = new Location(downloadRequest.getRequetedNodeRefs());
			crawlerParameters.setExportFrom(exportFrom);

			crawlerParameters.setCrawlSelf(true);
			crawlerParameters.setCrawlChildNodes(false);
			crawlerParameters.setCrawlAssociations(false);
			crawlerParameters.setCrawlContent(false);
			crawlerParameters.setExcludeAspects(new QName[] { ContentModel.ASPECT_WORKING_COPY });

			// Get an estimate of the size for statuses
		
			
			 ZipSearchDownloadExporter handler = new ZipSearchDownloadExporter(namespaceService, checkOutCheckInService, nodeService, transactionHelper,
					updateService, downloadStorage, contentService, actionedUponNodeRef, templateNodeRef);
			
			
			exporterService.exportView(handler, crawlerParameters, null);

			if ((maximumContentSize > 0) && (handler.getSize() > maximumContentSize)) {
				maximumContentSizeExceeded(actionedUponNodeRef, handler.getSize(), handler.getFileCount());
			} else {
				final File tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
				handler.setZipFile(tempFile);
				try {
					exporterService.exportView(handler, crawlerParameters, null);
					archiveCreationComplete(actionedUponNodeRef, tempFile, handler);
				} catch (DownloadCancelledException ex) {
					downloadCancelled(actionedUponNodeRef, handler);
				} finally {
					tempFile.delete();
				}
			}
			return null;
		}, downloadRequest.getOwner());

	}

	@Override

	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_TPL_NODEREF, DataTypeDefinition.NODE_REF, true, "Search template nodeRef"));
	}

	private void maximumContentSizeExceeded(final NodeRef actionedUponNodeRef, final long size, final long fileCount) {
		log.debug("Maximum contentent size ({}), exceeded ({})", maximumContentSize, size);

		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.MAX_CONTENT_SIZE_EXCEEDED, maximumContentSize, size, 0, fileCount);
			updateService.update(actionedUponNodeRef, status, 1);
			return null;
		}, false, true);
	}

	private void archiveCreationComplete(final NodeRef actionedUponNodeRef, final File tempFile, final ZipSearchDownloadExporter handler) {
		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			try {
				contentServiceHelper.updateContent(actionedUponNodeRef, tempFile);
				DownloadStatus status = new DownloadStatus(Status.DONE, handler.getDone(), handler.getSize(), handler.getFilesAdded(),
						handler.getFileCount());
				updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());

				return null;
			} catch (ContentIOException ex1) {
				throw new DownloadServiceException(CREATION_ERROR, ex1);
			} catch (FileNotFoundException ex2) {
				throw new DownloadServiceException(CREATION_ERROR, ex2);
			} catch (IOException ex3) {
				throw new DownloadServiceException(CREATION_ERROR, ex3);
			}

		}, false, true);
	}

	private void downloadCancelled(final NodeRef actionedUponNodeRef, final ZipSearchDownloadExporter handler) {
		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.CANCELLED, handler.getDone(), handler.getSize(), handler.getFilesAdded(),
					handler.getFileCount());
			updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());

			return null;
		}, false, true);

	}

}
