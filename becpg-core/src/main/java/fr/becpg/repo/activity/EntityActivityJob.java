package fr.becpg.repo.activity;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class EntityActivityJob  implements Job {

	private static final String KEY_ENTITY_ACTIVITY_SERVICE = "entityActivityService";
	private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";

	private static final Log logger = LogFactory.getLog(EntityActivityJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		logger.info("Start of Activity cleaner Job.");

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final EntityActivityService entityActivityService = (EntityActivityService) jobData.get(KEY_ENTITY_ACTIVITY_SERVICE);
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get(KEY_TENANT_ADMIN_SERVICE);

		AuthenticationUtil.runAsSystem(() -> {
			entityActivityService.cleanActivities();
			return null;
		});

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(() -> {
					entityActivityService.cleanActivities();
					return null;
				} , tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		}

		logger.info("End of Activity cleaner Job.");
	}
}
