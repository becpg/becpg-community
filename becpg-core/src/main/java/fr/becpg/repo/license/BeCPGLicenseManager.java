package fr.becpg.repo.license;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("becpgLicenseManager")
public class BeCPGLicenseManager {

	private static final Log logger = LogFactory.getLog(BeCPGLicenseManager.class);

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
	private BeCPGQueryBuilder beCPGQueryBuilder;

	@Autowired
	private TransactionService transactionService;

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
	
	//TODO ne fonctionne pas en multitenant

	@PostConstruct
	public void init() {
		if(beCPGQueryBuilder.isInit()) {
		
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				return AuthenticationUtil.runAsSystem(() -> {
					readLicense();
					return true;
				});
			}, true);
		}
	}

	public void readLicense() throws JSONException {
		JSONObject licenseObj = getLicense();
		if (licenseObj != null) {
			boolean valid = false;
			if (licenseObj.has("LicenseKey")) {

				String licenseKey = licenseObj.getString("LicenseKey");

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

				valid = isValid(licenseKey, licenseName, allowedNamedRead, allowedNamedWrite, allowedConcurrentRead, allowedConcurrentWrite,
						allowedConcurrentSupplier);

			}

			if (!valid) {
				licenseName = "Invalid license file";
				allowedNamedRead = 0;
				allowedNamedWrite = 0;
				allowedConcurrentRead = 0;
				allowedConcurrentWrite = 0;
				allowedConcurrentSupplier = 0;
			}
		}
	}

	public static boolean isValid(String licenseKey, String licenseName, long allowedNamedRead, long allowedNamedWrite, long allowedConcurrentRead,
			long allowedConcurrentWrite, long allowedConcurrentSupplier) {

		String computedKey = computeLicenseKey(licenseName, allowedNamedRead, allowedNamedWrite, allowedConcurrentRead, allowedConcurrentWrite,
				allowedConcurrentSupplier);
		
		boolean ret =  licenseKey.trim().equalsIgnoreCase(computedKey.trim());
		
		if(!ret) {
			logger.error("License key do not match: "+licenseKey+ "/"+computedKey);
			logger.warn("For: "+licenseName+ " / "+allowedNamedRead + " / "+ allowedNamedWrite 
					+ " / "+ allowedConcurrentRead + " / "+ allowedConcurrentWrite + " / "+ allowedConcurrentSupplier);
		}
		
		
		return ret;
	}

	public static String computeLicenseKey(String licenseName, long allowedNamedRead, long allowedNamedWrite, long allowedConcurrentRead,
			long allowedConcurrentWrite, long allowedConcurrentSupplier) {

		String key = licenseName + allowedNamedRead + allowedNamedWrite + allowedConcurrentRead + allowedConcurrentWrite + allowedConcurrentSupplier;

		return Base64.encode(key.getBytes());
	}

	private JSONObject getLicense() {
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
