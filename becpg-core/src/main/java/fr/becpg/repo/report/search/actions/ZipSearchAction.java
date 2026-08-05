
package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;

import fr.becpg.repo.download.AbstractDownloadArchiveAction;
import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.report.helpers.ExportSearchNodesHelper;
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
public class ZipSearchAction extends AbstractDownloadArchiveAction {
	/** Constant <code>TEMP_FILE_PREFIX="download"</code> */
	private static final String TEMP_FILE_PREFIX = "download";
	/** Constant <code>TEMP_FILE_SUFFIX=".zip"</code> */
	private static final String TEMP_FILE_SUFFIX = ".zip";

	/** Constant <code>PARAM_TPL_NODEREF="templateNodeRef"</code> */
	public static final String PARAM_TPL_NODEREF = "templateNodeRef";

	/** Constant <code>NAME="zipSearchAction"</code> */
	public static final String NAME = "zipSearchAction";

	// Dependencies
	private CheckOutCheckInService checkOutCheckInService;
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

		NodeRef templateNodeRef = (NodeRef) action.getParameterValue(PARAM_TPL_NODEREF);

		ParameterCheck.mandatory(PARAM_TPL_NODEREF, templateNodeRef);

		// Get the download request data and set up the exporter crawler
		// parameters.
		final DownloadRequest downloadRequest = downloadStorage.getDownloadRequest(actionedUponNodeRef);

		AuthenticationUtil.runAs(() -> {

			NodeRef[] nodeRefs = getNodeRefsToExport(actionedUponNodeRef, downloadRequest);
			if (completeIfEmpty(actionedUponNodeRef, nodeRefs)) {
				return null;
			}

			ExporterCrawlerParameters crawlerParameters = createCrawlerParameters(nodeRefs);

			ZipSearchDownloadExporter handler = new ZipSearchDownloadExporter(checkOutCheckInService, nodeService, transactionHelper,
					updateService, downloadStorage, contentService, expressionService, alfrescoRepository, actionedUponNodeRef, templateNodeRef);

			// First pass: estimate the size of the archive
			exporterService.exportView(handler, crawlerParameters, null);

			if ((maximumContentSize > 0) && (handler.getSize() > maximumContentSize)) {
				maximumContentSizeExceeded(actionedUponNodeRef, maximumContentSize, handler.getSize(), handler.getFileCount());
				return null;
			}

			final File tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
			handler.setZipFile(tempFile);

			runExport(actionedUponNodeRef, crawlerParameters, handler, tempFile);

			return null;
		}, downloadRequest.getOwner());

	}

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_TPL_NODEREF, DataTypeDefinition.NODE_REF, true, "Search template nodeRef"));
	}

	private NodeRef[] getNodeRefsToExport(NodeRef downloadNodeRef, DownloadRequest downloadRequest) {
		NodeRef[] storedNodeRefs = ExportSearchNodesHelper.readNodes(contentService, nodeService, downloadNodeRef);

		return storedNodeRefs.length > 0 ? storedNodeRefs : downloadRequest.getRequetedNodeRefs();
	}

}
