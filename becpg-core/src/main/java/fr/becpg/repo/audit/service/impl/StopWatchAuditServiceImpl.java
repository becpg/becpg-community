package fr.becpg.repo.audit.service.impl;

import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.audit.service.StopWatchAuditService;

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
	public void stop(Log logger, StopWatch stopWatch, String loggingMessage) {
		if (logger.isDebugEnabled() && (stopWatch != null)) {
			stopWatch.stop();
			logger.debug(loggingMessage + " takes " + stopWatch.getTotalTimeSeconds() + " seconds");
		}
	}

	@Override
	public void addLogMessage(Log logger, StopWatch stopWatch, String message) {
		if (logger.isDebugEnabled() && (stopWatch != null)) {
			stopWatch.stop();
			logger.debug(message + " - " + stopWatch.getTotalTimeSeconds() + " seconds");
			stopWatch.start();
		}
	}

}
