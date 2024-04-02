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

	private static final String CANCELLED = "cancelled";

	private static final String PERCENT_COMPLETED = "percentCompleted";

	private static final String ENTITY_DESCRIPTION = "entityDescription";

	private static final String STEPS_MAX = "stepsMax";

	private static final String STEP_COUNT = "stepCount";
	
	private static final String STEP_DESC_ID = "stepDescId";

	private static final String UNKNOWN = "unknown";

	private static final Log logger = LogFactory.getLog(BatchQueueServiceWebScript.class);
	
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
					jsonBatch.put(BatchInfo.BATCH_ID, batch.getBatchId());
					jsonBatch.put(BatchInfo.BATCH_USER, batch.getBatchUser());
					String label = I18NUtil.getMessage(batch.getBatchDescId(), entityDescription);
					
					jsonBatch.put(BatchInfo.BATCH_DESC_ID,label!=null ? label :  batch.getBatchDescId());
					jsonBatches.put(jsonBatch);
				}
				ret.put("queue", jsonBatches);

				BatchMonitor lastRunningBatch = batchQueueService.getLastRunningBatch();

				if (lastRunningBatch != null) {
					ret.put("last", buildLastBatchJson(lastRunningBatch));
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

	private JSONObject buildLastBatchJson(BatchMonitor lastRunningBatch) throws JSONException {
		JSONObject last = new JSONObject();
			
		try {
					
			JSONObject batchInfo = new JSONObject(lastRunningBatch.getProcessName());
			String entityDescription = null;
			
			if (batchInfo.has(ENTITY_DESCRIPTION)) {
				entityDescription = batchInfo.getString(ENTITY_DESCRIPTION);
			}
			
			if (batchInfo.has(STEP_COUNT) && batchInfo.has(STEPS_MAX)) {
				last.put(STEP_COUNT, batchInfo.get(STEP_COUNT));
				last.put(STEPS_MAX, batchInfo.get(STEPS_MAX));
			}
			
			last.put(BatchInfo.BATCH_ID, batchInfo.getString(BatchInfo.BATCH_ID));
			last.put(BatchInfo.BATCH_USER, batchInfo.getString(BatchInfo.BATCH_USER));
			
			String descriptionLabel = I18NUtil.getMessage(batchInfo.getString(BatchInfo.BATCH_DESC_ID), entityDescription);
			
			if (batchInfo.has(STEP_DESC_ID) && batchInfo.getString(STEP_DESC_ID) != null) {
				descriptionLabel += " - " + I18NUtil.getMessage(batchInfo.getString(STEP_DESC_ID));
			}
			
			last.put(BatchInfo.BATCH_DESC_ID, descriptionLabel != null ? descriptionLabel : batchInfo.getString(BatchInfo.BATCH_DESC_ID));
			
			
			if (batchQueueService.getCancelledBatches().contains(batchInfo.getString(BatchInfo.BATCH_ID))) {
				last.put(CANCELLED, true);
			}
			
			String percentCompleted = lastRunningBatch.getPercentComplete();
			last.put(PERCENT_COMPLETED, percentCompleted);
			
		} catch (JSONException e) {
			last.put(BatchInfo.BATCH_ID, lastRunningBatch.getProcessName());
			last.put(BatchInfo.BATCH_DESC_ID, lastRunningBatch.getProcessName());
			last.put(BatchInfo.BATCH_USER, UNKNOWN);
			logger.warn("Could not parse JSON : " + lastRunningBatch.getProcessName());
		}
		
		return last;
	}
}
