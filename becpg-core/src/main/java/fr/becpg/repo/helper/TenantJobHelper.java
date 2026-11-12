package fr.becpg.repo.helper;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
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
	 * A failure in one domain is logged and does not keep the work from running in the following ones. An
	 * exception the transaction helper retries on is the exception of that rule: it is thrown again untouched,
	 * so that a job called inside a retrying transaction is still replayed.
	 *
	 * @param tenantAdminService the tenant admin service, null when multi-tenancy is not wired
	 * @param work the work to run
	 */
	public static void runForEachTenant(TenantAdminService tenantAdminService, Runnable work) {

		// The tenant context is set explicitly: AuthenticationUtil.runAs as System@tenant only implies the tenant
		// when the thread holds no authentication, and a Quartz thread may still hold the one of a previous job
		for (String tenantDomain : domainsToServe(tenantAdminService)) {
			try {
				runAsSystem(work, tenantDomain);
			} catch (RuntimeException e) {
				if (RetryingTransactionHelper.extractRetryCause(e) != null) {
					throw e;
				}
				logger.error("Scheduled work failed in tenant '" + tenantDomain + "': " + e.getMessage(), e);
			}
		}
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

	private static void runAsSystem(Runnable work, String tenantDomain) {
		TenantUtil.runAsSystemTenant(() -> {
			work.run();
			return null;
		}, tenantDomain);
	}
}
