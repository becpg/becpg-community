/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * Engineering change order service implementation
 *
 * @author quere
 *
 */
public class ECOServiceImpl implements ECOService {

	private static final Log logger = LogFactory.getLog(ECOServiceImpl.class);

	private WUsedListService wUsedListService;
	private EntityVersionService entityVersionService;

	private NodeService nodeService;
	private TransactionService transactionService;

	private ProductService productService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public void doSimulation(NodeRef ecoNodeRef) {
		doRun(ecoNodeRef, ECOState.Simulated);
	}

	@Override
	public void apply(NodeRef ecoNodeRef) {
		doRun(ecoNodeRef, ECOState.Applied);
	}

	private void doRun(NodeRef ecoNodeRef, final ECOState state) {

		final ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

		// Do not run if already applied
		if (!ECOState.Applied.equals(ecoData.getEcoState())
				&& !(ECOState.InError.equals(ecoData.getEcoState()) && ECOState.Simulated.equals(state))) {

			L2CacheSupport.doInCacheContext(() -> {
				StopWatch watch = new StopWatch();
				if (logger.isDebugEnabled()) {
					watch.start();
					logger.warn("Start running ECM [" + state.toString() + "] for thread " + Thread.currentThread().getName());
				}

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
				if (ECOState.Simulated.equals(state)) {
					ecoData.getSimulationList().clear();
				}

				// Visit Wused
				Composite<WUsedListDataItem> composite = CompositeHelper.getHierarchicalCompoList(ecoData.getWUsedList());

				if (logger.isTraceEnabled()) {
					logger.trace("WUsedList to impact :" + composite.toString());
				}

				boolean hasError = !visitChildrens(composite, ecoData, ECOState.Simulated.equals(state));

				if (ECOState.Simulated.equals(state)) {
					for (ChangeUnitDataItem cul2 : ecoData.getChangeUnitList()) {
						cul2.setTreated(Boolean.FALSE);
					}
					ecoData.setEcoState(state);
				} else if (hasError) {
					ecoData.setEcoState(ECOState.InError);
				} else {
					ecoData.setEffectiveDate(new Date());
					ecoData.setEcoState(ECOState.Applied);
				}

				// Change eco state

				if (logger.isDebugEnabled()) {
					watch.stop();
					logger.warn("Impact Where Used [" + state.toString() + "] executed in  " + watch.getTotalTimeSeconds() + " seconds");
				}
			}, ECOState.Simulated.equals(state), true);

			alfrescoRepository.save(ecoData);

		}

	}

