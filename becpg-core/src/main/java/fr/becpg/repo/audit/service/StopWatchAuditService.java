package fr.becpg.repo.audit.service;

import org.apache.commons.logging.Log;
import org.springframework.util.StopWatch;

public interface StopWatchAuditService {

	StopWatch start(Log logger);

	void stop(Log logger, StopWatch watch, String loggingMessage);

	void addLogMessage(Log logger, StopWatch stopWatch, String message);

}
