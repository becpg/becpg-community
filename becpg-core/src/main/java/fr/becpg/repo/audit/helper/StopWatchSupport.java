package fr.becpg.repo.audit.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.service.StopWatchScope;

public class StopWatchSupport {
	
	private static final ThreadLocal<StopWatchScope> threadLocalScope = new ThreadLocal<>();

	private static final Log logger = LogFactory.getLog(StopWatchSupport.class);

	private StopWatchSupport() {
		
	}
	
	public interface Action<T> {
		T run();
	}
	
	public static <T> T stopWatch(Action<T> action) {
		return stopWatch(action, null);
	}
	
	public static <T> T stopWatch(Action<T> action, Object scope) {
		
		StackTraceElement[] currentStackTrace = Thread.currentThread().getStackTrace();
		
		if (currentStackTrace != null && currentStackTrace.length > 2) {
			
			String scopeName = currentStackTrace[2].getMethodName() + (scope != null ? " - " + scope : "");
			
			try {
				return stopWatch(action, Class.forName(currentStackTrace[2].getClassName()), scopeName);
			} catch (ClassNotFoundException e) {
				logger.error("Class could not be found: " + currentStackTrace[2].getClassName());
			}
		}
		
		return action.run();
	}
	
	public static <T> T stopWatch(Action<T> action, Class<?> clazz, String scopeName) {
		StopWatchScope parentScope = threadLocalScope.get();
		try (StopWatchScope scope = new StopWatchScope(scopeName, LogFactory.getLog(clazz))) {
			threadLocalScope.set(scope);
			scope.start();
			return action.run();
		} finally {
			threadLocalScope.remove();
			if (parentScope != null) {
				threadLocalScope.set(parentScope);
			}
		}
	}
	
	public static void addCheckpoint(String name) {
		StopWatchScope currentScope = threadLocalScope.get();
		if (currentScope != null) {
			currentScope.addCheckpoint(name);
		}
	}

}
