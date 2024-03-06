package fr.becpg.repo.entity.version;

import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * <p>VersionCleanerJob class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class VersionCleanerJob extends AbstractScheduledLockedJob implements Job {

	public VersionCleanerJob() {
		super();
	}

	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();
		VersionCleanerService versionCleanerService = (VersionCleanerService) jobData.get("versionCleanerService");
		versionCleanerService.cleanVersions(500, null);
	}
}
