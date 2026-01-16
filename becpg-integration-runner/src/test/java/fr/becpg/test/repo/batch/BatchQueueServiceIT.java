package fr.becpg.test.repo.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.test.RepoBaseTestCase;

public class BatchQueueServiceIT extends RepoBaseTestCase {

	@Autowired
	private BatchQueueService batchQueueService;
	
	@Test
	public void testBatchEnd() throws InterruptedException {
		
		AtomicInteger processedEntries = new AtomicInteger(0);
		BatchInfo batchInfo = new BatchInfo("batch.id", "batch.desc");
		BatchStep<Integer> step = new BatchStep<>();
		batchInfo.setWorkerThreads(1);
		step.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		step.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				processedEntries.addAndGet(1);
				Thread.sleep(500);
			}
		});
		
		batchQueueService.queueBatch(batchInfo, List.of(step));
		waitForBatchEnd(batchInfo);
		assertEquals(10, processedEntries.get());
		
		assertTrue(batchQueueService.getBatchesInQueue().isEmpty());
		assertNull(batchQueueService.getRunningBatchInfo());
	}
	
	@Test
	public void testCancelBatch() throws InterruptedException {
		
		AtomicInteger processedEntries = new AtomicInteger(0);
		BatchInfo batchInfo = new BatchInfo("batch.id", "batch.desc");
		batchInfo.setWorkerThreads(1);
		BatchStep<Integer> step = new BatchStep<>();
		step.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		step.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				processedEntries.addAndGet(1);
				if (processedEntries.get() == 5) {
					batchQueueService.cancelBatch(batchInfo.getBatchId());
				}
				Thread.sleep(500);
			}
		});
		
		batchQueueService.queueBatch(batchInfo, List.of(step));
		waitForBatchEnd(batchInfo);
		assertEquals(5, processedEntries.get());
		
		assertTrue(batchQueueService.getBatchesInQueue().isEmpty());
		assertNull(batchQueueService.getRunningBatchInfo());
	}
	
	@Test
	public void testCancelPausedBatch() throws InterruptedException {
		
		AtomicInteger lowProcessedEntries = new AtomicInteger(0);
		AtomicInteger highProcessedEntries = new AtomicInteger(0);
		
		BatchInfo lowBatchInfo = new BatchInfo("batch.low.id", "batch.low.desc");
		lowBatchInfo.setPriority(BatchPriority.LOW);
		lowBatchInfo.setWorkerThreads(1);
		BatchStep<Integer> lowStep = new BatchStep<>();
		lowStep.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		lowStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				lowProcessedEntries.addAndGet(1);
				Thread.sleep(500);
			}
		});
		
		batchQueueService.queueBatch(lowBatchInfo, List.of(lowStep));
		
		Thread.sleep(1000);
		
		BatchInfo highBatchInfo = new BatchInfo("batch.high.id", "batch.high.desc");
		highBatchInfo.setPriority(BatchPriority.HIGH);
		highBatchInfo.setWorkerThreads(1);
		BatchStep<Integer> highStep = new BatchStep<>();
		highStep.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		highStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				highProcessedEntries.addAndGet(1);
				if (highProcessedEntries.get() == 5) {
					batchQueueService.cancelBatch(lowBatchInfo.getBatchId());
				}
				assertTrue(lowProcessedEntries.get() > 0);
				assertTrue(lowProcessedEntries.get() < 10);
				Thread.sleep(500);
			}
		});
		
		batchQueueService.queueBatch(highBatchInfo, List.of(highStep));
		
		waitForBatchEnd(highBatchInfo);
		assertEquals(10, highProcessedEntries.get());
		assertTrue(lowProcessedEntries.get() < 10);
		
		waitForBatchEnd(lowBatchInfo);
		assertTrue(lowProcessedEntries.get() < 10);
		
	}
	
	@Test
	public void testBatchPriority() throws InterruptedException {
		
		AtomicInteger lowProcessedEntries = new AtomicInteger(0);
		AtomicInteger mediumProcessedEntries = new AtomicInteger(0);
		AtomicInteger medium2ProcessedEntries = new AtomicInteger(0);
		AtomicInteger highProcessedEntries = new AtomicInteger(0);
		
		BatchInfo lowBatchInfo = new BatchInfo("batch.low.id", "batch.low.desc");
		lowBatchInfo.setPriority(BatchPriority.LOW);
		lowBatchInfo.setBatchSize(1);
		lowBatchInfo.setWorkerThreads(1);
		BatchStep<Integer> lowStep = new BatchStep<>();
		lowStep.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		lowStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				lowProcessedEntries.addAndGet(1);
				Thread.sleep(500);
			}
		});
		
		batchQueueService.queueBatch(lowBatchInfo, List.of(lowStep));
		
		Thread.sleep(1000);
		
		BatchInfo mediumBatchInfo = new BatchInfo("batch.medium.id", "batch.medium.desc");
		mediumBatchInfo.setPriority(BatchPriority.MEDIUM);
		mediumBatchInfo.setBatchSize(1);
		mediumBatchInfo.setWorkerThreads(1);
		BatchStep<Integer> mediumStep = new BatchStep<>();
		mediumStep.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		mediumStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				mediumProcessedEntries.addAndGet(1);
				assertTrue(lowProcessedEntries.get() > 0);
				assertTrue(lowProcessedEntries.get() < 10);
				assertEquals(0, medium2ProcessedEntries.get());
				Thread.sleep(500);
			}
		});
		
		batchQueueService.queueBatch(mediumBatchInfo, List.of(mediumStep));
		
		Thread.sleep(1000);
		
		BatchInfo highBatchInfo = new BatchInfo("batch.high.id", "batch.high.desc");
		highBatchInfo.setPriority(BatchPriority.HIGH);
		highBatchInfo.setBatchSize(1);
		highBatchInfo.setWorkerThreads(1);
		BatchStep<Integer> highStep = new BatchStep<>();
		highStep.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		highStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				highProcessedEntries.addAndGet(1);
				assertTrue(lowProcessedEntries.get() > 0);
				assertTrue(lowProcessedEntries.get() < 10);
				assertTrue(mediumProcessedEntries.get() > 0);
				assertTrue(mediumProcessedEntries.get() < 10);
				assertEquals(0, medium2ProcessedEntries.get());
				Thread.sleep(500);
			}
		});
		
		batchQueueService.queueBatch(highBatchInfo, List.of(highStep));
		
		BatchInfo medium2BatchInfo = new BatchInfo("batch.medium2.id", "batch.medium2.desc");
		medium2BatchInfo.setPriority(BatchPriority.MEDIUM);
		medium2BatchInfo.setBatchSize(1);
		medium2BatchInfo.setWorkerThreads(1);
		BatchStep<Integer> medium2Step = new BatchStep<>();
		medium2Step.setWorkProvider(new EntityListBatchProcessWorkProvider<Integer>(IntStream.range(0, 10).boxed().toList()));
		medium2Step.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Integer>() {
			@Override
			public void process(Integer entry) throws Throwable {
				medium2ProcessedEntries.addAndGet(1);
				assertTrue(lowProcessedEntries.get() > 0);
				assertTrue(lowProcessedEntries.get() < 10);
				assertEquals(10, mediumProcessedEntries.get());
				Thread.sleep(500);
			}
		});
		
		Thread.sleep(1000);
		
		batchQueueService.queueBatch(medium2BatchInfo, List.of(medium2Step));
		
		waitForBatchEnd(highBatchInfo);
		assertEquals(10, highProcessedEntries.get());
		assertTrue(lowProcessedEntries.get() < 10);
		assertTrue(mediumProcessedEntries.get() < 10);
		assertEquals(0, medium2ProcessedEntries.get());
		
		waitForBatchEnd(mediumBatchInfo);
		assertEquals(10, mediumProcessedEntries.get());
		assertTrue(lowProcessedEntries.get() < 10);
		assertTrue(medium2ProcessedEntries.get() < 10);
		
		waitForBatchEnd(medium2BatchInfo);
		assertEquals(10, medium2ProcessedEntries.get());
		assertTrue(lowProcessedEntries.get() < 10);
		
		waitForBatchEnd(lowBatchInfo);
		assertEquals(10, lowProcessedEntries.get());
		
	}
	
	@Test
	public void testBatchStepWithErrorHandling_AllSuccess() throws InterruptedException {
		// Create test nodes
		List<NodeRef> testNodes = createTestNodes(5);
		AtomicInteger processedCount = new AtomicInteger(0);
		
		BatchInfo batchInfo = new BatchInfo("test.error.batch", "test.error.batch.desc");
		batchInfo.setRunAsSystem(true);
		batchInfo.setWorkerThreads(1);
		
		BatchStep<NodeRef> step = batchQueueService.createBatchStepWithErrorHandling(
			batchInfo, 
			testNodes, 
			new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
				@Override
				public void process(NodeRef entry) throws Throwable {
					processedCount.incrementAndGet();
					// Simulate successful processing
					nodeService.setProperty(entry, BeCPGModel.PROP_CODE, "processed");
				}
			}
		);
		
		batchQueueService.queueBatch(batchInfo, List.of(step));
		waitForBatchEnd(batchInfo);
		
		// All nodes should be processed successfully
		assertEquals(5, processedCount.get());
		
		// No nodes should have error markers
		for (NodeRef node : testNodes) {
			assertFalse(nodeService.hasAspect(node, BeCPGModel.ASPECT_BATCH_ERROR));
		}
	}
	
	@Test
	public void testBatchStepWithErrorHandling_SkipAlreadyFailed() throws InterruptedException {
		// Create test nodes
		List<NodeRef> testNodes = createTestNodes(3);
		AtomicInteger processedCount = new AtomicInteger(0);
		
		BatchInfo batchInfo = new BatchInfo("test.skip.batch", "test.skip.batch.desc");
		batchInfo.setRunAsSystem(true);
		batchInfo.setWorkerThreads(1);
		String batchFullId = batchInfo.getBatchId() + "|" + batchInfo.getBatchDescId();
		
		// Pre-mark node 1 as failed for this batch
		NodeRef preFailedNode = testNodes.get(1);
		inWriteTx(() -> {
			if (!nodeService.hasAspect(preFailedNode, BeCPGModel.ASPECT_BATCH_ERROR)) {
				nodeService.addAspect(preFailedNode, BeCPGModel.ASPECT_BATCH_ERROR, null);
			}
			List<String> errorIds = new ArrayList<>();
			errorIds.add(batchFullId);
			nodeService.setProperty(preFailedNode, BeCPGModel.PROP_BATCH_ERROR_IDS, (Serializable) errorIds);
			return null;
		});
		
		BatchStep<NodeRef> step = batchQueueService.createBatchStepWithErrorHandling(
			batchInfo, 
			testNodes, 
			new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
				@Override
				public void process(NodeRef entry) throws Throwable {
					processedCount.incrementAndGet();
					nodeService.setProperty(entry, BeCPGModel.PROP_CODE, "processed");
				}
			}
		);
		
		batchQueueService.queueBatch(batchInfo, List.of(step));
		waitForBatchEnd(batchInfo);
		
		// Only 2 nodes should be processed (node 1 should be skipped)
		assertEquals(2, processedCount.get());
		
		// Verify the pre-failed node still has the error marker
		@SuppressWarnings("unchecked")
		List<String> errorIds = (List<String>) nodeService.getProperty(preFailedNode, BeCPGModel.PROP_BATCH_ERROR_IDS);
		assertTrue(errorIds.contains(batchFullId));
	}
	
	@Test
	public void testBatchStepWithErrorHandling_MultipleErrorIds() throws InterruptedException {
		// Create test nodes
		List<NodeRef> testNodes = createTestNodes(3);
		AtomicInteger processedCount = new AtomicInteger(0);
		
		// First batch - will fail on node 0
		BatchInfo batchInfo1 = new BatchInfo("test.multi.batch1", "test.multi.batch.desc");
		batchInfo1.setRunAsSystem(true);
		batchInfo1.setWorkerThreads(1);
		String batchFullId1 = batchInfo1.getBatchId() + "|" + batchInfo1.getBatchDescId();
		
		BatchStep<NodeRef> step1 = batchQueueService.createBatchStepWithErrorHandling(
			batchInfo1, 
			testNodes, 
			new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
				@Override
				public void process(NodeRef entry) throws Throwable {
					processedCount.incrementAndGet();
					if (testNodes.indexOf(entry) == 0) {
						throw new RuntimeException("Error in batch 1");
					}
				}
			}
		);
		
		batchQueueService.queueBatch(batchInfo1, List.of(step1));
		waitForBatchEnd(batchInfo1);
		
		processedCount.set(0);
		
		// Second batch - will fail on node 0 again
		BatchInfo batchInfo2 = new BatchInfo("test.multi.batch2", "test.multi.batch.desc");
		batchInfo2.setRunAsSystem(true);
		batchInfo2.setWorkerThreads(1);
		String batchFullId2 = batchInfo2.getBatchId() + "|" + batchInfo2.getBatchDescId();
		
		BatchStep<NodeRef> step2 = batchQueueService.createBatchStepWithErrorHandling(
			batchInfo2, 
			testNodes, 
			new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
				@Override
				public void process(NodeRef entry) throws Throwable {
					processedCount.incrementAndGet();
					if (testNodes.indexOf(entry) == 0) {
						throw new RuntimeException("Error in batch 2");
					}
				}
			}
		);
		
		batchQueueService.queueBatch(batchInfo2, List.of(step2));
		waitForBatchEnd(batchInfo2);
		
		// Check that node 0 has both error IDs
		NodeRef failedNode = testNodes.get(0);
		@SuppressWarnings("unchecked")
		List<String> errorIds = (List<String>) nodeService.getProperty(failedNode, BeCPGModel.PROP_BATCH_ERROR_IDS);
		
		assertNotNull(errorIds);
		assertEquals(2, errorIds.size());
		assertTrue(errorIds.contains(batchFullId1));
		assertTrue(errorIds.contains(batchFullId2));
	}
	
	@Test
	public void testRetryBatchInError() throws InterruptedException {
		// Create test nodes
		List<NodeRef> testNodes = createTestNodes(3);
		
		BatchInfo batchInfo = new BatchInfo("test.retry.batch", "test.retry.batch.desc");
		batchInfo.setRunAsSystem(true);
		batchInfo.setWorkerThreads(1);
		String batchFullId = batchInfo.getBatchId() + "|" + batchInfo.getBatchDescId();
		
		// First run - fail all nodes
		BatchStep<NodeRef> step1 = batchQueueService.createBatchStepWithErrorHandling(
			batchInfo, 
			testNodes, 
			new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
				@Override
				public void process(NodeRef entry) throws Throwable {
					throw new RuntimeException("Simulated error");
				}
			}
		);
		
		batchQueueService.queueBatch(batchInfo, List.of(step1));
		waitForBatchEnd(batchInfo);
		
		// Verify all nodes have error markers
		for (NodeRef node : testNodes) {
			assertTrue(inReadTx(() -> nodeService.hasAspect(node, BeCPGModel.ASPECT_BATCH_ERROR)));
		}
		
		// Retry the batch
		batchInfo = batchQueueService.retryBatchInError(batchFullId);
		waitForBatchEnd(batchInfo);
		
		// Verify error markers are removed
		for (NodeRef node : testNodes) {
			@SuppressWarnings("unchecked")
			List<String> errorIds = inReadTx(() -> (List<String>) nodeService.getProperty(node, BeCPGModel.PROP_BATCH_ERROR_IDS));
			if (errorIds != null) {
				assertFalse(errorIds.contains(batchFullId));
			}
		}
	}
	
	private List<NodeRef> createTestNodes(int count) {
		return inWriteTx(() -> {
			List<NodeRef> nodes = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				NodeRef node = createEntity(i);
				nodes.add(node);
			}
			return nodes;
		});
	}
	
	private NodeRef createEntity(int index) {
		// Create a simple node for testing
		// Adjust according to your entity creation method
		return nodeService.createNode(
			getTestFolderNodeRef(),
			org.alfresco.model.ContentModel.ASSOC_CONTAINS,
			org.alfresco.model.ContentModel.ASSOC_CONTAINS,
			org.alfresco.model.ContentModel.TYPE_CONTENT
		).getChildRef();
	}
	
}
