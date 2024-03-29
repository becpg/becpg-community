package fr.becpg.repo.entity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The Interface AutoNumService.
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface AutoNumService {

	/**
	 * <p>getOrCreateCode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param codeQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getOrCreateCode(NodeRef nodeRef, QName codeQName);

	/**
	 * <p>getOrCreateBeCPGCode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
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
	 * @param className
	 * @param propertyName
	 * @param counter
	 * @return
	 */
	boolean setAutoNumValue(QName className, QName propertyName, Long counter);

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
	 * <p>getAutoNumMatchPattern.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param propertyName a {@link org.alfresco.service.namespace.QName} object.
	 * @return the pattern string for the corresponding code
	 */
	String getAutoNumMatchPattern(QName type, QName propertyName);

	/**
	 * <p>getPrefixedCode.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param propertyName a {@link org.alfresco.service.namespace.QName} object.
	 * @param autoNum a {@link java.lang.Long} object.
	 * @return the prefixed String for the given type
	 */
	String getPrefixedCode(QName type, QName propertyName, Long autoNum);

	/**
	 * <p>getAutoNumNodeRef.</p>
	 * 
	 * @param className
	 * @param propertyName
	 * @return the NodeRef of the AutoNum value
	 */
	NodeRef getAutoNumNodeRef(final QName className, final QName propertyName);

}