	@Override
	public void calculateWUsedList(NodeRef ecoNodeRef, boolean isWUsedImpacted) {

		logger.debug("calculateWUsedList");

		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

		// Do not run if already applied
		if (!ECOState.Applied.equals(ecoData.getEcoState()) && !ECOState.InError.equals(ecoData.getEcoState())) {

			// clear WUsedList
			ecoData.getWUsedList().clear();
			ecoData.getChangeUnitList().clear();

			if (ecoData.getReplacementList() != null) {

				for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {

					if ((replacementListDataItem.getSourceItems() != null) && !replacementListDataItem.getSourceItems().isEmpty()) {

						List<NodeRef> sourceList = new ArrayList<>(replacementListDataItem.getSourceItems());

						WUsedListDataItem parent = new WUsedListDataItem();
						parent.setSourceItems(sourceList);
						parent.setIsWUsedImpacted(true);
						// parent.setLink(replacementListDataItem.getNodeRef());

						ecoData.getWUsedList().add(parent);

						List<QName> associationQNames = evaluateWUsedAssociations(sourceList);

						for (QName associationQName : associationQNames) {

							MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(sourceList, WUsedOperator.AND, associationQName,
									RepoConsts.MAX_DEPTH_LEVEL);

							QName datalistQName = evaluateListFromAssociation(associationQName);
							calculateWUsedList(ecoData, wUsedData, datalistQName, parent, isWUsedImpacted);
						}
					}
				}
			}

			// change state
			ecoData.setEcoState(ECOState.WUsedCalculated);

			alfrescoRepository.save(ecoData);

		}
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

	private void calculateWUsedList(ChangeOrderData ecoData, MultiLevelListData wUsedData, QName dataListQName, WUsedListDataItem parent,
			boolean isWUsedImpacted) {

		for (Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()) {

			//
			// TODO
			// Ici les liens doivent être multiple cas 2 vers 1false
			// Les liens doivent être mis à jour ou supprimer lors de la
			// création d'une version
			// sinon impossible de sauvegarder l'ECM

			WUsedListDataItem wUsedListDataItem = new WUsedListDataItem();
			wUsedListDataItem.setParent(parent);
			wUsedListDataItem.setImpactedDataList(dataListQName);
			wUsedListDataItem.setIsWUsedImpacted(isWUsedImpacted);
			wUsedListDataItem.setSourceItems(kv.getValue().getEntityNodeRefs());

			ecoData.getWUsedList().add(wUsedListDataItem);

			// recursive
			calculateWUsedList(ecoData, kv.getValue(), dataListQName, wUsedListDataItem, isWUsedImpacted);
		}
	}

	private ChangeUnitDataItem getOrCreateChangeUnitDataItem(ChangeOrderData ecoData, WUsedListDataItem data) {

		if ((data.getSourceItems() != null) && !data.getSourceItems().isEmpty()) {

			ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(data.getSourceItems().get(0));

			if (logger.isDebugEnabled()) {
				logger.debug("Get ChangeUnit for " + nodeService.getProperty(data.getSourceItems().get(0), ContentModel.PROP_NAME));
			}

			RevisionType revisionType = RevisionType.NoRevision;

			for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {
				if (replacementListDataItem.getSourceItems().equals(data.getRoot().getSourceItems())) {
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

	private boolean visitChildrens(Composite<WUsedListDataItem> composite, final ChangeOrderData ecoData, final boolean isSimulation) {

		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {

			// Not First level
			if ((component.getData() != null) && (component.getData().getDepthLevel() > 1) && component.getData().getIsWUsedImpacted()) {

				final ChangeUnitDataItem changeUnitDataItem = getOrCreateChangeUnitDataItem(ecoData, component.getData());

				// We break if product treated
				if ((changeUnitDataItem != null) && !changeUnitDataItem.getTreated()) {

					// We test if all referring nodes are treated before
					// apply
					// to branch
					if ((component.getData().getDepthLevel() > 2) && shouldSkipCurrentBranch(ecoData, changeUnitDataItem)) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"Skip current branch at " + nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME));
						}
						break;
					}

					final RetryingTransactionCallback<Object> actionCallback = () -> {

						NodeRef productNodeRef = getProductToImpact(ecoData, changeUnitDataItem, isSimulation);

						if (productNodeRef != null) {

							ProductData productToFormulateData = (ProductData) alfrescoRepository.findOne(productNodeRef);

							if (isSimulation) {
								// Before formulate we create simulation
								// List
								createCalculatedCharactValues(ecoData, productToFormulateData);
							}

							// Level 2
							if (component.getData().getDepthLevel() == 2) {
								applyReplacementList(ecoData, productToFormulateData);
							}

							productService.formulate(productToFormulateData);

							if (isSimulation) {
								// update simulation List
								updateCalculatedCharactValues(ecoData, productToFormulateData);
							}

							// check req
							checkRequirements(changeUnitDataItem, productToFormulateData);

							alfrescoRepository.save(productToFormulateData);

							// Create new version if needed
							if (!isSimulation) {
								if (!changeUnitDataItem.getRevision().equals(RevisionType.NoRevision)) {
									createNewProductVersion(productNodeRef,
											changeUnitDataItem.getRevision().equals(RevisionType.Major) ? VersionType.MAJOR : VersionType.MINOR,
											ecoData);
								}
							}

						} else {
							logger.warn("Product to impact is empty");
						}

						changeUnitDataItem.setTreated(Boolean.TRUE);
						changeUnitDataItem.setErrorMsg(null);

						if (!isSimulation) {
							if (logger.isDebugEnabled()) {
								logger.debug("Applied Treated to item "
										+ nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME));
							}
						}

						return null;

					};

					try {
						RunAsWork<Object> actionRunAs = () -> transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback,
								isSimulation, true);
						AuthenticationUtil.runAsSystem(actionRunAs);
					} catch (Exception e) {

						changeUnitDataItem.setTreated(false);
						changeUnitDataItem.setErrorMsg(e.getMessage());
						logger.warn(e, e);
						// Todo log better error
						logger.error("Error applying for " + nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME), e);

						return false;
					}

				}

			}

