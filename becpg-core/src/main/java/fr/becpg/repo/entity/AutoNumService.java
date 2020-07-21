package fr.becpg.repo.entity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The Interface AutoNumService.
 *
 * @author querephi
 */
public interface AutoNumService {

	String getOrCreateCode(NodeRef nodeRef, QName codeQName);

	String getOrCreateBeCPGCode(NodeRef nodeRef);

	/**
	 * Gets the auto num value.
	 *
	 * @param className
	 *            the class name
	 * @param propertyName
	 *            the property name
	 * @return the auto num value
	 */
	String getAutoNumValue(QName className, QName propertyName);

	/**
	 * Delete auto num value.
	 *
	 * @param className
	 *            the class name
	 * @param propertyName
	 *            the property name
	 */
	void deleteAutoNumValue(QName className, QName propertyName);

	/**
	 * @param type
	 * @param propertyName
	 * @return the pattern string for the corresponding code
	 */
	String getAutoNumMatchPattern(QName type, QName propertyName);

	/**
	 * @param type
	 * @param propertyName
	 * @param autoNum
	 * @return the prefixed String for the given type
	 */
	String getPrefixedCode(QName type, QName propertyName, Long autoNum);

}
