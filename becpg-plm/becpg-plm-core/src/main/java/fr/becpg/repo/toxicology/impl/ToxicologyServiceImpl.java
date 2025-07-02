package fr.becpg.repo.toxicology.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.toxicology.ToxicologyService;

/**
 * <p>ToxicologyServiceImpl class.</p>
 *
 * @author valentin
 */
@Service("toxicologyService")
public class ToxicologyServiceImpl implements ToxicologyService {

	private static final Log logger = LogFactory.getLog(ToxicologyServiceImpl.class);

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private Repository repository;
	
	@Autowired
	private RepoService repoService;
	
	@Autowired
	private BatchQueueService batchQueueService;
	
	@Override
	public Double getMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		NodeRef toxIngNodeRef = getToxIngNodeRef(ingNodeRef, toxNodeRef);
		if (!nodeService.exists(toxIngNodeRef)) {
			logger.debug("ToxIng does not exist for ing: " + ingNodeRef + " and tox:" + toxNodeRef);
			return null;
		}
		return (Double) nodeService.getProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_MAX_VALUE);
	}

	/** {@inheritDoc} */
	@Override
	public void updateToxIngAfterIngUpdate(NodeRef ingNodeRef) {
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				List<NodeRef> toxList = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_TOX)
						.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
						.list();
				BatchInfo batchInfo = new BatchInfo("updateOrCreateToxIng-" + ingNodeRef.getId(), "becpg.batch.updateOrCreateToxIng");
				batchInfo.setRunAsSystem(true);
				BatchStep<NodeRef> batchStep = new BatchStep<>();
				batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(toxList));
				batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef toxNodeRef) throws Throwable {
						if (nodeService.exists(toxNodeRef) && nodeService.exists(ingNodeRef)) {
							updateOrCreateToxIng(ingNodeRef, toxNodeRef);
						}
					}
				});
				batchQueueService.queueBatch(batchInfo, List.of(batchStep));
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public void updateToxIngAfterToxUpdate(NodeRef toxNodeRef) {
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				List<NodeRef> toxIngList = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_TOX_ING)
						.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
						.andPropEquals(PLMModel.PROP_TOX_ING_TOX, toxNodeRef.toString())
						.list();
				BatchInfo batchInfo = new BatchInfo("updateToxIng-" + toxNodeRef.getId(), "becpg.batch.updateToxIng");
				batchInfo.setRunAsSystem(true);
				BatchStep<NodeRef> batchStep = new BatchStep<>();
				batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(toxIngList));
				batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef toxIngNodeRef) throws Throwable {
						if (nodeService.exists(toxIngNodeRef)) {
							updateToxIng(toxIngNodeRef);
						}
					}
				});
				batchQueueService.queueBatch(batchInfo, List.of(batchStep));
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public void deleteToxIngBeforeIngDelete(NodeRef ingNodeRef) {
		List<NodeRef> toxIngList = BeCPGQueryBuilder.createQuery().inDB()
				.ofType(PLMModel.TYPE_TOX_ING)
				.andPropEquals(PLMModel.PROP_TOX_ING_ING, ingNodeRef.toString())
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
				.list();
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				// make sure the node is actually deleted
				if (!nodeService.exists(ingNodeRef)) {
					deleteToxIngListInBatch(toxIngList, ingNodeRef.getId());
				}
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public void deleteToxIngBeforeToxDelete(NodeRef toxNodeRef) {
		List<NodeRef> toxIngList = BeCPGQueryBuilder.createQuery().inDB()
				.ofType(PLMModel.TYPE_TOX_ING)
				.andPropEquals(PLMModel.PROP_TOX_ING_TOX, toxNodeRef.toString())
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
				.list();
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				// make sure the node is actually deleted
				if (!nodeService.exists(toxNodeRef)) {
					deleteToxIngListInBatch(toxIngList, toxNodeRef.getId());
				}
			}
		});
	}
	
	private void deleteToxIngListInBatch(List<NodeRef> toxIngList, String id) {
		BatchInfo batchInfo = new BatchInfo("deleteToxIng-" + id + "-" + System.currentTimeMillis(), "becpg.batch.deleteToxIng");
		batchInfo.setRunAsSystem(true);
		BatchStep<NodeRef> batchStep = new BatchStep<>();
		batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(toxIngList));
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
			@Override
			public void process(NodeRef toxIngNodeRef) throws Throwable {
				if (nodeService.exists(toxIngNodeRef)) {
					logger.debug("Deleting toxIngNodeRef: " + toxIngNodeRef);
					nodeService.addAspect(toxIngNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(toxIngNodeRef);
				}
			}
		});
		batchQueueService.queueBatch(batchInfo, List.of(batchStep));
	}

	private void updateOrCreateToxIng(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		if (Boolean.TRUE.equals(nodeService.getProperty(ingNodeRef, BeCPGModel.PROP_IS_DELETED))
				|| Boolean.TRUE.equals(nodeService.getProperty(toxNodeRef, BeCPGModel.PROP_IS_DELETED))) {
			return;
		}
		NodeRef toxIngNodeRef = getToxIngNodeRef(ingNodeRef, toxNodeRef);
		if (!nodeService.exists(toxIngNodeRef)) {
			logger.debug("Create toxIng node from ingNodeRef: " + ingNodeRef + " and toxNodeRef: " + toxNodeRef);
			NodeRef listContainer = getCharactListContainer();
			NodeRef toxIngFolder = nodeService.getChildByName(listContainer, ContentModel.ASSOC_CONTAINS, PlmRepoConsts.PATH_TOX_ING);
			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NODE_UUID, toxIngNodeRef.getId());
			props.put(PLMModel.PROP_TOX_ING_ING, ingNodeRef);
			props.put(PLMModel.PROP_TOX_ING_TOX, toxNodeRef);
			toxIngNodeRef = nodeService
					.createNode(toxIngFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_TOX_ING, props).getChildRef();
		}
		updateToxIng(toxIngNodeRef);
	}

	private NodeRef getToxIngNodeRef(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		String toxIngId = ingNodeRef.getId().substring(0, 18) + toxNodeRef.getId().substring(0, 18);
		return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, toxIngId);
	}

	private NodeRef getCharactListContainer() {
		NodeRef companyHomeNodeRef = repository.getCompanyHome();
		NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
		NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
		return nodeService.getChildByName(charactsNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
	}

	private void updateToxIng(NodeRef toxIngNodeRef) {
		NodeRef toxNodeRef = (NodeRef) nodeService.getProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_TOX);
		NodeRef ingNodeRef = (NodeRef) nodeService.getProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_ING);
		if (toxNodeRef != null && nodeService.exists(toxNodeRef) && ingNodeRef != null && nodeService.exists(ingNodeRef)) {
			Boolean calculateSystemic = (Boolean) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_SYSTEMIC);
			if (Boolean.TRUE.equals(calculateSystemic)) {
				Double podMax = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_POD_SYSTEMIC);
				Double mosMoe = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MOS_MOE);
				Double finalQuantity = (Double) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_VALUE);
				String absorptionType = (String) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_ABSORPTION_TYPE);
				Double dermalAbsorption = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION);
				Double oralAbsorption = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_ORAL_ABSORPTION);
				Double finalAbsorption = dermalAbsorption;
				if ("Oral".equals(absorptionType)) {
					finalAbsorption = oralAbsorption;
				} else if ("Worst".equals(absorptionType)) {
					finalAbsorption = 100d;
				}
				if (podMax != null && finalAbsorption != null && finalAbsorption != 0 && mosMoe != null && mosMoe != 0 && finalQuantity != null) {
					Double systemicValue = (podMax * 60 / (finalQuantity * finalAbsorption / 100 * mosMoe)) * 100;
					logger.debug("Calculate systemic value from ingNodeRef: " + ingNodeRef + " and toxNodeRef: " + toxNodeRef + ", systemicValue=" + systemicValue);
					nodeService.setProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_SYSTEMIC_VALUE, systemicValue);
				}
			}

			Boolean calculateMax = (Boolean) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_MAX);
			if (Boolean.TRUE.equals(calculateMax)) {
				Double maxValue = computeMaxValue(ingNodeRef, toxNodeRef);
				Double systemicValue = (Double) nodeService.getProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_SYSTEMIC_VALUE);
				if (maxValue == null) {
					maxValue = systemicValue;
				} else if (systemicValue != null) {
					maxValue = Math.min(maxValue, systemicValue);
				}
				logger.debug("Calculate max value from ingNodeRef: " + ingNodeRef + " and toxNodeRef: " + toxNodeRef + ", maxValue=" + maxValue);
				nodeService.setProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_MAX_VALUE, maxValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Double computeMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		List<String> toxTypes = (List<String>) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_TYPES);
		if (toxTypes != null) {
			List<Double> maxList = new ArrayList<>();
			
			if (toxTypes.contains(ToxType.OcularIrritation.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION);
				if (value != null) {
					maxList.add(value);
				}
			}
			if (toxTypes.contains(ToxType.PhototoxicalPotential.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC);
				if (value != null) {
					maxList.add(value);
				}
			}
			if (toxTypes.contains(ToxType.Sensitization.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SENSITIZATION);
				if (value != null) {
					maxList.add(value);
				}
			}
			if (toxTypes.contains(ToxType.SkinIrritationRinseOff.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF);
				if (value != null) {
					maxList.add(value);
				}
			}
			if (toxTypes.contains(ToxType.SkinIrritationLeaveOn.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_LEAVE_ON);
				if (value != null) {
					maxList.add(value);
				}
			}
			
			if (!maxList.isEmpty()) {
				return maxList.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
			}
		}
		return null;
	}
}
