package fr.becpg.repo.audit.service;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;

public class StopWatchScope {
	
	private String scopeName;
	private Log logger;
	private StopWatch stopWatch;
	private long lastSplitTime;
	
	public StopWatchScope(String scopeName, Log logger) {
		this.scopeName = scopeName;
		this.logger = logger;
	}
	
	public void start() {
		if (logger.isDebugEnabled()) {
			logger.debug("start of StopWatchScope '" + scopeName + "'");
			stopWatch = new StopWatch();
			stopWatch.start();
			lastSplitTime = stopWatch.getTime();
		}
	}

	public void stop() {
		if (logger.isDebugEnabled() && stopWatch != null) {
			stopWatch.stop();
			logger.debug("StopWatchScope '" + scopeName + "' takes " + stopWatch.getTime() + " ms");
		}
	}

	public void addAnnotation(Object annotation) {
		if (logger.isDebugEnabled() && stopWatch != null) {
			logger.debug("'" + annotation + "' takes " + (stopWatch.getTime() - lastSplitTime) + " ms");
			lastSplitTime = stopWatch.getTime();
		}
	}

}
