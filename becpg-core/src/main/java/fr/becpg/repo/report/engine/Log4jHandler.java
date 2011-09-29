package fr.becpg.repo.report.engine;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.apache.log4j.Logger;

/**
 * Use to catch standard java logging to log4j
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class Log4jHandler extends Handler {
	public void close() throws SecurityException {
	}

	public void flush() {
	}

	public void publish(LogRecord record) {
		Logger log = Logger.getLogger(record.getLoggerName());
		Level level = record.getLevel();
		String message = record.getMessage();
		if (Level.SEVERE.equals(level)) {
			log.fatal(message);
		} else if (Level.INFO.equals(level)) {
			log.info(message);
		} else if (Level.WARNING.equals(level)) {
			log.warn(message);
		}
	}
}
