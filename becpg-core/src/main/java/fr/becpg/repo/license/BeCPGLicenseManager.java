package fr.becpg.repo.license;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.RepoService;

/**
 * <p>BeCPGLicenseManager class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("becpgLicenseManager")
@DependsOn
public class BeCPGLicenseManager {

	/** Constant <code>INVALID_LICENSE_FILE="Invalid license file"</code> */
	private static final String INVALID_LICENSE_FILE = "Invalid license file";

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(BeCPGLicenseManager.class);

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private AbstractAuthenticationService authenticationService;

	@Autowired
	private AuthorityService authorityService;
	
	@Value("${beCPG.licence.showWarning:true}")
	private boolean showLicenseWarning;

	/**
	 * <p>isShowUnauthorizedWarning.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isShowLicenseWarning() {
		return showLicenseWarning;
	}
	
	/**
	 * <p>getAllowedConcurrentRead.</p>
	 *
	 * @return a long.
	 */
	public long getAllowedConcurrentRead() {
		return getLicense().allowedConcurrentRead;
	}

	/**
	 * <p>getLicense.</p>
	 *
	 * @return a {@link fr.becpg.repo.license.BeCPGLicense} object
	 */
	private BeCPGLicense getLicense() {
		return beCPGCacheService.getFromCache(BeCPGLicenseManager.class.getName(), "license", () -> {
			BeCPGLicense ret = new BeCPGLicense();

			JSONObject licenseObj = getLicenseFile();
			if (licenseObj != null) {
				boolean valid = false;
				if (licenseObj.has("LicenseKey")) {
					try {
						String licenseKey = licenseObj.getString("LicenseKey");

						ret.licenseName = licenseObj.has("LicenseName") ? licenseObj.getString("LicenseName") : ret.licenseName;
						ret.allowedNamedRead = licenseObj.has(SystemGroup.LicenseReadNamed.toString())
								? licenseObj.getLong(SystemGroup.LicenseReadNamed.toString())
								: -1L;
						ret.allowedNamedWrite = licenseObj.has(SystemGroup.LicenseWriteNamed.toString())
								? licenseObj.getLong(SystemGroup.LicenseWriteNamed.toString())
								: -1L;
						ret.allowedConcurrentRead = licenseObj.has(SystemGroup.LicenseReadConcurrent.toString())
								? licenseObj.getLong(SystemGroup.LicenseReadConcurrent.toString())
								: -1L;
						ret.allowedConcurrentWrite = licenseObj.has(SystemGroup.LicenseWriteConcurrent.toString())
								? licenseObj.getLong(SystemGroup.LicenseWriteConcurrent.toString())
								: -1L;
						ret.allowedConcurrentSupplier = licenseObj.has(SystemGroup.LicenseSupplierConcurrent.toString())
								? licenseObj.getLong(SystemGroup.LicenseSupplierConcurrent.toString())
								: -1L;

						valid = isValid(licenseKey, ret);

					} catch (JSONException e) {
						logger.error(e);
					}

				}

				if (!valid) {
					ret.licenseName = INVALID_LICENSE_FILE;
					ret.allowedNamedRead = 0;
					ret.allowedNamedWrite = 0;
					ret.allowedConcurrentRead = 0;
					ret.allowedConcurrentWrite = 0;
					ret.allowedConcurrentSupplier = 0;
				}
			}

			return ret;

		});
	}

	/**
	 * <p>getAllowedConcurrentWrite.</p>
	 *
	 * @return a long.
	 */
	public long getAllowedConcurrentWrite() {
		return getLicense().allowedConcurrentWrite;
	}

	/**
	 * <p>getAllowedConcurrentSupplier.</p>
	 *
	 * @return a long.
	 */
	public long getAllowedConcurrentSupplier() {
		return getLicense().allowedConcurrentSupplier;
	}

	/**
	 * <p>getAllowedNamedWrite.</p>
	 *
	 * @return a long.
	 */
	public long getAllowedNamedWrite() {
		return getLicense().allowedNamedWrite;
	}

	/**
	 * <p>getAllowedNamedRead.</p>
	 *
	 * @return a long.
	 */
	public long getAllowedNamedRead() {
		return getLicense().allowedNamedRead;
	}

	/**
	 * <p>getLicenseName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLicenseName() {
		return getLicense().licenseName;
	}
	
	/**
	 * <p>isLicenseValid.</p>
	 *
	 * @return a boolean
	 */
	public boolean isLicenseValid() {
		return getLicense() != null && !INVALID_LICENSE_FILE.equals(getLicenseName()) && !namedLicenseExceeded();
	}


	/**
	 * <p>namedLicenseExceeded.</p>
	 *
	 * @return a boolean
	 */
	private boolean namedLicenseExceeded() {
		return (countLicensedMembers(SystemGroup.LicenseReadNamed) > getLicense().allowedNamedRead)
				|| (countLicensedMembers(SystemGroup.LicenseWriteNamed) > getLicense().allowedNamedWrite);
	}

	/**
	 * Counts the members of a license group, sub-groups included, that consume a license.
	 *
	 * @param licenseGroup the license group to count
	 * @return the number of members, special license users excluded
	 */
	public long countLicensedMembers(SystemGroup licenseGroup) {
		Set<String> members = AuthorityHelper.extractPeople(PermissionService.GROUP_PREFIX + licenseGroup);
		members.removeIf(this::isSpecialLicenseUser);
		return members.size();
	}

	/**
	 * Counts the given users by the single license each of them consumes.
	 *
	 * @param userNames the users to count, typically the connected ones
	 * @return the number of users per license, licenses without any user omitted
	 */
	public Map<UserLicense, Long> countUsersByLicense(Collection<String> userNames) {
		Map<UserLicense, Long> counts = new EnumMap<>(UserLicense.class);
		for (String userName : userNames) {
			counts.merge(getUserLicense(userName), 1L, Long::sum);
		}
		return counts;
	}

	/**
	 * <p>isValid.</p>
	 *
	 * @param licenseKey a {@link java.lang.String} object.
	 * @param license a {@link fr.becpg.repo.license.BeCPGLicense} object.
	 * @return a boolean.
	 */
	public static boolean isValid(String licenseKey, BeCPGLicense license) {

		String computedKey = computeLicenseKey(license);

		boolean ret = licenseKey.trim().equalsIgnoreCase(computedKey.trim());

		if (!ret) {
			logger.error("License key do not match: " + licenseKey + "/" + computedKey);
			logger.warn("For: " + license.toString());
		}

		return ret;
	}

	/**
	 * <p>computeLicenseKey.</p>
	 *
	 * @param license a {@link fr.becpg.repo.license.BeCPGLicense} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String computeLicenseKey(BeCPGLicense license) {

		String key = license.licenseName + license.allowedNamedRead + license.allowedNamedWrite + license.allowedConcurrentRead
				+ license.allowedConcurrentWrite + license.allowedConcurrentSupplier;

		return java.util.Base64.getEncoder().encodeToString(key.getBytes());
	}

	/**
	 * <p>getLicenseFile.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
	private JSONObject getLicenseFile() {
		try {

			NodeRef licenseProfilesFolderNodeRef = repoService
					.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseProfilesFolderNodeRef != null) {
				NodeRef licenseFileNodeRef = nodeService.getChildByName(licenseProfilesFolderNodeRef, ContentModel.ASSOC_CONTAINS, "license.json");

				if (licenseFileNodeRef != null) {

					ContentReader reader = contentService.getReader(licenseFileNodeRef, ContentModel.PROP_CONTENT);
					if(reader!=null) {
						String content = reader.getContentString();
						return new JSONObject(content);
					} else {
						logger.info("Empty license file installed");
					}
				} else {
					logger.info("No beCPG license file installed");
				}
			} else {
				logger.warn("No beCPG license folder, run init-repo");
			}

		} catch (JSONException e) {
			logger.error("Unable to serialize JSON", e);
		}

		return null;

	}
	
	private final Map<UserLicense, Set<String>> concurrentUsersAllowedMap = new ConcurrentHashMap<>();
	
	/**
	 * Tells whether the current user may connect, consuming a floating token when their license is a concurrent one.
	 * A user that also holds a named license never consumes a floating token.
	 *
	 * @return false when all the floating tokens of the user's license are taken
	 */
	public boolean isConcurrentUserAllowed() {
		String currentUser = AuthenticationUtil.getRunAsUser();
		UserLicense userLicense = getUserLicense(currentUser);
		if (!userLicense.isConcurrent()) {
			return true;
		}
		return acquireConcurrentToken(currentUser, userLicense);
	}

	private boolean acquireConcurrentToken(String userName, UserLicense userLicense) {
		Set<String> tokenHolders = concurrentUsersAllowedMap.computeIfAbsent(userLicense, k -> ConcurrentHashMap.newKeySet());
		if (tokenHolders.contains(userName)) {
			return true;
		}
		Set<String> connectedUsers = authenticationService.getUsersWithTickets(true);
		synchronized (tokenHolders) {
			tokenHolders.removeIf(u -> !connectedUsers.contains(u));
			if (tokenHolders.size() >= getAllowedConcurrent(userLicense)) {
				return false;
			}
			tokenHolders.add(userName);
			return true;
		}
	}

	private long getAllowedConcurrent(UserLicense userLicense) {
		return switch (userLicense) {
		case READ_CONCURRENT -> getAllowedConcurrentRead();
		case WRITE_CONCURRENT -> getAllowedConcurrentWrite();
		case SUPPLIER_CONCURRENT -> getAllowedConcurrentSupplier();
		default -> throw new IllegalArgumentException("Not a concurrent license: " + userLicense);
		};
	}

	/**
	 * <p>isSpecialLicenceUser.</p>
	 *
	 * @return a boolean
	 */
	public boolean isSpecialLicenceUser() {
		String runAsUser = AuthenticationUtil.getRunAsUser();
		return isSpecialLicenseUser(runAsUser);
	}

	/**
	 * <p>isSpecialLicenseUser.</p>
	 *
	 * @param runAsUser a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean isSpecialLicenseUser(String runAsUser) {
		if(authenticationService.getDefaultAdministratorUserNames().contains(runAsUser)) {
			return true;
		}
		if (runAsUser.equals("admin") || runAsUser.endsWith("@becpg.fr")
				|| runAsUser.startsWith("admin@") ) {
			return true;
		}
		
		return false;
	}

	/**
	 * Returns the single license a user consumes, the named licenses taking priority over the concurrent ones.
	 *
	 * @param username the user name, may be null
	 * @return the license of the user, {@link UserLicense#SPECIAL_USER} for admins and beCPG accounts
	 */
	public UserLicense getUserLicense(String username) {
		if (username == null) {
			return UserLicense.NO_LICENSE;
		}
		if (isSpecialLicenseUser(username)) {
			return UserLicense.SPECIAL_USER;
		}
		return UserLicense.resolve(authorityService.getAuthoritiesForUser(username));
	}
	
	/**
	 * <p>hasWriteLicense.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasWriteLicense() {
		String runAsUser = AuthenticationUtil.getRunAsUser();
		return beCPGCacheService.getFromCache(BeCPGLicenseManager.class.getName() + ".writeLicenses", runAsUser, () -> {
			if (!showLicenseWarning) {
				return true;
			}
			if(isSpecialLicenceUser()) {
				return true;
			}
			if (AuthorityHelper.hasGroupAuthority(runAsUser, SystemGroup.LicenseWriteNamed.toString())) {
				return true;
			}
			if (AuthorityHelper.hasGroupAuthority(runAsUser, SystemGroup.LicenseWriteConcurrent.toString())) {
				return true;
			}
			if (AuthorityHelper.hasGroupAuthority(runAsUser, SystemGroup.LicenseSupplierConcurrent.toString())) {
				return true;
			}
			return false;
		});
	}

}
