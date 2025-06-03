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
	 * @param permissionModel a {@link fr.becpg.repo.security.data.PermissionModel} object
	 * @return a boolean
	 */
	boolean checkIsInSecurityGroup(NodeRef nodeRef,  List<NodeRef> groups);

	/**
	 * <p>accept.</p>
	 *
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object
	 * @return a boolean
	 */
	boolean accept(QName nodeType);

	int computeAccessMode(NodeRef nodeRef, int accesMode);

}
