/*
 * 
 */
package fr.becpg.repo.policy;

// TODO: Auto-generated Javadoc
/**
 * The Interface AutoNumService.
 *
 * @author querephi
 */
public interface AutoNumService {

	/**
	 * Gets the auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @return the auto num value
	 */
	public long getAutoNumValue(String className, String propertyName);
	
	/**
	 * Decrease auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @return the long
	 */
	public long decreaseAutoNumValue(String className, String propertyName);
	
	/**
	 * Creates the or update auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @param value the value
	 */
	public void createOrUpdateAutoNumValue(String className, String propertyName, long value);
	
	/**
	 * Delete auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 */
	public void deleteAutoNumValue(String className, String propertyName);
}
