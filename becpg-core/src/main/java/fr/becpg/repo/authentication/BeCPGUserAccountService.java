package fr.becpg.repo.authentication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.BasicPasswordGenerator;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
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
	
	@Autowired
	private BehaviourFilter policyBehaviourFilter;
	
	@Autowired
	private PreferenceService preferenceService;

	/**
	 * <p>getOrCreateUser.</p>
	 *
	 * @param userAccount a {@link fr.becpg.repo.authentication.BeCPGUserAccount} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getOrCreateUser(BeCPGUserAccount userAccount) {
		return AuthenticationUtil.runAsSystem(() -> {
			userAccount.setUserName(createTenantAware(userAccount.getUserName()));
			NodeRef personNodeRef = null;

			Map<QName, Serializable> propMap = new HashMap<>();
			propMap.put(ContentModel.PROP_LASTNAME, userAccount.getLastName());
			propMap.put(ContentModel.PROP_FIRSTNAME, userAccount.getFirstName());
			propMap.put(ContentModel.PROP_EMAIL, userAccount.getEmail());
			propMap.putAll(userAccount.getExtraProps());

			if (personService.personExists(userAccount.getUserName())) {
				personNodeRef = updateUser(userAccount, propMap);
			} else {
				personNodeRef = createUser(userAccount, propMap);
			}
			
			updateGroups(userAccount);

			if (Boolean.TRUE.equals(userAccount.getSynchronizeWithIDS())) {
				nodeService.setProperty(personNodeRef, BeCPGModel.PROP_IS_SSO_USER, true);
			}
			if (Boolean.TRUE.equals(userAccount.getGeneratePassword())) {
				boolean shouldNotify = !Boolean.FALSE.equals(userAccount.getNotify());
				generatePassword((String) nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME), shouldNotify);
			}
			if (Boolean.TRUE.equals(userAccount.getDisable())) {
				if (authenticationService.isAuthenticationMutable(userAccount.getUserName())) {
					this.authenticationService.setAuthenticationEnabled(userAccount.getUserName(), false);
				} else {
					nodeService.addAspect(personService.getPerson(userAccount.getUserName()), ContentModel.ASPECT_PERSON_DISABLED, null);
				}
			}

			return personNodeRef;

		});

	}

	private NodeRef createUser(BeCPGUserAccount userAccount, Map<QName, Serializable> propMap) {
		NodeRef personNodeRef;
		if (logger.isDebugEnabled()) {
			logger.debug("Create external user: " + userAccount.getUserName() + " pwd: " + userAccount.getPassword());
		}
		propMap.put(ContentModel.PROP_USERNAME, userAccount.getUserName());
		if (propMap.containsKey(ContentModel.PROP_LASTNAME) && propMap.get(ContentModel.PROP_LASTNAME) == null) {
			propMap.put(ContentModel.PROP_LASTNAME, "");
		}
		personNodeRef = personService.createPerson(propMap);
		createAuthentication(userAccount, personNodeRef);

		// notify supplier
		if (Boolean.TRUE.equals(userAccount.getNotify())) {
			beCPGMailService.sendMailNewUser(personNodeRef, userAccount.getUserName(), userAccount.getPassword());
		}
		return personNodeRef;
	}

	private void updateGroups(BeCPGUserAccount userAccount) {
		for (String authority : userAccount.getAuthorities()) {
			if (authority.endsWith("_Remove")) {
				authorityService.removeAuthority(authority.replace("_Remove", ""), userAccount.getUserName());
			} else {
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
				
				if ((currGroup != null) && !currGroup.isEmpty()
						&& !authorityService.getAuthoritiesForUser(userAccount.getUserName()).contains(currGroup)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Add user  " + userAccount.getUserName() + " to " + currGroup);
					}
					authorityService.addAuthority(currGroup, userAccount.getUserName());
				}
			}
		}
	}
	
	private NodeRef updateUser(BeCPGUserAccount userAccount, Map<QName, Serializable> propMap) {
		if (logger.isDebugEnabled()) {
			logger.debug("Update an existing user");
		}
		String userName = userAccount.getUserName();
		NodeRef personNodeRef = personService.getPerson(userName);
		if (userAccount.getNewUserName() != null && !userAccount.getNewUserName().isBlank()) {
			renameUser(userAccount, personNodeRef);
		}
		
		nodeService.addProperties(personNodeRef, propMap);
		
		return personNodeRef;
	}

	private void renameUser(BeCPGUserAccount userAccount, NodeRef personNodeRef) {
		String newUserName = createTenantAware(userAccount.getNewUserName());
		if (!newUserName.equals(userAccount.getUserName())) {
			userAccount.setUserName(newUserName);
			TransactionSupportUtil.bindResource(PersonServiceImpl.KEY_ALLOW_UID_UPDATE, Boolean.TRUE);
			nodeService.setProperty(personNodeRef, ContentModel.PROP_USERNAME, newUserName);
			nodeService.setProperty(personNodeRef, ContentModel.PROP_OWNER, newUserName);
			preferenceService.clearPreferences(newUserName);
			NodeRef homeFolder = (NodeRef) nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
			if (homeFolder != null) {
				nodeService.setProperty(homeFolder, ContentModel.PROP_NAME, newUserName);
			}
		}
	}

	private void createAuthentication(BeCPGUserAccount userAccount, NodeRef personNodeRef) {
		if (Boolean.TRUE.equals(identityServiceAccountProvider.isEnabled())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Create user in Identity Service");
			}
			identityServiceAccountProvider.registerAccount(userAccount);
			addAuthorityToIdsZone(userAccount.getUserName());
			try {
				policyBehaviourFilter.disableBehaviour(personNodeRef);
				nodeService.setProperty(personNodeRef, BeCPGModel.PROP_IS_SSO_USER, true);
			} finally {
				policyBehaviourFilter.enableBehaviour(personNodeRef);
			}
		} else {
			authenticationService.createAuthentication(userAccount.getUserName(), userAccount.getPassword().toCharArray());
		}
	}

	private void addAuthorityToIdsZone(String authority) {
		Set<String> zones = authorityService.getAuthorityZones(authority);
		String zoneId = identityServiceAccountProvider.getZoneId();
		if (zones == null || !zones.contains(zoneId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("add authority '" + authority + "' to zone '" + zoneId + "'");
			}
			authorityService.addAuthorityToZones(authority, Set.of(zoneId));
		}
	}

	private String createTenantAware(String userName) {

		if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())
				&& !userName.endsWith(tenantAdminService.getCurrentUserDomain())) {
			userName += "@" + tenantAdminService.getCurrentUserDomain();
		}
		return userName;
	}

	/**
	 * <p>synchronizeSsoUser.</p>
	 *
	 * @param username a {@link java.lang.String} object
	 */
	public void synchronizeWithIDS(String username) {
		if (Boolean.TRUE.equals(identityServiceAccountProvider.isEnabled())) {
			if (!personService.personExists(username)) {
				throw new IllegalStateException("user does not exist: " + username);
			}
			NodeRef personNodeRef = personService.getPerson(username);
			BeCPGUserAccount userAccount = new BeCPGUserAccount();
			userAccount.setEmail((String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL));
			userAccount.setUserName(username);
			userAccount.setFirstName((String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME));
			userAccount.setLastName((String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME));
			userAccount.getAuthorities().addAll(authorityService.getAuthoritiesForUser(username).stream().map(s -> authorityService.getShortName(s)).toList());
			Map<QName, Serializable> extraProps = new HashMap<>();
			extraProps.put(ContentModel.PROP_EMAIL_FEED_DISABLED, true);
			userAccount.getExtraProps().putAll(extraProps);
			if (identityServiceAccountProvider.registerAccount(userAccount)) {
				if (logger.isDebugEnabled()) {
					logger.debug("user '" + username + "' was successfully registered by identity service");
				}
			} else if (logger.isDebugEnabled()) {
				logger.debug("user '" + username + "' already exists in identity service");
			}
			addAuthorityToIdsZone(username);
			if (repositoryAuthenticationDao.userExists(username)) {
				repositoryAuthenticationDao.deleteUser(username);
				if (logger.isDebugEnabled()) {
					logger.debug("user '" + username + "' successfully deleted in authentication repository");
				}
			}
		} else {
			logger.warn("identity service is not enabled");
		}
	}

	public void generatePassword(String username, boolean notify) {
		if (!personService.personExists(username)) {
			throw new IllegalStateException("user does not exist: " + username);
		}
		BasicPasswordGenerator pwdGen = new BasicPasswordGenerator();
		pwdGen.setPasswordLength(10);
		String newPassword = pwdGen.generatePassword();
		boolean updateSuccess = false;
		if (Boolean.TRUE.equals(identityServiceAccountProvider.isEnabled())) {
			updateSuccess = identityServiceAccountProvider.generatePassword(username, newPassword);
		} else {
			repositoryAuthenticationDao.updateUser(username, newPassword.toCharArray());
			updateSuccess = true;
		}
		if (updateSuccess && notify) {
			beCPGMailService.sendMailNewPassword(personService.getPerson(username), username, newPassword);
		}
	}

}
