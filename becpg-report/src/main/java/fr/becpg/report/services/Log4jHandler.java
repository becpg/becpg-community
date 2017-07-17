/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.report.services;

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

	final Log logger = LogFactory.getLog(Log4jHandler.class);

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
