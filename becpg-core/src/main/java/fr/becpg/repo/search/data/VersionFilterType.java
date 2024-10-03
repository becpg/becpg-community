package fr.becpg.repo.search.data;

import org.alfresco.service.cmr.version.VersionType;

/**
 * <p>VersionFilterType class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public enum VersionFilterType {
	MAJOR, MINOR, NONE;

	/**
	 * <p>match.</p>
	 *
	 * @param versionType a {@link org.alfresco.service.cmr.version.VersionType} object
	 * @return a boolean
	 */
	public boolean match(VersionType versionType) {
		return versionType!=null && versionType.toString().equals(this.toString());
	}
}
