package fr.becpg.repo.license;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.RepoService;

@Service("becpgLicenseManager")
@DependsOn
public class BeCPGLicenseManager {

	private static final Log logger = LogFactory.getLog(BeCPGLicenseManager.class);

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	public long getAllowedConcurrentRead() {
		return getLicense().allowedConcurrentRead;
	}

	private BeCPGLicense getLicense() {
		return beCPGCacheService.getFromCache(BeCPGLicenseManager.class.getName(), "lisence", () -> {
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
					ret.licenseName = "Invalid license file";
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

	public long getAllowedConcurrentWrite() {
		return getLicense().allowedConcurrentWrite;
	}

	public long getAllowedConcurrentSupplier() {
		return getLicense().allowedConcurrentSupplier;
	}

	public long getAllowedNamedWrite() {
		return getLicense().allowedNamedWrite;
	}

	public long getAllowedNamedRead() {
		return getLicense().allowedNamedRead;
	}

	public String getLicenseName() {
		return getLicense().licenseName;
	}


	public static boolean isValid(String licenseKey, BeCPGLicense license) {

		String computedKey = computeLicenseKey(license);

		boolean ret = licenseKey.trim().equalsIgnoreCase(computedKey.trim());

		if (!ret) {
			logger.error("License key do not match: " + licenseKey + "/" + computedKey);
			logger.warn("For: " + license.toString());
		}

		return ret;
	}

	public static String computeLicenseKey(BeCPGLicense lisence) {

		String key = lisence.licenseName + lisence.allowedNamedRead + lisence.allowedNamedWrite + lisence.allowedConcurrentRead
				+ lisence.allowedConcurrentWrite + lisence.allowedConcurrentSupplier;

		return Base64.encode(key.getBytes());
	}

	private JSONObject getLicenseFile() {
		try {

			NodeRef licenseProfilesFolderNodeRef = repoService
					.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseProfilesFolderNodeRef != null) {
				NodeRef licenseFileNodeRef = nodeService.getChildByName(licenseProfilesFolderNodeRef, ContentModel.ASSOC_CONTAINS, "license.json");

				if (licenseFileNodeRef != null) {

					ContentReader reader = contentService.getReader(licenseFileNodeRef, ContentModel.PROP_CONTENT);
					String content = reader.getContentString();
					return new JSONObject(content);
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

}
