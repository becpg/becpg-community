package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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

		// TODO Look at ContentStreamer streamContent class and do better
		// (Mimetype ...)
		try (OutputStream out = res.getOutputStream()) {

			IOUtils.copy(reader.getContentInputStream(), out);
		}

	}

}
