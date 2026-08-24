package fr.becpg.repo.report.entity;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
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

	private static final Log logger = LogFactory.getLog(EntityReportJob.class);

	/** Constant <code>MAX_RESULTS=50</code> */
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
		BehaviourFilter policyBehaviourFilter = (BehaviourFilter) jobData.get("policyBehaviourFilter");
		int total = generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, transactionService, policyBehaviourFilter, BatchPriority.VERY_HIGH, MAX_RESULTS);
		if (total < MAX_RESULTS) {
			total += generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, transactionService, policyBehaviourFilter, BatchPriority.HIGH, MAX_RESULTS - total);
		}
		if (total < MAX_RESULTS) {
			total += generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, transactionService, policyBehaviourFilter, BatchPriority.MEDIUM, MAX_RESULTS - total);
		}
		if (total < MAX_RESULTS) {
			total += generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, transactionService, policyBehaviourFilter, BatchPriority.LOW, MAX_RESULTS - total);
		}
		if (total < MAX_RESULTS) {
			generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, transactionService, policyBehaviourFilter, BatchPriority.VERY_LOW, MAX_RESULTS - total);
		}
	}
	
	/**
	 * <p>generatePendingReports.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object
	 * @param entityReportService a {@link fr.becpg.repo.report.entity.EntityReportService} object
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 * @param transactionService a {@link org.alfresco.service.transaction.TransactionService} object
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 * @param priority a {@link fr.becpg.repo.batch.BatchPriority} object
	 * @param maxResults a int
	 * @return a int
	 */
	private int generatePendingReports(NodeService nodeService, EntityVersionService entityVersionService, EntityReportService entityReportService,
			BatchQueueService batchQueueService, TransactionService transactionService, BehaviourFilter policyBehaviourFilter,
			BatchPriority priority, int maxResults) {
		String batchId = "generatePendingReports-" + priority;
		String batchDescId = "becpg.batch.entity.generatePendingReports." + priority;
		String batchFullId = batchId + "|" + batchDescId;
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
		List<NodeRef> pendingNodes = new ArrayList<>();
		if (priority != null) {
			pendingNodes.addAll(BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
					.andPropEquals(BeCPGModel.PROP_PENDING_ENTITY_REPORT_PRIORITY, priority.toString()).maxResults(maxResults)
					.excludeProp(BeCPGModel.PROP_BATCH_ERROR_IDS, batchFullId).inDBIfPossible().list());

			if (pendingNodes.size() < maxResults) {
				List<NodeRef> versionPendingNodes = BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
						.andPropEquals(BeCPGModel.PROP_PENDING_ENTITY_REPORT_PRIORITY, priority.toString())
						.maxResults(maxResults - pendingNodes.size()).excludeProp(BeCPGModel.PROP_BATCH_ERROR_IDS, batchFullId)
						.inStore(RepoConsts.VERSION_STORE).inDBIfPossible().list();
				pendingNodes.addAll(versionPendingNodes);
			}

			removeDeletedNodes(nodeService, pendingNodes, batchId);
		}

		if (!pendingNodes.isEmpty()) {
			BatchInfo batchInfo = new BatchInfo(batchId, batchDescId);
			batchInfo.setRunAsSystem(true);
			batchInfo.setPriority(priority == null ? BatchPriority.MEDIUM : priority);
			batchInfo.setWorkerThreads(1);
			BatchStep<NodeRef> batchStep = batchQueueService.createBatchStepWithErrorHandling(batchInfo, pendingNodes,
					new BatchProcessor.BatchProcessWorkerAdaptor<>() {
				@Override
				public void process(NodeRef nodeRef) throws Throwable {

					/*
					 * The step declares itself non transactional, so nothing wraps this
					 * method: each phase opens the transaction it needs and closes it.
					 *
					 * Wrapped, the entry held one transaction from the first read to the
					 * last write, generation included - and generation waits on the report
					 * server. A transaction held across that wait keeps its row locks and
					 * stops InnoDB from purging its undo records, which every other write
					 * on the instance then pays for.
					 */
					Boolean pending = transactionService.getRetryingTransactionHelper()
							.doInTransaction(() -> nodeService.exists(nodeRef), true, true);

					if (!Boolean.TRUE.equals(pending)) {
						return;
					}

					NodeRef extractedNode = nodeRef;

					if (Boolean.TRUE.equals(transactionService.getRetryingTransactionHelper()
							.doInTransaction(() -> VersionHelper.isVersion(nodeRef)
									&& (nodeService.getProperty(nodeRef, BeCPGModel.PROP_ENTITY_FORMAT) != null), true, true))) {
						extractedNode = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							return entityVersionService.extractVersion(nodeRef);
						}, false, true);
					}

					/*
					 * Not wrapped: generation waits on the report server, and the service
					 * opens the short transactions it needs around its reads and its
					 * writes. Wrapping it here would put them all back inside one.
					 */
					entityReportService.generateReports(extractedNode, nodeRef);

					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						// Clearing the pending flag is bookkeeping, not a change to the product:
						// without this the entity would come back modified every time a report is
						// generated, which reformulation and synchronisation both react to.
						policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
						try {
							nodeService.removeAspect(nodeRef, BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT);
						} finally {
							policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
						}
						return null;
					}, false, true);
				}
				
			});

			batchStep.setTransactional(false);
			batchQueueService.queueBatch(batchInfo, List.of(batchStep));
		}
		
		return pendingNodes.size();
	}

	/**
	 * Removes the nodes that no longer exist from the pending list.
	 *
	 * A deleted node still carrying the aspect is returned by the search index but cannot be
	 * flagged in error, so queuing a batch for it would reschedule that batch on every job run.
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param pendingNodes a {@link java.util.List} object
	 * @param batchId a {@link java.lang.String} object
	 */
	private void removeDeletedNodes(NodeService nodeService, List<NodeRef> pendingNodes, String batchId) {
		List<NodeRef> deletedNodes = pendingNodes.stream().filter(nodeRef -> !nodeService.exists(nodeRef)).toList();

		if (!deletedNodes.isEmpty()) {
			pendingNodes.removeAll(deletedNodes);
			logger.warn("Skipping " + deletedNodes.size() + " deleted node(s) still marked as pending report for batch '" + batchId
					+ "': " + deletedNodes);
		}
	}
}
