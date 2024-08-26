package fr.becpg.repo.authentication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.BasicPasswordGenerator;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.authentication.provider.IdentityServiceAccountProvider;
import fr.becpg.repo.mail.BeCPGMailService;

/**
 * <p>BeCPGUserAccountService class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class BeCPGUserAccountService {

	private static Log logger = LogFactory.getLog(BeCPGUserAccountService.class);

	/** Constant <code>PATH_SEPARATOR="\\/"</code> */
	private static final String PATH_SEPARATOR = "\\/";

	@Autowired
	private TenantAdminService tenantAdminService;

	@Autowired
	private PersonService personService;

	@Autowired
	private BeCPGMailService beCPGMailService;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private MutableAuthenticationService authenticationService;

	@Autowired
	private IdentityServiceAccountProvider identityServiceAccountProvider;
	
	@Autowired
	private MutableAuthenticationDao repositoryAuthenticationDao;

	@Autowired
	private NodeService nodeService;
	
	/**
	 * <p>getOrCreateUser.</p>
	 *
	 * @param userAccount a {@link fr.becpg.repo.authentication.BeCPGUserAccount} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getOrCreateUser(BeCPGUserAccount userAccount) {
		return AuthenticationUtil.runAsSystem(() -> {
			String userName = createTenantAware(userAccount.getUserName());
			userAccount.setUserName(userAccount.getUserName());
			NodeRef personNodeRef = null;

			if (personService.personExists(userName)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Reassign to an existing user");
				}

				personNodeRef = personService.getPerson(userName);

				//				Set<String> userGroups = authorityService.getContainingAuthorities(AuthorityType.GROUP, userName, false);
				//				userGroups.forEach(group -> {
				//					logger.info("Group: " + group + ", user: " + userName);
				//					if (!group.startsWith("GROUP_site_")) {
				//						authorityService.removeAuthority(group, userName);
				//					}
				//				});

			} else {

				if (logger.isDebugEnabled()) {
					logger.debug("Create external user: " + userName + " pwd: " + userAccount.getPassword());
				}

				Map<QName, Serializable> propMap = new HashMap<>();
				propMap.put(ContentModel.PROP_USERNAME, userAccount.getUserName());
				propMap.put(ContentModel.PROP_LASTNAME, userAccount.getLastName());
				propMap.put(ContentModel.PROP_FIRSTNAME, userAccount.getFirstName());
				propMap.put(ContentModel.PROP_EMAIL, userAccount.getEmail());
				propMap.putAll(userAccount.getExtraProps());
				
				if (propMap.containsKey(ContentModel.PROP_LASTNAME) && propMap.get(ContentModel.PROP_LASTNAME) == null) {
					propMap.put(ContentModel.PROP_LASTNAME, "");
				}

				personNodeRef = personService.createPerson(propMap);

				createAuthentication(userAccount);

				
				for (String authority : userAccount.getAuthorities()) {

					String[] grp = authority.split(PATH_SEPARATOR);
					String currGroup = null;
					for (String aGrp : grp) {
						String tmp = aGrp;
						if ((tmp != null) && !tmp.isEmpty()) {
							if (!authorityService.authorityExists(authorityService.getName(AuthorityType.GROUP, tmp))) {
								tmp = authorityService.createAuthority(AuthorityType.GROUP, tmp);
								logger.debug("Create group : " + tmp);
								if ((currGroup != null) && !currGroup.isEmpty()) {
									logger.debug("Add group  " + tmp + " to " + currGroup);
									authorityService.addAuthority(currGroup, tmp);

								}
							} else {
								tmp = authorityService.getName(AuthorityType.GROUP, tmp);
							}
							currGroup = tmp;
						}
					}

					if ((currGroup != null) && !currGroup.isEmpty()) {
						logger.debug("Add user  " + userName + " to " + currGroup);

						authorityService.addAuthority(currGroup, userName);
					}

				}

				// notify supplier
				if (Boolean.TRUE.equals(userAccount.getNotify())) {
					beCPGMailService.sendMailNewUser(personNodeRef, userName, userAccount.getPassword(), false);
				}

			}

			return personNodeRef;

		});

	}

	private void createAuthentication(BeCPGUserAccount userAccount) {
		if (Boolean.TRUE.equals(identityServiceAccountProvider.isEnabled())) {
			logger.debug("Create user in Identity Service");
			identityServiceAccountProvider.registerAccount(userAccount);
			Set<String> zones = authorityService.getAuthorityZones(userAccount.getUserName());

			if (zones == null) {
				zones = new HashSet<>();
			}

			if (!zones.contains(identityServiceAccountProvider.getZoneId())) {
				zones.add(identityServiceAccountProvider.getZoneId());
				authorityService.addAuthorityToZones(userAccount.getUserName(), zones);
			}

		} else {
			authenticationService.createAuthentication(userAccount.getUserName(), userAccount.getPassword().toCharArray());
		}

	}

	private String createTenantAware(String userName) {

		if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())
				&& !userName.endsWith(tenantAdminService.getCurrentUserDomain())) {
			userName += "@" + tenantAdminService.getCurrentUserDomain();
		}
		return userName;
	}

	public void synchronizeSso(String username) {
		if (Boolean.TRUE.equals(identityServiceAccountProvider.isEnabled())) {
			if (!personService.personExists(username)) {
				throw new IllegalStateException("user does not exist: " + username);
			}
			NodeRef personNodeRef = personService.getPerson(username);
			
			BasicPasswordGenerator pwdGen = new BasicPasswordGenerator();
			pwdGen.setPasswordLength(10);
			BeCPGUserAccount userAccount = new BeCPGUserAccount();
			userAccount.setEmail((String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL));
			userAccount.setUserName(username);
			userAccount.setFirstName((String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME));
			userAccount.setLastName((String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME));
			userAccount.setPassword(pwdGen.generatePassword());
			userAccount.setNotify(true);
			userAccount.getAuthorities().addAll(authorityService.getAuthoritiesForUser(username).stream().map(s -> authorityService.getShortName(s)).toList());
			Map<QName, Serializable> extraProps = new HashMap<>();
			extraProps.put(ContentModel.PROP_EMAIL_FEED_DISABLED, true);
			userAccount.getExtraProps().putAll(extraProps);
			
			if (identityServiceAccountProvider.registerAccount(userAccount)) {
				logger.debug("user account successfully registered by identity service: " + userAccount.getUserName());
				beCPGMailService.sendMailNewUser(personNodeRef, username, userAccount.getPassword(), false);
			} else {
				logger.debug("user account already registered by identity service: " + userAccount.getUserName());
			}
			
			if (repositoryAuthenticationDao.userExists(username)) {
				repositoryAuthenticationDao.deleteUser(username);
				logger.debug("user account successfully deleted by authentication repository: " + userAccount.getUserName());
			}
		} else {
			logger.warn("identity service is not enabled");
		}
	}

}
