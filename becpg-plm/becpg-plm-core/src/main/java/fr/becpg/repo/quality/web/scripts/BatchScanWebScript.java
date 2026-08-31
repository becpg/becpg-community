package fr.becpg.repo.quality.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.becpg.repo.quality.BatchScanResult;
import fr.becpg.repo.quality.BatchScanService;

public class BatchScanWebScript extends DeclarativeWebScript {

	private static final Log logger = LogFactory.getLog(BatchScanWebScript.class);

	private NodeService nodeService;
	
	@Autowired
	private BatchScanService batchScanService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<>();

		String batchNodeRefStr = req.getParameter("nodeRef");
		if (batchNodeRefStr == null) {
			status.setCode(Status.STATUS_BAD_REQUEST);
			model.put("scanStatus", "malformed");
			model.put("msgText", "message.qa-batch-scan.malformed");
			return model;
		}

		NodeRef batchNodeRef = new NodeRef(batchNodeRefStr);
		if (!nodeService.exists(batchNodeRef)) {
			status.setCode(Status.STATUS_NOT_FOUND);
			model.put("scanStatus", "batch_not_found");
			model.put("msgText", "message.qa-batch-scan.batch_not_found");
			return model;
		}

		String scanInput = null;
		try {
			String content = req.getContent().getContent();
			if (content != null && !content.isEmpty()) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> jsonMap = mapper.readValue(content, Map.class);
				if (jsonMap != null && jsonMap.containsKey("prop_qa_batchScannerInput")) {
					scanInput = (String) jsonMap.get("prop_qa_batchScannerInput");
				}
			}
		} catch (Exception e) {
			logger.error("Error parsing JSON content", e);
		}

		if (scanInput == null) {
			scanInput = req.getParameter("prop_qa_batchScannerInput");
		}

		BatchScanResult result = batchScanService.scan(batchNodeRef, scanInput);

		model.put("productFound", result.isProductFound());
		model.put("batchIdFound", result.isBatchIdFound());
		model.put("codeErp", result.getCodeErp());
		model.put("productName", result.getProductName());
		model.put("batchId", result.getBatchId());
		model.put("scanStatus", result.getScanStatus());
		model.put("msgText", result.getMsgText());

		if (result.isProductFound() && result.isBatchIdFound()) {
			status.setCode(Status.STATUS_OK);
		} else if ("malformed".equals(result.getScanStatus())) {
			status.setCode(Status.STATUS_BAD_REQUEST);
		} else {
			status.setCode(Status.STATUS_NOT_FOUND);
		}

		return model;
	}
}