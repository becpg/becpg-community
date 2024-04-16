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
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;	
	}

	@SuppressWarnings("deprecation")
	public static Set<String> extractPeople(Set<String> authorities) {
		Set<String> people = new HashSet<>();
		
		for (String authority : authorities) {
			
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
		}
		return people;
	}
	
	public static List<NodeRef> extractPeople(List<NodeRef> viewRecipients) {
		List<NodeRef> recipients = new ArrayList<>();

		for (NodeRef viewRecipient : viewRecipients) {
			QName type = instance.nodeService.getType(viewRecipient);

			if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)) {
				List<NodeRef> members = instance.associationService.getChildAssocs(viewRecipient, ContentModel.ASSOC_MEMBER);
				recipients.addAll(members);
			} else {
				recipients.add(viewRecipient);
			}
		}
		return recipients;
	}

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
	
	public static boolean isCurrentUserExternal() {
		for (String currAuth : instance.authorityService.getAuthorities()) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isExternalUser(String userName) {
		for (String currAuth : instance.authorityService.getAuthoritiesForUser(userName)) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}
	
}
