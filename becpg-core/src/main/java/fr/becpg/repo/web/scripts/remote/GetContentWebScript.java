package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.AccessDeniedException;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * <p>GetContentWebScript class.</p>
 *
 * @author steven
 * @version $Id: $Id
 */
public class GetContentWebScript extends AbstractEntityWebScript {

	private static final String PARAM_SHARE = "share";

	private EntityReportService entityReportService;
	private AssociationService associationService;
	private ContentService contentService;
	private QuickShareService quickShareService;

	/**
	 * <p>Setter for the field <code>entityReportService</code>.</p>
	 *
	 * @param entityReportService a {@link fr.becpg.repo.report.entity.EntityReportService} object.
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setQuickShareService(QuickShareService quickShareService) {
		this.quickShareService = quickShareService;
	}

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef documentNodeRef = findEntity(req);

		try {

			if ("true".equalsIgnoreCase(req.getParameter(PARAM_SHARE))) {
				if (AccessStatus.ALLOWED.equals(permissionService.hasPermission(documentNodeRef, PermissionService.WRITE))) {
					String sharedId = quickShareService.shareContent(documentNodeRef).getId();
					res.getOutputStream().write(sharedId.getBytes());
				} else {
					throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to share this node");
				}
			} else {
				if (ReportModel.TYPE_REPORT.equals(nodeService.getType(documentNodeRef))) {
					List<NodeRef> sourceAssocList = associationService.getSourcesAssocs(documentNodeRef, ReportModel.ASSOC_REPORTS);

					if (!sourceAssocList.isEmpty()) {
						entityReportService.getOrRefreshReport(sourceAssocList.get(0), documentNodeRef);
					}
				}

				// get the content reader
				ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);
				if ((reader == null) || !reader.exists()) {
					throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to locate content for node ref " + documentNodeRef);
				}

				String mimetype = reader.getMimetype();
				String extensionPath = req.getExtensionPath();
				if ((mimetype == null) || (mimetype.length() == 0)) {
					mimetype = MimetypeMap.MIMETYPE_BINARY;
					int extIndex = extensionPath.lastIndexOf('.');
					if (extIndex != -1) {
						String ext = extensionPath.substring(extIndex + 1);
						mimetype = mimetypeService.getMimetype(ext);
					}
				}
				final String encoding = reader.getEncoding();

				// set mimetype for the content and the character encoding for the stream
				res.setContentType(mimetype);
				res.setContentEncoding(encoding);

				// get the content and stream directly to the response output stream
				// assuming the repository is capable of streaming in chunks, this should allow large files
				// to be streamed directly to the browser response stream.
				reader.getContent(res.getOutputStream());

			}

		} catch (BeCPGException e) {
			logger.error("Cannot export content", e);
			throw new WebScriptException(e.getMessage());
		} catch (AccessDeniedException e) {
			throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}

		}

	}

}
