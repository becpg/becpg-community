package fr.becpg.test.repo.license;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.license.BeCPGLicense;
import fr.becpg.repo.license.BeCPGLicenseManager;
import fr.becpg.test.BeCPGTestHelper;
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
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef licenseFolderNodeRef = null, licenseFileNodeRef = null;
			licenseFolderNodeRef = repoService.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseFolderNodeRef != null) {
				licenseFileNodeRef = nodeService.getChildByName(licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS, "license.json");
				if (licenseFileNodeRef != null) {
					nodeService.deleteNode(licenseFileNodeRef);
				}
			}
			cacheService.clearCache(BeCPGLicenseManager.class.getName());

			return true;
		}, false, false);

		// Before Init license.json
		logger.info("Initialize BeCPG License");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertEquals(-1L, licenseManager.getAllowedNamedRead());
			assertEquals(-1L, licenseManager.getAllowedNamedWrite());
			assertEquals(-1L, licenseManager.getAllowedConcurrentRead());
			assertEquals(-1L, licenseManager.getAllowedConcurrentWrite());
			assertEquals(-1L, licenseManager.getAllowedConcurrentSupplier());


			NodeRef licenseFolderNodeRef = null, licenseFileNodeRef = null;
			licenseFolderNodeRef = repoService.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseFolderNodeRef != null) {
				licenseFileNodeRef = nodeService.getChildByName(licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS, "license.json.sample");
				if (licenseFileNodeRef != null) {
					licenseFileNodeRef = copyService.copyAndRename(licenseFileNodeRef, licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, true);
					if (licenseFileNodeRef != null) {
						nodeService.setProperty(licenseFileNodeRef, ContentModel.PROP_NAME, "license.json");
					}
				}
			}
			

			cacheService.clearCache(BeCPGLicenseManager.class.getName());
			
			assertNotNull("License folder", licenseFolderNodeRef);
			assertNotNull("License file", licenseFileNodeRef);

			return true;
		}, false, false);

		// After Init license.json
		logger.info("Update BeCPG License");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertEquals(1L, licenseManager.getAllowedNamedRead());
			assertEquals(1L, licenseManager.getAllowedNamedWrite());
			assertEquals(10L, licenseManager.getAllowedConcurrentRead());
			assertEquals(1L, licenseManager.getAllowedConcurrentWrite());
			assertEquals(10L, licenseManager.getAllowedConcurrentSupplier());

			cacheService.clearCache(BeCPGLicenseManager.class.getName());

			NodeRef licenseFolderNodeRef = repoService.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
			if (licenseFolderNodeRef != null) {
				NodeRef licenseFileNodeRef = nodeService.getChildByName(licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS, "license.json");
				if (licenseFileNodeRef != null) {
					ContentWriter writer = contentService.getWriter(licenseFileNodeRef, ContentModel.PROP_CONTENT, true);
					String content = " {\n" + "	\"LicenseName\":\"beCPG Sample LICENSE\",\n" + "	\"LicenseWriteNamed\":2,\n"
							+ "	\"LicenseReadNamed\":1,\n" + "	\"LicenseWriteConcurrent\":1,\n" + "	\"LicenseReadConcurrent\":10,\n"
							+ "	\"LicenseSupplierConcurrent\":10,\n" + "	\"LicenseKey\": \"YmVDUEcgU2FtcGxlIExJQ0VOU0UxMTEwMTEw\"\n" + "}";

					writer.setMimetype(MimetypeMap.MIMETYPE_JSON);
					writer.setEncoding("UTF-8");
					writer.putContent(content);
				}
			}

			return true;
		}, false, false);

		// After violence license.json
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertEquals(0L, licenseManager.getAllowedNamedRead());
			assertEquals(0L, licenseManager.getAllowedNamedWrite());
			assertEquals(0L, licenseManager.getAllowedConcurrentRead());
			assertEquals(0L, licenseManager.getAllowedConcurrentWrite());
			assertEquals(0L, licenseManager.getAllowedConcurrentSupplier());
			assertEquals("Invalid license file", licenseManager.getLicenseName());

			return true;
		}, true, false);

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

	@Test
	public void concurrentReadLicenseShouldBeReleasedWhenUserDisconnects() {
		String firstUser = "licenseConcurrentReadUserOne";
		String secondUser = "licenseConcurrentReadUserTwo";
		String firstUserTicket = null;
		String secondUserTicket = null;

		inWriteTx(() -> {
			BeCPGTestHelper.createUser(firstUser);
			BeCPGTestHelper.createUser(secondUser);
			if (!authorityService.getAuthoritiesForUser(firstUser).contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadConcurrent.toString())) {
				authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadConcurrent.toString(), firstUser);
			}
			if (!authorityService.getAuthoritiesForUser(secondUser).contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadConcurrent.toString())) {
				authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadConcurrent.toString(), secondUser);
			}
			installLicense("beCPG Concurrent License", 1, 0, 0, 0, 0);
			return null;
		});

		try {
			firstUserTicket = authenticate(firstUser);
			assertTrue(isConcurrentUserAllowed(firstUser));

			secondUserTicket = authenticate(secondUser);
			assertFalse(isConcurrentUserAllowed(secondUser));

			authenticationService.invalidateTicket(firstUserTicket);
			assertTrue(isConcurrentUserAllowed(secondUser));
		} finally {
			invalidateTicket(firstUserTicket);
			invalidateTicket(secondUserTicket);
		}
	}

	private void installLicense(String licenseName, long allowedConcurrentRead, long allowedConcurrentWrite,
			long allowedConcurrentSupplier, long allowedNamedWrite, long allowedNamedRead) {
		NodeRef licenseFolderNodeRef = repoService.getFolderByPath(RepoConsts.PATH_SYSTEM + RepoConsts.PATH_SEPARATOR + RepoConsts.PATH_LICENSE);
		assertNotNull("License folder", licenseFolderNodeRef);

		NodeRef licenseFileNodeRef = nodeService.getChildByName(licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS, "license.json");
		if (licenseFileNodeRef == null) {
			NodeRef sampleLicenseNodeRef = nodeService.getChildByName(licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS, "license.json.sample");
			assertNotNull("Sample license file", sampleLicenseNodeRef);
			licenseFileNodeRef = copyService.copyAndRename(sampleLicenseNodeRef, licenseFolderNodeRef, ContentModel.ASSOC_CONTAINS,
					ContentModel.ASSOC_CONTAINS, true);
			nodeService.setProperty(licenseFileNodeRef, ContentModel.PROP_NAME, "license.json");
		}

		ContentWriter writer = contentService.getWriter(licenseFileNodeRef, ContentModel.PROP_CONTENT, true);
		BeCPGLicense license = new BeCPGLicense(licenseName, allowedConcurrentRead, allowedConcurrentWrite, allowedConcurrentSupplier,
				allowedNamedWrite, allowedNamedRead);
		String licenseKey = BeCPGLicenseManager.computeLicenseKey(license);
		String content = " {\n" + "\t\"LicenseName\":\"" + licenseName + "\",\n" + "\t\"LicenseWriteNamed\":"
				+ allowedNamedWrite + ",\n" + "\t\"LicenseReadNamed\":" + allowedNamedRead + ",\n"
				+ "\t\"LicenseWriteConcurrent\":" + allowedConcurrentWrite + ",\n" + "\t\"LicenseReadConcurrent\":"
				+ allowedConcurrentRead + ",\n" + "\t\"LicenseSupplierConcurrent\":" + allowedConcurrentSupplier
				+ ",\n" + "\t\"LicenseKey\": \"" + licenseKey + "\"\n" + "}";

		writer.setMimetype(MimetypeMap.MIMETYPE_JSON);
		writer.setEncoding("UTF-8");
		writer.putContent(content);
		cacheService.clearCache(BeCPGLicenseManager.class.getName());
	}

	private String authenticate(String userName) {
		try {
			AuthenticationUtil.pushAuthentication();
			authenticationService.authenticate(userName, "PWD".toCharArray());
			return authenticationService.getCurrentTicket();
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}

	private boolean isConcurrentUserAllowed(String userName) {
		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setFullyAuthenticatedUser(userName);
			return inReadTx(new RetryingTransactionCallback<Boolean>() {
				@Override
				public Boolean execute() throws Throwable {
					return licenseManager.isConcurrentUserAllowed();
				}
			});
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}

	private void invalidateTicket(String ticket) {
		if (ticket != null) {
			authenticationService.invalidateTicket(ticket);
		}
	}

}
