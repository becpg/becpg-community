/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.product.helper.CharactHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.L2CacheSupport.Action;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Engineering change order service implementation
 * 
 * @author quere
 * 
 */
public class ECOServiceImpl implements ECOService {

	private static final String VERSION_DESCRIPTION = "Applied by ECO %s";
	private static Log logger = LogFactory.getLog(ECOServiceImpl.class);

	private WUsedListService wUsedListService;

	private NodeService nodeService;
	private CheckOutCheckInService checkOutCheckInService;
	private TransactionService transactionService;

	private ProductService productService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public void doSimulation(NodeRef ecoNodeRef) {
		logger.debug("Run simulation");
		doRun(ecoNodeRef, ECOState.Simulated);
	}

	@Override
	public void apply(NodeRef ecoNodeRef) {
		logger.debug("Run apply");
		doRun(ecoNodeRef, ECOState.Applied);
	}

	private void doRun(NodeRef ecoNodeRef, final ECOState state) {

		final ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

		// Do not run if already applied
		if (!ECOState.Applied.equals(ecoData.getEcoState()) && !(ECOState.ToApply.equals(ecoData.getEcoState()) && ECOState.Simulated.equals(state))) {

			L2CacheSupport.doInCacheContext(new Action() {

				@Override
				public void run() {
					StopWatch watch = new StopWatch();
					if (logger.isDebugEnabled()) {
						watch.start();
					}

					// Clear changeUnitList
					List<ChangeUnitDataItem> toRemove = new ArrayList<>();
					for (ChangeUnitDataItem cul : ecoData.getChangeUnitList()) {
						if (Boolean.FALSE.equals(cul.getTreated())) {
							toRemove.add(cul);
						}
					}
					
					if(logger.isDebugEnabled()) {
						logger.debug("Remove "+toRemove.size()+ " previous changeUnit");
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
						for (ChangeUnitDataItem cul : ecoData.getChangeUnitList()) {
							cul.setTreated(Boolean.FALSE);
						}
						ecoData.setEcoState(state);
					} else if (hasError) {
						ecoData.setEcoState(ECOState.ToApply);
					} else {
						ecoData.setEffectiveDate(new Date());
						ecoData.setEcoState(ECOState.Applied);
					}

					// Change eco state

					if (logger.isDebugEnabled()) {
						watch.stop();
						logger.warn("Impact Where Used [" + state.toString() + "] executed in  " + watch.getTotalTimeSeconds() + " seconds");
					}
				}

			}, ECOState.Simulated.equals(state));

			alfrescoRepository.save(ecoData);

		}

	}

