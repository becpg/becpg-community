package fr.becpg.repo.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;

public abstract class NodeRefErrorHandlingBatchStep extends BatchStep<NodeRef> {

	@SuppressWarnings("unchecked")
	protected NodeRefErrorHandlingBatchStep(NodeService nodeService, BeCPGCacheService beCPGCacheService, BatchInfo batchInfo) {
		String batchFullId = batchInfo.getBatchId() + "|" + batchInfo.getBatchDescId();
		processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entry) throws Throwable {
				processEntry(entry);
			}
		};
		setBatchStepErrorHandler(nodeRef -> {
			List<String> batchErrorIds = (List<String>) nodeService.getProperty(nodeRef, BeCPGModel.PROP_BATCH_ERROR_IDS);
			if (batchErrorIds == null) {
				batchErrorIds = new ArrayList<>();
			}
			if (!batchErrorIds.contains(batchFullId)) {
				batchErrorIds.add(batchFullId);
			}
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_BATCH_ERROR_IDS, (Serializable) batchErrorIds);
			beCPGCacheService.clearCache(BatchQueueServiceImpl.class.getName());
		});
	}
	
	public abstract void processEntry(NodeRef entry) throws Throwable;
	
}
