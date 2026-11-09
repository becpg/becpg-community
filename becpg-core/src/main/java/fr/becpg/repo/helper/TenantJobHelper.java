package fr.becpg.repo.helper;

import java.util.List;

import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;

/**
 * Runs the work of a scheduled job as System in the default domain and in each enabled tenant.
 *
 * @author matthieu
 */
public final class TenantJobHelper {

	private TenantJobHelper() {
	}

	/**
	 * Runs the work in the default domain, then once in each other enabled tenant.
	 *
	 * @param tenantAdminService the tenant admin service, null when multi-tenancy is not wired
	 * @param work the work to run
	 */
	public static void runForEachTenant(TenantAdminService tenantAdminService, Runnable work) {

		// The tenant context is set explicitly: AuthenticationUtil.runAs as System@tenant only implies the tenant
		// when the thread holds no authentication, and a Quartz thread may still hold the one of a previous job
		runAsSystem(work, TenantService.DEFAULT_DOMAIN);

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				if (tenant.isEnabled() && !TenantService.DEFAULT_DOMAIN.equals(tenantDomain)) {
					runAsSystem(work, tenantDomain);
				}
			}
		}
	}

	private static void runAsSystem(Runnable work, String tenantDomain) {
		TenantUtil.runAsSystemTenant(() -> {
			work.run();
			return null;
		}, tenantDomain);
	}
}
