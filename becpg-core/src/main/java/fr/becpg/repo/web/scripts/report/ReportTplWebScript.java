/*
 *
 */
package fr.becpg.repo.web.scripts.report;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ReportTplWebScript class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class ReportTplWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ReportTplWebScript.class);

	private static final String ACTION_REFRESH = "refresh";
	private static final String ACTION_DISABLE = "disable";
	private static final String ACTION_ENABLE = "enable";
	private static final String ACTION_DELETE_REPORTS = "deleteReports";
	private static final String ACTION_UPDATE_PERMISSIONS = "updatePermissions";

	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private NodeService nodeService;

	private EntityReportService entityReportService;

	private AssociationService associationService;

	private BatchQueueService batchQueueService;


	/**
	 * <p>Setter for the field <code>batchQueueService</code>.</p>
	 *
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 */
	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}

	/**
	 * <p>Setter for the field <code>entityReportService</code>.</p>
	 *
	 * @param entityReportService a {@link fr.becpg.repo.report.entity.EntityReportService} object.
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) {
		logger.debug("start report webscript");
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String action = templateArgs.get(PARAM_ACTION);

		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

		if (nodeService.exists(nodeRef)) {
			if (ACTION_REFRESH.equals(action)) {
				updateReports(nodeRef, ACTION_REFRESH);
			} else if (ACTION_DISABLE.equals(action)) {
				nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
				updateReports(nodeRef, ACTION_REFRESH);
			} else if (ACTION_ENABLE.equals(action)) {
				nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, false);
				updateReports(nodeRef, ACTION_REFRESH);
			} else if (ACTION_DELETE_REPORTS.equals(action)) {
				deleteReports(nodeRef);
			} else if (ACTION_UPDATE_PERMISSIONS.equals(action)) {
				updateReports(nodeRef, ACTION_UPDATE_PERMISSIONS);
			} else {
				String error = "Unsupported action: " + action;
				logger.error(error);
				throw new WebScriptException(error);
			}
		}
	}

	private void updateReports(NodeRef nodeRef, String action) {

		Boolean isSystem = (Boolean) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM);
		QName classType = (QName) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_CLASS_NAME);
		List<NodeRef> refs = null;

		if (Boolean.TRUE.equals(isSystem) && (classType != null)) {

			refs = BeCPGQueryBuilder.createQuery().ofType(classType).withAspect(ReportModel.ASPECT_REPORT_ENTITY).excludeVersions()
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		} else {
			refs = associationService.getSourcesAssocs(nodeRef, ReportModel.ASSOC_REPORT_TEMPLATES);
		}

		if (refs != null) {

			String entityDescription = nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE) + " - " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

			BatchInfo batchInfo = new BatchInfo( String.format("generateReports-%s-%s", action, nodeRef.getId()), "becpg.batch.entityTpl.generateReports", entityDescription);
			batchInfo.enableNotifyByMail("generate-reports", null);
			batchInfo.setRunAsSystem(true);
			batchInfo.setPriority(BatchPriority.VERY_LOW);
			
			BatchProcessWorkProvider<NodeRef> workProvider = new EntityListBatchProcessWorkProvider<>(refs);

			BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

				@Override
				public void process(NodeRef entityNodeRef) throws Throwable {

					if (ACTION_REFRESH.equals(action)) {

						entityReportService.generateReports(entityNodeRef, entityNodeRef);
					} else if (ACTION_UPDATE_PERMISSIONS.equals(action)) {
						List<NodeRef> reports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS);
						for (NodeRef report : reports) {
							NodeRef tplNodeRef = associationService.getTargetAssoc(report, ReportModel.ASSOC_REPORT_TPL);
							if (nodeRef.equals(tplNodeRef)) {
								entityReportService.setPermissions(nodeRef, report);
							}
						}
					}
				}
			};

			batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);

		}
	}

	private void deleteReports(NodeRef nodeRef) {

		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, ReportModel.ASSOC_REPORT_TPL);

		BatchInfo batchInfo = new BatchInfo( String.format("deleteReports-%s",nodeRef.getId()), "becpg.batch.entityTpl.deleteReports");
		batchInfo.setRunAsSystem(true);
		batchInfo.setPriority(BatchPriority.VERY_LOW);
		
		BatchProcessWorkProvider<AssociationRef> workProvider = new EntityListBatchProcessWorkProvider<>(assocRefs);

		BatchProcessWorker<AssociationRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

			@Override
			public void process(AssociationRef assocRef) throws Throwable {
				if (logger.isDebugEnabled()) {
					logger.debug("Delete report " + assocRef.getSourceRef() + " - name: "
							+ nodeService.getProperty(assocRef.getSourceRef(), ContentModel.PROP_NAME));
				}
				nodeService.addAspect(assocRef.getSourceRef(), ContentModel.ASPECT_TEMPORARY, null);
				nodeService.deleteNode(assocRef.getSourceRef());

			}
		};

		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);

	}
}
