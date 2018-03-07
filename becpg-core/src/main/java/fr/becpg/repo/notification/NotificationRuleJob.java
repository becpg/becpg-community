package fr.becpg.repo.notification;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NotificationRuleJob implements Job {
	
	private static final Log logger = LogFactory.getLog(NotificationRuleJob.class);

	private static final String KEY_NOTIFICATION_RULE_SERVICE = "notificationRuleService";
	private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";
	private static final String KEY_TRANSACTION_SERVICE = "transactionService";
	
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Start of Notification Job.");

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		final TransactionService transactionService = (TransactionService) jobData.get(KEY_TRANSACTION_SERVICE);
		final TenantAdminService tenantAdminService = (TenantAdminService) jobData.get(KEY_TENANT_ADMIN_SERVICE);
		final NotificationRuleService notificationRuleService = (NotificationRuleService) jobData.get(KEY_NOTIFICATION_RULE_SERVICE);
		
		AuthenticationUtil.runAsSystem(() -> {
			return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				notificationRuleService.sendNotifications();
				return null;				
			}, true, false);
		});
		
		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(() -> {
					return transactionService.getRetryingTransactionHelper().doInTransaction(()->{						
						notificationRuleService.sendNotifications();
						return null;
					}, true, false);
					
				} , tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		}
		
		logger.info("End of Notification Job.");
	}

}
