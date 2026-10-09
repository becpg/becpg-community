package fr.becpg.repo.web.scripts.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;

/**
 * Unit tests of the status {@link BatchQueueServiceWebScript} returns when a batch is cancelled or
 * removed from the queue.
 */
public class BatchQueueServiceWebScriptTest {

	private static final String BATCH_ID = "import_HKFoods Import - #37112.xlsx";

	private static final String CANCEL_ACTION = "cancel";

	private static final String REMOVE_ACTION = "remove";

	private final BatchQueueService batchQueueService = mock(BatchQueueService.class);

	private final WebScriptRequest req = mock(WebScriptRequest.class);

	private final WebScriptResponse res = mock(WebScriptResponse.class);

	private final StringWriter writer = new StringWriter();

	@Test
	public void acceptsCancellationOfRunningBatch() throws IOException {
		when(batchQueueService.cancelBatch(BATCH_ID)).thenReturn(true);

		execute(CANCEL_ACTION);

		verify(res, never()).setStatus(ArgumentMatchers.anyInt());
	}

	@Test
	public void rejectsCancellationOfUnknownBatch() throws IOException {
		when(batchQueueService.cancelBatch(BATCH_ID)).thenReturn(false);

		execute(CANCEL_ACTION);

		assertNotFound();
	}

	@Test
	public void acceptsRemovalOfPendingBatch() throws IOException {
		when(batchQueueService.removeBatchFromQueue(BATCH_ID)).thenReturn(true);

		execute(REMOVE_ACTION);

		verify(res, never()).setStatus(ArgumentMatchers.anyInt());
	}

	@Test
	public void rejectsRemovalOfUnknownBatch() throws IOException {
		when(batchQueueService.removeBatchFromQueue(BATCH_ID)).thenReturn(false);

		execute(REMOVE_ACTION);

		assertNotFound();
	}

	private void execute(String action) throws IOException {
		Match match = mock(Match.class);
		when(match.getTemplateVars()).thenReturn(Map.of("action", action, BatchInfo.BATCH_ID, BATCH_ID));
		when(req.getServiceMatch()).thenReturn(match);
		when(res.getWriter()).thenReturn(writer);

		BatchQueueServiceWebScript webScript = new BatchQueueServiceWebScript();
		webScript.setBatchQueueService(batchQueueService);
		webScript.execute(req, res);
	}

	private void assertNotFound() {
		verify(res).setStatus(Status.STATUS_NOT_FOUND);
		Assert.assertTrue(writer.toString().contains("\"error\""));
	}
}
