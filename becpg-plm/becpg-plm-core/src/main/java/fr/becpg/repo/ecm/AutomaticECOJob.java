package fr.becpg.repo.ecm;

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
 * <p>AutomaticECOJob class.</p>
 *
 * @author matthieu
 *
 *         Case 1: - Automatic applied changed on wused (Formulation and
 *         Reports) - Changed with Version Case 2: - Create OM
 * @version $Id: $Id
 */
// Can be made with a job ?
// Maybe a policy on entity
// Avec quoi on remplit les OM automatic ?
// - Product , entity
// On fait un wused à chaud ?
// On les mets tous ?
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class AutomaticECOJob extends AbstractScheduledLockedJob implements Job {

	/** {@inheritDoc} */
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final AutomaticECOService automaticECOService = (AutomaticECOService) jobData.get("automaticECOService");
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get("tenantAdminService");

		TenantJobHelper.runForEachTenant(tenantAdminService, automaticECOService::applyAutomaticEco);

	}

}
