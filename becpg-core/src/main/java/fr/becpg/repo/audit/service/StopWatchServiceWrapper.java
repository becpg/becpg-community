package fr.becpg.repo.audit.service;

import org.apache.commons.logging.Log;
import org.springframework.util.StopWatch;

public class StopWatchServiceWrapper {
	
	private StopWatchAuditService stopWatchAuditService;
	private String name;
	private Log logger;
	private StopWatch stopWatch;
	
	public StopWatchServiceWrapper(StopWatchAuditService stopWatchAuditService, String name, Log logger) {
		this.stopWatchAuditService = stopWatchAuditService;
		this.name = name;
		this.logger = logger;
	}
	
	public StopWatchAuditService getStopWatchAuditService() {
		return stopWatchAuditService;
	}
	
	public String getName() {
		return name;
	}
	
	public Log getLogger() {
		return logger;
	}
	
	public StopWatch getStopWatch() {
		return stopWatch;
	}

	public void start() {
		stopWatch = stopWatchAuditService.start(logger);
	}

	public void stop() {
		stopWatchAuditService.stop(logger, stopWatch, name);
	}

	public void addAnnotation(String string) {
		stopWatchAuditService.addLogMessage(logger, stopWatch, string);
	}

}
