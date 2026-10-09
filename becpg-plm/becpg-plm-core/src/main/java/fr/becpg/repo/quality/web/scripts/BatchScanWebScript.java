package fr.becpg.repo.quality.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.becpg.repo.quality.BatchScanResult;
import fr.becpg.repo.quality.BatchScanService;

/**
 * Controller for batch scanning operations. Delegates calls to BatchScanService.
 * Handles both initial barcode scan and manual lot entry for an allocation line.
 */
public class BatchScanWebScript extends DeclarativeWebScript {

    private static final Log logger = LogFactory.getLog(BatchScanWebScript.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private NodeService nodeService;

    @Autowired
    private BatchScanService batchScanService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setBatchScanService(BatchScanService batchScanService) {
        this.batchScanService = batchScanService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>();

        String batchNodeRefStr = req.getParameter("nodeRef");
        String scanInput = null;

        try {
            Content content = req.getContent();
            if (content != null) {
                String contentStr = content.getContent();
                if (contentStr != null && !contentStr.isEmpty()) {
                    Map<?, ?> jsonMap = OBJECT_MAPPER.readValue(contentStr, Map.class);
                    if (jsonMap != null) {
                        if (jsonMap.containsKey("nodeRef")) {
                            batchNodeRefStr = (String) jsonMap.get("nodeRef");
                        }
                        if (jsonMap.containsKey("prop_qa_batchScannerInput")) {
                            scanInput = (String) jsonMap.get("prop_qa_batchScannerInput");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing JSON content in BatchScanWebScript", e);
        }

        BatchScanResult result;

        if (batchNodeRefStr != null && !batchNodeRefStr.trim().isEmpty() && NodeRef.isNodeRef(batchNodeRefStr.trim())) {
            NodeRef batchNodeRef = new NodeRef(batchNodeRefStr.trim());
            if (!nodeService.exists(batchNodeRef)) {
                status.setCode(Status.STATUS_NOT_FOUND);
                model.put("scanStatus", BatchScanService.STATUS_BATCH_NOT_FOUND);
                return model;
            }
            result = batchScanService.scan(batchNodeRef, scanInput);
        } else {
            status.setCode(Status.STATUS_BAD_REQUEST);
            model.put("scanStatus", BatchScanService.STATUS_MALFORMED);
            return model;
        }

        model.put("scanStatus", result.getScanStatus());
        model.put("allocationNodeRef", result.getAllocationNodeRef());
        model.put("codeErp", result.getCodeErp());
        model.put("productName", result.getProductName());
        model.put("batchId", result.getBatchId());
        model.put("productType", result.getProductType());
        model.put("stockNodeRef", result.getStockNodeRef());

        if (BatchScanService.STATUS_MALFORMED.equals(result.getScanStatus())) {
            status.setCode(Status.STATUS_BAD_REQUEST);
        } else {
            status.setCode(Status.STATUS_OK);
        }

        return model;
    }
}
