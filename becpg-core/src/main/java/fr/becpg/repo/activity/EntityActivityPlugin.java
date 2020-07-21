package fr.becpg.repo.activity;

import org.alfresco.service.namespace.QName;

/**
 * <p>EntityActivityPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityActivityPlugin {

	/**
	 * <p>isMatchingStateProperty.</p>
	 *
	 * @param propName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isMatchingStateProperty(QName propName);

	/**
	 * <p>isMatchingEntityType.</p>
	 *
	 * @param entityName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isMatchingEntityType(QName entityName);

	/**
	 * <p>isIgnoreStateProperty.</p>
	 *
	 * @param propName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isIgnoreStateProperty(QName propName);
	
	
}
