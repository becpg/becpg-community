package fr.becpg.repo.batch;

import java.util.List;
import java.util.Set;

import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;

public interface BatchQueueService {

	<T> Boolean queueBatch(BatchInfo batchInfo, BatchProcessWorkProvider<T> workProvider, BatchProcessWorker<T> processWorker,
			BatchErrorCallback errorCallback);
	
	<T> Boolean queueBatch(BatchInfo batchInfo, List<BatchStep<T>> batchSteps);
	
	<T> Boolean queueBatch(BatchInfo batchInfo, List<BatchStep<T>> batchSteps, BatchClosingHook closingHook);
	
    List<BatchInfo> getBatchesInQueue();

	boolean removeBatchFromQueue(String batchId);

	BatchMonitor getLastRunningBatch();
	
	boolean cancelBatch(String batchId);
	
	Set<String> getCancelledBatches();

	
}
