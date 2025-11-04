package fr.becpg.repo.security.jscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.jscript.People;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.util.ScriptPagingDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import fr.becpg.model.SystemGroup;

/**
 * <p>BeCPGPeople class.</p>
 *
 * @author matthieu
 */
public class BeCPGPeople extends People {

	private static final String SUPPLIER_GROUP_PREFIX = "EXTERNAL_SUPPLIER_";

	private static final Log logger = LogFactory.getLog(BeCPGPeople.class);

	private AuthorityService authorityService;

	/** {@inheritDoc} */
	@Override
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
		super.setAuthorityService(authorityService);
	}

	/** {@inheritDoc} */
	@Override
	public Scriptable getPeoplePaging(String filter, ScriptPagingDetails pagingRequest, String sortBy, Boolean sortAsc) {
		List<PersonInfo> peopleImpl = getPeopleImpl(filter, pagingRequest, sortBy, sortAsc);
		List<PersonInfo> persons = filter(peopleImpl);
		Object[] peopleRefs = new Object[persons.size()];
		for (int i = 0; i < peopleRefs.length; i++) {
			peopleRefs[i] = persons.get(i).getNodeRef();
		}
		return Context.getCurrentContext().newArray(getScope(), peopleRefs);
	}

	private List<PersonInfo> filter(List<PersonInfo> peopleImpl) {
		String currentUser = AuthenticationUtil.getRunAsUser();
		Set<String> authoritiesForUser = authorityService.getAuthoritiesForUser(currentUser);
		if (containsSupplierGroup(authoritiesForUser)) {
			logger.debug("filter people as user is supplier: " + currentUser);
			List<PersonInfo> filteredPeople = new ArrayList<>();
			for (String currAuth : authoritiesForUser) {
				if (currAuth.startsWith(PermissionService.GROUP_PREFIX + SUPPLIER_GROUP_PREFIX)) {
					Set<String> supplierGroupUsers = authorityService.getContainedAuthorities(AuthorityType.USER, currAuth, false);
					List<PersonInfo> subList = peopleImpl.stream().filter(p -> supplierGroupUsers.contains(p.getUserName())).toList();
					logger.debug("retain '" + supplierGroupUsers + "' from group '" + currAuth + "'");
					filteredPeople.addAll(subList);
				}
			}
			logger.debug("final filtered people :" + filteredPeople);
			return filteredPeople;
		}
		return peopleImpl;
	}

	private boolean containsSupplierGroup(Set<String> authoritiesForUser) {
		for (String currAuth : authoritiesForUser) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)
					|| currAuth.startsWith(PermissionService.GROUP_PREFIX + SUPPLIER_GROUP_PREFIX)
					|| currAuth.startsWith(PermissionService.GROUP_PREFIX + SystemGroup.LicenseSupplierConcurrent.toString())) {
				return true;
			}
		}
		return false;
	}

}
