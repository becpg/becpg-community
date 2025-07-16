package fr.becpg.repo.security.plugins;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>DefaultSecurityServicePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DefaultSecurityServicePlugin implements SecurityServicePlugin {

	@Autowired
	private AuthorityService authorityService;

	/** {@inheritDoc} */
	@Override
	public boolean checkIsInSecurityGroup(NodeRef nodeRef, List<NodeRef> groups) {

		for (String currAuth : authorityService.getAuthorities()) {
			if (groups.contains(authorityService.getAuthorityNodeRef(currAuth))) {
				return true;
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(QName nodeType) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public int computeAccessMode(NodeRef nodeRef, int accesMode) {
		return accesMode;
	}

}
