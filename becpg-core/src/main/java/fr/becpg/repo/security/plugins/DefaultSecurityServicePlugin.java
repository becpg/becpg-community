package fr.becpg.repo.security.plugins;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.security.data.dataList.ACLEntryDataItem.PermissionModel;

@Service
public class DefaultSecurityServicePlugin implements SecurityServicePlugin {

	@Autowired
	private AuthorityService authorityService;

	@Override
	public boolean checkIsInSecurityGroup(NodeRef nodeRef, PermissionModel permissionModel) {

		for (String currAuth : authorityService.getAuthorities()) {
			if (permissionModel.getGroups().contains(authorityService.getAuthorityNodeRef(currAuth))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean accept(QName nodeType) {
		return true;
	}

}
