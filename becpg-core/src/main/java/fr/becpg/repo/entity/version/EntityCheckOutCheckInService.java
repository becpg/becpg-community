package fr.becpg.repo.entity.version;

import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.util.VersionNumber;

public interface EntityCheckOutCheckInService extends CheckOutCheckInService {

	/**
	 * Calculate new version
	 * @param versionLabel
	 * @param majorVersion
	 * @return
	 */
	public VersionNumber getVersionNumber(String versionLabel, boolean majorVersion);
}
