package fr.becpg.repo.entity.remote;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>RemoteRateLimiter class.</p>
 *
 * @author Matthieu
 */
@Service("remoteRateLimiter")
public class RemoteRateLimiter {
	
    private double tokens = 100; // Current number of tokens in the bucket
    private Instant lastRefillTime = Instant.now(); // Time of last refill
    
    @Autowired
    private SystemConfigurationService systemConfigurationService;
    
    
    private Integer remoteRateLimiterCapacity() {
		return Integer.valueOf(systemConfigurationService.confValue("beCPG.remote.rateLimiter.capacity"));
	}

	private Double remoteRateLimiterRefillRate() {
		return Double.valueOf(systemConfigurationService.confValue("beCPG.remote.rateLimiter.refillRate"));
	}

    /**
     * <p>allowRequest.</p>
     *
     * @return a boolean
     */
    public synchronized boolean allowRequest() {
        refillTokens();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true; 
        }
        return false; 
    }

    private void refillTokens() {
        Instant now = Instant.now();
        double timeElapsed = (now.toEpochMilli() - lastRefillTime.toEpochMilli());
        double tokensToAdd = timeElapsed * remoteRateLimiterRefillRate();
        tokens = Math.min(remoteRateLimiterCapacity(), tokens + tokensToAdd);
        lastRefillTime = now;
    }
}
