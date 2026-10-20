package fr.becpg.repo.license;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.SystemGroup;

/**
 * Unit tests of the license priority applied by {@link UserLicense} when a user belongs to several license groups.
 */
public class UserLicenseTest {

	@Test
	public void writeNamedWinsOverAnyConcurrentLicense() {
		Set<String> authorities = authorities(SystemGroup.LicenseWriteNamed, SystemGroup.LicenseReadConcurrent,
				SystemGroup.LicenseWriteConcurrent, SystemGroup.LicenseSupplierConcurrent);

		Assert.assertEquals(UserLicense.WRITE_NAMED, UserLicense.resolve(authorities));
	}

	@Test
	public void readNamedWinsOverReadConcurrent() {
		Set<String> authorities = authorities(SystemGroup.LicenseReadNamed, SystemGroup.LicenseReadConcurrent);

		Assert.assertEquals(UserLicense.READ_NAMED, UserLicense.resolve(authorities));
	}

	@Test
	public void writeConcurrentWinsOverReadNamed() {
		Set<String> authorities = authorities(SystemGroup.LicenseReadNamed, SystemGroup.LicenseWriteConcurrent);

		Assert.assertEquals(UserLicense.WRITE_CONCURRENT, UserLicense.resolve(authorities));
	}

	@Test
	public void writeConcurrentWinsOverReadConcurrent() {
		Set<String> authorities = authorities(SystemGroup.LicenseReadConcurrent, SystemGroup.LicenseWriteConcurrent);

		Assert.assertEquals(UserLicense.WRITE_CONCURRENT, UserLicense.resolve(authorities));
	}

	@Test
	public void supplierConcurrentIsTheLowestPriority() {
		Set<String> authorities = authorities(SystemGroup.LicenseSupplierConcurrent, SystemGroup.LicenseReadConcurrent);

		Assert.assertEquals(UserLicense.READ_CONCURRENT, UserLicense.resolve(authorities));
	}

	@Test
	public void onlyLicenseGroupsGrantALicense() {
		Set<String> authorities = authorities(SystemGroup.ExternalUser);
		authorities.add(PermissionService.ALL_AUTHORITIES);

		Assert.assertEquals(UserLicense.NO_LICENSE, UserLicense.resolve(authorities));

		authorities.add(PermissionService.GROUP_PREFIX + SystemGroup.LicenseSupplierConcurrent);

		Assert.assertEquals(UserLicense.SUPPLIER_CONCURRENT, UserLicense.resolve(authorities));
	}

	@Test
	public void onlyFloatingLicensesConsumeConcurrentTokens() {
		Assert.assertTrue(UserLicense.READ_CONCURRENT.isConcurrent());
		Assert.assertTrue(UserLicense.WRITE_CONCURRENT.isConcurrent());
		Assert.assertTrue(UserLicense.SUPPLIER_CONCURRENT.isConcurrent());
		Assert.assertFalse(UserLicense.READ_NAMED.isConcurrent());
		Assert.assertFalse(UserLicense.WRITE_NAMED.isConcurrent());
		Assert.assertFalse(UserLicense.SPECIAL_USER.isConcurrent());
		Assert.assertFalse(UserLicense.NO_LICENSE.isConcurrent());
	}

	@Test
	public void keysMatchTheAdminConsoleLabels() {
		Assert.assertEquals("licensewritenamed", UserLicense.WRITE_NAMED.getKey());
		Assert.assertEquals("licensereadconcurrent", UserLicense.READ_CONCURRENT.getKey());
		Assert.assertEquals("specialuser", UserLicense.SPECIAL_USER.getKey());
		Assert.assertEquals("nolicense", UserLicense.NO_LICENSE.getKey());
	}

	private static Set<String> authorities(SystemGroup... groups) {
		Set<String> authorities = new HashSet<>();
		for (SystemGroup group : groups) {
			authorities.add(PermissionService.GROUP_PREFIX + group);
		}
		return authorities;
	}
}
