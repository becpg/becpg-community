package fr.becpg.repo.security.plugins;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.security.data.PermissionModel;

public interface SecurityServicePlugin {

	boolean checkIsInSecurityGroup(NodeRef nodeRef, PermissionModel permissionModel);

	boolean accept(QName nodeType);

}
