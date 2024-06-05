package fr.becpg.repo.audit.service;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;

/**
 * <p>StopWatchScope class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class StopWatchScope implements AutoCloseable {
	
	private String scopeName;
	private Log logger;
	private StopWatch stopWatch;
	private long lastCheckpointTime;
	
	/**
	 * <p>Constructor for StopWatchScope.</p>
	 *
	 * @param scopeName a {@link java.lang.String} object
	 * @param logger a {@link org.apache.commons.logging.Log} object
	 */
	public StopWatchScope(String scopeName, Log logger) {
		this.scopeName = scopeName;
		this.logger = logger;
	}
	
	/**
	 * <p>start.</p>
	 */
	public void start() {
		if (logger.isDebugEnabled()) {
			logger.debug("StopWatchScope start '" + scopeName + "'");
			stopWatch = new StopWatch();
			stopWatch.start();
			lastCheckpointTime = stopWatch.getTime();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void close() {
		if (logger.isDebugEnabled() && stopWatch != null) {
			stopWatch.stop();
			logger.debug("StopWatchScope finish '" + scopeName + "' => " + stopWatch.getTime() + " ms");
		}
	}

	/**
	 * <p>addCheckpoint.</p>
	 *
	 * @param checkpointName a {@link java.lang.String} object
	 */
	public void addCheckpoint(String checkpointName) {
		if (logger.isDebugEnabled() && stopWatch != null) {
			logger.debug("StopWatchScope step '" + checkpointName + "' from '" + scopeName + "' => " + (stopWatch.getTime() - lastCheckpointTime) + " ms");
			lastCheckpointTime = stopWatch.getTime();
		}
	}

}