	@Override
	public void calculateWUsedList(NodeRef ecoNodeRef) {

		logger.debug("calculateWUsedList");

		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

		// Do not run if already applied
		if (!ECOState.Applied.equals(ecoData.getEcoState())) {

			// clear WUsedList
			ecoData.getWUsedList().clear();
			ecoData.getChangeUnitList().clear();

			if (ecoData.getReplacementList() != null) {

				for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {

					if (replacementListDataItem.getSourceItems() != null && replacementListDataItem.getSourceItems().size() > 0) {

						List<NodeRef> sourceList = new ArrayList<>(replacementListDataItem.getSourceItems());

						WUsedListDataItem parent = new WUsedListDataItem();
						parent.setSourceItems(sourceList);
						parent.setIsWUsedImpacted(true);
						// parent.setLink(replacementListDataItem.getNodeRef());

						ecoData.getWUsedList().add(parent);

						List<QName> associationQNames = evaluateWUsedAssociations(sourceList);

						for (QName associationQName : associationQNames) {

							MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(sourceList, associationQName, RepoConsts.MAX_DEPTH_LEVEL);

							QName datalistQName = evaluateListFromAssociation(associationQName);
							calculateWUsedList(ecoData, wUsedData, datalistQName, parent);
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

	private void calculateWUsedList(ChangeOrderData ecoData, MultiLevelListData wUsedData, QName dataListQName, WUsedListDataItem parent) {

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
			wUsedListDataItem.setIsWUsedImpacted(false);
			wUsedListDataItem.setSourceItems(kv.getValue().getEntityNodeRefs());

			ecoData.getWUsedList().add(wUsedListDataItem);

			// recursive
			calculateWUsedList(ecoData, kv.getValue(), dataListQName, wUsedListDataItem);
		}
	}

	private ChangeUnitDataItem getOrCreateChangeUnitDataItem(ChangeOrderData ecoData, WUsedListDataItem data) {

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
			if (RevisionType.Major.equals(revisionType) || (RevisionType.Minor.equals(revisionType) && RevisionType.NoRevision.equals(changeUnitDataItem.getRevision()))) {
				changeUnitDataItem.setRevision(revisionType);
			}
		}

		return changeUnitDataItem;
	}

	private boolean visitChildrens(Composite<WUsedListDataItem> composite, final ChangeOrderData ecoData, final boolean isSimulation) {


		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {

			// Not First level
			if (component.getData() != null && component.getData().getDepthLevel() > 1 && component.getData().getIsWUsedImpacted()) {

				final ChangeUnitDataItem changeUnitDataItem = getOrCreateChangeUnitDataItem(ecoData, component.getData());

				// We break if product treated
				if (changeUnitDataItem != null && !changeUnitDataItem.getTreated()) {

					// We test if all referring nodes are treated before
					// apply
					// to branch
					if (component.getData().getDepthLevel() > 2 && shouldSkipCurrentBranch(ecoData, changeUnitDataItem)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Skip current branch at " + nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME));
						}
						break;
					}

					final RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
						@Override
						public Object execute() {

							NodeRef productNodeRef = getProductToImpact(ecoData, changeUnitDataItem, isSimulation);

							if (productNodeRef != null) {

								ProductData productToFormulateData = (ProductData) alfrescoRepository.findOne(productNodeRef);

								if(isSimulation) {
									// Before formulate we create simulation List
									createCalculatedCharactValues(ecoData, productToFormulateData);
								}
								
								// Level 2
								if (component.getData().getDepthLevel() == 2) {
									applyReplacementList(ecoData, productToFormulateData);
								}
								// } else {
								// // Level 3 OR more
								// applyImpactedProductsData(ecoData,
								// changeUnitDataItem, productToFormulateData);
								// }

								try {
									productService.formulate(productToFormulateData);
								} catch (FormulateException e) {
									logger.warn("Failed to formulate product. NodeRef: " + productToFormulateData.getNodeRef(), e);
								}

								if(isSimulation) {
									// update simulation List
									updateCalculatedCharactValues(ecoData, productToFormulateData);
								}

								// check req
								checkRequirements(changeUnitDataItem, productToFormulateData);

								alfrescoRepository.save(productToFormulateData);

							} else {
								logger.warn("Product to impact is empty");
							}

							changeUnitDataItem.setTreated(Boolean.TRUE);

							if (!isSimulation) {
								if (logger.isDebugEnabled()) {
									logger.debug("Applied Treated to item " + nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME));
								}
							}
							

							return null;

						}

					};

					try {
						RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
							@Override
							public Object doWork() throws Exception {
								return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, isSimulation, true);
							}
						};
						AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
					} catch (Throwable e) {
						
						changeUnitDataItem.setTreated(false);
						//Todo log better error
						logger.error("Error applying for "+nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME),e);
						
						return false;
					}

				}

			}

