package fr.becpg.repo.entity.remote;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>Token bucket guarding the beCPG remote API.</p>
 *
 * <p>
 * Each caller gets its own bucket, so a single chatty client can no longer starve the connectors of
 * every other client. A global bucket is kept on top as a backstop on the total load reaching the
 * repository, with the same settings as before, so a lone caller behaves exactly as it used to.
 * </p>
 *
 * @author Matthieu
 */
@Service("remoteRateLimiter")
public class RemoteRateLimiter {

	private static final Log logger = LogFactory.getLog(RemoteRateLimiter.class);

	/** Beyond this many known callers, the idle ones are dropped rather than kept for ever. */
	private static final int MAX_TRACKED_CLIENTS = 1000;

	/** A bucket untouched for this long is forgotten. */
	private static final long CLIENT_IDLE_MILLIS = 3600_000L;

	/** One log line per client and per window: a throttled client retries, and would flood the log. */
	private static final long LOG_THROTTLE_MILLIS = 60_000L;

	private static final String UNAUTHENTICATED = "<unauthenticated>";

	private final Bucket globalBucket = new Bucket();

	private final Map<String, Bucket> clientBuckets = new ConcurrentHashMap<>();

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	/**
	 * <p>remoteRateLimiterCapacity.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	private Integer remoteRateLimiterCapacity() {
		return Integer.valueOf(systemConfigurationService.confValue("beCPG.remote.rateLimiter.capacity"));
	}

	/**
	 * <p>remoteRateLimiterRefillRate.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	private Double remoteRateLimiterRefillRate() {
		return Double.valueOf(systemConfigurationService.confValue("beCPG.remote.rateLimiter.refillRate"));
	}

	private Integer clientCapacity() {
		String value = systemConfigurationService.confValue("beCPG.remote.rateLimiter.clientCapacity");
		return ((value != null) && !value.isBlank()) ? Integer.valueOf(value.trim()) : remoteRateLimiterCapacity();
	}

	private Double clientRefillRate() {
		String value = systemConfigurationService.confValue("beCPG.remote.rateLimiter.clientRefillRate");
		return ((value != null) && !value.isBlank()) ? Double.valueOf(value.trim()) : remoteRateLimiterRefillRate();
	}

	/**
	 * <p>allowRequest.</p>
	 *
	 * @return a boolean
	 */
	public boolean allowRequest() {
		String client = currentClient();

		if (!clientBucket(client).take(clientCapacity(), clientRefillRate())) {
			logRejection(client, "its own");
			return false;
		}

		if (!globalBucket.take(remoteRateLimiterCapacity(), remoteRateLimiterRefillRate())) {
			logRejection(client, "the global");
			return false;
		}

		return true;
	}

	/**
	 * <p>The authenticated user is the caller identity here: a connector authenticates with its own
	 * account, so it is what tells apart the client to talk to when the limit is reached.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	protected String currentClient() {
		String user = AuthenticationUtil.getFullyAuthenticatedUser();
		return (user != null) ? user : UNAUTHENTICATED;
	}

	private Bucket clientBucket(String client) {
		Bucket bucket = clientBuckets.get(client);
		if (bucket == null) {
			evictIdleClientsIfCrowded();
			bucket = clientBuckets.computeIfAbsent(client, key -> new Bucket());
		}
		return bucket;
	}

	private void evictIdleClientsIfCrowded() {
		if (clientBuckets.size() < MAX_TRACKED_CLIENTS) {
			return;
		}
		long threshold = Instant.now().toEpochMilli() - CLIENT_IDLE_MILLIS;
		for (Iterator<Map.Entry<String, Bucket>> it = clientBuckets.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().lastRefillMillis() < threshold) {
				it.remove();
			}
		}
	}

	private void logRejection(String client, String which) {
		if (logger.isWarnEnabled() && clientBucket(client).shouldLog()) {
			logger.warn("Remote API rate limit reached by client '" + client + "' on " + which
					+ " bucket - its calls are being rejected with a 429 until it slows down");
		}
	}

	/**
	 * <p>A token bucket. Refill rate is expressed per millisecond, so the shipped 0.1 means
	 * 100 requests per second.</p>
	 */
	private static class Bucket {

		private double tokens = -1;
		private long lastRefillMillis = Instant.now().toEpochMilli();
		private long lastLoggedMillis;

		synchronized boolean take(int capacity, double refillRate) {
			long now = Instant.now().toEpochMilli();
			if (tokens < 0) {
				tokens = capacity;
			}
			tokens = Math.min(capacity, tokens + ((now - lastRefillMillis) * refillRate));
			lastRefillMillis = now;
			if (tokens >= 1.0) {
				tokens -= 1.0;
				return true;
			}
			return false;
		}

		synchronized long lastRefillMillis() {
			return lastRefillMillis;
		}

		synchronized boolean shouldLog() {
			long now = Instant.now().toEpochMilli();
			if ((now - lastLoggedMillis) < LOG_THROTTLE_MILLIS) {
				return false;
			}
			lastLoggedMillis = now;
			return true;
		}
	}
}
