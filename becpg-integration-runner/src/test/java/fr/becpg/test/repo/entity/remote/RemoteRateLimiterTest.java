package fr.becpg.test.repo.entity.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.entity.remote.RemoteRateLimiter;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>The point of the per client buckets: a chatty client must not starve the others.</p>
 *
 * @author matthieu
 */
public class RemoteRateLimiterTest {

	private SystemConfigurationService config;

	/** Lets the test choose who is calling, without an authenticated Alfresco session. */
	private static class TestableRateLimiter extends RemoteRateLimiter {
		private String client = "connector-a";

		void callingAs(String client) {
			this.client = client;
		}

		@Override
		protected String currentClient() {
			return client;
		}
	}

	private TestableRateLimiter limiter;

	@Before
	public void setUp() {
		config = mock(SystemConfigurationService.class);
		limiter = new TestableRateLimiter();
		limiter.setSystemConfigurationService(config);
	}

	private void withLimits(String globalCapacity, String globalRefill, String clientCapacity, String clientRefill) {
		when(config.confValue("beCPG.remote.rateLimiter.capacity")).thenReturn(globalCapacity);
		when(config.confValue("beCPG.remote.rateLimiter.refillRate")).thenReturn(globalRefill);
		when(config.confValue("beCPG.remote.rateLimiter.clientCapacity")).thenReturn(clientCapacity);
		when(config.confValue("beCPG.remote.rateLimiter.clientRefillRate")).thenReturn(clientRefill);
	}

	@Test
	public void aChattyClientDoesNotStarveTheOthers() {
		// no refill, so the budget is exactly the capacity: 3 calls each, out of a global 100
		withLimits("100", "0", "3", "0");

		limiter.callingAs("connector-a");
		for (int i = 0; i < 3; i++) {
			assertTrue("connector-a should get its 3 calls", limiter.allowRequest());
		}
		assertFalse("connector-a has spent its budget", limiter.allowRequest());

		limiter.callingAs("connector-b");
		assertTrue("connector-b must be unaffected by connector-a", limiter.allowRequest());
	}

	@Test
	public void theGlobalBucketStillCapsTheWhole() {
		// each client could do 10, but the repository as a whole only allows 2
		withLimits("2", "0", "10", "0");

		limiter.callingAs("connector-a");
		assertTrue(limiter.allowRequest());
		assertTrue(limiter.allowRequest());

		limiter.callingAs("connector-b");
		assertFalse("the global backstop must still apply across clients", limiter.allowRequest());
	}

	@Test
	public void perClientSettingsFallBackOnTheGlobalOnes() {
		// shipped configuration: the per client keys are empty
		withLimits("2", "0", "", null);

		limiter.callingAs("connector-a");
		assertTrue(limiter.allowRequest());
		assertTrue(limiter.allowRequest());
		assertFalse("a lone caller keeps exactly its former budget", limiter.allowRequest());
	}

	@Test
	public void tokensAreRefilledOverTime() throws InterruptedException {
		// 1 token, refilled at 1 per millisecond
		withLimits("100", "1", "1", "1");

		limiter.callingAs("connector-a");
		assertTrue(limiter.allowRequest());
		assertFalse("the single token is spent", limiter.allowRequest());

		Thread.sleep(5);
		assertTrue("the bucket should have refilled", limiter.allowRequest());
	}
}
