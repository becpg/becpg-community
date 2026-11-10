package fr.becpg.repo.helper;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs the work of a scheduled job as System in the default domain and in each enabled tenant.
 *
 * @author matthieu
 */
public final class TenantJobHelper {

	private static final Log logger = LogFactory.getLog(TenantJobHelper.class);

	private TenantJobHelper() {
	}

	/**
	 * Runs the work in the default domain, then once in each other enabled tenant.
	 * <p>
	 * A failure in one domain does not keep the work from running in the following ones: it is logged, and the
	 * first failure is thrown again once every domain has been served, the others being attached to it as
	 * suppressed, so that the job still reports its failure.
	 *
	 * @param tenantAdminService the tenant admin service, null when multi-tenancy is not wired
	 * @param work the work to run
	 */
	public static void runForEachTenant(TenantAdminService tenantAdminService, Runnable work) {

		List<RuntimeException> failures = new ArrayList<>();

		// The tenant context is set explicitly: AuthenticationUtil.runAs as System@tenant only implies the tenant
		// when the thread holds no authentication, and a Quartz thread may still hold the one of a previous job
		for (String tenantDomain : domainsToServe(tenantAdminService)) {
			try {
				runAsSystem(work, tenantDomain);
			} catch (RuntimeException e) {
				logger.error("Scheduled work failed in tenant '" + tenantDomain + "': " + e.getMessage(), e);
				failures.add(e);
			}
		}

		throwFirstFailure(failures);
	}

	private static List<String> domainsToServe(TenantAdminService tenantAdminService) {
		List<String> domains = new ArrayList<>();
		domains.add(TenantService.DEFAULT_DOMAIN);
		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				if (tenant.isEnabled() && !TenantService.DEFAULT_DOMAIN.equals(tenant.getTenantDomain())) {
					domains.add(tenant.getTenantDomain());
				}
			}
		}
		return domains;
	}

	private static void throwFirstFailure(List<RuntimeException> failures) {
		if (failures.isEmpty()) {
			return;
		}
		RuntimeException firstFailure = failures.get(0);
		for (RuntimeException failure : failures.subList(1, failures.size())) {
			firstFailure.addSuppressed(failure);
		}
		throw firstFailure;
	}

	private static void runAsSystem(Runnable work, String tenantDomain) {
		TenantUtil.runAsSystemTenant(() -> {
			work.run();
			return null;
		}, tenantDomain);
	}
}
