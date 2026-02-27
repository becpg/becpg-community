/*
 * 
 */
package fr.becpg.repo.security.filter;

import javax.annotation.Nullable;

/**
 * Helper class to manage security context via ThreadLocal
 * Used to skip security rules in specific contexts like wizards
 *
 * @author matthieu
 */
public class SecurityContextHelper {

	private static final ThreadLocal<Boolean> skipSecurityRules = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static final ThreadLocal<Boolean> userHasAssignedTask = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return null; // null = not computed yet
		}
	};

	/**
	 * Set whether security rules should be skipped in current context
	 *
	 * @param skip true if security rules should be skipped
	 */
	public static void setSkipSecurityRules(boolean skip) {
		skipSecurityRules.set(skip);
	}

	/**
	 * Check if security rules should be skipped in current context
	 *
	 * @return true if security rules should be skipped
	 */
	public static boolean skipSecurityRules() {
		return skipSecurityRules.get();
	}

	/**
	 * Set whether user has assigned task (cached result)
	 *
	 * @param hasTask true if user has assigned task, false if not, null if not computed
	 */
	public static void setUserHasAssignedTask(Boolean hasTask) {
		userHasAssignedTask.set(hasTask);
	}

	/**
	 * Get cached result of whether user has assigned task
	 *
	 * @return true if user has assigned task, false if not, null if not computed yet
	 */
	@Nullable
	public static Boolean getUserHasAssignedTask() {
		return userHasAssignedTask.get();
	}

	/**
	 * Check if user has assigned task (with automatic caching)
	 *
	 * @return true if user has assigned task, false if not
	 */
	public static boolean checkUserHasAssignedTask() {
		Boolean cached = userHasAssignedTask.get();
		if (cached != null) {
			return cached;
		}
		return false; // default if not computed
	}

	/**
	 * Clear all thread local values
	 */
	public static void clear() {
		skipSecurityRules.remove();
		userHasAssignedTask.remove();
	}
}
