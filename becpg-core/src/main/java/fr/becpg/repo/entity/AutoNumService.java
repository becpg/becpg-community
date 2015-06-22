package fr.becpg.repo.entity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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
	String getAutoNumValue(QName className, QName propertyName);
	
	/**
	 * Decrease auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @return the Long
	 */
	String decreaseAutoNumValue(QName className, QName propertyName);
	
	/**
	 * Creates the or update auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @param code the value
	 * @return the prefixed code value
	 */
	String createOrUpdateAutoNumValue(QName className, QName propertyName, String code);
	
	/**
	 * Delete auto num value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 */
	void deleteAutoNumValue(QName className, QName propertyName);
	
	
	/**
	 * Return the pattern string for the corresponding code
	 * @param type
	 * @param propertyName
	 * @return
	 */
	String getAutoNumMatchPattern(QName type, QName propertyName);
	
	
	/**
	 * Return the prefixed String for the given type
	 * @param type
	 * @param propertyName
	 * @param autoNum
	 * @return 
	 */
	String getPrefixedCode(QName type, QName propertyName, Long autoNum);

	
	String getOrCreateBeCPGCode(NodeRef nodeRef);
}
