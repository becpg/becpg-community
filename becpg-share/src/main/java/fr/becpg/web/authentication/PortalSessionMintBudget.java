package fr.becpg.web.authentication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>PortalSessionMintBudget class.</p>
 *
 * A fixed window rate limiter for {@link fr.becpg.web.authentication.PortalSessionPost}.
 *
 * It exists so that a leaked <code>X-beCPG-Portal-Key</code> cannot be used to flood Share
 * with sessions or to enumerate users, and so that a portal stuck in a retry loop costs a few
 * refusals rather than one Tomcat session per attempt for the whole session timeout.
 *
 * A fixed window is deliberate: it is a few lines, it needs no background thread, and the
 * worst it can do — twice the budget across a window boundary — is irrelevant at these rates.
 *
 * @author matthieu
 */
class PortalSessionMintBudget {

	/** Windows are one minute long, which is the unit the configured budgets are expressed in. */
	private static final long WINDOW_MILLIS = 60_000L;

	/**
	 * Hard cap on the number of tracked users, so a caller that presents a stream of distinct
	 * usernames cannot grow the heap. Reaching it simply clears the map: the counters are a
	 * safety net, not an accounting record.
	 */
	private static final int MAX_TRACKED_USERS = 10_000;

	private final Map<String, AtomicInteger> perUser = new ConcurrentHashMap<>();

	private final AtomicInteger global = new AtomicInteger();

	private volatile long windowStart = System.currentTimeMillis();

	/**
	 * Records one attempt and says whether it is within budget.
	 *
	 * @param username the verified username, or null to only charge the global budget
	 * @param globalBudget maximum mints per minute, all users together
	 * @param userBudget maximum mints per minute for one user
	 * @return true when the attempt is allowed
	 */
	synchronized boolean tryConsume(String username, int globalBudget, int userBudget) {
		long now = System.currentTimeMillis();
		if (now - windowStart >= WINDOW_MILLIS) {
			windowStart = now;
			global.set(0);
			perUser.clear();
		}

		if (global.incrementAndGet() > globalBudget) {
			return false;
		}

		if (username == null) {
			return true;
		}

		if (perUser.size() >= MAX_TRACKED_USERS) {
			perUser.clear();
		}
		return perUser.computeIfAbsent(username, k -> new AtomicInteger()).incrementAndGet() <= userBudget;
	}

}
