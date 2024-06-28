package fr.becpg.test.repo.license;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.license.BeCPGLicense;
import fr.becpg.repo.license.BeCPGLicenseManager;
import fr.becpg.test.RepoBaseTestCase;

public class LicenseManagerIT extends RepoBaseTestCase {

	private static final Log logger = LogFactory.getLog(LicenseManagerIT.class);

	@Autowired
	TransactionService transactionService;

	@Autowired
	BeCPGLicenseManager licenseManager;

	@Autowired
	BeCPGCacheService cacheService;

	@Autowired
	private CopyService copyService;

	@Test
	public void readLicenseTest() {

		// Purge existing licence
		inWriteTx(() -> {
			NodeRef licenseFolderNodeRef = null, licenseFileNodeRef = null;
			licenseFolderNodeRef = repoService
					.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseFolderNodeRef != null) {
				licenseFileNodeRef = nodeService.getChildByName(licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						"license.json");
				if (licenseFileNodeRef != null) {
					nodeService.deleteNode(licenseFileNodeRef);
				}
			}
			cacheService.clearCache(BeCPGLicenseManager.class.getName());

			return true;
		});

		// Before Init license.json
		logger.info("Initialize BeCPG License");
		inReadTx(() -> {
			assertEquals(-1L, licenseManager.getAllowedNamedRead());
			assertEquals(-1L, licenseManager.getAllowedNamedWrite());
			assertEquals(-1L, licenseManager.getAllowedConcurrentRead());
			assertEquals(-1L, licenseManager.getAllowedConcurrentWrite());
			assertEquals(-1L, licenseManager.getAllowedConcurrentSupplier());

			NodeRef licenseFolderNodeRef = null, licenseFileNodeRef = null;
			licenseFolderNodeRef = repoService
					.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseFolderNodeRef != null) {
				licenseFileNodeRef = nodeService.getChildByName(licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						"license.json.sample");
				if (licenseFileNodeRef != null) {
					licenseFileNodeRef = copyService.copyAndRename(licenseFileNodeRef, licenseFolderNodeRef,
							ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, true);
					if (licenseFileNodeRef != null) {
						nodeService.setProperty(licenseFileNodeRef, ContentModel.PROP_NAME, "license.json");
					}
				}
			}

			cacheService.clearCache(BeCPGLicenseManager.class.getName());

			assertNotNull("License folder", licenseFolderNodeRef);
			assertNotNull("License file", licenseFileNodeRef);

			return true;
		});

		// After Init license.json
		logger.info("Update BeCPG License");
		inReadTx(() -> {
			assertEquals(1L, licenseManager.getAllowedNamedRead());
			assertEquals(1L, licenseManager.getAllowedNamedWrite());
			assertEquals(10L, licenseManager.getAllowedConcurrentRead());
			assertEquals(1L, licenseManager.getAllowedConcurrentWrite());
			assertEquals(10L, licenseManager.getAllowedConcurrentSupplier());

			cacheService.clearCache(BeCPGLicenseManager.class.getName());

			NodeRef licenseFolderNodeRef = repoService
					.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseFolderNodeRef != null) {
				NodeRef licenseFileNodeRef = nodeService.getChildByName(licenseFolderNodeRef,
						ContentModel.ASSOC_CONTAINS, "license.json");
				if (licenseFileNodeRef != null) {
					ContentWriter writer = contentService.getWriter(licenseFileNodeRef, ContentModel.PROP_CONTENT,
							true);
					String content = " {\n" + "	\"LicenseName\":\"beCPG Sample LICENSE\",\n"
							+ "	\"LicenseWriteNamed\":2,\n" + "	\"LicenseReadNamed\":1,\n"
							+ "	\"LicenseWriteConcurrent\":1,\n" + "	\"LicenseReadConcurrent\":10,\n"
							+ "	\"LicenseSupplierConcurrent\":10,\n"
							+ "	\"LicenseKey\": \"YmVDUEcgU2FtcGxlIExJQ0VOU0UxMTEwMTEw\"\n" + "}";

					writer.setMimetype(MimetypeMap.MIMETYPE_JSON);
					writer.setEncoding("UTF-8");
					writer.putContent(content);
				}
			}

			return true;
		});

		// After violence license.json
		inWriteTx(() -> {
			assertEquals(0L, licenseManager.getAllowedNamedRead());
			assertEquals(0L, licenseManager.getAllowedNamedWrite());
			assertEquals(0L, licenseManager.getAllowedConcurrentRead());
			assertEquals(0L, licenseManager.getAllowedConcurrentWrite());
			assertEquals(0L, licenseManager.getAllowedConcurrentSupplier());
			assertEquals("Invalid license file", licenseManager.getLicenseName());

			return true;
		});

	}

	@Test
	public void testLicenseKey() {

		BeCPGLicense license = new BeCPGLicense("beCPG Sample LICENSE", 1, 1, 10, 1, 10);

		String licenseKey = BeCPGLicenseManager.computeLicenseKey(license);

		// Used to get the key System.out.println("Sample license
		// key:"+lisenceKey);

		assertTrue(BeCPGLicenseManager.isValid(licenseKey, license));

		license = new BeCPGLicense("beCPG Sample LICENSE", 2, 2, 10, 1, 10);

		assertFalse(BeCPGLicenseManager.isValid(licenseKey, license));

	}

}
