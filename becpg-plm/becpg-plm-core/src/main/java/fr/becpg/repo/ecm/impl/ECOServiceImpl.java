/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMGroup;
import fr.becpg.model.ECMModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.batch.BatchClosingHook;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.entity.datalist.WUsedFilter;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.repository.model.EffectiveDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.security.BeCPGAccessDeniedException;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Engineering change order service implementation
 *
 * @author quere
 * @version $Id: $Id
 */
@Service("ecoService")
public class ECOServiceImpl implements ECOService {

	private static final Log logger = LogFactory.getLog(ECOServiceImpl.class);
	
	private static final String ACTION_URL_PREFIX = "page/entity-data-lists?list=changeUnitList&nodeRef=%s";

	@Autowired
	private WUsedListService wUsedListService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private ProductService productService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private BatchQueueService batchQueueService;
	
	@Autowired
	private LockService lockService;
	
	@Autowired
	@Qualifier("namespaceService")
    private NamespacePrefixResolver namespacePrefixResolver;
	
	@Autowired
	private EntityActivityService entityActivityService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@Value("${beCPG.eco.impactwused.states}")
	private String impactWUsedStates;

	/** {@inheritDoc} */
	@Override
	public BatchInfo doSimulation(NodeRef ecoNodeRef, boolean calculateWUsed, boolean notifyByMail) {
		return doRunInBatch(ecoNodeRef, ECOState.Simulated, false, calculateWUsed, notifyByMail);
	}

	/** {@inheritDoc} */
	@Override
	public BatchInfo apply(NodeRef ecoNodeRef, boolean deleteOnApply, boolean calculateWUsed, boolean notifyByMail) {
		if (securityService.isCurrentUserAllowed(ECMGroup.ApplyChangeOrder.toString())) {
			return doRunInBatch(ecoNodeRef, ECOState.Applied, deleteOnApply, calculateWUsed, notifyByMail);
		} else {
			throw new BeCPGAccessDeniedException(ECMGroup.ApplyChangeOrder.toString());
		}
	}

	private BatchInfo doRunInBatch(NodeRef ecoNodeRef, final ECOState state, boolean deleteOnApply, boolean calculateWUsed, boolean notifyByMail) {
		
		final ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
		
		boolean isSimulated = ECOState.Simulated.equals(state);
		
		// Do not run if already applied
		if (!ECOState.Applied.equals(ecoData.getEcoState()) && !(ECOState.InError.equals(ecoData.getEcoState()) && isSimulated)) {
			
			String entityDescription = nodeService.getProperty(ecoNodeRef, BeCPGModel.PROP_CODE) + " - " + nodeService.getProperty(ecoNodeRef, ContentModel.PROP_NAME);

			BatchInfo batchInfo = new BatchInfo(String.format(isSimulated ? "simulateECO-%s" : "applyECO-%s", ecoNodeRef.getId()),
					isSimulated ? "becpg.batch.eco.simulate" : "becpg.batch.eco.apply", entityDescription);
			batchInfo.setRunAsSystem(true);
			
			batchInfo.setWorkerThreads(1);
			
			if (notifyByMail) {
				batchInfo.enableNotifyByMail(isSimulated ? "eco.simulate" : "eco.apply", String.format(ACTION_URL_PREFIX, ecoNodeRef.toString()));
			}

			List<BatchStep<Object>> batchStepList = new LinkedList<>();
			
			BatchClosingHook closingHook = null;
			
			boolean applyToAll = Boolean.TRUE.equals(ecoData.getApplyToAll()) && !isSimulated;
			
			if (calculateWUsed || applyToAll) {
				batchStepList.add(createCalculateWUsedListBatchStep(ecoData, true, applyToAll));
			}
			
			if (isSimulated) {
				batchStepList.add(createSimulateECOBatchStep(ecoData));
			} else {
				
				List<NodeRef> impactedProducts = new ArrayList<>();
				
				if (ChangeOrderType.ImpactWUsed.equals(ecoData.getEcoType()) || ChangeOrderType.Replacement.equals(ecoData.getEcoType())) {
					batchStepList.add(createAddChangeOrderAspectStep(batchInfo, ecoData, impactedProducts));
				}
				
				batchStepList.add(createApplyECOStep(ecoData, deleteOnApply));
				batchStepList.add(createCopyPropertiesStep(ecoData));
				
				if (ChangeOrderType.ImpactWUsed.equals(ecoData.getEcoType()) || ChangeOrderType.Replacement.equals(ecoData.getEcoType())) {
					closingHook = () -> closeECO(ecoNodeRef, impactedProducts);
				}
			}
			
			batchQueueService.queueBatch(batchInfo, batchStepList, closingHook);
			
			return batchInfo;
		}
		
		return null;
		
	}

