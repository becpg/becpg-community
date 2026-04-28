package fr.becpg.repo.batch;

import java.util.List;
import java.util.function.BiConsumer;

import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>BatchQueueService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BatchQueueService {

	/**
	 * <p>queueBatch.</p>
	 *
	 * @param batchInfo a {@link fr.becpg.repo.batch.BatchInfo} object
	 * @param workProvider a {@link org.alfresco.repo.batch.BatchProcessWorkProvider} object
	 * @param processWorker a {@link org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker} object
	 * @param errorCallback a {@link fr.becpg.repo.batch.BatchErrorCallback} object
	 * @return a {@link java.lang.Boolean} object
	 * @param <T> a T class
	 */
	<T> Boolean queueBatch(BatchInfo batchInfo, BatchProcessWorkProvider<T> workProvider, BatchProcessWorker<T> processWorker,
			BatchErrorCallback errorCallback);
	
	/**
	 * <p>queueBatch.</p>
	 *
	 * @param batchInfo a {@link fr.becpg.repo.batch.BatchInfo} object
	 * @param batchSteps a {@link java.util.List} object
	 * @return a {@link java.lang.Boolean} object
	 * @param <T> a T class
	 */
	<T> Boolean queueBatch(BatchInfo batchInfo, List<BatchStep<T>> batchSteps);
	
	/**
	 * <p>isBatchInQueue.</p>
	 *
	 * @param batchInfo a {@link fr.becpg.repo.batch.BatchInfo} object
	 * @return a boolean
	 */
	boolean isBatchInQueue(BatchInfo batchInfo);
	
	/**
	 * <p>queueBatch.</p>
	 *
	 * @param batchInfo a {@link fr.becpg.repo.batch.BatchInfo} object
	 * @param batchSteps a {@link java.util.List} object
	 * @param closingHook a {@link fr.becpg.repo.batch.BatchClosingHook} object
	 * @return a {@link java.lang.Boolean} object
	 * @param <T> a T class
	 */
	<T> Boolean queueBatch(BatchInfo batchInfo, List<BatchStep<T>> batchSteps, BatchClosingHook closingHook);
	
    /**
     * <p>getBatchesInQueue.</p>
     *
     * @return a {@link java.util.List} object
     */
    List<String> getBatchesInQueue();

	/**
	 * <p>removeBatchFromQueue.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 * @return a boolean
	 */
	boolean removeBatchFromQueue(String batchId);

	/**
	 * <p>getLastRunningBatch.</p>
	 *
	 * @return a {@link org.alfresco.repo.batch.BatchMonitor} object
	 */
	BatchMonitor getLastRunningBatch();
	
	/**
	 * <p>cancelBatch.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 * @return a boolean
	 */
	boolean cancelBatch(String batchId);
	
	/**
	 * <p>getRunningBatchInfo.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getRunningBatchInfo();

	/**
	 * <p>getBatchesInError.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getBatchesInError();

	/**
	 * <p>retryBatchInError.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.batch.BatchInfo} object
	 */
	BatchInfo retryBatchInError(String batchId);

	/**
	 * <p>viewErrors.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String viewErrors(String batchId);

	/**
	 * <p>createBatchStepWithErrorHandling.</p>
	 *
	 * @param batchInfo a {@link fr.becpg.repo.batch.BatchInfo} object
	 * @param list a {@link java.util.List} object
	 * @param consumer a {@link org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker} object
	 * @return a {@link fr.becpg.repo.batch.BatchStep} object
	 */
	BatchStep<NodeRef> createBatchStepWithErrorHandling(BatchInfo batchInfo, List<NodeRef> list, BatchProcessWorker<NodeRef> consumer);

	BatchStep<NodeRef> createBatchStepWithErrorHandling(BatchInfo batchInfo, List<NodeRef> list, BatchProcessWorker<NodeRef> processor,
			BiConsumer<NodeRef, Throwable> errorHandler);
}
