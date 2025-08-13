package fr.becpg.repo.license;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

	private static final String INVALID_LICENSE_FILE = "Invalid license file";

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


	private boolean namedLicenseExceeded() {
		Set<String> namedReadList = AuthorityHelper.extractPeople(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadNamed.toString());
		namedReadList.removeIf(this::isSpecialLicenseUser);
		int namedRead = namedReadList.size();
		if (namedRead > getLicense().allowedNamedRead) {
			return true;
		}
		Set<String> namedWriteList = AuthorityHelper.extractPeople(PermissionService.GROUP_PREFIX + SystemGroup.LicenseWriteNamed.toString());
		int namedWrite = namedWriteList.size();
		namedWriteList.removeIf(this::isSpecialLicenseUser);
		if (namedWrite > getLicense().allowedNamedWrite) {
			return true;
		}
		return false;
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

	/**
	 * <p>floatingLicensesExceeded.</p>
	 *
	 * @param sessionId a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean floatingLicensesExceeded(String sessionId) {
		return beCPGCacheService.getFromCache(BeCPGLicenseManager.class.getName() + ".sessions", sessionId, () -> {
			Set<String> users = new HashSet<>(authenticationService.getUsersWithTickets(true));
			Set<String> concurrentReadUsers = AuthorityHelper.extractPeople(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadConcurrent.toString());
			concurrentReadUsers.removeIf(this::isSpecialLicenseUser);
			concurrentReadUsers.retainAll(users);
			if (concurrentReadUsers.contains(AuthenticationUtil.getRunAsUser()) && concurrentReadUsers.size() > getLicense().allowedConcurrentRead) {
				return true;
			}
			Set<String> concurrentWriteUsers = AuthorityHelper.extractPeople(PermissionService.GROUP_PREFIX + SystemGroup.LicenseWriteConcurrent.toString());
			concurrentWriteUsers.retainAll(users);
			concurrentWriteUsers.removeIf(this::isSpecialLicenseUser);
			if (concurrentWriteUsers.contains(AuthenticationUtil.getRunAsUser()) && concurrentWriteUsers.size() > getLicense().allowedConcurrentWrite) {
				return true;
			}
			Set<String> concurrentSupplierUsers = AuthorityHelper.extractPeople(PermissionService.GROUP_PREFIX + SystemGroup.LicenseSupplierConcurrent.toString());
			concurrentSupplierUsers.retainAll(users);
			concurrentSupplierUsers.removeIf(this::isSpecialLicenseUser);
			if (concurrentSupplierUsers.contains(AuthenticationUtil.getRunAsUser()) && concurrentSupplierUsers.size() > getLicense().allowedConcurrentSupplier) {
				return true;
			}
			return false;
		});
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
