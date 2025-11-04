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
		int total = generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, BatchPriority.VERY_HIGH, MAX_RESULTS);
		if (total < MAX_RESULTS) {
			total += generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, BatchPriority.HIGH, MAX_RESULTS - total);
		}
		if (total < MAX_RESULTS) {
			total += generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, BatchPriority.MEDIUM, MAX_RESULTS - total);
		}
		if (total < MAX_RESULTS) {
			total += generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, BatchPriority.LOW, MAX_RESULTS - total);
		}
		if (total < MAX_RESULTS) {
			total += generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, BatchPriority.VERY_LOW, MAX_RESULTS - total);
		}
		if (total < MAX_RESULTS) {
			generatePendingReports(nodeService, entityVersionService, entityReportService, batchQueueService, null, MAX_RESULTS - total);
		}
	}
	
	private int generatePendingReports(NodeService nodeService, EntityVersionService entityVersionService, EntityReportService entityReportService, BatchQueueService batchQueueService, BatchPriority priority, int maxResults) {
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
		List<NodeRef> pendingNodes;
		if (priority == null) {
			pendingNodes = new ArrayList<>(BeCPGQueryBuilder.createQuery()
					.withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
					.maxResults(maxResults)
					.inDBIfPossible().list());
			if (pendingNodes.size() < maxResults) {
				List<NodeRef> versionPendingNodes = BeCPGQueryBuilder.createQuery()
						.withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
						.maxResults(maxResults - pendingNodes.size())
						.inStore(RepoConsts.VERSION_STORE)
						.inDBIfPossible().list();
				pendingNodes.addAll(versionPendingNodes);
			}
		} else {
			pendingNodes = new ArrayList<>(BeCPGQueryBuilder.createQuery()
					.withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
					.andPropEquals(BeCPGModel.PROP_PENDING_ENTITY_REPORT_PRIORITY, priority.toString())
					.maxResults(maxResults)
					.inDBIfPossible().list());
			
			if (pendingNodes.size() < maxResults) {
				List<NodeRef> versionPendingNodes = BeCPGQueryBuilder.createQuery()
						.withAspect(BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT)
						.andPropEquals(BeCPGModel.PROP_PENDING_ENTITY_REPORT_PRIORITY, priority.toString())
						.maxResults(maxResults - pendingNodes.size())
						.inStore(RepoConsts.VERSION_STORE)
						.inDBIfPossible().list();
				pendingNodes.addAll(versionPendingNodes);
			}
		}
		
		BatchInfo batchInfo = new BatchInfo("generatePendingReports-" + priority, "becpg.batch.entity.generatePendingReports");
		batchInfo.setRunAsSystem(true);
		batchInfo.setPriority(priority == null ? BatchPriority.MEDIUM : priority);
		BatchProcessWorkProvider<NodeRef> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(pendingNodes));
		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				if (nodeService.exists(entityNodeRef)) {
					NodeRef extractedNode = entityNodeRef;
					if (VersionHelper.isVersion(entityNodeRef)
							&& (nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT) != null)) {
						extractedNode = entityVersionService.extractVersion(entityNodeRef);
					}
					entityReportService.generateReports(extractedNode, entityNodeRef);
					nodeService.removeAspect(entityNodeRef, BeCPGModel.ASPECT_PENDING_ENTITY_REPORT_ASPECT);
				}
			}
		};
		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);
		return pendingNodes.size();
	}
}
