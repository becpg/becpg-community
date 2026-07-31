
package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.download.AbstractDownloadArchiveAction;
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
public abstract class AbstractExportSearchAction extends AbstractDownloadArchiveAction {

	/** Constant <code>PARAM_TPL_NODEREF="templateNodeRef"</code> */
	public static final String PARAM_TPL_NODEREF = "templateNodeRef";
	/** Constant <code>PARAM_FORMAT="format"</code> */
	public static final String PARAM_FORMAT = "format";
	/** Constant <code>PARAM_PARAMETERS="parameters"</code> */
	public static final String PARAM_PARAMETERS = "parameters";

	/** Number of nodes crawled per exporter pass */
	private static final int EXPORT_PAGE_SIZE = 500;

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(AbstractExportSearchAction.class);

	protected ContentService contentService;
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
		final DownloadRequest originalRequest = downloadStorage.getDownloadRequest(actionedUponNodeRef);
		final NodeRef[] nodeRefs = getNodeRefsToExport(actionedUponNodeRef, originalRequest);
		
		final DownloadRequest downloadRequest = new DownloadRequest(false, java.util.Collections.emptyList(), originalRequest.getOwner()) {
			@Override
			public NodeRef[] getRequetedNodeRefs() {
				return nodeRefs;
			}
		};

		AuthenticationUtil.runAs(() -> {

			if (completeIfEmpty(actionedUponNodeRef, nodeRefs)) {
				return null;
			}

			ReportFormat reportFormat = ReportFormat.valueOf(formatString);
			String tplName = ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME));
			String extension = ReportUtils.getReportExtension(tplName, reportFormat);

			entityActivityService.postExportActivity(null,
					(QName) nodeService.getProperty(templateNodeRef, ReportModel.PROP_REPORT_TPL_CLASS_NAME), FilenameUtils.removeExtension(tplName) + "." + extension.toLowerCase());
			
			AbstractSearchDownloadExporter handler = createHandler(action, actionedUponNodeRef, templateNodeRef, downloadRequest, reportFormat);

			final File tempFile = TempFileProvider.createTempFile(FilenameUtils.removeExtension(tplName), extension);
			handler.setTempFile(tempFile);
			handler.setExtension(extension);
			
			Locale currentLocal = I18NUtil.getLocale();
			Locale currentContentLocal = I18NUtil.getContentLocale();
			try {
				applyOwnerLocale(downloadRequest.getOwner());

				runExport(actionedUponNodeRef, () -> exportByPages(nodeRefs, handler), handler, tempFile);
			} finally {
				I18NUtil.setLocale(currentLocal);
				I18NUtil.setContentLocale(currentContentLocal);
			}
			return null;
		}, downloadRequest.getOwner());

	}

	/**
	 * Crawl the requested nodes page by page.
	 *
	 * A single crawl over the whole result set keeps every visited node in the transactional caches
	 * until the end of the export; paging bounds that footprint whatever the number of results.
	 *
	 * @param nodeRefs an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects
	 * @param handler a {@link fr.becpg.repo.report.search.actions.AbstractSearchDownloadExporter} object
	 */
	private void exportByPages(NodeRef[] nodeRefs, AbstractSearchDownloadExporter handler) {
		for (int fromIndex = 0; fromIndex < nodeRefs.length; fromIndex += EXPORT_PAGE_SIZE) {
			NodeRef[] page = Arrays.copyOfRange(nodeRefs, fromIndex, Math.min(fromIndex + EXPORT_PAGE_SIZE, nodeRefs.length));

			if (logger.isDebugEnabled()) {
				logger.debug("Exporting page of " + page.length + " node(s), " + fromIndex + " already crawled");
			}

			exporterService.exportView(handler, createCrawlerParameters(page), null);
		}

		handler.endExport();
	}

	/**
	 * Render the report with the locale of the user who requested the download, not the one of the
	 * thread running the asynchronous action.
	 *
	 * @param userId a {@link java.lang.String} object
	 */
	private void applyOwnerLocale(String userId) {
		if ((userId == null) || userId.isEmpty() || AuthenticationUtil.getGuestUserName().equals(userId) || !personService.personExists(userId)) {
			return;
		}

		NodeRef personNodeRef = personService.getPerson(userId);

		if ((personNodeRef == null) || !nodeService.exists(personNodeRef)) {
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Set content locale:" + MLTextHelper.getUserContentLocale(nodeService, personNodeRef));
		}

		I18NUtil.setLocale(MLTextHelper.getUserLocale(nodeService, personNodeRef));
		I18NUtil.setContentLocale(MLTextHelper.getUserContentLocale(nodeService, personNodeRef));
	}

	/**
	 * <p>createHandler.</p>
	 *
	 * @param action a {@link org.alfresco.service.cmr.action.Action} object
	 * @param actionedUponNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param downloadRequest a {@link org.alfresco.service.cmr.download.DownloadRequest} object
	 * @param format a {@link fr.becpg.report.client.ReportFormat} object
	 * @return a {@link fr.becpg.repo.report.search.actions.AbstractSearchDownloadExporter} object
	 */
	protected abstract AbstractSearchDownloadExporter createHandler(Action action, NodeRef actionedUponNodeRef, NodeRef templateNodeRef,
			DownloadRequest downloadRequest, ReportFormat format);

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_TPL_NODEREF, DataTypeDefinition.NODE_REF, true, "Search template nodeRef"));
		paramList.add(new ParameterDefinitionImpl(PARAM_FORMAT, DataTypeDefinition.TEXT, false, "Export search format"));
		paramList.add(new ParameterDefinitionImpl(PARAM_PARAMETERS, DataTypeDefinition.ANY, false, "Extra parameters"));
	}

	private NodeRef[] getNodeRefsToExport(NodeRef downloadNodeRef, DownloadRequest downloadRequest) {
		ContentReader reader = contentService.getReader(downloadNodeRef, ContentModel.PROP_CONTENT);
		if (reader != null && reader.exists() && MimetypeMap.MIMETYPE_JSON.equals(reader.getMimetype())) {
			String json = reader.getContentString();
			return parseNodeRefs(json);
		}
		return downloadRequest.getRequetedNodeRefs();
	}

	private NodeRef[] parseNodeRefs(String json) {
		if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
			return new NodeRef[0];
		}
		String cleaned = json.replace("[", "").replace("]", "").replace("\"", "").trim();
		if (cleaned.isEmpty()) {
			return new NodeRef[0];
		}
		String[] parts = cleaned.split(",");
		NodeRef[] nodeRefs = new NodeRef[parts.length];
		for (int i = 0; i < parts.length; i++) {
			nodeRefs[i] = new NodeRef(parts[i].trim());
		}
		return nodeRefs;
	}

}
