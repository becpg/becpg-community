package fr.becpg.repo.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Objects;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemGroup;

/**
 * <p>AuthorityHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class AuthorityHelper implements InitializingBean {
	
	@Autowired
	private AuthorityService authorityService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private AssociationService associationService;
	
	private static AuthorityHelper instance = null;
	
	private AuthorityHelper() {
		//Singleton
	}
	
	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;	
	}

	/**
	 * <p>extractPeople.</p>
	 *
	 * @param authorities a {@link java.util.Set} object
	 * @return a {@link java.util.Set} object
	 */
	public static Set<String> extractPeople(Set<String> authorities) {
		Set<String> people = new HashSet<>();
		for (String authority : authorities) {
			people.addAll(extractPeople(authority));
		}
		return people;
	}
	
	@SuppressWarnings("deprecation")
	public static Set<String> extractPeople(String authority) {
		Set<String> people = new HashSet<>();
		
		AuthorityType authType = AuthorityType.getAuthorityType(authority);
		
		if (authType.equals(AuthorityType.GROUP) || authType.equals(AuthorityType.EVERYONE)) {
			// Notify all members of the group
			Set<String> users;
			if (authType.equals(AuthorityType.GROUP)) {
				users = instance.authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
			} else {
				users = instance.authorityService.getAllAuthorities(AuthorityType.USER);
			}
			
			people.addAll(users);
			
		} else {
			people.add(authority);
		}
		return people;
	}
	
	/**
	 * <p>extractPeople.</p>
	 *
	 * @param nodeRefs a {@link java.util.List} object
	 * @return a {@link java.util.List} object
	 */
	public static List<NodeRef> extractPeople(List<NodeRef> nodeRefs) {
		List<NodeRef> people = new ArrayList<>();

		for (NodeRef nodeRef : nodeRefs) {
			QName type = instance.nodeService.getType(nodeRef);

			if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)) {
				List<NodeRef> members = instance.associationService.getChildAssocs(nodeRef, ContentModel.ASSOC_MEMBER);
				people.addAll(members);
			} else {
				people.add(nodeRef);
			}
		}
		return people;
	}

	/**
	 * <p>getCommonLocale.</p>
	 *
	 * @param people a {@link java.util.Set} object
	 * @return a {@link java.util.Locale} object
	 */
	public static Locale getCommonLocale(Set<String> people) {
		
		Locale commonLocale = null;
		
		boolean isFirst = true;
		
		for (String person : people) {
			if (instance.personService.personExists(person)) {
				
				String localeString = (String) instance.nodeService.getProperty(instance.personService.getPerson(person), BeCPGModel.PROP_USER_LOCALE);
				
				Locale personLocale = null;
				
				if (localeString != null) {
					personLocale = MLTextHelper.parseLocale(localeString);
				}
					
				if (isFirst) {
					commonLocale = personLocale;
					isFirst = false;
				} else if (!Objects.equal(personLocale, commonLocale)) {
					return null;
				}
			}
		}
		
		return commonLocale;
	}
	
	/**
	 * <p>extractAuthoritiesFromGroup.</p>
	 *
	 * @param group a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param includeCurrentUser a boolean
	 * @return a {@link java.util.List} object
	 */
	public static List<String> extractAuthoritiesFromGroup(NodeRef group, boolean includeCurrentUser) {
		List<String> ret = new ArrayList<>();
		String authorityName = (String) instance.nodeService.getProperty(group, ContentModel.PROP_AUTHORITY_NAME);
		for (String userAuth : instance.authorityService.getContainedAuthorities(AuthorityType.USER, authorityName, false)) {
			if (includeCurrentUser || !userAuth.equals(AuthenticationUtil.getFullyAuthenticatedUser())) {
				ret.add(userAuth);
			}
		}

		return ret;
	}
	
	/**
	 * <p>isCurrentUserExternal.</p>
	 *
	 * @return a boolean
	 */
	public static boolean isCurrentUserExternal() {
		for (String currAuth : instance.authorityService.getAuthorities()) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>isExternalUser.</p>
	 *
	 * @param userName a {@link java.lang.String} object
	 * @return a boolean
	 */
	public static boolean isExternalUser(String userName) {
		for (String currAuth : instance.authorityService.getAuthoritiesForUser(userName)) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasGroupAuthority(String userName, String groupAuthority) {
		for (String currAuth : instance.authorityService.getAuthoritiesForUser(userName)) {
			if ((PermissionService.GROUP_PREFIX + groupAuthority).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}
	
}
