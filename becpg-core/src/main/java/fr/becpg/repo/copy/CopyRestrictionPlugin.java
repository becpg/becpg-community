package fr.becpg.repo.copy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>CopyRestrictionPlugin interface.</p>
 *
 * @author matthieu
 */
public interface CopyRestrictionPlugin {

	/**
	 * <p>shouldCopy.</p>
	 *
	 * @param sourceClassQName a {@link org.alfresco.service.namespace.QName} object
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param typeToReset a {@link java.lang.String} object
	 * @return a boolean
	 */
	boolean shouldCopy(QName sourceClassQName, NodeRef sourceNodeRef, NodeRef targetNodeRef, String typeToReset);

}
