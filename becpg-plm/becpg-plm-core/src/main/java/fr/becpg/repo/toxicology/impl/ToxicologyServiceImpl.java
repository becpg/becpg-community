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
 * @author valentin
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
		if (logger.isDebugEnabled()) {
			logger.debug("Getting max value for ingredient: " + ingNodeRef + " and tox: " + toxNodeRef);
		}
		String maxValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES);
		String toxName = extractToxName(toxNodeRef);
		Double result = ToxHelper.extractToxValue(maxValues, toxName);
		if (logger.isDebugEnabled()) {
			logger.debug("Max value extracted: " + result + " from maxValues: " + maxValues + " for toxName: " + toxName);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void updateIngredient(NodeRef ingNodeRef) {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting ingredient update for: " + ingNodeRef);
		}
		String oldSystemicValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES);
		String oldMaxValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES);
		if (logger.isDebugEnabled()) {
			logger.debug("Current systemic values: " + oldSystemicValues + ", max values: " + oldMaxValues);
		}
		
		if (Boolean.TRUE.equals(nodeService.getProperty(ingNodeRef, BeCPGModel.PROP_IS_DELETED))) {
			if (logger.isDebugEnabled()) {
				logger.debug("Ingredient is deleted, clearing tox values");
			}
			if (oldSystemicValues != null) {
				nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES, null);
			}
			if (oldMaxValues != null) {
				nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES, null);
			}
			return;
		}
		
		List<NodeRef> toxList = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_TOX)
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list()
				.stream().filter(n -> !Boolean.TRUE.equals(nodeService.getProperty(n, BeCPGModel.PROP_IS_DELETED))).toList();
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + toxList.size() + " active tox nodes to process");
		}
		
		String previousSystemicValues = oldSystemicValues;
		String previousMaxValues = oldMaxValues;
		
		for (NodeRef toxNodeRef : toxList) {
			String toxName = extractToxName(toxNodeRef);
			if (logger.isDebugEnabled()) {
				logger.debug("Processing tox: " + toxNodeRef + " with name: " + toxName);
			}
			
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
			
			previousSystemicValues = newSystemicValues;
			previousMaxValues = newMaxValues;
		}
		
		if (!Objects.equals(oldSystemicValues, previousSystemicValues)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Updating systemic values from: " + oldSystemicValues + " to: " + previousSystemicValues);
			}
			nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES, previousSystemicValues);
		}
		if (!Objects.equals(oldMaxValues, previousMaxValues)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Updating max values from: " + oldMaxValues + " to: " + previousMaxValues);
			}
			nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES, previousMaxValues);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Ingredient update completed for: " + ingNodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void updateIngredientsFromTox(NodeRef toxNodeRef) {
		String toxName = extractToxName(toxNodeRef);
		if (logger.isDebugEnabled()) {
			logger.debug("Scheduling batch update of ingredients from tox: " + toxNodeRef + " (name: " + toxName + ")");
		}
		
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				if (logger.isDebugEnabled()) {
					logger.debug("Transaction committed, starting batch update for tox: " + toxName);
				}
				List<NodeRef> ingList = findAllUndeletedIngredients();
				if (logger.isDebugEnabled()) {
					logger.debug("Found " + ingList.size() + " ingredients to update");
				}
				
				BatchInfo batchInfo = new BatchInfo("updateToxIngAfterToxUpdate-" + toxName, "becpg.batch.updateToxIng");
				batchInfo.setRunAsSystem(true);
				BatchStep<NodeRef> batchStep = new BatchStep<>();
				batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(ingList));
				batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
					@Override
					public void process(NodeRef ingNodeRef) throws Throwable {
						if (nodeService.exists(ingNodeRef) && nodeService.exists(toxNodeRef)) {
							updateIngFromTox(ingNodeRef, toxNodeRef);
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Skipping non-existent node - ingredient: " + ingNodeRef + ", tox: " + toxNodeRef);
							}
						}
					}
				});
				batchQueueService.queueBatch(batchInfo, List.of(batchStep));
				if (logger.isDebugEnabled()) {
					logger.debug("Batch queued for tox update: " + toxName);
				}
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public void removeToxFromIngredients(NodeRef toxNodeRef) {
		String toxName = extractToxName(toxNodeRef);
		if (logger.isDebugEnabled()) {
			logger.debug("Scheduling removal of tox: " + toxNodeRef + " (name: " + toxName + ") from ingredients");
		}
		
		List<NodeRef> ingList = findAllUndeletedIngredients();
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + ingList.size() + " ingredients for tox removal");
		}
		
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				if (logger.isDebugEnabled()) {
					logger.debug("Transaction committed, checking if tox node is actually deleted: " + toxNodeRef);
				}
				// make sure the node is actually deleted
				if (!nodeService.exists(toxNodeRef)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Tox node confirmed deleted, proceeding with removal from ingredients");
					}
					removeToxFromIngList(toxName, ingList);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Tox node still exists, skipping removal from ingredients");
					}
				}
			}
		});
	}

	private List<NodeRef> findAllUndeletedIngredients() {
		logger.debug("Searching for all undeleted ingredients");
		List<NodeRef> ingredients = BeCPGQueryBuilder.createQuery().inDB()
				.ofType(PLMModel.TYPE_ING)
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
				.list().stream().filter(n -> !Boolean.TRUE.equals(nodeService.getProperty(n, BeCPGModel.PROP_IS_DELETED))).toList();
		logger.debug("Found " + ingredients.size() + " undeleted ingredients");
		return ingredients;
	}
	
	private void removeToxFromIngList(String toxName, List<NodeRef> ingList) {
		logger.debug("Starting batch removal of tox: " + toxName + " from " + ingList.size() + " ingredients");
		
		BatchInfo batchInfo = new BatchInfo("deleteToxIng-" + toxName, "becpg.batch.deleteToxIng");
		batchInfo.setRunAsSystem(true);
		BatchStep<NodeRef> batchStep = new BatchStep<>();
		batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(ingList));
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
			@Override
			public void process(NodeRef ingNodeRef) throws Throwable {
				if (nodeService.exists(ingNodeRef)) {
					logger.debug("Removing tox: " + toxName + " from ingredient: " + ingNodeRef);
					
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
				} else {
					logger.debug("Ingredient no longer exists: " + ingNodeRef);
				}
			}
		});
		batchQueueService.queueBatch(batchInfo, List.of(batchStep));
		logger.debug("Batch queued for tox removal: " + toxName);
	}

	private String extractToxName(NodeRef toxNodeRef) {
		logger.debug("Extracting tox name from: " + toxNodeRef);
		boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
		try {
			String toxName = MLTextHelper.getClosestValue((MLText) nodeService.getProperty(toxNodeRef, BeCPGModel.PROP_CHARACT_NAME), Locale.getDefault());
			logger.debug("Extracted tox name: " + toxName);
			return toxName;
		} finally {
			MLPropertyInterceptor.setMLAware(wasMLAware);
		}
	}

	private Double computeMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef, String toxName, Double systemicValue) {
		logger.debug("Computing max value for ingredient: " + ingNodeRef + ", tox: " + toxName + ", systemic value: " + systemicValue);
		
		Boolean calculateMax = (Boolean) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_MAX);
		logger.debug("Calculate max flag: " + calculateMax);
		
		if (Boolean.TRUE.equals(calculateMax)) {
			Double maxValue = computeMaxValue(ingNodeRef, toxNodeRef);
			logger.debug("Computed raw max value: " + maxValue);
			
			if (maxValue == null) {
				maxValue = systemicValue;
				logger.debug("No raw max value, using systemic value: " + maxValue);
			} else if (systemicValue != null) {
				maxValue = Math.min(maxValue, systemicValue);
				logger.debug("Using minimum of raw max and systemic: " + maxValue);
			}
			
			logger.debug("Final max value for " + toxName + ": " + maxValue);
			if (maxValue != null) {
				return maxValue;
			}
		}
		return null;
	}

	private Double computeSystemicValue(NodeRef ingNodeRef, NodeRef toxNodeRef, String toxName) {
		logger.debug("Computing systemic value for ingredient: " + ingNodeRef + ", tox: " + toxName);
		
		Boolean calculateSystemic = (Boolean) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_SYSTEMIC);
		logger.debug("Calculate systemic flag: " + calculateSystemic);
		
		Double systemicValue = null;
		if (Boolean.TRUE.equals(calculateSystemic)) {
			Double podMax = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_POD_SYSTEMIC);
			Double mosMoe = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MOS_MOE);
			Double finalQuantity = (Double) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_VALUE);
			String absorptionType = (String) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_ABSORPTION_TYPE);
			Double dermalAbsorption = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION);
			Double oralAbsorption = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_ORAL_ABSORPTION);
			
			logger.debug("Systemic calculation inputs - podMax: " + podMax + ", mosMoe: " + mosMoe + 
						", finalQuantity: " + finalQuantity + ", absorptionType: " + absorptionType + 
						", dermalAbsorption: " + dermalAbsorption + ", oralAbsorption: " + oralAbsorption);
			
			Double finalAbsorption = dermalAbsorption;
			if ("Oral".equals(absorptionType)) {
				finalAbsorption = oralAbsorption;
			} else if ("Worst".equals(absorptionType)) {
				finalAbsorption = 100d;
			}
			logger.debug("Final absorption value: " + finalAbsorption);
			
			if (podMax != null && finalAbsorption != null && finalAbsorption != 0 && mosMoe != null && mosMoe != 0 && finalQuantity != null) {
				systemicValue = (podMax * 60 / (finalQuantity * finalAbsorption / 100 * mosMoe)) * 100;
				if (logger.isDebugEnabled()) {
					logger.debug("Calculate systemic value from ingNodeRef: " + ingNodeRef + " and toxNodeRef: " + toxNodeRef + ", systemicValue=" + systemicValue);
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Missing required values for systemic calculation");
				}
			}
		}
		return systemicValue;
	}
	
	private void updateIngFromTox(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		String currentSystemicValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES);
		String currentMaxValues = (String) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_VALUES);
		if ((currentSystemicValues == null || currentSystemicValues.isBlank()) && (currentMaxValues == null || currentMaxValues.isBlank()))  {
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Processing ingredient: " + ingNodeRef + " for tox: " + toxNodeRef);
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
			if (logger.isDebugEnabled()) {
				logger.debug("Computing max value from tox types: " + toxTypes + " for ingredient: " + ingNodeRef);
			}
			List<Double> maxList = new ArrayList<>();
			
			if (toxTypes.contains(ToxType.OcularIrritation.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION);
				if (value != null) {
					maxList.add(value);
					if (logger.isDebugEnabled()) {
						logger.debug("Added ocular irritation value: " + value);
					}
				}
			}
			if (toxTypes.contains(ToxType.PhototoxicalPotential.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC);
				if (value != null) {
					maxList.add(value);
					if (logger.isDebugEnabled()) {
						logger.debug("Added phototoxic value: " + value);
					}
				}
			}
			if (toxTypes.contains(ToxType.Sensitization.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SENSITIZATION);
				if (value != null) {
					maxList.add(value);
					if (logger.isDebugEnabled()) {
						logger.debug("Added sensitization value: " + value);
					}
				}
			}
			if (toxTypes.contains(ToxType.SkinIrritationRinseOff.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF);
				if (value != null) {
					maxList.add(value);
					if (logger.isDebugEnabled()) {
						logger.debug("Added skin irritation rinse-off value: " + value);
					}
				}
			}
			if (toxTypes.contains(ToxType.SkinIrritationLeaveOn.toString())) {
				Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_LEAVE_ON);
				if (value != null) {
					maxList.add(value);
					if (logger.isDebugEnabled()) {
						logger.debug("Added skin irritation leave-on value: " + value);
					}
				}
			}
			
			if (!maxList.isEmpty()) {
				Double result = maxList.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
				if (logger.isDebugEnabled()) {
					logger.debug("Computed minimum max value: " + result + " from values: " + maxList);
				}
				return result;
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("No tox types found for toxNodeRef: " + toxNodeRef);
			}
		}
		return null;
	}
}