			if (!component.isLeaf()) {
				if(!visitChildrens((Composite<WUsedListDataItem>) component, ecoData, isSimulation)) {
					return false;
				}
			}

		}

		return true;
	}

	private boolean shouldSkipCurrentBranch(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem) {

		boolean skip = false;
		for (WUsedListDataItem wulDataItem : ecoData.getWUsedList()) {
			if (wulDataItem.getParent() != null && wulDataItem.getParent().getIsWUsedImpacted() && wulDataItem.getSourceItems().contains(changeUnitDataItem.getSourceItem())) {
				if (ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)) == null
						|| !ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)).getTreated()) {
					skip = true;
					break;
				}
			}

		}
		return skip;
	}

	// private void applyImpactedProductsData(ChangeOrderData ecoData,
	// ChangeUnitDataItem changeUnitDataItem, ProductData product) {
	//
	// // TODO On doit également stopper la propagation si
	// // !wulDataItem.getIsWUsedImpacted()
	//
	// for (WUsedListDataItem wulDataItem : ecoData.getWUsedList()) {
	// if (wulDataItem.getParent() != null &&
	// wulDataItem.getSourceItems().contains(changeUnitDataItem.getSourceItem()))
	// {
	// ChangeUnitDataItem toApply =
	// ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0));
	// if (toApply.getTargetItem() != null) {
	// applyToList(product.getCompoList(), toApply.getSourceItem(),
	// toApply.getTargetItem());
	// applyToList(product.getPackagingList(), toApply.getSourceItem(),
	// toApply.getTargetItem());
	//
	// }
	//
	// }
	//
	// }
	//
	// }

	private <T extends CompositionDataItem> void applyToList(List<T> items, NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		for (T item : items) {
			if (item.getProduct() != null && item.getProduct().equals(sourceNodeRef)) {
				item.setProduct(targetNodeRef);
				break;
			}
		}

	}

	private void applyReplacementList(ChangeOrderData ecoData, ProductData product) {

		if (ecoData.getReplacementList() != null) {

			applyToList(ecoData, product.getCompoList());
			applyToList(ecoData, product.getPackagingList());

		}

	}

	@Deprecated
	// On doit passer par wused pour faire ça en utilisants les liens multiples
	// Et le premier lien vers la replacementList
	//
	private <T extends CompositionDataItem> void applyToList(ChangeOrderData ecoData, List<T> items) {
		Map<NodeRef, CompositionDataItem> temp = new HashMap<>();
		Set<NodeRef> toDelete = new HashSet<>();

		for (Iterator<T> iterator = items.iterator(); iterator.hasNext();) {
			T compoListDataItem = (T) iterator.next();

			for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {
				if (replacementListDataItem.getSourceItems() != null && !replacementListDataItem.getSourceItems().isEmpty()) {

					// TODO Can be retrieve by link from wusedList

					// Look if compo match with
					if (replacementListDataItem.getSourceItems().get(0).equals(compoListDataItem.getProduct())) {

						boolean apply = true;
						if (replacementListDataItem.getSourceItems().size() > 1) {

							for (int i = 1; i < replacementListDataItem.getSourceItems().size(); i++) {
								apply = false;
								for (Iterator<T> iterator2 = items.iterator(); iterator2.hasNext();) {
									CompositionDataItem compoListDataItem2 = (CompoListDataItem) iterator2.next();
									if (replacementListDataItem.getSourceItems().get(i).equals(compoListDataItem2.getProduct())) {
										apply = true;
										// DELETE
										toDelete.add(compoListDataItem2.getNodeRef());
									}
								}

								if (!apply) {
									break;
								}
							}

						}

						if (apply) {
							// ADD
							if (temp.containsKey(compoListDataItem.getProduct())) {
								T newCompoListDataItem = (T) compoListDataItem.createCopy();
								newCompoListDataItem.setNodeRef(null);
								newCompoListDataItem.setProduct(replacementListDataItem.getTargetItem());
								if (compoListDataItem.getQty() != null && replacementListDataItem.getQtyPerc() != null) {
									newCompoListDataItem.setQty(replacementListDataItem.getQtyPerc() / 100 * compoListDataItem.getQty());
								}
								items.add(newCompoListDataItem);
							} // UPDATE
							else {
								if (replacementListDataItem.getTargetItem() == null) {
									iterator.remove();
								} else {
									compoListDataItem.setProduct(replacementListDataItem.getTargetItem());
									if (compoListDataItem.getQty() != null && replacementListDataItem.getQtyPerc() != null) {
										compoListDataItem.setQty(replacementListDataItem.getQtyPerc() / 100 * compoListDataItem.getQty());
									}
									temp.put(compoListDataItem.getProduct(), compoListDataItem);
								}
							}
						}
					}

				}

			}
		}

		items.removeAll(toDelete);

	}

	private NodeRef getProductToImpact(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem, boolean isSimulation) {

		final NodeRef productToImpact = changeUnitDataItem.getSourceItem();

		if (productToImpact != null) {

			// Create a new revision if apply else use
			if (!isSimulation) {

				/*
				 * manage revision
				 */
				if (!changeUnitDataItem.getRevision().equals(RevisionType.NoRevision)) {


					if(!nodeService.hasAspect(productToImpact, ContentModel.ASPECT_VERSIONABLE)) {
						RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
							@Override
							public Object doWork() throws Exception {
								return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {

									@Override
									public NodeRef execute() throws Throwable {
										logger.debug("Add ASPECT_VERSIONABLE");
										Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
										aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
										nodeService.addAspect(productToImpact, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
										return productToImpact;
									}
									
								}, false, true);
							}
						};
						AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
						
						
						
						
					}
					
					VersionType versionType = changeUnitDataItem.getRevision().equals(RevisionType.Major) ? VersionType.MAJOR : VersionType.MINOR;
					
					logger.debug("Create new version :" + versionType);
					
					logger.debug("Checkout");
					// checkout
					NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(productToImpact);

					logger.debug("Checkin");
					// checkin
					Map<String, Serializable> properties = new HashMap<String, Serializable>();
					properties.put(VersionModel.PROP_VERSION_TYPE, versionType);
					properties.put(Version.PROP_DESCRIPTION, String.format(VERSION_DESCRIPTION, ecoData.getCode()));

					return checkOutCheckInService.checkin(workingCopyNodeRef, properties);
				}
			}
			// TODO mettre à jour les wUsedLink
		}

		return productToImpact;
	}

	private void createCalculatedCharactValues(ChangeOrderData ecoData, ProductData sourceData) {

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Double sourceValue = CharactHelper.getCharactValue(charactNodeRef, charactType, sourceData);
			if (logger.isDebugEnabled()) {
				logger.debug("create calculated charact: " + nodeService.getProperty(sourceData.getNodeRef(), ContentModel.PROP_NAME) + " - " + charactNodeRef + " - sourceValue: "
						+ sourceValue);
			}
			ecoData.getSimulationList().add(new SimulationListDataItem(null, sourceData.getNodeRef(), charactNodeRef, sourceValue, null));
		}

	}

	private void updateCalculatedCharactValues(ChangeOrderData ecoData, ProductData targetData) {

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Double targetValue = CharactHelper.getCharactValue(charactNodeRef, charactType, targetData);
			for (SimulationListDataItem simulationListDataItem : ecoData.getSimulationList()) {
				if (simulationListDataItem.getCharact().equals(charactNodeRef) && simulationListDataItem.getSourceItem().equals(targetData.getNodeRef())) {
					simulationListDataItem.setTargetValue(targetValue);
					if (logger.isDebugEnabled()) {
						logger.debug("calculated charact: " + nodeService.getProperty(targetData.getNodeRef(), ContentModel.PROP_NAME) + " - " + charactNodeRef
								+ " - sourceValue: " + simulationListDataItem.getSourceValue() + " - targetValue: " + targetValue);
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

		if (targetData.getCompoListView() != null && targetData.getCompoListView().getReqCtrlList() != null) {
			for (ReqCtrlListDataItem rcl : targetData.getCompoListView().getReqCtrlList()) {

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
		List<QName> wUsedAssociations = new ArrayList<QName>();

		QName nodeType = nodeService.getType(targetAssocNodeRef);

		if (nodeType.isMatch(PLMModel.TYPE_RAWMATERIAL) || nodeType.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
				|| nodeType.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) {

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

}
