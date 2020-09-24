/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.ECMGroup;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
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
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.repository.model.EffectiveDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.security.BeCPGAccessDeniedException;
import fr.becpg.repo.security.SecurityService;

/**
 * Engineering change order service implementation
 *
 * @author quere
 * @version $Id: $Id
 */
@Service("ecoService")
public class ECOServiceImpl implements ECOService {

	private static final Log logger = LogFactory.getLog(ECOServiceImpl.class);

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

	/** {@inheritDoc} */
	@Override
	public boolean doSimulation(NodeRef ecoNodeRef) {
		return doRun(ecoNodeRef, ECOState.Simulated);
	}

	/** {@inheritDoc} */
	@Override
	public boolean apply(NodeRef ecoNodeRef) {
		if (securityService.isCurrentUserAllowed(ECMGroup.ApplyChangeOrder.toString())) {
			return doRun(ecoNodeRef, ECOState.Applied);
		} else {
			throw new BeCPGAccessDeniedException(ECMGroup.ApplyChangeOrder.toString());
		}
	}

	private boolean doRun(NodeRef ecoNodeRef, final ECOState state) {

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

				checkMissingWUsed(composite);

				if (logger.isTraceEnabled()) {
					logger.trace("WUsedList to impact :" + composite.toString());
				}

				Set<String> errors = new HashSet<>();

				boolean hasError = !visitChildrens(composite, ecoData, ECOState.Simulated.equals(state), errors);

				if (ECOState.Simulated.equals(state)) {
					for (ChangeUnitDataItem cul2 : ecoData.getChangeUnitList()) {
						cul2.setTreated(Boolean.FALSE);
					}
					ecoData.setEcoState(state);
				} else if (hasError) {
					ecoData.setEcoState(ECOState.InError);
					StringBuilder comments = new StringBuilder();
					for (String error : errors) {
						comments.append(error + "</br>");
					}

					commentService.createComment(ecoData.getNodeRef(), "", comments.toString(), false);
				} else {
					if (!isFuture(ecoData)) {
						ecoData.setEffectiveDate(new Date());
					}
					ecoData.setEcoState(ECOState.Applied);
				}

				// Change eco state

				if (logger.isDebugEnabled()) {
					watch.stop();
					logger.warn("Impact Where Used [" + state.toString() + "] executed in  " + watch.getTotalTimeSeconds() + " seconds");
				}
			}, ECOState.Simulated.equals(state), true);

			alfrescoRepository.save(ecoData);

			if ((state != null) && state.equals(ecoData.getEcoState())) {
				return true;
			}

		}

		return false;

	}

	private void checkMissingWUsed(Composite<WUsedListDataItem> composite) {

		boolean childChecked = false;
		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {
			if (!component.isLeaf()) {
				checkMissingWUsed(component);
			}

			if (component.getData().getIsWUsedImpacted()) {
				childChecked = true;
			}
		}

		if ((composite.getData() != null) && childChecked) {
			composite.getData().setIsWUsedImpacted(true);
		}

	}

	/** {@inheritDoc} */
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

				int sort = 1;
				
				for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {

					List<NodeRef> replacements = getSourceItems(ecoData, replacementListDataItem);

					if ((replacements != null) && !replacements.isEmpty()) {

						WUsedListDataItem parent = new WUsedListDataItem();
						parent.setSourceItems(replacements);
						parent.setIsWUsedImpacted(true);
						parent.setDepthLevel(1);
						parent.setSort(sort++);
						
						// parent.setLink(replacementListDataItem.getNodeRef());

						ecoData.getWUsedList().add(parent);

						List<QName> associationQNames = evaluateWUsedAssociations(replacements);

						for (QName associationQName : associationQNames) {

							MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(replacements, WUsedOperator.AND, associationQName,
									RepoConsts.MAX_DEPTH_LEVEL);

							QName datalistQName = evaluateListFromAssociation(associationQName);
							sort = calculateWUsedList(ecoData, wUsedData, datalistQName, parent,
									ChangeOrderType.Merge.equals(ecoData.getEcoType()) ? true : isWUsedImpacted, sort);
						}
					}
				}
			}

			// change state
			ecoData.setEcoState(ECOState.WUsedCalculated);

			alfrescoRepository.save(ecoData);

		}
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
			boolean isWUsedImpacted, int sort) {

		for (Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()) {

			WUsedListDataItem wUsedListDataItem = new WUsedListDataItem();
			wUsedListDataItem.setParent(parent);
			wUsedListDataItem.setImpactedDataList(dataListQName);
			wUsedListDataItem.setIsWUsedImpacted(isWUsedImpacted);
			wUsedListDataItem.setSourceItems(kv.getValue().getEntityNodeRefs());
			wUsedListDataItem.setSort(sort++);

			ecoData.getWUsedList().add(wUsedListDataItem);

			// recursive
			sort = calculateWUsedList(ecoData, kv.getValue(), dataListQName, wUsedListDataItem, isWUsedImpacted, sort);
		}

		return sort;
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

	private boolean visitChildrens(Composite<WUsedListDataItem> composite, final ChangeOrderData ecoData, final boolean isSimulation,
			Set<String> errors) {

		int sort = 1;

		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {

			boolean isMergeItem = ChangeOrderType.Merge.equals(ecoData.getEcoType()) && (component.getData().getDepthLevel() == 1);

			// Not First level
			if ((component.getData() != null) && ((component.getData().getDepthLevel() > 1) || isMergeItem)
					&& component.getData().getIsWUsedImpacted()) {

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

					final int finalSort = sort++;
					final RetryingTransactionCallback<Object> actionCallback = () -> {

						NodeRef productNodeRef = getProductToImpact(ecoData, changeUnitDataItem, isSimulation);

						if (productNodeRef != null) {

							ProductData productToFormulateData = (ProductData) alfrescoRepository.findOne(productNodeRef);

							if (isSimulation) {
								// Before formulate we create simulation
								// List
								createCalculatedCharactValues(ecoData, productToFormulateData, finalSort);
							}

							// Level 2
							if ((component.getData().getDepthLevel() == 2) || isMergeItem) {
								applyReplacementList(ecoData, productToFormulateData, isSimulation, isMergeItem);
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
											ecoData, composite.getData());
								}
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

						return null;

					};

					try {
						RunAsWork<Object> actionRunAs = () -> transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback,
								isSimulation, true);
						AuthenticationUtil.runAsSystem(actionRunAs);
					} catch (Exception e) {
						Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
						if (validCause != null) {
							throw (RuntimeException) validCause;
						}

						changeUnitDataItem.setTreated(false);
						changeUnitDataItem.setErrorMsg(e.getMessage());
						errors.add("Change unit in Error: " + changeUnitDataItem.getNodeRef());
						errors.add("Error message: " + e.getMessage());

						try (StringWriter buffer = new StringWriter()) {
							try (PrintWriter printer = new PrintWriter(buffer)) {
								e.printStackTrace(printer);
							}
							errors.add("StackTrace : " + buffer.toString());
						} catch (IOException e1) {
							// Nothing can be done here

						}

						if (logger.isDebugEnabled()) {
							logger.debug("Error applying for: " + changeUnitDataItem.toString(), e);
						}

						return false;
					}

				}

			}

			if (!component.isLeaf() && component.getData().getIsWUsedImpacted()) {
				if (!visitChildrens(component, ecoData, isSimulation, errors)) {
					return false;
				}
			}

		}

		return true;
	}

	private boolean shouldSkipCurrentBranch(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem) {

		boolean skip = false;
		for (WUsedListDataItem wulDataItem : ecoData.getWUsedList()) {
			if (wulDataItem.getIsWUsedImpacted() && (wulDataItem.getParent() != null) && wulDataItem.getParent().getIsWUsedImpacted()
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

	@SuppressWarnings("unchecked")
	private <T extends CompositionDataItem> void applyToList(ChangeOrderData ecoData, ProductData productData, List<T> items) {

		Predicate<EffectiveDataItem> filter = null;

		boolean isFuture = isFuture(ecoData);

		if (!isFuture) {
			filter = (new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).createPredicate(productData);
		} else {
			filter = (new EffectiveFilters<>(ecoData.getEffectiveDate())).createPredicate(productData);
		}

		Map<NodeRef, Set<Pair<NodeRef, Integer>>> replacements = new HashMap<>();
		Set<T> toDelete = new HashSet<>();
		for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {

			// Only reformulate SKIP
			if (!((replacementListDataItem.getSourceItems() != null) && (replacementListDataItem.getTargetItem() != null)
					&& (replacementListDataItem.getQtyPerc() != null) && (replacementListDataItem.getSourceItems().size() == 1)
					&& replacementListDataItem.getSourceItems().contains(replacementListDataItem.getTargetItem())
					&& (replacementListDataItem.getQtyPerc() == 100))) {

				List<NodeRef> replacementsList = getSourceItems(ecoData, replacementListDataItem);

				// if rule match compoList
				if (items.stream().filter(filter).map(c -> c.getComponent()).collect(Collectors.toSet()).containsAll(replacementsList)) {
					boolean first = true;
					for (NodeRef sourceItem : replacementsList) {

						if (toDelete.stream().map(c -> c.getComponent()).collect(Collectors.toSet()).contains(sourceItem)) {
							logger.warn("Cannot add rule: " + sourceItem + " deleted by another rule");
							break;
						}

						Set<Pair<NodeRef, Integer>> targetItems = replacements.get(sourceItem);
						if (first) {
							if (targetItems == null) {
								targetItems = new HashSet<>();
							}
							if (ChangeOrderType.Merge.equals(ecoData.getEcoType())) {
								if (replacementListDataItem.getSourceItems() != null) {
									targetItems
											.add(new Pair<>(replacementListDataItem.getSourceItems().get(0), replacementListDataItem.getQtyPerc()));
								}
							} else {
								if (replacementListDataItem.getTargetItem() != null) {
									targetItems.add(new Pair<>(replacementListDataItem.getTargetItem(), replacementListDataItem.getQtyPerc()));
								} else {
									toDelete.addAll(items.stream().filter(filter).filter(c -> sourceItem.equals(c.getComponent()))
											.collect(Collectors.toSet()));
								}
							}
							replacements.put(sourceItem, targetItems);

							first = false;
						} else {
							if ((targetItems == null) || targetItems.isEmpty()) {
								toDelete.addAll(
										items.stream().filter(filter).filter(c -> sourceItem.equals(c.getComponent())).collect(Collectors.toSet()));
							} else {
								logger.warn("Cannot delete target item: " + sourceItem + " used in another rule");
							}
						}

					}
				}
			}
		}
		if (isFuture) {
			for (T item : items) {
				for (T itemToDelete : toDelete) {
					if (item.equals(itemToDelete)) {
						item.setEndEffectivity(ecoData.getEffectiveDate());
					}
				}
			}
		} else {
			items.removeAll(toDelete);
		}

		for (Map.Entry<NodeRef, Set<Pair<NodeRef, Integer>>> replacement : replacements.entrySet()) {
			Set<T> components = items.stream().filter(filter).filter(c -> replacement.getKey().equals(c.getComponent())).collect(Collectors.toSet());
			if (components.size() > 0) {
				boolean first = true;
				for (Pair<NodeRef, Integer> target : replacement.getValue()) {

					if (first && !isFuture) {
						for (T component : components) {
							updateComponent(component, target.getFirst(), target.getSecond());
						}
						first = false;
					} else {
						T origComponent = components.iterator().next();
						T newCompoListDataItem = (T) origComponent.clone();
						newCompoListDataItem.setNodeRef(null);
						updateComponent(newCompoListDataItem, target.getFirst(), target.getSecond());
						if (isFuture) {
							if (first) {
								origComponent.setEndEffectivity(ecoData.getEffectiveDate());
							}
							newCompoListDataItem.setStartEffectivity(ecoData.getEffectiveDate());
						}
						items.add(newCompoListDataItem);
					}

				}
			}

		}

	}

	private boolean isFuture(ChangeOrderData ecoData) {
		Date now = new Date();
		return (ecoData.getEffectiveDate() != null) && (ecoData.getEffectiveDate().getTime() > now.getTime());
	}

	private <T extends CompositionDataItem> void updateComponent(T component, NodeRef target, Integer qtyPerc) {
		component.setComponent(target);
		if (component instanceof CompoListDataItem) {
			if ((((CompoListDataItem) component).getQtySubFormula() != null) && (qtyPerc != null)) {

				Double newQty = (qtyPerc / 100d) * ((CompoListDataItem) component).getQtySubFormula();

				((CompoListDataItem) component).setQtySubFormula(newQty);
			}
		} else {

			if ((component.getQty() != null) && (qtyPerc != null)) {

				Double newQty = (qtyPerc / 100d) * component.getQty();

				component.setQty(newQty);
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

		return entityVersionService.createVersion(productToImpact, properties);

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

	/**
	 * <p>evaluateWUsedAssociations.</p>
	 *
	 * @param targetAssocNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<QName> evaluateWUsedAssociations(NodeRef targetAssocNodeRef) {
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public Boolean setInError(NodeRef ecoNodeRef, Exception e) {

		List<String> errors = new ArrayList<>();

		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
		Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
		if (validCause == null) {
			errors.add("OM in error ");
			errors.add("Error message: " + e.getMessage());

			try (StringWriter buffer = new StringWriter()) {
				try (PrintWriter printer = new PrintWriter(buffer)) {
					e.printStackTrace(printer);
				}
				errors.add("StackTrace : " + buffer.toString());
			} catch (IOException e1) {
				// Nothing can be done here

			}
		}

		if (!ECOState.InError.equals(ecoData.getEcoState())) {
			ecoData.setEcoState(ECOState.InError);

			if (!errors.isEmpty()) {
				StringBuilder comments = new StringBuilder();
				for (String error : errors) {
					comments.append(error + "</br>");
				}

				commentService.createComment(ecoData.getNodeRef(), "", comments.toString(), false);
			}

			alfrescoRepository.save(ecoData);
			return true;
		}

		return false;
	}

}
