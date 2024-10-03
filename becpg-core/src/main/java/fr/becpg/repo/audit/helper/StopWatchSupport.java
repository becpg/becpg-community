package fr.becpg.repo.audit.helper;

import java.lang.System.Logger.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.service.StopWatchScope;

/**
 * <p>StopWatchSupport class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class StopWatchSupport {
	
	private static final ThreadLocal<StopWatchScope> threadLocalScope = new ThreadLocal<>();

	private static final Log stopWatchlogger = LogFactory.getLog(StopWatchSupport.class);

	private Log logger;
	
	private String scopeName = "default";
	
	private Level level = Level.DEBUG;
	
	private StopWatchSupport() {
		
	}
	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.audit.helper.StopWatchSupport} object
	 */
	public static StopWatchSupport build() {
		return new StopWatchSupport();
	}
	
	/**
	 * <p>run.</p>
	 *
	 * @param action a {@link fr.becpg.repo.audit.helper.StopWatchSupport.Action} object
	 * @return a T object
	 */
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
	
	/**
	 * <p>logger.</p>
	 *
	 * @param logger a {@link org.apache.commons.logging.Log} object
	 * @return a {@link fr.becpg.repo.audit.helper.StopWatchSupport} object
	 */
	public StopWatchSupport logger(Log logger) {
		this.logger = logger;
		return this;
	}
	
	/**
	 * <p>scopeName.</p>
	 *
	 * @param scopeName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.audit.helper.StopWatchSupport} object
	 */
	public StopWatchSupport scopeName(String scopeName) {
		this.scopeName = scopeName;
		return this;
	}
	
	/**
	 * <p>level.</p>
	 *
	 * @param level a {@link java.lang.System.Logger.Level} object
	 * @return a {@link fr.becpg.repo.audit.helper.StopWatchSupport} object
	 */
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
	
	/**
	 * <p>addCheckpoint.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public static void addCheckpoint(String name) {
		StopWatchScope currentScope = threadLocalScope.get();
		if (currentScope != null) {
			currentScope.addCheckpoint(name);
		}
	}

}
