/*
 * 
 */
package fr.becpg.repo.entity;

import org.alfresco.service.namespace.QName;

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
	public Long getAutoNumValue(QName className, QName propertyName);
	
	/**
	 * Decrease auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @return the Long
	 */
	public Long decreaseAutoNumValue(QName className, QName propertyName);
	
	/**
	 * Creates the or update auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @param value the value
	 */
	public void createOrUpdateAutoNumValue(QName className, QName propertyName, Long value);
	
	/**
	 * Delete auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 */
	public void deleteAutoNumValue(QName className, QName propertyName);
}
