package fr.becpg.repo.batch;

import java.util.List;

import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;

public interface BatchQueueService {
	

	<T> Boolean queueBatch(BatchInfo batchInfo, BatchProcessWorkProvider<T> workProvider, BatchProcessWorker<T> processWorker,
			BatchErrorCallback errorCallback);

    List<BatchInfo> getBatchesInQueue();

	boolean removeBatchFromQueue(String batchId);

	BatchMonitor getLastRunningBatch();

	
	
}
