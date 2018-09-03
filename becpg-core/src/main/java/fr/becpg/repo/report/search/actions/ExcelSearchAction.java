
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.report.search.impl.ExcelReportSearchPlugin;

/**
 * {@link ActionExecuter} for creating an excel file containing
 * content from the repository.
 * 
 * The maximum total size of the content which can be downloaded is controlled
 * by the maximumContentSie property. -1 indicates no limit.
 *
 * @author matthieu form Alex Miller code
 */
public class ExcelSearchAction extends ActionExecuterAbstractBase {

	private static final String CREATION_ERROR = "Unexpected error creating file for download";
	private static final String TEMP_FILE_PREFIX = "download";
	private static final String TEMP_FILE_SUFFIX = ".xlsx";

	public static final String PARAM_TPL_NODEREF = "templateNodeRef";

	// Dependencies
	@Autowired
	private ExcelReportSearchPlugin[] excelReportSearchPlugins;
	private AttributeExtractorService attributeExtractorService;
	private EntityDictionaryService entityDictionaryService;
	
	private NodeService nodeService;
	private ContentServiceHelper contentServiceHelper;
	private DownloadStorage downloadStorage;
	private ExporterService exporterService;
	private RetryingTransactionHelper transactionHelper;
	private DownloadStatusUpdateService updateService;
	private ContentService contentService;
	private NamespaceService namespaceService;

	
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setExcelReportSearchPlugins(ExcelReportSearchPlugin[] excelReportSearchPlugins) {
		this.excelReportSearchPlugins = excelReportSearchPlugins;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
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


	public void setTransactionHelper(RetryingTransactionHelper transactionHelper) {
		this.transactionHelper = transactionHelper;
	}

	public void setUpdateService(DownloadStatusUpdateService updateService) {
		this.updateService = updateService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
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

			 ExcelSearchDownloadExporter handler = new ExcelSearchDownloadExporter(namespaceService, transactionHelper,
					updateService, downloadStorage, contentService, excelReportSearchPlugins, attributeExtractorService, 
					entityDictionaryService,  actionedUponNodeRef, templateNodeRef,Long.valueOf(downloadRequest.getRequetedNodeRefs().length ));
			
				final File tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
				handler.setTemplateFile(tempFile);
				try {
					exporterService.exportView(handler, crawlerParameters, null);
					fileCreationComplete(actionedUponNodeRef, tempFile, handler);
				} catch (DownloadCancelledException ex) {
					downloadCancelled(actionedUponNodeRef, handler);
				} finally {
					tempFile.delete();
				}
			return null;
		}, downloadRequest.getOwner());

	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_TPL_NODEREF, DataTypeDefinition.NODE_REF, true, "Search template nodeRef"));
	}

	private void fileCreationComplete(final NodeRef actionedUponNodeRef, final File tempFile, final ExcelSearchDownloadExporter handler) {
		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			try {

				contentServiceHelper.updateContent(actionedUponNodeRef, tempFile);
				DownloadStatus status = new DownloadStatus(Status.DONE, handler.getFilesAddedCount(), handler.getFileCount(), handler.getFilesAddedCount(),
						handler.getFileCount());
				updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());
				ContentData contentData = (ContentData) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT);
				ContentData excelContentData = ContentData.setMimetype(contentData, "application/vnd.ms-excel");
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
			DownloadStatus status = new DownloadStatus(Status.CANCELLED, handler.getFilesAddedCount(), handler.getFileCount(), handler.getFilesAddedCount(),
					handler.getFileCount());
			updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());

			return null;
		}, false, true);

	}
	
	


}
