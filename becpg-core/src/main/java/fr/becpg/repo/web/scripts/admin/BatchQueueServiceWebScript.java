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
	
	private BatchQueueService batchQueueService;
	
	

	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}


	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		
		List<BatchInfo> batches = batchQueueService.getBatchesInQueue();
			
		JSONObject ret = new JSONObject();
		
		if (req.getURL().contains("/becpg/batch/queue")) {
			
			try {
				JSONArray jsonBatches = new JSONArray();
				for(BatchInfo batch : batches) {
					JSONObject jsonBatch = new JSONObject();
					jsonBatch.put("batchId", batch.getBatchId());
					jsonBatch.put("batchUser", batch.getBatchUser());
					String label = I18NUtil.getMessage(batch.getBatchDescId());
					
					jsonBatch.put("batchDesc",label!=null ? label :  batch.getBatchDescId());
					jsonBatches.put(jsonBatch);
				}
				ret.put("queue", jsonBatches);
				
				BatchMonitor lastRunningBatch = batchQueueService.getLastRunningBatch();
				
				if(lastRunningBatch!=null) {
					JSONObject last = new JSONObject();
					last.put("batchId", lastRunningBatch.getProcessName());
					last.put("percentCompleted", lastRunningBatch.getPercentComplete());
					ret.put("last", last);
				}
			} catch (JSONException e) {
				logger.error(e,e);
			}
		} else if (req.getURL().contains("/becpg/batch/remove")) {
			String batchId = req.getServiceMatch().getTemplateVars().get("batchId");
			if (batchId != null) {
				batchQueueService.removeBatchFromQueue(batchId);
			}
		}
		
		resp.setContentType("application/json");
		resp.setContentEncoding("UTF-8");
		ret.write(resp.getWriter());
		
	}
}
