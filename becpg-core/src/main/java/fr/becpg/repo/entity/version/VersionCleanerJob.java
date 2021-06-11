package fr.becpg.repo.entity.version;

import java.util.Calendar;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

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

	private EntityFormatService entityFormatService;

	private TransactionService transactionService;

	private NodeService nodeService;

	TenantAdminService tenantAdminService;

	public VersionCleanerJob() {
		super();
	}
	
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {

		logger.info("Start of Version cleaner Job.");

		JobDataMap jobData = context.getJobDetail().getJobDataMap();
		
		entityFormatService = (EntityFormatService) jobData.get("entityFormatService");
		transactionService = (TransactionService) jobData.get("transactionService");
		nodeService = (NodeService) jobData.get("nodeService");
		tenantAdminService = (TenantAdminService) jobData.get("tenantAdminService");
		
		AuthenticationUtil.runAsSystem(this::cleanVersions);

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(() -> {
					cleanVersions();
					return null;
				}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		}

		logger.info("End of Version cleaner Job.");
	}
	
	private boolean cleanVersions() {
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		
		List<NodeRef> temporaryNodes = BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION).withAspect(ContentModel.ASPECT_TEMPORARY).inDB().ftsLanguage().maxResults(10).andBetween(ContentModel.PROP_MODIFIED, "MIN", "'" + ISO8601DateFormat.format(cal.getTime()) + "'").list();
		
		for (NodeRef temporaryNode: temporaryNodes) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				nodeService.deleteNode(nodeService.getPrimaryParent(temporaryNode).getParentRef());
				return null;
			}, false, false);
			
			logger.info("deleted node : " + temporaryNode);
		}
		
		List<NodeRef> notConvertedNodes = BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION).excludeAspect(BeCPGModel.ASPECT_ENTITY_FORMAT).excludeAspect(ContentModel.ASPECT_TEMPORARY).inDB().ftsLanguage().maxResults(10).andBetween(ContentModel.PROP_MODIFIED, "MIN", "'" + ISO8601DateFormat.format(cal.getTime()) + "'").list();
		
		for (NodeRef notConvertedNode : notConvertedNodes) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				entityFormatService.convert(notConvertedNode, EntityFormat.JSON);
				return null;
			}, false, false);
			
			logger.info("converted node : " + notConvertedNode);
		}
		
		return true;
	}

}
