/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;

/**
 * <p>BatchQueueServiceWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BatchQueueServiceWebScript extends AbstractWebScript {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(BatchQueueServiceWebScript.class);
	
	/** Constant <code>QUEUE_ACTION="queue"</code> */
	private static final String QUEUE_ACTION = "queue";

	/** Constant <code>CANCEL_ACTION="cancel"</code> */
	private static final String CANCEL_ACTION = "cancel";

	/** Constant <code>REMOVE_ACTION="remove"</code> */
	private static final String REMOVE_ACTION = "remove";
	
	/** Constant <code>RETRY_ACTION="retry"</code> */
	private static final String RETRY_ACTION = "retry";
	
	/** Constant <code>ERRORS_ACTION="errors"</code> */
	private static final String ERRORS_ACTION = "errors";

	private BatchQueueService batchQueueService;

	/**
	 * <p>Setter for the field <code>batchQueueService</code>.</p>
	 *
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 */
	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		try {
			JSONObject ret = new JSONObject();

			String action = req.getServiceMatch().getTemplateVars().get("action");

			if (QUEUE_ACTION.equals(action)) {
				
				List<String> batches = batchQueueService.getBatchesInQueue();

				JSONArray jsonBatches = new JSONArray();
				for (String batch : batches) {
					jsonBatches.put(batch);
				}
				ret.put("queue", jsonBatches);

				String lastRunningBatch = batchQueueService.getRunningBatchInfo();

				if (lastRunningBatch != null) {
					ret.put("last", lastRunningBatch);
				}
				
				String errorBatches = batchQueueService.getBatchesInError();
				ret.put("errors", errorBatches);

			} else if (CANCEL_ACTION.equals(action)) {
				String batchId = req.getServiceMatch().getTemplateVars().get(BatchInfo.BATCH_ID);
				if (batchId != null) {
					batchQueueService.cancelBatch(batchId);
				}
			} else if (REMOVE_ACTION.equals(action)) {
				String batchId = req.getServiceMatch().getTemplateVars().get(BatchInfo.BATCH_ID);
				if (batchId != null) {
					batchQueueService.removeBatchFromQueue(batchId);
				}
			} else if (RETRY_ACTION.equals(action)) {
				String batchId = req.getServiceMatch().getTemplateVars().get("batchId");
				String nodeRefStr = req.getParameter("nodeRef");
				if (nodeRefStr != null && !nodeRefStr.isBlank()) {
					if (NodeRef.isNodeRef(nodeRefStr)) {
						batchQueueService.retryBatchEntryInError(batchId, new NodeRef(nodeRefStr));
					} else {
						resp.setStatus(400);
						ret.put("error", "Invalid nodeRef: " + nodeRefStr);
					}
				} else {
					batchQueueService.retryBatchInError(batchId);
				}
			} else if (ERRORS_ACTION.equals(action)) {
				String batchId = req.getServiceMatch().getTemplateVars().get("batchId");
				String offsetParam = req.getParameter("offset");
				String limitParam = req.getParameter("limit");
				int offset = parseInt(offsetParam, 0);
				int limit = parseInt(limitParam, -1);
				ret = new JSONObject(batchQueueService.viewErrors(batchId, offset, limit));
			}

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			ret.write(resp.getWriter());
		} catch (Exception e) {
			logger.error("Error executing batch queue webscript: " + e.getMessage(), e);
			resp.setStatus(500);
			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			try {
				JSONObject err = new JSONObject();
				err.put("error", e.getMessage());
				err.write(resp.getWriter());
			} catch (JSONException je) {
				logger.error(je, je);
			}
		}
	}

	private int parseInt(String param, int defaultValue) {
		if (param != null && !param.isBlank()) {
			try {
				return Integer.parseInt(param);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

}
