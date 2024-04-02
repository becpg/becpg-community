package fr.becpg.repo.audit.service;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;

public class StopWatchScope implements AutoCloseable {
	
	private String scopeName;
	private Log logger;
	private StopWatch stopWatch;
	private long lastCheckpointTime;
	
	public StopWatchScope(String scopeName, Log logger) {
		this.scopeName = scopeName;
		this.logger = logger;
	}
	
	public void start() {
		if (logger.isDebugEnabled()) {
			logger.debug("StopWatchScope start '" + scopeName + "'");
			stopWatch = new StopWatch();
			stopWatch.start();
			lastCheckpointTime = stopWatch.getTime();
		}
	}
	
	@Override
	public void close() {
		if (logger.isDebugEnabled() && stopWatch != null) {
			stopWatch.stop();
			logger.debug("StopWatchScope finish '" + scopeName + "' => " + stopWatch.getTime() + " ms");
		}
	}

	public void addCheckpoint(String checkpointName) {
		if (logger.isDebugEnabled() && stopWatch != null) {
			logger.debug("StopWatchScope step '" + checkpointName + "' from '" + scopeName + "' => " + (stopWatch.getTime() - lastCheckpointTime) + " ms");
			lastCheckpointTime = stopWatch.getTime();
		}
	}

}
