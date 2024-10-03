package fr.becpg.repo.entity.version;

import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>VersionCleanerJob class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class VersionCleanerJob extends AbstractScheduledLockedJob implements Job {

	/**
	 * <p>Constructor for VersionCleanerJob.</p>
	 */
	public VersionCleanerJob() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();
		VersionCleanerService versionCleanerService = (VersionCleanerService) jobData.get("versionCleanerService");
		SystemConfigurationService systemConfigurationService = (SystemConfigurationService) jobData.get("systemConfigurationService");
		versionCleanerService.cleanVersions(Integer.parseInt(systemConfigurationService.confValue("beCPG.version.cleaner.maxProcessedNodes")), null);
	}
}
