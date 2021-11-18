package fr.becpg.repo.batch;

import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;

public interface BatchQueueService {
	

	public static final int BATCH_THREAD = 3;
	public static final int BATCH_SIZE = 15;

	public <T> Boolean  queueBatch(BatchInfo batchInfo, BatchProcessWorkProvider<T> workProvider, BatchProcessWorker<T> processWorker);
	
	public List<BatchInfo> getBatchesInQueue();

	boolean removeBatchFromQueue(String batchId);
	
	
}
