package fr.becpg.repo.batch;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests of the cancellation of batches that {@link BatchQueueServiceImpl} does not run.
 */
public class BatchQueueServiceImplTest {

	@Test
	public void refusesToCancelUnknownBatch() {
		BatchQueueServiceImpl batchQueueService = new BatchQueueServiceImpl();

		Assert.assertFalse(batchQueueService.cancelBatch("import_HKFoods Import - "));
	}
}
