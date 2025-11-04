package fr.becpg.repo.security.plugins;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.security.SecurityService;

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
	 * <p>getMaxAccessMode.</p>
	 * Returns the maximum access mode this plugin allows for the given node.
	 * This method should return the plugin's own access restriction without
	 * considering the input access mode.
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a int representing the maximum allowed access mode
	 */
	default int getMaxAccessMode(NodeRef nodeRef) {
		return SecurityService.WRITE_ACCESS;
	}

}
