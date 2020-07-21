
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
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
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
import org.apache.commons.io.FilenameUtils;

import fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer;
import fr.becpg.repo.report.template.ReportTplService;

/**
 * {@link org.alfresco.repo.action.executer.ActionExecuter} for creating an excel file containing content from the
 * repository.
 *
 * The maximum total size of the content which can be downloaded is controlled
 * by the maximumContentSie property. -1 indicates no limit.
 *
 * @author matthieu form Alex Miller code
 * @version $Id: $Id
 */
public class ExcelSearchAction extends ActionExecuterAbstractBase {

	private static final String CREATION_ERROR = "Unexpected error creating file for download";

	/** Constant <code>PARAM_TPL_NODEREF="templateNodeRef"</code> */
	public static final String PARAM_TPL_NODEREF = "templateNodeRef";

	// Dependencies

	private ExcelReportSearchRenderer excelReportSearchRenderer;

	private NodeService nodeService;
	private ContentServiceHelper contentServiceHelper;
	private DownloadStorage downloadStorage;
	private ExporterService exporterService;
	private RetryingTransactionHelper transactionHelper;
	private DownloadStatusUpdateService updateService;
	private ContentService contentService;

	/**
	 * <p>Setter for the field <code>excelReportSearchRenderer</code>.</p>
	 *
	 * @param excelReportSearchRenderer a {@link fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer} object.
	 */
	public void setExcelReportSearchRenderer(ExcelReportSearchRenderer excelReportSearchRenderer) {
		this.excelReportSearchRenderer = excelReportSearchRenderer;
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
	 * {@inheritDoc}
	 *
	 * Create an archive file containing content from the repository.
	 *
	 * Uses the {@link ExporterService} with custom exporters to create the
	 * archive files.
	 */
	@Override
	protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {

		NodeRef templateNodeRef = (NodeRef) action.getParameterValue(PARAM_TPL_NODEREF);

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

			ExcelSearchDownloadExporter handler = new ExcelSearchDownloadExporter(transactionHelper, updateService, downloadStorage, contentService,
					excelReportSearchRenderer, actionedUponNodeRef, templateNodeRef, Long.valueOf(downloadRequest.getRequetedNodeRefs().length));

			String suffix = ReportTplService.PARAM_VALUE_XLSXREPORT_EXTENSION;
			String name = ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME));
			
			if(name.endsWith(ReportTplService.PARAM_VALUE_XLSMREPORT_EXTENSION)) {
				suffix = ReportTplService.PARAM_VALUE_XLSMREPORT_EXTENSION;
			}
			
			final File tempFile = TempFileProvider.createTempFile( FilenameUtils.removeExtension(name), suffix);
			handler.setTemplateFile(tempFile);
			try {
				exporterService.exportView(handler, crawlerParameters, null);
				fileCreationComplete(actionedUponNodeRef, suffix , tempFile, handler);
			} catch (DownloadCancelledException ex) {
				downloadCancelled(actionedUponNodeRef, handler);
			} finally {
				tempFile.delete();
			}
			return null;
		}, downloadRequest.getOwner());

	}

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_TPL_NODEREF, DataTypeDefinition.NODE_REF, true, "Search template nodeRef"));
	}

	private void fileCreationComplete(final NodeRef actionedUponNodeRef, String suffix, final File tempFile, final ExcelSearchDownloadExporter handler) {
		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			try {

				contentServiceHelper.updateContent(actionedUponNodeRef, tempFile);
				DownloadStatus status = new DownloadStatus(Status.DONE, handler.getFilesAddedCount(), handler.getFileCount(),
						handler.getFilesAddedCount(), handler.getFileCount());
				updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());
				ContentData contentData = (ContentData) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT);
				
				
				ContentData excelContentData = null;
						
				if(ReportTplService.PARAM_VALUE_XLSMREPORT_EXTENSION.equals(suffix)) {
					excelContentData = ContentData.setMimetype(contentData, "application/vnd.ms-excel.sheet.macroEnabled.12");
				} else {
					excelContentData = ContentData.setMimetype(contentData, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				}
				
				nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT, excelContentData);
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

	private void downloadCancelled(final NodeRef actionedUponNodeRef, final ExcelSearchDownloadExporter handler) {
		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.CANCELLED, handler.getFilesAddedCount(), handler.getFileCount(),
					handler.getFilesAddedCount(), handler.getFileCount());
			updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());

			return null;
		}, false, true);

	}

}
