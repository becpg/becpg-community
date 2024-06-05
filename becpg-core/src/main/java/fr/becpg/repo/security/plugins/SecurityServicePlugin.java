package fr.becpg.repo.security.plugins;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.security.data.PermissionModel;

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
	boolean checkIsInSecurityGroup(NodeRef nodeRef, PermissionModel permissionModel);

	/**
	 * <p>accept.</p>
	 *
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object
	 * @return a boolean
	 */
	boolean accept(QName nodeType);

}
