package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.file.AccessDeniedException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 *
 * @author steven
 *
 */
public class GetContentWebScript extends AbstractEntityWebScript {

	private EntityReportService entityReportService;
	private AssociationService associationService;
	private ContentService contentService;

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef documentNodeRef = findEntity(req);

		try (OutputStream out = res.getOutputStream()) {
			
			if (ReportModel.TYPE_REPORT.equals(nodeService.getType(documentNodeRef))) {
				List<NodeRef> sourceAssocList = associationService.getSourcesAssocs(documentNodeRef, ReportModel.ASSOC_REPORTS);

				if (!sourceAssocList.isEmpty()) {
					entityReportService.getOrRefreshReport(sourceAssocList.get(0), documentNodeRef);
				}
			}

			// get the content reader
			ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);
			if ((reader == null) || !reader.exists()) {
				throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to locate content for node ref " + documentNodeRef);
			}

			IOUtils.copy(reader.getContentInputStream(), out);
			
		} catch (BeCPGException e) {
			logger.error("Cannot export content", e);
			throw new WebScriptException(e.getMessage());
		} catch (AccessDeniedException e) {
			throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled())
				logger.info("Client aborted stream read:\n\tcontent", e1);

		} 
		
		
	}

}
