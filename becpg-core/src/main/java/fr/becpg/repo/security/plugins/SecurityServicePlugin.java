package fr.becpg.repo.security.plugins;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>SecurityServicePlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SecurityServicePlugin {

	/**
	 * <p>checkIsInSecurityGroup.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 * @param groups a {@link java.util.List} object
	 */
	boolean checkIsInSecurityGroup(NodeRef nodeRef,  List<NodeRef> groups);

	/**
	 * <p>accept.</p>
	 *
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object
	 * @return a boolean
	 */
	boolean accept(QName nodeType);

	/**
	 * <p>computeAccessMode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param accesMode a int
	 * @return a int
	 */
	int computeAccessMode(NodeRef nodeRef, int accesMode);

}
