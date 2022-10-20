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

	private static final String STEPS_MAX = "stepsMax";

	private static final String STEP_COUNT = "stepCount";

	private static final String UNKNOWN = "unknown";

	private static final Log logger = LogFactory.getLog(BatchQueueServiceWebScript.class);
	
	private static final String BATCH_DESC_ID = "batchDescId";

	private static final String BATCH_USER = "batchUser";
	
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
					
					String entityDescription = batch.getEntityDescription();
					
					JSONObject jsonBatch = new JSONObject();
					jsonBatch.put(BATCH_ID, batch.getBatchId());
					jsonBatch.put(BATCH_USER, batch.getBatchUser());
					String label = I18NUtil.getMessage(batch.getBatchDescId(), entityDescription);

					jsonBatch.put(BATCH_DESC_ID,label!=null ? label :  batch.getBatchDescId());
					jsonBatches.put(jsonBatch);
				}
				ret.put("queue", jsonBatches);

				BatchMonitor lastRunningBatch = batchQueueService.getLastRunningBatch();

				if (lastRunningBatch != null) {
					ret.put("last", buildLastBatchJson(lastRunningBatch));
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

	private JSONObject buildLastBatchJson(BatchMonitor lastRunningBatch) throws JSONException {
		JSONObject last = new JSONObject();
			
		try {
					
			JSONObject batchInfo = new JSONObject(lastRunningBatch.getProcessName());
			String entityDescription = null;
			
			if (batchInfo.has("entityDescription")) {
				entityDescription = batchInfo.getString("entityDescription");
			}
			
			if (batchInfo.has(STEP_COUNT) && batchInfo.has(STEPS_MAX)) {
				last.put(STEP_COUNT, batchInfo.get(STEP_COUNT));
				last.put(STEPS_MAX, batchInfo.get(STEPS_MAX));
			}
			
			last.put(BATCH_ID, batchInfo.getString(BATCH_ID));
			last.put(BATCH_USER, batchInfo.getString(BATCH_USER));
			String descriptionLabel = I18NUtil.getMessage(batchInfo.getString(BATCH_DESC_ID), entityDescription);
			last.put(BATCH_DESC_ID, descriptionLabel != null ? descriptionLabel : batchInfo.getString(BATCH_DESC_ID));
		} catch (JSONException e) {
			last.put(BATCH_ID, lastRunningBatch.getProcessName());
			last.put(BATCH_DESC_ID, lastRunningBatch.getProcessName());
			last.put(BATCH_USER, UNKNOWN);
			logger.warn("Could not parse JSON : " + lastRunningBatch.getProcessName());
		}
		
		last.put("percentCompleted", lastRunningBatch.getPercentComplete());
		
		return last;
	}
}
