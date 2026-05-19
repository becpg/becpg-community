/*
 * 
 */
package fr.becpg.repo.security.filter;

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
	 * Clear all thread local values
	 */
	public static void clear() {
		skipSecurityRules.remove();
	}
}