	private BatchStep<Object> createCopyPropertiesStep(ChangeOrderData ecoData) {
		BatchStep<Object> batchStep = new BatchStep<>();
		
		batchStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void beforeStep() {
				
				Set<NodeRef> impactedProducts = provideImpactedProducts(CompositeHelper.getHierarchicalCompoList(ecoData.getWUsedList()), false);
				
				BatchProcessWorkProvider<Object> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(impactedProducts));
				
				batchStep.setWorkProvider(workProvider);
				
			}
		});
		
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Object>() {
			
			@Override
			public void process(Object entry) throws Throwable {
				if (entry instanceof NodeRef) {
					
					boolean isMLAware = MLPropertyInterceptor.setMLAware(true);
					
					try {
						NodeRef nodeRef = (NodeRef) entry;
						
						List<String> propertiesToCopy = ecoData.getPropertiesToCopy();
						
						if (propertiesToCopy != null) {
							for (String propertyToCopy : propertiesToCopy) {
								QName propertyQName = QName.createQName(propertyToCopy.split(":")[0], propertyToCopy.split(":")[1], namespacePrefixResolver);
								Serializable property = nodeService.getProperty(ecoData.getNodeRef(), propertyQName);
								nodeService.setProperty(nodeRef, propertyQName, property);
							}
						}
					} finally {
						MLPropertyInterceptor.setMLAware(isMLAware);
					}
				}
			}
		});
		
		return batchStep;
	}

	private BatchStep<Object> createAddChangeOrderAspectStep(BatchInfo batchInfo, ChangeOrderData ecoData, List<NodeRef> entries) {
		BatchStep<Object> batchStep = new BatchStep<>();
		
		batchStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void beforeStep() {
				
				entries.addAll(provideImpactedProducts(CompositeHelper.getHierarchicalCompoList(ecoData.getWUsedList()), true));
				
				BatchProcessWorkProvider<Object> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(entries));
				
				batchStep.setWorkProvider(workProvider);
				
				ecoData.setEcoState(ECOState.InProgress);
				
				alfrescoRepository.save(ecoData);
			}
		});
		
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Object>() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void process(Object entry) throws Throwable {
				if (entry instanceof NodeRef  nodeRef && !nodeService.hasAspect((NodeRef) entry, ECMModel.ASPECT_CHANGE_ORDER)) {
					
					JSONObject lockInfo = new JSONObject();
					
					lockInfo.put("lockType", "versioning");
					lockInfo.put("sourceNodeRef", ecoData.getNodeRef());
					lockInfo.put("sourceInfo", batchInfo.toJson());
					
					lockService.lock(nodeRef, LockType.WRITE_LOCK, 172800, Lifetime.PERSISTENT, lockInfo.toString());
					
					nodeService.createAssociation(nodeRef, ecoData.getNodeRef(), ECMModel.ASSOC_CHANGE_ORDER_REF);
					
					if (logger.isDebugEnabled()) {
						String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
						logger.debug("adding change order aspect to: " + name + " (" + nodeRef + ")");
					}
				}
			}
			
		});
		
		return batchStep;
	}
	
	private BatchStep<Object> createApplyECOStep(ChangeOrderData ecoData, boolean deleteOnApply) {
		
		BatchStep<Object> applyStep = new BatchStep<>();
		applyStep.setRunAsSystem(false);
		
		ApplyECOProcessWorker processWorker = new ApplyECOProcessWorker(ecoData);
		
		applyStep.setProcessWorker(processWorker);
		
		applyStep.setBatchStepListener(new BatchStepAdapter() {
			
			@Override
			public void beforeStep() {
				
				BatchProcessWorkProvider<Object> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(provideApplyECOEntries(ecoData)));
				
				applyStep.setWorkProvider(workProvider);
				
				ecoData.setEcoState(ECOState.InProgress);
				
				alfrescoRepository.save(ecoData);
			}
			
			@Override
			public void afterStep() {
				
				if (!processWorker.getErrors().isEmpty()) {
					ecoData.setEcoState(ECOState.InError);
					StringBuilder comments = new StringBuilder();
					for (String error : processWorker.getErrors()) {
						comments.append(error + "</br>");
					}
					
					commentService.createComment(ecoData.getNodeRef(), "", comments.toString(), false);
				} else {
					if (!isFuture(ecoData.getEffectiveDate())) {
						ecoData.setEffectiveDate(new Date());
					}
					ecoData.setEcoState(ECOState.Applied);
				}
				
				// Change eco state
				
				alfrescoRepository.save(ecoData);
				
				if (deleteOnApply) {
					nodeService.deleteNode(ecoData.getNodeRef());
				}
				
			}
		});
	
		return applyStep;
	}

	private BatchStep<Object> createRemoveChangeOrderAspectStep(List<NodeRef> entries) {
		BatchStep<Object> batchStep = new BatchStep<>();
		
		batchStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void beforeStep() {
				
				BatchProcessWorkProvider<Object> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(entries));
				
				batchStep.setWorkProvider(workProvider);
				
			}
		});
		
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<Object>() {

			@Override
			public void process(Object entry) throws Throwable {
				
				if (entry instanceof NodeRef && nodeService.hasAspect((NodeRef) entry, ECMModel.ASPECT_CHANGE_ORDER)) {
					
					lockService.unlock((NodeRef) entry);
					nodeService.removeAspect((NodeRef) entry, ECMModel.ASPECT_CHANGE_ORDER);
					
					if (logger.isDebugEnabled()) {
						String name = (String) nodeService.getProperty((NodeRef) entry, ContentModel.PROP_NAME);
						logger.debug("removing change order aspect from: " + name + " (" + entry + ")");
					}
				}
			}
		});
		
		return batchStep;
	}

	private BatchStep<Object> createSimulateECOBatchStep(final ChangeOrderData ecoData) {
		BatchStep<Object> batchStep = new BatchStep<>();
		
		SimulateECOProcessWorker processWorker = new SimulateECOProcessWorker(ecoData);
		
		batchStep.setProcessWorker(processWorker);
		
		batchStep.setBatchStepListener(createSimulationECOBatchAdapter(ecoData, batchStep, processWorker));
		return batchStep;
	}

	private BatchStepAdapter createSimulationECOBatchAdapter(final ChangeOrderData ecoData, BatchStep<Object> batchStep, SimulateECOProcessWorker processWorker) {
		return new BatchStepAdapter() {
			
			@Override
			public void beforeStep() {
				
				List<Object> entries = provideSimulateECOEntries(ecoData);
				
				BatchProcessWorkProvider<Object> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(entries));
				
				batchStep.setWorkProvider(workProvider);
				
				ecoData.setEcoState(ECOState.InProgress);
				
				alfrescoRepository.save(ecoData);
			}
			
			@Override
			public void afterStep() {

				if (!processWorker.getErrors().isEmpty()) {
					ecoData.setEcoState(ECOState.InError);
					StringBuilder comments = new StringBuilder();
					for (String error : processWorker.getErrors()) {
						comments.append(error + "</br>");
					}
					
					commentService.createComment(ecoData.getNodeRef(), "", comments.toString(), false);
				} else {
					ecoData.setEcoState(ECOState.Simulated);
				}
				
				// Change eco state
				alfrescoRepository.save(ecoData);
				
			}
		};
	}

	private Set<NodeRef> provideImpactedProducts(Composite<WUsedListDataItem> composite, boolean includeRoot) {
		Set<NodeRef> impactedProducts = new HashSet<>();
		
		if (composite.getData() != null && Boolean.TRUE.equals(composite.getData().getIsWUsedImpacted()) && (includeRoot || composite.getData().getParent() != null)) {
			impactedProducts.addAll(composite.getData().getSourceItems());
		}
		
		for (Composite<WUsedListDataItem> children : composite.getChildren()) {
			impactedProducts.addAll(provideImpactedProducts(children, includeRoot));
		}
		
		return impactedProducts;
	}
	
	private List<Composite<WUsedListDataItem>> provideApplyECOEntries(final ChangeOrderData ecoData) {
		// Clear changeUnitList
		List<ChangeUnitDataItem> toRemove = new ArrayList<>();
		for (ChangeUnitDataItem cul1 : ecoData.getChangeUnitList()) {
			if (Boolean.FALSE.equals(cul1.getTreated())) {
				toRemove.add(cul1);
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Remove " + toRemove.size() + " previous changeUnit");
		}
		
		ecoData.getChangeUnitList().removeAll(toRemove);
		
		// Visit Wused
		Composite<WUsedListDataItem> composite = CompositeHelper.getHierarchicalCompoList(ecoData.getWUsedList());
		
		checkMissingWUsed(composite);
		
		if (logger.isTraceEnabled()) {
			logger.trace("WUsedList to impact :" + composite.toString());
		}
		
		return findAllImpactedChildrenComposites(composite, ecoData);
	}
	
	@SuppressWarnings("unchecked")
	private List<Object> provideSimulateECOEntries(final ChangeOrderData ecoData) {
		// Clear changeUnitList
		List<ChangeUnitDataItem> toRemove = new ArrayList<>();
		for (ChangeUnitDataItem cul1 : ecoData.getChangeUnitList()) {
			if (Boolean.FALSE.equals(cul1.getTreated())) {
				toRemove.add(cul1);
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Remove " + toRemove.size() + " previous changeUnit");
		}
		
		ecoData.getChangeUnitList().removeAll(toRemove);
		
		// Reset simulation item
		ecoData.getSimulationList().clear();
		
		// Visit Wused
		Composite<WUsedListDataItem> composite = CompositeHelper.getHierarchicalCompoList(ecoData.getWUsedList());
		
		checkMissingWUsed(composite);
		
		if (logger.isTraceEnabled()) {
			logger.trace("WUsedList to impact :" + composite.toString());
		}
		
		List<Object> compositesList = new LinkedList<>();
		
		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {
			if (component.getData() != null && ChangeOrderType.Merge.equals(ecoData.getEcoType())) {
				
				List<Composite<WUsedListDataItem>> composites = new LinkedList<>();
				
				composites.add(component);
				composites.addAll(findAllImpactedChildrenComposites(component, ecoData));
				
				compositesList.add(composites);
			} else {
				for (final Composite<WUsedListDataItem> childComponent : component.getChildren()) {
					List<Composite<WUsedListDataItem>> composites = new LinkedList<>();
					
					if (childComponent.getData() != null && Boolean.TRUE.equals(childComponent.getData().getIsWUsedImpacted())) {
						composites.add(childComponent);
						composites.addAll(findAllImpactedChildrenComposites(childComponent, ecoData));
						
						compositesList.add(composites);
					}
				}
			}
		}
		
		int currentSize = -1;
		
		//merge lists that have elements in common because they need to be executed in the same cache-only transaction
		do {
			
			currentSize = compositesList.size();
			
			for (int i = 0; i < compositesList.size() - 1; i++) {
				for (int j = i + 1; j < compositesList.size(); j++) {
					List<Object> firstList = (List<Object>) compositesList.get(i);
					List<Object> secondList = (List<Object>) compositesList.get(j);
					
					if (hasIntersection(firstList, secondList)) {
						firstList.addAll(secondList);
						secondList.clear();
					}
				}
			}
			
			compositesList.removeIf( list -> ((List<?>) list).isEmpty());
			
		} while (compositesList.size() != currentSize);
		
		return compositesList;
		
	}

	private boolean hasIntersection(List<?> firstList, List<?> secondList) {
		
		for (Object firstItem : firstList) {
			for (Object secondItem : secondList) {
				
				WUsedListDataItem firstData = (WUsedListDataItem) ((Composite<?>) firstItem).getData();
				
				WUsedListDataItem secondData = (WUsedListDataItem) ((Composite<?>) secondItem).getData();
				
				if (!Sets.intersection(new HashSet<>(firstData.getSourceItems()), new HashSet<>(secondData.getSourceItems())).isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private class ApplyECOProcessWorker extends BatchProcessor.BatchProcessWorkerAdaptor<Object> {

		private ChangeOrderData ecoData;
		
		private Set<String> errors = new HashSet<>();
		
		public ApplyECOProcessWorker(ChangeOrderData ecoData) {
			this.ecoData = ecoData;
		}
		
		public Set<String> getErrors() {
			return errors;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void process(Object component) throws Throwable {
			if (component instanceof Composite<?> && ((Composite<?>) component).getData() instanceof WUsedListDataItem) {
				L2CacheSupport.doInCacheContext(() -> applyECO(ecoData, (Composite<WUsedListDataItem>) component, false, 1, errors), false, true);
			}
		}
		
	}
	
	private class SimulateECOProcessWorker extends BatchProcessor.BatchProcessWorkerAdaptor<Object> {
		
		Set<String> errors = new HashSet<>();
		
		private ChangeOrderData ecoData;
		
		public SimulateECOProcessWorker(ChangeOrderData ecoData) {
			this.ecoData = ecoData;
		}
		
		public Set<String> getErrors() {
			return errors;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void process(Object components) throws Throwable {
			
			L2CacheSupport.doInCacheContext(() -> {
				int sort = 1;
				
				if (components instanceof List) {
					for (Object component : (List<?>) components) {
						if (component instanceof Composite && ((Composite<?>) component).getData() instanceof WUsedListDataItem) {
							sort = applyECO(ecoData, (Composite<WUsedListDataItem>) component, true, sort, errors);
						}
					}
				}
				
			} , true, true);
			
			for (ChangeUnitDataItem cul2 : ecoData.getChangeUnitList()) {
				cul2.setTreated(Boolean.FALSE);
			}
			
			alfrescoRepository.save(ecoData);
		}
	}
	
	private int applyECO(ChangeOrderData ecoData, Composite<WUsedListDataItem> component, boolean isSimulation, int sort, Set<String> errors) {
		WUsedListDataItem wusedData = component.getData();
		boolean isMergeItem = ChangeOrderType.Merge.equals(ecoData.getEcoType()) && (wusedData.getDepthLevel() == 1);
		
		final ChangeUnitDataItem changeUnitDataItem = getOrCreateChangeUnitDataItem(ecoData, wusedData);
		
		// We break if product treated
		if ((changeUnitDataItem != null) && !Boolean.TRUE.equals(changeUnitDataItem.getTreated())) {
			
			// We test if all referring nodes are treated before
			// apply
			// to branch
			if ((wusedData.getDepthLevel() > 2) && shouldSkipCurrentBranch(ecoData, changeUnitDataItem)) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Skip current branch at " + nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME));
				}
				return sort;
			}
			
			final int finalSort = sort++;
			final RetryingTransactionCallback<Object> actionCallback = () -> {
				
				NodeRef productNodeRef = getProductToImpact(ecoData, changeUnitDataItem, isSimulation);
				
				if (productNodeRef != null) {
					
					RepositoryEntity repositoryEntity = alfrescoRepository.findOne(productNodeRef);
					
					if (repositoryEntity instanceof ProductData) {
						ProductData productToFormulateData = (ProductData) repositoryEntity;
						
						if (isSimulation) {
							// Before formulate we create simulation
							// List
							createCalculatedCharactValues(ecoData, productToFormulateData, finalSort);
						}
						
						// Level 2
						if ((wusedData.getDepthLevel() == 2) || isMergeItem) {
							applyReplacementList(ecoData, productToFormulateData, isSimulation, isMergeItem);
						}
						
						if (!isMergeItem) {
							applyLabelingReplacements(ecoData, productToFormulateData);
						}
						
						if (isMergeItem && isSimulation) {
							
							logger.debug("Merge finding corresponding branch...");
							
							for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {
								if ((replacementListDataItem.getSourceItems() != null) && (replacementListDataItem.getTargetItem() != null)
										&& (replacementListDataItem.getSourceItems().size() == 1)
										&& replacementListDataItem.getTargetItem().equals(productNodeRef)) {
									
									productToFormulateData = (ProductData) alfrescoRepository
											.findOne(replacementListDataItem.getSourceItems().get(0));
									
									logger.debug("Found matching branch product:" + productToFormulateData.getName());
									
									break;
								}
							}
						}
						
						productService.formulate(productToFormulateData);
						
						if (isSimulation) {
							// update simulation List
							updateCalculatedCharactValues(ecoData, productToFormulateData, productNodeRef);
						}
						
						// check req
						checkRequirements(changeUnitDataItem, productToFormulateData);
						
						alfrescoRepository.save(productToFormulateData);
						
						// Create new version if needed
						if (!isSimulation && !isMergeItem) {
							if (!changeUnitDataItem.getRevision().equals(RevisionType.NoRevision)) {
								createNewProductVersion(productNodeRef,
										changeUnitDataItem.getRevision().equals(RevisionType.Major) ? VersionType.MAJOR : VersionType.MINOR,
												ecoData, wusedData.getParent());
							}
						}
						
						if (!isSimulation) {
							entityActivityService.postChangeOrderActivity(productNodeRef, ecoData.getNodeRef());
						}
						
					} else {
						logger.warn("Product to impact is empty");
					}
					
					changeUnitDataItem.setErrorMsg(null);
					
					changeUnitDataItem.setTreated(Boolean.TRUE);
						
					if (!isSimulation) {
						
						// Store current state of ecoData
						alfrescoRepository.save(ecoData);
						if (logger.isDebugEnabled()) {
							logger.debug("Applied Treated to item "
									+ nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME));
						}
					}
					
				}
				return null;
				
			};
			
			try {
				AuthenticationUtil.runAsSystem(() -> transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true));
			} catch (Exception e) {
				
				changeUnitDataItem.setTreated(false);
				changeUnitDataItem.setErrorMsg(e.getMessage());
				
				if (errors != null) {
					errors.add("Change unit in Error: " + changeUnitDataItem.getNodeRef());
					errors.add("Error type: " + e.getClass());
					errors.add("Error message: " + e.getMessage());
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("Error applying for: " + changeUnitDataItem.toString(), e);
				}
				
				throw e;
			}
		}
		
		return sort;
	}
	
	private void checkMissingWUsed(Composite<WUsedListDataItem> composite) {

		boolean childChecked = false;
		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {
			if (!component.isLeaf()) {
				checkMissingWUsed(component);
			}

			if (Boolean.TRUE.equals(component.getData().getIsWUsedImpacted())) {
				childChecked = true;
			}
		}

		if ((composite.getData() != null) && childChecked) {
			composite.getData().setIsWUsedImpacted(true);
		}

	}
	
	private BatchStep<Object> createCalculateWUsedListBatchStep(ChangeOrderData ecoData, boolean isWUsedImpacted, boolean applyToAll) {
		BatchStep<Object> batchStep = new BatchStep<>();
		
		BatchProcessor.BatchProcessWorkerAdaptor<Object> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<Object>() {

			@Override
			public void process(Object entry) throws Throwable {
				internalCalculateWUsedList(ecoData, isWUsedImpacted, applyToAll);
			}
		};
		
		batchStep.setProcessWorker(processWorker);
		batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(Arrays.asList(ecoData)));
		batchStep.setBatchStepListener(new BatchStepAdapter() {
			
			@Override
			public void beforeStep() {
				ecoData.setEcoState(ECOState.InProgress);
				alfrescoRepository.save(ecoData);
			}
			
			@Override
			public void afterStep() {
				if (ECOState.InProgress.equals(ecoData.getEcoState())) {
					ecoData.setEcoState(ECOState.WUsedCalculated);
					alfrescoRepository.save(ecoData);
				}
			}
			
			@Override
			public void onError(String lastErrorEntryId, String lastError) {
				if (!ECOState.InError.equals(ecoData.getEcoState())) {
					ecoData.setEcoState(ECOState.InError);
					commentService.createComment(ecoData.getNodeRef(), "", "Error during OM calculating of WUsed: " + lastError, false);
					alfrescoRepository.save(ecoData);
				}
			}
		});
		
		return batchStep;
	}

	/** {@inheritDoc} */
	@Override
	public BatchInfo calculateWUsedList(NodeRef ecoNodeRef, boolean isWUsedImpacted, boolean notifyByMail) {
		
		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
		
		if (!ECOState.Applied.equals(ecoData.getEcoState()) && !ECOState.InError.equals(ecoData.getEcoState())) {
			
			String entityDescription = nodeService.getProperty(ecoNodeRef, BeCPGModel.PROP_CODE) + " - " + nodeService.getProperty(ecoNodeRef, ContentModel.PROP_NAME);
			
			BatchInfo batchInfo = new BatchInfo(String.format("calculateWUsed-%s", ecoNodeRef.getId()), "becpg.batch.eco.calculateWUsed", entityDescription);
			batchInfo.setRunAsSystem(true);
			
			if (notifyByMail) {
				batchInfo.enableNotifyByMail("eco.calculateWUsed", String.format(ACTION_URL_PREFIX, ecoNodeRef.toString()));
			}
			
			List<BatchStep<Object>> batchStepList = new LinkedList<>();
			
			batchStepList.add(createCalculateWUsedListBatchStep(ecoData, isWUsedImpacted, false));
			
			batchQueueService.queueBatch(batchInfo, batchStepList);
			
			return batchInfo;
		}
		
		return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public BatchInfo closeECO(NodeRef ecoNodeRef, List<NodeRef> impactedProducts) {
		
		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
		
		String entityDescription = nodeService.getProperty(ecoNodeRef, BeCPGModel.PROP_CODE) + " - " + nodeService.getProperty(ecoNodeRef, ContentModel.PROP_NAME);

		BatchInfo closingBatchInfo = new BatchInfo(String.format("closeECO-%s", ecoNodeRef.getId()), "becpg.batch.eco.close", entityDescription);
		closingBatchInfo.setRunAsSystem(true);
		
		List<BatchStep<Object>> closingBatchStepList = new LinkedList<>();
		
		if (impactedProducts.isEmpty()) {
			impactedProducts.addAll(provideImpactedProducts(CompositeHelper.getHierarchicalCompoList(ecoData.getWUsedList()), true));
		}
		
		closingBatchStepList.add(createRemoveChangeOrderAspectStep(impactedProducts));
		
		batchQueueService.queueBatch(closingBatchInfo, closingBatchStepList);
			
		return null;
	}

	private void internalCalculateWUsedList(ChangeOrderData ecoData, boolean isWUsedImpacted, boolean applyToAll) {
		logger.debug("calculateWUsedList");

		// clear WUsedList
		ecoData.getWUsedList().clear();
		ecoData.getChangeUnitList().clear();

		if (ecoData.getReplacementList() != null) {

			int sort = 1;
			
			for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {
				
				List<NodeRef> replacements = getSourceItems(ecoData, replacementListDataItem);
				
				if ((replacements != null) && !replacements.isEmpty()) {
					
					WUsedListDataItem parent = new WUsedListDataItem();
					parent.setSourceItems(replacements);
					parent.setIsWUsedImpacted(true);
					parent.setDepthLevel(1);
					parent.setSort(sort++);
					parent.setTargetItem(replacementListDataItem.getTargetItem());
					
					ecoData.getWUsedList().add(parent);
					
					List<QName> associationQNames = evaluateWUsedAssociations(replacements);
					
					WUsedFilter filter = null;
					
					List<String> wUsedStatesList = getWUsedStates(ecoData);
					filter = new WUsedFilter() {
						@Override
						public WUsedFilterKind getFilterKind() {
							return WUsedFilterKind.STANDARD;
						}
						@Override
						public void filter(MultiLevelListData wUsedData) {
							for (Iterator<Entry<NodeRef, MultiLevelListData>> iterator = wUsedData.getTree().entrySet().iterator(); iterator
									.hasNext();) {
								Entry<NodeRef, MultiLevelListData> entry = iterator.next();
								NodeRef nodeRef = entry.getValue().getEntityNodeRef();
								String productState = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_STATE);
								if (productState == null || !wUsedStatesList.contains(productState)) {
									iterator.remove();
								}
							}
						}
					};
					
					for (QName associationQName : associationQNames) {
						MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(replacements, WUsedOperator.AND, filter, associationQName,
								RepoConsts.MAX_DEPTH_LEVEL);
						
						QName datalistQName = evaluateListFromAssociation(associationQName);
						sort = calculateWUsedList(ecoData, wUsedData, datalistQName, parent,
								ChangeOrderType.Merge.equals(ecoData.getEcoType()) || isWUsedImpacted, sort, replacementListDataItem.getTargetItem(), applyToAll);
					}
				}
			}
		}

		alfrescoRepository.save(ecoData);

	}

	private List<String> getWUsedStates(ChangeOrderData ecoData) {
		if (ChangeOrderType.ImpactWUsed.equals(ecoData.getEcoType())) {
			return Arrays.stream(impactWUsedStates.split(",")).toList();
		}
		return List.of(SystemState.Simulation.toString(), SystemState.ToValidate.toString(), SystemState.Valid.toString());
	}
	
	private List<NodeRef> getSourceItems(ChangeOrderData ecoData, ReplacementListDataItem replacementListDataItem) {
		List<NodeRef> ret = new ArrayList<>();
		if (ChangeOrderType.Merge.equals(ecoData.getEcoType())) {
			if (replacementListDataItem.getTargetItem() != null) {
				ret.add(replacementListDataItem.getTargetItem());
			}
		} else {
			return replacementListDataItem.getSourceItems();
		}
		return ret;
	}

	// Keep only common assocs
	private List<QName> evaluateWUsedAssociations(List<NodeRef> sourceList) {
		List<QName> assocQNames = null;

		for (NodeRef replacementSourceNodeRef : sourceList) {
			if (assocQNames == null) {
				assocQNames = evaluateWUsedAssociations(replacementSourceNodeRef);
			} else {
				assocQNames.retainAll(evaluateWUsedAssociations(replacementSourceNodeRef));
			}
		}

		return assocQNames;
	}

	private int calculateWUsedList(ChangeOrderData ecoData, MultiLevelListData wUsedData, QName dataListQName, WUsedListDataItem parent,
			boolean isWUsedImpacted, int sort, NodeRef targetItem, boolean applyToAll) {

		List<NodeRef> sortedKeys = new ArrayList<>(wUsedData.getTree().keySet());
		
		sortedKeys.sort((key1, key2) -> {
			
			NodeRef sourceItem1 = wUsedData.getTree().get(key1).getEntityNodeRef();
			NodeRef sourceItem2 = wUsedData.getTree().get(key2).getEntityNodeRef();
			
			String entityRef1 = sourceItem1 == null ? "" : sourceItem1.toString();
			String entityRef2 = sourceItem2 == null ? "" : sourceItem2.toString();
			
			return entityRef1.compareTo(entityRef2);
			
		});
		
		int maxEcoWUsedSize = maxEcoWUsedSize();
		
		for (NodeRef key : sortedKeys) {
			
			if (applyToAll || ecoData.getWUsedList().size() < maxEcoWUsedSize) {
				WUsedListDataItem wUsedListDataItem = new WUsedListDataItem();
				wUsedListDataItem.setLink(key);
				wUsedListDataItem.setParent(parent);
				wUsedListDataItem.setImpactedDataList(dataListQName);
				wUsedListDataItem.setIsWUsedImpacted(isWUsedImpacted || applyToAll);
				wUsedListDataItem.setSourceItems(wUsedData.getTree().get(key).getEntityNodeRefs());
				wUsedListDataItem.setSort(sort++);
				wUsedListDataItem.setTargetItem(targetItem);

				ecoData.getWUsedList().add(wUsedListDataItem);
				// recursive
				sort = calculateWUsedList(ecoData, wUsedData.getTree().get(key), dataListQName, wUsedListDataItem, isWUsedImpacted, sort, targetItem, applyToAll);
			}
		}

		return sort;
	}

	private int maxEcoWUsedSize() {
		return Integer.parseInt(systemConfigurationService.confValue("beCPG.eco.max.wused.size"));
	}

	private ChangeUnitDataItem getOrCreateChangeUnitDataItem(ChangeOrderData ecoData, WUsedListDataItem data) {

		if ((data.getSourceItems() != null) && !data.getSourceItems().isEmpty()) {

			ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(data.getSourceItems().get(0));

			if (logger.isDebugEnabled()) {
				logger.debug("Get ChangeUnit for " + nodeService.getProperty(data.getSourceItems().get(0), ContentModel.PROP_NAME));
			}

			RevisionType revisionType = RevisionType.NoRevision;

			for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {
				if (getSourceItems(ecoData, replacementListDataItem).equals(data.getRoot().getSourceItems())) {
					if (RevisionType.Major.equals(replacementListDataItem.getRevision())) {
						revisionType = RevisionType.Major;
						break;
					} else if (RevisionType.Minor.equals(replacementListDataItem.getRevision())) {
						revisionType = RevisionType.Minor;
					}
				}
			}

			if (changeUnitDataItem == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Not found creating changeUnit");
				}
				changeUnitDataItem = new ChangeUnitDataItem(revisionType, null, null, Boolean.FALSE, data.getSourceItems().get(0), null);
				ecoData.getChangeUnitList().add(changeUnitDataItem);

			} else {
				if (RevisionType.Major.equals(revisionType)
						|| (RevisionType.Minor.equals(revisionType) && RevisionType.NoRevision.equals(changeUnitDataItem.getRevision()))) {
					changeUnitDataItem.setRevision(revisionType);
				}
			}
			return changeUnitDataItem;
		}

		logger.error("Wused data has no source item :" + data.toString());

		return null;

	}
	
	private List<Composite<WUsedListDataItem>> findAllImpactedChildrenComposites(Composite<WUsedListDataItem> composite, final ChangeOrderData ecoData) {
		
		List<Composite<WUsedListDataItem>> composites = new LinkedList<>();
		
		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {
			
			boolean isMergeItem = ChangeOrderType.Merge.equals(ecoData.getEcoType()) && (component.getData().getDepthLevel() == 1);
			
			// Not First level
			if ((component.getData() != null) && ((component.getData().getDepthLevel() > 1) || isMergeItem)
					&& Boolean.TRUE.equals(component.getData().getIsWUsedImpacted())) {
				
				composites.add(component);
				
			}
			
			if (!component.isLeaf() && Boolean.TRUE.equals(component.getData().getIsWUsedImpacted())) {
				composites.addAll(findAllImpactedChildrenComposites(component, ecoData));
			}
		}
		
		return composites;
		
	}

	private boolean shouldSkipCurrentBranch(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem) {

		boolean skip = false;
		for (WUsedListDataItem wulDataItem : ecoData.getWUsedList()) {
			if (Boolean.TRUE.equals(wulDataItem.getIsWUsedImpacted()) && (wulDataItem.getParent() != null)
					&& Boolean.TRUE.equals(wulDataItem.getParent().getIsWUsedImpacted())
					&& wulDataItem.getSourceItems().contains(changeUnitDataItem.getSourceItem())) {
				if ((ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)) == null)
						|| !Boolean.TRUE.equals(ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)).getTreated())) {
					skip = true;
					break;
				}
			}

		}
		return skip;
	}

	private void applyReplacementList(ChangeOrderData ecoData, ProductData product, boolean isSimulation, boolean isMergedItem) {
		if (ecoData.getReplacementList() != null) {
			if (isMergedItem && !isSimulation) {
				merge(ecoData);
			} else {
				for (AbstractProductDataView view : product.getViews()) {
					applyToList(ecoData, product, view.getMainDataList());
				}
			}
		}
	}

	private void applyLabelingReplacements(ChangeOrderData ecoData, ProductData product) {
		if (product.getLabelingListView() != null && product.getLabelingListView().getLabelingRuleList() != null) {
			for (LabelingRuleListDataItem labelingRuleListDataItem : product.getLabelingListView().getLabelingRuleList()) {
				for (ReplacementListDataItem rep : ecoData.getReplacementList()) {
					for (NodeRef sourceItem : rep.getSourceItems()) {
						if (labelingRuleListDataItem.getComponents() != null && labelingRuleListDataItem.getComponents().contains(sourceItem)) {
							labelingRuleListDataItem.getComponents().remove(sourceItem);
							if (!labelingRuleListDataItem.getComponents().contains(rep.getTargetItem())) {
								labelingRuleListDataItem.getComponents().add(rep.getTargetItem());
							}
						}
					}
				}
			}
		}
	}

	private void merge(ChangeOrderData ecoData) {
		for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {
			if ((replacementListDataItem.getSourceItems() != null) && (replacementListDataItem.getTargetItem() != null)
					&& (replacementListDataItem.getSourceItems().size() == 1)
					&& !replacementListDataItem.getTargetItem().equals(replacementListDataItem.getSourceItems().get(0))) {

				if (entityVersionService.getAllVersionBranches(replacementListDataItem.getTargetItem())
						.contains(replacementListDataItem.getSourceItems().get(0))) {

					VersionType versionType = VersionType.MINOR;
					if (RevisionType.Major.equals(replacementListDataItem.getRevision())) {
						versionType = VersionType.MAJOR;
					}

					String description = I18NUtil.getMessage("plm.ecm.apply.version.label", ecoData.getCode() + " - " + ecoData.getName());

					entityVersionService.mergeBranch(replacementListDataItem.getSourceItems().get(0), replacementListDataItem.getTargetItem(),
							versionType, description);

					replacementListDataItem.setSourceItems(Arrays.asList(replacementListDataItem.getTargetItem()));

				} else {
					logger.warn("Source item " + replacementListDataItem.getTargetItem() + " is not a branch of target item "
							+ replacementListDataItem.getSourceItems().get(0));
				}
			}

		}
	}
	
	private <T extends CompositionDataItem> void applyToList(ChangeOrderData ecoData, ProductData productData, List<T> items) {

		Map<T, WUsedListDataItem> itemToWUsedData = new HashMap<>();
		
		// map each item to the WUsed item
		items.forEach(item -> {
			for (WUsedListDataItem wUsedItem : ecoData.getWUsedList()) {
				if (Boolean.TRUE.equals(wUsedItem.getIsWUsedImpacted()) && item.getNodeRef().equals(wUsedItem.getLink())) {
					itemToWUsedData.put(item, wUsedItem);
				}
			}
		});
		
		for (ReplacementListDataItem replacement : ecoData.getReplacementList()) {
			checkForItemsToDelete(ecoData, items, itemToWUsedData, replacement);
		}
		
		Date ecoEffectiveDate = ecoData.getEffectiveDate();
		
		Set<T> newItems = new HashSet<>();
		
		Set<T> toRemoveItems = new HashSet<>();
		
		for (T item : items) {
			if (itemToWUsedData.containsKey(item)) {
				WUsedListDataItem wUsedData = itemToWUsedData.get(item);

				Date effectiveDate = ecoEffectiveDate;

				if (wUsedData.getEffectiveDate() != null) {
					effectiveDate = wUsedData.getEffectiveDate();
				}

				EffectiveFilters<EffectiveDataItem> filter = null;

				boolean impactEffectivity = !ChangeOrderType.ImpactWUsed.equals(ecoData.getEcoType()) && isFuture(effectiveDate);

				if (impactEffectivity) {
					filter = new EffectiveFilters<>(effectiveDate);
				} else {
					filter = new EffectiveFilters<>(EffectiveFilters.EFFECTIVE);
				}

				if (filter.createPredicate(productData).test(item)) {

					
					List<ReplacementListDataItem> itemReplacements = ecoData.getReplacementList().stream().filter(remp -> getSourceItems(ecoData, remp).contains(item.getComponent())).toList();
					
					if (!itemReplacements.isEmpty()) {
						
						boolean copyItem = impactEffectivity || itemReplacements.size() > 1 || ecoData.getReplacementList().stream().anyMatch(r -> !r.equals(itemReplacements.get(0)) && getSourceItems(ecoData, r).contains(itemReplacements.get(0).getTargetItem()));
						
						for (ReplacementListDataItem itemReplacement : itemReplacements) {
							
							NodeRef target = null;
							
							if (ChangeOrderType.Merge.equals(ecoData.getEcoType())) {
								target = itemReplacement.getSourceItems().get(0);
							} else {
								target = itemReplacement.getTargetItem();
							}
							
							T newItem = copyOrUpdateItem(item, itemReplacement, target, wUsedData, copyItem);
							
							newItems.add(newItem);
							
							if (impactEffectivity) {
								item.setEndEffectivity(effectiveDate);
								newItem.setStartEffectivity(effectiveDate);
							} else {
								toRemoveItems.add(item);
							}
						}
					}
				}
			}
		}
		
		items.removeAll(toRemoveItems);
		items.addAll(newItems);
		
	}

	private <T extends CompositionDataItem> void checkForItemsToDelete(ChangeOrderData ecoData, List<T> items, Map<T, WUsedListDataItem> itemToWUsedData, ReplacementListDataItem replacement) {
		List<NodeRef> sourceItems = getSourceItems(ecoData, replacement);
		
		if (sourceItems.size() > 1 || replacement.getTargetItem() == null) {
			
			boolean hasWUsed = false;
			
			boolean isFuture = false;
			
			Date effectiveDate = null;
			
			for (NodeRef sourceItem : sourceItems) {
				for (Entry<T, WUsedListDataItem> entry : itemToWUsedData.entrySet()) {
					if (entry.getKey().getComponent().equals(sourceItem)) {
						
						effectiveDate = ecoData.getEffectiveDate();
						
						if (entry.getValue().getEffectiveDate() != null) {
							effectiveDate = entry.getValue().getEffectiveDate();
						}
						
						isFuture = isFuture(effectiveDate);
						
						hasWUsed = true;
						break;
					}
				}
			}
			
			final boolean finalIsFuture = isFuture;
			final Date finalEffectiveDate = effectiveDate;
			
			if (hasWUsed) {
				items.forEach(item -> {
					if (sourceItems.contains(item.getComponent()) && (replacement.getTargetItem() == null || !itemToWUsedData.keySet().contains(item))) {
						if (finalIsFuture) {
							item.setEndEffectivity(finalEffectiveDate);
						} else {
							items.remove(item);
						}
					}
				});
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends CompositionDataItem> T copyOrUpdateItem(T item, ReplacementListDataItem replacement, NodeRef target, WUsedListDataItem wUsedData, boolean createNew) {
		
		Double newQuantity = wUsedData.getQty();
		
		if (newQuantity == null) {
			if (item instanceof CompoListDataItem) {
				if ((((CompoListDataItem) item).getQtySubFormula() != null) && (replacement.getQtyPerc() != null)) {
					newQuantity = (replacement.getQtyPerc() / 100d) * ((CompoListDataItem) item).getQtySubFormula();
				}
			} else {
				if ((item.getQty() != null) && (replacement.getQtyPerc() != null)) {
					newQuantity = (replacement.getQtyPerc() / 100d) * item.getQty();
				}
			}
		}
		
		Double newLoss = wUsedData.getLoss();
		
		if (newLoss == null) {
			newLoss = replacement.getLoss();
		}
		
		T newItem = item;
		
		if (createNew) {
			newItem = (T) item.copy();
			newItem.setNodeRef(null);
		}
		
		updateComponent(newItem, target, newQuantity, newLoss);
		
		return newItem;
	}

	private boolean isFuture(Date effectiveDate) {
		Date now = new Date();
		return (effectiveDate != null) && (effectiveDate.getTime() > now.getTime());
	}

	private <T extends CompositionDataItem> void updateComponent(T component, NodeRef target, Double newQuantity, Double newLoss) {
		component.setComponent(target);
		if (component instanceof CompoListDataItem) {
			((CompoListDataItem) component).setQtySubFormula(newQuantity);
		} else {
			component.setQty(newQuantity);
		}
		
		if (newLoss != null) {
			component.setLossPerc(newLoss);
		}
		
	}

	private NodeRef getProductToImpact(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem, boolean isSimulation) {
		NodeRef productToImpact = changeUnitDataItem.getSourceItem();
		if (productToImpact != null) {
			// Create a new revision if apply else use
			if (!isSimulation) {
				/*
				 * Create initial version if needed
				 */
				if (!changeUnitDataItem.getRevision().equals(RevisionType.NoRevision)) {
					Date effectiveDateToUse = ChangeOrderType.ImpactWUsed.equals(ecoData.getEcoType()) ? ecoData.getEffectiveDate() : null;
					entityVersionService.createInitialVersion(productToImpact, effectiveDateToUse);
				}
			}
		}
		return productToImpact;
	}

	private NodeRef createNewProductVersion(final NodeRef productToImpact, VersionType versionType, ChangeOrderData ecoData,
			WUsedListDataItem parent) {

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(VersionBaseModel.PROP_VERSION_TYPE, versionType);
		if ((ecoData.getDescription() != null) && !ecoData.getDescription().isEmpty()) {
			properties.put(Version.PROP_DESCRIPTION, ecoData.getDescription());
		} else {
			properties.put(Version.PROP_DESCRIPTION,
					I18NUtil.getMessage("plm.ecm.apply.version.label", ecoData.getCode() + " - " + ecoData.getName()));
		}
		if (((parent.getDepthLevel() > 1) && parent.getIsWUsedImpacted()) || ChangeOrderType.ImpactWUsed.equals(ecoData.getEcoType())) {
			properties.put(EntityVersionPlugin.POST_UPDATE_HISTORY_NODEREF, parent.getSourceItems().get(0));
		}
		
		if (logger.isDebugEnabled()) {
			String name = (String) nodeService.getProperty(productToImpact, ContentModel.PROP_NAME);
			logger.debug("creating new version for: " + name + " (" + productToImpact + ")");
		}
		
		Date effectiveDateToUse = ChangeOrderType.ImpactWUsed.equals(ecoData.getEcoType()) ? ecoData.getEffectiveDate() : null;
		
		return entityVersionService.createVersion(productToImpact, properties, effectiveDateToUse);

	}

	private void createCalculatedCharactValues(ChangeOrderData ecoData, ProductData sourceData, int sort) {

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Object sourceValue = getCharactValue(charactNodeRef, charactType, sourceData);
			if (logger.isDebugEnabled()) {
				logger.debug("create calculated charact: " + nodeService.getProperty(sourceData.getNodeRef(), ContentModel.PROP_NAME) + " - "
						+ charactNodeRef + " - sourceValue: " + sourceValue);
			}
			ecoData.getSimulationList().add(new SimulationListDataItem(null, sourceData.getNodeRef(), charactNodeRef, sourceValue, null, sort));
		}

	}

	private void updateCalculatedCharactValues(ChangeOrderData ecoData, ProductData targetData, NodeRef productNodeRef) {

		List<SimulationListDataItem> toRemove = new ArrayList<>();

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Object targetValue = getCharactValue(charactNodeRef, charactType, targetData);
			for (SimulationListDataItem simulationListDataItem : ecoData.getSimulationList()) {
				if (simulationListDataItem.getCharact().equals(charactNodeRef) && simulationListDataItem.getSourceItem().equals(productNodeRef)) {
					simulationListDataItem.setTargetValue(targetValue);

					if ((simulationListDataItem.getTargetValue() == null) && (simulationListDataItem.getSourceValue() == null)) {
						toRemove.add(simulationListDataItem);
					}

					if (logger.isDebugEnabled()) {
						logger.debug("calculated charact: " + nodeService.getProperty(targetData.getNodeRef(), ContentModel.PROP_NAME) + " - "
								+ charactNodeRef + " - sourceValue: " + simulationListDataItem.getSourceValue() + " - targetValue: " + targetValue);
					}
				}

			}
		}

		ecoData.getSimulationList().removeAll(toRemove);

		if (logger.isDebugEnabled()) {
			logger.debug("simList size: " + ecoData.getSimulationList().size());
		}
	}

	private void checkRequirements(ChangeUnitDataItem changeUnitDataItem, ProductData targetData) {

		RequirementType reqType = null;
		StringBuilder reqDetails = null;

		if ((targetData.getCompoListView() != null) && (targetData.getReqCtrlList() != null)) {
			for (RequirementListDataItem rcl : targetData.getReqCtrlList()) {

				RequirementType newReqType = rcl.getReqType();

				if (reqType == null) {
					reqType = newReqType;
				} else {

					if ((RequirementType.Tolerated.equals(newReqType) && reqType.equals(RequirementType.Info))
							|| (RequirementType.Forbidden.equals(newReqType) && !reqType.equals(RequirementType.Forbidden))) {
						reqType = newReqType;
					}
				}

				if (reqDetails == null) {
					reqDetails = new StringBuilder();
					reqDetails.append(rcl.getReqMessage());
				} else {

					reqDetails.append(RepoConsts.LABEL_SEPARATOR);
					reqDetails.append(rcl.getReqMessage());

				}
			}
		}
		changeUnitDataItem.setReqType(reqType);
		changeUnitDataItem.setReqDetails(reqDetails != null ? LargeTextHelper.elipse(reqDetails.toString()) : null);

	}

	/**
	 * <p>
	 * evaluateWUsedAssociations.
	 * </p>
	 *
	 * @param targetAssocNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	private List<QName> evaluateWUsedAssociations(NodeRef targetAssocNodeRef) {
		List<QName> wUsedAssociations = new ArrayList<>();

		QName nodeType = nodeService.getType(targetAssocNodeRef);

		if (nodeType.isMatch(PLMModel.TYPE_RAWMATERIAL) || nodeType.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)
				|| nodeType.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_FINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_LOGISTICUNIT)) {
			wUsedAssociations.add(PLMModel.ASSOC_COMPOLIST_PRODUCT);
		} else if (nodeType.isMatch(PLMModel.TYPE_PACKAGINGMATERIAL) || nodeType.isMatch(PLMModel.TYPE_PACKAGINGKIT)) {
			wUsedAssociations.add(PLMModel.ASSOC_PACKAGINGLIST_PRODUCT);
		} else if (nodeType.isMatch(PLMModel.TYPE_RESOURCEPRODUCT)) {
			wUsedAssociations.add(MPMModel.ASSOC_PL_RESOURCE);
		}

		return wUsedAssociations;
	}

	private QName evaluateListFromAssociation(QName associationName) {

		QName listQName = null;

		if (associationName.equals(PLMModel.ASSOC_COMPOLIST_PRODUCT)) {
			listQName = PLMModel.TYPE_COMPOLIST;
		} else if (associationName.equals(PLMModel.ASSOC_PACKAGINGLIST_PRODUCT)) {
			listQName = PLMModel.TYPE_PACKAGINGLIST;
		} else if (associationName.equals(MPMModel.ASSOC_PL_RESOURCE)) {
			listQName = MPMModel.TYPE_PROCESSLIST;
		}

		return listQName;
	}

	private Object getCharactValue(NodeRef charactNodeRef, QName charactType, ProductData productData) {
		if (charactType.equals(PLMModel.TYPE_COST)) {
			return getCharactValue(charactNodeRef, productData.getCostList());
		} else if (charactType.equals(PLMModel.TYPE_NUT)) {
			return getCharactValue(charactNodeRef, productData.getNutList());
		} else if (charactType.equals(PLMModel.TYPE_ING)) {
			return getCharactValue(charactNodeRef, productData.getIngList());
		} else if (charactType.equals(PLMModel.TYPE_PHYSICO_CHEM)) {
			return getCharactValue(charactNodeRef, productData.getPhysicoChemList());
		} else if (charactType.equals(PLMModel.TYPE_DYNAMICCHARACTLIST)) {
			String charactName = (String) nodeService.getProperty(charactNodeRef, PLMModel.PROP_DYNAMICCHARACT_TITLE);
			if (charactName != null) {
				for (AbstractProductDataView view : productData.getViews()) {
					for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
						if (charactName.equals(dynamicCharactListItem.getTitle())) {
							return dynamicCharactListItem.getValue();
						}

					}
				}
			}
		} else if (charactType.equals(PLMModel.TYPE_LABELINGRULELIST)) {
			for (IngLabelingListDataItem labelingListItem : productData.getLabelingListView().getIngLabelingList()) {
				if (charactNodeRef.equals(labelingListItem.getGrp())) {
					return MLTextHelper.getClosestValue(labelingListItem.getValue(), Locale.getDefault());
				}

			}
		} else if (charactType.equals(PLMModel.TYPE_LABEL_CLAIM)) {
			for (LabelClaimListDataItem labelClaimListDataItem : productData.getLabelClaimList()) {
				if (charactNodeRef.equals(labelClaimListDataItem.getLabelClaim())) {
					return labelClaimListDataItem.getLabelClaimValue();
				}

			}

		}
		return null;
	}

	private Double getCharactValue(NodeRef charactNodeRef, List<? extends SimpleCharactDataItem> charactList) {

		if ((charactList != null) && (charactNodeRef != null)) {
			for (SimpleCharactDataItem charactDataListItem : charactList) {
				if (charactNodeRef.equals(charactDataListItem.getCharactNodeRef())) {
					return charactDataListItem.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * <p>setInProgress.</p>
	 *
	 * @param ecoNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean setInProgress(NodeRef ecoNodeRef) {
		ChangeOrderData om = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
		if (!ECOState.InProgress.equals(om.getEcoState())) {
			om.setEcoState(ECOState.InProgress);
			alfrescoRepository.save(om);
			return true;
		}

		return false;
	}

}
