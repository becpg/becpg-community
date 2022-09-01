package fr.becpg.repo.audit.plugin.impl;

import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.audit.plugin.StopWatchAuditService;

@Service
public class StopWatchAuditServiceImpl implements StopWatchAuditService {

	
	@Override
	public StopWatch start(Log logger) {
		
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		
		return watch;
	}

	@Override
	public void stop(Log logger, StopWatch watch, String loggingMessage) {
		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug(loggingMessage + " takes " + watch.getTotalTimeSeconds() + " seconds");
		}
	}

}
