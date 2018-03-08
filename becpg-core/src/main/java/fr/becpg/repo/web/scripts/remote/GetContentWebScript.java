package fr.becpg.repo.web.scripts.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.entity.EntityReportService;

public class GetContentWebScript extends StreamContent {

	private EntityReportService entityReportService;
	private AssociationService associationService;
	private NodeService nodeService;
	private ContentService contentService;

	private static final String ENTITY_PARAM = "nodeRef";

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef entityNodeRef = new NodeRef(req.getParameter(ENTITY_PARAM));

		if (entityNodeRef != null) {

			if (ReportModel.TYPE_REPORT.equals(nodeService.getType(entityNodeRef))) {
				List<NodeRef> sourceAssocList = associationService.getSourcesAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS);

				if (!sourceAssocList.isEmpty()) {
					entityReportService.getOrRefreshReport(sourceAssocList.get(0), entityNodeRef);
				}
			}

			// Reading the data content of a NodeRef (binary)
			ContentReader reader = contentService.getReader(entityNodeRef, ContentModel.PROP_CONTENT);
			InputStream originalInputStream = reader.getContentInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final int BUF_SIZE = 1 << 8; // 1KiB buffer
			byte[] buffer = new byte[BUF_SIZE];
			int bytesRead = -1;
			while ((bytesRead = originalInputStream.read(buffer)) > -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			originalInputStream.close();
			byte[] binaryData = outputStream.toByteArray();

			res.getOutputStream().write(binaryData);
		}

	}

}
