package fr.becpg.repo.web.scripts.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.entity.EntityReportService;

public class GetContentWebScript extends StreamContent {
	
	private static final Log logger = LogFactory.getLog(GetContentWebScript.class);
	
	@Autowired
	private EntityReportService entityReportService;
	
	@Autowired
	private AssociationService associationService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContentService contentService;
	
	private static final String ENTITY_PARAM = "nodeRef";

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		logger.debug("Get entity: "+ req.getParameter(ENTITY_PARAM));
		NodeRef entityNodeRef = new NodeRef(req.getParameter(ENTITY_PARAM));

		logger.debug("Get entity: "+ entityNodeRef +", type: "+nodeService.getType(entityNodeRef));
		
		if(entityNodeRef != null && ReportModel.TYPE_REPORT.equals(nodeService.getType(entityNodeRef))){
			
			logger.debug("It's a report, assoc: "+associationService.getSourcesAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS).get(0));
			entityReportService.getOrRefreshReport(associationService.getSourcesAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS).get(0), entityNodeRef);
		}

		// Reading the data content of a NodeRef (binary)
		ContentReader reader = contentService.getReader(entityNodeRef, ContentModel.PROP_CONTENT);
		InputStream originalInputStream = reader.getContentInputStream();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final int BUF_SIZE = 1 << 8; //1KiB buffer
		byte[] buffer = new byte[BUF_SIZE];
		int bytesRead = -1;
		while((bytesRead = originalInputStream.read(buffer)) > -1) {
		 outputStream.write(buffer, 0, bytesRead);
		}
		originalInputStream.close();
		byte[] binaryData = outputStream.toByteArray();
		
		res.getOutputStream().write(binaryData);
		
		
	}

}
