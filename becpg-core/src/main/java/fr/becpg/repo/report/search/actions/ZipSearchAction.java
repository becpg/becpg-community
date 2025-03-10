
package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
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
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * {@link org.alfresco.repo.action.executer.ActionExecuter} for creating an archive (ie. zip) file containing
 * content from the repository.
 *
 * The maximum total size of the content which can be downloaded is controlled
 * by the maximumContentSie property. -1 indicates no limit.
 *
 * @author matthieu form Alex Miller code
 * @version $Id: $Id
 */
public class ZipSearchAction extends ActionExecuterAbstractBase {
	private static final Logger log = LoggerFactory.getLogger(ZipSearchAction.class);

	private static final String CREATION_ERROR = "Unexpected error creating archive file for download";
	private static final String TEMP_FILE_PREFIX = "download";
	private static final String TEMP_FILE_SUFFIX = ".zip";

	/** Constant <code>PARAM_TPL_NODEREF="templateNodeRef"</code> */
	public static final String PARAM_TPL_NODEREF = "templateNodeRef";
	private static final Log logger = LogFactory.getLog(ZipSearchAction.class);

	/** Constant <code>NAME="zipSearchAction"</code> */
	public static final String NAME = "zipSearchAction";

	// Dependencies
	private CheckOutCheckInService checkOutCheckInService;
	private ContentServiceHelper contentServiceHelper;
	private DownloadStorage downloadStorage;
	private ExporterService exporterService;
	private NodeService nodeService;
	private RetryingTransactionHelper transactionHelper;
	private DownloadStatusUpdateService updateService;
	private ContentService contentService;
	private ExpressionService expressionService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private long maximumContentSize = -1l;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	/**
	 * <p>Setter for the field <code>expressionService</code>.</p>
	 *
	 * @param expressionService a {@link fr.becpg.repo.expressions.ExpressionService} object
	 */
	public void setExpressionService(ExpressionService expressionService) {
		this.expressionService = expressionService;
	}
	
	/**
	 * <p>Setter for the field <code>checkOutCheckInService</code>.</p>
	 *
	 * @param checkOutCheckInService a {@link org.alfresco.service.cmr.coci.CheckOutCheckInService} object.
	 */
	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	/**
	 * <p>Setter for the field <code>contentServiceHelper</code>.</p>
	 *
	 * @param contentServiceHelper a {@link org.alfresco.repo.download.ContentServiceHelper} object.
	 */
	public void setContentServiceHelper(ContentServiceHelper contentServiceHelper) {
		this.contentServiceHelper = contentServiceHelper;
	}

	/**
	 * <p>Setter for the field <code>downloadStorage</code>.</p>
	 *
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object.
	 */
	public void setDownloadStorage(DownloadStorage downloadStorage) {
		this.downloadStorage = downloadStorage;
	}

	/**
	 * <p>Setter for the field <code>exporterService</code>.</p>
	 *
	 * @param exporterService a {@link org.alfresco.service.cmr.view.ExporterService} object.
	 */
	public void setExporterService(ExporterService exporterService) {
		this.exporterService = exporterService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>transactionHelper</code>.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object.
	 */
	public void setTransactionHelper(RetryingTransactionHelper transactionHelper) {
		this.transactionHelper = transactionHelper;
	}

	/**
	 * <p>Setter for the field <code>updateService</code>.</p>
	 *
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object.
	 */
	public void setUpdateService(DownloadStatusUpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>maximumContentSize</code>.</p>
	 *
	 * @param maximumContentSize a long.
	 */
	public void setMaximumContentSize(long maximumContentSize) {
		this.maximumContentSize = maximumContentSize;
	}

	
	/**
	 * {@inheritDoc}
	 *
	 * Create an archive file containing content from the repository.
	 *
	 * Uses the {@link ExporterService} with custom exporters to create the
	 * archive files.
	 */
	@Override
	protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {
		

		NodeRef templateNodeRef =  (NodeRef) action.getParameterValue(PARAM_TPL_NODEREF);
		
		ParameterCheck.mandatory(PARAM_TPL_NODEREF, templateNodeRef);
		
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
		
			
			 ZipSearchDownloadExporter handler = new ZipSearchDownloadExporter( checkOutCheckInService, nodeService, transactionHelper,
					updateService, downloadStorage, contentService, expressionService, alfrescoRepository, actionedUponNodeRef, templateNodeRef);
			
			
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
					if(!tempFile.delete()) {
						logger.error("Cannot delete dir: "+tempFile.getName());
					}
				}
			}
			return null;
		}, downloadRequest.getOwner());

	}

	/** {@inheritDoc} */
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
