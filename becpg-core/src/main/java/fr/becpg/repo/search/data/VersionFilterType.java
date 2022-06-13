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

	public boolean match(VersionType versionType) {
		return versionType!=null && versionType.toString().equals(this.toString());
	}
}
