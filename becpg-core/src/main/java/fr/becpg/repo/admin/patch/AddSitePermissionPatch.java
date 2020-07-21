package fr.becpg.repo.admin.patch;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * <p>AddSitePermissionPatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AddSitePermissionPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(AddSitePermissionPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.addSitePermissionPatch.result";

	private SiteService siteService;

	private PermissionService permissionService;

	private AuthorityService authorityService;

	/**
	 * <p>Setter for the field <code>siteService</code>.</p>
	 *
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object.
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object.
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object.
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		for (SiteInfo site : siteService.listSites(null, null)) {
			// Get the current user

			// Create the relevant groups and assign permissions
			AuthenticationUtil.runAs(() -> {
				Set<String> shareZones = new HashSet<>(2, 1.0f);
				shareZones.add(AuthorityService.ZONE_APP_SHARE);
				shareZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

				// Create the site's groups
				String siteGroupShortName = getSiteGroup(site.getShortName(), false);
				/*
				 * MNT-11289 fix - group is probably already exists. we should
				 * check it for existence
				 */
				String siteGroup = authorityService.getName(AuthorityType.GROUP, siteGroupShortName);

				QName siteType = nodeService.getType(site.getNodeRef());
				Set<String> permissions = permissionService.getSettablePermissions(siteType);
				for (String permission : permissions) {
					// Create a group for the permission
					String permissionGroupShortName = getSiteRoleGroup(site.getShortName(), permission, false);
					/*
					 * MNT-11289 fix - group is probably already exists. we
					 * should check it for existence
					 */
					String authorityName = authorityService.getName(AuthorityType.GROUP, permissionGroupShortName);
					if (authorityService.authorityExists(authorityName)) {
						continue;
					}

					logger.info("Adding permission (" + permissionGroupShortName + ") to site: " + site.getShortName());

					String permissionGroup = authorityService.createAuthority(AuthorityType.GROUP, permissionGroupShortName, permissionGroupShortName,
							shareZones);
					authorityService.addAuthority(siteGroup, permissionGroup);

					// Assign the group the relevant permission on the site
					permissionService.setPermission(site.getNodeRef(), permissionGroup, permission, true);
				}

				// Return nothing
				return null;
			}, AuthenticationUtil.getSystemUserName());
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	/**
	 * Helper method to get the name of the site group
	 *
	 * @param shortName
	 *            site short name
	 * @return String site group name
	 * @param withGroupPrefix a boolean.
	 */
	public String getSiteGroup(String shortName, boolean withGroupPrefix) {
		StringBuffer sb = new StringBuffer(64);
		if (withGroupPrefix == true) {
			sb.append(PermissionService.GROUP_PREFIX);
		}
		sb.append(SITE_PREFIX);
		sb.append(shortName);
		return sb.toString();
	}

	/**
	 * Helper method to get the name of the site permission group
	 *
	 * @param shortName
	 *            site short name
	 * @param permission
	 *            permission name
	 * @param withGroupPrefix
	 *            - should the name have the GROUP_ prefix?
	 * @return String site permission group name
	 */
	public String getSiteRoleGroup(String shortName, String permission, boolean withGroupPrefix) {
		return getSiteGroup(shortName, withGroupPrefix) + '_' + permission;
	}

	private static final String SITE_PREFIX = "site_";

}
