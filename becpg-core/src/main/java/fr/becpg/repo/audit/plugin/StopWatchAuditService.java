package fr.becpg.repo.audit.plugin;

import org.apache.commons.logging.Log;
import org.springframework.util.StopWatch;

public interface StopWatchAuditService {

	StopWatch start(Log logger);

	void stop(Log logger, StopWatch watch, String loggingMessage);

}
