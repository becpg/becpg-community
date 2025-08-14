package fr.becpg.test.repo.batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.alfresco.repo.batch.BatchProcessor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
		
		assertTrue(batchQueueService.getBatchesInQueue().isEmpty());
		assertNull(batchQueueService.getRunningBatchInfo());
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
		
		assertTrue(batchQueueService.getBatchesInQueue().isEmpty());
		assertNull(batchQueueService.getRunningBatchInfo());
	}
	
}
