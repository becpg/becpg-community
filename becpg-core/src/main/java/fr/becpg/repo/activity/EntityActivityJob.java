package fr.becpg.repo.activity;

import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.repo.helper.TenantJobHelper;

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

		final EntityActivityService entityActivityService = (EntityActivityService) jobData.get(KEY_ENTITY_ACTIVITY_SERVICE);
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get(KEY_TENANT_ADMIN_SERVICE);

		TenantJobHelper.runForEachTenant(tenantAdminService, entityActivityService::cleanActivities);

	}
}