			if (!component.isLeaf()) {
				if (!visitChildrens(component, ecoData, isSimulation)) {
					return false;
				}
			}

		}

		return true;
	}

	private boolean shouldSkipCurrentBranch(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem) {

		boolean skip = false;
		for (WUsedListDataItem wulDataItem : ecoData.getWUsedList()) {
			if ((wulDataItem.getParent() != null) && wulDataItem.getParent().getIsWUsedImpacted()
					&& wulDataItem.getSourceItems().contains(changeUnitDataItem.getSourceItem())) {
				if ((ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)) == null)
						|| !ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)).getTreated()) {
					skip = true;
					break;
				}
			}

		}
		return skip;
	}

	private void applyReplacementList(ChangeOrderData ecoData, ProductData product) {

		if (ecoData.getReplacementList() != null) {

			applyToListV2(ecoData, product.getCompoList());
			applyToListV2(ecoData, product.getPackagingList());

		}

	}

	@SuppressWarnings("unchecked")
	private <T extends CompositionDataItem> void applyToListV2(ChangeOrderData ecoData, List<T> items) {

		Map<NodeRef, Set<Pair<NodeRef, Integer>>> replacements = new HashMap<>();
		Set<T> toDelete = new HashSet<>();
		for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {

			// Only reformulate SKIP
			if (!((replacementListDataItem.getSourceItems() != null) && (replacementListDataItem.getTargetItem() != null)
					&& (replacementListDataItem.getQtyPerc() != null) && (replacementListDataItem.getSourceItems().size() == 1)
					&& replacementListDataItem.getSourceItems().contains(replacementListDataItem.getTargetItem())
					&& (replacementListDataItem.getQtyPerc() == 100))) {

				// if rule match compoList
				if (items.stream().map(c -> c.getComponent()).collect(Collectors.toSet()).containsAll(replacementListDataItem.getSourceItems())) {
					boolean first = true;
					for (NodeRef sourceItem : replacementListDataItem.getSourceItems()) {

						if (toDelete.stream().map(c -> c.getComponent()).collect(Collectors.toSet()).contains(sourceItem)) {
							logger.warn("Cannot add rule: " + sourceItem + " deleted by another rule");
							break;
						}

						Set<Pair<NodeRef, Integer>> targetItems = replacements.get(sourceItem);
						if (first) {
							if (targetItems == null) {
								targetItems = new HashSet<>();
							}
							if (replacementListDataItem.getTargetItem() != null) {
								targetItems.add(
										new Pair<NodeRef, Integer>(replacementListDataItem.getTargetItem(), replacementListDataItem.getQtyPerc()));
							} else {
								toDelete.addAll(items.stream().filter(c -> sourceItem.equals(c.getComponent())).collect(Collectors.toSet()));
							}
							replacements.put(sourceItem, targetItems);

							first = false;
						} else {
							if ((targetItems == null) || targetItems.isEmpty()) {
								toDelete.addAll(items.stream().filter(c -> sourceItem.equals(c.getComponent())).collect(Collectors.toSet()));
							} else {
								logger.warn("Cannot delete target item: " + sourceItem + " used in another rule");
							}
						}

					}
				}
			}
		}
		items.removeAll(toDelete);

		for (Map.Entry<NodeRef, Set<Pair<NodeRef, Integer>>> replacement : replacements.entrySet()) {
			Set<T> components = items.stream().filter(c -> replacement.getKey().equals(c.getComponent())).collect(Collectors.toSet());
			if (components.size() > 0) {
				boolean first = true;
				for (Pair<NodeRef, Integer> target : replacement.getValue()) {

					if (first) {
						for (T component : components) {
							updateComponent(component, target.getFirst(), target.getSecond());
						}
						first = false;
					} else {
						T newCompoListDataItem = (T) components.iterator().next().clone();
						newCompoListDataItem.setNodeRef(null);
						updateComponent(newCompoListDataItem, target.getFirst(), target.getSecond());
						items.add(newCompoListDataItem);
					}

				}
			}

		}

	}

	private <T extends CompositionDataItem> void updateComponent(T component, NodeRef target, Integer qtyPerc) {
		component.setComponent(target);
		if (component instanceof CompoListDataItem) {
			if ((((CompoListDataItem) component).getQtySubFormula() != null) && (qtyPerc != null)) {
				((CompoListDataItem) component).setQtySubFormula((qtyPerc / 100) * ((CompoListDataItem) component).getQtySubFormula());
			}
		} else {

			if ((component.getQty() != null) && (qtyPerc != null)) {
				component.setQty((qtyPerc / 100) * component.getQty());
			}
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
					entityVersionService.createInitialVersion(productToImpact);
				}
			}
		}

		return productToImpact;
	}

	private NodeRef createNewProductVersion(final NodeRef productToImpact, VersionType versionType, ChangeOrderData ecoData) {

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(VersionBaseModel.PROP_VERSION_TYPE, versionType);
		properties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage("plm.ecm.apply.version.label", ecoData.getCode() + " - " + ecoData.getName()));

		return entityVersionService.createVersion(productToImpact, properties);

	}

	private void createCalculatedCharactValues(ChangeOrderData ecoData, ProductData sourceData) {

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Double sourceValue = getCharactValue(charactNodeRef, charactType, sourceData);
			if (logger.isDebugEnabled()) {
				logger.debug("create calculated charact: " + nodeService.getProperty(sourceData.getNodeRef(), ContentModel.PROP_NAME) + " - "
						+ charactNodeRef + " - sourceValue: " + sourceValue);
			}
			ecoData.getSimulationList().add(new SimulationListDataItem(null, sourceData.getNodeRef(), charactNodeRef, sourceValue, null));
		}

	}

	private void updateCalculatedCharactValues(ChangeOrderData ecoData, ProductData targetData) {

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Double targetValue = getCharactValue(charactNodeRef, charactType, targetData);
			for (SimulationListDataItem simulationListDataItem : ecoData.getSimulationList()) {
				if (simulationListDataItem.getCharact().equals(charactNodeRef)
						&& simulationListDataItem.getSourceItem().equals(targetData.getNodeRef())) {
					simulationListDataItem.setTargetValue(targetValue);
					if (logger.isDebugEnabled()) {
						logger.debug("calculated charact: " + nodeService.getProperty(targetData.getNodeRef(), ContentModel.PROP_NAME) + " - "
								+ charactNodeRef + " - sourceValue: " + simulationListDataItem.getSourceValue() + " - targetValue: " + targetValue);
					}
				}

			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("simList size: " + ecoData.getSimulationList().size());
		}
	}

	private void checkRequirements(ChangeUnitDataItem changeUnitDataItem, ProductData targetData) {

		RequirementType reqType = null;
		String reqDetails = null;

		if ((targetData.getCompoListView() != null) && (targetData.getReqCtrlList() != null)) {
			for (ReqCtrlListDataItem rcl : targetData.getReqCtrlList()) {

				RequirementType newReqType = rcl.getReqType();

				if (reqType == null) {
					reqType = newReqType;
				} else {

					if (RequirementType.Tolerated.equals(newReqType) && reqType.equals(RequirementType.Info)) {
						reqType = newReqType;
					} else if (RequirementType.Forbidden.equals(newReqType) && !reqType.equals(RequirementType.Forbidden)) {
						reqType = newReqType;
					}
				}

				if (reqDetails == null) {
					reqDetails = rcl.getReqMessage();
				} else {
					reqDetails += RepoConsts.LABEL_SEPARATOR;
					reqDetails += rcl.getReqMessage();
				}
			}
		}

		changeUnitDataItem.setReqType(reqType);
		changeUnitDataItem.setReqDetails(reqDetails);
	}

	@Deprecated
	private List<QName> evaluateWUsedAssociations(NodeRef targetAssocNodeRef) {
		List<QName> wUsedAssociations = new ArrayList<>();

		QName nodeType = nodeService.getType(targetAssocNodeRef);

		if (nodeType.isMatch(PLMModel.TYPE_RAWMATERIAL) || nodeType.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)
				|| nodeType.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) {

			wUsedAssociations.add(PLMModel.ASSOC_COMPOLIST_PRODUCT);
		} else if (nodeType.isMatch(PLMModel.TYPE_PACKAGINGMATERIAL) || nodeType.isMatch(PLMModel.TYPE_PACKAGINGKIT)) {
			wUsedAssociations.add(PLMModel.ASSOC_PACKAGINGLIST_PRODUCT);
		} else if (nodeType.isMatch(PLMModel.TYPE_RESOURCEPRODUCT)) {
			wUsedAssociations.add(MPMModel.ASSOC_PL_RESOURCE);
		}

		return wUsedAssociations;
	}

	@Deprecated
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

	@Deprecated
	private Double getCharactValue(NodeRef charactNodeRef, QName charactType, ProductData productData) {
		// TODO make more generic use an annotation instead
		if (charactType.equals(PLMModel.TYPE_COST)) {
			return getCharactValue(charactNodeRef, productData.getCostList());
		} else if (charactType.equals(PLMModel.TYPE_NUT)) {
			return getCharactValue(charactNodeRef, productData.getNutList());
		} else if (charactType.equals(PLMModel.TYPE_ING)) {
			return getCharactValue(charactNodeRef, productData.getIngList());
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

	@Override
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
