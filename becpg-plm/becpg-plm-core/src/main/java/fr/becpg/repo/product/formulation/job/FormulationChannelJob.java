package fr.becpg.repo.product.formulation.job;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.transaction.TransactionService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * <p>FormulationChannelJob class.</p>
 *
 * @author matthieu
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class FormulationChannelJob extends AbstractScheduledLockedJob implements Job {

	/** {@inheritDoc} */
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final FormulationChannelService formulationChannelService = (FormulationChannelService) jobData.get("formulationChannelService");
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get("tenantAdminService");
		final TransactionService transactionService = (TransactionService) jobData.get("transactionService");

		AuthenticationUtil.runAsSystem(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			formulationChannelService.formulateEntities();
			return null;
		}, false, true));

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				if (tenant.isEnabled()) {
					String tenantDomain = tenant.getTenantDomain();
					if (!TenantService.DEFAULT_DOMAIN.equals(tenantDomain)) {
						AuthenticationUtil.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							formulationChannelService.formulateEntities();
							return null;
						}, false, true), tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
					}
				}
			}
		}

	}

}
