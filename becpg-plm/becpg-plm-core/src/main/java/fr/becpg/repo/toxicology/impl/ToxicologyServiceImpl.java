package fr.becpg.repo.toxicology.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.toxicology.ToxHelper;
import fr.becpg.repo.toxicology.ToxicologyService;

/**
 * <p>ToxicologyServiceImpl class.</p>
 *
 * @author matthieu
 */
@Service("toxicologyService")
public class ToxicologyServiceImpl implements ToxicologyService {

	private static final Log logger = LogFactory.getLog(ToxicologyServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private BatchQueueService batchQueueService;

	@Override
	public Double getMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		String maxValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES);
		String toxName = extractToxName(toxNodeRef);
		return ToxHelper.extractToxValue(maxValues, toxName);
	}

	/** {@inheritDoc} */
	@Override
	public void updateToxIngAfterIngUpdate(NodeRef ingNodeRef) {
		if (Boolean.TRUE.equals(nodeService.getProperty(ingNodeRef, BeCPGModel.PROP_IS_DELETED))) {
			return;
		}
		List<NodeRef> toxList = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_TOX)
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list()
				.stream().filter(n -> !Boolean.TRUE.equals(nodeService.getProperty(n, BeCPGModel.PROP_IS_DELETED))).toList();
		for (NodeRef toxNodeRef : toxList) {
			String toxName = extractToxName(toxNodeRef);
			Double systemicValue = computeSystemicValue(ingNodeRef, toxNodeRef, toxName);
			String currentSystemicValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES);
			String newSystemicValues = ToxHelper.appendToxValue(currentSystemicValues, toxName, systemicValue);
			if (!Objects.equals(newSystemicValues, currentSystemicValues)) {
				nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES, newSystemicValues);
			}
			
			Double maxValue = computeMaxValue(ingNodeRef, toxNodeRef, toxName, systemicValue);
			String currentMaxValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES);
			String newMaxValues = ToxHelper.appendToxValue(currentMaxValues, toxName, maxValue);
			if (!Objects.equals(newMaxValues, currentMaxValues)) {
				nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES, newMaxValues);
			}
		}
	}

	@Override
	public void updateToxIngAfterToxUpdate(NodeRef toxNodeRef) {
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				List<NodeRef> ingList = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_ING)
						.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
						.list();
				BatchInfo batchInfo = new BatchInfo("updateToxIngAfterToxUpdate-" + extractToxName(toxNodeRef), "becpg.batch.updateToxIng");
				batchInfo.setRunAsSystem(true);
				BatchStep<NodeRef> batchStep = new BatchStep<>();
				batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(ingList));
				batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef ingNodeRef) throws Throwable {
						if (nodeService.exists(ingNodeRef) && nodeService.exists(toxNodeRef)) {
							updateIngFromTox(ingNodeRef, toxNodeRef);
						}
					}
				});
				batchQueueService.queueBatch(batchInfo, List.of(batchStep));
			}
		});
	}


	/** {@inheritDoc} */

	@Override
	public void deleteToxIngBeforeToxDelete(NodeRef toxNodeRef) {
		List<NodeRef> ingList = BeCPGQueryBuilder.createQuery().inDB()
				.ofType(PLMModel.TYPE_ING)
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
				.list();
		String toxName = extractToxName(toxNodeRef);
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				// make sure the node is actually deleted
				if (!nodeService.exists(toxNodeRef)) {
					removeToxFromIngList(toxName, ingList);
				}
			}

		});
	}
	
	private void removeToxFromIngList(String toxName, List<NodeRef> ingList) {
		BatchInfo batchInfo = new BatchInfo("deleteToxIng-" + toxName, "becpg.batch.deleteToxIng");
		batchInfo.setRunAsSystem(true);
		BatchStep<NodeRef> batchStep = new BatchStep<>();
		batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(ingList));
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
			@Override
			public void process(NodeRef ingNodeRef) throws Throwable {
				if (nodeService.exists(ingNodeRef)) {
					String currentSystemicValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES);
					if (currentSystemicValues != null) {
						String newSystemicValues = ToxHelper.removeToxValue(currentSystemicValues, toxName);
						if (!Objects.equals(newSystemicValues, currentSystemicValues)) {
							nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES, newSystemicValues);
						}
					}
					String currentMaxValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES);
					if (currentMaxValues != null) {
						String newMaxValues = ToxHelper.removeToxValue(currentMaxValues, toxName);
						if (!Objects.equals(newMaxValues, currentMaxValues)) {
							nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES, newMaxValues);
						}
					}
				}
			}
		});
		batchQueueService.queueBatch(batchInfo, List.of(batchStep));
	}

	private String extractToxName(NodeRef toxNodeRef) {
		boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
		try {
			return MLTextHelper.getClosestValue((MLText) nodeService.getProperty(toxNodeRef, BeCPGModel.PROP_CHARACT_NAME), Locale.getDefault());
		} finally {
			MLPropertyInterceptor.setMLAware(wasMLAware);
		}
	}

	private Double computeMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef, String toxName, Double systemicValue) {
		Boolean calculateMax = (Boolean) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_MAX);
		if (Boolean.TRUE.equals(calculateMax)) {
			Double maxValue = computeMaxValue(ingNodeRef, toxNodeRef);
			if (maxValue == null) {
				maxValue = systemicValue;
			} else if (systemicValue != null) {
				maxValue = Math.min(maxValue, systemicValue);
			}
			logger.debug("Calculate max value from ingNodeRef: " + ingNodeRef + " and toxNodeRef: " + toxNodeRef + ", maxValue=" + maxValue);
			if (maxValue != null)  {
				return maxValue;
			}
		}
		return null;
	}

	private Double computeSystemicValue(NodeRef ingNodeRef, NodeRef toxNodeRef, String toxName) {
		Boolean calculateSystemic = (Boolean) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_SYSTEMIC);
		Double systemicValue = null;
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
				systemicValue = (podMax * 60 / (finalQuantity * finalAbsorption / 100 * mosMoe)) * 100;
				logger.debug("Calculate systemic value from ingNodeRef: " + ingNodeRef + " and toxNodeRef: " + toxNodeRef + ", systemicValue=" + systemicValue);
			}
		}
		return systemicValue;
	}
	
	private void updateIngFromTox(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		if (Boolean.TRUE.equals(nodeService.getProperty(ingNodeRef, BeCPGModel.PROP_IS_DELETED))) {
			return;
		}
		String currentSystemicValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES);
		String currentMaxValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES);
		if ((currentSystemicValues == null || currentSystemicValues.isBlank()) && (currentMaxValues == null || currentMaxValues.isBlank()))  {
			return;
		}
		String toxName = extractToxName(toxNodeRef);
		Double systemicValue = computeSystemicValue(ingNodeRef, toxNodeRef, toxName);
		String newSystemicValues = ToxHelper.appendToxValue(currentSystemicValues, toxName, systemicValue);
		if (!Objects.equals(currentSystemicValues, newSystemicValues)) {
			nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES, newSystemicValues);
		}
		Double maxValue = computeMaxValue(ingNodeRef, toxNodeRef, toxName, systemicValue);
		String newMaxValues = ToxHelper.appendToxValue(currentMaxValues, toxName, maxValue);
		if (!Objects.equals(currentMaxValues, newMaxValues)) {
			nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES, newMaxValues);
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
