package fr.becpg.repo.license;

import java.util.Set;

import org.alfresco.service.cmr.security.PermissionService;

import fr.becpg.model.SystemGroup;

/**
 * The single license a user consumes, whatever the number of license groups they belong to.
 * The license groups are declared by priority: the first one the user belongs to wins.
 *
 * @author valentin
 */
public enum UserLicense {

	WRITE_NAMED(SystemGroup.LicenseWriteNamed, false, "licensewritenamed"),
	WRITE_CONCURRENT(SystemGroup.LicenseWriteConcurrent, true, "licensewriteconcurrent"),
	READ_NAMED(SystemGroup.LicenseReadNamed, false, "licensereadnamed"),
	READ_CONCURRENT(SystemGroup.LicenseReadConcurrent, true, "licensereadconcurrent"),
	SUPPLIER_CONCURRENT(SystemGroup.LicenseSupplierConcurrent, true, "licensesupplierconcurrent"),
	SPECIAL_USER(null, false, "specialuser"),
	NO_LICENSE(null, false, "nolicense");

	private final SystemGroup licenseGroup;

	private final boolean concurrent;

	private final String key;

	UserLicense(SystemGroup licenseGroup, boolean concurrent, String key) {
		this.licenseGroup = licenseGroup;
		this.concurrent = concurrent;
		this.key = key;
	}

	/**
	 * Resolves the license consumed by a user from all the authorities they belong to, sub-groups included.
	 *
	 * @param userAuthorities the authorities of the user, as returned by <code>AuthorityService.getAuthoritiesForUser</code>
	 * @return the license of highest priority, or {@link #NO_LICENSE} when the user is in no license group
	 */
	public static UserLicense resolve(Set<String> userAuthorities) {
		for (UserLicense userLicense : values()) {
			if (userLicense.isGrantedBy(userAuthorities)) {
				return userLicense;
			}
		}
		return NO_LICENSE;
	}

	private boolean isGrantedBy(Set<String> userAuthorities) {
		return (licenseGroup != null) && userAuthorities.contains(PermissionService.GROUP_PREFIX + licenseGroup);
	}

	/**
	 * Tells whether this license consumes a floating token while the user is connected.
	 *
	 * @return true for the concurrent licenses
	 */
	public boolean isConcurrent() {
		return concurrent;
	}

	/**
	 * Returns the key displayed by the admin console for this license.
	 *
	 * @return the license key
	 */
	public String getKey() {
		return key;
	}
}
