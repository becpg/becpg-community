package fr.becpg.repo.audit.helper;

import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.service.StopWatchScope;

public class StopWatchSupport {
	
	private static final ThreadLocal<StopWatchScope> threadLocalScope = new ThreadLocal<>();

	private StopWatchSupport() {
		
	}
	
	public interface Action<T> {
		T run();
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
