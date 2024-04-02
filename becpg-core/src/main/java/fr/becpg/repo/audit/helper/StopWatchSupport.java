package fr.becpg.repo.audit.helper;

import java.lang.System.Logger.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.service.StopWatchScope;

public class StopWatchSupport {
	
	private static final ThreadLocal<StopWatchScope> threadLocalScope = new ThreadLocal<>();

	private static final Log stopWatchlogger = LogFactory.getLog(StopWatchSupport.class);

	private Log logger;
	
	private String scopeName = "default";
	
	private Level level = Level.DEBUG;
	
	private StopWatchSupport() {
		
	}
	
	public static StopWatchSupport build() {
		return new StopWatchSupport();
	}
	
	public <T> T run(Action<T> action) {
		if (logger == null) {
			logger = stopWatchlogger;
		}
		if (isLoggerEnabled(logger, level)) {
			StopWatchScope parentScope = threadLocalScope.get();
			try (StopWatchScope scope = new StopWatchScope(scopeName, logger)) {
				threadLocalScope.set(scope);
				scope.start();
				return action.run();
			} finally {
				threadLocalScope.remove();
				if (parentScope != null) {
					threadLocalScope.set(parentScope);
				}
			}
		} else {
			return action.run();
		}
	}
	
	public StopWatchSupport logger(Log logger) {
		this.logger = logger;
		return this;
	}
	
	public StopWatchSupport scopeName(String scopeName) {
		this.scopeName = scopeName;
		return this;
	}
	
	public StopWatchSupport level(Level level) {
		this.level = level;
		return this;
	}
	
	public interface Action<T> {
		T run();
	}
	
	private static boolean isLoggerEnabled(Log logger, Level level) {
		if (level.equals(Level.ERROR)) {
			return logger.isErrorEnabled();
		}
		if (level.equals(Level.INFO)) {
			return logger.isInfoEnabled();
		}
		if (level.equals(Level.DEBUG)) {
			return logger.isDebugEnabled();
		}
		if (level.equals(Level.TRACE)) {
			return logger.isTraceEnabled();
		}
		return false;
	}
	
	public static void addCheckpoint(String name) {
		StopWatchScope currentScope = threadLocalScope.get();
		if (currentScope != null) {
			currentScope.addCheckpoint(name);
		}
	}

}
