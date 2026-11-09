package fr.becpg.repo.product.formulation.job;

import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.transaction.TransactionService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.repo.helper.TenantJobHelper;

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

		TenantJobHelper.runForEachTenant(tenantAdminService, () -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			formulationChannelService.formulateEntities();
			return null;
		}, false, true));

	}

}
