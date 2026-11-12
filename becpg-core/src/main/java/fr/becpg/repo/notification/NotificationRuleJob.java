package fr.becpg.repo.notification;

import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.repo.helper.TenantJobHelper;

/**
 * <p>
 * NotificationRuleJob class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class NotificationRuleJob extends AbstractScheduledLockedJob implements Job {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(NotificationRuleJob.class);

	/** Constant <code>KEY_NOTIFICATION_RULE_SERVICE="notificationRuleService"</code> */
	private static final String KEY_NOTIFICATION_RULE_SERVICE = "notificationRuleService";
	/** Constant <code>KEY_TENANT_ADMIN_SERVICE="tenantAdminService"</code> */
	private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";
	/** Constant <code>KEY_TRANSACTION_SERVICE="transactionService"</code> */
	private static final String KEY_TRANSACTION_SERVICE = "transactionService";

	/** {@inheritDoc} */
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {
		logger.info("Start of Notification Job.");

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final TransactionService transactionService = (TransactionService) jobData.get(KEY_TRANSACTION_SERVICE);
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get(KEY_TENANT_ADMIN_SERVICE);
		final NotificationRuleService notificationRuleService = (NotificationRuleService) jobData.get(KEY_NOTIFICATION_RULE_SERVICE);

		TenantJobHelper.runForEachTenant(tenantAdminService, () -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			notificationRuleService.sendNotifications();
			return null;
		}, false, true));

		logger.info("End of Notification Job.");
	}

}
