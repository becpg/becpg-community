package fr.becpg.repo.entity.version;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class VersionCleanerJob  extends AbstractScheduledLockedJob implements Job {

	private static final Log logger = LogFactory.getLog(VersionCleanerJob.class);

	public VersionCleanerJob() {
		super();
	}
	
	/** {@inheritDoc} */
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {

		logger.info("Start of Version cleaner Job.");

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		AuthenticationUtil.runAsSystem(() -> {
			return cleanVersions(jobData);
		});
		
		
		logger.info("End of Version cleaner Job.");
	}
	
	private boolean cleanVersions(JobDataMap jobData) {
		
		return true;

//		EntityFormatService entityFormatService = (EntityFormatService) jobData.get("entityFormatService");
//		TransactionService transactionService = (TransactionService) jobData.get("transactionService");
//		NodeService nodeService = (NodeService) jobData.get("nodeService");
//		
//		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
//			
//			// TODO
//			
//			return true;
//		});
		
		
	}
	/*
	Version
	 1.0
	 2.0
	 2.1
	
	2.1 Produit (Future)
	
	Version
	 1.0
	 2.0
	
	(1.0) Produit 

	2.0
	*/
	
}
