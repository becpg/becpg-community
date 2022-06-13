/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;
import java.util.List;

import org.alfresco.repo.batch.BatchMonitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.web.scripts.remote.AbstractEntityWebScript;

/**
 * <p>BatchQueueServiceWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BatchQueueServiceWebScript extends AbstractEntityWebScript {

	private static final Log logger = LogFactory.getLog(BatchQueueServiceWebScript.class);

	private static final String BATCH_ID = "batchId";

	private static final String QUEUE_ACTION = "queue";

	private static final String CANCEL_ACTION = "cancel";

	private static final String REMOVE_ACTION = "remove";

	private BatchQueueService batchQueueService;

	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		List<BatchInfo> batches = batchQueueService.getBatchesInQueue();
		try {
			JSONObject ret = new JSONObject();

			String action = req.getServiceMatch().getTemplateVars().get("action");

			if (QUEUE_ACTION.equals(action)) {

				JSONArray jsonBatches = new JSONArray();
				for (BatchInfo batch : batches) {
					JSONObject jsonBatch = new JSONObject();
					jsonBatch.put(BATCH_ID, batch.getBatchId());
					jsonBatch.put("batchUser", batch.getBatchUser());
					String label = I18NUtil.getMessage(batch.getBatchDescId());

					jsonBatch.put("batchDesc", label != null ? label : batch.getBatchDescId());
					jsonBatches.put(jsonBatch);
				}
				ret.put("queue", jsonBatches);

				BatchMonitor lastRunningBatch = batchQueueService.getLastRunningBatch();

				if (lastRunningBatch != null) {
					JSONObject last = new JSONObject();
					last.put(BATCH_ID, lastRunningBatch.getProcessName());
					last.put("percentCompleted", lastRunningBatch.getPercentComplete());
					ret.put("last", last);
				}

			} else if (CANCEL_ACTION.equals(action)) {
				String batchId = req.getServiceMatch().getTemplateVars().get(BATCH_ID);
				if (batchId != null) {
					batchQueueService.cancelBatch(batchId);
				}
			} else if (REMOVE_ACTION.equals(action)) {
				String batchId = req.getServiceMatch().getTemplateVars().get(BATCH_ID);
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
