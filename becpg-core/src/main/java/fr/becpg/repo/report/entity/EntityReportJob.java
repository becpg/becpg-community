package fr.becpg.repo.report.entity;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.entity.version.VersionHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>EntityReportJob class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EntityReportJob extends AbstractScheduledLockedJob implements Job {

	private static final int MAX_RESULTS = 50;

	
	/**
	 * <p>Constructor for EntityReportJob.</p>
	 */
	public EntityReportJob() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();
		NodeService nodeService = (NodeService) jobData.get("nodeService");
		EntityVersionService entityVersionService = (EntityVersionService) jobData.get("entityVersionService");
		EntityReportService entityReportService = (EntityReportService) jobData.get("entityReportService");
		BatchQueueService batchQueueService = (BatchQueueService) jobData.get("batchQueueService");
		TransactionService transactionService = (TransactionService) jobData.get("transactionService");
		generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, transactionService);
	}
	
	private void generatePendingReports(NodeService nodeService, EntityVersionService entityVersionService, EntityReportService entityReportService, BatchQueueService batchQueueService, TransactionService transactionService) {
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
		List<NodeRef> pendingNodes = new ArrayList<>(BeCPGQueryBuilder.createQuery()
				.withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
				.maxResults(MAX_RESULTS)
				.inDBIfPossible().list());
		
		if (pendingNodes.size() < MAX_RESULTS) {
			List<NodeRef> versionPendingNodes = BeCPGQueryBuilder.createQuery()
					.withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
					.maxResults(MAX_RESULTS - pendingNodes.size())
					.inStore(RepoConsts.VERSION_STORE)
					.inDBIfPossible().list();
			pendingNodes.addAll(versionPendingNodes);
		}
		
		BatchInfo batchInfo = new BatchInfo("generatePendingReports", "becpg.batch.entity.generatePendingReports");
		batchInfo.setRunAsSystem(true);
		batchInfo.setWorkerThreads(1);
		BatchProcessWorkProvider<NodeRef> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(pendingNodes));
		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				NodeRef extractedNode = entityNodeRef;
				if (VersionHelper.isVersion(entityNodeRef)
						&& (nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT) != null)) {
					extractedNode = transactionService.getRetryingTransactionHelper()
							.doInTransaction(() -> entityVersionService.extractVersion(entityNodeRef), false, true);
				}
				final NodeRef finalExtractedNode = extractedNode;
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					entityReportService.generateReports(finalExtractedNode, entityNodeRef);
					nodeService.removeAspect(entityNodeRef, BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT);
					return null;
				}, false, true);
			}
		};
		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);

	}
}
