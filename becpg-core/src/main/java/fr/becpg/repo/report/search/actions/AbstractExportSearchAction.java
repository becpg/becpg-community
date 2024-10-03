
package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.report.helpers.ReportUtils;
import fr.becpg.report.client.ReportFormat;

/**
 * {@link org.alfresco.repo.action.executer.ActionExecuter} for creating an
 * excel file containing content from the repository.
 *
 * The maximum total size of the content which can be downloaded is controlled
 * by the maximumContentSie property. -1 indicates no limit.
 *
 * @author matthieu form Alex Miller code
 * @version $Id: $Id
 */
public abstract class AbstractExportSearchAction extends ActionExecuterAbstractBase {

	private static final String CREATION_ERROR = "Unexpected error creating file for download";

	/** Constant <code>PARAM_TPL_NODEREF="templateNodeRef"</code> */
	public static final String PARAM_TPL_NODEREF = "templateNodeRef";
	/** Constant <code>PARAM_FORMAT="format"</code> */
	public static final String PARAM_FORMAT = "format";

	private static final Log logger = LogFactory.getLog(AbstractExportSearchAction.class);

	protected NodeService nodeService;
	protected ContentServiceHelper contentServiceHelper;
	protected DownloadStorage downloadStorage;
	protected ExporterService exporterService;
	protected RetryingTransactionHelper transactionHelper;
	protected DownloadStatusUpdateService updateService;
	protected ContentService contentService;
	protected MimetypeService mimetypeService;
	protected EntityActivityService entityActivityService;
	private PersonService personService;
	
	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>contentServiceHelper</code>.
	 * </p>
	 *
	 * @param contentServiceHelper
	 *            a {@link org.alfresco.repo.download.ContentServiceHelper}
	 *            object.
	 */
	public void setContentServiceHelper(ContentServiceHelper contentServiceHelper) {
		this.contentServiceHelper = contentServiceHelper;
	}

	/**
	 * <p>
	 * Setter for the field <code>downloadStorage</code>.
	 * </p>
	 *
	 * @param downloadStorage
	 *            a {@link org.alfresco.repo.download.DownloadStorage} object.
	 */
	public void setDownloadStorage(DownloadStorage downloadStorage) {
		this.downloadStorage = downloadStorage;
	}

	/**
	 * <p>
	 * Setter for the field <code>exporterService</code>.
	 * </p>
	 *
	 * @param exporterService
	 *            a {@link org.alfresco.service.cmr.view.ExporterService}
	 *            object.
	 */
	public void setExporterService(ExporterService exporterService) {
		this.exporterService = exporterService;
	}

	/**
	 * <p>
	 * Setter for the field <code>transactionHelper</code>.
	 * </p>
	 *
	 * @param transactionHelper
	 *            a
	 *            {@link org.alfresco.repo.transaction.RetryingTransactionHelper}
	 *            object.
	 */
	public void setTransactionHelper(RetryingTransactionHelper transactionHelper) {
		this.transactionHelper = transactionHelper;
	}

	/**
	 * <p>
	 * Setter for the field <code>updateService</code>.
	 * </p>
	 *
	 * @param updateService
	 *            a
	 *            {@link org.alfresco.repo.download.DownloadStatusUpdateService}
	 *            object.
	 */
	public void setUpdateService(DownloadStatusUpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * <p>
	 * Setter for the field <code>contentService</code>.
	 * </p>
	 *
	 * @param contentService
	 *            a {@link org.alfresco.service.cmr.repository.ContentService}
	 *            object.
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
	 * <p>Setter for the field <code>entityActivityService</code>.</p>
	 *
	 * @param entityActivityService a {@link fr.becpg.repo.activity.EntityActivityService} object
	 */
	public void setEntityActivityService(EntityActivityService entityActivityService) {
		this.entityActivityService = entityActivityService;
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
		String formatString = (String) action.getParameterValue(PARAM_FORMAT);
		

		ParameterCheck.mandatory(PARAM_TPL_NODEREF, templateNodeRef);
		ParameterCheck.mandatory(PARAM_FORMAT, formatString);

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

			ReportFormat reportFormat = ReportFormat.valueOf(formatString);
			String tplName = ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME));
			String extension = ReportUtils.getReportExtension(tplName, reportFormat);

