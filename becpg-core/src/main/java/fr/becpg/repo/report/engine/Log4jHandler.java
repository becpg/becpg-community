package fr.becpg.repo.report.engine;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Use to catch standard java logging to log4j
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class Log4jHandler extends Handler {

	Log logger = LogFactory.getLog(Log4jHandler.class);

	public void close() throws SecurityException {
	}

	public void flush() {
	}

	public void publish(LogRecord record) {

		if (record != null) {
			String loggerName = record.getLoggerName();
			if (logger.isDebugEnabled()) {
				logger.debug("Map java logging to common Logging :" + loggerName);
			}
			String message = record.getMessage();
			if (loggerName != null) {
				Log log = LogFactory.getLog(loggerName);
				Level level = record.getLevel();
				if (message != null) {
					if (Level.SEVERE.equals(level)) {
						log.fatal(message);
					} else if (Level.INFO.equals(level)) {
						log.info(message);
					} else if (Level.WARNING.equals(level)) {
						log.warn(message);
					}
				}
			} else if (message != null) {
				logger.warn("No catched message:" + message);
			}
		}
	}
}
