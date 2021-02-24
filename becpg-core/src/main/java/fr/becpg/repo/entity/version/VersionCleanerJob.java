package fr.becpg.repo.entity.version;

import java.util.Calendar;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

		AuthenticationUtil.runAsSystem(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			return cleanVersions();
			
		}, false, false));

		logger.info("End of Version cleaner Job.");
	}
	
	private boolean cleanVersions() {
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		
		List<NodeRef> nodes = BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION).inDB().ftsLanguage().maxResults(100).andBetween(ContentModel.PROP_MODIFIED, "MIN", "'" + ISO8601DateFormat.format(cal.getTime()) + "'").list();
		
		for (NodeRef node : nodes) {
			if (nodeService.hasAspect(node, ContentModel.ASPECT_TEMPORARY)) {
				nodeService.deleteNode(nodeService.getPrimaryParent(node).getParentRef());
			} else if (!EntityFormat.JSON.toString().equals(entityFormatService.getEntityFormat(node))) {
				entityFormatService.convert(node, EntityFormat.JSON);
			}
		}
		
		return true;
	}

}
