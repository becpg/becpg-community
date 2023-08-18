/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.google.common.net.HttpHeaders;

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.license.BeCPGLicenseManager;

/**
 * <p>MonitorWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MonitorWebScript extends DeclarativeWebScript {

	private static final Log logger = LogFactory.getLog(MonitorWebScript.class);
	
	private ContentService contentService;
	
	private AbstractAuthenticationService authenticationService;

	private BeCPGLicenseManager licenseManager;

	private TenantAdminService tenantAdminService;

	private String becpgSchema;

	private AuthorityService authorityService;

	private BatchQueueService batchQueueService;

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		
		return AuthenticationUtil.runAsSystem(() -> {
			logger.debug("start monitor webscript");
			
			Map<String, Object> ret = new HashMap<>();
			
			fillMonitoringInformation(ret, false);
			
			if ("beCPG Monitors".equals(req.getHeader(HttpHeaders.USER_AGENT))) {
				ret.put("authenticated", true);
			} else {
				ret.clear();
			}
			
			ret.put("status", "SUCCESS");
			
			return ret;
		});
		
	}

	protected Set<String> fillMonitoringInformation(Map<String, Object> ret, boolean isAuthenticated) {
		
		long concurrentReadUsers = 0;
		long concurrentSupplierUsers = 0;
		long concurrentWriteUsers = 0;
		long namedReadUsers = 0;
		long namedWriteUsers = 0;
		long withoutLicenseUsers = 0;
	
		Set<String> users = new HashSet<>(authenticationService.getUsersWithTickets(true));
		for (Iterator<String> iterator = users.iterator(); iterator.hasNext();) {
			String user = iterator.next();
			if ((AuthenticationUtil.getGuestUserName().equals(user) || AuthenticationUtil.getSystemUserName().equals(user))
					|| (tenantAdminService.isEnabled()
							&& !tenantAdminService.getCurrentUserDomain().equals(tenantAdminService.getUserDomain(user)))) {
				iterator.remove();
			}
		}
	
		for (String user : users) {
			if (!AuthenticationUtil.getAdminUserName().equals(user)) {
				Set<String> userAuthorities = authorityService.getAuthoritiesForUser(user);
				if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser)
						&& userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseSupplierConcurrent)) {
					concurrentSupplierUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseWriteNamed)) {
					namedWriteUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadNamed)) {
					namedReadUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseWriteConcurrent)) {
					concurrentWriteUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadConcurrent)) {
					concurrentReadUsers++;
				} else {
					withoutLicenseUsers++;
				}
			}
		}
	
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	
		Runtime runtime = Runtime.getRuntime();
	
		ret.put("diskFreeSpace", contentService.getStoreFreeSpace());
		ret.put("diskTotalSpace", contentService.getStoreTotalSpace());
		ret.put("totalMemory", runtime.totalMemory() / 1000000d);
		ret.put("freeMemory", runtime.freeMemory() / 1000000d);
		ret.put("maxMemory", runtime.maxMemory() / 1000000d);
		ret.put("nonHeapMemoryUsage", memoryMXBean.getNonHeapMemoryUsage().getUsed() / 1000000d);
		ret.put("connectedUsers", isAuthenticated ? users.size() : users.size() + 1);
		ret.put("concurrentReadUsers", concurrentReadUsers);
		ret.put("concurrentWriteUsers", concurrentWriteUsers);
		ret.put("concurrentSupplierUsers", concurrentSupplierUsers);
		ret.put("namedReadUsers", namedReadUsers);
		ret.put("namedWriteUsers", namedWriteUsers);
		ret.put("allowedConcurrentRead", licenseManager.getAllowedConcurrentRead());
		ret.put("allowedConcurrentWrite", licenseManager.getAllowedConcurrentWrite());
		ret.put("allowedConcurrentSupplier", licenseManager.getAllowedConcurrentSupplier());
		ret.put("allowedNamedWrite", licenseManager.getAllowedNamedWrite());
		ret.put("allowedNamedRead", licenseManager.getAllowedNamedRead());
		ret.put("licenseName", licenseManager.getLicenseName());
		ret.put("withoutLicenseUsers", withoutLicenseUsers);
		ret.put("becpgSchema", becpgSchema.replace("-SNAPSHOT",""));
		
		BatchMonitor lastRunningBatch = batchQueueService.getLastRunningBatch();
		
		boolean batchInProgress = false;
		
		batchInProgress = lastRunningBatch != null && !lastRunningBatch.getPercentComplete().contains("100");
		
		ret.put("batchCounts", batchQueueService.getBatchesInQueue().size() + (batchInProgress ? 1 : 0));
	
		return users;
		
	}

	/**
	 * <p>Setter for the field <code>authenticationService</code>.</p>
	 *
	 * @param authenticationService a {@link org.alfresco.repo.security.authentication.AbstractAuthenticationService} object.
	 */
	public void setAuthenticationService(AbstractAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	public void setBecpgSchema(String becpgSchema) {
		this.becpgSchema = becpgSchema;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}

	public void setLicenseManager(BeCPGLicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}

}