			AbstractSearchDownloadExporter handler = createHandler(actionedUponNodeRef, templateNodeRef, downloadRequest, reportFormat);

			final File tempFile = TempFileProvider.createTempFile(FilenameUtils.removeExtension(tplName), extension);
			handler.setTempFile(tempFile);
			
			Locale currentLocal = I18NUtil.getLocale();
			Locale currentContentLocal = I18NUtil.getContentLocale();
			try {
				
				
				String userId = downloadRequest.getOwner();
				
				if ((userId != null) && !userId.isEmpty() && !AuthenticationUtil.getGuestUserName().equals(userId)  && personService.personExists(userId)) {
					NodeRef personNodeRef = personService.getPerson(userId);
					if ((personNodeRef != null) && nodeService.exists(personNodeRef)) {

						if (logger.isDebugEnabled()) {
							logger.debug("Set content locale:" + MLTextHelper.getUserContentLocale(nodeService, personNodeRef));
						}

						I18NUtil.setLocale(MLTextHelper.getUserLocale(nodeService,personNodeRef));
						I18NUtil.setContentLocale(MLTextHelper.getUserContentLocale(nodeService,personNodeRef));
					}
				}
				
				exporterService.exportView(handler, crawlerParameters, null);
				
				
				fileCreationComplete(actionedUponNodeRef, extension, tempFile, handler);
				entityActivityService.postExportActivity(null,
						(QName) nodeService.getProperty(templateNodeRef, ReportModel.PROP_REPORT_TPL_CLASS_NAME), FilenameUtils.removeExtension(tplName) + "." + extension.toLowerCase());

			} catch (DownloadCancelledException ex) {
				downloadCancelled(actionedUponNodeRef, handler);
			} finally {
				I18NUtil.setLocale(currentLocal);
				I18NUtil.setContentLocale(currentContentLocal);
				
				if (!tempFile.delete()) {
					logger.error("Cannot delete dir: " + tempFile.getName());
				}
			}
			return null;
		}, downloadRequest.getOwner());

	}

	/**
	 * <p>createHandler.</p>
	 *
	 * @param actionedUponNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param downloadRequest a {@link org.alfresco.service.cmr.download.DownloadRequest} object
	 * @param format a {@link fr.becpg.report.client.ReportFormat} object
	 * @return a {@link fr.becpg.repo.report.search.actions.AbstractSearchDownloadExporter} object
	 */
	protected abstract AbstractSearchDownloadExporter createHandler(NodeRef actionedUponNodeRef, NodeRef templateNodeRef,
			DownloadRequest downloadRequest, ReportFormat format);

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_TPL_NODEREF, DataTypeDefinition.NODE_REF, true, "Search template nodeRef"));
		paramList.add(new ParameterDefinitionImpl(PARAM_FORMAT, DataTypeDefinition.TEXT, false, "Export search format"));
	}

	private void fileCreationComplete(final NodeRef actionedUponNodeRef, String format, final File tempFile,
			final AbstractSearchDownloadExporter handler) {
		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			try {

				contentServiceHelper.updateContent(actionedUponNodeRef, tempFile);
				DownloadStatus status = new DownloadStatus(Status.DONE, handler.getFilesAddedCount(), handler.getFileCount(),
						handler.getFilesAddedCount(), handler.getFileCount());
				updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());
				ContentData contentData = (ContentData) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT);

				nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_CONTENT,
						ContentData.setMimetype(contentData, mimetypeService.getMimetype(format)));
				return null;
			} catch (ContentIOException | IOException ex1) {
				throw new DownloadServiceException(CREATION_ERROR, ex1);
			}
		}, false, true);

	}

	private void downloadCancelled(final NodeRef actionedUponNodeRef, final AbstractSearchDownloadExporter handler) {
		// Update the content and set the status to done.
		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.CANCELLED, handler.getFilesAddedCount(), handler.getFileCount(),
					handler.getFilesAddedCount(), handler.getFileCount());
			updateService.update(actionedUponNodeRef, status, handler.getNextSequenceNumber());

			return null;
		}, false, true);

	}

}
