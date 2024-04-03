package fr.becpg.repo.entity.policy;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.report.entity.EntityReportService;

public class ArchivedEntityPolicy extends AbstractBeCPGPolicy implements OnAddAspectPolicy, OnRemoveAspectPolicy {

	private EntityFormatService entityFormatService;

	private EntityReportService entityReportService;

	private BatchQueueService batchQueueService;

	private FormulationService<FormulatedEntity> formulationService;

	private static final String KEY_ASPECT_ADDED = "aspectAdded";

	private static final String KEY_ASPECT_REMOVED = "aspectRemoved";

	public void setFormulationService(FormulationService<FormulatedEntity> formulationService) {
		this.formulationService = formulationService;
	}

	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setEntityFormatService(EntityFormatService entityFormatService) {
		this.entityFormatService = entityFormatService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_ARCHIVED_ENTITY,
				new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(OnRemoveAspectPolicy.QNAME, BeCPGModel.ASPECT_ARCHIVED_ENTITY,
				new JavaBehaviour(this, "onRemoveAspect"));
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		queueNode(KEY_ASPECT_ADDED, nodeRef);
	}

	@Override
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
		queueNode(KEY_ASPECT_REMOVED, nodeRef);
	}

	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		if (KEY_ASPECT_ADDED.equals(key)) {
			for (NodeRef nodeRef : pendingNodes) {
				String entityDescription = nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE) + " - "
						+ nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				BatchInfo batchInfo = new BatchInfo("entityArchiving", "becpg.batch.entityArchiving",
						entityDescription);
				batchInfo.setRunAsSystem(true);
				batchInfo.setPriority(BatchPriority.HIGH);
				BatchStep<NodeRef> formulationStep = new BatchStep<>();
				formulationStep.setStepDescId("becpg.batch.entityArchiving.formulation");
				formulationStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(List.of(nodeRef)));
				formulationStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef entry) throws Throwable {
						formulationService.formulate(nodeRef);
					}
				});

				BatchStep<NodeRef> reportStep = new BatchStep<>();
				reportStep.setStepDescId("becpg.batch.entityArchiving.reports");
				reportStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(List.of(nodeRef)));
				reportStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef entry) throws Throwable {
						entityReportService.generateReports(nodeRef, true);
					}
				});

				BatchStep<NodeRef> archivingStep = new BatchStep<>();
				archivingStep.setStepDescId("becpg.batch.entityArchiving.archiving");
				archivingStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(List.of(nodeRef)));
				archivingStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef entry) throws Throwable {
						entityFormatService.convertToFormat(nodeRef, EntityFormat.JSON);
					}
				});

				batchQueueService.queueBatch(batchInfo, List.of(formulationStep, reportStep, archivingStep));
			}
		} else if (KEY_ASPECT_REMOVED.equals(key)) {
			for (NodeRef nodeRef : pendingNodes) {
				String entityDescription = nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE) + " - "
						+ nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				BatchInfo batchInfo = new BatchInfo("entityUnarchiving", "becpg.batch.entityUnarchiving",
						entityDescription);
				batchInfo.setRunAsSystem(true);
				batchInfo.setPriority(BatchPriority.HIGH);
				BatchStep<NodeRef> batchStep = new BatchStep<>();

				batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(List.of(nodeRef)));
				batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef entry) throws Throwable {
						entityFormatService.convertToFormat(nodeRef, EntityFormat.NODE);
					}
				});

				batchQueueService.queueBatch(batchInfo, List.of(batchStep));
			}
		}
	}

}
