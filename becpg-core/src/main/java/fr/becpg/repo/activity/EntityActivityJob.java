package fr.becpg.repo.activity;

import java.util.List;

import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * <p>EntityActivityJob class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EntityActivityJob  extends AbstractScheduledLockedJob implements Job {

	/** Constant <code>KEY_ENTITY_ACTIVITY_SERVICE="entityActivityService"</code> */
	private static final String KEY_ENTITY_ACTIVITY_SERVICE = "entityActivityService";
	/** Constant <code>KEY_TENANT_ADMIN_SERVICE="tenantAdminService"</code> */
	private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";

	/** {@inheritDoc} */
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		cleanActivities((EntityActivityService) jobData.get(KEY_ENTITY_ACTIVITY_SERVICE), (TenantAdminService) jobData.get(KEY_TENANT_ADMIN_SERVICE));
	}

	static void cleanActivities(EntityActivityService entityActivityService, TenantAdminService tenantAdminService) {

		// AuthenticationUtil.runAs as System@tenant leaves the default tenant context, so every tenant batch was
		// queued under the default batch id and rejected as a duplicate
		cleanActivities(entityActivityService, TenantService.DEFAULT_DOMAIN);

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				if (tenant.isEnabled()) {
					cleanActivities(entityActivityService, tenant.getTenantDomain());
				}
			}
		}
	}

	private static void cleanActivities(EntityActivityService entityActivityService, String tenantDomain) {
		TenantUtil.runAsSystemTenant(() -> {
			entityActivityService.cleanActivities();
			return null;
		}, tenantDomain);
	}
}
