package fr.becpg.repo.license;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
import org.springframework.stereotype.Service;

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.RepoService;

@Service("becpgLicenseManager")
public class BeCPGLicenseManager {

	private static final Log logger = LogFactory.getLog(BeCPGLicenseManager.class);

	private static final String CACHE_KEY = "LICENSE_CACHE_KEY";

	private String licenseName = "beCPG OO License";
	private long allowedConcurrentRead = -1L;
	private long allowedConcurrentWrite = -1L;
	private long allowedConcurrentSupplier = -1L;
	private long allowedNamedWrite = -1L;
	private long allowedNamedRead = -1L;

	@Autowired
	private RepoService repoService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private BeCPGCacheService cacheService;

	public long getAllowedConcurrentRead() {
		return allowedConcurrentRead;
	}

	public long getAllowedConcurrentWrite() {
		return allowedConcurrentWrite;
	}

	public long getAllowedConcurrentSupplier() {
		return allowedConcurrentSupplier;
	}

	public long getAllowedNamedWrite() {
		return allowedNamedWrite;
	}

	public long getAllowedNamedRead() {
		return allowedNamedRead;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void readLicense() {
		try {
			JSONObject licenseObj = getLicenseFromCache();
			if (licenseObj != null) {
				if (licenseObj.has("LicenseKey")) {
					String licenseKey = licenseObj.getString("LicenceKey");

					licenseName = licenseObj.has("LicenseName") ? licenseObj.getString("LicenseName") : licenseName;
					allowedNamedRead = licenseObj.has(SystemGroup.LicenseReadNamed.toString())
							? licenseObj.getLong(SystemGroup.LicenseReadNamed.toString())
							: -1L;
					allowedNamedWrite = licenseObj.has(SystemGroup.LicenseWriteNamed.toString())
							? licenseObj.getLong(SystemGroup.LicenseWriteNamed.toString())
							: -1L;
					allowedConcurrentRead = licenseObj.has(SystemGroup.LicenseReadConcurrent.toString())
							? licenseObj.getLong(SystemGroup.LicenseReadConcurrent.toString())
							: -1L;
					allowedConcurrentWrite = licenseObj.has(SystemGroup.LicenseWriteConcurrent.toString())
							? licenseObj.getLong(SystemGroup.LicenseWriteConcurrent.toString())
							: -1L;
					allowedConcurrentSupplier = licenseObj.has(SystemGroup.LicenseSupplierConcurrent.toString())
							? licenseObj.getLong(SystemGroup.LicenseSupplierConcurrent.toString())
							: -1L;

					if (!isValid(licenseKey, licenseName, allowedNamedRead, allowedNamedWrite, allowedConcurrentRead, allowedConcurrentWrite,
							allowedConcurrentSupplier)) {
						licenseName = "Invalid license file";
						allowedNamedRead = 0;
						allowedNamedWrite = 0;
						allowedConcurrentRead = 0;
						allowedConcurrentWrite = 0;
						allowedConcurrentSupplier = 0;
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Can't read license informations", e);
		}
	}

	public static boolean isValid(String licenseKey, String licenseName, long allowedNamedRead, long allowedNamedWrite, long allowedConcurrentRead,
			long allowedConcurrentWrite, long allowedConcurrentSupplier) {


		return licenseKey.equals(computeLicenseKey(licenseName, allowedNamedRead, allowedNamedWrite, allowedConcurrentRead, allowedConcurrentWrite, allowedConcurrentSupplier));
	}
	
	public static String computeLicenseKey(String licenseName, long allowedNamedRead, long allowedNamedWrite, long allowedConcurrentRead,
			long allowedConcurrentWrite, long allowedConcurrentSupplier) {
		
		String key = licenseName+allowedNamedRead+allowedNamedWrite+allowedConcurrentRead+allowedConcurrentWrite+allowedConcurrentSupplier;
		
		return Base64.encode(key.getBytes());
	}
	

	private JSONObject getLicenseFromCache() {

		BufferedReader bfReader = null;
		JSONObject licenseObj = null;
		try {

			licenseObj = cacheService.getFromCache(BeCPGLicenseManager.class.getName(), CACHE_KEY);

			if (licenseObj == null) {
				NodeRef licenseProfilesFolderNodeRef = repoService
						.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
				if (licenseProfilesFolderNodeRef != null) {
					NodeRef licenseFileNodeRef = nodeService.getChildByName(licenseProfilesFolderNodeRef, ContentModel.ASSOC_CONTAINS,
							"license.json");

					ContentReader reader = contentService.getReader(licenseFileNodeRef, ContentModel.PROP_CONTENT);

					bfReader = new BufferedReader(new InputStreamReader(reader.getContentInputStream(), "UTF-8"));
					StringBuilder responseStrBuilder = new StringBuilder();

					String strStream;

					while ((strStream = bfReader.readLine()) != null) {
						responseStrBuilder.append(strStream);
					}

					licenseObj = new JSONObject(responseStrBuilder.toString());
					cacheService.storeInCache(BeCPGLicenseManager.class.getName(), CACHE_KEY, licenseObj);
				}
			}

		} catch (IOException e) {
			logger.error("Couldn't read license file ", e);
		} catch (JSONException e) {
			logger.error("Unable to serialize JSON", e);
		}

		finally {
			if (bfReader != null) {
				try {
					bfReader.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

		return licenseObj;
	}

}
