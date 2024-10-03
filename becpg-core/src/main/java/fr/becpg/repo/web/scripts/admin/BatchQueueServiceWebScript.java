/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;
import java.util.List;

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

	private static final Log logger = LogFactory.getLog(BatchQueueServiceWebScript.class);
	
	private static final String QUEUE_ACTION = "queue";

	private static final String CANCEL_ACTION = "cancel";

	private static final String REMOVE_ACTION = "remove";

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
			}

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			ret.write(resp.getWriter());
		} catch (JSONException e) {
			logger.error(e, e);
		}
	}

}
